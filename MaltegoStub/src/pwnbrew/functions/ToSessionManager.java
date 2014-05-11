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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import pwnbrew.MaltegoStub;
import pwnbrew.StubConfig;
import pwnbrew.misc.Constants;
import pwnbrew.misc.CountSeeker;
import pwnbrew.misc.DebugPrinter;
import pwnbrew.misc.HostHandler;
import pwnbrew.misc.SocketUtilities;
import pwnbrew.misc.Utilities;
import pwnbrew.network.ClientPortRouter;
import pwnbrew.network.control.ControlMessageManager;
import pwnbrew.network.control.messages.ClearSessions;
import pwnbrew.network.control.messages.ControlMessage;
import pwnbrew.network.control.messages.GetCount;
import pwnbrew.sessions.SessionJFrameListener;
import pwnbrew.sessions.SessionsJFrame;
import pwnbrew.xml.maltego.MaltegoMessage;
import pwnbrew.xml.maltego.custom.Host;

/**
 *
 * @author Securifera
 */
public class ToSessionManager extends Function implements SessionJFrameListener, HostHandler, CountSeeker {
    
    private static final String NAME_Class = MaltegoStub.class.getSimpleName();
    
    private volatile boolean notified = false;
    private int theHostId = 0;   
    private String theOS;
    private String theHostName;
    
    private final List<Host> theHostList = new ArrayList<>();
    private volatile int theClientCount = 0;  
    private SessionsJFrame theSessionsJFrame = null;
    
    //Create the return msg
    private MaltegoMessage theReturnMsg = new MaltegoMessage();
    
  
    //==================================================================
    /**
     * Constructor
     * @param passedManager
     */
    public ToSessionManager( MaltegoStub passedManager ) {
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
        
//        //Get host id
//        String hostIdStr = objectMap.get( Constants.HOST_ID);
//        if( hostIdStr == null ){
//            DebugPrinter.printMessage( NAME_Class, "listclients", "No host id provided", null);
//            return retStr;
//        }
        
//        //Get host id
//        String tempOs = objectMap.get( Constants.HOST_OS);
//        if( tempOs == null ){
//            DebugPrinter.printMessage( NAME_Class, "listclients", "No host id provided", null);
//            return retStr;
//        }
        
//        //Get host id
//        String tempName = objectMap.get( Constants.HOST_NAME);
//        if( tempName == null ){
//            DebugPrinter.printMessage( NAME_Class, "listclients", "No host id provided", null);
//            return retStr;
//        }
         
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
                
                 //Get the client count
                ControlMessage aMsg = new GetCount( Constants.SERVER_ID, GetCount.HOST_COUNT, 0 );
                aCMManager.send(aMsg);
                
                //Wait for the response
                waitToBeNotified( 180 * 1000);
                
                //Get the client info
                if( theClientCount > 0 ){
                
                    //Get each client msg                
                    aMsg = new pwnbrew.network.control.messages.GetHosts( Constants.SERVER_ID );
                    aCMManager.send(aMsg);
                
                    //Wait for the response
                    waitToBeNotified( 180 * 1000);

                    if( theClientCount == 0 ){ 

                         //Create the file browser frame
                        theSessionsJFrame = new SessionsJFrame( this, theHostList );

                        //Set the title
                        theSessionsJFrame.setTitle("Session Manager - "+serverIp);

                        //Set the icon
                        Image appIcon = Utilities.loadImageFromJar( Constants.SCHEDULE_IMG_STR );
                        if( appIcon != null )
                            theSessionsJFrame.setIconImage( appIcon );                

                        //Pack and show
                        theSessionsJFrame.setVisible(true);               

                        //Wait to be notified
                        waitToBeNotified();
                    }
                
                }
                retStr = theReturnMsg.getXml();
                
            } else {
                StringBuilder aSB = new StringBuilder()
                        .append("Unable to connect to the Pwnbrew server at \"")
                        .append(serverIp).append(":").append(serverPort).append("\"");
                DebugPrinter.printMessage( NAME_Class, "run", aSB.toString(), null);
            }
            
        } catch (IOException ex) {
            DebugPrinter.printMessage( NAME_Class, "run", ex.getMessage(), ex );
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
        return null ;
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

    //===============================================================
    /**
     * 
     * @param selected 
     */
    @Override
    public void setAutoSleepFlag(boolean selected) {
        //Send message to set the auto sleep flag
        ControlMessageManager aCMM = ControlMessageManager.getControlMessageManager();
    }

    //===============================================================
    /**
     * 
     * @param passedHostId
     */
    @Override
    public void clearSessionList( String passedHostId ) {
        
        //Send message to server to clear the session list
        ControlMessageManager aCMM = ControlMessageManager.getControlMessageManager();      
        ControlMessage aMsg = new ClearSessions( Constants.SERVER_ID, passedHostId);
        aCMM.send(aMsg);
        
        theSessionsJFrame.repaint();
    }

    @Override
    public void replaceDate(String aDate, String newDateStr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeCheckInDates() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    //===============================================================
    /**
     * 
     * @param countType
     * @param optionalHostId
     */
    @Override
    public synchronized void setCount(int passedCount, int countType, int optionalHostId ) {
        theClientCount = passedCount;
        beNotified();
    }
    
     //===============================================================
    /**
     * 
     * @param aHost 
     */
    @Override
    public synchronized void addHost(Host aHost) {
        
        theHostList.add(aHost);
        
        //Decrement and see if we are done
        theClientCount--;
        if( theClientCount == 0)
            beNotified();
        
    }

    //===============================================================
    /**
     * 
     * @param hostIdStr
     */
    @Override
    public void hostSelected(String hostIdStr) {
        //Get the client count
        ControlMessageManager aCMM = ControlMessageManager.getControlMessageManager();    
        
        //Get session count
        ControlMessage aMsg = new GetCount( Constants.SERVER_ID, GetCount.SESSION_COUNT, Integer.parseInt(hostIdStr) );
        aCMM.send(aMsg);
        
         //Get checkin count
        aMsg = new GetCount( Constants.SERVER_ID, GetCount.CHECKIN_COUNT, Integer.parseInt(hostIdStr) );
        aCMM.send(aMsg);
    }


}
