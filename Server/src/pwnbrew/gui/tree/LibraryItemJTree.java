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
 * LibraryItemJTree.java
 *
 * Created on June 21, 2013 8:19:21 PM
 */

package pwnbrew.gui.tree;

//import pwnbrew.controllers.CommandController;
//import pwnbrew.controllers.JobSetController;
import pwnbrew.library.LibraryItemController;
//import pwnbrew.controllers.ScriptController;
import pwnbrew.xmlBase.XmlBase;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import pwnbrew.generic.SearchableJTree;
import pwnbrew.host.HostController;
import pwnbrew.library.Ancestor;
import pwnbrew.utilities.GuiUtilities;

/**
 *
 *  
 */
public class LibraryItemJTree extends SearchableJTree {
    
    
     
    //****************************************************************************
    /**
    * Returns a list of {@link DefaultMutableTreeNode}s for the passed {@link LibraryItemController} within the
    * passed {@link JTree}.
    *
    * @param theController  the object whose path is to be found in {@code theTree}
    *
    * @return  the {@link DefaultMutableTreeNode} to the passed object ({@code null} if it is not found})
    */
    public List<DefaultMutableTreeNode> findAllNodesInTree( LibraryItemController theController ) {

        List<DefaultMutableTreeNode> nodeList = new ArrayList<>();
        if( theController != null ) {

            DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode)getModel().getRoot();
            Enumeration<DefaultMutableTreeNode> e = rootNode.breadthFirstEnumeration();
            DefaultMutableTreeNode currentNode;

            //Iterate through the nodes
            while( e.hasMoreElements() ) {
                
                currentNode = e.nextElement();
                Object theObj = currentNode.getUserObject();
                if(theObj instanceof LibraryItemController){

                    LibraryItemController currCtrler = (LibraryItemController)theObj;
                    Object currObj = currCtrler.getObject();
                    Object anObj = theController.getObject();
                    
                    if( currObj instanceof XmlBase && anObj instanceof XmlBase){
                        
                        XmlBase currXMB = (XmlBase) currObj;
                        XmlBase theXMB = (XmlBase) anObj;
                    
                        if( currXMB.getClass() == theXMB.getClass() && theXMB.getName().equals(currXMB.getName()) ) {
                            nodeList.add( currentNode );
                        }
                        
                    } else {
                        
                        //Default to the equal operator
                        if( currObj.equals(anObj)){
                            nodeList.add( currentNode );
                        }
                    }
                }
            }

        }

        return nodeList;
    }
     
    //==========================================================================
    /**
    * Returns the {@link DefaultMutableTreeNode} to the passed {@code objectToFind} within the
    * passed {@link JTree}.
    *
    * @param theController  the object whose path is to be found in {@code theTree}
    *
    * @return  the {@link DefaultMutableTreeNode} to the passed object ({@code null} if it is not found})
    */
    public DefaultMutableTreeNode findNodeInTree( LibraryItemController theController ) {

        DefaultMutableTreeNode retNode = null;
        if( theController != null ) {

            DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode)getModel().getRoot();
            
            //Get the first node
            Enumeration<DefaultMutableTreeNode> e = rootNode.depthFirstEnumeration();
            DefaultMutableTreeNode currentNode;

            //Iterate through the nodes
            while( e.hasMoreElements() ) {
                
                currentNode = e.nextElement();
                Object theObj = currentNode.getUserObject();
                if(theObj instanceof LibraryItemController){

                    LibraryItemController currCtrler = (LibraryItemController)theObj;
                    Object currObj = currCtrler.getObject();
                    Object anObj = theController.getObject();
                    
                    if( currObj instanceof XmlBase && anObj instanceof XmlBase){
                        
                        XmlBase currXMB = (XmlBase) currObj;
                        XmlBase theXMB = (XmlBase) anObj;

                        if( currXMB.getClass() == theXMB.getClass() && theXMB.getName().equals(currXMB.getName()) 
                                && currXMB.getId().equals(theXMB.getId())) {
                            retNode = currentNode;
                            break;
                        }
                        
                    } else {
                        
                        //Default to the equal operator
                        if( currObj.equals(anObj)){
                            retNode = currentNode;
                            break;
                        }
                    }
                }
            }

        }

        return retNode;
    }
    
    
    // ==========================================================================
    /**
    * Returns the object of the node nearest to the given {@link DefaultMutableTreeNode}.
    * <p>
    * If the given {@code DefaultMutableTreeNode} is null, this method does nothing and returns null.
    *
    * @param node the node in question
    *
    * @return the {@code Object} of the node nearest to the given node
    *
    * @see getNearestNode()
    */
    public static LibraryItemController getNearestNodeObject( DefaultMutableTreeNode node ) {

        LibraryItemController rtnObject = null;
        DefaultMutableTreeNode nearestNode = GuiUtilities.getNearestNode( node ); 
        if( nearestNode != null ) { 
            rtnObject = (LibraryItemController)nearestNode.getUserObject(); 
        }

        return rtnObject;

    }
    
    //==========================================================================
    /**
    * Returns the {@link LibraryItemController } contained in the currently selected tree node.
    * <p>
    * If there are no {@code LibraryItemController}s in the tree, this method returns null.
    *
    * @return the {@code LibraryItemController} contained in the currently selected tree node
    */
    public List<LibraryItemController> getSelectedObjectControllers() {

        TreePath[] selPaths = getSelectionPaths();
        if( selPaths == null || selPaths.length == 0 ) { 
            //There's nothing to return.
            return null; //Do nothing
        }

        List<LibraryItemController> theControllerList = new ArrayList<>();

        //Get the last-selected node
        for( TreePath aPath : selPaths ){
            DefaultMutableTreeNode theSelectedNode = (DefaultMutableTreeNode)aPath.getLastPathComponent();
            if ( theSelectedNode != null ) {
                Object theUserObject = theSelectedNode.getUserObject();
                if(theUserObject instanceof LibraryItemController ){
                    theControllerList.add((LibraryItemController)theUserObject);
                }
            }
        }

        return theControllerList;

    }
    
    // ==========================================================================
    /**
     * Wrapper method for the following getLibraryItemControllers function.
     * @param aHostController
     * @param passedClass
     * @return 
    */
    public List<LibraryItemController> getLibraryItemControllers( HostController aHostController, Class<?> passedClass ) {
        
        DefaultMutableTreeNode rootNode = findNodeInTree(aHostController);
        return getLibraryItemControllers( passedClass, rootNode);
        
    }  
    
    // ==========================================================================
    /**
     * Wrapper method for the following getLibraryItemControllers function.
     * @param passedClass
     * @return 
    */
    public List<LibraryItemController> getLibraryItemControllers( Class<?> passedClass ) {
        
        DefaultTreeModel theModel = (DefaultTreeModel)getModel();
        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) theModel.getRoot();
        
        return getLibraryItemControllers( passedClass, rootNode);
        
    }  
    
     // ==========================================================================
    /**
     * Returns a list of all of the {@link LibraryItemController}s.
     * 
     * @return a {@link List} of the {@code LibraryItemController}s; if there are no {@code LibraryItemController}s
     * the {@code List} will be empty
    */
    private List<LibraryItemController> getLibraryItemControllers( Class passedClass, DefaultMutableTreeNode rootNode ) {
        
        List<LibraryItemController> theList = new ArrayList<>();
        
        //Loop throught the children and add the objects
        for( int i =0; i < rootNode.getChildCount(); i++  ){
            DefaultMutableTreeNode aNode = (DefaultMutableTreeNode) rootNode.getChildAt(i);
            Object anObj = aNode.getUserObject();
            
            if( anObj instanceof LibraryItemController ){
                
                if( anObj instanceof HostController ){
                    theList.addAll( getLibraryItemControllers( passedClass, aNode ));
                }
                
                LibraryItemController theController =  (LibraryItemController)anObj;
                if( passedClass == null ){
                    theList.add( theController );
                } else if( anObj.getClass() == passedClass ){
                    theList.add( theController );
//                } else if( anObj instanceof CommandController && passedClass == CommandController.class ){
//                    theList.add( theController );
//                } else if( anObj instanceof JobSetController && passedClass == JobSetController.class ){
//                    theList.add( theController );
//                } else if( anObj instanceof HostController && passedClass == HostController.class ){
//                    theList.add( theController );
                }
            }
        }
         
        return theList;
    }
      
    // ==========================================================================
    /**
    *   Returns the full list of {@link LibraryItemController} objects that have dirty objects.
    *
    * @return  the full list of {@link LibraryItemController} objects that have dirty objects
    *
    * @see #getEditedObjects()
    */
    public List<LibraryItemController> getEditedObjectControllers() {
        
        List<LibraryItemController> editedObjectControllers = new ArrayList<>();

        List<LibraryItemController> theControllerList = getLibraryItemControllers( null );
        for( LibraryItemController aController : theControllerList ) {
            
            if( aController.isDirty() ) {
                editedObjectControllers.add( aController );
            }
        }

        return editedObjectControllers;
    }
    
      
    
    // ==========================================================================
    /**
    * Determines if there are currently any unsaved changes
    *
    * @return <tt>true</tt> if any {@code Object} has unsaved changes, <tt>false</tt>
    * otherwise
    */
    public boolean hasUnsavedChanges() {

        boolean rtnBool = false;

        //Check if any of the scripts need saving
        List<LibraryItemController> editedObjCtrlList = getEditedObjectControllers();
        if(!editedObjCtrlList.isEmpty()){
            rtnBool = true;
        }

        //Else, ignore it / Should never happen
        return rtnBool;

    }/* END hasUnsavedChanges() */
         
    // =======================================================================
    /**
    * Adds {@code theObjectToAdd} as a child node under {@code parentNode} at the specified {@code index}.
    *
    * @param theObjectToAdd  the {@link XmlBase} object to be added to the tree
    * @param parentNode      the node under which this object will be added ({@code null}
    *                        to set {@code theObjectToAdd} as the root of the tree)
    * @param index           the child index of {@code parentNode} at which to add {@code theNodeToAdd},
    *                        if {@code null}, it will be added as the last child of {@code parentNode}
     * @return 
    *
    */
    public DefaultMutableTreeNode addObjectToTree( Object theObjectToAdd, DefaultMutableTreeNode parentNode, Integer index ) {

        DefaultMutableTreeNode theNodeToAdd = new DefaultMutableTreeNode( theObjectToAdd );

        if( theObjectToAdd instanceof LibraryItemController ){
            theNodeToAdd = new IconNode( theObjectToAdd, theObjectToAdd instanceof Ancestor );
        }

        if ( index == -1 ) {
            //Add the node at the end of the children of the parentNode
            index = parentNode.getChildCount();
        }

        ((DefaultTreeModel)getModel()).insertNodeInto( theNodeToAdd, parentNode, index );
       
        //Add the children of the node
        if( theObjectToAdd instanceof Ancestor ){
            
            Ancestor anAncestor = (Ancestor)theObjectToAdd;
            List<LibraryItemController> aList = anAncestor.getChildren();
            
            //Loop through the children
            for( LibraryItemController aChild : aList){
                addObjectToTree( aChild, theNodeToAdd, -1);
            }
        }
        
        
        return theNodeToAdd;

    }

    // ==========================================================================
    /**
     * Returns a controller with the same name and class as the one provided.
     * 
     * @param passedHostname
     * @param itemName
     * @param passedClassName
     * @return a {@link List} of the {@code LibraryItemController}s; if there are no {@code LibraryItemController}s
     * the {@code List} will be empty
    */
    public LibraryItemController getLibraryItemController(String passedHostname, String passedClassName, String itemName ) {
        
        LibraryItemController theController = null;
        DefaultTreeModel theModel = (DefaultTreeModel)getModel();
        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) theModel.getRoot();
        
        //Loop throught the children and add the objects
        for( int i =0; i < rootNode.getChildCount(); i++  ){
            DefaultMutableTreeNode hostNode = (DefaultMutableTreeNode) rootNode.getChildAt(i);
            Object anObj = hostNode.getUserObject();
            
            //Get the host first
            if( anObj instanceof HostController ){

                HostController aHostController = (HostController)anObj;
                if( aHostController.getItemName().equals( passedHostname )){
                    
                    for( int j =0; j < hostNode.getChildCount(); j++  ){
                        
                        //Get the child node
                        DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) hostNode.getChildAt(j);
                        anObj = childNode.getUserObject();
                        
                        if( anObj instanceof LibraryItemController ){
                            LibraryItemController aController = (LibraryItemController)anObj;
                            if( aController.getObject().getClass().getSimpleName().equals( passedClassName ) && itemName.equals( aController.getItemName() )){
                                theController = aController;
                                break;
                            }
                        }
                    }
                    
                    //Break out of the other loop
                    if( theController != null ){
                        break;
                    }
                    
                }
            
            }
        }
         
        return theController;
    }
    
     /**
     * Returns the <code>TreeModel</code> that is providing the data.
     *
     * @return the <code>TreeModel</code> that is providing the data
     */
    @Override
    public MainGuiTreeModel getModel() {
        return (MainGuiTreeModel)treeModel;
    }

}
