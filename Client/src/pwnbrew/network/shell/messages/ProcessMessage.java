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
 *  ProcessMessage.java
 *
 *  Created on July 25, 2013
 */

package pwnbrew.network.shell.messages;

import java.nio.ByteBuffer;
import java.util.Arrays;
import pwnbrew.log.LoggableException;
import pwnbrew.utilities.SocketUtilities;
import pwnbrew.network.Message;
import static pwnbrew.network.Message.PROCESS_MESSAGE_TYPE;

/**
 *
 *  
 */
abstract public class ProcessMessage extends Message {
    
    private byte[] messageBytes = new byte[0];
    
    //Functions
    static final byte STD_IN = 2;
    static final byte STD_OUT = 3;
    static final byte STD_ERR = 4;
    
    private final byte function;
   
    //==========================================================================
    /**
     * Constructor
     *
     * @param passedFunction
     * @param passedChannelId
     * @param passedBB
    */
    @SuppressWarnings("ucd")
    public ProcessMessage( byte passedFunction, int passedChannelId, ByteBuffer passedBB ) {
        super( PROCESS_MESSAGE_TYPE );
        function = passedFunction;
        msgChannelId = SocketUtilities.intToByteArray( passedChannelId );
          
        setBytes(passedBB);
    }
    
    //==========================================================================
    /**
     * Constructor
     *
     * @param passedFunction
     * @param passedId
    */
    @SuppressWarnings("ucd")
    public ProcessMessage( byte passedFunction, byte[] passedId) {
        super(PROCESS_MESSAGE_TYPE, passedId);
        function = passedFunction;
    }
    
    //==========================================================================
    /*
     *  Sets the process bytes
    */     
    private void setBytes( ByteBuffer passedBB ){
        messageBytes = new byte[passedBB.remaining()];
        passedBB.get(messageBytes, 0, messageBytes.length);
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
        
         //Add the function
        rtnBuffer.put( function );
               
        //Add the message bytes
        rtnBuffer.put( messageBytes );
        
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
       
        //Add function
        count++;
          
        //Add message bytes
        count += messageBytes.length;
        
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
    public static ProcessMessage getMessage( ByteBuffer passedBuffer ) throws LoggableException {

       ProcessMessage aMessage;       
       byte[] theId = new byte[4], clientId = new byte[4], tempHostId = new byte[4];

       //Copy over the client id
       passedBuffer.get(clientId, 0, clientId.length);
       
       //Copy over the host id
       passedBuffer.get(tempHostId, 0, tempHostId.length);
       
        //Copy over the id
       passedBuffer.get(theId, 0, theId.length);

       //Copy over the id
       byte tmpFunc = passedBuffer.get();

       switch(tmpFunc){
           case STD_IN:
               aMessage = new StdInMessage( theId );
               break;
           case STD_OUT:
               aMessage = new StdOutMessage( theId );
               break;
           case STD_ERR:
               aMessage = new StdErrMessage( theId );
               break;           
           default:
               throw new LoggableException("Unknown message type encountered.");
       }
       
       //Set client id
       int theClientId = SocketUtilities.byteArrayToInt(clientId);
       aMessage.setSrcHostId( theClientId );
       
       //Set dest host id
       int theDestHostId= SocketUtilities.byteArrayToInt(tempHostId);
       aMessage.setDestHostId( theDestHostId );
       
       //Set bytes
       aMessage.setBytes(passedBuffer);

       return aMessage;
    }
    
    //===============================================================
    /**
     *  Gets the message bytes
     * 
     * @return 
     */
    public byte[] getMsgBytes() {
        return Arrays.copyOf(messageBytes, messageBytes.length);    
    }
    
}
