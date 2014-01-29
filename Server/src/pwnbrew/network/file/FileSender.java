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
import pwnbrew.logging.Log;
import pwnbrew.logging.LoggableException;
import pwnbrew.manager.CommManager;
import pwnbrew.misc.Constants;
import pwnbrew.execution.ManagedRunnable;
import pwnbrew.misc.DebugPrinter;
import pwnbrew.misc.Directories;
import pwnbrew.utilities.SocketUtilities;
import pwnbrew.network.Message;
import pwnbrew.network.PortRouter;
import pwnbrew.network.ServerPortRouter;
import pwnbrew.network.control.ControlMessageManager;
import pwnbrew.network.control.messages.PushFileAbort;
import pwnbrew.network.control.messages.PushFileAck;
import pwnbrew.selector.SocketChannelHandler;
import pwnbrew.xmlBase.ServerConfig;

/**
 *
 *  
 */
@SuppressWarnings("ucd")
public class FileSender extends ManagedRunnable {

    private final CommManager theCommManager;
    private final PushFileAck theFileAck;
//    private ProgressListener theListener;
    private final int thePort;
    
    
//    private static int maxMsgLen = (256 * 256) - 8; 
    private static final int maxMsgLen = 12582 - 7;    
    
    //Class name
    private static final String NAME_Class = FileSender.class.getSimpleName();
    
    
    //=========================================================================
    /*
     *  Constructor
     */
    FileSender( CommManager passedManager, PushFileAck passedAck, int passedPort ) {
        super( Constants.Executor);
        theCommManager = passedManager;
        theFileAck = passedAck;
        thePort = passedPort;
                        
    }   
    
    @Override
    protected void go() {
        
         //Get the socket router
        ServerPortRouter aPR = (ServerPortRouter) theCommManager.getPortRouter( thePort );
                       
        //Initiate the file transfer
        if(aPR != null){
            
            int fileId = theFileAck.getFileId();
            try {

                File fileToSend = new File( theFileAck.getFilename());
                if( !fileToSend.exists()){
                    fileToSend = new File( Directories.getFileLibraryPath(), theFileAck.getFilehash());
                }
                
                //Send the file
                if( fileToSend.exists() ){
                    sendFile( fileToSend, fileId );
                } else {
                    Log.log(Level.WARNING, NAME_Class, "go()", fileToSend.getAbsolutePath() + " doesn't exist.", null);
                }             

            } catch (IOException | LoggableException ex) {

                Log.log(Level.INFO, NAME_Class, "go()", ex.getMessage(), ex );

                ControlMessageManager aCMManager = ControlMessageManager.getControlMessageManager();
                try {
                    
                    if( aCMManager == null ){
                        aCMManager = ControlMessageManager.initialize(theCommManager);
                    }

                    //Send message to cleanup the file transfer on the client side
                    int clientId = theFileAck.getClientId();
                    PushFileAbort fileAbortMsg = new PushFileAbort( fileId, clientId );
                    aCMManager.send(fileAbortMsg ); 
                    
                } catch( IOException ex1 ){
                     Log.log(Level.INFO, NAME_Class, "go()", ex.getMessage(), ex );
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
    private void sendFile( File fileToBeSent, int fileId ) throws IOException, LoggableException {
        
        //Get the port router
        int dstHostId = theFileAck.getClientId();
        PortRouter thePR = theCommManager.getPortRouter( thePort );
        SocketChannelHandler aHandler = thePR.getSocketChannelHandler( dstHostId );
        DebugPrinter.printMessage(NAME_Class, "Sending file to: " + dstHostId);
        
        //Get the client id and dest id
        int clientId = Integer.parseInt( ServerConfig.getServerConfig().getHostId() );
        byte[] clientIdArr = SocketUtilities.intToByteArray(clientId);
        byte[] destIdArr = SocketUtilities.intToByteArray(dstHostId);
    
        //Get the id and port router
        if( aHandler != null ){
            
            byte[] theFileId = SocketUtilities.intToByteArray(fileId); 
            if( fileToBeSent.length() == 0 ){

                ByteBuffer tempBuffer = ByteBuffer.allocate( 2 + clientIdArr.length + destIdArr.length + theFileId.length + 1 );
                tempBuffer.put( Message.FILE_MESSAGE_TYPE );
                tempBuffer.put( new byte[]{0x0,0x0c});
                tempBuffer.put(clientIdArr);
                tempBuffer.put(destIdArr);
                tempBuffer.put(theFileId);

                //Send the message
                thePR.queueSend( Arrays.copyOf( tempBuffer.array(), tempBuffer.position()), theFileAck.getClientId() );

            } else {  

                FileInputStream aFIS = new FileInputStream( fileToBeSent);
                try {

                    //Get the file channel
                    FileChannel theFC = aFIS.getChannel();
                    ByteBuffer fileChannelBB = ByteBuffer.allocate(maxMsgLen);

                    //Set the msglen
                    final byte[] msgLen = new byte[2];
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
                        thePR.queueSend( Arrays.copyOf( tempBuffer.array(), tempBuffer.position()), theFileAck.getClientId() );

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
            
        } else {
            throw new IOException("Not connected to the client.");
        }
    }

}
