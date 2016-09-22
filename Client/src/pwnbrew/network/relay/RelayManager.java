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
 *  RelayManager.java
 *
 *  Created on Dec 2, 2013
 */

package pwnbrew.network.relay;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import pwnbrew.ClientConfig;
import pwnbrew.log.RemoteLog;
import pwnbrew.manager.ConnectionManager;
import pwnbrew.manager.DataManager;
import pwnbrew.manager.PortManager;
import pwnbrew.network.DataHandler;
import pwnbrew.network.Message;
import pwnbrew.network.PortRouter;
import pwnbrew.network.PortWrapper;
import pwnbrew.network.ServerPortRouter;
import pwnbrew.network.http.ServerHttpWrapper;
import pwnbrew.selector.SocketChannelHandler;
import pwnbrew.utilities.SocketUtilities;

/**
 *
 *  
 */
public class RelayManager extends DataManager {

    private static RelayManager theRelayManager;
    private ServerPortRouter theServerPortRouter = null;
    public ExecutorService MyExecutor;
    
    private static final String NAME_Class = RelayManager.class.getSimpleName();
    
//    private static final Map<Integer, Integer> childToParentChannelRouteMap = new HashMap<>();
//    private static final Map<Integer, Integer> parentToChildChannelRouteMap = new HashMap<>();
    
    //===========================================================================
    /*
     *  Constructor
     */
    private RelayManager( PortManager passedCommManager ) throws IOException {
        
        super(passedCommManager);   
        MyExecutor = Executors.newSingleThreadExecutor();
        
        //Create the port router
        if( theServerPortRouter == null )
            theServerPortRouter = new ServerPortRouter( passedCommManager, true, MyExecutor );           
        
    }  
    
    // ==========================================================================
    /**
     *   Creates a ControlMessageManager
     * @param passedCommManager
     * @return 
     * @throws java.io.IOException 
     */
    public synchronized static RelayManager initialize( PortManager passedCommManager ) throws IOException {

        //Create the manager
        if( theRelayManager == null ) {
            theRelayManager = new RelayManager( passedCommManager );
        }   
        return theRelayManager;

    }/* END initialize() */
   
    
    // ==========================================================================
    /**
     *   Gets the RelayManager
     * @return 
     */
    public synchronized static RelayManager getRelayManager(){
        return theRelayManager;
    }
    
    //===============================================================
    /**
     *   Send the message out the given channel.
     *
     * @param srcPortRouter
     * @param msgBytes
    */
    @Override
    public void handleMessage( PortRouter srcPortRouter, byte[] msgBytes ) {        
             
        //Get the dest id
        byte[] dstHostIdArr = Arrays.copyOfRange(msgBytes, Message.DEST_HOST_ID_OFFSET, Message.DEST_HOST_ID_OFFSET + 4);
        int destHostId = SocketUtilities.byteArrayToInt(dstHostIdArr);

        PortRouter thePR = thePortManager.getPortRouter( ClientConfig.getConfig().getSocketPort() );
        if( thePR.equals( srcPortRouter))
            thePR = theServerPortRouter;
        
        ConnectionManager aCM = thePR.getConnectionManager(destHostId);
        if( aCM != null ){
            
            //Get the src id
            byte[] srcHostIdArr = Arrays.copyOfRange(msgBytes, Message.SRC_HOST_ID_OFFSET, Message.SRC_HOST_ID_OFFSET + 4);
            int srcHostId = SocketUtilities.byteArrayToInt(srcHostIdArr);

            //Get the channel id
            byte[] channelIdArr = Arrays.copyOfRange(msgBytes, Message.CHANNEL_ID_OFFSET, Message.CHANNEL_ID_OFFSET + 4);
            int channelId = SocketUtilities.byteArrayToInt(channelIdArr);
            
            //If it is a staging message just send on the comm channel
            if( destHostId == -1 && channelId == ConnectionManager.STAGE_CHANNEL_ID )
                channelId = ConnectionManager.COMM_CHANNEL_ID;

            
            SocketChannelHandler theHandler = aCM.getSocketChannelHandler( channelId );
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
                        
                        //Wrap things
                        ByteBuffer aByteBuffer = aWrapper.wrapBytes( msgBytes );  
                        msgBytes = Arrays.copyOf(aByteBuffer.array(), aByteBuffer.position());
                    } 
                }
                
                theHandler.queueBytes(msgBytes);
                
            } else {
                RemoteLog.log( Level.SEVERE, NAME_Class, "handleMessage()", "No socket handler found for the given id.", null);
            }
        }
        
    }
    
    //===========================================================================
    /*
     *  Returns the data handler
    */
    @Override
    public DataHandler getDataHandler() {
        return theDataHandler;
    }  

    //===========================================================================
    /**
     *  Shutdown the relay 
     */
    @Override
    public void shutdown() {
        
        theServerPortRouter.shutdown();
        theServerPortRouter = null;
        
        MyExecutor.shutdownNow();
        MyExecutor = null;
        theRelayManager = null;
    }

    //===========================================================================
    /**
     *  Return the Port Router
     * @return 
    */
    public ServerPortRouter getServerPortRouter() {
        return theServerPortRouter;
    }
    

    
}
