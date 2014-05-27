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
 * TaskManager.java
 *
 * Created on June 21, 2013, 8:07:13PM
 */

package pwnbrew.tasks;

import java.io.File;
import pwnbrew.host.Host;
import pwnbrew.host.HostController;
import pwnbrew.network.control.messages.TaskStatus;


/**
 *  
 */
public interface TaskManager {

    // ==========================================================================
    /**
    * Called by a {@link TaskProgressPanel} when a task is to be canceled.
     * @param theTask
    */
    public void startTask(RemoteTask theTask);


    // ==========================================================================
    /**
    * Called by a {@link TaskProgressPanel} when a task is to be canceled.
     * @param passedClientId
     * @param msgId
    */
    public void cancelTask( Integer passedClientId, int msgId );


    // ==========================================================================
    /**
    * Called by a {@link TaskProgressPanel} when a task is to be retried.
     * @param theTask
    */
    public void retryTask(RemoteTask theTask);
  
    //===============================================================
    /**
     * Handles the completion of a task
     *
     * @param passedMsg
    */
    public void taskChanged( TaskStatus passedMsg );
    
    
  
    //===============================================================
    /**
     * Returns the client directory
     *
     * @param clientId
     * @return
    */
    public File getHostDirectory( int clientId );
    
     // ==========================================================================
    /**
     *  Get the host with the given id string
     * 
     * @param clientIdStr
     * @return 
     */
    public HostController getHostController( String clientIdStr );
    
    //===============================================================
    /**
     * Adds a new host to the map
     *
     * @param passedHost
    */
    public void registerHost( Host passedHost );
    
    //===============================================================
    /**
     * 
     * @param clientId 
     * @param passedVersion 
     */
    public void stagerUpgradeComplete( int clientId, String passedVersion );
    
    //=================================================================
    /*
     *  Write the output to the runner panel
     */
    public void populateRunnerPanel( RemoteTask passedTask );
    
     //======================================================================
    /**
     *  Get the remote task
     * 
     * @param passedTaskId
     * @return 
     */
    public RemoteTask getRemoteTask(int passedTaskId );
    

}
