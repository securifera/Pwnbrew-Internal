/*

Copyright (C) 2013-2014, Securifera, Inc 

All rights reserved. 

Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice,
	this list of conditions and the following disclaimer.

    * Redistributions in binary form must reproduce the above copyright notice,
	this list of conditions and the following disclaimer in the documentation 
	and/or other materials provided with the distribution.

    * Neither the name of Securifera, Inc nor the names of its contributors may be 
	used to endorse or promote products derived from this software without specific
	prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS 
OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY 
AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER 
OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON 
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

================================================================================

Pwnbrew is provided under the 3-clause BSD license above.

The copyright on this package is held by Securifera, Inc

*/


/*
* SSLUtilities.java
*
* Created on July 20, 2013, 6:50:37 PM
*/

package pwnbrew.utilities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import pwnbrew.Persistence;
import pwnbrew.logging.Log;
import pwnbrew.logging.LoggableException;
import pwnbrew.misc.X509CertificateFactory;
import pwnbrew.xmlBase.ServerConfig;
/**
 *
 *  
 */
final public class SSLUtilities {

    private static final String NAME_Class = "SSLUtilities";
    private static KeyStore theKeystore;
    private static SSLContext theSSLContext = null;
    
//    private static final String KEYSTORE_NAME =  "keystore.jks";    
    
    //===============================================================
    /**
     * Create a SSL context
     * @return
     * @throws LoggableException 
    */   
    private static SSLContext createSSLContext() throws LoggableException {

        SSLContext aContext = null;

        ServerConfig theConf = ServerConfig.getServerConfig();
        KeyStore theKeyStore = SSLUtilities.getKeystore();
        String keyStorePass = theConf.getKeyStorePass();

        if(theKeyStore != null && !keyStorePass.isEmpty()){

            try {
                
                KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
                kmf.init(theKeyStore, keyStorePass.toCharArray());

                TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
                tmf.init(theKeyStore);

                aContext = SSLContext.getInstance("TLS");
                aContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

            } catch (    KeyManagementException | KeyStoreException | UnrecoverableKeyException | NoSuchAlgorithmException ex) {
                throw new LoggableException(ex);
            }

        }

        return aContext;
    }

    //===============================================================
    /**
     *  Returns the keystore
     * @return
     * @throws LoggableException 
     */
    public static synchronized KeyStore getKeystore() throws LoggableException {
       if(theKeystore == null){
          theKeystore = loadKeystore( ServerConfig.getServerConfig() );
       }
       return theKeystore;
    }

    //****************************************************************************
    /**
     * Loads the keystore from the configuration in the conf file
     * @param theConf
     * @return 
     * @throws pwnbrew.logging.LoggableException 
    */
    public static KeyStore loadKeystore( ServerConfig theConf ) throws LoggableException {

        KeyStore tempKeystore = null;
        boolean saveConf = false;     

        try{

            //The keystore password
            String keyStorePass;
            List<byte[]> theConfEntries = Persistence.getLabelBytes( Persistence.SSL_CHUNK );
            if( theConfEntries.isEmpty() ){    
                
                //Create a random keypass
                keyStorePass = Utilities.simpleEncrypt(Integer.toString(Utilities.SecureRandomGen.nextInt()), Long.toString(Utilities.SecureRandomGen.nextLong()));
                tempKeystore = createKeystore( keyStorePass );

                //Set the keypath and passphrase
                theConf.setKeyStorePass(keyStorePass);
                saveConf = true;

            } else {

                //Get the keystore pass
                keyStorePass = theConf.getKeyStorePass();
                
                //Loop through the return entries
                for( byte[] theBytes : theConfEntries ){
                
                    ByteArrayInputStream theBIS = new ByteArrayInputStream( theBytes );
                    try {

                        //Load the keystore
                        tempKeystore = KeyStore.getInstance("JKS");                      
                        tempKeystore.load(theBIS, keyStorePass.toCharArray());
                        break;

                    } catch(IOException ex){

                        //Ensure the file stream is closed
                        try {
                            theBIS.close();
                        } catch (IOException ex1 ) {
                            ex1 = null;
                        }

                        //Log it
                        Log.log(Level.INFO, NAME_Class, "loadKeystore()", ex.getMessage(), ex);

                        //If the keystore password doesn't work then create a new one
                        if(ex.getMessage().contains("tampered")){

                            //Delete the keystore
                            Persistence.removeLabel( Persistence.SSL_CHUNK );
                            //Try and load it again
                            try {
                                return SSLUtilities.loadKeystore(theConf);
                            } catch (LoggableException ex1) {    
                                ex1 = null;
                            }
                            
                        }

                    } finally {

                        //Ensure the file stream is closed
                        try {
                            theBIS.close();
                        } catch (IOException ex) {
                            ex = null;
                        }
                    }
                }

            }

            String theAlias = theConf.getAlias();
            //If the alias is not set then it hasn't been replaced
            if(theAlias.isEmpty()){

                //Try and get the hostname
                String hostname = SocketUtilities.getHostname();

                //Get the new alias and set it
                theAlias = new StringBuilder().append(hostname)
                   .append("_").append(SocketUtilities.getNextId()).toString();

                theConf.setAlias(theAlias);
                saveConf = true;

            }

            //Check that the host alias has a certificate
            if(!checkAlias(tempKeystore, theAlias)){
               String distName = "CN=lisle.net, O=RSA, L=Snailville, S=CA, C=USA";
               String issuerName = "CN=a.gov, O=sfsefse, L=sefesf, S=CA, C=USA";
               createSelfSignedCertificate(distName, issuerName, 365, tempKeystore, keyStorePass, theAlias);
            }

        } catch (NoSuchAlgorithmException | CertificateException | KeyStoreException | IOException ex) {
           throw new LoggableException(ex);
        } finally {

            //Write to disk if needed
            if( saveConf ){
                theConf.writeSelfToDisk();
            }    
        }

        return tempKeystore;

    }

    //****************************************************************************
    /**
     *  * Returns a file representing a java keystore
     * 
     * @param keystorePass
     * @return
     * @throws KeyStoreException
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws CertificateException
     * @throws LoggableException 
    */    
    private static KeyStore createKeystore( String keystorePass ) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, LoggableException {

        KeyStore tempKeystore = KeyStore.getInstance("JKS");
        tempKeystore.load(null);

        //Save it
        saveKeyStore( tempKeystore, keystorePass );      
        
        return tempKeystore;
    }

    //===============================================================
    /**
     *  Saves the passed keystore
     * 
     * @param passedKeyStore
     * @param keystorePass
     * @return 
     */
    private static void saveKeyStore( KeyStore passedKeyStore, String keystorePass ) throws LoggableException{
        
        //Write the keystore back to disk        
        try {
            
            ByteArrayOutputStream theOS = new ByteArrayOutputStream();
            try {    
                //Store the keystore in the byte array stream
                passedKeyStore.store(theOS, keystorePass.toCharArray());            
            } finally {            
                try { theOS.close(); } catch (IOException ex) { ex = null; }            
            }

            //Get the bytes 
            byte[] objectBytes = theOS.toByteArray();
            Persistence.writeLabel( Persistence.SSL_CHUNK, objectBytes);
            
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException ex) {
           throw new LoggableException(ex);
        }
        
    }
    
    //===========================================================================
    /**
     * Returns a file representing a java keystore
    */
    private static void createSelfSignedCertificate(String subjectDN, String issuerDN, int days, KeyStore passedKeyStore, String keystorePass, String hostAlias ) throws LoggableException {
     
        try {
            Object[] theObjArr = X509CertificateFactory.generateCertificate( subjectDN, issuerDN, days, "RSA", 2048 );
            Key theKey = (Key) theObjArr[0];
            Certificate newCert = (Certificate) theObjArr[1];

            passedKeyStore.setKeyEntry(hostAlias, theKey, keystorePass.toCharArray(), new Certificate[]{newCert});

            //Save it
            saveKeyStore( passedKeyStore, keystorePass ); 

        } catch (KeyStoreException | CertificateException | InvalidKeyException | NoSuchAlgorithmException | NoSuchProviderException | SignatureException | IOException ex) {
           throw new LoggableException(ex);
        }
    }


    //===============================================================
    /**
     * Returns the certificate for the localhost
     * @return 
     * @throws java.security.KeyStoreException 
     * @throws pwnbrew.logging.LoggableException 
    */
    public static Certificate getCertificate() throws KeyStoreException, LoggableException {

       Certificate theCert = null;
       ServerConfig theConf = ServerConfig.getServerConfig();
       String theAlias = theConf.getAlias();

       KeyStore localKeyStore = getKeystore();
       if(localKeyStore != null){
          theCert = localKeyStore.getCertificate(theAlias);
       }
       
       return theCert;
    }


    //===============================================================
    /*
     * Check if the keystore has an entry for the alias
    */
    public static synchronized boolean checkAlias(KeyStore passedKeyStore, String passedAlias) throws KeyStoreException, LoggableException {

        boolean retVal = false;

        if( passedKeyStore != null && passedAlias != null ){
           retVal = passedKeyStore.containsAlias(passedAlias);
        }

        return retVal;
    }
    
    //===============================================================
    /**
     *  Reloads the SSL context
     *
     * @throws pwnbrew.logging.LoggableException
    */
    public static synchronized void reloadSSLContext() throws LoggableException {
        theSSLContext = createSSLContext();
    }
    
    //===============================================================
    /**
     * Returns the SSL context for the comm
     *
     * @return
     * @throws pwnbrew.logging.LoggableException
     */
    public static synchronized SSLContext getSSLContext() throws LoggableException {

        //If the context has not be created than create it
        if ( theSSLContext == null ){               
            theSSLContext = createSSLContext();        
        }

        return theSSLContext;
    }
    
    //===============================================================
    /**
     * Replaces the Public/Private entries with ones imported from a PKCS12
     * @param passedFile
     * @param passedPW
     * @return 
     * @throws pwnbrew.logging.LoggableException 
    */
    public static synchronized boolean importPKCS12Keystore( File passedFile, char[] passedPW ) throws LoggableException {

        boolean retVal = false;
        try {

            KeyStore localKeystore = getKeystore();
            if(passedFile != null && localKeystore != null){

                //Get the pass to the local keystore
                ServerConfig theConf = ServerConfig.getServerConfig();
                char[] keyStorePassArr = theConf.getKeyStorePass().toCharArray();

                //Write it to disk
                String localAlias = theConf.getAlias();

                KeyStore fileKeystore = KeyStore.getInstance("PKCS12");
                FileInputStream aFIS = new FileInputStream(passedFile);

                try {

                    fileKeystore.load(aFIS, passedPW); 

                    //Get all of the aliases
                    Enumeration<String> keystoreAliases = fileKeystore.aliases();
                    while( keystoreAliases.hasMoreElements() ){
                        String anAlias = keystoreAliases.nextElement();

                        //If the entry is a key entry
                        if(fileKeystore.isKeyEntry(anAlias)){
                            Key thePrivKey = fileKeystore.getKey(anAlias, passedPW);
                            Certificate[] theCertChain = fileKeystore.getCertificateChain(anAlias);

                            //Create a unique alias so that the key is different
                            if(anAlias.equals(localAlias)){

                                String hostname = localAlias.split("_")[0];
                                anAlias = new StringBuilder().append(hostname)
                                .append("_").append(SocketUtilities.getNextId())
                                .toString();
                            }

                            //Delete local entry
                            localKeystore.deleteEntry(localAlias); 

                            //Store the cert
                            theConf.setAlias(anAlias);
                            localKeystore.setKeyEntry(anAlias, thePrivKey, keyStorePassArr, theCertChain);
                            break;
                        }                    
                    }

                    //Save the keystore
                    SSLUtilities.saveKeyStore(localKeystore, new String(keyStorePassArr));
                    retVal = true;

                } catch (IOException ex){

                    if(ex.getMessage().contains("failed to decrypt"))
                        throw new IllegalArgumentException("Incorrect password. Import aborted.");
                    
                    retVal = false;

                } finally {
                    //Close the file
                    try {
                        aFIS.close();
                    } catch (IOException ex){
                        ex = null;
                    } 
                }
            }
        } catch (UnrecoverableKeyException | KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException ex) {
            throw new LoggableException(ex);
        }

        return retVal;
    }

    //==========================================================================
    /**
     *  Wrapper for creating a self signed certificate
     * 
     * @param issueeDN
     * @param sueeDN
     * @param issuerDN
     * @param suerDN
     * @param days 
     * @throws pwnbrew.logging.LoggableException 
     */
    public static void createSelfSignedCertificate(String issueeDN, String issuerDN, int days) throws LoggableException {
        
        //Get the pass to the local keystore
        ServerConfig theConf = ServerConfig.getServerConfig();
        char[] keyStorePassArr = theConf.getKeyStorePass().toCharArray();
                
        KeyStore theKeyStore = getKeystore();
        String theAlias = theConf.getAlias();
        createSelfSignedCertificate(issueeDN, issuerDN, days, theKeyStore, new String(keyStorePassArr), theAlias );
    }

}/* END CLASS SSLUtilities */
