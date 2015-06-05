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

package pwnbrew.network;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import pwnbrew.ClientConfig;
import pwnbrew.log.LoggableException;
import pwnbrew.log.RemoteLog;
import pwnbrew.manager.ConnectionManager;
import pwnbrew.manager.PortManager;
import static pwnbrew.network.Message.REGISTER_MESSAGE_TYPE;
import pwnbrew.network.control.ControlMessageManager;
import pwnbrew.network.control.messages.Hello;
import pwnbrew.selector.SocketChannelHandler;
import pwnbrew.utilities.SocketUtilities;

/**
 *
 *  
 */
public class RegisterMessage extends Message {
    
       
    //Functions
    public static final byte REG = (byte)90;
    public static final byte REG_ACK = (byte)91;
       
    private final byte function;
    
    private static final String NAME_Class = RegisterMessage.class.getSimpleName();
   
     //==========================================================================
    /**
     * Constructor
     *
     * @param passedFunction
     * @param passedChannelId
    */
    @SuppressWarnings("ucd")
    public RegisterMessage( byte passedFunction, int passedChannelId ) {
        super( REGISTER_MESSAGE_TYPE );
        channelId = SocketUtilities.intToByteArray( passedChannelId );
        function = passedFunction;
    }
    
    //==========================================================================
    /**
     * Constructor
     *
     * @param passedFunction
     * @param passedId
    */
    @SuppressWarnings("ucd")
    public RegisterMessage( byte passedFunction, byte[] passedId) {
        super(REGISTER_MESSAGE_TYPE, passedId);
        function = passedFunction;
    }

    //========================================================================
    /**
     * 
     * @return 
     */
    public byte getFunction() {
        return function;
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
    public static RegisterMessage getMessage( ByteBuffer passedBuffer ) throws LoggableException {

       RegisterMessage aMessage;       
       byte[] theId = new byte[4], clientId = new byte[4], tempHostId = new byte[4];

       //Copy over the client id
       passedBuffer.get(clientId, 0, clientId.length);
       
       //Copy over the host id
       passedBuffer.get(tempHostId, 0, tempHostId.length);
       
        //Copy over the id
       passedBuffer.get(theId, 0, theId.length);

       //Copy over the id
       byte tmpFunc = passedBuffer.get();
       aMessage = new RegisterMessage(tmpFunc, theId );
       
       
       //Set client id
       int theClientId = SocketUtilities.byteArrayToInt(clientId);
       aMessage.setSrcHostId( theClientId );
       
       //Set dest host id
       int theDestHostId= SocketUtilities.byteArrayToInt(tempHostId);
       aMessage.setDestHostId( theDestHostId );
      
       return aMessage;
    }
    
       //===============================================================
    /**
    *   Performs the logic specific to the message.
    *
     * @param passedManager
    */
    @Override
    public void evaluate( PortManager passedManager ) {   
        
        if( function == RegisterMessage.REG_ACK ){
            
            ControlMessageManager aCMManager = ControlMessageManager.getControlMessageManager();
            if( aCMManager != null ){

                try {

                    ClientConfig theClientConfig = ClientConfig.getConfig();
                    PortRouter aPR = passedManager.getPortRouter( theClientConfig.getSocketPort() );
                    if( aPR != null ){
                        SocketChannelHandler aSCH = aPR.getConnectionManager().getSocketChannelHandler( getChannelId() );

                        //Set the wrapping flag
                        if( aSCH != null )
                            aSCH.setWrapping(false);

                        //Create a hello message and send it
                        if( getChannelId() == ConnectionManager.COMM_CHANNEL_ID ){
                            //Get the port router
                            String hostname = SocketUtilities.getHostname();
                            Hello helloMessage = new Hello( hostname, ConnectionManager.COMM_CHANNEL_ID );
                            aCMManager.send(helloMessage); 
                        }
                    }

                } catch ( IOException | LoggableException ex) {
                    RemoteLog.log(Level.INFO, NAME_Class, "evaluate()", ex.getMessage(), ex );
                }
            }  
        }

    }
    
}
