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
* ShellMessageHandler.java
*
*/

package pwnbrew.shell;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import pwnbrew.MaltegoStub;
import pwnbrew.functions.Function;
import pwnbrew.functions.ToShell;
import pwnbrew.log.LoggableException;
import pwnbrew.manager.PortManager;
import pwnbrew.misc.Constants;
import pwnbrew.misc.DebugPrinter;
import pwnbrew.network.DataHandler;
import pwnbrew.network.shell.messages.ProcessMessage;
import pwnbrew.network.shell.messages.StdErrMessage;
import pwnbrew.network.shell.messages.StdOutMessage;


/**
 *
 *  
 */
public class ShellMessageHandler extends DataHandler {

    private final Queue<byte[]> recByteQueue = new LinkedList<>();
    private volatile boolean sendFileFlag = false;

    private static final String NAME_Class = ShellMessageHandler.class.getSimpleName();

    //===============================================================
     /**
     * ShellMessageHandler constructor
     *
     * @param passedListener
    */
    public ShellMessageHandler( ShellMessageManager passedListener ) {        
        super(passedListener);
    }   

    //===========================================================================
    /**
     *  Processes the most recent 
     * 
     * @param passedByteArray 
     */
    @Override
    public void processData( byte[] passedByteArray ) {
       
        if(passedByteArray.length > 0){

            //Copy over the bytes
            byte[] theByteArray = Arrays.copyOf(passedByteArray, passedByteArray.length);
     
            //Add the message to the queue to be handled
            synchronized(recByteQueue) {
                recByteQueue.add(theByteArray);
                recByteQueue.notifyAll();
            }
        }
        
    }

    //===============================================================
    /**
    *  The main loop for the thread execution.
    *
    */
    @Override
    public void go() {

        byte[] currByteArray;     
        while(!shutdownRequested) {

            // Wait for next message to become available
            synchronized(recByteQueue) {
                
                while(recByteQueue.isEmpty() && sendFileFlag == false) {
                    try {
                        recByteQueue.wait();
                    } catch (InterruptedException ex) {
                        ex = null;
                    }

                    //If a shutdown is requested then return immediately
                    if(shutdownRequested) return;

                }
                currByteArray = (byte[]) recByteQueue.poll();
            }

            //Handle the byte array
            if( currByteArray != null ){
                receiveByteArray( ByteBuffer.wrap(currByteArray) );
            }
        }
            
    }

    //===============================================================
    /**
     *  Receive the byte array
     * 
     * @param currByteArray 
    */    
    private void receiveByteArray( ByteBuffer currByteArray) {
        
        try {
            
            ProcessMessage aMessage = ProcessMessage.getMessage( currByteArray );
            byte[] msgBytes = aMessage.getMsgBytes();  
            
            ShellMessageManager theManager = getDataManager();
            PortManager theCommManager = theManager.getCommManager();
            if( theCommManager instanceof MaltegoStub ){
           
                MaltegoStub maltegoManager = (MaltegoStub)theCommManager;
                Function theFunction = maltegoManager.getFunction();
                if( theFunction instanceof ToShell ){
                    
                    ToShell toShellFunc = (ToShell)theFunction;
                    Shell theShell = toShellFunc.getShell();
                    if( theShell != null ){
                        if( aMessage instanceof StdOutMessage ) { 
                            theShell.handleBytesRead( Constants.STD_OUT_ID, msgBytes );
                        } else if( aMessage instanceof StdErrMessage ) {
                            theShell.handleBytesRead( Constants.STD_ERR_ID, msgBytes );    
                        }
                    }
                }
            }           
            
        } catch (LoggableException ex) {
            DebugPrinter.printMessage( NAME_Class, "receiveByteArray", ex.getMessage(), ex);   
        }
        
    }
    
    //===============================================================
    /**
     *  Returns the data manager
     * 
     * @return 
    */
    @Override
    public ShellMessageManager getDataManager(){
        return (ShellMessageManager)theDataManager;
    }

}
