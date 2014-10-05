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

package pwnbrew.fileoperation;

import java.util.*;
import pwnbrew.utilities.SocketUtilities;

public class RemoteFileIO {

    private static final String NAME_Class = RemoteFileIO.class.getSimpleName();
    
    //States
    public static final String TASK_XFER_FILES = "Transferring Files";
    public static final String TASK_COMPLETED = "Completed";
    public static final String TASK_FAILED = "Failed";
    public static final String TASK_CANCELLED = "Cancelled";

    private final List<RemoteFileIOListener> theTaskListeners = new ArrayList<>();

    private String theRemoteFilePath = "";
    private String theIconStr=  "IconStr";  
    private String theTaskType = "";
    private String theTaskId = "";
    private String theOperationState = "";
    private int theStateProgress = 0;
  
    // ==========================================================================
    /**
     * Creates a new instance of {@link Task}.
     */
    public RemoteFileIO() {    
    }

    // ==========================================================================
    /**
     * Creates a new instance of {@link Task}.
     * @param passedFilePath
     * @param passedType
     * @param passedIconStr
     */
    public RemoteFileIO( String passedFilePath, String passedType, String passedIconStr ) {

        theRemoteFilePath = passedFilePath;
        theTaskType = passedType;
        theTaskId = Integer.toString(SocketUtilities.getNextId());
        theIconStr = passedIconStr;
   
    }
    
    // ==========================================================================
    /**
     *  Get the icon image
     * @return 
     */
    public String getIconString() {
        return theIconStr;
    }    

    // ==========================================================================
    /**
     * Adds the RemoteFileIOListener to the list.
     *
     * @param passedListener
    */
    public void addListener( RemoteFileIOListener passedListener ) {
       if(passedListener != null){

          synchronized(theTaskListeners){
             if(!theTaskListeners.contains(passedListener)){
                theTaskListeners.add(passedListener);
             }
          }
       }
    }
    
     // ==========================================================================
    /**
     * Returns the type of task.
     *
     * @return the type of task
     */
    public String getType() {
        return theTaskType ;
    }

    // ==========================================================================
    /**
     * Sets the state to the given {@code String}.
     * <p>
     * If the given {@code String} is null, the state will be set to the empty
     * {@code String}.
     *
     * @param state
     */
    public void setState( String state ) {

       theOperationState = state;

    }
  
    // ==========================================================================
    /**
     * Sets the task id to the given {@code String}.
     * <p>
     * If the given {@code String} is null, the type will be set to the empty
     * {@code String}.
     *
     * @param taskId
     */
    public void setTaskId( String taskId ) {
        theTaskId = taskId;
    }


    // ==========================================================================
    /**
     * Sets the progress of the current state to the given {@code String}.
     * <p>
     *
     * @param progress
     */
    public void setStateProgress( int progress ) {
        theStateProgress = progress;
    }
    
    // ==========================================================================
    /**
     * Returns the state.
     *
     * @return the state
     */
    public String getState() {
        return theOperationState;
    }

    // ==========================================================================
    /**
     * Returns the progress of the current state.
     *
     * @return the state progress
     */
    public int getStateProgress() {
        return theStateProgress;
    }

    
    // ==========================================================================
    /**
     * Returns a list of {@link ITaskListener}'s
     * <p>
     * This method will return at least an empty {@link ArrayList}.
     *
     * @return an {@code List} of {@link ITaskListener}'s
     */
    public List<RemoteFileIOListener> getRemoteListeners() {
        
        List<RemoteFileIOListener> theListenerList;
        synchronized(theTaskListeners){
            theListenerList = new ArrayList<>(theTaskListeners);
        }
        return theListenerList;
    }

    // ==========================================================================
    /**
     * Returns the task id
     *
     * @return a {@code String} representing the task id.
     */
    public String getTaskId() {
        return theTaskId;
    }     

    // ==========================================================================
    /**
     *  Get the task name
     * @return 
     */
    public String getFilePath() {
        return theRemoteFilePath;
    }
}
