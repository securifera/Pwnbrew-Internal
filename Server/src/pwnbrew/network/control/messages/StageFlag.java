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
import pwnbrew.network.ControlOption;
import pwnbrew.utilities.SocketUtilities;

/**
 *
 *  
 */

public final class StageFlag extends ControlMessage{
    
    private static final byte OPTION_STAGE_FLAG = 42;
    private static final byte OPTION_CLIENT_ID = 43;
    private static final byte OPTION_JVM_VERSION = 16;
    
     //Class name
    private static final String NAME_Class = StageFlag.class.getSimpleName();    


    // ==========================================================================
    /**
     * Constructor
     *
     * @param dstHostId
     * @param clientId
     * @param isStaged
     * @param passedVersion
     * @throws java.io.UnsupportedEncodingException
    */
    public StageFlag( int dstHostId, int clientId, boolean isStaged, String passedVersion ) throws UnsupportedEncodingException {
        super( dstHostId );
        
        //Add the option
        byte[] clientIdArr = SocketUtilities.intToByteArray(clientId);
        ControlOption aTlv = new ControlOption( OPTION_CLIENT_ID, clientIdArr );
        addOption(aTlv);
        
        //Add the flag
        byte stageFlag = 0x0;
        if( isStaged ){
            stageFlag = 0x1;
        }
        
        //Add the option
        aTlv = new ControlOption( OPTION_STAGE_FLAG, new byte[]{ stageFlag });
        addOption(aTlv);
        
        byte[] tempArr = passedVersion.getBytes("US-ASCII");
        aTlv = new ControlOption( OPTION_JVM_VERSION, tempArr);
        addOption(aTlv);
    }

}
