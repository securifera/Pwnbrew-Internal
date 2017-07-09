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
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import pwnbrew.log.Log;
import pwnbrew.log.LoggableException;
import pwnbrew.manager.ConnectionManager;
import pwnbrew.manager.DataManager;
import pwnbrew.manager.IncomingConnectionManager;
import pwnbrew.manager.PortManager;
import pwnbrew.manager.ServerManager;
import pwnbrew.misc.DebugPrinter;
import pwnbrew.network.PortRouter;
import pwnbrew.network.ServerPortRouter;
import pwnbrew.network.control.messages.PushFile;
import pwnbrew.network.control.messages.PushFileAck;
import pwnbrew.network.control.messages.PushFileFin;
import pwnbrew.selector.SocketChannelHandler;
import pwnbrew.xml.ServerConfig;

/**
 *
 *  
 */
public class FileMessageManager extends DataManager {

    private static FileMessageManager theFileManager;
    private final Map<Integer, FileReceiver> theFileReceiverMap = new HashMap<>();
    private final Map<Integer, Map<Integer, FileSender>> theFileSenderMap = new HashMap<>();
       
    private static final String NAME_Class = FileMessageManager.class.getSimpleName();
  
    //===========================================================================
    /*
     *  Constructor
     */
    private FileMessageManager( PortManager passedCommManager ) {
        
        super(passedCommManager); 
        //Set the port
        try {
            
            ServerConfig theConfig = ServerConfig.getServerConfig();
            int thePort = theConfig.getSocketPort();
            setPort( thePort );
            
        } catch (LoggableException ex) {
            Log.log( Level.SEVERE, NAME_Class, "FileMessageManager()", ex.getMessage(), ex);
        }        
        
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
     */
    public synchronized static FileMessageManager initialize( PortManager passedCommManager ) throws IOException {

        if( theFileManager == null ) {
            theFileManager = new FileMessageManager( passedCommManager );
            createPortRouter( passedCommManager, theFileManager.getPort(), true );
        }
        
        return theFileManager;

    }
    
    // ==========================================================================
    /**
     *   Gets the FileMessageManager
     * @return 
     */
    public synchronized static FileMessageManager getMessageManager(){
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
        theFileManager.getDataHandler().processData( srcPortRouter, msgBytes);        
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
        ServerPortRouter aPR = (ServerPortRouter) thePortManager.getPortRouter( operatingPort );
                       
        //Initiate the file transfer
        if(aPR != null){
            
            //Initialize the file transfer
            synchronized( theFileReceiverMap ){
                
                int fileId = passedMsg.getFileId();
                FileReceiver theReceiver = theFileReceiverMap.get(fileId);
                //If the receive flag is not set
                if( theReceiver == null ){

                    try {
                        //theReceiver = new FileReceiver( this, clientId, passedTaskId, passedFileId, passedFileSize, parentDir, hashFilenameStr );
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
     */
    protected void removeFileReceiver(int fileId) {
        synchronized( theFileReceiverMap ){
            theFileReceiverMap.remove(fileId );
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
        
        int srcId = passedMessage.getSrcHostId();
        ServerManager aSM = (ServerManager)thePortManager;
        File aClientDir = aSM.getHostDirectory( srcId );

        File libDir = null;
        int fileType = passedMessage.getFileType();
        switch(fileType){
            case PushFile.JOB_RESULT:
                if( aClientDir == null )
                    return retVal;
                libDir = new File(aClientDir, Integer.toString(taskId));
                break;
            case PushFile.FILE_DOWNLOAD:
                if( aClientDir == null )
                    return retVal;
                libDir = new File(aClientDir, Integer.toString(taskId));
                break;
            case PushFile.JAR_UPLOAD:
            case PushFile.CERT_UPLOAD:
                File aFile = File.createTempFile("tmp", null);
                libDir = aFile.getParentFile();
                aFile.delete();
                break;
        }

        //Get the hash/filename
        String hashFileNameStr = passedMessage.getHashFilenameString();

        //Try to begin the file transfer
        long fileSize = passedMessage.getFileSize();
        initFileTransfer( passedMessage, libDir );

        //If filesize == -1 then it is streaming
        if( fileSize >= 0 ){
            //Send an ack to the sender to begin transfer
            DebugPrinter.printMessage( getClass().getSimpleName(), "Sending ACK for " + hashFileNameStr);
            PushFileAck aSFMA = new PushFileAck(taskId, fileId, fileChannelId, hashFileNameStr, srcId );
            DataManager.send(thePortManager, aSFMA);
        } 
        
        retVal = true;  

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
        if( theReceiver != null )
            theReceiver.cleanupFileTransfer();        
          
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
     *  Send the file referenced by the message.
     * 
     * @param passedMsg 
     */
    public void cleanupFileSender(PushFileFin passedMsg) {
        
         
        //Close the socket
        try {
            
            int srcId = passedMsg.getSrcHostId();
            int taskId = passedMsg.getTaskId();
            int fileId = passedMsg.getFileId();
            int channelId = passedMsg.getFileChannelId();
            
            ServerConfig theConfig = ServerConfig.getServerConfig();
            int socketPort = theConfig.getSocketPort();

            //Shutdown the socket
            PortRouter aSPR = getPortManager().getPortRouter(socketPort);
            IncomingConnectionManager aICM = (IncomingConnectionManager)aSPR.getConnectionManager(srcId);
            if( aICM != null ){
                SocketChannelHandler aSCH = aICM.removeHandler(channelId);
                if( aSCH != null )
                    aSCH.shutdown();
            }
            
        
                
            synchronized( theFileSenderMap ){
                Map<Integer, FileSender> senderMap = theFileSenderMap.get(taskId);
                if( senderMap != null){
                    FileSender aFileSender = senderMap.remove(fileId);
                    if( aFileSender != null )
                        aFileSender.shutdown();
                }
            }
        
        } catch (LoggableException ex) {
            Log.log(Level.SEVERE, NAME_Class, "cleanupFileTransfer()", ex.getMessage(), ex);
        } 
        
    }

    //===============================================================
    /**
     *  Cancel any file transfers with the given taskId
     * 
     * @param clientId
     * @param taskId 
     */
    public void cancelFileTransfer( int clientId, int taskId ) {
        
        int channelId = 0;
        synchronized( theFileReceiverMap ){
            
            List<Integer> fileIds = new ArrayList<>(theFileReceiverMap.keySet());
            for( Integer anId : fileIds ){
                
                FileReceiver aReceiver = theFileReceiverMap.get(anId);
                if( aReceiver != null ){
                    int receiverId = aReceiver.getTaskId();
                    if( receiverId == taskId ){
                        aReceiver.cleanupFileTransfer();
                        channelId = aReceiver.getChannelId();
                        theFileReceiverMap.remove( anId );
                    }
                }
            }
        }
        
         //Cancel file sends
        synchronized( theFileSenderMap ){
            
            List<Integer> taskIds = new ArrayList<>(theFileSenderMap.keySet());
            for( Integer aTaskId : taskIds ){
                
                Map<Integer, FileSender> aSenderMap = theFileSenderMap.get(aTaskId);
                if( aSenderMap != null ){
                    List<Integer> aFileId = new ArrayList<>(aSenderMap.keySet());
                    for( Integer anId : aFileId ){
                        FileSender aSender = aSenderMap.get(anId);
                        channelId = aSender.getChannelId();
                        aSender.shutdown();
                        aSenderMap.remove( anId );
                    }
                }
                
                //Remove from the map
                theFileSenderMap.remove(aTaskId);
            }
        }
        
         //Clear the send buffer
        if( channelId != 0 ){
            
            ServerPortRouter aPR = (ServerPortRouter) thePortManager.getPortRouter( getPort() );
            ConnectionManager aCM = aPR.getConnectionManager(clientId);
            SocketChannelHandler aSCH = aCM.getSocketChannelHandler( channelId );

            //Set the wrapper
            if( aSCH != null )
                aSCH.clearQueue();  
        }        
        
    }

    //===============================================================
    /**
     *  Updates the file size
     * @param passedFileId
     * @param fileSize 
     */
    public void updateFileSize(int passedFileId, long fileSize) {
        FileReceiver theReceiver;
        synchronized( theFileReceiverMap ){
            theReceiver = theFileReceiverMap.remove(passedFileId );  
        }
        
        //Update the size
        if( theReceiver != null )
            theReceiver.updateFileSize(fileSize);
        
    }
    
}
