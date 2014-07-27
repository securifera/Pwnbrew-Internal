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

package pwnbrew.misc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import javax.swing.SwingUtilities;
import pwnbrew.StubConfig;
import pwnbrew.log.LoggableException;
/**
 *
 *  
 */
final public class SSLUtilities {

    private static KeyStore theKeystore;
    private static final String NAME_Class = SSLUtilities.class.getSimpleName();
    private static final String KEYSTORE_NAME =  "keystore.jks"; 
    
//    //===============================================================
//    /**
//     * Create a SSL context
//     * @return
//     * @throws LoggableException 
//    */   
//    public static SSLContext createSSLContext() throws LoggableException {
//
//        SSLContext aContext = null;
//        try {
//
//            //Important, add trustmanager that trusts all certs
//            TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
//
//                @Override
//                public java.security.cert.X509Certificate[] getAcceptedIssuers() { 
//                    return null;
//                }
//
//                @Override
//                public void checkClientTrusted(X509Certificate[] certs, String authType) {}
//
//                @Override
//                public void checkServerTrusted(X509Certificate[] certs, String authType) {}
//            }};
//
//            aContext = SSLContext.getInstance("TLS");
//            aContext.init( null, trustAllCerts, new SecureRandom());
//
//        } catch (KeyManagementException | NoSuchAlgorithmException ex) {
//            throw new LoggableException(ex);
//        }
//
//        return aContext;
//    }

      //===============================================================
    /**
     * Create a SSL context
     * @return
     * @throws LoggableException 
    */   
    public static SSLContext createSSLContext() throws LoggableException {

        SSLContext aContext = null;
        StubConfig theConf = StubConfig.getConfig();
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
    
    //=======================================================================
    /**
     * Returns the keystore
     * @return 
     * @throws pwnbrew.log.LoggableException
    */
    public static synchronized KeyStore getKeystore() throws LoggableException {
       if(theKeystore == null)
          theKeystore = loadKeystore( StubConfig.getConfig());
       
       return theKeystore;
    }
    
     //========================================================================
    /**
     * Loads the keystore
     * @param theConf
     * @return
     * @throws pwnbrew.log.LoggableException
    */
    public static KeyStore loadKeystore( StubConfig theConf ) throws LoggableException {

        File libDir = new File( "." );
        File keyStoreFile = new File(libDir, KEYSTORE_NAME);
        KeyStore tempKeystore = null;
        boolean saveConf = false;
     
        try{

            //The keystore password
            String keyStorePass;

            //If the keystore path is empty create one
            if(!keyStoreFile.exists()){

               //Create a random keypass
               keyStorePass = Utilities.simpleEncrypt(Integer.toString( SocketUtilities.SecureRandomGen.nextInt()), Long.toString( SocketUtilities.SecureRandomGen.nextLong()));
               tempKeystore = createKeystore( keyStorePass);

               //Set the keypath and passphrase
               theConf.setKeyStorePass(keyStorePass);
               saveConf = true;

            } else {

               //Get the keystore pass
               keyStorePass = theConf.getKeyStorePass();

               //Load the keystore
               FileInputStream theFIS = new FileInputStream(keyStoreFile.getAbsolutePath());
               try {

                  tempKeystore = KeyStore.getInstance("JKS");                      
                  tempKeystore.load(theFIS, keyStorePass.toCharArray());

               } catch(IOException ex){

                    //Ensure the file stream is closed
                    try {
                        theFIS.close();
                    } catch (IOException ex1 ) {
                        ex1 = null;
                    }

                    DebugPrinter.printMessage( NAME_Class, "loadKeystore()", ex.getMessage(), ex);
                    if(ex.getMessage().contains("tampered")){

                        //Delete the keystore
                        if( keyStoreFile.delete() ){
                            //Try and load it again
                            try {
                                return SSLUtilities.loadKeystore(theConf);
                            } catch (LoggableException ex1) {    
                                ex1 = null;
                            }
                        }
                    }


               } finally {

                  //Ensure the file stream is closed
                  try {
                     theFIS.close();
                  } catch (IOException ex) {
                     ex = null;
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
            if( saveConf )
                theConf.writeSelfToDisk();
                
        }

        return tempKeystore;

    }
    
    //===============================================================
    /*
     * Check if the keystore has an entry for the alias
    */
    public static synchronized boolean checkAlias(KeyStore passedKeyStore, String passedAlias) throws KeyStoreException, LoggableException {

        boolean retVal = false;

        if( passedKeyStore != null && passedAlias != null )
           retVal = passedKeyStore.containsAlias(passedAlias);        

        return retVal;
    }
    
    //=======================================================================
    /**
     * Returns a file representing a java keystore
     * @param keystorePass
     * @return 
     * @throws pwnbrew.logging.LoggableException
    */
    public static KeyStore createKeystore( String keystorePass ) throws LoggableException {

        KeyStore tempKeystore = null;
        try {
            
            tempKeystore = KeyStore.getInstance("JKS");
            tempKeystore.load(null);

            //Save it
            saveKeyStore( tempKeystore, keystorePass ); 
            
        } catch( IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException ex ){
            throw new LoggableException(ex);
        }
        
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
            
            File libDir = new File(".");
            //Write the keystore back to disk
            File keyStore = new File(libDir, KEYSTORE_NAME);
            FileOutputStream theOS = new FileOutputStream(keyStore);
            try {
                
                passedKeyStore.store(theOS, keystorePass.toCharArray());
               
            } finally {
               //Close the file stream
                try{
                    theOS.close();
                } catch(IOException ex){
                    ex = null;
                }
            }
            
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
            //Generate a certificate
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
    
    //=======================================================================
    /**
     * Returns the certificate for the localhost
     * @return 
     * @throws java.security.KeyStoreException
     * @throws pwnbrew.log.LoggableException
    */
    public static Certificate getCertificate() throws KeyStoreException, LoggableException {

        Certificate theCert = null;
        KeyStore localKeyStore = getKeystore();
        if(localKeyStore != null){
            //Get the alias
            String theAlias = StubConfig.getConfig().getAlias();
            theCert = localKeyStore.getCertificate(theAlias);
        }
        return theCert;
    }
    
    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException {
        
//        //Assign the service name
//        if( args.length > 1 ) {
//            
//            //Get the first options
//            String firstOption = args[0];
//            String firstValue = args[1];
//            
//            File certFile = new File(firstValue);
//            if( certFile.exists() ){
//                switch(firstOption){
//                    case "-export":                    
//                        //Pass the status up to the manager
//                        try {
//                            Certificate theCert = SSLUtilities.getCertificate();
//
//                            //If a cert is returned then send it to the client
//                            if(theCert != null){
//                                byte[] certBytes = theCert.getEncoded();
//                                try (FileOutputStream aFOS = new FileOutputStream(certFile)) {
//                                    aFOS.write(certBytes);
//                                    aFOS.flush();
//                                }
//                            }
//                        } catch(KeyStoreException | CertificateEncodingException | LoggableException ex ){
//                            System.out.println(ex.getMessage());
//                        }
//                        return;
//                     case "-import":                    
//                        //Pass the status up to the manager
//                        try {
//                         
//                            //If a cert is returned then send it to the client                            
//                            byte[] certBytes = new byte[(int)certFile.length()];
//                            try (FileInputStream aFOS = new FileInputStream(certFile)) {
//                                aFOS.read(certBytes);                              
//                            }
//                            
//                            //Create a cert from the bytes
//                            Certificate aCert = new sun.security.x509.X509CertImpl( certBytes );
//                            SSLUtilities.importCertificate( "", aCert);
//                            
//                        } catch( CertificateException | LoggableException ex ){
//                            System.out.println(ex.getMessage());
//                        }
//                        return;
//                    default:
//                        break;
//                }
//            }
//        
//        } 
//                
//        //Print Usage
//        printUsage();
        /* Create and display the form */
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new SSLJFrame().setVisible(true);
            }
        });
        
    }
        
//    //========================================================================
//    /**
//     * Print stager info
//     */
//    private static void printUsage(){
//        StringBuilder aSB = new StringBuilder();
//        aSB.append("Usage: java -cp <this jar> pwnbrew.misc.SSLUtilities\n")
//                .append(" -h\tPrint usage\n")
//                .append(" -import <DER Encoded Certificate Filename>\n")
//                .append(" -export <DER Encoded Certificate Filename>\n");
//                
//        System.out.println(aSB.toString());
//    }
    
       
    //====================================================================
    /**
     * Returns the certificate for the localhost
     * @param passedAlias
     * @param certificate
     * @return 
     * @throws pwnbrew.log.LoggableException
    */
    public static synchronized boolean importCertificate(String passedAlias, Certificate certificate) throws LoggableException {

        boolean retVal = false;
        try {
            
            KeyStore localKeyStore = getKeystore();
            if(certificate != null && localKeyStore != null){
                localKeyStore.setCertificateEntry(passedAlias, certificate);

                //Write it to disk
                StubConfig theConf = StubConfig.getConfig();
                String keyStorePass = theConf.getKeyStorePass();

                //Set path to write to
                File libDir = new File( "." );
                File keyStore = new File(libDir, KEYSTORE_NAME);

                try ( //Write the keystore back to disk
                    FileOutputStream theOS = new FileOutputStream(keyStore)) {
                    localKeyStore.store(theOS, keyStorePass.toCharArray());
                    retVal = true;
                }
            }
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException ex) {
            throw new LoggableException(ex);
        }

       return retVal;
    }
  
}/* END CLASS SSLUtilities */
