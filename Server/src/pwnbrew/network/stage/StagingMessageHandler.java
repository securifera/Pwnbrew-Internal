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


package pwnbrew.network.stage;

/*
* StagingMessageHandler.java
*
* Created on June 7, 2013, 11:41:21 PM
*/

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import pwnbrew.exception.RemoteExceptionWrapper;
import pwnbrew.log.Log;
import pwnbrew.log.LoggableException;
import pwnbrew.manager.DataManager;
import pwnbrew.network.DataHandler;
import pwnbrew.network.Message;
import pwnbrew.network.PortRouter;

/**
 *
 *  
 */
@SuppressWarnings("ucd")
public class StagingMessageHandler extends DataHandler {

    private static final String NAME_Class = StagingMessageHandler.class.getSimpleName();
    private final Queue<StageMessage> incomingMsgQueue = new LinkedList<>();
    
   
    //===============================================================
     /**
     * ControlMessageHandler constructor
     *
     * @param passedComm
      */
    StagingMessageHandler( DataManager passedListener ) {
        super( passedListener);
    }
    
    //===============================================================
    /**
     *  Returns the data manager
     * 
     * @return 
    */
   @Override
    public StagingMessageManager getDataManager(){
        return (StagingMessageManager)theDataManager;
    }
       
    //===============================================================
     /**
     *  Handles incoming messages
     *
     * @param theMessage
     * @return 
    */
    private void handleIncoming( final Message aMessage ) {  
        theExecutor.execute( new Runnable() {

            @Override
            public void run() {
                try { 
                    aMessage.evaluate( theDataManager.getPortManager() );
                } catch (LoggableException ex) {
                    Log.log(Level.INFO, NAME_Class, "processData()", ex.getMessage(), ex );
                }
            }
        });
    }


    //===============================================================
    /**
    *  Queues a control message to be handled
    *
    * @param passedMessage
    */
    private void processIncoming( StageMessage passedMessage) {

        //Copy over the bytes
        if(passedMessage != null){

            synchronized(incomingMsgQueue) {
                incomingMsgQueue.add(passedMessage);
            }
            beNotified();

        }
    }

    //===============================================================
    /**
     *  Process the message
     * 
     * @param srcPortRouter
     * @param passedByteArray 
     * @throws pwnbrew.exception.RemoteExceptionWrapper 
     */
    @Override
    public void processData( PortRouter srcPortRouter, byte[] passedByteArray ) throws RemoteExceptionWrapper {
      
        try {
            
            StageMessage aMessage = StageMessage.getMessage( ByteBuffer.wrap( passedByteArray ) );        
            processIncoming(aMessage);

        } catch (LoggableException | IOException ex) {
            Log.log(Level.INFO, NAME_Class, "processData()", ex.getMessage(), ex );
        }
        
    }

    //===============================================================
    /**
     *  Main message loop
     */
    @Override
    protected void go() {
        
        StageMessage aMessage;

        while(!shutdownRequested) {


            waitToBeNotified();

            //Waits until a msg comes in
            while( !isIncomingEmpty() ){


                // Handle the next message
                synchronized(incomingMsgQueue) {
                    aMessage = (StageMessage)incomingMsgQueue.poll();
                }

                //Handles a message if there is one
                if(aMessage != null){
                    handleIncoming(aMessage);
                }

            }
        }
    }

    
    //===============================================================
    /**
    *  Checks if the incoming queue is empty
    *
     * @return 
    */
    public boolean isIncomingEmpty(){

        boolean retVal;

        synchronized(incomingMsgQueue) {
            retVal = incomingMsgQueue.isEmpty();
        }
        return retVal;
    }


}/* END CLASS ControlMessageHandler */
