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
 * FileUtilities.java
 *
 * Created on June 2, 2013, 11:59 AM
 */

package pwnbrew.utilities;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 */
abstract public class FileUtilities {
  

    // ==========================================================================
    /**
     * Returns the os temp dir
     * <p>
     * 
     * @return a {@link File} representing the os temp dir
     * @throws java.io.IOException
    */
    public static File getTempDir() throws IOException {

        File aFile = File.createTempFile("ttf", null);
        File rtnFile = aFile.getParentFile();
        aFile.delete();

        return rtnFile;
    }

  
    // ==========================================================================
    /**
    * Returns the SHA-256 hash for the passed file
    *
    * @param aFile
    *
    * @return a {@code String} representing the hash of the file
    *
    * @throws FileNotFoundException if the given {@code File} represents a directory,
    * or represents a file that doesn't exist or cannot be read by the application
    * @throws IOException if a problem occurs while reading the file
    * @throws NoSuchAlgorithmException if the hash algorithm cannot be found
    */
    public static String getFileHash(File aFile ) throws NoSuchAlgorithmException, IOException {

        int bytesRead = 0;
        byte[] byteBuffer = new byte[Constants.GENERIC_BUFFER_SIZE];
        String theHash = "";

        if( aFile.exists() && aFile.canRead() ) { //If the file can be read...

            MessageDigest hash = MessageDigest.getInstance(Constants.HASH_FUNCTION);
            FileInputStream theFileStream = new FileInputStream(aFile);
            BufferedInputStream theBufferedIS = new BufferedInputStream(theFileStream);

            try {

                //Read in the bytes and update the hash
                while( bytesRead != -1){
                    bytesRead = theBufferedIS.read(byteBuffer);
                    if(bytesRead > 0)
                        hash.update(byteBuffer, 0, bytesRead);                    
                }

                byte[] byteHash = hash.digest();
                theHash = Utilities.byteToHexString(byteHash);

            } finally {

                //Ensure the file is closed
                try {
                    theBufferedIS.close();
                } catch(IOException ex){
                    ex = null;
                }
            }

        } else { //If the file doesn't exist or is not actually a file...
            throw new FileNotFoundException();
        }

        return theHash;

    }

}/* END CLASS FileUtilities */
