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
import java.util.HashMap;
import java.util.Map;
import pwnbrew.MaltegoStub;
import pwnbrew.StubConfig;
import pwnbrew.misc.Constants;
import pwnbrew.misc.DebugPrinter;
import pwnbrew.misc.SocketUtilities;
import pwnbrew.network.ClientPortRouter;
import pwnbrew.network.control.ControlMessageManager;
import pwnbrew.network.control.messages.AddToJarLibrary;
import pwnbrew.network.control.messages.ControlMessage;
import pwnbrew.network.control.messages.DeleteJarItem;
import pwnbrew.network.control.messages.GetJarItems;
import pwnbrew.network.control.messages.GetNetworkSettings;
import pwnbrew.network.control.messages.ImportCert;
import pwnbrew.network.control.messages.NetworkSettingsMsg;
import pwnbrew.network.control.messages.PushFile;
import pwnbrew.options.OptionsJFrame;
import pwnbrew.options.OptionsJFrameListener;
import pwnbrew.options.panels.JarLibraryPanel;
import pwnbrew.options.panels.NetworkOptionsPanel;
import pwnbrew.xml.maltego.MaltegoMessage;

/**
 *
 * @author Securifera
 */
public class ToOptions extends Function implements OptionsJFrameListener {
    
    private static final String NAME_Class = ToOptions.class.getSimpleName();
    
    private volatile boolean notified = false;
    private OptionsJFrame optionsGui = null;
    
    //Map for temp strings
    private final Map<Integer, String> taskIdToStringMap = new HashMap<>();
    
    //Create the return msg
    private final MaltegoMessage theReturnMsg = new MaltegoMessage();
  
    //==================================================================
    /**
     * Constructor
     * @param passedManager
     */
    public ToOptions( MaltegoStub passedManager ) {
        super(passedManager);
    }      
    
    //===================================================================
    /**
     * 
     * @param passedObjectStr
     * @return 
     */
    @Override
    public String run(String passedObjectStr) {
        
        String retStr = "";
        Map<String, String> objectMap = getKeyValueMap(passedObjectStr); 
         
        //Get server IP
        String serverIp = objectMap.get( Constants.SERVER_IP);
        if( serverIp == null ){
            DebugPrinter.printMessage( NAME_Class, "ToOptions", "No pwnbrew server IP provided", null);
            return retStr;
        }
         
        //Get server port
        String serverPortStr = objectMap.get( Constants.SERVER_PORT);
        if( serverPortStr == null ){
            DebugPrinter.printMessage( NAME_Class, "ToOptions", "No pwnbrew server port provided", null);
            return retStr;
        }
        
        //Get host id
        String hostIdStr = objectMap.get( Constants.HOST_ID);
        if( hostIdStr == null ){
            DebugPrinter.printMessage( NAME_Class, "ToOptions", "No host id provided", null);
            return retStr;
        }
         
        //Create the connection
        try {
            
            //Set the server ip and port
            StubConfig theConfig = StubConfig.getConfig();
            theConfig.setServerIp(serverIp);
            theConfig.setSocketPort(serverPortStr);
            
            //Set the client id
            Integer anInteger = SocketUtilities.getNextId();
            theConfig.setHostId(anInteger.toString());
            
            ControlMessageManager aCMManager = ControlMessageManager.getControlMessageManager();
            if( aCMManager == null ){
                aCMManager = ControlMessageManager.initialize( theManager );
            }

            //Get the port router
            int serverPort = Integer.parseInt( serverPortStr);
            ClientPortRouter aPR = (ClientPortRouter) theManager.getPortRouter( serverPort );

            //Initiate the file transfer
            if(aPR == null){
                DebugPrinter.printMessage( NAME_Class, "ToOptions", "Unable to retrieve port router.", null);
                return retStr;     
            }           
            
            //Set up the port wrapper
            theManager.initialize();
            
            //Connect to server
            boolean connected = aPR.ensureConnectivity( serverPort, theManager );
            if( connected ){
                
                optionsGui = new OptionsJFrame( this );
                
                //Get the jar items
                getJarItems();
                
                //Get the network settings
                getNetworkSettings();
                
                //Show the gui
                optionsGui.setVisible(true);
                
                //Wait to be notified
                waitToBeNotified();
                
            } else {
                StringBuilder aSB = new StringBuilder()
                        .append("Unable to connect to the Pwnbrew server at \"")
                        .append(serverIp).append(":").append(serverPort).append("\"");
                DebugPrinter.printMessage( NAME_Class, "listclients", aSB.toString(), null);
            }
            
        } catch (IOException ex) {
            DebugPrinter.printMessage( NAME_Class, "listclients", ex.getMessage(), ex );
        }
        
        //Create the return message
        retStr = theReturnMsg.getXml();
              
        return retStr;
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
        
        try {
            
            ControlMessageManager aCMManager = ControlMessageManager.getControlMessageManager();
            if( aCMManager == null ){
                aCMManager = ControlMessageManager.initialize( theManager );
            }
            
            //Send the msg
            GetJarItems aMsg = new GetJarItems(Constants.SERVER_ID );
            aCMManager.send(aMsg);
            
        } catch (IOException ex) {
            DebugPrinter.printMessage( NAME_Class, "getJarItems", ex.getMessage(), ex );
        }
        
    }
    
    //========================================================================
    /**
     * 
     */
    public void getNetworkSettings() {
        
        try {
            
            ControlMessageManager aCMManager = ControlMessageManager.getControlMessageManager();
            if( aCMManager == null ){
                aCMManager = ControlMessageManager.initialize( theManager );
            }
            
            //Send the msg
            GetNetworkSettings aMsg = new GetNetworkSettings(Constants.SERVER_ID );
            aCMManager.send(aMsg);
            
        } catch (IOException ex) {
            DebugPrinter.printMessage( NAME_Class, "getJarItems", ex.getMessage(), ex );
        }
        
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
            
            ControlMessageManager aCMManager = ControlMessageManager.getControlMessageManager();
            if( aCMManager == null ){
                aCMManager = ControlMessageManager.initialize( theManager );
            }
            
            //Send the msg
            DeleteJarItem aMsg = new DeleteJarItem(Constants.SERVER_ID, jarName, jarType, jvmVersion, jarVersion );
            aCMManager.send(aMsg);
            
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
            
            ControlMessageManager aCMManager = ControlMessageManager.getControlMessageManager();
            if( aCMManager == null )
                aCMManager = ControlMessageManager.initialize( theManager );            
            
            //Send the msg
            NetworkSettingsMsg aMsg = new NetworkSettingsMsg( Constants.SERVER_ID, serverPort, issueeDN, issuerDN, "", Integer.toString(days));
            aCMManager.send(aMsg);
            
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
            
            ControlMessageManager aCMManager = ControlMessageManager.getControlMessageManager();
            if( aCMManager == null )
                aCMManager = ControlMessageManager.initialize( theManager );            
            
            int taskId = SocketUtilities.getNextId();
            
            //Add to the map
            taskIdToStringMap.put(taskId, selVal);

            //Queue the file to be sent
            
            String fileHashNameStr = new StringBuilder().append("0").append(":").append(userSelectedFile.getAbsolutePath()).toString();
            PushFile thePFM = new PushFile( taskId, fileHashNameStr, userSelectedFile.length(), PushFile.JAR_UPLOAD, Constants.SERVER_ID );

            //Send the message
            aCMManager.send( thePFM );  
            
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
        String tempStr = taskIdToStringMap.remove(taskId);
        if( tempStr != null ){
            
            try {
            
                ControlMessageManager aCMManager = ControlMessageManager.getControlMessageManager();
                if( aCMManager == null )
                    aCMManager = ControlMessageManager.initialize( theManager );            

                //Queue the file to be sent
                ControlMessage aMsg = null;
                if( hashFilenameStr.endsWith("jar")){
                    aMsg = new AddToJarLibrary(Constants.SERVER_ID, hashFilenameStr, tempStr, "", "");
                } else if( hashFilenameStr.endsWith("p12")){
                    aMsg = new ImportCert(Constants.SERVER_ID, hashFilenameStr, tempStr );
                }
                
                //Send the message
                if( aMsg != null )
                    aCMManager.send( aMsg );
                            
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
     */
    public void setNetworkSettings(int theServerPort, String theIssueeName, String theIssuerName, String theExpDate, String theAlgorithm) {
        NetworkOptionsPanel thePanel = optionsGui.getNetworkSettingsPanel();
        thePanel.setNetworkSettings(theServerPort, theIssueeName, theIssuerName, theAlgorithm, theExpDate );
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
            
            ControlMessageManager aCMManager = ControlMessageManager.getControlMessageManager();
            if( aCMManager == null )
                aCMManager = ControlMessageManager.initialize( theManager );            
            
            int taskId = SocketUtilities.getNextId();
            
            //Add to the map
            taskIdToStringMap.put(taskId, string);

            //Queue the file to be sent            
            String fileHashNameStr = new StringBuilder().append("0").append(":").append(userSelectedFile.getAbsolutePath()).toString();
            PushFile thePFM = new PushFile( taskId, fileHashNameStr, userSelectedFile.length(), PushFile.JAR_UPLOAD, Constants.SERVER_ID );

            //Send the message
            aCMManager.send( thePFM );  
            
        } catch (IOException ex) {
            DebugPrinter.printMessage( NAME_Class, "sendJarFile", ex.getMessage(), ex );
        }
    }

}
