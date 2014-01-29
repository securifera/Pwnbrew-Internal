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
 * Created on Nov 9, 2013, 9:22:42 PM
 */

package pwnbrew.output;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidParameterException;
import java.util.logging.Level;
import pwnbrew.logging.Log;


/**
* StreamReader reads the bytes from an {@link InputStream} and reports to
* an {@link IStreamReaderListener}.
* <p>
*
*/
public class StreamReader implements Runnable {

    private static final String NAME_Class = StreamReader.class.getSimpleName();
    private InputStream theInputStream = null; //The InputStream from which to read
    private StreamReaderListener theListener = null; //The listener to which the read bytes will be passed

    private int theBufferLength = 1000; //The maximum number of bytes to read at one time
    private int theNumberOfBytesRead = 0; //The number of bytes that have hitherto been read
    protected volatile boolean aborting = false;


    // ==========================================================================
    /**
    * Constructor
    */
    public StreamReader(){
    }/* END CONSTRUCTOR() */

    // ==========================================================================
    /**
    * Constructor
    *
    * @param stream the {@code InputStream} from which to read
    *
    * @throws InvalidParameterException if the given {@code InputStream} is null
    */
    public StreamReader( InputStream stream ) { // NO_UCD (use default)

        if( stream == null ) { //If the InputStream is null...
            throw new InvalidParameterException();
        }

        theInputStream = stream;

    }


    // ==========================================================================
    /**
    * Sets the {@link IStreamReaderListener} to notify when bytes are read or the
    * end of the stream is reached.
    * <p>
    * If the {@link StreamReader} has already been started, this method does nothing.
    * 
    * @param listener the {@code IStreamReaderListener} to notify
    */
    public void setIStreamReaderListener( StreamReaderListener listener ) {

        theListener = listener; //Set the IStreamReaderListener

    }


    // ==========================================================================
    /**
    * Reads bytes from the {@link InputStream}.
    * <p>
    * This method calls {@link InputStream#read( byte[], int, int )} which <strong>WILL BLOCK</strong>
    * until data is received or an {@code IOException} occurs.
    */
    @Override //Runnable
    public void run() {

        int numberOfBytesJustRead = 0;
        byte[] buffer = new byte[ theBufferLength ];
        while( numberOfBytesJustRead != -1 && //Until the end of the InputStream has been reached or...
        !aborting ) { //...an abort is triggered...

            try {
                numberOfBytesJustRead = theInputStream.read( buffer, 0, buffer.length );
          
            } catch( IOException ioex ) {
                handleIoException( ioex ); //Handle the IOException
                break; //Stop looping
            }

            if( numberOfBytesJustRead > 0 ) { //If any bytes were read...

                theNumberOfBytesRead += numberOfBytesJustRead; //Add the number of bytes read to the total
                handleBytesRead( buffer, numberOfBytesJustRead ); //Handle the bytes

            }

        } //End of "while( numberOfBytesJustRead != -1 || //Until the end of the InputStream has been reached or..."

        if( numberOfBytesJustRead == -1 ) { //If the end of file was detected...
            try {
                //Handle the end of the stream
                theInputStream.close();
                handleEndOfStream(); //Handle the end of the stream
            } catch (IOException ex) {
                Log.log(Level.SEVERE, NAME_Class, "run()", ex.getMessage(), ex );
            }
        }

    }


    // ==========================================================================
    /**
    * Called by {@link StreamReader#act} each time at least one byte is read from
    * the {@link InputStream}.
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
            theListener.handleBytesRead( this, buffer, numberRead );
    }


    // ==========================================================================
    /**
    * Called by {@link StreamReader#act} when the end of file is detected from the
    * {@link InputStream}.
    */
    public void handleEndOfStream() {

        if( theListener != null )
            theListener.handleEndOfStream( this );        

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
            return; //Do nothing        

        if( theListener != null )
            theListener.handleIOException( this, exception );
        

    }


    // ==========================================================================
    /**
    * Returns the number of bytes that have hitherto been read.
    *
    * @return the number of bytes that have hitherto been read
    */
    public int getNumberOfBytesRead() {
        return theNumberOfBytesRead;
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
        return theBufferLength;
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
            theBufferLength = length;         

    }

    // ==========================================================================
    /**
    * Sets the input stream.
    *
     * @param stream
    */
    public void setInputStream( InputStream stream ) {

        if( stream == null ) { //If the InputStream is null...
            throw new InvalidParameterException();
        }

        theInputStream = stream;

    }/* END setInputStream( InputStream stream ) */


    // ==========================================================================
    /**
    * Triggers an abort.
    * <p>
    * Calling this method will signal {@link StreamReader} to stop reading bytes
    * from the {@link InputStream} and end its run.
    * <p>
    * NOTE: Triggering an abort will prevent {@code StreamReader} from performing
    * another read from the {@code InputStream}, but does not force it to abandon
    * a read in progress. If an abort is triggered, {@code StreamReader} will complete
    * its read in progress, handle any bytes read as normal, then end its run.
    */
    public void abort() {
        aborting = true;
    }

}/* END CLASS StreamReader */
