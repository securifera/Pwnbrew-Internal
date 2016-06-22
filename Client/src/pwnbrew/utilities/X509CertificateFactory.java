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
 * X509CertificateFactory.java
 *
 * Created on Nov 11, 2013, 7:00:14 PM
 */

package pwnbrew.utilities;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.util.Date;
import javax.net.ssl.SSLException;

public class X509CertificateFactory {
  
    //NOTE: Java cryptography Architecture API Specification & Reference...
    // http://www.ida.liu.se/~TDDB32/kursbibliotek/BlueJ/docs/guide/security/CryptoSpec.html#AppA
  
    //NOTE: Some of this code is based on code acquired from an article...
    //  at: http://bfo.com/blog/2011/03/08/odds_and_ends_creating_a_new_x_509_certificate.html
  
    private static final String Algorithm_RSA = "RSA";
    private static final String Hash_SHA256withRSA = "SHA256withRSA";
    
    
   // ==========================================================================
    /**
     * 
     * 
     * @param name the X.509 Distinguished Name, eg "CN=Test, L=London, C=GB"
     * @param issuer
     * @param algorithm
     * @param days how many days from now the Certificate is valid for
     * @param KeySize
     * 
     * @return
     * @throws java.security.cert.CertificateException
     * @throws java.security.SignatureException
     * @throws java.security.InvalidKeyException
     * @throws java.security.NoSuchProviderException
     * @throws java.io.IOException
     * @throws java.security.NoSuchAlgorithmException
     */
    @SuppressWarnings("ucd")
    public static Object[] generateCertificate( String name, String issuer, int days, String algorithm, int KeySize )
            throws CertificateException, InvalidKeyException, IOException, NoSuchAlgorithmException,
            NoSuchProviderException, SignatureException {

        //The object array that contains the Private Key and the Certificate
        PrivateKey privKey = null;
        sun.security.x509.X509CertImpl cert = null;
        String hashAlg = Hash_SHA256withRSA;
            
        KeyPairGenerator kpGenerator = KeyPairGenerator.getInstance( algorithm );
        if( kpGenerator != null ){

            if(!algorithm.equals(Algorithm_RSA)){
                throw new SSLException("Unsupported PKI algorithm.");
            }

            kpGenerator.initialize( KeySize, new SecureRandom() );
            KeyPair keyPair = kpGenerator.generateKeyPair();

            Date from = new Date();
            Date to = new Date( from.getTime() + ((long)days * (long)86400000) ); //Milliseconds in one day = 86400000
            sun.security.x509.CertificateValidity certValidity = new sun.security.x509.CertificateValidity( from, to );

            BigInteger certSerialNumber = new BigInteger( 64, new SecureRandom() );
            sun.security.x509.X500Name ownerName = new sun.security.x509.X500Name( name );
            sun.security.x509.X500Name issuerName = new sun.security.x509.X500Name( issuer );

            sun.security.x509.X509CertInfo certInfo = new sun.security.x509.X509CertInfo();
            certInfo.set( sun.security.x509.X509CertInfo.VALIDITY, certValidity );
            certInfo.set( sun.security.x509.X509CertInfo.SERIAL_NUMBER, new sun.security.x509.CertificateSerialNumber( certSerialNumber ) );
            certInfo.set( sun.security.x509.X509CertInfo.SUBJECT, new sun.security.x509.CertificateSubjectName( ownerName ) );
            certInfo.set( sun.security.x509.X509CertInfo.ISSUER, new sun.security.x509.CertificateIssuerName( issuerName ) );
            certInfo.set( sun.security.x509.X509CertInfo.KEY, new sun.security.x509.CertificateX509Key( keyPair.getPublic() ) );
            certInfo.set( sun.security.x509.X509CertInfo.VERSION, new sun.security.x509.CertificateVersion( sun.security.x509.CertificateVersion.V3 ) );
               
            sun.security.x509.AlgorithmId algorithmId = new sun.security.x509.AlgorithmId( sun.security.x509.AlgorithmId.sha256WithRSAEncryption_oid );
            certInfo.set( sun.security.x509.X509CertInfo.ALGORITHM_ID, new sun.security.x509.CertificateAlgorithmId( algorithmId ) );

            //Sign the certificate to identify the Algorithm_RSA that's used...
            privKey = keyPair.getPrivate();
            cert = new sun.security.x509.X509CertImpl( certInfo );
            cert.sign( privKey, hashAlg );

            //Update the algorith, and resign...
            algorithmId = (sun.security.x509.AlgorithmId)cert.get( sun.security.x509.X509CertImpl.SIG_ALG );
            certInfo.set( sun.security.x509.CertificateAlgorithmId.NAME + "." + sun.security.x509.CertificateAlgorithmId.ALGORITHM, algorithmId );
            cert = new sun.security.x509.X509CertImpl( certInfo );
            cert.sign( privKey, hashAlg );
        }
        
        return new Object[]{ privKey, cert };
        
    }/* END generateCertificate( String, KeyPair, int, String ) */
  
  
}/* END CLASS X509CertificateFactory */
