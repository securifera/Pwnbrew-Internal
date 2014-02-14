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
* CreateShell.java
*
* Created on June 7, 2013, 8:55:42 PM
*/

package pwnbrew.network.control.messages;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import pwnbrew.misc.Constants;
import pwnbrew.network.ControlOption;

/**
 *
 *  
 */
public final class CreateShell extends ControlMessage{
    
    public static final byte OPTION_CMD_STRING = 5;
    public static final byte OPTION_ENCODING = 20;
    public static final byte OPTION_STARTUP_CMD = 22;
    public static final byte OPTION_REDIRECT_STDERR = 24;
    
    // ==========================================================================
    /**
    * Constructor
    *
     * @param dstHostId
     * @param passedEncoding
     * @param passedCmdString
     * @param passedStartCmd
     * @param passedBool
     * @throws java.io.UnsupportedEncodingException
    */
    public CreateShell( int dstHostId, String[] passedCmdString, String passedEncoding, 
            String passedStartCmd, boolean passedBool ) throws UnsupportedEncodingException {
        super( dstHostId );

        //Get each string and append a null byte to ensure that each string is terminated
        ByteBuffer aBB = ByteBuffer.allocate(Constants.GENERIC_BUFFER_SIZE);
        for(String aString : passedCmdString){

           byte[] strBytes = aString.getBytes("US-ASCII");

           aBB.put(strBytes);
           aBB.put((byte)0x00);
        }

        //Create a byte array to hold the delimited string
        byte[] cmdLineArr = new byte[aBB.position()];
        aBB.flip();
        aBB.get(cmdLineArr);
           
        ControlOption aTlv = new ControlOption( OPTION_CMD_STRING, cmdLineArr);
        optionList.add(aTlv);

        //Add the encoding
        byte[] strBytes = passedEncoding.getBytes("US-ASCII");
        aTlv = new ControlOption( OPTION_ENCODING, strBytes);
        optionList.add(aTlv);
        
        //Add the startup command
        strBytes = passedStartCmd.getBytes("US-ASCII");
        aTlv = new ControlOption( OPTION_STARTUP_CMD, strBytes);
        optionList.add(aTlv);
        
        //Add STDERR redirect byte
        byte redByte = 0x0;
        if(passedBool){
            redByte = 0x1;
        }
        byte[] tempBytes = new byte[]{ redByte };
        aTlv = new ControlOption( OPTION_REDIRECT_STDERR, tempBytes);
        addOption(aTlv);
        
    }

}/* END CLASS CreateShell */
