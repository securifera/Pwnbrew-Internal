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

package pwnbrew;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 *
 * @author Securifera
 */
public class Installer {
      
    private static URL theURL;
    private static File classPath;
    
    //Stub name
    private static final String MALTEGO_STUB = "MaltegoStub.jar";
    private static final String MALTEGO_DIR = ".maltego";
    
    //OS properties...
    private static final String PROPERTY_OsName    = "os.name";  

    //OS name values...
    private static final String OS_NAME_Windows    = "windows";
    private static final String OS_NAME_SunSolaris   = "sunos";
    private static final String OS_NAME_Linux        = "linux";
    private static final String OS_NAME_Unix         = "unix";
    
    public static final String FILE_SEPARATOR    = System.getProperty("file.separator");
    
    //Local OS values...
    public static final String OS_NAME    = System.getProperty( PROPERTY_OsName ).toLowerCase();
    static {
        
        try {
            
            try {
                //Check if we are staging first
                theURL = Class.forName("stager.Stager").getProtectionDomain().getCodeSource().getLocation();
            } catch (ClassNotFoundException ex) {
                theURL = Installer.class.getProtectionDomain().getCodeSource().getLocation();
            }
            
            //Check for null
            classPath = new File( theURL.toURI() );            
        
        } catch( URISyntaxException | IllegalArgumentException ex1) {
        }
    }
    
    private static final List<String> OS_FAMILY_Unix;
    static {
        ArrayList<String> temp = new ArrayList<>();
        temp.add( OS_NAME_SunSolaris );
        temp.add( OS_NAME_Linux );
        temp.add( OS_NAME_Unix );
        OS_FAMILY_Unix = Collections.unmodifiableList( temp );
    }
    
      /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException {
        copyMaltegoFiles(); 
        copyMaltegoStub();
    }
    
    //===========================================================================
    /**
     * 
     * @throws java.io.IOException
     */
    public static void copyMaltegoStub() throws IOException{ 
        
        JFileChooser theFileChooser = new JFileChooser();
        theFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        theFileChooser.setMultiSelectionEnabled( false ); //Let the user select multiple files
        
        if( isWindows()){
            
            File aDir = new File("C:\\Program Files (x86)");
            if( !aDir.exists()){
                aDir = new File("C:\\Program Files");
            }
            
            //Check for maltego directory
            File malDir = new File(aDir, "Paterva");
            if( !malDir.exists()){
                int returnVal = theFileChooser.showDialog( null, "Select Maltego Installation Directory" ); //Show the dialog
                switch( returnVal ) {

                    case JFileChooser.CANCEL_OPTION: //If the user canceled the selecting...
                    case JFileChooser.ERROR_OPTION: //If the dialog was dismissed or an error occurred...
                        break; //Do nothing

                    case JFileChooser.APPROVE_OPTION: //If the user approved the selection...
                        malDir = theFileChooser.getSelectedFile(); //Get the files the user selected
                        break;
                    default:
                        break;

                }

            } else {
                
                //List the files
                File[] aFileArr = malDir.listFiles();
                if( aFileArr.length > 0 ){
                    File installDir = aFileArr[0];
                    if( installDir.isDirectory()){
                        File[] anotherFileArr = installDir.listFiles();
                        if( anotherFileArr.length > 0 ){
                            
                            //Get the install dir
                            installDir = anotherFileArr[anotherFileArr.length - 1];
                            File testFile = new File( installDir,  "uninstall.exe");
                            if( testFile.exists())
                                malDir = installDir;
                            else
                                malDir = null;
                        }
                    }
                                      
                } else
                    malDir = null;
                
            }
            
            //Move the JAR to the install dir
            if( malDir !=null && malDir.exists()){
                File destFile = new File( malDir, "pwnbrew" + FILE_SEPARATOR + MALTEGO_STUB);
                writeJarElementToDisk(destFile, MALTEGO_STUB);               
            }
            
        } else if( isUnix() ){
        
            File aDir = new File("/usr/share/maltego");
            if( !aDir.exists()){
                aDir = new File("/opt/maltego");
                if( !aDir.exists() ){
                    int returnVal = theFileChooser.showDialog( null, "Select Maltego Installation Directory" ); //Show the dialog
                    switch( returnVal ) {

                        case JFileChooser.CANCEL_OPTION: //If the user canceled the selecting...
                        case JFileChooser.ERROR_OPTION: //If the dialog was dismissed or an error occurred...
                            break; //Do nothing

                        case JFileChooser.APPROVE_OPTION: //If the user approved the selection...
                            aDir = theFileChooser.getSelectedFile(); //Get the files the user selected
                            break;
                        default:
                            break;
                      
                    }
                }
            }   
            
            //Move the JAR to the install dir
            if( aDir !=null && aDir.exists()){
                File destFile = new File( aDir, "bin" + FILE_SEPARATOR + "pwnbrew" + FILE_SEPARATOR + MALTEGO_STUB);
                writeJarElementToDisk(destFile, MALTEGO_STUB);               
            }
                        
        }
    }
    
      //===========================================================================
    /**
     * 
     * @throws java.io.FileNotFoundException
     */
    public static void copyMaltegoFiles() throws IOException{  
        
        //Get the path prefix
        String pathPrefix = null;
        if( isWindows())
            pathPrefix = System.getenv("APPDATA");
        else if( isUnix() )
            pathPrefix = System.getProperty("user.home");
    
        if( pathPrefix != null ){
            
            //Make sure the maltego file exists
            File aFile = new File(pathPrefix, MALTEGO_DIR);
            if( aFile.exists() ){
                                
                //List the files
                boolean configDirFound = false;
                File[] aFileArr = aFile.listFiles();
                for( File nextFile : aFileArr ){
                    if( nextFile.isDirectory() ){
                        File maltegoFile = new File( nextFile, "config" + FILE_SEPARATOR + "Maltego");
                        if( maltegoFile.exists()){
                            pathPrefix = nextFile.getAbsolutePath();
                            configDirFound = true;
                            break;
                        }
                    }
                }
                
                //Make sure the file exists
                if( configDirFound && classPath != null && classPath.isFile() ){                 

                    FileInputStream fis = new FileInputStream(classPath);

                    //Open the zip input stream
                    ZipInputStream theZipInputStream = new ZipInputStream(fis);
                    ZipEntry anEntry;
                    while((anEntry = theZipInputStream.getNextEntry())!=null){
                        
                        //Get the entry name
                        String theEntryName = anEntry.getName();

                        //Check if it's a maltego config file
                        if( theEntryName.contains("config")){                                    

                            aFile = new File( pathPrefix, theEntryName );                                    
                            try {
                                
                                if( theEntryName.contains(".") ){
                                    //Write the file.
                                    try (FileOutputStream theFileOS = new FileOutputStream(aFile)) {

                                        int temp;
                                        byte[] buffer = new byte[1024];
                                        while((temp = theZipInputStream.read(buffer)) > 0)
                                            theFileOS.write(buffer, 0, temp);                                    

                                        //Close the file
                                        theFileOS.flush();
                                    }
                                } else {
                                    //Make the directories
                                    if( !aFile.exists())
                                        aFile.mkdirs();
                                }
                            } catch( FileNotFoundException ex ){
                                
                            }
                        }

                    }

                }
                
            } else {
                
                //Show error message
                JOptionPane.showMessageDialog(null, "Unable to locate maltego config directory. "
                        + "\n Please ensure the installer is being run\n   as the user that installed Maltego.");
            }
        }
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
    static public boolean isUnix() {
        return OS_FAMILY_Unix.contains( OS_NAME );
    }/* END isUnix() */
    
// ==========================================================================
    /**
    * Writes the jar element to disk
    *
    * @param filePath
    * @param passedJarElementName
     * @throws java.io.IOException
    */
    public static void writeJarElementToDisk( File filePath, String passedJarElementName ) throws IOException {

        int bytesRead = 0;
        if( passedJarElementName!=null ){
                
//                String theResourceStr = "/" + passedJarElementName;
                InputStream theIS = Installer.class.getClassLoader().getResourceAsStream(passedJarElementName);
                if( theIS != null ){
                    
                    try {

                        if(filePath != null){

                            byte[] byteBuffer = new byte[ 4096 ];
                            FileOutputStream theOutStream = new FileOutputStream(filePath);
                            try (BufferedOutputStream theBOS = new BufferedOutputStream(theOutStream)) {

                                //Read to the end
                                while( bytesRead != -1){
                                    bytesRead = theIS.read(byteBuffer);
                                    if(bytesRead != -1)
                                        theBOS.write(byteBuffer, 0, bytesRead);                                   
                                }

                                theBOS.flush();

                            }
                        }
                        
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
    
    
}
