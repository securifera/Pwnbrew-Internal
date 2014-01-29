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
 * MainGuiController.java
 *
 * Created on June 21, 2013
 */

package pwnbrew.controllers;

import pwnbrew.library.LibraryItemController;
import pwnbrew.exception.XmlBaseCreationException;
import pwnbrew.xmlBase.Bundle;
import pwnbrew.xmlBase.XmlBase;
import pwnbrew.exception.FileContentException;
import pwnbrew.xmlBase.XmlBaseFactory;
import pwnbrew.xmlBase.job.Job;
import pwnbrew.xmlBase.job.JobSet;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import pwnbrew.Server;
import pwnbrew.gui.MainGui;
import pwnbrew.gui.dialogs.OptionsJDialogListener;
import pwnbrew.gui.dialogs.TasksJDialog;
import pwnbrew.gui.input.ValidTextField;
import pwnbrew.gui.panels.RunnerPane;
import pwnbrew.gui.tree.IconNode;
import pwnbrew.gui.tree.MainGuiTreeModel;
import pwnbrew.gui.tree.LibraryItemJTree;
import pwnbrew.host.Host;
import pwnbrew.host.HostController;
import pwnbrew.host.HostFactory;
import pwnbrew.host.HostListener;
import pwnbrew.host.Session;
import pwnbrew.library.Ancestor;
import pwnbrew.logging.Log;
import pwnbrew.logging.LoggableException;
import pwnbrew.manager.ServerManager;
import pwnbrew.misc.Constants;
import pwnbrew.misc.Directories;
import pwnbrew.utilities.FileUtilities;
import pwnbrew.misc.GarbageCollector;
import pwnbrew.misc.ProgressListener;
import pwnbrew.network.control.ControlMessageManager;
import pwnbrew.network.control.messages.Migrate;
import pwnbrew.network.control.messages.RelayStart;
import pwnbrew.network.control.messages.RelayStop;
import pwnbrew.network.control.messages.Reload;
import pwnbrew.network.control.messages.TaskNew;
import pwnbrew.network.control.messages.TaskStatus;
import pwnbrew.network.control.messages.Uninstall;
import pwnbrew.network.file.FileMessageManager;
import pwnbrew.tasks.RemoteTask;
import pwnbrew.tasks.RemoteTaskListener;
import pwnbrew.tasks.TaskManager;
import pwnbrew.utilities.GuiUtilities;
import pwnbrew.utilities.SSLUtilities;
import pwnbrew.utilities.Utilities;
import pwnbrew.validation.StandardValidation;

/**
 *
 *  
 */
final public class MainGuiController extends Controller implements ActionListener, TreeSelectionListener,
        JobSetControllerListener, OptionsJDialogListener,
        HostListener, TaskManager, ProgressListener {
    
    public static final String ACTION_RunSelectedItem = "Run Selected Item";
    public static final String ACTION_StopSelectedItem = "Stop Selected Item";
    
    protected MainGui theMainGui = null;  
    private static final String NAME_Class = MainGuiController.class.getSimpleName();
    
    private final List<LibraryItemController> justImportedObjects = new ArrayList<>();
    private Map<String, Class> theActionClassMap;    
        
    protected final ServerManager theServerManager;
    //Map relating the msgid to the task
    private final Map<Integer, RemoteTask> theActiveTaskMap = new HashMap<>();

    
    //===============================================================
    /**
     *  Constructor
     * @param passedParent
    */
    public MainGuiController( ServerManager passedParent ) {  
        theServerManager = passedParent;
        theMainGui = MainGui.createInstance( this ); 
        
        TasksJDialog.setMainGuiController( this );
        theServerManager.addDetectListener( this);
        updateComponents();
        
    }
  
    //===============================================================
    /**
    *  Returns the managed object
    *
     * @return 
    */
    @Override
    public Object getObject() {
        return theMainGui;
    }
    
      
    //===============================================================
    /**
    *  Sets the managed object
    *
     * @param passedObject
    */
    @Override
    public void setObject(Object passedObject) {
        theMainGui = (MainGui)passedObject;
    }

    //===============================================================
    /**
    *  Returns the main root panel
     * @return 
    */
    @Override
    public JPanel getRootPanel() {
        
        JPanel thePanel = null;
        JViewport theVP = theMainGui.getMainScrollPane().getViewport();
        if(theVP != null){

            //Call repaint on the view so that the scriptset will reflect any changes
            thePanel = (JPanel)theVP.getView();
            
        }
        
        return thePanel;
    }
   
    // ==========================================================================
    /**
    * Deletes the currently selected {@link XmlBase} from the Library
    * and updates the displayed tree.
    * <p>
    * If the given {@code boolean} is <tt>true</tt>, the user will be prompted to
    * confirm the delete. Otherwise, the method will simply delete the object.
    *
     * @param selObjects
    * @param prompt a flag to indicating if the user is to be prompted to confirm
    * the deletion
    */
    public void deleteObjects( List<DefaultMutableTreeNode> selObjects, boolean prompt ) {

        //NOTE: If the last object is removed from the JTree, leaving nothing to be selected,
        //  JTree.getLastSelectedPathComponent() and JTree.getSelectionPath() both still
        //  return a non-null and JTree.getSelectionCount() still returns 1.  However,
        //  JTree.getSelectionRows() returns null or an empty array (according to the
        //  JavaDoc) so it is being used here to determine if the tree is in fact empty
        //  before continuing.

        LibraryItemJTree theJTree = theMainGui.getJTree();

        int deleteConfirmation = JOptionPane.YES_OPTION; //By default, confirm the delete
        if( prompt ) { //If prompting the user for confirmation...
            deleteConfirmation = Utilities.confirmDelete( theMainGui, selObjects ); //Prompt the user for confirmation
        }

        //Get the last-selected node
        if( deleteConfirmation == JOptionPane.YES_OPTION ) { //If the delete is confirmed...

            //Remove the nodes
            DefaultMutableTreeNode lastSelectedNode = null;
            for( int i = 0; i < selObjects.size(); i++ ){
                lastSelectedNode = selObjects.get(i);
                removeObjectFromTree( (LibraryItemController)lastSelectedNode.getUserObject() ); //Remove the node and delete its object
            }

            List<LibraryItemController> theControllerList = theJTree.getLibraryItemControllers( null );
            if( !theControllerList.isEmpty() && lastSelectedNode != null ) { //If there is at least one element in the tree...

                //Find the nearest sibling node that can be selected
                Object theNewSelectedNodeObject = LibraryItemJTree.getNearestNodeObject( lastSelectedNode );
                if( theNewSelectedNodeObject == null ) { //If no sibling object is located...
                    theNewSelectedNodeObject = theControllerList.iterator().next(); //Get the first object in the tree
                }

                DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) theJTree.getModel().getRoot();
                if( theNewSelectedNodeObject != null && theNewSelectedNodeObject != rootNode.getUserObject()  ) { //If an object was found to be selected...

                    theJTree.setSelectedObject( theNewSelectedNodeObject ); //Select the object
                    if(theNewSelectedNodeObject instanceof LibraryItemController){
                        theMainGui.populatePanels( (LibraryItemController)theNewSelectedNodeObject ); //Populate the panels with the new object's data
                    }

                } else { //If no object was found to be selected...
                    theMainGui.populatePanels( null ); //Clear the panels
                }

            } else { //If the tree is empty...
                theMainGui.populatePanels( null ); //Clear the panels
            }

        }

    }/* END deleteSelectedObject( boolean ) */
    
    // ==========================================================================
    /**
    * Replaces any {@link DefaultMutableTreeNode}s that manage the {@link LibraryItemController}
    * in the main {@link JTree} and replaces it with the new {@link LibraryItemController}.
    * <p>
    * If the given {@code DefaultMutableTreeNode} is null or is not in the main {@code JTree}
    * this method does nothing.
    *
    * @param prevController the {@code LibraryItemController} to replace
    * @param newController the {@code LibraryItemController} to replace with
    */
    private void replaceObjectInTree( LibraryItemController prevController, LibraryItemController newController ) {

        if( prevController == null || newController == null ) { //If the DefaultMutableTreeNode is null...
            return; //Do nothing
        }

        theMainGui.setCursor( Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR ) );

        try{
     
            //Remove the job from any job sets
            LibraryItemJTree theJTree = theMainGui.getJTree();
            DefaultTreeModel theTreeModel = (DefaultTreeModel) theJTree.getModel();
            List<DefaultMutableTreeNode> nodeList = theJTree.findAllNodesInTree( prevController );
            
            for( DefaultMutableTreeNode aNode : nodeList){
                
                //Remove the current children
                GuiUtilities.removeAllChildren( aNode, theTreeModel );
                aNode.setUserObject(newController);
                
                //Remove the job set controllers children
                if( newController instanceof Ancestor ){
                  
                    Ancestor anAncestor = (Ancestor)newController;
                    List<LibraryItemController> aList = anAncestor.getChildren();

                    //Loop through the children and add them
                    for( LibraryItemController aChild : aList){
                        theJTree.addObjectToTree( aChild, aNode, -1);
                    }
                }
            }
            
            prevController.deleteFromLibrary(); //Delete the underlying object from disk

        } finally{
            theMainGui.setCursor( null );
        }

    }/* END replaceObjectInTree( DefaultMutableTreeNode ) */
    
    // ==========================================================================
    /**
    * Removes the given {@link DefaultMutableTreeNode} from the main {@link JTree}
    * and deletes its {@code Object} from the library.
    * <p>
    * If the given {@code DefaultMutableTreeNode} is null or is not in the main {@code JTree}
    * this method does nothing.
    *
    * @param node the {@code DefaultMutableTreeNode} to remove
    */
    private List<LibraryItemController> removeObjectFromTree( LibraryItemController theController ) {

        if( theController == null ) { //If the DefaultMutableTreeNode is null...
            return null; //Do nothing
        }

        List<LibraryItemController> theParentList = new ArrayList<>();
        theMainGui.setCursor( Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR ) );

        try{
            
             if( theController instanceof JobController ) { 

                JobController aJobController = (JobController) theController;
                //Make sure the job isnt' running
                if( aJobController.isRunning() ) {
                    JOptionPane.showMessageDialog( theMainGui, "Unable to delete job.  The Job is currently running.","Error", JOptionPane.ERROR_MESSAGE );
                    return null;
                } 
                
                Job jobToDelete = (Job)aJobController.getObject();

                //Delete the file
                File rtnFile = new File( Directories.getLocalTasksDirectory(), jobToDelete.getId() );
                if(rtnFile.exists()){
                    FileUtilities.deleteFile( rtnFile ); //Delete any existing incarnation of the directory
                }
                    
                //Remove the object from all job sets
                HostController theHostController = aJobController.getHostController();
                List<LibraryItemController> theObjSet = theMainGui.getJTree().getLibraryItemControllers( theHostController, JobSetController.class );
                for(LibraryItemController aController : theObjSet){

                    JobSetController aJobSetController = (JobSetController)aController;
                    
                    //Remove the controller and add the parent to the list
                    if( aJobSetController.contains( aJobController )){
                        aJobSetController.removeChild( aJobController );
                        aJobSetController.saveToDisk();
                        theParentList.add(aJobSetController);
                    }                    
                }

            }
             
            theController.deleteFromLibrary(); //Delete the underlying object from disk

            //Remove the object
            DefaultMutableTreeNode aNode;
            LibraryItemJTree theJTree = theMainGui.getJTree();
            DefaultTreeModel theTreeModel = (DefaultTreeModel) theJTree.getModel();
            do {
                aNode = theJTree.findNodeInTree( theController );
                if(aNode != null){
                    theTreeModel.removeNodeFromParent(aNode);
                }
            } while ( aNode != null );


        } finally{
            theMainGui.setCursor( null );
        }

        return theParentList;
    }/* END removeNodeAndDeleteObject( DefaultMutableTreeNode ) */
    
    
    // ========================================================================
    /**
     * Searches controller list for an object of type class with the given
     * name.
     * <p>
     * If the given {@code Class} is null, this method returns the first occurring
     * {@code LibraryItemController} that's controlling a {@code LibraryItem} with
     * the given name.
     * If the given name is null or empty this method does nothing and returns null.
     * 
     * @param passedHostname
     * @param passedClassName the type of {@code LibraryItem} for which to search
     * @param name the name of the {@code LibraryItem}
     * 
     * @return the {@link LibraryItemController}
     * 
    */
    public LibraryItemController getControllerByObjName( String passedHostname, String passedClassName, String name) {
        
        if( name == null || name.isEmpty() ) return null;
    
        //Populate the list of appropriate objects
        if(passedClassName != null){            
            return theMainGui.getJTree().getLibraryItemController( passedHostname, passedClassName, name);            
        } 
        
        return null;
        
    }/* END getControllerByObjName( Class, String ) */
     
    // ========================================================================
    /**
     * Runs the currently selected item.
     * <p>
     * If no item is selected this method does nothing.
     * @param riController
     */
    public void runObject( RunnableItemController riController ) {        

        if( riController.isRunning() ) { //If the item is already running...
            theMainGui.informUserItemIsAlreadyRunning( riController );
        } else { //If the item is not already running...

            String cantRunMessage = riController.canRun(); //Determine if the item can be run
            if( cantRunMessage == null ) { //If the item can be run...

                //Prompt the user to confirm the run...
                if( theMainGui.promptUserToConfirmRun( riController ) ) { //If the user confirms the run...
                    riController.runItem( null ); //Run the item
                }

            } else { //If the item can't be run...
                theMainGui.informUserItemCantRun( riController.getItemName(), cantRunMessage );
            }

        }
        
    }/* END runObject() */
    
    // ==========================================================================
    /**
    * Exports the selected object to a bundle
    * <p>
     * @param theObjList
    */
    public void exportLibraryObjects( List<LibraryItemController> theObjList ) {

        File userSelectedFile = null;

        if(theObjList == null){
            JOptionPane.showMessageDialog( theMainGui, "Please select a job/job set before attempting to export.","Error", JOptionPane.ERROR_MESSAGE );
            return;
        }
        
        //Get the object
        JFileChooser theFileChooser = theMainGui.getFileChooser();
        File aFile;
        if( theObjList.size() == 1 ){
            XmlBase theObj = (XmlBase) theObjList.get(0).getObject();
            aFile = new File(theObj.getName());
            theFileChooser.setSelectedFile( aFile );
        } 

        theFileChooser.setFileFilter( Bundle.theFilenameFilterTool ); 
        theFileChooser.setMultiSelectionEnabled( false ); 

        int returnVal = theFileChooser.showDialog( theMainGui, "Export" ); 
        switch( returnVal ) {

            case JFileChooser.CANCEL_OPTION: 
            case JFileChooser.ERROR_OPTION: 
                break; 

            case JFileChooser.APPROVE_OPTION: //If the user approved the selection...
                userSelectedFile = theFileChooser.getSelectedFile(); //Get the file the user selected
                break;
            default:
                break;

        }

        if(userSelectedFile != null){
            //Create a Bundle and add the contents
            Bundle aBundle = new Bundle();
            String bundleSuffix = Bundle.FilenameSuffixStr;
            String exportFileName = userSelectedFile.getName();

            if(!Bundle.theFilenameFilterTool.endsWithCaseInsensitive(exportFileName, bundleSuffix)){
                //If it already has an extension, take it off
                if(exportFileName.contains(".")){
                    exportFileName = exportFileName.split("\\.")[0];
                }
                userSelectedFile = new File(userSelectedFile.getParent(), exportFileName + bundleSuffix);
            }

            try{
                
                aBundle.addToBundle( theMainGui, theObjList );
                aBundle.writeSelfToDisk(userSelectedFile.getParentFile(), userSelectedFile.getName());
            
            } catch(FileContentException ex){

                //Alert the user that the file content write failed and delete the file
                JOptionPane.showMessageDialog( theMainGui, ex.getMessage(),"Error", JOptionPane.ERROR_MESSAGE );
                Log.log(Level.SEVERE, NAME_Class, "exportSelectedObject()", ex.getMessage(), ex);
                FileUtilities.deleteFile(userSelectedFile);

            }  catch ( IOException | LoggableException ex) {

                JOptionPane.showMessageDialog( theMainGui, ex.getMessage(),"Error", JOptionPane.ERROR_MESSAGE );
                Log.log(Level.SEVERE, NAME_Class, "exportSelectedObject()", ex.getMessage(), ex);

            }
        }
    }
    
    
    // ==========================================================================
    /**
    * Cancels the run of the currently selected item.
     * <p>
     * If no item is selected this method does nothing.
     * @param theController
    */
    @Override
    public void cancelRunForCurrentNode( RunnableItemController theController ) {
        
        //Run locally
        HostController theHostController = getParentController(theController);
        if( theHostController != null ){
            
            if( theHostController.isLocalHost() ){

                theController.cancelRun();

            } else {

                //Cancel the job
                if( theHostController.isConnected() && theController instanceof JobController ){

                    int clientId = Integer.parseInt( theHostController.getId() );
                    int theRemoteTaskId = ((JobController)theController).getRemoteTaskId();
                    cancelTask( clientId, theRemoteTaskId );
                    ((JobController)theController).setRemoteTaskId( 0 );

                    //Update the gui
                    RemoteTask theRemoteTask = theActiveTaskMap.get( theRemoteTaskId );
                    if( theRemoteTask != null ){
                        List<RemoteTaskListener> theListeners = theRemoteTask.getRemoteListeners();
                        for(RemoteTaskListener aListener : theListeners){
                            aListener.taskChanged(theRemoteTask);
                        }
                    }
                }
                
            }
        }
    }
    
    
    //=======================================================================
    /**
    * Imports the selected bundle into the application
    * <p>
     * @param userSelectedFile
    */
    public void importBundle( File userSelectedFile ) {

        XmlBase anXRB = null;
        theMainGui.setJustImported( false );
        LibraryItemJTree mainJTree = theMainGui.getJTree();

        if( userSelectedFile == null ) {

            JFileChooser theFileChooser = theMainGui.getFileChooser();
            theFileChooser.setFileFilter( Bundle.theFilenameFilterTool ); 
            theFileChooser.setMultiSelectionEnabled( false ); //Let the user select multiple files

            int returnVal = theFileChooser.showDialog( theMainGui, "Import" ); //Show the dialog
            switch( returnVal ) {

                case JFileChooser.CANCEL_OPTION: //If the user canceled the selecting...
                case JFileChooser.ERROR_OPTION: //If the dialog was dismissed or an error occurred...
                    break; //Do nothing

                case JFileChooser.APPROVE_OPTION: //If the user approved the selection...
                    userSelectedFile = theFileChooser.getSelectedFile(); //Get the file the user selected
                    break;
                default:
                    break;

            }

            //If the file chooser is canceled
            if(userSelectedFile == null ){
                return;
            }

        }

        JLabel message = new JLabel("<html><center>Importing a bundle will overwrite any matching library items.<br>Would you like to continue? &nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp<br><br></center></html>");
        message.setFont(new Font("Tahoma", Font.PLAIN, 12));

        //Prompt the user to confirm the run...
        int usersChoice = JOptionPane.showConfirmDialog( theMainGui, message, //The message
            "Warning!", JOptionPane.YES_NO_OPTION ); //Dialog option

        //If the chosen file is not null and exists
        if( usersChoice == JOptionPane.YES_OPTION && userSelectedFile.exists()){

            try {
                anXRB = XmlBaseFactory.createFromXmlFile( userSelectedFile ); //Reconstruct the XmlBase
            } catch( XmlBaseCreationException ex ) {
                Log.log(Level.SEVERE, NAME_Class, "importBundle()", ex.getMessage(), ex);
            }

            if(anXRB != null && anXRB instanceof Bundle){

                //Iterate through the list and write them to the library
                Bundle aBundle = (Bundle)anXRB;
                List<JobSetController> importedJobSetCtrlList = new ArrayList<>();

                DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode)theMainGui.getJTree().getModel().getLocalRoot();
                for( XmlBase currXB : aBundle.getBundleList() ){

                    //Add them to their appropriate lists
                    LibraryItemController prevController;
                    LibraryItemController aController;
                    if( currXB instanceof JobSet ) {

                        JobSet aJobSet = (JobSet)currXB;
                                            
                        //Create a controller and add it to the map
                        aController = aJobSet.instantiateController( this );
                        importedJobSetCtrlList.add((JobSetController)aController);

                    } else if(currXB instanceof Job){ 
                        
                        //Get a controller for the library item and see if one exists already
                        Job aJob = (Job)currXB;
                        aController = aJob.instantiateController(this);

                        //Find a controller for the object given the class
                        prevController = getControllerByObjName( HostFactory.LOCALHOST, aJob.getClass().getSimpleName(), aJob.getName() );                                   
                        //If a controller doesn't exists, create one
                        if( prevController == null ){

                            //Add to the tree
                            mainJTree.addObjectToTree( aController, parentNode, -1 );

                        } else {

                            //If it already exists set the object and use the old one
                            prevController.setObject( aJob );
                            prevController.updateComponents();
                            aController = prevController;
                        }                           

                        //Set to just imported
                        justImportedObjects.add(aController);
                        aController.setJustImported(true);
                        aController.saveToDisk(); 
                        
                    }                     

                }
                
                //Add the job controllers to each job set
                List importedJobCtrlList = new ArrayList(justImportedObjects);
                for( JobSetController aJobSetCtrl : importedJobSetCtrlList){
                    
                    aJobSetCtrl.addJobControllerReferences( importedJobCtrlList );     
                    XmlBase anObj = (XmlBase) aJobSetCtrl.getObject();
                    LibraryItemController prevController = getControllerByObjName( HostFactory.LOCALHOST, anObj.getClass().getSimpleName(), anObj.getName() );

                    //If a controller already exists then replace it else just add to root
                    if( prevController != null ){
                        replaceObjectInTree( prevController, aJobSetCtrl);
                    } else {
                        mainJTree.addObjectToTree( aJobSetCtrl, parentNode, -1 );
                    }
                    
                    //Save
                    aJobSetCtrl.saveToDisk();
                    
                    //Set just imported flags
                    justImportedObjects.add(aJobSetCtrl);
                    aJobSetCtrl.setJustImported(true);
                }
                
                //Ensure the root node is expanded
                TreePath thePath = GuiUtilities.getTreePath( parentNode );
                mainJTree.expandPath(thePath);

                //Select something
                thePath = GuiUtilities.getTreePath(new DefaultMutableTreeNode(justImportedObjects.get(0)));
                mainJTree.setSelectionPath(thePath);

                //Set just imported flag
                theMainGui.setJustImported(true);

            } else {
                JOptionPane.showMessageDialog( theMainGui, "Unable to import bundle, not a valid XML bundle.","Error", JOptionPane.ERROR_MESSAGE );
            }

        } else {

            if(usersChoice == JOptionPane.YES_OPTION){
                JOptionPane.showMessageDialog( theMainGui, "Unable to import bundle, not a valid XML bundle.","Error", JOptionPane.ERROR_MESSAGE );
            }
        }
    }

    //==========================================================================
    /**
    *   Returns the action class map.
    * 
    * @return 
    */    
    public  Map<String, Class> getActionClassMap(){
        if( theActionClassMap == null ){
            theActionClassMap = Utilities.getActionControllerClassMap();
        }
        return theActionClassMap;
    }
    
    // ==========================================================================
    /**
    *   Callback for when a value changes in the JTree
    *
     * @param e
    */
    @Override //TreeSelectionListener
    public void valueChanged(TreeSelectionEvent e) {

        //Returns the last path element of the selection.
        //Note: This method is useful only when the selection model allows only a single selection.
        if(theMainGui != null){
            JTree mainJTree = theMainGui.getJTree();
            DefaultMutableTreeNode theSelectedNode = (DefaultMutableTreeNode)mainJTree.getLastSelectedPathComponent();

            if ( theSelectedNode != null ) {

                TreePath[] theSelNodes = mainJTree.getSelectionPaths();

                if(theSelNodes != null && theSelNodes.length == 1){
                    LibraryItemController selectedObject = (LibraryItemController)theSelectedNode.getUserObject();
                    theMainGui.populatePanels(selectedObject);
                } 

            }

            if( theMainGui.justImported()){

                for(LibraryItemController aController : justImportedObjects){
                    aController.setJustImported(false);
                }

                justImportedObjects.clear();
                theMainGui.setJustImported(false);
                mainJTree.repaint();
            }
        }
          
    }
    
    //****************************************************************************
    /**
    * Saves the current object to disk
     * @return 
    */
    public boolean saveCurrentObject() {

        boolean rtnBool = true;

        LibraryItemJTree theJTree = theMainGui.getJTree();
        List<LibraryItemController> theControllerList = theJTree.getSelectedObjectControllers(); //Get the selected XmlBase
        for( LibraryItemController aController : theControllerList ){
            aController.saveToDisk();
        }
       
        //Update all edited objects with any changes made in the GUI
        List<LibraryItemController> editedObjects = theJTree.getEditedObjectControllers(); //Get the objects that have unsaved changes
        if(editedObjects.isEmpty()){
            theMainGui.setSaveButtons( false ); //Disable the Save button
        }

        theJTree.repaint();

        return rtnBool;
    }
     
    // ==========================================================================
    /** 
     * Saves all of the {@link XmlBase} objects to the Object Library directory.
     * 
     * @param repaint boolean specifying whether to repaint the gui or not 
     * @return  
    */
    public boolean saveAllObjects( boolean repaint ) {

        //Change to the wait cursor...
        theMainGui.setCursor( Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR ) ); //Change to the wait cursor

        boolean rtnBool = false;

        try {

            LibraryItemJTree theJTree = theMainGui.getJTree();
            //Update all edited objects with any changes made in the GUI
            List<LibraryItemController> editedObjects = theJTree.getEditedObjectControllers(); //Get the objects that have unsaved changes
            for( LibraryItemController aController : editedObjects ){
                aController.saveToDisk();
            }
 
            rtnBool = true; //The save was successful
            theMainGui.setSaveButtons( false ); //Disable the Save button


            //Show that the changes were saved
            if(repaint){
                theJTree.repaint();
            }

        } finally {
            theMainGui.setCursor( null ); //Restore the cursor
        }

        return rtnBool;

    }/* END saveAllObjects() */
      
    
    // ========================================================================
    /**
     * 
     */
    void renameObject( LibraryItemController theObjController ) {
        
        HostController theHostController = theObjController.getHostController();
        List<LibraryItemController> theControllerList = theMainGui.getJTree().getLibraryItemControllers( theHostController, theObjController.getClass() );
        //Else, the Object is not a JobSet or a Command (the only two types that
         //  can be renamed) or it is null. In any of those cases, do nothing.
        
        //NOTE: If the retrieval of the specific type of LibraryItemControllers can
        //  can be genericized (we don't need to use instanceof) then this entire
        //  method will be generic. The if statement below can be replaced with
        //  an non-final boolean LibraryItemController.itemCanBeRenamed() method.
        
        if( theControllerList != null ) { //If the selected Object is of a type that can be renamed...
          
            String itemTypeDisplayName = theObjController.getItemTypeDisplayName();
        
            String enterNameMessage = new StringBuilder( "Enter a new name for the " ).append( itemTypeDisplayName ).append( ":" ).toString();
            String userInputStr;
            LibraryItemController aController;
        
            boolean doneRenaming = false;
            while( doneRenaming == false ) { //Until we're done renaming...
              
                //Prompt the user for a new name
                userInputStr = JOptionPane.showInputDialog( theMainGui, enterNameMessage, itemTypeDisplayName + " Name", JOptionPane.PLAIN_MESSAGE );
                if( userInputStr == null ) { //If the new name String is null...
                    //The user pressed the "X" (close) or "Cancel" button.
                    doneRenaming = true; //The user is done renaming / Exit the while loop
                    
                } else { //If the new name String is not null...
                    //The user pressed the "Ok" button. Evaluate the input.
                  
                    if( theObjController.isValidNameForItem( userInputStr ) ) { //If the new name is valid...
                      
                        if( userInputStr.equals( theObjController.getItemName() ) ) { //If the item already has the given name...
                            doneRenaming = true; //The renaming is done
                            
                        } else { //If the item does not already have the given name...
                            
                            //Determine if another instance of the same type already has the given name...
                            aController = Utilities.findControllerByItemName( theControllerList, userInputStr );
                            if( aController == null ) { //If there is no item of the same type with the new name...
                                
                                theObjController.changeLibraryItemName( userInputStr );
                                
                                doneRenaming = true; //The renaming is complete / Exit the while loop

                                //Update the interface...
                                SwingUtilities.invokeLater( new Runnable() {
                                    @Override //Runnable
                                    public void run() {
                                        theMainGui.getJTree().updateUI();
                                    }
                                });
                                
                            } else { //If there is already an item of the same type with the new name...
                                JOptionPane.showMessageDialog( theMainGui,
                                        new StringBuilder( "A " ).append( itemTypeDisplayName ).append( " with that name already exists.  Please enter a different name." ).toString(),
                                        "Duplicate Name", JOptionPane.ERROR_MESSAGE );

                            }
                          
                        } //End of "} else { //If the object does not already have the given name..."
                        
                    } else { //If the new name is invalid...
                        JOptionPane.showMessageDialog( theMainGui,
                                "That is not a valid name.", "Invalid Name", JOptionPane.ERROR_MESSAGE );
                        //Prompt the user for a new name again
                    }
                  
                } //End of "} else { //If the new name String is not null..."
                
            } //End of "while( doneRenaming == false ) { //Until we're done renaming..."

        } //End of "if( objectType != null ) { //If the selected Object is of a type that can be renamed..."
        
    }/* END renameSelectedObject() */
      
    //****************************************************************************
    /**
    * Performs any cleanup prior to shutting down this window.
    *
    * @return {@code true} if all cleanup logic completes successfully
    */
    public boolean closeWindow() {

        boolean rtnCode = true;

        LibraryItemJTree theJTree = theMainGui.getJTree();
        if( theJTree.hasUnsavedChanges() ) { //If there are unsaved changes...

            //Prompt the user to save the changes...
            int usersChoice = JOptionPane.showConfirmDialog( theMainGui,
                        "Some configuration changes have not been saved.\n" +
                        "Do you want to save these changes before closing?",
                        "Save changes?", JOptionPane.YES_NO_CANCEL_OPTION );

            switch( usersChoice ) {

                case JOptionPane.YES_OPTION:
                    saveAllObjects(false); //Save the changes
                    break;

                case JOptionPane.CANCEL_OPTION:
                    return false;

            }

        } 

        //Shutdown any shells
        List<LibraryItemController> theCtrlList = theJTree.getLibraryItemControllers( HostController.class);
        for( LibraryItemController aController : theCtrlList ){
            if( aController instanceof HostController ){
                HostController aHostController = (HostController)aController;
                aHostController.closeShell();
            }
        }
        
        //Create a garbage collection and start it up
        Thread garbageThread = new Thread(new GarbageCollector( theJTree ));
        garbageThread.start();

        //Shutdown the parent process as well
        shutdownParent();
                
        theMainGui.dispose();
        
        return rtnCode;

    }/* END closeWindow() */
    
      // ==========================================================================
    /**
    *   Action Listener implementation.
    *
     * @param evt
    */
    @Override
    public void actionPerformed(ActionEvent evt) {

        String actionCommand = evt.getActionCommand();
        try {
            switch (actionCommand) {

                case Constants.IMPORT_BUNDLE:
                    importBundle( null );
                    break;
                case Constants.ACTION_Rename:

                    List<LibraryItemController> theControllerList = theMainGui.getJTree().getSelectedObjectControllers();
                    for( LibraryItemController aController : theControllerList){
                        renameObject( aController );
                    }
                    break;

                case Constants.EXPORT_TO_BUNDLE:
                    //Export the selected object
                    exportLibraryObjects( theMainGui.getJTree().getSelectedObjectControllers() );
                    break;

                case Constants.ACTION_SLEEP:
                    theControllerList = theMainGui.getJTree().getSelectedObjectControllers();
                    for( LibraryItemController aController : theControllerList){
                        if( aController instanceof HostController){
                            ((HostController)aController).sleep( theMainGui, false );
                        }
                    }
                    break;

                case Constants.ACTION_UNINSTALL:

                    ControlMessageManager aCMManager = ControlMessageManager.getControlMessageManager();
                    if( aCMManager == null ){
                        aCMManager = ControlMessageManager.initialize( getServer().getServerManager());
                    }

                    theControllerList = theMainGui.getJTree().getSelectedObjectControllers();
                    for( LibraryItemController aController : theControllerList){
                        if( aController instanceof HostController){

                            int hostId = Integer.parseInt( ((HostController)aController).getId());
                            //Send sleep message
                            Uninstall aMsg = new Uninstall(hostId); //Convert mins to seconds
                            aCMManager.send( aMsg );

                        }
                    }

                    //Remove the node
                    removeSelectedNodes( false );
                    break;

                case Constants.ACTION_REMOVE:
                    //Remove the node
                    removeSelectedNodes( true );
                    break;
                case Constants.ACTION_RELOAD:
                    //Tell the client to reload                      
                    //Get the control message manager
                    aCMManager = ControlMessageManager.getControlMessageManager();
                    if( aCMManager == null ){
                        aCMManager = ControlMessageManager.initialize( getServer().getServerManager());
                    }

                    //Send reload message
                    theControllerList = theMainGui.getJTree().getSelectedObjectControllers();
                    for( LibraryItemController aController : theControllerList){
                        if( aController instanceof HostController){

                            int hostId = Integer.parseInt(((HostController)aController).getId());
                            Reload aRelMsg = new Reload(hostId); //Convert mins to seconds
                            aCMManager.send(aRelMsg );
                        }
                    }
                    break;

                case Constants.ACTION_CREATE_RELAY:
                    
                    ValidTextField aField = new ValidTextField( "0" );
                    aField.setValidation( StandardValidation.KEYWORD_Port );
                    aField.setMargin(new Insets(2,4,2,4));
                    Object[] objMsg = { "Please enter the port number to start listening.", " ", aField};
                              
                    //Send relay message
                    theControllerList = theMainGui.getJTree().getSelectedObjectControllers();
                    for( LibraryItemController aController : theControllerList){
                        if( aController instanceof HostController){
                            
                            //Have the user manually put in the server ip
                            Object retVal = JOptionPane.showOptionDialog(null, objMsg, "Enter Port",
                                   JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);

                            //If the user pressed OK and the ip was valid
                            if((Integer)retVal == JOptionPane.OK_OPTION && aField.isDataValid()){
                                
                                String strPort =  aField.getText();
                                int port = Integer.parseInt( strPort );                    

                                //Get the control message manager
                                aCMManager = ControlMessageManager.getControlMessageManager();
                                if( aCMManager == null ){
                                    aCMManager = ControlMessageManager.initialize( getServer().getServerManager());
                                }                      

                                HostController aHostController = (HostController)aController;
                                Host aHost = aHostController.getObject();
                                aHost.setRelayPort( strPort );
                                
                                int hostId = Integer.parseInt(aHostController.getId());
                                RelayStart aMigMsg = new RelayStart( port, hostId ); //Convert mins to seconds
                                aCMManager.send( aMigMsg );
                            }
                        }
                    }
                    break;
                case Constants.ACTION_REMOVE_RELAY:
                    
                    //Send relay message
                    theControllerList = theMainGui.getJTree().getSelectedObjectControllers();
                    for( LibraryItemController aController : theControllerList){
                        if( aController instanceof HostController){
                            
                            Host theHost = (Host) aController.getObject();
                            int rtnCode = JOptionPane.CLOSED_OPTION;
                            StringBuilder messageBuilder = new StringBuilder( "Are you sure you want to stop the relay on \"")
                                    .append(theHost.getHostname()).append("\" ?");
                            while( rtnCode == JOptionPane.CLOSED_OPTION ) { //Until the user chooses 'Yes' or 'No'...
                                //Prompt user to confirm the delete
                                rtnCode = JOptionPane.showConfirmDialog( null, messageBuilder.toString(),
                                        "Stop Relay", JOptionPane.YES_NO_OPTION );
                            } 
                            
                            //Get the last-selected node
                            if( rtnCode == JOptionPane.YES_OPTION ) { //If the delete is confirmed...

                                try {
                                   
                                    aCMManager = ControlMessageManager.getControlMessageManager();
                                    if( aCMManager == null ){
                                        aCMManager = ControlMessageManager.initialize( getServer().getServerManager());
                                    }
                                    //Send sleep message
                                    int dstHostId = Integer.parseInt( theHost.getId());
                                    RelayStop stopMsg = new RelayStop( dstHostId ); //Convert mins to seconds
                                    aCMManager.send(stopMsg );

                                } catch( IOException ex ){
                                    Log.log(Level.WARNING, NAME_Class, "actionPerformed()", ex.getMessage(), ex );
                                }
                            }
                        } 
                    } 
                                  
                    break;
                case Constants.ACTION_MIGRATE:

                    //Have the user manually put in the server ip
                    aField = new ValidTextField( "0.0.0.0:0" );
                    aField.setValidation( StandardValidation.KEYWORD_ClientConnect );
                    aField.setMargin(new Insets(2,4,2,4));
                    objMsg = new Object[]{ "Please enter the IP address:Port of the new server.", " ", aField};
                    Object retVal = JOptionPane.showOptionDialog(null, objMsg, "Enter IP Address:Port",
                           JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);

                    //If the user pressed OK and the ip was valid
                    if((Integer)retVal == JOptionPane.OK_OPTION && aField.isDataValid()){
                       String serverIp = aField.getText();                    

                       //Get the control message manager
                       aCMManager = ControlMessageManager.getControlMessageManager();
                       if( aCMManager == null ){
                           aCMManager = ControlMessageManager.initialize( getServer().getServerManager());
                       }

                       //Send sleep message
                        theControllerList = theMainGui.getJTree().getSelectedObjectControllers();
                        for( LibraryItemController aController : theControllerList){
                            if( aController instanceof HostController){

                                int hostId = Integer.parseInt(((HostController)aController).getId());
                                Migrate aMigMsg = new Migrate( hostId, serverIp ); //Convert mins to seconds
                                aCMManager.send( aMigMsg );
                            }
                        }
                    }
                    break;
                default:
                    //Check if the command is a creation action
                    Map<String, Class> theMap = getActionClassMap();
                    Class<?> theClass = theMap.get(actionCommand);
                    if(theClass != null){

                        try {

                            Object anObj = theClass.newInstance();

                            //Create a new object and add it to the 
                            if( anObj instanceof LibraryItemController ){  
                                LibraryItemController theController = (LibraryItemController)anObj;
                                Object controllerObject = theController.createItem( this );
                                if( controllerObject != null ){
                                    
                                    theController.setObject(controllerObject);

                                    //Create a task and controller and display it
                                    LibraryItemJTree theJTree = theMainGui.getJTree();
                                    DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode)theJTree.getLastSelectedPathComponent();
                                    if(parentNode != null){
                                        //Add it to the tree
                                        theJTree.addObjectToTree(theController, parentNode, 0);
                                        theJTree.setSelectedObject( theController );
                                    }
                                    
                                    theController.saveToDisk();
                                }
                            }  

                        } catch (InstantiationException ex) {
                            ex = null;
                        } catch (IllegalAccessException ex) {
                            ex = null;
                        } 
                    }
                    break;
            }
            
        } catch (IOException ex ){
            Log.log(Level.SEVERE, NAME_Class, "actionPerformed()", ex.getMessage(), ex);
        } 

    }/* END actionPerformed( ActionEvent ) */
    
    
    //===============================================================
    /**
     *  Ensures any external threads are shutdown properly  
     *
    */
    public void shutdownParent() {
        
        Server theServer = (Server)theServerManager.getServer();
        TasksJDialog theTaskDialog = TasksJDialog.getTasksJDialog();
        try {

            //Save all the tasks to disk
            theTaskDialog.saveAllTasks();
            theTaskDialog.prepClose();

        } catch (IOException ex) {
            Log.log(Level.SEVERE, NAME_Class, "closeWindow()", ex.getMessage(), ex);
        }

        //Shutdown the server
        theServer.shutdown();
    }
 
     //===============================================================
    /**
     * 
     */
    @Override
    public void updateComponents() {
        theMainGui.initializeComponents();
        theMainGui.initializeTreeModel(); //Initialize the TreeModel
    }
    
     //===============================================================
    /**
     *  Returns the server reference
     * @return 
    */
    public Server getServer(){
        return (Server)theServerManager.getServer();
    }


    //===============================================================
    /**
    *   Returns the LibraryItemJTree
    *
     * @return 
    */
    public LibraryItemJTree getJTree() {
        return theMainGui.getJTree();
    }   

     // ==========================================================================
    /**
     *
     * <p>
     * If the argument is null this method does nothing.
     *
     * @param controller the {@code Controller} reporting the change
     *
    */
    @Override //ControllerListener
    public void valuesChanged( Controller controller ) {

        if( controller == null ) //If the Controller is null...
            return; //Do nothing

        if( controller instanceof LibraryItemController ){

           theMainGui.setDirtyFlag((LibraryItemController)controller, true, true);
        }
        
    }/* END valuesChanged( Controller ) */

     
    // ==========================================================================
    /**
        *  Sets the selected object to the one double clicked 
        * <p>
        *
     * @param aController
     * @param showRunner
    */
    @Override
    public void selectController(LibraryItemController aController, boolean showRunner){
        theMainGui.setSelectedObject(aController, showRunner);
    }/* END selectController( LibraryItemController, boolean ) */
   
    
    // ========================================================================
    /**
     * 
     * @param controller
     */
    @Override //LibraryItemControllerListener
    public void persistentValueChanged( LibraryItemController controller ) {
        
        if( controller == null ) return;

        theMainGui.setSaveButtons( true );

        theMainGui.getJTree().repaint();
        
    }/* END persistentValueChanged( LibraryItemController ) */
    
    // ==========================================================================
    /**
     *  Returns the component that is associated with the listener.
     * @return 
    */
    @Override
    public MainGui getListenerComponent(){
        return theMainGui;
    }

    //==========================================================================
    /**
     *  If we are running as a client then send a message to reload the SSLContext,
     *  otherwise perform it here.
     * 
    */
    @Override
    public void reloadSSLContext() {
        
        try {
               
            SSLUtilities.reloadSSLContext();

        } catch (LoggableException ex) {
            Log.log(Level.SEVERE, NAME_Class, "reloadSSLContext()", ex.getMessage(), ex);
        }
    }

    // ==========================================================================
    /**
     *  Return the JFrame
    */
    @Override
    public JFrame getParentJFrame() {
        return theMainGui;
    }

    //==========================================================================
    /**
     *  Recreate the sockets since the ports changed.
     * 
     * @return 
     * @throws pwnbrew.logging.LoggableException 
    */
    @Override
    public boolean recreateSockets() throws LoggableException {
        
        return theServerManager.rebuildServerSockets();
        
    }
    
    // ==========================================================================
    /**
     *  Returns the parent id of the passed controller
     * @param passedController
     * @return 
     */
    @Override
    public HostController getParentController( LibraryItemController passedController ){
        
        HostController retController = null;
        LibraryItemJTree theJTree = theMainGui.getJTree();
         
        DefaultMutableTreeNode aNode = theJTree.findNodeInTree( passedController );
        if(aNode != null){
            
            //Get the host controller 
            while( true ){
                DefaultMutableTreeNode aParentNode = (DefaultMutableTreeNode) aNode.getParent();
                if(aParentNode != null){
                    Object parentObj = aParentNode.getUserObject();
                    if( parentObj instanceof HostController){

                        retController = (HostController)parentObj;
                        break;
                        
                    } else if( aParentNode.isRoot()){
                        
                        break;
                    } else {

                        aNode = aParentNode;
                    }
                }
            }
        }
        
        return retController;
        
    }
    
    // ==========================================================================
    /**
     *  Basically fires a repaint because a node changed.
     * 
     * @param passedNode
     */
    @Override
    public void hostChanged(final Host passedNode) { }
     
    // ==========================================================================
    /**
     * Adds the host to the JTree
     *
     * @param passedHost 
     */
     
    @Override
    public void hostDetected( final Host passedHost ) {

        //Get the Host from the id
        String clientIdStr = passedHost.getId();  
        final HostController theController = getHostController( clientIdStr );
        if(theController != null && theController.getObject().equals(passedHost)){
                
            //Get the address
            Host theHost = (Host) theController.getObject();
            theHost.setConnected(true);
                                       
            //Add the new connections to the host
            theHost.addNicPairs( passedHost.getNicMap() );

            //Set time
            Session aSession = new Session();
            theHost.addSession(aSession);

            //Purge stale dates
            theController.removeStaleDates();

            //Write it to disk
            theController.saveToDisk();
            
            //Get the relayPort
            String thePortStr = theHost.getRelayPort();

            //Call for repaint
            SwingUtilities.invokeLater( new Runnable(){
                @Override
                public void run() {
                    getJTree().repaint();
                    getJTree().requestFocus();
                    theController.updateComponents();
                }
            });

            //Get the auto sleep flag and if it is set then tell the client to goto sleep
            final JFrame theParent = getParentJFrame();
            if( theController.getAutoSleepFlag() ){

                Constants.Executor.execute( new Runnable(){
                    @Override
                    public void run() {
                        try {
                            Thread.sleep( 30000 );
                            theController.sleep( theParent, true );
                        } catch (InterruptedException ex) {
                            ex = null;
                        }
                    }
                });
                
            } else {
                
                //Get the port
                if( !thePortStr.isEmpty() ){
                    
                    //Parse the port
                    int port = Integer.parseInt( thePortStr );     

                    //Get the control message manager
                    try {
                        
                        ControlMessageManager aCMManager = ControlMessageManager.getControlMessageManager();
                        if( aCMManager == null ){
                            aCMManager = ControlMessageManager.initialize( getServer().getServerManager());
                        }                      
                        
                        int hostId = Integer.parseInt(theHost.getId());
                        RelayStart aMigMsg = new RelayStart( port, hostId ); //Convert mins to seconds
                        aCMManager.send( aMigMsg );                        
                  
                    } catch( IOException ex ){
                       Log.log(Level.SEVERE, NAME_Class, "hostDetected()", ex.getMessage(), ex );
                    }
                }
            }
                              
        } else {

            //Start in swing thread since it affects the gui
            final HostController aHostController = new HostController( passedHost, this );
            Session aSession = new Session();
            passedHost.addSession(aSession);
            
            SwingUtilities.invokeLater( new Runnable(){
                @Override
                public void run() {

                    //Get the JTree and add the controller
                    LibraryItemJTree theJTree = getJTree();
                    MainGuiTreeModel treeModel = theJTree.getModel();               
                    DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) treeModel.getRoot();

                    //Add the host node
                    IconNode newParentNode = new IconNode( aHostController, true );

                    //Add the node at the end of the children of the parentNode
                    int index = parentNode.getChildCount();
                    treeModel.insertNodeInto( newParentNode, parentNode, index );                

                }
            });
            
            aHostController.saveToDisk();
                        
        }
                
    }

    // ==========================================================================
    /**
     *  Notify that the host has disconnected.
     *
     * @param passedHost 
     */
    @Override
    public void hostDisconnected(final Host passedHost) {
                
        List<Session> sessionList = passedHost.getSessionList();
        Session aSession = sessionList.get(sessionList.size() - 1);
        aSession.setDisconnectedTime(Constants.CHECKIN_DATE_FORMAT.format( new Date() ));

        passedHost.setConnected( false );
        getJTree().repaint();

    }
    
    // ==========================================================================
    /**
     *  Creates a remote job out of the selected component  
     * @param passedController 
    */    
    @Override
    public void sendRemoteJob( RunnableItemController passedController ){
        
        LibraryItemController theHostController = passedController.getHostController();
        final List<RemoteTask> theTaskList = new LinkedList<>();

        //Get the host controller
        Host theHost = (Host) theHostController.getObject();
        if( passedController instanceof JobSetController ){
            
            List<LibraryItemController> childList = ((JobSetController)passedController).getChildren();
            for(LibraryItemController aChild : childList){
                
                //Add each child
                if( aChild instanceof JobController ){
                    
                    JobController aJobController = (JobController)aChild;
                    RemoteTask aRemoteTask = aJobController.generateRemoteTask( theHost );

                    //Add it to the list
                    theTaskList.add(aRemoteTask);
                }
            }
            
            //Find out if the reboot and concurrent flags are specified
            JobSet theJobSet = (JobSet) passedController.getObject();
            boolean reboot = theJobSet.rebootsAfter();
            boolean stopOnError = theJobSet.stopsOnError();
            boolean concurrent = theJobSet.runsConcurrently();
                 
            //Loop throught the tasks and set the flags appropriately
            RemoteTask prevTask = null;
            for(int i =0; i < theTaskList.size(); i++){

                //Set the pointer for the next task
                RemoteTask aTask = theTaskList.get(i);

                //Set the stop on error flag
                if(stopOnError){
                    aTask.setStopFlag(stopOnError);    
                }

                if( prevTask != null && ( !concurrent )){
                    prevTask.setNextTaskId(Integer.valueOf(aTask.getTaskId()));  
                }

                //Set the reboot flag on the last element if reboot is specified
                if( (i == (theTaskList.size() - 1)) && reboot){
                    aTask.setRebootFlag(reboot);  
                }             

                //Assign to next
                prevTask = aTask;
            }
            
            
        } else if( passedController instanceof JobController ){

            JobController aJobController = (JobController)passedController;
            RemoteTask aRemoteTask = aJobController.generateRemoteTask( theHost );

            //Add it to the list
            theTaskList.add(aRemoteTask);            

        }

        //Display the task window
        final TasksJDialog theTasksDialog = TasksJDialog.getTasksJDialog();
        theTasksDialog.setVisible(true);

        Constants.Executor.execute( new Runnable () {

            @Override
            public void run() {

                //Add the tasks to the server
                addTaskList(theTaskList);
            }
        } );        
        
    }
    
     //===============================================================
    /**
     * Adds the task list to the task map as appropriate
     *
     * @param taskList  the passed task list
     *
     * @return
    */
    private void addTaskList(List<RemoteTask> taskList){
        
       if(taskList != null && !taskList.isEmpty()){
          
          RemoteTask firstTask  = taskList.get(0);
          Integer theNextTask = firstTask.getNextTaskId();
          
          RemoteTask lastTask = taskList.get( taskList.size() - 1);
          boolean reboot = lastTask.shouldReboot();
          
          //If this is a job set that wants to wait until completion of
          //each job before proceeding
          if( theNextTask != null || reboot ){
              
              //Add the tasks to the map
              for(int i = 1; i < taskList.size(); i++){
                 RemoteTask aTask = taskList.get(i);
                 synchronized(theActiveTaskMap){
                    theActiveTaskMap.put(Integer.valueOf(aTask.getTaskId()), aTask);
                 }
              }
              
              //Send the first task
              try {
                 addTask(firstTask, true);
              } catch (LoggableException ex) {
                 Log.log(Level.SEVERE, NAME_Class, "addTaskList()", ex.getMessage(), ex );
              }
                          
          } else {
              
              //Loop through and send all tasks
              for(RemoteTask aTask : taskList){
                 try {
                    addTask(aTask, true);
                 } catch (LoggableException ex) {
                    Log.log(Level.SEVERE, NAME_Class, "addTaskList()", ex.getMessage(), ex );
                 }
              }
          }        
          
       }
               
    }
    
     //===============================================================
    /**
    * Adds the task to the list of active task.
    *
    * @param aTask    the passed task
    * @param newTask  is this a new task
     * @throws pwnbrew.logging.LoggableException
    */
    public void addTask(final RemoteTask aTask, boolean newTask) throws LoggableException {

        //Add the task to the map
        synchronized(theActiveTaskMap){
            theActiveTaskMap.put( Integer.parseInt(aTask.getTaskId()), aTask);
        }

        //Add to the task window if not staging
        String theState = aTask.getState();
        if( newTask && !theState.equals( RemoteTask.TASK_STAGING)){

            final TasksJDialog theTasksDialog = TasksJDialog.getTasksJDialog();
            //Put in swing utitlites since it is a non gui thread
            SwingUtilities.invokeLater( new Runnable() {

                @Override
                public void run() {
                    theTasksDialog.addTask(aTask);
                }
            });

        } else {

            int clientId = Integer.parseInt( aTask.getClientId() );
            final TaskNew aMessage = aTask.getControlMessage(clientId);

            if( aMessage != null ){
                //Send staging message
                try {

                    ControlMessageManager aCMManager = ControlMessageManager.getControlMessageManager();
                    if( aCMManager == null ){
                        aCMManager = ControlMessageManager.initialize( theServerManager );
                    }

                    aCMManager.send(aMessage);

                } catch( IOException ex ){
                   Log.log(Level.SEVERE, NAME_Class, "addTask()", ex.getMessage(), ex );
                }
            }
        }
        
    }    
    
    //===============================================================
    /**
     * Start the task
     *
     * @param theTask
    */
    @Override
    public void startTask(RemoteTask theTask) {
        
        try {
             
            ControlMessageManager aCMManager = ControlMessageManager.getControlMessageManager();
            if( aCMManager == null ){
                aCMManager = ControlMessageManager.initialize( theServerManager );
            }
            
            int dstHostId = Integer.parseInt( theTask.getClientId());
            aCMManager.send(theTask.getControlMessage(dstHostId) );

        } catch ( LoggableException | IOException ex) {
           Log.log(Level.WARNING, NAME_Class, "startTask()", ex.getMessage(), ex );
        }
        
    }
    
     //===============================================================
    /**
     * Attempts to cancel the running task
     *
     * @param taskId
    */
    @Override
    public void cancelTask( Integer msgId, int taskId ) {

        RemoteTask theTask;
        synchronized(theActiveTaskMap){
            theTask = theActiveTaskMap.remove(taskId);
        }

        //Send out task fin
        if(theTask != null){

            //Send a cancel message to the client if the task is active
            //Cancel any file transfers that might be going on.
            try {                
               
                FileMessageManager aFMM = FileMessageManager.getFileMessageManager();
                if( aFMM == null ){
                    aFMM = FileMessageManager.initialize( theServerManager );
                }
                aFMM.cancelFileTransfer( msgId, taskId);
                
                if(msgId != null){
                    
                    ControlMessageManager aCMManager = ControlMessageManager.getControlMessageManager();
                    if( aCMManager == null ){
                        aCMManager = ControlMessageManager.initialize( theServerManager );
                    }
                    
                    TaskStatus cancelMessage = new TaskStatus( taskId, RemoteTask.TASK_CANCELLED, msgId );
                    aCMManager.send(cancelMessage);
                }
                
            } catch ( IOException ex) {
                Log.log(Level.SEVERE, NAME_Class, "cancelTask()", ex.getMessage(), ex );
            }

            //Set the time the task was cancelled
            String taskEndTime = new SimpleDateFormat( Constants.FORMAT_SessionDateTime ).format( new Date() );
            theTask.setEndTime(taskEndTime);

            //Set the state to cancelled
            theTask.setState( RemoteTask.TASK_CANCELLED);                 

        }
       
    }
    
     //===============================================================
    /**
     * Attempts to retry the task
     *
     * @param theTask
    */
    @Override
    public void retryTask(RemoteTask theTask) {
        
        try {
            theTask.setState( RemoteTask.TASK_START);   
            addTask(theTask, false);
        } catch ( LoggableException ex) {
            Log.log(Level.WARNING, NAME_Class, "iRetryTask()", ex.getMessage(), ex );
        }
        
    }
    
    //===============================================================
    /**
     * Handles the completion of a task
     *
     * @param passedMsg
    */
    @Override
    public void taskChanged(TaskStatus passedMsg) {
        
        final RemoteTask theRemoteTask;
        int theTaskId = passedMsg.getTaskId();
        
        try {
            String taskState = passedMsg.getStatus();
            
            synchronized(theActiveTaskMap){
                if(taskState.equals( RemoteTask.TASK_COMPLETED) || taskState.equals( RemoteTask.TASK_FAILED) 
                        || taskState.equals( RemoteTask.TASK_CANCELLED)){
                    theRemoteTask = theActiveTaskMap.remove(Integer.valueOf(theTaskId));
                } else {
                    theRemoteTask = theActiveTaskMap.get(Integer.valueOf(theTaskId));
                }
            }

            if(theRemoteTask != null){

                theRemoteTask.setState(taskState);
                
                //Check if we should proceed or end it
                if(taskState.equals( RemoteTask.TASK_COMPLETED) || taskState.equals( RemoteTask.TASK_FAILED) 
                        || taskState.equals( RemoteTask.TASK_CANCELLED)){
                   
                    //Set end time
                    String taskEndTime = new SimpleDateFormat( Constants.FORMAT_SessionDateTime ).format( new Date() );
                    theRemoteTask.setEndTime(taskEndTime);  

//                    DebugPrinter.printMessage(this, "Task Completed.");
                    
                    //TODO Populate the runner panel
                    SwingUtilities.invokeLater( new Runnable() {
                        @Override
                        public void run() {
                            populateRunnerPanel( theRemoteTask );  
                        }
                    });
                    
                    //Get the next task
                    Integer nextTaskId = theRemoteTask.getNextTaskId();
                    if(nextTaskId != null){

                        //Check if there was an error
                        if(theRemoteTask.getStdErr().isEmpty() || !theRemoteTask.shouldStopOnError()){                  

                            final RemoteTask nextTask;
                            synchronized(theActiveTaskMap){
                                nextTask = theActiveTaskMap.get(nextTaskId);
                            }  

                            //If one exists then send it
                            if(nextTask != null){
                                                           
                                try {
                                    addTask(nextTask, true);
                                } catch (LoggableException ex) {
                                    Log.log(Level.SEVERE, NAME_Class, "taskChanged()", ex.getMessage(), ex );
                                }                                 
                                
                            }

                        } else {

                            //If the task failed then remove all subsequent tasks
                            synchronized(theActiveTaskMap){
                                while(nextTaskId != null){
                                    RemoteTask nextTask = theActiveTaskMap.remove(nextTaskId);
                                    nextTaskId = nextTask.getNextTaskId();
                                } 
                            }

                        }

                    //Check for reboot
                    } else if( theRemoteTask.shouldReboot() ){
                  
                    }
                    
                }
                
                List<RemoteTaskListener> theListeners = theRemoteTask.getRemoteListeners();
                for(RemoteTaskListener aListener : theListeners){
                    aListener.taskChanged(theRemoteTask);
                }
            }
        } catch (IOException ex){
            Log.log(Level.SEVERE, NAME_Class, "taskChanged()", ex.getMessage(), ex );
        }

    }
    
    //===============================================================
    /**
     * Handles the completion of a task
     *
     * @param msgId
     * @param progress
    */
    @Override
    public void progressChanged(int msgId, int progress) {

        RemoteTask theTask;
        synchronized(theActiveTaskMap){
            theTask = theActiveTaskMap.get(Integer.valueOf(msgId));
        }

        if(theTask != null){
            theTask.setStateProgress(progress);
            List<RemoteTaskListener> theListeners = theTask.getRemoteListeners();
            for(RemoteTaskListener aListener : theListeners){
                aListener.taskChanged(theTask);
            }
        }

    }
    
    //=================================================================
    /*
     *  Write the output to the runner panel
     */
    private void populateRunnerPanel( RemoteTask passedTask ) {
        
        //Get the host controller
        HostController theController = getHostController( passedTask.getClientId() );
      
//        RunnerPane theRunnerPane = null;
        LibraryItemController aLibController = getControllerByObjName( theController.getItemName(), passedTask.getType(), passedTask.getName());
        RunnerPane theRunnerPane = aLibController.getRunnerPane( true );
        
        //Fill the pane with the output
        if( theRunnerPane != null ){
            
            theRunnerPane.setText(" ");
            File aClientDir = getHostDirectory( Integer.parseInt( passedTask.getClientId()) );
            File theTaskDir = new File(aClientDir, passedTask.getTaskId());
                      
            //Read the stdout file
            try {
                
                //Readt the stderr file
                File stdErrFile = new File( theTaskDir, Constants.STD_ERR_FILENAME );
                byte[] fileBytes = FileUtilities.readFile(stdErrFile);
                if( fileBytes != null && fileBytes.length > 0){
                    theRunnerPane.handleStdErr(fileBytes);
                }
                
                File stdOutFile = new File( theTaskDir, Constants.STD_OUT_FILENAME ); 
                fileBytes = FileUtilities.readFile(stdOutFile);
                if( fileBytes != null && fileBytes.length > 0 ){
                    theRunnerPane.handleStdOut( fileBytes );
                }

                           
            } catch( IOException ex ){
                ex = null;
            }
            
        }
                                              
    }
    
     //===============================================================
    /**
     * Adds a new host to the map
     *
     * @param passedHost
    */
    @Override
    public void registerHost( Host passedHost ) {

        List<HostListener> theListenerList = theServerManager.getDetectListenerList();
        for(HostListener aListener : theListenerList){
            aListener.hostDetected(passedHost);
        }

    }
    
     // ==========================================================================
    /**
     *  Get the host with the given id string
     * 
     * @param clientIdStr
     * @return 
     */
    @Override
    public HostController getHostController( String clientIdStr ) {
        
        HostController retController = null;
        for( LibraryItemController aController : theMainGui.getJTree().getLibraryItemControllers( HostController.class ) ){
            Host aHost = (Host)aController.getObject();
            if( aHost.getId().equals( clientIdStr )){
                retController = (HostController) aController;
                break;
            }           
        }
        return retController;
    }
    
     //=========================================================================
    /**
     *  Removes the selected nodes from the JTree 
     * @param prompt
     */
    public void removeSelectedNodes( boolean prompt ) {
        
        //Remove the node
        LibraryItemJTree theJTree = getJTree();
        TreePath[] paths = theJTree.getSelectionPaths();
        if ((paths == null) || (paths.length == 0)) {
            // We can't move the root node or an empty selection
            return;
        }

        //Get the objects
        List<DefaultMutableTreeNode> selObjects = new ArrayList<>();
        for(TreePath aPath : paths){
            DefaultMutableTreeNode sourceNode = (DefaultMutableTreeNode)aPath.getLastPathComponent();
            selObjects.add(sourceNode);
        }

        deleteObjects(selObjects, prompt);
        
    }

    // ==========================================================================
    /**
     * Gets the directory of the client id passed
     * 
     * @param clientId
     * @return 
     */
    @Override
    public File getHostDirectory(int clientId) {
        
        File dirFile = null;
        HostController theController = getHostController(Integer.toString(clientId));
        if( theController != null ){
            dirFile = new File(Directories.getRemoteTasksDirectory(), theController.getItemName());
        }
        return dirFile;
    }

}
