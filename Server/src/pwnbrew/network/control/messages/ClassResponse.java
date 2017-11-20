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
* NoOp.java
*
* Created on June 7, 2013, 9:55:42 PM
*/

package pwnbrew.network.control.messages;

import pwnbrew.network.ControlOption;

/**
 *
 *  
 */
@SuppressWarnings("ucd")
public final class ClassResponse extends ControlMessage{
    
    private static final byte OPTION_MSG_TO_RESEND = 91;
    private static final byte OPTION_CLASS_BYTES = 92;
    
     //Class name
    private static final String NAME_Class = ClassResponse.class.getSimpleName();

    public static final short MESSAGE_ID = 0x32;    


    // ==========================================================================
    /**
     * Constructor
     *
     * @param destId
     * @param classBytes
     * @param msgBytes
    */
    @SuppressWarnings("ucd")
    public ClassResponse( int destId, byte[] classBytes, byte[] msgBytes ) {
        super(MESSAGE_ID, destId);
        
        //Add the bytes for the class
        ControlOption aTlv = new ControlOption( OPTION_CLASS_BYTES, classBytes);
        addOption(aTlv);
        
        //Add the bytes for the class
        aTlv = new ControlOption( OPTION_MSG_TO_RESEND, msgBytes);
        addOption(aTlv);
    }
 
}
