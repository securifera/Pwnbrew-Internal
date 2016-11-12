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
package pwnbrew.socks;

import java.io.IOException;
import java.util.logging.Level;
import pwnbrew.log.RemoteLog;
import pwnbrew.manager.DataManager;
import pwnbrew.manager.PortManager;
import pwnbrew.network.ConnectionCallback;
import pwnbrew.network.control.messages.SocksOperationAck;

/**
 *
 * @author Securifera
 */
public class SocksConnectionCallback extends ConnectionCallback{
    
    private final PortManager theManager;
    private final int theHostId;
    
    private static final String NAME_Class = SocksConnectionCallback.class.getSimpleName();

    //=====================================================================
    /**
     * 
     * @param serverIp
     * @param passedPort
     * @param passedManager 
     * @param passedId 
     */
    public SocksConnectionCallback( String serverIp, int passedPort, PortManager passedManager, int passedId ) {
        super(serverIp, passedPort);
        theManager = passedManager;
        theHostId = passedId;
    }   

    //=====================================================================
    /**
     * 
     * @param theChannelId
     */
    @Override
    public void handleConnection( int theChannelId ) {
        
        if(theChannelId != 0 ){        
            
            try {
                
                SocksMessageManager aSMM = SocksMessageManager.getSocksMessageManager();
                if( aSMM == null)
                    aSMM = SocksMessageManager.initialize(theManager);
                aSMM.setChannelId(theHostId, theChannelId);              

                //Send ack back to set channel id
                SocksOperationAck retMsg = new SocksOperationAck( theChannelId );
                retMsg.setDestHostId( theHostId );
                DataManager.send(theManager, retMsg);     
            
            } catch (IOException ex) {
                RemoteLog.log(Level.INFO, NAME_Class, "evaluate()", ex.getMessage(), ex );   
            }

        }
    }
    
}
