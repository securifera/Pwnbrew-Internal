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

package pwnbrew.misc;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.*;
import java.util.*;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import pwnbrew.log.LoggableException;


/**
 *
 *  This class provides various utility functions.
 *
 *  
 *
 */
public class Utilities {
    
    //OS properties...
    private static final String PROPERTY_OsName    = "os.name";  
    private static final String PROPERTY_OsArch    = "os.arch";

    //OS name values...
    private static final String OS_NAME_Windows    = "windows";
    private static final String OS_NAME_SunSolaris   = "sunos";
    private static final String OS_NAME_Linux        = "linux";
    private static final String OS_NAME_Unix         = "unix";
    
    public static final String WINDOWS_LINE_SEPARATOR = "\r\n";
    public static final String UNIX_LINE_SEPARATOR = "\n";
    
    
    //Local OS values...
    public static final String OS_NAME    = System.getProperty( PROPERTY_OsName ).toLowerCase();
    public static final String JAVA_ARCH    = System.getProperty( PROPERTY_OsArch ).toLowerCase();
    
    private static final String PATH_SEP = System.getProperty("path.separator");

    //UNIX OS family...
    private static final List<String> OS_FAMILY_Unix;
    static {
        ArrayList<String> temp = new ArrayList<>();
        temp.add( OS_NAME_SunSolaris );
        temp.add( OS_NAME_Linux );
        temp.add( OS_NAME_Unix );
        OS_FAMILY_Unix = Collections.unmodifiableList( temp );
    }

    //Used for simple encrypt and decrypt
    private static final String AES_CFB_ENCRYPTION = "AES/CFB/NoPadding";
    private static final String AES_Cipher = "AES";  
    private static final String NAME_Class = Utilities.class.getSimpleName();
    
    public static final String IMAGE_PATH_IN_JAR= "pwnbrew/images";
    
    //Path to the file library
//    private static String PATH_FileLibrary;

    private static URL ourUrl;
    private static File classPath;

    static {
        
        try {
            
            try {
                //Check if we are staging first
                ourUrl = Class.forName("stager.Stager").getProtectionDomain().getCodeSource().getLocation();
            } catch (ClassNotFoundException ex) {
                ourUrl = Utilities.class.getProtectionDomain().getCodeSource().getLocation();
            }
            
            //Check for null
            classPath = new File( ourUrl.toURI() );            
        
        } catch( URISyntaxException ex1) {
            ex1 = null;
        } catch( IllegalArgumentException ex ){
                
        }
    }
    
     // ==========================================================================
    /**
     * Ensures the directory represented by the given {@link File} exists.
     * <p>
     * If the directory already exists this method does nothing; otherwise this
     * method will attempt to create it. This method will also attempt to create
     * any necessary but non-existing parents of the given directory.
     * 
     * @param directory a {@code File} representing the directory
     * 
     * @throws IOException if the directory did not exist and could not be created
     */
    public static void ensureDirectoryExists( File directory ) throws IOException {
      
        if( directory == null )
            throw new IOException( "Could not create a directory, the given File is null." );
      
        if( !directory.exists() ) 
            if( !directory.mkdirs() ) 
                throw new IOException(
                        new StringBuilder( "Could not create the directory \"" )
                        .append( directory ).append( "\"" ).toString() );

    }
    
    // ==========================================================================
    /**
    * Determines if the local host is running an OS that is in the UNIX family.
    *
     * @param passedOsName
    * @return {@code true} if the local OS is a flavor of UNIX, {@code false}
    * otherwise
    */
    static public boolean isUnix( String passedOsName ) {
        return OS_FAMILY_Unix.contains( passedOsName );
    }/* END isUnix() */
    
    // ==========================================================================
    /**
    * Returns the OS_Name
     * @return 
    */
    static public String getOsName() {
        return OS_NAME;
    }
    
    // ==========================================================================
    /**
    * Returns the OS_Name
     * @return 
    */
    static public String getOsArch() {
        return JAVA_ARCH;
    }
    
    // ==========================================================================
    /**
    * Determines if the local host is running an OS that is in the Windows family.
    *
    * @return {@code true} if the local OS is a flavor of Windows, {@code false}
    * otherwise
    */
    static public boolean isWindows() {
        return OS_NAME.contains( OS_NAME_Windows );
    }    

    // ==========================================================================
    /**
    * Writes the jar element to disk
    *
    * @param filePath
    * @param passedRelativePathInJar
    * @param passedJarElementName
     * @throws pwnbrew.log.LoggableException
    */
    public static void writeJarElementToDisk( File filePath, String passedRelativePathInJar, String passedJarElementName ) throws LoggableException {

        int bytesRead = 0;
        if( passedRelativePathInJar!= null && passedJarElementName!=null ){
                
                String theResourceStr = passedRelativePathInJar + "/" + passedJarElementName;
                InputStream theIS = Utilities.class.getClassLoader().getResourceAsStream(theResourceStr);
                if( theIS != null ){
                    
                    try {

                        if(filePath != null){

                            byte[] byteBuffer = new byte[Constants.GENERIC_BUFFER_SIZE];
                            FileOutputStream theOutStream = new FileOutputStream(filePath);
                            BufferedOutputStream theBOS = new BufferedOutputStream(theOutStream);

                            try {

                                //Read to the end
                                while( bytesRead != -1){
                                    bytesRead = theIS.read(byteBuffer);
                                    if(bytesRead != -1){
                                        theBOS.write(byteBuffer, 0, bytesRead);
                                    }
                                }

                                theBOS.flush();

                            } finally {

                                //Close output stream
                                theBOS.close();
                            }
                        }
                        
                    } catch (IOException ex){
                        throw new LoggableException(ex);
                    } finally {
                        
                        try {
                            //Make sure an close the input stream
                            theIS.close();
                        } catch (IOException ex) {
                            ex = null;
                        }
                    }
                    
                } 
            
            }

    }
   
    //===============================================================
    /**
    * Converts a byte array into hex string representation
    *
    * @param byteHash
    * @return the hex string
    */
    public static String byteToHexString(byte[] byteHash) {
        StringBuilder aSB = new StringBuilder();
        String tempString;

        for(int i = 0; i < byteHash.length; i++){
            tempString = Integer.toHexString(0xff & byteHash[i]);
            if(tempString.length() == 1) aSB.append('0');
            aSB.append(tempString);
        }
        return aSB.toString();
    }
   
        
    //==========================================================================
    /**
    *   Wrapper function for simpleEncrypt that takes a string and returns a Base64 encoded String.
     * @param clearText
     * @param preSharedKey
     * @return 
     * @throws pwnbrew.log.LoggableException 
    */
    public static String simpleEncrypt( String clearText, String preSharedKey) throws LoggableException {
        
        try {
            
            byte[] dataToEncrypt = clearText.getBytes("ISO-8859-1");
            byte[] encryptedData = simpleEncrypt( dataToEncrypt, preSharedKey );
            sun.misc.BASE64Encoder anEncoder = new sun.misc.BASE64Encoder();
            return anEncoder.encode(encryptedData);
            
        } catch (IOException ex){
           throw new LoggableException(ex);
        }
    }
    
    //==========================================================================
    /**
    * Takes a byte array and input key and encrypts using
     * AES_Cipher 256
     * @param dataToEncrypt
     * @param preSharedKey
     * @return 
     * @throws pwnbrew.log.LoggableException 
    */
    public static byte[] simpleEncrypt( byte[] dataToEncrypt, String preSharedKey ) throws LoggableException {

        byte[] encryptedData = null;
        try {
            Cipher theEncryptCipher = Cipher.getInstance( AES_CFB_ENCRYPTION  );
            byte[] initVect = new byte[ 16 ];

            if(dataToEncrypt != null && preSharedKey != null){
                
                //Get a 256 bit key for the preshared input
                MessageDigest hash = MessageDigest.getInstance(Constants.HASH_FUNCTION);
                
                //Instantiate a secret key object from the SHA1 256 bit digest
                byte[] encryptionKey = hash.digest(preSharedKey.getBytes("ISO-8859-1"));
                SecretKeySpec key = new SecretKeySpec( encryptionKey, AES_Cipher );
                IvParameterSpec ivSpec = new IvParameterSpec( initVect );
                
                try {
                   theEncryptCipher.init( Cipher.ENCRYPT_MODE, key, ivSpec );
                } catch (InvalidKeyException ex) {

                   //Use 128 bit since the policy file is obviously not in place
                   //for 256 bit encryption
                   key = new SecretKeySpec(encryptionKey, 0, 16, AES_Cipher);
                   theEncryptCipher = Cipher.getInstance( AES_CFB_ENCRYPTION  );
                   theEncryptCipher.init( Cipher.ENCRYPT_MODE, key, ivSpec );
                }

                encryptedData = theEncryptCipher.doFinal( dataToEncrypt, 0, dataToEncrypt.length );
            }

        } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchPaddingException | UnsupportedEncodingException ex) {
           throw new LoggableException(ex);
        }

        return encryptedData;

    }
    
     //===============================================================
    /**
     * Takes a base64 encrypted string and input key and returns the clear text after
     * decoding the base 64 and decrypting using AES_Cipher 256.
     * @param encryptedText
     * @param preSharedKey
     * @return 
     * @throws pwnbrew.log.LoggableException 
    */
    public static String simpleDecrypt ( String encryptedText, String preSharedKey ) throws LoggableException {

        String retVal = null;

        try {
           Cipher theDecryptCipher = Cipher.getInstance( AES_CFB_ENCRYPTION  );
           byte[] initVect = new byte[ 16 ];

           if(encryptedText != null && preSharedKey != null){

              //Get a 256 bit key for the preshared input
              MessageDigest hash = MessageDigest.getInstance(Constants.HASH_FUNCTION);
              byte[] decryptionKey = hash.digest(preSharedKey.getBytes("US-ASCII"));

              SecretKeySpec key = new SecretKeySpec( decryptionKey, AES_Cipher );
              IvParameterSpec ivSpec = new IvParameterSpec( initVect );

              try {
                 theDecryptCipher.init( Cipher.DECRYPT_MODE, key, ivSpec );
              } catch (InvalidKeyException ex) {

                 //Use 128 bit since the policy file is obviously not in place
                 //for 256 bit encryption
                 key = new SecretKeySpec(decryptionKey, 0, 16, AES_Cipher);
                 theDecryptCipher = Cipher.getInstance( AES_CFB_ENCRYPTION  );
                 theDecryptCipher.init( Cipher.DECRYPT_MODE, key, ivSpec );
              }

              sun.misc.BASE64Decoder aDecoder = new sun.misc.BASE64Decoder();
              byte[] decodedBytes = aDecoder.decodeBuffer(encryptedText);
              byte[] decryptedData = theDecryptCipher.doFinal( decodedBytes, 0, decodedBytes.length );
              retVal = new String(decryptedData, "US-ASCII");
           }
           
        } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | NoSuchPaddingException | NoSuchAlgorithmException | IOException ex) {
           throw new LoggableException(ex);
        }

        return retVal;

    }

    //==========================================================================
    /**
     * Takes a base64 encrypted string and input key and returns the clear text after
     * decoding the base 64 and decrypting using AES_Cipher 256.
     * @param encryptedText
     * @param preSharedKey
     * @return 
     * @throws pwnbrew.log.LoggableException 
    */
    public static byte[] simpleDecrypt ( byte[] encryptedText, String preSharedKey ) throws LoggableException {

        byte[] retVal = null;

        try {
           Cipher theDecryptCipher = Cipher.getInstance( AES_CFB_ENCRYPTION  );
           byte[] initVect = new byte[ 16 ];

           if(encryptedText != null && preSharedKey != null){

              //Get a 256 bit key for the preshared input
              MessageDigest hash = MessageDigest.getInstance(Constants.HASH_FUNCTION);
              byte[] decryptionKey = hash.digest(preSharedKey.getBytes("ISO-8859-1"));

              SecretKeySpec key = new SecretKeySpec( decryptionKey, AES_Cipher );
              IvParameterSpec ivSpec = new IvParameterSpec( initVect );

              try {
                 theDecryptCipher.init( Cipher.DECRYPT_MODE, key, ivSpec );
              } catch (InvalidKeyException ex) {

                 //Use 128 bit since the policy file is obviously not in place
                 //for 256 bit encryption
                 key = new SecretKeySpec(decryptionKey, 0, 16, AES_Cipher);
                 theDecryptCipher = Cipher.getInstance( AES_CFB_ENCRYPTION  );
                 theDecryptCipher.init( Cipher.DECRYPT_MODE, key, ivSpec );
              }

              retVal = theDecryptCipher.doFinal( encryptedText, 0, encryptedText.length );
           }
           
        } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | NoSuchPaddingException | NoSuchAlgorithmException | UnsupportedEncodingException ex) {
           throw new LoggableException(ex);
        }

        return retVal;

    }
    
    //==========================================================================
    /**
     *   Returns the class path for the application.
     * 
     * @return 
    */   
    public static File getClassPath(){
        return classPath;
    }

    //==========================================================================
    /*
    * Returns the four bytes that compose an file header
    */
    public static byte[] getFileHeader(){
        return new byte[]{ (byte)'P', (byte)'P',(byte)'F',(byte)0x00 };
    }
    
    // ==========================================================================
    /**
     *  Xor the data.
     * 
     * @param bData
     * @param xorString
     * @return 
    */
    public static byte[] xorData(byte[] bData, byte[] xorString) {
       
        byte bCrypto[] = new byte[bData.length];
        for(int i = 0 ; i < bData.length; ++i) {
            bCrypto[i] = (byte)(bData[i] ^ xorString[i % xorString.length]);
        }
 
        return bCrypto;
    }   
    
     // ==========================================================================
    /**
    * Determines if the local host is running an OS that is in the Windows family.
    *
     * @param passedOsName
    * @return {@code true} if the local OS is a flavor of Windows, {@code false}
    * otherwise
    */
    static public boolean isWindows( String passedOsName ) {
        return passedOsName.contains( OS_NAME_Windows );

    }
    
     // ==========================================================================
    /**
    * Returns the OS_Name
     * @param passedOs
     * @return 
    */
    static public String getLineEnding( String passedOs ) {
        if( Utilities.isWindows(passedOs)){
            return WINDOWS_LINE_SEPARATOR;
        } else {
            return UNIX_LINE_SEPARATOR;
        }
    }
    
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
    private static String getFileHash(File aFile, boolean addHeader ) throws NoSuchAlgorithmException, IOException {

        int bytesRead = 0;
        byte[] byteBuffer = new byte[Constants.GENERIC_BUFFER_SIZE];
        String theHash = "";

        if( aFile.exists() && aFile.canRead() ) { 

            MessageDigest hash = MessageDigest.getInstance(Constants.HASH_FUNCTION);
            FileInputStream theFileStream = new FileInputStream(aFile);
            BufferedInputStream theBufferedIS = new BufferedInputStream(theFileStream);

            try {

                //Add the header to the hash if it was removed
                if(addHeader)
                    hash.update( Utilities.getFileHeader() );
                

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

    }   
  
    // ===============================================
    /**
     * Convenience method for loadImageFromJar
     * @param passedImageName
     * @return 
    */
    public static BufferedImage loadImageFromJar ( String passedImageName ) {
       return loadImageFromJar ( passedImageName, classPath, IMAGE_PATH_IN_JAR);
    }
  
    // ===============================================
    /**
    * Returns the image in the jar, or jar supplement, that matches the {@code passedImageName).
    *
    * @param passedImageName   the name of the image to be loaded
     * @param jarPath
     * @param imagePath
    *
    * @return  the {@link BufferedImage} with the given filename,
    *          or {@code null} if unable to locate a matching image
    */
    public static BufferedImage loadImageFromJar ( String passedImageName, File jarPath, String imagePath ) {

        BufferedImage rtnImage = null;
        byte [] theBytes = getBytesForJarElement( jarPath, imagePath, passedImageName );

        if ( theBytes != null ) {
            rtnImage = createImageFromBytes( theBytes );    //get an "Image"
        }

        return rtnImage;
    }
    
    //===============================================
    /**
    * Creates a {@link BufferedImage} from the passed array of {@code byte}s.
    * The images produced from this method tend to be better quality than the images
    * produced by {@link #toBufferedImage(Image)}.  However, if you cannot obtain the
    * image bytes, that provides an alternate method to use for creating a {@link BufferedImage}
    * from an {@link Image}.
    *
    * @param passedBytesForImage   the {@code byte} array containing image data
    *
    * @return  the {@link BufferedImage} created from the passed {@code byte} array
    *          of image data, {@code null} if unable to create an image from the passed bytes
    */
    private static BufferedImage createImageFromBytes( byte [] passedBytesForImage ) {
        BufferedImage rtnImage = null;

        if ( passedBytesForImage != null ) {
            try {
                rtnImage = ImageIO.read( new ByteArrayInputStream( passedBytesForImage ) );
            } catch( IOException ex ) {
                ex = null;
            }
        }

        return rtnImage;
    }
    
    //===============================================
    /**
    * Reads the bytes from an element in the jar or build path.
    * 
    * @param passedRelativePathInJar
    * @param passedJarElementName
    * 
    * @return an array of bytes representing the jar element
    */
    private static byte [] getBytesForJarElement( File jarPath, String passedRelativePathInJar, String passedJarElementName ) {

        byte [] theBytesForJarEntry= null;    
        if ( jarPath != null ) {
            try {
                if ( jarPath.isDirectory() ) {

                    // Then this execution is running within the IDE's '.../build/classes/...'
                    // and there isnt actualy a JAR file !!
                    String tmpFullFilenameWithPath = jarPath.getAbsolutePath() + File.separator + passedRelativePathInJar;
                    File testFile = new File( tmpFullFilenameWithPath, passedJarElementName );

                    if( testFile.canRead() ) {
                        
                        FileInputStream tmpFileInputStream= new FileInputStream( testFile );
                        int fileLen= (int)testFile.length();
                        theBytesForJarEntry= new byte[ fileLen ];

                        int totalQtyReadAlready= 0;
                        int qtyStillNeeded= fileLen;
                        int qtyJustRead;

                        try{
                            while ( totalQtyReadAlready < fileLen ) {
                                qtyJustRead= tmpFileInputStream.read( theBytesForJarEntry, fileLen-qtyStillNeeded, qtyStillNeeded);

                                if ( qtyJustRead < 0 )
                                    break;
                                
                                totalQtyReadAlready+= qtyJustRead;
                                qtyStillNeeded-= qtyJustRead;
                            }
                            // if totalQtyread != btyeSizeOfEntry then an error has occurred

                        } finally {
                            tmpFileInputStream.close();
                        }
                    }

                } else {

                    JarFile tmpJarFile = null;
                    try {
                        
                        tmpJarFile= new JarFile( jarPath );

                        // These exist too, but not needed: JarEntry tmpJarEntry= tmpJarFile.getJarEntry( ImagePathWithinJar +passedImageName);
                        String tmpIntraJarPathAndFilename= passedRelativePathInJar + "/" +passedJarElementName;

                        ZipEntry tmpZipEntry= tmpJarFile.getEntry( tmpIntraJarPathAndFilename );
                        if ( tmpZipEntry != null ) {
                            // OK, read the image "file" into a byte array
                            //
                            int btyeSizeOfEntry= (int)tmpZipEntry.getSize();
                            theBytesForJarEntry= new byte[btyeSizeOfEntry];
                            //
                            InputStream tmpInputStream= tmpJarFile.getInputStream( tmpZipEntry );
                            try {

                                int totalQtyReadAlready= 0;
                                int qtyStillNeeded= btyeSizeOfEntry;

                                int qtyJustRead;
                                while ( totalQtyReadAlready < btyeSizeOfEntry ) {
                                    qtyJustRead= tmpInputStream.read( theBytesForJarEntry, btyeSizeOfEntry-qtyStillNeeded, qtyStillNeeded);

                                    if ( qtyJustRead < 0 )
                                        break;
                                    

                                    totalQtyReadAlready+= qtyJustRead;
                                    qtyStillNeeded-= qtyJustRead;
                                }
                            // if totalQtyread != btyeSizeOfEntry then an error has occurred
                            } finally {
                                tmpInputStream.close();
                            }
                        }

                    } finally {

                        //Close the jar
                        if(tmpJarFile != null){

                            try{
                                tmpJarFile.close();
                            } catch (IOException ex) {
                                ex = null;
                            }
                        }
                    }
                }
            
            } catch (IOException ex) {
                ex = null;
            }
        }

        return theBytesForJarEntry;
    }
    
      //===============================================
    /**
    * Convenience method for setComponentIcon
     * @param passedJComponent
     * @param logoImageStr
     * @param buttonWidth
     * @param buttonHeight
    */
    public static void setComponentIcon(JComponent passedJComponent, int buttonWidth, int buttonHeight, String logoImageStr ){
        setComponentIcon(passedJComponent, buttonWidth, buttonHeight, logoImageStr, Utilities.getClassPath(), IMAGE_PATH_IN_JAR);
    }
    
    //===============================================
    /**
    * Sets the icon for the given component
    */
    private static void setComponentIcon(JComponent passedJComponent, int buttonWidth, int buttonHeight, String logoImageStr, File passedFile, String imageLocation ){

       Image nodeImage = loadImageFromJar( logoImageStr, passedFile, imageLocation );
       if(nodeImage != null){
          ImageIcon nodeIcon = new ImageIcon(nodeImage.getScaledInstance(buttonWidth, buttonHeight, Image.SCALE_SMOOTH));

          if(passedJComponent instanceof AbstractButton){
             JButton passedJButton = (JButton)passedJComponent;
             passedJButton.setIcon(nodeIcon);
             passedJButton.setText("");
          }
       }
    }
    
      //===========================================================================
    /**
     * 
     * @param passedFile
     * @param properties 
     * @param propMap 
     */
    public static void updateJarProperties( File passedFile, String properties, Map<String, String> propMap ){        
    
        if( passedFile != null && passedFile.isFile() ){                 
            
            //Open the zip
            ByteArrayOutputStream aBOS = new ByteArrayOutputStream();
            try {
                
                FileInputStream fis = new FileInputStream(passedFile);
                try{

                    //Read into the buffer
                    byte[] buf = new byte[1024];                
                    for (int readNum; (readNum = fis.read(buf)) != -1;)
                        aBOS.write(buf, 0, readNum);                    

                //Close and delete
                } catch (IOException ex) {
                    DebugPrinter.printMessage( NAME_Class, "updateJarProperties()",  ex.getMessage(), ex);         
                } finally {
                    try {
                        fis.close();
                    } catch (IOException ex) {
                        ex = null;
                    }
                }
                passedFile.delete();

                //Create the file back
                FileOutputStream theFileOS = new FileOutputStream(passedFile);
                ZipOutputStream theZipOS = new ZipOutputStream(theFileOS );

                //Creat an inputstream
                ByteArrayInputStream aBIS = new ByteArrayInputStream(aBOS.toByteArray());    
                
                //Close the stream
                try {
                    aBOS.close();
                } catch (IOException ex) { ex = null;}

                //Open the zip input stream
                ZipInputStream theZipInputStream = new ZipInputStream(aBIS);
                try {
                    
                    ZipEntry anEntry;
                    while((anEntry = theZipInputStream.getNextEntry())!=null){
                        //Get the entry name
                        String theEntryName = anEntry.getName();

                        //Change the properties file
                        if( theEntryName.equals(properties) ){

                            //Get the input stream and modify the value
                            ManifestProperties localProperties = new ManifestProperties();
                            localProperties.load(theZipInputStream);

                            //Set the IP to something else
                            //Add the entry
                            anEntry = new ZipEntry(properties);
                            
                            //Add each property
                            for (Map.Entry<String, String> mapEntry : propMap.entrySet())
                                localProperties.setProperty(mapEntry.getKey(), mapEntry.getValue());

                            //Add the entry
                            theZipOS.putNextEntry(anEntry);
                            localProperties.store(theZipOS);

                            //Write to zip
                            theZipOS.closeEntry();

                            continue;
                        } 

                        //Add the entry
                        theZipOS.putNextEntry(anEntry);
                        /*
                        * After creating entry in the zip file, actually
                        * write the file.
                        */
                        int temp;
                        byte[] buffer = new byte[1024];
                        while((temp = theZipInputStream.read(buffer)) > 0) {
                            theZipOS.write(buffer, 0, temp);
                        }
                        theZipOS.closeEntry();
                    }

                    //Close the jar
                    theZipOS.flush();
                    theZipOS.close();
                    
                } catch (IOException ex) {
                    DebugPrinter.printMessage( NAME_Class, "updateJarProperties()",  ex.getMessage(), ex);         
                } finally {
                    try {
                        theZipInputStream.close();
                    } catch (IOException ex) { ex = null; }
                }
                
            } catch (FileNotFoundException ex ){
                DebugPrinter.printMessage( NAME_Class, "updateJarProperties()",  ex.getMessage(), ex);      
            }
        }
    }
    
    /**
     *
     * @author Securifera
     */
    public static class ManifestProperties extends Properties {

        //===================================================================
        /**
         * Constructor
         */
        public ManifestProperties() {
            super();
        }   

        //=========================================================================
        /**
         * 
         * After the entries have been written, the output stream is flushed.  
         * The output stream remains open after this method returns.
         * <p>
         * @param   out      an output stream.
         * @exception  IOException if writing this property list to the specified
         *             output stream throws an <tt>IOException</tt>.
         * @exception  ClassCastException  if this <code>Properties</code> object
         *             contains any keys or values that are not <code>Strings</code>.
         * @exception  NullPointerException  if <code>out</code> is null.
         * @since 1.2
         */
        public void store(OutputStream out ) throws IOException {

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out, "8859_1"));
            synchronized (this) {
                for (Enumeration e = keys(); e.hasMoreElements();) {
                    String key = (String)e.nextElement();
                    String val = (String)get(key);

                    bw.write(key + ": " + val);
                    bw.newLine();
                }
            }
            bw.flush();
        }
    }
}
