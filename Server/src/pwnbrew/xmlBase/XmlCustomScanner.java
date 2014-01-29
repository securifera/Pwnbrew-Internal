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
* XmlCustomScanner.java
*
* Created on June 25, 2013, 10:31:17 PM
*/

package pwnbrew.xmlBase;

import pwnbrew.exception.XmlContentHandlingException;
import pwnbrew.logging.Log;
import pwnbrew.logging.LoggableException;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import org.apache.xerces.impl.XMLEntityManager.ScannedEntity;
import org.apache.xerces.impl.XMLNSDocumentScannerImpl;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XNIException;
import pwnbrew.generic.gui.GenericProgressDialog;
import pwnbrew.misc.Base64Converter;
import pwnbrew.misc.Constants;
import pwnbrew.misc.Directories;
import pwnbrew.utilities.FileUtilities;
import pwnbrew.misc.ProgressDriver;
import pwnbrew.misc.ProgressListener;
import pwnbrew.utilities.ReflectionUtilities;
import pwnbrew.utilities.Utilities;

/**
 *
 *  
 */
@SuppressWarnings("ucd")
public class XmlCustomScanner extends XMLNSDocumentScannerImpl implements ProgressDriver {

         private long currentFileSize = 0;
         private static final String closeQuoteError = "CloseQuoteExpected";
         private static final int B64DECODE_BUFFER_SIZE = 4202;

         private static final String NAME_Class = XmlCustomScanner.class.getSimpleName();

         public XmlCustomScanner() {
            super();
            this.setProperty(fAmpSymbol, this);
         }

         /**
         * Scans an attribute value and normalizes whitespace converting all
         * whitespace characters to space characters.
         *
         * [10] AttValue ::= '"' ([^<&"] | Reference)* '"' | "'" ([^<&'] | Reference)* "'"
         *
         * @param value The XMLString to fill in with the value.
         * @param nonNormalizedValue The XMLString to fill in with the
         *                           non-normalized value.
         * @param atName The name of the attribute being parsed (for error msgs).
     * @param eleName
         * @param checkEntities true if undeclared entities should be reported as VC violation,
         *                      false if undeclared entities should be reported as WFC violation.
         *
         * <strong>Note:</strong> This method uses fStringBuffer2, anything in it
         * at the time of calling is lost.
     * @return 
     * @throws java.io.IOException 
         **/
        @Override
        protected boolean scanAttributeValue(XMLString value, XMLString nonNormalizedValue,
                String atName, boolean checkEntities, String eleName) throws IOException, XNIException {

           boolean retVal;
           //This particular class can cause heap overflows if the file is large
           if(eleName.equals(FileContent.NAME_Class) ){

               if( atName.equals(FileContent.ATTRIBUTE_Base64)){
//                   byte[] decompressBytes = null;
//                   boolean fileFinished = false;;

                   // quote
                   int quote = fEntityScanner.peekChar();
                   if (quote != '\'' && quote != '"') {
                      reportFatalError("OpenQuoteExpected", new Object[]{eleName,atName});
                   }

                   fEntityScanner.scanChar();
                   ScannedEntity theEntity = fEntityManager.getCurrentEntity();
                   
                   //Need to get the chars from position to count from the
                   //Setup the list of objects needed for the progress function
                   List progressList = new ArrayList();
                   progressList.add(theEntity);
                
                   //Import the files with progress
                   GenericProgressDialog pDialog = new GenericProgressDialog(null,
                            "Importing files to library...", this, false, progressList);
                   pDialog.setVisible(true);

                   //See if an error occurred
                   String retStr = pDialog.getReturn();
                   if(retStr != null){
                      if(retStr.equals(closeQuoteError)){
                         reportFatalError(closeQuoteError, new Object[]{eleName,atName});
                      } else {
                         throw new IOException(retStr);
                      }
                   }
                              
                   currentFileSize = 0;
                   char[] emptyChar = new char[1];
                   
                   //Set the value
                   value.setValues( emptyChar, 0, 1);
                   nonNormalizedValue.setValues(emptyChar, 0, 1);

                   return true;

               } else if(atName.equals(FileContent.ATTRIBUTE_Size)){

                  //Get the file size
                  retVal = super.scanAttributeValue(value, value, atName, checkEntities, eleName);
                  currentFileSize = Long.parseLong(value.toString());
                  return retVal;

               }

           }

           //Call super
           retVal = super.scanAttributeValue(value, value, atName, checkEntities, eleName);
           
           //Decode the XML
           String decodedVal = XmlUtilities.decode(value);
           value.setValues( decodedVal.toCharArray(), 0, decodedVal.length());
           
           return retVal;
        }

    // ==========================================================================
    /**
    *   Uses reflection to write the remains in the entity buffer to disk.
    *
    **/
    private char[] getBuffer( ScannedEntity theEntity, long theFileSize ) throws XmlContentHandlingException, IllegalArgumentException, IllegalAccessException, LoggableException {

        char[] theRemainder = null;
      
        Object anObj = ReflectionUtilities.getValue(theEntity, "ch");
        if(anObj != null && anObj instanceof char[]){
           char[] theCharArray = (char[])anObj;

           anObj = ReflectionUtilities.getValue(theEntity, "position");
           if(anObj != null && anObj instanceof Integer){
              int position = (Integer)anObj;

              anObj = ReflectionUtilities.getValue(theEntity, "count");
              if(anObj != null && anObj instanceof Integer){
                 int count = (Integer)anObj;

                 int remainder = count - position;
                 if(theFileSize > remainder){

                    //Get the string
                    theRemainder = Arrays.copyOfRange(theCharArray, position, count);

                    //Set the position to the count
                    ReflectionUtilities.setValue(theEntity, "position", Integer.valueOf(count));
                 } else {

                     //Calculate the amount to read
                    int range = (int) (position + theFileSize);

                    //Get the string
                    theRemainder = Arrays.copyOfRange(theCharArray, position, range );

                    //Set to the position of right after the value and quote
                    ReflectionUtilities.setValue(theEntity, "position", range + 1);
                 }

              } else {
                 throw new XmlContentHandlingException("Unable to find 'count' declared variable.");
              }

           } else {
              throw new XmlContentHandlingException("Unable to find 'position' declared variable.");
           }
        } else {
           throw new XmlContentHandlingException("Unable to find 'ch' declared variable.");
        }

        return theRemainder;

    }

   //======================================================================
   /**
    * The functionality being performed while the progress bar is moving
    * <p>
     * @param progressListener
     * @param passedObjects
     * @return 
   */
    @Override
   public String executeFunction(ProgressListener progressListener, List passedObjects) {

       String retStr = null;
       ScannedEntity theEntity = (ScannedEntity)passedObjects.get(0);

       try {
           //Create a temp file until the hash is computed
           File outFile = File.createTempFile("lib_", null);
           FileOutputStream theOutStream = new FileOutputStream(outFile);
           BufferedOutputStream theBOS = new BufferedOutputStream(theOutStream);
//           Inflater theDecompresser = null;
           String theHash = null;

           try {

               MessageDigest hash = MessageDigest.getInstance(Constants.HASH_FUNCTION);
               byte[] theReadBuf = new byte[B64DECODE_BUFFER_SIZE];
               byte[] theHeader = Utilities.getFileHeader();

               int bytesRead = 0;
               long theFileSize = currentFileSize;
               boolean readQuote = false;

               //Variables for progress updates
               int sndFileProgress = 0;
               int tempProgressInt = 0;
               double tempProgressDouble = 0;
               long byteCounter = 0;

               //Write the header
               theBOS.write(theHeader, 0, theHeader.length);
               hash.update(theHeader, 0, theHeader.length);

               //Write the remainder from the buffer first
               char[] initialBuffer = getBuffer(theEntity, theFileSize);
               StringBuilder aSB = new StringBuilder().append(initialBuffer);

               //Read in the rest of a full decode buffer
               if( theFileSize > initialBuffer.length){

                  //Set the flag to read the ending quote
                  readQuote = true;

                  //Ensure that we only read as much as needed
                  int bufferSize = B64DECODE_BUFFER_SIZE - initialBuffer.length;
                  if( theFileSize < B64DECODE_BUFFER_SIZE ){
                     bufferSize = (int)(theFileSize - initialBuffer.length);
                  }

                  byte[] tempArr = new byte[bufferSize];
                  bytesRead = theEntity.stream.read(tempArr);
                  aSB.append(new String(tempArr, "US-ASCII"));
               }

               //Decode the first set of bytes and write it
               byte[] decodedBytes = Base64Converter.decode(aSB.toString());
               theBOS.write(decodedBytes, 0, decodedBytes.length);
               hash.update(decodedBytes, 0, decodedBytes.length);

               //Subtract from overall bytes
               theFileSize -= bytesRead + initialBuffer.length;

               //Read the base64 content
               while( theFileSize > 0){

                   if(theReadBuf.length > theFileSize){
                      theReadBuf = new byte[(int)theFileSize];
                   }

                   bytesRead = theEntity.stream.read(theReadBuf);
                   if(bytesRead != theReadBuf.length){
                      theReadBuf = Arrays.copyOf(theReadBuf, bytesRead);
                   }

                   //Update counters
                   byteCounter += bytesRead;
                   theFileSize -= bytesRead;

                   //Check for divide by zero
                   if(progressListener != null){
                       if(currentFileSize != 0){
                          tempProgressDouble = (1.0 * byteCounter) / (1.0 * currentFileSize );
                          tempProgressInt = (int)Math.round(tempProgressDouble * 100.0);
                       }

                       if(tempProgressInt != sndFileProgress){
                          progressListener.progressChanged(0, tempProgressInt);
                          sndFileProgress = tempProgressInt;
                       }
                   }

                   //Decode the string
                   decodedBytes = Base64Converter.decode(new String(theReadBuf, "US-ASCII"));

                   //Decompress if necessary
//                   if(useCompression){
//                      //Create the decompresser
//                      theDecompresser = new Inflater();
//                      theDecompresser.setInput(decodedBytes);
//
//                      //Get the decompressed bytes
//                      decompressedBytes = new byte[decodedBytes.length];
//                      int newSize = theDecompresser.inflate(decompressedBytes);
//                      decodedBytes = new byte[newSize];
//                      decodedBytes = Arrays.copyOf(decompressedBytes, newSize);
//                   }

                   //Write to disk
                   theBOS.write(decodedBytes, 0, decodedBytes.length);
                   hash.update(decodedBytes, 0, decodedBytes.length);

               }

               //Read the ending quote
               if(readQuote){
                  //Ensure the next char is the end quote
                  int c = theEntity.reader.read();
                  if(c != '\"'){
                     retStr = closeQuoteError;
                  }
               }
               

               byte[] byteHash = hash.digest();
               theHash = Utilities.byteToHexString(byteHash);
               theBOS.flush();


           } finally {
               theOutStream.getFD().sync();
               theBOS.close();
           }

           //Rename the file flush
           File fileContentDir = Directories.getFileLibraryDirectory();
           File finFile = new File(fileContentDir, theHash);
           if(!finFile.exists()){

                //Rename the file that already exists
                File dstFile = new File(fileContentDir, theHash);
                if(!outFile.renameTo(dstFile)){
                    FileUtilities.moveFile(outFile, dstFile);
                }

           } else {
               FileUtilities.deleteFile(outFile);
           }

       } catch ( XmlContentHandlingException | IllegalArgumentException | IllegalAccessException | LoggableException | NoSuchAlgorithmException | IOException ex) {
           Log.log(Level.SEVERE, NAME_Class, "IProgressLogic_Run()", ex.getMessage(), ex );
           retStr = ex.getMessage();
       }
      
       return retStr;
   }

}/* END CLASS XmlCustomScanner */
