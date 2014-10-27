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
* FileSystemMsg.java
*
* Created on Dec 21, 2013, 10:12:42 PM
*/

package pwnbrew.network.control.messages;

import java.io.UnsupportedEncodingException;
import pwnbrew.MaltegoStub;
import pwnbrew.functions.Function;
import pwnbrew.functions.ToFileBrowser;
import pwnbrew.manager.PortManager;
import pwnbrew.utilities.SocketUtilities;
import pwnbrew.network.ControlOption;

/**
 *
 *  
 */
public final class FileSystemMsg extends Tasking {
    
    private static final byte OPTION_SIZE = 6; 
    private static final byte OPTION_PATH = 7; 
    private static final byte OPTION_FILE_TYPE = 8; 
    private static final byte OPTION_LAST_MODIFIED = 9;  
    
    public static final byte HOST = 11; 
    public static final byte DRIVE = 12; 
    public static final byte FILE = 13; 
    public static final byte FOLDER = 14; 
    
    private String filePath;
    private long size;
    private String dateModified;
    private byte fileType = 0;

     // ==========================================================================
    /**
     * Constructor
     *
     * @param passedId
    */
    public FileSystemMsg(byte[] passedId ) {
        super( passedId );
    }
    
     //=========================================================================
    /**
     *  Sets the variable in the message related to this TLV
     * 
     * @param tempTlv 
     * @return  
     */
    @Override
    public boolean setOption( ControlOption tempTlv ){        

        boolean retVal = true;    
        if( !super.setOption(tempTlv)){
            try {
                byte[] theValue = tempTlv.getValue();
                switch( tempTlv.getType()){
                    case OPTION_PATH:
                        filePath = new String( theValue, "US-ASCII");
                        break;
                    case OPTION_SIZE:
                        size = SocketUtilities.byteArrayToLong(theValue);
                        break;
                    case OPTION_LAST_MODIFIED:
                        dateModified = new String( theValue, "US-ASCII");
                        break;
                    case OPTION_FILE_TYPE:                    
                        if( theValue.length == 1){
                            fileType = theValue[0];
                        }
                        break;
                    default:
                        retVal = false;
                        break;
                }
            } catch (UnsupportedEncodingException ex) {
                ex = null;
            }
        }
        return retVal;
    }
    
    //===============================================================
    /**
    *   Performs the logic specific to the message.
    *
     * @param passedManager
    */
    @Override
    public void evaluate( PortManager passedManager ) {   
    
        if( passedManager instanceof MaltegoStub ){
            MaltegoStub theStub = (MaltegoStub)passedManager;
            Function aFunction = theStub.getFunction();
            if( aFunction instanceof ToFileBrowser ){                
                ToFileBrowser tfbFunc = (ToFileBrowser)aFunction;
                tfbFunc.updateFileSystem( getTaskId(), size, filePath, fileType, dateModified );
            }
        }
    
    }
   
}
