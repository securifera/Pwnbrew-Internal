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
* RemoteTaskController.java
*
* Created on June 13, 2013, 9:11:22 PM
*/

package pwnbrew.fileoperation;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
/**
 *
 *  
 */
public class FileOperationController implements RemoteFileIOListener {

    private RemoteFileIO theTask = null;
    private FileOperationJProgressPanel theProgressPanel = null;
    private TaskManager theManager = null;
 
    // ==========================================================================
    /**
     * Constructor
     *
     * @param passedTask
     * @param passedManager
    */
    public FileOperationController(RemoteFileIO passedTask, TaskManager passedManager ) {

        theTask = passedTask;
        theTask.addListener(this);
        theManager = passedManager;
    }

     
    // ========================================================================
    /**
     *  Returns the Object managed by the controller.
     * @return 
     */
//    @Override
    public Object getObject() {
       return theTask;
    }
    
   
    // ==========================================================================
    /**
     * Creates the {@link JobJProgressPanel} for the {@link Task}.
     *
     * @see #getProgressPanel()
     */
    private void createProgressPanel() {
        theProgressPanel = new FileOperationJProgressPanel(theManager, theTask);
    }/* END createProgressPanel() */

    // ==========================================================================
    /**
     * Called when the status of the task changes
     *
     * @param theTask
    */
    @Override
    public void taskChanged(final RemoteFileIO theTask) {

       if(theProgressPanel == null){
          createProgressPanel();
       }

       SwingUtilities.invokeLater( new Runnable(){
           
            @Override
            public void run() {
               theProgressPanel.updateComponents();
            }
       });
       
    }

    // ==========================================================================
    /**
     * Returns the root panel for the controller
     *
     * @return 
    */
//    @Override
    public JPanel getRootPanel() {
       if( theProgressPanel == null ) //If the ParameterPanel has not yet been created...
            createProgressPanel(); //Create the ParameterPanel for the Task
       return theProgressPanel;
    }

    
//    @Override
//    public void updateComponents() {
//
//    }

}
