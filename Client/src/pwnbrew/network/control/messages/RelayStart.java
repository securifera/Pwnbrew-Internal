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
* RelayStart.java
*
* Created on Dec 2, 2013, 9:22:22 PM
*/

package pwnbrew.network.control.messages;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.logging.Level;
import pwnbrew.log.RemoteLog;
import pwnbrew.manager.PortManager;
import pwnbrew.manager.DataManager;
import pwnbrew.utilities.SocketUtilities;
import pwnbrew.network.ControlOption;
import pwnbrew.network.ServerPortRouter;
import pwnbrew.network.control.ControlMessageManager;
import pwnbrew.network.http.ServerHttpWrapper;
import pwnbrew.network.relay.RelayManager;

/**
 *
 *  
 */
public final class RelayStart extends ControlMessage{

    private static final byte OPTION_PORT = 24;
    private int port;   
    
    //Class name
    private static final String NAME_Class = RelayStart.class.getSimpleName();
    
    // ==========================================================================
    /**
     * Constructor
     *
     * @param passedId
    */
    public RelayStart( byte[] passedId ) {
        super(passedId);
    }
    
    //=========================================================================
    /**
     *  Sets the variable in the message related to this TLV
     * 
     * @param tempTlv 
     * @return  
     */
    @Override
    public boolean setOption( ControlOption tempTlv ){        
       
        boolean retVal = true;
        byte[] theValue = tempTlv.getValue();
        switch( tempTlv.getType()){

            case OPTION_PORT:                    
                port = SocketUtilities.byteArrayToInt(theValue); 
                break;
            default:
                retVal = false;
                break;
        }
       
        return retVal;
    }
    
    //===============================================================
    /**
    *   Performs the logic specific to the message.
    *
     * @param passedManager
    */
    @Override
    public void evaluate( PortManager passedManager ) { 
        
        boolean retVal = true;
        try {
            
            RelayManager aManager = RelayManager.getRelayManager();
            if( aManager == null ){
                aManager = RelayManager.initialize( passedManager );
            }
            
            ServerPortRouter aSPR = aManager.getServerPorterRouter();
            if( aSPR.getServerSocketChannel() == null ){
                aSPR.startServer(null, port );
                
                ServerHttpWrapper aWrapper = new ServerHttpWrapper();
                DataManager.setPortWrapper( port, aWrapper);
                
            } else {
                retVal = false;
            }
            
        } catch(IOException | GeneralSecurityException ex ){
            RemoteLog.log( Level.SEVERE, NAME_Class, "evaluate", ex.getMessage(), null); 
            retVal = false;
        }
        
        //Get the control message manager send an ack        
        ControlMessageManager aCMManager = ControlMessageManager.getControlMessageManager();
        if( aCMManager != null ){
            //Send the message
            RelayStatus aMsg = new RelayStatus( retVal );
            aMsg.setDestHostId( getSrcHostId() );
            aCMManager.send(aMsg);
        }
        
    }

}
