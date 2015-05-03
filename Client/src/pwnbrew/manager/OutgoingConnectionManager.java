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
package pwnbrew.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import pwnbrew.misc.ReconnectTimer;
import pwnbrew.network.KeepAliveTimer;
import pwnbrew.selector.SocketChannelHandler;

/**
 *
 * @author Securifera
 */
public class OutgoingConnectionManager extends ConnectionManager {
    
    
    private final Map<Integer, SocketChannelHandler> channelIdHandlerMap = new HashMap<>();
    private final Map<Integer, KeepAliveTimer> theKeepAliveTimerMap = new HashMap<>();
    private final Map<Integer, ReconnectTimer> theReconnectTimerMap = new HashMap<>();
    
    //Channel Id generator
    private static int messageCounter = 2;
 

    //==========================================================================
    /**
     * 
     */
    public OutgoingConnectionManager() {        
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
        synchronized( channelIdHandlerMap){
            channelIdHandlerMap.put( passedId, theAccessHandler);
        }
    }
    
     //===============================================================
    /**
     *  Removes the client id
     * 
     * @param passedId 
    */
    @Override
    public void removeHandler( int passedId ) {
        synchronized( channelIdHandlerMap ){
            channelIdHandlerMap.remove( passedId );
        }
    }
 
    //==========================================================================
    /**
     *  Returns the keep alive timer
     * @param passedId
     * @return 
     */
    public KeepAliveTimer getKeepAliveTimer( int passedId ) {
        
        KeepAliveTimer theTimer;
        synchronized(theKeepAliveTimerMap){
            theTimer = theKeepAliveTimerMap.get(passedId);
        }
        return theTimer;
    }
    
    //==========================================================================
    /**
     *  Returns the reconnect timer
     * @param passedId
     * @return 
     */
    public ReconnectTimer getReconnectTimer( int passedId ) {
        
        ReconnectTimer theTimer;
        synchronized(theReconnectTimerMap){
            theTimer = theReconnectTimerMap.get(passedId);
        }
        return theTimer;
    }
    
//    //==========================================================================
//    /**
//     * 
//     */
//    @Override
//    public void closeConnections() {
//  
//        synchronized( channelIdHandlerMap ){
//            
//            Set<Integer> aSet = channelIdHandlerMap.keySet();
//            for( Integer aKey : aSet ){
//                SocketChannelHandler theHandler = getSocketChannelHandler(aKey);
//                if( theHandler != null){      
//                    theHandler.shutdown();
//                }
//            }
//            
//        }
//    }
    
    //===============================================================
    /**
     *  Returns the SocketChannelHandler for the server.
     * 
     * @param passedId
     * @return 
    */  
    @Override
    public SocketChannelHandler getSocketChannelHandler( Integer passedId ){
        SocketChannelHandler aSCH;
        synchronized( channelIdHandlerMap){
            aSCH = channelIdHandlerMap.get( passedId );
        }
        return aSCH;
    }
    
    //================================================================
    /**
     * 
     */
    @Override
    public void shutdown() {
        
         //Shutdown the handlers and there keepalives
        Set<Integer> theKeys = theKeepAliveTimerMap.keySet();
        for( Integer aKey : theKeys )
            theKeepAliveTimerMap.get(aKey).shutdown();
        
        theKeys = channelIdHandlerMap.keySet();
        for( Integer aKey : theKeys )
            channelIdHandlerMap.get(aKey).shutdown();
    }

     //===============================================================
    /**
    * Returns an integer to be used for packet ids
    *
    * @return
    */
    public int getNextChannelId(){

        SocketChannelHandler aSH;
        int channelId;
                
        synchronized( channelIdHandlerMap){
            do {
                channelId = messageCounter++;
                aSH = channelIdHandlerMap.get( channelId );
            } while( aSH != null );
        }       

        return channelId;
    }
    
   
}
