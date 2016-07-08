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
 * RemoteLog.java
 *
 * Created on June 11, 2013, 8:12:34 PM
 */

package pwnbrew.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import pwnbrew.manager.DataManager;
import pwnbrew.network.control.ControlMessageManager;
import pwnbrew.utilities.DebugPrinter;
import pwnbrew.network.control.messages.LogMsg;

/**
 *  
 */
final public class RemoteLog {
    
    // ==========================================================================
    /**
     * Logs a message, specifying source class and method, with associated {@link Throwable}
     * information at the given {@link Level}.
     *
     * @param	level	the {@code Level} at which the message is to be logged
     * @param sourceClass name of the class that issued the logging request
     * @param sourceMethod name of the method that issued the logging request
     * @param message the message to log
     * @param thrown the {@code Throwable}
     */
    public static synchronized void log( Level level, String sourceClass, 
            String sourceMethod, String message, Throwable thrown ) {

        StringWriter errors = new StringWriter();
        if( thrown != null )
            thrown.printStackTrace( new PrintWriter(errors) );
        StringBuilder aSB = new StringBuilder()
                .append(message)
                .append("\n")
                .append(errors.toString());
        ControlMessageManager aCMManager = ControlMessageManager.getControlMessageManager();
        if( aCMManager != null ){ 
            
            LogMsg aMsg = new LogMsg( aSB.toString() );
            DataManager.send(aCMManager.getPortManager(), aMsg);
            
        }
        DebugPrinter.printMessage(RemoteLog.class.getSimpleName(), aSB.toString());
        
        //Print to screen if debugging
        DebugPrinter.printMessage( new StringBuilder().append(sourceClass).append(".")
                .append(sourceMethod).toString(), message);

    }  

}
