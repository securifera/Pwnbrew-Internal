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
import pwnbrew.selector.SocketChannelHandler;

/**
 *
 * @author Securifera
 */
public class IncomingConnectionManager extends ConnectionManager {
    
    private final int theClientId;
    private final Map<Integer, SocketChannelHandler> channelIdHandlerMap = new HashMap<>();

    //==========================================================================
    /**
     * 
     * @param passedClientId 
     */
    public IncomingConnectionManager(int passedClientId) {
        theClientId = passedClientId;
    }

    //==========================================================================
    /**
     * 
     * @return 
     */
    public int getClientId() {
        return theClientId;
    }

    //==========================================================================
    /**
     * 
     * @param channelId
     * @param theHandler
     * @return 
     */
    @Override
    public boolean setHandler(int channelId, SocketChannelHandler theHandler) {        
         
        boolean retVal;
        
        retVal = verifyHandlerId(channelId);
        if( retVal ){
            synchronized(channelIdHandlerMap){
                channelIdHandlerMap.put(channelId, theHandler);
            }
        }
        
        return retVal;
    }
    
    //==========================================================================
    /**
     * 
     * @param channelId 
     */
    @Override
    public void removeHandler( int channelId ) {
        
        synchronized(channelIdHandlerMap){
            channelIdHandlerMap.remove(channelId);
            if( channelId == COMM_CHANNEL_ID ){
                //Shutdown the rest if the comm channel is being removed them clear
                for( SocketChannelHandler aHandler : channelIdHandlerMap.values() ){
                    aHandler.shutdown();
                }
                channelIdHandlerMap.clear();
            }                           
        }
    }

    //==========================================================================
    /**
     * Return the socket handler
     * @param channelId
     * @return 
     */
    @Override
    public SocketChannelHandler getSocketChannelHandler(Integer channelId) {
        SocketChannelHandler aHandler;
        synchronized(channelIdHandlerMap){
            aHandler = channelIdHandlerMap.get(channelId);
        }
        return aHandler;
    }

    //==========================================================================
    /**
     * 
     * @param channelId
     * @return 
     */
    private boolean verifyHandlerId(int channelId) {
        
        boolean retVal = true;
        synchronized(channelIdHandlerMap){
            
            //Check if the channel id is staging
            if( channelId == STAGE_CHANNEL_ID){
                if( !channelIdHandlerMap.isEmpty())
                    //Make sure this is the first connection
                    retVal = false;     
                
            } else {
                SocketChannelHandler commChan = channelIdHandlerMap.get(COMM_CHANNEL_ID);
                //Make sure the comm channel is defined in the right order
                if( (commChan == null && (channelId != COMM_CHANNEL_ID )) || 
                    commChan != null && channelId == COMM_CHANNEL_ID )
                    retVal = false;
            }
        }
        
        return retVal;
        
    }

    //====================================================================
    /**
     * 
     */
    @Override
    public void shutdown() {
          
        //Shutdown everything
        synchronized(channelIdHandlerMap){
            Set<Integer> channelKeySet = channelIdHandlerMap.keySet();
            for( Integer aKey : channelKeySet ){
                SocketChannelHandler aHandler = channelIdHandlerMap.get(aKey);
                aHandler.shutdown();
            }
            channelIdHandlerMap.clear();
        }
    }

}
