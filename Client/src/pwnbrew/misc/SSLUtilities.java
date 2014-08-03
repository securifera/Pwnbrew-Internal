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
* Created on Nov 2, 2013, 10:11:23 PM
*/

package pwnbrew.misc;

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
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import pwnbrew.log.LoggableException;
/**
 *
 *  
 */
final public class SSLUtilities {

    private static final String NAME_Class = "SSLUtilities";
    private static KeyStore theKeystore;
    
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
                    return new X509Certificate[0];
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

    //===============================================================
    /**
     *  Returns the keystore
     * @return
     * @throws LoggableException 
     */
    public static synchronized KeyStore getKeystore() throws LoggableException {
        if(theKeystore == null)
            theKeystore = loadKeystore();

        return theKeystore;
    }

    //===============================================================
    /**
     * Loads the keystore from the configuration in the conf file
     * @return 
     * @throws pwnbrew.log.LoggableException 
    */
    public static KeyStore loadKeystore() throws LoggableException {

        KeyStore tempKeystore = null;
        try{
            
            tempKeystore = KeyStore.getInstance("JKS");
            tempKeystore.load(null);

            //Try and get the hostname
            String hostname = SocketUtilities.getHostname();

            //Get the new alias and set it
            String theAlias = new StringBuilder().append(hostname)
                .append("_").append(SocketUtilities.getNextId()).toString();

            createSelfSignedCertificate(tempKeystore, theAlias);

        } catch (NoSuchAlgorithmException | CertificateException | KeyStoreException | IOException ex) {
           throw new LoggableException(ex);
        } 

        return tempKeystore;

    }
    
    //===============================================================
    /**
     * Returns a file representing a java keystore
    */
    private static void createSelfSignedCertificate(KeyStore passedKeyStore, String hostAlias ) throws LoggableException {
     
        String distName = "CN=sesef.net, O=RSA, L=lkjfe, S=CA, C=USA";
        String issuerName = "CN=olkef.gov, O=sfsefse, L=sefesf, S=CA, C=USA";

        try {
            Object[] theObjArr = X509CertificateFactory.generateCertificate( distName, issuerName, 365, "RSA", 2048 );
            Key theKey = (Key) theObjArr[0];
            Certificate newCert = (Certificate) theObjArr[1];

            passedKeyStore.setKeyEntry(hostAlias, theKey, "".toCharArray(), new Certificate[]{newCert});

        } catch (KeyStoreException | CertificateException | InvalidKeyException | NoSuchAlgorithmException | NoSuchProviderException | SignatureException | IOException ex) {
           throw new LoggableException(ex);
        }
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
     * Create a SSL context
     * @return
     * @throws LoggableException 
    */   
    public static SSLContext createServerSSLContext() throws LoggableException {

        SSLContext aContext = null;
        KeyStore theKeyStore = SSLUtilities.getKeystore();
        if(theKeyStore != null ){

            try {
                
                KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
                kmf.init(theKeyStore, "".toCharArray());

                TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
                tmf.init(theKeyStore);

                aContext = SSLContext.getInstance("TLS");
                aContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

            } catch ( KeyManagementException | KeyStoreException | UnrecoverableKeyException | NoSuchAlgorithmException ex ){
                throw new LoggableException(ex);
            }

        }

        return aContext;
    }

}/* END CLASS SSLUtilities */
