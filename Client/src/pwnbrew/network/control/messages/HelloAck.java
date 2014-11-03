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
* HelloAck.java
*
* Created on June 7, 2013, 10:09:02 PM
*/

package pwnbrew.network.control.messages;


import java.util.Map;
import java.util.Set;
import pwnbrew.ClientConfig;
import pwnbrew.manager.PortManager;
import pwnbrew.network.PortRouter;
import pwnbrew.network.ServerPortRouter;
import pwnbrew.network.relay.RelayManager;
import pwnbrew.selector.SocketChannelHandler;

/**
 *
 *  
 */
@SuppressWarnings("ucd")
public final class HelloAck extends ControlMessage {

    //Class name
    private static final String NAME_Class = HelloAck.class.getSimpleName();
     
    // ==========================================================================
    /**
     *  Constructor 
     * 
     * @param passedId 
     */
    public HelloAck( byte[] passedId ) {
       super(passedId );
    }
      
    //===============================================================
    /**
    *   Performs the logic specific to the message.
    *
     * @param passedManager
    */
    @Override
    public void evaluate( PortManager passedManager ) {   
        
        //Get the address and connect    
        int theClientId = getSrcHostId();
        ClientConfig theClientConfig = ClientConfig.getConfig();
        PortRouter aPR = passedManager.getPortRouter( theClientConfig.getSocketPort() );
        if( aPR != null ){

            //Get the handler
            SocketChannelHandler aSCH = aPR.getSocketChannelHandler();

            //Set the wrapping flag
            if( aSCH != null ){
                aSCH.setWrapping(false);
            }
            
            //Set the server id
            theClientConfig.setServerId( theClientId );
            
            //Ensure any internal hosts resend their hello message
            RelayManager theManager = RelayManager.getRelayManager();
            if( theManager != null ){
                ServerPortRouter aSPR = theManager.getServerPorterRouter();
                Map<Integer, SocketChannelHandler> aMap = aSPR.getSocketChannelHandlerMap();
                if( !aMap.isEmpty() ){
                    Set<Integer> keySet = aMap.keySet();
                    for( Integer aInt : keySet){
                        HelloRepeat aRepeatMsg = new HelloRepeat(aInt);
                        theManager.send(aRepeatMsg);                    
                    }
                }     
            }       
            
        }    

    }

}
