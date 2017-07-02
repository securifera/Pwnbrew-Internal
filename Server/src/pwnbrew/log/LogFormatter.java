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
 * LogFormatter.java
 *
 * Created on Nov 11, 2013, 8:26:25 PM
 */

package pwnbrew.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;


/**
 * 
 */

@SuppressWarnings("ucd")
public class LogFormatter extends SimpleFormatter {

    private static final String LINE_SEPARATOR = System.getProperty( "line.separator" );
  
    //Date and time format...
    private static final String FORMAT_DateTime = "yyyy/MM/dd HH:mm:ss.SSS";
    private final SimpleDateFormat theDateFormat = new SimpleDateFormat( FORMAT_DateTime );
    private final Date theDate = new Date();

    //Log level format...
    private static final String FORMAT_LevelName = "%1$-10s";

    private final StringBuilder theStringBuilder = new StringBuilder(); //Used to build the log message
    private String theLevelName = null;

    private StringWriter theStringWriter = null; //Used to capture the stack trace of Throwables
    private PrintWriter thePrintWriter = null; //Used to capture the stack trace of Throwables


    // ==========================================================================
    /**
     * Formats the message of the given {@link LogRecord}.
     * <p>
     * If the argument is null this method does nothing and returns null.
     *
     * @param logRecord the {@code LogRecord}
     *
     * @return a formatted log message
     */
    @Override
    public synchronized String format( LogRecord logRecord ) {

        if( logRecord == null ) //If the LogRecord is null...
            return null; //Do nothing

        theStringBuilder.setLength( 0 ); //Clear the StringBuilder

        //Date and time...
        theDate.setTime( logRecord.getMillis() ); //Set the date/time of the log
        theStringBuilder.append( theDateFormat.format( theDate ) ); //Append the date/time

        //The message level...
        theStringBuilder.append( " " );
        theLevelName = logRecord.getLevel().getLocalizedName();
        theStringBuilder.append( String.format( FORMAT_LevelName, theLevelName ) ); //Append the name of the message's level

        if( logRecord.getLevel().intValue() < 0 ) { //If the LogRecord's Level has a negative value...
            //The LogRecord carries a debug message. Add the class and method names.

            //Source class name...
            theStringBuilder.append( " " );
            if( logRecord.getSourceClassName() != null ) //If the source class's name is known...
                theStringBuilder.append( logRecord.getSourceClassName() ); //Append the source class's name
            else //If the source class's name is not known...
                theStringBuilder.append( logRecord.getLoggerName() ); //Append the logger's name

            //Source method name...
            if( logRecord.getSourceMethodName() != null ) //If the source method's name is known...
                //Append the source method's name
                theStringBuilder.append( " " ).append( logRecord.getSourceMethodName() );

            theStringBuilder.append( " -" );

        }

        //The message...
        theStringBuilder.append( " " ).append( formatMessage( logRecord ) ); //Append the formatted message

        //Throwable handling...
        if( logRecord.getThrown() != null ) { //If the LogRecord has a Throwable...

            theStringBuilder.append( LINE_SEPARATOR ); //Append a line separator
            try {

                //Add the Throwable's stack trace...
                theStringWriter = new StringWriter();
                thePrintWriter = new PrintWriter( theStringWriter );
                logRecord.getThrown().printStackTrace( thePrintWriter );
                theStringBuilder.append( theStringWriter.toString() );

            } finally {

                thePrintWriter.flush();
                thePrintWriter.close();

                theStringWriter = null; //Null the references
                thePrintWriter = null;
            }

        }

        theStringBuilder.append( LINE_SEPARATOR ); //Append a line separator
        
        //Parameters...
        Object[] parameters = logRecord.getParameters();
        if( parameters != null && parameters.length > 0 ) {
            
            Object parameter = null;
            for( int i = 0; i < parameters.length; i++ ) { //For each parameter...
              
                parameter = parameters[ i ];
                if( parameter != null )
                  theStringBuilder.append( "  " ).append( i ).append( " - " )
                          .append( parameter.getClass().getSimpleName() ).append( " - " )
                          .append( parameter.toString() ).append( LINE_SEPARATOR );
                
            }
          
            theStringBuilder.append( LINE_SEPARATOR ); //Append a line separator
        
        }

        return theStringBuilder.toString(); //Return the formatted log message

    }


    // ==========================================================================
    /**
     * Returns the last message the {@link LogFormatter} formatted.
     * <p>
     * @return the last message the {@code LogFormatter} formatted, an empty
     * String if no message has yet been formatted
     */
    public synchronized String getLastLogMessage() {
        return ( theStringBuilder == null ? "" : theStringBuilder.toString() );
    }

}
