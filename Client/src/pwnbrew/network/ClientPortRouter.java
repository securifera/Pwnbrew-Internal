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

package pwnbrew.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.AlreadyConnectedException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Calendar;
import java.util.Date;
import java.util.EmptyStackException;
import java.util.logging.Level;
import pwnbrew.ClientConfig;
import pwnbrew.concurrent.LockListener;
import pwnbrew.concurrent.LockingThread;
import pwnbrew.log.LoggableException;
import pwnbrew.log.RemoteLog;
import pwnbrew.manager.ConnectionManager;
import pwnbrew.manager.DataManager;
import pwnbrew.manager.OutgoingConnectionManager;
import pwnbrew.manager.PortManager;
import pwnbrew.network.control.ControlMessageManager;
import pwnbrew.selector.ConnectHandler;
import pwnbrew.selector.SocketChannelHandler;
import pwnbrew.utilities.Constants;
import pwnbrew.utilities.DebugPrinter;
import pwnbrew.utilities.ReconnectTimer;


/**
 *
 *  
 */
public class ClientPortRouter extends PortRouter {

    private static final int SLEEP_TIME = 1000;
    private static final int CONNECT_RETRY = 3;
    
    private final LockingThread theConnectionLock;
       
    private static final String NAME_Class = ClientPortRouter.class.getSimpleName();
    private OutgoingConnectionManager theOCM;
    
    volatile boolean reconnectEnable = true;
    
      
    //===============================================================
     /**
     * ClientComm constructor
     *
     * @param passedPortManager
     * @param passedBool
     * @throws IOException
    */
    public ClientPortRouter( PortManager passedPortManager, boolean passedBool ) throws IOException {
        super(passedPortManager, passedBool, Constants.Executor );
        
        //Create server connection manager
        theOCM = new OutgoingConnectionManager();
        
        theConnectionLock = new LockingThread( Constants.Executor );
        theConnectionLock.start();
        
    }

    //===============================================================
     /**
      * 
      * @return 
    */
    public OutgoingConnectionManager getSCM() {
        return theOCM;
    }
    
    
    //===============================================================
     /**
     * Recursive function for connecting to the server
     *
     * @return
     * @throws IOException
    */
    private boolean connect( int channelId, InetAddress hostAddress, int passedPort ) throws LoggableException {

        SocketChannelHandler theSCH = theOCM.getSocketChannelHandler( channelId );
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
                ConnectHandler connectHandler = new ConnectHandler( this, channelId );

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
                theSCH = theOCM.getSocketChannelHandler( channelId );
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
     * Set reconnect enable
     * @param passedFlag
     */
    public void setReconnectFlag(boolean passedFlag ){
        reconnectEnable = passedFlag;
    }

    //===============================================================
    /**
    * Adds an event for the selector that the client is interested in opening
    * a connection.
    *
    * @return
    * @throws IOException
    */
    private boolean initiateConnection( int channedId, LockListener passedListener, InetAddress hostAddress, int passedPort, int retry ) throws LoggableException {

        int sleepTime = SLEEP_TIME;
        boolean connected = false;
           
        //Block until we get the lock
        int retVal = -1;
        while( retVal != LockingThread.LOCK ){
            theConnectionLock.lock( passedListener );
            retVal = passedListener.waitForLock();
        }

        DebugPrinter.printMessage( NAME_Class, "Obtained connection lock");
            
        try {
            //Call the recursive function for connecting
            while( retry > 0 && !connected ){

                connected =  connect( channedId, hostAddress, passedPort );

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


                    SocketChannelHandler aSC = theOCM.getSocketChannelHandler( channedId );   
                    if( aSC != null ){

                        //Get the message sender
//                        try {
                            
//                            ControlMessageManager aCMManager = ControlMessageManager.getControlMessageManager();
//                            if( aCMManager == null ){
//                                aCMManager = ControlMessageManager.initialize(thePortManager);
//                            }
                            
                            //Send register message
                            RegisterMessage aMsg = new RegisterMessage( RegisterMessage.REG, channedId);
                            DataManager.send( thePortManager, aMsg );
                            
                            //Wait for the registration to complete
                            waitForConnection();
                            
//                        } catch(IOException ex){
//                            throw new LoggableException(ex);
//                        }


                    } else {
                        connected = false;
                    }

                }

            }
            
        } finally {
        
            theConnectionLock.unlock();
            DebugPrinter.printMessage(this.getClass().getSimpleName(), "Released connection lock");
        }

        return connected;
    }

    //===============================================================
    /**
     *  Connection dropped.
     * 
     * @param theHandler 
     */
    @Override
    public void socketClosed( SocketChannelHandler theHandler ){
        
        int channelId = theHandler.getChannelId();
        theOCM.removeHandler( channelId );
        
        //Stop the keepalive
        KeepAliveTimer aKAT = theOCM.getKeepAliveTimer( channelId );
        if( aKAT != null )
            aKAT.shutdown();

        DebugPrinter.printMessage(NAME_Class, "Socket closed.");
        ReconnectTimer aReconnectTimer = theOCM.getReconnectTimer(channelId);
        
        //Create one if it doesn't exist
        if( aReconnectTimer == null && channelId == ConnectionManager.COMM_CHANNEL_ID )
            aReconnectTimer = new ReconnectTimer(ConnectionManager.COMM_CHANNEL_ID); 
        
        if( aReconnectTimer != null && !aReconnectTimer.isRunning() && reconnectEnable ){
            
            DebugPrinter.printMessage(NAME_Class, "Starting Reconnect Timer");
            aReconnectTimer.clearTimes();
                        
            //Create the calendar
            Calendar theCalendar = Calendar.getInstance(); 
            theCalendar.setTime( new Date() );
               
            //Add 1 Minute
            theCalendar.add( Calendar.MINUTE, 2);
            Date aTime = theCalendar.getTime();
                
            //Format and add to the queue
            String dateStr = Constants.CHECKIN_DATE_FORMAT.format(aTime);
            aReconnectTimer.addReconnectTime( dateStr );
            
            //Add 5 Mins
            theCalendar.add( Calendar.MINUTE, 5);
            aTime = theCalendar.getTime();
                
            //Format and add to the queue
            dateStr = Constants.CHECKIN_DATE_FORMAT.format(aTime);
            aReconnectTimer.addReconnectTime( dateStr );
            
            //Add 10 Mins
            theCalendar.add( Calendar.MINUTE, 10);
            aTime = theCalendar.getTime();
                
            //Format and add to the queue
            dateStr = Constants.CHECKIN_DATE_FORMAT.format(aTime);
            aReconnectTimer.addReconnectTime( dateStr );
            
            //Add 15 Mins
            theCalendar.add( Calendar.MINUTE, 15);
            aTime = theCalendar.getTime();
                
            //Format and add to the queue
            dateStr = Constants.CHECKIN_DATE_FORMAT.format(aTime);
            aReconnectTimer.addReconnectTime( dateStr );
            
            for( int i= 0; i < 7 * 23; i++ ){
                
                theCalendar.add( Calendar.HOUR_OF_DAY, 1 );
                aTime = theCalendar.getTime();
                
                //Format and add to the queue
                dateStr = Constants.CHECKIN_DATE_FORMAT.format(aTime);
                aReconnectTimer.addReconnectTime( dateStr );
                
            }
        
            //Execute it
            aReconnectTimer.start();
        }
        
    };
    
     //===============================================================
    /**
     *  Shuts down the handler and selector
     */
    @Override
    public void shutdown(){

           
        theSelectionRouter.shutdown();
        theConnectionLock.shutdown();
        
        //Shut down the handler 
        theOCM.shutdown();

    }
    
    //===============================================================
    /**
     * Checks that a connection has been made to the passed port.  If not it
     * creates one.
     *
     * @param serverIp
     * @param passedPort
     * @param passedListener
     * @param passedIdArr
     * @return 
    */
    public synchronized int ensureConnectivity( String serverIp, int passedPort, LockListener passedListener, Integer... passedIdArr ) {

        int channelId = 0;
        try {
            
            //Get the channelId
            if( passedIdArr.length > 0){
                channelId = passedIdArr[0];
            } else {
                channelId = theOCM.getNextChannelId();
            }
            
            //Get the handler
            SocketChannelHandler aSC = theOCM.getSocketChannelHandler( channelId );            
            if(aSC == null || aSC.getState() == Constants.DISCONNECTED){            
           
                //Get the inet
                InetAddress srvInet = InetAddress.getByName(serverIp);
                DebugPrinter.printMessage( NAME_Class, "Attempting to connect to " + srvInet.getHostAddress() + ":" + passedPort);
     
                if( !initiateConnection( channelId, passedListener, srvInet, passedPort, CONNECT_RETRY )){
                    RemoteLog.log(Level.INFO, NAME_Class, "isConnected()", "Unable to connect to port " + passedPort, null );
                    channelId = 0x0;
                } else {
                
                    //Set flag
                    aSC = theOCM.getSocketChannelHandler( channelId );  
                    if( aSC == null || aSC.getState() == Constants.DISCONNECTED ){
                        channelId = 0x0;
                    } else {
                        
                        if( channelId == ConnectionManager.COMM_CHANNEL_ID ){
                            //Set the connected flag
                            KeepAliveTimer theKeepAliveTimer = new KeepAliveTimer( thePortManager, channelId);
                            theKeepAliveTimer.start();   

                            //Set timer
                            theOCM.setKeepAliveTimer( channelId, theKeepAliveTimer );
                        }
                        
                    }
                }
        
            }
     
        } catch ( EmptyStackException | UnknownHostException | LoggableException ex) {
            RemoteLog.log(Level.INFO, NAME_Class, "checkConnection()", ex.getMessage(), ex );
            channelId = 0x0;
        }

        return channelId;
    }  

    //==========================================================================
    /**
     * 
     * @param passedIdArr
     * @return 
     */    
    @Override
    public OutgoingConnectionManager getConnectionManager( Integer... passedIdArr ) {
        ClientConfig theClientConfig = ClientConfig.getConfig();
        int servId = theClientConfig.getServerId();
        if( passedIdArr.length != 0){
            int passedId = passedIdArr[0];
            if( passedId != -1 && passedId != servId )
                return null;            
        }
        return theOCM;
    }
    
    //==========================================================================
    /**
     * 
     * @param aCM
     * @param srcId 
     */
    @Override
    public void setConnectionManager( ConnectionManager aCM, Integer... srcId ) {
                    
        if( aCM instanceof OutgoingConnectionManager ){
            OutgoingConnectionManager anOCM = (OutgoingConnectionManager)aCM;
            theOCM = anOCM;
        }
      
    }

}/* END CLASS ClientPortRouter */
