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
import java.util.List;
import java.util.logging.Level;
import pwnbrew.exception.RemoteExceptionWrapper;
import pwnbrew.logging.Log;
import pwnbrew.logging.LoggableException;
import pwnbrew.misc.DebugPrinter;
import pwnbrew.network.ControlOption;
import pwnbrew.network.Message;
import pwnbrew.utilities.SocketUtilities;

/**
 *
 *  
 */
public abstract class ControlMessage extends Message {
    
    private static final String NAME_Class = ControlMessage.class.getSimpleName();
        
    //Data members
    protected List<ControlOption> optionList = new ArrayList<>();

    //=========================================================================
    /*
     *  Contructor
     */
    public ControlMessage( int passedDestHostId ) { // NO_UCD (use default)
        //Set id
        super( CONTROL_MESSAGE_TYPE, passedDestHostId );
    }

    //=========================================================================
    /*
     *  Contructor
     */
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
        
        byte[] classPathLenArr = new byte[2];
        byte[] classPathStrArr = getClass().getCanonicalName().getBytes();
        
        //Get the length
        int classPathLen = classPathStrArr.length;
        SocketUtilities.intToByteArray(classPathLenArr, classPathLen);
        
        //Add the classpath
        rtnBuffer.put(classPathLenArr);
        rtnBuffer.put(classPathStrArr);
        
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
     * @throws pwnbrew.logging.LoggableException
     * @throws java.io.IOException
     * @throws pwnbrew.exception.RemoteExceptionWrapper
    */
    public static ControlMessage getMessage( ByteBuffer passedBuffer ) throws LoggableException, IOException, RemoteExceptionWrapper {

        byte[] theId = new byte[4],  clientId = new byte[4], destHostId = new byte[4];
        ControlMessage aMessage = null;

        //Copy over the client id
        passedBuffer.get(clientId, 0, clientId.length);

        //Copy over the dst host id
        passedBuffer.get(destHostId, 0, destHostId.length);

        //Copy over the id
        passedBuffer.get(theId, 0, theId.length);

        try {
            //Create a message
            aMessage = instatiateMessage(theId, passedBuffer );
             
            //Ignore NoOps
            if( !aMessage.getClass().equals( NoOp.class ))
                DebugPrinter.printMessage(ControlMessage.class.getSimpleName(), "Received " + aMessage.getClass().getSimpleName() + " message.");

            //Set client id
            int theClientId = SocketUtilities.byteArrayToInt(clientId);
            aMessage.setSrcHostId( theClientId );

            //Set dest host id
            int theDestHostId= SocketUtilities.byteArrayToInt(destHostId);
            aMessage.setDestHostId( theDestHostId );

            //Parse the tlvs
            aMessage.parseControlOptions(passedBuffer);
            
        } catch (ClassNotFoundException ex) {
            
            //Convert the clientid
            int theClientId = SocketUtilities.byteArrayToInt(clientId);
            String retStr = "Unable to run the Pwnbrew transform.\nPlease ensure the appropriate extension has been loaded into the target Pwnbrew server.";
            RemoteException anException = new RemoteException(theClientId, retStr);
            throw new RemoteExceptionWrapper(anException);
            
        }

        return aMessage;
    }
    
     //===============================================================
    /**
     *  Create a message from the byte buffer
     * 
     * @param msgId
     * @param passedBuffer
     * @return 
     * @throws pwnbrew.logging.LoggableException 
     * @throws java.lang.ClassNotFoundException 
     */
    public static ControlMessage instatiateMessage( byte[] msgId, ByteBuffer passedBuffer ) throws LoggableException, ClassNotFoundException{
        
        ControlMessage aMsg = null;
        byte[] classFqnLength = new byte[2];
        passedBuffer.get(classFqnLength, 0, classFqnLength.length);
        
        //Get the length of the class path
        int theLength = SocketUtilities.byteArrayToInt(classFqnLength);
        byte[] classPath = new byte[theLength];
        
        passedBuffer.get(classPath, 0, classPath.length);
        String thePath = new String(classPath);
        
        try {
            
            //Get the class
            Class aClass = Class.forName(thePath);
            Constructor aConstruct = aClass.getConstructor( byte[].class);
            aMsg = (ControlMessage)aConstruct.newInstance(msgId);

        } catch ( NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Log.log(Level.SEVERE, NAME_Class, "instatiateMessage()", ex.getMessage(), ex );  
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

//       //===============================================================
//    /**
//     *   Parses out the tlv list form the passed byte array
//     *
//     * @param tlvArray
//    */
//    private void parseTlvByteArray(byte[] tlvArray) throws LoggableException  {
//
//        int marker = 0;
//        final byte[] tempLen = new byte[2];
//
//        //Loop through the array and create tlvs
//        while(marker < tlvArray.length){
//
//            byte localType = tlvArray[marker++];
//            if(localType == 0){
//                continue;
//            }
//
//            //Ensure we won't get an out of bound exception
//            if(marker + tempLen.length > tlvArray.length){
//                throw new LoggableException("Error parsing length field in TLV.");
//            }
//
//            System.arraycopy( tlvArray, marker, tempLen, 0, tempLen.length );
//            marker = marker + tempLen.length;
//            int localLen = SocketUtilities.byteArrayToInt(tempLen);
//
//            //And the length value with 0xff in case it is cnegative
//            byte[] value = new byte[localLen & 0xffff];
//
//            //Copy value into tempArray
//            System.arraycopy( tlvArray, marker, value, 0, value.length );
//            marker = marker + value.length;
//
//            //Create the tlv and add it to the message
//            ControlOption tempTlv = new ControlOption(localType, value);
//            setOption(tempTlv);
//        }
//    }
    
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
        
        //Add the function
        count += 2;
        count += getClass().getCanonicalName().getBytes().length;
        
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
    public boolean setOption( ControlOption tempTlv ){ return true; }

}/* END CLASS ControlMessage */
