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
 *  KeepAliveTimer.java
 *
 *  Created on June 7, 2013
 */

package pwnbrew.network;

import pwnbrew.utilities.ManagedRunnable;
import pwnbrew.utilities.DebugPrinter;
import pwnbrew.utilities.Constants;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import pwnbrew.ClientConfig;
import pwnbrew.log.RemoteLog;
import pwnbrew.log.LoggableException;
import pwnbrew.manager.PortManager;
import pwnbrew.network.control.ControlMessageManager;
import pwnbrew.network.control.messages.NoOp;
import pwnbrew.selector.SocketChannelHandler;

/**
 *
 *  
 */
public class KeepAliveTimer extends ManagedRunnable {
    
    private PortManager theCommManager = null;
    private final SecureRandom aSR = new SecureRandom();
    private volatile boolean connected = true;
    final Object syncedObject = new Object();
    final int channelId;
    
    //Static instance
    private static final String NAME_Class = KeepAliveTimer.class.getSimpleName();
           

    // ==========================================================================
    /**
     * Constructor
     *
     * @param passedManager
    */
    @SuppressWarnings("ucd")
    public KeepAliveTimer(PortManager passedManager, int passedId ) {
        super(Constants.Executor);
        theCommManager = passedManager;
        channelId = passedId;
    }
    
    // ==========================================================================
    /**
     *  Main thread loop
     *
    */
    @Override
    public void go() {
                                   
        //Loop while connected
        while( isConnected() && !shutdownRequested ){

            //Get the next sleep time
            int sleepTime = Math.abs( aSR.nextInt() % 300 );

            Calendar theCalendar = Calendar.getInstance(); 
            theCalendar.setTime( new Date() );
            theCalendar.add(Calendar.SECOND, sleepTime );

            //Wait until the random time
            waitUntil(theCalendar.getTime());  
            try {

                ControlMessageManager aCMManager = ControlMessageManager.getControlMessageManager();
                if( aCMManager == null ){
                    aCMManager = ControlMessageManager.initialize( theCommManager );
                }

                //Get the socket router
                ClientPortRouter aPR = (ClientPortRouter) theCommManager.getPortRouter( ClientConfig.getConfig().getSocketPort() );
                if(aPR != null){

                    //Create the connection
                    SocketChannelHandler aHandler = aPR.getConnectionManager().getSocketChannelHandler(channelId);
                    if( aHandler != null && aHandler.getState() == Constants.CONNECTED ){
                        //Send noop to keepalive
                        NoOp aNoOp = new NoOp();                        
                        aCMManager.send( aNoOp );
                    }
                }

            } catch ( IOException | LoggableException ex) {
                RemoteLog.log(Level.SEVERE, NAME_Class, "start()", ex.getMessage(), ex);
            }         

        }
        
    }
    
    // ==========================================================================
    /**
     *  Gets the connected flag 
     * @return  
     */
    public boolean isConnected(){
        return connected;
    }
    
    // ==========================================================================
    /**
     *  Sets the connected flag
     * @param passedBool 
     */
    public void setConnectedFlag( boolean passedBool ){
        connected = passedBool;
        beNotified();
    }
    
    // ==========================================================================
    /**
     *  Wait until a certain date to reconnect
     * 
     * @param date 
     */
    private void waitUntil(Date date) {
        
        TimerTask aTimerTask = new TimerTask() {
            @Override
            public void run() {
                synchronized (syncedObject) {
                    syncedObject.notify();
                }
            }
        };
        
        DebugPrinter.printMessage(NAME_Class, "Sending a keep alive at " + date.toString());
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
    
    //===============================================================
    /**
    *  Shut down the detector
    */
    @Override
    public void shutdown(){
        
        super.shutdown();
        
        //Stop the timer
        synchronized(syncedObject) {
            syncedObject.notifyAll();
        }        
    }
}
