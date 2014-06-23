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

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.logging.Level;
import pwnbrew.controllers.MainGuiController;
import pwnbrew.host.HostController;
import pwnbrew.logging.Log;
import pwnbrew.manager.CommManager;
import pwnbrew.network.ControlOption;
import pwnbrew.utilities.SocketUtilities;

/**
 *
 *  
 */
public final class CheckInTimeMsg extends ControlMessage{ 
    
    private static final byte OPTION_HOST_ID = 124;
    private static final byte OPTION_CHECK_IN = 72;
    private static final byte OPTION_PREV_CHECK_IN = 73;
    private static final byte OPTION_OPERATION = 74;
    
    public static final byte ADD_TIME = 1;
    public static final byte REMOVE_TIME = 2;
    public static final byte REPLACE_TIME = 3;
    
    private int hostId;
    private String checkInDatStr;
    private String prevCheckInDatStr;
    private byte theOperation;
    
    private static final String NAME_Class = CheckInTimeMsg.class.getSimpleName();
    
    // ==========================================================================
    /**
     * Constructor
     *
     * @param dstHostId
     * @param hostId
     * @param passedIP
     * @throws java.io.UnsupportedEncodingException
    */
    public CheckInTimeMsg( int dstHostId, int hostId, String passedIP ) throws UnsupportedEncodingException {
        super( dstHostId );
        
        //Add host id
        byte[] tempBytes = SocketUtilities.intToByteArray( hostId );
        ControlOption aTlv = new ControlOption( OPTION_HOST_ID, tempBytes);
        addOption(aTlv);
        
        tempBytes = passedIP.getBytes("US-ASCII");
        aTlv = new ControlOption( OPTION_CHECK_IN, tempBytes);
        addOption(aTlv);
 
    }

    //===========================================================================
    /**
     *  Constructor
     * 
     * @param passedId 
     */
    public CheckInTimeMsg( byte[] passedId ) {
       super(passedId);
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
                case OPTION_CHECK_IN:
                    checkInDatStr = new String( theValue, "US-ASCII");
                    break;
                case OPTION_PREV_CHECK_IN:
                    prevCheckInDatStr = new String( theValue, "US-ASCII");
                    break;
                case OPTION_OPERATION:
                    theOperation = theValue[0];
                    break;
                case OPTION_HOST_ID:
                    hostId = SocketUtilities.byteArrayToInt(theValue);
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
    
        //Get the host controller 
        MainGuiController theGuiController = (MainGuiController)passedManager.getTaskManager();
        String hostStr = Integer.toString( hostId );
        HostController theHostController = theGuiController.getHostController( hostStr );
        if( theHostController != null ){
            switch(theOperation){
            
                case ADD_TIME:
                    theHostController.addCheckInDate(checkInDatStr);
                    break;
                case REMOVE_TIME:
                    theHostController.removeCheckInDates( Arrays.asList( new String[]{checkInDatStr}) );
                    break;
                case REPLACE_TIME:
                    theHostController.replaceDate(prevCheckInDatStr, checkInDatStr);
                    break;
                default:
                    Log.log(Level.WARNING, NAME_Class, "evaluate()", "Unknown check-in time operation", null );   
                    return;
            } 
            theHostController.saveToDisk();
        }
    }
}
