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

package pwnbrew.task;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import pwnbrew.log.RemoteLog;
import pwnbrew.log.LoggableException;
import pwnbrew.manager.CommManager;
import pwnbrew.misc.Constants;
import pwnbrew.misc.DebugPrinter;
import pwnbrew.misc.FileUtilities;
import pwnbrew.misc.ManagedRunnable;
import pwnbrew.network.control.ControlMessageManager;
import pwnbrew.network.control.messages.PushFile;
import pwnbrew.network.control.messages.PushFileUpdate;
import pwnbrew.network.control.messages.TaskGetFile;
import pwnbrew.network.control.messages.TaskNew;
import pwnbrew.network.control.messages.TaskStatus;
import pwnbrew.network.file.FileData;
import pwnbrew.output.StreamReader;
import pwnbrew.output.StreamReaderListener;
import pwnbrew.output.StreamRecorder;


/**
 *
 *  
 */
public class TaskRunner extends ManagedRunnable implements StreamReaderListener {

    private final CommManager theCommManager;
    private final TaskNew theTask;
    private Process theProcess = null;

    //Stdout reading mechanisms...
    private StreamRecorder theStdOutRecorder = null;

    //Stderr reading mechanisms...
    private StreamRecorder theStdErrRecorder = null;
    
    private volatile boolean runCancelled = false;
    private volatile int fileCounter = 0;
    private volatile int limit = 0;
    private Integer theTaskId = 0;

    private final ReentrantLock TheCancelLock = new ReentrantLock();
    private static final String NAME_Class = TaskRunner.class.getSimpleName();
    
    //The fileid for stdout, stderr
    private int stdOutFileId;
    private int stdErrFileId;

    //=========================================================================
    /**
    * Constructor
    * 
    * @param passedParent
    * @param passedTaskMessage 
    */
    public TaskRunner( CommManager passedParent, TaskNew passedTaskMessage) {
        super(Constants.Executor);
        theCommManager = passedParent;
        theTask = passedTaskMessage;
    }

     //===============================================================
     /**
     *  The main loop for the thread execution.
     *
     */
    @Override
    public void go() {

        boolean filesPresent = false;

        //Get the server inet and the support files needed
        String resultStatus = TaskStatus.TASK_COMPLETED;

        //Get the task message id
        theTaskId = theTask.getTaskId();
        
        DebugPrinter.printMessage(this.getClass().getSimpleName(), "Received task.");
        try {
            
            ControlMessageManager aCMManager = ControlMessageManager.getControlMessageManager();
            if( aCMManager == null )
                aCMManager = ControlMessageManager.initialize( theCommManager);            

            //Get the support file list
            List<String> hashFilenameList = theTask.getSupportFiles();

            //Retreive the command line
            String[] cmdLineArr = theTask.getCmdLine();
            if(cmdLineArr.length > 0){

                //Get temp dir
                File taskDir = FileUtilities.getTempDir();

                //If the files aren't all present, wait until they arrive
                while(!shutdownRequested && !runCancelled && !filesPresent) {

                    int requestedFileCount = 0;

                    List<String> clonedFileList = new ArrayList<>(hashFilenameList);
                    for(String hashFilename : clonedFileList){
                        String aStr = hashFilename;
                        String fileHash = aStr.split(":")[0];
                        String fileName = aStr.split(":")[1];

                        File aFile = new File( taskDir, fileHash);

                        //If the file exists, remove it from the list, else request it from the server
                        if(aFile.exists() && FileUtilities.getFileHash( aFile ).equals(aFile.getName())){
                            hashFilenameList.remove(aStr);
                        } else {

                            //request the file from the server
                            requestedFileCount++;
                            TaskGetFile getMessage = new TaskGetFile( theTaskId, new File(fileName), fileHash );
                            aCMManager.send(getMessage);
                        }

                    }

                    //If the needed files are present then continue
                    if(hashFilenameList.isEmpty()){
                        filesPresent = true;
                        continue;
                    }

                    //Wait until a file comes in
                    waitForFiles(requestedFileCount);

                }

                //Run the task if the files are present
                if(filesPresent && !runCancelled){

                    
                    //Send status msg indicating that the task is running
                    TaskStatus taskRunMsg = new TaskStatus( theTaskId, TaskStatus.TASK_RUNNING );
                    aCMManager.send(taskRunMsg);                   

                    //Copy over the needed files to the dir
                    hashFilenameList = theTask.getSupportFiles();
                    if(renameSupportFiles(taskDir, hashFilenameList)){
                        execute(cmdLineArr, taskDir);
                        DebugPrinter.printMessage(this.getClass().getSimpleName(), "Executed the task.");
                    }
                }
            }
        
        } catch (NoSuchAlgorithmException | IOException | LoggableException ex) {
            resultStatus = TaskStatus.TASK_FAILED;
            RemoteLog.log(Level.INFO, NAME_Class, "run()", ex.getMessage(), ex );
        }

        //Remove the handler from the list
        if( theCommManager instanceof TaskListener ){
            ((TaskListener)theCommManager).taskFinished( theTaskId, resultStatus );
        }

    }

    // ==========================================================================
    /**
     * Executes the task in the directory represented by the given {@link File}.
     * <p>
     * @param workingDirectory the directory in which to run the task
     *
    */
    private void execute( String[] cmdLineArgs, File workingDirectory ) {

        if( theProcess != null || runCancelled)
            return;     

        if( workingDirectory == null )
            return; 

        try {

            ProcessBuilder theProcessBuilder = new ProcessBuilder( cmdLineArgs );
            theProcessBuilder.directory( workingDirectory );
        
            //Create the stderr reader
            theStdErrRecorder = new StreamRecorder( Constants.STD_ERR_ID );

            try {
                theProcess = theProcessBuilder.start(); //Start a new process
            } catch( IOException ex ) {                

                String retStr = "Please ensure this machine has the necessary software to run a task of this type.";
                byte[] strBytes = retStr.getBytes();
                theStdErrRecorder.handleBytesRead(strBytes, strBytes.length);
                theStdErrRecorder.handleEndOfStream();

                return;
            }

            OutputStream theirStdin = theProcess.getOutputStream();
            try {
                theirStdin.close();
            } catch ( IOException ioe ){
                ioe = null;
            }
            
            //Send messages for the stdout & stderr
            ControlMessageManager aCMManager = ControlMessageManager.getControlMessageManager();
            if( aCMManager != null ){
                
                PushFile resultFile = new PushFile( theTaskId, "0:stderr.txt", -1, PushFile.JOB_RESULT );
                stdErrFileId = resultFile.getFileId();
                aCMManager.send(resultFile);
                
                resultFile = new PushFile( theTaskId, "0:stdout.txt", -1, PushFile.JOB_RESULT );
                stdOutFileId = resultFile.getFileId();
                aCMManager.send(resultFile);
            }

            //Collect the data from stdout...
            theStdOutRecorder = new StreamRecorder( Constants.STD_OUT_ID, theProcess.getInputStream() );
            theStdOutRecorder.setStreamReaderListener( this );
            theStdOutRecorder.start();
       
            //Collect the data from stderr...
            theStdErrRecorder.setInputStream(theProcess.getErrorStream());
            theStdErrRecorder.setStreamReaderListener( this );
            theStdErrRecorder.start();
        
            //Wait for the process to complete...
            int exitValue = Integer.MIN_VALUE;
            while( exitValue == Integer.MIN_VALUE ) { //Until the exit value is obtained...

                try {
                    exitValue = theProcess.waitFor();
                } catch( InterruptedException ex ) {
                    //Do nothing / Continue to wait for the process to exit
                    ex = null;
                }

            }


        } finally {
            //Reset for the next execution...
            theProcess = null;
        }

    }

    // ==========================================================================
    /**
     * Ends the execution of the task.
     * <p>
     * @return 
     */
    public boolean cancelRun() {

        boolean rtnBool = false;

        runCancelled = true;
        if(theProcess != null){

            TheCancelLock.lock();
            try {

                theProcess.destroy();
                
                theStdOutRecorder.shutdown();
                theStdErrRecorder.shutdown();
                
              //Try and kill the child process
//              Utilities.killChildProcesses(theProcess);                            
              rtnBool = true; //The run was cancelled
           
            } finally {
              TheCancelLock.unlock();
            }
        } 
       
        beNotified();

        return rtnBool;
    }

    // ==========================================================================
    /**
     * Relays the bytes from the {@link StreamRecorder}s to the appropriate data
     * receiver.
     * <p>
     *
     * @param buffer the buffer into which the bytes were read
    */
    @Override
    public void handleBytesRead( int theStreamId, byte[] buffer ){
        
        int fileId;
        switch( theStreamId ){
            case Constants.STD_OUT_ID:
                fileId = stdOutFileId;
                break;
            case Constants.STD_ERR_ID:
                fileId = stdErrFileId;
                break;
            default:
                RemoteLog.log(Level.SEVERE, NAME_Class, "handleBytesRead()", "Unrecognized stream id.", null );        
                return;
        }
        
        //Send the bytes
        ControlMessageManager aCMManager = ControlMessageManager.getControlMessageManager();
        if( aCMManager != null ){
            FileData fileDataMsg = new FileData(fileId, buffer); 
            aCMManager.send(fileDataMsg);
        }
        
    }
    
    // ==========================================================================
    /**
     * Notifies the {@link ExecutionHandler} that the given {@link StreamReader}
     * has completed.
     * <p>
     * If the given {@link StreamReader} is null or is not one of the {@code ExecutionHandler}'s
     * {@code StreamRecorder}s, this method does nothing.
     * <P>
     * Called by a {@code StreamReader} when it detects the end of file in its {@link InputStream}.
     *
     * @param passedId
     */
     @Override
     public synchronized void handleEndOfStream( int passedId, long bytesRead ) {

        PushFileUpdate theUpdate;
        switch( passedId ){
            case Constants.STD_OUT_ID:
                theUpdate = new PushFileUpdate(theTaskId, stdOutFileId, bytesRead);
                break;
            case Constants.STD_ERR_ID:
                theUpdate = new PushFileUpdate(theTaskId, stdErrFileId, bytesRead);
                break;
            default:
                RemoteLog.log(Level.SEVERE, NAME_Class, "handleEndOfStream()", "Unrecognized stream id.", null );    
                return;
        } 

        //Send the message
        ControlMessageManager aCMM = ControlMessageManager.getControlMessageManager();
        if( aCMM != null ){
            aCMM.send(theUpdate);
        }
    }

    
    // ==========================================================================
    /**
     * Handles any exceptions from the reader
     *
     * @param passedId
     * @param ex the {@code IOException} thrown
     */
    @Override
    public synchronized void handleIOException( int passedId, IOException ex ) {
        handleEndOfStream(passedId, 0);
        RemoteLog.log(Level.INFO, NAME_Class, "handleIOException()", ex.getMessage(), ex );        
    }

    //===============================================================
    /**
     * Renames the support files from their hash to their real names
     *  
     * @param taskDir
    */
    private boolean renameSupportFiles(File taskDir, List<String> hashFilenameList ) {
        
        if( taskDir == null )
            return false;        

        //Write the supporting files in the directory
        boolean rtnBool = true;
        try {

            for( String aHashFilenameStr : hashFilenameList ) { //For each FileContentRef...

                String fileHash = aHashFilenameStr.split(":")[0];
                String filename = aHashFilenameStr.split(":")[1];

                if( fileHash != null && !fileHash.isEmpty()) { //If the FileContent was rebuilt from the file...

                    //If the hash was obtained and is not empty...
                    File supportFile = new File( taskDir, filename ); //Create a File for the file
                    FileUtilities.renameLibFile( fileHash, supportFile, taskDir );

                } else {
                    rtnBool = false; //The working directory was not successfully populated
                    break; 
                }

            } 
            
        } catch (IOException ex){
           rtnBool = false;
        }

        return rtnBool;
    }

    //===================================================================
    /**
     * Shutdown the Task
     */
    @Override
    public void shutdown() {
      
        runCancelled = true;
        if(theProcess != null){

            TheCancelLock.lock();
            try {

                theProcess.destroy();
                
                theStdOutRecorder.shutdown();
                theStdErrRecorder.shutdown();
                
              //Try and kill the child process
//              Utilities.killChildProcesses(theProcess);     
           
            } finally {
              TheCancelLock.unlock();
            }
        }        

        super.shutdown();
    }

    // ==========================================================================
    /**
    *  Notifies the {@link Task Runner} that one of its output files
    * was successfully sent or received.  If a true is passed then it increments
    * the file sent counter.
    *
    * @param fileOp
    */
    public synchronized void notifyFileOp(int fileOp) {

        //Increment the file sent counter if passed true
        fileCounter = fileCounter + 1;
        if(fileCounter == limit)
            notifyAll();        

    }


    //===============================================================
    /**
     * Waits until all the output files have been successfully sent
    *
    * @param passedNum
    */
    private synchronized boolean waitForFiles(int passedNum) {

        limit = passedNum;
        while(fileCounter < passedNum && !shutdownRequested
            && !runCancelled){

            //Wait until the files have been received
            try {
                wait();
            } catch( InterruptedException ex ) {
            }

        }
        fileCounter = 0;
        return true;
    }

}
