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
* PushFile.java
*
* Created on June 7, 2013, 7:22:43 PM
*/

package pwnbrew.network.control.messages;

import pwnbrew.logging.Log;
import pwnbrew.logging.LoggableException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.logging.Level;
import pwnbrew.manager.PortManager;
import pwnbrew.misc.DebugPrinter;
import pwnbrew.utilities.SocketUtilities;
import pwnbrew.network.ControlOption;
import pwnbrew.network.file.FileMessageManager;
import pwnbrew.tasks.RemoteTask;
import pwnbrew.tasks.TaskManager;


/**
 *
 *  
 */
public class PushFile extends FileMessage {
    
    private static final byte OPTION_DATASIZE = 4;
    private static final byte OPTION_FILE_TYPE = 10;    
    public static final byte OPTION_REMOTE_DIR = 12;
    
    private String hashFilenameStr;
    private long fileSize = 0;
    protected int fileType = -1;
    
    public static final int JOB_SUPPORT = 0;
    public static final int JOB_RESULT = 1;
    public static final int FILE_UPLOAD = 2;
    public static final int FILE_DOWNLOAD = 3;
    public static final int JAR_UPLOAD = 4;
    public static final int CERT_UPLOAD = 5;
    public static final int JAR_DOWNLOAD = 6;
  
     //Class name
    private static final String NAME_Class = PushFile.class.getSimpleName();
    
    // ==========================================================================
    /**
     * Constructor
     *
     * @param taskId
     * @param dstHostId
     * @param fileHashNameStr
     * @param passedType
     * @param passedLength
     * @throws java.io.IOException
    */
    public PushFile(int taskId, String fileHashNameStr, long passedLength, int passedType, int dstHostId ) throws IOException {
        super(taskId ,dstHostId );

        byte[] tempArr = fileHashNameStr.getBytes("US-ASCII");
        ControlOption aTlv = new ControlOption( OPTION_HASH_FILENAME, tempArr);
        addOption(aTlv);

        fileSize = passedLength;
        tempArr = SocketUtilities.longToByteArray(fileSize);
        aTlv = new ControlOption( OPTION_DATASIZE, tempArr);
        addOption(aTlv);
        
        fileType = passedType;
        tempArr = SocketUtilities.intToByteArray(fileType);
        aTlv = new ControlOption( OPTION_FILE_TYPE, tempArr);
        addOption(aTlv);
    }
      // ==========================================================================
    /**
     * Constructor
     *
     * @param taskId
     * @param dstHostId
     * @throws pwnbrew.logging.LoggableException
    */
    public PushFile( int taskId , int dstHostId ) throws LoggableException { // NO_UCD (use default)
        super( taskId, dstHostId );
    }
    // ==========================================================================
    /**
     * Constructor
     *
     * @param passedId
    */
    public PushFile( byte[] passedId ) { // NO_UCD (use default)
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
                         hashFilenameStr = new String( theValue, "US-ASCII");
                        break;
                    case OPTION_DATASIZE:
                        fileSize = SocketUtilities.byteArrayToLong( theValue );
                        break; 
                    case OPTION_FILE_TYPE:
                        fileType = SocketUtilities.byteArrayToInt( theValue );
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
     * Returns a file reference specifying the local file name.
     *
     * @return
     */
    public String getHashFilenameString() {
       return hashFilenameStr;
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
          String[] hashFileNameArr = hashFileNameStr.split(":",2);
          fileNameToSend = hashFileNameArr[1];
       }
       
       return fileNameToSend;
    }

    //===============================================================
    /**
     *
     * @return
     */
    public long getFileSize() {
        return fileSize;
    }
    
    //===============================================================
    /**
     * Returns the file type.
     *
     * @return
     */
    public int getFileType() {
       return fileType;
    }

     //===============================================================
    /**
     *  Adds an option to the list
     *
     * @param passedObject
     */
    @Override
    public void addOption(ControlOption passedObject) {
        optionList.add(passedObject);
    }

    /**
    * Indicates whether some other object is "equal to" this one.
    * <p>
    * This method provides a default override implementation of {@link Object#equals(Object)}
    *
    * @param passedObj   the reference object with which to compare
    *
    * @return  {@code true} if this object is the same as the obj argument; {@code false} otherwise
    *
    */
    @Override
    public boolean equals( Object passedObj ) {

        if(passedObj != null && passedObj instanceof PushFile){

            PushFile passedMessage = (PushFile)passedObj;

            String hashFileStr = getHashFilenameString();
            String otherHFStr = passedMessage.getHashFilenameString();

            if(Arrays.equals(msgId, passedMessage.msgId ) &&
                    hashFileStr.equals(otherHFStr) ){
                return true;
            }       
        }

        return false;
    }

    //=========================================================================
    /**
     * 
     * @return 
     */
    @Override
    public int hashCode() {
        int hash = 3;
        return hash;
    }
  
  //===============================================================
    /**
    *   Performs the logic specific to the message.
    *
     * @param passedManager
    */
    @Override
    public void evaluate( PortManager passedManager ) {
                
        try {
            
            if( fileType == PushFile.JOB_RESULT){  
                TaskManager aMgr = passedManager.getTaskManager();
                if( aMgr != null )
                    aMgr.taskChanged(new TaskStatus( getTaskId(), RemoteTask.TASK_XFER_RESULTS, -1 ));
            }
            
             
            //Get the file manager
            FileMessageManager theFileMM = FileMessageManager.getFileMessageManager();
            if( theFileMM == null ){
                theFileMM = FileMessageManager.initialize( passedManager );
            }
            
            DebugPrinter.printMessage(  getClass().getSimpleName(), "Received push file.");
            theFileMM.prepFilePush( this );

        } catch (LoggableException | IOException ex) {
            Log.log(Level.INFO, NAME_Class, "evaluate()", ex.getMessage(), ex );
        }
        
    }

}/* END CLASS PushFile */
