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
 * ExecutionHandler.java
 *
 * Created on June 23, 2013, 12:11:31 AM
 */

package pwnbrew.execution;

import pwnbrew.xmlBase.FileContentRef;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import pwnbrew.log.Log;
import pwnbrew.misc.Constants;
import pwnbrew.misc.Directories;
import pwnbrew.utilities.FileUtilities;
import pwnbrew.misc.LibraryFileCopyListener;
import pwnbrew.output.StreamReader;
import pwnbrew.output.StreamReaderListener;
import pwnbrew.output.StreamRecorder;
import pwnbrew.utilities.Utilities;


/**
 * 
 */
public class ExecutionHandler implements Runnable, LibraryFileCopyListener, StreamReaderListener {
    
    private ExecutionObserver theExecutionObserver;
    private ExecutableItem theExecutableItem;
    private ProcessBuilder theProcessBuilder;
    private Process theProcess;
    private int theExitValue = Integer.MIN_VALUE;
    
    private String[] theCommandArgs;
    
    private File theWorkingDirectory;
    
    private StreamRecorder theStdoutStreamRecorder;
    private StreamRecorder theStderrStreamRecorder;
    private boolean stdoutRecorderFinished = false;
    private boolean stderrRecorderFinished = false;
    
    private String theCurrentFileHash;
    private String theCurrentFileName;
    private boolean fileCopyFailed;
    
    private String theErrorString;
    
    private static final String NAME_Class = ExecutionHandler.class.getSimpleName();
    
    // ========================================================================
    /**
     * Constructor
     * @param item
     * @param observer
     */
    public ExecutionHandler( ExecutableItem item, ExecutionObserver observer ) {
        
        if( item == null )
            throw new IllegalArgumentException( "The ExecutableItem cannot be null." );
        
        theExecutableItem = item;
        theExecutionObserver = observer;
        
    }    

    // ========================================================================
    /**
     * 
     */
    @Override
    public void run() {
        
        //Get the command args
        theCommandArgs = theExecutableItem.getCommandArgs( Utilities.getOsName() );
        if( theCommandArgs != null && theCommandArgs.length > 0 ) { //If at least one argument was obtained...
        
            if( theExecutionObserver != null )
                theExecutionObserver.executionObserver_PreparingForExecution( theExecutableItem );
            
            //Create the working directory...
            try {
                theWorkingDirectory = Directories.createWorkingDirectory( theExecutableItem );
            } catch( IOException ex ) {
                ex = null;
            }

            if( theWorkingDirectory != null ) { 

                populateWorkingDirectory();

                if( theErrorString == null ) { 
                    //The working directory should now be extant and populated.

                    if( theExecutionObserver != null )
                        theExecutionObserver.executionObserver_ExecutionStarting( theExecutableItem );
                    
                    //Start the Process
                    theProcessBuilder = new ProcessBuilder( theCommandArgs );
                    theProcessBuilder.directory( theWorkingDirectory );
                    try {
                        theProcess = theProcessBuilder.start();
                    } catch( IOException ex ) {
                        theErrorString = "Failed to start the process.";
                    }

                    if( theProcess != null ) { //If the Process was started...
                        handleTheProcess();
                    }
                    
                } 
                
            } else { 
                theErrorString = "Could not create the working directory.";
            }

        } else { 
            theErrorString = "Could not obtain any command arguments.";
        }
        
        //Alert observer of completion
        if( theExecutionObserver != null ) {
            
            if( theErrorString != null ) { //If an error occurred...
                theExecutionObserver.executionObserver_ExecutionFailed( theExecutableItem, theErrorString );
            } else { //If no errors occurred...
                theExecutionObserver.executionObserver_ExecutionComplete( theExecutableItem );
            }
            
        }
        
    }/* END run() */
    
    
    // ========================================================================
    /**
     * 
     */
    private void populateWorkingDirectory() {
        
        Map<String, FileContentRef> fileMap = theExecutableItem.getFileContentRefMap();
        if( fileMap != null ) { //If a List was obtained...

            for( Iterator<FileContentRef> anIter = fileMap.values().iterator(); 
                       anIter.hasNext(); ) { //For each FileContentRef...

                FileContentRef aFCR = anIter.next();
                theCurrentFileHash = aFCR.getFileHash();
                theCurrentFileName = aFCR.getFilename();
                
                if( theCurrentFileHash != null ) { //If a file hash was obtained...

                    if( theCurrentFileName.isEmpty() == false ) { //If the file name is not empty...
                        
                        //Copy the library file to the working directory
                        //NOTE: Each file in the library is named with its hash.
                        FileUtilities.copyLibraryFile( theCurrentFileHash, new File( theWorkingDirectory, theCurrentFileName ), this ); 
                        
                        if( fileCopyFailed ) { //If the file copy failed...
                            
                            theErrorString = new StringBuilder( "Unable to copy the file \"" ).append( theCurrentFileName )
                                    .append( "\" to the working directory." ).toString();
                            break; //Stop iterating through the FileContentRefs

                        } else { //If the file copy succeeded...
                            if( theExecutionObserver != null )
                                theExecutionObserver.executionObserver_FileCopyComplete( theExecutableItem, theCurrentFileName );
                        }
                        
                    } else { //If the file name is empty...

                        theErrorString = new StringBuilder( "The FileContentRef with the file hash \"" ).append( theCurrentFileHash )
                                .append( "\" has no file name.\"" ).toString();
                        break; //Stop iterating through the FileContentRefs

                    }

                } else { //If a file has was not obtained...
                    
                    if( theCurrentFileName != null && theCurrentFileName.isEmpty() == false ) {
                        theErrorString = new StringBuilder( "The FileContentRef for the file named \"" ).append( theCurrentFileName )
                                .append( "\" has no file hash.\"" ).toString();
                    } else {
                        theErrorString = "Encountered a FileContentRef with no file hash nor file name.";
                    }
                    
                    break; //Stop iterating through the FileContentRefs
            
                }

            } //End of "for( FileContentRef fcr : fcrList ) { //For each FileContentRef..."

        } //End of "if( fcrList != null ) { //If a List was obtained..."
            
    }/* END populateWorkingDirectory() */
    
    
    // ========================================================================
    /**
     * 
     */
    private void handleTheProcess() {
        
        //Close the Process's input stream
        OutputStream stdinStream = theProcess.getOutputStream();
        try {
            stdinStream.close();
        } catch ( IOException ex ) {
            ex = null;
        }

        //Collect the data from stdout...
        theStdoutStreamRecorder = new StreamRecorder( Constants.STD_OUT_ID, theProcess.getInputStream() );
        theStdoutStreamRecorder.setStreamReaderListener( this );
        theStdoutStreamRecorder.setOutputFile( new File( theWorkingDirectory, "stdout.txt" ) );
        theStdoutStreamRecorder.start();
        
//        Constants.Executor.execute( theStdoutStreamRecorder );

        //Collect the data from stderr...
        theStderrStreamRecorder = new StreamRecorder( Constants.STD_ERR_ID, theProcess.getErrorStream() );
        theStderrStreamRecorder.setStreamReaderListener( this );
        theStderrStreamRecorder.setOutputFile( new File( theWorkingDirectory, "stderr.txt" ) );
        theStderrStreamRecorder.start();
        
//        Constants.Executor.execute( theStderrStreamRecorder );

        waitForProcessToFinish();
        synchronized( this ) {
            //NOTE: This block is synchronized with a block in stopExecution() that
            //  checks to see if the Process is null before calling Process.destroy().
            theProcess = null;
        }

        waitForStreamRecordersToFinish();
        
    }/* END handleTheProcess() */

    
    // ========================================================================
    /**
     * Causes the calling {@link Thread} to wait until the {@link Process} has completed.
     */
    private void waitForProcessToFinish() {
        
        //Wait for the process to complete...
        while( theExitValue == Integer.MIN_VALUE ) { //Until an exit value is obtained...

            //NOTE: If the subprocess represented by this Process object is forcibly
            //  terminated by calling Process.destroy(), then waitFor() still returns
            //  (as opposed to throwing an exception of some kind). The value it returns
            //  in that case is -1.  It returns 0 if the process terminates normally.
            //NOTE: I don't know if there's a case in which waitFor() does not return.

            try {
                theExitValue = theProcess.waitFor();
            } catch( InterruptedException ex ) {
                //Do nothing / Continue to wait for the process to exit
                ex = null;
            }

        }

    }/* END waitForProcessToFinish() */
    
    
    // ==========================================================================
    /**
     * Causes the calling {@link Thread} to wait until {@link StreamRecorder}s for
     * stdout and stderr have finished reading from their {@link InputStream}s and
     * notified the {@link ExecutionHandler}.
     */
    private synchronized void waitForStreamRecordersToFinish() {

        //Until both StreamRecorders have finished...
        while( stdoutRecorderFinished == false || stderrRecorderFinished == false ) {

            try {
                wait();
            } catch( InterruptedException ex ) {
                ex = null;
            }

        }

    }/* END waitForStreamRecordersToFinish() */
    
    
    // ==========================================================================
    /**
     * Ends the execution of the {@link ExecutableItem}.
     */
    public void stopExecution() {

        synchronized( this ) {
            //NOTE: This block is synchronized with a block in run() that sets the Process
            //  to null after the subprocess it represents has ended.

            if( theProcess != null ) {
                
                //Kill child process first
//                Utilities.killChildProcesses(theProcess);
                
                theProcess.destroy(); //Stop the process
                theStdoutStreamRecorder.shutdown();
                theStderrStreamRecorder.shutdown();
            }
          
        }
        
    }/* END stopExecution() */

    
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

        if( theExecutionObserver != null ) { //If an ExecutionObserver was given...

            switch( passedId ){
                case Constants.STD_OUT_ID:
                    theExecutionObserver.executionObserver_HandleStdoutData( theExecutableItem, buffer );
                    break;
                case Constants.STD_ERR_ID:
                    theExecutionObserver.executionObserver_HandleStderrData( theExecutableItem, buffer );
                    break;
                default:
                    Log.log(Level.SEVERE, NAME_Class, "handleBytesRead()", "Unrecognized stream id.", null );        
                    break;
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
     public synchronized void handleEndOfStream( int passedId ) {

        switch( passedId ){
            case Constants.STD_OUT_ID:
                stdoutRecorderFinished = true; 
                break;
            case Constants.STD_ERR_ID:
                stderrRecorderFinished = true;
                break;
            default:
                Log.log(Level.SEVERE, NAME_Class, "handleEndOfStream()", "Unrecognized stream id.", null );    
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
    
    
    // ========================================================================
    /**
     * 
     * @param libFileName
     * @param error
     * @param destination
     */
    @Override 
    public void libraryFileCopyFailed( String libFileName, File destination, String error ) {
        
        if( libFileName != null && libFileName.equals( theCurrentFileHash ) )
            fileCopyFailed = true;
        
    }

    
    // ========================================================================
    /**
     * 
     * @param libFileName
     * @param fileBytes
     * @param bytesCopied
     */
    @Override
    public void libraryFileCopyProgress( String libFileName, long bytesCopied, long fileBytes ) {
        
        if( libFileName != null && libFileName.equals( theCurrentFileHash ) ) {
            
            if( theExecutionObserver != null )
                theExecutionObserver.executionObserver_FileCopyProgress( theExecutableItem, theCurrentFileName, bytesCopied, fileBytes );
            
        }
        
    }
    
    // ========================================================================
    /**
     * 
     * @param libFileName
     * @param destination
     */
    @Override 
    public void libraryFileCopyCompleted( String libFileName, File destination ) {
    }

}
