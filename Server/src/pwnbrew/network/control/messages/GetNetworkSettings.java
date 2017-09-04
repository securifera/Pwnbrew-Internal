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
package pwnbrew.network.control.messages;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.Principal;
import java.security.cert.Certificate;
import java.util.Date;
import java.util.logging.Level;
import pwnbrew.log.Log;
import pwnbrew.log.LoggableException;
import pwnbrew.manager.DataManager;
import pwnbrew.manager.PortManager;
import pwnbrew.misc.Constants;
import pwnbrew.utilities.SSLUtilities;
import pwnbrew.xml.ServerConfig;

/**
 *
 * @author Securifera
 */
public class GetNetworkSettings extends MaltegoMessage{ // NO_UCD (use default)
    
    private static final String NAME_Class = GetNetworkSettings.class.getSimpleName();
    
    // ==========================================================================
    /**
     * Constructor
     *
     * @param passedId
    */
    public GetNetworkSettings(byte[] passedId ) {
        super( passedId );
    }
  
    //===============================================================
    /**
    *   Performs the logic specific to the message.
    *
     * @param passedManager
    */
    @Override
    public void evaluate( PortManager passedManager ) {     

        //Populate the componenets
        try {

            ServerConfig theConf = ServerConfig.getServerConfig();        
            if(theConf != null){

                int serverPort = theConf.getSocketPort();

                //Get the PKI Cert
                Certificate theCert = SSLUtilities.getCertificate();
                if( theCert instanceof sun.security.x509.X509CertImpl ){
                    sun.security.x509.X509CertImpl theCustomCert = (sun.security.x509.X509CertImpl)theCert;

                    //Get the issuee name
                    Principal aPrincipal = theCustomCert.getSubjectDN();
                    String issueeName = aPrincipal.getName();

                    //Get the issuer org
                    aPrincipal = theCustomCert.getIssuerDN();
                    String issuerName = aPrincipal.getName();

                    //Set the algorithm
                    String theAlgorithm = theCustomCert.getSigAlgName();

                    //Set the date
                    Date expirationDate = theCustomCert.getNotAfter();
                    String expDateStr = Constants.CHECKIN_DATE_FORMAT.format(expirationDate);    

                    //Set the serial num
                    String serialNum = theCustomCert.getSerialNumber().toString(10);
                  
                    //Send back the data
                    NetworkSettingsMsg aMsg = new NetworkSettingsMsg( getSrcHostId(), serverPort, issueeName, issuerName, theAlgorithm, expDateStr, serialNum );
                    DataManager.send( passedManager, aMsg);

                }

            }

        } catch(KeyStoreException | LoggableException | IOException ex){
            Log.log(Level.WARNING, NAME_Class, "evaluate()", ex.getMessage(), ex );  
        }
    }
          
    

}
