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
 * Created on June 2, 2013, 11:59 AM
 */

package pwnbrew.misc;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 */
abstract public class FileUtilities {
  
  

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
  private static boolean verifyCanRead( File file ) {
    
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

     if(!dir.exists()){
         return;
     } else if(!dir.isDirectory()){
        throw new IOException("Not a directory " + dir);
     }

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
   
  
   // ==========================================================================
  /**
   *    Wrapper function. 
     * @param aFile
     * @return 
     * @throws java.security.NoSuchAlgorithmException 
     * @throws java.io.IOException 
   */
  public static String getFileHash(File aFile) throws NoSuchAlgorithmException, IOException {
      return getFileHash(aFile, false);
  }

   // ==========================================================================
  /**
   * Returns the SHA-256 hash for the passed file
   *
     * @param aFile
     * @param addHeader
   *
   * @return a {@code String} representing the hash of the file
   *
   * @throws FileNotFoundException if the given {@code File} represents a directory,
   * or represents a file that doesn't exist or cannot be read by the application
   * @throws IOException if a problem occurs while reading the file
   * @throws NoSuchAlgorithmException if the hash algorithm cannot be found
   * @throws NullPointerException if the given {@code File} is null
   */
  public static String getFileHash(File aFile, boolean addHeader ) throws NoSuchAlgorithmException, IOException {

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

            //Add the header to the hash if it was removed
            if(addHeader){
                hash.update( Utilities.getFileHeader() );
            }
            
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

  }/* END createFileContentFromFile( File ) */

    //===============================================================
    /**
     * Converts the library file to the File path passed
     *
     * @param fileHash
     * @param destFile
     * @param fileContentDir
     * @throws java.io.IOException
    */
    public static void renameLibFile(String fileHash, File destFile, File fileContentDir ) throws IOException {
    
        File aFile = new File(fileContentDir, fileHash);
        
        //Rename the file that already exists
        if(!aFile.renameTo(destFile)){
            //throw new IOException("Unable to rename file with the same hash.");
            FileUtilities.moveFile(aFile, destFile);
        }
        
    }

    // ==========================================================================
    /**
    * Moves one file to another using file channels
    *
    * @param srcFile a {@code File} representing the source file 
    * @param dstFile a {@code File} representing the source file 
    *
    * @return 
    *
    * @throws FileNotFoundException if the given {@code File} represents a directory,
    * or represents a file that doesn't exist or cannot be read by the application
    * @throws IOException if a problem occurs while reading the file
    * @throws NullPointerException if the given {@code File} is null
    */
    public static boolean moveFile( File srcFile, File dstFile ) throws IOException {
        
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
        srcFile.delete();
        return true;
    }
   

}/* END CLASS FileUtilities */
