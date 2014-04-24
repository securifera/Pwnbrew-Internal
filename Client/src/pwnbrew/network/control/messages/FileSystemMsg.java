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

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import pwnbrew.misc.Constants;
import pwnbrew.misc.SocketUtilities;
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
    
    public static final byte DRIVE = 12; 
    public static final byte FILE = 13; 
    public static final byte FOLDER = 14; 
    
    

    // ==========================================================================
    /**
     * Constructor
     *
     * @param taskId
     * @param passedFile
     * @param isRoot
     * @throws java.io.UnsupportedEncodingException
    */
    public FileSystemMsg( int taskId, File passedFile, boolean isRoot ) throws UnsupportedEncodingException {
        super(taskId);
        
        //Get file details
        if( passedFile != null ){
            
            byte fileType = FILE;  
            if( isRoot){
                fileType = DRIVE;
            } else if( passedFile.isDirectory()){               
                fileType = FOLDER;
            } 

            //If a drive or folder set size as the number of children
            long size = 0;
            if( fileType == DRIVE || fileType == FOLDER ){
                File[] fileList = passedFile.listFiles();
                if( fileList != null )
                    size = fileList.length;
            } else {
                size = passedFile.length();
            }
              
            //Get the length
            byte[] tempBytes = SocketUtilities.longToByteArray(size);

            //Add the option
            ControlOption aTlv = new ControlOption(OPTION_SIZE, tempBytes);
            addOption(aTlv);

            //Add last modified
            long lastModified = passedFile.lastModified();
            String dateStr = Constants.CHECKIN_DATE_FORMAT.format( new Date(lastModified));
            tempBytes = dateStr.getBytes();
            aTlv = new ControlOption( OPTION_LAST_MODIFIED, tempBytes);
            addOption(aTlv);

            //Add file path
            String path = passedFile.getAbsolutePath();        
            tempBytes = path.getBytes();
            aTlv = new ControlOption( OPTION_PATH, tempBytes);
            addOption(aTlv);

            //Add file type
            tempBytes = new byte[]{ fileType };
            aTlv = new ControlOption( OPTION_FILE_TYPE, tempBytes);
            addOption(aTlv);
        }
    }   
   
}
