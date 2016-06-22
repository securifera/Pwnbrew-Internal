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
* SetRelayWrap.java
*
* Created on Feb 4, 2014, 6:13:45 PM
*/

package pwnbrew.network.control.messages;

import pwnbrew.manager.ConnectionManager;
import pwnbrew.manager.IncomingConnectionManager;
import pwnbrew.manager.PortManager;
import pwnbrew.utilities.SocketUtilities;
import pwnbrew.network.ControlOption;
import pwnbrew.network.ServerPortRouter;
import pwnbrew.network.relay.RelayManager;
import pwnbrew.selector.SocketChannelHandler;

/**
 *
 *  
 */
@SuppressWarnings("ucd")
public final class SetRelayWrap extends ControlMessage{
      
    private static final int NO_WRAP = 0x0;
    private static final byte OPTION_WRAPPER = 45; 
    private static final byte OPTION_CLIENT_ID = 43;
    
    private byte relayWrap;
    private int theRelayClientId;
    
    // ==========================================================================
    /**
     * Constructor
     *
     * @param passedId
    */
    public SetRelayWrap(byte[] passedId ) {
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
        byte[] theValue = tempTlv.getValue();
        switch( tempTlv.getType()){
            case OPTION_WRAPPER:
                if( theValue.length > 0 ){
                    relayWrap = theValue[0];
                }
                break;
            case OPTION_CLIENT_ID:
                theRelayClientId = SocketUtilities.byteArrayToInt(theValue);
                break;
            
            default:
                retVal = false;
                break;
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
    
        switch( relayWrap){
            case NO_WRAP:
                
                //Get the relay
                RelayManager theRelayManager = RelayManager.getRelayManager();
                ServerPortRouter theSPR = theRelayManager.getServerPorterRouter();

                //Set the flag on the handler
                 //Set the flag on the handler
                IncomingConnectionManager theICM = theSPR.getConnectionManager(theRelayClientId);
                if( theICM != null ){
                    
                    SocketChannelHandler aHandler = theICM.getSocketChannelHandler(ConnectionManager.COMM_CHANNEL_ID);
                    if( aHandler != null ){
                        aHandler.setWrapping(false);
                    }
                }
                
                break;
            default:
                break;
        }            
           
    }

}
