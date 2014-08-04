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
 * Log.java
 *
 * Created on July 20, 2013, 7:24:29 PM
 */

package pwnbrew.logging;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import pwnbrew.misc.Directories;
import pwnbrew.misc.DebugPrinter;

/**
 *
 */
final public class Log {

    private static final Logger theLogger = Logger.getLogger( "PwnBrew" );
    private static LogHandler theLogHandler;
    private static final LogFilter theLogFilter = new LogFilter();

    private static LogFormatter theLogFormatter;    
    private static boolean hasBeenInitialized = false;  
    
    // ==========================================================================
    /**
     * Contructor
     */
    private Log() {
    }
    
    // ==========================================================================
    /**
     * Initializes the {@link Log}.     
     * 
     * @param directory the directory in which to place the log files
     * 
     * @throws IOException if the file represented by the given {@code File} does
     * not exist or is not a directory or if there are IO problems opening the files
     * @throws NullPointerException if the argument is null
     */
    public static void initializeLog( File directory ) throws IOException {
      
        if( hasBeenInitialized ) //If the logging has already been initialized
            return; //Do nothing
        
        //Create the directories is they do not exisit
        Directories.ensureDirectoryExists(directory);
        
        theLogHandler = new LogHandler( directory );
            
        //LogFilter...
        theLogFilter.allow( LogLevel.SEVERE ); //Allow default levels...
        theLogFilter.allow( LogLevel.WARNING );
        theLogFilter.allow( LogLevel.INFO );
        theLogFilter.allow( LogLevel.CONFIG );
        theLogFilter.allow( LogLevel.FINE );
        theLogFilter.allow( LogLevel.FINER );
        theLogFilter.allow( LogLevel.FINEST );

        //LogFormatter...
        theLogFormatter = new LogFormatter();
        
        theLogHandler.setFilter( theLogFilter );
        theLogHandler.setLogFormatter( theLogFormatter );

        //Logger...
        theLogger.setUseParentHandlers( false );
        theLogger.setLevel( LogLevel.ALL );

        theLogger.addHandler( theLogHandler ); //Add the LogHandler to the Logger
        hasBeenInitialized = true;

    }/* END initializeLog() */

    // ==========================================================================
    /**
     * Logs a message, specifying source class and method, with associated {@link Throwable}
     * information at the given {@link Level}.
     *
     * @param	level	the {@code Level} at which the message is to be logged
     * @param sourceClass name of the class that issued the logging request
     * @param sourceMethod name of the method that issued the logging request
     * @param message the message to log
     * @param thrown the {@code Throwable}
     */
    public static synchronized void log(
            Level level, String sourceClass, String sourceMethod, String message, Throwable thrown ) {
      
        if( theLogger != null ) { //If the Logger was created...
            
            if( thrown instanceof LoggableException ){
                thrown = ((LoggableException)thrown).getException();
                if( thrown != null ){
                    message = thrown.getMessage();
                }
            }
            
            LogRecord logRecord = new LogRecord( level, message );
            logRecord.setSourceClassName( sourceClass );
            logRecord.setSourceMethodName( sourceMethod );
            logRecord.setThrown( thrown );
            theLogger.log( logRecord );
        } else {
            try {
                //No logger has been initialized
                File logFile = new File(Directories.getLogPath(), "fatal_err.log");
                RandomAccessFile aFile = new RandomAccessFile(logFile, "rw");
                
                try {
                    aFile.write(thrown.toString().getBytes("US-ASCII"));
                } finally {
                    aFile.close();
                }
                
            } catch (IOException ex) {
                ex = null;
            } 
        }
        
        //Print to screen if debugging   
        if( thrown != null )
            message = thrown.getClass().getSimpleName() + " - " + message;          
        
        DebugPrinter.printMessage( new StringBuilder().append(sourceClass).append(".")
                .append(sourceMethod).toString(), message);  
        
        //Print stace trace
        if( thrown != null ){
            DebugPrinter.printException(thrown);
        }
               
        
    }/* END log( Level, String, String, String, Throwable ) */

}/* END CLASS Log */
