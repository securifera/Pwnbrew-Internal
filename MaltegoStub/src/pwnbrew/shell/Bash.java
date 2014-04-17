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
 *  Bash.java
 *
 */

package pwnbrew.shell;

import java.util.concurrent.Executor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import pwnbrew.misc.Constants;
import pwnbrew.misc.DebugPrinter;
import pwnbrew.output.StreamReceiver;
import static pwnbrew.shell.Shell.NAME_Class;

/**
 *
 *  
 */
public class Bash extends Shell {
    
    private static final String[] BASH_EXE_STR = new String[]{ "/bin/bash", "-i"};
    private static final String encoding = "UTF-8";
    //private static final String PROMPT_REGEX_BASH = "\\w+@\\w+:\\S.*[$#]";
    private static final String PROMPT_REGEX_BASH = "\\x1b.*[$#]";
    private static final Pattern PROMPT_PATTERN = Pattern.compile(PROMPT_REGEX_BASH);
   
    
    // ==========================================================================
    /**
     *  Constructor
     * 
     * @param passedExecutor
     * @param passedListener 
     */
    public Bash(Executor passedExecutor, ShellListener passedListener) {
        super(passedExecutor, passedListener);
        setStderrRedirectFlag(true);
    }
    
    // ==========================================================================
    /**
     *  Get the command string
     * 
     * @return 
     */
    @Override
    public String[] getCommandStringArray() {                
        return BASH_EXE_STR;
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

        super.handleBytesRead(passedId, buffer);
        
        //Get runner pane
        StreamReceiver theReceiver = theListener.getStreamReceiver(); 
        String aStr = null;
        
        
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
                            
                            //Split on funky byte
                            String[] theStrArr = m.group().split("\u0007");
                            
                            if(theStrArr.length > 0 ){
                                //Construct the prompt
                                int promptIndex = 0;
                                if( theStrArr[promptIndex].isEmpty() ){
                                    promptIndex++;
                                }

                                //Get the prompt
                                String[] promptArr = theStrArr[promptIndex].split(";");
                                if(promptArr.length > 1){

                                    //Get first part of prompt
                                    String prompt = promptArr[1].trim();
                                    
                                    //Get prompt terminator
                                    String termSec = theStrArr[ theStrArr.length - 1];
                                    aStr = prompt.concat( termSec.substring( termSec.length() - 1));
                                    
                                    promptFlag = true;
                                    setShellPrompt( aStr );                                 

                                }     
                            }                                                
                                                        
                        } else{
                            aStr = tempStr;
                        }
                        
                    }   
                    
                    //Reset the string builder
                    theStdOutStringBuilder.setLength(0);
                    
                }
                break;
            default:
                DebugPrinter.printMessage( NAME_Class, "routeMessage", "Unrecognized stream id.", null);   
                break;
        }          
        
        //Send to the runner pane
        if( aStr != null ){
            theReceiver.handleStreamBytes(Constants.STD_OUT_ID, aStr);
        }

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
        return "Bash";
    }
}