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
 *  FileSender.java
 *
 *  Created on June 7, 2013
 */

package pwnbrew.network.file;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import pwnbrew.ClientConfig;
import pwnbrew.log.RemoteLog;
import pwnbrew.manager.ConnectionManager;
import pwnbrew.manager.DataManager;
import pwnbrew.manager.PortManager;
import pwnbrew.utilities.Constants;
import pwnbrew.utilities.DebugPrinter;
import pwnbrew.utilities.FileUtilities;
import pwnbrew.utilities.ManagedRunnable;
import pwnbrew.utilities.SocketUtilities;
import pwnbrew.network.Message;
import pwnbrew.network.control.ControlMessageManager;
import pwnbrew.network.control.messages.PushFileAbort;
import pwnbrew.network.control.messages.PushFileAck;
import pwnbrew.network.control.messages.PushFileUpdate;

/**
 *
 *  
 */
public class FileSender extends ManagedRunnable /*implements LockListener */{

    private final PortManager thePortManager;
    private final PushFileAck theFileAck;
    
//    protected static final int CONNECT_RETRY = 3;
//    private static int maxMsgLen = (256 * 256) - 8; 
    private static final int maxMsgLen = 14982 - 7;
    
    //Class name
    private static final String NAME_Class = FileSender.class.getSimpleName();
    
//    private int lockVal = 0;
    private int channelId = 0;
    
    
    //=========================================================================
    /*
     *  Constructor
     */
    @SuppressWarnings("ucd")
    public FileSender( PortManager passedExecutor, PushFileAck passedAck ) {
        super( Constants.Executor);
        thePortManager = passedExecutor;
        theFileAck = passedAck;
        channelId = theFileAck.getFileChannelId();
    }   
    
    @Override
    protected void go() {
        
        int fileId = theFileAck.getFileId();
        int taskId = theFileAck.getTaskId();
        try {

            File fileToSend = new File( theFileAck.getFilename());
            if( !fileToSend.exists()){

                File libDir = FileUtilities.getTempDir();
                fileToSend = new File(libDir, theFileAck.getFilename());
            }

            //If the file exist
            if( fileToSend.exists() ){
                sendFile( fileToSend,  fileId ); 
            } else {
                throw new IOException("File does not exist");
            }            

        } catch (Exception ex) {

            RemoteLog.log(Level.INFO, NAME_Class, "go()", ex.getMessage(), ex );

            ControlMessageManager aCMManager = ControlMessageManager.getControlMessageManager();
            if( aCMManager != null ){
                //Send message to cleanup the file transfer on the client side
                PushFileAbort fileAbortMsg = new PushFileAbort( channelId, taskId, fileId );
                DataManager.send(thePortManager, fileAbortMsg);
            }

        }
        
    } 
    
    //===============================================================
    /**
     * Sends a file from the file queue to the other end of the socket channel
     *
     * @return
    */
    private void sendFile( File fileToBeSent, int fileId ) throws Exception {
        
        //Get the port router
        int dstHostId = theFileAck.getSrcHostId();
        ClientConfig theClientConf = ClientConfig.getConfig();
        
        //Get the client id and dest id
        int clientId = Integer.parseInt( theClientConf.getHostId() );
        byte[] clientIdArr = SocketUtilities.intToByteArray(clientId);
        byte[] destIdArr = SocketUtilities.intToByteArray(dstHostId);
        
        //Get the id and port router
        byte[] theFileId = SocketUtilities.intToByteArray(fileId); 
        if( fileToBeSent.length() == 0 ){
            
            //Send the file data
            FileData fileDataMsg = new FileData(fileId, new byte[0]);   
            fileDataMsg.setDestHostId(dstHostId);
            
            //Send the message
            DataManager.send(thePortManager, fileDataMsg);
            
        } else {  
            
            //Compress file then send
            if( theFileAck.useCompression() ){
                
                compressFileAndSend( fileToBeSent );
                
            } else {
        
                //Normal file send
                FileInputStream aFIS = new FileInputStream( fileToBeSent);
                try {

                    //Get the file channel
                    FileChannel theFC = aFIS.getChannel();
                    ByteBuffer fileChannelBB = ByteBuffer.allocate(maxMsgLen);

                    //Set the msglen
                    final byte[] msgLen = new byte[Message.MSG_LEN_SIZE];
                    int readCount;            

                    int fileRead = 0;
                    DebugPrinter.printMessage( this.getClass().getSimpleName(), "Sending " + theFileAck.getHashFilenameString() + " Channel: " + Integer.toString(channelId));
                    while(fileRead != -1 && !finished() ){

                        //Add the file message type
                        fileChannelBB.clear();
                        fileRead = theFC.read(fileChannelBB);                

                        //Set file length
                        if( fileRead == -1 ){
                            continue;
                        } else {
                            readCount = fileRead;
                        }

                        //Convert the length to a byte array
                        SocketUtilities.intToByteArray(msgLen, readCount + clientIdArr.length + destIdArr.length + theFileId.length );
                        fileChannelBB.flip();

                        //Set the data and channel id
                        byte[] fileBytes = Arrays.copyOf(fileChannelBB.array(), fileChannelBB.limit());
                        FileData fileDataMsg = new FileData(fileId, fileBytes);
                        fileDataMsg.setChannelId(channelId);
                        fileDataMsg.setDestHostId(dstHostId);

                        DataManager.send(thePortManager, fileDataMsg);

                    }

                    //Close the file channel
                    theFC.force(true);

                }  finally {

                    //Close file input stream
                    try {
                        aFIS.close();
                    } catch (IOException ex) {
                        ex = null;
                    }
                }
            }
        }        
        
        //Remove from the parent map
        FileMessageManager theFMM = FileMessageManager.getFileMessageManager();
        theFMM.removeFileSender( theFileAck.getTaskId(), channelId );

    }

//    //===============================================================
//    /**
//     * 
//     * @param lockOp 
//     */
//    @Override
//    public synchronized void lockUpdate(int lockOp) {
//        lockVal = lockOp;
//        notifyAll();
//    }
    
//    //===============================================================
//    /**
//     * 
//     * @return  
//     */
//    @Override
//    public synchronized int waitForLock() {
//        
//        int retVal;        
//        while( lockVal == 0 && !shutdownRequested ){
//            try {
//                wait();
//            } catch (InterruptedException ex) {
//                continue;
//            }
//        }
//        
//        //Set to temp and reset
//        retVal = lockVal;
//        lockVal = 0;
//        
//        return retVal;
//    }

    //=====================================================================
    /**
     * 
     * @return 
     */
    public int getChannelId() {
        return channelId;
    }

    //=====================================================================
    /**
     * 
     */
    private void compressFileAndSend( File fileToBeSent ) {
    
        ByteArrayOutputStream baos = new ByteArrayOutputStream( (int) fileToBeSent.length());
        Deflater aDef = new Deflater();
        aDef.setLevel(Deflater.BEST_COMPRESSION);
        DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(baos, aDef, 2048);

        //Normal file send
        try {
            
            byte[] readBuf = new byte[4096];
            FileInputStream aFIS = new FileInputStream( fileToBeSent);
            try {                
                int read;
                while ( (read = (aFIS.read(readBuf))) > 0 ) {
                    deflaterOutputStream.write(readBuf, 0, read);
                }
                
            }  finally {

                //Close file input stream
                try {
                    aFIS.close();
                } catch (IOException ex) {
                    ex = null;
                }
            }

            deflaterOutputStream.finish();
                        
        } catch (IOException ex) {
            Logger.getLogger(FileSender.class.getName()).log(Level.SEVERE, null, ex);
        }

        //Get compressed bytes
        int fileId = theFileAck.getFileId();
        int taskId = theFileAck.getTaskId();
        int dstHostId = theFileAck.getSrcHostId();
        
        byte[] compressedBytes = baos.toByteArray();
        ControlMessageManager aCMManager = ControlMessageManager.getControlMessageManager();
        if( aCMManager != null ){
            //Send message to cleanup the file transfer on the client side
            PushFileUpdate fileSizeUpdateMsg = new PushFileUpdate( ConnectionManager.COMM_CHANNEL_ID, taskId, fileId, compressedBytes.length );
            fileSizeUpdateMsg.setDestHostId(dstHostId);
            DataManager.send(thePortManager, fileSizeUpdateMsg);
        }
    
        byte[] byteChunk = new byte[maxMsgLen];
        ByteBuffer fileChannelBB = ByteBuffer.wrap(compressedBytes);
        while( fileChannelBB.hasRemaining() ){   
            
            if( fileChannelBB.remaining() >  maxMsgLen )
                fileChannelBB.get(byteChunk);
            else {
                byteChunk = new byte[fileChannelBB.remaining()];
                fileChannelBB.get(byteChunk);
            }
            
            //Set the data and channel id
            FileData fileDataMsg = new FileData(fileId, byteChunk);
            fileDataMsg.setChannelId(channelId);
            fileDataMsg.setDestHostId(dstHostId);

            //Send
            DataManager.send(thePortManager, fileDataMsg);
        }
    }

}
