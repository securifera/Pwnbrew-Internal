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
* ResetId.java
*
* Created on December 11, 2013, 11:12:12 PM
*/

package pwnbrew.network.control.messages;

import java.io.IOException;
import java.util.logging.Level;
import pwnbrew.ClientConfig;
import pwnbrew.log.LoggableException;
import pwnbrew.log.RemoteLog;
import pwnbrew.manager.ConnectionManager;
import pwnbrew.manager.DataManager;
import pwnbrew.manager.PortManager;
import pwnbrew.network.ConnectionCallback;
import pwnbrew.network.PortRouter;
import pwnbrew.selector.SocketChannelHandler;
import pwnbrew.utilities.DebugPrinter;
import pwnbrew.utilities.SocketUtilities;

/**
 *
 *  
 */
public final class ResetId extends ControlMessage{ // NO_UCD (use default)

     //Class name
    private static final String NAME_Class = ResetId.class.getSimpleName();
    
    public static final short MESSAGE_ID = 0x4c;
    
    // ==========================================================================
    /**
     *  Constructor
     * 
     * @param passedId 
     */
    public ResetId(byte[] passedId ) {
        super( passedId );
    }
    
    //===============================================================
    /**
    *   Performs the logic specific to the message.
    *
     * @param passedManager
    */
    @Override
    public void evaluate( PortManager passedManager ) {
          
        
        try {
            //Create a new client id
            ClientConfig theConf = ClientConfig.getConfig();
            Integer anInteger = SocketUtilities.getNextId();
            theConf.setHostId(anInteger.toString());
            theConf.writeSelfToDisk();
            
            ClientConfig theClientConfig = ClientConfig.getConfig();
            PortRouter aPR = passedManager.getPortRouter( theClientConfig.getSocketPort() );
            if( aPR != null ){
                  
                int tempId = getChannelId();                
                
                //Notify reconnect timer
                ConnectionCallback aCC = aPR.removeConnectionCallback(tempId);
                if( aCC != null ){
                    DebugPrinter.printMessage(NAME_Class, "Calling callback function.");
                    aCC.handleConnection(tempId);
                }
                
                //Shutdown the socket
                SocketChannelHandler aSCH = aPR.getConnectionManager().getSocketChannelHandler( tempId ); 
                aSCH.shutdown(); 
                aPR.socketClosed(getSrcHostId(), tempId);
                                          
            }             
                       
        } catch (LoggableException ex) {
            RemoteLog.log(Level.INFO, NAME_Class, "evaluate()", ex.getMessage(), ex );
        }
        
    }

}/* END CLASS ResetId */
