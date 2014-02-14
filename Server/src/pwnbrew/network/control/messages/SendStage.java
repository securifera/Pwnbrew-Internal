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
* SendStage.java
*
* Created on Oct 7, 2013, 10:12:33 PM
*/

package pwnbrew.network.control.messages;

import pwnbrew.manager.CommManager;
import pwnbrew.network.PortRouter;
import pwnbrew.network.control.ControlMessageManager;
import pwnbrew.selector.SocketChannelHandler;
import pwnbrew.utilities.Utilities;

/**
 *
 *  
 */
public final class SendStage extends ControlMessage{ // NO_UCD (use default)
    
    protected static final String NAME_Class = SendStage.class.getSimpleName();
  
    // ==========================================================================
    /**
     * Constructor
     *
     * @param dstHostId
    */
    public SendStage( int dstHostId ) {
        super( dstHostId );
    }
    
     // ==========================================================================
    /**
     * Constructor
     *
     * @param passedId
    */
    public SendStage(byte[] passedId ) {
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

            //Get the socketchannel handler
            int clientId = getClientId();
            PortRouter aPR = passedManager.getPortRouter( aCMManager.getPort());
            SocketChannelHandler aSCH = aPR.getSocketChannelHandler( clientId );

            //Turn on staging flag and send the payload if it isn't relayed
            if( aSCH.setStaging(clientId, true) ) {
                Payload aPayload = Utilities.getClientPayload(clientId);
                aCMManager.send(aPayload);
            }
        }
 
    }
}
