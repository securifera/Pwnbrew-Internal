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
import pwnbrew.controllers.MainGuiController;
import pwnbrew.host.HostController;
import pwnbrew.manager.CommManager;
import pwnbrew.network.ControlOption;
import pwnbrew.utilities.SocketUtilities;

/**
 *
 *  
 */
public final class AutoSleep extends ControlMessage{ // NO_UCD (use default)
    
    private static final byte OPTION_HOST_ID = 67;
    private static final byte OPTION_OPERATION = 68;
    private static final byte OPTION_BOOLEAN_VALUE = 69;
    
    public static final byte GET_VALUE = 18;
    public static final byte SET_VALUE = 19;
    
    private boolean autoSleepBool;
    private byte theOperation;
    private int hostId;
    
    // ==========================================================================
    /**
     * Constructor
     *
     * @param dstHostId
     * @param passedHostId
     * @param passedOperation
     * @param passedBool
    */
    public AutoSleep( int dstHostId, int passedHostId, byte passedOperation, boolean passedBool ) {
        super( dstHostId );
        
        //Add host id
        byte[] tempBytes = SocketUtilities.intToByteArray(passedHostId);
        ControlOption aTlv = new ControlOption( OPTION_HOST_ID, tempBytes);
        addOption(aTlv);
        
        //Add operation
        aTlv = new ControlOption( OPTION_OPERATION, new byte[]{passedOperation});
        addOption(aTlv);
        
        byte aByte = 0;
        if( passedBool )
            aByte = 1;
        
        //Add the flag
        aTlv = new ControlOption( OPTION_BOOLEAN_VALUE, new byte[]{aByte});
        addOption(aTlv);
    }
    
    //===========================================================================
    /**
     *  Constructor
     * 
     * @param passedId 
     */
    public AutoSleep( byte[] passedId ) {
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
        
        byte[] theValue = tempTlv.getValue();
        switch( tempTlv.getType()){
            case OPTION_OPERATION:
                theOperation = theValue[0];
                break;
            case OPTION_HOST_ID:
                hostId = SocketUtilities.byteArrayToInt(theValue);
                break;
            case OPTION_BOOLEAN_VALUE:
                byte theVal = theValue[0];
                if( theVal == 1)
                    autoSleepBool = true;
                break;
            default:
                retVal = false;
                break;
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
            if(theOperation == SET_VALUE){            
                theHostController.setAutoSleepFlag(autoSleepBool);            
                theHostController.saveToDisk();
            }
        }
    
    }

}
