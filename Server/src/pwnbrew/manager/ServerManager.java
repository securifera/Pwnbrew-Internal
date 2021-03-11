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

import java.io.File;
import java.io.IOException;
import java.net.BindException;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.logging.Level;
import javax.swing.SwingUtilities;
import pwnbrew.Server;
import pwnbrew.host.Host;
import pwnbrew.host.HostController;
import pwnbrew.host.Session;
import pwnbrew.log.Log;
import pwnbrew.log.LoggableException;
import pwnbrew.misc.Constants;
import pwnbrew.misc.Directories;
import pwnbrew.network.control.ControlMessageManager;
import pwnbrew.network.ServerPortRouter;
import pwnbrew.network.control.messages.HelloAck;
import pwnbrew.network.control.messages.RelayStart;
import pwnbrew.network.file.FileMessageManager;
import pwnbrew.selector.SocketChannelHandler;
import pwnbrew.utilities.Utilities;
import pwnbrew.xml.ServerConfig;
import pwnbrew.xml.XmlObject;

/**
 *
 *  
 */
public class ServerManager extends PortManager {

    private final Server theServer;
    
    //Create the host map
    private final Map<String, HostController> theHostControllerMap = new HashMap<>();    
    private static final String NAME_Class = ServerManager.class.getSimpleName();
    
    // ==========================================================================
    /**
     * Constructor
     *
     * @param passedServer
     * @throws pwnbrew.log.LoggableException
     * @throws java.io.IOException
    */
    public ServerManager( Server passedServer ) throws LoggableException, IOException {

        theServer = passedServer;
            
        //Create list to hold of the separate controllers
        Map<Host, List<XmlObject>> retMap = Utilities.rebuildLibrary(); //Get the Scripts in the library
        Set<Host> hostSet = retMap.keySet();
        synchronized(theHostControllerMap){
            for( Host aHost : hostSet ){
                HostController aController = new HostController(aHost );
                theHostControllerMap.put( aHost.getId(), aController);
            }
        }
            
            
    }

    //===============================================================
     /**
     *  Starts the server manager and its associated components.
     *
     * @throws pwnbrew.log.LoggableException
     */
    public void start() throws LoggableException {

        //Builds the sockets
        rebuildServerSockets();
    }

    
    //===============================================================
     /**
     *  Builds the server sockets.
     *
     * @return 
     * @throws pwnbrew.log.LoggableException
     */
    public boolean rebuildServerSockets() throws LoggableException {
        
        //Get the ports and try to connect
        ServerConfig theConf = ServerConfig.getServerConfig();
        int theControlPort = theConf.getSocketPort();
        
        boolean retVal = true;
        try {
            
            ControlMessageManager aCMManager = ControlMessageManager.getMessageManager();
            if( aCMManager == null )
                aCMManager = ControlMessageManager.initialize( this );            
            
            int controlPort = aCMManager.getPort();
            ServerPortRouter aSPR = (ServerPortRouter)getPortRouter( controlPort );
            aSPR.startServer(null, controlPort );
            
        } catch (BindException ex) {
            throw new LoggableException( ex, Integer.toString(theControlPort));
        } catch (IOException | GeneralSecurityException ex) {
            throw new LoggableException( ex, Integer.toString(theControlPort));
        }
        
        try {
             
            FileMessageManager aFileMManager = FileMessageManager.getMessageManager();
            if( aFileMManager == null ){
                aFileMManager = FileMessageManager.initialize( this );
            }
            
            int filePort = aFileMManager.getPort();
            ServerPortRouter aSPR = (ServerPortRouter)getPortRouter( filePort );
            aSPR.startServer(null, filePort );
            
        } catch (BindException ex) {
            retVal = false;
        } catch (IOException | GeneralSecurityException ex) {
            throw new LoggableException(ex, Integer.toString(theControlPort));
        }
        
        return retVal;
    }

    //===============================================================
    /**
     * Notification that a socket was closed
     *  
     * @param theHandler
    */
    @Override
    public void socketClosed( SocketChannelHandler theHandler ) {

        //Should ever return null since it's a closing socket
//        ControlMessageManager aCMManager = ControlMessageManager.getMessageManager();
//        int controlPort = aCMManager.getPort();
//        
//        ServerPortRouter aSPR = (ServerPortRouter)getPortRouter( controlPort );
//        int clientId = theHandler.getRootHostId();
//        int channelId = theHandler.getChannelId();
//        
//        //Get the connection manager for the id
//        ConnectionManager aCM = aSPR.getConnectionManager(clientId);
//        if( aCM != null ){
//            
//            //Get the socket channel handler
//            SocketChannelHandler aHandler = aCM.getSocketChannelHandler( channelId );
//            if( aHandler != null && aHandler.equals( theHandler )){
//
//                //If the connection was closed
//                String clientIdStr = Integer.toString( clientId );
//                aCM.removeHandler(channelId);
//                
//                if( channelId == ConnectionManager.COMM_CHANNEL_ID ){
//                    //Get the host controller
//                    HostController theController = getHostController(clientIdStr);
//                    if( theController != null ){
//
//                        final List<HostController> theHostList = new ArrayList<>();
//                        theHostList.add(theController);
//
//                        //Add any pivoting hosts
//                        List<String> theInternalHosts = theController.getHost().getConnectedHostIdList();
//                        for( String idStr : theInternalHosts ){
//                            HostController aController = getHostController(idStr);
//                            theHostList.add(aController);
//                        }         
//
//                        SwingUtilities.invokeLater( new Runnable() {
//
//                            @Override
//                            public void run() {                    
//
//                                for( HostController nextController: theHostList ){
//                                    hostDisconnected( (Host) nextController.getObject() );
//                                    nextController.saveToDisk();
//                                }
//                            }
//                        });
//
//                    }
//                }
//            }
//        }
    }   

    //===============================================================
    /**
     *  Get the server
     * 
     * @return 
     */
    public Server getServer() {
        return theServer;
    }
    
    // ==========================================================================
    /**
     *  Get the host with the given id string
     * 
     * @param clientIdStr
     * @return 
     */
    public HostController getHostController( String clientIdStr ) {
        
        HostController retController;
        synchronized(theHostControllerMap){
            retController = theHostControllerMap.get(clientIdStr);
        }

        return retController;
    }
    
    // ==========================================================================
    /**
     *  Get all of the host controllers
     * 
     * @return 
     */
    public List<HostController> getHostControllers() {
        
        List<HostController> aList = new ArrayList<>();
        synchronized(theHostControllerMap){
            aList.addAll( theHostControllerMap.values() );
        }
        return aList;  
    }
    
    //===============================================================
    /**
     * Adds a new host to the map
     *
     * @param passedHost
    */
    public void registerHost( Host passedHost ) {
        hostDetected(passedHost);
    }
    
     // ==========================================================================
    /**
     * Gets the directory of the client id passed
     * 
     * @param clientId
     * @return 
     */
    public File getHostDirectory(int clientId) {
        
        File dirFile = null;
        HostController theController = getHostController(Integer.toString(clientId));
        if( theController != null )
            dirFile = new File(Directories.getRemoteTasksDirectory(), theController.getItemName());
        
        return dirFile;
    }
    
    //===============================================================
    /**
     * Handles the completion of a task
     *
     * @param clientId
     * @param passedVersion
    */
    public void stagerUpgradeComplete( int clientId, String passedVersion ) {
        
        //Get the host
        HostController theController = getHostController( Integer.toString( clientId) );
        Host theHost = theController.getHost();
        
        //Set the new version and save
        theHost.setJreVersion( passedVersion );
        theController.saveToDisk();
    }
    
     // ==========================================================================
    /**
     * Adds the host to the JTree
     *
     * @param passedHost 
     */
    public void hostDetected( final Host passedHost ) {

        
        //Get the Host from the id
        String clientIdStr = passedHost.getId();  
        
        //Register the client with the parent
        int parentId = getClientParent( Integer.parseInt(clientIdStr));
        HostController parentController = getHostController( Integer.toString(parentId));
        if( parentController != null ){
            Host parentHost = parentController.getHost();
            parentHost.addConnectedHostId( clientIdStr ); 
            parentController.saveToDisk();
        }
        
        HostController theController = getHostController( clientIdStr );
        if(theController != null /*&& theController.getObject().equals(passedHost)*/){
                
            //Get the address
            Host theHost = (Host) theController.getObject();
            theHost.setConnected(true);
                                       
            //Update all data
            theHost.updateData(passedHost);

            //Set time
            Session aSession = new Session();
            theHost.addSession(aSession);

            //Purge stale dates
            theController.removeStaleDates();

            //Write it to disk
            theController.saveToDisk();
            
            //Get the relayPort
            String thePortStr = theHost.getRelayPort();
                
            //Get the port
            if( !thePortStr.isEmpty() ){

                //Parse the port
                int port = Integer.parseInt( thePortStr );     

                //Get the control message manager                              
                int hostId = Integer.parseInt(theHost.getId());
                RelayStart aMigMsg = new RelayStart( port, hostId ); //Convert mins to seconds
                DataManager.send( getServer().getServerManager(), aMigMsg );                        

            }
                              
        } else {

            //Start in swing thread since it affects the gui
            theController = new HostController( passedHost );
            Session aSession = new Session();
            passedHost.addSession(aSession);
            
            final HostController finalController = theController;
            SwingUtilities.invokeLater( new Runnable(){
                @Override
                public void run() {
                    synchronized(theHostControllerMap){
                        theHostControllerMap.put( passedHost.getId(), finalController);
                    }
                }
            });
            
            theController.saveToDisk();
                        
        }
        
        //Send the hello ack
        int clientId = Integer.parseInt(clientIdStr);
        int beaconInterval = theController.getBeaconInterval();
        HelloAck aHostAck = new HelloAck( clientId, beaconInterval );
        DataManager.send( this, aHostAck );
                
    }
    
    //===========================================================================
    /**
     * 
     * @param clientIdStr 
     * @return  
     */
    public boolean removeHost( String clientIdStr ){
        
        boolean retVal = false;
        HostController theController = getHostController( clientIdStr );
        if(theController != null ){  
            //Remove from the map and delete from disk
            synchronized(theHostControllerMap){
                theHostControllerMap.remove( clientIdStr);
            }
            //Delete
            theController.deleteFromLibrary();
            
            retVal = true;
        }  
        
        return retVal;
    }

//    // ==========================================================================
//    /**
//     *  Notify that the host has disconnected.
//     *
//     * @param passedHost 
//     */
//    public void hostDisconnected(final Host passedHost) {
//                
//        List<Session> sessionList = passedHost.getSessionList();
//        Session aSession = sessionList.get(sessionList.size() - 1);
//        aSession.setDisconnectedTime(Constants.CHECKIN_DATE_FORMAT.format( new Date() ));
//       
//        passedHost.setConnected( false );
//    }

    //===========================================================================
    /**
     * 
     * @param hostIdStr
     * @param beaconInterval 
     */
    public void updateHostBeaconInterval(String hostIdStr, int beaconInterval) {
        HostController theController = getHostController( hostIdStr );
        if(theController != null ){  
            theController.setBeaconInterval(beaconInterval);
        }
    }


}
