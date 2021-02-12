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
package pwnbrew.selector;

import pwnbrew.ClientConfig;
import pwnbrew.manager.ConnectionManager;
import pwnbrew.manager.DataManager;
import pwnbrew.network.ConnectionCallback;
import pwnbrew.network.RegisterMessage;

/**
 *
 * @author Securifera
 */
public class SocketChannelCallback extends ConnectionCallback{
    
    private final RegisterMessage theRegMsg;
    private final ConnectionManager theCM;
    
    //===========================================================================
    /**
     * 
     * @param passedIp
     * @param passedPort 
     * @param aMsg 
     * @param passedCM 
     */
    public SocketChannelCallback(String passedIp, int passedPort, RegisterMessage aMsg, ConnectionManager passedCM) {
        super(passedIp, passedPort);
        theRegMsg = aMsg;
        theCM = passedCM;
    }
    
     //=====================================================================
    /**
     * 
     * @param theChannelId
     */
    @Override
    public void handleConnection( int theChannelId ) {
        
        SocketChannelHandler srvHandler = theCM.getSocketChannelHandler( theChannelId );
        if( srvHandler != null ){
            byte[] regBytes = theRegMsg.getBytes();
            srvHandler.queueBytes(regBytes, theRegMsg.getCancelId());
            srvHandler.signalSend();
        }            
    
    }
    
}
