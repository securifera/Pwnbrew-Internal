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
 *  DataManager.java
 *
 */

package pwnbrew.manager;

import java.io.IOException;
import pwnbrew.MaltegoStub;
import pwnbrew.StubConfig;
import pwnbrew.network.control.ControlMessageManager;
import pwnbrew.network.ClientPortRouter;
import pwnbrew.network.DataHandler;
import pwnbrew.network.Message;
import pwnbrew.network.PortRouter;
import pwnbrew.network.control.messages.SocksOperation;
import pwnbrew.network.file.FileMessageManager;
import pwnbrew.shell.ShellMessageManager;
import pwnbrew.socks.SocksMessageManager;
/**
 *
 *  
 */
abstract public class DataManager {    

    protected DataHandler theDataHandler;
    protected final PortManager thePortManager;
    
    private transient static final String NAME_Class = DataManager.class.getSimpleName();
    

    //===========================================================================
    /**
     *  Constructor
     * @param passedProvider
     */
    public DataManager( PortManager passedProvider ) {
        thePortManager = passedProvider ;
    }  
    
    //===========================================================================
    /*
     *  Returns the comm manager
     */
    public PortManager getPortManager(){
        return thePortManager;
    }
    //===========================================================================
    /*
     *  Returns whether the passed type is a known message type
     */
    public static boolean isValidType(byte type) {
        
        boolean retVal = true;
        switch(type){
            
            case Message.REGISTER_MESSAGE_TYPE:                
            case Message.CONTROL_MESSAGE_TYPE:                
            case Message.PROCESS_MESSAGE_TYPE:                
            case Message.SOCKS_MESSAGE_TYPE:                
            case Message.FILE_MESSAGE_TYPE:
                break;            
            default:
                retVal = false;
                break;
                
        }
        return retVal;
        
    }
    
    //===========================================================================
    /*
     *  Returns the data handler
    */
    public DataHandler getDataHandler() {
        return theDataHandler;
    }  

    //===========================================================================
    /*
     *  Sets the data handler
    */
    public void setDataHandler( DataHandler passedHandler ) {
        theDataHandler = passedHandler;
    }     
     
     //===============================================================
    /**
     *   Handle the message.
     *
     * @param msgBytes
    */
    abstract public void handleMessage( byte[] msgBytes );
    
    //===========================================================================
    /**
     *  Handles the passed message with the correct manager
     * 
     * @param theCommManager
     * @param msgType
     * @param msgBytes  
     * @param dstId  
     */
    public static void routeMessage( PortManager theCommManager, byte msgType, int dstId, byte[] msgBytes ) {
        
        DataManager aManager = null;
        switch( msgType ){
            case Message.CONTROL_MESSAGE_TYPE:
                aManager = ControlMessageManager.getControlMessageManager();
                break;
            case Message.PROCESS_MESSAGE_TYPE:
                aManager = ShellMessageManager.getShellMessageManager();
                break;
            case Message.SOCKS_MESSAGE_TYPE:
                aManager = SocksMessageManager.getSocksMessageManager();
                break;
            case Message.FILE_MESSAGE_TYPE:
                aManager = FileMessageManager.getFileMessageManager();
                break;
            default:
                break;
                
        }
        if( aManager != null ){
            aManager.handleMessage( msgBytes );                                
        }
    }
    
    //===========================================================================
    /**
     *  Creates the port router
     * @param passedManager
     * @param encrypted
     * @param passedPort
     * @return 
     * @throws java.io.IOException
     */
    public synchronized static PortRouter createPortRouter( PortManager passedManager, int passedPort, boolean encrypted ) throws IOException{
        PortRouter aPR = passedManager.getPortRouter( passedPort );
        if( aPR == null ){
            aPR = new ClientPortRouter( passedManager, encrypted );
            passedManager.setPortRouter( passedPort, aPR);            
        }
        return aPR;
    }
    
    //===========================================================================
    /**
     *  Shutdown the handler 
     */
    public void shutdown() {
        theDataHandler.shutdown();         
    }
    
      //===============================================================
    /**
     *  Queues the byte array to be sent
     * @param passedManager
     * @param passedMessage
     */
    public static void send( PortManager passedManager, Message passedMessage ) {
        
        //Send the message out
        StubConfig aConf = StubConfig.getConfig();
        int thePort = aConf.getSocketPort();
        PortRouter thePR = passedManager.getPortRouter( thePort );  
        if( thePR != null ){            
            //Get destination id and send
            int destClientId = passedMessage.getDestHostId();
            thePR.queueSend( passedMessage.getBytes(), destClientId, passedMessage.getCancelId() );
        }
         
    }
    
}
