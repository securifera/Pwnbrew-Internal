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

package pwnbrew.utilities;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import pwnbrew.misc.Constants;
import pwnbrew.misc.Directories;

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

        if( file != null && file.isFile() )
            rtnBool = file.canRead();      

        return rtnBool;

    }

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

        if( file != null && file.isDirectory() == false ) {

            boolean fileExists = false;

            if( file.exists() )
                fileExists = true;
            else { 

                File parentDir = file.getParentFile(); 

                if( parentDir.exists() )
                    fileExists = file.createNewFile(); 
                
            }

            if( fileExists )
                rtnBool = file.canWrite();            

        }

        return rtnBool;

    }

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

                if(aFile.isDirectory())
                    deleteDir(aFile);
                else {
                    boolean fileDeleted = aFile.delete();
                    if(!fileDeleted)
                        throw new IOException("Unable to delete file " + aFile);                    
                }

            }

            //If unable to delete throw
            if(!dir.delete())
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
                    if( fileDeleted == false )
                        allContentsDeleted = false;                   

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

        } 

        return rtnBool;

    }


    // ==========================================================================
    /**
    * Returns the SHA-256 hash for the passed file
    *
     * @param aFile
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

        if( FileUtilities.verifyCanRead( aFile ) ) { //If the file can be read...

            MessageDigest hash = MessageDigest.getInstance(Constants.HASH_FUNCTION);
            FileInputStream theFileStream = new FileInputStream(aFile);
            BufferedInputStream theBufferedIS = new BufferedInputStream(theFileStream);

            try {

                //Read in the bytes and update the hash
                while( bytesRead != -1){    
                    bytesRead = theBufferedIS.read(byteBuffer);
                    if(bytesRead > 0)
                        hash.update(byteBuffer, 0, bytesRead);                    
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

        } else
            throw new FileNotFoundException();
        

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
        
        try( FileInputStream aFIS = new FileInputStream(srcFile); 
            FileOutputStream aFOS = new FileOutputStream(dstFile)) {
            FileChannel inputChannel = aFIS.getChannel();
            FileChannel outputChannel = aFOS.getChannel();
            inputChannel.transferTo(0, inputChannel.size(), outputChannel);         

        }
        
        //Delete the file later
        srcFile.deleteOnExit();
    }

   // ==========================================================================
  /**
   * Convenience method for function below.
   *
     * @param aFile
   *
   * @return a {@code String} representing the hash of the file written to disk
   *
   * @throws FileNotFoundException if the given {@code File} represents a directory,
   * or represents a file that doesn't exist or cannot be read by the application
   * @throws IOException if a problem occurs while reading the file
   * @throws NoSuchAlgorithmException if the hash algorithm cannot be found
   * @throws NullPointerException if the given {@code File} is null
   */
   public static String createHashedFile( File aFile ) throws IOException, NoSuchAlgorithmException {
       return createHashedFile(aFile, Directories.getFileLibraryDirectory());   
   }
   
  
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
    private static String createHashedFile( File aFile, File parentDir ) throws IOException, NoSuchAlgorithmException {

        int bytesRead = 0;
        byte[] byteBuffer = new byte[Constants.GENERIC_BUFFER_SIZE];
        String theHash = null;

        if( FileUtilities.verifyCanRead( aFile ) ) { //If the file can be read...

            File outFile = File.createTempFile("lib_", null);
            MessageDigest hash = MessageDigest.getInstance(Constants.HASH_FUNCTION);

            FileInputStream theFileStream = new FileInputStream(aFile);
            BufferedInputStream theBufferedIS = new BufferedInputStream(theFileStream);
            try {

                FileOutputStream theOutStream = new FileOutputStream(outFile);
                BufferedOutputStream theBOS = new BufferedOutputStream(theOutStream);

                try {
                    
                    while( bytesRead != -1){
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
            if(!finFile.exists() && !outFile.renameTo(finFile))
                moveFile(outFile, finFile);                


        } else { //If the file doesn't exist or is not actually a file...
            throw new FileNotFoundException();
        }

        return theHash;

    }
     
}