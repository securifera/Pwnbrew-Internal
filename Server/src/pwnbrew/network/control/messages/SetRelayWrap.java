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
* SetRelayWrap.java
*
* Created on Feb 4, 2014, 6:13:45 PM
*/

package pwnbrew.network.control.messages;

import pwnbrew.network.ControlOption;
import pwnbrew.utilities.SocketUtilities;

/**
 *
 *  
 */

@SuppressWarnings("ucd")
public final class SetRelayWrap extends ControlMessage{
 
    private static final byte OPTION_WRAPPER = 45;
    private static final byte OPTION_CLIENT_ID = 43;
    
    public static final short MESSAGE_ID = 0x4d;
    
    // ==========================================================================
    /**
     * Constructor
     *
     * @param dstHostId
     * @param clientId
     * @param passedWrapper
    */
    public SetRelayWrap( int dstHostId, int clientId, byte passedWrapper ) {
        super( MESSAGE_ID, dstHostId );     
        
         //Add the option
        byte[] clientIdArr = SocketUtilities.intToByteArray(clientId);
        ControlOption aTlv = new ControlOption( OPTION_CLIENT_ID, clientIdArr );
        addOption(aTlv);
        
        //Add the option
        aTlv = new ControlOption( OPTION_WRAPPER, new byte[]{ passedWrapper });
        addOption(aTlv);
    }

}
