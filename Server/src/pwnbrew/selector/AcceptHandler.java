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


package pwnbrew.selector;

/*
* AcceptHandler.java
*
* Created on June 10, 2013, 11:12:34 PM
*/

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import pwnbrew.logging.Log;
import pwnbrew.logging.LoggableException;
import pwnbrew.manager.ServerManager;
import pwnbrew.misc.DebugPrinter;
import pwnbrew.network.ServerPortRouter;
import pwnbrew.network.SocketDisconnectTimer;
import pwnbrew.network.socket.SecureSocketChannelWrapper;

/**
 *
 *  
 */
final public class AcceptHandler implements Selectable {

    private final ServerPortRouter theSPR;
    private final boolean requireAuthentication;

    private static final String NAME_Class = AcceptHandler.class.getSimpleName();
  
    //===========================================================================
    /**
     *  Constructor
     * 
     * @param passedSPR 
     * @param passedBool 
     */
    public AcceptHandler( ServerPortRouter passedSPR, boolean passedBool ) {
	theSPR = passedSPR;
        requireAuthentication = passedBool;
    }

    //===============================================================
     /**
     * Handles the operation associated with the incoming key
     *
     * @param theSelKey
    */
    @Override
    public void handle (SelectionKey theSelKey) {

        if (!theSelKey.isAcceptable()) return;

        ServerSocketChannel theSSC = theSPR.getServerSocketChannel();
        SocketChannel theSocketChannel;
        try {
            theSocketChannel = theSSC.accept();
        } catch (IOException ex) {
            return;        
        }

        //Return if the socket is null
        if (theSocketChannel == null) return;

        //Get the source address - This is necessary - DO NOT REMOVE
        InetAddress srcAddr = theSocketChannel.socket().getInetAddress();
        try {
            srcAddr = InetAddress.getByName(srcAddr.getHostAddress());
        } catch (UnknownHostException ex) {
            Log.log(Level.SEVERE, NAME_Class, "handle()", ex.getMessage(), ex );
        }        
        
        try {            
            
            DebugPrinter.printMessage(NAME_Class, "Received a connection from " + srcAddr.getHostAddress());
            SocketChannelHandler theSCH = new SocketChannelHandler(theSPR);
            if( !requireAuthentication ){
                
                //Create a disconnect timer
                SocketDisconnectTimer aTimerTask = new SocketDisconnectTimer( (ServerManager)theSPR.getPortManager(), theSCH);
                
                //Create a timer
                theSPR.schedulerKillTimer(aTimerTask);
            }
            
            try {
                //Set a keepalive so we are notified of disconnects
                theSocketChannel.socket().setKeepAlive(true);
            } catch (SocketException ex) {
                Log.log(Level.SEVERE, NAME_Class, "handle()", ex.getMessage(), ex );
            }

            //Assign an unencyrpted socketwrapper to the handler temporarily
            SecureSocketChannelWrapper theSCW = new SecureSocketChannelWrapper( theSocketChannel, theSCH, requireAuthentication );
            theSCH.setSocketChannelWrapper(theSCW);
            
            theSCW.beginHandshake();

            //Register the new socket with this handler
            theSPR.getSelRouter().register(theSocketChannel, SelectionKey.OP_READ | SelectionKey.OP_WRITE, theSCH);

//            //Set to connected
//            if( theSCH.getState() == Constants.DISCONNECTED){
//                theSCH.setState(Constants.CONNECTED);
//            }           

        } catch ( IOException | LoggableException | InterruptedException ex) {
            Log.log(Level.WARNING, NAME_Class, "handle()", ex.getMessage(), ex);
        }

    }
    
}
