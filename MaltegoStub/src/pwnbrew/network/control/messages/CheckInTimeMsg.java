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
import pwnbrew.MaltegoStub;
import pwnbrew.functions.Function;
import pwnbrew.functions.ToSessionManager;
import pwnbrew.manager.PortManager;
import pwnbrew.utilities.SocketUtilities;
import pwnbrew.network.ControlOption;

/**
 *
 *  
 */
public final class CheckInTimeMsg extends MaltegoMessage{ 
    
    private static final byte OPTION_HOST_ID = 124;
    private static final byte OPTION_CHECK_IN = 72;
    private static final byte OPTION_PREV_CHECK_IN = 73;
    private static final byte OPTION_OPERATION = 74;
    
    public static final byte ADD_TIME = 1;
    public static final byte REMOVE_TIME = 2;
    public static final byte REPLACE_TIME = 3;
    
    private int hostId;
    private String checkInDatStr;
    
    public static final short MESSAGE_ID = 0x64;
    
    // ==========================================================================
    /**
     * Constructor
     *
     * @param dstHostId
     * @param hostId
     * @param passedStr
     * @param passedOperation
     * @throws java.io.UnsupportedEncodingException
    */
    public CheckInTimeMsg( int dstHostId, int hostId, String passedStr, byte passedOperation ) throws UnsupportedEncodingException {
        super( MESSAGE_ID, dstHostId );
        
        //Add host id
        byte[] tempBytes = SocketUtilities.intToByteArray( hostId );
        ControlOption aTlv = new ControlOption( OPTION_HOST_ID, tempBytes);
        addOption(aTlv);
        
        tempBytes = passedStr.getBytes("US-ASCII");
        aTlv = new ControlOption( OPTION_CHECK_IN, tempBytes);
        addOption(aTlv);
        
        //Add operation
        aTlv = new ControlOption( OPTION_OPERATION, new byte[]{passedOperation});
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
    
    //===========================================================================
    /**
     * Sets the prev check-in time 
     * @param passedStr 
     */
    public void addPrevCheckIn( String passedStr ){
        try {
            byte[] tempBytes = passedStr.getBytes("US-ASCII");
            ControlOption aTlv = new ControlOption( OPTION_PREV_CHECK_IN, tempBytes);
            addOption(aTlv);
        } catch (UnsupportedEncodingException ex) {
        }
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
    public void evaluate( PortManager passedManager ) {
    
        if( passedManager instanceof MaltegoStub ){
            MaltegoStub theStub = (MaltegoStub)passedManager;
            Function aFunction = theStub.getFunction();
            if( aFunction instanceof ToSessionManager ){
                
                //Cast the function
                ToSessionManager aFunc = (ToSessionManager)aFunction;
                
                //Add the ip to the list
                aFunc.addCheckInTime( hostId, checkInDatStr );
            }            
        }  
    }

}
