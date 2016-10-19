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

import java.beans.IntrospectionException;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import pwnbrew.log.RemoteLog;


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
    
    //Local OS values...
    public static final String OS_NAME    = System.getProperty( PROPERTY_OsName ).toLowerCase();
    public static final String JAVA_ARCH    = System.getProperty( PROPERTY_OsArch ).toLowerCase();
    
    private static final String PATH_SEP = System.getProperty("path.separator");
    private static final boolean IS_AIX = "aix".equals(OS_NAME);
    private static final boolean IS_DOS = PATH_SEP.equals(";");
    private static final String JAVA_HOME = System.getProperty("java.home");

    //UNIX OS family...
    private static final List<String> OS_FAMILY_Unix;
    static {
        ArrayList<String> temp = new ArrayList<>();
        temp.add( OS_NAME_SunSolaris );
        temp.add( OS_NAME_Linux );
        temp.add( OS_NAME_Unix );
        OS_FAMILY_Unix = Collections.unmodifiableList( temp );
    }

    
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
    * Determines if the local host is running an OS that is in the UNIX family.
    *
    * @return {@code true} if the local OS is a flavor of UNIX, {@code false}
    * otherwise
    */
    @SuppressWarnings("ucd")
    static public boolean isUnix() {
        return OS_FAMILY_Unix.contains( OS_NAME );
    }/* END isUnix() */
  
    //Used for simple encrypt and decrypt
    private static final String AES_CFB_ENCRYPTION = "AES/CFB/NoPadding";
    private static final String AES_Cipher = "AES";
  
    private static final String NAME_Class = Utilities.class.getSimpleName();

    private static URL theURL;
    private static File classPath;

    static {
        
        try {
            
            try {
                //Check if we are staging first
                theURL = Class.forName("stager.Stager").getProtectionDomain().getCodeSource().getLocation();
            } catch (ClassNotFoundException ex) {
                theURL = Utilities.class.getProtectionDomain().getCodeSource().getLocation();
            }
            
            //Check for null
            classPath = new File( theURL.toURI() );            
        
        } catch( URISyntaxException ex1) {
            ex1 = null;
        } catch( IllegalArgumentException ex ){
                
        }
    }
    
    //===============================================================
    /**
     *  Returns whether the client is staged
     * @return 
     */
    public static boolean isStaged(){
        boolean retVal = true;
        try {
            Class.forName("stager.Stager");
        } catch (ClassNotFoundException ex) {
            retVal = false;
        }
        return retVal;
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
    
    //======================================================================
    /**
     *   Returns the class path for the application.
     * 
     * @return 
    */   
    public static URL getURL(){
        return theURL;
    }
    
    //======================================================================
    /**
     *   Returns the class path for the application.
     * 
     * @return 
    */   
    public static File getClassPath(){
        return classPath;
    }

    //===========================================================================
    /**
     * 
     * @param passedFile
     * @param properties 
     * @param propLabel 
     * @param propValue 
     */
    public static void updateJarProperties( File passedFile, String properties,
            String propLabel, String propValue ){        
    
        if( passedFile != null && passedFile.isFile() ){                 
            
            //Open the zip
            ByteArrayOutputStream aBOS = new ByteArrayOutputStream();
            try {
                
                FileInputStream fis = new FileInputStream(passedFile);
                try{

                    //Read into the buffer
                    byte[] buf = new byte[1024];                
                    for (int readNum; (readNum = fis.read(buf)) != -1;) {
                        aBOS.write(buf, 0, readNum);
                    }

                //Close and delete
                } catch (IOException ex) {
                    RemoteLog.log(Level.WARNING, NAME_Class, "updateJarProperties()", ex.getMessage(), ex);        
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
                            localProperties.setProperty(propLabel, propValue);
    

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
                    RemoteLog.log(Level.WARNING, NAME_Class, "updateJarProperties()", ex.getMessage(), ex);        
                } finally {
                    try {
                        theZipInputStream.close();
                    } catch (IOException ex) { ex = null; }
                }
                
            } catch (FileNotFoundException ex ){
                RemoteLog.log(Level.WARNING, NAME_Class, "updateJarProperties()", ex.getMessage(), ex);        
            }
        }
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
     *  Get the path to the JRE
     * @param paramString
     * @return 
     */
    public static String getJreExecutable(String paramString){
        
        File localFile = null;
        if (IS_AIX)
            localFile = findInDir(JAVA_HOME + "/sh", paramString);
        if (localFile == null)
            localFile = findInDir(JAVA_HOME + "/bin", paramString);
        if (localFile != null)
            return localFile.getAbsolutePath();
        return addExtension(paramString);
    }

    // ==========================================================================
    /**
     *  Add the extension
     * 
     * @param paramString
     * @return 
     */
    private static String addExtension(String paramString){
        return paramString + (IS_DOS ? ".exe" : "");
    }

    // ==========================================================================
    /**
     *  Construct the path 
     * 
     * @param paramString1
     * @param paramString2
     * @return 
     */
    private static File findInDir(String paramString1, String paramString2){
        
        File localFile1 = normalize(paramString1);
        File localFile2 = null;
        if (localFile1.exists()){
            localFile2 = new File(localFile1, addExtension(paramString2));
            if (!localFile2.exists())
                localFile2 = null;
        }
        return localFile2;
    }
    
    // ==========================================================================
    /**
     *  Normalize the string
     * 
     * @param paramString
     * @return 
    */
    private static File normalize(String paramString){
        
        Stack localStack = new Stack();
        String[] arrayOfString = dissect(paramString);
        localStack.push(arrayOfString[0]);
        StringTokenizer localStringTokenizer = new StringTokenizer(arrayOfString[1], File.separator);
        
        while (localStringTokenizer.hasMoreTokens()){
            String localObject = localStringTokenizer.nextToken();
            if (!".".equals(localObject))
            if ("..".equals(localObject)){
                if (localStack.size() < 2)
                    return new File(paramString);
                localStack.pop();
            } else {
                localStack.push(localObject);
            }
        }
        
        Object localObject = new StringBuffer();
        for (int i = 0; i < localStack.size(); i++){
            if (i > 1)
                ((StringBuffer)localObject).append(File.separatorChar);
            ((StringBuffer)localObject).append(localStack.elementAt(i));
        }
        return new File(((StringBuffer)localObject).toString());
    }
    
    //=====================================================================
    /**
     * 
     * @param connectStr 
     * @throws java.lang.ClassNotFoundException 
     * @throws java.io.IOException 
     */
    public static void updateServerInfoInJar( String connectStr ) throws ClassNotFoundException, IOException{
                
        Class stagerClass = Class.forName("stager.Stager");
        File theClassPath = Utilities.getClassPath();
        ClassLoader aClassLoader = stagerClass.getClassLoader();            

        //Get append spaces to avoid == at the end
        StringBuilder aSB = new StringBuilder()
            .append("https://")
            .append( connectStr )
            .append("/");
        int neededChars = aSB.length() % 3;
        for( int i = 0; i < neededChars + 1; i++)
            aSB.append(" ");

        //Decode the base64   
        DebugPrinter.printMessage(NAME_Class, "Migrating to " + aSB.toString());
        
        sun.misc.BASE64Encoder anEncoder = new sun.misc.BASE64Encoder();
        connectStr = anEncoder.encodeBuffer(connectStr.getBytes());
        
        String properties = Constants.PROP_FILE;
        String propLabel = Constants.URL_LABEL;  

        //Unload the stager
        LoaderUtilities.unloadLibs( aClassLoader );

        //Replace the file
        Utilities.updateJarProperties( theClassPath, properties, propLabel, connectStr );

        //Load it back                    
        LoaderUtilities.reloadLib(theClassPath); 
     
    }
    
   
//    // ==========================================================================
//    /**
//     * Attempt to restart pwnbrew
//     * 
//     * @param passedManager
//     * @param passedBool
//     * @param milDelay
//     */
//    public static void restart( CommManager passedManager, boolean passedBool, final int milDelay ){
//        
//        if( Utilities.isStaged()){
//                
//            try {
//                
//                Class stagerClass = Class.forName("stager.Stager");            
//                Field aField = stagerClass.getField("serviceName");
//                File theClassPath = Utilities.getClassPath(); 
//                
//                final List<String> strList = new ArrayList<>();
//                //Get the jre path
//                String jrePath = Utilities.getJreExecutable("java"); 
//                strList.add(jrePath);
//                strList.add("-jar");
//                strList.add(theClassPath.getAbsolutePath());
//                
//                Object anObj = aField.get(null);
//                if( anObj != null && anObj instanceof String ){
//
//                    //Cast to string
//                    String svcStr = (String)anObj;
//                    if( !svcStr.isEmpty() && Utilities.isWindows() ){
//                           
//                        //Clear the list
//                        strList.clear();
//
//                        //Tell the service to stop and restart
//                        strList.add("cmd.exe");
//                        strList.add("/c");
//
//                        StringBuilder aSB = new StringBuilder()
//                                .append("net stop \"").append(svcStr).append("\"")
//                                .append(" && net start \"").append(svcStr).append("\"");
//                        strList.add(aSB.toString());
//                        
//                        //Run restart command
//                        Constants.Executor.execute( new Runnable(){
//
//                            @Override
//                            public void run() {
//                                if( milDelay > 0 ){
//                                    try {
//                                        Thread.sleep(milDelay);
//                                    } catch (InterruptedException ex) {
//                                    }
//                                }
//                                
//                                //Run the command
//                                try {
//                                    Runtime.getRuntime().exec(strList.toArray( new String[strList.size()]) );
//                                } catch (IOException ex) {
//                                }
//                            }
//                        
//                        });
//                        return;
//
//                    } 
//                    
//                } 
//                
//                DebugPrinter.printMessage(NAME_Class, strList.toString());
//                //Tell it to run itself on shutdown
//                Runtime.getRuntime().addShutdownHook( new Thread(){
//                
//                    @Override
//                    public void run(){       
//                        try {
//                            //Run restart command
//                            Runtime.getRuntime().exec(strList.toArray( new String[strList.size()]) );
//                        } catch (IOException ex) {
//                        }
//                    }
//                
//                });
//                    
//                //Tell it not to reconnect
//                ClientPortRouter aCPR = (ClientPortRouter)passedManager.getPortRouter(ClientConfig.getConfig().getSocketPort());
//                aCPR.setReconnectFlag( passedBool );
//                passedManager.shutdown();   
//                                               
//            } catch (ClassNotFoundException | NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
//                RemoteLog.log(Level.WARNING, NAME_Class, "restart()", ex.getMessage(), ex);        
//            }
//        }
//        
//    }
    

    // ==========================================================================
    /**
     *  Dissect the path
     * 
     * @param paramString
     * @return 
     */
    private static String[] dissect(String paramString){
        char c = File.separatorChar;
        paramString = paramString.replace('/', c).replace('\\', c);
        String str;
        int i = paramString.indexOf(':');
        int j;
        if ((i > 0) && (IS_DOS)){
            j = i + 1;
            str = paramString.substring(0, j);
            char[] arrayOfChar = paramString.toCharArray();
            str = str + c;
            j = arrayOfChar[j] == c ? j + 1 : j;
            StringBuilder localStringBuffer = new StringBuilder();
            for (int k = j; k < arrayOfChar.length; k++)
                if ((arrayOfChar[k] != c) || (arrayOfChar[(k - 1)] != c))
                    localStringBuffer.append(arrayOfChar[k]);
            paramString = localStringBuffer.toString();
        } else if ((paramString.length() > 1) && (paramString.charAt(1) == c)){
            j = paramString.indexOf(c, 2);
            j = paramString.indexOf(c, j + 1);
            str = j > 2 ? paramString.substring(0, j + 1) : paramString;
            paramString = paramString.substring(str.length());
        } else {
            str = File.separator;
            paramString = paramString.substring(1);
        }
        return new String[] { str, paramString };
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
