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

import java.util.Arrays;
import pwnbrew.manager.PortManager;
import pwnbrew.manager.DataManager;
import pwnbrew.misc.DebugPrinter;
import pwnbrew.network.ControlOption;
import pwnbrew.utilities.Utilities;

/**
 *
 *  
 */

@SuppressWarnings("ucd")
public final class ClassRequest extends ControlMessage{
    
    private static final byte OPTION_CLASS_PATH = 90;
    private static final byte OPTION_MSG_TO_RESEND = 91;
    
    private byte[] theMsgToResend = null;
    private String theClassPath = null;
    
     //Class name
    private static final String NAME_Class = ClassRequest.class.getSimpleName();    

    // ==========================================================================
    /**
     * Constructor
     *
     * @param passedId
    */
    public ClassRequest(byte[] passedId ) {
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
        if( !super.setOption(tempTlv)){
            
            byte[] theValue = tempTlv.getValue();
            switch( tempTlv.getType()){
                case OPTION_MSG_TO_RESEND:
                    theMsgToResend = Arrays.copyOf(theValue, theValue.length);
                    break;
                case OPTION_CLASS_PATH:
                    theClassPath = new String(theValue);
                    break;
                default:
                    retVal = false;
                    break;
            }           
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
        
        DebugPrinter.printMessage(NAME_Class, "Received request for class: " + theClassPath);
        
        //Get the address and connect                   
        byte[] retBytes = Utilities.getClassBytes(theClassPath);
        if( retBytes != null && retBytes.length > 0 ){
            //Send the class to be loaded
            ClassResponse aCR = new ClassResponse( getSrcHostId(), retBytes, theMsgToResend );
            DataManager.send(passedManager, aCR);
        }
    
    }

}
