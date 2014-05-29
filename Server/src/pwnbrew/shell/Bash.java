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

package pwnbrew.shell;

import java.util.concurrent.Executor;
import java.util.regex.Pattern;
import javax.swing.text.StyledDocument;
import pwnbrew.gui.panels.RunnerPane;

/**
 *
 *  
 */
public class Bash extends Shell {
    
//    private static final String[] BASH_EXE_STR = new String[]{ "/bin/bash", "-i"};
    private static final String[] BASH_EXE_STR = new String[]{ "python", "-c", "import pty;pty.spawn(\"/bin/bash\")"};
    private static final String encoding = "UTF-8";
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

        //Remove ansi codes
        String aStr = new String(buffer);
        if( !aStr.equals("\r\n")){
            aStr = aStr.replaceAll("\u001B\\[[;\\d]*[ -/]*[@-~]", "");
            aStr = aStr.replaceAll("\u001B.*\u0007", "");
            aStr = aStr.replaceAll("\u0007", "");
        }
        super.handleBytesRead(passedId, aStr.getBytes());
       
    }
    
    //===============================================================
    /**
     *
    */
    @Override
    public void printPreviousCommand(){
        
        RunnerPane thePane = theListener.getShellTextPane();
        StyledDocument theSD = thePane.getStyledDocument();
            
        //Set the new length
        int newLength = theSD.getLength();
        thePane.setCaretPosition( newLength ); 
        
        char escape = (byte)0x1b;
        sendInput( escape + "[A" );
    }
    
    //===============================================================
    /**
     *
    */
    @Override
    public void printNextCommand(){
        
        RunnerPane thePane = theListener.getShellTextPane();
        StyledDocument theSD = thePane.getStyledDocument();
        
        //Set the new length
        int newLength = theSD.getLength();
        thePane.setCaretPosition( newLength ); 
        
        char escape = 0x1b;
        sendInput( escape + "[B" );
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
