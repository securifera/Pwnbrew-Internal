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
* PushFileAck.java
*
*/

package pwnbrew.network.control.messages;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import pwnbrew.manager.PortManager;
import pwnbrew.misc.DebugPrinter;
import pwnbrew.network.ControlOption;
import pwnbrew.network.file.FileMessageManager;


/**
 *
 *  
 */
public final class PushFileAck extends FileMessage {
   
    private File fileToReceive = null;
    private String hashFilenameStr;

     //Class name
    private static final String NAME_Class = PushFileAck.class.getSimpleName();
    
    public static final short MESSAGE_ID = 0x44;
    
    // ==========================================================================
    /**
     * Constructor
     *
     * @param passedId
     * @param dstHostId
     * @param passedChannelId
     * @param passedFileId
     * @param hashFileNameStr
     * @throws java.io.IOException
    */
    public PushFileAck(int passedId, int passedFileId, int passedChannelId, String hashFileNameStr, int dstHostId ) throws IOException  {
       super( MESSAGE_ID, dstHostId, passedChannelId, passedId, passedFileId  );

       byte[] strBytes = hashFileNameStr.getBytes("US-ASCII");
       ControlOption aTlv = new ControlOption( OPTION_HASH_FILENAME, strBytes);
       addOption(aTlv);
    }
    
    // ==========================================================================
    /**
     * Constructor
     *
     * @param passedId
    */
    public PushFileAck(byte[] passedId ) { // NO_UCD (use default)
       super(passedId );
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

                if( tempTlv.getType() == OPTION_HASH_FILENAME){
                    byte[] theValue = tempTlv.getValue();
                    hashFilenameStr = new String( theValue, "US-ASCII");
                } else {
                    retVal = false;
                }

            } catch (UnsupportedEncodingException ex) {
                ex = null;
            }
        }
        
        return retVal;
    }  

    //===============================================================
    /**
     * Returns a file reference specifying the local file name.
     *
     * @return
     */
    public File getFileToReceive() {
        fileToReceive = new File(hashFilenameStr.split(":")[1]);
        return fileToReceive;
    }
    
     //===============================================================
    /**
     * Returns a file reference specifying the local file name.
     *
     * @return
     */
    public String getHashFilenameString() {
       return hashFilenameStr;
    }
    
     //===============================================================
    /**
     * Returns a string representing the file hash
     *
     * @return
     * @throws java.io.IOException
     */
    public String getFilehash() throws IOException {

       String fileNameToSend = null;
       String hashFileNameStr = getHashFilenameString();
       if( hashFileNameStr != null){
          String[] hashFileNameArr = hashFileNameStr.split(":", 2);
          fileNameToSend = hashFileNameArr[0];
       }
       
       return fileNameToSend;
    }

    //===============================================================
    /**
     * Returns a string representing the file name
     *
     * @return
     * @throws java.io.IOException
     */
    public String getFilename() throws IOException {

        String fileNameToSend = null;
        String hashFileNameStr = getHashFilenameString();
        if( hashFileNameStr != null){
            String[] hashFileNameArr = hashFileNameStr.split(":", 2);
            if( hashFileNameArr.length > 1 ){
                fileNameToSend = hashFileNameArr[1];
            }
        }

        return fileNameToSend;
    }
    
    //===============================================================
    /**
    *   Performs the logic specific to the message.
    *
     * @param passedManager
    */
    @Override
    public void evaluate( PortManager passedManager ) {
        
        FileMessageManager theFileMM = FileMessageManager.getFileMessageManager();
        theFileMM.fileUpload(this );
        
    }


}/* END CLASS PushFileAck */
