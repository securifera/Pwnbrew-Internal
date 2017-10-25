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
* SocketChannelHandler.java
*
* Created on June 2, 2013, 9:12:00 PM
*/

package pwnbrew.selector;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import javax.net.ssl.SSLException;
import pwnbrew.host.Host;
import pwnbrew.host.HostFactory;
import pwnbrew.log.Log;
import pwnbrew.log.LoggableException;
import pwnbrew.manager.ConnectionManager;
import pwnbrew.manager.DataManager;
import pwnbrew.manager.IncomingConnectionManager;
import pwnbrew.misc.Constants;
import pwnbrew.misc.DebugPrinter;
import pwnbrew.network.Message;
import pwnbrew.network.PortRouter;
import pwnbrew.network.PortWrapper;
import pwnbrew.network.RegisterMessage;
import pwnbrew.network.ServerPortRouter;
import pwnbrew.network.control.messages.ResetId;
import pwnbrew.network.control.messages.StageFlag;
import pwnbrew.network.http.ServerHttpWrapper;
import pwnbrew.network.socket.SocketChannelWrapper;
import pwnbrew.utilities.SocketUtilities;

/**
 *
 *  
 */
public class SocketChannelHandler implements Selectable {

    private SocketChannelWrapper theSCW = null;
    private static final String NAME_Class = SocketChannelHandler.class.getSimpleName();

    private PortRouter thePortRouter = null;
    private int rootHostId = -1;
    private int channelId = -1;
         
    private volatile boolean wrappingFlag = true;
    private volatile boolean staging = false;
    
    private final Queue<byte[]> pendingByteArrs = new LinkedList<>();
    private byte currMsgType = 0;
    private ByteBuffer localMsgBuffer = null;
    
    private boolean isRegistered = false;
    // ==========================================================================
    /**
     * Constructor
     *
     * @param passedParent
     * @throws java.io.IOException
    */
    public SocketChannelHandler( PortRouter passedParent ) throws IOException{ // NO_UCD (use default)
        thePortRouter = passedParent;

    }
   
    //===============================================================
        /**
        * Handles the operation associated with the incoming key
        *
     * @param passedSelKey
    */
    @Override
    public void handle(SelectionKey passedSelKey) {

        //Do nothing if the socket is null
        if(theSCW == null){
            passedSelKey.cancel();
            return;
        }

        // Switch on event
        try {

            if (passedSelKey.isReadable()) {
                receive(passedSelKey);
            } else if (passedSelKey.isWritable()) {
                send(passedSelKey);
            } else {
                passedSelKey.cancel();
            }
            
        } catch ( CancelledKeyException | LoggableException | IOException ex ){
            
            //Cancel the key
            passedSelKey.cancel();
            
            //Flush any remaining packets from the queue in the handler
            shutdown();
            
            //Print the message if it exists
            if( ex != null ){
                String msg = ex.getMessage();
                if( msg != null && !msg.contains("closed"))
                    DebugPrinter.printException(ex);
            }
                        
            
            thePortRouter.getPortManager().socketClosed( this );
            
        } catch ( Exception ex ){
            DebugPrinter.printException(ex);
        }

        //TODO handle the case a RuntimeException is thrown doing SSL handshake
    }

    //===============================================================
    /**
     *  Returns the socket channel managed by the AccessHandler
     * 
     * @return 
     */
    public SocketChannelWrapper getSocketChannelWrapper(){
        return theSCW;
    }
    
    //===============================================================
    /**
    *  Queues a byte array to be sent out the specified socket channel
    *
    * @param data
    */
    public void queueBytes( byte[] data) {

        SelectionRouter aSR = getPortRouter().getSelRouter();
        if( theSCW != null ){
            
            SocketChannel aSC = theSCW.getSocketChannel();
            synchronized (pendingByteArrs) {
                pendingByteArrs.add(data);
                if( ( aSR.interestOps( aSC) & SelectionKey.OP_WRITE ) == 0){
                    aSR.changeOps( aSC, SelectionKey.OP_WRITE | SelectionKey.OP_READ);
                }
            }
            
        }
        
    }
    
     //===============================================================
    /**
    *  Clears the outgoing queue
    *
    */
    public void clearQueue() {

        synchronized (pendingByteArrs) {
            pendingByteArrs.clear();
        }
    }
    
     //===============================================================
    /**
     *  Method that handles the reception of bytes from the socket channel.
     * 
     * @param sk the Selection Key
     * @throws IOException 
    */
    private void receive(SelectionKey sk) throws IOException, LoggableException {
	
        try {
            
            if ( !theSCW.doHandshake(sk)) {
                return;
            }
            
        } catch (SSLException | LoggableException ex){
            
            Log.log(Level.WARNING, NAME_Class, "receive()", ex.getMessage(), ex);
            return;
        }      

        theSCW.clear();
        int bytesRead = theSCW.read();        
        if(bytesRead > 0){
            
            //Copy over the bytes
            ByteBuffer readByteBuf = ByteBuffer.wrap( Arrays.copyOf( theSCW.getReadBuf().array(), bytesRead ) );
            
            //Check if a port wrapper has been assigned
            PortWrapper aPortWrapper = DataManager.getPortWrapper( getPort() );
            if( aPortWrapper == null || !isWrapping() ){  
                
                //Until the message length is populated
                ByteBuffer msgLenBuffer = ByteBuffer.allocate( Message.MSG_LEN_SIZE );
                while( readByteBuf.remaining() > 0){
                    
                    //See if we are already in the middle of receive
                    if( localMsgBuffer == null ){
                    
                        //Get the message type and ensure it is supported
                        if( currMsgType == 0 ){
                            currMsgType = readByteBuf.get();
                            if( !DataManager.isValidType( currMsgType ) ){
                                 
                                //Print error message
                                currMsgType = 0;
                                
                                String ipStr = "";
                                if( theSCW != null ){
                                    ipStr = theSCW.getSocketChannel().socket().getInetAddress().getHostAddress();
                                }
                                
                                StringBuilder aSB = new StringBuilder()
                                    .append("Encountered unrecognized data on the socket channel: ")
                                    .append( ipStr )
                                    .append( ":0x")
                                    .append( Integer.toHexString(currMsgType) );
                                
                                Log.log( Level.SEVERE, NAME_Class, "receive()", aSB.toString(), null);
                                break;
                                
                            }
                        }
                                                   
                        //Copy over the bytes until we get how many we need
                        while( msgLenBuffer.hasRemaining() && readByteBuf.hasRemaining() ){
                            msgLenBuffer.put( readByteBuf.get());
                        }
                        
                        //Convert to the counter
                        if( !msgLenBuffer.hasRemaining() ){
                            //Get the counter
                            byte[] msgLen = Arrays.copyOf( msgLenBuffer.array(), msgLenBuffer.capacity());
                            localMsgBuffer = ByteBuffer.allocate( SocketUtilities.byteArrayToInt(msgLen) );
                            msgLenBuffer = ByteBuffer.allocate( Message.MSG_LEN_SIZE );
                        }
                        
                        //Break out of the loop until more bytes are available
                        if( !readByteBuf.hasRemaining()){
                            break;
                        }           

                    }   
                    
                      //Add the bytes to the msg buffer
                    if( localMsgBuffer != null ){
                        //Copy over the bytes until we get how many we need
                        int remBytes = localMsgBuffer.remaining();
                        if( remBytes >= readByteBuf.remaining()){
                            
                            //Put all the bytes in there
                            localMsgBuffer.put(readByteBuf);
                        } else {  //if( remBytes < readByteBuf.remaining()){
                            //Put as many as we can
                            byte[] remBytesArr = new byte[remBytes];
                            readByteBuf.get(remBytesArr);
                            localMsgBuffer.put(remBytesArr);
                        }
                        
                        //If it's full then process it
                        if( !localMsgBuffer.hasRemaining() ){

                            //copy into byte array
                            byte [] msgByteArr = Arrays.copyOf( localMsgBuffer.array(), localMsgBuffer.position());
                            if( msgByteArr.length > 3 ){

                                //Register the handler
                                if( currMsgType == Message.REGISTER_MESSAGE_TYPE ){
                                    
                                    DebugPrinter.printMessage(NAME_Class, "Received register message.");
                                    RegisterMessage aMsg = RegisterMessage.getMessage( ByteBuffer.wrap( msgByteArr ));  
                                    int srcHostId = aMsg.getSrcHostId();
                                    int chanId = aMsg.getChannelId();

                                    if( registerId(srcHostId, chanId) ){
                                        setRegisteredFlag(true);                                           

                                        RegisterMessage retMsg = new RegisterMessage(RegisterMessage.REG_ACK, aMsg.getStlth(), srcHostId, chanId);
                                        DataManager.send( getPortRouter().getPortManager(), retMsg);
                                        
                                        //Turn off wrapping if not stlth
                                        if( !aMsg.keepWrapping() )
                                            setWrapping( srcHostId, false);
                                        
                                    } else {
                                        DebugPrinter.printMessage(NAME_Class, "Error unable to register handler.");
                                    }
                                    
                                } else {
                                    
                                    if( currMsgType == Message.STAGING_MESSAGE_TYPE ){
                                        
                                        //Get src id
                                        byte[] srcHostIdArr = Arrays.copyOfRange(msgByteArr, 0, 4);
                                        int srcHostId = SocketUtilities.byteArrayToInt(srcHostIdArr);
                                        
                                        //Register the relay
                                        int parentId = rootHostId;
                                        if( srcHostId != parentId ){
                                            ServerPortRouter aSPR = (ServerPortRouter)thePortRouter;
                                            aSPR.registerHandler(srcHostId, parentId, ConnectionManager.STAGE_CHANNEL_ID, this);        
                                        }                                      
                                    } 
                                
                                    //Get dest id
                                    byte[] dstHostId = Arrays.copyOfRange(msgByteArr, 4, 8);
                                    int dstId = SocketUtilities.byteArrayToInt(dstHostId);

                                    try{
                                        DataManager.routeMessage( thePortRouter, currMsgType, dstId, msgByteArr ); 
                                    } catch(Exception ex ){
                                        Log.log( Level.SEVERE, NAME_Class, "receive()", ex.toString(), ex);
                                    }
                                }
                            }

                            //Reset the counter
                            currMsgType = 0;
                            localMsgBuffer = null;
                        }
                        
                    }
                
                }
                
            } else {
                
                //Unwrap and process the data
                try {
                    aPortWrapper.processData( this, readByteBuf );
                } catch (BufferUnderflowException ex ){
                    Log.log( Level.SEVERE, NAME_Class, "receive()", ex.toString(), ex);                
                }
            }  

        
        } else  if(bytesRead == 0){
            
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                ex = null;
            }
            
        } else if(bytesRead == -1){
            
            //Socket has been closed on the other side
            throw new IOException("Socket closed from other end.");
        }

    }
    
    //===============================================================
    /**
     *  Register the client
     * 
     * @param passedClientId
     * @param passedChannelId
     * @return 
     */
    public boolean registerId( int passedClientId, int passedChannelId ){        
             
        //Get the comm manager to register children ids
        try {
            
            Host localHost = HostFactory.getLocalHost();
            String localhostId = localHost.getId();
            if( rootHostId == -1 && thePortRouter instanceof ServerPortRouter ){

                ServerPortRouter aSPR = (ServerPortRouter)thePortRouter;
                channelId = passedChannelId;
                IncomingConnectionManager aICM = (IncomingConnectionManager) aSPR.getConnectionManager(passedClientId);
                if( aICM != null ){
                    
                    SocketChannelHandler aHandler = aICM.getSocketChannelHandler( passedChannelId );
                    if( aHandler != null ){
                        
                        if( aHandler == this ){

                            //Set the clientId
                            rootHostId = passedClientId;
                            return true;

                        //If handler exist but is not this one
                        } else {

                            SocketChannel sockChan = null;
                            if( theSCW != null )
                                sockChan = theSCW.getSocketChannel();
                            
                            SocketChannel handlerChan = aHandler.getSocketChannelWrapper().getSocketChannel();
                            if( sockChan != null && handlerChan != null && sockChan.socket().getInetAddress().equals( 
                                    handlerChan.socket().getInetAddress())){

                                //Shutdown the previous one and remove it
                                aICM.removeHandler(passedChannelId);
                                aHandler.shutdown();
                                
                                //Register the new one
                                rootHostId = passedClientId;
                                if( !aSPR.registerHandler(passedClientId, Integer.parseInt(localhostId), passedChannelId, this) )
                                    return false;

                                return true;

                            } else {    

                                //Send message to tell client to reset their id
                                ResetId resetIdMsg = new ResetId( passedClientId );
                                ByteBuffer aByteBuffer;

                                int msgLen = resetIdMsg.getLength();
                                aByteBuffer = ByteBuffer.allocate( msgLen );
                                resetIdMsg.append(aByteBuffer);

                                //Queue to be sent
                                byte[] msgBytes = Arrays.copyOf( aByteBuffer.array(), aByteBuffer.position());

                                //If wrapping is necessary then wrap it
                                if( isWrapping() ){
                                    PortWrapper aWrapper = DataManager.getPortWrapper( getPort() );        
                                    if( aWrapper != null ){

                                        //Set the staged wrapper if necessary
                                        if( aWrapper instanceof ServerHttpWrapper ){
                                            ServerHttpWrapper aSrvWrapper = (ServerHttpWrapper)aWrapper;
                                            aSrvWrapper.setStaging( isStaged());
                                        }

                                        ByteBuffer anotherBB = aWrapper.wrapBytes( msgBytes );  
                                        msgBytes = Arrays.copyOf(anotherBB.array(), anotherBB.position());
                                    } 
                                }

                                queueBytes(msgBytes);
                                return false;
                            }
                        }
                    }
                }
                
                //Register the handler
                rootHostId = passedClientId;
                if( !aSPR.registerHandler(passedClientId, Integer.parseInt(localhostId), passedChannelId, this) )
                    return false;

            } else {

                //Register the relay
                int parentId = rootHostId;
                if( passedClientId != parentId ){
                    ServerPortRouter aSPR = (ServerPortRouter)thePortRouter;
                    aSPR.registerHandler(passedClientId, parentId, passedChannelId, this);        
                }       
//                if( passedId != rootHostId )
//                    thePortRouter.registerHandler(passedId, parentId, this); 

            }
            
        } catch(LoggableException ex){
            Log.log(Level.INFO, NAME_Class, "registerId()", ex.getMessage(), ex );
        }

        
        return true;
    }

    //===============================================================
    /**
     * This method is responsible for sending the next control message
     *
     * @param theSelKey
     * @throws IOException
    */
    private void send(SelectionKey theSelKey) throws IOException {

        //Sending handshake messages as needed
        if( canSend( theSelKey ) ){            

            synchronized (pendingByteArrs) {

                // Write until there's not more data ...
                if( !pendingByteArrs.isEmpty() ){

                    //Send message
                    byte[] nextArr = pendingByteArrs.poll();
                    if( nextArr != null){

                        try {

                            send(nextArr);

                        } catch( IOException ex ){

                            String msg = ex.getMessage();
                            if ( msg != null && msg.startsWith("Resource temporarily")) {
                                Log.log(Level.INFO, NAME_Class, "send()", ex.getMessage(), ex );
                                return;
                            }                                   

                        } catch( IllegalStateException ex1 ){
                            //Resend it
                            retrySend(nextArr);    
                        }                            
                    }
                    
                } else {
                    
                    SocketChannelWrapper aSCW = getSocketChannelWrapper();
                    if( aSCW != null ){
                        getPortRouter().getSelRouter().changeOps( aSCW.getSocketChannel(), SelectionKey.OP_READ);
                    }
                }

                //Notify any threads waiting on this monitor
                pendingByteArrs.notifyAll();
            }

        }
    }    
     
    //===============================================================
    /**
     *   Send the message out the given channel.
     *
     * @param passedArr
    */
    private void send( byte[] passedArr ) throws IOException {

        ByteBuffer aByteBuffer = ByteBuffer.wrap( passedArr );
        while(aByteBuffer.hasRemaining()) {
            if (theSCW.write(aByteBuffer) <= 0) 
                break;
        }

        //Flush the channel
        theSCW.dataFlush();

    }
    
     //===============================================================
    /**
     * This method is responsible for sending the next control message
     *
     * @param theSelKey
     * @throws IOException
    */
    private void retrySend( byte[] passedArr ){
        
        //Handshake is not complete, sleep then add back to the queue
        final byte[] byteArr = passedArr;
        Constants.Executor.execute( new Runnable(){

            @Override
            public void run() {
                
                try {
                    Thread.sleep(250);                    
                } catch (InterruptedException ex1) {
                    ex1 = null;
                }

                queueBytes(byteArr);
                
            }

        }); 
    }
     
    //===============================================================
    /**
    * Returns the Inet Address associated with the channel io for the
    * access handler
    *
    * @return
    */
    public InetAddress getInetAddress(){
        
        InetAddress theInet = null;
        if( theSCW != null){
            theInet = theSCW.getSocketChannel().socket().getInetAddress();
        }
        return theInet;
    }
    
    //===============================================================
    /**
    * Returns the Inet Address associated with the channel io for the
    * access handler
    *
    * @return
    */
    public int getType(){
        return currMsgType;
    }
    
    //===============================================================
    /**
    * Returns the channelId
    *
    * @return
    */
    public int getChannelId(){
        return channelId;
    }
    
    //===============================================================
    /**
    * Returns the Inet Address associated with the channel io for the
    * access handler
    *
    * @return
    */
    public int getPort(){
        
        int port = 0;
        if( theSCW != null){
            port = theSCW.getSocketChannel().socket().getLocalPort();
        }
        return port;
    }

    //===============================================================
    /**
     * Sets the socket channel wrapper
     *
     * @param passedSCW
    */
    public void setSocketChannelWrapper(SocketChannelWrapper passedSCW){      
        theSCW = passedSCW;
    }

    //===============================================================
    /**
    * Gets the client id
    *
    * @return
    */
    public int getRootHostId() {
       return rootHostId;
    }

    //===============================================================
    /**
    * Shutdown the handler and any of its resources
    *
    */
    public void shutdown() {

        try {

            //Shutdown the socket channel
            if(theSCW != null){
                theSCW.shutdown();
                theSCW = null;
            }
                        
        } catch (IOException ex) {
            ex = null;
        }
        
    }

    //===============================================================
    /**
     * Returns whether or not the channel is able to send messages out.
     *
    */
    private boolean canSend( SelectionKey theSelKey) throws IOException {
        
        try {
            
            if ( !theSCW.doHandshake(theSelKey) ) {

                //Set the flag that specifies a send was attempted but failed.
                return false;
            }
            
        } catch (SSLException | LoggableException ex){
            
            return false;
        }
        
        return true;
    }

    //===============================================================
    /**
     *  Returns the port router
     * 
     * @return 
     */
    public PortRouter getPortRouter() {
        return thePortRouter;
    }
    
     //===================================================================
    /**
     *  Set the flag
     * 
     * @param passedClientId
     * @param passedBool 
     */
    public synchronized void setWrapping(int passedClientId, boolean passedBool ) {
        if( rootHostId == passedClientId ){
            wrappingFlag = passedBool;
        }
    }
    
    //===================================================================
    /**
     *  Wrap the data
     * 
     * @return 
     */
    public synchronized boolean isWrapping() {
        return wrappingFlag;
    }
    
     //===================================================================
    /**
     *  Set the staging flag
     * 
     * @param passedClientId
     * @param passedBool 
     * @param theJreVersion 
     * @return  
     * @throws java.io.UnsupportedEncodingException  
     */
    public synchronized boolean setStaging( int passedClientId, boolean passedBool, String theJreVersion ) throws UnsupportedEncodingException {
        
        boolean retVal = true;
        if( rootHostId == -1 || rootHostId == passedClientId ){
            staging = passedBool;
        } else if( passedBool ){
            
            //Set stage flag if relaying the message
            StageFlag aFlag = new StageFlag( rootHostId, passedClientId, passedBool, theJreVersion );
            ByteBuffer aByteBuffer;

            int msgLen = aFlag.getLength();
            aByteBuffer = ByteBuffer.allocate( msgLen );
            aFlag.append(aByteBuffer);

            //Queue to be sent
            queueBytes(Arrays.copyOf( aByteBuffer.array(), aByteBuffer.position())); 
            retVal = false;
            
        }
        return retVal;
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

    //===================================================================
    /**
     * 
     * @return 
     */
    public boolean hasRegistered() {
        return isRegistered;
    }
    
    //===================================================================
    /**
     * 
     * @param passedBool 
     */
    public void setRegisteredFlag( boolean passedBool ) {
        isRegistered = passedBool;
    }

}
