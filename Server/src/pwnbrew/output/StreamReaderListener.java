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
 * StreamReaderListener.java
 *
 * Created on July 9, 2013, 9:08:32 PM
 */

package pwnbrew.output;

import java.io.IOException;


/**
 */
public interface StreamReaderListener {

    // ==========================================================================
    /**
    * Called by a {@link StreamReader} each time it reads bytes from its {@link InputStream}.
    * <p>
    * The bytes placed in the buffer during the read will occupy the elements at
    * indices 0 - (numberRead - 1).
    *
    * @param reader the {@code StreamReader}
    * @param buffer the buffer into which the bytes were read
    * @param numberRead the number of bytes read
    */
    public void handleBytesRead( StreamReader reader, byte[] buffer, int numberRead );


    // ==========================================================================
    /**
    * Called by a {@link StreamReader} when it detects the end of file in its {@link InputStream}.
    *
    * @param reader the {@code StreamReader}
    */
    public void handleEndOfStream( StreamReader reader );


    // ==========================================================================
    /**
    * Called by a {@link StreamReader} when reading from its {@link InputStream} throws
    * an {@link IOException}.
    *
    * @param reader the {@code StreamReader}
    * @param ex the {@code IOException} thrown
    */
    public void handleIOException( StreamReader reader, IOException ex );

}
