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
 * MainGui.java
 *
 * Created on June 23, 2013, 11:22:32 AM
 */

package pwnbrew.gui;

import pwnbrew.library.LibraryItemController;
//import pwnbrew.controllers.RunnableItemController;
//import pwnbrew.controllers.JobSetController;
//import pwnbrew.controllers.JobController;
import pwnbrew.controllers.MainGuiController;
import pwnbrew.xmlBase.XmlBase;
import pwnbrew.xmlBase.job.Job;
//import pwnbrew.xmlBase.job.JobSet;
import java.awt.*;
import java.awt.dnd.DnDConstants;
import java.awt.event.*;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import pwnbrew.generic.Folder;
import pwnbrew.gui.dialogs.AboutJDialog;
import pwnbrew.gui.dialogs.OptionsJDialog;
import pwnbrew.gui.dialogs.TasksJDialog;
import pwnbrew.gui.tree.MainGuiTreeModel;
import pwnbrew.gui.tree.IconNode;
import pwnbrew.gui.tree.IconTreeCellRenderer;
import pwnbrew.gui.tree.IconTreeDragSource;
import pwnbrew.gui.tree.IconTreeTransferHandler;
import pwnbrew.gui.tree.LibraryItemJTree;
import pwnbrew.host.Host;
import pwnbrew.host.HostController;
import pwnbrew.log.Log;
import pwnbrew.log.LoggableException;
import pwnbrew.misc.Constants;
import pwnbrew.misc.Directories;
import pwnbrew.misc.EditMenuUpdater;
import pwnbrew.misc.FileFilterImp;
import pwnbrew.utilities.GuiUtilities;
import pwnbrew.utilities.Utilities;


/**
 *
 *  
 */
public final class MainGui extends javax.swing.JFrame {

    private FileFilterImp theXmlFilter = null; 
    private HashMap<String, JMenuItem> theNameToMenuItemMap = null;

    private IconTreeDragSource theIconTreeDragSource;
    //NOTE: Though this IconTreeDragSource is not "used" it is still necessary.
    //  DO NOT remove it. It is instantiated in the constructor.

    private final JScrollPane treeJScrollPane = new JScrollPane();
    private final LibraryItemJTree mainJTree = new LibraryItemJTree();
    private final MainGuiController theGuiController;

    private JFileChooser theFileChooser = null;
    private boolean justImportedXrb = false;
    private Rectangle maxBounds = null;
    private static MainGui theMainGui = null;    
 
    private static final String NAME_Class = MainGui.class.getSimpleName();

    // ==========================================================================
    /**
     *
     * @param passedController
     * @return 
     */
    public synchronized static MainGui createInstance( MainGuiController passedController ) {

        if( theMainGui == null ) {
            theMainGui = new MainGui( passedController );
        }
        return theMainGui;

    }/* END createInstance( MainGui ) */
    
    //===============================================================
    /**
     *  Constructor
    */
    private MainGui ( MainGuiController passedController ) {

        EditMenuUpdater.createInstance( this );
        //NOTE: The leak of "this" above should be inocuous.
        
        theGuiController = passedController;
 
        theXmlFilter = new FileFilterImp();
        theXmlFilter.addExt( "xml" );

        //Create a JFileChooser to select files...
        theFileChooser = new JFileChooser();

        initComponents();

        //Set the icon
        setAppIconImage(Constants.EDITOR_IMG_STR);

        addTreeRightClickListener();
        addTreeKeyListener();

        //Set the main window to be centered on the screen
        setLocationRelativeTo( null );
        
    }
    
    //===============================================================
    /**
    * This function returns the Gui Controller
    *
     * @return 
    */
    public MainGuiController getController() {
        return theGuiController;
    }
   
    //===============================================================
    /**
    * Sets the just imported flag
    *
     * @param passedBool
    */
    public void setJustImported( boolean passedBool) {
       justImportedXrb = passedBool;
    }

    //===============================================================
    /**
    * This function gets whether something has just imported
    *
     * @return 
    */
    public boolean justImported() {
       return justImportedXrb;
    }


    // ==========================================================================
    /**
    * Processes {@link WindowEvent}s occurring on this component.
    * <p>
    * This method is overridden to handle unsaved changes when the window is closed
    * using the X(exit) button and give the user the option of cancelling the close.
    *
    * @param event the {@code WindowEvent}
    */
    @Override //Overrides JFrame.processWindowEvent( WindowEvent )
    protected void processWindowEvent( WindowEvent event ) {

        if( WindowEvent.WINDOW_CLOSING == event.getID() ) { //If the event is the window closing...
            theGuiController.closeWindow();
            
        } else { //If the event is not the window closing...
            super.processWindowEvent( event ); //Proceed normally
        }

    }/* END processWindowEvent( WindowEvent ) */


    //===========================================================================
    /**
    * Displays the about dialog
    */
    static private void displayHelp() {

       AboutJDialog aboutDialog = new AboutJDialog( null );
       aboutDialog.setVisible( true );
    }

    // ==========================================================================
    /**
    *
    */
    public void initializeTreeModel() {

        //Add the objects to the tree
        try {

            //Create list to hold of the separate controllers
            Map<HostController, List<LibraryItemController>> theRtnMap = new LinkedHashMap<>();
            Map<Host, List<XmlBase>> retMap = Utilities.rebuildLibrary(); //Get the Scripts in the library
            for (Entry<Host, List<XmlBase>> anEntry : retMap.entrySet()) {
                List<XmlBase> treeList = anEntry.getValue();

                //Initialize the lists
                List<LibraryItemController> theControllerList = new ArrayList<>();
//                List<JobController> jobControllerList = new ArrayList<>();
//                List<JobSetController> jobSetControllerList = new ArrayList<>();

                //Iterate through the library items and create controllers for them
                LibraryItemController theController;
                for( XmlBase anXB : treeList){

                    if(anXB instanceof Job){

                        //Ensure the job doesn't think it is still running
                        Job aJob = (Job)anXB;
                        if(aJob.getAttribute(Job.theLastRunResult).equals(Constants.LastRunResults_Running)){
                            aJob.setAttribute( Job.theLastRunResult, Constants.LastRunResults_Cancelled );
                        }

                        theController = aJob.instantiateController( theGuiController );
//                        jobControllerList.add( (JobController)theController );

//                    } else if( anXB instanceof JobSet ) {
//
//                        JobSet aJobSet = (JobSet)anXB;
//                        theController = aJobSet.instantiateController( theGuiController );
//                        
//                        jobSetControllerList.add( (JobSetController) theController );
                        
                    } else {
                        continue;    
                    }

                    theControllerList.add( theController );
                }

//                //Initialize the references to JobControllers in the JobSetControllers...
//                for( JobSetController jsController : jobSetControllerList ) { //For each JobSetController...
//                    jsController.addJobControllerReferences( jobControllerList );
//                }

                //Add the entry
                Host aHost = anEntry.getKey();
                HostController aController = new HostController(aHost, theGuiController, theGuiController.isHeadless());
                theRtnMap.put( aController, theControllerList);
            }
            
            populateTreeModel( theRtnMap ); //Populate the TreeModel

        } catch (IllegalAccessException | LoggableException | InstantiationException | SocketException ex) {
            Log.log(Level.SEVERE, NAME_Class, "initializeTreeModel()", ex.getMessage(), ex);
        }


    }/* END initializeTreeModel() */

    //===============================================================
    /**
    * Returns the xml filter
    *
    * @return
    */
    public FileFilterImp getXmlFilter() {
        return theXmlFilter;
    }

   //===============================================================
    /**
    * Initializes all the components
    */
    public void initializeComponents(){
       
       //Set the model and add the tree to the scroll pane
       Folder localFolder = new Folder( Directories.getObjectLibraryPath());
              
       MainGuiTreeModel theModel = new MainGuiTreeModel( new DefaultMutableTreeNode( localFolder, true ) );
       mainJTree.setModel( theModel );
       mainJTree.setCellRenderer( new IconTreeCellRenderer() );
       mainJTree.setShowsRootHandles(true);
       mainJTree.setRowHeight(25);
        
       //Create a new JPanel to put the JTree and buttons in
       JPanel treePanel = initializeJTreePanel();
       mainJSplitPane.setLeftComponent(treePanel);

       //Add text editing actions allowed for text components.
       theNameToMenuItemMap = GuiUtilities.createEditMenuOptions();
       populateEditMenu();
       
       setupDragNDrop();

    }
    
    //===========================================================================
    /**
     *  Returns a JPanel with the main JTree in it.
     * @return 
     */
    private JPanel initializeJTreePanel(){
        
        //Create a new JPanel to put the JTree and buttons in
        JPanel treePanel = new JPanel();
        treePanel.setBackground( Color.WHITE );
        
        //Create the buttons for adding and removing to the tree
        JButton addScriptButton = new JButton();
        addScriptButton.setText(" ");
        addScriptButton.setIconTextGap(0);
        addScriptButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        addScriptButton.setActionCommand( Constants.ACTION_Add );
        addScriptButton.addActionListener( theGuiController );
        
        JButton removeScriptButton = new JButton();
        removeScriptButton.setText(" ");
        removeScriptButton.setIconTextGap(0);
        removeScriptButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        removeScriptButton.setActionCommand( Constants.ACTION_Remove );
        removeScriptButton.addActionListener( theGuiController );
        
        GuiUtilities.setComponentIcon(addScriptButton,  15, 15, Constants.ADD_IMAGE_STR);
        GuiUtilities.setComponentIcon(removeScriptButton, 15, 15, Constants.DELETE_IMG_STR);
        
        treeJScrollPane.setMinimumSize(new java.awt.Dimension(150, 320));
        
        // Set scroll unit to a higher value to scroll faster.
        int vertScrollUnit = treeJScrollPane.getVerticalScrollBar().getUnitIncrement( 1 );
        treeJScrollPane.getVerticalScrollBar().setUnitIncrement( vertScrollUnit * 10 );

        int horizScrollUnit = treeJScrollPane.getHorizontalScrollBar().getUnitIncrement( 1 );
        treeJScrollPane.getHorizontalScrollBar().setUnitIncrement( horizScrollUnit * 10 );
        treeJScrollPane.setViewportView(mainJTree);
        treeJScrollPane.setBorder(null);
        
        GroupLayout layout = new GroupLayout(treePanel);
        treePanel.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent( treeJScrollPane, 150, 150, Short.MAX_VALUE ))
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, Short.MAX_VALUE)
                .addComponent( addScriptButton )
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent( removeScriptButton )
                .addGap(20, 20, Short.MAX_VALUE))
            
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(3)
                .addComponent(treeJScrollPane, 320, 320, Short.MAX_VALUE)
                .addGap(10)
                .addGroup( layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(addScriptButton)
                    .addComponent(removeScriptButton))
                .addContainerGap())     
        );
        
        return treePanel;
    }

    //========================================================================
    /**
    * Sets up drag and drop for the jTree
    */
    private void setupDragNDrop(){

       mainJTree.setDragEnabled(true);
       mainJTree.setDropMode(DropMode.ON_OR_INSERT);
       mainJTree.setTransferHandler( new IconTreeTransferHandler( theGuiController, mainJTree ));
              
       GuiUtilities.oneClickDnd(mainJTree);
       
       //Set the Drag and Drop features
       theIconTreeDragSource = new IconTreeDragSource(mainJTree);
       theIconTreeDragSource.createDefaultDragGestureRecognizer( DnDConstants.ACTION_MOVE );

    }

    //======================================================================
    /**
    * Sets the icon to be used for the application (on the top left of the windows
    * and on the taskbar).
    *
    * @param imageName the name of the image to be used as the application icon
    */
    private void setAppIconImage( String imageName ) {
        Image appIcon = Utilities.loadImageFromJar( imageName );
        if( appIcon != null ) {
            setIconImage( appIcon );
        }
    }
    
    //======================================================================
    /**
    *   Populates the tree model
    */
    private boolean populateTreeModel( Map<HostController, List<LibraryItemController>> theControllerMap ) throws IllegalAccessException, InstantiationException {

        setCursor( Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) );
        try {

            mainJTree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
            mainJTree.setScrollsOnExpand( true );

            // Only add a TreeListener if one has not already been added
            if( mainJTree.getTreeSelectionListeners().length == 0) {
                mainJTree.addTreeSelectionListener( theGuiController );
            }

            DefaultTreeModel treeModel = (DefaultTreeModel)mainJTree.getModel();
            DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) treeModel.getRoot();
            
            for( Iterator<Entry<HostController, List<LibraryItemController>>> anIter = theControllerMap.entrySet().iterator();
                    anIter.hasNext(); ){
                
                //Get an entry
                Entry<HostController, List<LibraryItemController>> anEntry = anIter.next();
                HostController aHostController = anEntry.getKey();             
                
                //Add the host node
                IconNode newParentNode = new IconNode( aHostController, true );
                
                //Add the node at the end of the children of the parentNode
                int index = parentNode.getChildCount();
                mainJTree.getModel().insertNodeInto( newParentNode, parentNode, index );
               
                List<LibraryItemController> children = anEntry.getValue();
                for( LibraryItemController libController : children ){
                     mainJTree.addObjectToTree( libController, newParentNode, -1 );
                }
                
            }

            mainJTree.setRootVisible( false );
            mainJTree.getModel().reload();
            mainJTree.setSelectionRow(0);

            if( mainJTree.getSelectionRows() == null ){
                populatePanels(null);
            }

            //Pack the componenents
            pack();

        } finally {
            setCursor( null );
        }

        return true;

    }
      
    //======================================================================
    /**
    *  Determines what menu options to show on the popup menu based on the
    *  {@link XmlBase} object contained in the currently selected node.
    *
    *  @param  e   the {@code MouseEvent} that triggered the popup
     * @param selPaths
    */
    private void showSelectedPopup( MouseEvent e, TreePath[] selPaths ) {

        JPopupMenu popup = new JPopupMenu();
        JMenuItem menuItem;
        
        if( selPaths.length > 1 ) {

            for( TreePath aPath : selPaths ){
                Object anObj = aPath.getLastPathComponent();
                if( anObj != null && anObj instanceof DefaultMutableTreeNode ){
                    DefaultMutableTreeNode theSelectedNode = (DefaultMutableTreeNode)aPath.getLastPathComponent();
                    if ( theSelectedNode != null ) {
                        LibraryItemController theController = (LibraryItemController) theSelectedNode.getUserObject();
                        anObj = theController.getObject();
                        popup = theController.getPopupJMenu( true );    

                        if( anObj instanceof Host ){

                            Host aHost = (Host)anObj;
                            if( !aHost.getCheckInList().isEmpty() ){
                                break;
                            }
                        }

                    }
                }
            }
                         
        } else if( selPaths.length > 0 ) {
            
            DefaultMutableTreeNode theSelectedNode = (DefaultMutableTreeNode)selPaths[0].getLastPathComponent();
            if ( theSelectedNode != null ) {
                LibraryItemController theController = (LibraryItemController) theSelectedNode.getUserObject();
                popup = theController.getPopupJMenu( false );  
            }
        
//        } else {
//            
//            //Loop through the controller types and add their creation strings
//            //to the right click menu
//            Iterator<String> anIter = theGuiController.getActionClassMap().keySet().iterator();
//            while( anIter.hasNext() ){        
//
//                String addString = anIter.next();
//                menuItem = new JMenuItem( addString );
//                menuItem.setActionCommand( addString );
//                menuItem.addActionListener( theGuiController );
//                menuItem.setEnabled( true );
//                popup.add( menuItem );   
//
//            }
//
//            //Add separator
//            popup.addSeparator();
//
//            menuItem = new JMenuItem( Constants.IMPORT_BUNDLE );
//            menuItem.setActionCommand( Constants.IMPORT_BUNDLE );
//            menuItem.addActionListener( theGuiController );
//            menuItem.setEnabled( true );
//            popup.add( menuItem );
        }

        if( popup != null && popup.getComponentCount() > 0 ) {
            popup.show(e.getComponent(), e.getX(), e.getY());
        }

    }

//    //===============================================================
//    /**
//    * Drops the files at the specified child index
//    *
//    * @param fileArray
//    * @param childIndex
//    */
//    public void dropFilesAtIndex(File[] fileArray, int childIndex) {       
//        theGuiController.addScriptsFromFiles(fileArray, childIndex);        
//    }

    //======================================================================
    /**
    * Sets the dirty flag for the passed XmlBase
    *
    * @param passedController  the XmlBase to set the dirty flag
     * @param dirtyFlag
     * @param repaintBool
    */
    public void setDirtyFlag( LibraryItemController passedController, boolean dirtyFlag, boolean repaintBool) {

        //Set the object to dirty and show save button
        passedController.setIsDirty(dirtyFlag);
        setSaveButtons(dirtyFlag);

        //Refresh tree
        if(repaintBool){
            mainJTree.repaint();
        }

    }/* END setIsDirty( boolean ) */
    
    //======================================================================
    /**
    * Selects the tree node containing the {@code objectToSelect}.  If the {@code objectToSelect}
    * is {@code null}, or it is not found in any tree node, the root node will be selected.
    *
     * @param theController
     * @param showRunner
    */
    public void setSelectedObject( LibraryItemController theController, boolean showRunner ) {

        mainJTree.setSelectedObject( theController );
//        if(showRunner && theController instanceof JobController ){
//            JobController aJobController = (JobController)theController;
//            aJobController.showRunnerTab();
//        }
    }

    //======================================================================
    /**
    * Sets the current editor panel to display the panel for the {@code passedObject}.
    * This will not change the current "default mode" or "mode list".
    *
     * @param passedController
    *
    * @see #showEditorPanel(XmlBase, PropertyEditorToolMode, Map)
    */
    public void populatePanels( LibraryItemController passedController ) {

        JPanel theRootPanel = new JPanel();
        if( passedController != null ) {
            theRootPanel = passedController.getRootPanel();
            passedController.updateComponents();
        }

        //Set the view
        if( mainJScrollPane.getViewport().getView() != theRootPanel){
            mainJScrollPane.setViewportView(theRootPanel);
        }

        populateEditMenu();

    }/* END populatePanels() */
    
    // ==========================================================================
    /**
     * Updates the edit menu, enabling and/or disabling the options according to
     * the given set of flags.
     *
     * @param enableCut
     * @param enableCopy
     * @param enablePaste
     * @param enableSelectAll
    */
    public synchronized void updateEditMenu( boolean enableCut, boolean enableCopy,
           boolean enablePaste, boolean enableSelectAll ) {

        for( int i = 0; i < editMenu.getMenuComponentCount(); i++ ) { //For each component in the Edit menu...

            Component aComponent = editMenu.getMenuComponent( i );

            if( aComponent instanceof JMenuItem ) {
                String menuText = ((JMenuItem)aComponent).getText();

                if( menuText.equals( Utilities.EditMenuOptions.CUT.getValue() )  ) {
                    ((JMenuItem)aComponent).setEnabled( enableCut );
                } else if( menuText.equals( Utilities.EditMenuOptions.COPY.getValue() ) ) {
                    ((JMenuItem)aComponent).setEnabled( enableCopy );
                } else if( menuText.equals( Utilities.EditMenuOptions.PASTE.getValue() ) ) {
                    ((JMenuItem)aComponent).setEnabled( enablePaste );
                } else if( menuText.equals( Utilities.EditMenuOptions.SELECT_ALL.getValue() ) ) {
                    ((JMenuItem)aComponent).setEnabled( enableSelectAll );
                }

            }

        }

    }/* END updateEditMenu( boolean, boolean, boolean, boolean ) */

    //======================================================================
    /**
    * Updates the edit menu to enable/disable options based on the currently selected component.
     * @param passedVal
    */
    public void setSaveButtons(boolean passedVal) {
        saveJMenuItem.setEnabled(passedVal);
        saveAllJMenuItem.setEnabled(passedVal);
    }
    
    //======================================================================
    /**
    * Populates the edit menu with the menu items for edit operations that
    * can be performed in this window.
    *
    * @see EditMenuOptions
    */
    private void populateEditMenu() {

        JMenu menu = editMenu;

        //Clear the menu of any current menu items
        menu.removeAll();

        List<LibraryItemController> theObjList = mainJTree.getSelectedObjectControllers(); 
        if( theObjList != null ) { //If no object is selected...
            
            menu.add( theNameToMenuItemMap.get( Utilities.EditMenuOptions.CUT.getValue() ) );
            menu.add( theNameToMenuItemMap.get( Utilities.EditMenuOptions.COPY.getValue() ) );
            menu.add( theNameToMenuItemMap.get( Utilities.EditMenuOptions.PASTE.getValue() ) );
            menu.addSeparator();
            menu.add( theNameToMenuItemMap.get( Utilities.EditMenuOptions.SELECT_ALL.getValue() ) );

        } else {
            
            Utilities.EditMenuOptions.fillDummyMenu( menu );
        }
        
        //Add separator
        menu.addSeparator();
        
        //Add options menu
        JMenuItem optionsJMenuItem = new JMenuItem();
        optionsJMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        optionsJMenuItem.setMnemonic('O');
        optionsJMenuItem.setText("Options");
        optionsJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                
                try {
                    OptionsJDialog optionsGui = new OptionsJDialog( theGuiController, true);
                    optionsGui.setVisible(true); // This blocks...(evt);
                } catch (LoggableException ex) {
                    JOptionPane.showMessageDialog( theMainGui, ex.getMessage(),
                                                "Error", JOptionPane.ERROR_MESSAGE );                            
                }
            }
        });
        
        menu.add(optionsJMenuItem);

    }/* END populateEditMenu() */

//    // ==========================================================================
//    /**
//     * Displays a dialog prompting the user to confirm the run of the given {@link RunnableItemController}'s
//     * item.
//     * <p>
//     * If the argument is null this method does nothing and returns false.
//     *
//     * @param controller the {@code RunnableItemController}
//     *
//     * @return {@code true} if and only if the user selects the "Yes" option of
//     * the dialog confirming the run, {@code false} otherwise
//     *
//    */
//    public boolean promptUserToConfirmRun( RunnableItemController controller ) {
//
//        if( controller == null ) return false; //Check the argument
//        
//        //Create the prompt message...
//        StringBuilder messageBldr = new StringBuilder();
//        controller.createRunConfirmationMessage( messageBldr );
//        
//        //Prompt the user to confirm the run...
//        String itemName = controller.getItemName();
//        int usersChoice = JOptionPane.showConfirmDialog( this,
//                messageBldr.toString(), //The message
//                new StringBuilder( "Run " ).append( itemName ).append( "?" ).toString(),  //The title
//                JOptionPane.YES_NO_OPTION ); //Dialog option
//
//        //If the user chose "Yes" the run is confirmed. Otherwise, the user either chose
//        //  "No" or closed the dialog (which we'll treat as "No")
//        return JOptionPane.YES_OPTION == usersChoice;
//
//    }/* END promptUserToConfirmRun( RunnableItemController ) */
    
    
//    // ==========================================================================
//    /**
//     * Displays a dialog informing the user that the given {@link RunnableItemController}'s
//     * item is already running.
//     * <p>
//     * If the argument is null this method does nothing.
//     *
//     * @param controller the {@code RunnableItemController}
//     * 
//    */
//    public void informUserItemIsAlreadyRunning( RunnableItemController controller ) {
//
//        if( controller == null ) return; //Check the argument
//        
//        JOptionPane.showMessageDialog( this,
//                new StringBuilder( controller.getItemName() ).append( " is already running." ).toString(), //The message
//                "Already running",  //The title
//                JOptionPane.OK_OPTION ); //Dialog option
//
//    }/* END informUserIsAlreadyRunning( RunnableItemController ) */
    
    
    // ========================================================================
    /**
     * Displays a dialog informing the user that the given item cannot be run.
     * <p>
     * If either argument is null this method does nothing.
     *
     * @param itemName the name of the item
     * @param reason the reason the item cannot run
     * 
    */
    public void informUserItemCantRun( String itemName, String reason ) {

        if( itemName == null || reason == null ) return; //Check the arguments
        
        JOptionPane.showMessageDialog( this,
                new StringBuilder( itemName ).append( " cannot run.\n\n" ).append( reason ).toString(), //The message
                "Cannot Run " + itemName,  //The title
                JOptionPane.WARNING_MESSAGE ); //Dialog option

    }/* END informUserItemCantRun( String, String ) */
    

    // ==========================================================================
    /**
    *   Keeps the windows taskbar from being hidden during maximize
     * @param state
    */
    @Override
    public synchronized void setExtendedState(int state){
        if( maxBounds == null &&
                (state & java.awt.Frame.MAXIMIZED_BOTH) == java.awt.Frame.MAXIMIZED_BOTH){
            Insets screenInsets = getToolkit().getScreenInsets(getGraphicsConfiguration());
            Rectangle screenSize = getGraphicsConfiguration().getBounds();
            maxBounds = new Rectangle(screenInsets.left, screenInsets.top,
                                                screenSize.width - screenInsets.right - screenInsets.left,
                                                screenSize.height - screenInsets.bottom - screenInsets.top);
            super.setMaximizedBounds(maxBounds);
        }
        super.setExtendedState(state);
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainJSplitPane = new javax.swing.JSplitPane();
        mainJScrollPane = new javax.swing.JScrollPane();
        mainMenuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        importJMenuItem = new javax.swing.JMenuItem();
        exportJMenuItem = new javax.swing.JMenuItem();
        fileMenuSepartor = new javax.swing.JPopupMenu.Separator();
        saveJMenuItem = new javax.swing.JMenuItem();
        saveAllJMenuItem = new javax.swing.JMenuItem();
        fileMenuSepartor1 = new javax.swing.JPopupMenu.Separator();
        exitJMenuItem = new javax.swing.JMenuItem();
        editMenu = new javax.swing.JMenu();
        cutJMenuItem = new javax.swing.JMenuItem();
        copyJMenuItem = new javax.swing.JMenuItem();
        pasteJMenuItem = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JSeparator();
        selectAllMenuItem = new javax.swing.JMenuItem();
        runMenu = new javax.swing.JMenu();
        runMeuItem = new javax.swing.JMenuItem();
        stopMenuItem = new javax.swing.JMenuItem();
        viewMenu = new javax.swing.JMenu();
        tasksMenuItem = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        helpUserManualJMenuItem = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JSeparator();
        helpAboutJMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("PwnBrew");
        setBounds(new java.awt.Rectangle(0, 0, 0, 0));
        setMinimumSize(new java.awt.Dimension(815, 625));
        setPreferredSize(new java.awt.Dimension(815, 590));
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });

        mainJSplitPane.setDividerLocation(180);
        mainJSplitPane.setContinuousLayout(true);
        mainJSplitPane.setLastDividerLocation(180);
        mainJSplitPane.setMinimumSize(new java.awt.Dimension(150, 102));
        mainJSplitPane.setPreferredSize(new java.awt.Dimension(725, 523));

        mainJScrollPane.setMinimumSize(new java.awt.Dimension(0, 0));
        mainJScrollPane.setPreferredSize(new java.awt.Dimension(600, 459));
        mainJSplitPane.setRightComponent(mainJScrollPane);

        mainMenuBar.setMaximumSize(new java.awt.Dimension(93, 21));
        mainMenuBar.setPreferredSize(new java.awt.Dimension(155, 25));

        fileMenu.setMnemonic('F');
        fileMenu.setText("File");

        importJMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_I, java.awt.event.InputEvent.CTRL_MASK));
        importJMenuItem.setText("Import");
        importJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importJMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(importJMenuItem);

        exportJMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, java.awt.event.InputEvent.CTRL_MASK));
        exportJMenuItem.setText("Export");
        exportJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportJMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(exportJMenuItem);
        fileMenu.add(fileMenuSepartor);

        saveJMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        saveJMenuItem.setMnemonic('x');
        saveJMenuItem.setText("Save");
        saveJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveJMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(saveJMenuItem);

        saveAllJMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        saveAllJMenuItem.setMnemonic('x');
        saveAllJMenuItem.setText("Save All");
        saveAllJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveAllJMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(saveAllJMenuItem);
        fileMenu.add(fileMenuSepartor1);

        exitJMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, java.awt.event.InputEvent.ALT_MASK));
        exitJMenuItem.setMnemonic('x');
        exitJMenuItem.setText("Exit");
        exitJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitJMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(exitJMenuItem);

        mainMenuBar.add(fileMenu);

        editMenu.setMnemonic('E');
        editMenu.setText("Edit");

        cutJMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.CTRL_MASK));
        cutJMenuItem.setMnemonic('X');
        cutJMenuItem.setText("Cut");
        editMenu.add(cutJMenuItem);

        copyJMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_MASK));
        copyJMenuItem.setMnemonic('C');
        copyJMenuItem.setText("Copy");
        editMenu.add(copyJMenuItem);

        pasteJMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.event.InputEvent.CTRL_MASK));
        pasteJMenuItem.setMnemonic('P');
        pasteJMenuItem.setText("Paste");
        editMenu.add(pasteJMenuItem);
        editMenu.add(jSeparator5);

        selectAllMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.CTRL_MASK));
        selectAllMenuItem.setMnemonic('A');
        selectAllMenuItem.setText("Select All");
        editMenu.add(selectAllMenuItem);

        mainMenuBar.add(editMenu);

        runMenu.setMnemonic('F');
        runMenu.setText("Run");

        runMeuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_MASK));
        runMeuItem.setMnemonic('r');
        runMeuItem.setLabel("Run Selected");
        runMeuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runMeuItemActionPerformed(evt);
            }
        });
        runMenu.add(runMeuItem);

        stopMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.CTRL_MASK));
        stopMenuItem.setMnemonic('p');
        stopMenuItem.setText("Stop Selected");
        stopMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopMenuItemActionPerformed(evt);
            }
        });
        runMenu.add(stopMenuItem);

        mainMenuBar.add(runMenu);

        viewMenu.setText("View");

        tasksMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_T, java.awt.event.InputEvent.CTRL_MASK));
        tasksMenuItem.setMnemonic('T');
        tasksMenuItem.setText("Tasks");
        tasksMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tasksMenuItemActionPerformed(evt);
            }
        });
        viewMenu.add(tasksMenuItem);

        mainMenuBar.add(viewMenu);

        helpMenu.setMnemonic('H');
        helpMenu.setText("Help");

        helpUserManualJMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_M, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        helpUserManualJMenuItem.setMnemonic('M');
        helpUserManualJMenuItem.setText("User Manual");
        helpUserManualJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpUserManualJMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(helpUserManualJMenuItem);
        helpMenu.add(jSeparator3);

        helpAboutJMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        helpAboutJMenuItem.setMnemonic('A');
        helpAboutJMenuItem.setText("About PwnBrew");
        helpAboutJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpAboutJMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(helpAboutJMenuItem);

        mainMenuBar.add(helpMenu);

        setJMenuBar(mainMenuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(mainJSplitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 723, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(mainJSplitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 512, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void exitJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitJMenuItemActionPerformed
       theGuiController.closeWindow();
}//GEN-LAST:event_exitJMenuItemActionPerformed

    private void helpUserManualJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpUserManualJMenuItemActionPerformed
       
//        String retString = GuiUtilities.displayUserManual();
//        if(retString != null){
//           JOptionPane.showMessageDialog( this, retString, "", JOptionPane.ERROR_MESSAGE );
//        }
      
}//GEN-LAST:event_helpUserManualJMenuItemActionPerformed

    private void helpAboutJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpAboutJMenuItemActionPerformed
       displayHelp();
}//GEN-LAST:event_helpAboutJMenuItemActionPerformed

    private void runMeuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runMeuItemActionPerformed
//        List<LibraryItemController> theControllerList = getJTree().getSelectedObjectControllers();
//        for( LibraryItemController aController : theControllerList ){
//            if( aController instanceof RunnableItemController ) {
//                theGuiController.runObject( (RunnableItemController)theControllerList );
//            }
//        }
    }//GEN-LAST:event_runMeuItemActionPerformed

    private void stopMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopMenuItemActionPerformed
//        List<LibraryItemController> theControllerList = getJTree().getSelectedObjectControllers();
//        for( LibraryItemController aController : theControllerList ){
//            if( aController instanceof RunnableItemController ){
//                theGuiController.cancelRunForCurrentNode((RunnableItemController) aController);
//            }
//        }
    }//GEN-LAST:event_stopMenuItemActionPerformed

    private void saveJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveJMenuItemActionPerformed
       theGuiController.saveCurrentObject();
    }//GEN-LAST:event_saveJMenuItemActionPerformed

    private void saveAllJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAllJMenuItemActionPerformed
       theGuiController.saveAllObjects(true);
    }//GEN-LAST:event_saveAllJMenuItemActionPerformed

    private void exportJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportJMenuItemActionPerformed
//        theGuiController.exportLibraryObjects( getJTree().getSelectedObjectControllers() );
    }//GEN-LAST:event_exportJMenuItemActionPerformed

    private void importJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importJMenuItemActionPerformed
//       theGuiController.importBundle( null );
    }//GEN-LAST:event_importJMenuItemActionPerformed

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
        super.setCursor(null);
    }//GEN-LAST:event_formComponentResized

    private void tasksMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tasksMenuItemActionPerformed
        TasksJDialog theTasksDialog = TasksJDialog.getTasksJDialog();
        theTasksDialog.setVisible(true);
    }//GEN-LAST:event_tasksMenuItemActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem copyJMenuItem;
    private javax.swing.JMenuItem cutJMenuItem;
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenuItem exitJMenuItem;
    private javax.swing.JMenuItem exportJMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JPopupMenu.Separator fileMenuSepartor;
    private javax.swing.JPopupMenu.Separator fileMenuSepartor1;
    private javax.swing.JMenuItem helpAboutJMenuItem;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JMenuItem helpUserManualJMenuItem;
    private javax.swing.JMenuItem importJMenuItem;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JScrollPane mainJScrollPane;
    private javax.swing.JSplitPane mainJSplitPane;
    private javax.swing.JMenuBar mainMenuBar;
    private javax.swing.JMenuItem pasteJMenuItem;
    private javax.swing.JMenu runMenu;
    private javax.swing.JMenuItem runMeuItem;
    private javax.swing.JMenuItem saveAllJMenuItem;
    private javax.swing.JMenuItem saveJMenuItem;
    private javax.swing.JMenuItem selectAllMenuItem;
    private javax.swing.JMenuItem stopMenuItem;
    private javax.swing.JMenuItem tasksMenuItem;
    private javax.swing.JMenu viewMenu;
    // End of variables declaration//GEN-END:variables


    //======================================================================
    /**
    *  Adds a {@code MouseListener} to listen for {@code MouseEvent}'s
    *  occurring over the {@code JTree} and trigger a popup menu when
    *  appropriate.
    */
    private void addTreeRightClickListener() {

        MouseListener ml = new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                
                //Get selection rows
                TreePath[] selRows = mainJTree.getSelectionPaths();
                if( selRows != null ){
                    if(e.getButton() == MouseEvent.BUTTON3 && e.getClickCount() == 1){
                        showSelectedPopup( e, selRows );
                    }

                    //If they click away then deselect
                    if(e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1){

                        int selRow = mainJTree.getRowForLocation(e.getX(), e.getY());
                        if( selRow == -1 ){
                            mainJTree.clearSelection();
                        }
                    }
                }

            } // end MouseReleased
        }; // end MouseAdapter class
        mainJTree.addMouseListener(ml);

    }

    //=========================================================================
    /**
    *  Adds a {@code KeyListener} to listen for {@code KeyEvent}'s
    *  occurring in the {@code JTree}
    */
    private void addTreeKeyListener() {

        KeyListener ml = new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                if(e.getKeyChar() == KeyEvent.VK_DELETE){
                    
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
                    
                    theGuiController.deleteObjects( selObjects, true );
                }
            }
        }; // end MouseAdapter class
        mainJTree.addKeyListener(ml);

    }

    //===============================================================
    /**
     * Sets the cursor
     *
     * @param defaultCursor
    */
    public void iTaskControllerListener_setCursor(Cursor defaultCursor) {
        setCursor(defaultCursor);
    }

    // ==========================================================================
    /**
    * Sets the cursor
    *
    * @param predefinedCursor
    *
    */
    @Override
    public void setCursor(Cursor predefinedCursor) {
        super.setCursor(predefinedCursor);
    }

    // ==========================================================================
    /**
     *  Returns the primary scroll pane for the gui.
     *
     * @return 
    */
    public JScrollPane getMainScrollPane() {
        return mainJScrollPane;
    }
    
    // ==========================================================================
    /**
     *  Returns the JTree
     *
     * @return 
    */
    public LibraryItemJTree getJTree() {
        return mainJTree;
    }
    
    // ==========================================================================
    /**
     *  Returns the JFileChooser
     *
     * @return 
    */
    public JFileChooser getFileChooser() {
        return theFileChooser;
    }
   
}/* END CLASS MainGui */
