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
* FileOperation.java
*
* Created on Dec 25, 2013, 10:12:42 PM
*/

package pwnbrew.network.control.messages;

import java.io.UnsupportedEncodingException;
import pwnbrew.misc.SocketUtilities;
import pwnbrew.network.ControlOption;

/**
 *
 *  
 */
public final class FileOperation extends Tasking {
    
    public static final byte DELETE = 78;
    public static final byte RENAME = 79;
    public static final byte DATE = 80;
    
    private static final byte OPTION_OPERATION = 42;
    private static final byte OPTION_FILE_PATH = 43;
    private static final byte OPTION_ADDITIONAL = 44;
    
    // ==========================================================================
    /**
     * Constructor
     *
     * @param dstHostId
     * @param passedOperation
     * @param filePath
     * @param addParam
     * @throws java.io.UnsupportedEncodingException
    */
    public FileOperation( int dstHostId, byte passedOperation, String filePath, String addParam ) throws UnsupportedEncodingException {
        super( SocketUtilities.getNextId(), dstHostId );   
        
        //Add file type
        byte[] tempBytes = new byte[]{ passedOperation };
        ControlOption aTlv = new ControlOption( OPTION_OPERATION, tempBytes);
        addOption(aTlv);
        
        //Add file path
        tempBytes = filePath.getBytes("US-ASCII");
        aTlv = new ControlOption( OPTION_FILE_PATH, tempBytes);
        addOption(aTlv);
        
        //Add additional param
        tempBytes = addParam.getBytes("US-ASCII");
        aTlv = new ControlOption( OPTION_ADDITIONAL, tempBytes);
        addOption(aTlv);
    }
    
  
}
