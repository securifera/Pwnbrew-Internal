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

import pwnbrew.host.Host;
import pwnbrew.host.HostController;
import pwnbrew.manager.PortManager;
import pwnbrew.manager.DataManager;
import pwnbrew.manager.ServerManager;
import pwnbrew.network.ControlOption;
import pwnbrew.utilities.SocketUtilities;

/**
 *
 *  
 */
public final class RelayStopRelay extends MaltegoMessage{
    
    private static final byte OPTION_TARGET_HOST_ID = 22;    
    private int hostId;
        
    public static final short MESSAGE_ID = 0x71;
    
     // ==========================================================================
    /**
     * Constructor
     *
     * @param passedId
    */
    public RelayStopRelay(byte[] passedId ) {
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
            case OPTION_TARGET_HOST_ID:
                hostId = SocketUtilities.byteArrayToInt(theValue);
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
                
        //Get the host controllers 
        String hostIdStr = Integer.toString(hostId);
        ServerManager aSM = (ServerManager) passedManager;
        HostController theHostController = aSM.getHostController(hostIdStr);
        if( theHostController != null ){

            Host theHost = theHostController.getObject();
            if( !theHost.getRelayPort().isEmpty() ) {
                theHost.setRelayPort( "" );
                theHostController.saveToDisk();

                //Send relay start message
                RelayStop relayMsg = new RelayStop( hostId );
                relayMsg.setSrcHostId( getSrcHostId() );
                DataManager.send( passedManager, relayMsg );
            } else {

                //Send the message
                RelayStatus aMsg = new RelayStatus(getSrcHostId(), true );
                DataManager.send( passedManager, aMsg);

            }

        }               
          
    }

}/* END CLASS RelayStartRelay */
