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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import pwnbrew.log.LoggableException;
import pwnbrew.misc.Constants;
import pwnbrew.selector.SocketChannelHandler;

/**
 */
public class SocketChannelWrapper {

    protected SocketChannel theSocketChannel;    
    protected SocketChannelHandler theParentHandler;

    protected ByteBuffer theRequestByteBuffer;
    private static final int theRequestByteBufferSize = Constants.GENERIC_BUFFER_SIZE;

    //===============================================================
    /**
     * Constructor
     * 
     * @param passedSocketChannel
     * @param passedListener
     * @throws IOException 
     */
    @SuppressWarnings("ucd")
    public SocketChannelWrapper (final SocketChannel passedSocketChannel, final SocketChannelHandler passedListener) throws IOException {
	theSocketChannel = passedSocketChannel;
        theParentHandler = passedListener;
	theSocketChannel.configureBlocking (false);        
        theRequestByteBuffer = ByteBuffer.allocate(theRequestByteBufferSize);
    }

    //===============================================================
    /**
     * Returns the socket channel
     * 
     * @return 
     */
    public SocketChannel getSocketChannel() {
	return theSocketChannel;
    }

    //===============================================================
    /**
     *  Clears the byte buffer
     */
    public void clear(){
       theRequestByteBuffer.clear();
    }

    //===============================================================
    /**
     * Return a ByteBuffer with "remaining" space to work.  If you have to
     * reallocate the ByteBuffer, copy the existing info into the new buffer.
     *
     * @param remaining 
    */    
    protected void resizeRequestBB (final int remaining) {
       if (theRequestByteBuffer.remaining() < remaining) {
          // Expand buffer for large request
          ByteBuffer bb = ByteBuffer.allocate(theRequestByteBuffer.capacity() * 2);
          theRequestByteBuffer.flip();
          bb.put(theRequestByteBuffer);
          theRequestByteBuffer = bb;
       }
    }

    //===============================================================
    /**
    * Perform any handshaking processing.
    * <P>
    * This variant is for Servers with SelectionKeys, so that
    * we can register for selectable operations (e.g. selectable
    * non-blocking).
    * <P>
    * return true when we're done with handshaking.
    * @param sk
    * @return
    * @throws IOException 
     * @throws pwnbrew.log.LoggableException 
    */
    public boolean doHandshake(SelectionKey sk) throws IOException, LoggableException {
        return true;
    }

    /*
     * Resize (if necessary) the inbound data buffer, and then read more
     * data into the read buffer.
     */
    public int read() throws IOException {
	/*
	 * Allocate more space if less than 5% remains
	 */
	resizeRequestBB(theRequestByteBufferSize/20);
	return theSocketChannel.read(theRequestByteBuffer);
    }

    /*
     * All data has been read, pass back the request in one buffer.
     */
    public ByteBuffer getReadBuf() {
	return theRequestByteBuffer;
    }

    /*
     * Write the src buffer into the socket channel.
     */
    public int write(ByteBuffer src) throws IOException {
	return theSocketChannel.write(src);
    }

    /*
     * Flush any outstanding data to the network if possible.
     * <P>
     * This isn't really necessary for the insecure variant, but needed
     * for the secure one where intermediate buffering must take place.
     * <P>
     * Return true if successful.
     */
    public boolean dataFlush() throws IOException {
	return true;
    }

    /*
     * Start any connection shutdown processing.
     * <P>
     * This isn't really necessary for the insecure variant, but needed
     * for the secure one where intermediate buffering must take place.
     * Return true if successful, an
     * <P>d the data has been flushed.
     */
    public boolean shutdown() throws IOException {
        close();
	return true;
    }

    //===============================================================
    /**
     * Close the underlying connection.
     * @throws java.io.IOException
     */
    public void close() throws IOException { // NO_UCD (use default)
        theParentHandler.getPortRouter().getSelRouter().unregister(theSocketChannel);
	theSocketChannel.close();
    }
    

}
