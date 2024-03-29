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
* RelayDisconnect.java
*
* Created on December 14, 2013, 7:22:43 PM
*/

package pwnbrew.network.control.messages;

import java.util.logging.Level;
import javax.swing.SwingUtilities;
import pwnbrew.host.Host;
import pwnbrew.host.HostController;
import pwnbrew.log.Log;
import pwnbrew.log.LoggableException;
import pwnbrew.manager.ConnectionManager;
import pwnbrew.manager.PortManager;
import pwnbrew.manager.ServerManager;
import pwnbrew.network.ControlOption;
import pwnbrew.network.PortRouter;
import pwnbrew.utilities.SocketUtilities;
import pwnbrew.xml.ServerConfig;

/**
 *
 *  
 */
@SuppressWarnings("ucd")
public final class RelayDisconnect extends ControlMessage{
    
    private static final String NAME_Class = RelayDisconnect.class.getSimpleName();
    
    private static final byte OPTION_ID = 25; 
    private static final byte OPTION_CHANNEL_ID = 102; 
    private int relayHostId;
    private int relayChannelId;   
    
    public static final short MESSAGE_ID = 0x47;
    
    // ==========================================================================
    /**
     * Constructor
     *
     * @param passedId
    */
    public RelayDisconnect( byte[] passedId ) {
        super(passedId);
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

            case OPTION_ID:                    
                relayHostId = SocketUtilities.byteArrayToInt(theValue);                 
                break;
            case OPTION_CHANNEL_ID:                    
                relayChannelId = SocketUtilities.byteArrayToInt(theValue);                 
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
    
        //Get the host and set the relay information
        String clientIdStr = Integer.toString( relayHostId );
        final ServerManager aSM = (ServerManager) passedManager;
        
        //Remove the connection in the map
        try {
            ServerConfig aConf = ServerConfig.getServerConfig();
            int serverPort = aConf.getSocketPort();
            PortRouter aSPR = aSM.getPortRouter(serverPort);
            ConnectionManager aCM = aSPR.getConnectionManager(relayHostId);
            if( aCM != null )
                aCM.removeHandler(relayChannelId);
            
            
        } catch (LoggableException ex) {
            Log.log(Level.SEVERE, NAME_Class, "main()", ex.getMessage(), ex );
        }
        
        if( relayChannelId == ConnectionManager.COMM_CHANNEL_ID ){
            final HostController theController = aSM.getHostController(clientIdStr);
            if( theController != null ){

                SwingUtilities.invokeLater( new Runnable() {
                    @Override
                    public void run() { 
                        aSM.hostDisconnected( (Host) theController.getObject() );
                        theController.saveToDisk();
                    }
                });
            }
        }
    }
    
    
}
