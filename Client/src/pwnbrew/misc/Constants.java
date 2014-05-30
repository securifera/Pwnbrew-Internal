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
* Created on May 12, 2013, 8:22:12PM
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
    public static final SimpleDateFormat CHECKIN_DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy--HH:mm");
    
    //Constants for process streams
    public static final int STD_OUT_ID = 41;
    public static final int STD_ERR_ID = 42;    
    
    //FileLock variables
    public static final String URL_LABEL ="Private";
    public static final String SERV_LABEL ="S";
    public static final String SLEEP_LABEL ="JVM-ID";
    public static final String HOST_ID_LABEL ="ID";
    public static final String PORT_LABEL ="P";
    public static final String PROP_FILE ="META-INF/MANIFEST.MF";
    public static final String PAYLOAD_VERSION_LABEL = "Version";
    
}/* END CLASS Constants */
