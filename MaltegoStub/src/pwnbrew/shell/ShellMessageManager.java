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
 *  ShellMessageManager.java
 *
 */

package pwnbrew.shell;

import pwnbrew.MaltegoStub;
import pwnbrew.manager.PortManager;
import pwnbrew.manager.DataManager;

/**
 *
 *  
 */
public class ShellMessageManager extends DataManager {

    private static ShellMessageManager theShellMsgManager;
    private static final String NAME_Class = ShellMessageManager.class.getSimpleName();
    
    //The map for relating shells to their ids
    private Shell theShell = null;
    
    //===========================================================================
    /*
     *  Constructor
     */
    private ShellMessageManager( PortManager passedCommManager ) {
        
        super(passedCommManager);        

        //Create the handler
        ShellMessageHandler theMessageHandler = new ShellMessageHandler( this );
        theMessageHandler.start();
       
        //Set the data handler
        setDataHandler(theMessageHandler);
    }  
       
    // ==========================================================================
    /**
     *   Gets the ShellMessageManager
     * @return 
     */
    public synchronized static ShellMessageManager getShellMessageManager(){
        if( theShellMsgManager == null )
            theShellMsgManager = new ShellMessageManager( MaltegoStub.getMaltegoStub() );
        return theShellMsgManager;
    }
    
    //===============================================================
    /**
     *   Send the message out the given channel.
     *
     * @param msgBytes
    */
    @Override
    public void handleMessage( byte[] msgBytes ) {        
        theShellMsgManager.getDataHandler().processData(msgBytes);        
    }
    
//    //===============================================================
//    /**
//     *   Send the message out the given channel.
//     *
//     * @param passedMessage
//     * @throws java.io.IOException
//    */
//    public void send( ProcessMessage passedMessage ) throws IOException {
//
//        
//        ByteBuffer aByteBuffer;
//        int msgLen = passedMessage.getLength();
//        aByteBuffer = ByteBuffer.allocate( msgLen );
//        passedMessage.append(aByteBuffer);
//
//        
//        //Get the port router
//        PortRouter thePR = thePortManager.getPortRouter(  StubConfig.getConfig().getSocketPort() );
//                
//        //Queue the message to be sent
//        thePR.queueSend( Arrays.copyOf( aByteBuffer.array(), aByteBuffer.position()), passedMessage.getDestHostId() );
////        DebugPrinter.printMessage(this, "Queueing " + passedMessage.getClass().getSimpleName() + " message");
//          
//    }
    
    //===========================================================================
    /*
     *  Returns the data handler
    */
    @Override
    public ShellMessageHandler getDataHandler() {
        return (ShellMessageHandler)theDataHandler;
    }   

    //===========================================================================
    /*
     *  Return the shell
     */
    public Shell getShell() {
        return theShell;
    }    
    
    //===========================================================================
    /*
     *  Set the shell
     */
    public void setShell(Shell aShell) {
        theShell = aShell;
    }
    
}
