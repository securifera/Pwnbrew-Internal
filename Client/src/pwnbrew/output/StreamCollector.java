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
 * StreamCollector.java
 *
 * Created on Aug 22, 2013, 7:32:12PM
 */

package pwnbrew.output;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;


/**
 * StreamCollector is a {@link StreamReader} that writes the bytes read from the
 * {@link InputStream} into a StringBuilder
 * <p>
 *
 *  
 */
public class StreamCollector extends StreamReader {
  
    
    private StringBuilder theStringBuilder = new StringBuilder();
    private int maxLength = 1024 * 1000;

    // ==========================================================================
    /**
    * Creates a new instance of {@link StreamRecorder}.
    *
    */
    public StreamCollector() {
        super();
    }    

    // ==========================================================================
    /**
    * Called by {@link StreamReader} each time at least one byte is read from
    * the {@link InputStream}.
    * <p>
    * If the given {@code byte[]} is null, this method does nothing.
    *
    * @param buffer the buffer into which the bytes were read
    * @param numberRead the number of bytes read
    */
    @Override
    public void handleBytesRead( byte[] buffer, int numberRead ) {

        if( buffer == null || theStringBuilder.length() >= maxLength) { //If the byte[] is null...
            return; //Do nothing
        }
        
        try {
            theStringBuilder.append(new String(buffer, "US-ASCII"));
        } catch (UnsupportedEncodingException ex) {
            ex = null;
        }

    }/* END handleBytesRead( byte[], int ) */


    // ==========================================================================
    /**
    * Closes the output {@link File} and calls {@link StreamReader#handleEndOfStream}.
    * <p>
    * Called by {@link StreamReader} when the end of file is detected from the
    * {@link InputStream}.
    */
    @Override
    public void handleEndOfStream() {}/* END handleEndOfStream() */


    // ==========================================================================
    /**
    * Closes the output {@link File} and calls {@link StreamReader#handleIoException}.
    * <p>
    * Called by {@link StreamReader} if an {@link IOException} occurs.
    * <p>
    * If the given {@code IOException} is null, this method does nothing.
    *
     * @param exception
    */
    @Override
    protected void handleIoException( IOException exception ) {}/* END handleIoException( IOException ) */

  
    // ==========================================================================
    /**
    * Returns a String representing the data retrieved.
     * @return 
    */
    public String getString( ) {
    
        return theStringBuilder.toString();
    
    }/* END getString( ) */
    
    // ==========================================================================
    /**
    * Sets the max length of the field
     * @param passedLength
    */
    public void setMaxLength( int passedLength ) {
    
        maxLength = passedLength;
    
    }/* END setMaxLength( ) */

  
  
}
