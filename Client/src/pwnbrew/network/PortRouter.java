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
 *  PortRouter.java
 *
 *  Created on Jun 2, 2013
 */

package pwnbrew.network;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import javax.net.ssl.SSLContext;
import pwnbrew.log.LoggableException;
import pwnbrew.manager.ConnectionManager;
import pwnbrew.manager.PortManager;
import pwnbrew.utilities.SSLUtilities;
import pwnbrew.selector.SelectionRouter;
import pwnbrew.utilities.DebugPrinter;

/**
 *
 *  
 */
abstract public class PortRouter {
    
    protected final SelectionRouter theSelectionRouter;
    private final boolean encrypted;
    protected final PortManager thePortManager;
    
    private volatile boolean notified = false;
    private volatile boolean waiting = false;
    
    private SSLContext theSSLContext = null;
    
    public static final int CONTROL_CHANNEL_ID = 1;
    private static final String NAME_Class = PortRouter.class.getSimpleName();
    
    private final Map<Integer, ConnectionCallback> connectionCallbackMap = new HashMap();
  
    //===============================================================
    /**
     * PortRouter constructor
     *
     * @param passedManager the listener for the created comm
     * @param passedBool
     * @param passedExecutor
     * @throws IOException
     */
    public PortRouter(PortManager passedManager, boolean passedBool, Executor passedExecutor ) throws IOException { // NO_UCD (use default)

        thePortManager = passedManager;       
        encrypted = passedBool;
        
        //Create the selection router and start it
        theSelectionRouter = new SelectionRouter( passedExecutor );
        theSelectionRouter.start();
    }

    //===============================================================
    /**
     * Returns the selection router for the comm
     *
     * @return
    */
    public SelectionRouter getSelRouter() {
        return theSelectionRouter;
    }
    
    //===============================================================
    /**
     * Returns the comm manager
     *
     * @return
     */
    public PortManager getPortManager() {
        return thePortManager;
    }
    
    //===============================================================
    /**
     *  Handles when the socket is closed
     * 
     * @param clientId
     * @param channelId 
    */
    abstract public void socketClosed( int clientId, int channelId );
 
    
    //===============================================================
    /**
     *  Returns whether the comm is using encryption.
     * @return 
     */
    final public boolean isEncrypted() {
        return encrypted;
    }

    // ==========================================================================
    /**
    * Causes the calling {@link Thread} to <tt>wait()</tt> until notified by
    * another.
    * <p>
    * <strong>This method "blocks" for 3 seconds at which point it returns
     * with whether is was notified or not.</strong>
     * @return 
    */
    protected synchronized boolean waitForConnection() {

        boolean timedOut = true;
        waiting = true;
        while( notified == false ) { //Until notified...

            try {
                wait(60000); //Wait here until notified
                if(notified){
                    //Return that the thread was notified
                    timedOut = false;
                    break;

                } else {
                    break;
                }

            } catch( InterruptedException ex ) {
            }
        }
        waiting = false;
        notified = false;
        return timedOut;
    }
    
    // ==========================================================================
    /**
    *  Notifies the {@link Comm}
    *
    */
    public synchronized void beNotified() {

        DebugPrinter.printMessage( this.getClass().getSimpleName(), "Notified.");
        if(waiting){
            DebugPrinter.printMessage( this.getClass().getSimpleName(), "Was waiting.");

            notified = true;
            notifyAll(); //Notify the thread
        }
    }/* END beNotified() */
     
    //===============================================================
    /**
     * Returns the SSL context for the comm
     *
     * @param client
     * @return
     * @throws pwnbrew.log.LoggableException
     */
    public synchronized SSLContext getSSLContext( boolean client ) throws LoggableException {

        //If the context has not be created than create it
        if ( theSSLContext == null ){       
            if( client )
                theSSLContext = SSLUtilities.createSSLContext();    
            else
                theSSLContext = SSLUtilities.createServerSSLContext();            
        }

        return theSSLContext;
    }
    
    //===============================================================
    /**
    * Closes and removes any connections provided by passed InetAddress
    *
     * @param passedId
     * @return 
    */
    abstract public ConnectionManager getConnectionManager( Integer... passedId );
    
    //==========================================================================
    /**
     * 
     * @param srcId
     * @param anICM 
     */
    abstract public void setConnectionManager( ConnectionManager anICM, Integer... srcId );
     

    //===============================================================
    /**
     *  Shuts down the handler and selector
     */
    abstract public void shutdown();

    //===============================================================
    /**
     * 
     * @param channelId
     * @return 
     */
    public ConnectionCallback removeConnectionCallback(int channelId) {
        ConnectionCallback aCC;
        synchronized(connectionCallbackMap){
            aCC = connectionCallbackMap.get(channelId);
        }        
        return aCC;
    }
    
    //===============================================================
    /**
     * 
     * @param channelId 
     * @param aCC 
     */
    public void setConnectionCallback( int channelId, ConnectionCallback aCC ) {
        synchronized(connectionCallbackMap){
            connectionCallbackMap.put(channelId, aCC);
        }        
    }
}
