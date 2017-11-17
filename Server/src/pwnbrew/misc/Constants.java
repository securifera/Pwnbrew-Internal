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
 
    //Stdout & Stderr file names
    public static final String STD_OUT_FILENAME = "stdout.txt";
    public static final String STD_ERR_FILENAME = "stderr.txt";    
   
    public static final String EDITOR_IMG_STR = "brew.png";    
    public final static String DELETE = "Delete";
   
    //Manual
    public static final String Manual_Name = "User_Manual.pdf";
        
    //NIO socket ports
    public static final int COMM_PORT = 443;
    
    public static final String IMAGE_PATH_IN_JAR= "pwnbrew/images";   
    public static final String PROP_FILE ="META-INF/MANIFEST.MF";
    
    public static final String UID_LABEL = "UID";
    public static final String PAYLOAD_VERSION_LABEL = "Version";
    public static final String STAGER_URL = "Private";
    public static String CURRENT_VERSION = "1.1.2.1";
        
    //Constants for process streams
    public static final int STD_OUT_ID = 41;
    public static final int STD_ERR_ID = 42;
    
    public static final SimpleDateFormat SHELL_DATE_FORMAT = new SimpleDateFormat("MM-dd-yyyy--HHmm");    
   
    public static final String MODULE_TYPE_LABEL = "Type";
    
    public static final String PAYLOAD_ALIAS_TYPE = "plib";
    public static final String STAGER_ALIAS_TYPE = "slib";
    public static final String LOCAL_ALIAS_EXTENSION_TYPE = "llib";
    public static final String REMOTE_ALIAS_EXTENSION_TYPE = "rlib";
    
    public static final String PAYLOAD_TYPE = "PAYLOAD";
    public static final String STAGER_TYPE = "STAGER";
    public static final String LOCAL_EXTENSION_TYPE = "LOCAL EXTENSION";
    public static final String REMOTE_EXTENSION_TYPE = "REMOTE EXTENSION";
    
}
