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
*/

package pwnbrew.selector;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLException;
import pwnbrew.log.LoggableException;
import pwnbrew.manager.DataManager;
import pwnbrew.misc.Constants;
import pwnbrew.misc.DebugPrinter;
import pwnbrew.utilities.SocketUtilities;
import pwnbrew.network.Message;
import pwnbrew.network.PortRouter;
import pwnbrew.network.RegisterMessage;
import pwnbrew.network.socket.SocketChannelWrapper;

/**
 *
 *  
 */
public class SocketChannelHandler implements Selectable {

    public static final int SERVER_TYPE = 124;
    public static final int CLIENT_TYPE = 125;
    
    //The type of handler, used for getting the right port
    private final int theHandlerType;
    
    private SocketChannelWrapper theSCW = null;
    private static final String NAME_Class = SocketChannelHandler.class.getSimpleName();

    private PortRouter thePortRouter = null;
    private volatile boolean staging = false;

    private int clientId = -1;
    private int state = 0;
    
    private final Queue< byte[] > pendingByteArrs = new LinkedList<>();
    
    //The byte buffer for holding received bytes
    private byte currMsgType = 0;
    private ByteBuffer localMsgBuffer = null;
    
    // ==========================================================================
    /**
     * Constructor
     *
     * @param passedParent
     * @param passedType
    */
    public SocketChannelHandler( PortRouter passedParent, int passedType ){ // NO_UCD (use default)
        thePortRouter = passedParent;
        theHandlerType = passedType;
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
        } catch ( CancelledKeyException | IOException | LoggableException ex ){
            
            //Cancel the key
            passedSelKey.cancel();
            
            //Flush any remaining packets from the queue in the handler
            shutdown();
            thePortRouter.socketClosed( this );
            
        } 

        //TODO handle the case a RuntimeException is thrown doing SSL handshake
    }

    //===============================================================
    /**
     *  Returns the socket channel managed by the AccessHandler
     * 
     * @return 
     */
    public SocketChannel getSocketChannel(){
        SocketChannel theChannel = null;
        if( theSCW != null ){
            theChannel = theSCW.getSocketChannel();
        }
        return theChannel;
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
    *  Queues a byte array to be sent out the specified socket channel
    *
    * @param data
    */
    public void queueBytes( byte[] data) {

        SelectionRouter aSR = thePortRouter.getSelRouter();
        SocketChannel aSC = getSocketChannel();
        synchronized (pendingByteArrs) {
            pendingByteArrs.add(data);
            if( ( aSR.interestOps( aSC) & SelectionKey.OP_WRITE ) == 0){
                aSR.changeOps( aSC, SelectionKey.OP_WRITE | SelectionKey.OP_READ);
            }
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
            
        } catch (SSLException ex){
            
            DebugPrinter.printMessage( NAME_Class, "receive", ex.getMessage(), ex);
            throw new IOException(ex);
        }       

        theSCW.clear();
        int bytesRead = theSCW.read();        
        if(bytesRead > 0){
            
            //Copy over the bytes
            byte[] byteArr = Arrays.copyOf( theSCW.getReadBuf().array(), bytesRead );
            ByteBuffer readByteBuf = ByteBuffer.wrap( byteArr );
                
            //Until the message length is populated
            ByteBuffer msgLenBuffer = ByteBuffer.allocate( Message.MSG_LEN_SIZE );
            while( readByteBuf.hasRemaining() ){

                //See if we are already in the middle of receive
                if( localMsgBuffer == null ){

                    //Get the message type and ensure it is supported
                    if( currMsgType == 0 ){
                        currMsgType = readByteBuf.get();
                        if( !DataManager.isValidType( currMsgType ) ){

                            //Print error message
                            currMsgType = 0;
                            DebugPrinter.printMessage( NAME_Class, "handleBytes()", "Encountered unrecognized data on the socket channel.", null);
                            return;

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
                        msgLen = null;
                    }

                    //Break out of the loop until more bytes are available
                    if( !readByteBuf.hasRemaining()){
                        return;
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

                            //Get the src id
                            byte[] clientIdArr = Arrays.copyOf(msgByteArr, 4);
                            int srcId = SocketUtilities.byteArrayToInt(clientIdArr);

                            //Get dest id
                            byte[] dstHostId = Arrays.copyOfRange(msgByteArr, 4, 8);
                            int dstId = SocketUtilities.byteArrayToInt(dstHostId);

                            if( currMsgType == Message.REGISTER_MESSAGE_TYPE ){

                                RegisterMessage aMsg = RegisterMessage.getMessage( ByteBuffer.wrap( msgByteArr ));                                                
                                aMsg.evaluate(thePortRouter.getPortManager());

                            } else {                                

                                try{
                                    DataManager.routeMessage( thePortRouter.getPortManager(), currMsgType, dstId, msgByteArr );                      
                                } catch(Exception ex ){
                                    DebugPrinter.printMessage( NAME_Class, "receive", ex.getMessage(), ex);
                                }
                            }
                        }

                        //Reset the counter
                        currMsgType = 0;
                        localMsgBuffer = null;
                    }

                }

            }
            byteArr = null;
            readByteBuf.clear();

        
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

                    //Send while there are messages
                        byte[] nextArr = pendingByteArrs.poll();
                        if( nextArr != null){

                            try {

                                send(nextArr);

                            } catch( IOException ex ){

                                if (ex.getMessage().startsWith("Resource temporarily")) {
                                    DebugPrinter.printMessage( NAME_Class, "send", ex.getMessage(), ex);
                                    return;
                                }                                   

                            } catch( IllegalStateException ex1 ){
                                //Resend it
                                retrySend(nextArr);    
                            }                            
                        }
                        
                }  else {
                    
                    SocketChannel aSC = getSocketChannel();
                    if( aSC != null ){
                        thePortRouter.getSelRouter().changeOps( aSC, SelectionKey.OP_READ);
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
                return;
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

                //Get the dest id
                byte[] dstHostId = Arrays.copyOfRange(byteArr, Message.DEST_HOST_ID_OFFSET, Message.DEST_HOST_ID_OFFSET + 4);
                int tempId = SocketUtilities.byteArrayToInt(dstHostId);
                thePortRouter.queueSend( byteArr, tempId );
                
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
    public int getPort(){
        
        int port = 0;
        if( theSCW != null){
            if( theHandlerType == SocketChannelHandler.CLIENT_TYPE )
                port = theSCW.getSocketChannel().socket().getPort();
            else if(theHandlerType == SocketChannelHandler.SERVER_TYPE  )
                port = theSCW.getSocketChannel().socket().getLocalPort();
        }
        return port;
    }

    //===============================================================
    /**
    * Returns the state of the channel
    *
    * @return
    */
    public synchronized int getState(){

        //Get the current state
        return Integer.valueOf(state);
      
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
    * Sets the client id
    *
     * @param passedInt
    */
    public void setClientId(int passedInt) {
        clientId = passedInt;
    }

    //===============================================================
    /**
    * Gets the client id
    *
    * @return
    */
    public int getClientId() {
       return clientId;
    }

    //===============================================================
    /**
    * Sets the state of the underlying
    *
     * @param passedState
    */
    public synchronized void setState(int passedState) {
        state = passedState;
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
        } finally {
            state = Constants.DISCONNECTED;
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
            
        } catch (SSLException ex){
            
            return false;
        }
        
        return true;
    }
//
//    //===================================================================
//    /**
//     *  Set the flag
//     * 
//     * @param passedBool 
//     */
//    public synchronized void setWrapping( boolean passedBool ) {
//        wrappingFlag = passedBool;
//    }
//    
//    //===================================================================
//    /**
//     *  Wrap the data
//     * 
//     * @return 
//     */
//    public synchronized boolean isWrapping() {
//        return wrappingFlag;
//    }

}/* END CLASS AccessHandler */
