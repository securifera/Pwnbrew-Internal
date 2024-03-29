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
 *  Http.java
 *
 *  Created on June 29, 2013
 */

package pwnbrew.network.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 *  
 */
public class Http {

    private final ExchangeDetails operation;    
    private final Map<String, String> optionList = new HashMap<>();
    private byte[] thePayLoadBytes = new byte[0];   
    
    private static final String DEFAULT_VERSION = "HTTP/1.1";   
    
    private static final String CONNECTION = "Connection";    
    public static final String ACCEPT_LANGUAGE = "Accept-Language";   
    public static final String COOKIE = "Cookie";     
    
    //RESPONSE
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String LOCATION = "Location";    
    public static final String CONTENT_LENGTH = "Content-Length";    
    
    public static final String SET_COOKIE = "Set-Cookie";  
    public static final String SERVER = "Server";    
    public static final String AGE = "Age"; 
    
    public static final int _200 = 200;
    public static final int _302 = 302;
    private static final int _404 = 404;
    
    public static final int DEFAULT_PORT = 80;
    public static final int SECURE_PORT = 443;
    
    private static final String _404_Page = new StringBuilder()
        .append("<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>\n")
        .append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"\n")
        .append("\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n")
        .append("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">")
        .append("<head>\n")
        .append("<title>404 - Not Found</title>\n")
        .append("</head>\n")
        .append("<body>\n")
        .append("<h1>404 - Not Found</h1>\n")
        .append("</body>\n")
        .append("</html>\n").toString();

   
    // ==========================================================================
    /**
     *  Constructor
     * 
     * @param headerParts 
    */
    @SuppressWarnings("ucd")
    public Http( String... headerParts ) {
        operation = new ExchangeDetails( headerParts );          
        setOption( Http.CONNECTION, "keep-alive");
    } 
    
    // ==========================================================================
    /**
     *  Sets the value of the passed key
     * 
     * @param passedKey
     * @param passedVal 
     */
    public void setOption( String passedKey, String passedVal ){
        synchronized(optionList){
            optionList.put(passedKey, passedVal);
        }
    }
    
    // ==========================================================================
    /**
     *  Gets the value of the passed key
     * 
     * @param passedKey
    */
    String getOption( String passedKey ){
        
        String retVal;
        synchronized(optionList){
            retVal = optionList.get(passedKey);
        }
        return retVal;
    }

    // ==========================================================================
    /**
     *  Set the payload
     * 
     * @param passedBytes
    */
    public void setPayloadBytes( byte[] passedBytes ){
        if( passedBytes != null ){
            thePayLoadBytes = Arrays.copyOf(passedBytes, passedBytes.length);
        }
    }   
    
    //===============================================================
    /**
    *  Returns the length of the header
     * @return 
    */
    public int getLength() {
        
        int count =0;
        count += operation.getLength();
        
        //Add the lengths of each
        for( Iterator<Entry<String,String>> anIter = optionList.entrySet().iterator(); anIter.hasNext(); ){
        
            //Add each entry
            Entry<String,String> anEntry = anIter.next();
            count += anEntry.getKey().length() + anEntry.getValue().length() + 4;
                        
        }   
        
        //Add line ending
        count += 2;
        
        //Add the payload bytes
        count += thePayLoadBytes.length;
        
        return count;
        
    }
    
    //===============================================================
    /**
    *  Appends the header bytes to the passed byte buffer.
     * @param rtnBuffer
    */
    public void append(ByteBuffer rtnBuffer) {
       
        try {
            
            //Add the operation
            operation.append(rtnBuffer);
            
            //For each option
            StringBuilder aSB;
            for( Iterator<Entry<String,String>> anIter = optionList.entrySet().iterator(); anIter.hasNext(); ){
                
                //Add each entry
                Entry<String,String> anEntry = anIter.next();
                aSB = new StringBuilder()
                        .append(anEntry.getKey())
                        .append(": ")
                        .append(anEntry.getValue())
                        .append("\r\n");
                        
                //Add the line
                rtnBuffer.put( aSB.toString().getBytes( "US-ASCII" ) );
            }   
            
            //Add header ender
            rtnBuffer.put((byte)'\r');
            rtnBuffer.put((byte)'\n');
            
            //Add the payload
            rtnBuffer.put( thePayLoadBytes );
            
        } catch (UnsupportedEncodingException ex) {
            ex = null;
        }
    }
    
    //===============================================================
    /**
    *  Returns a generic Http message of a certain kind.
     * @param passedType
     * @return 
    */
    public static Http getGeneric( int passedType ) {
    
        Http aHttpMsg = null;
        try {
            
            switch( passedType ){
                case _200:
                    
                    aHttpMsg = new Http( Http.DEFAULT_VERSION, Integer.toString(passedType), "Found");
                    aHttpMsg.setOption( Http.CONTENT_TYPE, "text/html");
                    break;
                case _302:
                    
                    aHttpMsg = new Http( Http.DEFAULT_VERSION, Integer.toString(passedType), "Found");
                    aHttpMsg.setOption( Http.LOCATION, "http://www.google.com");
                    aHttpMsg.setOption( Http.CONTENT_TYPE, "text/html");
                    aHttpMsg.setOption( Http.CONTENT_LENGTH, Integer.toString(0) );                  
                    
                    break;
                case _404:
                    
                    byte[] pageBytes = _404_Page.getBytes("US-ASCII");
                    aHttpMsg = new Http( Http.DEFAULT_VERSION, Integer.toString(passedType), "Not Found");
                    aHttpMsg.setPayloadBytes( pageBytes );
                    aHttpMsg.setOption( Http.CONTENT_TYPE, "text/html");
                    aHttpMsg.setOption( Http.CONTENT_LENGTH, Integer.toString(pageBytes.length) );
                    
                    break;
                default:
                    break;
            }
            
        } catch( IOException ex ){
            
        }
        
        return aHttpMsg;
    }

    // ==========================================================================
    /**
     *  Subclass that details an HTTP exchange
     * 
     * @param passedMethod
     * @param passedURI
     * @param passedCode 
    */
    private static class ExchangeDetails {

        private String requestMethod = "";
        private String requestURI = "";
        private final String requestVersion = DEFAULT_VERSION;
        private String statusCode = "";
        private String responsePhrase = "";
        
        // ==========================================================================
        /**
         * Constructor
         *
        */
        public ExchangeDetails( String... headerParts ) {            

            //Convert to list
            List<String> stringList = new ArrayList<>( Arrays.asList(headerParts));

            //Get the version
            try { 
                
                int versionIndex = stringList.indexOf(DEFAULT_VERSION);
                if( versionIndex != 0 ){
                    
                    //Get the operation
                    requestMethod = stringList.get( 0 );
                    requestURI = stringList.get( 1 );
                    
                } else {            

                    //Get the status code
                    statusCode = stringList.get( versionIndex + 1 );
                    responsePhrase = stringList.get( versionIndex + 2 );
                }
                
            } catch( IndexOutOfBoundsException ex){
                ex = null;
            }
                      
        }
        
         //===============================================================
        /**
        *  Appends the header bytes to the passed byte buffer.
        */
        public void append(ByteBuffer rtnBuffer) {

            try {
                //Add the request type
                StringBuilder aSB = new StringBuilder();                
                if( !requestMethod.isEmpty()){   
                    aSB.append(requestMethod);
                }

                //Add the URI
                if( !requestURI.isEmpty()){
                    aSB.append(" ")                
                       .append(requestURI)
                       .append(" ");
                }            

                //Add the version
                aSB.append(requestVersion);

                //Add the status code
                if( !statusCode.isEmpty()){
                    aSB.append(" ")
                       .append(statusCode);
                } 
                
                //Add the response phrase
                if( !responsePhrase.isEmpty()){
                    aSB.append(" ")
                       .append(responsePhrase);
                } 

                 //Add the operation
                rtnBuffer.put(aSB.toString().getBytes( "US-ASCII" ));
                rtnBuffer.put((byte)'\r');
                rtnBuffer.put((byte)'\n');
            } catch (UnsupportedEncodingException ex) {
                ex = null;
            }
        }

        //===============================================================
        /**
        *  Gets the length of the header
        */
        public int getLength() {
            
            int count = 0;
            count += requestMethod.length();
            
            int tempInt = requestURI.length();
            if( tempInt > 0){
                count += tempInt + 2;
            }
            
            //Add the version length
            count += requestVersion.length();
            
            //Add status code
            tempInt = statusCode.length();
            if( tempInt > 0){
                count += tempInt + 1;
            }
            
            //Add response phrase
            tempInt = responsePhrase.length();
            if( tempInt > 0){
                count += tempInt + 1;
            }
            
            //Add line end
            count += 2;
                        
            return count;
        }
    }       
    
   
}
