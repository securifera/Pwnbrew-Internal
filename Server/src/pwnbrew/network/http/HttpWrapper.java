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
 *  HttpWrapper.java
 *
 *  Created on Apr 8, 2013
 */

package pwnbrew.network.http;

import java.nio.ByteBuffer;
import pwnbrew.network.PortWrapper;
import pwnbrew.selector.SocketChannelHandler;

/**
 *
 *  
 */

@SuppressWarnings("ucd")
abstract public class HttpWrapper extends PortWrapper {

    Http currentHeader = null;
    
    //Use for flagging that a '\r' was detected
    private boolean firstCarriage = false;
    private boolean firstLineFeed = false;
    private boolean secondCarriage = false;            
          
    //Cookie ID
    protected static final String COOKIE_REF = "zrefx";
    protected static final String XOR_STRING = "PWNZ";
    
    //The stringbuilder
    private final StringBuilder aSB = new StringBuilder();
    
    //==========================================================================
    /**
     * Constructor
     *
    */
    public HttpWrapper() {
    }
    
     //===============================================================
    /**
     * Handles the bytes passed in from the selector
     *
     * @param passedHandler
     * @param aByteBuffer
    */
    @Override
    public void processData( SocketChannelHandler passedHandler, ByteBuffer aByteBuffer ) {
        
        if(aByteBuffer != null){             

            byte[] aByteArray = new byte[aByteBuffer.remaining()];
            aByteBuffer.get(aByteArray);
            
//            DebugPrinter.printMessage(this, "Got message");
            
            for( int i = 0; i < aByteArray.length; i++ ){

                if( firstCarriage && firstLineFeed && secondCarriage ){

                    //Check for newline first byte
                    if( aByteArray[i] == '\n' ){

                        processHeader( passedHandler );
                        aSB.setLength( 0 );
                       
//                        DebugPrinter.printMessage(this, "Processed message");
                        
                        //Skip past the page
                        String theLength = currentHeader.getOption( Http.CONTENT_LENGTH );
                        if( theLength != null ){
                            int packetLength = Integer.parseInt(theLength);
                            i += packetLength;                       
                        }
                        
                        currentHeader = null;
                       
                    //Add to the stringbuilder
                    } else {

                        aSB.append((char)aByteArray[i]);
                    } 

                    //Reset the flags
                    firstCarriage = false;
                    firstLineFeed = false;
                    secondCarriage = false;

               } else if( firstCarriage && firstLineFeed ){

                   //Check for newline first byte
                   if( aByteArray[i] == '\r' ){

                       secondCarriage = true;

                   //Add to the stringbuilder
                   } else {

                       aSB.append((char)aByteArray[i]);
                       firstCarriage = false;
                       firstLineFeed = false;
                   }

                //We've consume one line    
               } else if( firstCarriage ){

                   //Make sure that 
                   if( aByteArray[i] == '\n'){

                       String aLine = aSB.toString();                         
                       processLine(aLine);
                       aSB.setLength( 0 );

                       firstLineFeed = true;

                   //Add to the stringbuilder
                   } else {

                       aSB.append((char)aByteArray[i]);
                       firstCarriage = false;
                   }

                   //We've consume one line    
               } else  {

                   //Check for newline first byte
                   if( aByteArray[i] == '\r' ){
                       firstCarriage = true;                           
                   //Add to the stringbuilder
                   } else {
                       aSB.append((char)aByteArray[i]);
                   }

               }
           }
        
        }
    }

     //===============================================================
    /**
     *  Process the HTTP header, extract the data from it, and pass the message
     * on to the message handler.
     * 
     * @param aLine 
     */
    abstract void processHeader( SocketChannelHandler passedHandler );

    //===============================================================
    /**
     *  Process the line
     * 
     * @param aLine 
     */
    private void processLine(String aLine) {
        
        //Set the option in the header
        if( currentHeader != null ){
            
            //Set the option
            String[] lineArr = aLine.split(":");
            if( lineArr.length > 1 ){
                currentHeader.setOption(lineArr[0].trim(), lineArr[1].trim());
            }
            
        } else {
            
            String[] headerParts = aLine.split(" ");
            currentHeader = new Http( headerParts );
        }
    }
}
