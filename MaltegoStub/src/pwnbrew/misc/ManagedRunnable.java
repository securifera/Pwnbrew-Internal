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
 * ManagedRunnable.java
 *
 */

package pwnbrew.misc;

import java.util.concurrent.Executor;

/**
 *
 *  
 */
abstract public class ManagedRunnable implements Runnable {
    
    protected volatile boolean shutdownRequested = false;
    private volatile boolean isRunning = false;
    private volatile boolean notified = false;
    protected final Executor theExecutor;

    //===============================================================
    /**
    *  Base constructor
     * @param passedExecutor
    */
    protected ManagedRunnable( Executor passedExecutor ) {
        theExecutor = passedExecutor;
    }   
   
    //===============================================================
    /**
    *  Starts the detector thread
    */
    public synchronized void start(){
        if( !isRunning ){           
            theExecutor.execute( this );
        }
    }
    
    //===============================================================
    /**
     *  Used for setting the run flags
    */
    @Override //Runnable
    final public void run() {
        
        //Set flag
        isRunning = true;
        
        //Run the main function
        go();
        
        //Set flag
        isRunning = false;
    
    }
    
    //===============================================================
    /**
     *  The main thread function
    */
    abstract protected void go();
    
    //===============================================================
    /**
    *  Shut down the detector
    */
    public synchronized void shutdown(){
        shutdownRequested = true;
        notifyAll();
    }
    
    
     // ==========================================================================
    /**
    * Causes the calling {@link Thread} to <tt>wait()</tt> until notified by
    * another.
    * <p>
    * <strong>This method most certainly "blocks".</strong>
     * @param anInt
    */
    protected synchronized void waitToBeNotified( Integer... anInt ) {

        while( !notified && !shutdownRequested) { //Until notified...

            try {
                
                //Add a timeout if necessary
                if( anInt.length > 0 ){
                    
                    wait( anInt[0]);
                    break;
                    
                } else {
                    wait(); //Wait here until notified
                }
                
            } catch( InterruptedException ex ) {
            }

        }
        notified = false;
    }
    
    //===============================================================
    /**
     * Notifies the thread
    */
    protected synchronized void beNotified() {
        notified = true;
        notifyAll();
    }
    
     // ==========================================================================
    /**
    *  Checks the shutdown flag.
    *
     * @return 
    */
    public synchronized boolean finished() {
        return shutdownRequested;
    }
    
    // ==========================================================================
    /**
    *  Check if the running flag has been set
    *
     * @return 
    */
    public boolean isRunning() {
        return isRunning;
    }
}
