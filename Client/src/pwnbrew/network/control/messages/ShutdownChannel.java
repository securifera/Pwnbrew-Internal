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
* NoOp.java
*
* Created on April 18, 2018, 9:55:42 PM
*/

package pwnbrew.network.control.messages;

import pwnbrew.ClientConfig;
import pwnbrew.manager.ConnectionManager;
import pwnbrew.manager.PortManager;
import pwnbrew.network.ClientPortRouter;
import pwnbrew.utilities.SocketUtilities;
import pwnbrew.network.ControlOption;
import pwnbrew.network.PortRouter;
import pwnbrew.selector.SocketChannelHandler;

/**
 *
 *  
 */
@SuppressWarnings("ucd")
public final class ShutdownChannel extends ControlMessage{
    
    private static final byte OPTION_CHANNEL_ID = 102; 
    protected int fileChannelId = 0;   
    
     //Class name
    private static final String NAME_Class = ShutdownChannel.class.getSimpleName();  
    
    public static final short MESSAGE_ID = 0x81;
    
    // ==========================================================================
    /**
     * Constructor
     *
     * @param passedId
    */
    public ShutdownChannel(byte[] passedId ) {
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
        if( !super.setOption(tempTlv)){
            byte[] theValue = tempTlv.getValue();
            switch( tempTlv.getType()){
                case OPTION_CHANNEL_ID:
                    fileChannelId = SocketUtilities.byteArrayToInt(theValue);
                    break;
                default:
                    retVal = false;
                    break;              
            }  
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
                      
        //Send one back
        if( fileChannelId != ConnectionManager.CHANNEL_DISCONNECTED ){
            
            ClientConfig aConf = ClientConfig.getConfig();
            PortRouter aPR = passedManager.getPortRouter(aConf.getSocketPort());
            if( aPR != null && aPR instanceof ClientPortRouter){
                ClientPortRouter aCPR = (ClientPortRouter)aPR;
                ConnectionManager aCM = aCPR.getConnectionManager();
                SocketChannelHandler aSCH = aCM.getSocketChannelHandler(fileChannelId);
                if( aSCH != null ){
                    aSCH.shutdown();
                    aCPR.socketClosed(getSrcHostId(), fileChannelId);
                }
            }
        }
   
    }

}
