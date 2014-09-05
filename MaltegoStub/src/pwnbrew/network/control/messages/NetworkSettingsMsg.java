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

import java.io.UnsupportedEncodingException;
import pwnbrew.MaltegoStub;
import pwnbrew.functions.Function;
import pwnbrew.functions.ToServerConfiguration;
import pwnbrew.manager.PortManager;
import pwnbrew.misc.SocketUtilities;
import pwnbrew.network.ControlOption;

/**
 *
 * @author Securifera
 */
public final class NetworkSettingsMsg extends ControlMessage {
    
    private static final byte OPTION_SERVER_PORT = 54;  
    private static final byte OPTION_ISSUEE_NAME = 55;
    private static final byte OPTION_ISSUER_NAME = 56;
    private static final byte OPTION_ALGORITHM = 57;
    private static final byte OPTION_EXP_DATE = 58;
    
    private int theServerPort;
    private String theIssueeName = null;
    private String theIssuerName = null;
    private String theAlgorithm = null;
    private String theExpDate = null;
    
    // ==========================================================================
    /**
     * Constructor
     *
     * @param dstHostId
     * @param passedPort
     * @param issueeName
     * @param issuerName
     * @param theAlgorithm
     * @param expDateStr
     * @throws java.io.UnsupportedEncodingException
    */
    public NetworkSettingsMsg( int dstHostId, int passedPort, String issueeName, String issuerName, String theAlgorithm, String expDateStr ) throws UnsupportedEncodingException {
        super( dstHostId );
        
        byte[] tempBytes = SocketUtilities.intToByteArray(passedPort);
        ControlOption aTlv = new ControlOption( OPTION_SERVER_PORT, tempBytes);
        addOption(aTlv);
        
        tempBytes = issueeName.getBytes("US-ASCII");
        aTlv = new ControlOption( OPTION_ISSUEE_NAME, tempBytes);
        addOption(aTlv);
        
        tempBytes = issuerName.getBytes("US-ASCII");
        aTlv = new ControlOption( OPTION_ISSUER_NAME, tempBytes);
        addOption(aTlv);
        
        tempBytes = theAlgorithm.getBytes("US-ASCII");
        aTlv = new ControlOption( OPTION_ALGORITHM, tempBytes);
        addOption(aTlv);
        
        tempBytes = expDateStr.getBytes("US-ASCII");
        aTlv = new ControlOption( OPTION_EXP_DATE, tempBytes);
        addOption(aTlv);
 
    }
        
    // ==========================================================================
    /**
     * Constructor
     *
     * @param passedId
    */
    public NetworkSettingsMsg(byte[] passedId ) {
        super( passedId );
    }
    
    //=========================================================================
    /**
     *  Sets the variable in the message related to this TLV
     * 
     * @param tempTlv 
     * @return  
     */
    @Override
    public boolean setOption( ControlOption tempTlv ){        

        boolean retVal = true;
        try {
            byte[] theValue = tempTlv.getValue();
            switch( tempTlv.getType()){
                case OPTION_SERVER_PORT:
                    theServerPort = SocketUtilities.byteArrayToInt( theValue );
                    break;
                case OPTION_ISSUEE_NAME:
                    theIssueeName = new String( theValue, "US-ASCII");
                    break;
                case OPTION_ISSUER_NAME:
                    theIssuerName = new String( theValue, "US-ASCII");
                    break;
                case OPTION_EXP_DATE:
                    theExpDate = new String( theValue, "US-ASCII");
                    break;
                case OPTION_ALGORITHM:
                    theAlgorithm = new String( theValue, "US-ASCII");
                    break;
                default:
                    retVal = false;
                    break;
            }
        } catch (UnsupportedEncodingException ex) {
            ex = null;
        }
        return retVal;
    }

 
    //===============================================================
    /**
    *   Performs the logic specific to the message.
    *
     * @param passedManager
    */
    @Override
    public void evaluate( PortManager passedManager ) {
    
         if( passedManager instanceof MaltegoStub ){
            MaltegoStub theStub = (MaltegoStub)passedManager;
            Function aFunction = theStub.getFunction();
            if( aFunction instanceof ToServerConfiguration ){
                
                //Cast the function
                ToServerConfiguration aFunc = (ToServerConfiguration)aFunction;
                
                //Add the jar item
                aFunc.setNetworkSettings(theServerPort, theIssueeName, theIssuerName, theExpDate, theAlgorithm );
            }            
        }  
    }

}
