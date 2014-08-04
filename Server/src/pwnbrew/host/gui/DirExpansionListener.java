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
 * DirExpansionListener.java
 *
 * Created on December 23 2013, 11:46 PM
 */

package pwnbrew.host.gui;

import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import pwnbrew.host.Host;
import pwnbrew.host.HostController;
import pwnbrew.manager.DataManager;
import pwnbrew.network.control.ControlMessageManager;
import pwnbrew.network.control.messages.GetDrives;
import pwnbrew.network.control.messages.ListFiles;
import pwnbrew.network.control.messages.Tasking;

/**
 *
 *  
 */
public class DirExpansionListener implements TreeExpansionListener {

    private final HostController theController;
    
     //Class name
    private static final String NAME_Class = DirExpansionListener.class.getSimpleName();
  
    
    //=======================================================================
    /**
     * Constructor
     * @param passedController
     */
    public DirExpansionListener( HostController passedController) {
        theController = passedController;
    }    
       
    @Override
    public void treeExpanded(TreeExpansionEvent event) {
            
        TreePath thePath = event.getPath();
        final DefaultMutableTreeNode node = (DefaultMutableTreeNode)(thePath.getLastPathComponent());
        DefaultMutableTreeNode firstNode = (DefaultMutableTreeNode)node.getFirstChild();
        if (firstNode==null)    // No flag
            return;

        ControlMessageManager aCMM = ControlMessageManager.getControlMessageManager();
        int hostId = Integer.parseInt( theController.getId() );
        Object anObj = node.getUserObject();

        Tasking aTaskMessage = null;
        if( anObj instanceof IconData) 
            anObj = ((IconData)anObj).getObject();

        if( anObj instanceof Host ){                
            aTaskMessage = new GetDrives( hostId );                
        } else if( anObj instanceof FileNode ){
            FileNode aFN = (FileNode)anObj;
            RemoteFile aFile = aFN.getFile();
            aTaskMessage = new ListFiles( hostId, aFile.getAbsolutePath());
        }

        //Send the message
        if( aTaskMessage != null ){

            int taskId = aTaskMessage.getTaskId();
            RemoteFileSystemTask aRFST = new RemoteFileSystemTask(taskId, node );
            
            //Send a message
            theController.addRemoteFileSystemTask(aRFST);
            DataManager.send( theController.getCommManager(), aTaskMessage);
            
        }

    }

    @Override
    public void treeCollapsed(TreeExpansionEvent event) {}
}
