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
 * FileUtilities.java
 *
 * Created on June 23, 2013, 11:21 PM
 */

package pwnbrew.utilities;

import java.awt.Desktop;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import pwnbrew.logging.Log;
import pwnbrew.misc.Constants;
import pwnbrew.misc.Directories;
import pwnbrew.misc.LibraryFileCopyListener;
import pwnbrew.misc.ProgressListener;

/**
 */
abstract public class FileUtilities {
  
    private static final String NAME_Class = "FileUtilities";

    // ==========================================================================
    /**
    * Verifies that the file represented by the given {@link File} can be read
    * to by the application.
    * <p>
    * If the file doesn't exist, this method <strong>will not</strong> attempt to
    * create it.
    * 
    * @param file a <tt>File</tt> representing the file to verify
    * 
    * @return <tt>true</tt> if the file can be read; <tt>false</tt>
    * otherwise, specifically if the given {@link File} is <tt>NULL</tt>,
    * represents a directory, or represents a file that doesn't exist or cannot
    * be read by the application
    */
    public static boolean verifyCanRead( File file ) {

        boolean rtnBool = false;

        if( file != null ) { //If the File is not NULL...

            //NOTE: isFile() returns false if the file doesn't not exist
            if( file.isFile() ) { //If the File represents a "normal" file...
                rtnBool = file.canRead(); //Check if it can be read
            }

        }

        return rtnBool;

    }/* END verifyCanRead( File ) */

    // ==========================================================================
    /**
    * Verifies that the file represented by the given {@link File} can be written
    * to by the application.
    * <p>
    * If the file doesn't exist, this method will attempt to create it, but
    * <strong>will not</strong> attempt to create any parent directories.
    * 
    * @param file a <tt>File</tt> representing the file to verify
    * 
    * @return <tt>true</tt> if the file can be written ; <tt>false</tt>
    * otherwise, specifically if the given {@link File} is <tt>NULL</tt>,
    * represents a directory, or represents a file that cannot be created or
    * to which the application cannot write
    * 
    * @throws IOException If an I/O error occurs
    */
    public static boolean verifyCanWrite( File file ) throws IOException {

        boolean rtnBool = false;

        if( file != null ) { //If the File is not NULL...

            if( file.isDirectory() == false ) { //If the File is not a directory...

                boolean fileExists = false;

                if( file.exists() ) { //If the file exists...
                    fileExists = true;
                } else { //If the file doesn't exist...

                    File parentDir = file.getParentFile(); //Get the file's parent directory

                    if( parentDir.exists() ) { //If the parent directory exists...
                        fileExists = file.createNewFile(); //Try to create a new file
                    }

                }

                if( fileExists ) { //If the file exists...
                    rtnBool = file.canWrite(); //Check if it can be written to
                }

            }

        }

        return rtnBool;

    }/* END verifyCanWrite() */

    // ==========================================================================
    /**
    * Deletes the directory represented by the given {@link File}.
     * @param dir
     * @throws java.io.IOException
    */
    public static void deleteDir(File dir) throws IOException{

        if(dir != null && dir.exists()){
            
            if(!dir.isDirectory())
                throw new IOException("Not a directory " + dir);            
            
             File[] files = dir.listFiles();
            for( File aFile : files){

                if(aFile.isDirectory()){
                    deleteDir(aFile);
                } else {
                    boolean fileDeleted = aFile.delete();
                    if(!fileDeleted){
                        throw new IOException("Unable to delete file " + aFile);
                    }
                }

            }

            //If unable to delete throw
            if(!dir.delete()){
                throw new IOException("Unable to delete file " + dir);
            }
            
        }
        
    }

    // ==========================================================================
    /**
    * Deletes the file or directory represented by the given {@link File}.
    * <p>
    * If the given {@code File} is null or the file/directory it represents does
    * not exist, this method does nothing and returns null.
    * <p>
    * If the given {@code File} represents a directory, all of the directory's
    * contents will be deleted. If any of the directory's contents cannot be deleted,
    * this method will delete all of the contents that can be deleted and return
    * false.
    * 
    * @param file a {@code File} representing the file/directory to be deleted
    *
    * @return <tt>true</tt> if and only if the file/directory was deleted, <tt>false</tt>
    * if the file/directory could not be deleted, null if the file/directory represented
    * by the given {@code File} does not exist
    */
    public static boolean deleteFile( File file ) {

        if( file == null ) { //If the File is null...
            throw new NullPointerException(); //Do nothing
        }

        boolean rtnBool = false;

        if( file.exists() ) { //If the file exists...

            //Ensure it is not readonly
            file.setReadable(true);

            if( file.isDirectory() ) { //If the File represents a directory...

                //Attempt to delete all of the files and directories in the directory. If
                //  any cannot be deleted, return false after deleting all that can be.

                File[] fileList = file.listFiles(); //Get the contents of the directory...

                boolean allContentsDeleted = true;
                for( File aFile : fileList ) { //For each file/dir in the directory...

                    Boolean fileDeleted = deleteFile( aFile ); //Delete the file/dir
                    if( fileDeleted == false ) { //If the file/dir could not be deleted...
                        allContentsDeleted = false; //Not all of the contents were deleted
                    } //Else, it's already gone / Could this ever occur?

                }

                if( allContentsDeleted ) { //If all of the directory's contents were deleted...

                    try {
                        rtnBool = file.delete(); //Delete the directory
                    } catch( SecurityException ex ) {
                        //Do nothing / Return false
                        ex = null;
                    }

                } //Else, some of the directory's contents were not deleted / Return false

            } else { //If the File does not represent a directory...

                try {
                    rtnBool = file.delete(); //Delete the file
                } catch( SecurityException ex ) {
                    //Do nothing / Return false
                    ex = null;
                }

            }

        } //End of "if( file.exists() ) { //If the file exists..."
        //Else, there's nothing to delete / Return null

        return rtnBool;

    }/* END deleteFile( File ) */


//    // ==========================================================================
//    /**
//    *    Wrapper function. 
//     * @param aFile
//     * @return 
//     * @throws java.security.NoSuchAlgorithmException 
//     * @throws java.io.IOException 
//    */
//    public static String getFileHash(File aFile) throws NoSuchAlgorithmException, IOException {
//        return getFileHash(aFile, false);
//    }

   // ==========================================================================
  /**
   * Returns the SHA-256 hash for the passed file
   *
   * @param file a {@code File} representing the file from which the {@code FileContent}
   * is to be created
   *
   * @return a {@code String} representing the hash of the file
   *
   * @throws FileNotFoundException if the given {@code File} represents a directory,
   * or represents a file that doesn't exist or cannot be read by the application
   * @throws IOException if a problem occurs while reading the file
   * @throws NoSuchAlgorithmException if the hash algorithm cannot be found
   * @throws NullPointerException if the given {@code File} is null
   */
  public static String getFileHash( File aFile ) throws NoSuchAlgorithmException, IOException {

    int bytesRead = 0;
    byte[] byteBuffer = new byte[Constants.GENERIC_BUFFER_SIZE];
    String theHash = "";

    if( aFile == null ) { //If the given File is null...
      throw new NullPointerException();
    }

    if( FileUtilities.verifyCanRead( aFile ) ) { //If the file can be read...

        MessageDigest hash = MessageDigest.getInstance(Constants.HASH_FUNCTION);
        FileInputStream theFileStream = new FileInputStream(aFile);
        BufferedInputStream theBufferedIS = new BufferedInputStream(theFileStream);

        try {

//            //Add the header to the hash if it was removed
//            if(addHeader){
//                hash.update( Utilities.getFileHeader() );
//            }
            
            //Read in the bytes and update the hash
            while( bytesRead != -1){
                bytesRead = theBufferedIS.read(byteBuffer);
                if(bytesRead > 0){
                   hash.update(byteBuffer, 0, bytesRead);
                }
            }

            byte[] byteHash = hash.digest();
            theHash = Utilities.byteToHexString(byteHash);

        } finally {
            
           //Ensure the file is closed
           try {
              theBufferedIS.close();
           } catch(IOException ex){
              ex = null;
           }
        }

    } else { //If the file doesn't exist or is not actually a file...
      throw new FileNotFoundException();
    }

    return theHash;

  }/* END getFileHash( File ) */

    

    // ==========================================================================
    /**
    * Moves one file to another using file channels
    *
    * @param srcFile a {@code File} representing the source file 
    * @param dstFile a {@code File} representing the source file 
    *
    * @throws FileNotFoundException if the given {@code File} represents a directory,
    * or represents a file that doesn't exist or cannot be read by the application
    * @throws IOException if a problem occurs while reading the file
    * @throws NullPointerException if the given {@code File} is null
    */
    public static void moveFile( File srcFile, File dstFile ) throws IOException {
        
        FileInputStream aFIS = new FileInputStream(srcFile);
        try{
            FileOutputStream aFOS = new FileOutputStream(dstFile);
            try{

                //Copy the file manually
                FileChannel inputChannel = aFIS.getChannel();
                FileChannel outputChannel = aFOS.getChannel();

                inputChannel.transferTo(0, inputChannel.size(), outputChannel);

            } finally {
                aFOS.close();
            }         

        } finally {
            aFIS.close();
        }
        
        //Delete the file later
        srcFile.deleteOnExit();
    }

    //===============================================================
    /**
     * Creates an empty file given the parent dir and a hash-filename string
     *
     * @param passedParentDir the parent directory file
     * @param hashFileNameStr a string representing the hash:filename of a file
     * @throws java.io.IOException
    */
    public static void createEmptyFile(File passedParentDir, String hashFileNameStr) throws IOException {

        if(passedParentDir != null){

            Directories.ensureDirectoryExists(passedParentDir);
            String[] fileHashFileNameArr = hashFileNameStr.split(":");
            String fileName = fileHashFileNameArr[1];

            File localFile = new File( passedParentDir, fileName);
            if(localFile.exists() && !localFile.delete()){
                throw new IOException("File already exists, the hash does not matach, and was unable to remove it.");
            }

            //Try to open a file stream to the file and then close it
            FileOutputStream aFileStream = new FileOutputStream(localFile, true);
            aFileStream.close();

        }


    }
    
    // ==========================================================================
    /**
    * Reads the lines from a file, represented by the given {@link File}.
    * <p>
    * The returned {@link ArrayList} represents the entire file with each
    * <tt>String</tt> element in the list representing one line of the file.
    * <strong>NOTE: Line terminators are consumed by the reading
    * process.</strong> A line is considered to be terminated by any one of a
    * line feed ('\n'), a carriage return ('\r'), or a carriage return followed
    * immediately by a linefeed
    * 
    * @param file a <tt>File</tt> representing the file to read
    * 
    * @return the data in the file; <tt>NULL</tt> otherwise, specifically if the
    * given <tt>File</tt> is <tt>NULL</tt>, represents a directory, or represents
    * a file that doesn't exist or cannot be read by the application
    * 
    * @throws IOException
    */
    public static List<String> readFileLines( File file ) throws IOException {

        if( verifyCanRead( file ) == false ) { //If the file cannot be read...
            return null; //Do nothing
        }

        ArrayList<String> rtnList = new ArrayList<>();
        char[] theByteArray = new char[2];
        InputStreamReader theFileReader = new InputStreamReader( new FileInputStream( file), "UTF-8");
        try {
            int charsRead = theFileReader.read(theByteArray, 0, theByteArray.length);

            if(charsRead > 0){
                if(theByteArray[0] == '\ufffd' && theByteArray[1] == '\ufffd'){
                    theFileReader.close();
                    theFileReader = new InputStreamReader( new FileInputStream( file), "UTF-16");
                } else {
                    theFileReader.close();
                    theFileReader = new InputStreamReader( new FileInputStream( file), "US-ASCII");
                }

                String aFileLine;
                BufferedReader theBufferedReader = new BufferedReader( theFileReader );
                try {

                    while( ( aFileLine = theBufferedReader.readLine() ) != null ) { //While there is a line to process...
                    rtnList.add( aFileLine ); //Add the line to the list
                    }

                } finally {

                    try {
                        theBufferedReader.close();
                    } catch( IOException ex ) {
                        ex = null;
                    }

                }

            }

        } finally {

            //Close the file reader
            try {
                theFileReader.close();
            } catch (IOException ex){
                ex = null;
            }

        }

        return rtnList;

    }/* END readFileLines() */
    
    // ==========================================================================
    /**
    * Reads the lines from a file, represented by the given {@link File}.
    * <p>
    * The returned {@link ArrayList} represents the entire file with each
    * <tt>String</tt> element in the list representing one line of the file.
    * <strong>NOTE: Line terminators are consumed by the reading
    * process.</strong> A line is considered to be terminated by any one of a
    * line feed ('\n'), a carriage return ('\r'), or a carriage return followed
    * immediately by a linefeed
    * 
    * @param file a <tt>File</tt> representing the file to read
    * 
    * @return the data in the file; <tt>NULL</tt> otherwise, specifically if the
    * given <tt>File</tt> is <tt>NULL</tt>, represents a directory, or represents
    * a file that doesn't exist or cannot be read by the application
    * 
    * @throws IOException
    */
    public static byte[] readFile( File file ) throws IOException {

        if( verifyCanRead( file ) == false ) { //If the file cannot be read...
            return null; //Do nothing
        }

        ByteBuffer aBB = ByteBuffer.allocate(1048576);
        char[] theByteArray = new char[2];
        InputStreamReader theFileReader = new InputStreamReader( new FileInputStream( file), "UTF-8");
        try {
            int charsRead = theFileReader.read(theByteArray, 0, theByteArray.length);

            if(charsRead > 0){
                if(theByteArray[0] == '\ufffd' && theByteArray[1] == '\ufffd'){
                    theFileReader.close();
                    theFileReader = new InputStreamReader( new FileInputStream( file), "UTF-16");
                } else {
                    theFileReader.close();
                    theFileReader = new InputStreamReader( new FileInputStream( file), "US-ASCII");
                }

                int aByte;
                BufferedReader theBufferedReader = new BufferedReader( theFileReader );
                try {

                    while( ( aByte = theBufferedReader.read() ) != -1 ) { //While there is a line to process...
                        aBB.put( (byte)aByte ); //Add the line to the list
                    }

                } finally {

                    try {
                        theBufferedReader.close();
                    } catch( IOException ex ) {
                        ex = null;
                    }

                }

            }

        } finally {

            //Close the file reader
            try {
                theFileReader.close();
            } catch (IOException ex){
                ex = null;
            }

        }

        return Arrays.copyOf(aBB.array(), aBB.position());

    }/* END readFile() */
    
     //===============================================================
    /**
     * Converts the library file to the File path passed
     *
     * @param fileHash
     * @param destFile
     * @param theListener
     * @return
     * @throws java.io.IOException
     * @throws pwnbrew.utilities.FileUtilities.InvalidFileLengthException
     * @throws pwnbrew.utilities.FileUtilities.InvalidLibraryFileException
    */
    public static boolean convertLibFile(String fileHash, File destFile, ProgressListener theListener) throws IOException, InvalidLibraryFileException, InvalidFileLengthException{
        return convertLibFile(fileHash, destFile, theListener, Directories.getFileLibraryDirectory());
    }

    //===============================================================
    /**
     * Converts the library file to the File path passed
     *
     * @param fileHash
     * @param destFile
     * @return
    */
    private static boolean convertLibFile(String fileHash, File destFile, ProgressListener theListener, File fileContentDir ) throws IOException, InvalidLibraryFileException, InvalidFileLengthException {

//        byte[] fileHeader = Utilities.getFileHeader();

//        byte[] headerBytes = new byte[fileHeader.length];
        byte[] byteBuffer = new byte[Constants.GENERIC_BUFFER_SIZE];
        int marker = 0;

        //Variables for progress updates
        int sndFileProgress = 0;
        int tempProgressInt = 0;
        double tempProgressDouble;
        long writeByteCounter = 0;
        long tempFileSize;
 
        File aFile = new File(fileContentDir, fileHash);
        if( FileUtilities.verifyCanRead( aFile ) ) { //If the file can be read...

            tempFileSize = aFile.length();
            if(tempFileSize == 0){
               throw new InvalidFileLengthException("This file contains 0 bytes.");
            }

            FileInputStream theFileStream = new FileInputStream(aFile);
            BufferedInputStream theBufferedIS = new BufferedInputStream(theFileStream);
            try {

                FileOutputStream theOutStream = new FileOutputStream(destFile);
                BufferedOutputStream theBOS = new BufferedOutputStream(theOutStream);

                try {

//                    //Check the header
//                    int bytesRead = theBufferedIS.read(headerBytes, marker, fileHeader.length);
//                    if(bytesRead != 4 && !Arrays.equals(headerBytes, fileHeader)){
//                      throw new InvalidLibraryFileException("This file does not contain the required header.");
//                    }

                    //Reset bytes read 
                    int bytesRead = 0;
                    while( bytesRead != -1){

                        writeByteCounter += bytesRead;

                        //Calculate the progress
                        //Check for divide by zero
                        if(theListener != null){
                            if(tempFileSize != 0){
                               tempProgressDouble = (1.0 * writeByteCounter) / (1.0 * tempFileSize );
                               tempProgressInt = (int)Math.round(tempProgressDouble * 100.0);
                            }

                            if(tempProgressInt != sndFileProgress){
                               theListener.progressChanged(0, tempProgressInt);
                               sndFileProgress = tempProgressInt;
                            }
                        }

                        theBOS.write(byteBuffer, 0, bytesRead);
                        bytesRead = theBufferedIS.read(byteBuffer);
                    }

                    theBOS.flush();

                } finally {

                   //Close output stream
                   try{
                      theBOS.close();
                   } catch(IOException ex){
                      ex = null;
                   }
                }
                

            //Make sure to clean up the file streams
            } finally {

                //Close input stream
                try {
                   theBufferedIS.close();
                } catch(IOException ex){
                   ex = null;
                }
            }
            
        } else {
            return false;
        }
        
        return true;
    }
    
    // ==========================================================================
    /**
     * Creates a copy of the library file identified by the given name at the location
     * represented by the given {@link File}.
     *
     * @param libFileName the name of the library file to copy
     * @param destination a {@code File} representing where to place the file
     * @param listener 
     */
    public static void copyLibraryFile( String libFileName, File destination, LibraryFileCopyListener listener ) {
        
        //NOTE: The logic in this method should be placed in a dedicated handler class
        //  which should be equipped with mechanisms to enable the canceling of the
        //  file copy and to clean up files created during canceled or failed copies.
        
        //NOTE: This method currently maintains the requirement that the library files
        //  begin with the header bytes. If the purpose of the header could be
        //  accomplished by some other fashion then the complexity of this method could
        //  be greatly reduced.
        
        if( libFileName == null ) throw new IllegalArgumentException( "The library file name cannot be null." );
        if( libFileName.isEmpty() ) throw new IllegalArgumentException( "The library file name cannot be empty." );
        if( destination == null ) throw new IllegalArgumentException( "The destination File cannot be null." );
        
        boolean fileCopyCompleted = false;
        String errorString = null;
        
        File libraryFile = new File( Directories.getFileLibraryDirectory(), libFileName ); //Create a File to represent the library file to be copied
        if( FileUtilities.verifyCanRead( libraryFile ) ) { //If the library file can be read...
            
            long libFileLength = libraryFile.length();
            if( libFileLength > 0 ) { //If the library file length is at least 1...
                
                //Create a BufferedInputStream to read the library file...
                BufferedInputStream bufferedInputStream = null;
                try {
                    bufferedInputStream = new BufferedInputStream( new FileInputStream( libraryFile ) );
                } catch( FileNotFoundException ex ) {
                    errorString = "The library file could not be found.";
                }
                
                if( bufferedInputStream != null ) { //If the BufferedInputStream was created...
                    
                    //Check the header of the library file...
//                    byte[] fileHeader = Utilities.getFileHeader();
//                    byte[] fileHeaderBytes = new byte[ fileHeader.length ];
                    int bytesRead;
//                    boolean hasHeader = false;
//                    try {
//                        
//                        //Read the bytes of the library file's header
//                        bufferedInputStream.read( fileHeaderBytes, 0, fileHeader.length );
//                        if( Arrays.equals( fileHeaderBytes, fileHeader ) ) {
//                            hasHeader = true; //The library file has the header
//                        } else { //If the library file does not have the header...
//                            errorString = "The library file does not have the header.";
//                        }
//                        
//                    } catch( IOException ex ) {
//                        errorString = "Could not read the library file.";
//                    }

//                    if( hasHeader ) { //If the library file has the header...
                        
                        BufferedOutputStream bufferedOutputStream = null;
                        try {
                            bufferedOutputStream = new BufferedOutputStream( new FileOutputStream( destination ) );
                        } catch( FileNotFoundException ex ) {
                            errorString = "The destination file could not be found.";
                        }
                        
                        if( bufferedOutputStream != null ) { //If the BufferedOutputStream was created...
                            
                            //Copy the bytes from the library file to the destination file...
                            long copiedBytesCounter = 0;
                            bytesRead = 0; //Reset
                            byte[] byteBuffer = new byte[ Constants.GENERIC_BUFFER_SIZE ];
                            
                            if( listener != null ) //If a LibraryFileCopyListener was given...
                                listener.libraryFileCopyProgress( libFileName, copiedBytesCounter, libFileLength );

                            while( bytesRead > -1 ) { //Until the end of the library file is reached...

                                if( bytesRead > 0 ) { //If any bytes have been read...
                                    
                                    //Write the bytes to the destination file...
                                    try {
                                        bufferedOutputStream.write( byteBuffer, 0, bytesRead );
                                        bufferedOutputStream.flush();
                                    } catch( IOException ex ) {
                                        errorString = "An error occured while writing to the destination file.";
                                        break; //Stop copying the file
                                    }
                                    
                                    copiedBytesCounter += bytesRead; //Update the counter
                                
                                    if( listener != null ) //If a LibraryFileCopyListener was given...
                                        listener.libraryFileCopyProgress( libFileName, copiedBytesCounter, libFileLength );

                                }
                                
                                try {
                                    bytesRead = bufferedInputStream.read( byteBuffer );
                                } catch( IOException ex ) {
                                    errorString = "An error occured while reading from the library file.";
                                    break; //Stop copying the file
                                }
                                
                                if( bytesRead == -1 ) { //If the end of the library file was reached...
                                    fileCopyCompleted = true; //The file copy was completed
                                }
                                
                            }

                            //Close the BufferedOutputStream...
                            try{
                                bufferedOutputStream.close();
                            } catch( IOException ex ) {
                                ex = null;
                            }
                            
                        } //End of "if( bufferedOutputStream != null ) { //If the BufferedOutputStream was created..."
                        
//                    } //End of "if( hasActHeader ) { //If the library file has the header..."
                    
                    //Close the BufferedInputStream...
                    try {
                        bufferedInputStream.close();
                    } catch( IOException ex ) {
                        ex = null;
                    }
                    
                } //End of "if( bufferedInputStream != null ) { //If the BufferedInputStream was created..."
                
            } else { //If the library file length is 0...
                //The library file cannot possibly contain the header which all
                //  library files should have.
                errorString = "The library file length is 0 bytes.";
            }

        } else { //If the library file cannot be read...
            errorString = "The library file cannot be read.";
        }
        
        if( listener != null ) //If a LibraryFileCopyListener was given...
            if( fileCopyCompleted ) //If the library file was copied...
                listener.libraryFileCopyCompleted( libFileName, destination );
            else //If the library file was not copied...
                listener.libraryFileCopyFailed( libFileName, destination, errorString );

    }/* END copyLibraryFile( String, File, LibraryFileCopyListener ) */
    
    /**
    *  Opens a file in the default editor and returns the file upon close
    *
    *
     * @param passedFile
     * @param passedAction
     * @return  **/
    public static boolean openFileInEditor(File passedFile, Desktop.Action passedAction) {

        boolean rtnVal = true;

        if(!passedFile.exists() && passedFile.length() < 0){
            System.err.println( "FileUtilities.openFileInEditor() - " + passedFile.getAbsolutePath() + ": does not exist.");
        }

        //If the desktop object is supported
        if (Desktop.isDesktopSupported()) {

            //Get the desktop object
            Desktop desktop = Desktop.getDesktop();

            try {
                if(desktop.isSupported(passedAction)){
                    if(passedAction.equals(Desktop.Action.OPEN)){
                        desktop.open(passedFile);
                    } else if(passedAction.equals(Desktop.Action.EDIT)){
                        desktop.edit(passedFile);
                    }
                } else {
                    rtnVal = false;
                }
            } catch (IOException ex) {

                //Try to just open the file
                if(passedAction.equals(Desktop.Action.EDIT)){
                    openFileInEditor(passedFile, Desktop.Action.OPEN);
                } else {
                    Log.log(Level.WARNING, NAME_Class, "openFileInEditor()", ex.getMessage(), ex );
                    rtnVal = false;
                }
            }

        } else {
            rtnVal = false;       
        }
        
        //Try to open with gedit
        if( rtnVal == false && Utilities.isUnix( Utilities.getOsName() ) ){
            try {

                //Try and run gedit
                Runtime.getRuntime().exec("gedit " + passedFile.getAbsolutePath());
                rtnVal = true;
                
            } catch ( IOException ex) {
            }                
        }

        return rtnVal;
    }
    
     // ==========================================================================
  /**
   * Convenience method for function below.
   *
     * @param aFile
     * @param theListener
   *
   * @return a {@code String} representing the hash of the file written to disk
   *
   * @throws FileNotFoundException if the given {@code File} represents a directory,
   * or represents a file that doesn't exist or cannot be read by the application
   * @throws IOException if a problem occurs while reading the file
   * @throws NoSuchAlgorithmException if the hash algorithm cannot be found
   * @throws NullPointerException if the given {@code File} is null
   */
   public static String createHashedFile( File aFile, ProgressListener theListener ) throws IOException, NoSuchAlgorithmException {
       return createHashedFile(aFile, theListener, Directories.getFileLibraryDirectory());   
   }
   
    // ==========================================================================
    /**
    * Creates a library file out of the passed byte array and returns the hash
    *
    * @param passedBytes a {@code byte[]} representing the bytes from which the hash file
    * is to be created
     * @param fileDir
    *
    * @return a {@code String} representing the hash of the file written to disk
    *
    * @throws FileNotFoundException if the given {@code File} represents a directory,
    * or represents a file that doesn't exist or cannot be read by the application
    * @throws IOException if a problem occurs while reading the file
    * @throws NoSuchAlgorithmException if the hash algorithm cannot be found
    * @throws NullPointerException if the given {@code File} is null
    */
    public static String createHashedFile(byte[] passedBytes, File fileDir ) throws NoSuchAlgorithmException, IOException {

        byte[] byteBuffer = new byte[4096];
        String theHash = "";

        if( passedBytes == null ) { //If the given File is null...
            throw new NullPointerException();
        }

        File outFile = File.createTempFile("lib_", null);
//        byte[] actHeader = Utilities.getFileHeader();

        ByteArrayInputStream theBytesStream = new ByteArrayInputStream(passedBytes);
        BufferedInputStream theBufferedIS = new BufferedInputStream(theBytesStream);
        try {
            
            FileOutputStream theOutStream = new FileOutputStream(outFile);
            BufferedOutputStream theBOS = new BufferedOutputStream(theOutStream);
            try {
                
                MessageDigest hash = MessageDigest.getInstance(Constants.HASH_FUNCTION);

//                //Setup the header
//                System.arraycopy(actHeader, 0, byteBuffer, 0, actHeader.length);
//                int bytesRead  = theBufferedIS.read(byteBuffer, actHeader.length, byteBuffer.length - actHeader.length);
//                bytesRead += actHeader.length;

                int bytesRead = 0;
                while( bytesRead != -1){
                    
                    //Update the hash
                    theOutStream.write(byteBuffer, 0, bytesRead);
                    hash.update(byteBuffer, 0, bytesRead);
                    bytesRead = theBufferedIS.read(byteBuffer);
                }

                byte[] byteHash = hash.digest();
                theHash = Utilities.byteToHexString(byteHash);

                //Rename the file flush the stream and close it
                theOutStream.flush();
                
            } finally {

               try {
                  theBOS.close();
               } catch(IOException ex){
                  ex = null;
               }
            }
        } finally {
            //Close the input stream
            try {
               theBufferedIS.close();
            } catch(IOException ex){
               ex = null;
            }
        }

        //Rename the file flush the stream and close it
        File finFile = new File(fileDir, theHash);
        if(!finFile.exists()){

           //Rename the file that already exists
           if(!outFile.renameTo(finFile)){
               //throw new IOException("Unable to rename file with the same hash.");
               moveFile(outFile, finFile);
           }

        }

        return theHash;

    }/* END createFileContentFromFile( byte[] ) */
  
    // ==========================================================================
    /**
    * Creates a library file out of the passed file and returns the hash
    *
    * @param file a {@code File} representing the file from which the {@code FileContent}
    * is to be created
    *
    * @return a {@code String} representing the hash of the file written to disk
    *
    * @throws FileNotFoundException if the given {@code File} represents a directory,
    * or represents a file that doesn't exist or cannot be read by the application
    * @throws IOException if a problem occurs while reading the file
    * @throws NoSuchAlgorithmException if the hash algorithm cannot be found
    * @throws NullPointerException if the given {@code File} is null
    */
    private static String createHashedFile( File aFile, ProgressListener theListener, File parentDir ) throws IOException, NoSuchAlgorithmException {

        int bytesRead = 0;
        byte[] byteBuffer = new byte[Constants.GENERIC_BUFFER_SIZE];
        String theHash = null;

        //Variables for progress updates
        int sndFileProgress = 0;
        int tempProgressInt = 0;
        double tempProgressDouble;
        long writeByteCounter = 0;

        if( aFile == null ) { //If the given File is null...
            throw new NullPointerException();
        }

        //Get file size
        long tempFileSize = aFile.length();

        if( FileUtilities.verifyCanRead( aFile ) ) { //If the file can be read...

            File outFile = File.createTempFile("lib_", null);
            MessageDigest hash = MessageDigest.getInstance(Constants.HASH_FUNCTION);
//            byte[] fileHeader = Utilities.getFileHeader();

            FileInputStream theFileStream = new FileInputStream(aFile);
            BufferedInputStream theBufferedIS = new BufferedInputStream(theFileStream);
            try {

                FileOutputStream theOutStream = new FileOutputStream(outFile);
                BufferedOutputStream theBOS = new BufferedOutputStream(theOutStream);

                try {

                    //Setup the header
//                    System.arraycopy(fileHeader, 0, byteBuffer, 0, fileHeader.length);
//                    bytesRead = theBufferedIS.read(byteBuffer, fileHeader.length, byteBuffer.length - fileHeader.length);
//                    bytesRead += fileHeader.length;

                    while( bytesRead != -1){

                        writeByteCounter += bytesRead;

                        //Calculate the progress
                        //Check for divide by zero
                        if(theListener != null){
                            if(tempFileSize != 0){
                                tempProgressDouble = (1.0 * writeByteCounter) / (1.0 * tempFileSize );
                                tempProgressInt = (int)Math.round(tempProgressDouble * 100.0);
                            }

                            if(tempProgressInt != sndFileProgress){
                                theListener.progressChanged(0, tempProgressInt);
                                sndFileProgress = tempProgressInt;
                            }
                        }

                        theBOS.write(byteBuffer, 0, bytesRead);
                        hash.update(byteBuffer, 0, bytesRead);
                        bytesRead = theBufferedIS.read(byteBuffer);
                    }

                    byte[] byteHash = hash.digest();
                    theHash = Utilities.byteToHexString(byteHash);
                    theBOS.flush();

                } finally {

                    try {
                        theBOS.close();
                    } catch(IOException ex){
                        ex = null;
                    }
                }

            } finally {
                //Close the input stream
                try {
                    theBufferedIS.close();
                } catch(IOException ex){
                    ex = null;
                }
            }

            //Rename the file flush the stream and close it
            File finFile = new File(parentDir, theHash);
            if(!finFile.exists()){

                //Rename the file that already exists
                if(!outFile.renameTo(finFile)){
                    //throw new IOException("Unable to rename file with the same hash.");
                    moveFile(outFile, finFile);
                }

            }


        } else { //If the file doesn't exist or is not actually a file...
            throw new FileNotFoundException();
        }

        return theHash;

    }/* END createFileContentFromFile( File ) */
    
     //===============================================================
    /**
    * Inner Exception class for invalid library files
    *
    */
    public static class InvalidLibraryFileException extends Exception{

        private InvalidLibraryFileException(String string) {
           super(string);
        }
    }

     //===============================================================
    /**
    * Inner Exception class for invalid library files
    *
    */
    public static class InvalidFileLengthException extends Exception{

        private InvalidFileLengthException(String string) {
           super(string);
        }
    }
  

}/* END CLASS FileUtilities */
