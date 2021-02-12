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


package pwnbrew.network.socket;

import pwnbrew.selector.SocketChannelHandler;
import pwnbrew.log.Log;
import pwnbrew.log.LoggableException;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import static javax.net.ssl.SSLEngineResult.HandshakeStatus.NEED_TASK;
import javax.net.ssl.SSLEngineResult.Status;
import javax.net.ssl.SSLException;
import pwnbrew.misc.DebugPrinter;
import pwnbrew.utilities.SSLUtilities;
import pwnbrew.utilities.SocketUtilities;


public class EncryptedSocketChannelWrapper extends SocketChannelWrapper {

    private final SSLEngine theSSLEngine;
    private static final String NAME_Class = EncryptedSocketChannelWrapper.class.getSimpleName();

    private final int applicationByteBufferSize;
    private final int networkByteBufferSize;
    
    private final ByteBuffer incomingByteBuffer;
    private final ByteBuffer outgoingByteBuffer;
    private static final ByteBuffer handshakeByteBuffer = ByteBuffer.allocate(0);

    private volatile boolean handshakeComplete;
    private boolean handShakeMsg = true;
    private boolean shutdown = false;

    //===============================================================
    /**
     * Constructor
     * 
     * @param sc
     * @param passedHandler
     * @param requireAuth
     * @throws LoggableException
     * @throws IOException
     * @throws InterruptedException 
     */
    public EncryptedSocketChannelWrapper ( SocketChannel sc, SocketChannelHandler passedHandler, boolean requireAuth ) throws LoggableException, IOException, InterruptedException {
        super(sc, passedHandler );

        //Get the type and the SSL context
        SSLContext theContext = SSLUtilities.getSSLContext();

        theSSLEngine = theContext.createSSLEngine(SocketUtilities.getHostname(), sc.socket().getPort());
        theSSLEngine.setUseClientMode(false);  
        
        //Set authentication is it is set
        theSSLEngine.setNeedClientAuth(requireAuth);        
     
        networkByteBufferSize = theSSLEngine.getSession().getPacketBufferSize();
        incomingByteBuffer  = ByteBuffer.allocate(networkByteBufferSize);
        outgoingByteBuffer = ByteBuffer.allocate(networkByteBufferSize);
        outgoingByteBuffer.position(0);
        outgoingByteBuffer.limit(0);
        
        //Set the size
        applicationByteBufferSize = theSSLEngine.getSession().getApplicationBufferSize();
        theRequestByteBuffer = ByteBuffer.allocate( applicationByteBufferSize );
    }

    //==========================================================================
    /**
     * Returns the buffer size
     * @return 
     */
    public int getBufferSize(){
        return applicationByteBufferSize;
    }

    //==========================================================================
    /**
     *  Calls up to the superclass to adjust the buffer size
     *   by an appropriate increment.
     */
    private void resizeRequestByteBuffer() {
        resizeRequestBB(applicationByteBufferSize);
    }

    //==========================================================================
    /**
    * Writes bb to the SocketChannel.
    *
    * Returns true when the ByteBuffer has no remaining data.
    */
    private boolean tryFlush(ByteBuffer bb) throws IOException {
        super.write(bb);
        return !bb.hasRemaining();
    }

    //==========================================================================
    /**
    * If any data remains in the output buffer then send it
    */
    private boolean flushHandshakeBuffer(SelectionKey passedKey) throws IOException {

        HandshakeStatus currStatus = theSSLEngine.getHandshakeStatus();
        boolean setRead = false;

        //Send data
        if (!tryFlush(outgoingByteBuffer)) {
            return false;
        }

        // See if we need to switch from write to read mode.
        switch (currStatus) {

            //If finished
            case FINISHED:
                handshakeComplete = true;
//                theParentHandler.setState(Constants.CONNECTED);
                theParentHandler.getPortRouter().getSelRouter().changeOps(theSocketChannel, SelectionKey.OP_WRITE | SelectionKey.OP_READ);
        
                setRead = true;
                break;

            case NEED_UNWRAP:
                setRead = true;
                break;
            default:
                break;
        }

        //Set the selector to read if appropriate
        if(setRead && passedKey != null){
            theParentHandler.getPortRouter().getSelRouter().changeOps(theSocketChannel, SelectionKey.OP_READ);
        }

        return handshakeComplete;
    }

    /*
    * Perform any handshaking processing.
    * <P>
    * If a SelectionKey is passed, register for selectable
    * operations.
    * <P>
    * In the blocking case, our caller will keep calling us until
    * we finish the handshake.  Our reads/writes will block as expected.
    * <P>
    * In the non-blocking case, we just received the selection notification
    * that this channel is ready for whatever the operation is, so give
    * it a try.
    * <P>
    * return:
    *		true when handshake is done.
    *		false while handshake is in progress
    */
    @Override
    public boolean doHandshake(SelectionKey passedKey) throws IOException, LoggableException {

        HandshakeStatus currStatus;

        if ( handshakeComplete ){
            return true;
        }

        /*
        * Flush out the outgoing buffer, if there's anything left in
        * it.
        */
        if (outgoingByteBuffer.hasRemaining()) {
            return flushHandshakeBuffer(passedKey);
        }

        //Get the current handshake status
        currStatus = theSSLEngine.getHandshakeStatus();

        //If no data to send, switch on status
        switch (currStatus) {

            case NEED_UNWRAP:

                //If socket is closed
                try {

                    //Read the next bytes
                    int readCount;
                    synchronized(incomingByteBuffer){
                        readCount = theSocketChannel.read(incomingByteBuffer);
                    }

                    if (readCount == -1) {
                        theSSLEngine.closeInbound();
                        return handshakeComplete;
                    }

                } catch (BufferOverflowException ex){
                    Log.log(Level.WARNING, NAME_Class, "doHandshake()", ex.getMessage(), ex);
                }

                doUnwrap(passedKey);
                break;

            case NEED_WRAP:

                doWrap(passedKey);
                break;
            
            case NEED_TASK:
                
                doTasks( passedKey );
                break;

            default: // NOT_HANDSHAKING/NEED_TASK/FINISHED
                //TODO Come back and code handling for this case
                flushHandshakeBuffer(passedKey);
                passedKey.cancel();
                throw new SSLException("Invalid Handshaking State" + currStatus);
        } // switch

        return handshakeComplete;
    }

    //==========================================================================
    /**
    /*
    * Do all the outstanding handshake tasks in a separate thread.
    */
    private void doTasks( final SelectionKey passedKey) {

        
        //Need to check for "No trusted certificate found" - ValidatorException
        Runnable taskRunnable;
        while((taskRunnable = theSSLEngine.getDelegatedTask()) != null) {
            try {
                taskRunnable.run();
            } catch( RuntimeException ex ){
                ex = null;
            }
        }

        //Notify listener
        taskFinished(passedKey);

    }

    /*
    * Read the channel for more information, then unwrap the
    * (hopefully application) data we get.
    * <P>
    * If we run out of data, we'll return to our caller (possibly using
    * a Selector) to get notification that more is available.
    * <P>
    * Each call to this method will perform at most one underlying read().
    */
    @Override
    public int read() throws IOException {

        if (!handshakeComplete) throw new IllegalStateException();

        final int pos = theRequestByteBuffer.position();

        if (theSocketChannel.read(incomingByteBuffer) == -1) {
            theSSLEngine.closeInbound();  // probably throws exception
            return -1;
        }

        SSLEngineResult result;
        do {
            resizeRequestByteBuffer();    // guarantees enough room for unwrap
            incomingByteBuffer.flip();
            result = theSSLEngine.unwrap(incomingByteBuffer, theRequestByteBuffer);
            incomingByteBuffer.compact();

            /*
            * Could check here for a renegotation, but we're only
            * doing a simple read/write, and won't have enough state
            * transitions to do a complete handshake, so ignore that
            * possibility.
            */
            switch (result.getStatus()) {

                case BUFFER_UNDERFLOW:
                case OK:
                    if (result.getHandshakeStatus() == HandshakeStatus.NEED_TASK) {
                        doTasks( null );
                    }
                case CLOSED:
                    if (result.getHandshakeStatus() == HandshakeStatus.FINISHED) {
//                        DebugPrinter.printMessage(NAME_Class, "Finished");
                    }
                    break;

                default:
                    throw new SSLException("sslEngine error during data read: " + result.getStatus());
            }
        } while ((incomingByteBuffer.position() != 0) && result.getStatus() != Status.BUFFER_UNDERFLOW);

        return (theRequestByteBuffer.position() - pos);
    }

    /*
    * Try to write out as much as possible from the src buffer.
    */
    @Override
    public int write(ByteBuffer src) throws IOException {

        if (!handshakeComplete) {

            //Check if this is the handshake message
            if(handShakeMsg){
                handShakeMsg = false;
                return super.write(src);
            }

            //TODO Possible change to wait or queue
            throw new IllegalStateException();
        }

        return doWrite(src);
    }

    /*
    * Try to flush out any existing outbound data, then try to wrap
    * anything new contained in the src buffer.
    * <P>
    * Return the number of bytes actually consumed from the buffer,
    * but the data may actually be still sitting in the output buffer,
    * waiting to be flushed.
    */
    private int doWrite(ByteBuffer src) throws IOException {
        int retValue = 0;

        if (outgoingByteBuffer.hasRemaining() && !tryFlush(outgoingByteBuffer)) {
            return retValue;
        }

        /*
        * The data buffer is empty, we can reuse the entire buffer.
        */
        outgoingByteBuffer.clear();

        final SSLEngineResult result = theSSLEngine.wrap(src, outgoingByteBuffer);
        retValue = result.bytesConsumed();

        outgoingByteBuffer.flip();

        switch (result.getStatus()) {

        case OK:
            if (result.getHandshakeStatus() == HandshakeStatus.NEED_TASK) {
                doTasks( null );
            }
            break;

        default:
            throw new IOException("sslEngine error during data write: " +
                                result.getStatus());
        }

        /*
        * Try to flush the data, regardless of whether or not
        * it's been selected.  Odds of a write buffer being full
        * is less than a read buffer being empty.
        */
        while (outgoingByteBuffer.hasRemaining()) {
            tryFlush(outgoingByteBuffer);
            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {}
        }

        return retValue;
    }

    /*
    * Flush any remaining data.
    * <P>
    * Return true when the fileChannelBB and outNetBB are empty.
    */
    @Override
    public boolean dataFlush() throws IOException {
        boolean fileFlushed = true;

        if (outgoingByteBuffer.hasRemaining()) {
            tryFlush(outgoingByteBuffer);
        }

        theSocketChannel.socket().getOutputStream().flush();
        return (fileFlushed && !outgoingByteBuffer.hasRemaining());
    }

    /*
    * Begin the shutdown process.
    * <P>
    * Close out the SSLEngine if not already done so, then
    * wrap our outgoing close_notify message and try to send it on.
    * <P>
    * Return true when we're done passing the shutdown messsages.
    */
    @Override
    public boolean shutdown() throws IOException {

        //DebugPrinter.printMessage(NAME_Class, "Shutdown socket");
        final SSLEngineResult result;
        try {
            
            if (!shutdown) {
                theSSLEngine.closeOutbound();
                shutdown = true;
            }

            if (outgoingByteBuffer.hasRemaining() && tryFlush(outgoingByteBuffer)) {
                return false;
            }

            /*
            * By RFC 2616, we can "fire and forget" our close_notify
            * message, so that's what we'll do here.
            */
            outgoingByteBuffer.clear();
            result = theSSLEngine.wrap(handshakeByteBuffer, outgoingByteBuffer);
            if (result.getStatus() != Status.CLOSED) {
                throw new SSLException("Improper close state");
            }
            outgoingByteBuffer.flip();

            /*
            * We won't wait for a select here, but if this doesn't work,
            * we'll cycle back through on the next select.
            */
            if (outgoingByteBuffer.hasRemaining()) {
                tryFlush(outgoingByteBuffer);
            }
            
        } finally {
            close();
        }
        
        return (!outgoingByteBuffer.hasRemaining() && (result.getHandshakeStatus() != HandshakeStatus.NEED_WRAP));
    }


    /*
    * Begins the handshake process
    */
    public void beginHandshake() throws SSLException {
        theSSLEngine.beginHandshake();
    }

    /*
    * Called when the SSL engine needs to wrap data
    *
    */
    private void doWrap(SelectionKey passedKey) throws IOException {

        SSLEngineResult result;
        //DebugPrinter.printMessage( this.getClass().getSimpleName(), "Wrapping.");

        // The flush above guarantees the out buffer to be empty
        outgoingByteBuffer.clear();
        result = theSSLEngine.wrap(handshakeByteBuffer, outgoingByteBuffer);
        outgoingByteBuffer.flip();

        HandshakeStatus currStatus = result.getHandshakeStatus();
        if(currStatus == HandshakeStatus.FINISHED){

            handshakeComplete = true;
//            DebugPrinter.printMessage( this.getClass().getSimpleName(), "Finished handshaking.");

            //Flush the buffer so that the client will get a finished flag too
            if (outgoingByteBuffer.hasRemaining()){
                tryFlush(outgoingByteBuffer);
            }

            //Set the state and change to read
            theParentHandler.getPortRouter().getSelRouter().changeOps(theSocketChannel, SelectionKey.OP_WRITE | SelectionKey.OP_READ);
            
            return;
        }

        //Switch on ssl engine wrap
        switch (result.getStatus()) {
                case OK:

                if (currStatus == HandshakeStatus.NEED_TASK) {
                    doTasks( passedKey );
                }

                if (passedKey != null) {
                    theParentHandler.getPortRouter().getSelRouter().changeOps(theSocketChannel, SelectionKey.OP_WRITE | SelectionKey.OP_READ);
                }

                break;

                default: // BUFFER_OVERFLOW/BUFFER_UNDERFLOW/CLOSED:
                    throw new SSLException("Received" + result.getStatus() +
                                        "during initial handshaking");
        }

    }

    //==========================================================================
    /**
    /*
    * Called when the SSL engine needs to unwrap data
    *
    */
    private void doUnwrap(SelectionKey passedKey) throws IOException, LoggableException {

        SSLEngineResult result;
        //DebugPrinter.printMessage( this.getClass().getSimpleName(), "Unwrapping.");

        // Don't need to resize requestBB, since no app data should
        try {
            synchronized(incomingByteBuffer){
                incomingByteBuffer.flip();
                result = theSSLEngine.unwrap(incomingByteBuffer, theRequestByteBuffer);
                incomingByteBuffer.compact();
            } 
            
        } catch(RuntimeException ex ){
            throw new LoggableException(ex);
        }

        HandshakeStatus currStatus = result.getHandshakeStatus();

        //Switch on SSL engine unwrap
        switch (result.getStatus()) {

            case OK:

                //Switch on current status
                switch (currStatus) {
                    case NOT_HANDSHAKING:
                        throw new IOException("Not handshaking during initial handshake");

                    case NEED_TASK:
                        doTasks( passedKey );
                        break;

                    case FINISHED:
                        handshakeComplete = true;
//                        DebugPrinter.printMessage( this.getClass().getSimpleName(), "Finished handshaking.");

                        //Set the state and change to read
                        theParentHandler.getPortRouter().getSelRouter().changeOps(theSocketChannel, SelectionKey.OP_WRITE | SelectionKey.OP_READ);
                                   
                        break;

                    case NEED_UNWRAP:
                        doUnwrap(passedKey);
                        break;

                    case NEED_WRAP:
                        doWrap(passedKey);
                        break;

                }

                break;

            case BUFFER_UNDERFLOW:
                /*
                * Need to go reread the Channel for more data.
                */
                if (passedKey != null) {
                    theParentHandler.getPortRouter().getSelRouter().changeOps(theSocketChannel, SelectionKey.OP_READ);
                }
                break;

            default: // BUFFER_OVERFLOW/CLOSED:
                throw new SSLException("Received" + result.getStatus() +
                                            "during initial handshaking");
        }
    }

    //===============================================================
    /**
    /*
    * Called when the SSL tasks have completed
    * 
     * @param passedKey
    */
    public void taskFinished(SelectionKey passedKey) {

        HandshakeStatus currStatus = theSSLEngine.getHandshakeStatus();

        try {

            switch (currStatus) {
                case NEED_UNWRAP:

                    // Don't need to resize requestBB, since no app data should
                    if(!outgoingByteBuffer.hasRemaining()){
                        doUnwrap(passedKey);
                    }
                    break;

                case NEED_WRAP:
                    if(!outgoingByteBuffer.hasRemaining())  {

                        // Wrap up the next data and change to read
                        doWrap(passedKey);
                    }
                    break;
                    
                case FINISHED:
                    handshakeComplete = true;
                    DebugPrinter.printMessage( this.getClass().getSimpleName(), "Finished handshaking.");

                    //Set the state and change to read
                    theParentHandler.getPortRouter().getSelRouter().changeOps(theSocketChannel, SelectionKey.OP_READ | SelectionKey.OP_WRITE );
                                               
                    break;
                    
                default:
    //                   DebugPrinter.printMessage(SecureSocketChannelWrapper.class, "Not handshaking.");

            }

        } catch (IOException ex){
            Log.log(Level.SEVERE, NAME_Class, "taskFinished()", ex.getMessage(), ex );
        } catch (LoggableException ex) {
            DebugPrinter.printException(ex.getException());
        }
    }

}
