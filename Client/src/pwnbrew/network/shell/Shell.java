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
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import pwnbrew.log.RemoteLog;
import pwnbrew.log.LoggableException;
import pwnbrew.manager.CommManager;
import pwnbrew.misc.Constants;
import pwnbrew.misc.ManagedRunnable;
import pwnbrew.output.StreamReader;
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
    
    private final CommManager theCommManager;
    private static final String NAME_Class = Shell.class.getSimpleName();
      
    // ==========================================================================
    /**
     *  Constructor
     * 
     * @param passedExecutor 
     * @param passedManager 
     * @param passedArr 
     * @param passedEncoding 
     */
    public Shell( Executor passedExecutor, CommManager passedManager, String passedEncoding, String[] passedArr ) {
        super(passedExecutor);
        
        theCommManager = passedManager;
        encoding = passedEncoding;
        cmdStringArr = passedArr;
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
     * @param reader the {@code StreamReader}
     * @param buffer the buffer into which the bytes were read
     * @param numberRead the number of bytes read
     */
    @Override 
    public void handleBytesRead( StreamReader reader, byte[] buffer, int numberRead ) {

        if( reader == null || //If the StreamReader is null or...
                ( reader != theStdOutRecorder && reader != theStdErrRecorder ) ) { //The StreamReader does not belong to the ExecutionHandler...
            return; //Do nothing
        }
        
        byte[] byteArr = Arrays.copyOf(buffer, numberRead);
        if( sendRemote ){
            
            //Get runner pane
            ProcessMessage aMsg;
            if( reader == theStdOutRecorder ) { //If the StreamReader is the stdout StreamRecorder...
                aMsg = new StdOutMessage( ByteBuffer.wrap(byteArr));
            } else { //If the StreamReader is the stderr StreamRecorder...
                aMsg = new StdErrMessage( ByteBuffer.wrap(byteArr));       
            }

            try {

                ShellMessageManager aSMM = ShellMessageManager.getShellMessageManager();
                if( aSMM == null){
                    aSMM = ShellMessageManager.initialize( theCommManager );
                }
                aSMM.send(aMsg);

            } catch (IOException ex) {
                RemoteLog.log( Level.SEVERE, NAME_Class, "handleBytesRead()", ex.getMessage(), null);
            } catch (LoggableException ex) {
                RemoteLog.log( Level.SEVERE, NAME_Class, "handleBytesRead()", ex.getMessage(), null);
            }
            
        } else {
            
            try {
                //Add to the stringbuilder
                localStringBuilder.append( new String(byteArr, encoding));
                beNotified();
            } catch (UnsupportedEncodingException ex) {
                RemoteLog.log( Level.SEVERE, NAME_Class, "handleBytesRead()", ex.getMessage(), null);
            }
            
        }

    }/* END handleBytesRead( StreamReader, byte[], int ) */


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
     * @param reader the {@code StreamReader}
     */
    @Override 
    public void handleEndOfStream( StreamReader reader ) {
        
        if( reader == null || //If the StreamReader is null or...
                ( reader != theStdOutRecorder && reader != theStdErrRecorder ) ) { //The StreamReader does not belong to the ExecutionHandler...
            return; //Do nothing
        }

        if( reader == theStdOutRecorder ) { //If the StreamReader is the stdout StreamRecorder...
            stdoutRecorderFinished = true; //The stdout StreamRecorder has finished
        } else { //If the StreamReader is the stderr StreamRecorder...
            stderrRecorderFinished = true; //The stderr StreamRecorder has finished
        }

        beNotified(); //Notify the ExecutionHandler
        
    }/* END handleEndOfStream( StreamReader ) */

    
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
     * @param reader the {@code StreamReader}
     * @param ex the {@code IOException} thrown
     */
    @Override 
    public void handleIOException( StreamReader reader, IOException ex ) {
        
        if( reader == null || //If the StreamReader is null or...
                ( reader != theStdOutRecorder && reader != theStdErrRecorder ) ) //The StreamReader does not belong to the ExecutionHandler...
            return; //Do nothing
        
        if( reader == theStdOutRecorder )//If the StreamReader is the stdout StreamRecorder...
            stdoutRecorderFinished = true; //The stdout StreamRecorder has finished
        else //If the StreamReader is the stderr StreamRecorder...
            stderrRecorderFinished = true; //The stderr StreamRecorder has finished
        
        beNotified(); //Notify the ExecutionHandler    
        
    }/* END handleIOException( StreamReader, IOException ) */
    
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
            theProcessBuilder.directory( null );

            //Create the stderr reader
            theStdErrRecorder = new StreamRecorder();
            theStdErrRecorder.setIStreamReaderListener( this );
            
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
            theStdOutRecorder = new StreamRecorder( theProcess.getInputStream() );
            theStdOutRecorder.setIStreamReaderListener( this );
                        
            //Collect the data from stderr...
            theStdErrRecorder.setInputStream( theProcess.getErrorStream() );
          
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

    }/* END execute( File, IStdOutReceiver, IStdErrReceiver ) */
 
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

    }/* END waitForStreamRecordersToFinish() */

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

    // ==========================================================================
    /**
     *  Attempts to retrieve the current directory of the shell
     * 
     * @return 
     */
    public synchronized File getCurrentDirectory() {
        
        File aFile;
        
        //Set flag
        localStringBuilder = new StringBuilder();
        sendRemote = false;
        sendInput("\n");
        
        //Wait to be notified
        while(true){
            
            waitToBeNotified();

            String currentDir = localStringBuilder.toString().trim().replace(">", "");
            aFile = new File(currentDir);  
            if( aFile.exists() ){
                break;
            }
            
        }
        
        //Reset flag
        sendRemote = true;
        
        return aFile;        
        
    }

}
