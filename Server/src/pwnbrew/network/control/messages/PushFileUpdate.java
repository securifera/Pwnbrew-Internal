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

import java.io.IOException;
import java.util.logging.Level;
import pwnbrew.logging.Log;
import pwnbrew.manager.CommManager;
import pwnbrew.network.ControlOption;
import pwnbrew.network.file.FileMessageManager;
import pwnbrew.tasks.RemoteTask;
import pwnbrew.tasks.TaskManager;
import pwnbrew.utilities.SocketUtilities;


/**
 *
 *  
 */
public final class PushFileUpdate extends FileMessage {

    private static final byte OPTION_DATASIZE = 4;
    private long fileSize;
    
    //Class name
    private static final String NAME_Class = PushFileUpdate.class.getSimpleName();
    
    // ==========================================================================
    /**
     * Constructor
     *
     * @param passedId
    */
    public PushFileUpdate( byte[] passedId ) { // NO_UCD (use default)
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
                case OPTION_DATASIZE:
                    fileSize = SocketUtilities.byteArrayToLong( theValue );
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
     *
     * @return
     */
    public long getFileSize() {
        return fileSize;
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
              
            //Get the file manager
            FileMessageManager theFileMM = FileMessageManager.getFileMessageManager();
            if( theFileMM == null )
                theFileMM = FileMessageManager.initialize( passedManager );            
            
            theFileMM.updateFileSize( getFileId(), getFileSize() );
            
            //Call for update
            final TaskManager aManager = passedManager.getTaskManager();
            final RemoteTask rmTask = aManager.getRemoteTask( getTaskId() );
            if( rmTask != null ){
                rmTask.resultFileReceived();
                TaskManager aMgr = passedManager.getTaskManager();
                if( aMgr != null )
                    aMgr.taskChanged(new TaskStatus( getTaskId(), RemoteTask.TASK_COMPLETED, -1 ));
            }

        } catch (IOException ex) {
            Log.log(Level.INFO, NAME_Class, "evaluate()", ex.getMessage(), ex );
        }
        
    }
}