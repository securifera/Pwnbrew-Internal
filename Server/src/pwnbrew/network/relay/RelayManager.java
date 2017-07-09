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
package pwnbrew.network.relay;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.logging.Level;
import pwnbrew.log.Log;
import pwnbrew.log.LoggableException;
import pwnbrew.manager.ConnectionManager;
import pwnbrew.manager.DataManager;
import pwnbrew.manager.PortManager;
import pwnbrew.network.DataHandler;
import pwnbrew.network.Message;
import pwnbrew.network.PortRouter;
import pwnbrew.network.ServerPortRouter;
import pwnbrew.network.control.messages.RemoteException;
import pwnbrew.selector.SocketChannelHandler;
import pwnbrew.utilities.SocketUtilities;
import pwnbrew.xml.ServerConfig;

/**
 *
 *  
 */
public class RelayManager extends DataManager {

    private static RelayManager theRelayManager;
    private ServerPortRouter theServerPortRouter = null;
    
    private static final String NAME_Class = RelayManager.class.getSimpleName();
    
    //===========================================================================
    /*
     *  Constructor
     */
    private RelayManager( PortManager passedCommManager ) throws IOException {
        
        super(passedCommManager);     
        
        //Create the port router
        if( theServerPortRouter == null )
            theServerPortRouter = new ServerPortRouter( passedCommManager, true, true );  
        
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
        byte[] dstHostId = Arrays.copyOfRange(msgBytes, Message.DEST_HOST_ID_OFFSET, Message.DEST_HOST_ID_OFFSET + 4);
        int tempId = SocketUtilities.byteArrayToInt(dstHostId);
               
        //Get the port router
        try {
            
            PortRouter thePR = thePortManager.getPortRouter( ServerConfig.getServerConfig().getSocketPort() );
            if( thePR.equals( srcPortRouter))
                 thePR = theServerPortRouter;

            //Get the channel id
            byte[] channelIdArr = Arrays.copyOfRange(msgBytes, Message.CHANNEL_ID_OFFSET, Message.CHANNEL_ID_OFFSET + 4);
            int channelId = SocketUtilities.byteArrayToInt(channelIdArr);
            
            //Get the src id
            byte[] srcHostIdArr = Arrays.copyOfRange(msgBytes, Message.SRC_HOST_ID_OFFSET, Message.SRC_HOST_ID_OFFSET + 4);
            int srcHostId = SocketUtilities.byteArrayToInt(srcHostIdArr);                    
            
            //Get the socketchannel handler
            ConnectionManager aCM = thePR.getConnectionManager(tempId);
            if( aCM != null ){
                SocketChannelHandler theHandler = aCM.getSocketChannelHandler( channelId );
                if( theHandler != null ){
                    theHandler.queueBytes(msgBytes);
                    return;
                }
            } 
            
             //Set error msg
            String errMsg = "No socket handler found for the given host/channel id.";                    

            //Send back error msg
            RemoteException exceptionMsg = new RemoteException(srcHostId, errMsg);
            exceptionMsg.setChannelId(channelId);

            aCM = srcPortRouter.getConnectionManager(srcHostId);
            if( aCM != null ){
                SocketChannelHandler theHandler = aCM.getSocketChannelHandler( channelId );
                if( theHandler != null ){
                    byte[] retBytes = exceptionMsg.getBytes();
                    theHandler.queueBytes(retBytes);
                }                    
            }

            Log.log( Level.SEVERE, NAME_Class, "handleMessage()", errMsg, null);  
        } catch (LoggableException | UnsupportedEncodingException ex) {
            Log.log( Level.SEVERE, NAME_Class, "handleMessage()", ex.getMessage(), ex);        
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
     *  Return the Port Router
     * @return 
    */
    public ServerPortRouter getServerPorterRouter() {
        return theServerPortRouter;
    }
    
     //===========================================================================
    /**
     *  Shutdown the relay 
     */
    @Override
    public void shutdown() {
        
        if( theServerPortRouter != null )
            theServerPortRouter.shutdown();
        
        theServerPortRouter = null;
        theRelayManager = null;
    }

}
