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
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JList;
import pwnbrew.MaltegoStub;
import pwnbrew.StubConfig;
import pwnbrew.log.LoggableException;
import pwnbrew.manager.DataManager;
import pwnbrew.misc.Constants;
import pwnbrew.misc.CountSeeker;
import pwnbrew.misc.DebugPrinter;
import pwnbrew.misc.HostHandler;
import pwnbrew.utilities.SocketUtilities;
import pwnbrew.misc.Utilities;
import pwnbrew.network.ClientPortRouter;
import pwnbrew.network.control.messages.AutoSleep;
import pwnbrew.network.control.messages.CheckInTimeMsg;
import pwnbrew.network.control.messages.ClearSessions;
import pwnbrew.network.control.messages.ControlMessage;
import pwnbrew.network.control.messages.GetCheckInSchedule;
import pwnbrew.network.control.messages.GetCount;
import pwnbrew.network.control.messages.GetSessions;
import pwnbrew.network.control.messages.RemoveHost;
import pwnbrew.sessions.SessionJFrameListener;
import pwnbrew.sessions.SessionsJFrame;
import pwnbrew.xml.maltego.Field;
import pwnbrew.xml.maltego.MaltegoTransformExceptionMessage;
import pwnbrew.xml.maltego.custom.Host;

/**
 *
 * @author Securifera
 */
public class ToSessionManager extends Function implements SessionJFrameListener, HostHandler, CountSeeker {
    
    private static final String NAME_Class = MaltegoStub.class.getSimpleName();
    
    private volatile boolean notified = false;    
    private final List<Host> theHostList = new ArrayList<>();
    
    private volatile int theClientCount = 0;  
    private SessionsJFrame theSessionsJFrame = null;
            
  
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
     */
    @Override
    public void run(String passedObjectStr) {
        
        String retStr = "";
        Map<String, String> objectMap = getKeyValueMap(passedObjectStr); 
         
        //Get server IP
        String serverIp = objectMap.get( Constants.SERVER_IP);
        if( serverIp == null ){
            DebugPrinter.printMessage( NAME_Class, "ToSessionManager", "No pwnbrew server IP provided", null);
            return;
        }
         
        //Get server port
        String serverPortStr = objectMap.get( Constants.SERVER_PORT);
        if( serverPortStr == null ){
            DebugPrinter.printMessage( NAME_Class, "ToSessionManager", "No pwnbrew server port provided", null);
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
                DebugPrinter.printMessage( NAME_Class, "to_session_mgr", "Unable to create port router.", ex);
                return;
            }
        }  
        theManager.initialize();
        try {
            
            aPR.ensureConnectivity( serverPort, theManager );
            
            //Get the client count
            ControlMessage aMsg = new GetCount( Constants.SERVER_ID, GetCount.HOST_COUNT, "0" );
            DataManager.send( theManager, aMsg);
            
            //Wait for the response
            waitToBeNotified( 180 * 1000);           
            
            //Get the client info
            if( theClientCount > 0 ){
                
                //Get each client msg
                aMsg = new pwnbrew.network.control.messages.GetHosts( Constants.SERVER_ID, "0" );
                DataManager.send( theManager, aMsg);
                
                //Wait for the response
                waitToBeNotified( 180 * 1000);
                
                if( theClientCount == 0 ){
                    
                    //Create the file browser frame
                    theSessionsJFrame = new SessionsJFrame( this, theHostList );
                    
                    //Set to the first element
                    JList hostList = theSessionsJFrame.getHostJList();
                    if( hostList.getModel().getSize() > 0)
                        hostList.setSelectedIndex(0);
                    
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
            
        } catch( LoggableException ex ) {
            
            //Create a relay object
            pwnbrew.xml.maltego.Exception exMsg = new pwnbrew.xml.maltego.Exception( ex.getMessage() );
            MaltegoTransformExceptionMessage malMsg = theReturnMsg.getExceptionMessage();
            
            //Create the message list
            malMsg.getExceptionMessages().addExceptionMessage(exMsg);
        }
    
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
    @Override
    public synchronized void beNotified() {
        notified = true;
        notifyAll();
    }

    //===============================================================
    /**
     * 
     * @param passedHostId
     * @param selected 
     * @param passedOperation 
     */
    @Override
    public void setAutoSleepFlag( int passedHostId, boolean selected, byte passedOperation ) {
        
        if( passedOperation == AutoSleep.SET_VALUE ){
            
            //Get flag and send a msg
            AutoSleep anASMsg = new AutoSleep( Constants.SERVER_ID, passedHostId, passedOperation, selected );
            DataManager.send( theManager, anASMsg);               
            
            
        } else if(passedOperation == AutoSleep.GET_VALUE){
            
            //If it is the selected host
            if(theSessionsJFrame.isSelectedHost(passedHostId))
                theSessionsJFrame.setAutoSleepCheckbox(selected);
            
        }
    }

    //===============================================================
    /**
     * 
     * @param passedHostId
     */
    @Override
    public void clearSessionList( String passedHostId ) {
        
        //Send message to server to clear the session list
        ControlMessage aMsg = new ClearSessions( Constants.SERVER_ID, passedHostId);
        DataManager.send( theManager, aMsg);
        
        theSessionsJFrame.repaint();

    }

    //===============================================================
    /**
     * 
     * @param aDate
     * @param newDateStr 
     */
    @Override
    public void replaceDate(String aDate, String newDateStr) {
         
        //Get currently selected host
        JList hostJList = theSessionsJFrame.getHostJList();
        Object anObj = hostJList.getSelectedValue();
        if( anObj != null && anObj instanceof Host ){
            Host aHost = (Host)anObj;
            Field hostIdField = aHost.getField( Constants.HOST_ID );
            String hostIdStr = hostIdField.getXmlObjectContent();
            
            //Check if they are equal
            int hostId = Integer.parseInt(hostIdStr);
            
            //Send message to server to replace the given date
            try {
                CheckInTimeMsg aMsg = new CheckInTimeMsg( Constants.SERVER_ID, hostId, newDateStr, CheckInTimeMsg.REPLACE_TIME );
                aMsg.addPrevCheckIn(aDate);
                DataManager.send( theManager, aMsg);
            } catch (UnsupportedEncodingException ex) {
            }

            //refresh
            refreshSelection();
            
        }
    }
    
     //===============================================================
    /**
     * 
     */
    @Override
    public void refreshSelection() {
        JList hostJList = theSessionsJFrame.getHostJList();
        int selIndex = hostJList.getSelectedIndex();
        hostJList.clearSelection();
        hostJList.setSelectedIndex(selIndex);
    }

    //===============================================================
    /**
     * 
     */
    @Override
    public void removeCheckInDates() {
        
        //Get currently selected host
        JList hostJList = theSessionsJFrame.getHostJList();
        Object anObj = hostJList.getSelectedValue();
        if( anObj != null && anObj instanceof Host ){
            Host aHost = (Host)anObj;
            Field hostIdField = aHost.getField( Constants.HOST_ID );
            String hostIdStr = hostIdField.getXmlObjectContent();
            
            //Check if they are equal
            int hostId = Integer.parseInt(hostIdStr);
            
            //Get selected check-in time list
            JList checkInTimeList = theSessionsJFrame.getCheckInJList();
            List<String> theDatesToRemove = new ArrayList<>(checkInTimeList.getSelectedValuesList());
         
            //Remote the dates
            try {
                for( String aStr : theDatesToRemove ){
                    CheckInTimeMsg aMsg = new CheckInTimeMsg( Constants.SERVER_ID, hostId, aStr, CheckInTimeMsg.REMOVE_TIME );
                    DataManager.send( theManager, aMsg);
                }
            } catch (UnsupportedEncodingException ex) {
            }

            //refresh
            refreshSelection();
            
                     
        }   
    }
    
    //===============================================================
    /**
     * 
     * @param countType
     * @param optionalHostId
     */
    @Override
    public synchronized void setCount(int passedCount, int countType, int optionalHostId ) {
        
        switch( countType){
            case GetCount.HOST_COUNT:
                theClientCount = passedCount;
                break;            
        }
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
       
        //Send a msg to get the sessions
        int hostId = Integer.parseInt(hostIdStr);
        ControlMessage aMsg = new GetSessions( Constants.SERVER_ID, hostId);
        DataManager.send( theManager, aMsg);
        
        //Send a msg to get the checkins
        aMsg = new GetCheckInSchedule(  Constants.SERVER_ID, hostId);
        DataManager.send( theManager, aMsg);

    }

    //===============================================================
    /**
     * Add the check in time
     * 
     * @param hostId
     * @param checkInDatStr 
     */
    public void addCheckInTime(int hostId, String checkInDatStr) {
        if( theSessionsJFrame != null )
            theSessionsJFrame.addCheckInDate(hostId, checkInDatStr); 
    }

    //===============================================================
    /**
     * Add the session 
     * 
     * @param hostId
     * @param checkInDatStr 
     */
    public void addSession(int hostId, String checkInDatStr/**, String checkOutDatStr**/) {
        if( theSessionsJFrame != null )
            theSessionsJFrame.addSession( hostId, checkInDatStr/**, checkOutDatStr**/);  
    }

    //===============================================================
    /**
     * Remove the passed host 
     * 
     * @param passedHost
     */
    @Override
    public void removeHost(Host passedHost) {
        
        //Send a msg to remove the host
        int hostId = Integer.parseInt( passedHost.getField(Constants.HOST_ID).getXmlObjectContent() );
        ControlMessage aMsg = new RemoveHost( Constants.SERVER_ID, hostId);
        DataManager.send( theManager, aMsg);
        
    }

}
