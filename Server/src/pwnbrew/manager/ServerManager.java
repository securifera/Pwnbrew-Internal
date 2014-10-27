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

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.net.BindException;
import java.security.GeneralSecurityException;
import java.util.*;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import pwnbrew.Server;
import pwnbrew.controllers.MainGuiController;
import pwnbrew.gui.MainGui;
import pwnbrew.gui.tree.IconNode;
import pwnbrew.gui.tree.LibraryItemJTree;
import pwnbrew.gui.tree.MainGuiTreeModel;
import pwnbrew.host.Host;
import pwnbrew.host.HostController;
import pwnbrew.host.Session;
import pwnbrew.host.gui.HostTabPanel;
import pwnbrew.library.LibraryItemController;
import pwnbrew.logging.LoggableException;
import pwnbrew.misc.Constants;
import pwnbrew.misc.Directories;
import pwnbrew.misc.ProgressListener;
import pwnbrew.network.control.ControlMessageManager;
import pwnbrew.network.ServerPortRouter;
import pwnbrew.network.control.messages.RelayStart;
import pwnbrew.network.file.FileMessageManager;
import pwnbrew.selector.SocketChannelHandler;
import pwnbrew.tasks.TaskManager;
import pwnbrew.utilities.Utilities;
import pwnbrew.xmlBase.ServerConfig;
import pwnbrew.xmlBase.XmlBase;

/**
 *
 *  
 */
public class ServerManager extends PortManager {

    private final Server theServer;
    private final boolean headless;
    private MainGuiController theGuiController = null;

    //Create the host map
    private final Map<String, HostController> theHostControllerMap = new HashMap<>();    
    private static final String NAME_Class = ServerManager.class.getSimpleName();
    
    // ==========================================================================
    /**
     * Constructor
     *
     * @param passedServer
     * @param passedBool
     * @throws pwnbrew.logging.LoggableException
     * @throws java.io.IOException
    */
    public ServerManager( Server passedServer, boolean passedBool ) throws LoggableException, IOException {

        //The comm channels
        headless = passedBool;
        theServer = passedServer;
        
        //Create the main controller and gui if flag is set            
        if( !headless )
            theGuiController = new MainGuiController( this, headless );   
        else {
            
            //Create list to hold of the separate controllers
            Map<Host, List<XmlBase>> retMap = Utilities.rebuildLibrary(); //Get the Scripts in the library
            Set<Host> hostSet = retMap.keySet();
            synchronized(theHostControllerMap){
                for( Host aHost : hostSet ){
                    HostController aController = new HostController(aHost, theGuiController, headless);
                    theHostControllerMap.put( aHost.getId(), aController);
                }
            }
            
        }
            
    }
    
    //===============================================================
     /**
      * 
      * @return 
      */
    public boolean isHeadless(){
        return headless;
    }

    //===============================================================
     /**
     *  Starts the server manager and its associated components.
     *
     * @throws pwnbrew.logging.LoggableException
     */
    public void start() throws LoggableException {

        //Builds the sockets
        rebuildServerSockets();
        if( !headless )
            ((MainGui)theGuiController.getObject()).setVisible(true); 
    }
    
     //===============================================================
    /**
     * Returns the server controller
     *
     * @return 
    */
    public MainGuiController getGuiController(){
       return theGuiController;
    }
    
    //===============================================================
     /**
     *  Builds the server sockets.
     *
     * @return 
     * @throws pwnbrew.logging.LoggableException
     */
    public boolean rebuildServerSockets() throws LoggableException {
        
        //Get the ports and try to connect
        ServerConfig theConf = ServerConfig.getServerConfig();
        int theControlPort = theConf.getSocketPort();
        
        boolean retVal = true;
        try {
            
            ControlMessageManager aCMManager = ControlMessageManager.getControlMessageManager();
            if( aCMManager == null )
                aCMManager = ControlMessageManager.initialize( this );            
            
            int controlPort = aCMManager.getPort();
            ServerPortRouter aSPR = (ServerPortRouter)getPortRouter( controlPort );
            aSPR.startServer(null, controlPort );
            
        } catch (BindException ex) {
            if( theGuiController != null ){
                String aSB = "Unable to bind to port " + theControlPort + ".  Please select a new control port.  Tools->Options : \"Network\" Tab";
                JOptionPane.showMessageDialog( null, aSB);
            } else {
                throw new LoggableException( ex, Integer.toString(theControlPort));
            }
            retVal = false;
        } catch (IOException | GeneralSecurityException ex) {
            throw new LoggableException( ex, Integer.toString(theControlPort));
        }
        
        try {
             
            FileMessageManager aFileMManager = FileMessageManager.getFileMessageManager();
            if( aFileMManager == null ){
                aFileMManager = FileMessageManager.initialize( this );
            }
            
            int filePort = aFileMManager.getPort();
            ServerPortRouter aSPR = (ServerPortRouter)getPortRouter( filePort );
            aSPR.startServer(null, filePort );
            
        } catch (BindException ex) {
            if( theGuiController != null ){
                String aSB = "Unable to bind to port " + theControlPort + ". Please select a new data port. Tools->Options : \"Network\" Tab";
                JOptionPane.showMessageDialog( theGuiController.getParentJFrame(), aSB);
            }
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
        ControlMessageManager aCMManager = ControlMessageManager.getControlMessageManager();
        int controlPort = aCMManager.getPort();
        
        ServerPortRouter aSPR = (ServerPortRouter)getPortRouter( controlPort );
        int clientId = theHandler.getRootHostId();
        SocketChannelHandler aHandler = aSPR.getSocketChannelHandler( clientId );
        
        if( aHandler != null && aHandler.equals( theHandler )){
            
            //If the connection was closed
            String clientIdStr = Integer.toString( clientId );
            aSPR.removeHandler(clientId);
            HostController theController = getHostController(clientIdStr);

            if( theController != null ){
                
                final List<HostController> theHostList = new ArrayList<>();
                theHostList.add(theController);

                //Add any pivoting hosts
                List<String> theInternalHosts = theController.getHost().getConnectedHostIdList();
                for( String idStr : theInternalHosts ){
                    HostController aController = getHostController(idStr);
                    theHostList.add(aController);
                }         

                SwingUtilities.invokeLater( new Runnable() {

                    @Override
                    public void run() {                    

                        for( HostController nextController: theHostList ){
                            hostDisconnected( (Host) nextController.getObject() );

                            HostTabPanel thePanel = nextController.getRootPanel();
                            if( thePanel != null )
                                thePanel.getShellPanel().disablePanel( false );
                                
                            nextController.updateComponents();
                            nextController.saveToDisk();
                        }
                    }
                });

            }
        }
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
    
    //===============================================================
    /**
     *  Get the task manager
     * 
     * @return 
     */
    @Override
    public TaskManager getTaskManager() {
        TaskManager aMgr = null;
        if( theGuiController != null && theGuiController instanceof TaskManager)
            aMgr = (TaskManager) theGuiController;
        
        return aMgr;
    }

    //===============================================================
    /**
     * 
     * @return 
     */
    @Override
    public ProgressListener getProgressListener() {
        return theGuiController;
    }
    
    // ==========================================================================
    /**
     *  Get the host with the given id string
     * 
     * @param clientIdStr
     * @return 
     */
    public HostController getHostController( String clientIdStr ) {
        
        HostController retController = null;
        if( theGuiController != null ){
            
            MainGui theMainGui = (MainGui) theGuiController.getObject();
            for( LibraryItemController aController : theMainGui.getJTree().getLibraryItemControllers( HostController.class ) ){
                Host aHost = (Host)aController.getObject();
                if( aHost.getId().equals( clientIdStr )){
                    retController = (HostController) aController;
                    break;
                }           
            }
            
        } else {
            synchronized(theHostControllerMap){
                retController = theHostControllerMap.get(clientIdStr);
            }
        }
        return retController;
    }
    
    // ==========================================================================
    /**
     *  Get all of the host controllers
     * 
     * @return 
     */
    public List<LibraryItemController> getHostControllers() {
        
        List<LibraryItemController> aList = new ArrayList<>();
        if( theGuiController != null ){
            MainGui theMainGui = (MainGui) theGuiController.getObject();
            aList.addAll(theMainGui.getJTree().getLibraryItemControllers( HostController.class ));
        } else {
            synchronized(theHostControllerMap){
                aList.addAll( theHostControllerMap.values() );
            }
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
        
        if( theGuiController != null )
            JOptionPane.showMessageDialog((Component) theGuiController.getObject(), "Stager upgrade complete for " + theController.getItemName());
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
        
        final HostController theController = getHostController( clientIdStr );
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

            //Call for repaint
            SwingUtilities.invokeLater( new Runnable(){
                @Override
                public void run() {
                    if( theGuiController != null ){
                        LibraryItemJTree theJTree = theGuiController.getJTree();
                        theJTree.repaint();
                        theJTree.requestFocus();
                        theController.updateComponents();
                    }
                }
            });

            //Get the auto sleep flag and if it is set then tell the client to goto sleep
            if( theController.getAutoSleepFlag() ){

                Constants.Executor.execute( new Runnable(){
                    @Override
                    public void run() {
                        try {
                            JFrame theParent = null;
                            if( theGuiController != null )
                                theParent = theGuiController.getParentJFrame();
                            
                            Thread.sleep( 30000 );
                            theController.sleep( theParent, true );
                            
                        } catch (InterruptedException ex) {
                            ex = null;
                        }
                    }
                });
                
            } else {
                
                //Get the port
                if( !thePortStr.isEmpty() ){
                    
                    //Parse the port
                    int port = Integer.parseInt( thePortStr );     

                    //Get the control message manager                              
                    int hostId = Integer.parseInt(theHost.getId());
                    RelayStart aMigMsg = new RelayStart( port, hostId ); //Convert mins to seconds
                    DataManager.send( getServer().getServerManager(), aMigMsg );                        

                }
            }
                              
        } else {

            //Start in swing thread since it affects the gui
            final HostController aHostController = new HostController( passedHost, theGuiController, headless );
            Session aSession = new Session();
            passedHost.addSession(aSession);
            
            SwingUtilities.invokeLater( new Runnable(){
                @Override
                public void run() {

                    //Get the JTree and add the controller
                    if( theGuiController != null ){
                        LibraryItemJTree theJTree = theGuiController.getJTree();
                        MainGuiTreeModel treeModel = theJTree.getModel();               
                        DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) treeModel.getRoot();

                        //Add the host node
                        IconNode newParentNode = new IconNode( aHostController, true );

                        //Add the node at the end of the children of the parentNode
                        int index = parentNode.getChildCount();
                        treeModel.insertNodeInto( newParentNode, parentNode, index );  
                    } else {
                        synchronized(theHostControllerMap){
                            theHostControllerMap.put( passedHost.getId(), aHostController);
                        }
                    }           

                }
            });
            
            aHostController.saveToDisk();
                        
        }
                
    }

    // ==========================================================================
    /**
     *  Notify that the host has disconnected.
     *
     * @param passedHost 
     */
    public void hostDisconnected(final Host passedHost) {
                
        List<Session> sessionList = passedHost.getSessionList();
        Session aSession = sessionList.get(sessionList.size() - 1);
        aSession.setDisconnectedTime(Constants.CHECKIN_DATE_FORMAT.format( new Date() ));
       
        passedHost.setConnected( false );
        if( theGuiController != null )
            theGuiController.getJTree().repaint();

    }
    

}/* END CLASS ServerManager */
