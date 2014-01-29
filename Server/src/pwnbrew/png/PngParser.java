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
* PngParser.java
*
* Created on July 21, 2013, 6:57:24 PM
*/

package pwnbrew.png;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import pwnbrew.logging.LoggableException;
import pwnbrew.png.Png.PngChunk;
import pwnbrew.utilities.SocketUtilities;

/**
 *
 *  
 */
public class PngParser {

    public static Png parse(File passedFile) throws LoggableException {

        Png aPNG = null;

        try {
            if(passedFile != null){

                //Create new PNG
                aPNG = new Png(passedFile);

                //Open the file
                FileInputStream theFileStream = new FileInputStream(passedFile);
                BufferedInputStream theBufferedIS = new BufferedInputStream(theFileStream);
                try{

                   int chunkBytes = 0;
                   byte[] chunkLenArr = new byte[4];
                   byte[] labelArr = new byte[4];
                   byte[] valueArr;
                   byte[] crcArr = new byte[4];

                   //Read the header
                   byte[] theHeader = new byte[8];
                   chunkBytes = theBufferedIS.read(theHeader);

                   if(!Arrays.equals(theHeader, Png.getHeader())){
                      throw new LoggableException("The passed file is not a valid PNG File.");
                   }

                   //Read all of the chunks
                   while(chunkBytes != -1){

                      //Read the chunk length
                      chunkBytes = theBufferedIS.read(chunkLenArr);
                      if(chunkBytes != 4){
                         if(chunkBytes == -1){
                            continue;
                         }
                         throw new LoggableException("Unable to read the PNG chunk length, aborting");
                      }

                      //Read the label
                      chunkBytes = theBufferedIS.read(labelArr);
                      if(chunkBytes != 4){
                         if(chunkBytes == -1){
                            continue;
                         }
                         throw new LoggableException("Unable to read the PNG chunk label, aborting");
                      }

                      //Read the value
                      int chunkLength = SocketUtilities.byteArrayToInt(chunkLenArr);
                      valueArr = new byte[chunkLength];

                      chunkBytes = theBufferedIS.read(valueArr);
                      if(chunkBytes != chunkLength){
                         if(chunkBytes == -1){
                            continue;
                         }
                         throw new LoggableException("Unable to read the PNG chunk value, aborting");
                      }

                      //Read the crc
                      chunkBytes = theBufferedIS.read(crcArr);
                      if(chunkBytes != 4){
                         if(chunkBytes == -1){
                            continue;
                         }
                         throw new LoggableException("Unable to read the PNG chunk crc, aborting");
                      }

                      PngChunk aChunk = new PngChunk(chunkLenArr, labelArr, valueArr, crcArr);
                      aPNG.addChunk( aChunk );

                   }

                } finally {
                   theBufferedIS.close();
                }
            }

        } catch (IOException ex){
           throw new LoggableException(ex);
        }
        return aPNG;
    }
}/* END CLASS PNGParser */
