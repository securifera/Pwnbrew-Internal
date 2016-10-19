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
 *  Created on Jun 2, 2013
 */

package pwnbrew.network.file;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import pwnbrew.ClientConfig;
import pwnbrew.log.LoggableException;
import pwnbrew.log.RemoteLog;
import pwnbrew.manager.ConnectionManager;
import pwnbrew.manager.PortManager;
import pwnbrew.manager.DataManager;
import pwnbrew.utilities.FileUtilities;
import pwnbrew.network.ClientPortRouter;
import pwnbrew.network.PortRouter;
import pwnbrew.network.control.messages.PushFile;
import pwnbrew.network.control.messages.PushFileAck;
import pwnbrew.network.control.messages.TaskGetFile;
import pwnbrew.selector.SocketChannelHandler;
import pwnbrew.utilities.DebugPrinter;

/**
 *
 *  
 */
public class FileMessageManager extends DataManager {

    private static FileMessageManager theFileManager;
    private final Map<Integer, FileReceiver> theFileReceiverMap = new HashMap<>();
    private final Map<Integer, Map<Integer, FileSender>> theFileSenderMap = new HashMap<>();
    
    private static final String NAME_Class = FileMessageManager.class.getSimpleName();    
    private final AtomicInteger retChannelId = new AtomicInteger(ConnectionManager.CHANNEL_DISCONNECTED);
    
    //Queues for tasks while trying to connect
    private final Queue< PushFile > pendingDownloads = new LinkedList<>();
    private final Queue< PushFile > pendingUploads = new LinkedList<>();
  
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
     *   Creates the FileMessageManager
     * @param passedCommManager
     * @return 
     * @throws java.io.IOException 
     * @throws pwnbrew.log.LoggableException 
     */
    public synchronized static FileMessageManager initialize( PortManager passedCommManager ) throws IOException, LoggableException {

        if( theFileManager == null ) {
            theFileManager = new FileMessageManager( passedCommManager );
            createPortRouter( passedCommManager, ClientConfig.getConfig().getSocketPort(), true );
        }
        
        return theFileManager;

    }/* END instantiate() */
    
    // ==========================================================================
    /**
     *   Gets the FileMessageManager
     * @return 
     */
    public synchronized static FileMessageManager getFileMessageManager(){
        return theFileManager;
    }
    
    //===============================================================
    /**
     *   Send the message out the given channel.
     *
     * @param msgBytes
    */
    @Override
    public void handleMessage( PortRouter srcPortRouter, byte[] msgBytes ) {        
        theFileManager.getDataHandler().processData(srcPortRouter, msgBytes);        
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

        //Get the socket router
        ClientConfig theConf = ClientConfig.getConfig();
        int socketPort = theConf.getSocketPort();
        ClientPortRouter aPR = (ClientPortRouter) thePortManager.getPortRouter( socketPort );
                       
        //Initiate the file transfer
        if(aPR != null){
                   
            int fileId = passedMsg.getFileId();
            //Initialize the file transfer
            synchronized( theFileReceiverMap ){
                FileReceiver theReceiver = theFileReceiverMap.get(fileId);
                //If the receive flag is not set
                if( theReceiver == null ){

                    try {
                        theReceiver = new FileReceiver( this, passedMsg, parentDir );
                        theFileReceiverMap.put(fileId, theReceiver);
                    } catch (NoSuchAlgorithmException | IOException ex) {
                        throw new LoggableException("Unable to create a new file receiver.");
                    }

                } else {
                    throw new LoggableException("A file receive is already in progress.");
                } 
            }
            
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
        synchronized( theFileReceiverMap ){
            theReceiver = theFileReceiverMap.get( passedId );
        }
        return theReceiver;
    }

    //===========================================================================
    /**
     *  Removed the file receiver
     * 
     * @param fileId 
     * @param channelId 
     */
    protected void removeFileReceiver( int fileId, int channelId) {
        synchronized( theFileReceiverMap ){
            theFileReceiverMap.remove(fileId );
            if( theFileReceiverMap.isEmpty() && theFileSenderMap.isEmpty() && channelId == retChannelId.get() ){
                retChannelId.set(ConnectionManager.CHANNEL_DISCONNECTED);
            }
        }
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
    public boolean prepFilePush( PushFile passedMessage ) throws LoggableException, IOException {

        boolean retVal = false;
        int taskId = passedMessage.getTaskId();
        int fileId = passedMessage.getFileId();
        int fileChannelId = passedMessage.getFileChannelId();
        
        DebugPrinter.printMessage(  this.getClass().getSimpleName(), "Prepping file push.");
      
     
        File libDir = null;
        int fileType = passedMessage.getFileType();
        switch(fileType){
            case PushFile.JOB_SUPPORT:
                libDir = FileUtilities.getTempDir();
                break;
            case PushFile.FILE_UPLOAD:
                libDir = new File( passedMessage.getRemoteDir() );
                break;
        }
                
        if( libDir != null ){
                            
            String hashFileNameStr = passedMessage.getHashFilenameString();
            initFileTransfer( passedMessage, libDir );

            //DebugPrinter.printMessage(PortManager.class.getSimpleName(), "Sending ACK for " + hashFileNameStr);
            PushFileAck aSFMA = new PushFileAck(fileChannelId, taskId, fileId, hashFileNameStr);
            aSFMA.setDestHostId( passedMessage.getSrcHostId() );

            //Send the message
            DataManager.send(thePortManager, aSFMA);
            retVal = true;

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
        synchronized( theFileReceiverMap ){
            theReceiver = theFileReceiverMap.remove(passedFileId );  
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
    public void sendFile(PushFileAck aMessage) {
        
        FileSender aSender = new FileSender( getPortManager(), aMessage );
        int taskId = aMessage.getTaskId();
        int fileId = aMessage.getFileId();
                
        synchronized( theFileSenderMap ){
            Map<Integer, FileSender> senderMap = theFileSenderMap.get(taskId);
            if( senderMap == null){
                senderMap = new HashMap<>();
                theFileSenderMap.put(taskId, senderMap);
            }            
            senderMap.put(fileId, aSender);
            
        }
        
        aSender.start();
        
    }

    //===============================================================
    /**
     *  Cancel any file transfers with the given taskId
     * 
     * @param taskId 
     */
    public void cancelFileTransfer( int taskId ) {
        
        //Cancel file receives
        int channelId = 0;
        synchronized( theFileReceiverMap ){
            
            List<Integer> fileIds = new ArrayList<>(theFileReceiverMap.keySet());
            for( Integer anId : fileIds ){
                
                FileReceiver aReceiver = theFileReceiverMap.get(anId);
                int receiverId = aReceiver.getTaskId();
                if( receiverId == taskId ){
                    aReceiver.cleanupFileTransfer();
                    channelId = aReceiver.getChannelId();
                    theFileReceiverMap.remove( anId );
                }
                
            }
        }
        
        //Cancel file sends
        synchronized( theFileSenderMap ){
            
            List<Integer> taskIds = new ArrayList<>(theFileSenderMap.keySet());
            for( Integer aTaskId : taskIds ){
                
                Map<Integer, FileSender> aSenderMap = theFileSenderMap.get(aTaskId);
                List<Integer> aFileId = new ArrayList<>(aSenderMap.keySet());
                for( Integer anId : aFileId ){
                    FileSender aSender = aSenderMap.get(anId);
                    channelId = aSender.getChannelId();
                    aSender.shutdown();
                    aSenderMap.remove( anId );
                }
                
                //Remove from the map
                theFileSenderMap.remove(aTaskId);
            }
        }
        
        //Clear the send buffer
        if( channelId != 0 ){
            PortRouter aPR = thePortManager.getPortRouter( ClientConfig.getConfig().getSocketPort() );
            SocketChannelHandler aSCH = aPR.getConnectionManager().getSocketChannelHandler(channelId);

            //Set the wrapper
            if( aSCH != null ){
                aSCH.clearQueue();
            }  
        }  
        
        
    }

    //========================================================================
    /**
     * 
     * @param downloadFileMsg 
     */
    public void fileDownload(TaskGetFile downloadFileMsg) {
        
        //Get the filename hash 
        String theHashFilenameStr = downloadFileMsg.getHashFilenameString();
        String[] theFilePathArr = theHashFilenameStr.split(":", 2);
        if( theFilePathArr.length > 1 ){

            String theFilePath = theFilePathArr[1];            
            File fileToSend = new File(theFilePath);
            if(fileToSend.exists()){
        
                ClientConfig theConf = ClientConfig.getConfig();
                int socketPort = theConf.getSocketPort();
                String serverIp = theConf.getServerIp();

                PortManager aPM = getPortManager();
                ClientPortRouter aPR = (ClientPortRouter) aPM.getPortRouter( socketPort );
                
                //Queue the file to be sent
                String fileHashNameStr = new StringBuilder().append("0").append(":").append(theFilePath).toString();
                PushFile thePFM = new PushFile( downloadFileMsg.getTaskId(), 0, fileHashNameStr, fileToSend.length(), PushFile.FILE_DOWNLOAD );
                thePFM.setDestHostId( downloadFileMsg.getSrcHostId() );
                               
                //Get the channel id and determine what to do
                synchronized(retChannelId){
                    int channelId = retChannelId.get();
                    switch (channelId) {
                        case -1:
                            //Make sure to set channel Id before sending
                            queueFileDownload(thePFM);
                            //Start connection
                            FileConnectionCallback aFCC = new FileConnectionCallback(serverIp, socketPort);
                            aPR.ensureConnectivity( aFCC );
                            retChannelId.set(0);
                            break;
                        case 0:
                            //Queue file download
                            queueFileDownload(thePFM);
                            break;
                        default:
                            thePFM.setChannelId(channelId);
                            DataManager.send(aPM, thePFM);
                            break;
                    }
                }                
            }
        }
    }

    //========================================================================
    /**
     * 
     * @param pushFileMsg 
     */
    public void fileUpload( PushFile pushFileMsg ) {
        
        ClientConfig theConf = ClientConfig.getConfig();
        int socketPort = theConf.getSocketPort();
        String serverIp = theConf.getServerIp();

        //Get the port router
        PortManager aPM = getPortManager();
        ClientPortRouter aPR = (ClientPortRouter) aPM.getPortRouter( socketPort );
        DebugPrinter.printMessage(  this.getClass().getSimpleName(), "Received push file.");
        
        //Get the channel id and determine what to do
        synchronized(retChannelId){
            int channelId = retChannelId.get();
            switch (channelId) {
                case ConnectionManager.CHANNEL_DISCONNECTED:
                    //Make sure to set channel Id before sending
                    queueFileUpload(pushFileMsg);
                    //Start connection
                    FileConnectionCallback aFCC = new FileConnectionCallback(serverIp, socketPort);
                    aPR.ensureConnectivity( aFCC );
                    retChannelId.set(0);
                    break;
                case 0:
                    //Queue file download
                    queueFileUpload(pushFileMsg);
                    break;
                default:
                    try {
                        pushFileMsg.setFileChannelId(channelId);
                        prepFilePush( pushFileMsg );
                    } catch ( LoggableException | IOException ex) {
                        RemoteLog.log(Level.INFO, NAME_Class, "evaluate()", ex.getMessage(), ex );
                    }  
                    break;
            }
        }
        
    }

    //=================================================================
    /**
     * 
     * @param fileId 
     * @param channelId 
     */
    public void removeFileSender(int fileId, int channelId ) {
        synchronized( theFileSenderMap ){
            theFileSenderMap.remove(fileId );
            if( theFileReceiverMap.isEmpty() && theFileSenderMap.isEmpty() && channelId == retChannelId.get() ){
                retChannelId.set(ConnectionManager.CHANNEL_DISCONNECTED);
            }
        }
    }

    //==========================================================================
    /**
     * 
     * @param thePFM 
     */
    private void queueFileDownload(PushFile thePFM) {
        synchronized( pendingDownloads ){
            pendingDownloads.add(thePFM);
        }
    }
    
     //==========================================================================
    /**
     * 
     * @param thePFM 
     */
    private void queueFileUpload(PushFile thePFM) {
        synchronized( pendingUploads ){
            pendingUploads.add(thePFM);
        }
    }

    //==========================================================================
    /**
     * 
     * @param theChannelId 
     */
    public void connectionCallback(int theChannelId) {
        
        //Set the channelId
        synchronized(retChannelId){
            retChannelId.set(theChannelId);
        }
        
        //Get the port manager
        PortManager aPM = getPortManager();
        //Start the downloads
        synchronized( pendingDownloads ){
            while( !pendingDownloads.isEmpty() ){
                //Send while there are messages
                PushFile aPF = pendingDownloads.poll();
                if( aPF != null ){
                    aPF.setFileChannelId(theChannelId);
                    DataManager.send(aPM, aPF);    
                }          
            }
        }
        
        //Start the uploads
        synchronized( pendingUploads ){
            try {
                while( !pendingUploads.isEmpty() ){
                    //Send while there are messages
                    PushFile aPF = pendingUploads.poll();
                    if( aPF != null ){
                        aPF.setFileChannelId(theChannelId);
                        prepFilePush( aPF );
                    }
                }
            } catch ( LoggableException | IOException ex) {
                RemoteLog.log(Level.INFO, NAME_Class, "evaluate()", ex.getMessage(), ex );
            }  
        }
        
    }
    
}
