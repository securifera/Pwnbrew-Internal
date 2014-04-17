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
 * JobUpdater.java
 *
 * Created on Nov 10, 2013, 10:11 PM
 */

package pwnbrew.gui.remote.job;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.concurrent.locks.ReentrantLock;
import javax.swing.Timer;

/**
 *
*/
public class JobUpdater extends Observable implements ActionListener {

    private Timer repaintTimer = null;
    private final ReentrantLock counterLock = new ReentrantLock();
    private Integer repaintTimerCounter = 0;
   

    //========================================================================
    /**
    * Constructor
    */
    public JobUpdater() {
        repaintTimer = new Timer(120, this) ;
    }

     //===============================================================
    /**
     *  Starts a timer that repaints the JDialog at a certain interval
    */
    synchronized void startRepaintTimer() {

        
        //Try to acquire the lock
        counterLock.lock();

        try {
           repaintTimerCounter++;
        } finally {
           counterLock.unlock();
        }

        if(!repaintTimer.isRunning()){
           repaintTimer.start();
        }
    }

    //===============================================================
    /**
     *  Stops the timer that repaints the JDialog at a certain interval
    */
    synchronized void decrementRepaintTimer() {

        boolean stopTimer = false;

        //Try to acquire the lock
        counterLock.lock();

        //Decrement the timer counter and if it equals zero then stop the timer
        try {

           if(repaintTimer.isRunning()){
              repaintTimerCounter--;
              if(repaintTimerCounter == 0){
                 stopTimer = true;
              }
           }

        } finally {
           counterLock.unlock();
        }

        //Stop the timer if applicable
        if(stopTimer){
           repaintTimer.stop();
        }
    }

    //===============================================================
    /**
    *  Tells the observer that something has changed
    */
    void taskChanged() {

        if(!repaintTimer.isRunning()){
            setChanged();
            notifyObservers();
        }
    }

    //===============================================================
    /**
    *  Tells the observer that something has changed
    * @param e
    */
    @Override
    public void actionPerformed(ActionEvent e) {
        setChanged();
        notifyObservers();
    }

}
