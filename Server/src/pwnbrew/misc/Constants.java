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
* Created on July 18, 2013, 8:42:12 PM
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
    public static final int SERVER_ID = -1;    
 
    //File notification messages for the taskruner
    public static final int FILE_SENT = 1000;    
    
    //The hash algorithm used by all message digest functions
    public static final String HASH_FUNCTION = "SHA-256";
    
    
    public static final int  GENERIC_BUFFER_SIZE = 4096;
               
    public static final ExecutorService Executor = Executors.newCachedThreadPool();
    public static final SimpleDateFormat CHECKIN_DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy--HH:mm");    
    
    //Session's start date/time...
    public static final String FORMAT_SessionDateTime
    = new StringBuilder( "yyyyMMdd" ).append( "_" ).append( "HHmmss" ).toString();
            
    public static final String EXPORT_TO_BUNDLE = "Export";
    public static final String IMPORT_BUNDLE = "Import";

    public static final String SAVE_ALL = "Save All";
    public static final String CANCEL = "Cancel";
    
    public static final String ACTION_Run = "Run";
    public static final String ACTION_Add = "Add Script";
    public static final String ACTION_Remove = "Remove";
    public static final String ACTION_Rename = "Rename";
    
    public final static String ACTION_SLEEP = "Sleep"; 
    public final static String ACTION_RELOAD = "Reload"; 
    public final static String ACTION_MIGRATE = "Migrate"; 
    public final static String ACTION_UNINSTALL = "Uninstall"; 
    public static final String ACTION_REMOVE = "Remove";
    public static final String ACTION_CREATE_RELAY = "Create Relay";
    public static final String ACTION_UPGRADE = "Upgrade";
    public static final String ACTION_REMOVE_RELAY = "Remove Relay";
    
    //Stdout & Stderr file names
    public static final String STD_OUT_FILENAME = "stdout.txt";
    public static final String STD_ERR_FILENAME = "stderr.txt";
    
    //Constants for runnable tasks
    public static final String LastRunResults_Completed = "Completed";
    public static final String LastRunResults_Cancelled = "Cancelled";
    public static final String LastRunResults_ErrorOccurred = "Error Occurred";
    public static final String LastRunResults_Failed = "Failed";
    public static final String LastRunResults_Running = "Running...";
    
    //GUI tree icons
//    public static final String NEG_TASK_ICON_STR = "taskRunning.png";
    public static final String JOB_SET_IMAGE_STR = "folder1.png";
    
    public static final String EDITOR_IMG_STR = "brew.png";
//    public static final String SPLASH_IMG_STR = "LogoImage.png";
    
    public final static String DELETE = "Delete";
    
    //Panel pictures
    public static final String OVERVIEW_IMG_STR = "information.png";
    public static final String SCHED_IMG_STR = "schedule.png";
    public static final String TERM_IMG_STR = "terminal.png";
    public static final String PARAM_IMG_STR = "paramIcon.png";
       
    //Toolbar pictures
    public static final String PLAY_IMG_STR = "play.png";
    public static final String STOP_IMG_STR = "stop.png";
    public static final String SAVE_IMG_STR = "save.png";
    public static final String DELETE_IMG_STR = "delete_icon.png";
    public static final String ADD_IMAGE_STR = "add.png";
    public static final String UPLOAD_IMG_STR = "upload.png";
    public static final String DOWNLOAD_IMG_STR = "download.png";
      
    //Manual
    public static final String Manual_Name = "User_Manual.pdf";
    
    //Shell encoding
    public static String SHELL_ENCODING = "US-ASCII";
    
    //NIO socket ports
    public static final int COMM_PORT = 443;
    
    public static final String IMAGE_PATH_IN_JAR= "pwnbrew/images";   
    public static final String PROP_FILE ="META-INF/MANIFEST.MF";
    
    public static final String UID_LABEL = "UID";
    public static final String PAYLOAD_VERSION_LABEL = "Version";
    public static final String STAGER_URL = "Private";
    public static String CURRENT_VERSION = "1.1.0.1";
    
    public static String FILE_UPLOAD = "FILE_UPLOAD";
    public static String FILE_DOWNLOAD = "FILE_DOWNLOAD";
    
    //Constants for process streams
    public static final int STD_OUT_ID = 41;
    public static final int STD_ERR_ID = 42;
    
    public static final SimpleDateFormat SHELL_DATE_FORMAT = new SimpleDateFormat("MM-dd-yyyy--HHmm");    
   
    public static final String MODULE_TYPE_LABEL = "Type";
    public static final String PAYLOAD_ACTION_COMMAND = "Payload";
    public static final String STAGER_ACTION_COMMAND = "Stager";
    
    public static final String PAYLOAD_ALIAS_TYPE = "plib";
    public static final String STAGER_ALIAS_TYPE = "slib";
    public static final String LOCAL_ALIAS_EXTENSION_TYPE = "llib";
    public static final String REMOTE_ALIAS_EXTENSION_TYPE = "rlib";
    
    public static final String PAYLOAD_TYPE = "PAYLOAD";
    public static final String STAGER_TYPE = "STAGER";
    public static final String LOCAL_EXTENSION_TYPE = "LOCAL EXTENSION";
    public static final String REMOTE_EXTENSION_TYPE = "REMOTE EXTENSION";
    
}
