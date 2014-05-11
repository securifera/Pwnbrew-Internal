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
* CreateShell.java
*
* Created on June 7, 2013, 8:55:42 PM
*/

package pwnbrew.network.control.messages;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import pwnbrew.log.RemoteLog;
import pwnbrew.log.LoggableException;
import pwnbrew.manager.CommManager;
import pwnbrew.misc.Constants;
import pwnbrew.network.ControlOption;
import pwnbrew.network.shell.Shell;
import pwnbrew.network.shell.ShellMessageManager;

/**
 *
 *  
 */
public final class CreateShell extends ControlMessage{

    private String[] cmdString;
    private String encoding;
    private String startupCmd;
    private boolean redirectStderr = false;
    
    private static final byte OPTION_CMD_STRING = 5;
    private static final byte OPTION_ENCODING = 20;
    public static final byte OPTION_STARTUP_CMD = 22;
    public static final byte OPTION_REDIRECT_STDERR = 24;
    
    //Class name
    private static final String NAME_Class = CreateShell.class.getSimpleName();
    
    // ==========================================================================
    /**
     * Constructor
     *
     * @param passedId
    */
    public CreateShell( byte[] passedId ) {
        super( passedId );
    }
    
    //=========================================================================
    /**
     *  Sets the variable in the message related to this TLV
     * 
     * @param tempTlv 
     * @return  
     */
    @Override
    public boolean setOption( ControlOption tempTlv ){        
       
        boolean retVal = true;
        try {
            
            byte[] theValue = tempTlv.getValue();
            switch( tempTlv.getType()){
                
                case OPTION_CMD_STRING:
                    
                    String currStr = new String( theValue, "US-ASCII");
                    List<String> cmdLineStringList = new ArrayList<>();

                    //Split the strings out of the null byte delimited string
                    while(currStr.length() > 0){
                        int nullByteIndex = currStr.indexOf((byte)0x00);
                        cmdLineStringList.add(currStr.substring(0, nullByteIndex));
                        currStr = currStr.substring(nullByteIndex + 1, currStr.length());                   
                    }

                    cmdString = (String[])(cmdLineStringList.toArray(new String[cmdLineStringList.size()]));
                    
                    break;
                case OPTION_ENCODING:
                    encoding = new String( theValue, "US-ASCII");
                    break;
                case OPTION_STARTUP_CMD:
                    startupCmd = new String( theValue, "US-ASCII");
                    break;
                case OPTION_REDIRECT_STDERR:
                    if( theValue.length > 0 ){
                        byte aByte = theValue[0];
                        if( aByte == 0x1)
                            redirectStderr = true;
                    }
                    break;
                default:
                    retVal = false;
                    break;
            }
            
        } catch (UnsupportedEncodingException ex) {
            ex = null;
        }
        return retVal;
    }
    
    //===============================================================
    /**
    *   Performs the logic specific to the message.
    *
     * @param passedManager
    */
    @Override
    public void evaluate( CommManager passedManager ) { 
        
        try {
            
            ShellMessageManager aShellMsgManager = ShellMessageManager.getShellMessageManager();
            if( aShellMsgManager == null ){
                aShellMsgManager = ShellMessageManager.initialize( passedManager );
            }
            
            int theClientId = getSrcHostId();
            Shell aShell = aShellMsgManager.getShell( theClientId );
            if( aShell == null){            
            
                aShell = new Shell( Constants.Executor, passedManager, theClientId, encoding, 
                        cmdString, startupCmd, redirectStderr );
                aShell.start();                

                //Register the shell
                aShellMsgManager.setShell( theClientId, aShell );
            }
            
        } catch(IOException | LoggableException ex ){
            RemoteLog.log( Level.SEVERE, NAME_Class, "evaluate", ex.getMessage(), null);        
        }
        
    }


}/* END CLASS CreateShell */
