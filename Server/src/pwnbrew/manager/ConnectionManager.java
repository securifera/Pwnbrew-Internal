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
package pwnbrew.manager;

import pwnbrew.network.Message;
import pwnbrew.selector.SocketChannelHandler;

/**
 *
 * @author Securifera
 */
public abstract class ConnectionManager {
    
    public final static int STAGE_CHANNEL_ID = Message.STAGING_MESSAGE_TYPE;
    public final static int COMM_CHANNEL_ID = 1;
   
    
//    abstract public void closeConnections();
    
    abstract public void shutdown();
    
    //===============================================================
    /**
     *  Returns the SocketChannelHandler for the passed id.
     * 
     * @param passedInt
     * @return 
    */  
    abstract public SocketChannelHandler getSocketChannelHandler( Integer passedInt );
    
     //===============================================================
     /**
     *  Registers the provided SocketChannelHandler with the server under the
     * given channel id.
     *
     * @param channelId
     * @param theHandler
     * @return 
     */
    abstract public boolean setHandler( int channelId, SocketChannelHandler theHandler);

    //===============================================================
    /**
     *  Removes the channel id
     * 
     * @param channelId 
     * @return  
    */
    abstract public SocketChannelHandler removeHandler(int channelId);

}
