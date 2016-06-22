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
 * LogHandler.java
 *
 * Created on Nov 12, 2013, 9:12:31 PM
 */

package pwnbrew.log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;


/**
 * 
 */
@SuppressWarnings("ucd")
public class LogHandler extends FileHandler {

    private static final int DEFAULT_SizeLimit_Bytes = 300000; //300kB
    private static final int DEFAULT_FileLimit = 10;

    private static final String FORMAT_FileNameDate = "yyyy-MM-dd";
    private static final String FORMAT_FileNameSuffix = "-%g.txt";
    
    private LogFormatter theFormatter = null;
    private boolean publishToStdOut = false;
    
  
    // ==========================================================================
    /**
     * Constructor
     * 
     */
    LogHandler( File directory ) throws IOException, SecurityException {      
        super( createLogFileNamePattern( directory ), DEFAULT_SizeLimit_Bytes, DEFAULT_FileLimit, true );
    }
    
    
    // ==========================================================================
    /**
     * Creates the pattern for naming the log files.
     * 
     * @param directory the directory in which the log files will be placed
     * 
     * @return the pattern for naming the log files
     * 
     * @throws IOException if the given {@code File} does not exist or is not a directory
     * @throws NullPointerException if the argument is null
     */
    private static String createLogFileNamePattern( File directory ) throws IOException {
        
        if( directory.exists() == false )
            throw new IOException( new StringBuilder( "The directory \"" )
                    .append( directory.getPath() )
                    .append( "\" does not exist." ).toString() );
      
        if( directory.isDirectory() == false )
            throw new IOException( new StringBuilder( "The file \"" )
                    .append( directory.getPath() )
                    .append( "\" is not a directory." ).toString() );
            
        StringBuilder rtnBuilder = new StringBuilder();
        
        rtnBuilder.append( directory.getPath() )
          .append( File.separator )
          .append( ( new SimpleDateFormat( FORMAT_FileNameDate ) ).format( new Date() ) )
          .append( FORMAT_FileNameSuffix );

        //NOTE: The "%g" in the log filename pattern marks the position of the generation
        //  number. The file to which the new logs are written always has a generation
        //  number of zero. Each time the current log file reaches its size limit, this
        //  number will be incremented to 1 and a new file is created with a generation
        //  number of zero. The generation number of all previously existing log files
        //  is also incremented.
        //  For example, when the first log file reaches the limit, its generation number
        //  is incremented to 1 and a new file, with a generation number of zero, is created.
        //  When the new file reaches the limit, 1 is incremented to 2, 0 is incremented
        //  to 1, and a new file (again with a generation number of zero) is created.

        return rtnBuilder.toString();
        
    }/* END createLogFileNamePattern( String ) */
    
    
    // ==========================================================================
    /**
     * Sets the {@link LogFormatter}.
     * <p>
     * If the argument is null this method does nothing.
     * 
     * @param formatter the {@code LogFormatter}
     */
    public synchronized void setLogFormatter( LogFormatter formatter ) {
      
        if( formatter == null )
            return; //Do nothing
        
        theFormatter = formatter;
        super.setFormatter( formatter );
      
    }/* END setLogFormatter( LogFormatter ) */
    
    
    // ==========================================================================
    /**
     * Sets whether the log messages will be published to the stdout stream.
     * 
     * @param publish
     */
    public synchronized void setPublishToStdOut( boolean publish ) {
        publishToStdOut = publish;
    }/* END setPublishToStdOut( boolean ) */
    

    // ==========================================================================
    /**
     * Determines if the given {@link LogRecord} should be logged.
     * <p>
     * If a {@link Filter} has been set, this method returns the value returned by
     * the {@code Filter}'s isLoggable(). If no {@code Filter} is set, this method
     * will always return <tt>true</tt>.
     *
     * @param logRecord
     *
     * @return <tt>true</tt> if the given {@code LogRecord} should be logged, <tt>false</tt>
     * otherwise
     */
    @Override
    public boolean isLoggable( LogRecord logRecord ) {

        boolean rtnBool = true;

        Filter filter = getFilter();
        if( filter != null ) { //If there is a Filter...
            rtnBool = filter.isLoggable( logRecord ); //Have the filter determine if the LogRecord is loggable
        }

        return rtnBool;

    }/* END isLoggable( LogRecord ) */


    // ==========================================================================
    /**
     * Publishes the given {@link LogRecord}'s message to the log file and stdout.
     * <p>
     * If the given {@code LogRecord} is null this method does nothing.
     *
     * @param logRecord the {@code LogRecord} to publish
     */
    @Override
    public synchronized void publish( LogRecord logRecord ) {

        if( logRecord == null ) //If the given LogRecord is null...
            return; //Do nothing

        if( isLoggable( logRecord ) ) { //If the LogRecord is loggable...

            //Publish the LogRecord...

            super.publish( logRecord ); //Publish the log to the file
            //NOTE: The above call to super.publish(.) will eventually call the same isLoggable(.)
            //  in the condition of this if-block. It was simpler to allow the call to
            //  be repeated than to replicate the file-related logic and components
            //  from the super classes in this class. ( Also, the call is only repeated
            //  when isLoggable(.) returns true, otherwise this block is skipped. )

            if( publishToStdOut && theFormatter != null )
                System.out.print( theFormatter.getLastLogMessage() ); //Publish the log to stdout

        }

    }

}
