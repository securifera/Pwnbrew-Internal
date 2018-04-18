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
* PushFileFin.java
*
* Created on June 7, 2013, 7:42:31 PM
*/

package pwnbrew.network.control.messages;

import pwnbrew.log.LoggableException;
import java.io.UnsupportedEncodingException;
import pwnbrew.ClientConfig;
import pwnbrew.manager.OutgoingConnectionManager;
import pwnbrew.manager.PortManager;
import pwnbrew.network.ClientPortRouter;
import pwnbrew.utilities.DebugPrinter;
import pwnbrew.network.ControlOption;
import pwnbrew.network.file.FileMessageManager;
import pwnbrew.selector.SocketChannelHandler;

/**
 *
 *  
 */
public final class PushFileFin extends FileMessage {

    private String hashFilenameStr;
    
    public static final short MESSAGE_ID = 0x45; 
     
    // ==========================================================================
    /**
     * Constructor
     *
     * @param passedChannelId
     * @param passedTaskId
     * @param passedFileId
     * @param hashFileNameStr
    */
    public PushFileFin( int passedChannelId, int passedTaskId, int passedFileId, String hashFileNameStr ) {
       super(MESSAGE_ID, passedChannelId, passedTaskId, passedFileId );     
         
       byte[] strBytes = hashFileNameStr.getBytes();
       ControlOption aTlv = new ControlOption( OPTION_HASH_FILENAME, strBytes);
       addOption(aTlv);
       
    }
    
     // ==========================================================================
    /**
     * Constructor
     *
     * @param msgId
     * @throws pwnbrew.log.LoggableException
    */
    public PushFileFin( byte[] msgId ) throws LoggableException  {
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

                if( tempTlv.getType() == OPTION_HASH_FILENAME){
                    hashFilenameStr = new String( tempTlv.getValue(), "US-ASCII");                   
                } else {
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
    *   Performs the logic specific to the message.
    *
     * @param passedManager
    */
    @Override
    public void evaluate( PortManager passedManager ) {

        DebugPrinter.printMessage( this.getClass().getSimpleName(), "Received Fin message. Id: " + Integer.toString( getChannelId()));
         //Remove from the parent map
        FileMessageManager theFMM = FileMessageManager.getFileMessageManager();
        theFMM.removeFileSender( getTaskId(), getFileChannelId() );
//
//        ClientConfig theConf = ClientConfig.getConfig();
//        int socketPort = theConf.getSocketPort();
//        ClientPortRouter aPR = (ClientPortRouter) passedManager.getPortRouter( socketPort );
//        
//        //Get the connection manager
//        OutgoingConnectionManager aOCM = aPR.getConnectionManager( getSrcHostId() );
//        if( aOCM != null ){
//            SocketChannelHandler aSCH = aOCM.removeHandler( getFileChannelId() );
//            if( aSCH != null ){
//                DebugPrinter.printMessage( this.getClass().getSimpleName(), "Shutting down file channel: " + Integer.toString(getFileChannelId()));
//                aSCH.shutdown();
//            }            
//        }
    }

}/* END CLASS PushFileFin */
