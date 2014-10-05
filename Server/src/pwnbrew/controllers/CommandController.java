///*
//
//Copyright (C) 2013-2014, Securifera, Inc 
//
//All rights reserved. 
//
//Redistribution and use in source and binary forms, with or without modification, 
//are permitted provided that the following conditions are met:
//
//    * Redistributions of source code must retain the above copyright notice,
//	this list of conditions and the following disclaimer.
//
//    * Redistributions in binary form must reproduce the above copyright notice,
//	this list of conditions and the following disclaimer in the documentation 
//	and/or other materials provided with the distribution.
//
//    * Neither the name of Securifera, Inc nor the names of its contributors may be 
//	used to endorse or promote products derived from this software without specific
//	prior written permission.
//
//THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS 
//OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY 
//AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER 
//OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
//CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
//SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON 
//ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
//(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
//EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
//================================================================================
//
//Pwnbrew is provided under the 3-clause BSD license above.
//
//The copyright on this package is held by Securifera, Inc
//
//*/
//
///*
//* CommandController.java
//*
//* Created on Sept 19, 2013, 7:23:33 PM
//*/
//
//package pwnbrew.controllers;
//
//import pwnbrew.library.LibraryItemControllerListener;
//import pwnbrew.execution.ExecutableItem;
//import pwnbrew.execution.ExecutionHandler;
//import pwnbrew.execution.ExecutionObserver;
//import pwnbrew.gui.panels.RunnerPane;
//import pwnbrew.logging.Log;
//import pwnbrew.xmlBase.Command;
//import pwnbrew.xmlBase.FileContentRef;
//import pwnbrew.tasks.RemoteTask;
//import pwnbrew.exception.XmlBaseCreationException;
//import pwnbrew.xmlBase.XmlBaseFactory;
//import java.awt.Image;
//import java.awt.event.ActionListener;
//import java.awt.image.BufferedImage;
//import java.util.List;
//import java.util.logging.Level;
//import javax.swing.*;
//import javax.swing.tree.DefaultMutableTreeNode;
//import pwnbrew.generic.gui.NewProgressDialog;
//import pwnbrew.gui.MainGui;
//import pwnbrew.gui.panels.command.CommandOverviewPanelListener;
//import pwnbrew.gui.panels.command.CommandTabPanel;
//import pwnbrew.host.Host;
//import pwnbrew.host.HostController;
//import pwnbrew.library.LibraryItemController;
//import pwnbrew.misc.Constants;
//import pwnbrew.misc.IdGenerator;
//import pwnbrew.utilities.SocketUtilities;
//import pwnbrew.utilities.StringUtilities;
//import pwnbrew.utilities.Utilities;
//
///**
// *
// * 
// */ 
//public class CommandController extends JobController implements ActionListener, CommandOverviewPanelListener, ExecutionObserver {
//
//    private static final String ACTION_Create = "Create Command";
//    
//    private Command theCommand = null;
//    private CommandTabPanel theTabPanel = null;
//    private LibraryItemControllerListener theListener = null;
//    
//    public static final BufferedImage commandBuffImage = Utilities.loadImageFromJar( Command.ICON_STR );    
//    private static final Icon commandIcon = new ImageIcon( commandBuffImage.getScaledInstance( ICON_WIDTH,  ICON_HEIGHT, Image.SCALE_SMOOTH));
//        
//    private static final BufferedImage commandNegBuffImage = Utilities.loadImageFromJar( Command.NEG_TASK_ICON_STR  );  
//    private static final Icon commandNegIcon = new ImageIcon( commandNegBuffImage.getScaledInstance( ICON_WIDTH, ICON_HEIGHT, Image.SCALE_SMOOTH));
//    private final JPopupMenu commandPopupMenu = new JPopupMenu();
//  
//    private NewProgressDialog theProgressDialog = null;
//   
//    //===============================================================
//    /**
//     *  Constructor 
//     *  ( Necessary for the right click menu creation string )
//    */
//    public CommandController() {
//        theIcon = commandIcon; 
//    }
//    
//    // ==========================================================================
//    /**
//     * Constructor
//     *
//    */
//    private CommandController(Command command ) {
//
//        this();
//        theCommand = command;
//    }
//    
//    // ==========================================================================
//    /**
//     * Constructor
//     *
//     * @param command
//     * @param passedControllerListener
//    */
//    public CommandController(Command command, LibraryItemControllerListener passedControllerListener ) {
//
//        this( command );
//        theListener = passedControllerListener;
//    }
//    
//    // ========================================================================
//    /**
//     *  Sets the listener for the controller
//     * 
//     * @param passedListener
//     */
//    public void setListener( LibraryItemControllerListener passedListener ) {
//        
//        theListener = passedListener;
//        
//    }/* END setListener() */
//    
//     // ========================================================================
//    /**
//     *  Returns the object being managed
//     * @return 
//     */
//    @Override
//    public Command getObject() {
//       return theCommand;
//    }
//
//    // ==========================================================================
//    /**
//     * Creates the {@link TaskTabPanel} for the {@link Task}.
//     *
//     * @see #getProgressPanel()
//     */
//    private void createTabPanel() {
//        theTabPanel = new CommandTabPanel( this );
//    }/* END createTabPanel() */
//
//
//    // ==========================================================================
//    /**
//     * Returns the {@link TaskTabPanel} for the {@link TaskController}'s {@link Task}.
//     * <p>
//     * If the {@code TaskTabPanel} has not yet been created this method will create
//     * it.
//     *
//     * @return the {@code TaskTabPanel} for the {@code TaskController}'s {@code Task}
//     *
//     */
//    @Override
//    public JPanel getRootPanel() {
//        if( theTabPanel == null ){ //If the TaskTabPanel has not yet been created...
//            createTabPanel(); //Create the TaskTabPanel for the Task 
//        }
//        return theTabPanel;
//    }/* END getTabPanel() */
//
//    // ==========================================================================
//    /**
//     *  Updates the GUI components being managed by the controller.
//     * 
//     */
//    @Override
//    public void updateComponents() {
//
//       theTabPanel.setLibraryItemName( theCommand.getName() );
//      
//       //Update the Runner Pane
//       getRunnerPane( false );
//
//       JTabbedPane theTabPane = theTabPanel.getTabCollection();
//       theTabPane.setEnabledAt( 0, true);
//       theTabPane.setEnabledAt( 1, true);
//    }
//
//    // ==========================================================================
//    /**
//    * Returns the runner pane for the task
//    *
//    * @param passedBool
//    * @return 
//    */
//    @Override
//    public RunnerPane getRunnerPane( boolean passedBool ) {
//
//        CommandTabPanel aTabPanel = (CommandTabPanel)getRootPanel();
//        JTextPane theTextPane = aTabPanel.getRunnerTextPane();
//        if(!(theTextPane instanceof RunnerPane)){
//            theTextPane = new RunnerPane();
//            aTabPanel.setRunnerTextPane((RunnerPane) theTextPane);
//        }
//
//        //Show runner panel
//        if( passedBool ){
//            showRunnerTab();
//        }
//
//        return (RunnerPane)theTextPane;
//    }
//  
//    // ==========================================================================
//    /**
//     * Sets the description
//     *
//    */
//    @Override
//    public void descriptionChanged(String passedString) {
//
//      //Set the text from the description area in the ScriptSet
//      if(theCommand != null){
//         theCommand.setAttribute( Command.ATTRIBUTE_Description, passedString );
//         theListener.valuesChanged(this);
//      }
//    }
//
//    // ==========================================================================
//    /**
//     * Sets the command
//     *
//     * @param passedString
//    */
//    @Override
//    public void commandChanged(String passedString) {
//        //Set the text from the description area in the ScriptSet
//      if(theCommand != null){
//        
//         if( passedString != null ) {
//           
//           boolean useGivenCommand = false;
//           
//           List<String> currentCommandStrings = theCommand.getCommand();
//           if( currentCommandStrings != null ) {
//             
//               String[] givenCommandStrings = passedString.trim().split(" ");
//               if( givenCommandStrings.length == currentCommandStrings.size() ) {
//
//                   for( int i = 0; i < givenCommandStrings.length; i++ ) {
//
//                       if( givenCommandStrings[ i ].equals( currentCommandStrings.get( i ) ) == false ) {
//                           useGivenCommand = true;
//                           break;
//                       }
//
//                   }
//
//               } else {
//                   useGivenCommand = true;
//               }
//             
//           } else {
//               useGivenCommand = true;
//           }
//           
//           if( useGivenCommand ) {
//             
//               String[] givenCommandStrings = passedString.trim().split(" ");
//               theCommand.setCommand( givenCommandStrings );
//               theListener.valuesChanged(this);
//           
//           }
//           
//         }
//         
//      }
//      
//    }
//    
//    // ==========================================================================
//    /**
//     * Adds a support file
//     *
//    */
//    @Override
//    public void supportFileAdded(FileContentRef aFileContentRef) {
//        theCommand.addUpdateComponent(aFileContentRef);
//        theListener.valuesChanged(this);
//    }
//
//    // ==========================================================================
//    /**
//     * Removes a support file
//     *
//     * @param theFileContentRef
//    */
//    @Override
//    public void supportFileRemoved(FileContentRef theFileContentRef) {
//        theCommand.removeComponent(theFileContentRef);
//        theListener.valuesChanged(this);
//    }
//
//     // ==========================================================================
//    /**
//     * Switches to the runner panel in the tabbed pane
//     *
//    */
//    @Override
//    public void showRunnerTab() {
//       theTabPanel.getTabCollection().setSelectedComponent( theTabPanel.getRunnerPanel() );
//    }
//
//    // ==========================================================================
//    /**
//     * Runs the {@link Command}.
//     * @param observer
//     */
//    @Override //RunnableItemController
//    public void runItem( RunObserver observer ) {
//        
//        theRunObserver = observer;
//        
//        RunnerPane theRunnerPane = getRunnerPane( true );
//        theRunnerPane.setText( " " );
//        
//        showRunnerTab();
//        
//        errorOccurred = false;
//        cancelled = false;
//
//        //Run the Command...
//        theExecutionHandler = new ExecutionHandler( theCommand, this );
//        Constants.Executor.execute( theExecutionHandler );
//        
//    }/* END runItem( RunObserver ) */
//    
//     // ==========================================================================
//    /**
//     * Cancels the running of the {@link Command}.
//     */
//    @Override //RunnableItemController
//    public void cancelRun() {
//        
//        if( theExecutionHandler != null ) {
//            theExecutionHandler.stopExecution();
//        }
//        
//        cancelled = true;
//        
//        theCommand.setAttribute( Command.theLastRunResult, Constants.LastRunResults_Cancelled );
//        
//    }/* END cancelRun() */
//      
//    // ========================================================================
//    /**
//     * Determines if the {@link Command} can run.
//     * 
//     * @return null if the {@code Command} can be run; a String describing why the
//     * {@code Command} cannot be run
//     */
//    @Override
//    public String canRun() {
//        
//        String rtnString = null;
//        
//        if( theCommand == null ) {
//            
//            rtnString = "There is no Command to run.";
//            
//        } else {
//            
//            String[] commandArgs = theCommand.getCommandArgs( Utilities.getOsName() );
//            if( commandArgs.length == 0 ) {
//                rtnString = "No command has been set.";
//            }
//            
//        }
//        
//        return rtnString;
//        
//    }/* END canRun() */
//    
//
//    // ========================================================================
//    /**
//     * Creates a message for confirming the run of the {@link Command}.
//     * <p>
//     * If the argument is null this method does nothing.
//     * 
//     * @param builder the {@link StringBuilder} to which the message is to be appended
//     */
//    @Override //RunnableItemController
//    public void createRunConfirmationMessage( StringBuilder builder ) {
//        
//        if( builder == null ) return; //Check the argument
//
//        builder.append( "Are you sure you want to run this command?\n\n" );
//
//        List<String> theCommandStringList = theCommand.getCommand();
//        String command = StringUtilities.join( theCommandStringList, " " );
//        builder.append( command );
//        
//    }/* END createRunConfirmationMessage( StringBuilder ) */
//    
//    
//    // ==========================================================================
//    /**
//    * Called by a {@link ExecutableItem} to process the stdout.
//    * <p>
//    * If the given {@code ExecutableItem} is null, this method does nothing and returns null.
//    *
//    * @param item the {@code ExecutableItem}
//     * @param buffer
//    */
//    @Override //ExecutionObserver
//    public void executionObserver_HandleStdoutData( ExecutableItem item, byte[] buffer ){
//    
//        if( item == null )
//            return;        
//
//        //Send to the runner pane.
//        RunnerPane theRunnerPane = getRunnerPane( false );
//        theRunnerPane.handleStreamBytes(Constants.STD_OUT_ID, new String( buffer ));
//        
//    }
//
//    // ==========================================================================
//    /**
//    * Called by a {@link ExecutableItem} to process the stderr.
//    * <p>
//    * If the given {@code ExecutableItem} is null, this method does nothing and returns null.
//    *
//    * @param item the {@code ExecutableItem}
//     * @param buffer
//    */
//    @Override //ExecutionObserver
//    public void executionObserver_HandleStderrData( ExecutableItem item, byte[] buffer ) {
//
//        if( item == null )
//            return;
//                
//        errorOccurred = true;
//        theCommand.setAttribute( Command.theLastRunResult, Constants.LastRunResults_ErrorOccurred );
//
//
//        //Send to the runner pane
//        RunnerPane theRunnerPane = getRunnerPane( false );
//        theRunnerPane.handleStreamBytes(Constants.STD_ERR_ID, new String( buffer) );
//            
//        if( theRunObserver != null )
//            theRunObserver.errorOccurred( this );
//        
//    }
//    
//    // ==========================================================================
//    /**
//     * {@inheritDoc }
//     * @return 
//     */
//    @Override
//    public String toString() {
//       return theCommand.getName();
//    }
//
//    // ==========================================================================
//    /**
//    * Called by a {@link ExecutableItem} when it is running.
//    *
//    * @param item the {@code ExecutableItem}
//    */
//    @Override //ExecutionObserver
//    public void executionObserver_ExecutionStarting( ExecutableItem item ) {
//    
//        theCommand.setAttribute( Command.theLastRunResult, Constants.LastRunResults_Running );
//      
//        if( theRunObserver != null )
//            theRunObserver.runBeginning( this );
//        updateLastRunDetails();
//        
//    }
//    
//
//    // ==========================================================================
//    /**
//    * Called by a {@link ExecutableItem} when it has finished executing its Runnable.
//    *
//    * @param item the {@code ExecutableItem} that completed the execution of its Runnable.
//    */
//    @Override //ExecutionObserver
//    public void executionObserver_ExecutionComplete( ExecutableItem item ) {
//        
//        if( !errorOccurred && !cancelled ) //If no errors occurred and the run wasn't cancelled...
//            theCommand.setAttribute( Command.theLastRunResult, Constants.LastRunResults_Completed );
//        
//        
//        theExecutionHandler = null;
//        
//        if( theRunObserver != null ) {
//            theRunObserver.runCompleted( this );
//            theRunObserver = null;
//        }
//        
//        updateLastRunDetails();
//   
//        
//    }/* END executionObserver_ExecutionComplete( ExecutableItem ) */
//    
//
//    // ==========================================================================
//    /*  Updates the last run details 
//    *
//    */
//    @Override
//    public void updateLastRunDetails(){
//        saveToDisk();
//        ((MainGui)theListener.getListenerComponent()).repaint(); //Repaint the job/set tree  
//    }
//    
//    // ==========================================================================
//    /**
//    * Called by a {@link ExecutableItem} when it is unable to execute its ExecutableItem.
//    *
//    * @param item the {@code ExecutableItem} that failed to execute
//    * @param message a message explaining why the execution failed
//    */
//    @Override //ExecutionObserver
//    public void executionObserver_ExecutionFailed( ExecutableItem item, String message ) {
//        
//        if( item == null ) {
//            return; //Do nothing
//        }
//
//        Command passedItem = (Command)item;
//        //Display a dialog box explaining the problem...
//        StringBuilder messageBuilder = new StringBuilder( "Could not run " );
//        messageBuilder.append( passedItem.getName() ).append( ".\n" ).append( message );
//
//        JOptionPane.showMessageDialog( theListener.getListenerComponent(), messageBuilder.toString(),
//                    passedItem.getName() + " Failed", JOptionPane.ERROR_MESSAGE );
//        
//        theExecutionHandler = null;
//        
//        //Set the last run result
//        theCommand.setAttribute( Command.theLastRunResult, Constants.LastRunResults_Failed );
//   
//        
//        if( theRunObserver != null ) {
//            theRunObserver.runCompleted( this );
//            theRunObserver = null;
//        }
//        
//        updateLastRunDetails();
//    }
//    
//    
//    // ========================================================================
//    /**
//     * 
//     * @param item
//     */
//    @Override //ExecutionObserver
//    public void executionObserver_PreparingForExecution( ExecutableItem item ) {
//        if( theRunObserver != null )
//            theRunObserver.preparingToRun( this );
//    }/* END executionObserver_FileCopyBeginning( ExecutableItem ) */
//    
//    
//    // ==========================================================================
//    /**
//    * Called by a {@link ExecutableItem} to convey the progress of a file copy
//    *
//     * @param item
//     * @param fileBytes
//     * @param fileName
//     * @param bytesCopied
//    */
//    @Override //ExecutionObserver
//    public void executionObserver_FileCopyProgress(ExecutableItem item, String fileName, long bytesCopied, long fileBytes) {
//        
//        //Create the dialog if it is null
//        if( theProgressDialog == null ){
//            
//            theProgressDialog = new NewProgressDialog( null, "Copying files...", false );
//            
//            SwingUtilities.invokeLater( new Runnable(){
//
//                @Override
//                public void run() {                    
//                    //Set the dialog to visible
//                    theProgressDialog.setVisible( true );
//                }          
//            
//            });
//            
//        } 
//          
//        //Calculate the progress
//        int tempProgressInt = 0;
//        if(fileBytes != 0){
//            double tempProgressDouble = (1.0 * bytesCopied) / (1.0 * fileBytes );
//            tempProgressInt = (int)Math.round(tempProgressDouble * 100.0);
//        }
//
//        //Set the progress if it is not what is already there
//        if( tempProgressInt != theProgressDialog.getProgress() ){
//            theProgressDialog.setProgress(tempProgressInt);
//        }
//        
//    }
//    
//    // ==========================================================================
//    /**
//    * Called by a {@link ExecutableItem} when it is finished copying a file.
//    *
//    * @param item the {@code ExecutableItem} that finished copying.
//    * @param fileName the filename
//    */
//    @Override //ExecutionObserver
//    public void executionObserver_FileCopyComplete(ExecutableItem item, String fileName) {
//        
//        if( theProgressDialog != null ){ 
//            theProgressDialog.dispose();
//            theProgressDialog = null;
//        }
//    }
//
//    
//    // ========================================================================
//    /**
//     * Returns the name of the {@link Command}.
//     * 
//     * @return the name of the {@code Command}; null if the {@code Command} is not set
//     */
//    @Override //LibraryItemController
//    public String getItemName() {
//        
//        String rtnString = null;
//        if( theCommand != null )
//            rtnString = theCommand.getName();
//        
//        return rtnString;
//        
//    }/* END getItemName() */
//
//    
//    // ========================================================================
//    /**
//     * Returns the name of the {@link Command} type for use in messages to the
//     * user.
//     * @return 
//     */
//    @Override //LibraryItemController
//    public String getItemTypeDisplayName() {
//        return "Command";
//    }
//
//     // ========================================================================
//    /**
//     * Returns the last run result of the {@link Command} to the user.
//     * @return 
//     */
//    @Override
//    public String getLastRunResult() {
//        return theCommand.getAttribute( Command.theLastRunResult);
//    }
//
//    // ========================================================================
//    /**
//     *  Sets the object being managed by the controller.
//     * @param passedObj
//     */
//    @Override
//    public void setObject(Object passedObj) {
//        theCommand = (Command) passedObj;               
//    }
//    
//    // ========================================================================
//    /**
//     *  Returns the icon for the library item.
//     * @return 
//     */
//    @Override
//    public Icon getIcon() {
//        
//        Icon anIcon;
//        if( theExecutionHandler != null ){
//            anIcon = commandNegIcon;
//        } else {
//            anIcon = commandIcon;
//        }
//        return anIcon;
//    }
//    
//     // ==========================================================================
//    /**
//    * Clones the command controller and the underlying object.
//    *
//     * @return 
//    */
//    @Override
//    public CommandController copy(){
//        
//        Command newCommand;
//        CommandController aController = null;
//        try {
//            
//            //Clone the command and create a new controller
//            newCommand = XmlBaseFactory.clone( theCommand);
//            aController = new CommandController( newCommand, theListener );
//                       
//        } catch (XmlBaseCreationException ex) {
//            Log.log(Level.SEVERE, NAME_Class, "clone()", ex.getMessage(), ex );
//        }
//        
//        return aController;
//    }
//    
//     // ========================================================================
//    /**
//     *  Returns the right click popup menu for the library item.
//     * @param multi
//     * @return 
//     */
//    @Override
//    public JPopupMenu getPopupJMenu( boolean multi ){
//        
//        JMenuItem menuItem;
//        commandPopupMenu.removeAll();
//        
//        if( getHostController().isLocalHost() ){
//
//            menuItem = new JMenuItem( Constants.ACTION_Run );
//            menuItem.setActionCommand( Constants.ACTION_Run );
//            menuItem.setEnabled( true );
//            menuItem.addActionListener( this );
//
//            commandPopupMenu.add(menuItem);
//
//        } else {
//
//            //Only show run if it is connected
//            if( getHostController().isConnected() ){
//                menuItem = new JMenuItem( Constants.ACTION_Run );
//                menuItem.setActionCommand( Constants.ACTION_Run );
//                menuItem.setEnabled( true );
//                menuItem.addActionListener( this );
//                commandPopupMenu.add(menuItem);
//            }
//
//        }
//
//        menuItem = new JMenuItem( Constants.ACTION_Remove );
//        menuItem.setActionCommand( Constants.ACTION_Remove );
//        menuItem.setEnabled( true );
//        menuItem.addActionListener( (ActionListener)theListener );
//        commandPopupMenu.add(menuItem);
//        
//        menuItem = new JMenuItem( Constants.ACTION_Rename );
//        menuItem.setActionCommand( Constants.ACTION_Rename );
//        menuItem.setEnabled( true );
//        menuItem.addActionListener( (ActionListener)theListener );
//        commandPopupMenu.add( menuItem );       
//
//        commandPopupMenu.addSeparator();
//        menuItem = new JMenuItem( Constants.EXPORT_TO_BUNDLE );
//        menuItem.setActionCommand( Constants.EXPORT_TO_BUNDLE );
//        menuItem.setEnabled( true );
//        menuItem.addActionListener( (ActionListener)theListener );
//        commandPopupMenu.add(menuItem); 
//        
//        return commandPopupMenu;
//    }
//     
//    // ========================================================================
//    /**
//     *  Returns the string displayed when adding a new object and controller.
//     * @return 
//     */
//    @Override
//    public String getCreationAction() {
//        return ACTION_Create;
//    }
//     
//    // ========================================================================
//    /**
//     *  Creates the underlying item that the controller is in charge of.
//     * @param passedListener
//     * @return 
//     */
//    @Override
//    public Object createItem( LibraryItemControllerListener passedListener ) {
//         
//        Command newCommand = null;
//        String commandName;
//        theListener = passedListener;
//        MainGuiController theMainController = (MainGuiController)theListener;
//        MainGui theGui = (MainGui) theListener.getListenerComponent();
//        
//        while( true ) { //Forever...
//            
//            //Get a name for the new Command from the user
//            commandName = JOptionPane.showInputDialog( theGui,
//                    "Enter a name for the command:\n(Leading and trailing whitespace is ignored.)", "Command Name", JOptionPane.PLAIN_MESSAGE );
//            
//            if( commandName != null ) { //If the user provided a value...
//
//                commandName = commandName.trim(); //Trim any surrounding whitespace
//                if( commandName.isEmpty() == false ) { //If the user provided a valid value...
//
//                    DefaultMutableTreeNode theNode = (DefaultMutableTreeNode) theGui.getJTree().getLastSelectedPathComponent();
//                    LibraryItemController parentController = (LibraryItemController) theNode.getUserObject();
//                    if( theMainController.getControllerByObjName( parentController.getItemName(), Command.class.getSimpleName(), commandName) == null ) { //If no other Command has the given name...
//
//                        newCommand = new Command();
//                        break; //Stop prompting the user
//
//                    } else { //If another Command has the given name...
//                        JOptionPane.showMessageDialog( theGui,
//                                "A Command with that name already exists.  Please enter a different name.",
//                                "Duplicate Name", JOptionPane.ERROR_MESSAGE );
//                    }
//
//                } else { //If the user provided an invalid value...
//                    JOptionPane.showMessageDialog( theGui,
//                            "That is not a valid name.", "Invalid Name", JOptionPane.ERROR_MESSAGE );
//                }
//
//            } else { //If the user did not provide a value...
//                //The user pressed either the "X" (exit) button or "Cancel" button.
//                break; //Abort the creation
//            }
//        
//        } //End of "while( true ) { //Forever..."
//        
//    
//        if( newCommand != null ) { //If a new Command was created...
//            
//            newCommand.setId( IdGenerator.next() );
//            newCommand.setAttribute( Command.ATTRIBUTE_Name, commandName );
//            
//        }
//        
//        return newCommand;
//    }
//    
//    // ==========================================================================
//    /**
//     *  Get the path of the parent for saving to disk
//     * 
//     * @return 
//     */
//    @Override
//    public HostController getHostController() {
//        return theListener.getParentController(this);
//    }
//
//    //===============================================================
//    /**
//     * Returns a RemoteTask object derived from the managed object
//     *  
//     * @param passedHost
//     * @return
//    */
//    @Override
//    public RemoteTask generateRemoteTask( Host passedHost ) {
//           
//        String theOsName = passedHost.getOsName();
//        String[] theCmdList = theCommand.getCommandArgs( theOsName );
//
//        //Set the remote task information
//        RemoteTask aRemoteTask = new RemoteTask( theCommand.getName(), passedHost, Command.class.getSimpleName(), theCmdList, Command.ICON_STR );
//        int taskId = SocketUtilities.getNextId();
//        aRemoteTask.setTaskId(Integer.toString( taskId ));
//        aRemoteTask.setState( RemoteTask.TASK_INIT);
//
//        aRemoteTask.setClientId(passedHost.getId());
//        aRemoteTask.setTarget(passedHost);
//
//        //Add support files and add to the task list
//        aRemoteTask.setFileContentRefMap( theCommand.getFileContentRefMap() );
//        setRemoteTaskId( taskId );
//        
//        return aRemoteTask;
//        
//    }
//
//    //===========================================================================
//    /**
//     *  Return the listener
//     * 
//     * @return 
//     */
//    @Override
//    public LibraryItemControllerListener getLibraryItemControllerListener() {
//        return theListener;
//    }
//   
//
//}/* END CLASS CommandController */
