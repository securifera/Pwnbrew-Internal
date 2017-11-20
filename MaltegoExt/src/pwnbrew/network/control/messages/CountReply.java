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
* CountReply.java
*
* Created on Oct 18, 2013, 10:12:33 PM
*/

package pwnbrew.network.control.messages;

import java.io.UnsupportedEncodingException;
import pwnbrew.network.ControlOption;
import pwnbrew.utilities.SocketUtilities;

/**
 *
 *  
 */
public final class CountReply extends MaltegoMessage{ 
    
    private static final byte OPTION_COUNT = 70;    
    private static final byte OPTION_COUNT_ID = 80;
    private static final byte OPTION_OPTIONAL_ID = 81;
    
    public static final short MESSAGE_ID = 0x66;
    
    // ==========================================================================
    /**
     * Constructor
     *
     * @param dstHostId
     * @param passedCount the count
     * @param passedType Id for the thing being counted
     * @param passedId Id for the object that has the thing being counted
     * @throws java.io.UnsupportedEncodingException
    */
    public CountReply( int dstHostId, int passedCount, int passedType, int passedId ) throws UnsupportedEncodingException {
        super( MESSAGE_ID, dstHostId );
        
        byte[] tempBytes = SocketUtilities.intToByteArray(passedCount);
        ControlOption aTlv = new ControlOption(OPTION_COUNT, tempBytes);
        addOption(aTlv); 
        
        //Add file type
        tempBytes = SocketUtilities.intToByteArray(passedType);
        aTlv = new ControlOption( OPTION_COUNT_ID, tempBytes);
        addOption(aTlv);
        
        //Add file type
        tempBytes = SocketUtilities.intToByteArray(passedId);
        aTlv = new ControlOption( OPTION_OPTIONAL_ID, tempBytes);
        addOption(aTlv);
    }

}/* END CLASS CountReply */
