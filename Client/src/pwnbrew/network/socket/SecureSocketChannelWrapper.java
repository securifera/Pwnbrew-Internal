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
import pwnbrew.log.RemoteLog;
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
import javax.net.ssl.SSLEngineResult.Status;
import javax.net.ssl.SSLException;
import pwnbrew.misc.Constants;
import pwnbrew.misc.DebugPrinter;

/**
 * A helper class which performs I/O using the SSLEngine API.
 *
 */
public class SecureSocketChannelWrapper extends SocketChannelWrapper {

    private final SSLEngine sslEngine;
    private static final String NAME_Class = SecureSocketChannelWrapper.class.getSimpleName();
//    private static final int NUMBER_OF_SENDS = 15;

    private int appBBSize;
    private int netBBSize;

    /*
    * All I/O goes through these buffers.
    * <P>
    * It might be nice to use a cache of ByteBuffers so we're
    * not alloc/dealloc'ing ByteBuffer's for each new SSLEngine.
    * <P>
    * We use our superclass' requestBB for our application input buffer.
    * Outbound application data is supplied to us by our callers.
    */
    private final ByteBuffer inNetBB;
    private final ByteBuffer outNetBB;

    /*
    * An empty ByteBuffer for use when one isn't available, say
    * as a source buffer during initial handshake wraps or for flush
    * operations.
    */
    private static final ByteBuffer handshakeBB = ByteBuffer.allocate(0);

    //Accessible from several threads
    private volatile boolean initialHSComplete;
    private boolean handShakeMsg = true;
    
     /*
    * We have received the shutdown request by our caller, and have
    * closed our outbound side.
    */
    private boolean shutdown = false;

    //===============================================================
    /**
     * Constructor
     * 
     * @param sc
     * @param passedHandler
     * @param isClient
     * @throws LoggableException
     * @throws IOException
     * @throws InterruptedException 
     */
    public SecureSocketChannelWrapper ( SocketChannel sc, SocketChannelHandler passedHandler, boolean isClient ) throws LoggableException, IOException, InterruptedException {
        super(sc, passedHandler );

        //Get the type and the SSL context
        SSLContext theContext = passedHandler.getPortRouter().getSSLContext( isClient );

        sslEngine = theContext.createSSLEngine();
        sslEngine.setUseClientMode(isClient);

        netBBSize = sslEngine.getSession().getPacketBufferSize();
        inNetBB  = ByteBuffer.allocate(netBBSize);
        outNetBB = ByteBuffer.allocate(netBBSize);
        outNetBB.position(0);
        outNetBB.limit(0);
        
        //Set the size
        appBBSize = sslEngine.getSession().getApplicationBufferSize();
        requestBB = ByteBuffer.allocate( appBBSize );
    }

    /*
    * Returns the buffer size
    */
    public int getBufferSize(){
        return appBBSize;
    }

    /*
    * Calls up to the superclass to adjust the buffer size
    * by an appropriate increment.
    */
    private void resizeRequestBB() {
        resizeRequestBB(appBBSize);
    }

    /*
    * Writes bb to the SocketChannel.
    * <P>
    * Returns true when the ByteBuffer has no remaining data.
    */
    private boolean tryFlush(ByteBuffer bb) throws IOException {
        super.write(bb);
        return !bb.hasRemaining();
    }

    /*
    * If any data remains in the output buffer then send it
    */
    private boolean flushHandshakeBuffer(SelectionKey passedKey) throws IOException {

        HandshakeStatus currStatus = sslEngine.getHandshakeStatus();
        boolean setRead = false;

        //Send data
        if (!tryFlush(outNetBB)) {
            return false;
        }

        // See if we need to switch from write to read mode.
        switch (currStatus) {

            //If finished
            case FINISHED:
                initialHSComplete = true;
                theSocketHandler.setState(Constants.CONNECTED);
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
            theSocketHandler.getPortRouter().getSelRouter().changeOps(theSocketChannel, SelectionKey.OP_READ);
        }

        return initialHSComplete;
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
    public boolean doHandshake(SelectionKey passedKey) throws IOException {

        HandshakeStatus currStatus;

        if ( initialHSComplete ){
            return true;
        }

        /*
        * Flush out the outgoing buffer, if there's anything left in
        * it.
        */
        if (outNetBB.hasRemaining()) {
            return flushHandshakeBuffer(passedKey);
        }

        //Get the current handshake status
        currStatus = sslEngine.getHandshakeStatus();

        //If no data to send, switch on status
        switch (currStatus) {

            case NEED_UNWRAP:

                //If socket is closed
                try {

                    //Read the next bytes
                    int readCount;
                    synchronized(inNetBB){
                        readCount = theSocketChannel.read(inNetBB);
                    }

                    if (readCount == -1) {
                        sslEngine.closeInbound();
                        return initialHSComplete;
                    }

                } catch (BufferOverflowException ex){
                    RemoteLog.log(Level.WARNING, NAME_Class, "doHandshake()", ex.getMessage(), ex);
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

        return initialHSComplete;
    }

    /*
    * Do all the outstanding handshake tasks in a separate thread.
    */
    private void doTasks( final SelectionKey passedKey) {


//        final SSLTaskListener theListener = this;
//        Constants.Executor.execute( new Runnable() {
//            @Override
//            public void run(){

                //Need to check for "No trusted certificate found" - ValidatorException
                Runnable taskRunnable;
                while((taskRunnable = sslEngine.getDelegatedTask()) != null) {
                    taskRunnable.run();
                }

                //Notify listener
                taskFinished(passedKey);

//            }
//        });

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

        if (!initialHSComplete) throw new IllegalStateException();

        final int pos = requestBB.position();

        if (theSocketChannel.read(inNetBB) == -1) {
            sslEngine.closeInbound();  // probably throws exception
            return -1;
        }

        SSLEngineResult result;
        do {
            resizeRequestBB();    // guarantees enough room for unwrap
            inNetBB.flip();
            result = sslEngine.unwrap(inNetBB, requestBB);
            inNetBB.compact();

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
                    break;

                default:
                    throw new SSLException("sslEngine error during data read: " + result.getStatus());
            }
        } while ((inNetBB.position() != 0) && result.getStatus() != Status.BUFFER_UNDERFLOW);

        return (requestBB.position() - pos);
    }

    /*
    * Try to write out as much as possible from the src buffer.
    */
    @Override
    public int write(ByteBuffer src) throws IOException {

        if (!initialHSComplete) {

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

        if (outNetBB.hasRemaining() && !tryFlush(outNetBB)) {
            return retValue;
        }

        /*
        * The data buffer is empty, we can reuse the entire buffer.
        */
        outNetBB.clear();

        final SSLEngineResult result = sslEngine.wrap(src, outNetBB);
        retValue = result.bytesConsumed();

        outNetBB.flip();

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
        while (outNetBB.hasRemaining()) {
            tryFlush(outNetBB);
            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {}
        }

        return retValue;
    }

    //=============================================================================
    /**
    * Flush any remaining data.
    * <P>
    * Return true when the fileChannelBB and outNetBB are empty.
     * @return 
     * @throws java.io.IOException 
    */
    @Override
    public boolean dataFlush() throws IOException {
        boolean fileFlushed = true;

        if (outNetBB.hasRemaining()) {
            tryFlush(outNetBB);
        }

        theSocketChannel.socket().getOutputStream().flush();
        return (fileFlushed && !outNetBB.hasRemaining());
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

        if (!shutdown) {
            sslEngine.closeOutbound();
            shutdown = true;
        }

        if (outNetBB.hasRemaining() && tryFlush(outNetBB)) {
            return false;
        }

        /*
        * By RFC 2616, we can "fire and forget" our close_notify
        * message, so that's what we'll do here.
        */
        outNetBB.clear();
        final SSLEngineResult result = sslEngine.wrap(handshakeBB, outNetBB);
        if (result.getStatus() != Status.CLOSED) {
            throw new SSLException("Improper close state");
        }
        outNetBB.flip();

        /*
        * We won't wait for a select here, but if this doesn't work,
        * we'll cycle back through on the next select.
        */
        if (outNetBB.hasRemaining()) {
            tryFlush(outNetBB);
        }

        //Close the socket
        close();

        return (!outNetBB.hasRemaining() && (result.getHandshakeStatus() != HandshakeStatus.NEED_WRAP));
    }


    /*
    * Begins the handshake process
    */
    public void beginHandshake() throws SSLException {
        sslEngine.beginHandshake();
    }

    /*
    * Called when the SSL engine needs to wrap data
    *
    */
    private void doWrap(SelectionKey passedKey) throws IOException {

        SSLEngineResult result;
        DebugPrinter.printMessage( this.getClass().getSimpleName(), "Wrapping.");

        // The flush above guarantees the out buffer to be empty
        outNetBB.clear();
        result = sslEngine.wrap(handshakeBB, outNetBB);
        outNetBB.flip();

        HandshakeStatus currStatus = result.getHandshakeStatus();
        if(currStatus == HandshakeStatus.FINISHED){

            initialHSComplete = true;
            DebugPrinter.printMessage( this.getClass().getSimpleName(), "Finished handshaking.");

            //Flush the buffer so that the client will get a finished flag too
            if (outNetBB.hasRemaining()){
                tryFlush(outNetBB);
            }

            //Notify the comm
            theSocketHandler.setState( Constants.CONNECTED);
            theSocketHandler.getPortRouter().beNotified();           

            return;
        }

        //Switch on ssl engine wrap
        switch (result.getStatus()) {
                case OK:

                if (currStatus == HandshakeStatus.NEED_TASK) {
                    doTasks( passedKey );
                }

                if (passedKey != null) {
                    theSocketHandler.getPortRouter().getSelRouter().changeOps(theSocketChannel, SelectionKey.OP_WRITE | SelectionKey.OP_READ);
                }

                break;

                default: // BUFFER_OVERFLOW/BUFFER_UNDERFLOW/CLOSED:
                    throw new SSLException("Received" + result.getStatus() +
                                        "during initial handshaking");
        }

    }

    /*
    * Called when the SSL engine needs to unwrap data
    *
    */
    private void doUnwrap(SelectionKey passedKey) throws IOException {

        SSLEngineResult result;
        DebugPrinter.printMessage( this.getClass().getSimpleName(), "Unwrapping.");

        // Don't need to resize requestBB, since no app data should
        synchronized(inNetBB){
            inNetBB.flip();
            result = sslEngine.unwrap(inNetBB, requestBB);
            inNetBB.compact();
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
                        initialHSComplete = true;
                        DebugPrinter.printMessage( this.getClass().getSimpleName(), "Finished handshaking.");

                        
                        //Notify the comm
                        theSocketHandler.setState( Constants.CONNECTED);
                        theSocketHandler.getPortRouter().beNotified(); 
//                        thePortRouter.connectionCompleted(theSocketChannel, true, true);
                        
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
                    theSocketHandler.getPortRouter().getSelRouter().changeOps(theSocketChannel, SelectionKey.OP_READ);
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
    */
    private void taskFinished(SelectionKey passedKey) {

        HandshakeStatus currStatus = sslEngine.getHandshakeStatus();

        try {

            switch (currStatus) {
                case NEED_UNWRAP:

                    // Don't need to resize requestBB, since no app data should
                    if(!outNetBB.hasRemaining()){
                        doUnwrap(passedKey);
                    }
                    break;

                case NEED_WRAP:
                    if(!outNetBB.hasRemaining())  {

                        // Wrap up the next data and change to read
                        doWrap(passedKey);
                    }
                    break;
                    
                case FINISHED:
                    initialHSComplete = true;
                    DebugPrinter.printMessage( this.getClass().getSimpleName(), "Finished handshaking.");

                    //Notify the comm
                    theSocketHandler.setState( Constants.CONNECTED);
                    theSocketHandler.getPortRouter().beNotified(); 
//                    thePortRouter.connectionCompleted(theSocketChannel, true, true);                                        
                    break;
                    
                default:
    //                   DebugPrinter.printMessage(SecureSocketChannelWrapper.class, "Not handshaking.");

            }

        } catch (IOException ex){
            RemoteLog.log(Level.SEVERE, NAME_Class, "taskFinished()", ex.getMessage(), ex );
        }
    }

}
