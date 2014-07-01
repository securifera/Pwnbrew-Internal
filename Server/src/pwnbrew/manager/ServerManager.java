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
* ServerManager.java
*
* Created on June 21, 2013, 8:25:11 PM
*/

package pwnbrew.manager;

import java.io.IOException;
import java.net.BindException;
import java.security.GeneralSecurityException;
import java.util.*;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import pwnbrew.Server;
import pwnbrew.host.Host;
import pwnbrew.host.HostController;
import pwnbrew.host.HostListener;
import pwnbrew.logging.LoggableException;
import pwnbrew.misc.ProgressListener;
import pwnbrew.network.control.ControlMessageManager;
import pwnbrew.network.ServerPortRouter;
import pwnbrew.network.file.FileMessageManager;
import pwnbrew.selector.SocketChannelHandler;
import pwnbrew.tasks.TaskManager;
import pwnbrew.xmlBase.ServerConfig;

/**
 *
 *  
 */
public class ServerManager extends CommManager {

    private final Server theServer;
    private final List<HostListener> hostListeners = new ArrayList<>();


    // ==========================================================================
    /**
     * Constructor
     *
     * @param passedServer
     * @throws pwnbrew.logging.LoggableException
     * @throws java.io.IOException
    */
    public ServerManager( Server passedServer ) throws LoggableException, IOException {

       //The comm channels
       theServer = passedServer;
       
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
//        int theDataPort = theConf.getDataPort();
        
        boolean retVal = true;
        try {
            
            ControlMessageManager aCMManager = ControlMessageManager.getControlMessageManager();
            if( aCMManager == null ){
                aCMManager = ControlMessageManager.initialize( this );
            }
            
            int controlPort = aCMManager.getPort();
            ServerPortRouter aSPR = (ServerPortRouter)getPortRouter( controlPort );
            aSPR.startServer(null, controlPort );
            
        } catch (BindException ex) {
            StringBuilder aSB = new StringBuilder()
                    .append("Unable to bind to port ").append( theControlPort )
                    .append(".  Please select a new control port.  Tools->Options : \"Network\" Tab");
            JOptionPane.showMessageDialog( getServer().getGuiController().getParentJFrame(), aSB.toString());
            retVal = false;
        } catch (IOException ex) {
            throw new LoggableException( ex, Integer.toString(theControlPort));
        } catch (GeneralSecurityException ex) {
            throw new LoggableException( ex, Integer.toString(theControlPort)) ;
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
            StringBuilder aSB = new StringBuilder()
                    .append("Unable to bind to port ").append( theControlPort )
                    .append(". Please select a new data port. Tools->Options : \"Network\" Tab");
            JOptionPane.showMessageDialog( getServer().getGuiController().getParentJFrame(), aSB.toString());
            retVal = false;
        } catch (IOException ex) {
            throw new LoggableException(ex, Integer.toString(theControlPort));
        } catch (GeneralSecurityException ex) {
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
            HostController theController = theServer.getGuiController().getHostController(clientIdStr);

            if( theController != null ){
                
                final List<HostController> theHostList = new ArrayList<>();
                theHostList.add(theController);

                //Add any pivoting hosts
                List<String> theInternalHosts = theController.getHost().getConnectedHostIdList();
    //            List<Integer> theInternalHosts = theHandler.getInternalHosts();
                for( String idStr : theInternalHosts ){
                    HostController aController = theServer.getGuiController().getHostController(idStr);
                    theHostList.add(aController);
                }         

                SwingUtilities.invokeLater( new Runnable() {

                    @Override
                    public void run() {                    

                        for( HostController nextController: theHostList ){
                            List<HostListener> theListenerList = getDetectListenerList();
                            for(HostListener aListener : theListenerList){
                                aListener.hostDisconnected( (Host) nextController.getObject() );
                            }                    

                            nextController.getRootPanel().getShellPanel().disablePanel( false );
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
        return (TaskManager) theServer.getGuiController();
    }
    
     //===============================================================
    /**
     * Adds a detect listener to the list
     * 
     * @param aListener
    */
    public void addDetectListener(HostListener aListener) {
        if( !hostListeners.contains(aListener)){
            hostListeners.add(aListener);
        }
    }

    //===============================================================
    /**
     * Removes a detect listener from the list
     *
     * @param aListener
    */
    public void removeDetectListener(HostListener aListener) {
        hostListeners.remove(aListener);
    }
    
    //===============================================================
    /**
     * Returns a list of the detect listeners
     *
     * @return 
    */
    public List<HostListener> getDetectListenerList() {
       return new ArrayList<>(hostListeners);
    }

    //===============================================================
    /**
     * 
     * @return 
     */
    @Override
    public ProgressListener getProgressListener() {
        return theServer.getGuiController();
    }

}/* END CLASS ServerManager */
