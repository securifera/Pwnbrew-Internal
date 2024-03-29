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
* Message.java
*
* Created on Jun 2, 2013, 9:11:29 PM
*/

package pwnbrew.network;

import java.nio.ByteBuffer;
import pwnbrew.ClientConfig;
import pwnbrew.log.LoggableException;
import pwnbrew.manager.PortManager;
import pwnbrew.utilities.SocketUtilities;

/**
 *
 *  
 */
public abstract class Message {
    
    public static final byte STAGING_MESSAGE_TYPE = 0;
    public static final byte REGISTER_MESSAGE_TYPE = 40;
    public static final byte GENERIC_MESSAGE_TYPE = 87;
    public static final byte CONTROL_MESSAGE_TYPE = 88;
    public static final byte PROCESS_MESSAGE_TYPE = 89;
    public static final byte FILE_MESSAGE_TYPE = 90;
    public static final byte SOCKS_MESSAGE_TYPE = 91;

    public static final int SRC_HOST_ID_OFFSET = 5;
    public static final int DEST_HOST_ID_OFFSET = 9;
    public static final int CHANNEL_ID_OFFSET = 13;
    
    public static final int MSG_LEN_SIZE = 4;
    
    //Data members
    private final byte type;
    protected final byte[] length = new byte[MSG_LEN_SIZE];
    private final byte[] srcHostId = new byte[4];
    private final byte[] destHostId = new byte[4];
    protected byte[] msgChannelId = new byte[4];

    //Class name
    private static final String NAME_Class = Message.class.getSimpleName();

    //===========================================================================
    /**
     *  Constructor
     * 
     * @param passedType 
     */
    public Message(byte passedType ) {
        
        type = passedType;
        
        ClientConfig theConf = ClientConfig.getConfig();
        if( theConf != null ){
            SocketUtilities.intToByteArray(srcHostId, Integer.parseInt(theConf.getHostId())); 
            SocketUtilities.intToByteArray(destHostId,  theConf.getServerId());
        }
        
        SocketUtilities.intToByteArray(msgChannelId, 0);        
        
        //Set the base length
        length[3] = (byte)(1 + srcHostId.length + destHostId.length + msgChannelId.length);
    }
    
    //===========================================================================
    /**
     * 
     * @param passedType
     * @param passedId 
     */
    public Message(byte passedType, byte[] passedId ) {
        
        type = passedType;

        if(passedId != null){
           System.arraycopy(passedId, 0, msgChannelId, 0, msgChannelId.length );          
        } else {
           SocketUtilities.intToByteArray(msgChannelId, 0);
        }
        
        //Set the base length
        length[3] = (byte)(1 + srcHostId.length + destHostId.length + msgChannelId.length);;
    }
    
    //===============================================================
    /**
     * Sets the dest host id of the message
     *
     * @param passedId
    */
    public void setDestHostId( int passedId ){
        SocketUtilities.intToByteArray( destHostId, passedId );
    }
    
     //===============================================================
    /**
     * Returns the integer representation of the dest host id
     *
     * @return
    */
    public int getDestHostId(){
        return SocketUtilities.byteArrayToInt(destHostId);
    }

    //===============================================================
    /**
     * Returns the length of the 
     *
     * @return
    */
    public int getLength(){
        
        int count = 0;

        //Add the type
        count += 1;

        //Add the length
        count += length.length;
        
        //Add the ID
        count += msgChannelId.length;

        //Add Src Host ID
        count += srcHostId.length;
        
        //Add Dest Host ID
        count += destHostId.length;

        return count;
    }

    //===============================================================
    /**
     * Returns the integer representation of the msgId
     *
     * @return
    */
    public int getChannelId(){
        return SocketUtilities.byteArrayToInt(msgChannelId);
    }
    
     //===============================================================
    /**
     * Sets the channel id
     *
     * @param passedId
    */
    public void setChannelId( int passedId ){
       msgChannelId = SocketUtilities.intToByteArray(passedId);
    }
    
    //===============================================================
    /**
     * Returns the integer representation of the msgId
     *
     * @return
    */
    public int getSrcHostId(){
        return SocketUtilities.byteArrayToInt(srcHostId);
    }

    //===============================================================
    /**
     * Sets the client id of the message
     *
     * @param passedId
    */
    public void setSrcHostId( int passedId ){
        SocketUtilities.intToByteArray( srcHostId, passedId );
    }

   
    //===============================================================
    /**
    *   Performs the logic specific to the message.
    *
     * @param theManager
     * @throws pwnbrew.log.LoggableException
    */
    public void evaluate( PortManager theManager ) throws LoggableException {    
    }
    
    //===============================================================
    /**
    *   Returns a byte array representing the control message
    *
     * @param rtnBuffer
    */
    public void append( ByteBuffer rtnBuffer) {

        //Add the type
        rtnBuffer.put( type );

        //Add the length
        int fullLength = getLength() - ( length.length + 1);
         
        //Set the length
        SocketUtilities.intToByteArray(length, fullLength );
        rtnBuffer.put(length, 0, length.length );

        //Add Src Host ID
        rtnBuffer.put(srcHostId, 0, srcHostId.length );
        
         //Add Dest Host ID
        rtnBuffer.put(destHostId, 0, destHostId.length );
        
        //Add the Channel ID
        rtnBuffer.put(msgChannelId, 0, msgChannelId.length );

    }
    
      
    //================================================================
    /**
     *  Get a a byte array with the message bytes
     * 
     * @return 
     */
    public byte[] getBytes(){
        
        //Control message byte buffer
        int msgLen = getLength();
        ByteBuffer msgBuffer = ByteBuffer.allocate( msgLen );
        append(msgBuffer);
        
        //Convert to bytes
        byte[] msgBytes = new byte[ msgBuffer.position() ];
        msgBuffer.flip();
        msgBuffer.get( msgBytes, 0, msgBytes.length ); 
        
        return msgBytes;
    }

    //=========================================================================
    /**
     * Used to cancel an operation and clear the send queue
     * 
     * @return 
     */
    public int getCancelId() {
        return 0;
    }
    
}
