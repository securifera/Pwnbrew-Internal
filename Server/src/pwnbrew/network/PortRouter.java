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
import java.nio.ByteBuffer;
import java.util.Arrays;
import pwnbrew.manager.CommManager;
import pwnbrew.manager.DataManager;
import pwnbrew.misc.Constants;
import pwnbrew.network.http.ServerHttpWrapper;
import pwnbrew.selector.SelectionRouter;
import pwnbrew.selector.SocketChannelHandler;

/**
 *
 *  
 */
abstract public class PortRouter {
    
    protected final SelectionRouter theSelectionRouter;
    private final boolean encrypted;
    private final CommManager theCommManager;
    
    //===============================================================
    /**
     * PortRouter constructor
     *
     * @param passedManager the listener for the created comm
     * @param passedBool
     * @throws IOException
     */
    public PortRouter(CommManager passedManager, boolean passedBool ) throws IOException { // NO_UCD (use default)

        theCommManager = passedManager;       
        encrypted = passedBool;
        
        //Create the selection router and start it
        theSelectionRouter = new SelectionRouter( Constants.Executor );
        theSelectionRouter.start();
    }
    
    abstract public SocketChannelHandler getSocketChannelHandler(Integer passedInt );

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
    public CommManager getCommManager() {
        return theCommManager;
    }
    
    //===============================================================
     /**
     *  Registers the provided SocketChannelHandler with the server under the
     * given InetAddress.
     *
     * @param passedClientId
     * @param theHandler
     */
    abstract public void registerHandler(int passedClientId, SocketChannelHandler theHandler);

    //===============================================================
    /**
     *  Removes the client id
     * 
     * @param clientId 
    */
    abstract public void removeHandler(int clientId);
    
    //===============================================================
    /**
    * Checks if the host is connected to the specified address
    *
    * @param passedClientId 
    * @return
    */
//    @Override
    public int getState( Integer passedClientId ){

        int retVal = 0;
        SocketChannelHandler theHandler = getSocketChannelHandler( passedClientId  );
        if(theHandler != null){
            retVal = theHandler.getState();
        }
        return retVal;
    }
    
    //===============================================================
    /**
     *  Returns whether the comm is using encryption.
     * @return 
     */
    final public boolean isEncrypted() {
        return encrypted;
    }

    //===============================================================
    /**
     *  Queues the byte array to be sent
     * @param byteArr
     * @param passedClientId
     */
    public void queueSend( byte[] byteArr, Integer passedClientId ) throws IOException {
        
        SocketChannelHandler theHandler = getSocketChannelHandler( passedClientId );
        if( theHandler != null ){
            
            //If wrapping is necessary then wrap it
            if( theHandler.isWrapping() ){
                PortWrapper aWrapper = DataManager.getPortWrapper( theHandler.getPort() );        
                if( aWrapper != null ){
                    
                    //Set the staged wrapper if necessary
                    if( aWrapper instanceof ServerHttpWrapper ){
                        ServerHttpWrapper aSrvWrapper = (ServerHttpWrapper)aWrapper;
                        aSrvWrapper.setStaging( theHandler.isStaged());
                    }
                    
                    ByteBuffer aByteBuffer = aWrapper.wrapBytes( byteArr );  
                    byteArr = Arrays.copyOf(aByteBuffer.array(), aByteBuffer.position());
                } 
            }
            
            theHandler.queueBytes(byteArr);
        } else {
            throw new IOException("Not connected to the specified client.");
        }
                
    }

    //===============================================================
    /**
     *  Shuts down the handler and selector
     */
    abstract public void shutdown();

}
