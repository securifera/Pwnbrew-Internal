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
* DebugPrinter.java
*
*/

package pwnbrew.misc;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import pwnbrew.log.LoggableException;

/**
 * 
 */
public class DebugPrinter implements Runnable {
    
    private static volatile boolean shutdownRequested = false;
    private static final Queue<String> queue = new LinkedList<>();
    private static DebugPrinter staticSelf = null;
    private static volatile boolean enabled = false;
    
     //Session's start date/time...
    private static final String FORMAT_SessionDateTime
            = new StringBuilder( "yyyyMMdd" ).append( "_" ).append( "HHmmss" ).toString();

     //===============================================================
     /**
     *  Queues a message to be handled
     *
     * @param passedBool
     */
    public static synchronized void enable( boolean passedBool ){
        enabled = passedBool;
    }
    
    //===============================================================
     /**
     *  Queues a message to be handled
     *
     * @param sourceClassName
     * @param passedMessage
     */
      public static synchronized void printMessage( String sourceClassName, String functionName, String passedMessage, Throwable thrown ) { 

        if( staticSelf == null && enabled ){
            staticSelf = getInstance();
            Constants.Executor.execute(staticSelf);
        }

        if( staticSelf != null && passedMessage != null){
            
            String debugMsg = passedMessage;
            if( thrown != null ){
                
                if( thrown instanceof LoggableException )
                    thrown = ((LoggableException)thrown).getException();                
                
                StringWriter errors = new StringWriter();
                thrown.printStackTrace( new PrintWriter(errors) );

                StringBuilder aSB = new StringBuilder()
                    .append(passedMessage)
                    .append("\n")
                    .append(errors.toString());
                debugMsg = aSB.toString();
            }

            //Add the message to the queue to be handled
            synchronized(queue) {
                queue.add(new StringBuilder().append(sourceClassName).append("-").append(functionName).append(": ").append(debugMsg).toString());
                queue.notifyAll();
            }
        }
    }


    //===============================================================
     /**
     *  Returns an instance of a debug printer
     *
     * @param passedMessage
    */
    private static DebugPrinter getInstance() {
       return new DebugPrinter();
    }

    //===============================================================
    /**
     *  Main thread loop
     *
    */
    @Override
    public void run() {

       String currentMsg;

       while(!shutdownRequested) {
           // Wait for next message to become available
           synchronized(queue) {
              while(queue.isEmpty()) {
                 try {
                    queue.wait();
                 } catch (InterruptedException e) {
                    //Shutdown the handler if interrupted and the shutdown flag was triggered
                    if(shutdownRequested) return;
                 }

                 //If shutdown is requested, exit
                 if(shutdownRequested) return;

              }
              currentMsg = (String)queue.poll();
              if(currentMsg != null){
                 System.err.println( new StringBuilder()
                    .append( new SimpleDateFormat( FORMAT_SessionDateTime).format(new Date()))
                    .append("- ").append(currentMsg).toString());
              }
           }
       }
    }

    //===============================================================
    /**
     *  Shutdown the thread
     *
     * @return 
    */
    public static boolean shutdown() {

        //If there is a thread running
        if(staticSelf != null){
            synchronized(queue){
                shutdownRequested = true;
                queue.notifyAll();
            }
        }

        return true;
    }

}/* END CLASS DebugPrinter */
