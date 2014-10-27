///*
//
//Copyright (C) 2013-2014, Securifera, Inc 
//
//All rights reserved. 
//
//Redistribution and use in source and binary forms, with or without modification, 
//are permitted provided that the following conditions are met:
//
//    * Redistributions of source code must retain the above copyright notice,
//	this list of conditions and the following disclaimer.
//
//    * Redistributions in binary form must reproduce the above copyright notice,
//	this list of conditions and the following disclaimer in the documentation 
//	and/or other materials provided with the distribution.
//
//    * Neither the name of Securifera, Inc nor the names of its contributors may be 
//	used to endorse or promote products derived from this software without specific
//	prior written permission.
//
//THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS 
//OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY 
//AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER 
//OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
//CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
//SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON 
//ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
//(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
//EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
//================================================================================
//
//Pwnbrew is provided under the 3-clause BSD license above.
//
//The copyright on this package is held by Securifera, Inc
//
//*/
//
//
///*
//* PNG.java
//*
//* Created on Oct 10, 2013, 9:12:33 PM
//*/
//
//package pwnbrew.misc;
//
//import java.io.BufferedOutputStream;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.UnsupportedEncodingException;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.HashSet;
//import java.util.Iterator;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Set;
//import java.util.zip.CRC32;
//import pwnbrew.log.LoggableException;
//
///**
// *
// *  
// */
//public class Png {
//
//    private static final byte[] header = new byte[] { (byte)0x89, (byte)0x50,
//                                                      (byte)0x4E, (byte)0x47,
//                                                      (byte)0x0D, (byte)0x0A,
//                                                      (byte)0x1A, (byte)0x0A,};
//
//    //TODO This should be changed to a list for PNGs that have multiple chunks
//    //with the same name.  Fortunately the splash screen does not.
//    private final List<PngChunk> theChunks = new LinkedList<>();
//    private final File thePNGFile;
//
//    //===============================================================
//    /**
//     * Constructor
//     * @param passedFile
//    */
//    public Png(File passedFile) { // NO_UCD (use default)
//        thePNGFile = passedFile;
//    }
//
//     //===============================================================
//    /**
//     * Returns the PNG header
//     *
//     * @return
//    */
//    public static byte[] getHeader(){ // NO_UCD (use default)
//       return Arrays.copyOf(header, header.length);
//    }
//
//    //===============================================================
//    /**
//     * Gets the specified chunk
//     *
//     * @param theName
//     * @return
//    */
//    public Set<PngChunk> getChunks( String theName ) {
//
//        Set<PngChunk> theChunkSet = new HashSet<>();        
//        for(PngChunk aChunk : theChunks ){
//            if( aChunk.getLabel().equals(theName) ){
//                theChunkSet.add(aChunk);
//            }
//        }
//       
//        return theChunkSet;
//    }
//
//    //===============================================================
//    /**
//     * Adds a chunk to the map
//     *
//     * @param theChunk
//    */
//    public void addChunk( PngChunk theChunk ){
//        theChunks.add( theChunk );        
//    }
//
//     //===============================================================
//    /**
//     * Adds a chunk to the map
//     *
//     * @return
//    */
//    private void insertChunk(String theName, PngChunk theChunk){
//        theChunks.add( theChunks.size() - 1, theChunk );           
//    }
//    
//     //===============================================================
//    /**
//     * Adds a chunk to the map
//     *
//     * @param theName
//     * @param theChunk
//    */
//    public void replaceChunk( String theName, PngChunk theChunk){
//        
//        //Remove the chunks
//        removeChunks(theName);
//        
//        //Insert the new one
//        insertChunk(theName, theChunk);
//        
//    }
//
//    //===============================================================
//    /**
//     * Writes the picture to disk
//     *
//     * @throws pwnbrew.log.LoggableException
//    */
//    public void writeToDisk() throws LoggableException {
//
//        try {
//            
//            FileOutputStream fos= new FileOutputStream( thePNGFile );
//            BufferedOutputStream theBOS = new BufferedOutputStream(fos);
//
//            try {
//
//                //Copy the header
//                theBOS.write(header, 0, header.length);
//
//                Iterator<PngChunk> theIterator = theChunks.iterator();
//                while( theIterator.hasNext() ) {
//
//                    //Get the chunk and add its bytes
//                    PngChunk aChunk = theIterator.next();
//                    byte[] chunkBytes = aChunk.getBytes();
//
//                    //Write each chunk
//                    theBOS.write(chunkBytes, 0, chunkBytes.length);
//
//                }
//
//            } finally {
//
//                //Close the file
//                theBOS.flush();
//                theBOS.close();
//            }
//
//        } catch (IOException ex){
//            throw new LoggableException(ex);
//        }
//    }
//
//    //===============================================================
//    /**
//     *  Remove any chunks that match the label.
//     * 
//     * @param passedLabel 
//     */
//    public void removeChunks(String passedLabel) {
//        
//        List<PngChunk> tempList = new ArrayList<>(theChunks);
//        for( PngChunk aChunk : tempList ){
//            if( aChunk.getLabel().equals(passedLabel )){
//                theChunks.remove(aChunk);
//            }
//        }
//    }
//
//
//    //===============================================================
//    /**
//     * Inner class for representing the png chunks
//     *
//    */
//    public static class PngChunk {
//
//        private final byte[] length = new byte[4];
//        private final byte[] label = new byte[4];
//        private byte[] value = null;
//        private final byte[] crc = new byte[4];
//
//        public PngChunk(byte[] passedLength, byte[] passedLabel, byte[] passedValue, byte[] passedCrc) throws LoggableException {
//
//            //Copy the length
//            if(passedLength != null && passedLength.length == 4){
//                System.arraycopy( passedLength, 0, length, 0, length.length );
//            } else {
//                throw new LoggableException("Invalid PNG chunk length");
//            }
//
//            //Copy the label
//            if(passedLabel != null && passedLabel.length == 4){
//                System.arraycopy( passedLabel, 0, label, 0, label.length );
//            } else {
//                throw new LoggableException("Invalid PNG chunk label");
//            }
//
//            //Copy the value
//            if(passedValue != null){
//                value = new byte[passedValue.length];
//                System.arraycopy( passedValue, 0, value, 0, value.length );
//            } else {
//                throw new LoggableException("Invalid PNG chunk value");
//            }
//
//            //Copy the crc if it's not null, otherwise calculate
//            if(passedCrc != null && passedLabel.length == 4){
//                System.arraycopy( passedCrc, 0, crc, 0, crc.length );
//            } else {
//
//                //Get the crc
//                CRC32 aCRCgen = new CRC32();
//                aCRCgen.update(label);
//                aCRCgen.update(value);
//
//                long theCrc = aCRCgen.getValue();
//                byte[] theCrcArr = SocketUtilities.longToByteArray(theCrc);
//                System.arraycopy( theCrcArr, 0, crc, 0, crc.length );
//
//            }
//        }
//
//        //===============================================================
//        /**
//        * Returns the label for the chunk
//        *
//        * @return
//        */
//        public String getLabel() {
//            String retStr = "";
//            try {
//                retStr = new String(label, "US-ASCII");
//            } catch (UnsupportedEncodingException ex) {
//                ex = null;
//            }
//            return retStr; 
//        }
//
//       //===============================================================
//       /**
//        * Returns the chunk value.
//        *
//        * @return
//       */
//       public byte[] getValue() {
//          return Arrays.copyOf(value, value.length);
//       }
//
//       //===============================================================
//       /**
//        * Returns the byte representation of the chunk.
//        *
//        * @return
//       */
//       public byte[] getBytes(){
//
//           int marker = 0;
//           byte[] rtnBuffer = new byte[ length.length +  label.length + value.length + crc.length];
//
//           //Add the length
//           System.arraycopy( length, 0, rtnBuffer, marker, length.length );
//           marker = marker + length.length;
//
//           //Add the label
//           System.arraycopy( label, 0, rtnBuffer, marker, label.length );
//           marker = marker + label.length;
//
//           //Add the value
//           System.arraycopy( value, 0, rtnBuffer, marker, value.length );
//           marker = marker + value.length;
//
//           //Get the crc
//           CRC32 aCRCgen = new CRC32();
//           aCRCgen.update(label);
//           aCRCgen.update(value);
//
//           long theCrc = aCRCgen.getValue();
//           byte[] theCrcArr = SocketUtilities.longToByteArray(theCrc);
//           System.arraycopy( theCrcArr, 0, crc, 0, crc.length );
//
//
//           //Add the crc
//           System.arraycopy( crc, 0, rtnBuffer, marker, crc.length );
//
//           return rtnBuffer;
//       }
//
//       @Override
//       public String toString(){
//           
//           String theStr = "";
//           try {
//              return new String(label, "US-ASCII");
//           } catch (UnsupportedEncodingException ex) {
//              ex = null;
//           }
//           return theStr;
//       }
//
//
//    }
//
//}/* END CLASS PNG */
