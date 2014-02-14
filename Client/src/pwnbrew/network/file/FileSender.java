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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.logging.Level;
import pwnbrew.ClientConfig;
import pwnbrew.Persistence;
import pwnbrew.concurrent.LockListener;
import pwnbrew.log.RemoteLog;
import pwnbrew.log.LoggableException;
import pwnbrew.manager.CommManager;
import pwnbrew.misc.Constants;
import pwnbrew.misc.DebugPrinter;
import pwnbrew.misc.ManagedRunnable;
import pwnbrew.misc.SocketUtilities;
import pwnbrew.network.ClientPortRouter;
import pwnbrew.network.Message;
import pwnbrew.network.PortRouter;
import pwnbrew.network.control.ControlMessageManager;
import pwnbrew.network.control.messages.PushFileAbort;
import pwnbrew.network.control.messages.PushFileAck;

/**
 *
 *  
 */
public class FileSender extends ManagedRunnable implements LockListener {

    private final CommManager theCommManager;
    private final PushFileAck theFileAck;
//    private final int thePort;
    
//    protected static final int CONNECT_RETRY = 3;
//    private static int maxMsgLen = (256 * 256) - 8; 
    private static int maxMsgLen = 14982 - 7;
    
    //Class name
    private static final String NAME_Class = FileSender.class.getSimpleName();
    
    private int lockVal = 0;
    
    
    //=========================================================================
    /*
     *  Constructor
     */
    public FileSender( CommManager passedExecutor, PushFileAck passedAck /*, int passedPort */ ) {
        super( Constants.Executor);
        theCommManager = passedExecutor;
        theFileAck = passedAck;
//        thePort = passedPort;
    }   
    
    @Override
    protected void go() {
        
         //Get the socket router
        int thePort = ClientConfig.getConfig().getSocketPort();
        ClientPortRouter aPR = (ClientPortRouter) theCommManager.getPortRouter( thePort );

        //Initiate the file transfer
        if(aPR != null){

            int fileId = theFileAck.getFileId();
            aPR.ensureConnectivity( thePort, this );       
            try {

                File fileToSend = new File( theFileAck.getFilename());
                if( !fileToSend.exists()){
                     File libDir = new File( Persistence.getDataPath(), Integer.toString( theFileAck.getTaskId()) );
                     fileToSend = new File(libDir, theFileAck.getFilename());
                }

                //If the file exist
                if( fileToSend.exists() ){
                    sendFile( aPR, fileToSend,  fileId); 
                } else {
                    throw new IOException("File does not exist");
                }            

            } catch (Exception ex) {

                RemoteLog.log(Level.INFO, NAME_Class, "go()", ex.getMessage(), ex );

                ControlMessageManager aCMManager = ControlMessageManager.getControlMessageManager();
                try {

                    if( aCMManager != null ){
                        //Send message to cleanup the file transfer on the client side
                        PushFileAbort fileAbortMsg = new PushFileAbort( fileId );
                        aCMManager.send(fileAbortMsg); 
                    }

                } catch( LoggableException ex1 ){
                     RemoteLog.log(Level.INFO, NAME_Class, "go()", ex.getMessage(), ex );
                }

            }
        }       
        
    } 
    
    //===============================================================
    /**
     * Sends a file from the file queue to the other end of the socket channel
     *
     * @return
    */
    private void sendFile( PortRouter thePR, File fileToBeSent, int passedId ) throws Exception {
        
        //Get the port router
//        int dstHostId = theFileAck.getClientId();
        int dstHostId = Constants.SERVER_ID;
//        PortRouter thePR = theCommManager.getPortRouter( thePort );
        
        //Get the client id and dest id
        int clientId = Integer.parseInt( ClientConfig.getConfig().getHostId() );
        byte[] clientIdArr = SocketUtilities.intToByteArray(clientId);
        byte[] destIdArr = SocketUtilities.intToByteArray(dstHostId);
        
        //Get the id and port router
        byte[] theFileId = SocketUtilities.intToByteArray(passedId); 
        if( fileToBeSent.length() == 0 ){
            
            ByteBuffer tempBuffer = ByteBuffer.allocate( Message.MSG_LEN_SIZE + clientIdArr.length + destIdArr.length + theFileId.length + 1 );
            tempBuffer.put( Message.FILE_MESSAGE_TYPE );
            tempBuffer.put( new byte[]{0x0,0x0,0x0,0x0c});
            tempBuffer.put(clientIdArr);
            tempBuffer.put(destIdArr);
            tempBuffer.put(theFileId);

            
            //Send the message
            thePR.queueSend( Arrays.copyOf( tempBuffer.array(), tempBuffer.position()), dstHostId);
            
        } else {  
        
            FileInputStream aFIS = new FileInputStream( fileToBeSent);
            try {

                //Get the file channel
                FileChannel theFC = aFIS.getChannel();
                ByteBuffer fileChannelBB = ByteBuffer.allocate(maxMsgLen);

                //Set the msglen
                final byte[] msgLen = new byte[Message.MSG_LEN_SIZE];
                int readCount;            

                int fileRead = 0;
                ByteBuffer tempBuffer;
                DebugPrinter.printMessage( this.getClass().getSimpleName(), "Sending " + theFileAck.getHashFilenameString());
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

                    //Construct the buffer
                    tempBuffer = ByteBuffer.allocate( readCount + msgLen.length + clientIdArr.length + destIdArr.length + theFileId.length + 1 );
                    tempBuffer.put( Message.FILE_MESSAGE_TYPE );
                    tempBuffer.put(msgLen);
                    tempBuffer.put(clientIdArr);
                    tempBuffer.put(destIdArr);
                    tempBuffer.put(theFileId);
                    tempBuffer.put(fileChannelBB);

                    //Send the message
                    thePR.queueSend( Arrays.copyOf( tempBuffer.array(), tempBuffer.position()), dstHostId);
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
        while( lockVal == 0 && !shutdownRequested ){
            try {
                wait();
            } catch (InterruptedException ex) {
                continue;
            }
        }
        
        //Set to temp and reset
        retVal = lockVal;
        lockVal = 0;
        
        return retVal;
    }

}
