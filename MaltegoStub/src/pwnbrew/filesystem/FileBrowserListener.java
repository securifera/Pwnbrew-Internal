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
package pwnbrew.filesystem;

import java.io.File;
import java.util.List;
import javax.swing.event.ListSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import pwnbrew.fileoperation.TaskManager;

/**
 *
 * @author Securifera
 */
public interface FileBrowserListener extends TaskManager {

    public void downloadFiles(List<RemoteFile> theFileList);

    public void uploadFiles( List<File> theObjList );
    
    public String getId();

    public void addRemoteFileSystemTask(RemoteFileSystemTask aRFST);
    
    public RemoteFileSystemTask removeRemoteFileSystemTask( int taskId );
    
    public void performFileOperation(byte DATE, String filePath, String string);

    public String getHost();

    public void beNotified();

    public void getChildren( DefaultMutableTreeNode passedNode );
    
    public String getHostDelimiter();

    public void fileJTableValueChanged(ListSelectionEvent e);
    
    public void fileTreePanelValueChanged(TreePath aPath);

    public void selectNodeInTree(FileNode aFileNode);

    public void searchForFiles(DefaultMutableTreeNode passedNode, String searchStr);

    public void cancelSearch();

    public void cancelOperation();
    
    public FileBrowserSettings getFileBrowserSettings();

    public void downloadFolders(List<RemoteFile> theFolderList);

    public boolean getClearTableFlag();

}
