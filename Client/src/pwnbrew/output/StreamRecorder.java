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
 * StreamRecorder.java
 *
 * Created on Nov 12, 2013, 1:40:22 PM
 */

package pwnbrew.output;

import java.io.*;
import pwnbrew.misc.FileUtilities;


/**
* StreamRecorder is a {@link StreamReader} that writes the bytes read from the
* {@link InputStream} into a file.
*
*/
public class StreamRecorder extends StreamReader {

    private File theOutputFile = null;
    private FileOutputStream theFileOutputStream = null;


    // ==========================================================================
    /**
    * Constructor
    */
    public StreamRecorder() {
        super();
    }
    
    // ==========================================================================
    /**
    * Creates a new instance of {@link StreamRecorder}.
    * 
    * @param stream the {@code InputStream} from which to read
    */
    public StreamRecorder( InputStream stream ) {
        super( stream ); 
    }


    // ==========================================================================
    /**
    * Sets the file to which the {@link StreamRecorder} will write the bytes from
    * the {@link InputStream}.
    * <p>
    * If the given {@code File} is null or the {@code StreamRecorder} has already
    * been started, this method does nothing and returns <tt>false</tt>.
    * <p>
    * The given {@code File} is "set" if and only if...
    *   <ul>
    *     <li>it represents a file (as opposed to a directory)
    *     <li>it represents a file that exists (or can be created in an existing parent directory)
    *     <li>it represents a file that can be written to by the application
    *     <li>no {@link IOException} occurs while evaluating these conditions
    *   </ul>
    * If all of these conditions are met, the {@code File} is "set" and this method
    * returns <tt>true</tt>. Otherwise, the method has no effect and <tt>false</tt>
    * is returned.
    * <p>
    * NOTE: This method will attempt to create a non-existent file only if its parent
    * directories exist. It will not attempt to create any parent directories.
    *
    * @param file a {@link File} representing the file to which the bytes will be
    * written
     * @return 
    */
    public boolean setOutputFile( File file ) {

        if( file == null )
            return false; 

        boolean rtnBool = false;
        boolean canWriteToFile = false;
        try {
            canWriteToFile = FileUtilities.verifyCanWrite( file ); 
        } catch( IOException ex ) {
            ex = null;
        }

        if( canWriteToFile ) { 

            try {
                theFileOutputStream = new FileOutputStream( file );
            } catch( FileNotFoundException ex ) {
             
                try {
                    if( file.createNewFile() )
                        theFileOutputStream = new FileOutputStream( file );
                } catch( IOException ex2 ) {
                    ex2 = null;
                }

            }

            if( theFileOutputStream != null ) { 
                theOutputFile = file;
                rtnBool = true; 
            }

        } 

        return rtnBool;

    }


    // ==========================================================================
    /**
    * Returns a {@link File} representing the file to which the bytes read from the
    * {@link InputStream} are written.
    *
    * @return a {@link File} representing the file to which the bytes read from the
    * {@link InputStream} are written; null if the {@code File} has not been set
    */
    public File getOutputFile() {
        return theOutputFile;
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

        if( buffer == null ) { //If the byte[] is null...
            return; //Do nothing
        }

        try {
            if( theFileOutputStream != null ){
                theFileOutputStream.write( buffer, 0, numberRead ); //Write the bytes to the file
                theFileOutputStream.flush();
            }
        } catch( IOException ex ) {
            shutdown();
        }

        super.handleBytesRead( buffer, numberRead );

    }


    // ==========================================================================
    /**
    * Closes the output {@link File} and calls {@link StreamReader#handleEndOfStream}.
    * <p>
    * Called by {@link StreamReader} when the end of file is detected from the
    * {@link InputStream}.
    */
    @Override
    public void handleEndOfStream() {

        try {
            if( theFileOutputStream != null ){
                theFileOutputStream.getFD().sync();
                theFileOutputStream.close(); 
            }
        } catch( IOException ex ) {
            ex = null;
        }

        super.handleEndOfStream(); 

    }

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
    protected void handleIoException( IOException exception ) {

        if( exception == null )
            return; 

        try {
            if( theFileOutputStream != null )
                theFileOutputStream.close();            
        } catch( IOException ex ) {
            ex = null;
        }

        super.handleIoException( exception );
    }

}
