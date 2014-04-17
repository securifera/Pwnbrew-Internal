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
 * LockingThread.java
 *
 */

package pwnbrew.concurrent;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import pwnbrew.misc.ManagedRunnable;

/**
 *
 *  
 */
public class LockingThread extends ManagedRunnable {

    private final ReentrantLock theLock = new ReentrantLock();
 
    public static final int LOCK = 1;
    private static final int UNLOCK = 2;
    
    private final Map<LockListener, Integer> lockListenerMap = new LinkedHashMap<>();

    //===============================================================
    /**
    *  Constructor
     * @param passedExecutor
    */
    public LockingThread(Executor passedExecutor) {
        super(passedExecutor);
    }   
    
    //===============================================================
    /**
     * Main thread loop
    */
    @Override
    public void go() {

        int retOp;
        while(!shutdownRequested){

            waitToBeNotified();
            
            //Loop until emtpy
            while( true ){
                
                Entry<LockListener, Integer> anEntry;
                synchronized( lockListenerMap ){
                    if( lockListenerMap.isEmpty() ){
                        break;
                    } else {
                        anEntry = lockListenerMap.entrySet().iterator().next();                        
                    }
                }

                //Switch on the operation
                switch( anEntry.getValue() ){
                    case LOCK:
                        retOp = acquireTransferLock();
                        synchronized( lockListenerMap ){
                            lockListenerMap.remove(anEntry.getKey());
                            if( retOp != LOCK ){
                                lockListenerMap.put( anEntry.getKey(), anEntry.getValue());
                            }
                        }

                        //Send the result 
                        anEntry.getKey().lockUpdate(retOp);
                        break;

                    case UNLOCK:

                        releaseTransferLock();
                        synchronized( lockListenerMap ){
                            lockListenerMap.remove(anEntry.getKey());
                        }
                        
                        break;

                    default:
                        break;
                }
            
            }
            
        }
    }

    //===============================================================
    /**
     * Returns whether the transfer lock was acquired
     * @return
    */
    private int acquireTransferLock(){

        boolean lockAcquired = false;
        int lockOp = UNLOCK;

        //Acquire the lock
        if(!theLock.isLocked()){

            while(!lockAcquired && !shutdownRequested){
                try {
                    lockAcquired = theLock.tryLock(0, TimeUnit.SECONDS);
                } catch (InterruptedException ex) {
                    continue;
                }

                if(!lockAcquired && !shutdownRequested){
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        continue;
                    }
                }
            }

            //If the lock was acquired return lock
            if(lockAcquired)
            lockOp = LOCK;

        }

        return lockOp;
    }

    //===============================================================
    /**
     * Releases the transfer lock
     * @return
    */
    private int releaseTransferLock() {

        //If it is locked, unlock it
        if(theLock.isLocked()){
            theLock.unlock();
        }
        return UNLOCK;

    }

    //===============================================================
    /**
     * Tells the thread to acquire the transfer lock
     * @param theLockListener
    */
    public void lock( LockListener theLockListener ) {

        synchronized( lockListenerMap ){
            lockListenerMap.put(theLockListener, LOCK);
        }
        beNotified();
        
    }

    //===============================================================
    /**
     * Releases the transfer lock
    */
    public void unlock() {
        
        synchronized( lockListenerMap ){
            lockListenerMap.put(null, UNLOCK);
        }
        beNotified();
    }

}
