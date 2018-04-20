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
* TaskStatus.java
*
* Created on June 7, 2013, 9:21:51 PM
*/

package pwnbrew.network.control.messages;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import pwnbrew.network.ControlOption;

/**
 *
 *  
 */
public class TaskStatus extends Tasking {    
    
    protected static final byte OPTION_HASH_FILENAME = 3;
    protected static final byte OPTION_TASK_STATUS = 8;
    protected static final byte TASK_STATUS = 9;
    protected String taskStatus = "";
    
    //Class name
    private static final String NAME_Class = TaskStatus.class.getSimpleName();    
    
    public static final short MESSAGE_ID = 0x7a;
        
     // ==========================================================================
    /**
    * Constructor
    *
     * @param passedClassId
     * @param taskId
     * @param dstHostId
     * @param passedStatus
     * @throws java.io.IOException
    */
    public TaskStatus(short passedClassId, int taskId, String passedStatus, int dstHostId ) throws IOException  {
        super( passedClassId, taskId, dstHostId );

        taskStatus = passedStatus;
        byte[] strBytes = passedStatus.getBytes("US-ASCII");

        ControlOption aTlv = new ControlOption(OPTION_TASK_STATUS, strBytes);
        addOption(aTlv);
    }
    
        // ==========================================================================
    /**
    * Constructor
    *
     * @param taskId
     * @param dstHostId
     * @param passedStatus
     * @throws java.io.UnsupportedEncodingException
    */
    public TaskStatus( int taskId, String passedStatus, int dstHostId ) throws UnsupportedEncodingException  {
        super( MESSAGE_ID, taskId, dstHostId );

        taskStatus = passedStatus;
        byte[] strBytes = passedStatus.getBytes("US-ASCII");

        ControlOption aTlv = new ControlOption(OPTION_TASK_STATUS, strBytes);
        addOption(aTlv);
    }
    
      // ==========================================================================
    /**
    * Constructor
    *
     * @param msgId
    */
    public TaskStatus(byte[] msgId) {
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
                    case OPTION_TASK_STATUS:
                        taskStatus = new String( theValue, "US-ASCII");
                        break;
                    default:
                        retVal = false;
                        break;              
                }
            } catch (UnsupportedEncodingException ex) {
                ex = null;
            }
        }
        return retVal;
    }  
    
    //===============================================================
    /**
     * Returns the status of the task
     *
     * @return
     */
    public String getStatus() {
        return taskStatus;
    }


}/* END CLASS TaskStatus */
