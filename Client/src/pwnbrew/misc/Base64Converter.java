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
 *  Base64Converter.java
 *
 * Created on Oct 19, 2008, 3:00 PM
 *
 */

package pwnbrew.misc;


import java.io.IOException;

/**
 *
 *  
 */
public class Base64Converter {
  
   
   
 // ==========================================================================
  /**
   *  This method reads the <code>byte</code> array
   *  and then converts the byte[] into base64 <code>byte</code> array
   *
   *  @param  passedByteArray
   *
   *  @return the <code>byte</code> array in b64
     * @throws java.io.IOException
   */
   public static String encode(byte[] passedByteArray) throws IOException {

       //TODO files larger than 30M will cause heap overflow.
       //Need to come back and stream all B64 incrementally straight to the disk
       
      sun.misc.BASE64Encoder anEncoder = new sun.misc.BASE64Encoder();
      return anEncoder.encode(passedByteArray);
   }

   // ==========================================================================
   /**
   *  This method reads the base64 encoded <code>byte</code> array
   *  and then decodes it into a <code>byte</code> array
   *
     * @param passedString
   *
   *  @return the <code>byte</code> array in b64
     * @throws java.io.IOException
   */

   public static byte[] decode(String passedString) throws IOException {

      sun.misc.BASE64Decoder aDecoder = new sun.misc.BASE64Decoder();
      return aDecoder.decodeBuffer(passedString);
   }

  
}//End Class
