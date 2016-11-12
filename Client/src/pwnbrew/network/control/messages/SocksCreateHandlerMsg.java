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

package pwnbrew.network.control.messages;

import java.io.IOException;
import java.util.logging.Level;
import pwnbrew.log.LoggableException;
import pwnbrew.log.RemoteLog;
import pwnbrew.manager.DataManager;
import pwnbrew.manager.PortManager;
import pwnbrew.network.ControlOption;
import pwnbrew.socks.SocksMessageManager;
import pwnbrew.utilities.SocketUtilities;

/**
 *
 *  
 */
public class SocksCreateHandlerMsg extends ControlMessage {
    
    private int theHandlerId = 0;
    private String theConnectStr = null;
    
    private static final byte OPTION_HANDLER_ID = 29;
    private static final byte OPTION_CONNECT_STR = 30;
    //Class name
    private static final String NAME_Class = SocksCreateHandlerMsg.class.getSimpleName();
     
    // ==========================================================================
    /**
     * Constructor
     *
     * @param passedId
    */
    public SocksCreateHandlerMsg( byte[] passedId ) {
        super( passedId );
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

            case OPTION_HANDLER_ID:                    
                theHandlerId = SocketUtilities.byteArrayToInt(theValue); 
                break;
            case OPTION_CONNECT_STR:                    
                theConnectStr = new String(theValue); 
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
        
        try {
            
            SocksMessageManager aSMM = SocksMessageManager.getSocksMessageManager();
            if( aSMM == null )
                aSMM = SocksMessageManager.initialize(passedManager);
            
            //Create a handler for this id
            boolean retVal = aSMM.createHandler( getSrcHostId(), theHandlerId, theConnectStr);
            SocksCreateHandlerAckMsg anAck = new SocksCreateHandlerAckMsg(theHandlerId, retVal);
            anAck.setDestHostId(getSrcHostId());
            
            //Send back message
            DataManager.send(passedManager, anAck);
            
        } catch( IOException ex){
            RemoteLog.log( Level.SEVERE, NAME_Class, "evaluate", ex.getMessage(), null); 
        }
        
    }

}/* END CLASS SocksCreateHandlerMsg */
