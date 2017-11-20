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
 *  Created on Jun 2, 2013
 */

package pwnbrew.network.control;

import pwnbrew.network.PortRouter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import pwnbrew.ClientConfig;
import pwnbrew.log.LoggableException;
import pwnbrew.manager.PortManager;
import pwnbrew.manager.DataManager;
import pwnbrew.network.control.messages.*;
import pwnbrew.utilities.DebugPrinter;

/**
 *
 *  
 */
public class ControlMessageManager extends DataManager {

    private static ControlMessageManager theControlManager;
    private String serverAlias = null;
    private static final Map<Short, String> theControlMessageMap = new HashMap();
    
    private static final String NAME_Class = ControlMessageManager.class.getSimpleName();    
    //Populate message map
    static {
        
        //Add message
        if( theControlMessageMap.containsKey(CancelSearch.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "ControlMessageMap already contains id: " + CancelSearch.MESSAGE_ID);
        theControlMessageMap.put(CancelSearch.MESSAGE_ID, CancelSearch.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(ClassRequest.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "ControlMessageMap already contains id" + ClassRequest.MESSAGE_ID);
        theControlMessageMap.put(ClassRequest.MESSAGE_ID, ClassRequest.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(ClassResponse.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "ControlMessageMap already contains id" + ClassResponse.MESSAGE_ID);
        theControlMessageMap.put(ClassResponse.MESSAGE_ID, ClassResponse.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(CreateShell.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "ControlMessageMap already contains id" + CreateShell.MESSAGE_ID);
        theControlMessageMap.put(CreateShell.MESSAGE_ID, CreateShell.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(CreateShellAck.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "ControlMessageMap already contains id" + CreateShellAck.MESSAGE_ID);
        theControlMessageMap.put(CreateShellAck.MESSAGE_ID, CreateShellAck.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(DirCount.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "ControlMessageMap already contains id" + DirCount.MESSAGE_ID);
        theControlMessageMap.put(DirCount.MESSAGE_ID, DirCount.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(FileOpResult.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "ControlMessageMap already contains id" + FileOpResult.MESSAGE_ID);
        theControlMessageMap.put(FileOpResult.MESSAGE_ID, FileOpResult.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(FileOperation.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "ControlMessageMap already contains id" + FileOperation.MESSAGE_ID);
        theControlMessageMap.put(FileOperation.MESSAGE_ID, FileOperation.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(FileSystemMsg.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "ControlMessageMap already contains id" + FileSystemMsg.MESSAGE_ID);
        theControlMessageMap.put(FileSystemMsg.MESSAGE_ID, FileSystemMsg.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(GetDrives.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "ControlMessageMap already contains id" + GetDrives.MESSAGE_ID);
        theControlMessageMap.put(GetDrives.MESSAGE_ID, GetDrives.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(Hello.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "ControlMessageMap already contains id" + Hello.MESSAGE_ID);
        theControlMessageMap.put(Hello.MESSAGE_ID, Hello.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(HelloAck.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "ControlMessageMap already contains id" + HelloAck.MESSAGE_ID);
        theControlMessageMap.put(HelloAck.MESSAGE_ID, HelloAck.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(HelloRepeat.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "ControlMessageMap already contains id" + HelloRepeat.MESSAGE_ID);
        theControlMessageMap.put(HelloRepeat.MESSAGE_ID, HelloRepeat.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(KillShell.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "ControlMessageMap already contains id" + KillShell.MESSAGE_ID);
        theControlMessageMap.put(KillShell.MESSAGE_ID, KillShell.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(ListFiles.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "ControlMessageMap already contains id" + ListFiles.MESSAGE_ID);
        theControlMessageMap.put(ListFiles.MESSAGE_ID, ListFiles.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(LogMsg.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "ControlMessageMap already contains id" + LogMsg.MESSAGE_ID);
        theControlMessageMap.put(LogMsg.MESSAGE_ID, LogMsg.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(Migrate.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "ControlMessageMap already contains id" + Migrate.MESSAGE_ID);
        theControlMessageMap.put(Migrate.MESSAGE_ID, Migrate.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(NoOp.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "ControlMessageMap already contains id" + NoOp.MESSAGE_ID);
        theControlMessageMap.put(NoOp.MESSAGE_ID, NoOp.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(PushFile.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "ControlMessageMap already contains id" + PushFile.MESSAGE_ID);
        theControlMessageMap.put(PushFile.MESSAGE_ID, PushFile.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(PushFileAbort.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "ControlMessageMap already contains id" + PushFileAbort.MESSAGE_ID);
        theControlMessageMap.put(PushFileAbort.MESSAGE_ID, PushFileAbort.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(PushFileAck.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "ControlMessageMap already contains id" + PushFileAck.MESSAGE_ID);
        theControlMessageMap.put(PushFileAck.MESSAGE_ID, PushFileAck.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(PushFileFin.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "ControlMessageMap already contains id" + PushFileFin.MESSAGE_ID);
        theControlMessageMap.put(PushFileFin.MESSAGE_ID, PushFileFin.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(PushFileUpdate.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "ControlMessageMap already contains id" + PushFileUpdate.MESSAGE_ID);
        theControlMessageMap.put(PushFileUpdate.MESSAGE_ID, PushFileUpdate.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(RelayDisconnect.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "ControlMessageMap already contains id" + RelayDisconnect.MESSAGE_ID);
        theControlMessageMap.put(RelayDisconnect.MESSAGE_ID, RelayDisconnect.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(RelayStart.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "ControlMessageMap already contains id" + RelayStart.MESSAGE_ID);
        theControlMessageMap.put(RelayStart.MESSAGE_ID, RelayStart.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(RelayStatus.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "ControlMessageMap already contains id" + RelayStatus.MESSAGE_ID);
        theControlMessageMap.put(RelayStatus.MESSAGE_ID, RelayStatus.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(RelayStop.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "ControlMessageMap already contains id" + RelayStop.MESSAGE_ID);
        theControlMessageMap.put(RelayStop.MESSAGE_ID, RelayStop.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(Reload.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "ControlMessageMap already contains id" + Reload.MESSAGE_ID);
        theControlMessageMap.put(Reload.MESSAGE_ID, Reload.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(SetRelayWrap.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "ControlMessageMap already contains id" + SetRelayWrap.MESSAGE_ID);
        theControlMessageMap.put(SetRelayWrap.MESSAGE_ID, SetRelayWrap.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(Sleep.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "ControlMessageMap already contains id" + Sleep.MESSAGE_ID);
        theControlMessageMap.put(Sleep.MESSAGE_ID, Sleep.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(SocksCreateHandlerAckMsg.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "ControlMessageMap already contains id" + SocksCreateHandlerAckMsg.MESSAGE_ID);
        theControlMessageMap.put(SocksCreateHandlerAckMsg.MESSAGE_ID, SocksCreateHandlerAckMsg.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(SocksOperation.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "ControlMessageMap already contains id" + SocksOperation.MESSAGE_ID);
        theControlMessageMap.put(SocksOperation.MESSAGE_ID, SocksOperation.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(SocksOperationAck.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "ControlMessageMap already contains id" + SocksOperationAck.MESSAGE_ID);
        theControlMessageMap.put(SocksOperationAck.MESSAGE_ID, SocksOperationAck.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(StageFlag.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "ControlMessageMap already contains id" + StageFlag.MESSAGE_ID);
        theControlMessageMap.put(StageFlag.MESSAGE_ID, StageFlag.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(StageFlagAck.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "ControlMessageMap already contains id" + StageFlagAck.MESSAGE_ID);
        theControlMessageMap.put(StageFlagAck.MESSAGE_ID, StageFlagAck.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(TaskGetFile.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "ControlMessageMap already contains id" + TaskGetFile.MESSAGE_ID);
        theControlMessageMap.put(TaskGetFile.MESSAGE_ID, TaskGetFile.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(TaskProgress.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "ControlMessageMap already contains id" + TaskProgress.MESSAGE_ID);
        theControlMessageMap.put(TaskProgress.MESSAGE_ID, TaskProgress.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(Uninstall.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "ControlMessageMap already contains id" + Uninstall.MESSAGE_ID);
        theControlMessageMap.put(Uninstall.MESSAGE_ID, Uninstall.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(UpgradeStager.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "ControlMessageMap already contains id" + UpgradeStager.MESSAGE_ID);
        theControlMessageMap.put(UpgradeStager.MESSAGE_ID, UpgradeStager.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(UpgradeStagerComplete.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "ControlMessageMap already contains id" + UpgradeStagerComplete.MESSAGE_ID);
        theControlMessageMap.put(UpgradeStagerComplete.MESSAGE_ID, UpgradeStagerComplete.class.getCanonicalName());
        //Add message
        if( theControlMessageMap.containsKey(TaskStatus.MESSAGE_ID))
            DebugPrinter.printMessage(NAME_Class, "ControlMessageMap already contains id" + TaskStatus.MESSAGE_ID);
        theControlMessageMap.put(TaskStatus.MESSAGE_ID, TaskStatus.class.getCanonicalName());

        
    }
    
    public static String getControlMessagePath( short msgId ){
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
     * @throws pwnbrew.log.LoggableException 
     */
    public synchronized static ControlMessageManager initialize( PortManager passedCommManager ) throws IOException, LoggableException {

        if( theControlManager == null ) {
            theControlManager = new ControlMessageManager( passedCommManager );
            createPortRouter( passedCommManager, ClientConfig.getConfig().getSocketPort(), true );
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
    public void handleMessage( PortRouter srcPortRouter, byte[] msgBytes ) {        
        theControlManager.getDataHandler().processData( srcPortRouter, msgBytes);        
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
