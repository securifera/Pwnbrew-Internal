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
* HostMsg.java
*
* Created on Oct 18, 2013, 10:12:33 PM
*/

package pwnbrew.network.control.messages;

import java.io.UnsupportedEncodingException;
import pwnbrew.network.ControlOption;
import pwnbrew.network.control.messages.ControlMessage;
import pwnbrew.utilities.SocketUtilities;

/**
 *
 *  
 */
public final class HostMsg extends ControlMessage{ 
    
    private static final byte OPTION_HOSTNAME = 60;
    private static final byte OPTION_ARCH = 61;
    private static final byte OPTION_OS = 62;
    private static final byte OPTION_CONNECTED = 63;
    private static final byte OPTION_HOST_ID = 64;
    
    // ==========================================================================
    /**
     * Constructor
     *
     * @param dstHostId
    */
    public HostMsg( int dstHostId, String passeHostname, String passedOS, 
            String passedArch, int hostId, boolean isConnected ) throws UnsupportedEncodingException {
        super( dstHostId );
        
          //Add file type
        byte[] tempBytes = passeHostname.getBytes("US-ASCII");
        ControlOption aTlv = new ControlOption( OPTION_HOSTNAME, tempBytes);
        addOption(aTlv);
        
        //Add file path
        tempBytes = passedArch.getBytes("US-ASCII");
        aTlv = new ControlOption( OPTION_ARCH, tempBytes);
        addOption(aTlv);
        
        //Add additional param
        tempBytes = passedOS.getBytes("US-ASCII");
        aTlv = new ControlOption( OPTION_OS, tempBytes);
        addOption(aTlv);
        
        //Add file type
        tempBytes = SocketUtilities.intToByteArray(hostId);
        aTlv = new ControlOption( OPTION_HOST_ID, tempBytes);
        addOption(aTlv);
        
         //Add file type
        byte conByte = 0;
        if( isConnected )
            conByte = 1;
        
        tempBytes = new byte[]{ conByte };
        aTlv = new ControlOption( OPTION_CONNECTED, tempBytes);
        addOption(aTlv);
    }

}/* END CLASS HostMsg */
