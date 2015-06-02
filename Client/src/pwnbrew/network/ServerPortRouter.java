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
* ServerPortRouter.java
*
* Created on June 3, 2013, 10:46:36 PM
*/

package pwnbrew.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import pwnbrew.log.LoggableException;
import pwnbrew.log.RemoteLog;
import pwnbrew.manager.ConnectionManager;
import pwnbrew.manager.IncomingConnectionManager;
import pwnbrew.manager.PortManager;
import pwnbrew.utilities.DebugPrinter;
import pwnbrew.network.control.ControlMessageManager;
import pwnbrew.network.control.messages.RelayDisconnect;
import pwnbrew.selector.AcceptHandler;
import pwnbrew.selector.SocketChannelHandler;


/**
 *
 *  
 */
public class ServerPortRouter extends PortRouter {

    private ServerSocketChannel theServerSocketChannel = null;
    private final Map<Integer, IncomingConnectionManager> clientIdManagerMap= new HashMap<>();
     
    
//    private final Map<Integer, SocketChannelHandler> hostHandlerMap = new HashMap<>();
    
    private AcceptHandler currentHandler = null;    
    private static final String NAME_Class = ServerPortRouter.class.getSimpleName();
  
      
    //===============================================================
     /**
     * ClientComm constructor
     *
     * @param passedCommManager
     * @param passedBool
     * @param passedExecutor
     * @throws IOException
    */
    public ServerPortRouter( PortManager passedCommManager, boolean passedBool, Executor passedExecutor ) throws IOException {
        super(passedCommManager, passedBool, passedExecutor);
    }

    //===============================================================
    /**
     * Returns the server socket channel
     *
     * @return
    */
    public ServerSocketChannel getServerSocketChannel() {
        return theServerSocketChannel;
    }
    
//     //===============================================================
//     /**
//     *  Registers the provided AccessHandler with the server under the
//     * given InetAddress.
//     *
//     * @param passedClientId
//     * @param theHandler
//     */
//    @Override
//    public void registerHandler(int passedClientId, SocketChannelHandler theHandler) {
//
//        if( theHandler != null){
////            DebugPrinter.printMessage(NAME_Class, "Registering " + passedClientId.toString());
//            synchronized(hostHandlerMap){
//                hostHandlerMap.put( passedClientId, theHandler);
//            }
//        }
//    }
    
//  
//    //===============================================================
//    /**
//     *  Removes the client id
//     * 
//     * @param clientId 
//    */
//    @Override
//    public void removeHandler(int clientId) {
////        DebugPrinter.printMessage(NAME_Class, "Removing " + Integer.toString( clientId ));
//        synchronized(hostHandlerMap){
//            hostHandlerMap.remove( clientId );
//        }
//    }
//    
//    //===============================================================
//    /**
//     *  Returns the SocketChannelHandler map.  
//     * 
//     * @return 
//    */  
//    public Map<Integer, SocketChannelHandler> getSocketChannelHandlerMap(){
//        
//        Map<Integer, SocketChannelHandler> retMap;
//        synchronized(hostHandlerMap){
//            retMap = new HashMap<>(hostHandlerMap);
//        }
//        return retMap;
//    }
    
//    //===============================================================
//    /**
//     *  Returns the SocketChannelHandler for the passed address.  
//     * 
//     * @param passedInt
//     * @return 
//    */  
//    @Override
//    public SocketChannelHandler getSocketChannelHandler(Integer... passedInt ){
//        
//        //Get the Address
//        SocketChannelHandler theSCH = null;
//        if( passedInt.length > 0){
//            synchronized(hostHandlerMap){
//                theSCH = hostHandlerMap.get( passedInt[0] );
//            }
//        }
//        return theSCH;
//    }
//    //===============================================================
//    /**
//     *  Returns the SocketChannelHandler for the passed address.  
//     * 
//     * @param clientId
//     * @param channelId
//     * @return 
//    */  
//    @Override
//    public SocketChannelHandler getSocketChannelHandler(Integer clientId, Integer channelId ){
//        
//        //Get the Address
//        SocketChannelHandler theSCH = null;     
//        synchronized(clientIdManagerMap){
//            IncomingConnectionManager aCCM = clientIdManagerMap.get( clientId );
//            if( aCCM != null )
//                theSCH = aCCM.getSocketChannelHandler( channelId );
//            
//        }
//        return theSCH;
//    }
    
    //===============================================================
    /**
     *  Connection dropped.
     * 
     * @param theHandler 
     */
    @Override
    public void socketClosed( SocketChannelHandler theHandler ){
        
        try {
            
            ControlMessageManager aCMManager = ControlMessageManager.getControlMessageManager();
            if( aCMManager == null ){
                aCMManager = ControlMessageManager.initialize( thePortManager );
            }
            
            //Send a disconnect msg
            RelayDisconnect aMsg = new RelayDisconnect( theHandler.getClientId() );
            aCMManager.send(aMsg);
            
        } catch( LoggableException | IOException ex ){
            RemoteLog.log(Level.INFO, NAME_Class, "socketClosed()", ex.getMessage(), null );                    
        }
        
    }
        
    
    //===============================================================
    /**
     * Starts the server on the specified port
     *
     * @param passedAddress
     * @param passedPort
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public void startServer(InetAddress passedAddress, int passedPort ) throws IOException, GeneralSecurityException{

        //Shutdown the server socket
        if(theServerSocketChannel != null){
            int thePort = theServerSocketChannel.socket().getLocalPort();
            if( thePort != passedPort ){
                theServerSocketChannel.close();
            } else {
                return;
            }
        }
         
        //Spawn a new server socket channel
        theServerSocketChannel = createServerSocketChannel(passedAddress, passedPort, false);

        // Register the server socket channel, indicating an interest in
        // accepting new connections
        currentHandler = new AcceptHandler( this );
        theSelectionRouter.register(theServerSocketChannel, SelectionKey.OP_ACCEPT, currentHandler);

    }
    
    //===============================================================
    /**
     *  Returns a generic server socket
     * 
     * @param passedAddress
     * @param passedPort
     * @param blocking
     * @return
     * @throws IOException 
     */
    public static ServerSocketChannel createServerSocketChannel( InetAddress passedAddress, int passedPort, boolean blocking ) throws IOException{
         
        // Create a new non-blocking server socket channel
        ServerSocketChannel aServerSocketChannel = ServerSocketChannel.open();
        aServerSocketChannel.socket().setReuseAddress(true);
        aServerSocketChannel.configureBlocking( blocking );
      
        // Bind the server socket to the specified address and port
        InetSocketAddress isa = new InetSocketAddress(passedAddress, passedPort);
        aServerSocketChannel.socket().bind(isa);
        return aServerSocketChannel;
 
    }
    
    //===============================================================
    /**
     *  Shuts down the everything managed by the port router
     */
    @Override
    public void shutdown() {

//        synchronized(hostHandlerMap){
//            //Loop through and close them
//            for (SocketChannelHandler aHandler : hostHandlerMap.values() ) {                
//                aHandler.shutdown();               
//            }
//        }
        
        //Shutdown each socket
        synchronized( clientIdManagerMap ){
            for (IncomingConnectionManager aManager : clientIdManagerMap.values() )             
                aManager.shutdown();  
            clientIdManagerMap.clear();
        }
        
        //shutdown handler
        theSelectionRouter.unregister(theServerSocketChannel);        
                
         //Shutdown the server socket
        if(theServerSocketChannel != null){
            try {
                DebugPrinter.printMessage(NAME_Class, "Shutting down server port router.");
                theServerSocketChannel.close();
                theServerSocketChannel.socket().close();
            } catch (IOException ex) {
                DebugPrinter.printMessage(NAME_Class, ex.getMessage());
            }
        }
        
        //Clear the handler list
        theSelectionRouter.shutdown();
        
    }   
    
    //==========================================================================
    /**
     * 
     * @return 
     */    
    public Map<Integer, IncomingConnectionManager> getConnectionManagerMap() {
        return clientIdManagerMap;
    }
    
    //==========================================================================
    /**
     * 
     * @param passedId
     * @return 
     */    
    @Override
    public IncomingConnectionManager getConnectionManager( Integer... passedId ) {
        
        IncomingConnectionManager aManager = null;
        if( passedId.length > 0 ){
            synchronized(clientIdManagerMap){
                aManager = clientIdManagerMap.get(passedId[0]);
            }
        }
        return aManager;
    }

    //==========================================================================
    /**
     * 
     * @param aCM
     * @param srcId
     * @param anICM 
     */
    @Override
    public void setConnectionManager( ConnectionManager aCM, Integer... srcId ) {
        
        if( srcId.length > 0 ){
            
            if( aCM instanceof IncomingConnectionManager ){
             
                //Cast it to an incoming connection manager
                IncomingConnectionManager anICM = (IncomingConnectionManager)aCM;
                int theChannelId = srcId[0];
                synchronized(clientIdManagerMap){
                    clientIdManagerMap.put(theChannelId, anICM);
                }
            }
            
        }
        
    }
   
}/* END CLASS ServerPortRouter */
