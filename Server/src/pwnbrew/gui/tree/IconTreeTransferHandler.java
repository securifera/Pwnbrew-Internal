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
* IconTreeTransferHandler.java
*
* Created on June 24, 2013, 10:17:31 PM
*/

package pwnbrew.gui.tree;

import pwnbrew.controllers.MainGuiController;
//import pwnbrew.controllers.JobController;
//import pwnbrew.controllers.JobSetController;
import pwnbrew.library.LibraryItemController;
import pwnbrew.xmlBase.XmlBase;
//import pwnbrew.scripting.languages.ScriptingLanguage;
import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.InvalidDnDOperationException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import pwnbrew.gui.MainGui;
import pwnbrew.host.HostController;
import pwnbrew.library.Ancestor;
import pwnbrew.log.Log;
import pwnbrew.misc.FileFilterImp;
import pwnbrew.utilities.Utilities;

/**
 *
 *  
 */

public class IconTreeTransferHandler extends TransferHandler {

    private final MainGuiController theMainController;
    private final JTree mainJTree;

    private static final String NAME_Class = IconTreeTransferHandler.class.getSimpleName();

    //========================================================================
    /**
     * Constructor
     * @param parentGui
     * @param passedJTree 
     */
    public IconTreeTransferHandler( MainGuiController parentGui, JTree passedJTree ) {
        theMainController = parentGui;
        mainJTree = passedJTree;
    }

    //Called to decided if the object being dragged can be dropped into the tree
    @Override
    public boolean canImport(TransferHandler.TransferSupport info) {

        if (!info.isDrop()) {
            return false;
        }

        Transferable theTransferable = info.getTransferable();

        //Access private member of transferableproxy to decided
        //if the transfer is local or not
        boolean isLocal;
        try{
            Class transferableClass = theTransferable.getClass();
            Field m = transferableClass.getDeclaredField("isLocal");
            m.setAccessible(true);
            isLocal = (boolean)m.getBoolean(theTransferable);

            if(isLocal){
                int[] selectionRows = mainJTree.getSelectionRows(); //Get the rows that have a selected node
                if( selectionRows == null || selectionRows.length == 0 ) { //If there are no selected rows...
                    return false; //Do nothing
                }
            }
            
        } catch (NoSuchFieldException | SecurityException | IllegalAccessException ex) {
            Log.log(Level.WARNING, NAME_Class, "canImport()", ex.getMessage(), ex);
        }

        //Support files dropping and node moving
        try {
            if (!info.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                return false;
            } else {

                Object theObject = theTransferable.getTransferData(DataFlavor.javaFileListFlavor);

                //Drag and Drop from outside the application
                if(theObject instanceof List){
                    List<File> fileList = (List<File>)theObject;
                    FileFilterImp theScriptFilter = Utilities.getFileFilter();

                    //See if the files all pass
                    if( theScriptFilter != null ){
                        MainGui theMainGui = (MainGui) theMainController.getObject();
                        for(File aFile : fileList){

                            if(!theScriptFilter.accept(aFile) && !theMainGui.getXmlFilter().accept( aFile )){
                                return false;
                            }
                        }
                    }

                }
                info.setShowDropLocation(true);
            }
        } catch (IOException | UnsupportedFlavorException | InvalidDnDOperationException ex) {
            Log.log(Level.WARNING, NAME_Class, "canImport()", ex.getMessage(), ex);
        }

        return true;
    }

    // ==========================================================================
    /**
    * Handles the dropping of objects in the JTree
     * @param info
     * @return 
    */
    @Override
    public boolean importData(TransferHandler.TransferSupport info) {

        // fetch the drop location
        JTree.DropLocation dl = (JTree.DropLocation)info.getDropLocation();

        // fetch the path and child index from the drop location
        TreePath destPath = dl.getPath();
        int childIndex = dl.getChildIndex();

        try {

            Transferable theTransferable = info.getTransferable();
            MainGui theMainGui = (MainGui) theMainController.getObject();

            if (theTransferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {

                Object theObject = theTransferable.getTransferData(DataFlavor.javaFileListFlavor);

                //Drag and Drop from outside the application
                if(theObject instanceof List){

//                    List<File> fileList = (List<File>)theObject;
//                    if(!fileList.isEmpty()){
//                        ArrayList<File> newList = new ArrayList<>();
//                        for(File currFile : fileList){
//
//                            if( ScriptingLanguage.getFileFilter().accept(currFile)){
//                                newList.add(currFile);
//                            } else if( !currFile.isDirectory() && theActGui.getXmlFilter().accept( currFile ) ) {
//                                theActController.importBundle( currFile );
//                            } else {
//                                //Reject whatever was dragged in
//                            }
//                        }
//                        theActGui.dropFilesAtIndex(newList.toArray(new File[newList.size()]), childIndex);
//                    }
                
                //Drag and Drop from the JTree
                } else if(theObject instanceof TreePath[]){

                    DefaultTreeModel theTreeModel = ((DefaultTreeModel)mainJTree.getModel());
                    DefaultMutableTreeNode destNode = (DefaultMutableTreeNode)destPath.getLastPathComponent();
                    Object theDestObj = destNode.getUserObject();

                    TreePath[] thePathArr = ((TreePath[])theObject);
                    for(TreePath aTreePath : thePathArr){

                        DefaultMutableTreeNode sourceNode = (DefaultMutableTreeNode)aTreePath.getLastPathComponent();
                        Object theSourceObj = sourceNode.getUserObject();

                        //Moving out of a ScriptSet to somewhere else
                        DefaultMutableTreeNode currSourceParentNode = (DefaultMutableTreeNode)sourceNode.getParent();
                        Object currParentObj = currSourceParentNode.getUserObject();
                        
                        //Check first if the destination allows children
                        if(!destNode.getAllowsChildren()){
                            return false;
                        }

//                        if( theDestObj instanceof JobSetController ) { //If the destination is a JobSetController...
//                            //The user dragged something into a JobSet.
//                            
//                            if( !( theSourceObj instanceof JobController ) ) //If the source object is not a JobController...
//                                continue; //Skip it / Only Jobs can be added to a JobSet.
//                            
//                            //The user dragged a Job into a JobSet.
//
//                            JobController sourceJobController = (JobController)theSourceObj;
//                            JobSetController destinationJobSetController = (JobSetController)theDestObj;
//
//                            if( currParentObj instanceof JobSetController ) { //If the source node's parent node's Object is a JobSetController...
//                                //The user dragged a Job from a JobSet to a JobSet.
//                                
//                                if( currParentObj == theDestObj ) { //If the transfer's destination is the same as the source's parent node...
//                                    //The user dragged a Job from a JobSet to the same JobSet.
//                                    
//                                    destinationJobSetController.insertChild( sourceJobController, childIndex ); //Move the Job within the JobSet
//                                    addNodeToTree( sourceNode, destNode, childIndex ); //Move the Job's node to the new position
//                                    theMainGui.setDirtyFlag( destinationJobSetController, true, false ); //Update the gui
//
//                                } else { //If the transfer's destination is NOT the same as the source's parent node...
//                                    //The user dragged a Job from one JobSet to another JobSet.
//
//                                    //If the destination JobSet does not already contain the Job...
//                                    boolean found = false;                                    
//                                    List<LibraryItemController> theContList = destinationJobSetController.getChildren();
//                                    for( LibraryItemController aCont : theContList ){
//                                        if( aCont.getItemName().equals(sourceJobController.getItemName()) 
//                                                && aCont.getItemTypeDisplayName().equals( sourceJobController.getItemTypeDisplayName()) ){
//                                            found = true;
//                                        }
//                                    }
//                                    
//                                    if( !found ) {
//                                        
//                                        //Remove the Job from the source JobSet...
//                                        JobSetController sourceJobSet = (JobSetController)currParentObj;
//                                        sourceJobSet.removeChild( sourceJobController ); //Remove the Job from the JobSet
//                                        theTreeModel.removeNodeFromParent( sourceNode ); //Remove the Job's node from the JobSet's node
//                                        theMainGui.setDirtyFlag( sourceJobSet, true, false ); //Update the gui
//                                        
//                                        //Add the Job to the destination JobSet...
//                                        destinationJobSetController.insertChild( sourceJobController, childIndex ); //Add the Job to the JobSet
//                                        addNodeToTree( sourceNode, destNode, childIndex ); //Add the Job's node to the JobSet's node
//                                        theMainGui.setDirtyFlag( destinationJobSetController, true, false ); //Update the gui
//
//                                    } //Else, the destination JobSet already contains the Job; do nothing.
//                                    
//                                } //End of "} else { //If the transfer's destination is NOT the same as the source's parent node..."
//
//                            } else if( currParentObj instanceof HostController ) { //If the source node's parent node's Object is not a JobSetController...
//                                //The user dragged a Job into a JobSet from somewhere that isn't a JobSet.
//
//                                //This will be the case if the source node's parent node is the root node. (It's Object is a Folder.)
//                                HostController theHostController = (HostController)currParentObj;
//                                if( destinationJobSetController.getHostController().equals(theHostController) ){
////                                if( theHostController.isLocalHost()){
//                                    //If the destination JobSet already contains the Job...
//                                    if( destinationJobSetController.contains( sourceJobController ) )
//                                        continue; //Do nothing
//                                    //Else, if the destination JobSet does not already contain the Job...
//
//                                    //Add the Job to the destination JobSet...
//                                    destinationJobSetController.insertChild( sourceJobController, childIndex ); //Add the Job to the JobSet
//                                    addNodeToTree( new IconNode( sourceJobController, sourceJobController instanceof Ancestor ), destNode, childIndex );
//                                    theMainGui.setDirtyFlag( destinationJobSetController, true, false ); //Update the gui
//                                
//                                } else {
//                                
//                                    //Remove from source 
//                                    //Must be in this order
//                                    ((LibraryItemController)theSourceObj).deleteFromLibrary();
//                                    theTreeModel.removeNodeFromParent( sourceNode ); //Remove the Job's node from the JobSet's node
//                                    
//                                    continue;
//                                }
//                            }
//
//                            mainJTree.expandPath( destPath ); //Expand the destination path

//                        } else 
                        if( theDestObj instanceof HostController ){
                            
                            HostController theDestHostController = (HostController)(theDestObj);
                                    
                            //If the destination is a jobset
//                            if( currParentObj instanceof JobSetController ) {
//
//                                JobSetController jobSetController = (JobSetController)currParentObj;
//                                if( jobSetController.getHostController().equals(theDestHostController)){
//                                    //The destination is the root folder. (The user dragged the item to
//                                    //  the empty space in the panel; not to a particular node.)
//                                    JobController jobController = (JobController)theSourceObj;
//                                                             jobSetController.removeChild( jobController ); //Remove the Job from the JobSet
//                                    theTreeModel.removeNodeFromParent( sourceNode ); //Remove the Job's node from the JobSet's node
//
//                                    theMainGui.setDirtyFlag( jobSetController, true, true ); //Update the gui
//                                    continue;
//                                    
//                                } else {
//                                    
//                                    LibraryItemController srcObjController = (LibraryItemController)theSourceObj;
//                                    XmlBase anXMB = (XmlBase) srcObjController.getObject();
//                                    
//                                    //If the object already exists
//                                    if( theMainController.getControllerByObjName( theDestHostController.getItemName(), 
//                                            anXMB.getClass().getSimpleName(), anXMB.getName() ) != null) {
//                                        continue;
//                                    }
//                                    
//                                    //Add the Job to the destination JobSet...
//                                    LibraryItemController copiedController = srcObjController.copy();
//                                    
//                                    //Must be in this order
//                                    addNodeToTree( new IconNode( copiedController, copiedController instanceof Ancestor ), destNode, childIndex );
//                                    copiedController.saveToDisk();
//                                 
//                                    mainJTree.expandPath( destPath ); //Expand the destination path
//                                    continue;
//                                    
//                                }
                                    
//                            } else 
                            if( currParentObj instanceof HostController ){
                                
                                //Reorder the object
                                if( theDestObj.equals( currParentObj ) ){
                                    addNodeToTree(sourceNode, destNode, childIndex);
                                    mainJTree.expandPath( destPath ); //Expand the destination path
                                    continue;
                                }
                                
                                LibraryItemController srcObjController = (LibraryItemController)theSourceObj;
                                XmlBase anXMB = (XmlBase) srcObjController.getObject();
                                
                                //If the object already exists
                                if( theMainController.getControllerByObjName( theDestHostController.getItemName(), 
                                        anXMB.getClass().getSimpleName(), anXMB.getName() ) != null) {
                                    continue;
                                }
                                
                                
                                //Check if the source is the loopback or remote
//                                HostController srcHostController = (HostController)currParentObj;
//                                if( srcHostController.isLocalHost()){
                                     
                                    //Add the Job to the destination JobSet...
                                    LibraryItemController copiedController = srcObjController.copy();
                                    
                                    //Must be in this order
                                    IconNode newNode = new IconNode( copiedController, copiedController instanceof Ancestor );
                                    addNodeToTree( newNode, destNode, childIndex );
                                    copiedController.saveToDisk();
                                    
                                    //Add the child nodes to the jobset
//                                    if( theSourceObj instanceof JobSetController ){
//                                        Enumeration<DefaultMutableTreeNode> e = sourceNode.children();
//                                        //Iterate through the nodes
//                                        while( e.hasMoreElements() ) {
//
//                                            DefaultMutableTreeNode currentNode = e.nextElement();
//                                            Object theObj = currentNode.getUserObject();
//                                            if( theObj instanceof JobController ){
//
//                                                //Add the Job to the destination JobSet...
//                                                srcObjController = (JobController)theObj;
//                                                copiedController = srcObjController.copy();
//
//                                                //Must be in this order
//                                                addNodeToTree( new IconNode( copiedController, copiedController instanceof Ancestor ), newNode, childIndex );
//                                                copiedController.saveToDisk();
//
//                                            }
//                                        }
//                                    }                                
                                 
                                    mainJTree.expandPath( destPath ); //Expand the destination path
                                    continue;
                                    
//                                } else {
//                                    
//                                    //Remove from source 
//                                    //Must be in this order
//                                    ((LibraryItemController)theSourceObj).deleteFromLibrary();
//                                    theTreeModel.removeNodeFromParent( sourceNode ); //Remove the Job's node from the JobSet's node
//                                    
//                                    continue;
//                                    
//                                }                              
                                
                            } 
                           
//                            addNodeToTree(sourceNode, destNode, childIndex);
//                            mainJTree.expandPath( destPath ); //Expand the destination path
                            
                        } 
                    }
                }

                return true;
            }

        } catch (UnsupportedFlavorException | IOException ex) {
            return false;
        }

        cancelDnD(info);
        return false;

    }


    // ==========================================================================
    /**
    *  This method adds the node to the tree
    *
    *  @param  sourceNode
    *  @param  destNode
    *  @param  childIndex
    *
    *  @return the <code>length</code> of the encoded string
    */
    private void addNodeToTree(DefaultMutableTreeNode sourceNode, DefaultMutableTreeNode destNode, int childIndex){

        DefaultTreeModel theTreeModel = ((DefaultTreeModel)mainJTree.getModel());

        if ( childIndex != -1 ) {

            //These account for the end of the list or if a node is being moved down the list
            int currentIndex = destNode.getIndex(sourceNode);
            if((childIndex == destNode.getChildCount() && childIndex != 0) ||
            (currentIndex != -1 && childIndex > currentIndex) ){
                childIndex = childIndex - 1;
            }
        }

        //Remove it if it exists
        try {
            theTreeModel.removeNodeFromParent(sourceNode);
        } catch(IllegalArgumentException ex){
            ex = null;
        }

        //Add to the end
        if ( childIndex == -1 ) {
            childIndex = destNode.getChildCount();
        }

        //Insert the node
        theTreeModel.insertNodeInto( sourceNode, destNode, childIndex );

    }

    //==========================================================================
    /**
    * 
    * @param support 
    */
    private void cancelDnD(TransferSupport support) {

        //TransferSupport.setDNDVariables(Component component, DropTargetEvent event)
        //Call setDNDVariables(null, null) to free the component.
        try{
            Method m = support.getClass().getDeclaredMethod("setDNDVariables", new Class[] { Component.class, DropTargetEvent.class });
            m.setAccessible(true);
            m.invoke(support, null, null);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException ex) {
            Log.log(Level.INFO, NAME_Class, "cancelDnD()", ex.getMessage(), ex);
        }
    }


}/* END CLASS IconTreeTransferHandler */
