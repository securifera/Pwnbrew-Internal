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
 *  DataManager.java
 *
 *  Created on Jun 10, 2013 8:24:31 PM
 */

package pwnbrew.manager;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import pwnbrew.exception.RemoteExceptionWrapper;
import pwnbrew.log.Log;
import pwnbrew.log.LoggableException;
import pwnbrew.misc.DebugPrinter;
import pwnbrew.network.DataHandler;
import pwnbrew.network.Message;
import pwnbrew.network.PortRouter;
import pwnbrew.network.PortWrapper;
import pwnbrew.network.ServerPortRouter;
import pwnbrew.network.control.ControlMessageManager;
import pwnbrew.network.file.FileMessageManager;
import pwnbrew.network.http.ServerHttpWrapper;
import pwnbrew.network.relay.RelayManager;
import pwnbrew.network.shell.ShellMessageManager;
import pwnbrew.network.stage.StagingMessageManager;
import pwnbrew.selector.SocketChannelHandler;
import pwnbrew.utilities.SocketUtilities;
import pwnbrew.xmlBase.ServerConfig;
/**
 *
 *  
 */
abstract public class DataManager {    

    protected DataHandler theDataHandler;
    protected final PortManager thePortManager;
    protected int operatingPort;
    
    private static final Map<Integer, PortWrapper> thePortWrapperMap = new HashMap<>();
    private transient static final String NAME_Class = DataManager.class.getSimpleName();
    

    //===========================================================================
    /**
     *  Constructor
     * @param passedProvider
     */
    public DataManager( PortManager passedProvider ) {
        thePortManager = passedProvider ;
    }  
    
    //===========================================================================
    /**
     *  Get the the port wrapper.
     * 
     * @param passedPort 
     * @return  
     */
    public static PortWrapper getPortWrapper( int passedPort ) {
        return thePortWrapperMap.get( passedPort );
    }
    
     //===========================================================================
    /**
     *  Set the the port wrapper.
     * 
     * @param passedPort 
     * @param passedWrapper 
     */
    public static void setPortWrapper( int passedPort, PortWrapper passedWrapper) {
        thePortWrapperMap.put( passedPort, passedWrapper );
    }
    
    //===========================================================================
    /*
     *  Returns the comm manager
     */
    public PortManager getPortManager(){
        return thePortManager;
    }
    //===========================================================================
    /*
     *  Returns whether the passed type is a known message type
     */
    public static boolean isValidType(byte type) {
        
        boolean retVal = true;
        switch(type){
            
            case Message.REGISTER_MESSAGE_TYPE:
                break;
            case Message.STAGING_MESSAGE_TYPE:
                break;
            case Message.CONTROL_MESSAGE_TYPE:
                break;
            case Message.PROCESS_MESSAGE_TYPE:
                break;
            case Message.FILE_MESSAGE_TYPE:
                break;            
            default:
                retVal = false;
                break;
                
        }
        return retVal;
        
    }
    
    //===========================================================================
    /*
     *  Returns the data handler
    */
    public DataHandler getDataHandler() {
        return theDataHandler;
    }  

    //===========================================================================
    /*
     *  Sets the data handler
    */
    public void setDataHandler( DataHandler passedHandler ) {
        theDataHandler = passedHandler;
    }     
    
     //===============================================================
    /**
     *   Handle the message.
     *
     * @param srcPortRouter
     * @param msgBytes
     * @throws pwnbrew.exception.RemoteExceptionWrapper
    */
    abstract public void handleMessage( PortRouter srcPortRouter, byte[] msgBytes ) throws RemoteExceptionWrapper;
    
     //===========================================================================
    /**
     *  Handles the passed message with the correct manager
     * 
     * @param passedRouter
     * @param type
     * @param msgBytes  
     * @param dstId  
     */
    public static void routeMessage( PortRouter passedRouter, byte type, int dstId, byte[] msgBytes ) {
        
        try {
            
            //Get the comm manager
            PortManager theCommManager = passedRouter.getPortManager();
                        
            //Get the config
            DataManager aManager = null;
            ServerConfig aConf = ServerConfig.getServerConfig();
            int serverId = Integer.parseInt( aConf.getHostId() );            
            if( dstId != serverId && dstId != -1){ 
                
                aManager = RelayManager.getRelayManager();
                if( aManager == null){
                    aManager = RelayManager.initialize(theCommManager);
                }   
                
                //Reconstruct the msg
                byte[] msgLen = new byte[Message.MSG_LEN_SIZE];
                byte[] tmpBytes = new byte[msgBytes.length + msgLen.length + 1];
                tmpBytes[0] = type;

                //Copy length
                SocketUtilities.intToByteArray(msgLen, msgBytes.length );
                System.arraycopy(msgLen, 0, tmpBytes, 1, msgLen.length);
                                
                //Copy payload
                System.arraycopy(msgBytes, 0, tmpBytes, msgLen.length + 1, msgBytes.length);
                                
                //Reassign
                msgBytes = tmpBytes;
            
            } else {
            
                //Pass the message to the right handler
                switch( type ){   
                     case Message.STAGING_MESSAGE_TYPE:

                        aManager = StagingMessageManager.getMessageManager();
                        if( aManager == null){
                            aManager = StagingMessageManager.initialize(theCommManager);
                        }
                        break;
                    case Message.CONTROL_MESSAGE_TYPE:

                        aManager = ControlMessageManager.getMessageManager();
                        if( aManager == null){
                            aManager = ControlMessageManager.initialize(theCommManager);
                        }
                        break;
                    case Message.PROCESS_MESSAGE_TYPE:

                        aManager = ShellMessageManager.getMessageManager();
                        if( aManager == null){
                            aManager = ShellMessageManager.initialize(theCommManager);
                        }
                        break;
                    case Message.FILE_MESSAGE_TYPE:
                        aManager = FileMessageManager.getMessageManager();
                        if( aManager == null){
                            aManager = FileMessageManager.initialize(theCommManager);
                        }
                        break;            
                    default:
                        break;

                }
            }
            
            //Handle it
            if( aManager != null )
                aManager.handleMessage( passedRouter, msgBytes );            
            
        } catch (LoggableException | IOException ex) {
            DebugPrinter.printMessage(DataManager.class.getSimpleName(), "No manager for bytes");                                 
        } catch (RemoteExceptionWrapper ex) {
            send( passedRouter.getPortManager(), ex.getRemoteExceptionMsg() );
        }
    }
    
    //===============================================================
    /**
     * Single point for sending out messages from the server  
     *
     * @param passedCommManager
     * @param passedMessage
    */
    public static void send( PortManager passedCommManager, Message passedMessage ) {
    
        //Get the port router
        try {
            
            ServerConfig aConf = ServerConfig.getServerConfig();
            int serverPort = aConf.getSocketPort();
            int destClientId = passedMessage.getDestHostId();
            int channelId = passedMessage.getChannelId();
            
            //Try the default port router
            PortRouter thePR = passedCommManager.getPortRouter( serverPort );
            ConnectionManager aCM = thePR.getConnectionManager(destClientId);
            
            //Get the scoket handler
            SocketChannelHandler theHandler = aCM.getSocketChannelHandler(channelId);
            if( theHandler == null ){
                
                RelayManager aRelayManager = RelayManager.getRelayManager();
                if( aRelayManager == null )
                    aRelayManager = RelayManager.initialize(passedCommManager);
                
                //See if the relay port router has the client id
                thePR = aRelayManager.getServerPorterRouter();
                aCM = thePR.getConnectionManager(destClientId);
                if( aCM != null ){
                    theHandler = aCM.getSocketChannelHandler(channelId);
                    if( theHandler == null ){
                        Log.log( Level.SEVERE, NAME_Class, "send()", "Not connected to the specified channel.", null);      
                        return;
                    }
                    
                } else {
                    Log.log( Level.SEVERE, NAME_Class, "send()", "Not connected to the specified client.", null);      
                    return;
                }
            }                        

            ByteBuffer aByteBuffer;
            int msgLen = passedMessage.getLength();
            aByteBuffer = ByteBuffer.allocate( msgLen );
            passedMessage.append(aByteBuffer);
        
            //Create a byte array from the messagen byte buffer
            byte[] msgBytes = Arrays.copyOf( aByteBuffer.array(), aByteBuffer.position());
         
            //If wrapping is necessary then wrap it
            if( theHandler.isWrapping() ){
                PortWrapper aWrapper = DataManager.getPortWrapper( theHandler.getPort() );        
                if( aWrapper != null ){
                    
                    //Set the staged wrapper if necessary
                    if( aWrapper instanceof ServerHttpWrapper ){
                        ServerHttpWrapper aSrvWrapper = (ServerHttpWrapper)aWrapper;
                        aSrvWrapper.setStaging( theHandler.isStaged());
                    }
                    
                    aByteBuffer = aWrapper.wrapBytes( msgBytes );  
                    msgBytes = Arrays.copyOf(aByteBuffer.array(), aByteBuffer.position());
                } 
            }
            
            theHandler.queueBytes(msgBytes);
    //        DebugPrinter.printMessage(NAME_Class, "Queueing " + passedMessage.getClass().getSimpleName() + " message");
        } catch (IOException | LoggableException ex) {
            Log.log( Level.SEVERE, NAME_Class, "send()", ex.getMessage(), ex);           
        }
    }

    //===========================================================================
    /**
     *  Set the operating port
     * @param passedPort 
     */
    public void setPort( int passedPort ) {
        operatingPort = passedPort;
    }
    
    //===========================================================================
    /**
     *  Get the operating port
     
     * @return 
     */
    public int getPort() {
        return operatingPort;
    }
    
    //===========================================================================
    /**
     *  Shutdown the handler 
     */
    public void shutdown() {
        theDataHandler.shutdown();
    }
    
    //===========================================================================
    /**
     *  Creates the port router
     * @param passedManager
     * @param encrypted
     * @param passedPort
     * @throws java.io.IOException
     */
    public static synchronized void createPortRouter( PortManager passedManager, int passedPort, boolean encrypted ) throws IOException{
        PortRouter aPR = passedManager.getPortRouter( passedPort );
        if( aPR == null ){
            aPR = new ServerPortRouter( passedManager, encrypted, false );
            passedManager.setPortRouter( passedPort, aPR);            
        }
    }
    
}
