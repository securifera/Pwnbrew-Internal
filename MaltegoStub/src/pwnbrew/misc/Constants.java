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
* Constants.java
*
*/

package pwnbrew.misc;

import java.text.SimpleDateFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 *  
 */
abstract public class Constants {

    // Network states
    public static final int DISCONNECTED = 0;
    public static final int CONNECTED = 2;
 
    //File notification messages for the taskruner
    public static final int FILE_SENT = 1000;
    public static final int FILE_RECEIVED = 1001;
    
    //The hash algorithm used by all message digest functions
    public static final String HASH_FUNCTION = "SHA-256";
    
    public static final int  MAX_MESSAGE_SIZE = 1500;
    public static final int  GENERIC_BUFFER_SIZE = 4096;
               
    public static final ExecutorService Executor = Executors.newCachedThreadPool();
    public static final int SERVER_ID = -1;    
    
    //Constants for process streams
    public static final int STD_OUT_ID = 41;
    public static final int STD_ERR_ID = 42;
    
    public static final SimpleDateFormat THE_DATE_FORMAT = new SimpleDateFormat("MM-dd-yyyy--HHmm");  
    public static final SimpleDateFormat CHECKIN_DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy--HH:mm");      
    
    public static final String EDITOR_IMG_STR = "brew.png";  
    public static final String UPLOAD_IMG_STR = "upload.png";
    public static final String DOWNLOAD_IMG_STR = "download.png";
    public static final String ZIP_IMG_STR = "zip.png";    
    public static final String ZIP_PRESSED_IMG_STR = "zip_pressed.png";
    public static final String TERM_IMG_STR = "terminal.png";
    public static final String SCHEDULE_IMG_STR = "schedule.png";
    public static final String HOST_IMG_STR = "computer_small.png";
    public static final String DIS_HOST_IMG_STR = "dis_computer_small.png";
    public static final String OPTIONS_IMG_STR = "system.png";
    
    public static final String BUILD_STAGER_IMG_STR = "build_stager.png";
    public static final String DELETE_IMG_STR = "delete_icon.png";
    public static final String ADD_IMAGE_STR = "add.png";
    
    public static final String NAME = "name";
    public static final String SERVER_IP = "serverip";
    public static final String SERVER_PORT = "serverport";
    public static final String RELAY_PORT = "relayport";
    
    public static final String HOST_ARCH = "arch";
    public static final String HOST_OS = "os";
    public static final String HOST_ID = "hostid";
    
    public static final String FILE_UPLOAD = "FILE_UPLOAD";
    public static final String FILE_DOWNLOAD = "FILE_DOWNLOAD";
    public static final String DELETE = "Delete";
    
    public static final String HOST_NAME = "name";
    public static final String SEARCH_IMG_STR = "search.png";
    public static final String FOLDER_IMG_STR = "folder_yellow.png";
    public static final String FOLDER_OPEN_IMG_STR = "folder_open.png";
    
    public static final String MANIFEST_FILE = "META-INF/MANIFEST.MF";
    public static final String CERT_ALIAS = "Alias";  
    public static final String CERT_PW = "PS";  
    public static final String UID_LABEL = "UID";
    
    public static final String MODULE_TYPE_LABEL = "Type";
    public static final String PAYLOAD_VERSION_LABEL = "Version";
    public static final String STAGER_URL = "Private";
    
    public static final String PAYLOAD_ALIAS_TYPE = "plib";
    public static final String STAGER_ALIAS_TYPE = "slib";
    public static final String LOCAL_ALIAS_EXTENSION_TYPE = "llib";
    public static final String REMOTE_ALIAS_EXTENSION_TYPE = "rlib";
    
    public static final String PAYLOAD_TYPE = "PAYLOAD";
    public static final String STAGER_TYPE = "STAGER";
    public static final String LOCAL_EXTENSION_TYPE = "LOCAL EXTENSION";
    public static final String REMOTE_EXTENSION_TYPE = "REMOTE EXTENSION";
    
    public final static int COMM_CHANNEL_ID = 1;
    
    
}/* END CLASS Constants */
