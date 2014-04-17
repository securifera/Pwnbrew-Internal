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
*/

package pwnbrew.misc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import pwnbrew.Persistence;
import pwnbrew.StubConfig;
import pwnbrew.log.LoggableException;
/**
 *
 *  
 */
final public class SSLUtilities {

//    private static final String NAME_Class = "SSLUtilities";
//    private static KeyStore theKeystore;
    
    //===============================================================
    /**
     * Create a SSL context
     * @return
     * @throws LoggableException 
    */   
    public static SSLContext createSSLContext() throws LoggableException {

        SSLContext aContext = null;
        try {

            //Important, add trustmanager that trusts all certs
            TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {

                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() { 
                    return null;
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {}

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {}
            }};

            aContext = SSLContext.getInstance("TLS");
            aContext.init( null, trustAllCerts, new SecureRandom());

        } catch (KeyManagementException | NoSuchAlgorithmException ex) {
            throw new LoggableException(ex);
        }

        return aContext;
    }

//    //===============================================================
//    /**
//     *  Returns the keystore
//     * @return
//     * @throws LoggableException 
//     */
//    public static synchronized KeyStore getKeystore() throws LoggableException {
//       if(theKeystore == null){
//          theKeystore = loadKeystore( StubConfig.getConfig() );
//       }
//       return theKeystore;
//    }

//    //===============================================================
//    /**
//     * Loads the keystore from the configuration in the conf file
//     * @param theConf
//     * @return 
//     * @throws pwnbrew.log.LoggableException 
//    */
//    public static KeyStore loadKeystore( StubConfig theConf ) throws LoggableException {
//
//        KeyStore tempKeystore = null;
//        boolean saveConf = false;     
//
//        try{
//
//            //The keystore password
//            String keyStorePass;
//            List<byte[]> theConfEntries = Persistence.getLabelBytes( Persistence.SSL_CHUNK );
//            if( theConfEntries.isEmpty() ){    
//                
//                //Create a random keypass
//                keyStorePass = Utilities.simpleEncrypt(Integer.toString(SocketUtilities.SecureRandomGen.nextInt()), Long.toString(SocketUtilities.SecureRandomGen.nextLong()));
//                tempKeystore = createKeystore( keyStorePass );
//
//                //Set the keypath and passphrase
//                theConf.setKeyStorePass(keyStorePass);
//                saveConf = true;
//
//            } else {
//
//                //Get the keystore pass
//                keyStorePass = theConf.getKeyStorePass();
//                
//                //Loop through the return entries
//                for( byte[] theBytes : theConfEntries ){
//                
//                    ByteArrayInputStream theBIS = new ByteArrayInputStream( theBytes );
//                    try {
//
//                        //Load the keystore
//                        tempKeystore = KeyStore.getInstance("JKS");                      
//                        tempKeystore.load(theBIS, keyStorePass.toCharArray());
//                        break;
//
//                    } catch(IOException ex){
//
//                        //Ensure the file stream is closed
//                        try {
//                            theBIS.close();
//                        } catch (IOException ex1 ) {
//                            ex1 = null;
//                        }
//
//                        //Log it
//                        DebugPrinter.printMessage( NAME_Class, "loadKeystore", ex.getMessage(), ex);
//
//                        //If the keystore password doesn't work then create a new one
//                        if(ex.getMessage().contains("tampered")){
//
//                            //Delete the keystore
//                            Persistence.removeLabel( Persistence.SSL_CHUNK );
//                            //Try and load it again
//                            try {
//                                return SSLUtilities.loadKeystore(theConf);
//                            } catch (LoggableException ex1) {    
//                                ex1 = null;
//                            }
//                            
//                        }
//
//                    } finally {
//
//                        //Ensure the file stream is closed
//                        try {
//                            theBIS.close();
//                        } catch (IOException ex) {
//                            ex = null;
//                        }
//                    }
//                }
//
//            }
//
//            String theAlias = theConf.getAlias();
//            //If the alias is not set then it hasn't been replaced
//            if(theAlias == null || theAlias.isEmpty()){
//
//                //Try and get the hostname
//                String hostname = SocketUtilities.getHostname();
//
//                //Get the new alias and set it
//                theAlias = new StringBuilder().append(hostname)
//                   .append("_").append(SocketUtilities.getNextId()).toString();
//
//                theConf.setAlias(theAlias);
//                saveConf = true;
//
//            }
//
//            //Check that the host alias has a certificate
//            if(!checkAlias(tempKeystore, theAlias)){
//               createSelfSignedCertificate(tempKeystore, keyStorePass, theAlias);
//            }
//
//        } catch (NoSuchAlgorithmException | CertificateException | KeyStoreException | IOException ex) {
//           throw new LoggableException(ex);
//        } finally {
//
//            //Write to disk if needed
//            if( saveConf ){
//                theConf.writeSelfToDisk();
//            }    
//        }
//
//        return tempKeystore;
//
//    }

//    //===============================================================
//    /**
//     *  * Returns a file representing a java keystore
//     * 
//     * @param keystorePass
//     * @return
//     * @throws KeyStoreException
//     * @throws IOException
//     * @throws NoSuchAlgorithmException
//     * @throws CertificateException
//     * @throws LoggableException 
//    */    
//    private static KeyStore createKeystore( String keystorePass ) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, LoggableException {
//
//        KeyStore tempKeystore = KeyStore.getInstance("JKS");
//        tempKeystore.load(null);
//
//        //Save it
//        saveKeyStore( tempKeystore, keystorePass );      
//        
//        return tempKeystore;
//    }

//    //===============================================================
//    /**
//     *  
//     * 
//     * @param passedKeyStore
//     * @param keystorePass
//     * @return 
//     */
//    private static void saveKeyStore( KeyStore passedKeyStore, String keystorePass ) throws LoggableException{
//        
//        //Write the keystore back to disk        
//        try {
//            
//            ByteArrayOutputStream theOS = new ByteArrayOutputStream();
//            try {    
//                //Store the keystore in the byte array stream
//                passedKeyStore.store(theOS, keystorePass.toCharArray());            
//            } finally {            
//                try { theOS.close(); } catch (IOException ex) { ex = null; }            
//            }
//
//            //Get the bytes 
//            byte[] objectBytes = theOS.toByteArray();
//            Persistence.writeLabel( Persistence.SSL_CHUNK, objectBytes);
//            
//        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException ex) {
//           throw new LoggableException(ex);
//        }
//        
//    }
//    
//    //===============================================================
//    /**
//     * Returns a file representing a java keystore
//    */
//    private static void createSelfSignedCertificate(KeyStore passedKeyStore, String keystorePass, String hostAlias ) throws LoggableException {
//     
//        String distName = "CN=PWN, OU=PLACE, O=ORG, L=CITY, S=STATE, C=COUNTRY";
//
//        try {
//            Object[] theObjArr = X509CertificateFactory.generateCertificate( distName, 365, "RSA", 2048 );
//            Key theKey = (Key) theObjArr[0];
//            Certificate newCert = (Certificate) theObjArr[1];
//
//            passedKeyStore.setKeyEntry(hostAlias, theKey, keystorePass.toCharArray(), new Certificate[]{newCert});
//
//            //Save it
//            saveKeyStore( passedKeyStore, keystorePass ); 
//
//        } catch (KeyStoreException | CertificateException | InvalidKeyException | NoSuchAlgorithmException | NoSuchProviderException | SignatureException | IOException ex) {
//           throw new LoggableException(ex);
//        }
//    }

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

}/* END CLASS SSLUtilities */
