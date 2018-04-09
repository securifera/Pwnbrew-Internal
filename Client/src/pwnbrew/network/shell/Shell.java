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
 *  Shell.java
 *
 *  Created on June 7, 2013
 */

package pwnbrew.network.shell;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import pwnbrew.log.RemoteLog;
import pwnbrew.manager.DataManager;
import pwnbrew.manager.PortManager;
import pwnbrew.utilities.Constants;
import pwnbrew.utilities.ManagedRunnable;
import pwnbrew.output.StreamReaderListener;
import pwnbrew.output.StreamRecorder;
import pwnbrew.network.shell.messages.ProcessMessage;
import pwnbrew.network.shell.messages.StdErrMessage;
import pwnbrew.network.shell.messages.StdOutMessage;

/**
 *
 *  
 */
public class Shell extends ManagedRunnable implements StreamReaderListener {
    
    private Process theProcess = null;
   
    //Stdout reading mechanisms...
    private StreamRecorder theStdOutRecorder = null;
    private boolean stdoutRecorderFinished = false;
 
    //Stderr reading mechanisms...
    private StreamRecorder theStdErrRecorder = null;
    private boolean stderrRecorderFinished = false;
    
    //The input stream
    private BufferedOutputStream theOsStream;
    
    //Flag for sending output remote or keeping it local
    private volatile boolean sendRemote = true;
    private StringBuilder localStringBuilder = new StringBuilder();    
    
    private final String encoding;
    private final String[] cmdStringArr;
    private final String currentDir;
    private final String startupCmd;
    private final boolean redirect_stderr;
    
    private final int parentId;
    private int channelId = 0;
    private final PortManager theCommManager;
    private static final String NAME_Class = Shell.class.getSimpleName();
      
    // ==========================================================================
    /**
     *  Constructor
     * 
     * @param passedExecutor 
     * @param passedManager 
     * @param passedSrcId 
     * @param passedArr 
     * @param passedEncoding 
     * @param passedStartupCmd 
     * @param passedDir 
     * @param passedBool 
     */
    public Shell( Executor passedExecutor, PortManager passedManager, 
            int passedSrcId, String passedEncoding, String[] passedArr, 
            String passedStartupCmd, String passedDir, boolean passedBool ) {
        super(passedExecutor);
        
        parentId = passedSrcId;
        theCommManager = passedManager;
        encoding = passedEncoding;
        cmdStringArr = passedArr;
        startupCmd = passedStartupCmd;
        currentDir = passedDir;
        redirect_stderr = passedBool;
    }  
    
    //==========================================================================
    /**
     * 
     * @return 
     */
    public int getHostId(){
        return parentId;
    }
    
    //==========================================================================
    /**
     * 
     * @param passedId 
     */
    public void setChannelId(int passedId ){
        channelId = passedId;            
    }
           
    // ==========================================================================
    /**
     * Relays the bytes from the {@link StreamRecorder}s to the appropriate data
     * receiver.
     * <p>
     * If the given {@link StreamReader} is null or is not one of the {@link ExecutionHandler}'s
     * {@code StreamReader}s, this method does nothing.
     * <P>
     * Called by a {@code StreamReader} each time it reads bytes from its {@link InputStream}.
     * <p>
     * The bytes placed in the buffer during the read will occupy the elements at
     * indices 0 - (numberRead - 1).
     *
     * @param passedId
     * @param buffer the buffer into which the bytes were read
     */
    @Override 
    public void handleBytesRead( int passedId, byte[] buffer ) {

        if( sendRemote ){
            
            ProcessMessage aMsg;
            switch( passedId ){
                case Constants.STD_OUT_ID:
                    aMsg = new StdOutMessage( parentId, channelId, ByteBuffer.wrap(buffer));
                    break;
                case Constants.STD_ERR_ID:
                    aMsg = new StdErrMessage( parentId, channelId, ByteBuffer.wrap(buffer));
                    break;
                default:
                    RemoteLog.log(Level.SEVERE, NAME_Class, "handleBytesRead()", "Unrecognized stream id.", null );        
                    return;
            }

            
            DataManager.send(theCommManager, aMsg);
            
        } else {
            
            try {
                //Add to the stringbuilder
                localStringBuilder.append( new String(buffer, encoding));
                beNotified();
            } catch (UnsupportedEncodingException ex) {
                RemoteLog.log( Level.SEVERE, NAME_Class, "handleBytesRead()", ex.getMessage(), null);
            }
            
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

        switch( passedId ){
            case Constants.STD_OUT_ID:
                stdoutRecorderFinished = true; 
                break;
            case Constants.STD_ERR_ID:
                stderrRecorderFinished = true;
                break;
            default:
                RemoteLog.log(Level.SEVERE, NAME_Class, "handleEndOfStream()", "Unrecognized stream id.", null );    
                break;
        } 

        notifyAll();

    }

    
    // ==========================================================================
    /**
     * Notifies the {@link ExecutionHandler} that the given {@link StreamReader} has encountered
     * an {@link IOException} and will read no more bytes from its {@link InputStream}.
     * <p>
     * If the given {@code StreamReader} is null or is not one of the {@code ExecutionHandler}'s
     * {@link StreamRecorder}s, this method does nothing.
     * <P>
     * Called by a {@code StreamReader} when reading from its {@code InputStream} throws
     * an {@code IOException}.
     *
     * @param passedId
     * @param ex the {@code IOException} thrown
     */
    @Override
    public synchronized void handleIOException( int passedId, IOException ex ) {
        handleEndOfStream(passedId, 0);
        RemoteLog.log(Level.INFO, NAME_Class, "receiveByteArray()", ex.getMessage(), ex );        
    }
    
    // ==========================================================================
    /**
    * The main thread loop
    *
    */
    @Override
    public void go() {        

        if( theProcess != null ) { 
            return; //Do nothing
        }

        try {
            
            //Get the command line array
            String[] theShellCmdArr = getCommandStringArray();

            ProcessBuilder theProcessBuilder = new ProcessBuilder( theShellCmdArr );
            
            //Set currend directory
            File currentDirFile = null;
            if( currentDir != null && !currentDir.isEmpty() )
                currentDirFile = new File(currentDir);
            
            theProcessBuilder.directory( currentDirFile );
            theProcessBuilder.redirectErrorStream(redirect_stderr);
                       
            //Create the stderr reader
            theStdErrRecorder = new StreamRecorder( Constants.STD_ERR_ID);
            theStdErrRecorder.setStreamReaderListener( this );
            
            //Start the execution
            try {
                theProcess = theProcessBuilder.start(); //Start a new process
            } catch( IOException ex ) {

                try {

                    String retStr = "Please ensure this machine has the necessary software to run a task of this type.";
                    byte[] strBytes = retStr.getBytes( getEncoding() );
                    theStdErrRecorder.handleBytesRead(strBytes, strBytes.length);
                    theStdErrRecorder.handleEndOfStream();

                    return;

                } catch (UnsupportedEncodingException ex1) {
                    ex1 = null;
                }
            }

            theOsStream = new BufferedOutputStream( theProcess.getOutputStream() );

            //Collect the data from stdout...
            theStdOutRecorder = new StreamRecorder( Constants.STD_OUT_ID, theProcess.getInputStream() );
            theStdOutRecorder.setStreamReaderListener( this );
            theStdOutRecorder.start();
                        
            //Collect the data from stderr...
            theStdErrRecorder.setInputStream( theProcess.getErrorStream() );
            theStdErrRecorder.start();
            
            //Send any startup commands
            if( startupCmd != null && !startupCmd.isEmpty() ){
                sendInput( startupCmd );
            }
          
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

            waitForStreamRecordersToFinish(); //Wait for the StreamRecorders to finish

        } finally {

            //Reset for the next execution...
            theProcess = null;

        }

    }
 
    //===============================================================
    /*
     * Sent the input to the input stream.
     */
    public void sendInput(String theStr) {
        try {
            if( theStr != null ){
                byte[] outStream = theStr.getBytes(  getEncoding() );
                theOsStream.write(outStream);
                theOsStream.flush();
            }
        } catch (IOException ex) {
            ex = null;
        }
    }
    
    //===============================================================
    /**
    *  Shut down the detector
    */
    @Override
    public synchronized void shutdown(){
        super.shutdown();
        
        //Kill the process
        if( theProcess != null ){
            theProcess.destroy();
        }
        
        //Tell the recorders to stop
        if( theStdOutRecorder != null ){
            theStdOutRecorder.shutdown();
        }
        
        if( theStdOutRecorder != null ){
            theStdErrRecorder.shutdown();
        }
        
        try {
            if( theOsStream != null ){
                //Close the stdin
                theOsStream.close();
            }
        } catch (IOException ex) {
            ex = null;
        }
        
    }
    
    // ==========================================================================
    /**
     * Causes the calling {@link Thread} to wait until {@link StreamRecorder}s for
     * stdout and stderr have finished reading from their {@link InputStream}s and
     * notified the {@link ExecutionHandler}.
     */
    private void waitForStreamRecordersToFinish() {

        //Until both StreamRecorders have finished...
        while( stdoutRecorderFinished == false || stderrRecorderFinished == false ) {
            waitToBeNotified();            
        }

    }

    // ==========================================================================
    /**
     *  Get the command string
     * 
     * @return 
     */
    public String[] getCommandStringArray(){
        return cmdStringArr;
    }
    
     // ==========================================================================
    /**
     *  Get character encoding.
     * 
     * @return 
     */
    public String getEncoding(){
        return encoding;
    }

//    // ==========================================================================
//    /**
//     *  Attempts to retrieve the current directory of the shell
//     * 
//     * @return 
//     */
//    public synchronized File getCurrentDirectory() {
//        
//        File aFile;
//        
//        //Set flag
//        localStringBuilder = new StringBuilder();
//        sendRemote = false;
//        sendInput("\n");
//        
//        //Wait to be notified
//        while(true){
//            
//            waitToBeNotified();
//
//            String currentDir = localStringBuilder.toString().trim().replace(">", "");
//            aFile = new File(currentDir);  
//            if( aFile.exists() ){
//                break;
//            }
//            
//        }
//        
//        //Reset flag
//        sendRemote = true;
//        
//        return aFile;        
//        
//    }

}
