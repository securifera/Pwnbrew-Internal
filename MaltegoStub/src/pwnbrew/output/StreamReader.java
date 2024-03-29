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
 * StreamReader.java
 *
 */

package pwnbrew.output;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidParameterException;
import java.util.Arrays;
import pwnbrew.misc.Constants;
import pwnbrew.misc.DebugPrinter;
import pwnbrew.misc.ManagedRunnable;


/**
*
*/
public class StreamReader extends ManagedRunnable {

    private static final String NAME_Class = StreamReader.class.getSimpleName();
    private InputStream theInputStream = null; 
    private StreamReaderListener theListener = null;

    private int theMaxBufferSize = 1000; 
    private final int theStreamId;
  
    // ==========================================================================
    /**
    * Constructor
     * @param passedId
    */
    public StreamReader( int passedId ){
        super(Constants.Executor);
        theStreamId = passedId;
    }

    // ==========================================================================
    /**
    * Constructor
    *
     * @param passedId
    * @param stream the {@code InputStream} from which to read
    *
    * @throws InvalidParameterException if the given {@code InputStream} is null
    */
    public StreamReader( int passedId, InputStream stream ) { // NO_UCD (use default)

        this(passedId);
        if( stream == null )
            throw new InvalidParameterException();        

        theInputStream = stream;
    }


    // ==========================================================================
    /**
    * Sets the Listener to notify when bytes are read or the
    * end of the stream is reached.
    * <p>
    * If the {@link StreamReader} has already been started, this method does nothing.
    * 
    * @param listener 
    */
    public void setStreamReaderListener( StreamReaderListener listener ){ 
        theListener = listener; 
    }


    // ==========================================================================
    /**
    * Reads bytes from the {@link InputStream}.
    * <p>
    */
    @Override //Runnable
    public void go() {

        int bytesRead = 0;
        byte[] buffer = new byte[ theMaxBufferSize ];
        while( bytesRead != -1 && !shutdownRequested ) { 

            try {
                bytesRead = theInputStream.read( buffer, 0, buffer.length );          
            } catch( IOException ioex ) {
                break; 
            }

            //Handle the bytes read
            if( bytesRead > 0 )
                handleBytesRead( buffer, bytesRead );

        } 
        
        try {
            //Handle the end of the stream
            theInputStream.close();
            handleEndOfStream();
        } catch (IOException ex) {
            DebugPrinter.printMessage( NAME_Class, "routeMessage", ex.getMessage(), ex);
        }

    }


    // ==========================================================================
    /**
    * Handle the bytes read
    * <p>
    * If the given {@code byte[]} is null, this method does nothing.
    *
    * @param buffer the buffer into which the bytes were read
    * @param numberRead the number of bytes read
    */
    public void handleBytesRead( byte[] buffer, int numberRead ) {

        if( buffer == null )
            return;        

        if( theListener != null )
            theListener.handleBytesRead( theStreamId, Arrays.copyOf(buffer, numberRead) );
    }


    // ==========================================================================
    /**
    * Called by {@link StreamReader#act} when the end of file is detected from the
    * {@link InputStream}.
    */
    public void handleEndOfStream() {
        if( theListener != null )
            theListener.handleEndOfStream( theStreamId );        

    }


    // ==========================================================================
    /**
    * Called by {@link StreamReader#act} if an {@link IOException} occurs.
    * <p>
    * If the given {@code IOException} is null, this method does nothing.
    * 
     * @param exception
    */
    protected void handleIoException( IOException exception ) {

        if( exception == null )
            return;   

        if( theListener != null )
            theListener.handleIOException( theStreamId, exception );        

    }

    // ==========================================================================
    /**
    * Returns the {@link InputStream} that was given to the {@link StreamReader}
    * when it was instantiated.
    *
    * @return the {@code InputStream} that was given to the {@code StreamReader}
    * when it was instantiated
    */
    public InputStream getInputStream() {
        return theInputStream;
    }

    // ==========================================================================
    /**
    * Returns the length of the byte buffer.
    *
    * @return the length of the byte buffer
    */
    public int getBufferLength() {
        return theMaxBufferSize;
    }

    // ==========================================================================
    /**
    * Sets the length of the byte buffer.
    * <p>
    * The length of the byte buffer is the maximum number of bytes the {@code StreamReader}
    * will read at one time.
    * <p>
    * If the {@code StreamReader} has already been started or the given number is
    * less than 1, this method does nothing.
    *
    * @param length the length of the byte buffer
    */
    public void setBufferLength( int length ) {
        if( length > 0 )
            theMaxBufferSize = length; 
    }

    // ==========================================================================
    /**
    * Sets the input stream.
    *
     * @param stream
    */
    public void setInputStream( InputStream stream ) {

        if( stream == null )
            throw new InvalidParameterException();        

        theInputStream = stream;

    }

}
