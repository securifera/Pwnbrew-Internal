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

import java.io.UnsupportedEncodingException;
import pwnbrew.MaltegoStub;
import pwnbrew.functions.Function;
import pwnbrew.manager.PortManager;
import pwnbrew.network.ControlOption;

/**
 *
 *  
 */
public final class RemoteException extends ControlMessage{ // NO_UCD (use default)

    public static final byte OPTION_EXCEPTION_MSG = 22;
    private String theExceptionMsg;
    
    public static final short MESSAGE_ID = 0x62;
    
    // ==========================================================================
    /**
     * Constructor
     *
     * @param passedId
    */
    public RemoteException( byte[] passedId ) { // NO_UCD (use default)
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
        try {
            byte[] theValue = tempTlv.getValue();
            switch( tempTlv.getType()){
                case OPTION_EXCEPTION_MSG:
                     theExceptionMsg = new String( theValue, "US-ASCII");
                    break;
            }
        } catch (UnsupportedEncodingException ex) {
            ex = null;
        }
        
        return retVal;
    }  
    
    //=========================================================================
    /**
     * 
     * @return 
     */
    public String getMessage(){
        return theExceptionMsg;
    }
    
    //===============================================================
    /**
    *   Performs the logic specific to the message.
    *
     * @param passedManager
    */
    @Override
    public void evaluate( PortManager passedManager ) {
        //If the return value is true
        if( passedManager instanceof MaltegoStub ){            
            MaltegoStub theStub = (MaltegoStub)passedManager;
            Function aFunction = theStub.getFunction(); 
            aFunction.handleException( this );
            
//            MaltegoMessage aMsg = aFunction.getMaltegoMsg();
//            
//             //Create a relay object
//            pwnbrew.xml.maltego.Exception exMsg = new pwnbrew.xml.maltego.Exception( theExceptionMsg );
//            MaltegoTransformExceptionMessage malMsg = aMsg.getExceptionMessage();
//
//            //Create the message list
//            malMsg.getExceptionMessages().addExceptionMessage(exMsg); 
//            System.out.println( aMsg.getXml() );
//            System.exit(0);
            
        }
    }

}/* END CLASS RemoteException */
