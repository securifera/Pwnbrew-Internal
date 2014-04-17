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
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import javax.swing.JFrame;
import pwnbrew.MaltegoStub;
import pwnbrew.StubConfig;
import pwnbrew.manager.PortManager;
import pwnbrew.misc.Constants;
import pwnbrew.misc.DebugPrinter;
import pwnbrew.misc.SocketUtilities;
import pwnbrew.misc.Utilities;
import pwnbrew.network.ClientPortRouter;
import pwnbrew.network.control.ControlMessageManager;
import pwnbrew.output.StreamReceiver;
import pwnbrew.shell.Bash;
import pwnbrew.shell.CommandPrompt;
import pwnbrew.shell.Powershell;
import pwnbrew.shell.Shell;
import pwnbrew.shell.ShellJPanel;
import pwnbrew.shell.ShellJPanelListener;
import pwnbrew.shell.ShellListener;
import pwnbrew.xml.maltego.MaltegoMessage;

/**
 *
 * @author Securifera
 */
public class ToShell extends Function implements ShellJPanelListener, ShellListener {
    
    private static final String NAME_Class = MaltegoStub.class.getSimpleName();
    
    private volatile boolean notified = false;
    private int theHostId = 0;   
    private String theOS;
    private String theHostName;
    
    //Create the return msg
    private MaltegoMessage theReturnMsg = new MaltegoMessage();
    private ShellJPanel theShellPanel;
    private Shell theShell = null;
  
    //==================================================================
    /**
     * Constructor
     */
    public ToShell( MaltegoStub passedManager ) {
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
            DebugPrinter.printMessage( NAME_Class, "listclients", "No pwnbrew server IP provided", null);
            return retStr;
        }
         
        //Get server port
        String serverPortStr = objectMap.get( Constants.SERVER_PORT);
        if( serverPortStr == null ){
            DebugPrinter.printMessage( NAME_Class, "listclients", "No pwnbrew server port provided", null);
            return retStr;
        }
        
        //Get host id
        String hostIdStr = objectMap.get( Constants.HOST_ID);
        if( hostIdStr == null ){
            DebugPrinter.printMessage( NAME_Class, "listclients", "No host id provided", null);
            return retStr;
        }
        
        //Get host id
        String tempOs = objectMap.get( Constants.HOST_OS);
        if( tempOs == null ){
            DebugPrinter.printMessage( NAME_Class, "listclients", "No host id provided", null);
            return retStr;
        }
        
        //Get host id
        String tempName = objectMap.get( Constants.HOST_NAME);
        if( tempName == null ){
            DebugPrinter.printMessage( NAME_Class, "listclients", "No host id provided", null);
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
                DebugPrinter.printMessage( NAME_Class, "listclients", "Unable to retrieve port router.", null);
                return retStr;     
            }           
            
            //Set up the port wrapper
            theManager.initialize();
            
            //Connect to server
            boolean connected = aPR.ensureConnectivity( serverPort, theManager );
            if( connected ){
             
                //Set the host id
                theHostId = Integer.parseInt( hostIdStr);
                theOS = tempOs;
                theHostName = tempName;
                
                
                JFrame aFrame = new JFrame(){
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
                            } else { //If the event is not the window closing...
                                super.processWindowEvent( event ); //Proceed normally
                            } 
                       }
                };
                
                //Set the title
                aFrame.setTitle(theHostName);
                
                //Create and add the shell panel
                theShellPanel = new ShellJPanel( this );
                aFrame.add(theShellPanel);
                
                //Set the icon
                Image appIcon = Utilities.loadImageFromJar( Constants.TERM_IMG_STR );
                if( appIcon != null ) {
                    aFrame.setIconImage( appIcon );
                }
                
                //Pack and show
                aFrame.pack();
                aFrame.setVisible(true);
                
                //Wait to be notified
                waitToBeNotified();
                
                //Send shell shutdown message
                killShell();
                
                //Sleep a couple seconds to make sure the message was sent
                Thread.sleep(2000);
                
                retStr = theReturnMsg.getXml();
                
            } else {
                StringBuilder aSB = new StringBuilder()
                        .append("Unable to connect to the Pwnbrew server at \"")
                        .append(serverIp).append(":").append(serverPort).append("\"");
                DebugPrinter.printMessage( NAME_Class, "listclients", aSB.toString(), null);
            }
            
        } catch (IOException | InterruptedException ex) {
            DebugPrinter.printMessage( NAME_Class, "listclients", ex.getMessage(), ex );
        }
        
        return retStr;
    }
    
    //========================================================================
    /**
     * 
     * @return 
     */
    @Override
    public Component getParentComponent(){
        return theShellPanel;
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
    protected synchronized void beNotified() {
        notified = true;
        notifyAll();
    }


    // ==========================================================================
    /**
     *  Spawns a shell
     * 
     * @param passedClass 
     */
    @Override
    public void spawnShell(Class passedClass) {
        //Get the shell
        if( passedClass != null ){
            
            try {
                
                Constructor aConstructor = passedClass.getConstructor( Executor.class, ShellListener.class );
                theShell = (Shell)aConstructor.newInstance( Constants.Executor, this);
                    
                //Start the shell
                theShell.start();

            } catch (  NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                DebugPrinter.printMessage( NAME_Class, "spawnShell", ex.getMessage(), ex );
            }
            
        }
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
            
        } else if( Utilities.isUnix(theOS)){
            
            //Add bash
            theShellList.add(Bash.class);   
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

    //========================================================================
    /**
     *  Get the text pane for the shell output
     * @return 
     */
    @Override
    public StreamReceiver getStreamReceiver() {
        return theShellPanel.getShellTextPane();
    }

     //===============================================================
    /**
     * Returns the shell log dir.
     *
     * @return 
    */
    @Override
    public File getShellLogDir() {
        
        //Create a directory in the local dir
        File parentDir = new File( theHostName );
                
        //Add the shell dir
        File shellDir = new File( parentDir, "shell");
        shellDir.mkdirs(); 
        
        return shellDir;
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


}
