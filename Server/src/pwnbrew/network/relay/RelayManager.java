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
 *  RelayManager.java
 *
 *  Created on Dec 2, 2013
 */

package pwnbrew.network.relay;

import pwnbrew.network.PortRouter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.logging.Level;
import pwnbrew.logging.Log;
import pwnbrew.logging.LoggableException;
import pwnbrew.manager.CommManager;
import pwnbrew.manager.DataManager;
import pwnbrew.misc.Constants;
import pwnbrew.misc.DebugPrinter;
import pwnbrew.network.DataHandler;
import pwnbrew.network.Message;
import pwnbrew.network.ServerPortRouter;
import pwnbrew.utilities.SocketUtilities;
import pwnbrew.xmlBase.ServerConfig;

/**
 *
 *  
 */
public class RelayManager extends DataManager {

    private static RelayManager theRelayManager;
//    private ServerPortRouter theServerPortRouter = null;
    
    private static final String NAME_Class = RelayManager.class.getSimpleName();
    
    //===========================================================================
    /*
     *  Constructor
     */
    private RelayManager( CommManager passedCommManager ) {
        
        super(passedCommManager);        
        
    }  
    
    // ==========================================================================
    /**
     *   Creates a ControlMessageManager
     * @param passedCommManager
     * @return 
     * @throws java.io.IOException 
     */
    public synchronized static RelayManager initialize( CommManager passedCommManager ) throws IOException {

        //Create the manager
        if( theRelayManager == null ) {
            theRelayManager = new RelayManager( passedCommManager );
        }   
        
//        //Create the port router
//        if( theRelayManager.getServerPorterRouter() == null ){
//            ServerPortRouter theServerPortRouter = new ServerPortRouter( passedCommManager, true );   
//            theRelayManager.setPortRouter(theServerPortRouter);       
//        }
        
        return theRelayManager;

    }/* END initialize() */
   
    
    // ==========================================================================
    /**
     *   Gets the RelayManager
     * @return 
     */
    public synchronized static RelayManager getRelayManager(){
        return theRelayManager;
    }
    
    //===============================================================
    /**
     *   Send the message out the given channel.
     *
     * @param msgBytes
    */
    @Override
    public void handleMessage( byte[] msgBytes ) {        
                        
        //Get the dest id
        byte[] dstHostId = Arrays.copyOfRange(msgBytes, Message.DEST_HOST_ID_OFFSET, Message.DEST_HOST_ID_OFFSET + 4);
        int tempId = SocketUtilities.byteArrayToInt(dstHostId);
//               
//        //Get the port router
        PortRouter thePR = null;
//        if( tempId == Constants.SERVER_ID ){   
            try {
                thePR = theCommManager.getPortRouter( ServerConfig.getServerConfig().getSocketPort() );    
                DebugPrinter.printMessage(NAME_Class, "Queueing relay message");
            } catch (LoggableException ex) {
                Log.log( Level.SEVERE, NAME_Class, "handleMessage()", ex.getMessage(), ex);        
            }
//        } else {     
//
//            //If the dest is not the server
//            thePR = theServerPortRouter;  
//            DebugPrinter.printMessage(NAME_Class, "Queueing relay message to client");
//        }

        //Queue the message to be sent
        if( thePR != null ){
            thePR.queueSend( msgBytes, tempId ); 
        }      
        
    }
    
    //===========================================================================
    /*
     *  Returns the data handler
    */
    @Override
    public DataHandler getDataHandler() {
        return theDataHandler;
    }  
  
//    //===============================================================
//    /**
//     * Sets the port router
//     *
//    */
//    private void setPortRouter(ServerPortRouter passedRouter) {
//        theServerPortRouter = passedRouter;
//    }

//    //===========================================================================
//    /**
//     *  Shutdown the relay 
//     */
//    @Override
//    public void shutdown() {
//        theServerPortRouter.shutdown();
//        theServerPortRouter = null;
//    }

//    //===========================================================================
//    /**
//     *  Return the Port Router
//     * @return 
//    */
//    public ServerPortRouter getServerPorterRouter() {
//        return theServerPortRouter;
//    }
    
//    //===============================================================
//    /**
//     *   Send the message out the given channel.
//     *
//     * @param passedMessage
//    */
//    public void send( Message passedMessage ) {
//
//        int msgLen = passedMessage.getLength();
//        ByteBuffer aByteBuffer = ByteBuffer.allocate( msgLen );
//        passedMessage.append(aByteBuffer);
//        
//        //Queue the message to be sent
//        theServerPortRouter.queueSend( Arrays.copyOf( aByteBuffer.array(), aByteBuffer.position()), passedMessage.getDestHostId());
//        DebugPrinter.printMessage(NAME_Class, "Queueing " + passedMessage.getClass().getSimpleName() + " message");
//              
//    }
    
}
