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
* ClientPortRouter.java
*
*/

package pwnbrew.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.AlreadyConnectedException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import pwnbrew.StubConfig;
import pwnbrew.concurrent.LockListener;
import pwnbrew.concurrent.LockingThread;
import pwnbrew.log.LoggableException;
import pwnbrew.manager.PortManager;
import pwnbrew.misc.Constants;
import pwnbrew.misc.DebugPrinter;
import pwnbrew.network.control.ControlMessageManager;
import pwnbrew.network.control.messages.StubHello;
import pwnbrew.selector.ConnectHandler;
import pwnbrew.selector.SocketChannelHandler;


/**
 *
 *  
 */
public class ClientPortRouter extends PortRouter {

    private static final int SLEEP_TIME = 1000;
    private static final int CONNECT_RETRY = 3;
    
    private SocketChannelHandler serverSCH = null;
    private final LockingThread theConnectionLock;       
    private static final String NAME_Class = ClientPortRouter.class.getSimpleName();    
      
    //===============================================================
     /**
     * ClientComm constructor
     *
     * @param passedCommManager
     * @param passedBool
     * @throws IOException
    */
    public ClientPortRouter( PortManager passedCommManager, boolean passedBool ) throws IOException {
        super(passedCommManager, passedBool);
        
        theConnectionLock = new LockingThread( Constants.Executor );
        theConnectionLock.start();
        
    }

    //===============================================================
     /**
     * Recursive function for connecting to the server
     *
     * @return
     * @throws IOException
    */
    private boolean connect( InetAddress hostAddress, int passedPort ) throws LoggableException {

        SocketChannelHandler theSCH = getSocketChannelHandler();
        try {
            
            if( theSCH == null || theSCH.getState() == Constants.DISCONNECTED ){

                // Create a non-blocking socket channel
                SocketChannel theSocketChannel = SocketChannel.open();
                theSocketChannel.configureBlocking(false);

                // Kick off connection establishment
                try {
                    theSocketChannel.connect(new InetSocketAddress(hostAddress, passedPort));
                } catch( AlreadyConnectedException ex ) {
                    return true;
                }

                // Register the server socket channel, indicating an interest in
                // accepting new connections
                ConnectHandler connectHandler = new ConnectHandler( this );

                // Register the socket channel and handler with the selector
                SelectionKey theSelKey = theSelectionRouter.register(theSocketChannel, SelectionKey.OP_CONNECT, connectHandler);

                //Wait until the thread is notified or times out
                waitForConnection();

                //Return if the key was cancelled
                if(!theSelKey.isValid()){
                    theSelKey.cancel();
                    return false;
                }

                //If we returned but we are not connected
                theSCH = getSocketChannelHandler();
                if( theSCH == null || theSCH.getState() == Constants.DISCONNECTED){

                    //Shutdown the first connect handler and set it to null
                    theSelKey.cancel();
                    return false;
                }
            }

        } catch(IOException ex){
            throw new LoggableException(ex);
        }

        return true;
    }

    //===============================================================
    /**
    * Adds an event for the selector that the client is interested in opening
    * a connection.
    *
    * @return
    * @throws IOException
    */
    private boolean initiateConnection( LockListener passedListener, InetAddress hostAddress, int passedPort, int retry ) throws LoggableException {

        int sleepTime = SLEEP_TIME;
        boolean connected = false;
           
        //Block until we get the lock
        int retVal = -1;
        while( retVal != LockingThread.LOCK ){
            theConnectionLock.lock( passedListener );
            retVal = passedListener.waitForLock();
        }

        //DebugPrinter.printMessage( NAME_Class, "Obtained connection lock");            
        try {
            //Call the recursive function for connecting
            while( retry > 0 && !connected ){

                connected =  connect( hostAddress, passedPort );

                //Sleep if not connected
                if(!connected){
                    try {                    
                        //Intentially sleeping with lock held so there are no other attempts to
                        //connect to the server during this loop.
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException ex) {
                        ex = null;
                    }

                    //Update counters and test
                    sleepTime += sleepTime;  
                    retry--;

                } else {
                    
                    if( serverSCH != null ){

                        //Get the message sender
                        try {
                            
                            ControlMessageManager aCMManager = ControlMessageManager.getControlMessageManager();
                            if( aCMManager == null ){
                                aCMManager = ControlMessageManager.initialize(theCommManager);
                            }
                            
                            //Create a hello message and send it
                            StubHello helloMessage = new StubHello( Constants.SERVER_ID );
                            aCMManager.send( helloMessage );
                            
                            //Wait until the thread is notified or times out
                            waitForConnection();
                            
                        } catch(IOException ex){
                            throw new LoggableException(ex);
                        }


                    } else {
                        //Set connected to false if the server socket is null
                        connected = false;
                    }

                }

            }
            
        } finally {
        
            theConnectionLock.unlock();
            //DebugPrinter.printMessage(this.getClass().getSimpleName(), "Released connection lock");
        }

        return connected;
    }

    //===============================================================
    /**
     * Sets the access handler for the server
     *
     * @param passedId
     * @param theAccessHandler
     */
    @Override
    public void registerHandler( int passedId, SocketChannelHandler theAccessHandler ) {
        serverSCH = theAccessHandler;
    }
    
     //===============================================================
    /**
     *  Removes the client id
     * 
     * @param passedInt 
    */
    @Override
    public void removeHandler( int passedInt ) {
        serverSCH = null;
    }
 
    //===============================================================
    /**
     *  Returns the SocketChannelHandler for the server.
     * 
     * @param passedId
     * @return 
    */  
    @Override
    public SocketChannelHandler getSocketChannelHandler( Integer... passedId ){
        return serverSCH;
    }
    
        //===============================================================
    /**
     *  Connection dropped.
     * 
     * @param theHandler 
     */
    @Override
    public void socketClosed( SocketChannelHandler theHandler ){
    };
    
     //===============================================================
    /**
     *  Shuts down the handler and selector
     */
    @Override
    public void shutdown(){

        //Shutdown the server access handler
        if(serverSCH != null ){            
            serverSCH.shutdown();           
        }
        
        theSelectionRouter.shutdown();
        theConnectionLock.shutdown();

    }
    
    //===============================================================
    /**
     * Checks that a connection has been made to the passed port.  If not it
     * creates one.
     *
     * @param passedPort
     * @param passedListener
     * @return 
    */
    public boolean ensureConnectivity( int passedPort, LockListener passedListener ) {

        boolean isConnected = true;
        try {
            
            if(serverSCH == null || serverSCH.getState() == Constants.DISCONNECTED){            
           
                //Get the data port and try to connect
                StubConfig theConf = StubConfig.getConfig();
                String serverIp = theConf.getServerIp();
               
                //Get the inet
                InetAddress srvInet = InetAddress.getByName(serverIp);
                //DebugPrinter.printMessage( NAME_Class, "Attempting to connect to " + srvInet.getHostAddress() + ":" + passedPort);
     
                if( !initiateConnection( passedListener, srvInet, passedPort, CONNECT_RETRY )){
                    DebugPrinter.printMessage( NAME_Class, "routeMessage", "Unable to connect to port " + passedPort, null);  
                    isConnected = false;
                } else {
                
                    //Set flag
                    if( serverSCH == null || serverSCH.getState() == Constants.DISCONNECTED ){
                        isConnected = false;               
                    }
                }
        
            }
     
        } catch ( UnknownHostException | LoggableException ex) {
            DebugPrinter.printMessage( NAME_Class, "routeMessage", ex.getMessage(), ex);  
            isConnected = false;
        }

        return isConnected;
    }  


}/* END CLASS ClientPortRouter */
