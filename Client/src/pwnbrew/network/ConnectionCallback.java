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

import pwnbrew.utilities.ReconnectTimer;

/**
 *
 * @author Securifera
 */
public class ConnectionCallback {
    
    protected final int socketPort;
    protected int channelId = -1;
    protected final String serverIp;
    protected final ReconnectTimer theReconnectThread;
//    private volatile boolean notified = false;
//    private volatile boolean waiting = false;
    
    private static final String NAME_Class = ConnectionCallback.class.getSimpleName();
    
    //=================================================================
    /**
     * 
     * @param passedIp
     * @param passedPort 
     * @param passedThread 
     */
    public ConnectionCallback( String passedIp, int passedPort, ReconnectTimer passedThread ){
        serverIp = passedIp;
        socketPort = passedPort;
        theReconnectThread = passedThread;
    }
    
    //=================================================================
    /**
     * 
     * @param passedId
     */
    public void handleConnection( int passedId ){
        channelId = passedId;
        theReconnectThread.beNotified();  
    }
    
    //=================================================================
    /**
     * 
     * @return 
     */
    public String getServerIp(){
        return serverIp;
    }
    
    //=================================================================
    /**
     * 
     * @return 
     */
    public int getPort(){
        return socketPort;
    }
    
     //=================================================================
    /**
     * 
     * @return 
     */
    public int getChannelId(){
        return channelId;
    }
    
//     //=================================================================
//    /**
//     * 
//     * @return 
//     */
//    public boolean isNotified() {
//        return notified;
//    }
//
//     //=================================================================
//    /**
//     * 
//     * @param passedVal
//     */
//    public void setNotified(boolean passedVal) {
//        notified = passedVal;
//    }
//    
//     // ==========================================================================
//    /**
//    * Causes the calling {@link Thread} to <tt>wait()</tt> until notified by
//    * another.
//    * <p>
//    * <strong>This method "blocks" for 60 seconds at which point it returns
//     * with whether is was notified or not.</strong>
//     * @return 
//    */
//    protected synchronized boolean waitForConnection() {
//        
//        boolean timedOut = true;
//        waiting = true;
//        while( notified == false ) { //Until notified...
//
//            try {
//                wait(5000); //Wait here until notified
//                if(notified){
//                    //Return that the thread was notified
//                    timedOut = false;
//                } else {
//                    DebugPrinter.printMessage( NAME_Class, "Thread woke notified variable not set.");
//                }
//                
//                break;
//
//            } catch( InterruptedException ex ) {
//                DebugPrinter.printMessage( NAME_Class, "Connection thread interrupted.");
//                break;
//            }
//        }
//        
//        waiting = false;
//        return timedOut;
//    
//    }
    
//     // ==========================================================================
//    /**
//    *  Notifies the thread waiting for the socket connection to complete
//    *
//    */
//    public synchronized void beNotified() {
//
//        if(waiting){
//            notified = true;
//            notifyAll(); 
//        } 
//        
//    }        
    
}
