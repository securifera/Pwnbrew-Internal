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
* SelectionRouter.java
*
* Created on June 9, 2013, 2:58:59 PM
*/

package pwnbrew.selector;

import pwnbrew.log.RemoteLog;
import java.io.IOException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import pwnbrew.utilities.ManagedRunnable;

/**
 *
 *  
 */
public class SelectionRouter extends ManagedRunnable{

    private final Selector theSelector;
    private final Object gate = new Object();

    private static final String NAME_Class = SelectionRouter.class.getSimpleName();

    // =========================================================================
    /*
     *  Constructor
     */
    public SelectionRouter( Executor passedExecutor ) throws IOException {
        super( passedExecutor );
        theSelector = Selector.open();
    }

    @Override
    public void go() {

        //Shutdown if requested 
        while (!shutdownRequested) {
            dispatch();
        }

        //Close the selector
        try {
            theSelector.close();
        } catch (IOException ex) {
            RemoteLog.log(Level.SEVERE, NAME_Class, "run()", ex.getMessage(), ex );
        }
    }

    //===============================================================
    /**
    *  Main selector loop
    */
    private void dispatch() {

        try {
            if(theSelector.select() != 0){

                for (Iterator<SelectionKey> i = theSelector.selectedKeys().iterator(); i.hasNext(); ) {
                    
                    final SelectionKey sk = (SelectionKey)i.next();
                    i.remove();
                    final Selectable theSelectHandler = (Selectable)sk.attachment();
                    theSelectHandler.handle(sk);
                }  
                
            } else {
                
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
                    ex = null;
                }
            }
            synchronized (gate) { }

        } catch (IOException ex) {
            
            //Log the message if it doesn't have to do with the channel closing
            if( !ex.getMessage().toUpperCase().contains("CLOSED") ) {
                RemoteLog.log(Level.SEVERE, NAME_Class, "run()", ex.getMessage(), ex );
            }
            
        } catch (CancelledKeyException ex ){
            ex = null;
        }
    }

    //===============================================================
    /**
    *  Registers the channel with the selector
     * @param passedChannel
     * @param h
     * @param ops
     * @return 
     * @throws java.io.IOException 
    */
    public SelectionKey register (SelectableChannel passedChannel, int ops, Selectable h) throws IOException {
        
        SelectionKey theKey = null;
        if( passedChannel != null ){
            synchronized (gate) {
                theSelector.wakeup();
                theKey = passedChannel.register (theSelector, ops, h);
            }
        }
        return theKey;
    }

    //===============================================================
    /**
    *  Cancels the key for the passed channel
     * @param passedChannel
    */
    public void unregister(SelectableChannel passedChannel) {
        SelectionKey sk = passedChannel.keyFor(theSelector);
        synchronized (gate) {
            if(sk != null){
                sk.cancel();
            }
        }
    }
    
     //===============================================================
    /**
    *  Changes the operation for a selection key
    *
    *  Example of how to negate an interest op
    *       theSelKey.interestOps( theSelKey.interestOps() & (~SelectionKey.OP_CONNECT) );
     * @param passedChannel
     * @return 
    */
    @SuppressWarnings("ucd")
    public int interestOps( SelectableChannel passedChannel ) {
        
        int retVal = 0;
        if( passedChannel != null ){
            SelectionKey sk = passedChannel.keyFor(theSelector);
            if(sk != null){
                synchronized (gate) {
                    retVal = sk.interestOps();
                }
            }
        }
        return retVal;
    }

    //===============================================================
    /**
    *  Changes the operation for a selection key
    *
    *  Example of how to negate an interest op
    *       theSelKey.interestOps( theSelKey.interestOps() & (~SelectionKey.OP_CONNECT) );
     * @param passedChannel
     * @param passedOp
     * @return 
    */
    public boolean changeOps(SelectableChannel passedChannel, int passedOp) {

        boolean retVal = false;

        if( passedChannel != null ){
            SelectionKey sk = passedChannel.keyFor(theSelector);
            if(sk != null){
                synchronized (gate) {
                    theSelector.wakeup();

                    try {
                        sk.interestOps(passedOp);
                        retVal = true;
                    } catch (CancelledKeyException ex){
                        ex = null;
                    }
                }
            }
        }

        return retVal;
    }


}/* END CLASS SelectionRouter */
