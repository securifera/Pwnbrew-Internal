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
import java.util.Stack;
import pwnbrew.network.KeepAliveTimer;
import pwnbrew.network.Message;
import pwnbrew.selector.SocketChannelHandler;

/**
 *
 * @author Securifera
 */
public class ServerConnectionManager extends ConnectionManager{
    
    public final static int COMM_CHANNEL_ID = Message.CONTROL_MESSAGE_TYPE; 
    
    private final Stack<Byte> availableKeys = new Stack(); 
    private final Map<Byte, SocketChannelHandler> channelIdHandlerMap = new HashMap<>();
    private final Map<Byte, KeepAliveTimer> theKeepAliveTimerMap = new HashMap<>();
 

    //==========================================================================
    /**
     * 
     */
    public ServerConnectionManager() {        
        //Initialize 
        for( byte i = 2; i < 0xff; i++){
            availableKeys.add(i);
        }
    }

//    //==========================================================================
//    /**
//     * 
//     * @return 
//     */
//    public int getClientId() {
//        return theClientId;
//    }

//    //==========================================================================
//    /**
//     * 
//     * @param channelId
//     * @param theHandler
//     * @return 
//     */
//    public boolean setHandler(int channelId, SocketChannelHandler theHandler) {        
//         
//        boolean retVal;
//        
//        retVal = verifyHandlerId(channelId);
//        if( retVal ){
//            synchronized(channelIdHandlerMap){
//                channelIdHandlerMap.put(channelId, theHandler);
//            }
//        }
//        
//        return retVal;
//    }
    
//   
//    //==========================================================================
//    /**
//     * Return the socket handler
//     * @param channelId
//     * @return 
//     */
//    public SocketChannelHandler getHandler( byte channelId) {
//        SocketChannelHandler aHandler;
//        synchronized(channelIdHandlerMap){
//            aHandler = channelIdHandlerMap.get(channelId);
//        }
//        return aHandler;
//    }

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
            channelIdHandlerMap.put( (byte)( passedId & 0xff ), theAccessHandler);
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
            channelIdHandlerMap.remove((byte)( passedId & 0xff ));
            //Add the key back
            synchronized( availableKeys){
                availableKeys.push((byte)( passedId & 0xff ));
            }
        }
    }
 
    //==========================================================================
    /**
     *  Returns the keep alive timer
     * @param passedId
     * @return 
     */
    public KeepAliveTimer getKeepAliveTimer( Byte passedId ) {
        
        KeepAliveTimer theTimer;
        synchronized(theKeepAliveTimerMap){
            theTimer = theKeepAliveTimerMap.get(passedId);
        }
        return theTimer;
    }
    
    //==========================================================================
    /**
     * 
     */
    @Override
    public void closeConnections() {
  
        synchronized( channelIdHandlerMap ){
            
            synchronized(availableKeys){
                Set<Byte> aSet = channelIdHandlerMap.keySet();
                for( Byte aKey : aSet ){
                    SocketChannelHandler theHandler = getSocketChannelHandler(aKey);
                    if( theHandler != null){      
                        theHandler.shutdown();
                        availableKeys.push(aKey);
                    }

                }
            }
        
        }
    }
    
    //===============================================================
    /**
     *  Returns the SocketChannelHandler for the server.
     * 
     * @param passedId
     * @return 
    */  
    @Override
    public SocketChannelHandler getSocketChannelHandler( int passedId ){
        SocketChannelHandler aSCH;
        synchronized( channelIdHandlerMap){
            aSCH = channelIdHandlerMap.get((byte)( passedId & 0xff ));
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
        Set<Byte> theKeys = theKeepAliveTimerMap.keySet();
        for( Byte aKey : theKeys )
            theKeepAliveTimerMap.get(aKey).shutdown();
        
        theKeys = channelIdHandlerMap.keySet();
        for( Byte aKey : theKeys )
            channelIdHandlerMap.get(aKey).shutdown();
    }

    //================================================================
    /**
     * 
     * @return 
     */
    public Byte getNextChannelId() {
        
        Byte aChannelId;
        synchronized(availableKeys){
            aChannelId = availableKeys.pop();
        }
        return aChannelId;
        
    }

}
