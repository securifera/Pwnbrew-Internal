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

package pwnbrew.network.control.messages;

import java.io.File;
import java.io.UnsupportedEncodingException;
import pwnbrew.ClientConfig;
import pwnbrew.concurrent.LockListener;
import pwnbrew.manager.PortManager;
import pwnbrew.network.ClientPortRouter;
import pwnbrew.utilities.DebugPrinter;
import pwnbrew.network.ControlOption;
import pwnbrew.network.control.ControlMessageManager;


/**
 *
 *  
 */
public final class TaskGetFile extends TaskStatus implements LockListener {

    private String hashFilenameStr;
    private int lockVal = 0;
    
    //Class name
    private static final String NAME_Class = TaskGetFile.class.getSimpleName();
     
    // ==========================================================================
    /**
     * Constructor
     *
     * @param msgId
    */
    public TaskGetFile(byte[] msgId ) { // NO_UCD (use default)
        super(msgId);
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
        if( !super.setOption(tempTlv)){
            try {
                byte[] theValue = tempTlv.getValue();
                switch( tempTlv.getType()){
                    case OPTION_HASH_FILENAME:
                        hashFilenameStr = new String( theValue, "US-ASCII");
                        break;

                    case TASK_STATUS:
                        taskStatus = new String( theValue, "US-ASCII");
                        break;
                    default:
                        retVal = false;
                }

            } catch (UnsupportedEncodingException ex) {
                ex = null;
            }
        }
        return retVal;
    }    

    //===============================================================
    /**
     * Returns a file reference specifying the local file name.
     *
     * @return
     */
    public String getHashFilenameString() {
       return hashFilenameStr;
    }
    
    //===============================================================
    /**
    *   Performs the logic specific to the message.
    *
     * @param passedManager
    */
    @Override
    public void evaluate( PortManager passedManager ) {       
            
        ControlMessageManager aCMManager = ControlMessageManager.getControlMessageManager();
        if( aCMManager != null ){

            //Get the filename hash 
            String theHashFilenameStr = getHashFilenameString();
            String[] theFilePathArr = theHashFilenameStr.split(":", 2);
            if( theFilePathArr.length > 1 ){

                String theFilePath = theFilePathArr[1];
                //Debug
                DebugPrinter.printMessage( this.getClass().getSimpleName(), "Received TaskGetFile for " + theFilePath);

                File fileToSend = new File(theFilePath);
                if(fileToSend.exists()){
                    
                    ClientConfig theConf = ClientConfig.getConfig();
                    int socketPort = theConf.getSocketPort();
                    String serverIp = theConf.getServerIp();

                    //Get the port router
                    ClientPortRouter aPR = (ClientPortRouter) passedManager.getPortRouter( socketPort );
                  
                    int retChannelId = aPR.ensureConnectivity( serverIp, socketPort, this );   
                    if(retChannelId != 0 ){
                        //Queue the file to be sent
                        String fileHashNameStr = new StringBuilder().append("0").append(":").append(theFilePath).toString();

                        PushFile thePFM = new PushFile( getTaskId(), retChannelId, fileHashNameStr, fileToSend.length(), PushFile.FILE_DOWNLOAD );
                        thePFM.setDestHostId( getSrcHostId() );
                        aCMManager.send(thePFM);
                    }
                }
            }
        }     
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

}/* END CLASS TaskGetFile */
