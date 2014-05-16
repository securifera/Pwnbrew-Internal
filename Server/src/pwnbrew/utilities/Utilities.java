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

import com.sun.jna.Pointer;
import java.awt.Component;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.beans.IntrospectionException;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.ByteBuffer;
import java.security.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;
import pwnbrew.host.Host;
import pwnbrew.host.HostFactory;
import pwnbrew.library.LibraryItemController;
import pwnbrew.logging.Log;
import pwnbrew.logging.LoggableException;
import pwnbrew.misc.Base64Converter;
import pwnbrew.misc.Constants;
import pwnbrew.misc.Directories;
import pwnbrew.tasks.RemoteTask;
import pwnbrew.xmlBase.XmlBase;
import pwnbrew.exception.XmlBaseCreationException;
import pwnbrew.xmlBase.XmlBaseFactory;
import pwnbrew.jna.Kernel32;
import pwnbrew.misc.FileFilterImp;
import pwnbrew.misc.IdGenerator;
import pwnbrew.misc.RuntimeRunnable;
import pwnbrew.network.control.messages.Payload;
import pwnbrew.network.control.messages.SendStage;
import pwnbrew.xmlBase.JarItem;


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
     
    //Used for simple encrypt and decrypt
    private static final String AES_CFB_ENCRYPTION = "AES/CFB/NoPadding";
    private static final String AES_Cipher = "AES";
  
    private static final String NAME_Class = Utilities.class.getSimpleName();

    public static final SecureRandom SecureRandomGen = new SecureRandom();

    private static final URL ourUrl= Utilities.class.getProtectionDomain().getCodeSource().getLocation();
    private static File classPath;
    
    private static final List<String> theControllerList = new ArrayList<>();
    private static final List<String> theExtPanelClassList = new ArrayList<>();
      
    public static final String IMAGE_PATH_IN_JAR= "pwnbrew/images";
    
    public static final String WINDOWS_LINE_SEPARATOR = "\r\n";
    public static final String UNIX_LINE_SEPARATOR = "\n";
    
    public static final Map<String, JarItem> theStagerRefMap = new HashMap<>();
    public static final Map<String, JarItem> thePayloadRefMap = new HashMap<>();
    public static final Map<String, JarItem> theLocalExtMap = new HashMap<>();
    public static final Map<String, JarItem> theRemoteExtMap = new HashMap<>();
    
    //UNIX OS family...
    private static final List<String> OS_FAMILY_Unix;
    static {
        ArrayList<String> temp = new ArrayList<>();
        temp.add( OS_NAME_SunSolaris );
        temp.add( OS_NAME_Linux );
        temp.add( OS_NAME_Unix );
        OS_FAMILY_Unix = Collections.unmodifiableList( temp );
    }

    //Local OS values...
    public static final String OsName    = System.getProperty( PROPERTY_OsName ).toLowerCase();
    public static final String JAVA_ARCH    = System.getProperty( PROPERTY_OsArch ).toLowerCase();

    // ==========================================================================
    /**
     *  Add the Controller to the Runnable Controller list
     * @param passedClassPath
     */
    public static void addRunnableController( String passedClassPath ) {
        if( passedClassPath != null && !passedClassPath.isEmpty() )
            theControllerList.add(passedClassPath);
    }
    
     // ==========================================================================
    /**
     *  Adds the class
     * @param passedClassPath
     */
    public static void addOptionsJPanel( String passedClassPath ) {
        if( passedClassPath != null && !passedClassPath.isEmpty() )
            theExtPanelClassList.add(passedClassPath);
    }

    // ==========================================================================
    /**
     *  Return a list of option panels from the loaded extensions
     * @return 
     */
    public static List<String> getExtPanelClassList() {
        return new ArrayList<>(theExtPanelClassList);
    }

    //===========================================================================
    /**
     * 
     * @param theJvmVersion
     * @return 
     */
    
    public static File getPayloadFile(String theJvmVersion) {

        JarItem aRef = null;
        synchronized( thePayloadRefMap ){
            int version = Integer.parseInt(theJvmVersion);
            while( aRef == null && version > 0 ){
                aRef = thePayloadRefMap.get(theJvmVersion);
                version--;
                theJvmVersion = Integer.toString(version);
            }
        }
        
        File theFile = null;        
        if( aRef != null ){
            theFile = new File( Directories.getFileLibraryDirectory(), aRef.getFileHash() ); 
        }
        
        return theFile;
    }

     //===========================================================================
    /**
     * 
     * @param theJvmVersion
     * @return 
     */
    
    public static JarItem getStagerJarItem(String theJvmVersion) {

        JarItem aRef = null;
        synchronized( theStagerRefMap ){
            int version = Integer.parseInt(theJvmVersion);
            while( aRef == null && version > 0 ){
                aRef = theStagerRefMap.get(theJvmVersion);
                version--;
                theJvmVersion = Integer.toString(version);
            }
        }
             
        return aRef;
    }
    
    //===========================================================================
    /**
     * 
     * @return 
     */
    public static List<JarItem> getJarItems() {
        
        List<JarItem> aList = new ArrayList<>();
        synchronized( theStagerRefMap ){
            aList.addAll( theStagerRefMap.values() );
        }
        synchronized( thePayloadRefMap ){
            aList.addAll( thePayloadRefMap.values() );
        }
        synchronized( theLocalExtMap ){
            aList.addAll( theLocalExtMap.values() );
        }
        synchronized( theRemoteExtMap ){
            aList.addAll( theRemoteExtMap.values() );
        }

        return aList;
    }
    
    //===========================================================================
    /**
     * 
     * @param aRef 
     */
    public static void addJarItem( JarItem aRef ){
        
        switch( aRef.getType()){
            case JarItem.STAGER_TYPE:
                synchronized( theStagerRefMap ){
                    theStagerRefMap.put(aRef.getJvmMajorVersion(), aRef);
                }
                break;
            case JarItem.PAYLOAD_TYPE:
                synchronized( thePayloadRefMap ){
                    thePayloadRefMap.put(aRef.getJvmMajorVersion(), aRef);
                }
                break;
            case JarItem.LOCAL_EXTENSION_TYPE:
                synchronized( theLocalExtMap ){
                    theLocalExtMap.put(aRef.getName(), aRef);
                }
                break;
            case JarItem.REMOTE_EXTENSION_TYPE:
                synchronized( theRemoteExtMap ){
                    theRemoteExtMap.put(aRef.getName(), aRef);
                }
                break;
            default:                
                break;
        }
    }
    
    //===========================================================================
    /**
     * 
     * @param aRef 
     */
    public static void removeJarItem( JarItem aRef ){
        
        switch( aRef.getType()){
            case JarItem.STAGER_TYPE:
                synchronized( theStagerRefMap ){
                    theStagerRefMap.remove(aRef.getJvmMajorVersion());
                }
                break;
            case JarItem.PAYLOAD_TYPE:
                synchronized( thePayloadRefMap ){
                    thePayloadRefMap.remove(aRef.getJvmMajorVersion());
                }
                break;
            case JarItem.LOCAL_EXTENSION_TYPE:
                synchronized( theLocalExtMap ){
                    theLocalExtMap.remove(aRef.getName());
                }
                break;
            case JarItem.REMOTE_EXTENSION_TYPE:
                synchronized( theRemoteExtMap ){
                    theRemoteExtMap.remove(aRef.getName());
                }
                break;
            default:                
                break;
        }
                 
    }
 
      
     //==========================================================================
    /**
     *  Get version
     * 
     * @param payloadFile
     * @return 
     */
    public static JarItem getJavaItem( File payloadFile ){
        
        //Remove JAR if that's how we are running
        JarItem aJarItem = null;        
        if( payloadFile != null && payloadFile.getPath().endsWith(".jar")){ 

            try {                 
                
                aJarItem = (JarItem) XmlBaseFactory.instantiateClass( JarItem.class );
                aJarItem.setId( IdGenerator.next() );
                String jarVersionString = "";
                String jvmVersionString = "";
       
                //Open the zip
                ByteArrayOutputStream aBOS = new ByteArrayOutputStream();
                ByteArrayInputStream aBIS;
                try{
                    
                    FileInputStream fis = new FileInputStream(payloadFile);
                    try{

                        //Read into the buffer
                        byte[] buf = new byte[1024];                
                        for (int readNum; (readNum = fis.read(buf)) != -1;)
                            aBOS.write(buf, 0, readNum);                        

                    } finally{

                        try {
                            //Close and delete
                            fis.close();
                        } catch (IOException ex) {                        
                        }
                    }

                    //Creat an inputstream
                    aBIS = new ByteArrayInputStream(aBOS.toByteArray());    
                
                } finally {
                    try {
                        aBOS.close();
                    } catch (IOException ex) {
                    }
                }

                //Open the zip input stream
                ZipInputStream theZipInputStream = new ZipInputStream(aBIS);
                ZipEntry anEntry;
                byte[] classHeader = new byte[]{ (byte)0xCA, (byte)0xFE, (byte)0xBA, (byte)0xBE};
                try {
                    
                    while((anEntry = theZipInputStream.getNextEntry())!=null){
                        
                        //Get the entry name
                        String theEntryName = anEntry.getName();
                        if ( !jvmVersionString.isEmpty() && !jarVersionString.isEmpty() )
                            break;
                                                
                        //Change the properties file
                        if( theEntryName.equals( Constants.PROP_FILE) && jarVersionString.isEmpty() ){

                            //Get the input stream and modify the value
                            Properties localProperties = new Properties();
                            localProperties.load(theZipInputStream);

                            //Set the IP to something else
                            String version = localProperties.getProperty(Constants.PAYLOAD_VERSION_LABEL);
                            if( version != null ){
                                //Set the jar version
                                jarVersionString = version;
                            }     
                            continue;
                            
                        } 
                        
                        if( theEntryName.endsWith("class") && jvmVersionString.isEmpty() ){
                            
                            //Get the JVM version
                            byte[] aByteArray = new byte[4];
                            
                            int bytesRead = theZipInputStream.read(aByteArray, 0, aByteArray.length);
                            if( bytesRead ==4 && Arrays.equals(aByteArray, classHeader)){
                                theZipInputStream.skip(3);
                                int jvmVersion = theZipInputStream.read();
                                switch( jvmVersion ){
                                    case 46:
                                        jvmVersionString = "2";
                                        break;
                                    case 47:
                                        jvmVersionString = "3";
                                        break;
                                    case 48:
                                        jvmVersionString = "4";
                                        break;
                                    case 49:
                                        jvmVersionString = "5";
                                        break;
                                    case 50:
                                        jvmVersionString = "6";
                                        break;
                                    case 51:
                                        jvmVersionString = "7";
                                        break;
                                    case 52:
                                        jvmVersionString = "8";
                                        break;
                                }
                                
                            }   
                            
                            continue;
                            
                        }

                    }

                    aJarItem.setVersion(jarVersionString);
                    aJarItem.setJvmMajorVersion(jvmVersionString);
                    
                //Close the jar
                } finally {
                    
                    try {
                        theZipInputStream.close();
                    } catch (IOException ex) {
                    }
                    
                }
                
            } catch ( IOException | IllegalAccessException | InstantiationException ex) {
                Log.log(Level.SEVERE, NAME_Class, "saveChanges()", ex.getMessage(), ex);
            } 
        }
        return aJarItem;
    }

    // ==========================================================================
    /**
     *
     */
    public static enum EditMenuOptions {

        CUT("Cut"),
        COPY("Copy"),
        PASTE("Paste"),
        SELECT_ALL("Select All");

        //Return a dummy menu
        public static void fillDummyMenu( JMenu menu ) {
            menu.add( EditMenuOptions.CUT.getDummyMenuItem() ); //Cut
            menu.add( EditMenuOptions.COPY.getDummyMenuItem() ); //Copy
            menu.add( EditMenuOptions.PASTE.getDummyMenuItem() ); //Paste
            menu.addSeparator();
            menu.add( EditMenuOptions.SELECT_ALL.getDummyMenuItem() ); //Select All
        }

        private final String editMenuString;

        private final JMenuItem DummyMenuItem;

        EditMenuOptions(String theString){
            
            this.editMenuString = theString;

            DummyMenuItem = new JMenuItem();
            DummyMenuItem.setText( editMenuString );
            DummyMenuItem.setEnabled( false );

        }

        public String getValue() {
            return editMenuString;
        }

        public JMenuItem getDummyMenuItem() {
            return DummyMenuItem;
        }
       
    }
    
    // ==========================================================================
    /**
    * Returns the OS_Name
     * @param passedOs
     * @return 
    */
    static public String getLineEnding( String passedOs ) {        
        return Utilities.isWindows(passedOs) ? WINDOWS_LINE_SEPARATOR : UNIX_LINE_SEPARATOR ;
    }
    
    // ==========================================================================
    /**
    * Returns the OS_Name
     * @return 
    */
    static public String getOsName() {
        return OsName;
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
    * Determines if the local host is running an OS that is in the UNIX family.
    *
     * @param passedOsName
    * @return {@code true} if the local OS is a flavor of UNIX, {@code false}
    * otherwise
    */
    static public boolean isUnix( String passedOsName ) {
        return OS_FAMILY_Unix.contains( passedOsName );
    }
 
    static {
        try {
            URI ourUri= ourUrl.toURI();
            classPath = new File( ourUri );
        } catch( URISyntaxException ex ) {
            ex = null;
        }
    }

    // ==========================================================================
    /**
    * Writes the jar element to disk
    *
    * @param filePath
    * @param passedRelativePathInJar
    * @param passedJarElementName
     * @throws pwnbrew.logging.LoggableException
    */
    public static void writeJarElementToDisk( File filePath, String passedRelativePathInJar, String passedJarElementName ) throws LoggableException {

        int bytesRead = 0;

        if ( classPath != null && passedRelativePathInJar!= null && passedJarElementName!=null ) {

            JarFile tmpJarFile = null;
            try {

                BufferedInputStream theBIS = null;
                if ( classPath.isDirectory() ) {

                    //Running in IDE
                    String tmpFullFilenameWithPath;

                    tmpFullFilenameWithPath = ourUrl.toURI().getPath() + passedRelativePathInJar;
                    File testFile = new File( tmpFullFilenameWithPath, passedJarElementName );

                    if( testFile.exists() ) {

                        //Open the stream
                        FileInputStream tmpFileInputStream= new FileInputStream( testFile );
                        theBIS = new BufferedInputStream(tmpFileInputStream);

                    } else
                        throw new LoggableException("File does not exist in build directory.");                

                } else {

                    //The path points to the JAR
                    tmpJarFile= new JarFile( classPath );

                    // These exist too, but not needed: JarEntry tmpJarEntry= tmpJarFile.getJarEntry( ImagePathWithinJar +passedImageName);
                    String tmpIntraJarPathAndFilename= passedRelativePathInJar + "/" +passedJarElementName;

                    ZipEntry tmpZipEntry= tmpJarFile.getEntry( tmpIntraJarPathAndFilename );
                    if ( tmpZipEntry != null ) {
                        //Open the stream
                        InputStream tmpInputStream= tmpJarFile.getInputStream( tmpZipEntry );
                        theBIS = new BufferedInputStream(tmpInputStream);
                    } else
                        throw new LoggableException("Class does not exist in jar.");                

                }

                try {

                    if(filePath != null){

                        byte[] byteBuffer = new byte[Constants.GENERIC_BUFFER_SIZE];
                        FileOutputStream theOutStream = new FileOutputStream(filePath);
                        try (BufferedOutputStream theBOS = new BufferedOutputStream(theOutStream)) {

                            //Read to the end
                            while( bytesRead != -1){
                                bytesRead = theBIS.read(byteBuffer);
                                if(bytesRead != -1)
                                    theBOS.write(byteBuffer, 0, bytesRead);                            
                            }

                            theBOS.flush();

                        }
                    }

                } finally {

                    //Make sure an close the input stream
                    theBIS.close();
                }

            } catch (    URISyntaxException | IOException ex) {
                throw new LoggableException(ex);
            } finally {

                //Close the jar file
                if(tmpJarFile != null){
                    try {
                        tmpJarFile.close();
                    } catch (IOException ex) {
                        Log.log(Level.WARNING, NAME_Class, "writeJarElementToDisk()", ex.getMessage(), ex );
                    }
                }
            }

        } else {
            throw new LoggableException("Unable to retrieve JAR element.");
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
    
    //****************************************************************************
    /**
    *   Wrapper function for simpleEncrypt that takes a string and returns a Base64 encoded String.
     * @param clearText
     * @param preSharedKey
     * @return 
     * @throws pwnbrew.logging.LoggableException 
    */
    public static String simpleEncrypt( String clearText, String preSharedKey) throws LoggableException {
        
        try {
            
            byte[] dataToEncrypt = clearText.getBytes("US-ASCII");
            byte[] encryptedData = simpleEncrypt( dataToEncrypt, preSharedKey );
            return Base64Converter.encode(encryptedData);
            
        } catch (IOException ex){
           throw new LoggableException(ex);
        }
    }

    //****************************************************************************
    /**
    * Takes a byte array and input key and encrypts using
     * AES_Cipher 256
     * @param dataToEncrypt
     * @param preSharedKey
     * @return 
     * @throws pwnbrew.logging.LoggableException 
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
                byte[] encryptionKey = hash.digest(preSharedKey.getBytes("US-ASCII"));
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

        } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException | 
                InvalidAlgorithmParameterException | NoSuchAlgorithmException | 
                NoSuchPaddingException | IOException ex) {
           throw new LoggableException(ex);
        }

        return encryptedData;

    }
    
      //****************************************************************************
    /**
     * Takes a base64 encrypted string and input key and returns the clear text after
     * decoding the base 64 and decrypting using AES_Cipher 256.
     * @param encryptedText
     * @param preSharedKey
     * @return 
     * @throws pwnbrew.logging.LoggableException 
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

              byte[] decodedBytes = Base64Converter.decode(encryptedText);
              byte[] decryptedData = theDecryptCipher.doFinal( decodedBytes, 0, decodedBytes.length );
              retVal = new String(decryptedData, "US-ASCII");
           }
           
        } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | NoSuchPaddingException | NoSuchAlgorithmException | IOException ex) {
           throw new LoggableException(ex);
        }

        return retVal;

    }

    //****************************************************************************
    /**
     * Takes a base64 encrypted string and input key and returns the clear text after
     * decoding the base 64 and decrypting using AES_Cipher 256.
     * @param encryptedText
     * @param preSharedKey
     * @return 
     * @throws pwnbrew.logging.LoggableException 
    */
    public static byte[] simpleDecrypt ( byte[] encryptedText, String preSharedKey ) throws LoggableException {

        byte[] retVal = null;

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

              retVal = theDecryptCipher.doFinal( encryptedText, 0, encryptedText.length );
           }
           
        } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | NoSuchPaddingException | NoSuchAlgorithmException | IOException ex) {
           throw new LoggableException(ex);
        }

        return retVal;

    }
    
    //****************************************************************************
    /**
     *   Returns the class path for the application.
     * 
     * @return 
    */   
    public static File getClassPath(){
        return classPath;
    }

//    //****************************************************************************
//    /**
//    * Returns the four bytes that compose an file header
//     * @return 
//    */
//    public static byte[] getFileHeader(){
//        return new byte[]{ (byte)'P', (byte)'P',(byte)'F',(byte)0x00 };
//    }
//    
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
    
    //============================================================
    /**
     * Loads the classes from the jar file into the classloader
     * @param aJarFile
     * @return 
    */
    public static List<Class<?>> loadJar(File aJarFile) {
        
        List<Class<?>> theClasses = new ArrayList<>(); 
        
        if( aJarFile.exists() ){
            JarFile jarFile = null;
            try {

                URL jarURL = aJarFile.toURI().toURL();
                URLClassLoader systemLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();

                Class systemClass = URLClassLoader.class;
                Method systemMethod = systemClass.getDeclaredMethod("addURL",  new Class[]{URL.class});
                systemMethod.setAccessible(true);
                systemMethod.invoke(systemLoader, new Object[]{ jarURL });

                jarFile = new JarFile(aJarFile);
                Enumeration<JarEntry> entries = jarFile.entries();            

                //Add the classes to the list
                while(entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String entryName = entry.getName();
                    if(entryName.endsWith("class")) {
                        String className = entryName.replace('/', '.').replace('\\', '.').replace(".class", "");
                        try {
                            theClasses.add(Class.forName(className));
                        } catch (ClassNotFoundException e) {
                            continue;
                        }
                    }
                }

            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | IOException ex) {
                Log.log(Level.SEVERE, NAME_Class, "loadJar()", ex.getMessage(), ex );
            } finally {
                try {
                    if(jarFile != null){
                        jarFile.close();
                    }
                } catch (IOException ex) {
                    Log.log(Level.SEVERE, NAME_Class, "loadJar()", ex.getMessage(), ex );
                }
            }
        } else {
            Log.log(Level.SEVERE, NAME_Class, "loadJar()", "JAR file does not exist.", null );
        }
        
        return theClasses;
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

     // ==========================================================================
    /**
    *   Returns a map with the creation string and class for each
    * library item loaded into the application.
     * @return 
    */
    public static Map<String, Class> getActionControllerClassMap() {
        
        Map<String, Class> theActionClassMap = new HashMap<>();
        //Loop through the controller types and add their creation strings
        //to the right click menu
        for( String aString : theControllerList ){        
            
            Class aClass;
            try {
                
                aClass = Class.forName(aString);
                Object anObj = aClass.newInstance();
                
                //Add the menu item
                if( anObj instanceof LibraryItemController ){  
                    
                    LibraryItemController theController = (LibraryItemController)anObj;
                    String addString = theController.getCreationAction();
                    theActionClassMap.put(addString, aClass);
                }
                
            } catch (    InstantiationException | IllegalAccessException | ClassNotFoundException ex) {
            }  
        }
        
        return theActionClassMap;
        
    }
    
    //=====================================================================
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

                                if ( qtyJustRead < 0 ) { // eg -1 stream closed
                                    break;
                                }

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
                            
                            int btyeSizeOfEntry= (int)tmpZipEntry.getSize();
                            theBytesForJarEntry= new byte[btyeSizeOfEntry];
                            
                            try (InputStream tmpInputStream = tmpJarFile.getInputStream( tmpZipEntry )) {

                                int totalQtyReadAlready= 0;
                                int qtyStillNeeded= btyeSizeOfEntry;

                                int qtyJustRead;
                                while ( totalQtyReadAlready < btyeSizeOfEntry ) {
                                    qtyJustRead= tmpInputStream.read( theBytesForJarEntry, btyeSizeOfEntry-qtyStillNeeded, qtyStillNeeded);

                                    if ( qtyJustRead < 0 ) { // eg -1 stream closed
                                        break;
                                    }

                                    totalQtyReadAlready+= qtyJustRead;
                                    qtyStillNeeded-= qtyJustRead;
                                }
                            // if totalQtyread != btyeSizeOfEntry then an error has occurred
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
                Log.log(Level.WARNING, NAME_Class, "getBytesForJarElement()", ex.getMessage(), ex );
            }
        }

        return theBytesForJarEntry;
    }

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
                Log.log(Level.WARNING, NAME_Class, "createImageFromBytes()", ex.getMessage(), ex);
            }
        }

        return rtnImage;
    }
  
    
    // ==========================================================================
    /**
    * Prompts the user to confirm they want to delete the given {@link XmlBase}.
    * <p>
    * If the given {@code XmlBase} is null, this method does nothing and
    * returns 1 (the value of JOptionPane.NO_OPTION).
    *
     * @param parent
    * @param theObjectList the {@code XmlBase} in question
    *
    * @return an integer indicating the user's choice
    *            <ul>
    *            <li>JOptionPane.YES_OPTION  (0)
    *            <li>JOptionPane.NO_OPTION   (1)
    *            </ul>
    */
    public static int confirmDelete( Component parent, List<DefaultMutableTreeNode> theObjectList ) {

        if( theObjectList == null || theObjectList.isEmpty() ) { //If the XmlBase is null...
            return JOptionPane.NO_OPTION; //Do nothing, return 1
        }

        //Build the message for the user...
        StringBuilder messageBuilder = new StringBuilder( "This will permanently remove '" );
        for(int i = 0; i < theObjectList.size(); i++){        

            DefaultMutableTreeNode aNode = theObjectList.get(i);
            messageBuilder.append( aNode.toString() );
            if( i != theObjectList.size() -1 ){
                messageBuilder.append(" , ");      
            }

        }
        messageBuilder.append( "' from the library.\nDo you want continue?" );

        int rtnCode = JOptionPane.CLOSED_OPTION;
        while( rtnCode == JOptionPane.CLOSED_OPTION ) { //Until the user chooses 'Yes' or 'No'...
            //Prompt user to confirm the delete
            rtnCode = JOptionPane.showConfirmDialog( parent,
                    messageBuilder.toString(),
                    "Delete " + theObjectList.toString(),
                    JOptionPane.YES_NO_OPTION );
        }

        return rtnCode;

    }/* END confirmDelete( List<DefaultMutableTreeNode> ) */
    
    // ========================================================================
    /**
     * Returns the {@link LibraryItemController} in the given list that's controlling
     * the {@link LibraryItem} that has the given name.
     * <p>
     * If either argument is null or empty, this method does nothing and returns null.
     * 
     * @param <T>
     * @param controllers the list of {@code LibraryItemController}s to search
     * @param itemName the name of the {@code LibraryItem}
     * 
     * @return the {@code LibraryItemController} in the given list that's controlling
     * the {@code LibraryItem} having the given name; null if there isn't one
     */
     public static <T extends LibraryItemController> T findControllerByItemName( List<T> controllers, String itemName ) {
        
        if( controllers == null || controllers.isEmpty() )
            return null;
        
        if( itemName == null || itemName.isEmpty() )
            return null;
        
        T rtnT = null;
        
        for( T t : controllers ) { //For each controller...
            
            if( itemName.equals( t.getItemName() ) ) { //If the controller's item has the given name...
                rtnT = t; //Return the controller
                break; //Stop iterating throught the controllers
            } 
            
        }

        return rtnT;
        
    }/* END findControllerByItemName( List<T extends LibraryItemController>, String ) */
     
    //****************************************************************************
    /**
     * Returns a list of the previously run tasks on disk
     * @param parentDir
     * @return 
     * @throws java.io.IOException 
    */
    public static List<RemoteTask> getTasksFromDisk( File parentDir ) throws XmlBaseCreationException, IOException  {

        List<RemoteTask> theTasks = new ArrayList<>();
        File[] theTaskDirs = parentDir.listFiles();

        if( theTaskDirs != null ){
            for(File aDir : theTaskDirs){

               //Make sure it is a directory
               if(aDir.isDirectory()){

                  String taskName = aDir.getName();
                  File taskObjFile = new File(aDir, taskName + ".xml");

                  //Create a task object
                  if(taskObjFile.exists()){

                      XmlBase anObj = XmlBaseFactory.createFromXmlFile(taskObjFile);
                      if(anObj instanceof RemoteTask){
                         theTasks.add((RemoteTask)anObj);
                      } else {
                         //Delete the directory because it is not a task dir
                         FileUtilities.deleteDir(aDir);
                      }

                  } else {

                      //Delete the directory because it is not a task dir
                      theTasks.addAll( getTasksFromDisk(aDir) );
                  }
               }
            }
        }

        return theTasks;
    }
    
    //****************************************************************************
    /**
    * Returns a string for the date depending on how distant from the current date
     * @param passedDate
     * @return 
    */
    public static String getCustomDateString(Date passedDate) {

        String theDateStr;

        Calendar currCalendarDate = Calendar.getInstance();
        Calendar passedCalendarDate = Calendar.getInstance();

        currCalendarDate.setTime(new Date());
        passedCalendarDate.setTime(passedDate);

        if(currCalendarDate.get(Calendar.YEAR) == passedCalendarDate.get(Calendar.YEAR) &&
                currCalendarDate.get(Calendar.DAY_OF_YEAR) == passedCalendarDate.get(Calendar.DAY_OF_YEAR)){
           theDateStr = new SimpleDateFormat("h:mm a").format(passedDate);
        } else if(currCalendarDate.get(Calendar.YEAR) == passedCalendarDate.get(Calendar.YEAR) &&
                (currCalendarDate.get(Calendar.DAY_OF_YEAR) - 1) == passedCalendarDate.get(Calendar.DAY_OF_YEAR) ){
           theDateStr = "Yesterday";
        } else if(currCalendarDate.get(Calendar.YEAR) == passedCalendarDate.get(Calendar.YEAR)){
        
           //Show just month
           theDateStr = new SimpleDateFormat("MMMM d").format(passedDate);
        
        } else {
            
           //Show full
           theDateStr = new SimpleDateFormat("MM/d/yyyy").format(passedDate);    
        }

        return theDateStr;
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
    * Returns a list of {@link Object}s reconstructed from the xml files in the library.
    *
     * @return 
     * @throws pwnbrew.logging.LoggableException 
     * @throws java.net.SocketException 
    * @returna list of {@link Object}s reconstructed from the xml files in the library
    */
    public static Map<Host, List<XmlBase>> rebuildLibrary() throws LoggableException, SocketException {

        Map<Host, List<XmlBase>> rtnMap = new LinkedHashMap<>();
        
        File objDir = Directories.getObjectLibraryDirectory(); //Get the library directory
        if( objDir != null ) { //If the File for the directory was obtained...

            //List the dirs
            List<File> hostDirs = new ArrayList<>(Arrays.asList( objDir.listFiles() ));
           
            //Sort
            Collections.sort(hostDirs);
            for( File dirFile : hostDirs ){
                
                //Make sure it is a directory
                if( dirFile.isDirectory() ){                    
                    
                    //Create a host and get the file
                    List<File> fileList = new ArrayList<>(Arrays.asList( dirFile.listFiles() ));
                    Host aHost;
                    if( dirFile.equals( Directories.getLocalObjectLibraryDirectory() )){
                        
                        //Get the local host
                        aHost = HostFactory.getLocalHost();
                        
                    } else {
                        
                        //Find the host file
                        File theHostFile = new File( dirFile, dirFile.getName().substring(1));
                        
                        //Get the object
                        if( theHostFile.exists() ){
                            
                            //Create the host file and remove it from the list
                            aHost = (Host)XmlBaseFactory.createFromXmlFile( theHostFile);
                            fileList.remove(theHostFile);
                            
                        } else {
                            //Skip it
                            continue;
                        }
                    }
                    
                    List<XmlBase> rtnList = new ArrayList<>();
                    for( File aFile : fileList ) { //For each File...

                        try {

                            XmlBase anXB = XmlBaseFactory.createFromXmlFile( aFile ); //Reconstruct the XmlBase
                            anXB.doPostCreation();
                            
                            //Add to the list
                            rtnList.add( anXB );                            

                        } catch( XmlBaseCreationException ex ) {
                            Log.log(Level.WARNING, NAME_Class, "rebuildLibrary()", ex.getMessage(), ex );
                        }

                    }
                    
                    //Add the host and xmlbase list
                    rtnMap.put( aHost, rtnList );
                }
            }

        }

        return rtnMap;

    }/* END rebuildLibrary() */
     
     //==========================================================================
    /**
    *  Returns whether or not the first class instance's is a parent of the second
    * instance.  The check is recursive until the class being checked is Object.
     * @param parentClass
     * @param childClass
     * @return 
    */
    public static boolean isParentClass( Class parentClass, Class childClass ) { 
        
        if( !childClass.equals(Object.class) ){
            
            //Check if it is the parent
            Class childSuper = childClass.getSuperclass();
            if( !childSuper.equals(Object.class) && childSuper.equals(parentClass)){
                return true;
            } else {
                return isParentClass( parentClass, childSuper );
            }
            
        } else {
            return false;
        }
    }
    
     // ==========================================================================
    /**
     * Kill the children spawned by the parent process provided.
     * @param theProcess
    */
    public static void killChildProcesses(Process theProcess) {
        
        StringBuilder killString = new StringBuilder();
        List<String> stringList = new ArrayList<>();
        int pid;
        try {
            
            if (theProcess.getClass().getName().equals("java.lang.Win32Process") ||
                theProcess.getClass().getName().equals("java.lang.ProcessImpl")) {
                
                //Get the process handle
                Field f = theProcess.getClass().getDeclaredField("handle");
                f.setAccessible(true);				
                long handl = f.getLong(theProcess);

                //Get the pid
                Kernel32 kernel = Kernel32.INSTANCE;
                Kernel32.HANDLE handle = new Kernel32.HANDLE();
                handle.setPointer(Pointer.createConstant(handl));
                pid = kernel.GetProcessId(handle);
                
                //Create the kill string
                stringList.add("taskkill");
                stringList.add("/PID");
                stringList.add(Integer.toString(pid));
                stringList.add("/T");
            
            } else {

                if(theProcess.getClass().getName().equals("java.lang.UNIXProcess")) {

                    //Get the pid field
                    Field f = theProcess.getClass().getDeclaredField("pid");
                    f.setAccessible(true);
                    pid = f.getInt(theProcess);
                    stringList.add("/bin/bash");
                    stringList.add("-c");
                   
                    killString.append("pstree -p ").append(pid)
                            .append(" | sed 's/(/\\n(/g' | grep '(' | sed 's/(\\(.*\\)).*/\\1/' | tr '\\n' ' ' | xargs kill");

                    stringList.add(killString.toString());
                }
            }
            
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException ex) {
            Log.log(Level.WARNING, NAME_Class, "killChildProcesses()", ex.getMessage(), ex );
        } 

        String[] stringArr = stringList.toArray( new String[stringList.size()]);
        //Kill child process
        Constants.Executor.execute( new RuntimeRunnable( stringArr ));
             
    }
    
     //=====================================================================
    /*
     *  Adds a URL to the Class Loader
     */
    public static void addURLToClassLoader(URLClassLoader classLoader, URL url) throws IntrospectionException { 
        
        Class<URLClassLoader> classLoaderClass = URLClassLoader.class; 
        try { 
            Method method = classLoaderClass.getDeclaredMethod("addURL", new Class[]{URL.class}); 
            method.setAccessible(true); 
            method.invoke(classLoader, new Object[]{url}); 
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException t) { 
            throw new IntrospectionException("Error when adding url to system ClassLoader "); 
        } 
    }
    
    //=========================================================================
    /**
     * 
     * @return 
     */
    public static FileFilterImp getFileFilter() {
        return null;
    }
    
     //=========================================================================
    /**
     *  Get the client payload
     * @param clientId
     * @param passedVersion
     * @return 
     */
    public static Payload getClientPayload( int clientId, String passedVersion ) {
        
        Payload aPayload = null;            
        try{
            
            File payloadFile = Utilities.getPayloadFile(passedVersion);
            if( payloadFile.exists() ){

                byte[] byteBuffer = new byte[Constants.GENERIC_BUFFER_SIZE];
                String[] stagedClasses = new String[]{ 
                    "pwnbrew/stage/Stage",
                    "pwnbrew/stage/MemoryBufferURLConnection",
                    "pwnbrew/stage/MemoryBufferURLStreamHandler",
                    "pwnbrew/stage/Pwnbrew",
                };

                //Send each staged class
                ByteBuffer classByteBuffer = ByteBuffer.allocate( (int) (payloadFile.length() + 10000));
                for( String aClass : stagedClasses ){

                    int bytesRead = 0;
                    String thePath = aClass;             
                    InputStream anIS = SendStage.class.getClassLoader().getResourceAsStream(thePath);

                    //Read the bytes into a byte array
                    ByteArrayOutputStream theBOS = new ByteArrayOutputStream();
                    try {

                        //Read to the end
                        while( bytesRead != -1){
                            bytesRead = anIS.read(byteBuffer);
                            if(bytesRead != -1){
                                theBOS.write(byteBuffer, 0, bytesRead);
                            }
                        }

                        theBOS.flush();

                    } finally {

                        //Close output stream
                        theBOS.close();
                    }            

                    //Queue up the classes to be sent
                    byte[] tempArr = theBOS.toByteArray();
                    byte[] theBytes = new byte[ tempArr.length + 4 ];

                    byte[] classLen = SocketUtilities.intToByteArray(tempArr.length);
                    System.arraycopy(classLen, 0, theBytes, 0, classLen.length); 
                    System.arraycopy(tempArr, 0, theBytes, 4, tempArr.length);                

                    //Queue the bytes
                    classByteBuffer.put(theBytes);
                    theBOS = null;

                }

                //Add file ending byte
                classByteBuffer.put( new byte[]{ 0x0, 0x0, 0x0, 0x0});

                //Add jar and jar length
                FileInputStream anIS = new FileInputStream( payloadFile );
                try {

                    //Add the jar size
                    byte[] jarSize = SocketUtilities.intToByteArray( (int)payloadFile.length() );
                    classByteBuffer.put(jarSize);

                    //Read the bytes into the byte buffer
                    int bytesRead = 0;
                    while( bytesRead != -1){
                        bytesRead = anIS.read(byteBuffer);
                        if(bytesRead != -1){
                            classByteBuffer.put(byteBuffer, 0 , bytesRead );
                        }
                    }

                } finally {

                    try {
                        anIS.close();
                    } catch(IOException ex){

                    }
                }

                //Queue the bytes
                byte[] classBytes = Arrays.copyOf(classByteBuffer.array(), classByteBuffer.position());

                //Create message and send
                aPayload = new Payload( clientId, classBytes );

            }
            
        } catch( IOException ex ){
           Log.log(Level.SEVERE, NAME_Class, "getClientPayload()", ex.getMessage(), ex );
        }
        
        return aPayload;
    }
   
}
