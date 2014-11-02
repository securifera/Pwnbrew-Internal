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
 * HostController.java
 *
 * Created on June 17, 2013, 11:46 PM
 */

package pwnbrew.host;

import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import pwnbrew.controllers.MainGuiController;
import pwnbrew.gui.dialogs.TasksJDialog;
import pwnbrew.gui.panels.RunnerPane;
import pwnbrew.host.gui.DirExpansionListener;
import pwnbrew.host.gui.FileNode;
import pwnbrew.host.gui.FileTreePanel;
import pwnbrew.host.gui.HostDetailsPanel;
import pwnbrew.host.gui.HostSchedulerPanel;
import pwnbrew.host.gui.HostSchedulerPanelListener;
import pwnbrew.host.gui.HostShellPanelListener;
import pwnbrew.host.gui.HostTabPanel;
import pwnbrew.host.gui.IconData;
import pwnbrew.host.gui.RemoteFile;
import pwnbrew.host.gui.RemoteFileSystemTask;
import pwnbrew.library.LibraryItemController;
import pwnbrew.library.LibraryItemControllerListener;
import pwnbrew.logging.Log;
import pwnbrew.logging.LoggableException;
import pwnbrew.manager.DataManager;
import pwnbrew.manager.PortManager;
import pwnbrew.misc.Constants;
import pwnbrew.misc.Directories;
import pwnbrew.network.ControlOption;
import pwnbrew.network.control.messages.FileOperation;
import pwnbrew.network.control.messages.PushFile;
import pwnbrew.network.control.messages.Sleep;
import pwnbrew.network.control.messages.TaskGetFile;
import pwnbrew.shell.Bash;
import pwnbrew.shell.CommandPrompt;
import pwnbrew.shell.Powershell;
import pwnbrew.shell.Shell;
import pwnbrew.shell.ShellListener;
import pwnbrew.tasks.RemoteTask;
import pwnbrew.utilities.FileUtilities;
import pwnbrew.utilities.Utilities;
import pwnbrew.xmlBase.JarItem;

/**
 *
 *  
 */
public final class HostController extends LibraryItemController implements ActionListener, 
        HostSchedulerPanelListener, HostShellPanelListener, ShellListener {

    private Host theHost;
    private HostTabPanel theTabPanel = null;
    private final MainGuiController theMainGuiController;
    
    //The task map
    private final Map<Integer, RemoteFileSystemTask> theTaskMap = new HashMap<>();
    private Shell theShell = null;
    private String hostPathSeparator;    
  
    
    // ==========================================================================
    /**
     *  Constructor 
     * 
     * @param passedHost 
     * @param passedController 
     * @param passedBool 
    */
    public HostController( Host passedHost, MainGuiController passedController, boolean passedBool ) {
        super(passedBool);
        setObject(passedHost);
        theMainGuiController = passedController;
    }
    
    //===========================================================================
    /**
     *  Return the listener
     * 
     * @return 
     */
    @Override
    public LibraryItemControllerListener getLibraryItemControllerListener() {
        return theMainGuiController;
    }
    
    // ========================================================================
    /**
     *  Returns the scripting language managed by the controller.
     * @return 
     */
    @Override
    public Host getObject() {
        return theHost;
    }    
    
    // ========================================================================
    /**
     *  Returns the icon for the library item.
     * @return 
     */
    @Override
    public Icon getIcon() {
        return theHost.getIcon();
    }
    
    // ========================================================================
    /**
     *  Returns whether this host controller represents the local host
     * 
     * @return 
    */     
    @Override
    public boolean isLocalHost(){    
        return theHost.getHostname().equals( HostFactory.LOCALHOST);        
    }
 
    // ========================================================================
    /**
     *  Sets the Object managed by the controller.
     * @param passedObj
     */
    @Override
    public void setObject(Object passedObj) {
        if( passedObj != null ){
            theHost = (Host) passedObj;
                        
            if( Utilities.isWindows( theHost.getOsName() ) ){
                hostPathSeparator = "\\";
            } else {
                hostPathSeparator = "/";
            }
        }
    }
    
    // ==========================================================================
    /**
     * Returns the root panel
     *
     * @return 
    */
    @Override
    public HostTabPanel getRootPanel() {
        if( !headless && theTabPanel == null ){ 
            createTabPanel(); 
        }
        return theTabPanel;
    }
    
    
    // ==========================================================================
    /**
     * Creates the {@link HostTabPanel} for the {@link Host}.
     *
    */
    private void createTabPanel() {
        theTabPanel = new HostTabPanel( this );
    }/* END createTabPanel() */

    // ==========================================================================
    /**
     * Update the components that this controller manages
     *
    */
    @Override
    public void updateComponents() {
        
        HostTabPanel aPanel = getRootPanel();
        if( aPanel != null ){
            aPanel.setLibraryItemName( theHost.getHostname() );
            refreshOverviewPanel(); 

            //Update the Shell Pane
            int i = 1;

            JTabbedPane tabPane = aPanel.getTabCollection();
            String title = tabPane.getTitleAt( i );
            if( !title.equals( " Shell ")){
                i++;
                if( tabPane.getTabCount() > i ){
                    title = tabPane.getTitleAt( i);
                    if( !title.equals( " Shell ")){
                        return;
                    }
                }
            }

            //Set the visibility of the panel
            tabPane.setEnabledAt(i, isConnected());
        }
        
    }
    
    // ========================================================================
    /**
     * Returns the name of the {@link Host}.
     * 
     * @return the name of the {@code Host}; null if the {@code Host} is not set
     */
    @Override 
    public String getItemName() {
        
        String rtnString = null;
        if( theHost != null )
            rtnString = theHost.getHostname();
        
        return rtnString;
        
    }/* END getItemName() */
    
    
    // ==========================================================================
    /**
     *  Returns the object library.
     * @return 
     */
    @Override
    public File getObjectLibraryDirectory() {
        
        File objDir;
        if( isLocalHost() ){
            objDir = new File( Directories.getLocalObjectLibraryPath() );
        } else {
            objDir = new File( Directories.getObjectLibraryDirectory(), "r" + theHost.getId() );
        }
        return objDir;
    }
    
    
    // ==========================================================================
    /**
     * Returns the string representation of the scripting language.
     * 
     * @return 
    */
    @Override
    public String toString(){
        return theHost.getHostname();
    }

    
    // ========================================================================
    /**
     * Returns the name of the {@link ScriptingLanguage} type for use in messages
     * to the user.
     * @return 
     */
    @Override
    public String getItemTypeDisplayName() {
        return "Host";
    }

    // ==========================================================================
    /**
     *  Returns the popup for the host.
     * 
     * @param multi
     * @return 
     */
    @Override
    public JPopupMenu getPopupJMenu( boolean multi ) {
        
        JPopupMenu popup = new JPopupMenu();
        JMenuItem menuItem;

        if( isLocalHost() ){

            menuItem = new JMenuItem( Constants.IMPORT_BUNDLE );
            menuItem.setActionCommand( Constants.IMPORT_BUNDLE );
            menuItem.addActionListener( theMainGuiController );
            menuItem.setEnabled( true );
            popup.add( menuItem );
            
        } else {  
        
            if( theHost.isConnected() ){
                
                if( !theHost.getCheckInList().isEmpty() ){
                
                    menuItem = new JMenuItem( Constants.ACTION_SLEEP );
                    menuItem.setActionCommand( Constants.ACTION_SLEEP );
                    menuItem.addActionListener( theMainGuiController );
                    menuItem.setEnabled( true );
                    popup.add(menuItem);
                    
                    popup.addSeparator();
                
                }
                
                menuItem = new JMenuItem( Constants.ACTION_MIGRATE );
                menuItem.setActionCommand( Constants.ACTION_MIGRATE );
                menuItem.addActionListener(theMainGuiController);
                menuItem.setEnabled( true );
                popup.add(menuItem);

                popup.addSeparator();
                
                menuItem = new JMenuItem( Constants.ACTION_RELOAD );
                menuItem.setActionCommand( Constants.ACTION_RELOAD );
                menuItem.addActionListener(theMainGuiController);
                menuItem.setEnabled( true );
                popup.add(menuItem);

                menuItem = new JMenuItem( Constants.ACTION_UNINSTALL );
                menuItem.setActionCommand( Constants.ACTION_UNINSTALL );
                menuItem.addActionListener(theMainGuiController);
                menuItem.setEnabled( true );
                popup.add(menuItem);
            
            } else {
                
                //Add the ability to delete it
                menuItem = new JMenuItem( Constants.ACTION_REMOVE );
                menuItem.setActionCommand( Constants.ACTION_REMOVE );
                menuItem.addActionListener(theMainGuiController);
                menuItem.setEnabled( true );
                popup.add(menuItem);
                
            }
            
            
            //See if the JAR needs upgrading
            String theJarVersion = theHost.getJarVersion();
            String theJreVersion = theHost.getJreVersion();     
            if( theJreVersion.length() > 2 ){
                char theChar = theJreVersion.charAt(2);

                //Get the current library for that jre version
                JarItem aJarItem = Utilities.getStagerJarItem( String.valueOf(theChar) );
                if( aJarItem != null ){
                    String stagerJarVersion = aJarItem.getVersion();
                    if( stagerJarVersion.compareTo( theJarVersion ) > 0){
                        
                        popup.addSeparator();
                        //Add upgrade
                        menuItem = new JMenuItem( Constants.ACTION_UPGRADE );
                        menuItem.setActionCommand( Constants.ACTION_UPGRADE );
                        menuItem.addActionListener(theMainGuiController);
                        menuItem.setEnabled( true );
                        popup.add(menuItem);
                        
                        popup.addSeparator();
                    }
                }
            }
            
             //If a relay isn't setup
            String action;
            if( theHost.getRelayPort().isEmpty() ){
                action = Constants.ACTION_CREATE_RELAY;
            } else {
                action = Constants.ACTION_REMOVE_RELAY;
            }        

            menuItem = new JMenuItem( action );
            menuItem.setActionCommand( action );
            menuItem.addActionListener( theMainGuiController );
            menuItem.setEnabled( true );
            popup.add(menuItem);
        
        }        
        
        //Add separator
        if( !multi ){
            
//            popup.addSeparator();
//
//            //Loop through the controller types and add their creation strings
//            //to the right click menu
//            Iterator<String> anIter = theMainGuiController.getActionClassMap().keySet().iterator();
//            while( anIter.hasNext() ){        
//
//                String addString = anIter.next();
//                menuItem = new JMenuItem( addString );
//                menuItem.setActionCommand( addString );
//                menuItem.addActionListener( theMainGuiController );
//                menuItem.setEnabled( true );
//                popup.add( menuItem );   
//
//            }
        }
        
        return popup;        
    }

    @Override
    public String getCreationAction() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object createItem(LibraryItemControllerListener passedListener) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public LibraryItemController copy() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    //=========================================================================
    /**
     *  Refreshes the overview panel
     */
    public void refreshOverviewPanel() {
        
        //Set the next check in time
        HostTabPanel aPanel = getRootPanel();
        if( aPanel != null ){
            HostDetailsPanel thePanel = aPanel.getOverviewPanel();
            thePanel.populateComponents();   
        }         
    }
    
    // ==========================================================================
    /**
     *  Returns the check-in time list
     * 
     * @return 
     */
    @Override
    public List<String> getCheckInDateList() {
        return theHost.getCheckInList();
    }
    
    // ==========================================================================
    /**
     * Removes the date from the Host
     * 
     * @param passedDate 
     */
    @Override
    public void addCheckInDate(String passedDate) {
        
        List<String> dateList = theHost.getCheckInList();
        theHost.addCheckInTime(passedDate);
        
        theMainGuiController.valuesChanged(this);
        
        //Set the next check in time
        if( dateList == null || dateList.isEmpty() ){
            refreshOverviewPanel();       
        }
        
        //Enable the checkbox
        HostTabPanel aPanel = getRootPanel();
        if( aPanel != null ){
            HostSchedulerPanel thePanel = aPanel.getSchedulerPanel();
            thePanel.setAutoSleepCheckboxEnablement( true );
        }
    }
    
    //=========================================================================
    /**
     *  Replaces one date with the other
     * @param oldDate
     * @param newDate
     */
    @Override
    public void replaceDate(String oldDate, String newDate){
       
        //Replace and refresh
        List<String> theCheckInList = theHost.getCheckInList();
        theCheckInList.remove(oldDate);
        
        //Add the new one and sort it
        theCheckInList.add(newDate);
        Collections.sort(theCheckInList);
        
        //Set the list
        theHost.setCheckInList( theCheckInList );
        
        refreshOverviewPanel(); 
        
        //Notify the controller
        theMainGuiController.valuesChanged(this);
        
    }
      
    // ==========================================================================
    /**
     * Removes the next check in.
     * 
     * @param passedDate 
     */
    private void removeCheckInDate( String passedStr ) {
        
        //Remove the first checkin date        
        List<String> strList = new ArrayList<>();
        strList.add( passedStr );
        removeCheckInDates( strList );
        
    }
    
    // ==========================================================================
    /**
     * Removes the date from the Host
     * 
     * @param passedDateList 
     */
    @Override
    public void removeCheckInDates( List<String> passedDateList ) {
        
        HostSchedulerPanel schedulePanel = null;
        HostTabPanel aPanel = getRootPanel();
        if( aPanel != null )
            schedulePanel = getRootPanel().getSchedulerPanel();
        
        for( String aDateStr : passedDateList ){
            theHost.removeCheckInTime( aDateStr );
            if( schedulePanel != null )
                schedulePanel.removeCheckInDate( aDateStr );
        }

        //Set the checkbox enablement
        if( theHost.getCheckInList().isEmpty() ){
            theHost.setAutoSleepFlag(false);
            if( schedulePanel != null )
                schedulePanel.setAutoSleepCheckboxEnablement( false );
        }
        
        //Set the dirty flag
        theMainGuiController.valuesChanged(this);
        
        refreshOverviewPanel();  
        
    }

    // ==========================================================================
    /**
     *  Gets the action listener for the tab panel
     * @return 
     */
    @Override
    public ActionListener getActionListener() {
        return this;
    }
    
     //===================================================================
    /**
    * Deletes the given {@link XmlBase} from the library.
    * 
    * <p>
    * If the given {@code XmlBase} is null, this method does nothing.
    *
    */
    @Override
    public void deleteFromLibrary() {

        super.deleteFromLibrary();
        try {
            FileUtilities.deleteDir( getObjectLibraryDirectory() );
        } catch (IOException ex) {
            Log.log(Level.WARNING, NAME_Class, "deleteFromLibrary()", ex.getMessage(), ex );        
        }
        
   }

    //===================================================================
    /**
     *  Sets the auto sleep flag in the host
     * 
     * @param selected 
     */
    @Override
    public void setAutoSleepFlag(boolean selected) {
        
        theHost.setAutoSleepFlag( selected );
        theMainGuiController.valuesChanged( this );
        
    }
    
    //===================================================================
    /**
     *  Returns the auto sleep flag
     * 
     * @return 
     */
    @Override
    public boolean getAutoSleepFlag(){
        return theHost.getAutoSleepFlag();
    } 
    
     // ==========================================================================
    /**
     * Saves the given {@link XmlBase} to the Object Library directory.
     * 
    */
    @Override
    public void saveToDisk() {
        
        try {
            
            String clientIdStr = getId();
           
            //Make sure it save correctly
            theHost.writeSelfToDisk(getObjectLibraryDirectory(), clientIdStr);
            setIsDirty( false );            
            
        } catch (IOException ex) {
            Log.log(Level.WARNING, NAME_Class, "saveToDisk()", ex.getMessage(), ex );          
        }
    }

    // ==========================================================================
    /**
     *  Gets the first sleep date and sends a message to the client to goto sleep.
     * 
     * @param parent
     * @param autoFlag
    */
    public void sleep( JFrame parent, boolean autoFlag ) {
        
        //Purge stale dates
        removeStaleDates();
        
        int rtnCode = JOptionPane.YES_OPTION;
        if( !autoFlag && parent != null ){
            
            String messageBuilder = "Are you sure you want to put \"" + theHost.getHostname() + "\" to sleep?";
            //Prompt user to confirm the delete
            rtnCode = JOptionPane.showConfirmDialog( parent, messageBuilder,
                        "Sleep",
                        JOptionPane.YES_NO_OPTION );
            
        }

        //Get the last-selected node
        if( rtnCode == JOptionPane.YES_OPTION ) { //If the delete is confirmed...

            try {
                //Get the sleep time
                List<String> theCheckInList = theHost.getCheckInList();
                if( !theCheckInList.isEmpty() ){

                    //Get the first time
                    String theCheckInTime = theCheckInList.get(0);
               
                    //Send sleep message
                    int dstHostId = Integer.parseInt( theHost.getId());
                    Sleep sleepMsg = new Sleep( dstHostId, theCheckInTime ); //Convert mins to seconds
                    DataManager.send( theMainGuiController.getServer().getServerManager(), sleepMsg );

                }

            } catch( UnsupportedEncodingException ex ){
                Log.log(Level.WARNING, NAME_Class, "actionPerformed()", ex.getMessage(), ex );
            }
        }
        
    }

    // ==========================================================================
    /**
     *  Remove any dates that are before the current time
     */
    public void removeStaleDates() {
        
        //Create a calendar object
        Calendar currCalendar = Calendar.getInstance();
        currCalendar.setTime( new Date() );

        //Loop on the next check in
        Calendar nextCalendar = Calendar.getInstance();
        while( true ){

            String nextCheckInStr = theHost.getNextCheckInTime();
            if( nextCheckInStr != null ){
                try {

                    //Parse the next date
                    Date nextDate = Constants.CHECKIN_DATE_FORMAT.parse( nextCheckInStr );
                    nextCalendar.setTime(nextDate);                       

                    //Keep pulling dates until one is found that is after
                    if( nextCalendar.before(currCalendar) )
                        removeCheckInDate( nextCheckInStr );
                    else
                        break;

                } catch (ParseException ex) {
                    ex = null;
                }

            } else
                break;            
        } 
    }
    
    // ==========================================================================
    /**
     *  Returns the id of the host controller to save to
     * 
     * @return 
     */
    public String getId() {
        
        String retStr;
        if( isLocalHost()){
            retStr = HostFactory.LOCALHOST;
        } else {
            retStr = theHost.getId();
        }
        
        return retStr;
    }

    // ==========================================================================
    /**
     *  Returns the id of the host controller to save to
     * 
     */
    public void closeShell() {
        
        if( theShell != null ){
            theShell.shutdown();
        }
                    
    }
    
     // ==========================================================================
    /**
     *  Returns the id of the host controller to save to
     * 
     * @param passedClass 
     */
    @Override
    public void spawnShell( Class passedClass ) {
        
        //Get the shell
        if( passedClass != null ){
            
            try {
                Constructor aConstructor = passedClass.getConstructor( Executor.class, ShellListener.class );
                theShell = (Shell)aConstructor.newInstance( Constants.Executor, this);
                    
                //Start the shell
                theShell.start();
             
            } catch (  NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                Log.log(Level.WARNING, NAME_Class, "spawnShell()", ex.getMessage(), ex );
            }
            
        }
    }
    
    //==========================================================================
    /**
     *  Kill the shell
     */
    @Override
    public void killShell(){        
            
        if( theShell != null ){
                //&& theShell.isRunning() ){
            theShell.shutdown();
            theShell = null;
        }        
    }
    
    //==========================================================================
    /**
     *  Get the list of available shells
     * 
     * @return 
     */
    @Override
    public List<Class> getShellList(){
        
        List<Class> theShellList = new ArrayList<>();
        
        String osName = theHost.getOsName();
        if( Utilities.isWindows( osName) ){
            //Add the cmd shell
            theShellList.add( CommandPrompt.class );

            //Add powershell
            theShellList.add(Powershell.class);
            
        } else if( Utilities.isUnix(osName)){
            
            //Add bash
            theShellList.add(Bash.class);   
        }
        
        return theShellList;
    }

     // ==========================================================================
    /**
     *  Sends the input string to the shell
     * 
     * @param theStr 
     */
    @Override
    public void sendInput(String theStr) {            
        if( theShell != null ){
            theShell.sendInput(theStr);
        }
    }
    
     //===============================================================
    /**
     * Returns the runner text pane
     *
     * @return 
    */
    @Override
    public RunnerPane getShellTextPane() {
        
        RunnerPane aRunnerPane = null;
        HostTabPanel aPanel = getRootPanel();
        if( aPanel != null )
            aRunnerPane = aPanel.getShellPanel().getShellTextPane();
                    
        return  aRunnerPane;
    }

    //===============================================================
    /**
     * Returns the shell log dir.
     *
     * @return 
    */
    @Override
    public File getShellLogDir() {
        
        File parentDir;
        if( isLocalHost() ){                
            parentDir = Directories.getLocalTasksDirectory();
        } else {
            parentDir = new File( Directories.getRemoteTasksDirectory(), theHost.getHostname());
        }
        
        //Add the shell dir
        File shellDir = new File( parentDir, "shell");
        shellDir.mkdirs(); 
        
        return shellDir;
    }

    //===============================================================
    /**
     *  If the host is connected then return true.
     * 
     * @return 
     */
    public boolean isConnected() {        
        return theHost.isConnected();
    }

    //===============================================================
    /**
     * 
     * @param theObjList 
     * @param passedDir 
     */
    public void uploadFiles( List<File> theObjList, String passedDir ) {
        
        if( theObjList != null ) { //If the user selected any files...

            int clientId = Integer.parseInt( theHost.getId() );
            for( Object anObj : theObjList) { //For each file path...

                if(anObj instanceof File){
                    File aFile = (File)anObj;
                    if( FileUtilities.verifyCanRead( aFile ) ) { //If the file the File represents can be read...

                        try {

                            int taskId = Utilities.SecureRandomGen.nextInt();
                            String[] theCmdList = new String[]{};

                            //Set the remote task information
                            RemoteTask aRemoteTask = new RemoteTask( "File Upload", theHost, Constants.FILE_UPLOAD, theCmdList, Constants.UPLOAD_IMG_STR );
                            aRemoteTask.setTaskId(Integer.toString( taskId ));
                            aRemoteTask.setState( RemoteTask.TASK_XFER_FILES);

                            aRemoteTask.setClientId(theHost.getId());
                            aRemoteTask.setTarget(theHost);

                            //Add support files and add to the task list
                            theMainGuiController.addTask(aRemoteTask, true);

                            TasksJDialog theTasksDialog = TasksJDialog.getTasksJDialog();
                            theTasksDialog.setVisible(true);

                            //Queue the file to be sent
                            String fileHashNameStr = new StringBuilder().append("0").append(":").append(aFile.getAbsolutePath()).toString();
                            PushFile thePFM = new PushFile( taskId, fileHashNameStr, aFile.length(), PushFile.FILE_UPLOAD, clientId );

                            //Add the directory
                            byte[] tempArr = passedDir.getBytes("US-ASCII");
                            ControlOption aTlv = new ControlOption( PushFile.OPTION_REMOTE_DIR, tempArr);
                            thePFM.addOption(aTlv);

                            //Send the message
                            DataManager.send( theMainGuiController.getServer().getServerManager(), thePFM );  


                        } catch ( LoggableException | IOException ex) {
                            JOptionPane.showMessageDialog( theMainGuiController.getParentJFrame(), ex.getMessage(), "Could not upload the file(s).", JOptionPane.ERROR_MESSAGE );
                        }

                    } else { //If the file cannot be read...
                       JOptionPane.showMessageDialog( theMainGuiController.getParentJFrame(), new StringBuilder( "\tThe file(s) could not be read: \"" )
                                .append( aFile.getAbsolutePath() ).append( "\"" ).toString(), "Could not upload the file(s).", JOptionPane.ERROR_MESSAGE );
                    }
                }

            }
        
        }
        
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
        synchronized(theTaskMap){
            theTask = theTaskMap.get(taskId);
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
                DefaultMutableTreeNode parent = theTask.getParentNode();
                parent.removeAllChildren();  // Remove Flag

                List nodeList = theTask.getFileList();
                Collections.sort(nodeList);

                //Add the nodes to the parent
                for (int i=0; i<nodeList.size(); i++){

                    FileNode currentNode = (FileNode)nodeList.get(i);
                    IconData theIconData;
                    if( currentNode.isDirectory()){                        
                        theIconData = new IconData( currentNode.getIcon(), null, FileNode.OPEN_FOLDER_ICON, currentNode);
                    } else {                            
                        theIconData = new IconData( currentNode.getIcon(), null, currentNode);
                    }

                    DefaultMutableTreeNode node = new DefaultMutableTreeNode(theIconData);
                    parent.add(node);

                    //Set the child count
                    if( (currentNode.isDirectory() || currentNode.isDrive()) && currentNode.getSize() > 0)
                        node.add(new DefaultMutableTreeNode( true));                            

                }

                //Get the jtree panel
                HostTabPanel aPanel = getRootPanel();
                if( aPanel != null ){
                    HostDetailsPanel thePanel = aPanel.getOverviewPanel();
                    final FileTreePanel theFileTreePanel = thePanel.getFileTreePanel();

                    //Run in swing thread
                    final HostController thisController = this;
                    final TreePath aTreePath = new TreePath(parent.getPath());
                    SwingUtilities.invokeLater( new Runnable(){

                        @Override
                        public void run() {

                            //Reload the model
                            theFileTreePanel.getTreeModel().reload(); 

                            //Remote any listeners
                            JTree theJTree = theFileTreePanel.getJTree();
                            for( TreeExpansionListener aListener : theJTree.getTreeExpansionListeners()){
                                if( aListener instanceof DirExpansionListener ){
                                    theJTree.removeTreeExpansionListener( aListener );
                                }
                            }

                            //Expand the last path
                            theFileTreePanel.getJTree().expandPath(aTreePath);

                            //Add the listener back
                            theJTree.addTreeExpansionListener( new DirExpansionListener( thisController) );
                        }               
                    });
                }

                //Remote the task from the map
                synchronized(theTaskMap){
                    theTaskMap.remove(taskId);
                }

            }
            
        }
    }

    //=========================================================================
    /**
     *  Adds a task to the map
     * @param aRFST 
     */
    public void addRemoteFileSystemTask(RemoteFileSystemTask aRFST) {
        synchronized(theTaskMap){
            theTaskMap.put(aRFST.getTaskId(), aRFST);
        }
    }

    //=========================================================================
    /**
     * Updates the file panel with the passed file node information.
     * @param aNode 
     */
    public void updateFileDetailsPanel(FileNode aNode) {
        //Get the jtree panel
        HostTabPanel aPanel = getRootPanel();
        if( aPanel != null ){
            HostDetailsPanel detailsPanel = aPanel.getOverviewPanel();
            detailsPanel.updateFilePanel(aNode);
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
        synchronized(theTaskMap){
            theTask = theTaskMap.get(taskId);
        }
        
        if( theTask != null )
            theTask.setFileCount(theDirCount);
        
    }

    //=========================================================================
    /**
     * Download the file list
     * 
     * @param theRemoteFiles 
     */
    public void downloadFiles(List<RemoteFile> theRemoteFiles) {
        
        int clientId = Integer.parseInt( theHost.getId() );
        for( RemoteFile aFile : theRemoteFiles) { //For each file path...

            try {

                int taskId = Utilities.SecureRandomGen.nextInt();
                String[] theCmdList = new String[]{};

                //Set the remote task information
                RemoteTask aRemoteTask = new RemoteTask( "File Download", theHost, Constants.FILE_DOWNLOAD, theCmdList, Constants.DOWNLOAD_IMG_STR );
                aRemoteTask.setTaskId(Integer.toString( taskId ));
                aRemoteTask.setState( RemoteTask.TASK_XFER_FILES);

                aRemoteTask.setClientId(theHost.getId());
                aRemoteTask.setTarget(theHost);

                //Add support files and add to the task list
                theMainGuiController.addTask(aRemoteTask, true);

                TasksJDialog theTasksDialog = TasksJDialog.getTasksJDialog();
                theTasksDialog.setVisible(true);

                //Queue the file to be sent
                String fileHashNameStr = new StringBuilder().append("0").append(":").append(aFile.getAbsolutePath()).toString();
                TaskGetFile theTaskMsg = new TaskGetFile( taskId, fileHashNameStr, clientId );

                //Send the message
                DataManager.send( theMainGuiController.getServer().getServerManager(), theTaskMsg );  

            } catch ( LoggableException | IOException ex) {
                JOptionPane.showMessageDialog( theMainGuiController.getParentJFrame(), ex.getMessage(), "Could not upload the file(s).", JOptionPane.ERROR_MESSAGE );
            }       

        }
        
    }

    //=========================================================================
    /**
     * 
     */
    public void refreshFileSystemJTree() {
        
        HostTabPanel aPanel = getRootPanel();
        if( aPanel != null ){
            HostDetailsPanel thePanel = aPanel.getOverviewPanel();
            final FileTreePanel theFileTreePanel = thePanel.getFileTreePanel();

            //Run in swing thread
            SwingUtilities.invokeLater( new Runnable(){

                @Override
                public void run() {

                    //Remote any listeners
                    JTree theJTree = theFileTreePanel.getJTree();
                    TreePath aTreePath = theJTree.getSelectionPath().getParentPath();

                    theJTree.collapsePath(aTreePath);
                    theJTree.expandPath(aTreePath);
                }
            });
        }
      
    }   

    //=========================================================================
    /**
     * 
     * @param passedOp
     * @param filePath 
     * @param addParam 
     */
    public void performFileOperation(byte passedOp, String filePath, String addParam ) {
        
        //Get the control message manager
        int hostId = Integer.parseInt( getId() );        
        try {
            
            FileOperation aFileOp = new FileOperation( hostId, passedOp, filePath, addParam );
            DataManager.send( theMainGuiController.getServer().getServerManager(), aFileOp);
            
        } catch (UnsupportedEncodingException ex) {
            Log.log(Level.WARNING, NAME_Class, "performFileOperation()", ex.getMessage(), ex );        
        }
        
    }

    //========================================================================
    /**
     *  Get the host os name.
     * @return 
     */
    @Override
    public String getOsName() {
        return theHost.getOsName();
    }

    //========================================================================
    /**
     * 
     * @return 
     */
    @Override
    public Shell getShell() {
        return theShell;
    }

    //========================================================================
    /**
     * 
     * @return 
     */
    @Override
    public Host getHost() {
        return getObject();
    }

    //========================================================================
    /**
     * Returns the comm manager
     * @return 
     */
    @Override
    public PortManager getCommManager() {
        return theMainGuiController.getServer().getServerManager();
    }

}
