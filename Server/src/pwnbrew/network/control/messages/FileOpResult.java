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
* FileOpResult.java
*
* Created on Dec 25, 2013, 10:12:42 PM
*/

package pwnbrew.network.control.messages;

import pwnbrew.host.HostController;
import pwnbrew.manager.CommManager;
import pwnbrew.manager.ServerManager;
import pwnbrew.network.ControlOption;

/**
 *
 *  
 */
public final class FileOpResult extends Tasking {
    
    private static final byte OPTION_OP_RESULT = 50;
    private boolean opRetVal = false;
            
    // ==========================================================================
    /**
     * Constructor
     *
     * @param passedId
    */
    public FileOpResult(byte[] passedId ) {
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
                case OPTION_OP_RESULT:
                    if( theValue.length > 0 ){
                        byte retByte = theValue[0];
                        if( retByte == 1 ){
                            opRetVal = true;
                        }
                    }
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
    public void evaluate( CommManager passedManager ) {   
    
        //If the return value is true
        if( opRetVal ){
            String clientIdStr = Integer.toString( getSrcHostId() );
            final ServerManager aSM = (ServerManager) passedManager;
            final HostController theController = aSM.getServer().getGuiController().getHostController( clientIdStr );
            if( theController != null ){   
                theController.refreshFileSystemJTree();
            }
        }
    }
   
}
