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

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import pwnbrew.fileoperation.TaskManager;
import pwnbrew.log.LoggableException;
import pwnbrew.manager.DataManager;
import pwnbrew.misc.DebugPrinter;
import pwnbrew.misc.ProgressListener;
import pwnbrew.misc.Utilities;
import pwnbrew.network.control.messages.PushFile;
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
    private long fileSize;
    
    //Send update to the progress listener
    private final ProgressListener theListener;
    
    //For progress
    private int sndFileProgress = 0;                
    private final int srcHostId;
    private final int taskId;
    private final int fileId;
    private int channelId = 0;
//    private final String fileHash;
    private final boolean compressed;
    private ByteBuffer compBB;
    
    private FileOutputStream aFileStream = null;
//    private MessageDigest fileDigest = null;
        
    private final FileMessageManager theFileMessageManager;
    
    private static final String NAME_Class = FileReceiver.class.getSimpleName();

      //===========================================================================
    /**
     * 
     *  Constructor
     * 
     * @param passedManager
     * @param passedMsg
     * @param parentDir 
     * @throws pwnbrew.log.LoggableException 
     * @throws java.security.NoSuchAlgorithmException 
     * @throws java.io.IOException 
     */
    public FileReceiver( FileMessageManager passedManager, PushFile passedMsg, File parentDir ) 
            throws LoggableException, NoSuchAlgorithmException, IOException {
        
        theFileMessageManager = passedManager;
        taskId = passedMsg.getTaskId();
        fileId = passedMsg.getFileId();
        fileSize = passedMsg.getFileSize();
        srcHostId = passedMsg.getSrcHostId();
        channelId = passedMsg.getFileChannelId();
        compressed = passedMsg.useCompression();

        
        //Get file hash
        String hashFilenameStr = passedMsg.getHashFilenameString();
        
        //Set the hash
        String[] fileHashFileNameArr = hashFilenameStr.split(":", 2);
        if( fileHashFileNameArr.length != 2 )
            throw new LoggableException("Passed hash filename string is not correct.");
            
//        fileHash = fileHashFileNameArr[0];
      
        //Create the file digest
//        fileDigest = MessageDigest.getInstance(Constants.HASH_FUNCTION);
        
        //Ensure the parent directory exists
        Utilities.ensureDirectoryExists(parentDir);
        
        //create byte buffer
        if( compressed )
            compBB = ByteBuffer.allocate((int)fileSize + 256);

        String filePath = new File( fileHashFileNameArr[1] ).getName();        
        fileLoc = new File( parentDir,  filePath);
//        if(fileLoc.exists()){
//
//            //If file already exists and the hash is the same
//            String localFileHash = Utilities.getFileHash(fileLoc);
//            if( !localFileHash.equals(fileHash) && !fileLoc.delete()){
//                cleanupFileTransfer();
//                throw new LoggableException("File already exists, the hash does not match, and was unable to remove it.");
//            }
//
//        }

        //Open the file stream
        aFileStream = new FileOutputStream(fileLoc, true);
        
        //Set the progress listener
        TaskManager theTaskManager = passedManager.getPortManager().getTaskManager();
        theListener = ( theTaskManager != null ? theTaskManager.getProgressListener() : null );
        
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

    synchronized void receiveFile(byte[] passedByteArray){

        try {

//            if( fileByteCounter == 0 ){
//                //Check the header
//                byte[] fileHeader = Utilities.getFileHeader();
//                if( passedByteArray.length >= fileHeader.length){
//
//                    byte[] firstBytes = Arrays.copyOf(passedByteArray, fileHeader.length);
//                    if( Arrays.equals(firstBytes, fileHeader)){
//                        fileByteCounter += fileHeader.length;
//                        fileDigest.update(firstBytes);
//                        passedByteArray = Arrays.copyOfRange(passedByteArray, 4, passedByteArray.length);
//                    }
//
//                }
//            }
            
            if( passedByteArray.length + fileByteCounter > fileSize ){
                int diff = (int) (fileSize - fileByteCounter);
                passedByteArray = Arrays.copyOf(passedByteArray, diff);
            }

            //Copy over the bytes
            if( passedByteArray.length > 0 ){
                if( compressed ){
                    compBB.put(passedByteArray);
                } else {
                    aFileStream.write(passedByteArray);
                }
            }
            
            fileByteCounter += passedByteArray.length;
//            fileDigest.update(passedByteArray);
//            DebugPrinter.printMessage(this, "Receiving file, bytes: " + fileByteCounter);
            
            //Calculate the progress
            //Check for divide by zero
            if(theListener != null){
                
                int tempProgressInt;
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
                
                //Make sure to set the progress to 100
                if(theListener != null){
                    theListener.progressChanged(taskId, 100);
                    sndFileProgress = 100;
                }
                
                //If compressed
                if( compressed ){
                    
                    //Convert bytebuffer to array
                    byte[] compFileByteArr = new byte[ compBB.position() ];
                                        
                    compBB.flip();
                    compBB.get( compFileByteArr, 0, compFileByteArr.length ); 
                    compBB.clear();
                    
                    byte[] byteCopy = Arrays.copyOf(compFileByteArr, compFileByteArr.length);
                    
                    //Make the array into a stream                   
                    ByteArrayInputStream bais = new ByteArrayInputStream( compFileByteArr );
                    Inflater inflater = new Inflater();
                    
                    try ( //Unzip and write to file
                        InflaterInputStream iis = new InflaterInputStream(bais, inflater)) {
                        byte[] buffer = new byte[32768];
                        int len;
                        while((len = iis.read(buffer)) > 0){
                            aFileStream.write(buffer, 0, len);
                        }
                        aFileStream.flush();
                        
                    } catch(EOFException ex){
                        int i = 0;
                    }
                    aFileStream.close();
                    
                }

//                //Get the hash and reset it
//                byte[] byteHash = fileDigest.digest();
//                String hexString = Utilities.byteToHexString(byteHash);
//
//                if( !fileHash.equals("0") && !hexString.equals(fileHash)){
//                    DebugPrinter.printMessage( NAME_Class, "receiveFile", "Calculated file hash does not match the hash provided.", null);
//                }

//                DebugPrinter.printMessage( getClass().getSimpleName(), "Received File.");

                //Get the msg Id before clearing all values
                cleanupFileTransfer();

//                Send fin message to host
                PushFileFin finMessage = new PushFileFin( taskId, "", srcHostId, channelId );
                DataManager.send(theFileMessageManager.getPortManager(), finMessage);
                    

                //Remove from the parent map
                theFileMessageManager.removeFileReceiver( fileId );
                
                //Notify any functions that may be waiting
                theFileMessageManager.fileReceiveComplete( taskId, fileLoc );
                

            }


        } catch (IOException ex) {

             DebugPrinter.printMessage( NAME_Class, "receiveFile",ex.getMessage(), ex );

             //Clear all the file transfer variables
             cleanupFileTransfer();

        }

    }
    
    //===========================================================================
    /**
     * 
     * @param passedSize 
     */
    public void updateFileSize( long passedSize ){
        if( passedSize != fileSize ){
            fileSize = passedSize;
            receiveFile(new byte[0]);
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
    
    //===============================================================
    /**
     * 
     * @return 
     */
    public int getChannelId() {
        return channelId;
    }

}
