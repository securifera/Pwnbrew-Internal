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
import java.io.FileOutputStream;
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
import java.security.cert.X509Certificate;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
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

    //===============================================================
    /**
     * Create a SSL context
     * @return
     * @throws LoggableException 
    */   
    public static SSLContext createSSLContext() throws LoggableException {

        SSLContext aContext = null;
        KeyStore theKeyStore = SSLUtilities.getKeystore();
        String keyStorePass = StubConfig.MALTEGO_CERT_PW;

        if(theKeyStore != null && !keyStorePass.isEmpty()){

            try {
                
                KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
                kmf.init(theKeyStore, keyStorePass.toCharArray());

                //Important, add trustmanager that trusts all certs
                TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {

                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() { 
                        return new X509Certificate[0];
                    }

                    @Override
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}

                    @Override
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }};
                
                aContext = SSLContext.getInstance("TLS");
                aContext.init(kmf.getKeyManagers(), trustAllCerts, null);

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
          theKeystore = loadKeystore();
       
       return theKeystore;
    }
    
     //========================================================================
    /**
     * Loads the keystore
     * @return
     * @throws pwnbrew.log.LoggableException
    */
    public static KeyStore loadKeystore() throws LoggableException {

        File libDir = new File( "." );
        File keyStoreFile = new File(libDir, KEYSTORE_NAME);
        KeyStore tempKeystore = null;
     
        try{

            //The keystore password
            String keyStorePass = StubConfig.MALTEGO_CERT_PW;

            //If the keystore path is empty create one
            if(!keyStoreFile.exists()){

               //Create a random keypass
               tempKeystore = createKeystore( keyStorePass);

            } else {

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

               } finally {

                  //Ensure the file stream is closed
                  try {
                     theFIS.close();
                  } catch (IOException ex) {
                     ex = null;
                  }
               }

            }

            String theAlias = StubConfig.MALTEGO_CERT_ALIAS;

            //Check that the host alias has a certificate
            if(!checkAlias(tempKeystore, theAlias)){
                
                String hostname = Utilities.nextString();
                String issueOrg = Utilities.nextString();
                
                String distName = "CN="+ hostname +".com, O="+issueOrg+", L=San Francisco, S=CA, C=US";
                createSelfSignedCertificate(distName, distName, 365, tempKeystore, keyStorePass, theAlias);
            }

        } catch (NoSuchAlgorithmException | CertificateException | KeyStoreException | IOException ex) {
           throw new LoggableException(ex);
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
     * @throws pwnbrew.log.LoggableException
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
            String theAlias = StubConfig.MALTEGO_CERT_ALIAS;
            theCert = localKeyStore.getCertificate(theAlias);
        }
        return theCert;
    }
    
    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException {
          
        final boolean install;
        if( args.length > 0 ){
            String anArg = args[0];
            install = anArg.equals("-install");            
        } else
            install = false;
        
        String lookAndFeelClassStr = "javax.swing.plaf.metal.MetalLookAndFeel";
        if( Utilities.isWindows() )
            lookAndFeelClassStr = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
        
        try{
            UIManager.setLookAndFeel( lookAndFeelClassStr );
        } catch ( ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
        }
   
        /* Create and display the form */
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                SSLJFrame aFrame = new SSLJFrame();
                if( !install ){
                    aFrame.setVisible(true);
                } else {
                    aFrame.exportCertificate();
                    aFrame.dispose();
                }
            }
        });
        
    }
       
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
                
                localKeyStore.setEntry( passedAlias, new KeyStore.TrustedCertificateEntry( certificate ), null);

                //Write it to disk
                String keyStorePass = StubConfig.MALTEGO_CERT_PW;

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
