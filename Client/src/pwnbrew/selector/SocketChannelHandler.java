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
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import javax.net.ssl.SSLException;
import pwnbrew.ClientConfig;
import pwnbrew.log.LoggableException;
import pwnbrew.log.RemoteLog;
import pwnbrew.manager.ConnectionManager;
import pwnbrew.manager.DataManager;
import pwnbrew.network.ClientPortRouter;
import pwnbrew.network.Message;
import pwnbrew.network.PortRouter;
import pwnbrew.network.PortWrapper;
import pwnbrew.network.RegisterMessage;
import pwnbrew.network.ServerPortRouter;
import pwnbrew.network.file.FileMessageManager;
import pwnbrew.network.relay.RelayManager;
import pwnbrew.network.socket.SocketChannelWrapper;
import pwnbrew.utilities.Constants;
import pwnbrew.utilities.DebugPrinter;
import pwnbrew.utilities.SocketUtilities;

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
    private volatile boolean wrappingFlag = true;
    private volatile boolean staging = false;

    private int channelId = 0;
    private int hostId = -1;
    private int state = 0;
    
    private final Queue< ByteQueueItem > pendingByteQueueItems = new LinkedList<>();
    
    //The byte buffer for holding received bytes
    private byte currMsgType = 0;
    private ByteBuffer localMsgBuffer = null;
    
    private boolean isRegistered = false;
    
//    private int lockVal = 0;
    
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
            
        } catch ( LoggableException | CancelledKeyException | IOException ex ){
            
            //Cancel the key
            passedSelKey.cancel();
            
            DebugPrinter.printMessage(NAME_Class, "handle() Exception: " + ex.getMessage());
                                        
            //Flush any remaining packets from the queue in the handler
            shutdown();
            thePortRouter.socketClosed( hostId, channelId );
            
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

        synchronized (pendingByteQueueItems) {
            pendingByteQueueItems.clear();
        }
    }
    
    //===============================================================
    /**
    *  Queues a byte array to be sent out the specified socket channel
    *
    * @param data
     * @param cancelId
    */
    public void queueBytes( byte[] data, int cancelId ) {

        ByteQueueItem anItem = new ByteQueueItem(data, cancelId);
        SelectionRouter aSR = thePortRouter.getSelRouter();
        SocketChannel aSC = getSocketChannel();
        synchronized (pendingByteQueueItems) {
            pendingByteQueueItems.add(anItem);
            if( ( aSR.interestOps( aSC) & SelectionKey.OP_WRITE ) == 0){
                aSR.changeOps( aSC, SelectionKey.OP_WRITE | SelectionKey.OP_READ);
            }
        }
        
        
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
    
    //===============================================================
    /**
     *  Method that handles the reception of bytes from the socket channel.
     * 
     * @param sk the Selection Key
     * @throws IOException 
    */
    private void receive(SelectionKey sk) throws IOException, LoggableException {
	
        try {
            
            if ( theSCW != null && !theSCW.doHandshake(sk)) {
                return;
            }
            
        } catch (SSLException ex){
            
            RemoteLog.log(Level.WARNING, NAME_Class, "handle()", ex.getMessage(), ex);
            throw new IOException(ex);
        }       

        if( theSCW != null ){
            
            theSCW.clear();
            int bytesRead = theSCW.read();        
            if(bytesRead > 0){

                //Copy over the bytes
                ByteBuffer readByteBuf = ByteBuffer.wrap( Arrays.copyOf( theSCW.getReadBuf().array(), bytesRead ) );
    //            DebugPrinter.printMessage(this,  "Received Bytes.");

                //Check if a port wrapper has been assigned
                PortWrapper aPortWrapper = DataManager.getPortWrapper( getPort() );
                if( aPortWrapper == null || !isWrapping() ){  

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
                                    RemoteLog.log( Level.SEVERE, NAME_Class, "handleBytes()", "Encountered unrecognized data on the socket channel.", null);
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

                                    //Get dest id
                                    byte[] dstHostId = Arrays.copyOfRange(msgByteArr, 4, 8);
                                    int dstId = SocketUtilities.byteArrayToInt(dstHostId);

                                    if( currMsgType == Message.REGISTER_MESSAGE_TYPE ){

                                        RegisterMessage aMsg = RegisterMessage.getMessage( ByteBuffer.wrap( msgByteArr ));                                                
                                        int srcHostId = aMsg.getSrcHostId();
                                        int chanId = aMsg.getChannelId();

                                        //If register from pivot client
                                        if( aMsg.getFunction() == RegisterMessage.REG ){
                                            //Register the relay
                                            ServerPortRouter aSPR = (ServerPortRouter)getPortRouter();
                                            if( aSPR.registerHandler(srcHostId, chanId, this) ){

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
                                                        //Try to send back
                                                        DataManager.send(aSPR.getPortManager(), retMsg);

                                                        //Turn off wrapping if not stlth
                                                        if( !aMsg.keepWrapping() )
                                                            setWrapping( false);

                                                        if( thePR instanceof ClientPortRouter ){

                                                            //Create callback
                                                            SocketChannelCallback aSCC = new SocketChannelCallback(serverIp, theSocketPort, aMsg, aCM);

                                                            //TODO need to check if the id is taken
                                                            ClientPortRouter aCPR = (ClientPortRouter)thePR;
                                                            aCPR.ensureConnectivity(aSCC );

                                                        }
                                                    } else {

                                                        SocketChannelHandler srvHandler = aCM.getSocketChannelHandler( srcChannelId );
                                                        if( srvHandler != null ){
                                                            byte[] regBytes = aMsg.getBytes();
                                                            srvHandler.queueBytes(regBytes, 0);
                                                        }
                                                    }
                                                }

                                            }

                                        } else if( aMsg.getFunction() == RegisterMessage.REG_ACK ) {

                                            DebugPrinter.printMessage(NAME_Class, "Received register acknowledge message.");
                                            //Process it if it's meant this host or send it on
                                            if( dstId == hostId )
                                                aMsg.evaluate(thePortRouter.getPortManager());
                                            else {

                                                //Send the message then set wrapping
                                                DataManager.send( getPortRouter().getPortManager(), aMsg);
                                                RelayManager aRelayManager = RelayManager.getRelayManager();
                                                if( aRelayManager != null ){
                                                    ServerPortRouter thePR = aRelayManager.getServerPortRouter();
                                                    ConnectionManager aCM = thePR.getConnectionManager(dstId);    

                                                    //Get the socket handler
                                                    if( aCM != null ){
                                                        SocketChannelHandler theHandler = aCM.getSocketChannelHandler( chanId );
                                                        if( !aMsg.keepWrapping() && theHandler != null )
                                                            theHandler.setWrapping(false);   

                                                    }
                                                }
                                            }

                                        }

                                    } else {

                                         if( currMsgType == Message.STAGING_MESSAGE_TYPE ){

                                            //Get src id
                                            byte[] srcHostIdArr = Arrays.copyOfRange(msgByteArr, 0, 4);
                                            int srcHostId = SocketUtilities.byteArrayToInt(srcHostIdArr);

                                            //Register the relay
                                            PortRouter aPR = getPortRouter();
                                            if( aPR instanceof ServerPortRouter){                                            
                                                ServerPortRouter aSPR = (ServerPortRouter)aPR;
                                                if( !aSPR.registerHandler(srcHostId, ConnectionManager.STAGE_CHANNEL_ID, this) )
                                                    return;
                                            }

                                        }

                                        try{
                                            DataManager.routeMessage( thePortRouter, currMsgType, dstId, msgByteArr );                      
                                        } catch(Exception ex ){
                                            RemoteLog.log( Level.SEVERE, NAME_Class, "receive()", ex.toString(), ex);
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
                    aPortWrapper.processData( this, readByteBuf );
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

            synchronized (pendingByteQueueItems) {

                // Write until there's not more data ...
                if( !pendingByteQueueItems.isEmpty() ){

                    //Send while there are messages
                    ByteQueueItem nextItem = pendingByteQueueItems.poll();
                    if( nextItem != null){

                        byte[] nextArr = nextItem.byteArray;
                        try {

                            send(nextArr);

                        } catch( IOException ex ){

                            if (ex.getMessage().startsWith("Resource temporarily")) {
                                RemoteLog.log(Level.INFO, NAME_Class, "send()", ex.getMessage(), ex );
                                return;
                            }                                   

                        } catch( IllegalStateException ex1 ){
                            //Resend it
                            retrySend(nextItem);    
                        }                            
                    }
                        
                }  else {
                    
                    SocketChannel aSC = getSocketChannel();
                    if( aSC != null ){
                        thePortRouter.getSelRouter().changeOps( aSC, SelectionKey.OP_READ);
                    }
                }

                //Notify any threads waiting on this monitor
                pendingByteQueueItems.notifyAll();
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
        while(aByteBuffer.hasRemaining() && theSCW != null ) {            
            if (theSCW.write(aByteBuffer) <= 0) 
                return;
        }

        //Flush the channel
        try {
            if( theSCW != null)
                theSCW.dataFlush();
        } catch( IOException ex ){
            RemoteLog.log(Level.INFO, NAME_Class, "send()", ex.getMessage(), ex );
        }

    }
    
     //===============================================================
    /**
     * This method is responsible for sending the next control message
     *
     * @param theSelKey
     * @throws IOException
    */
    private void retrySend( ByteQueueItem passedItem ){
        
        //Handshake is not complete, sleep then add back to the queue
        final ByteQueueItem finByteQueueItem = passedItem;
        Constants.Executor.execute( new Runnable(){

            @Override
            public void run() {
                
                try {
                    Thread.sleep(250);                    
                } catch (InterruptedException ex1) {
                    ex1 = null;
                }
                
                queueBytes(finByteQueueItem.byteArray, finByteQueueItem.cancelId);
                                
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
        return state;
      
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
        hostId = passedInt;
    }

    //===============================================================
    /**
    * Gets the host id
    *
    * @return
    */
    public int getHostId() {
       return hostId;
    }
    
    //===============================================================
    /**
    * Sets the channel id
    *
     * @param passedId
    */
    public void setChannelId(int passedId) {
        channelId = passedId;
    }
    
    //===============================================================
    /**
    * Gets the channel id
    *
    * @return
    */
    public int getChannelId() {
       return channelId;
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

        FileMessageManager aFMM = FileMessageManager.getFileMessageManager();
        if( aFMM.getChannelId() == channelId )
            aFMM.setChannelId(ConnectionManager.CHANNEL_DISCONNECTED);
            
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
            
            if ( theSCW != null && !theSCW.doHandshake(theSelKey) ) {

                //Set the flag that specifies a send was attempted but failed.
                return false;
            }
            
        } catch (SSLException ex){
            
            return false;
        }
        
        return true;
    }

    //===================================================================
    /**
     *  Set the flag
     * 
     * @param passedBool 
     */
    public synchronized void setWrapping( boolean passedBool ) {
        wrappingFlag = passedBool;
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
     * 
     * @param cancelId 
     */
    public void cancelSend(Integer cancelId) {
        
        synchronized (pendingByteQueueItems) {
            Queue< ByteQueueItem > listCopy = new LinkedList(pendingByteQueueItems);
            pendingByteQueueItems.clear();
            for( ByteQueueItem aBQI : listCopy ){
                if( aBQI.cancelId == cancelId)
                    continue;
                pendingByteQueueItems.add(aBQI);
            }        
        }        
    }
    
    //=========================================================================
    /**
     * Internal class for handling byte queues
     */
    class ByteQueueItem {
        
        private final byte[] byteArray;
        private final int cancelId;

        public ByteQueueItem(byte[] byteArray, int cancelId) {
            this.byteArray = byteArray;
            this.cancelId = cancelId;
        }

    }

}/* END CLASS SocketChannelHandler */
