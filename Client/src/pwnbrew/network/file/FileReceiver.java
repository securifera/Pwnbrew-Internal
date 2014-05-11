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
 *  FileReceiver.java
 *
 *  Created on Jun 3, 2013
 */

package pwnbrew.network.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import pwnbrew.Persistence;
import pwnbrew.log.RemoteLog;
import pwnbrew.log.LoggableException;
import pwnbrew.manager.CommManager;
import pwnbrew.misc.Constants;
import pwnbrew.misc.DebugPrinter;
import pwnbrew.misc.FileUtilities;
import pwnbrew.misc.Utilities;
import pwnbrew.network.control.ControlMessageManager;
import pwnbrew.network.control.messages.PushFileFin;
import pwnbrew.network.control.messages.TaskProgress;
import pwnbrew.task.TaskListener;

/**
 *
 *  
 */
final public class FileReceiver {
    
    //File related variables
    private File fileLoc = null;
    private long fileByteCounter = 0;
    private long fileSize = 0;
    
    private int sndFileProgress = 0;  
    private int srcHostId = 0;
    private int taskId = 0;
    private int fileId = 0;
    private String fileHash = null;
    
    private FileOutputStream aFileStream = null;
    private MessageDigest fileDigest = null;
    
    private final FileMessageManager theFileMessageManager;
    
    private static final String NAME_Class = FileReceiver.class.getSimpleName();

    //===========================================================================
    /**
     * 
     *  Constructor
     * 
     * @param passedManager
     * @param passedSrcId
     * @param passedTaskId
     * @param passedFileId
     * @param passedFileSize
     * @param parentDir
     * @param hashFilenameStr 
     * @throws pwnbrew.log.LoggableException 
     * @throws java.io.IOException 
     * @throws java.security.NoSuchAlgorithmException 
     */
    public FileReceiver( FileMessageManager passedManager, int passedSrcId, int passedTaskId, int passedFileId, long passedFileSize, File parentDir, String hashFilenameStr) 
            throws LoggableException, NoSuchAlgorithmException, IOException {
        
        theFileMessageManager = passedManager;
        srcHostId = passedSrcId;
        taskId = passedTaskId;
        fileId = passedFileId;
        fileSize = passedFileSize;

        //Set the hash
        String[] fileHashFileNameArr = hashFilenameStr.split(":", 2);
        if( fileHashFileNameArr.length != 2 )
            throw new LoggableException("Passed hash filename string is not correct.");
      
        fileHash = fileHashFileNameArr[0];
      
        //Create the file digest
        fileDigest = MessageDigest.getInstance(Constants.HASH_FUNCTION);
        
        //Ensure the parent directory exists
        Persistence.ensureDirectoryExists(parentDir);

        String filePath = fileHashFileNameArr[1];
        String fileName = filePath.substring( filePath.lastIndexOf("\\") + 1 );
        fileLoc = new File( parentDir, fileName );
        if(fileLoc.exists()){

            //If file already exists and the hash is the same
            String localFileHash = FileUtilities.getFileHash(fileLoc);
            if( !localFileHash.equals(fileHash) && !fileLoc.delete()){
                cleanupFileTransfer();
                throw new LoggableException("File already exists, the hash does not match, and was unable to remove it.");
            }

        }

        //Open the file stream
        aFileStream = new FileOutputStream(fileLoc, true);
    }
    
    //===============================================================
    /**
    * Cleans up the file transfer
    *
    */
    public void cleanupFileTransfer(){

        try {

            //Close the file channell
            if(aFileStream != null){
               aFileStream.getFD().sync();
               aFileStream.flush();
            }

        } catch (IOException ex) {
            ex = null;
        } 

        //Close the file stream
        try {
            if(aFileStream != null){
                aFileStream.close();
            }
        } catch (IOException ex) {
            ex = null;
        }  
    }
    
    //===============================================================
    /**
     * Receives the bytes from the socket channel and puts them into a file
     *
     * @param passedByteArray
    */

    public void receiveFile(byte[] passedByteArray){

        try {

            //Copy over the bytes
            aFileStream.write(passedByteArray);
            fileByteCounter += passedByteArray.length;
            fileDigest.update(passedByteArray);
//            DebugPrinter.printMessage(NAME_Class, "Receiving file, bytes: " + fileByteCounter);
            
            //Returns if it should unlock or not
            ControlMessageManager theCMM = ControlMessageManager.getControlMessageManager();
                                    
            int tempProgressInt = 0;
            if(fileSize != 0){
                double tempProgressDouble = (1.0 * fileByteCounter) / (1.0 * fileSize );
                tempProgressInt = (int)Math.round(tempProgressDouble * 100.0);
            }

            if(tempProgressInt != sndFileProgress){
                
                //Send a progress update
                sndFileProgress = tempProgressInt;
                if( theCMM != null ){                    
                    try {
                        TaskProgress aProgMsg = new TaskProgress(taskId, sndFileProgress );
                        aProgMsg.setDestHostId(srcHostId);
                        theCMM.send(aProgMsg);
                    }  catch (IOException ex) {
                        RemoteLog.log(Level.SEVERE, NAME_Class, "receiveFile()", ex.getMessage(), ex);
                    }
                   
                }
            }            
            
            //If the byte count has passed the file size than send a finished message
            //so the socket can be closed
            if(fileByteCounter >= fileSize){

                //Get the hash and reset it
                byte[] byteHash = fileDigest.digest();
                String hexString = Utilities.byteToHexString(byteHash);

                if( !fileHash.equals("0") && !hexString.equals(fileHash)){
                    RemoteLog.log(Level.WARNING, NAME_Class, "receiveFile()", "Calculated file hash does not match the hash provided.", null);
                }

                DebugPrinter.printMessage( this.getClass().getSimpleName(), "Received File.");

                //Get the msg Id before clearing all values
                cleanupFileTransfer();

                //Notify any task handlers waiting
                CommManager theManager = theFileMessageManager.getCommManager();
                if( theManager instanceof TaskListener ){
                    ((TaskListener)theManager).notifyHandler(taskId, Constants.FILE_RECEIVED);
                }
                
                //Send fin message to host
                try {
                    
                    PushFileFin finMessage = new PushFileFin( taskId, fileId, hexString ); 
                    finMessage.setDestHostId(srcHostId);
                    //Returns if it should unlock or not
                    if( theCMM != null ){
                        theCMM.send(finMessage);
                    }
                    
                } catch ( UnsupportedEncodingException ex) {
                    RemoteLog.log(Level.SEVERE, NAME_Class, "receiveFile()", ex.getMessage(), ex);
                }

                //Remove from the parent map
                theFileMessageManager.removeFileReceiver( fileId );
                
            }            
     
        } catch (IOException ex) {

             RemoteLog.log(Level.SEVERE, NAME_Class, "receiveFile()", ex.getMessage(), ex);

             //Clear all the file transfer variables
             cleanupFileTransfer();

        }

    }

    //===============================================================
    /**
     *  Returns the file id
     * 
     * @return 
     */
    public int getTaskId() {
        return taskId;
    }

}
