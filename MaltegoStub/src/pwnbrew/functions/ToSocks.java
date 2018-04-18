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
import java.awt.Image;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import pwnbrew.MaltegoStub;
import pwnbrew.StubConfig;
import pwnbrew.log.LoggableException;
import pwnbrew.manager.DataManager;
import pwnbrew.manager.PortManager;
import pwnbrew.misc.Constants;
import pwnbrew.misc.DebugPrinter;
import pwnbrew.utilities.SocketUtilities;
import pwnbrew.misc.Utilities;
import pwnbrew.network.ClientPortRouter;
import pwnbrew.network.control.ControlMessageManager;
import pwnbrew.network.control.messages.RemoteException;
import pwnbrew.socks.SocksJPanel;
import pwnbrew.socks.SocksJPanelListener;
import pwnbrew.socks.SocksMessageManager;
import pwnbrew.xml.maltego.MaltegoTransformExceptionMessage;

/**
 *
 * @author Securifera
 */
public class ToSocks extends Function implements SocksJPanelListener {
    
    private static final String NAME_Class = ToSocks.class.getSimpleName();
    
    private int theHostId = 0;   
    private String theHostName;
    
    private JFrame parentFrame;
    private SocksJPanel theSocksJPanel; 
  
    //==================================================================
    /**
     * Constructor
     * @param passedManager
     */
    public ToSocks( MaltegoStub passedManager ) {
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
        
        //Get host os
        String tempOs = objectMap.get( Constants.HOST_OS);
        if( tempOs == null ){
            DebugPrinter.printMessage( NAME_Class, "ToShell", "No host id provided", null);
            return;
        }
        
        //Get host name
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
                        
            //Create the socks manager for later
            SocksMessageManager aManager = SocksMessageManager.getSocksMessageManager();  

            //Get the port router
            int serverPort = Integer.parseInt( serverPortStr);
            ClientPortRouter aPR = (ClientPortRouter) theManager.getPortRouter( serverPort );
            if(aPR == null){
                try {
                    aPR = (ClientPortRouter)DataManager.createPortRouter(theManager, serverPort, true);
                } catch (IOException ex) {
                    DebugPrinter.printMessage( NAME_Class, "to_socks", "Unable to create port router.", ex);
                    return;
                }
            }            
            
            //Setup skin
            theManager.initialize();
            
            //Connect to server
            try {
                
                aPR.ensureConnectivity( serverPort, theManager );
                
                //Set the host id
                theHostId = Integer.parseInt( hostIdStr);
                theHostName = tempName;                
                
                //Create and add the shell panel
                theSocksJPanel = new SocksJPanel( this );
                
                parentFrame = new JFrame(){
                       // ==========================================================================
                       /**
                       * Processes {@link WindowEvent}s occurring on this component.
                       * @param event the {@code WindowEvent}
                       */
                       @Override //Overrides JFrame.processWindowEvent( WindowEvent )
                       protected void processWindowEvent( WindowEvent event ) {
                            if( WindowEvent.WINDOW_CLOSING == event.getID() ) { //If the event is the window closing...
                                dispose();
                                beNotified();
                                theSocksJPanel.disablePanel( true );
                            } else { //If the event is not the window closing...
                                super.processWindowEvent( event ); //Proceed normally
                            } 
                       }
                };
                
                //Set the title
                parentFrame.setTitle("SOCKS: (" + theHostName + ")");
                parentFrame.add(theSocksJPanel);
                parentFrame.setResizable(false);
                parentFrame.setLocationRelativeTo(null);
                
                //Set the icon
                Image appIcon = Utilities.loadImageFromJar( Constants.RELAY_IMG_STR );
                if( appIcon != null )
                    parentFrame.setIconImage( appIcon );                
                
                //Pack and show
                parentFrame.pack();
                parentFrame.setVisible(true);
                
                //Wait to be notified
                waitToBeNotified();
                
                //Kill all
                aManager.stopSocksServer();
                aManager.shutdown();
                
                                
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
            
        } catch (InterruptedException ex) {
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
        return theSocksJPanel;
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
    public PortManager getPortManager() {
        return theManager;        
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
        SocksJPanel aPanel = (SocksJPanel) getParentComponent();
        aPanel.resetPanel();
        
        //Show popup
        JOptionPane.showMessageDialog( aPanel, "Server is not connected to the Host.","Error", JOptionPane.ERROR_MESSAGE );
                
    }
 
}
