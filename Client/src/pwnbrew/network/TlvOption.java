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
 * TlvObject.java
 *
 * Created on June 17, 2013, 8:11 PM
 */

package pwnbrew.network;

import java.nio.ByteBuffer;
import java.util.Arrays;
import pwnbrew.misc.SocketUtilities;

/**
 *
 *  
 */
abstract public class TlvOption { // NO_UCD (use default)

    protected byte[] type = new byte[0];
    protected byte[] length = new byte[0];
    protected byte[] value = new byte[0];

    //===============================================================
    /**
     *  Returns the length of the TLV
     * @return 
     */
    public int getLength(){
       return type.length + length.length + value.length;
    }

    //===============================================================
    /**
    *  Appends the option bytes to the passed byte buffer.
    * <p>
    * If the argument is null this method does nothing.
    * 
    * @param passedBuffer the {@link ByteBuffer} to which to append the data // NO_UCD (use default)
    */
    public void append( ByteBuffer passedBuffer ) {

        if( passedBuffer == null ) return;
        
        //Add the type
        passedBuffer.put(type);

        //Add the length
        passedBuffer.put(length);

        //Add the value
        passedBuffer.put(value);
       
    }
    
    //===============================================================
    /**
    *  Get the integer representation of the byte array.
     * @return 
    */
    public int getType() {
        return SocketUtilities.byteArrayToInt( type );
    }
    
    //===============================================================
    /**
    *  Get value array.
     * @return 
    */
    public byte[] getValue() {
        return Arrays.copyOf(value, value.length);
    }

}
