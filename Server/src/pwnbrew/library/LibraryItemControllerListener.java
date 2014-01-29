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
 * LibraryControllerListener.java
 *
 * Created on July 16, 2013
 */

package pwnbrew.library;

import java.awt.Component;
import javax.swing.JFrame;
import pwnbrew.controllers.ControllerListener;
import pwnbrew.controllers.RunnableItemController;
import pwnbrew.host.HostController;

/**
 *
 *  
 */
public interface LibraryItemControllerListener extends ControllerListener {
    
    
    
    // ==========================================================================
    /**
     *  Returns the component that is associated with the listener.
     * @return 
    */
    public Component getListenerComponent();
    
    
    
    public void persistentValueChanged( LibraryItemController controller );
    
    // ==========================================================================
    /**
     *  Returns the parent controller of the passed controller
     * 
     * @param controller
     * @return 
     */
    public HostController getParentController( LibraryItemController controller );

    // ==========================================================================
    /**
     *  Returns the main JFrame
     * 
     * @return 
     */
    public JFrame getParentJFrame();

     // ==========================================================================
    /**
     *  Attempts to send the job to the remote client
     * 
     * @param aController 
     */
    public void sendRemoteJob( RunnableItemController aController );

    // ==========================================================================
    /**
     *  Attempts to cancel the currently running job
     * 
     * @param aThis 
     */
    public void cancelRunForCurrentNode(RunnableItemController aThis);
    
}
