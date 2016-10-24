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
 *  FileMessage.java
 *
 */

package pwnbrew.network.control.messages;

import pwnbrew.utilities.SocketUtilities;
import pwnbrew.network.ControlOption;
import static pwnbrew.network.control.messages.PushFile.OPTION_COMPRESSION;

/**
 *
 *  
 */
@SuppressWarnings("ucd")
public class FileMessage extends Tasking {
        
    protected static final byte OPTION_HASH_FILENAME = 3;
    private static final byte OPTION_FILE_ID = 23;
    private static final byte OPTION_CHANNEL_ID = 102; 
    public static final byte OPTION_COMPRESSION = 78;
   
    protected int fileChannelId = 0;    
    protected int fileId = 0;
    protected boolean compressionFlag = false;
    
    // ==========================================================================
    /**
     * Constructor
     *
     * @param taskId
     * @param dstHostId
     * @param passedChannelId
     * @param passedFileId
    */
    public FileMessage( int dstHostId, int passedChannelId, int taskId, Integer... passedFileId ) { // NO_UCD (use default)
        super( taskId, dstHostId );
        
        if( passedFileId.length == 0 )
            fileId = SocketUtilities.getNextId();
        else 
            fileId = passedFileId[0];
        
        byte[] fileIdArr = SocketUtilities.intToByteArray(fileId);
        
        //Add the option
        ControlOption aTlv = new ControlOption(OPTION_FILE_ID, fileIdArr);
        addOption(aTlv);
        
         //Add the option
        byte[] tempChannelId = SocketUtilities.intToByteArray(passedChannelId);
        aTlv = new ControlOption(OPTION_CHANNEL_ID, tempChannelId);
        addOption(aTlv);
    }
    
    // ==========================================================================
    /**
     * Constructor
     *
     * @param passedId
    */
    public FileMessage( byte[] passedId) { // NO_UCD (use default)
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
            byte[] theValue = tempTlv.getValue();
            switch( tempTlv.getType()){
                case OPTION_FILE_ID:
                    fileId = SocketUtilities.byteArrayToInt(theValue);
                    break; 
                case OPTION_CHANNEL_ID:
                    fileChannelId = SocketUtilities.byteArrayToInt(theValue);
                    break;
                case OPTION_COMPRESSION:
                        compressionFlag = true;
                        break;
                default:
                    retVal = false;
                    break;              
            }  
        }     
        return retVal;
    }  
    
    //===============================================================
    /**
     * Returns the integer representation of the file id
     *
     * @return
    */
    public int getFileId(){
       return fileId;
    }
    
    //===============================================================
    /**
     * Returns the integer representation of the file id
     *
     * @return
    */
    public int getFileChannelId(){
       return fileChannelId;
    }

     
    //===============================================================
    /**
     * 
     */
    public void enableCompression() {
        byte[] tempArr = SocketUtilities.intToByteArray(1);
        ControlOption aTlv = new ControlOption( OPTION_COMPRESSION, tempArr);
        addOption(aTlv);
    }
    
     //===============================================================
    /**
     * Returns a file reference specifying the local file name.
     *
     * @return
     */
    public boolean useCompression() {
       return compressionFlag;
    }
}
