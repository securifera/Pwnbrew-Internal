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

package pwnbrew.network.shell;

import java.util.concurrent.Executor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import pwnbrew.misc.Constants;

/**
 *
 *  
 */
public class Powershell extends Shell {
    
    private static final String[] POWERSHELL_EXE_STR = new String[]{"powershell", "-ExecutionPolicy", "ByPass", "-"};
    private static final String encoding = "UTF-8";
    private static final String promptCmd = "prompt\r\n";
    private static final String PROMPT_REGEX = "PS [a-zA-Z]:(\\\\|(\\\\[^\\\\/:*\"<>|]+)+)>";
    private static final Pattern PROMPT_PATTERN = Pattern.compile(PROMPT_REGEX);

    // ==========================================================================
    /**
     *  Constructor
     * 
     * @param passedExecutor
     * @param passedListener 
     */
    public Powershell(Executor passedExecutor, ShellListener passedListener) {
        super(passedExecutor, passedListener);
    }
    
     // ==========================================================================
    /**
     * Handles the bytes read
     *
     * @param passedId
     * @param buffer the buffer into which the bytes were read
     */
    @Override
    public void handleBytesRead( int passedId, byte[] buffer ) {
           
        
        //Add the bytes to the string builder
        switch( passedId ){
            case Constants.STD_OUT_ID:
                synchronized(theStdOutStringBuilder) {
                    theStdOutStringBuilder.append( new String( buffer ));
                    String tempStr = theStdOutStringBuilder.toString();
                                        
                    //Set the prompt
                    if( !promptFlag ){
                        
                        //See if it matches the prompt
                        Matcher m = PROMPT_PATTERN.matcher(tempStr);
                        if( m.find()){
                            int matchEnd = m.end();
                            String aStr = tempStr.substring(0, matchEnd);
                            buffer = aStr.getBytes();
                            promptFlag = true;
                        } 
                        
                    } else {
                        return;
                    }
                    
                    //Reset the string builder
                    theStdOutStringBuilder.setLength(0);
                    
                }
                break;
        }          
        
        //Send to the runner pane
        super.handleBytesRead(passedId, buffer);        

    }
    
    // ==========================================================================
    /**
     *  Get the command string
     * 
     * @return 
     */
    @Override
    public String[] getCommandStringArray() {                
        return POWERSHELL_EXE_STR;
    }

    // ==========================================================================
    /**
     *  Get character encoding.
     * 
     * @return 
     */
    @Override
    public String getEncoding() {
        return encoding;
    }
    
    // ==========================================================================
    /**
     * 
     * @return 
     */
    @Override
    public String toString(){
        return "Powershell";
    }
    
     // ==========================================================================
    /**
     *  Get the string to append to the end of every command
     * 
     * @return 
     */
    @Override
    public String getInputTerminator(){
        String aStr = super.getInputTerminator();
        return aStr.concat(promptCmd);    
    }
    
    // ==========================================================================
    /**
     *  Get the string to run on the shell startup
     * 
     * @return 
     */
    @Override
    public String getStartupCommand(){
        return promptCmd;    
    }
}
