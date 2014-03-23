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
 *  ServerHttpWrapper.java
 *
 *  Created on July 8, 2013
 */
package pwnbrew.network.http;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.logging.Level;
import pwnbrew.logging.Log;
import pwnbrew.manager.DataManager;
import pwnbrew.misc.Constants;
import pwnbrew.network.Message;
import pwnbrew.utilities.SocketUtilities;
import pwnbrew.utilities.Utilities;
import pwnbrew.selector.SocketChannelHandler;

/**
 *
 *  
 */
public class ServerHttpWrapper extends HttpWrapper {

    private static final String NAME_Class = ServerHttpWrapper.class.getSimpleName();
    private static final int STEALTH_COOKIE_B64 = 5;   
    
    //Random number gen for age
    private final SecureRandom aSR = new SecureRandom();   
    private volatile boolean staging = false;
       
    //==========================================================================
    /**
     * Constructor
     *
    */
    public ServerHttpWrapper(){
    
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
            String langField = currentHeader.getOption( Http.ACCEPT_LANGUAGE );
            if( langField != null ){

                //Split on the semi-colon separater
                String[] langArr = langField.split(";");
                if( langArr.length > 1){
                    
                    //Get the q field
                    String[] qArr = langArr[1].split("\\.");
                    if( qArr.length > 1){
                        
                        //Try an parse the field
                        try {
                            
                            int stealthNum = Integer.parseInt( qArr[1] );
                            byte[] ctrlMsg = getMessageBytes( stealthNum);
                                                       
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
                                        
                                        //Get the id If the client is already registered then return
                                        if( msgBytes.length > Message.MSG_LEN_SIZE + 1 ){
                                            
                                            byte [] tempIdArr = Arrays.copyOf( msgBytes, 4);
                                            int tempId = SocketUtilities.byteArrayToInt(tempIdArr);
                                            if( !passedHandler.registerId(tempId)){
                                                return;
                                            }
                                            
                                            //Get dest id
                                            byte[] dstHostId = Arrays.copyOfRange(msgBytes, 4, 8);
                                            int dstId = SocketUtilities.byteArrayToInt(dstHostId);
                                            
                                            try{
                                                DataManager.routeMessage( passedHandler.getPortRouter().getCommManager(), type, dstId, msgBytes );
                                            } catch(Exception ex ){
                                                Log.log( Level.SEVERE, NAME_Class, "processHeader()", ex.toString(), ex);
                                            }
                                            return;
                                        }
                                        
                                    } else {
                                        
                                        //Try to process the previous version's protocol
                                        byte[] prevLenArr = Arrays.copyOf(msgLenArr, 2);
                                        msgLen = SocketUtilities.byteArrayToInt(prevLenArr);
                                        if( msgLen == msgBB.remaining() + 2){
                                        
                                            byte[] msgBytes = new byte[msgLen];
                                            System.arraycopy(msgLenArr, 2, msgBytes, 0, 2);
                                            msgBB.get(msgBytes, 2, msgLen - 2);

                                            //Get the id If the client is already registered then return
                                            if( msgBytes.length > 3 ){

                                                byte[] tempIdArr = Arrays.copyOf( msgBytes, 4);
                                                int tempId = SocketUtilities.byteArrayToInt(tempIdArr);
                                                if( !passedHandler.registerId(tempId)){
                                                    return;
                                                }
                                                
                                                //Get dest id
                                                byte[] dstHostId = Arrays.copyOfRange(msgBytes, 4, 8);
                                                int dstId = SocketUtilities.byteArrayToInt(dstHostId);
                                                
                                                //Set the marker so the handler will know to send a different stage
                                                System.arraycopy(Constants.OLD_STAGER_MARKER, 0, msgBytes, 8, Constants.OLD_STAGER_MARKER.length);

                                                try{
                                                    DataManager.routeMessage( passedHandler.getPortRouter().getCommManager(), type, dstId, msgBytes );
                                                } catch(Exception ex ){
                                                    Log.log( Level.SEVERE, NAME_Class, "processHeader()", ex.toString(), ex);
                                                }
                                                return;
                                            }
                                        
                                            
                                        } else {
                                            Log.log( Level.WARNING, NAME_Class, "processHeader()", "Message size doesn't match remaing size.", null);
                                        }
                                    }
                                }
                            }
                            
                        } catch ( NumberFormatException ex){
                            
                            //Do nothing because it doesn't fit the criteria
                            ex = null;
                        } catch (IOException ex) {
                            
                            //Do nothing because it doesn't fit the criteria
                            ex = null;
                        }
                    }
                }
            }  
            
            //Send redirect
            ByteBuffer reply = wrapBytes( new byte[0]);
            passedHandler.queueBytes( Arrays.copyOf(reply.array(), reply.position()));
            
        }
    }
    
      //===============================================================
    /**
     *  Returns a ByteBuffer with the necessary bytes in it.
     * 
     * @param msgBytes
     * @return 
    */
    @Override
    public ByteBuffer wrapBytes( byte[] msgBytes ) {
         
         //Allocate and add the bytes from the message
        if( staging )
            return wrapStager( msgBytes );
        else {
            
            Http aHttpMsg = Http.getGeneric( Http._302 );

            //XOR the bytes
            if( msgBytes.length > 0 ){     

                //Have to add 1 because this call is between 0 and n which means the check on the other
                //side will fail if the number returns 0
                int exp = aSR.nextInt( 9 ) + 1;
                int ageVal = (int) Math.pow(STEALTH_COOKIE_B64, exp);

                //XOR the bytes
                byte[] encodedBytes = Utilities.xorData(msgBytes, XOR_STRING.getBytes());
                //Get a hex string representation
                String encodedByteStr = SocketUtilities.toString(encodedBytes).replace(" ", "");
                StringBuilder aSB = new StringBuilder()
                        .append(COOKIE_REF).append("=").append( encodedByteStr ); 

                aHttpMsg.setOption(Http.SET_COOKIE, aSB.toString());
                aHttpMsg.setOption(Http.AGE, Integer.toString(ageVal));

            }

            //Allocate enough space
            int httpLen = aHttpMsg.getLength();
            ByteBuffer aByteBuffer = ByteBuffer.allocate( httpLen );

            //Add the bytes
            aHttpMsg.append(aByteBuffer);
            return aByteBuffer;
        }
      
    }
    
      
    //======================================================================
    /**
     * 
     * @param classBytes
     * @return 
     */
    private ByteBuffer wrapStager( byte[] classBytes ){
       
         //Allocate and add the bytes from the message
        Http aHttpMsg = Http.getGeneric( Http._200 );
        
        //Set the bytes
        if( classBytes.length > 0 ){         
             
            aHttpMsg.setPayloadBytes( classBytes );                    
            aHttpMsg.setOption( Http.CONTENT_LENGTH, Integer.toString(classBytes.length) );
         
        }
        
        //Allocate enough space
        int httpLen = aHttpMsg.getLength();
        ByteBuffer aByteBuffer = ByteBuffer.allocate( httpLen );
        
        //Add the bytes
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
        switch( stealthNum ){
            case STEALTH_COOKIE_B64:
                //Get the cookie
                String cookieVal = currentHeader.getOption( Http.COOKIE );
                if( cookieVal != null && cookieVal.startsWith(COOKIE_REF) ){
                    String[] cookieArr = cookieVal.split("=");
                    if( cookieArr.length > 1 ){
                        //Convert the hexstring to bytes
                        String hexString = cookieArr[1];                        
                        byte[] xorBytes = SocketUtilities.hexStringToByteArray(hexString);
                        
                        //XOR the bytes
                        msgBytes = Utilities.xorData(xorBytes, XOR_STRING.getBytes());
          
                    }
                }
                break;
            default:
                break;
        }  
           
        return msgBytes;
    }   
    
    //===================================================================
    /**
     *  Set the staging flag
     * 
     * @param passedBool 
     */
    public synchronized void setStaging( boolean passedBool ) {
        staging = passedBool;
    }
    
    //===================================================================
    /**
     *  Check if the handler is managing a staged connection
     * 
     * @return 
     */
    public synchronized boolean isStaged() {
        return staging;
    }
    
    
}
