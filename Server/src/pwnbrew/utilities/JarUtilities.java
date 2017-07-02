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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import pwnbrew.misc.Constants;
import pwnbrew.misc.ManifestProperties;
import pwnbrew.validation.StandardValidation;

/**
 *
 * @author Securifera
 */
@SuppressWarnings("ucd")
public class JarUtilities {
    
    public static final String URL_LABEL ="Private";
    private static final String prefixStr = "https://";

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException {
        //Assign the service name
        if( args.length > 1 ) {
            
            //Get the first options
            String firstOption = args[0];
            String firstValue = args[1];
            
            File stagerFile = new File(firstValue);
            if( stagerFile.exists() ){
                switch(firstOption){
                    case "-l":                    
                        printStagerConfiguration(stagerFile, Constants.PROP_FILE, URL_LABEL);
                        return;
                    case "-c":
                        if( args.length == 6 ){
                            if( args[2].equals("-i")){
                                String ipAddress = args[3].trim();
                                if( StandardValidation.validateIpAddress(ipAddress)){
                                    if( args[4].equals("-p")){
                                        String port = args[5].trim();
                                        if( StandardValidation.validatePort(port)){
                                            String connectStr = prefixStr + ipAddress + ":" + port;
                                            sun.misc.BASE64Encoder anEncoder = new sun.misc.BASE64Encoder();
                                            String encodedStr = anEncoder.encodeBuffer(connectStr.getBytes());
                                            if(encodedStr.contains("=")){
                                                connectStr += " ";
                                                encodedStr = anEncoder.encodeBuffer(connectStr.getBytes());
                                                if(encodedStr.contains("=")){
                                                    connectStr += " ";
                                                    encodedStr = anEncoder.encodeBuffer(connectStr.getBytes());
                                                }
                                            }
                                            updateJarProperties(stagerFile, Constants.PROP_FILE, URL_LABEL, encodedStr);
                                            System.out.println("Successfully updated stager.");
                                            return;
                                        }else {
                                            System.out.println("Invalid Port.");                                            
                                        }
                                    }
                                } else {
                                    System.out.println("Invalid IP Address.");                                            
                                }
                            }
                        }
                        break;    
                    default:
                        break;
                }
            }
        
        } 
                
        //Print Usage
        printUsage();
        
    }
        
    //========================================================================
    /**
     * Print stager info
     */
    private static void printUsage(){
        StringBuilder aSB = new StringBuilder();
        aSB.append("Usage: java -cp <this jar> pwnbrew.utilities.JarUtilities\n")
                .append(" -h\t\t\t\t\t\t\t\tPrint usage\n")
                .append(" -l <Stager Path>\t\t\t\t\t\tPrint stager configuration information\n")
                .append(" -c <Stager Path> -i <Server IP Address> -p <Server Port>\tChange the stager configuration\n");
                
        System.out.println(aSB.toString());
    }
    
     //===========================================================================
    /**
     * 
     * @param passedFile
     * @param properties 
     * @param propLabel 
     * @throws java.io.IOException 
     */
    public static void printStagerConfiguration( File passedFile, String properties,
            String propLabel ) throws IOException{        
    
        if( passedFile != null && passedFile.isFile() ){              
            
            //Creat an inputstream
            FileInputStream aFIS = new FileInputStream(passedFile);    

            //Open the zip input stream
            ZipInputStream theZipInputStream = new ZipInputStream(aFIS);
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
                        String theConnectStr = localProperties.getProperty(propLabel);
                        if (theConnectStr != null){
                            
                            StringBuilder aSB = new StringBuilder();
                            //Decode the URL
                            sun.misc.BASE64Decoder aDecoder = new sun.misc.BASE64Decoder();
                            String decodedURL = new String( aDecoder.decodeBuffer(theConnectStr) ).trim();
                            if ( decodedURL.startsWith(prefixStr)) {
                                decodedURL = decodedURL.replace(prefixStr, "");
                                String[] serverArr = decodedURL.split(":");
                                if( serverArr.length == 2){                                
                                    aSB.append("Stager Configuration:\n")
                                            .append("\tServer IP address:\t").append(serverArr[0]).append("\n")
                                            .append("\tServer port:\t\t").append(serverArr[1]).append("\n");
                                } else {
                                    aSB.append("Error: incorrect connect string.\n")
                                            .append("\"").append(decodedURL)
                                            .append("\"");
                                }        
                            } else {
                                    aSB.append("Error: incorrect connect string.\n")
                                            .append("\"").append(decodedURL)
                                            .append("\"");
                            } 
                            System.out.println(aSB.toString());
                        }

                        break;
                    } 

                }


            } finally {
                try {
                    theZipInputStream.close();
                } catch (IOException ex) { ex = null; }
            }
          
        }
    }
    
     //===========================================================================
    /**
     * 
     * @param passedFile
     * @param properties 
     * @param propLabel 
     * @param propValue 
     */
    private static void updateJarProperties( File passedFile, String properties,
            String propLabel, String propValue ) throws IOException{        
    
        if( passedFile != null && passedFile.isFile() ){                 
            
            //Open the zip
            ByteArrayOutputStream aBOS = new ByteArrayOutputStream();
                            
            FileInputStream fis = new FileInputStream(passedFile);
            try{

                //Read into the buffer
                byte[] buf = new byte[1024];                
                for (int readNum; (readNum = fis.read(buf)) != -1;) {
                    aBOS.write(buf, 0, readNum);
                }

            //Close and delete
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
                        localProperties.setProperty(propLabel, propValue.trim());


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

            } finally {
                try {
                    theZipInputStream.close();
                } catch (IOException ex) { ex = null; }
            }
           
        }
    }
}
