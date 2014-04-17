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
 */

package pwnbrew.network.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import pwnbrew.Persistence;
import pwnbrew.log.LoggableException;
import pwnbrew.misc.Constants;
import pwnbrew.misc.DebugPrinter;
import pwnbrew.misc.ProgressListener;
import pwnbrew.misc.Utilities;
import pwnbrew.network.control.ControlMessageManager;
import pwnbrew.network.control.messages.PushFileFin;

/**
 *
 *  
 */
@SuppressWarnings("ucd")
final public class FileReceiver {
    
    //File related variables
    private File fileLoc = null;
    private long fileByteCounter = 0;
    private final long fileSize;
    
    //Send update to the progress listener
    private final ProgressListener theListener;
    
    //For progress
    private int sndFileProgress = 0;                
    private final int clientId;
    private final int taskId;
    private final int fileId;
    private final String fileHash;
    
    private FileOutputStream aFileStream = null;
    private MessageDigest fileDigest = null;
        
    private final FileMessageManager theFileMessageManager;
    
    private static final String NAME_Class = FileReceiver.class.getSimpleName();

    //===========================================================================
    /**
     * 
     *  Constructor
     * 
     * @param passedId
     * @param passedFileId
     * @param passedFileSize
     * @param parentDir
     * @param hashFilenameStr 
     */
    FileReceiver( FileMessageManager passedManager, int passedClientId, int passedTaskId, int passedFileId, 
            long passedFileSize, File parentDir, String hashFilenameStr) 
            throws LoggableException, NoSuchAlgorithmException, IOException {
        
        theFileMessageManager = passedManager;
        taskId = passedTaskId;
        fileId = passedFileId;
        fileSize = passedFileSize;
        clientId = passedClientId;

        //Set the hash
        String[] fileHashFileNameArr = hashFilenameStr.split(":", 2);
        if( fileHashFileNameArr.length != 2 )
            throw new LoggableException("Passed hash filename string is not correct.");
            
        fileHash = fileHashFileNameArr[0];
      
        //Create the file digest
        fileDigest = MessageDigest.getInstance(Constants.HASH_FUNCTION);
        
        //Ensure the parent directory exists
        Persistence.ensureDirectoryExists(parentDir);

        String filePath = new File( fileHashFileNameArr[1] ).getName();        
        fileLoc = new File( parentDir,  filePath);
        if(fileLoc.exists()){

            //If file already exists and the hash is the same
            String localFileHash = Utilities.getFileHash(fileLoc);
            if( !localFileHash.equals(fileHash) && !fileLoc.delete()){
                cleanupFileTransfer();
                throw new LoggableException("File already exists, the hash does not match, and was unable to remove it.");
            }

        }

        //Open the file stream
        aFileStream = new FileOutputStream(fileLoc, true);
        
        //Set the progress listener
        theListener = passedManager.getCommManager().getTaskManager().getProgressListener();
    }
    
    //===============================================================
    /**
    * Cleans up the file transfer
    *
    * @param fileToTransfer
    * @return
    */
    void cleanupFileTransfer(){

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
     * @return
    */

    void receiveFile(byte[] passedByteArray){

        try {

            if( fileByteCounter == 0 ){
                //Check the header
                byte[] fileHeader = Utilities.getFileHeader();
                if( passedByteArray.length >= fileHeader.length){

                    byte[] firstBytes = Arrays.copyOf(passedByteArray, fileHeader.length);
                    if( Arrays.equals(firstBytes, fileHeader)){
                        fileByteCounter += fileHeader.length;
                        fileDigest.update(firstBytes);
                        passedByteArray = Arrays.copyOfRange(passedByteArray, 4, passedByteArray.length);
                    }

                }
            }

            //Copy over the bytes
            aFileStream.write(passedByteArray);
            fileByteCounter += passedByteArray.length;
            fileDigest.update(passedByteArray);
//            DebugPrinter.printMessage(this, "Receiving file, bytes: " + fileByteCounter);
            
            //Calculate the progress
            //Check for divide by zero
            if(theListener != null){
                
                int tempProgressInt = 0;
                if(fileSize != 0){
                    double tempProgressDouble = (1.0 * fileByteCounter) / (1.0 * fileSize );
                    tempProgressInt = (int)Math.round(tempProgressDouble * 100.0);
                } else {
                    tempProgressInt = 100;
                }

                if(tempProgressInt != sndFileProgress){
                    theListener.progressChanged(taskId, tempProgressInt);
                    sndFileProgress = tempProgressInt;
                }
            }

            //If the byte count has passed the file size than send a finished message
            //so the socket can be closed
            if(fileByteCounter >= fileSize){

                //Get the hash and reset it
                byte[] byteHash = fileDigest.digest();
                String hexString = Utilities.byteToHexString(byteHash);

                if( !fileHash.equals("0") && !hexString.equals(fileHash)){
                    DebugPrinter.printMessage( NAME_Class, "receiveFile", "Calculated file hash does not match the hash provided.", null);
                }

//                DebugPrinter.printMessage( getClass().getSimpleName(), "Received File.");

                //Get the msg Id before clearing all values
                cleanupFileTransfer();

                //Send fin message to host
                try {
                    
                    PushFileFin finMessage = new PushFileFin( taskId, hexString, clientId );
                   
                    //Returns if it should unlock or not
                    ControlMessageManager theCMM = ControlMessageManager.getControlMessageManager();
                    if( theCMM != null ){
                        theCMM.send( finMessage );
                    }
                    
                } catch ( UnsupportedEncodingException ex) {
                    DebugPrinter.printMessage( NAME_Class, "receiveFile",ex.getMessage(), ex );
                }

                //Remove from the parent map
                theFileMessageManager.removeFileReceiver( fileId );

            }


        } catch (IOException ex) {

             DebugPrinter.printMessage( NAME_Class, "receiveFile",ex.getMessage(), ex );

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
