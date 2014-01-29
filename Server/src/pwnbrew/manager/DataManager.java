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
 *  Created on Jun 10, 2013 8:24:31 PM
 */

package pwnbrew.manager;

import java.io.IOException;
import pwnbrew.network.control.ControlMessageManager;
import java.util.HashMap;
import java.util.Map;
import pwnbrew.misc.DebugPrinter;
import pwnbrew.network.PortWrapper;
import pwnbrew.network.DataHandler;
import pwnbrew.network.Message;
import pwnbrew.network.PortRouter;
import pwnbrew.network.ServerPortRouter;
import pwnbrew.network.file.FileMessageManager;
import pwnbrew.shell.ShellMessageManager;
/**
 *
 *  
 */
abstract public class DataManager {    

    protected DataHandler theDataHandler;
    protected final CommManager theCommManager;
    protected int operatingPort;
    
    private static final Map<Integer, PortWrapper> thePortWrapperMap = new HashMap<>();
    private transient static final String NAME_Class = DataManager.class.getSimpleName();
    

    //===========================================================================
    /**
     *  Constructor
     * @param passedProvider
     */
    public DataManager( CommManager passedProvider ) {
        theCommManager = passedProvider ;
    }  
    
    //===========================================================================
    /**
     *  Get the the port wrapper.
     * 
     * @param passedPort 
     * @return  
     */
    public static PortWrapper getPortWrapper( int passedPort ) {
        return thePortWrapperMap.get( passedPort );
    }
    
     //===========================================================================
    /**
     *  Set the the port wrapper.
     * 
     * @param passedPort 
     * @param passedWrapper 
     */
    public static void setPortWrapper( int passedPort, PortWrapper passedWrapper) {
        thePortWrapperMap.put( passedPort, passedWrapper );
    }
    
    //===========================================================================
    /*
     *  Returns the comm manager
     */
    public CommManager getCommManager(){
        return theCommManager;
    }
    //===========================================================================
    /*
     *  Returns whether the passed type is a known message type
     */
    public static boolean isValidType(byte type) {
        
        boolean retVal = true;
        switch(type){
            
            case Message.CONTROL_MESSAGE_TYPE:
                break;
            case Message.PROCESS_MESSAGE_TYPE:
                break;
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
     *  Handle the passed message with the correct manager
     * @param theCommManager
     * @param msgType
     * @param msgBytes  
     * @throws java.io.IOException  
     */
    public static void routeMessage( CommManager theCommManager, byte msgType, byte[] msgBytes ) throws IOException {
        
        //Pass the message to the right handler
        DataManager aManager = null;
        switch( msgType ){            
            case Message.CONTROL_MESSAGE_TYPE:
                
                aManager = ControlMessageManager.getControlMessageManager();
                if( aManager == null){
                    aManager = ControlMessageManager.initialize(theCommManager);
                }
                break;
            case Message.PROCESS_MESSAGE_TYPE:
                aManager = ShellMessageManager.getShellMessageManager();
                if( aManager == null){
                    aManager = ShellMessageManager.initialize(theCommManager);
                }
                break;
            case Message.FILE_MESSAGE_TYPE:
                aManager = FileMessageManager.getFileMessageManager();
                if( aManager == null){
                    aManager = FileMessageManager.initialize(theCommManager);
                }
                break;            
            default:
                break;
                           
        }
        
        //Handle it
        if( aManager != null ){
            aManager.handleMessage( msgBytes );
        } else {
            DebugPrinter.printMessage(DataManager.class.getSimpleName(), "No manager for bytes");
        }
    
    }

    //===========================================================================
    /**
     *  Set the operating port
     * @param passedPort 
     */
    public void setPort( int passedPort ) {
        operatingPort = passedPort;
    }
    
    //===========================================================================
    /**
     *  Get the operating port
     
     * @return 
     */
    public int getPort() {
        return operatingPort;
    }
    
    //===========================================================================
    /**
     *  Shutdown the handler 
     */
    public void shutdown() {
        theDataHandler.shutdown();
    }
    
    //===========================================================================
    /**
     *  Creates the port router
     * @param passedManager
     * @param encrypted
     * @param passedPort
     * @throws java.io.IOException
     */
    public static synchronized void createPortRouter( CommManager passedManager, int passedPort, boolean encrypted ) throws IOException{
        PortRouter aPR = passedManager.getPortRouter( passedPort );
        if( aPR == null ){
            aPR = new ServerPortRouter( passedManager, encrypted );
            passedManager.setPortRouter( passedPort, aPR);            
        }
    }
    
}
