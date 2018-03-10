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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;

/**
 *
 * @author Securifera
 */
public class Installer {
      
    private static URL theURL;
    private static File classPath;
    
    private static final String README_FILE = "README.txt";
        
    private static final String SERVER_JAR = "Server.jar";
    private static final String LICENSE_FILE = "LicenseAgreement.txt";
    private static final String IMPORT_FILE = "pwnbrew.mtz";
    private static final String MALTEGO_STUB = "MaltegoStub.jar";
    private static final String SSL_BAT = "sslutility.bat";
    private static final String MALTEGO = "maltego";
    private static File MALTEGO_INSTALL_DIR = null;
    private static File INSTALL_DIR = null;
    
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
    public static void main(String[] args) throws IOException, BadLocationException {
        
        String lookAndFeelClassStr = "javax.swing.plaf.metal.MetalLookAndFeel";
        if( isWindows() )
            lookAndFeelClassStr = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
        
        try{
            UIManager.setLookAndFeel( lookAndFeelClassStr );
        } catch ( ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
        }
        
        byte[] aByteArr = getJarElementBytes(LICENSE_FILE);
         
        JTextPane textPane = new JTextPane();
        StyledDocument doc = textPane.getStyledDocument();
        doc.insertString(0, new String(aByteArr), null);
        
        JPanel noWrapPanel = new JPanel(new BorderLayout());
        noWrapPanel.setPreferredSize(new Dimension(400,300));
        noWrapPanel.add(textPane);
        JScrollPane scrollPane = new JScrollPane(noWrapPanel);
        scrollPane.setPreferredSize(new Dimension(400,300));
        scrollPane.setViewportView(textPane); // creates a wrapped scroll pane using the text pane as a viewport.
                    
        int dialogValue = JOptionPane.showConfirmDialog(null, scrollPane, "Do you accept the license agreement?", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
        if ( dialogValue != JOptionPane.YES_OPTION )
            return;
                            
        InstallerGui aIG = new InstallerGui();
        //aIG.setLocationRelativeTo(null);
        //Point parentLocation = aIG.getLocation();
        aIG.setLocation(50, 50);
         
        //aIG.setAlwaysOnTop(true);
        aIG.setVisible(true);
        
        aIG.setButtonText("Cancel");
        aIG.addStatusString("Starting installation...\n");
        aIG.addStatusString("Copying Pwnbrew Files...\n");
        copyPwnbrewFiles();
        aIG.addStatusString("Copying Maltego Import File...\n");
        aIG.addStatusString("Copying Pwnbrew Maltego Plugin...\n");
        copyMaltegoPwnbrewFiles();
        
        if( INSTALL_DIR != null )
            aIG.addStatusString("Exporting SSL Public Key to " + INSTALL_DIR.getAbsolutePath() + "...\n");
        
        //Export the SSL Cert to a file specified by the user
        File aFile = new File(MALTEGO_INSTALL_DIR, MALTEGO_STUB );
        String[] cmdArgs = new String[]{"java", "-cp", aFile.getAbsolutePath(), "pwnbrew.utilities.SSLUtilities", "-install", INSTALL_DIR.getAbsolutePath() };
        executeCmd(cmdArgs, aFile.getParentFile(), true);
        
        aIG.addStatusString("Attemping to run Maltego for Pwnbrew import...\n");
        
        //Attempt to run maltego so the entities can be imported
        String installDir = "<NOT DEFINED>";
        if( INSTALL_DIR != null ){
            aFile = new File(INSTALL_DIR, IMPORT_FILE );
            installDir = aFile.getAbsolutePath();
        } 
        String msg = "To Import Pwnbrew Entities & Transforms,\n\n"
                + "Click Maltego Logo in the top left corner of the Maltego application.\n"
                + "Select Import->Import Configuration.\n"
                + "Navigate to \"" + installDir + "\"\n"
                + "Click \"Next\",\"Next\",\"Finish\"\n"
                + "Close Maltego to conclude installation.\n\n";
        
        //Get the parent frame location
        aIG.setInstructionsText(msg);
        aIG.setButtonText("Done Importing");
//        Point parentLocation = aIG.getLocation();
//        
//        JOptionPane aPane = new JOptionPane(msg , JOptionPane.INFORMATION_MESSAGE);
//        aPane.setOptions(new Object[]{"Finished Importing"});
//        JDialog aDialog = aPane.createDialog(aIG, "Import Pwnbrew Entities & Transforms");
//        aDialog.setLocation( parentLocation.x + 500, parentLocation.y);
//        aDialog.setModal(false);
//        aDialog.setAlwaysOnTop(true);
//        aDialog.setVisible(true); 
        
        
        File maltegoExe = null;
        if( MALTEGO_INSTALL_DIR != null ){
            if( isWindows() )
                maltegoExe = new File(MALTEGO_INSTALL_DIR.getParentFile(), "bin" + FILE_SEPARATOR + MALTEGO + ".exe" );
            else if ( isUnix() )   
                maltegoExe = new File(MALTEGO_INSTALL_DIR.getParentFile(), MALTEGO );
        }
        
        if( maltegoExe != null ){
            cmdArgs = new String[]{ maltegoExe.getAbsolutePath() };
            executeCmd(cmdArgs, maltegoExe.getParentFile(), false);
        }
        
        //aIG.addStatusString("Installation complete.\n");
        //aIG.installationComplete();
        
    }
    
      //===========================================================================
    /**
     * 
     * @throws java.io.IOException
     */
    public static void copyPwnbrewFiles() throws IOException{ 
        
        JFileChooser theFileChooser = new JFileChooser();
        theFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        theFileChooser.setMultiSelectionEnabled( false );
        
        int returnVal = theFileChooser.showDialog( null, "Select Pwnbrew Install Directory" ); //Show the dialog
        if( returnVal != JFileChooser.APPROVE_OPTION )
            System.exit(0);

        //Copy client
        INSTALL_DIR = theFileChooser.getSelectedFile(); 
                
        //Copy server
        File destFile = new File( INSTALL_DIR, SERVER_JAR);
        writeJarElementToDisk(destFile, SERVER_JAR); 
        
        //Copy license file
        destFile = new File( INSTALL_DIR, LICENSE_FILE);
        writeJarElementToDisk(destFile, LICENSE_FILE); 
        
        //Copy readme
        destFile = new File( INSTALL_DIR, README_FILE);
        writeJarElementToDisk(destFile, README_FILE); 
              
    }
    
    //===========================================================================
    /**
     * 
     * @throws java.io.IOException
     */
    public static void copyMaltegoPwnbrewFiles() throws IOException{ 
        
        JFileChooser theFileChooser = new JFileChooser();
        theFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        theFileChooser.setMultiSelectionEnabled( false ); //Let the user select multiple files
        
        if( isWindows()){
            
//            File aDir = new File("C:\\Program Files (x86)");
//            if( !aDir.exists()){
//                aDir = new File("C:\\Program Files");
//            }
//            
//            //Check for maltego directory
            File malDir = null;
//            File malDir = new File(aDir, "Paterva");
//            if( !malDir.exists()){
            int returnVal = theFileChooser.showDialog( null, "Select Maltego Installation Directory, ex. Paterva\\Maltego\\<version>" ); //Show the dialog
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

//            } else {
//                
//                //List the files
//                File[] aFileArr = malDir.listFiles();
//                if( aFileArr.length > 0 ){
//                    File installDir = aFileArr[0];
//                    if( installDir.isDirectory()){
//                        File[] anotherFileArr = installDir.listFiles();
//                        if( anotherFileArr.length > 0 ){                            
//                            //Get the install dir
//                            installDir = anotherFileArr[anotherFileArr.length - 1];
//                            File testFile = new File( installDir,  "uninstall.exe");
//                            if( testFile.exists())
//                                malDir = installDir;
//                            else
//                                malDir = null;
//                        }
//                    }
//                                      
//                } else
//                    malDir = null;
//                
//            }
//            
            //Move the JAR to the install dir
            if( malDir !=null && malDir.exists()){
                
                MALTEGO_INSTALL_DIR = new File( malDir, "pwnbrew");
                //Make the underlying dirs
                MALTEGO_INSTALL_DIR.mkdirs();
                
                //Create the maltego import file
                File destFile = new File( INSTALL_DIR, IMPORT_FILE);
                writeJarElementToDisk(destFile, IMPORT_FILE);
                
                //Create the maltego jar
                destFile = new File( MALTEGO_INSTALL_DIR, MALTEGO_STUB);
                writeJarElementToDisk(destFile, MALTEGO_STUB);
                
                //Create the ssl bat
                destFile = new File( MALTEGO_INSTALL_DIR, SSL_BAT);
                writeJarElementToDisk(destFile, SSL_BAT);
                
            } else {
                JOptionPane.showMessageDialog(null, "No Maltego directory defined. Exiting.", "Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
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
                
                MALTEGO_INSTALL_DIR = new File( aDir, "bin" + FILE_SEPARATOR + "pwnbrew");
                //Make the underlying dirs
                MALTEGO_INSTALL_DIR.mkdirs();
                 
                //Create the maltego import file
                File destFile = new File( INSTALL_DIR, IMPORT_FILE);
                writeJarElementToDisk(destFile, IMPORT_FILE);   
                
                //Create the maltego jar
                destFile = new File( MALTEGO_INSTALL_DIR, MALTEGO_STUB);
                writeJarElementToDisk(destFile, MALTEGO_STUB); 
                                               
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
    
    // ==========================================================================
    /**
    * Writes the jar element to disk
    *
    * @param passedJarElementName
     * @return 
     * @throws java.io.IOException
    */
    public static byte[] getJarElementBytes( String passedJarElementName ) throws IOException {

        byte[] aByteArr = null;
        int bytesRead = 0;
        if( passedJarElementName!=null ){
                
            InputStream theIS = Installer.class.getClassLoader().getResourceAsStream(passedJarElementName);
            if( theIS != null ){

                try {

                    byte[] byteBuffer = new byte[ 4096 ];
                    //Open the zip
                    try (ByteArrayOutputStream aBOS = new ByteArrayOutputStream()) {

                        //Read to the end
                        while( bytesRead != -1){
                            bytesRead = theIS.read(byteBuffer);
                            if(bytesRead != -1)
                                aBOS.write(byteBuffer, 0, bytesRead);                                   
                        }

                        aBOS.flush();
                        aByteArr = aBOS.toByteArray();

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
        
        return aByteArr;

    }

    //=====================================================================
    /**
     * 
     */
    private static void executeCmd( String[] cmdArgs, File workingDir, boolean waitFor ) {

        Process theProcess;
        try {

            if( MALTEGO_INSTALL_DIR != null ){
                
                ProcessBuilder theProcessBuilder = new ProcessBuilder( cmdArgs );
                theProcessBuilder.directory(workingDir);
                
                try {
                    theProcess = theProcessBuilder.start(); //Start a new process
                } catch( IOException ex ) {                
                    return;
                }

                OutputStream theirStdin = theProcess.getOutputStream();
                try {
                    theirStdin.close();
                } catch ( IOException ioe ){
                    ioe = null;
                }

                //Wait for the process to complete...
                if( waitFor ){
                    int exitValue = Integer.MIN_VALUE;
                    while( exitValue == Integer.MIN_VALUE ) { //Until the exit value is obtained...

                        try {
                            exitValue = theProcess.waitFor();
                        } catch( InterruptedException ex ) {
                            //Do nothing / Continue to wait for the process to exit
                            ex = null;
                        }

                    }
                }
            }
        } finally {
            //Reset for the next execution...
            theProcess = null;
        }
    
    }
    
    
}
