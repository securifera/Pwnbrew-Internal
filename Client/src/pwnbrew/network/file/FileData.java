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
* FileMessage.java
*
* Created on June 5, 2013, 6:05:08 PM
*/

package pwnbrew.network.file;

import java.nio.ByteBuffer;
import java.util.Arrays;
import pwnbrew.log.LoggableException;
import pwnbrew.utilities.SocketUtilities;
import pwnbrew.network.Message;


/**
 *
 *  
 */
public class FileData extends Message {
    
    private final byte[] fileId = new byte[4];
    private byte[] fileBytes = new byte[0];
   
    //=========================================================================
    /*
     *  Contructor
     */
    public FileData( int passedId, byte[] byteVal ) { // NO_UCD (use default)
        
        //Set id
        super( FILE_MESSAGE_TYPE );
        SocketUtilities.intToByteArray( fileId, passedId);
        fileBytes = byteVal;
        
    }
    
     //=========================================================================
    /*
     *  Contructor
     */
    public FileData( int passedFileId, byte[] passedMsgId, byte[] byteVal ) {
        //Set id
        super( FILE_MESSAGE_TYPE, passedMsgId);
        SocketUtilities.intToByteArray( fileId, passedFileId);
        fileBytes = Arrays.copyOf(byteVal, byteVal.length);
    }

    //===============================================================
    /**
    *   Returns a byte array representing the control message
    *
     * @param rtnBuffer
    */
    @Override
    public void append( ByteBuffer rtnBuffer) {
    
        //Add the parent
        super.append(rtnBuffer);
        
        //Add the file id
        rtnBuffer.put( fileId );
        
        //Add the file byte value
        rtnBuffer.put( fileBytes );
        
    }
    
    //===============================================================
    /**
     *  Get the file id
     * 
     * @return 
     */
    public Integer getFileId(){
        return SocketUtilities.byteArrayToInt(fileId);
    }
    
    //===============================================================
    /**
     *  Get the file bytes
     * 
     * @return 
     */
    public byte[] getFileBytes(){
        return Arrays.copyOf( fileBytes, fileBytes.length );
    }
    
     
    //===============================================================
    /**
     * Returns the length of the 
     *
     * @return
    */
    @Override
    public int getLength(){
    
        int count = 0;
        count += super.getLength();
        
        //Add the function
        count += fileId.length;
        
        //Add the file bytes
        count += fileBytes.length;
        
        //Set the length
        SocketUtilities.intToByteArray(length, count );
        
        return count;
    }
    
     //===============================================================
    /**
     *  Returns a message of the appropriate function type.
     *
     * @param passedBuffer
     * @return msgAddress
     * @throws pwnbrew.log.LoggableException
    */
    @SuppressWarnings("ucd")
    public static FileData getMessage( ByteBuffer passedBuffer ) throws LoggableException {

        byte[] theMsgId = new byte[4], theFileId = new byte[4],clientId = new byte[4], tempHostId = new byte[4];
        FileData aMessage;

        //Copy over the client id
        passedBuffer.get(clientId, 0, clientId.length);

        //Copy over the dest host id
        passedBuffer.get(tempHostId, 0, tempHostId.length);

        //Copy over the msg id
        passedBuffer.get(theMsgId, 0, theMsgId.length);
        int msgId = SocketUtilities.byteArrayToInt(theFileId);

        //Copy over the id
        passedBuffer.get(theFileId, 0, theFileId.length);
        int fileId = SocketUtilities.byteArrayToInt(theFileId);

        //Copy over the id
        byte[] theFileBytes = new byte[ passedBuffer.remaining() ];
        passedBuffer.get(theFileBytes, 0, theFileBytes.length);

        //Create the message type
        aMessage = new FileData( fileId, theMsgId, theFileBytes );
        aMessage.setClientId(SocketUtilities.byteArrayToInt(clientId));
        aMessage.setDestHostId(SocketUtilities.byteArrayToInt(tempHostId) );

        return aMessage;
       
    }
    
}/* END CLASS ControlMessage */
