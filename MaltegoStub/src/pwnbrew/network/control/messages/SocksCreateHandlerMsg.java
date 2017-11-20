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

import pwnbrew.utilities.SocketUtilities;
import pwnbrew.network.ControlOption;

/**
 *
 *  
 */
public final class SocksCreateHandlerMsg extends ControlMessage {
    
    private static final byte OPTION_HANDLER_ID = 29;
    private static final byte OPTION_CONNECT_STR = 30;
    
    //Class name
    private static final String NAME_Class = SocksCreateHandlerMsg.class.getSimpleName();
        
    public static final short MESSAGE_ID = 0x50;
 
     // ==========================================================================
    /**
     * Constructor
     *
     * @param dstHostId
     * @param targedId
     * @param connectStr
    */
    public SocksCreateHandlerMsg( int dstHostId, int targedId, String connectStr ) {
        super( MESSAGE_ID, dstHostId);
                
        byte[] tempBytes =SocketUtilities.intToByteArray(targedId);        
        ControlOption aTlv = new ControlOption(OPTION_HANDLER_ID, tempBytes);
        addOption(aTlv);
        
        tempBytes = connectStr.getBytes();
        aTlv = new ControlOption(OPTION_CONNECT_STR, tempBytes);
        addOption(aTlv);
    }

}/* END CLASS SocksCreateHandlerMsg */
