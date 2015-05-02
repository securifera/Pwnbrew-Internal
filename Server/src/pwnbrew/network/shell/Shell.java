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

package pwnbrew.network.shell;

import java.awt.Color;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.ListIterator;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import pwnbrew.execution.ManagedRunnable;
import pwnbrew.gui.panels.RunnerPane;
import pwnbrew.logging.Log;
import pwnbrew.manager.DataManager;
import pwnbrew.misc.Constants;
import pwnbrew.network.control.messages.CreateShell;
import pwnbrew.network.control.messages.KillShell;
import pwnbrew.network.shell.messages.StdInMessage;
import pwnbrew.output.StreamReader;
import pwnbrew.output.StreamReaderListener;
import pwnbrew.utilities.Utilities;

/**
 *
 *  
 */
abstract public class Shell extends ManagedRunnable implements StreamReaderListener {
    
    private Process theProcess = null;
    protected final ShellListener theListener;

    //Stdout reading mechanisms...
    private StreamReader theStdOutRecorder = null;
    private boolean stdoutRecorderFinished = false;
 
    //Stderr reading mechanisms...
    private StreamReader theStdErrRecorder = null;
    private boolean stderrRecorderFinished = false;
    
    //The input stream
    private BufferedOutputStream theOsStream;
    
    //The input stream
    protected final StringBuilder theStdOutStringBuilder = new StringBuilder();
    protected final StringBuilder theStdErrStringBuilder = new StringBuilder();
    
    protected int theHistoryOffset = -1;
    
    //If enabled then save the shell to disk
    private boolean logging = true;     
    protected static final String NAME_Class = Shell.class.getSimpleName();  
    protected volatile boolean promptFlag = false;
    protected boolean stderrRedirectFlag = false;
    
    //Logging for remote shells
    private FileOutputStream theFileOutputStream = null;
    
    //Logging for remote shells
    private final ArrayList<String> history = new ArrayList<>();
    private ListIterator<String> historyIterator = null;
    
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
    
    //===============================================================
    /**
    *  Starts the detector thread
    */
    @Override
    public synchronized void start(){
        
        //Get the log dir
        File shellDir = theListener.getShellLogDir();

        //Create the date string file
        String dateStr = Constants.SHELL_DATE_FORMAT.format( new Date()).concat(".txt");            
        try {
            theFileOutputStream = new FileOutputStream( new File(shellDir , dateStr), true);
        } catch (FileNotFoundException ex) {
            Log.log(Level.WARNING, NAME_Class, "start()", ex.getMessage(), ex );
        }
        
        if( theListener.isLocalHost() ){
            
            if( !isRunning ){           
                theExecutor.execute( this );
            }
            
        } else {
            
            try {

                //Add the command terminator
                String startupStr = getStartupCommand();
                String inputTerm = getInputTerminator();
                if( !startupStr.isEmpty() && !inputTerm.isEmpty() )
                    startupStr = startupStr.concat( inputTerm );                
                
                //Create the message
                int dstHostId = Integer.parseInt( theListener.getHost().getId());
                CreateShell aShellMsg = new CreateShell( dstHostId, getCommandStringArray(),
                        getEncoding(), startupStr, getStderrRedirectFlag() );
                DataManager.send(theListener.getCommManager(), aShellMsg );

            } catch ( UnsupportedEncodingException ex) {
                Log.log(Level.WARNING, NAME_Class, "spawnShell()", ex.getMessage(), ex );
            }     
        }
    }

    // ==========================================================================
    /**
     * 
     * @return 
     */
    public int getHistoryOffset() {
        return theHistoryOffset;
    }
    
     // ==========================================================================
    /**
     * 
     * @param passedOffset
     */
    public void setHistoryOffset( int passedOffset ) {
        theHistoryOffset = passedOffset;
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
     * @param passedBool
     */
    public void setLoggingFlag(boolean passedBool ){
        logging = passedBool;
    } 
    
    // ==========================================================================
    /**
    * Called by a {@link StreamReader} each time it reads bytes from its {@link InputStream}.
    * <p>
    * The bytes placed in the buffer during the read will occupy the elements at
    * indices 0 - (numberRead - 1).
    *
    * @param theStreamId
    * @param buffer the buffer into which the bytes were read
    */
    @Override
    public void handleBytesRead( int theStreamId, byte[] buffer ){
        if( logging && theFileOutputStream != null && buffer != null ){            
            try {
                theFileOutputStream.write( Arrays.copyOf(buffer, buffer.length) );
                theFileOutputStream.flush();
            } catch( IOException ex ) {
                Log.log(Level.WARNING, NAME_Class, "handleBytesRead()", ex.getMessage(), ex );
            }
        }
        
        //Get runner pane
        RunnerPane thePane = theListener.getShellTextPane(); 
        String aStr = new String( buffer );                    
        if( !aStr.isEmpty() )
            thePane.handleStreamBytes(theStreamId, aStr);

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
            theStdErrRecorder = new StreamReader( Constants.STD_ERR_ID );
            theStdErrRecorder.setStreamReaderListener( this );

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
            theStdOutRecorder = new StreamReader( Constants.STD_OUT_ID, theProcess.getInputStream() );
            theStdOutRecorder.setStreamReaderListener( this );
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
    *
    */
    public void addCommandToHistory(String theStr) {
        if( theStr != null && !theStr.isEmpty() ){
            history.remove(theStr);
            history.add(theStr);
            historyIterator = null;
        }
    }
    
    //===============================================================
    /*
    *
    */
    public void printPreviousCommand(){
        
        final MutableAttributeSet aSet = new SimpleAttributeSet();
        StyleConstants.setForeground(aSet, Color.WHITE); 
        
        //Create an iterator
        if( historyIterator == null )
            historyIterator = history.listIterator( history.size() );                        

        int nextIndex = historyIterator.previousIndex();
        if( nextIndex != -1){

            String nextCommand = historyIterator.previous();
            RunnerPane thePane = theListener.getShellTextPane();
            StyledDocument theDoc = thePane.getStyledDocument();


            //remove whatever is there and insert this
            int promptLoc = thePane.getEndOffset();
            try {
                theDoc.remove(promptLoc, theDoc.getLength() - promptLoc);
                theDoc.insertString( theDoc.getLength(), nextCommand, aSet); 
                thePane.setCaretPosition( theDoc.getLength() );  
            } catch (BadLocationException ex) {
                Log.log(Level.INFO, NAME_Class, "previousInput()", ex.getMessage(), ex );  
            }

        }
    }    
    
    //===============================================================
    /*
    *
    */
    public void printNextCommand(){
        //Make sure we are at the right place
        if( historyIterator != null ){
            final MutableAttributeSet aSet = new SimpleAttributeSet();
            StyleConstants.setForeground(aSet, Color.WHITE); 

            int nextIndex = historyIterator.nextIndex();
            if( nextIndex != history.size()){
                
                String nextCommand = historyIterator.next();
                RunnerPane thePane = theListener.getShellTextPane();
                StyledDocument theDoc = thePane.getStyledDocument();
                
                int promptLoc = thePane.getEndOffset();
                try {
                    theDoc.remove(promptLoc, theDoc.getLength() - promptLoc);
                    theDoc.insertString( theDoc.getLength(), nextCommand, aSet); 
                    thePane.setCaretPosition( theDoc.getLength() );  
                } catch (BadLocationException ex) {
                    Log.log(Level.INFO, NAME_Class, "previousInput()", ex.getMessage(), ex );  
                }
            }
        }
    
    }
 
    //===============================================================
    /*
     * Send the input to the input stream.
     */
    public void sendInput(String theStr) {
        
        try {
        
            if( theStr != null ){
                
                setPromptFlag(false);
                
                 //Log it
                byte[] outStream = theStr.getBytes(  getEncoding() );
                if( logging && theFileOutputStream != null ){            
                    try {
                        theFileOutputStream.write( outStream );
                        theFileOutputStream.flush();
                    } catch( IOException ex ) {
                        Log.log(Level.WARNING, NAME_Class, "sendInput()", ex.getMessage(), ex );
                    }
                }

                if( theListener.isLocalHost() ){

                    //Send it locally
                    if( theOsStream != null ){
                        theOsStream.write(outStream);
                        theOsStream.flush(); 
                    }

                } else {

                    int dstHostId = Integer.parseInt( theListener.getHost().getId());
                    StdInMessage aMsg = new StdInMessage( ByteBuffer.wrap(theStr.getBytes()), dstHostId);  
                    //Send the message
                    DataManager.send(theListener.getCommManager(), aMsg);

                }
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
        
        //Close the file
        try {
            if( theFileOutputStream != null ){
                theFileOutputStream.flush();
                theFileOutputStream.close();
            }        
        } catch (IOException ex) {
            ex = null;
        }
        
        if( theListener.isLocalHost() ){
            
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

                if( theOsStream != null ){
                    //Close the stdin
                    theOsStream.close();
                }

            } catch (IOException ex) {
                ex = null;
            }
            
        } else {

            //Create the message
            int dstHostId = Integer.parseInt( theListener.getHost().getId() );
            KillShell aShellMsg = new KillShell(dstHostId);
            DataManager.send(theListener.getCommManager(), aShellMsg );

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
        return Utilities.getLineEnding( theListener.getOsName());
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
    
    // ==========================================================================
    /**
     * 
     * @param passedBool 
     */
    public void setStderrRedirectFlag( boolean passedBool ) {
        stderrRedirectFlag = passedBool;
    }

    // ==========================================================================
    /**
     * 
     * @return 
     */
    public boolean getStderrRedirectFlag() {
        return stderrRedirectFlag;
    }

}