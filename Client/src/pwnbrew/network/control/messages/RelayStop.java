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
* RelayStop.java
*
* Created on Dec 2, 2013, 9:22:22 PM
*/

package pwnbrew.network.control.messages;

import java.io.IOException;
import java.util.logging.Level;
import pwnbrew.log.RemoteLog;
import pwnbrew.log.LoggableException;
import pwnbrew.manager.CommManager;
import pwnbrew.network.control.ControlMessageManager;
import pwnbrew.network.relay.RelayManager;

/**
 *
 *  
 */
public final class RelayStop extends ControlMessage{

    //Class name
    private static final String NAME_Class = RelayStop.class.getSimpleName();
    
    // ==========================================================================
    /**
     * Constructor
     *
     * @param passedId
    */
    public RelayStop( byte[] passedId ) {
        super(passedId);
    }
    
    //===============================================================
    /**
    *   Performs the logic specific to the message.
    *
     * @param passedManager
    */
    @Override
    public void evaluate( CommManager passedManager ) { 
        
        RelayManager aManager = RelayManager.getRelayManager();
        if( aManager != null ){
            aManager.shutdown();
        }       
        
         //Get the control message manager send an ack
        try {
            
            ControlMessageManager aCMManager = ControlMessageManager.getControlMessageManager();
            if( aCMManager == null ){
                aCMManager = ControlMessageManager.initialize(passedManager);
            }

            //Send the message
            RelayStatus aMsg = new RelayStatus( false );
            aCMManager.send(aMsg);
        
        } catch(IOException ex ){
            RemoteLog.log( Level.SEVERE, NAME_Class, "evaluate", ex.getMessage(), null);        
        } catch (LoggableException ex) {
            RemoteLog.log( Level.SEVERE, NAME_Class, "evaluate", ex.getMessage(), null);        
        }
        
    }


}/* END CLASS CreateShell */
