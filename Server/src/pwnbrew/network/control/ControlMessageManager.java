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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import pwnbrew.exception.RemoteExceptionWrapper;
import pwnbrew.log.Log;
import pwnbrew.log.LoggableException;
import pwnbrew.manager.PortManager;
import pwnbrew.manager.DataManager;
import pwnbrew.network.PortRouter;
import pwnbrew.xml.ServerConfig;

/**
 *
 *  
 */
public class ControlMessageManager extends DataManager {

    private static ControlMessageManager theControlManager;
    private static final Map<Short, String> theControlMessageMap = new HashMap();
        
    private static final String NAME_Class = ControlMessageManager.class.getSimpleName();
    
      //Populate message map
//    static {
//        //Add message
//        if( theControlMessageMap.containsKey(ClassRequest.MESSAGE_ID))
//            DebugPrinter.printMessage(NAME_Class, "ControlMessageMap already contains id" + ClassRequest.MESSAGE_ID);
//        theControlMessageMap.put(ClassRequest.MESSAGE_ID, ClassRequest.class.getCanonicalName());
//        //Add message
//        if( theControlMessageMap.containsKey(ClassResponse.MESSAGE_ID))
//            DebugPrinter.printMessage(NAME_Class, "ControlMessageMap already contains id" + ClassResponse.MESSAGE_ID);
//        theControlMessageMap.put(ClassResponse.MESSAGE_ID, ClassResponse.class.getCanonicalName());
//        //Add message
//        if( theControlMessageMap.containsKey(FileOperation.MESSAGE_ID))
//            DebugPrinter.printMessage(NAME_Class, "ControlMessageMap already contains id" + FileOperation.MESSAGE_ID);
//        theControlMessageMap.put(FileOperation.MESSAGE_ID, FileOperation.class.getCanonicalName());
//        //Add message
//        if( theControlMessageMap.containsKey(Hello.MESSAGE_ID))
//            DebugPrinter.printMessage(NAME_Class, "ControlMessageMap already contains id" + Hello.MESSAGE_ID);
//        theControlMessageMap.put(Hello.MESSAGE_ID, Hello.class.getCanonicalName());
//        //Add message
//        if( theControlMessageMap.containsKey(HelloAck.MESSAGE_ID))
//            DebugPrinter.printMessage(NAME_Class, "ControlMessageMap already contains id" + HelloAck.MESSAGE_ID);
//        theControlMessageMap.put(HelloAck.MESSAGE_ID, HelloAck.class.getCanonicalName());
//        //Add message
//        if( theControlMessageMap.containsKey(KillShell.MESSAGE_ID))
//            DebugPrinter.printMessage(NAME_Class, "ControlMessageMap already contains id" + KillShell.MESSAGE_ID);
//        theControlMessageMap.put(KillShell.MESSAGE_ID, KillShell.class.getCanonicalName());
//        //Add message
//        if( theControlMessageMap.containsKey(LogMsg.MESSAGE_ID))
//            DebugPrinter.printMessage(NAME_Class, "ControlMessageMap already contains id" + LogMsg.MESSAGE_ID);
//        theControlMessageMap.put(LogMsg.MESSAGE_ID, LogMsg.class.getCanonicalName());
//        //Add message
//        if( theControlMessageMap.containsKey(NoOp.MESSAGE_ID))
//            DebugPrinter.printMessage(NAME_Class, "ControlMessageMap already contains id" + NoOp.MESSAGE_ID);
//        theControlMessageMap.put(NoOp.MESSAGE_ID, NoOp.class.getCanonicalName());
//        //Add message
//        if( theControlMessageMap.containsKey(PushFile.MESSAGE_ID))
//            DebugPrinter.printMessage(NAME_Class, "ControlMessageMap already contains id" + PushFile.MESSAGE_ID);
//        theControlMessageMap.put(PushFile.MESSAGE_ID, PushFile.class.getCanonicalName());
//        //Add message
//        if( theControlMessageMap.containsKey(PushFileAbort.MESSAGE_ID))
//            DebugPrinter.printMessage(NAME_Class, "ControlMessageMap already contains id" + PushFileAbort.MESSAGE_ID);
//        theControlMessageMap.put(PushFileAbort.MESSAGE_ID, PushFileAbort.class.getCanonicalName());
//        //Add message
//        if( theControlMessageMap.containsKey(PushFileAck.MESSAGE_ID))
//            DebugPrinter.printMessage(NAME_Class, "ControlMessageMap already contains id" + PushFileAck.MESSAGE_ID);
//        theControlMessageMap.put(PushFileAck.MESSAGE_ID, PushFileAck.class.getCanonicalName());
//        //Add message
//        if( theControlMessageMap.containsKey(PushFileFin.MESSAGE_ID))
//            DebugPrinter.printMessage(NAME_Class, "ControlMessageMap already contains id" + PushFileFin.MESSAGE_ID);
//        theControlMessageMap.put(PushFileFin.MESSAGE_ID, PushFileFin.class.getCanonicalName());
//        //Add message
//        if( theControlMessageMap.containsKey(PushFileUpdate.MESSAGE_ID))
//            DebugPrinter.printMessage(NAME_Class, "ControlMessageMap already contains id" + PushFileUpdate.MESSAGE_ID);
//        theControlMessageMap.put(PushFileUpdate.MESSAGE_ID, PushFileUpdate.class.getCanonicalName());
//        //Add message
//        if( theControlMessageMap.containsKey(RelayDisconnect.MESSAGE_ID))
//            DebugPrinter.printMessage(NAME_Class, "ControlMessageMap already contains id" + RelayDisconnect.MESSAGE_ID);
//        theControlMessageMap.put(RelayDisconnect.MESSAGE_ID, RelayDisconnect.class.getCanonicalName());
//        //Add message
//        if( theControlMessageMap.containsKey(RelayStart.MESSAGE_ID))
//            DebugPrinter.printMessage(NAME_Class, "ControlMessageMap already contains id" + RelayStart.MESSAGE_ID);
//        theControlMessageMap.put(RelayStart.MESSAGE_ID, RelayStart.class.getCanonicalName());
//        //Add message
//        if( theControlMessageMap.containsKey(RelayStatus.MESSAGE_ID))
//            DebugPrinter.printMessage(NAME_Class, "ControlMessageMap already contains id" + RelayStatus.MESSAGE_ID);
//        theControlMessageMap.put(RelayStatus.MESSAGE_ID, RelayStatus.class.getCanonicalName());
//        //Add message
//        if( theControlMessageMap.containsKey(RelayStop.MESSAGE_ID))
//            DebugPrinter.printMessage(NAME_Class, "ControlMessageMap already contains id" + RelayStop.MESSAGE_ID);
//        theControlMessageMap.put(RelayStop.MESSAGE_ID, RelayStop.class.getCanonicalName());
//        //Add message
//        if( theControlMessageMap.containsKey(SetRelayWrap.MESSAGE_ID))
//            DebugPrinter.printMessage(NAME_Class, "ControlMessageMap already contains id" + SetRelayWrap.MESSAGE_ID);
//        theControlMessageMap.put(SetRelayWrap.MESSAGE_ID, SetRelayWrap.class.getCanonicalName());
//        //Add message
//        if( theControlMessageMap.containsKey(Sleep.MESSAGE_ID))
//            DebugPrinter.printMessage(NAME_Class, "ControlMessageMap already contains id" + Sleep.MESSAGE_ID);
//        theControlMessageMap.put(Sleep.MESSAGE_ID, Sleep.class.getCanonicalName());
//        //Add message
//        if( theControlMessageMap.containsKey(StageFlag.MESSAGE_ID))
//            DebugPrinter.printMessage(NAME_Class, "ControlMessageMap already contains id" + StageFlag.MESSAGE_ID);
//        theControlMessageMap.put(StageFlag.MESSAGE_ID, StageFlag.class.getCanonicalName());
//        //Add message
//        if( theControlMessageMap.containsKey(StageFlagAck.MESSAGE_ID))
//            DebugPrinter.printMessage(NAME_Class, "ControlMessageMap already contains id" + StageFlagAck.MESSAGE_ID);
//        theControlMessageMap.put(StageFlagAck.MESSAGE_ID, StageFlagAck.class.getCanonicalName());
//        //Add message
//        if( theControlMessageMap.containsKey(TaskGetFile.MESSAGE_ID))
//            DebugPrinter.printMessage(NAME_Class, "ControlMessageMap already contains id" + TaskGetFile.MESSAGE_ID);
//        theControlMessageMap.put(TaskGetFile.MESSAGE_ID, TaskGetFile.class.getCanonicalName());
//        //Add message
//        if( theControlMessageMap.containsKey(UpgradeStager.MESSAGE_ID))
//            DebugPrinter.printMessage(NAME_Class, "ControlMessageMap already contains id" + UpgradeStager.MESSAGE_ID);
//        theControlMessageMap.put(UpgradeStager.MESSAGE_ID, UpgradeStager.class.getCanonicalName());
//        //Add message
//        if( theControlMessageMap.containsKey(UpgradeStagerComplete.MESSAGE_ID))
//            DebugPrinter.printMessage(NAME_Class, "ControlMessageMap already contains id" + UpgradeStagerComplete.MESSAGE_ID);
//        theControlMessageMap.put(UpgradeStagerComplete.MESSAGE_ID, UpgradeStagerComplete.class.getCanonicalName());
//        //Add message
//        if( theControlMessageMap.containsKey(AddToJarLibrary.MESSAGE_ID))
//            DebugPrinter.printMessage(NAME_Class, "ControlMessageMap already contains id" + AddToJarLibrary.MESSAGE_ID);
//        theControlMessageMap.put(AddToJarLibrary.MESSAGE_ID, AddToJarLibrary.class.getCanonicalName());
//        //Add message
//        if( theControlMessageMap.containsKey(DeleteJarItem.MESSAGE_ID))
//            DebugPrinter.printMessage(NAME_Class, "ControlMessageMap already contains id" + DeleteJarItem.MESSAGE_ID);
//        theControlMessageMap.put(DeleteJarItem.MESSAGE_ID, DeleteJarItem.class.getCanonicalName());
//        //Add message
//        if( theControlMessageMap.containsKey(GetJarItemFile.MESSAGE_ID))
//            DebugPrinter.printMessage(NAME_Class, "ControlMessageMap already contains id" + GetJarItemFile.MESSAGE_ID);
//        theControlMessageMap.put(GetJarItemFile.MESSAGE_ID, GetJarItemFile.class.getCanonicalName());
//        //Add message
//        if( theControlMessageMap.containsKey(JarItemMsg.MESSAGE_ID))
//            DebugPrinter.printMessage(NAME_Class, "ControlMessageMap already contains id" + JarItemMsg.MESSAGE_ID);
//        theControlMessageMap.put(JarItemMsg.MESSAGE_ID, JarItemMsg.class.getCanonicalName());
//        //Add message
//        if( theControlMessageMap.containsKey(GetNetworkSettings.MESSAGE_ID))
//            DebugPrinter.printMessage(NAME_Class, "ControlMessageMap already contains id" + GetNetworkSettings.MESSAGE_ID);
//        theControlMessageMap.put(GetNetworkSettings.MESSAGE_ID, GetNetworkSettings.class.getCanonicalName());
//        //Add message
//        if( theControlMessageMap.containsKey(ImportCert.MESSAGE_ID))
//            DebugPrinter.printMessage(NAME_Class, "ControlMessageMap already contains id" + ImportCert.MESSAGE_ID);
//        theControlMessageMap.put(ImportCert.MESSAGE_ID, ImportCert.class.getCanonicalName());
//        //Add message
//        if( theControlMessageMap.containsKey(RemoteException.MESSAGE_ID))
//            DebugPrinter.printMessage(NAME_Class, "ControlMessageMap already contains id" + RemoteException.MESSAGE_ID);
//        theControlMessageMap.put(RemoteException.MESSAGE_ID, RemoteException.class.getCanonicalName());
//       
//        
//    }
//    
    public static String getControlMessageClassPath( short msgId ){
        String retStr;
        synchronized(theControlMessageMap){
            retStr = theControlMessageMap.get(msgId);
        }
        return retStr;
    }
    
     public static String setControlMessageClassPath( short msgId, String classPath ){
        String retStr;
        synchronized(theControlMessageMap){
            retStr = theControlMessageMap.put(msgId, classPath);
        }
        return retStr;
    }
    
    //===========================================================================
    /*
     *  Constructor
     */
    private ControlMessageManager( PortManager passedCommManager ) {
        
        super(passedCommManager);        
        
        //Set the port
        try {
            
            ServerConfig theConfig = ServerConfig.getServerConfig();
            int thePort = theConfig.getSocketPort();
            setPort( thePort );
            
        } catch (LoggableException ex) {
            Log.log( Level.SEVERE, NAME_Class, "ControlMessageManager()", ex.getMessage(), ex);
        }
        
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
            createPortRouter( passedCommManager, theControlManager.getPort(), true );
        }
        
        return theControlManager;
    }
    
    // ==========================================================================
    /**
     *   Gets the ControlMessageManager
     * @return 
     */
    public synchronized static ControlMessageManager getMessageManager(){
        return theControlManager;
    }
    
    //===============================================================
    /**
     *   Send the message out the given channel.
     *
     * @param msgBytes
     * @throws pwnbrew.exception.RemoteExceptionWrapper
    */
    @Override
    public void handleMessage( PortRouter srcPortRouter, byte[] msgBytes ) throws RemoteExceptionWrapper {        
        theControlManager.getDataHandler().processData(srcPortRouter, msgBytes);        
    }
     
     //===========================================================================
    /*
     *  Returns the data handler
    */
    @Override
    public ControlMessageHandler getDataHandler() {
        return (ControlMessageHandler)theDataHandler;
    }      
    
}
