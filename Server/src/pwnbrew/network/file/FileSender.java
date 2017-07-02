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
import pwnbrew.log.Log;
import pwnbrew.log.LoggableException;
import pwnbrew.manager.PortManager;
import pwnbrew.misc.Constants;
import pwnbrew.execution.ManagedRunnable;
import pwnbrew.manager.DataManager;
import pwnbrew.misc.DebugPrinter;
import pwnbrew.misc.Directories;
import pwnbrew.utilities.SocketUtilities;
import pwnbrew.network.control.messages.PushFileAbort;
import pwnbrew.network.control.messages.PushFileAck;
import pwnbrew.xmlBase.ServerConfig;

/**
 *
 *  
 */
@SuppressWarnings("ucd")
public class FileSender extends ManagedRunnable {

    private final PortManager theCommManager;
    private final PushFileAck theFileAck;
//    private final int thePort;
    
//    private static int maxMsgLen = (256 * 256) - 8; 
    private static final int maxMsgLen = 12582 - 7;    
    private int channelId;
    
    //Class name
    private static final String NAME_Class = FileSender.class.getSimpleName();
    
    
    //=========================================================================
    /*
     *  Constructor
     */
    FileSender( PortManager passedManager, PushFileAck passedAck ) {
        super( Constants.Executor);
        theCommManager = passedManager;
        theFileAck = passedAck;
//        thePort = passedPort;
                        
    }   
    
    @Override
    protected void go() {
        
        int fileId = theFileAck.getFileId();
        channelId = theFileAck.getChannelId();
        int taskId = theFileAck.getTaskId();
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

            //Send message to cleanup the file transfer on the client side
            int clientId = theFileAck.getSrcHostId();
            PushFileAbort fileAbortMsg = new PushFileAbort( taskId, clientId, channelId, fileId );
            DataManager.send(theCommManager, fileAbortMsg);

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
        int dstHostId = theFileAck.getSrcHostId();
        DebugPrinter.printMessage(NAME_Class, "Sending file to: " + dstHostId);
        
        //Get the client id and dest id
        int clientId = Integer.parseInt( ServerConfig.getServerConfig().getHostId() );
        byte[] clientIdArr = SocketUtilities.intToByteArray(clientId);
        byte[] destIdArr = SocketUtilities.intToByteArray(dstHostId);
    
        if( fileToBeSent.length() == 0 ){

            FileData fileDataMsg = new FileData(fileId, new byte[0]);
            fileDataMsg.setChannelId(channelId);
            fileDataMsg.setSrcHostId(SocketUtilities.byteArrayToInt(clientIdArr));
            fileDataMsg.setDestHostId(SocketUtilities.byteArrayToInt(destIdArr) );           

            //Send the message
            DataManager.send(theCommManager, fileDataMsg);

        } else {  

            FileInputStream aFIS = new FileInputStream( fileToBeSent);
            try {

                //Get the file channel
                FileChannel theFC = aFIS.getChannel();
                ByteBuffer fileChannelBB = ByteBuffer.allocate(maxMsgLen);

                int fileRead = 0;
                DebugPrinter.printMessage( this.getClass().getSimpleName(), "Sending " + theFileAck.getHashFilenameString());
                while(fileRead != -1 && !finished() ){

                    //Add the file message type
                    fileChannelBB.clear();
                    fileRead = theFC.read(fileChannelBB);                

                    //Set file length
                    if( fileRead == -1 )
                        continue;

                    fileChannelBB.flip();

                    byte[] fileBytes = Arrays.copyOf(fileChannelBB.array(), fileChannelBB.limit());
                    FileData fileDataMsg = new FileData(fileId, fileBytes);
                    fileDataMsg.setChannelId(channelId);
                    fileDataMsg.setSrcHostId(SocketUtilities.byteArrayToInt(clientIdArr));
                    fileDataMsg.setDestHostId(SocketUtilities.byteArrayToInt(destIdArr) ); 

                    DataManager.send(theCommManager, fileDataMsg);

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

    //=====================================================================
    /**
     * 
     * @return 
     */
    public int getChannelId() {
        return channelId;
    }

}
