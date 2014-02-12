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
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import pwnbrew.logging.Log;
import pwnbrew.misc.Constants;
import pwnbrew.network.shell.messages.StdInMessage;
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
    
    //The input stream
    private String thePrompt = "";
    protected final StringBuilder theStdOutStringBuilder = new StringBuilder();
    protected final StringBuilder theStdErrStringBuilder = new StringBuilder();
    
    //If enabled then save the shell to disk
    private boolean logging = false;     
    protected static final String NAME_Class = Shell.class.getSimpleName();  
    protected volatile boolean promptFlag = false;

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
     * 
     * @return 
     */
    public boolean getPromptFlag() {
        return promptFlag;
    }

    // ==========================================================================
    /**
     * 
     * @param passedBool
     */
    public void setPromptFlag(boolean passedBool ){
        promptFlag = passedBool;
    }   

    // ==========================================================================
    /**
     * 
     * @return 
     */
    public String getShellPrompt() {
        return thePrompt;
    }

    // ==========================================================================
    /**
     * 
     * @param passedStr
     */
    public void setShellPrompt(String passedStr ) {
        thePrompt = passedStr;
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
     public synchronized void handleEndOfStream( int passedId ) {

        switch( passedId ){
            case Constants.STD_OUT_ID:
                stdoutRecorderFinished = true; 
                break;
            case Constants.STD_ERR_ID:
                stderrRecorderFinished = true;
                break;
            default:

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
        handleEndOfStream(passedId);
        Log.log(Level.INFO, NAME_Class, "receiveByteArray()", ex.getMessage(), ex );        
    }
    
    // ==========================================================================
    /**
    *   The main loop thread
    */
    @Override
    public void go() {        

        if( theProcess != null )
            return; 

        try {
            
            //Get the command line array
            String[] theShellCmdArr = getCommandStringArray();

            ProcessBuilder theProcessBuilder = new ProcessBuilder( theShellCmdArr );
            theProcessBuilder.directory( null );
            
            //Create the stderr reader before we create the process in case there is an error
            theStdErrRecorder = new StreamRecorder( Constants.STD_ERR_ID );
            theStdErrRecorder.setStreamReaderListener( this );
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
            theStdOutRecorder = new StreamRecorder( Constants.STD_OUT_ID, theProcess.getInputStream() );
            theStdOutRecorder.setStreamReaderListener( this );
            if( logging ){
                //Set the output file
                File stdOutFile =  new File( theListener.getShellLogDir(), "stdout.txt" );
                theStdOutRecorder.setOutputFile( stdOutFile );
            }
            theStdOutRecorder.start();

            //Collect the data from stderr...
            theStdErrRecorder.setInputStream( theProcess.getErrorStream() );
            theStdErrRecorder.start();

            //Send any startup commands
            sendInput( getStartupCommand() );
            
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
        
            //Set the flag
            setPromptFlag(false);
            
            //Add the command terminator
            String inputTerm = getInputTerminator();
            if( !inputTerm.isEmpty() ){
                theStr = theStr.concat( getInputTerminator() );
            }
            
            byte[] outStream = theStr.getBytes(  getEncoding() );
            if( theListener.isLocalHost() ){

                //Send it locally
                theOsStream.write(outStream);
                theOsStream.flush(); 

            } else {

                int dstHostId = Integer.parseInt( theListener.getHost().getId());
                StdInMessage aMsg = new StdInMessage( ByteBuffer.wrap(theStr.getBytes()), dstHostId);  
                aMsg.setClientId( dstHostId );

                ShellMessageManager aSMM = ShellMessageManager.getShellMessageManager();
                if( aSMM == null){
                    aSMM = ShellMessageManager.initialize( theListener.getCommManager() );
                }
                aSMM.send(aMsg);

            }
        } catch (IOException ex) {
            Log.log( Level.SEVERE, NAME_Class, "sendInput()", ex.getMessage(), ex);
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
        
        if( theStdOutRecorder != null ){
            theStdOutRecorder.shutdown();
        }
        
        
        if( theStdErrRecorder != null ){
            theStdErrRecorder.shutdown();
        }
        
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
     */
    abstract public String[] getCommandStringArray();
    
     // ==========================================================================
    /**
     *  Get character encoding.
     * 
     * @return 
     */
    abstract public String getEncoding();
    
    // ==========================================================================
    /**
     *  Get the string to append to the end of every command
     * 
     * @return 
     */
    public String getInputTerminator(){
        return "";
    };
    
    // ==========================================================================
    /**
     *  Get the string to append to the end of every command
     * 
     * @return 
     */
    public String getStartupCommand(){
        return "";
    };

}
