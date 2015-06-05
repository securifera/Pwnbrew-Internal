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
* StageFlag.java
*
* Created on Feb 2, 2014, 8:11:15 PM
*/

package pwnbrew.network.control.messages;

import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import pwnbrew.log.Log;
import pwnbrew.manager.PortManager;
import pwnbrew.manager.DataManager;
import pwnbrew.utilities.SocketUtilities;
import pwnbrew.network.ControlOption;
import static pwnbrew.network.control.messages.SendStage.NAME_Class;
import pwnbrew.utilities.Utilities;

/**
 *
 *  
 */
@SuppressWarnings("ucd")
public final class StageFlagAck extends ControlMessage{
    
    private static final byte OPTION_CLIENT_ID = 44;
    private static final byte OPTION_JVM_VERSION = 16;   
    
    private String theJvmVersion;    
    private int theRelayClientId;
    
     //Class name
    private static final String NAME_Class = StageFlagAck.class.getSimpleName();    

    // ==========================================================================
    /**
     * Constructor
     *
     * @param passedId
    */
    public StageFlagAck(byte[] passedId ) {
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
            case OPTION_CLIENT_ID:
                theRelayClientId = SocketUtilities.byteArrayToInt(theValue);
                break;
            case OPTION_JVM_VERSION:
                try {
                    theJvmVersion = new String( theValue, "US-ASCII");
                } catch (UnsupportedEncodingException ex) {}
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

        Payload aPayload = Utilities.getClientPayload( theRelayClientId, theJvmVersion );
        if( aPayload != null )
            DataManager.send(passedManager,aPayload);
        else
            Log.log(Level.SEVERE, NAME_Class, "evaluate()", "Unable to retrieve payload, ensure one has been loaded into the library", null );
                 
       
    }

}
