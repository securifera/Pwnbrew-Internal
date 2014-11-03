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

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.security.GeneralSecurityException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import pwnbrew.host.Host;
import pwnbrew.host.HostController;
import pwnbrew.manager.PortManager;
import pwnbrew.manager.ServerManager;
import pwnbrew.selector.AcceptHandler;
import pwnbrew.selector.SocketChannelHandler;


/**
 *
 *  
 */
public class ServerPortRouter extends PortRouter {

    private ServerSocketChannel theServerSocketChannel = null;
    private final Map<Integer, SocketChannelHandler> hostHandlerMap = new HashMap<>();
    private final boolean authenticated;
    private final Timer aTimer = new Timer();    
    
    private static final String NAME_Class = ServerPortRouter.class.getSimpleName();
  
      
    //===============================================================
     /**
     * ClientComm constructor
     *
     * @param passedCommManager
     * @param passedBool
     * @param requireAuthentication
     * @throws IOException
    */
    public ServerPortRouter( PortManager passedCommManager, boolean passedBool, boolean requireAuthentication ) throws IOException {
        super(passedCommManager, passedBool);
        authenticated = requireAuthentication;
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
    
     //===============================================================
     /**
     *  Registers the provided AccessHandler with the server under the
     * given InetAddress.
     *
     * @param passedClientId
     * @param passedParentId
     */
    @Override
    public void registerHandler(int passedClientId, int passedParentId, SocketChannelHandler theHandler) {

        if( theHandler != null){
//            DebugPrinter.printMessage(NAME_Class, "Registering " + passedClientId.toString());
            synchronized(hostHandlerMap){
                hostHandlerMap.put( passedClientId, theHandler);
            }
            
            Integer anInt = theCommManager.getClientParent(passedClientId);
            if( anInt != null && !anInt.equals(passedParentId) ){    
                
                //Remove the registration with the other host if it exists
                ServerManager theServMgr = (ServerManager)theCommManager;
                HostController lastParent = theServMgr.getHostController( Integer.toString(anInt) );
                if( lastParent != null ){
                    Host parentHost = lastParent.getHost();
                    parentHost.removeConnectedHostId( Integer.toString( passedClientId) ); 
                    lastParent.saveToDisk();
                }
                
            }  
            
            theCommManager.setClientParent(passedClientId, passedParentId);
        }
    }
    
  
    //===============================================================
    /**
     *  Removes the client id
     * 
     * @param clientId 
    */
    @Override
    public void removeHandler(int clientId) {
//        DebugPrinter.printMessage(NAME_Class, "Removing " + Integer.toString( clientId ));
        synchronized(hostHandlerMap){
            hostHandlerMap.remove( clientId );
        }
    }
    
    //===============================================================
    /**
     *  Returns the SocketChannelHandler for the passed address.  
     * 
     * @param passedInt
     * @return 
    */  
    @Override
    public SocketChannelHandler getSocketChannelHandler(Integer passedInt ){
        
        //Get the Address
        SocketChannelHandler theSCH;     
        synchronized(hostHandlerMap){
            theSCH = hostHandlerMap.get( passedInt );
        }
        return theSCH;
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
            if( thePort != passedPort )
                theServerSocketChannel.close();
            else 
                return;
        }
        
        //Spawn a new server socket channel
        theServerSocketChannel = getServerSocketChannel(passedAddress, passedPort, false);


        // Register the server socket channel, indicating an interest in
        // accepting new connections
        theSelectionRouter.register(theServerSocketChannel, SelectionKey.OP_ACCEPT, new AcceptHandler( this, authenticated ));

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
    public static ServerSocketChannel getServerSocketChannel( InetAddress passedAddress, int passedPort, boolean blocking ) throws IOException{
         
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

        synchronized(hostHandlerMap){
            //Loop through and close them
            for (SocketChannelHandler aHandler : hostHandlerMap.values() ) {                
                aHandler.shutdown();               
            }
        }
             
        //Clear the handler list
        theSelectionRouter.shutdown();

        //Shutdown the server socket
        if(theServerSocketChannel != null){
            try {
                theServerSocketChannel.close();
            } catch (IOException ex) {
                ex = null;
            }
        }
        
        //Kill all threads and purge
        aTimer.cancel();
        aTimer.purge();
    }   

    //======================================================================
    /*
    * Cancel and kill timers
    */
    public void schedulerKillTimer(SocketDisconnectTimer aTimerTask) {
         
        //check if the time is before now
        Calendar theCalendar = Calendar.getInstance(); 
        theCalendar.setTime( new Date() );
        theCalendar.add(Calendar.SECOND, 20 );
        Date killDate = theCalendar.getTime();

        //Create a timer
        aTimer.schedule(aTimerTask, killDate);
    }
   
}/* END CLASS ServerPortRouter */
