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
* StageMessage.java
*
* Created on April 27, 2013, 8:05:08 PM
*/

package pwnbrew.network.stage;

import pwnbrew.network.control.messages.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import pwnbrew.exception.RemoteExceptionWrapper;
import pwnbrew.log.LoggableException;
import pwnbrew.misc.DebugPrinter;
import pwnbrew.network.ControlOption;
import pwnbrew.network.Message;
import pwnbrew.utilities.SocketUtilities;

/**
 *
 *  
 */
public class StageMessage extends Message {
    
    private static final String NAME_Class = StageMessage.class.getSimpleName();    
    private static final int JAVA_PAYLOAD = 18;
    
        
    //Data members
    protected List<ControlOption> optionList = new ArrayList<>();

    //=========================================================================
    /*
     *  Contructor
     */
    public StageMessage( int passedDestHostId ) { // NO_UCD (use default)
        //Set id
        super( STAGING_MESSAGE_TYPE, passedDestHostId );
    }

    //=========================================================================
    /*
     *  Contructor
     */
    public StageMessage( byte[] passedId ) {
        //Set id
        super( STAGING_MESSAGE_TYPE, passedId);
    }

    
    //===============================================================
    /**
    *   Returns a byte array representing the control message
    *
     * @param rtnBuffer
    */
    @Override
    public void append( ByteBuffer rtnBuffer) {
    
        //Add the parent
        super.append(rtnBuffer);
        
//        byte[] classPathLenArr = new byte[2];
//        byte[] classPathStrArr = getClass().getCanonicalName().getBytes();
//        
//        //Get the length
//        int classPathLen = classPathStrArr.length;
//        SocketUtilities.intToByteArray(classPathLenArr, classPathLen);
//        
//        //Add the classpath
//        rtnBuffer.put(classPathLenArr);
//        rtnBuffer.put(classPathStrArr);
        
        //Add the options
        for( ControlOption aTlv : optionList){            
            aTlv.append( rtnBuffer );
        }        
        
    }
    
    //===============================================================
    /**
     *  Adds an option to the list
     *
     * @param passedObject
     */
    public void addOption(ControlOption passedObject) {
        optionList.add(passedObject);
    }

    //===============================================================
    /**
     *  Returns a message of the appropriate function type.
     *
     * @param passedBuffer
     * @return msgAddress
     * @throws pwnbrew.log.LoggableException
     * @throws java.io.IOException
     * @throws pwnbrew.exception.RemoteExceptionWrapper
    */
    public static StageMessage getMessage( ByteBuffer passedBuffer ) throws LoggableException, IOException, RemoteExceptionWrapper {

        byte[] channelId = new byte[4],  clientId = new byte[4], destHostId = new byte[4], stageId = new byte[4];
        StageMessage aMessage = null;

        //Copy over the client id
        passedBuffer.get(clientId, 0, clientId.length);

        //Copy over the dst host id
        passedBuffer.get(destHostId, 0, destHostId.length);

        //Copy over the channelid
        passedBuffer.get(channelId, 0, channelId.length);  
        
        //Copy over the staging id
        passedBuffer.get(stageId, 0, stageId.length);  

        //Create a message
        aMessage = instatiateMessage( stageId );
        if( aMessage != null ){


            //Ignore NoOps
            if( !aMessage.getClass().equals( NoOp.class ))
                DebugPrinter.printMessage(StageMessage.class.getSimpleName(), "Received " + aMessage.getClass().getSimpleName() + " message.");

            //Set client id
            int theClientId = SocketUtilities.byteArrayToInt(clientId);
            aMessage.setSrcHostId( theClientId );

            //Set dest host id
            int theDestHostId= SocketUtilities.byteArrayToInt(destHostId);
            aMessage.setDestHostId( theDestHostId );
            
            //Set the channel id
            int theChanId= SocketUtilities.byteArrayToInt(channelId);
            aMessage.setChannelId( theChanId );

            //Parse the tlvs
            aMessage.parseControlOptions(passedBuffer);
            
        }        

        return aMessage;
    }
    
     //===============================================================
    /**
     *  Create a message from the byte buffer
     * 
     * @param msgId
     * @return 
     */
    public static StageMessage instatiateMessage( byte[] msgId ) {
        
        StageMessage aMsg = null;
        
        int stageId = SocketUtilities.byteArrayToInt(msgId);
        if( stageId == JAVA_PAYLOAD ){
            aMsg = new SendStage(msgId);
        }
        
        return aMsg;
    }
    
    //===============================================================
    /**
     *   Parses out the control options
     *
     * @param tlvArray
    */
    private void parseControlOptions( ByteBuffer passedBB ) throws LoggableException  {

        final byte[] tempLen = new byte[4];

        //Loop through the array and create tlvs
        while( passedBB.hasRemaining() ){

            byte localType = passedBB.get();
            if(localType == 0){
                continue;
            }

            //Ensure we won't get an out of bound exception
            if( passedBB.position() + tempLen.length > passedBB.limit() ){
                throw new LoggableException("Error parsing length field in Control Option.");
            }

            //Get the length of the value
            passedBB.get(tempLen);
            //AND the length value with 0xff in case it is negative
            int localLen = SocketUtilities.byteArrayToInt(tempLen) & 0xffffffff;
            
            //Get the value
            byte[] value = new byte[localLen];
            passedBB.get(value);

            //Create the tlv and add it to the message
            ControlOption tempTlv = new ControlOption(localType, value);
            setOption(tempTlv);
        }
    }
    
    //===============================================================
    /**
     * Returns the length of the 
     *
     * @return
    */
    @Override
    public int getLength(){
    
        int count = 0;
        count += super.getLength();
                
        //Add the options
        for( ControlOption aTlv : optionList){  
            count += aTlv.getLength();
        }
        
        //Set the length
        SocketUtilities.intToByteArray(length, count );
        
        return count;
    }
    
    //=========================================================================
    /**
     *  Sets the variable in the message related to this TLV
     * 
     * @param tempTlv 
     * @return  
     */
    public boolean setOption( ControlOption tempTlv ){ return false; }

}/* END CLASS StageMessage */
