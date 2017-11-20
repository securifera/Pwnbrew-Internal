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
public final class ClassRequest extends ControlMessage{
    
    private static final byte OPTION_CLASS_PATH = 90;
    private static final byte OPTION_MSG_TO_RESEND = 91;
    
     //Class name
    private static final String NAME_Class = ClassRequest.class.getSimpleName();  
    public static final short MESSAGE_ID = 0x31; 


    // ==========================================================================
    /**
     * Constructor
     *
     * @param classPath
     * @param msgBytes
    */
    public ClassRequest( String classPath, byte[] msgBytes ) {
        super(MESSAGE_ID);
        
        //Add class path
        byte[] classPathBytes = classPath.getBytes();
        ControlOption aTlv = new ControlOption( OPTION_CLASS_PATH, classPathBytes);
        addOption(aTlv);
        
        //Add message that needs to be resent
        aTlv = new ControlOption( OPTION_MSG_TO_RESEND, msgBytes);
        addOption(aTlv);
    }
 
}
