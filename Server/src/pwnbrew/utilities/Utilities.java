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

import pwnbrew.xml.JarItemException;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.beans.IntrospectionException;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.ByteBuffer;
import java.security.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.imageio.ImageIO;
import pwnbrew.host.Host;
import pwnbrew.host.HostFactory;
import pwnbrew.log.Log;
import pwnbrew.log.LoggableException;
import pwnbrew.misc.Constants;
import pwnbrew.misc.Directories;
import pwnbrew.xml.XmlObject;
import pwnbrew.exception.XmlObjectCreationException;
import pwnbrew.xml.XmlObjectFactory;
import pwnbrew.misc.IdGenerator;
import pwnbrew.network.control.messages.Payload;
import pwnbrew.network.control.messages.SendStage;
import pwnbrew.xml.JarItem;


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
              
    private static final String WINDOWS_LINE_SEPARATOR = "\r\n";
    private static final String UNIX_LINE_SEPARATOR = "\n";
    
    private static final Map<String, JarItem> theStagerRefMap = new HashMap<>();
    private static final Map<String, JarItem> thePayloadRefMap = new HashMap<>();
    private static final Map<String, JarItem> theLocalExtMap = new HashMap<>();
    private static final Map<String, JarItem> theRemoteExtMap = new HashMap<>();
    
    private static final SecureRandom aSR = new SecureRandom();
    
     //UNIX OS family...
    private static final List<String[]> CITY_STATE_TUPLES;
    static {
        ArrayList<String[]> temp = new ArrayList<>();
        temp.add( new String[] { "San Francisco", "CA"} );
        temp.add( new String[] { "Palo Alto", "CA"} );
        temp.add( new String[] { "San Jose", "CA"} );
        temp.add( new String[] { "Seattle", "WA"} );
        temp.add( new String[] { "Mountain View", "CA"} );
        temp.add( new String[] { "Los Angeles", "CA"} );
        temp.add( new String[] { "New York City", "NY"} );
        CITY_STATE_TUPLES = Collections.unmodifiableList( temp );
    }
    
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
    private static final String JAVA_ARCH    = System.getProperty( PROPERTY_OsArch ).toLowerCase();
    
    //IPv4 address regular expressions...
    private static final String REGEX_Ipv4Octet = "25[0-5]|2[0-4]\\d|[01]?\\d?\\d";
    private static final String REGEX_Ipv4Address = "((" + REGEX_Ipv4Octet + ")\\.){3}(" + REGEX_Ipv4Octet + ")";

    //Add port to IPv4
    private static final String REGEX_PORT = "(6553[0-5]|655[0-2]\\d|65[0-4]\\d{2}|6[0-4]\\d{3}|5\\d{4}|[0-9]\\d{0,3})";
    //Special Validation for Client
    private static final String REGEX_Client_Connection = REGEX_Ipv4Address +"(:("+REGEX_PORT+"))";
    
    private final static ArrayList<String> theSubnetMaskList = new ArrayList<>();
    static {

        theSubnetMaskList.add( "0.0.0.0" );        //0        
        theSubnetMaskList.add( "128.0.0.0" );        //1
        theSubnetMaskList.add( "192.0.0.0" );        //2
        theSubnetMaskList.add( "224.0.0.0" );        //3
        theSubnetMaskList.add( "240.0.0.0" );        //4
        theSubnetMaskList.add( "248.0.0.0" );        //5
        theSubnetMaskList.add( "252.0.0.0" );        //6
        theSubnetMaskList.add( "254.0.0.0" );        //7
        theSubnetMaskList.add( "255.0.0.0" );        //8

        theSubnetMaskList.add( "255.128.0.0" );      //9
        theSubnetMaskList.add( "255.192.0.0" );      //10
        theSubnetMaskList.add( "255.224.0.0" );      //11
        theSubnetMaskList.add( "255.240.0.0" );      //12
        theSubnetMaskList.add( "255.248.0.0" );      //13
        theSubnetMaskList.add( "255.252.0.0" );      //14
        theSubnetMaskList.add( "255.254.0.0" );      //15
        theSubnetMaskList.add( "255.255.0.0" );      //16

        theSubnetMaskList.add( "255.255.128.0" );    //17
        theSubnetMaskList.add( "255.255.192.0" );    //18
        theSubnetMaskList.add( "255.255.224.0" );    //19
        theSubnetMaskList.add( "255.255.240.0" );    //20
        theSubnetMaskList.add( "255.255.248.0" );    //21
        theSubnetMaskList.add( "255.255.252.0" );    //22
        theSubnetMaskList.add( "255.255.254.0" );    //23
        theSubnetMaskList.add( "255.255.255.0" );    //24

        theSubnetMaskList.add( "255.255.255.128" );  //25
        theSubnetMaskList.add( "255.255.255.192" );  //26
        theSubnetMaskList.add( "255.255.255.224" );  //27
        theSubnetMaskList.add( "255.255.255.240" );  //28
        theSubnetMaskList.add( "255.255.255.248" );  //29
        theSubnetMaskList.add( "255.255.255.252" );  //30
        theSubnetMaskList.add( "255.255.255.254" );  //31
        theSubnetMaskList.add( "255.255.255.255" );  //32

    }


    // ==========================================================================
    /**
    * Gets the octet notation associated with the given integer subnet mask.
    *
    * @param passedMask the subnet mask in integer notation
    * 
    * @return the corresponding octet notation or NULL if the arguement is
    * invalid
    */
    public static String get( int passedMask ) {

        if( passedMask < 0 || 32 < passedMask ) { //If the given int is not a mask...
            return null; //...the arguement is invalid
        }

        return theSubnetMaskList.get( passedMask ); //Return the mask's octet notation

    }/* END get( int ) */


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
     * @throws pwnbrew.xml.JarItemException 
     */
    public static JarItem getJavaItem( File payloadFile ) throws JarItemException{
        
        //Remove JAR if that's how we are running
        JarItem aJarItem = null;        
        if( payloadFile != null ){ 

            try {                 
                
                aJarItem = (JarItem) XmlObjectFactory.instantiateClass( JarItem.class );
                aJarItem.setId( IdGenerator.next() );
                String jarVersionString = "";
                String jvmVersionString = "";
                String jarType = "";
                String jarUID = "";
       
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
                            
                            //Get the type
                            String type = localProperties.getProperty(Constants.MODULE_TYPE_LABEL);
                            if( type == null )
                                throw new JarItemException("The selected JAR is not a valid Pwnbrew module.");
                            
                            //Switch on string                            
                            switch( type.trim().toLowerCase()) {
                                case Constants.PAYLOAD_ALIAS_TYPE:
                                    jarType = Constants.PAYLOAD_TYPE;
                                    break;
                                case Constants.STAGER_ALIAS_TYPE:
                                    jarType = Constants.STAGER_TYPE;
                                    break;
                                case Constants.LOCAL_ALIAS_EXTENSION_TYPE:
                                    jarType = Constants.LOCAL_EXTENSION_TYPE;
                                    break;
                                case Constants.REMOTE_ALIAS_EXTENSION_TYPE:
                                    jarType = Constants.REMOTE_EXTENSION_TYPE;
                                    break;
                                default:
                                    throw new JarItemException("The selected JAR is not a valid Pwnbrew module.");
                            }

                            //Get the version
                            String version = localProperties.getProperty(Constants.PAYLOAD_VERSION_LABEL);
                            if( version != null ){
                                //Set the jar version
                                jarVersionString = version;
                            }   
                            
                            //Get the id
                            String uid = localProperties.getProperty(Constants.UID_LABEL);
                            if( uid != null ){
                                //Set the jar uid
                                jarUID = uid;
                            }  
                            
                            if( jarType.equals( JarItem.STAGER_TYPE )){
                                String aStr = localProperties.getProperty(Constants.STAGER_URL);

                                sun.misc.BASE64Decoder aDecoder = new sun.misc.BASE64Decoder();
                                byte[] decodedBytes = aDecoder.decodeBuffer(aStr);
                                String connectStr = new String(decodedBytes).replace("https://", "").trim();


                                boolean isValid = validateClientConnect(connectStr);
                                if( !isValid )
                                    throw new JarItemException("The JAR item does not contain a valid [IP Address:Port] connection string.");

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

                    //throw exception
                    if( jarType.isEmpty() || jarVersionString.isEmpty() || jvmVersionString.isEmpty() || jarUID.isEmpty() )
                        throw new JarItemException("The selected JAR is not a valid Pwnbrew module.");
                    
                    aJarItem.setId(jarUID);
                    aJarItem.setVersion(jarVersionString);
                    aJarItem.setJvmMajorVersion(jvmVersionString);
                    aJarItem.setFilename( payloadFile.getName() );
                    aJarItem.setType(jarType);
                    
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
     * Determines if the given String is a valid connection string. (ie. "172.16.254.1:80")
     * <p>
     * If the argument is null this method returns false.
     *
     * @param address the String to test
     *
     * @return {@code true} if the given String is a valid connection string; {@code false} otherwise
     */
    private static boolean validateClientConnect( String address ) {

        if( address == null ) //If the String is null...
            return false; //Do nothing

        return address.matches( REGEX_Client_Connection ); //Determine if the String is a IPv4 address

    }/* END validateClientConnect( String ) */

    //=======================================================================
    /**
     * 
     * @return 
     */
    @SuppressWarnings("ucd")
    public static String[] nextCityState() {
        int index = aSR.nextInt(CITY_STATE_TUPLES.size() );
        return CITY_STATE_TUPLES.get(index);
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
     * @throws pwnbrew.log.LoggableException
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
    @SuppressWarnings("ucd")
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
    
    // ==========================================================================
    /**
    * 
    * @param passedBytes
    * @return
    */
    public static String convertHexBytesToString( byte [] passedBytes ) {
        if ( passedBytes == null )
            return null;
        
        StringBuilder strBuf= new StringBuilder();
        int byteInt;
        for ( int i = 0; i < passedBytes.length; i++ ) {

            byteInt = passedBytes[ i ];
            byteInt = byteInt << 24;
            byteInt = byteInt >>> 24;

            //Add the byte as hex to the String
            strBuf.append( byteInt < 16 ? "0" + Integer.toHexString( byteInt ) : Integer.toHexString( byteInt ) );
        }

        return strBuf.toString().toUpperCase();
    }
      
    // ==========================================================================
    /**
     *   Returns the class path for the application.
     * 
     * @return 
    @SuppressWarnings("ucd")
    */   
    public static File getClassPath(){
        return classPath;
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
     * @throws pwnbrew.log.LoggableException 
     * @throws java.net.SocketException 
    * @returna list of {@link Object}s reconstructed from the xml files in the library
    */
    public static Map<Host, List<XmlObject>> rebuildLibrary() throws LoggableException, SocketException {

        Map<Host, List<XmlObject>> rtnMap = new LinkedHashMap<>();
        
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
                        
                        //Get the host file
                        Host currHost = HostFactory.getLocalHost();
                        
                        //Get the local host
                        File theHostFile = new File( dirFile, dirFile.getName());
                        if( theHostFile.exists() ){
                            
                            //Create the host file and remove it from the list
                            aHost = (Host)XmlObjectFactory.createFromXmlFile( theHostFile);
                            aHost.updateData(currHost);    
                            aHost.setConnected(true);
                            HostFactory.setLocalHost(aHost);
                            
                        } else {                            
                            //Set host
                            aHost = currHost;
                        }
                        
                    } else {
                        
                        //Find the host file
                        File theHostFile = new File( dirFile, dirFile.getName().substring(1));
                        
                        //Get the object
                        if( theHostFile.exists() ){
                            
                            //Create the host file and remove it from the list
                            aHost = (Host)XmlObjectFactory.createFromXmlFile( theHostFile);
                            fileList.remove(theHostFile);
                            
                        } else {
                            //Skip it
                            continue;
                        }
                    }
                    
                    List<XmlObject> rtnList = new ArrayList<>();
                    for( File aFile : fileList ) { //For each File...

                        try {

                            XmlObject anXB = XmlObjectFactory.createFromXmlFile( aFile ); //Reconstruct the XmlObject
                            
                            //Add to the list
                            rtnList.add( anXB );                            

                        } catch( XmlObjectCreationException ex ) {
                            Log.log(Level.WARNING, NAME_Class, "rebuildLibrary()", ex.getMessage(), ex );
                        }

                    }
                    
                    //Add the host and XmlObject list
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
     *  Get the client payload
     * @param clientId
     * @param passedVersion
     * @return 
     */
    public static Payload getClientPayload( int clientId, String passedVersion ) {
        
        Payload aPayload = null;            
        try{
            
            File payloadFile = Utilities.getPayloadFile(passedVersion);
            if( payloadFile != null && payloadFile.exists() ){

                byte[] byteBuffer = new byte[Constants.GENERIC_BUFFER_SIZE];
                String[] stagedClasses = new String[]{ 
                    "pwnbrew/network/stage/Stage",
                    "pwnbrew/network/stage/MemoryJarFile",
                    "pwnbrew/network/stage/MemoryBufferURLConnection",
                    "pwnbrew/network/stage/MemoryBufferURLStreamHandler",
                    "pwnbrew/network/stage/Pwnbrew",
                };

                //Send each staged class
                ByteBuffer classByteBuffer = ByteBuffer.allocate( (int) (payloadFile.length() + 20000));
                for( String aClass : stagedClasses ){

                    int bytesRead = 0;
                    String thePath = aClass;             
                    InputStream anIS = SendStage.class.getClassLoader().getResourceAsStream(thePath);
                    if( anIS != null ){

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
    
    //=========================================================================
    /**
     *  Gets the bytes for the passed class path
     * @param passedClassPath
     * @return 
     */
    public static byte[] getClassBytes( String passedClassPath ) {
        
        byte[] retBytes = null;  
        try{

            int bytesRead = 0;
            passedClassPath = passedClassPath.replace(".", "/") + ".class";
            InputStream anIS = Utilities.class.getClassLoader().getResourceAsStream(passedClassPath);

            //Read the bytes into a byte array
            if( anIS != null ){
                
                ByteArrayOutputStream theBOS = new ByteArrayOutputStream();
                try {

                    //Read to the end
                    byte[] byteArr = new byte[1024];
                    while( bytesRead != -1){
                        bytesRead = anIS.read(byteArr);
                        if(bytesRead != -1){
                            theBOS.write(byteArr, 0, bytesRead);
                        }
                    }

                    theBOS.flush();

                } finally {

                    //Close output stream
                    theBOS.close();
                }            

                //Queue up the classes to be sent
                retBytes = theBOS.toByteArray();
            }
            
        } catch( IOException ex ){
           Log.log(Level.SEVERE, NAME_Class, "getClassBytes()", ex.getMessage(), ex );
        }
        
        return retBytes;
    }
    
    //=======================================================================
    /**
     * 
     * @return 
     @SuppressWarnings("ucd")
     */
    public static String nextString() {
        
        //Get the string length
        String aStr = "";
        int strLen = aSR.nextInt( 10 ) + 1;
        for( int i = 0; i < strLen; i++ ){
            char aChar = (char)(aSR.nextInt(25) + 97);
            aStr += aChar;
        }
        return aStr;
    }
   
}
