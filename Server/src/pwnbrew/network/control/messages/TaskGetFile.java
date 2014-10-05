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
* TaskGetFile.java
*
* Created on June 7, 2013, 6:52:10 PM
*/

package pwnbrew.network.control.messages;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import pwnbrew.logging.Log;
import pwnbrew.manager.CommManager;
import pwnbrew.manager.DataManager;
import pwnbrew.misc.DebugPrinter;
import pwnbrew.misc.Directories;
import pwnbrew.network.ControlOption;
import pwnbrew.tasks.TaskManager;


/**
 *
 *  
 */
@SuppressWarnings("ucd")
public final class TaskGetFile extends TaskStatus{

    private String fileName = null;
    private String fileHash = null;    
    
    static final String TASK_XFER_FILES = "Transferring Files";
     //Class name
    private static final String NAME_Class = TaskGetFile.class.getSimpleName();
     
    // ==========================================================================
    /**
     * Constructor
     *
     * @param taskId
     * @param fileNameHashStr
     * @param dstId
     * @throws java.io.IOException
    */
    public TaskGetFile(int taskId, String fileNameHashStr, int dstId ) throws IOException  {
        super(taskId , TASK_XFER_FILES, dstId );
   
        byte[] strBytes = fileNameHashStr.getBytes("US-ASCII");
        ControlOption aTlv = new ControlOption(OPTION_HASH_FILENAME, strBytes);
        addOption(aTlv);
    }
    
     // ==========================================================================
    /**
     * Constructor
     *
     * @param passedId
    */
    public TaskGetFile(byte[] passedId) {
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
                    case OPTION_HASH_FILENAME:

                        String hashFileNameStr = new String( theValue, "US-ASCII");
                        String[] hashFileNameArr = hashFileNameStr.split(":");
                        fileHash = hashFileNameArr[0];
                        fileName = hashFileNameArr[1]; 
                        break;

                    case TASK_STATUS:
                        taskStatus = new String( theValue, "US-ASCII");
                        break;
                    default:
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
     * Returns a string specifying the local file name.
     *
     * @return
     */
    public String getFileNameToRetrieve() {       
        return fileName;
    }

     //===============================================================
    /**
     * Returns a string specifying the file hash.
     *
     * @return
     */
    public String getHashToRetrieve() {
        return fileHash;
    }
    
    //===============================================================
    /**
    *   Performs the logic specific to the message.
    *
     * @param passedManager
    */
    @Override
    public void evaluate( CommManager passedManager ) {
        
//        try {
//
//            //Alert the manager of the change in the task
//            TaskManager aMgr = passedManager.getTaskManager();
//            if( aMgr != null )
//                aMgr.taskChanged(this);
//
//            File libDir = Directories.getFileLibraryDirectory();
//            String theHash = getHashToRetrieve();
//
//            //Debug
//            DebugPrinter.printMessage( this.getClass().getSimpleName(), "Received TaskGetFile for " + theHash);
//
//            File fileToSend = new File(libDir, theHash);
//            if(fileToSend.exists()){
//
//                //Queue the file to be sent
//                String fileHashNameStr = new StringBuilder().append(fileToSend.getName()).append(":").append(fileToSend.getName()).toString();
//
//                int clientId =  getSrcHostId();
//                PushFile thePFM = new PushFile( getTaskId(), fileHashNameStr, fileToSend.length(), PushFile.JOB_SUPPORT, clientId );
//                DataManager.send(passedManager,thePFM);
//            }
//
//        } catch (IOException ex) {
//            Log.log(Level.INFO, NAME_Class, "evaluate()", ex.getMessage(), ex );
//        }
    }

}
