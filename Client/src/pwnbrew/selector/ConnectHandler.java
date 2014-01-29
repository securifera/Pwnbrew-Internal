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
* ConnectHandler.java
*
* Created on June 2, 2013, 10:16:21 PM
*/

package pwnbrew.selector;

import pwnbrew.log.RemoteLog;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import pwnbrew.log.LoggableException;
import pwnbrew.network.ClientPortRouter;
import pwnbrew.network.socket.SecureSocketChannelWrapper;

/**
 *
 *  
 */
public class ConnectHandler implements Selectable {

    private final ClientPortRouter theClientPortRouter;

    private static final String NAME_Class = ConnectHandler.class.getSimpleName();
  
    public ConnectHandler( ClientPortRouter passedParent ) {
	theClientPortRouter = passedParent;
    }

    //===============================================================
    /**
    * Handles the operation associated with the incoming key
    *
    * @param theSelKey
    */
    @Override
    public void handle (SelectionKey theSelKey) {

        String msg;
        if (!theSelKey.isConnectable()) return;

        SocketChannel socketChannel = (SocketChannel) theSelKey.channel();

        // Finish the connection
        try {
            socketChannel.finishConnect();
        } catch (IOException ex) {

            // Notify the comm
            theClientPortRouter.beNotified(); 

            // Cancel the channel's registration with our selector           
            theSelKey.cancel();

            if(((msg = ex.getMessage()) != null) && msg.toUpperCase().contains("CLOSED"))
                theClientPortRouter.socketClosed( null );
            
            return;
        }

        //Necessary because the Inet does not comes back from the channel correctly
        InetAddress srcAddr = socketChannel.socket().getInetAddress();
        String theAddy = srcAddr.getHostAddress();
        try {
            srcAddr = InetAddress.getByName(theAddy);
        } catch (UnknownHostException ex) {
            RemoteLog.log(Level.SEVERE, NAME_Class, "handle()", ex.getMessage(), ex);
        }
        
        try {
            //Set a keepalive so we are notified of disconnects
            socketChannel.socket().setKeepAlive(true);
        } catch (SocketException ex) {
            RemoteLog.log(Level.SEVERE, NAME_Class, "handle()", ex.getMessage(), ex );
        }

        try {
            
            //Get or create the access handler
            SocketChannelHandler theSCH = theClientPortRouter.getSocketChannelHandler();
            if( theSCH == null ){
                theSCH = new SocketChannelHandler(theClientPortRouter);
                theClientPortRouter.registerHandler( 0, theSCH );
            }

            //Attach the accesshandler
            theSelKey.attach(theSCH);
                  
            //Assign an encyrpted socketwrapper to the handler
            SecureSocketChannelWrapper theSCW = new SecureSocketChannelWrapper( socketChannel, theSCH, true);
            theSCH.setSocketChannelWrapper(theSCW);
            
            theSCW.beginHandshake();

            //Notify the comm
            theClientPortRouter.getSelRouter().changeOps(theSCW.getSocketChannel(), SelectionKey.OP_READ | SelectionKey.OP_WRITE );                        
        
        } catch ( IOException ex) {
             RemoteLog.log(Level.WARNING, NAME_Class, "handle()", ex.getMessage(), ex);
        } catch (LoggableException ex) {
             RemoteLog.log(Level.WARNING, NAME_Class, "handle()", ex.getMessage(), ex);
        } catch (InterruptedException ex) {
             RemoteLog.log(Level.WARNING, NAME_Class, "handle()", ex.getMessage(), ex);
        }
     
    }


}/* END CLASS ConnectHandler */
