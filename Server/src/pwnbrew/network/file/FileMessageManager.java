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
import pwnbrew.logging.Log;
import pwnbrew.logging.LoggableException;
import pwnbrew.manager.CommManager;
import pwnbrew.manager.DataManager;
import pwnbrew.manager.ServerManager;
import pwnbrew.misc.DebugPrinter;
import pwnbrew.network.PortRouter;
import pwnbrew.network.ServerPortRouter;
import pwnbrew.network.control.ControlMessageManager;
import pwnbrew.network.control.messages.PushFile;
import pwnbrew.network.control.messages.PushFileAck;
import pwnbrew.selector.SocketChannelHandler;
import pwnbrew.xmlBase.ServerConfig;

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
    private FileMessageManager( CommManager passedCommManager ) {
        
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
    public synchronized static FileMessageManager initialize( CommManager passedCommManager ) throws IOException {

        if( theFileManager == null ) {
            theFileManager = new FileMessageManager( passedCommManager );
            createPortRouter( passedCommManager, theFileManager.getPort(), true );
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
    private void initFileTransfer( int clientId, int passedTaskId, int passedFileId, 
            File parentDir, String hashFilenameStr, long passedFileSize) 
            throws LoggableException, NoSuchAlgorithmException, IOException {

        //Get the socket router
        ServerPortRouter aPR = (ServerPortRouter) theCommManager.getPortRouter( operatingPort );
                       
        //Initiate the file transfer
        if(aPR != null){
            
            //Initialize the file transfer
            synchronized( theFileReceiverMap ){
                
                FileReceiver theReceiver = theFileReceiverMap.get(passedFileId);
                //If the receive flag is not set
                if( theReceiver == null ){

                    theReceiver = new FileReceiver( this, clientId, passedTaskId, passedFileId, passedFileSize, parentDir, hashFilenameStr );
                    theFileReceiverMap.put(passedFileId, theReceiver);

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
        
        int clientId = passedMessage.getSrcHostId();
        ServerManager aSM = (ServerManager)theCommManager;
        File aClientDir = aSM.getHostDirectory( clientId );

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

        try {

            //Get the hash/filename
            String hashFileNameStr = passedMessage.getHashFilenameString();

            //Try to begin the file transfer
            long fileSize = passedMessage.getFileSize();
            initFileTransfer( passedMessage.getSrcHostId(), taskId, fileId, libDir, hashFileNameStr, fileSize );

            //If filesize == -1 then it is streaming
            if( fileSize >= 0 ){
                //Send an ack to the sender to begin transfer
                DebugPrinter.printMessage( getClass().getSimpleName(), "Sending ACK for " + hashFileNameStr);
                PushFileAck aSFMA = new PushFileAck(taskId, fileId, hashFileNameStr, clientId );
                DataManager.send(theCommManager, aSFMA);
            } 

        } catch (IOException | NoSuchAlgorithmException ex){
            throw new LoggableException(ex);
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
        
        FileSender aSender = new FileSender( getCommManager(), aMessage, getPort() );
        
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
     * @param clientId
     * @param taskId 
     */
    public void cancelFileTransfer( int clientId, int taskId ) {
        
        synchronized( theFileReceiverMap ){
            
            List<Integer> fileIds = new ArrayList<>(theFileReceiverMap.keySet());
            for( Integer anId : fileIds ){
                
                FileReceiver aReceiver = theFileReceiverMap.get(anId);
                if( aReceiver != null ){
                    int receiverId = aReceiver.getTaskId();
                    if( receiverId == taskId ){
                        aReceiver.cleanupFileTransfer();
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
                List<Integer> aFileId = new ArrayList<>(aSenderMap.keySet());
                for( Integer anId : aFileId ){
                    FileSender aSender = aSenderMap.get(anId);
                    aSender.shutdown();
                    aSenderMap.remove( anId );
                }
                
                //Remove from the map
                theFileSenderMap.remove(aTaskId);
            }
        }
        
         //Clear the send buffer
        ServerPortRouter aPR = (ServerPortRouter) theCommManager.getPortRouter( getPort() );
        SocketChannelHandler aSCH = aPR.getSocketChannelHandler(clientId);

        //Set the wrapper
        if( aSCH != null )
            aSCH.clearQueue();          
        
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
