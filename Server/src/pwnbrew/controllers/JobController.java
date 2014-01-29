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
 * JobController.java
 *
 * Created on June 23, 2013, 2:31:12 PM
 */

package pwnbrew.controllers;

import pwnbrew.host.Host;
import pwnbrew.tasks.RemoteTask;


/**
 * 
 */
abstract public class JobController extends RunnableItemController {
    
    
    private int theRemoteTaskId = 0;
    
    //===============================================================
    /**
     * Returns a RemoteTask object derived from the managed object
     *  
     * @return
    */
    abstract RemoteTask generateRemoteTask( Host passedHost );
    
    //===============================================================
    /**
     * Returns the last run result
     *  
     * @return
    */
    abstract public String getLastRunResult();
    
    //===============================================================
    /**
     * 
     */
    abstract public void updateLastRunDetails();
    
    //===============================================================
    /*
     *  Set the remote task
    */
    public void setRemoteTaskId( int passedId ){
        theRemoteTaskId = passedId;
    }
    
    //===============================================================
    /*
     *  Get the remote task
    */
    public int getRemoteTaskId(){
        return theRemoteTaskId;
    }
    
    
    
    // ========================================================================
    /**
     * 
     * @param observer
     */
    @Override //RunnableItemController
    public void runItem( RunObserver observer ) {
        
    }/* END runItem() */
    
    
    // ========================================================================
    /**
     * 
     * @return 
     */
    public String getJobType() {
        
        String rtnStr = null;
        
        Object object = getObject();
        if( object != null )
            rtnStr = object.getClass().getSimpleName();
        
        return rtnStr;
        
    }/* END getJobType() */
    
    // ==========================================================================
    /**
     * Switches to the runner panel in the tabbed pane
     *
    */
    abstract public void showRunnerTab();  
      
    
    
}/* END CLASS JobController */
