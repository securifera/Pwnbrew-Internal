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
import java.util.Date;
import pwnbrew.misc.Constants;
import pwnbrew.utilities.SocketUtilities;
import pwnbrew.network.ControlOption;

/**
 *
 *  
 */
public class SleepRelay extends MaltegoMessage {
    
    private static final byte OPTION_TARGET_HOST_ID = 22;
    private static final byte OPTION_SENDER_TIME = 19;
    //Class name
    private static final String NAME_Class = SleepRelay.class.getSimpleName();
    
    public static final short MESSAGE_ID = 0x74;
 
     // ==========================================================================
    /**
     * Constructor
     *
     * @param dstHostId
     * @param targedId
     * @throws java.io.UnsupportedEncodingException
    */
    public SleepRelay( int dstHostId, int targedId ) throws UnsupportedEncodingException  {
        super( MESSAGE_ID, dstHostId);
        
        String senderTime = Constants.CHECKIN_DATE_FORMAT.format( new Date() );        
        byte[] strBytes = senderTime.getBytes("US-ASCII");
        
        ControlOption aTlv = new ControlOption(OPTION_SENDER_TIME, strBytes);
        addOption(aTlv);

        byte[] targetIdBytes =SocketUtilities.intToByteArray(targedId);        
        aTlv = new ControlOption(OPTION_TARGET_HOST_ID, targetIdBytes);
        addOption(aTlv);
    }

}/* END CLASS SleepRelay */
