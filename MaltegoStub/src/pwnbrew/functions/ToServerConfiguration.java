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
package pwnbrew.functions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JFileChooser;
import pwnbrew.MaltegoStub;
import pwnbrew.StubConfig;
import pwnbrew.log.LoggableException;
import pwnbrew.manager.DataManager;
import pwnbrew.misc.Constants;
import pwnbrew.misc.DebugPrinter;
import pwnbrew.misc.Utilities;
import pwnbrew.network.ClientPortRouter;
import pwnbrew.network.control.messages.AddToJarLibrary;
import pwnbrew.network.control.messages.ControlMessage;
import pwnbrew.network.control.messages.DeleteJarItem;
import pwnbrew.network.control.messages.GetJarItemFile;
import pwnbrew.network.control.messages.GetJarItems;
import pwnbrew.network.control.messages.GetNetworkSettings;
import pwnbrew.network.control.messages.ImportCert;
import pwnbrew.network.control.messages.NetworkSettingsMsg;
import pwnbrew.network.control.messages.PushFile;
import pwnbrew.options.OptionsJFrame;
import pwnbrew.options.OptionsJFrameListener;
import pwnbrew.options.panels.JarLibraryPanel;
import pwnbrew.options.panels.NetworkOptionsPanel;
import pwnbrew.utilities.SocketUtilities;
import pwnbrew.xml.maltego.MaltegoTransformExceptionMessage;

/**
 *
 * @author Securifera
 */
public class ToServerConfiguration extends Function implements OptionsJFrameListener {
    
    private static final String NAME_Class = ToServerConfiguration.class.getSimpleName();
    
    private volatile boolean notified = false;
    private OptionsJFrame optionsGui = null;
    private String theCertSerial = "";
    
    //Map for temp strings
    private final Map<Integer, String[]> taskIdToStringMap = new HashMap<>();
      
    //==================================================================
    /**
     * Constructor
     * @param passedManager
     */
    public ToServerConfiguration( MaltegoStub passedManager ) {
        super(passedManager);
    }      
    
    //===================================================================
    /**
     * 
     * @param passedObjectStr 
     */
    @Override
    public void run(String passedObjectStr) {
        
        String retStr = "";
        Map<String, String> objectMap = getKeyValueMap(passedObjectStr); 
         
        //Get server IP
        String serverIp = objectMap.get( Constants.SERVER_IP);
        if( serverIp == null ){
            DebugPrinter.printMessage( NAME_Class, "ToServerConfiguration", "No pwnbrew server IP provided", null);
            return;
        }
         
        //Get server port
        String serverPortStr = objectMap.get( Constants.SERVER_PORT);
        if( serverPortStr == null ){
            DebugPrinter.printMessage( NAME_Class, "ToServerConfiguration", "No pwnbrew server port provided", null);
            return;
        }
        
        //Get host id
        String hostIdStr = objectMap.get( Constants.HOST_ID);
        if( hostIdStr == null ){
            DebugPrinter.printMessage( NAME_Class, "ToServerConfiguration", "No host id provided", null);
            return;
        }
         
        StubConfig theConfig = StubConfig.getConfig();
        theConfig.setServerIp(serverIp);
        theConfig.setSocketPort(serverPortStr);
        Integer anInteger = SocketUtilities.getNextId();
        theConfig.setHostId(anInteger.toString());
        int serverPort = Integer.parseInt( serverPortStr);
        ClientPortRouter aPR = (ClientPortRouter) theManager.getPortRouter( serverPort );
        if(aPR == null){
            try {
                aPR = (ClientPortRouter)DataManager.createPortRouter(theManager, serverPort, true);
            } catch (IOException ex) {
                DebugPrinter.printMessage( NAME_Class, "to_server_config", "Unable to create port router.", ex);
                return;
            }
        }  
        
        theManager.initialize();
        try {
            
            aPR.ensureConnectivity( serverPort, theManager );
            
            String passedName = objectMap.get( Constants.NAME);
            optionsGui = new OptionsJFrame( passedName, this );
            
            //Get the jar items
            getJarItems();           
            
            //Get the network settings
            getNetworkSettings();
            
            //Show the gui
            optionsGui.setVisible(true);
            
            //Wait to be notified
            waitToBeNotified();
            
        } catch( LoggableException ex ) {
            
            //Create a relay object
            pwnbrew.xml.maltego.Exception exMsg = new pwnbrew.xml.maltego.Exception( ex.getMessage() );
            MaltegoTransformExceptionMessage malMsg = theReturnMsg.getExceptionMessage();
            
            //Create the message list
            malMsg.getExceptionMessages().addExceptionMessage(exMsg);
        }
        
    }
    
    // ==========================================================================
    /**
    * Causes the calling {@link Thread} to <tt>wait()</tt> until notified by
    * another.
    * <p>
    * <strong>This method most certainly "blocks".</strong>
     * @param anInt
    */
    protected synchronized void waitToBeNotified( Integer... anInt ) {

        while( !notified ) {

            try {
                
                //Add a timeout if necessary
                if( anInt.length > 0 ){
                    
                    wait( anInt[0]);
                    break;
                    
                } else {
                    wait(); //Wait here until notified
                }
                
            } catch( InterruptedException ex ) {
            }

        }
        notified = false;
    }
    
    //===============================================================
    /**
     * Notifies the thread
    */
    @Override
    public synchronized void beNotified() {
        notified = true;
        notifyAll();
    }

    //========================================================================
    /**
     * 
     * @param theJarName
     * @param theJarType
     * @param theJvmVersion
     * @param theJarVersion 
     */
    public void addJarItem(String theJarName, String theJarType, String theJvmVersion, String theJarVersion) {
        JarLibraryPanel thePanel = optionsGui.getJarLibraryPanel();
        thePanel.addJarItem( theJarName, theJarType, theJvmVersion, theJarVersion );       
    }

    //========================================================================
    /**
     * 
     */
    @Override
    public void getJarItems() {
              
        //Send the msg
        GetJarItems aMsg = new GetJarItems(Constants.SERVER_ID );
        DataManager.send( theManager, aMsg);
        
    }
    
    //========================================================================
    /**
     * 
     */
    public void getNetworkSettings() {
               
        //Send the msg
        GetNetworkSettings aMsg = new GetNetworkSettings(Constants.SERVER_ID );
        DataManager.send( theManager, aMsg);
        
    }

    //========================================================================
    /**
     * 
     * @param jarName
     * @param jarType
     * @param jvmVersion
     * @param jarVersion 
     */
    public void deleteJarItemFromTable(String jarName, String jarType, String jvmVersion, String jarVersion) {
        JarLibraryPanel thePanel = optionsGui.getJarLibraryPanel();
        thePanel.deleteJarItemFromTable(jarName, jarType, jvmVersion, jarVersion);
    }
    
    //========================================================================
    /**
     * 
     * @param jarName
     * @param jarType
     * @param jvmVersion
     * @param jarVersion 
     */
    @Override
    public void sendDeleteJarItemMsg(String jarName, String jarType, String jvmVersion, String jarVersion) {
        try {
                        
            //Send the msg
            DeleteJarItem aMsg = new DeleteJarItem(Constants.SERVER_ID, jarName, jarType, jvmVersion, jarVersion );
            DataManager.send( theManager, aMsg);
            
        } catch (IOException ex) {
            DebugPrinter.printMessage( NAME_Class, "deleteJarItem", ex.getMessage(), ex );
        }
    }
    
    //========================================================================
    /**
     * 
     * @param serverPort
     * @param issueeDN
     * @param issuerDN
     * @param days 
     */
    @Override
    public void sendCertInfo(int serverPort, String issueeDN, String issuerDN, int days) {
        
        try {    
            
            //Send the msg
            NetworkSettingsMsg aMsg = new NetworkSettingsMsg( Constants.SERVER_ID, serverPort, issueeDN, issuerDN, "", Integer.toString(days) );
            DataManager.send( theManager, aMsg);
            
        } catch (IOException ex) {
            DebugPrinter.printMessage( NAME_Class, "deleteJarItem", ex.getMessage(), ex );
        }
    }
    

    //========================================================================
    /**
     * 
     * @param userSelectedFile
     * @param selVal 
     */
    @Override
    public void sendJarFile(File userSelectedFile, String selVal) {
        
        try {
               
            int taskId = SocketUtilities.getNextId();
            
            //Add to the map
            synchronized(taskIdToStringMap){
                taskIdToStringMap.put(taskId, new String[]{selVal});
            }

            //Queue the file to be sent            
            String fileHashNameStr = new StringBuilder().append("0").append(":").append(userSelectedFile.getAbsolutePath()).toString();
            PushFile thePFM = new PushFile( taskId, fileHashNameStr, userSelectedFile.length(), PushFile.JAR_UPLOAD, Constants.SERVER_ID );

            //Send the message
            DataManager.send( theManager, thePFM );  
            
        } catch (IOException ex) {
            DebugPrinter.printMessage( NAME_Class, "sendJarFile", ex.getMessage(), ex );
        }
    }

    //========================================================================
    /**
     * 
     * @param hashFilenameStr
     * @param taskId 
     */
    public void fileSent(String hashFilenameStr, int taskId) {
        //Get the type
        String[] tempStrArr;
        synchronized( taskIdToStringMap){
            tempStrArr = taskIdToStringMap.remove(taskId);
        }
        if( tempStrArr != null && tempStrArr.length > 0 ){
            
            try {
            
                String tempStr = tempStrArr[0];

                //Queue the file to be sent
                ControlMessage aMsg = null;
                if( hashFilenameStr.endsWith("jar")){
                    aMsg = new AddToJarLibrary(Constants.SERVER_ID, hashFilenameStr, tempStr, "", "");
                } else if( hashFilenameStr.endsWith("p12") || hashFilenameStr.endsWith("pfx")){
                    aMsg = new ImportCert(Constants.SERVER_ID, hashFilenameStr, tempStr );
                }
                
                //Send the message
                if( aMsg != null )
                    DataManager.send( theManager, aMsg );
                            
            } catch (IOException ex) {
                DebugPrinter.printMessage( NAME_Class, "jarFileSent", ex.getMessage(), ex );
            }
        }
    }

    //========================================================================
    /**
     * 
     * @param theServerPort
     * @param theIssueeName
     * @param theIssuerName
     * @param theExpDate
     * @param theAlgorithm 
     * @param theSerial 
     */
    public void setNetworkSettings(int theServerPort, String theIssueeName, String theIssuerName, String theExpDate, String theAlgorithm, String theSerial) {
        NetworkOptionsPanel thePanel = optionsGui.getNetworkSettingsPanel();
        thePanel.setNetworkSettings(theServerPort, theIssueeName, theIssuerName, theAlgorithm, theExpDate );
        theCertSerial = theSerial;
    }

    //========================================================================
    /**
     * 
     * @param userSelectedFile
     * @param string 
     */
    @Override
    public void sendCertFile( File userSelectedFile, String string ) {
        
        try {
                  
            
            int taskId = SocketUtilities.getNextId();
            
            //Add to the map
            synchronized( taskIdToStringMap){
                taskIdToStringMap.put(taskId, new String[]{string});
            }

            //Queue the file to be sent            
            String fileHashNameStr = new StringBuilder().append("0").append(":").append(userSelectedFile.getAbsolutePath()).toString();
            PushFile thePFM = new PushFile( taskId, fileHashNameStr, userSelectedFile.length(), PushFile.JAR_UPLOAD, Constants.SERVER_ID );

            //Send the message
            DataManager.send( theManager, thePFM );
            
        } catch (IOException ex) {
            DebugPrinter.printMessage( NAME_Class, "sendJarFile", ex.getMessage(), ex );
        }
    }

    //========================================================================
    /**
     * 
     * @param connectStr 
     * @param passedName 
     * @param passedType 
     * @param passedJvmVersion 
     * @param passedJarVersion 
     * @param hostHeader 
     */
    @Override
    public void getStagerFile(String connectStr, String passedName, String passedType, 
            String passedJvmVersion, String passedJarVersion, String hostHeader ) {
        try {
                              
            int taskId = SocketUtilities.getNextId();
            String[] stagerArr;
            if( hostHeader != null){
                stagerArr = new String[]{connectStr, hostHeader};
            } else {
                stagerArr = new String[]{connectStr};  
            }
            
            //Add to the map
            synchronized( taskIdToStringMap){
                taskIdToStringMap.put(taskId, stagerArr);
            }

            //Queue the file to be sent            
            GetJarItemFile getStagerMsg = new GetJarItemFile( taskId, Constants.SERVER_ID, passedName, passedType, passedJvmVersion, passedJarVersion );

            //Send the message
            DataManager.send( theManager, getStagerMsg );
            
        } catch (IOException ex) {
            DebugPrinter.printMessage( NAME_Class, "sendJarFile", ex.getMessage(), ex );
        }
    }
    
    //===============================================================
    /**
     * 
     * @param taskId
     * @param fileLoc 
     */
    @Override
    public void fileReceived(int taskId, File fileLoc) { 
        //Add to the map
        String[] stagerArr;
        synchronized( taskIdToStringMap){
            stagerArr = taskIdToStringMap.remove(taskId );
        }
        
        //If it exists then update the value
        if( stagerArr != null && stagerArr.length > 0 ){
            
            String connectStr = stagerArr[0];
            if( fileLoc.exists() ){   

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
                
                //Add the properties that need to be updated
                Map<String, String> propMap = new HashMap<>();
                propMap.put(Constants.STAGER_URL, encodedStr.trim());
                propMap.put(Constants.CERT_SERIAL_LABEL, theCertSerial); //Replace with actual cert

                //Add the host header
                if( stagerArr.length > 1){
                    String hostHeader = stagerArr[1];
                    encodedStr = anEncoder.encodeBuffer(hostHeader.getBytes());
                    if(encodedStr.contains("=")){
                        hostHeader += " ";
                        encodedStr = anEncoder.encodeBuffer(hostHeader.getBytes());
                        if(encodedStr.contains("=")){
                            hostHeader += " ";
                            encodedStr = anEncoder.encodeBuffer(hostHeader.getBytes());
                        }
                    }
                    propMap.put(Constants.HOST_HEADER, encodedStr.trim());
                }
                
                
                //Update the jar
                Utilities.updateJarProperties(fileLoc, Constants.MANIFEST_FILE, propMap); 

                //Open a filechooser and save it to disk
                File aFile = new File("Stager.jar");
                File saveFile = getFilePath( aFile );
                if( saveFile != null ){
                    try {
                        Files.move(fileLoc.toPath(), saveFile.toPath(), StandardCopyOption.REPLACE_EXISTING);                
                    } catch (IOException ex) {
                        DebugPrinter.printMessage( NAME_Class, "fileReceived", ex.getMessage(), ex );
                    }
                }
            }
        }
    }
    
     // ==========================================================================
    /**
    * Selects the keystore path via a {@link JFileChooser}.
    */
    private File getFilePath( File initialFile ) {

        JFileChooser theCertificateChooser = new JFileChooser();
        theCertificateChooser.setMultiSelectionEnabled(false);
        
        File userSelectedFile = null;
        theCertificateChooser.setSelectedFile( initialFile );
        int returnVal = theCertificateChooser.showSaveDialog(null); //Show the dialogue
        switch( returnVal ) {

           case JFileChooser.CANCEL_OPTION: //If the user canceled the selecting...
              return null;
           case JFileChooser.ERROR_OPTION: //If the dialogue was dismissed or an error occurred...
              return null; //Do nothing

           case JFileChooser.APPROVE_OPTION: //If the user approved the selection...
              userSelectedFile = theCertificateChooser.getSelectedFile(); //Get the files the user selected
              break;
           default:
              return null;

        }

        //Check if the returned file is valid
        if(userSelectedFile == null  || userSelectedFile.isDirectory())
            return null;

        return userSelectedFile;

    }

}
