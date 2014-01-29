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
 *  Created on May 19, 2013
 */

package pwnbrew.shell;

import pwnbrew.execution.ManagedRunnable;
import pwnbrew.gui.panels.RunnerPane;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;
import pwnbrew.logging.Log;
import pwnbrew.misc.Constants;
import pwnbrew.output.StreamReader;
import pwnbrew.output.StreamReaderListener;
import pwnbrew.output.StreamRecorder;

/**
 *
 *  
 */
abstract public class Shell extends ManagedRunnable implements StreamReaderListener {
    
    private Process theProcess = null;
    protected final ShellListener theListener;

    //Stdout reading mechanisms...
    private StreamRecorder theStdOutRecorder = null;
    private boolean stdoutRecorderFinished = false;
 
    //Stderr reading mechanisms...
    private StreamRecorder theStdErrRecorder = null;
    private boolean stderrRecorderFinished = false;
    
    //The input stream
    private BufferedOutputStream theOsStream;
    
    //If enabled then save the shell to disk
    private final boolean logging = false; 
    
    protected static final String NAME_Class = Shell.class.getSimpleName();    


    //===========================================================================
    /**
     *  Constructor
     * 
     * @param passedExecutor
     * @param passedListener 
    */
    Shell( Executor passedExecutor, ShellListener passedListener ) {
        super(passedExecutor);
        theListener = passedListener;        
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
        
        //Get runner pane
        RunnerPane thePane = theListener.getShellTextPane();   
        byte[] byteArr = Arrays.copyOf(buffer, numberRead);
        if( reader == theStdOutRecorder ) { //If the StreamReader is the stdout StreamRecorder...
            thePane.handleStdOut( byteArr );
        } else { //If the StreamReader is the stderr StreamRecorder...
            thePane.handleStdErr( byteArr );        
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
     * @param reader the {@code StreamReader}
     * @param ex the {@code IOException} thrown
     */
    @Override 
    public void handleIOException( StreamReader reader, IOException ex ) {
        
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
    }
    
    // ==========================================================================
    /**
    *   The main loop thread
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
            if( logging ){
                //Set the output file
                File stdErrFile =  new File( theListener.getShellLogDir(), "stderr.txt" );
                theStdErrRecorder.setOutputFile( stdErrFile );
            }

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
            //          theStdOutRecorder = new StreamRecorder( theProcess.getErrorStream() );
            theStdOutRecorder = new StreamRecorder( theProcess.getInputStream() );
            theStdOutRecorder.setIStreamReaderListener( this );
            if( logging ){
                //Set the output file
                File stdOutFile =  new File( theListener.getShellLogDir(), "stdout.txt" );
                theStdOutRecorder.setOutputFile( stdOutFile );
            }
            
            //Execute the stdout reader
            Constants.Executor.execute(theStdOutRecorder);

            //Collect the data from stderr...
            theStdErrRecorder.setInputStream( theProcess.getErrorStream() );
            //          theStdErrRecorder.setInputStream( theProcess.getInputStream() );
            Constants.Executor.execute(theStdErrRecorder);

            //Wait for the process to complete...
            int exitValue = Integer.MIN_VALUE;
            while( exitValue == Integer.MIN_VALUE ) { //Until the exit value is obtained...

                //NOTE: If the subprocess represented by this Process object is forcibly
                //  terminated by calling theProcess.destroy(), then waitFor() still returns
                //  (as opposed to throwing an exception of some kind). The value it returns
                //  in that case is -1.  It returns 0 if the process terminates normally.
                //NOTE: Unknown if there is a scenario in which waitFor() does not return.

                try {
                    exitValue = theProcess.waitFor();
                } catch( InterruptedException ex ) {
                    //Do nothing / Continue to wait for the process to exit
                    ex = null;
                }

            }

            waitForStreamRecordersToFinish(); //Wait for the StreamRecorders to finish

        } catch (ShellException ex) {
            Log.log(Level.SEVERE, NAME_Class, "go()", ex.getMessage(), ex );
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
        theStdOutRecorder.abort();
        theStdErrRecorder.abort();
        
        try {
            //Close the stdin
            theOsStream.close();
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
     * @throws pwnbrew.shell.ShellException 
     */
    abstract public String[] getCommandStringArray() throws ShellException;
    
     // ==========================================================================
    /**
     *  Get character encoding.
     * 
     * @return 
     */
    abstract public String getEncoding();

}
