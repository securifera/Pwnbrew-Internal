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
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.logging.Level;
import pwnbrew.host.Host;
import pwnbrew.host.HostController;
import pwnbrew.logging.Log;
import pwnbrew.logging.LoggableException;
import pwnbrew.manager.CommManager;
import pwnbrew.manager.ServerManager;
import pwnbrew.network.ControlOption;
import pwnbrew.network.control.ControlMessageManager;
import pwnbrew.network.relay.RelayManager;
import pwnbrew.utilities.SocketUtilities;

/**
 *
 *  
 */
public final class GetCheckInSchedule extends ControlMessage{ 
    
    private static final byte OPTION_HOST_ID = 124;
    private int hostId;
    
    private static final String NAME_Class = GetCheckInSchedule.class.getSimpleName();
    // ==========================================================================
    /**
     * Constructor
     *
     * @param passedId
    */
    public GetCheckInSchedule(byte[] passedId ) {
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
            case OPTION_HOST_ID:
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
     * @throws pwnbrew.logging.LoggableException
    */
    @Override
    public void evaluate( CommManager passedManager ) throws LoggableException {     
            
        RelayManager aManager = RelayManager.getRelayManager();
        if( aManager != null ){

            String hostIdStr = Integer.toString( hostId );

            //Get the host controller 
            ServerManager aSM = (ServerManager) passedManager;
            HostController theHostController = aSM.getHostController( hostIdStr );
            if( theHostController != null ){

                int srcId = getSrcHostId();
                Host theHost = theHostController.getObject();
                List<String> theCheckInList = theHost.getCheckInList();
                for( String aString : theCheckInList ){
                    try {
                        CheckInTimeMsg aMsg = new CheckInTimeMsg( srcId, hostId, aString);
                        aManager.send(aMsg);
                    } catch (IOException ex) {
                        Log.log(Level.WARNING, NAME_Class, "evaluate()", ex.getMessage(), ex );                                
                    }
                }

                //Get flag and send a msg
                try {
                    boolean asfFlag = theHostController.getAutoSleepFlag();
                    AutoSleep anASMsg = new AutoSleep( srcId, hostId, AutoSleep.GET_VALUE, asfFlag );
                    aManager.send(anASMsg);
                } catch (IOException ex) {
                    Log.log(Level.WARNING, NAME_Class, "evaluate()", ex.getMessage(), ex );                                
                }
            }

             
        }      

    }        
    

}
