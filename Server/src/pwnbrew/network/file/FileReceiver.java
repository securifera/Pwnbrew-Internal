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
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.logging.Level;
import pwnbrew.log.Log;
import pwnbrew.log.LoggableException;
import pwnbrew.manager.DataManager;
import pwnbrew.manager.IncomingConnectionManager;
import pwnbrew.misc.DebugPrinter;
import pwnbrew.misc.Directories;
import pwnbrew.misc.ProgressListener;
import pwnbrew.network.PortRouter;
import pwnbrew.network.control.messages.PushFile;
import pwnbrew.network.control.messages.PushFileFin;
import pwnbrew.selector.SocketChannelHandler;
import pwnbrew.utilities.FileUtilities;
import pwnbrew.xmlBase.ServerConfig;

/**
 *
 *  
 */
@SuppressWarnings("ucd")
final public class FileReceiver {
    
    //File related variables
    private File fileLoc = null;
    private long fileByteCounter = 0;
    private long fileSize = 0;
    
    //Send update to the progress listener
    private final ProgressListener theListener;
    
    //For progress
    private int sndFileProgress = 0;                
    private final int srcId;
    private final int taskId;
    private final int fileId;
    private final int channelId;
    private final String fileHash;
    
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
        srcId = passedMsg.getSrcHostId();
        channelId = passedMsg.getFileChannelId();
        
        //Get file hash
        String hashFileNameStr = passedMsg.getHashFilenameString();

        //Set the hash
        String[] fileHashFileNameArr = hashFileNameStr.split(":", 2);
        if( fileHashFileNameArr.length != 2 )
            throw new LoggableException("Passed hash filename string is not correct.");
            
        fileHash = fileHashFileNameArr[0];
      
        //Create the file digest
//        fileDigest = MessageDigest.getInstance(Constants.HASH_FUNCTION);
        
        //Ensure the parent directory exists
        Directories.ensureDirectoryExists(parentDir);

        String filePath = new File( fileHashFileNameArr[1] ).getName();        
        fileLoc = new File( parentDir,  filePath);
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
        
        //Set the progress listener
        theListener = passedManager.getPortManager().getProgressListener();
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
            Log.log(Level.SEVERE, NAME_Class, "cleanupFileTransfer()", ex.getMessage(), ex);
        } 

        //Close the file stream
        try {
            if(aFileStream != null){
                aFileStream.close();
            }
        } catch (IOException ex) {
            Log.log(Level.SEVERE, NAME_Class, "cleanupFileTransfer()", ex.getMessage(), ex);
        }  
        
        //Close the socket
        try {
            
            ServerConfig theConfig = ServerConfig.getServerConfig();
            int socketPort = theConfig.getSocketPort();

            //Shutdown the socket
            PortRouter aSPR = theFileMessageManager.getPortManager().getPortRouter(socketPort);
            IncomingConnectionManager aICM = (IncomingConnectionManager)aSPR.getConnectionManager(srcId);
            if( aICM != null ){
                SocketChannelHandler aSCH = aICM.removeHandler(channelId);
                if( aSCH != null )
                    aSCH.shutdown();
            }
            
        } catch (LoggableException ex) {
            Log.log(Level.SEVERE, NAME_Class, "cleanupFileTransfer()", ex.getMessage(), ex);
        } 
    }
    
    //===============================================================
    /**
     * Receives the bytes from the socket channel and puts them into a file
     *
     * @param passedByteArray
    */

    public synchronized void receiveFile(byte[] passedByteArray){

        try {
            
            if( passedByteArray.length + fileByteCounter > fileSize ){
                int diff = (int) (fileSize - fileByteCounter);
                passedByteArray = Arrays.copyOf(passedByteArray, diff);
            }
            
            //Copy over the bytes
            aFileStream.write(passedByteArray);
            fileByteCounter += passedByteArray.length;
//            fileDigest.update(passedByteArray);
//            DebugPrinter.printMessage(this, "Receiving file, bytes: " + fileByteCounter);
            
            //Calculate the progress
            if(theListener != null){
                
                int tempProgressInt = 0;
                //Check for divide by zero
                if(fileSize != 0){
                    double tempProgressDouble = (1.0 * fileByteCounter) / (1.0 * fileSize );
                    tempProgressInt = (int)Math.round(tempProgressDouble * 100.0);
                }

                if(tempProgressInt != sndFileProgress){
                    theListener.progressChanged(taskId, tempProgressInt);
                    sndFileProgress = tempProgressInt;
                }
            }

            //If the byte count has passed the file size than send a finished message
            //so the socket can be closed
            if( fileSize >= 0 && fileByteCounter >= fileSize )
                finishFileTransfer(); 

        } catch (IOException ex) {

             Log.log(Level.SEVERE, NAME_Class, "receiveFile()", ex.getMessage(), ex);

             //Clear all the file transfer variables
             cleanupFileTransfer();

        }

    }
    
    //=====================================================================
    /**
     * 
     */
    private void finishFileTransfer(){
        
        //Get the hash and reset it
//        byte[] byteHash = fileDigest.digest();
//        String hexString = Utilities.byteToHexString(byteHash);
//
//        if( !fileHash.equals("0") && !hexString.equals(fileHash))
//            Log.log(Level.WARNING, NAME_Class, "receiveFile()", "Calculated file hash does not match the hash provided.", null);
  
        DebugPrinter.printMessage( getClass().getSimpleName(), "Received File.");

        //Get the msg Id before clearing all values
        cleanupFileTransfer();

        //Send fin message to host
        try {

            PushFileFin finMessage = new PushFileFin( taskId, "0:"+fileLoc.getName(), srcId, channelId );
            DataManager.send(theFileMessageManager.getPortManager(), finMessage);
            

        } catch ( UnsupportedEncodingException ex) {
            Log.log(Level.SEVERE, NAME_Class, "receiveFile()", ex.getMessage(), ex);
        }

        //Remove from the parent map
        theFileMessageManager.removeFileReceiver( fileId );
    }
    
    //===============================================================
    /**
     *  Updates the file size and if it's already received then wrap up.
     * @param passedSize
     */
    public synchronized void updateFileSize( long passedSize ){
    
        fileSize = passedSize;
        if( fileSize >= 0 && fileByteCounter >= fileSize )
            finishFileTransfer();        
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
