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
 * FileOperationJList.java
 *
 * Created on Oct 21, 2013, 1:55 PM
 */

package pwnbrew.fileoperation;

import java.io.IOException;
import javax.swing.DefaultListModel;
import javax.swing.JList;

/**
 *
 */
public class FileOperationJList extends JList{

    private DefaultListModel<?> theListModel = null;
    private FileOperationJListCellRenderer theProgressRenderer = null;
    
    //===============================================================
    /**
     * Constructor
     * 
     */
    public FileOperationJList() {
        super();
        
        //Initialize the model
        theListModel = new DefaultListModel();
        setModel(theListModel);
    }

    //===============================================================
    /**
     *  Sets up the task table
     * @param passedManager
    */
    public void initTable( TaskManager passedManager ) {

        theProgressRenderer = new FileOperationJListCellRenderer( passedManager);
        setCellRenderer(theProgressRenderer);

        FileOperationJListMouseListener aListener = new FileOperationJListMouseListener(this);
        addMouseListener(aListener);
        addMouseMotionListener(aListener);
    }

    //===============================================================
    /**
     *  Removes the task from the task map in the renderer
     * @throws java.io.IOException
    */
    public void clearTasks() throws IOException  {

       //Remove all the rows
       theListModel.clear();

       //Remove the map entries
       theProgressRenderer.removeAllTasks();
    }

    //===============================================================
    /**
     *  Removes the task from the task map in the renderer
     * @param passedTask
    */
    public void removeTask(RemoteFileIO passedTask) {
       theProgressRenderer.removeTaskFromMap(passedTask);
    }

}
