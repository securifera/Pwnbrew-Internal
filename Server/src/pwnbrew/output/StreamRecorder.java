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
import pwnbrew.utilities.FileUtilities;


/**
* 
*/
public class StreamRecorder extends StreamReader {

    private File theOutputFile = null;
    private FileOutputStream theFileOutputStream = null;


    // ==========================================================================
    /**
    * Constructor
    *
    */
    public StreamRecorder() {
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
    *
    * @param file a {@link File} representing the file to which the bytes will be
    * written
     * @return 
    */
    public boolean setOutputFile( File file ) {

        if( file == null ) { //If the File is null...
            return false; //Do nothing
        }

        boolean rtnBool = false;

        boolean canWriteToFile = false;
        try {
            canWriteToFile = FileUtilities.verifyCanWrite( file ); //Verify the file can be written to
            //NOTE: This call creates the file if it doesn't exist, but won't attempt
            //  to create any parent directories.
        } catch( IOException ex ) {
            //Do nothing; return false
            ex = null;
        }

        if( canWriteToFile ) { //If the file can be written to...

            try {
                theFileOutputStream = new FileOutputStream( file ); //Create the FileOutputStream
            } catch( FileNotFoundException ex ) {
                //This should never occur since the file's existence was verified earlier
                //  in this method, but just in case, try to re-create the file and the
                //  FileOutputStream.

                try {
                    if( file.createNewFile() ) { //If the file is created...
                        theFileOutputStream = new FileOutputStream( file ); //Create the FileOutputStream
                    }
                } catch( IOException ex2 ) {
                    //Do nothing (What more can be done?)
                    ex2 = null;
                }

            }

            if( theFileOutputStream != null ) { //If the FileOutputStream was created...
                theOutputFile = file;
                rtnBool = true; //The File is set
            }

        } //If the file cannot be written to, return false

        return rtnBool;

    }/* END setOutputFile( File ) */


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
    }/* END getOutputFile() */


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

        if( buffer == null ) 
            return; 

        try {
            if( theFileOutputStream != null ){
                theFileOutputStream.write( buffer, 0, numberRead );
            }
        } catch( IOException ex ) {
            aborting = true; 
        }

        if( aborting == false )
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
                theFileOutputStream.close(); //Close the FileOutputStream
            }
        } catch( IOException ex ) {
            //Do nothing
            ex = null;
        }

        super.handleEndOfStream(); //Handle the end of the stream as a StreamReader

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
            //Do nothing
            ex = null;
        }

        super.handleIoException( exception ); 

    }

}
