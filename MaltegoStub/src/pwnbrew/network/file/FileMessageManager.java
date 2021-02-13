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
 *  FileMessageManager.java
 *
 */

package pwnbrew.network.file;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import pwnbrew.MaltegoStub;
import pwnbrew.StubConfig;
import pwnbrew.functions.Function;
import pwnbrew.log.LoggableException;
import pwnbrew.manager.DataManager;
import pwnbrew.manager.PortManager;
import pwnbrew.misc.Constants;
import pwnbrew.misc.DebugPrinter;
import pwnbrew.network.PortRouter;
import pwnbrew.network.control.ControlMessageManager;
import pwnbrew.network.control.messages.PushFile;
import pwnbrew.network.control.messages.PushFileAck;
import pwnbrew.selector.SocketChannelHandler;

/**
 *
 *  
 */
public class FileMessageManager extends DataManager {

    private static FileMessageManager theFileManager;
    private final Map<Integer, FileReceiver> fileId_FileReceiverMap = new HashMap<>();
    private final Map<Integer, Map<Integer, FileSender>> taskId_fileId_FileSenderMap = new HashMap<>();
    
    private final AtomicInteger retChannelId = new AtomicInteger(Constants.CHANNEL_DISCONNECTED);
       
    private static final String NAME_Class = FileMessageManager.class.getSimpleName();
  
    //===========================================================================
    /*
     *  Constructor
     */
    private FileMessageManager( PortManager passedCommManager ) {
        
        super(passedCommManager); 
         
        FileHandler theFileHandler = new FileHandler( this );
        theFileHandler.start();
       
        //Set the data handler
        setDataHandler(theFileHandler);
    }  
    
    // ==========================================================================
    /**
     *   Gets the FileMessageManager
     * @return 
     */
    public synchronized static FileMessageManager getFileMessageManager(){
        if( theFileManager == null )
            theFileManager = new FileMessageManager( MaltegoStub.getMaltegoStub() );
        return theFileManager;
    }
    
    //===============================================================
    /**
     *   Send the message out the given channel.
     *
     * @param msgBytes
    */
    @Override
    public void handleMessage( byte[] msgBytes ) {        
        theFileManager.getDataHandler().processData(msgBytes);        
    }
    
     //===========================================================================
    /*
     *  Returns the data handler
    */
    @Override
    public FileHandler getDataHandler() {
        return (FileHandler)theDataHandler;
    }  
    
    //===============================================================
    /**
    * Sets up for a file transfer
    *
    * @param fileToTransfer
    * @return
    */
    private void initFileTransfer( PushFile passedMsg, File parentDir ) throws LoggableException {
            
        //Initialize the file transfer
        int fileId = passedMsg.getFileId();
        synchronized( fileId_FileReceiverMap ){

            FileReceiver theReceiver = fileId_FileReceiverMap.get(fileId);
            //If the receive flag is not set
            if( theReceiver == null ){
                try {
                    theReceiver = new FileReceiver( this, passedMsg, parentDir );
                    fileId_FileReceiverMap.put(fileId, theReceiver);
                } catch (NoSuchAlgorithmException | IOException ex) {
                    throw new LoggableException("Unable to create a new file receiver.");
                }

            } else
                throw new LoggableException("A file receive is already in progress.");
             
        }
        
    }
    
    //===========================================================================
    /**
     *  Get the file receiver
     * 
     * @param passedId
     * @return 
     */
    protected FileReceiver getFileReceiver( int passedId ){
        
        FileReceiver theReceiver;
        synchronized( fileId_FileReceiverMap ){
            theReceiver = fileId_FileReceiverMap.get( passedId );
        }
        return theReceiver;
    }

    //===========================================================================
    /**
     *  Removed the file receiver
     * 
     * @param fileId 
     */
    protected void removeFileReceiver(int fileId) {
        synchronized( fileId_FileReceiverMap ){
            fileId_FileReceiverMap.remove(fileId );
        }
    }
    
     //=========================================================================
    /**
     * 
     * @param passedInt 
     */
    public void setChannelId( int passedInt ){
        retChannelId.set(passedInt);   
    }
    
    //=========================================================================
    /**
     * 
     * @return 
     */
    public int getChannelId(){
        return retChannelId.get();   
    }
    
    //========================================================================
    /**
     * 
     * @param passedMessage
     */
    public void fileDownload( PushFile passedMessage ) {
        
        synchronized( retChannelId ){
            int channelId = getChannelId();
            if( channelId == Constants.CHANNEL_DISCONNECTED)
                setChannelId(passedMessage.getFileChannelId());
        }
        
        try {
            prepFilePush(passedMessage);
         } catch ( IOException | LoggableException ex) {
            DebugPrinter.printMessage( NAME_Class, "evaluate", ex.getMessage(), ex); 
        }
    }
    
    //========================================================================
    /**
     * 
     * @param passedMessage
     */
    public void fileUpload( PushFileAck passedMessage ) {
        
        synchronized( retChannelId ){
            int channelId = getChannelId();
            if( channelId == Constants.CHANNEL_DISCONNECTED)
                setChannelId(passedMessage.getFileChannelId());
        }
        
        sendFile(passedMessage);

    }
    
    //===============================================================
        /**
        * Prepares a file to be sent through a socket channel.
        * 
        * @param passedMessage
        * @return
        * @throws LoggableException 
     * @throws java.io.IOException 
    */
    private boolean prepFilePush( PushFile passedMessage ) throws LoggableException, IOException {

        boolean retVal = false;
        int taskId = passedMessage.getTaskId();
        int fileId = passedMessage.getFileId();        
        int srcId = passedMessage.getSrcHostId();
        int fileChannelId = passedMessage.getFileChannelId();
        
        File libDir = null;
        int fileType = passedMessage.getFileType();
        switch(fileType){
            case PushFile.FILE_DOWNLOAD:
                libDir = thePortManager.getTaskManager().getDownloadDirectory( Integer.toString(taskId) );
                if( libDir == null )
                    return retVal;
                break;
            case PushFile.JAR_DOWNLOAD:
                File aFile = File.createTempFile("tmp", null);
                libDir = aFile.getParentFile();
                break;
        }

        if( libDir != null ){
            //Get the control manager for sending messages
            ControlMessageManager aCMManager = ControlMessageManager.getControlMessageManager();
            if( aCMManager != null ){

                //Get the hash/filename
                String hashFileNameStr = passedMessage.getHashFilenameString();

                //Try to begin the file transfer
                initFileTransfer( passedMessage, libDir );
                //Send an ack to the sender to begin transfer
//                DebugPrinter.printMessage( getClass().getSimpleName(), "prepFilePush", "Sending ACK for " + hashFileNameStr + " channelId: " + fileChannelId, null);
                PushFileAck aSFMA = new PushFileAck(taskId, fileId, fileChannelId, hashFileNameStr, srcId );
                //Set compression flag
                if( passedMessage.useCompression() )
                    aSFMA.enableCompression();
                
                DataManager.send(thePortManager, aSFMA);
                retVal = true;
            }
        }

        return retVal;

    }
    
     //===============================================================
    /**
     *  Cleans up the work performed by the initFileTransfer function.
     * 
     * @param passedFileId
    */
    public void abortFileReceive( int passedFileId ) {
        
        FileReceiver theReceiver;
        synchronized( fileId_FileReceiverMap ){
            theReceiver = fileId_FileReceiverMap.remove(passedFileId );  
        }
        
        //Clean up
        if( theReceiver != null ){
            theReceiver.cleanupFileTransfer();
        }
          
    }

    //===============================================================
    /**
     *  Send the file referenced by the message.
     * 
     * @param aMessage 
     */
    private void sendFile(PushFileAck aMessage) {
        
        FileSender aSender = new FileSender( getPortManager(), aMessage, StubConfig.getConfig().getSocketPort() );
        
        int taskId = aMessage.getTaskId();
        int fileId = aMessage.getFileId();
        
        synchronized( taskId_fileId_FileSenderMap ){
            Map<Integer, FileSender> senderMap = taskId_fileId_FileSenderMap.get(taskId);
            if( senderMap == null){
                senderMap = new HashMap<>();
                taskId_fileId_FileSenderMap.put(taskId, senderMap);
            }
            senderMap.put(fileId, aSender);
        }
        
        aSender.start();
        
    }

    //===============================================================
    /**
     *  Cancel any file transfers with the given taskId
     * 
     * @param clientId
     * @param taskId 
     */
    public void cancelFileTransfer( int clientId, int taskId ) {
        
        synchronized( fileId_FileReceiverMap ){
            
            List<Integer> fileIds = new ArrayList<>(fileId_FileReceiverMap.keySet());
            for( Integer anId : fileIds ){
                
                FileReceiver aReceiver = fileId_FileReceiverMap.get(anId);
                if( aReceiver != null ){
                    int receiverId = aReceiver.getTaskId();
                    if( receiverId == taskId ){
                        aReceiver.cleanupFileTransfer();
                        fileId_FileReceiverMap.remove( anId );
                    }
                }
            }
        }
        
         //Cancel file sends
        synchronized( taskId_fileId_FileSenderMap ){
            
            List<Integer> taskIds = new ArrayList<>(taskId_fileId_FileSenderMap.keySet());
            for( Integer aTaskId : taskIds ){
                
                //Only do specified task id
                if( aTaskId == taskId ){
                    
                    Map<Integer, FileSender> aSenderMap = taskId_fileId_FileSenderMap.get(aTaskId);
                    List<Integer> aFileId = new ArrayList<>(aSenderMap.keySet());
                    for( Integer anId : aFileId ){
                        
                        FileSender aSender = aSenderMap.get(anId);
                        int channelId = aSender.getChannelId();
                        
                        //Shutdown and remove
                        aSender.shutdown();
                        aSenderMap.remove( anId );
                        
                        //Clear the send queue of all fileIds associated with the cancelled sender
                        PortRouter aPR = thePortManager.getPortRouter( StubConfig.getConfig().getSocketPort() );
                        SocketChannelHandler aSCH = aPR.getSocketChannelHandler(channelId);

                        //Remove any packets with the particular file id
                        if( aSCH != null )
                            aSCH.cancelSend(anId);
                    }

                    //Remove from the map
                    taskId_fileId_FileSenderMap.remove(aTaskId);
                }
            }
        }
        
    }

    //===============================================================
    /**
     * 
     * @param fileLoc
     * @param taskId 
     */
    public void fileReceiveComplete( int taskId, File fileLoc) {
        MaltegoStub theStub = MaltegoStub.getMaltegoStub();
        Function aFunc = theStub.getFunction();
        aFunc.fileReceived( taskId, fileLoc );
    }

    //===============================================================
    /**
     * 
     * @param fileId
     * @param fileSize 
     */
    public void updateFileSize(int fileId, long fileSize) {
    
        FileReceiver aFR;
        synchronized( fileId_FileReceiverMap ){
            aFR = fileId_FileReceiverMap.get(fileId);
        }
        
        //Update size
        if( aFR != null )
            aFR.updateFileSize(fileSize);
        
    }
    
}
