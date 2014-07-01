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
import java.util.List;
import java.util.logging.Level;
import pwnbrew.controllers.MainGuiController;
import pwnbrew.host.Host;
import pwnbrew.host.HostController;
import pwnbrew.logging.Log;
import pwnbrew.manager.CommManager;
import pwnbrew.network.ControlOption;
import pwnbrew.network.control.ControlMessageManager;
import pwnbrew.tasks.TaskManager;
import pwnbrew.utilities.SocketUtilities;

/**
 *
 *  
 */
public class SleepRelay extends ControlMessage {
    
    private static final byte OPTION_TARGET_HOST_ID = 22;
    private int hostId;
    
    //Class name
    private static final String NAME_Class = SleepRelay.class.getSimpleName();
 
    // ==========================================================================
    /**
     * Constructor
     *
     * @param passedId
    */
    public SleepRelay(byte[] passedId ) {
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
    public void evaluate( CommManager passedManager ) {     
        
        ControlMessageManager aCMManager = ControlMessageManager.getControlMessageManager();
        if( aCMManager != null ){
            
            TaskManager theTaskManager = passedManager.getTaskManager();
            if( theTaskManager instanceof MainGuiController ){
                
                //Get the host controllers 
                String hostIdStr = Integer.toString(hostId);
                MainGuiController theGuiController = (MainGuiController)theTaskManager;
                HostController theHostController = theGuiController.getHostController(hostIdStr);
                if( theHostController != null ){
                    
                    try {
                        Host theHost = theHostController.getObject();
                        //Get the sleep time
                        List<String> theCheckInList = theHost.getCheckInList();
                        if( !theCheckInList.isEmpty() ){

                            //Get the first time
                            String theCheckInTime = theCheckInList.get(0);
                            //Send sleep message
                            Sleep sleepMsg = new Sleep( hostId, theCheckInTime ); //Convert mins to seconds
                            aCMManager.send(sleepMsg );
                        }

                    } catch( UnsupportedEncodingException ex ){
                        Log.log(Level.WARNING, NAME_Class, "evaluate()", ex.getMessage(), ex );
                    }
                
                }               
            }
                        
        }        
    }
  

}/* END CLASS SleepRelay */