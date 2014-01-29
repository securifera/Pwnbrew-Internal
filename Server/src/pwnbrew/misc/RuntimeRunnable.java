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
 * RuntimeRunnable.java
 *
 * Created on July 20, 2013
 */

package pwnbrew.misc;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import pwnbrew.logging.Log;
import pwnbrew.output.StreamCollector;

/**
 *
 *  
 */
@SuppressWarnings("ucd")
public class RuntimeRunnable implements Runnable {

    private final String[] theCommand;
    private static final String NAME_Class = RuntimeRunnable.class.getSimpleName();
    private final StreamCollector theOutputReader = new StreamCollector();
    private final StreamCollector theErrReader = new StreamCollector();
    private int exitValue = 0;
    
    //===============================================================
    /**
     *  Constructor
     * @param passedCommand
    */
    public RuntimeRunnable( String[] passedCommand ) {
        theCommand = passedCommand;
    }
    
    //===============================================================
    /**
     * 
     */
    @Override
    public void run() {
        
        try {
            Process aProc = Runtime.getRuntime().exec(theCommand);
            OutputStream theirStdin = aProc.getOutputStream();
            try {
                theirStdin.close();
            } catch ( IOException ioe ) {
                ioe = null;
            }
            
            //Collect the data from stdout...
            theOutputReader.setInputStream( aProc.getInputStream() );
            Constants.Executor.execute(theOutputReader);
            
            //Collect the data from stderr...
            theErrReader.setInputStream( aProc.getErrorStream() );
            Constants.Executor.execute(theErrReader);
            
            exitValue = aProc.waitFor();           
            
        } catch (InterruptedException | IOException ex) {
           Log.log(Level.SEVERE, NAME_Class, "run()", ex.getMessage(), ex );
        }
        
    }
    
    //===============================================================
    /**
     *  Returns the standard output as a string.
     * @return 
    */
    public String getStdOut() {
        return theOutputReader.getString();
    }
    
     //===============================================================
    /**
     *   Returns the standard error as a string.
     * @return 
    */
    public String getStdErr() {
        return theErrReader.getString();
    }

    //===============================================================
    /**
     *  Return the exit value from the run.
     * @return 
    */
    public int getExitValue() {
        return exitValue;
    }
    

}
