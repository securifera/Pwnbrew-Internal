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

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import pwnbrew.MaltegoStub;
import pwnbrew.StubConfig;
import pwnbrew.log.LoggableException;
import pwnbrew.manager.PortManager;
import pwnbrew.misc.Constants;
import pwnbrew.misc.DebugPrinter;
import pwnbrew.utilities.SocketUtilities;
import pwnbrew.misc.Utilities;
import pwnbrew.network.ClientPortRouter;
import pwnbrew.network.control.ControlMessageManager;
import pwnbrew.network.control.messages.RemoteException;
import pwnbrew.shell.Bash;
import pwnbrew.shell.BashExp;
import pwnbrew.shell.CommandPrompt;
import pwnbrew.shell.Custom;
import pwnbrew.shell.Powershell;
import pwnbrew.shell.Shell;
import pwnbrew.shell.ShellJFrame;
import pwnbrew.shell.ShellJPanel;
import pwnbrew.shell.ShellJPanelListener;
import pwnbrew.shell.ShellListener;
import pwnbrew.shell.ShellSettings;
import pwnbrew.xml.maltego.MaltegoTransformExceptionMessage;

/**
 *
 * @author Securifera
 */
public class ToShell extends Function implements ShellJPanelListener, ShellListener {
    
    private static final String NAME_Class = ToShell.class.getSimpleName();
    
    private int theHostId = 0;   
    private String theOS;
    private String theHostName;
    
    private ShellJFrame parentFrame;
    private Shell theShell = null;
    private final ShellSettings theShellSettings = new ShellSettings();
      
    //==================================================================
    /**
     * Constructor
     * @param passedManager
     */
    public ToShell( MaltegoStub passedManager ) {
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
            DebugPrinter.printMessage( NAME_Class, "ToShell", "No pwnbrew server IP provided", null);
            return;
        }
         
        //Get server port
        String serverPortStr = objectMap.get( Constants.SERVER_PORT);
        if( serverPortStr == null ){
            DebugPrinter.printMessage( NAME_Class, "ToShell", "No pwnbrew server port provided", null);
            return;
        }
        
        //Get host id
        String hostIdStr = objectMap.get( Constants.HOST_ID);
        if( hostIdStr == null ){
            DebugPrinter.printMessage( NAME_Class, "ToShell", "No host id provided", null);
            return;
        }
        
        //Get host id
        String tempOs = objectMap.get( Constants.HOST_OS);
        if( tempOs == null ){
            DebugPrinter.printMessage( NAME_Class, "ToShell", "No host id provided", null);
            return;
        }
        
        //Get host id
        String tempName = objectMap.get( Constants.HOST_NAME);
        if( tempName == null ){
            DebugPrinter.printMessage( NAME_Class, "ToShell", "No host id provided", null);
            return;
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
                DebugPrinter.printMessage( NAME_Class, "ToShell", "Unable to retrieve port router.", null);
                return;     
            }           
            
            //Set look and feel            
            String lookAndFeelClassStr = "javax.swing.plaf.nimbus.NimbusLookAndFeel";        
            try{
                UIManager.setLookAndFeel( lookAndFeelClassStr );
            } catch ( ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            }
            
            JDialog.setDefaultLookAndFeelDecorated(true);
            
            //Connect to server
            try {
                
                aPR.ensureConnectivity( serverPort, theManager );
                             
                //Set the host id
                theHostId = Integer.parseInt( hostIdStr);
                theOS = tempOs;
                theHostName = tempName;                
                
                //Create and add the shell panel
                parentFrame = new ShellJFrame( this );
                parentFrame.setTitle(theHostName);
                parentFrame.setVisible(true);
                
                //Wait to be notified
                waitToBeNotified();
                
                //Send shell shutdown message
                killShell();
                
                //Sleep a couple seconds to make sure the message was sent
                Thread.sleep(2000);
                
                retStr = theReturnMsg.getXml();
                
            } catch( LoggableException ex ) {
                
                //Create a relay object
                pwnbrew.xml.maltego.Exception exMsg = new pwnbrew.xml.maltego.Exception( ex.getMessage() );
                MaltegoTransformExceptionMessage malMsg = theReturnMsg.getExceptionMessage();

                //Create the message list
                malMsg.getExceptionMessages().addExceptionMessage(exMsg);  
            }
            
        } catch (IOException | InterruptedException ex) {
            DebugPrinter.printMessage( NAME_Class, "ToShell", ex.getMessage(), ex );
        }
        
    }
    
    //========================================================================
    /**
     * 
     * @return 
     */
    @Override
    public Component getParentComponent(){
        //return theShellPanel;
        return parentFrame.getShellPanel();
    }

    // ==========================================================================
    /**
     *  Sends the input string to the shell
     * 
     * @param theStr 
     */
    @Override
    public void sendInput(String theStr) {            
        if( theShell != null ){
            theShell.sendInput(theStr);
        }
    }

    //==========================================================================
    /**
     *  Get the list of available shells
     * 
     * @return 
     */
    @Override
    public List<Class> getShellList(){
        
        List<Class> theShellList = new ArrayList<>();        
        if( Utilities.isWindows( theOS) ){
            //Add the cmd shell
            theShellList.add( CommandPrompt.class );

            //Add powershell
            theShellList.add(Powershell.class);
            
            //Add custom
            theShellList.add(Custom.class );
            
        } else if( Utilities.isUnix(theOS)){
            
            //Add bash
            theShellList.add(Bash.class);   
            
            //Add bash
            theShellList.add(BashExp.class);   
        }
        
        return theShellList;
    }
    
    //==========================================================================
    /**
     *  Kill the shell
     */
    @Override
    public void killShell(){        
            
        if( theShell != null ){
                //&& theShell.isRunning() ){
            theShell.shutdown();
            theShell = null;
        }        
    }

    //========================================================================
    /**
     * 
     * @return 
     */
    @Override
    public Shell getShell() {
        return theShell;
    }

     //===============================================================
    /**
     * Returns the shell log dir.
     *
     * @return 
    */
    @Override
    public File getShellLogDir() {
        
        File dataDir = MaltegoStub.getDataDir();
        File hostDir = new File(dataDir, theHostName );
        File retDir = new File(hostDir, "shell");
        
        try {
            Utilities.ensureDirectoryExists(retDir);
        } catch (IOException ex) {
            DebugPrinter.printMessage( NAME_Class, "getShellLogDir", ex.getMessage(), ex );            
        }
        
        
        return retDir;
    }
    
    //===============================================================
    /**
     * 
     * @param aMsg 
     */
    @Override
    public void handleException(RemoteException aMsg ) {
        super.handleException(aMsg); 
        
        
        //Set the component back
        ShellJPanel aPanel = (ShellJPanel) getParentComponent();
        aPanel.disablePanel(true);
        
        //Show popup
        JOptionPane.showMessageDialog( aPanel, "Server is not connected to the Host.","Error", JOptionPane.ERROR_MESSAGE );
                
    }

    //========================================================================
    /**
     * 
     * @return 
     */
    @Override
    public String getOsName() {
        return theOS;
    }

    //=======================================================================
    /**
     *  Returns the comm manager
     * @return 
     */
    @Override
    public PortManager getCommManager() {
        return theManager;
    }

    //=======================================================================
    /**
     * 
     * @return 
     */
    @Override
    public int getHostId() {
        return theHostId;
    }

    //=======================================================================
    /**
     * 
     * @return 
     */
    @Override
    public Component getShellView() {
        return parentFrame.getShellPanel().getShellView();
    }
    
    //=======================================================================
    /**
     * 
     * @return 
     */
    @Override
    public JFrame getParentJFrame() {
        return parentFrame;
    }

    //=======================================================================
    /**
     * 
     * @param passedShell
     */
    @Override
    public void setShell(Shell passedShell) {
        theShell = passedShell;
    }

    //=======================================================================
    /**
     * 
     */
    @Override
    public void setFrameTitle(String title) {
        parentFrame.setTitle(title);
    }

    //=======================================================================
    /**
     * 
     * @return 
     */
    @Override
    public ShellSettings getShellSettings() {
        return theShellSettings;
    }

    @Override
    public String getCurrentShellDir() {
        return theShellSettings.getCurrentDir();
    }

  

}
