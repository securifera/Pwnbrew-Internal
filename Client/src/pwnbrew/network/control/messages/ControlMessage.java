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
* ControlMessage.java
*
* Created on June 7, 2013, 8:05:08 PM
*/

package pwnbrew.network.control.messages;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import pwnbrew.log.LoggableException;
import pwnbrew.manager.ConnectionManager;
import pwnbrew.manager.PortManager;
import pwnbrew.utilities.DebugPrinter;
import pwnbrew.utilities.DynamicClassLoader;
import pwnbrew.utilities.SocketUtilities;
import pwnbrew.network.ControlOption;
import pwnbrew.network.Message;
import pwnbrew.network.control.ControlMessageManager;

/**
 *
 *  
 */
public abstract class ControlMessage extends Message {
    
    //Data members
    protected List<ControlOption> optionList = new ArrayList<>();
    private short theClassId;

    //=========================================================================
    /*
     *  Contructor
     */
    public ControlMessage( short classIdType ) { // NO_UCD (use default)
        //Set id
        super( CONTROL_MESSAGE_TYPE );
        setChannelId( ConnectionManager.COMM_CHANNEL_ID );
        theClassId = classIdType;
    }

    //=========================================================================
    /*
     *  Contructor
     */
    @SuppressWarnings("ucd")
    public ControlMessage( byte[] passedId ) {
        //Set id
        super( CONTROL_MESSAGE_TYPE, passedId);
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
        
        //Add the control message type
        byte[] msgTypeArr = new byte[2]; 
        SocketUtilities.intToByteArray(msgTypeArr, theClassId);
        rtnBuffer.put(msgTypeArr);
               
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
    */
    public static ControlMessage getMessage( ByteBuffer passedBuffer ) throws LoggableException, IOException {

        byte[] theId = new byte[4], srcHostId = new byte[4], tempHostId = new byte[4];
        byte[] classIdArr = new byte[2];
        ControlMessage aMessage = null;

        //Copy over the client id
        passedBuffer.get(srcHostId, 0, srcHostId.length);

        //Copy over the dest host id
        passedBuffer.get(tempHostId, 0, tempHostId.length);

        //Copy over the id
        passedBuffer.get(theId, 0, theId.length);

        //Copy over the class path length
        passedBuffer.get(classIdArr, 0, classIdArr.length);

        short classId = (short)SocketUtilities.byteArrayToInt(classIdArr);
        String thePath = ControlMessageManager.getControlMessagePath(classId);

        if( thePath != null ){
            try {
                //Create a message
                aMessage = instatiateMessage( theId, thePath );

                if( !(aMessage instanceof NoOp) )
                    DebugPrinter.printMessage(ControlMessage.class.getSimpleName(), "Received " + aMessage.getClass().getSimpleName() + " message.");

                //Set client id
                int theClientId = SocketUtilities.byteArrayToInt(srcHostId);
                aMessage.setSrcHostId( theClientId );

                //Set dest host id
                int theDestHostId= SocketUtilities.byteArrayToInt(tempHostId);
                aMessage.setDestHostId( theDestHostId );

                //Parse the tlvs
                aMessage.parseControlOptions(passedBuffer);

            } catch (ClassNotFoundException ex) {

                //Rewind the buffer
                passedBuffer.rewind();
                ClassRequest aClassRequest = new ClassRequest( thePath, Arrays.copyOf(passedBuffer.array(), passedBuffer.remaining()) );
                aMessage = aClassRequest;

            }
        } else {
            throw new LoggableException("Unknown message id: " + Integer.toString(classId));
        }

        return aMessage;
    }
    //===============================================================
    /**
     *  Create a message from the byte buffer
     * 
     * @param msgId
     * @param thePath
     * @return 
     * @throws pwnbrew.log.LoggableException 
     * @throws java.lang.ClassNotFoundException 
     */
    public static ControlMessage instatiateMessage( byte[] msgId, String thePath ) throws LoggableException, ClassNotFoundException{
        
        ControlMessage aMsg = null;
        try {
            
            //Get the class
            Class aClass = null;
            try {
                aClass = Class.forName(thePath);
            } catch( ClassNotFoundException ex ){
                ControlMessageManager aCMM = ControlMessageManager.getControlMessageManager();
                if( aCMM != null ){
                    PortManager aPM = aCMM.getPortManager();
                    DynamicClassLoader aDCL = aPM.getDynamicClassLoader();
                    aClass = Class.forName(thePath, true, aDCL);
                }
            }
            
            //Get a new message
            if( aClass != null ){
                Constructor aConstruct = aClass.getConstructor( byte[].class);
                aMsg = (ControlMessage)aConstruct.newInstance(msgId);
            }

        } catch ( NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new LoggableException(ex);
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
        
        //Add the classpath length
        count += 2;
       
        //Add the options
        for( ControlOption aTlv : optionList)  
            count += aTlv.getLength();        
        
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

}/* END CLASS ControlMessage */
