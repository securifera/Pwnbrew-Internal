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
 * JobJListCellRenderer.java
 *
 * Created on July 23, 2013, 9:31 PM
 */

package pwnbrew.gui.remote.job;

import pwnbrew.controllers.RemoteJobController;
import pwnbrew.tasks.RemoteTask;
import java.awt.Color;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.*;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import pwnbrew.utilities.FileUtilities;
import pwnbrew.tasks.TaskManager;

/**
 *
 *  
 */
public class JobJListCellRenderer extends DefaultListCellRenderer {

    private final TaskManager theTaskManager;
    private final Map<RemoteTask, RemoteJobController> theTaskControllerMap = new HashMap<>();

    //=========================================================
    /**
     * Constructor
     * 
     * @param passedManager 
     */
    JobJListCellRenderer( TaskManager passedManager ) {
         theTaskManager = passedManager;
    }
    
    //=========================================================
    /**
     *  Returns the list cell renderer
     * 
     * @param list
     * @param value
     * @param index
     * @param isSelected
     * @param cellHasFocus
     * @return 
     */
    @Override
    public Component getListCellRendererComponent( JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus){

        Component aComp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if( value instanceof RemoteTask ){

            RemoteTask theTask = (RemoteTask)value;
            RemoteJobController theTaskController = theTaskControllerMap.get(theTask);

            //Create the progress bar panel
            if( theTaskController == null || theTaskController.getObject() != theTask ){
                theTaskController = new RemoteJobController( theTask, theTaskManager );
                theTaskControllerMap.put(theTask, theTaskController);      
            }

            //Send the message here because now we know the listener is in place.
            //Update: Added state check because otherwise every job is sent on load of
            //the GUI.
            if( theTask.getState().equals(  RemoteTask.TASK_INIT )){
                theTask.setState( RemoteTask.TASK_START);
                theTaskManager.startTask(theTask);
            }

            JobJProgressPanel theProgPanel = (JobJProgressPanel) theTaskController.getRootPanel();

            //Set the background of the panel
            theProgPanel.setBackground(aComp.getBackground());

            if(!cellHasFocus){
                theProgPanel.setBorder( BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
            }

            aComp = theProgPanel;
        }

        return aComp;
    }

    //===============================================================
    /**
     *  Removes all tasks
    */
    void removeAllTasks() throws IOException {

       Iterator<RemoteTask> taskIter = theTaskControllerMap.keySet().iterator();
       while(taskIter.hasNext()){
           //Remove the task dir
           RemoteTask aTask = taskIter.next();
           File aClientDir = theTaskManager.getHostDirectory( Integer.parseInt( aTask.getClientId() ));
           FileUtilities.deleteDir(aClientDir);
       }
       theTaskControllerMap.clear();
    }

    //===============================================================
    /**
     *  Removes the task from the progress panel map
    */
    void removeTaskFromMap(RemoteTask passedTask) {
       theTaskControllerMap.remove(passedTask);
    }

    //===============================================================
    /**
     *  Ensures that none of the progress bars are set to indeterminate
     * which will keep the application from closing cleanly.
    */
    public void prepClose() {
       List<RemoteJobController> theControllers = new ArrayList(theTaskControllerMap.values());
       for(RemoteJobController aController : theControllers){
          JobJProgressPanel thePanel = (JobJProgressPanel) aController.getRootPanel();
          thePanel.prepClose();
       }
    }

}
