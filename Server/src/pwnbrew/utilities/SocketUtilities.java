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
 * SocketUtilities.java
 *
 * Created on July 20, 2013, 6:54 PM
 */

package pwnbrew.utilities;

import java.io.IOException;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import pwnbrew.log.LoggableException;
import pwnbrew.misc.RuntimeRunnable;


/**
 *  
 */
final public class SocketUtilities {
  
    //Constants...
    private static int messageCounter = -1;
    private static String theHostname = null;
    private static final String NAME_Class = SocketUtilities.class.getSimpleName();
    
    //===============================================================
    /**
    * Returns the hostname of the current host.
    *
    * @return
     * @throws pwnbrew.log.LoggableException
     * @throws java.io.IOException
    */
    public static String getHostname() throws LoggableException, IOException {

        //Try and get the hostname
        if(theHostname == null){
            try {
                InetAddress localInetAddress = InetAddress.getLocalHost();
                theHostname = localInetAddress.getHostName().toLowerCase();
            } catch(UnknownHostException ex){

                if(Utilities.isUnix( Utilities.getOsName() )){

                    RuntimeRunnable aCommand = new RuntimeRunnable( new String[]{"hostname"} );
                    aCommand.run();

                    theHostname = aCommand.getStdOut().trim().toLowerCase();
                    System.out.println("Hostname: " + theHostname);

                }
            }
        }

        return theHostname;
    }

    //===============================================================
    /**
    * Returns an integer to be used for packet ids
    *
    * @return
    */
    public static int getNextId(){

        if(messageCounter == -1){
            messageCounter = Utilities.SecureRandomGen.nextInt(0x7fffffff);
        } else {
            messageCounter++;
        }

        return messageCounter;
    }

    //===============================================================
    /**
     * 
     * @param value
     * @return 
     */
    public static byte[] intToByteArray(int value) {

        byte[] aByteArr = new byte[4];
        intToByteArray( aByteArr, value );
        
        return aByteArr;
    }
      
    //===============================================================
    /**
     * 
     * @param finalArray
     * @param value 
     */
    public static void intToByteArray( final byte[] finalArray, int value ) {

        //Loop through the byte array and convert the integer
        int arrayLen = finalArray.length;
        for( int i = 0; i < arrayLen; i++){
            finalArray[i] = (byte)( ( value >>> ( 8 * (arrayLen - ( i + 1) )  )) & 0xFF );
        }
    }

    //===============================================================
    /**
     * 
     * @param value
     * @return 
     */
    public static byte[] longToByteArray(long value) {

        return new byte[] {
                (byte)(value >>> 56 & 0xff),
                (byte)(value >>> 48 & 0xff),
                (byte)(value >>> 40 & 0xff),
                (byte)(value >>> 32 & 0xff),
                (byte)(value >>> 24 & 0xff),
                (byte)(value >>> 16 & 0xff),
                (byte)(value >>> 8  & 0xff),
                (byte)(value & 0xff)};
    }

    
    //===============================================================
    /**
     * 
     * @param value
     * @return 
     */
    public static int byteArrayToInt(byte[] value){

        int tempInt = 0;
        for(int i = 0, j = value.length; i < value.length; i++, j-- ){
            tempInt += (value[i] & 0xff) << (8 * (j - 1));
        }
        return tempInt;

    }
    
    //===============================================================
    /**
     * 
     * @param value
     * @return 
     */
    public static int byteArrayToInt2(byte[] value){

        int tempInt = 0;
        for(int i = 0, j = value.length; i < value.length; i++, j-- ){
            tempInt += ((int)value[i] << 24) >>> (j*8);
        }
        return tempInt;

    }

    //===============================================================
    /**
     * 
     * @param value
     * @return 
     */
    public static long byteArrayToLong(byte[] value){

        long tempInt = 0;
        for(int i = 0, j = value.length; i < value.length; i++, j-- ){
            tempInt += (value[i] & 0xff) << (8 * (j - 1));
        }
        return tempInt;

    }
    
    //===============================================================
    /**
     * 
     * @param passedBytes
     * @return 
     */
    public static String toString(byte[] passedBytes) {

        StringWriter aStringWriter = new StringWriter();
        int length = passedBytes.length;

        if(length > 0) {
            for(int i = 0; i < length; i++) {
                aStringWriter.write( toString(passedBytes[i]));
                if(i != length - 1){
                    aStringWriter.write(" ");
                }
            }
        }

        return aStringWriter.toString();
    }
    
    //===============================================================
    /**
     *  Convert byte to String
     * 
     * @param passedByte
     * @return 
     */
    private static String toString(byte passedByte) {
        StringBuilder aStringBuffer = new StringBuilder();
        aStringBuffer.append(nibbleToDigit((byte)(passedByte >> 4)));
        aStringBuffer.append(nibbleToDigit(passedByte));

        return aStringBuffer.toString();
    }
    
    //===============================================================
    /**
     *  Convert byte to char
     * 
     * @param passedByte
     * @return 
     */
    private static char nibbleToDigit(byte passedByte) {
        char aChar = (char)(passedByte & 0xf); // mask low nibble
        return(aChar > 9 ? (char)(aChar - 10 + 'a') : (char)(aChar + '0')); // int to hex char
    }
    
    //===============================================================
    /**
     *  Convert string to byte[]
     * 
     * @param s
     * @return 
     * @throws pwnbrew.log.LoggableException 
     */
    public static byte[] hexStringToByteArray(String s) throws LoggableException {
        int len = s.length();
        
        if( len % 2 == 1)
            throw new LoggableException(null, "Invalid hex string, not a multiple of 2.");
        
        byte[] data = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                + Character.digit(s.charAt(i+1), 16));
        }

        return data;
    }
  
}
