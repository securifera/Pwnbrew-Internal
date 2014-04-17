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
* GetHosts.java
*
* Created on Oct 18, 2013, 10:12:33 PM
*/

package pwnbrew.network.control.messages;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.logging.Level;
import pwnbrew.controllers.MainGuiController;
import pwnbrew.host.Host;
import pwnbrew.host.HostController;
import pwnbrew.library.LibraryItemController;
import pwnbrew.logging.Log;
import pwnbrew.manager.CommManager;
import pwnbrew.network.control.ControlMessageManager;
import pwnbrew.network.control.messages.ControlMessage;
import pwnbrew.tasks.TaskManager;

/**
 *
 *  
 */
public final class GetHosts extends ControlMessage{ // NO_UCD (use default)
    
    private static final String NAME_Class = GetHosts.class.getSimpleName();

    // ==========================================================================
    /**
     * Constructor
     *
     * @param passedId
    */
    public GetHosts(byte[] passedId ) {
        super( passedId );
    }
    
     //===============================================================
    /**
    *   Performs the logic specific to the message.
    *
     * @param passedManager
    */
    @Override
    public void evaluate( CommManager passedManager ) {     
        
        ControlMessageManager aCMManager = ControlMessageManager.getControlMessageManager();
        if( aCMManager != null ){
            
            TaskManager theTaskManager = passedManager.getTaskManager();
            if( theTaskManager instanceof MainGuiController ){
                
                //Get the host controllers 
                MainGuiController theGuiController = (MainGuiController)theTaskManager;
                List<LibraryItemController> theHostControllers = theGuiController.getHostControllers();
                for( LibraryItemController aController : theHostControllers ){
                    if( aController instanceof HostController ){
                        
                        HostController aHostController = (HostController)aController;
                        if( !aHostController.isLocalHost() ){
                            
                            Host aHost = aHostController.getHost();
                            try {
                                HostMsg aHostMsg = new HostMsg( getSrcHostId(), aHost.getHostname(), 
                                aHost.getOsName(), aHost.getJvmArch(), Integer.parseInt(aHost.getId()), aHost.isConnected());
                                aCMManager.send(aHostMsg);
                            } catch (UnsupportedEncodingException ex) {
                                Log.log(Level.WARNING, NAME_Class, "evaluate()", ex.getMessage(), ex );                                
                            }
                            
                        }
                        
                    }
                }                
                
            }
                        
        }        
    }

}/* END CLASS GetHosts */
