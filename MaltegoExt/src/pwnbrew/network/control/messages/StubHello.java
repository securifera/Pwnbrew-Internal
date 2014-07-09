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
import java.util.logging.Level;
import pwnbrew.logging.Log;
import pwnbrew.manager.CommManager;
import pwnbrew.network.PortRouter;
import pwnbrew.network.control.ControlMessageManager;
import pwnbrew.selector.SocketChannelHandler;

/**
 *
 *  
 */
public class StubHello extends ControlMessage {
        
    //Class name
    private static final String NAME_Class = StubHello.class.getSimpleName();
 
    // ==========================================================================
    /**
     * Constructor
     *
     * @param passedId
    */
    public StubHello(byte[] passedId ) {
        super( passedId );
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
            
            //Change the wrapping flag
            Integer theClientId = getSrcHostId();
            PortRouter aPR = passedManager.getPortRouter( aCMManager.getPort() );
            SocketChannelHandler aSCH = aPR.getSocketChannelHandler(theClientId);
            
            if( aSCH != null ){

                try{
                    //Send HostAck
                    HelloAck aHostAck = new HelloAck(theClientId);
                    aCMManager.send( aHostAck );

                    //Turn off wrapping
                    aSCH.setWrapping( theClientId, false);
                } catch (IOException ex) {
                    Log.log(Level.INFO, NAME_Class, "evaluate()", ex.getMessage(), ex );
                }
            }
                        
        }        
    }
 }