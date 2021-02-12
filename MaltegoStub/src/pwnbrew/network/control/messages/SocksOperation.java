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
* SocksOperation.java
*
*/

package pwnbrew.network.control.messages;

import pwnbrew.manager.PortManager;
import pwnbrew.misc.DebugPrinter;
import pwnbrew.network.ControlOption;
import pwnbrew.socks.SocksHandler;
import pwnbrew.socks.SocksMessageManager;
import pwnbrew.utilities.SocketUtilities;
import pwnbrew.socks.SocksServer;

/**
 *
 *  
 */
public final class SocksOperation extends ControlMessage{
    
     //Class name
    private static final String NAME_Class = SocksOperation.class.getSimpleName();   
    
    private static final byte OPTION_SOCKS_OPERATION = 43;
    private static final byte OPTION_HANDLER_ID = 29;
    
    public static final byte SOCKS_START = 23;
    public static final byte SOCKS_STOP = 24;
    public static final byte HANDLER_STOP = 25;
    public static final byte SHUTDOWN = 26;
        
    private byte theSocksOperation = 0;
    private int theHandlerId = -1;
        
    public static final short MESSAGE_ID = 0x51;
    
    // ==========================================================================
    /**
    * Constructor
    *
     * @param dstHostId
     * @param socksOperation
     * @param passedHandlerId
    */
    public SocksOperation( int dstHostId, byte socksOperation, Integer... passedHandlerId ) {
        super( MESSAGE_ID, dstHostId );
                
        //Set flag for handler creation 
        byte[] tempBytes = new byte[1];
        tempBytes[0] = socksOperation;
        
        ControlOption aTlv = new ControlOption(OPTION_SOCKS_OPERATION, tempBytes);
        addOption(aTlv);
        
        //=====================================================================
        /**
         * Add the handler id if it's specified
         */
        if( passedHandlerId.length > 0 ){
            tempBytes = SocketUtilities.intToByteArray(passedHandlerId[0]);
            aTlv = new ControlOption(OPTION_HANDLER_ID, tempBytes);
            addOption(aTlv);
        }
           
    }
    
    // ==========================================================================
    /**
     * Constructor
     *
     * @param passedId
    */
    public SocksOperation( byte[] passedId ) {
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
        if( !super.setOption(tempTlv)){
            
            byte[] theValue = tempTlv.getValue();
            switch( tempTlv.getType()){
                case OPTION_SOCKS_OPERATION:
                    theSocksOperation = theValue[0];
                    break;
                case OPTION_HANDLER_ID:
                    theHandlerId = SocketUtilities.byteArrayToInt(theValue);
                    break;
                default:
                    retVal = false;
                    break;
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
        
        String dbgMsg = "Received SocksOperation: ";
        if( theSocksOperation == HANDLER_STOP ){
            
            dbgMsg += " HANDLER_STOP";
            
            SocksMessageManager theSMM = SocksMessageManager.getSocksMessageManager();
            SocksServer aSS = theSMM.getSocksServer();
            
            //Get the socks handler
            if( aSS != null){
                SocksHandler aSH = aSS.removeSocksHandler(theHandlerId);   
                if( aSH != null ){
                    aSH.beNotified();
                    aSH.close();
                }
            }
            
        }
        
        DebugPrinter.printMessage(NAME_Class, "evaluate", dbgMsg, null);
    }


}/* END CLASS SocksOperation */
