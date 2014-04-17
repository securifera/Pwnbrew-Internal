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
package pwnbrew.functions;

import com.sun.java.swing.plaf.windows.WindowsTreeUI;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.plaf.metal.MetalIconFactory;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import pwnbrew.MaltegoStub;
import pwnbrew.StubConfig;
import pwnbrew.fileoperation.FileOperationUpdater;
import pwnbrew.fileoperation.RemoteFileIO;
import pwnbrew.fileoperation.RemoteFileIOListener;
import pwnbrew.filesystem.FileBrowserListener;
import pwnbrew.filesystem.FileNode;
import pwnbrew.filesystem.FileSystemJFrame;
import pwnbrew.filesystem.FileTreePanel;
import pwnbrew.filesystem.IconData;
import pwnbrew.filesystem.RemoteFile;
import pwnbrew.filesystem.RemoteFileSystemTask;
import pwnbrew.misc.Constants;
import pwnbrew.misc.DebugPrinter;
import pwnbrew.misc.ProgressListener;
import pwnbrew.misc.SocketUtilities;
import pwnbrew.misc.Utilities;
import pwnbrew.network.ClientPortRouter;
import pwnbrew.network.ControlOption;
import pwnbrew.network.control.ControlMessageManager;
import pwnbrew.network.control.messages.FileOperation;
import pwnbrew.network.control.messages.FileSystemMsg;
import pwnbrew.network.control.messages.GetDrives;
import pwnbrew.network.control.messages.ListFiles;
import pwnbrew.network.control.messages.PushFile;
import pwnbrew.network.control.messages.TaskGetFile;
import pwnbrew.network.control.messages.TaskStatus;
import pwnbrew.network.control.messages.Tasking;
import pwnbrew.network.file.FileMessageManager;
import pwnbrew.xml.maltego.MaltegoMessage;

/**
 *
 * @author Securifera
 */
public class ToFileBrowser extends Function implements FileBrowserListener, ProgressListener {
    
    private static final String NAME_Class = MaltegoStub.class.getSimpleName();
    
    private volatile boolean notified = false;
    private int theHostId = 0;   
    private String theOS;
    private String theHostName;
    
    //The file system separator
    private String hostPathSeparator;   
    private FileSystemJFrame theFsFrame;
        
    //Create the return msg
    private MaltegoMessage theReturnMsg = new MaltegoMessage();
    private final Map<Integer, RemoteFileSystemTask> theRemoteFileSystemTaskMap = new HashMap<>();
    
    //Map relating the msgid to the task
    private final Map<Integer, RemoteFileIO> theRemoteFileIOMap = new HashMap<>();
  
    //==================================================================
    /**
     * Constructor
     */
    public ToFileBrowser( MaltegoStub passedManager ) {
        super(passedManager);
    }      
    
    //===================================================================
    /**
     * 
     * @param passedObjectStr
     * @return 
     */
    @Override
    public String run(String passedObjectStr) {
        
        UIManager.put("Tree.expandedIcon", new WindowsTreeUI.ExpandedIcon());
        UIManager.put("Tree.collapsedIcon", new WindowsTreeUI.CollapsedIcon());
        UIManager.put("SplitPane.dividerSize", 5);
        
        UIDefaults uiDefaults = UIManager.getDefaults();
        final String metalPackageName = "javax.swing.plaf.metal.";
        final String windowsPackageName = "com.sun.java.swing.plaf.windows.";
        String fileChooserUI = "";

        if( Utilities.isWindows( Utilities.getOsName() )){
            
            fileChooserUI = windowsPackageName + "WindowsFileChooserUI";

            //Fix for java 7
            uiDefaults.put("FileChooser.viewMenuIcon", MetalIconFactory.getFileChooserDetailViewIcon() );

        } else {
            fileChooserUI = metalPackageName + "MetalFileChooserUI";
        }
        //Set the file chooser
        uiDefaults.put("FileChooserUI", fileChooserUI);
        
        String retStr = "";
        Map<String, String> objectMap = getKeyValueMap(passedObjectStr); 
         
        //Get server IP
        String serverIp = objectMap.get( Constants.SERVER_IP);
        if( serverIp == null ){
            DebugPrinter.printMessage( NAME_Class, "listclients", "No pwnbrew server IP provided", null);
            return retStr;
        }
         
        //Get server port
        String serverPortStr = objectMap.get( Constants.SERVER_PORT);
        if( serverPortStr == null ){
            DebugPrinter.printMessage( NAME_Class, "listclients", "No pwnbrew server port provided", null);
            return retStr;
        }
        
        //Get host id
        String hostIdStr = objectMap.get( Constants.HOST_ID);
        if( hostIdStr == null ){
            DebugPrinter.printMessage( NAME_Class, "listclients", "No host id provided", null);
            return retStr;
        }
        
        //Get host id
        String tempOs = objectMap.get( Constants.HOST_OS);
        if( tempOs == null ){
            DebugPrinter.printMessage( NAME_Class, "listclients", "No host id provided", null);
            return retStr;
        }
        
        //Get host id
        String tempName = objectMap.get( Constants.HOST_NAME);
        if( tempName == null ){
            DebugPrinter.printMessage( NAME_Class, "listclients", "No host id provided", null);
            return retStr;
        }
         
        //Create the connection
        try {
            
            //Set the server ip and port
            StubConfig theConfig = StubConfig.getConfig();
            theConfig.setServerIp(serverIp);
            theConfig.setSocketPort(serverPortStr);
            
            //Set the client id
            Integer anInteger = SocketUtilities.getNextId();
            theConfig.setHostId(anInteger.toString());
            
            ControlMessageManager aCMManager = ControlMessageManager.getControlMessageManager();
            if( aCMManager == null ){
                aCMManager = ControlMessageManager.initialize( theManager );
            }

            //Get the port router
            int serverPort = Integer.parseInt( serverPortStr);
            ClientPortRouter aPR = (ClientPortRouter) theManager.getPortRouter( serverPort );

            //Initiate the file transfer
            if(aPR == null){
                DebugPrinter.printMessage( NAME_Class, "listclients", "Unable to retrieve port router.", null);
                return retStr;     
            }           
            
            //Set up the port wrapper
            theManager.initialize();
            
            //Connect to server
            boolean connected = aPR.ensureConnectivity( serverPort, theManager );
            if( connected ){
             
                //Set the host id
                theHostId = Integer.parseInt( hostIdStr);
                theOS = tempOs;
                theHostName = tempName;
                
                //Set the path separator
                if( Utilities.isWindows( theOS ) ){
                    hostPathSeparator = "\\";
                } else {
                    hostPathSeparator = "/";
                }                
                
                //Create the file browser frame
                theFsFrame = new FileSystemJFrame( this );
                   
                //Set the icon
                Image appIcon = FileNode.FolderBuffImage;
                if( appIcon != null ) {
                    theFsFrame.setIconImage( appIcon );
                }
                
                //Pack and show
                theFsFrame.setVisible(true);
                
                //Wait to be notified
                waitToBeNotified();
    
                //Sleep a couple seconds to make sure the message was sent
                Thread.sleep(2000);
                
                retStr = theReturnMsg.getXml();
                
            } else {
                StringBuilder aSB = new StringBuilder()
                        .append("Unable to connect to the Pwnbrew server at \"")
                        .append(serverIp).append(":").append(serverPort).append("\"");
                DebugPrinter.printMessage( NAME_Class, "listclients", aSB.toString(), null);
            }
            
        } catch (IOException | InterruptedException ex) {
            DebugPrinter.printMessage( NAME_Class, "listclients", ex.getMessage(), ex );
        }
        
        return retStr;
    }
    
         // ==========================================================================
    /**
    * Causes the calling {@link Thread} to <tt>wait()</tt> until notified by
    * another.
    * <p>
    * <strong>This method most certainly "blocks".</strong>
     * @param anInt
    */
    protected synchronized void waitToBeNotified( Integer... anInt ) {

        while( !notified ) {

            try {
                
                //Add a timeout if necessary
                if( anInt.length > 0 ){
                    
                    wait( anInt[0]);
                    break;
                    
                } else {
                    wait(); //Wait here until notified
                }
                
            } catch( InterruptedException ex ) {
            }

        }
        notified = false;
    }
    
    //===============================================================
    /**
     * Notifies the thread
    */
    @Override
    public synchronized void beNotified() {
        notified = true;
        notifyAll();
    }
    
      //===============================================================
    /**
     * 
     * @param taskId
     * @param size
     * @param filePath
     * @param fileType
     * @param dateModified
     */
    public void updateFileSystem(int taskId, long size, String filePath, byte fileType, String dateModified ) {
        
        //Get the task for the id        
        RemoteFileSystemTask theTask;
        synchronized(theRemoteFileSystemTaskMap){
            theTask = theRemoteFileSystemTaskMap.get(taskId);
        }

        if( theTask != null ){

            //Create a file from the path
            if( filePath != null && !filePath.isEmpty() ){

                RemoteFile aFile = new RemoteFile( filePath, hostPathSeparator );
                FileNode aFileNode = new FileNode( aFile, fileType, size, dateModified );
                theTask.addFileNode(aFileNode);

            } 

            if( theTask.getListLength() == theTask.getFileCount() ){

                //Get the parent
                FileNode parentFileNode = null;
                DefaultMutableTreeNode parent = theTask.getParentNode();
                parent.removeAllChildren();  // Remove Flag

                Object theParentObj = parent.getUserObject();
                if( theParentObj instanceof IconData ){
                    IconData theIconData = (IconData)theParentObj;
                    Object innerObj = theIconData.getObject();
                    if( innerObj instanceof FileNode ){
                        parentFileNode = (FileNode)innerObj;
                    }
                }

                List nodeList = theTask.getFileList();
                Collections.sort(nodeList);

                //Add the nodes to the parent
                for (int i=0; i<nodeList.size(); i++){

                    FileNode currentNode = (FileNode)nodeList.get(i);
                    if( parentFileNode != null ){
                        parentFileNode.addChildNode(currentNode);
                    }                    

                    //Set the child count
                    if ( currentNode.isDirectory() || currentNode.isDrive() ){

                        IconData theIconData = new IconData( currentNode.getIcon(), currentNode.getExpandedIcon(), currentNode);
                        DefaultMutableTreeNode node = new DefaultMutableTreeNode(theIconData);
                        parent.add(node);

                        if( currentNode.getSize() > 0)
                            node.add(new DefaultMutableTreeNode( true));  

                    }                     

                }

                //Get the treepath
                DefaultMutableTreeNode aNode = parent;
                try {
                    parent.getFirstChild();       
                } catch(NoSuchElementException ex ){
                    aNode = (DefaultMutableTreeNode) parent.getParent();
                }

                //Run in swing thread
                final FileNode theParentNode = parentFileNode;                                
                final FileTreePanel theFileTreePanel = theFsFrame.getFileTreePanel();                
                final TreePath aTreePath = new TreePath( aNode.getPath() );
                SwingUtilities.invokeLater( new Runnable(){

                    @Override
                    public void run() {

                        //Reload the model
                        JTree theJTree = theFileTreePanel.getJTree();
                        TreePath theSelPath = theJTree.getSelectionPath();    

                        //Get the listener and remove it
                        TreeSelectionListener[] theTreeListenerArr = theJTree.getListeners( TreeSelectionListener.class );
                        TreeSelectionListener theTSListener = theTreeListenerArr[0];
                        theJTree.removeTreeSelectionListener( theTSListener);

                        //Remote any listeners
                        TreeExpansionListener[] theTreeExpansionArr = theJTree.getTreeExpansionListeners();
                        TreeExpansionListener theTEListener = theTreeExpansionArr[0];
                        theJTree.removeTreeExpansionListener( theTEListener );

                        theFileTreePanel.getTreeModel().reload(); 

                        //Expand the last path
                        theJTree.expandPath(aTreePath);         

                        //Update the talbe
                        if( theSelPath != null ){

                            theJTree.setSelectionPath(theSelPath);
                            DefaultMutableTreeNode selNode = (DefaultMutableTreeNode) theSelPath.getLastPathComponent();
                            IconData selObj = (IconData)selNode.getUserObject();
                            if(  selObj.getObject().equals(theParentNode) ){

                                //Get the model
                                DefaultTableModel theModel = (DefaultTableModel) theFsFrame.getFileJTable().getModel();
                                theModel.setRowCount(0);

                                if( theParentNode != null){
                                    for( FileNode childNode : theParentNode.getChildNodes() ){
                                        String theTypeStr = "";
                                        int theType = childNode.getType();
                                        switch( theType ){
                                            case FileSystemMsg.FOLDER:
                                                theTypeStr = "Directory";
                                                break;
                                            case FileSystemMsg.FILE:
                                                theTypeStr = "File";
                                                break;
                                            case FileSystemMsg.DRIVE:
                                                theTypeStr = "Drive";
                                                break;

                                        }            
                                        theModel.addRow( new Object[]{ childNode, 
                                        childNode.getLastModified(), theTypeStr, ( theType == FileSystemMsg.FILE ? childNode.getSize() : "" )});
                                    }
                                }
                            }
                        }

                        //Add the listeners back
                        theJTree.addTreeExpansionListener( theTEListener );
                        theJTree.addTreeSelectionListener(theTSListener);

                        theFsFrame.setCursor(null);
                    }               
                });

                //Remote the task from the map
                synchronized(theRemoteFileSystemTaskMap){
                    theRemoteFileSystemTaskMap.remove(taskId);
                }

            }

        }
        
    }

    //======================================================================
    /**
     * Get the id
     * @return 
     */
    @Override
    public String getId() {
        return Integer.toString( theHostId );
    }

     //=========================================================================
    /**
     *  Adds a task to the map
     * @param aRFST 
     */
    @Override
    public void addRemoteFileSystemTask(RemoteFileSystemTask aRFST) {
        synchronized(theRemoteFileSystemTaskMap){
            theRemoteFileSystemTaskMap.put(aRFST.getTaskId(), aRFST);
        }
    }

     //=========================================================================
    /**
     * 
     * @param passedOp
     * @param filePath 
     * @param addParam 
     */
    @Override
    public void performFileOperation(byte passedOp, String filePath, String addParam ) {
        
        //Get the control message manager
        ControlMessageManager aCMM = ControlMessageManager.getControlMessageManager();
        int hostId = Integer.parseInt( getId() );        
        try {
            
            FileOperation aFileOp = new FileOperation( hostId, passedOp, filePath, addParam );
            aCMM.send(aFileOp);
            
        } catch (UnsupportedEncodingException ex) {
            DebugPrinter.printMessage( NAME_Class, "performFileOperation", ex.getMessage(), ex );     
        }
        
    }

     //=========================================================================
    /**
     * 
     * @return 
     */
    @Override
    public String getHost() {
        return theHostName;
    }

     //=========================================================================
    /**
     * Download the file list
     * 
     * @param theRemoteFiles 
     */
    @Override
    public void downloadFiles(List<RemoteFile> theRemoteFiles) {
                        
        //Get the control message manager
        ControlMessageManager aCMManager = ControlMessageManager.getControlMessageManager();
        for( RemoteFile aFile : theRemoteFiles) { //For each file path...

            try {

                int taskId = SocketUtilities.getNextId();

                //Set the remote task information
                RemoteFileIO aRemoteTask = new RemoteFileIO( aFile.getAbsolutePath(), Constants.FILE_DOWNLOAD, Constants.DOWNLOAD_IMG_STR );
                aRemoteTask.setTaskId(Integer.toString( taskId ));
                aRemoteTask.setState( RemoteFileIO.TASK_XFER_FILES);

                //Add task to the jtable model
                addTask(aRemoteTask);

                //Queue the file to be sent
                String fileHashNameStr = new StringBuilder().append("0").append(":").append(aFile.getAbsolutePath()).toString();
                TaskGetFile theTaskMsg = new TaskGetFile( taskId, fileHashNameStr, theHostId );

                //Send the message
                aCMManager.send( theTaskMsg );  

            } catch ( IOException ex) {
                JOptionPane.showMessageDialog( theFsFrame, ex.getMessage(), "Could not upload the file(s).", JOptionPane.ERROR_MESSAGE );
            }       

        }

    }

    //===============================================================
    /**
     * 
     * @param theObjList 
     * @param passedDir 
     */
    @Override
    public void uploadFiles( List<File> theObjList, String passedDir ) {
        
        if( theObjList != null ) { //If the user selected any files...
                            
            //Get the control message manager
            ControlMessageManager aCMManager = ControlMessageManager.getControlMessageManager();
            for( Object anObj : theObjList) { //For each file path...

                if(anObj instanceof File){
                    File aFile = (File)anObj;
                    if( aFile.exists() && aFile.canRead() ) { 

                        try {

                            int taskId = SocketUtilities.getNextId();

                            //Set the remote task information
                            RemoteFileIO aRemoteTask = new RemoteFileIO( aFile.getAbsolutePath(), Constants.FILE_UPLOAD, Constants.UPLOAD_IMG_STR );
                            aRemoteTask.setTaskId(Integer.toString( taskId ));
                            aRemoteTask.setState( RemoteFileIO.TASK_XFER_FILES);
                            
                            //Add support files and add to the task list
                            addTask(aRemoteTask);

                            //Queue the file to be sent
                            String fileHashNameStr = new StringBuilder().append("0").append(":").append(aFile.getAbsolutePath()).toString();
                            PushFile thePFM = new PushFile( taskId, fileHashNameStr, aFile.length(), PushFile.FILE_UPLOAD, theHostId );

                            //Add the directory
                            byte[] tempArr = passedDir.getBytes("US-ASCII");
                            ControlOption aTlv = new ControlOption( PushFile.OPTION_REMOTE_DIR, tempArr);
                            thePFM.addOption(aTlv);

                            //Send the message
                            aCMManager.send( thePFM );  

                        } catch ( IOException ex) {
                            JOptionPane.showMessageDialog( theFsFrame, ex.getMessage(), "Could not upload the file(s).", JOptionPane.ERROR_MESSAGE );
                        }

                    } else { //If the file cannot be read...
                       JOptionPane.showMessageDialog( theFsFrame, new StringBuilder( "\tThe file(s) could not be read: \"" )
                                .append( aFile.getAbsolutePath() ).append( "\"" ).toString(), "Could not upload the file(s).", JOptionPane.ERROR_MESSAGE );
                    }
                }

            }

        }
        
    }   
    
    //=========================================================================
    /**
     *  Set the file count
     * 
     * @param taskId
     * @param theDirCount 
     */
    public void setFileCount(int taskId, int theDirCount) {
        
        //Get the task for the id
        RemoteFileSystemTask theTask;
        synchronized(theRemoteFileSystemTaskMap){
            theTask = theRemoteFileSystemTaskMap.get(taskId);
        }
        
        if( theTask != null )
            theTask.setFileCount(theDirCount);
        
    }

    //=========================================================================
    /**
     * 
     * @param anObj 
     */
    @Override
    public void getChildren( DefaultMutableTreeNode passedNode ) {
        
        //Set the Cursor
        theFsFrame.setCursor( Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) );
        
        Object anObj = passedNode.getUserObject();
        if( anObj instanceof IconData) 
            anObj = ((IconData)anObj).getObject();
        
        ControlMessageManager aCMM = ControlMessageManager.getControlMessageManager();
        Tasking aTaskMessage = null;
        if( anObj instanceof FileNode ){
            
            FileNode aFN = (FileNode)anObj;
            aFN.clearChildNodes();
            
            switch( aFN.getType() ) {
                case FileSystemMsg.FOLDER:                    
                case FileSystemMsg.DRIVE:
                    RemoteFile aFile = aFN.getFile();
                    aTaskMessage = new ListFiles( theHostId, aFile.getAbsolutePath());
                    break;
                default:
                    aTaskMessage = new GetDrives( theHostId );                        
                    break;
            }
        }

        if( aTaskMessage != null ){
            int taskId = aTaskMessage.getTaskId();
            RemoteFileSystemTask aRFST = new RemoteFileSystemTask(taskId, passedNode );

            //Send a message
            addRemoteFileSystemTask(aRFST);
            aCMM.send(aTaskMessage);
        }
    }

    //=================================================================
    /**
     * 
     * @return 
     */
    @Override
    public String getHostDelimiter() {
        return hostPathSeparator;
    }
    
    //=================================================================
    /**
     *  Add the task
     * 
     * @param passedTask 
     */
    public void addTask( RemoteFileIO passedTask ){
        
        //Add the task to the map
        synchronized(theRemoteFileIOMap){
            theRemoteFileIOMap.put( Integer.parseInt(passedTask.getTaskId()), passedTask);
        }
        
        theFsFrame.addTask(passedTask);
    }

    //=================================================================
    /**
     * 
     * @param theTaskId 
     */
    @Override
    public void cancelTask( int passedId ) {
        
        RemoteFileIO theTask;
        synchronized(theRemoteFileIOMap){
            theTask = theRemoteFileIOMap.remove(passedId);
        }

        //Send out task fin
        if(theTask != null){

            //Send a cancel message to the client if the task is active
            //Cancel any file transfers that might be going on.
            try {                
               
                FileMessageManager aFMM = FileMessageManager.getFileMessageManager();
                if( aFMM != null ){
                
                    aFMM.cancelFileTransfer( theHostId, passedId);
                    ControlMessageManager aCMManager = ControlMessageManager.getControlMessageManager();
                    if( aCMManager != null ){                       
                        TaskStatus cancelMessage = new TaskStatus( passedId, RemoteFileIO.TASK_CANCELLED, theHostId );
                        aCMManager.send(cancelMessage);
                    }

                }
                
            } catch ( IOException ex) {
                DebugPrinter.printMessage( NAME_Class, "cancelTask", ex.getMessage(), ex );     
            }

            //Set the state to cancelled
            theTask.setState( RemoteFileIO.TASK_CANCELLED);                 

        }
    }
    
      //========================================================================
    /**
     * 
     * @return 
     */
    @Override
    public Component getParentComponent(){
        return theFsFrame;
    }

    //=================================================================
    /**
     * 
     * @param clientId
     * @return 
     */
    @Override
    public File getDownloadDirectory() {
        return new File(theHostName, "download");
    }

    //=================================================================
    /**
     * 
     * @return 
     */
    @Override
    public FileOperationUpdater getUpdater() {
        return theFsFrame.getUpdater();
    }

    //=================================================================
    /**
     * 
     * @param theRemoteTask
     * @param currentRow 
     */
    @Override
    public void removeTask(RemoteFileIO theRemoteTask) {
        theFsFrame.removeTask(theRemoteTask);        
    }

    //=================================================================
    /**
     * 
     * @return 
     */
    @Override
    public ProgressListener getProgressListener() {
        return this;
    }

    //=================================================================
    /**
     * 
     * @param msgId
     * @param progress 
     */
    @Override
    public void progressChanged(int msgId, int progress) {
        
        RemoteFileIO theRemoteFITask;
        synchronized(theRemoteFileIOMap){
            theRemoteFITask = theRemoteFileIOMap.get(Integer.valueOf(msgId));
        }

        if(theRemoteFITask != null){
            theRemoteFITask.setStateProgress(progress);
            List<RemoteFileIOListener> theListeners = theRemoteFITask.getRemoteListeners();
            for(RemoteFileIOListener aListener : theListeners){
                aListener.taskChanged(theRemoteFITask);
            }
        }
    }

    //=================================================================
    /**
     * Handle the value change
     * @param e 
     */
    @Override
    public void fileJTableValueChanged(ListSelectionEvent e) {
        theFsFrame.tableValueChanged(e);
    }

     //=================================================================
    /**
     * 
     * @param e 
     */
    @Override
    public void fileTreePanelValueChanged(TreeSelectionEvent anEvent) {
        
        TreePath aTreePath = theFsFrame.getFileTreePanel().getJTree().getSelectionPath();
        if( aTreePath != null ){
            DefaultMutableTreeNode node = theFsFrame.getFileTreePanel().getTreeNode( anEvent.getPath() );    
            getChildren( node );
        }

        //Enable or disable the upload and download button
        theFsFrame.setFileIOButtonEnablements( false, false );
    }

    //=================================================================
    /**
     * 
     */
    public void refreshFileSystemJTree() {
        JTree theJTree = theFsFrame.getFileTreePanel().getJTree();
        TreePath aTreePath = theJTree.getSelectionPath();
        theJTree.clearSelection();
        theJTree.setSelectionPath(aTreePath);       
    }

    //=================================================================
    /**
     * 
     * @param aFileNode 
     */
    @Override
    public void selectNodeInTree(FileNode passedFileNode) {
        
        JTree theJTree = theFsFrame.getFileTreePanel().getJTree();
        DefaultMutableTreeNode aNode = (DefaultMutableTreeNode) theJTree.getLastSelectedPathComponent();
        Enumeration<DefaultMutableTreeNode> e = aNode.breadthFirstEnumeration();
        DefaultMutableTreeNode currentNode;
        while( e.hasMoreElements() ) {
            currentNode = e.nextElement();
            Object anObj = currentNode.getUserObject();
            if( anObj instanceof IconData){
                anObj = ((IconData)anObj).getObject();
                if( anObj instanceof FileNode ){            
                    FileNode aFileNode = (FileNode)anObj;
                    if( passedFileNode.equals(aFileNode) ) {
                        TreePath objectPath = new TreePath( currentNode.getPath() );
                        theJTree.setSelectionPath(objectPath);  
                        break;
                    }
                }
            }
        }
        
    }

}
