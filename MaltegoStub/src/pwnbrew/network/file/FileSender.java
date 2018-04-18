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
 */

package pwnbrew.network.file;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import pwnbrew.StubConfig;
import pwnbrew.log.LoggableException;
import pwnbrew.manager.DataManager;
import pwnbrew.manager.PortManager;
import pwnbrew.misc.Constants;
import pwnbrew.misc.DebugPrinter;
import pwnbrew.misc.ManagedRunnable;
import pwnbrew.network.PortRouter;
import pwnbrew.network.control.ControlMessageManager;
import pwnbrew.network.control.messages.PushFileAbort;
import pwnbrew.network.control.messages.PushFileAck;
import pwnbrew.network.control.messages.PushFileUpdate;
import pwnbrew.selector.SocketChannelHandler;

/**
 *
 *  
 */
@SuppressWarnings("ucd")
public class FileSender extends ManagedRunnable {

    private final PortManager thePortManager;
    private final PushFileAck theFileAck;
    private final int thePort;
    
    private static final int MAX_BUFFER_SIZE = 32768;
    private int channelId = 0;
    
    //Class name
    private static final String NAME_Class = FileSender.class.getSimpleName();
    
    
    //=========================================================================
    /*
     *  Constructor
     */
    FileSender( PortManager passedManager, PushFileAck passedAck, int passedPort ) {
        super( Constants.Executor);
        thePortManager = passedManager;
        theFileAck = passedAck;
        thePort = passedPort;
                        
    }   
    
    @Override
    protected void go() {
        
        int fileId = theFileAck.getFileId();
        int taskId = theFileAck.getTaskId();
        channelId = theFileAck.getChannelId();

        try {

            File fileToSend = new File( theFileAck.getFilename());

            //Send the file
            if( fileToSend.exists() ){
                sendFile( fileToSend, fileId );
            } else {
                DebugPrinter.printMessage( NAME_Class, "go", fileToSend.getAbsolutePath() + " doesn't exist.", null);
            }             

        } catch (IOException | LoggableException ex) {

            DebugPrinter.printMessage( NAME_Class, "go", ex.getMessage(), ex);   

//            ControlMessageManager aCMManager = ControlMessageManager.getControlMessageManager();
//            try {

//                if( aCMManager == null )
//                    aCMManager = ControlMessageManager.initialize(thePortManager);                    

                //Send message to cleanup the file transfer on the client side
                int srcHostId = theFileAck.getSrcHostId();
                PushFileAbort fileAbortMsg = new PushFileAbort( taskId, srcHostId, Constants.COMM_CHANNEL_ID, fileId );
                DataManager.send(thePortManager, fileAbortMsg);
//                aCMManager.send(fileAbortMsg ); 

//            } catch( IOException ex1 ){
//                 DebugPrinter.printMessage( NAME_Class, "go", ex.getMessage(), ex1);   
//            }

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
        PortRouter thePR = thePortManager.getPortRouter( thePort );
        if(thePR == null)
            thePR = DataManager.createPortRouter(thePortManager, thePort, true);
                
        SocketChannelHandler aHandler = thePR.getSocketChannelHandler( dstHostId );
        
        //Get the client id and dest id
        int srcHostId = Integer.parseInt( StubConfig.getConfig().getHostId() );    
        //Get the id and port router
        if( aHandler != null ){
            
            if( fileToBeSent.length() == 0 ){
                
                //Send the file data
                FileData fileDataMsg = new FileData(fileId, new byte[0]);  
                fileDataMsg.setChannelId(channelId);
                fileDataMsg.setSrcHostId(srcHostId);
                fileDataMsg.setDestHostId(dstHostId );   
                
                //Send the message
                thePR.queueSend( fileDataMsg.getBytes(), dstHostId, fileDataMsg.getCancelId() );

            } else {  
                
                //Compress file then send
                if( theFileAck.useCompression() ){
                    sendCompressedFile( fileToBeSent );
                } else {
                    sendUncompressedFile( fileToBeSent );
                }
            }
            
        } else {
            throw new IOException("Not connected to the client.");
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
    
    //=====================================================================
    /**
     * 
     */
    private void sendCompressedFile( File fileToBeSent ) throws IOException {
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream( (int) fileToBeSent.length());
        Deflater aDef = new Deflater();
        aDef.setLevel(Deflater.BEST_COMPRESSION);
        DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(baos, aDef );
        
        DebugPrinter.printMessage( NAME_Class, "evaluate", "Sending file " + fileToBeSent.getName(), null);
                
        //Normal file send
        try {
            
            byte[] readBuf = new byte[MAX_BUFFER_SIZE];
            FileInputStream aFIS = new FileInputStream( fileToBeSent);
            try {                
                int read;
                while ( (read = (aFIS.read(readBuf))) > 0 ) {
                    deflaterOutputStream.write(readBuf, 0, read);
                }
                
            }  finally {

                deflaterOutputStream.finish();
                deflaterOutputStream.close();
                
                //Close file input stream
                try {
                    aFIS.close();
                } catch (IOException ex) {
                    ex = null;
                }
            }
                         
        } catch (IOException ex) {
            DebugPrinter.printMessage( NAME_Class, "compressFileAndSend", ex.getMessage(), ex); 
            return;
        }
        
        byte[] outputBytes = baos.toByteArray();
        baos.close();
        ByteArrayInputStream fileInputStream = new ByteArrayInputStream(outputBytes);
        outputBytes = null;
            
        //Get compressed bytes
        int fileId = theFileAck.getFileId();
        int taskId = theFileAck.getTaskId();
        int dstHostId = theFileAck.getSrcHostId();

        //Send message update the file size to the compressed one
        PushFileUpdate fileSizeUpdateMsg = new PushFileUpdate( dstHostId, Constants.COMM_CHANNEL_ID, taskId, fileId, outputBytes.length );
        DataManager.send(thePortManager, fileSizeUpdateMsg);

        byte[] byteChunk = new byte[MAX_BUFFER_SIZE];
        int fileRead = 0;
        
        while(fileRead != -1 && !finished() ){

            try{
                fileRead = fileInputStream.read(byteChunk); 
            } catch(IOException ex){   
                fileRead = -1;
            }

            //Set file length
            if( fileRead == -1 )
                continue;

            FileData fileDataMsg = new FileData(fileId, Arrays.copyOf(byteChunk, fileRead));
            fileDataMsg.setChannelId(channelId);
            fileDataMsg.setDestHostId(dstHostId);

            DataManager.send(thePortManager, fileDataMsg);

        }
        
        try {
            //Close the stream
            fileInputStream.close();
        } catch (IOException ex) {
        }
         
    }
    
    //=====================================================================
    /**
     * 
     * @param fileToBeSent
     * @throws IOException 
     */
    private void sendUncompressedFile( File fileToBeSent ) throws IOException {
        
        //Get compressed bytes
        int fileId = theFileAck.getFileId();
        int dstHostId = theFileAck.getSrcHostId();
        int srcHostId = Integer.parseInt( StubConfig.getConfig().getHostId() );
        
        FileInputStream aFIS = new FileInputStream( fileToBeSent);
        try {

            //Get the file channel
            FileChannel theFC = aFIS.getChannel();
            ByteBuffer fileChannelBB = ByteBuffer.allocate(MAX_BUFFER_SIZE);

            //Set the msglen
            int fileRead = 0;
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
                fileDataMsg.setSrcHostId(srcHostId);
                fileDataMsg.setDestHostId(dstHostId ); 
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
