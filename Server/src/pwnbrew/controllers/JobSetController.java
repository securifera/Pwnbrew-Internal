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
 * JobSetController.java
 *
 * Created on Jun 23, 2013, 11:31:41 PM
 */

package pwnbrew.controllers;

import pwnbrew.library.LibraryItemController;
import pwnbrew.library.LibraryItemControllerListener;
import pwnbrew.library.Ancestor;
import pwnbrew.logging.Log;
import pwnbrew.exception.XmlBaseCreationException;
import pwnbrew.xmlBase.XmlBaseFactory;
import pwnbrew.xmlBase.job.JobRef;
import pwnbrew.xmlBase.job.JobSet;
import java.awt.Image;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.swing.*;
import pwnbrew.gui.MainGui;
import pwnbrew.gui.Iconable;
import pwnbrew.gui.panels.jobset.JobSetPanel;
import pwnbrew.gui.panels.jobset.JobSetPanelListener;
import pwnbrew.host.HostController;
import pwnbrew.host.HostFactory;
import pwnbrew.misc.Constants;
import pwnbrew.misc.IdGenerator;
import pwnbrew.utilities.Utilities;


/**
 * 
 */
public class JobSetController extends RunnableItemController implements Ancestor, JobSetPanelListener, RunObserver {
    
    private static final String ACTION_Create = "Create Job Set";
    
    public static final Icon ICON = new ImageIcon(
            Utilities.loadImageFromJar( Constants.JOB_SET_IMAGE_STR )
            .getScaledInstance( Iconable.ICON_WIDTH, Iconable.ICON_HEIGHT, Image.SCALE_SMOOTH ) );
    
    private JobSetControllerListener theListener;
    private JobSetPanel theJobSetPanel = null;
    private JobSet theJobSet;
    
    private final JPopupMenu jobSetPopupMenu = new JPopupMenu();
    
    private final List<JobController> theJobControllerList = new ArrayList<>();
    
    private boolean isRunningFlag = false;
    private final List<JobController> theRunningJobControllerList = new ArrayList<>();
    private int theRunningJobIndex = 0;
    private boolean errorOccurredFlag = false;
    
    private ProgressObserver jobSetObserver = null;
   
    
    //===============================================================
    /**
     *  Constructor 
     *  ( Necessary for the right click menu creation string )
    */
    public JobSetController() {
        theIcon = ICON; 
    }  
    
    // ========================================================================
    /**
     * Creates a new instance of {@link JobSetController}.
     */
    private JobSetController(JobSet passedJobSet) {
        this();
        if( passedJobSet == null )
            throw new IllegalArgumentException( "The job set cannot be null." );
        theJobSet = passedJobSet;       
    }
   
    
    // ========================================================================
    /**
     * Creates a new instance of {@link JobSetController}.
     * @param passedJobSet
     * @param passedListener
     */
    public JobSetController(JobSet passedJobSet, LibraryItemControllerListener passedListener ) {
        this(passedJobSet);
        theListener = (JobSetControllerListener)passedListener;
    }/* END CONSTRUCTOR() */
    
    //===========================================================================
    /**
     *  Return the listener
     * 
     * @return 
     */
    @Override
    public JobSetControllerListener getLibraryItemControllerListener() {
        return theListener;
    }
    
    // ========================================================================
    /**
     * Sets the {@link JobSet}.
     * <p>
     * If the argument is null, the values from the currently set {@code JobSet}
     * (if there is one) will be cleared from the {@link JobSetController} and its
     * {@link JobSetPanel}.
     * 
     * @param jobSet the {@code JobSet}
     */
    public void setJobSet( JobSet jobSet ) {
        
        theJobSet = jobSet;
        if( theJobSetPanel == null ) {
            theJobSetPanel = new JobSetPanel( this );
        }
        
        theJobSetPanel.populateComponents( this );
        
    }/* END setJobSet() */

    // ========================================================================
    /**
     * Returns the name of the {@link JobSet}.
     * 
     * @return the name of the {@code JobSet}; null if the {@code JobSet} is not set
     */
    @Override //LibraryItemController
    public String getItemName() {
        
        String rtnString = null;
        if( theJobSet != null )
            rtnString = theJobSet.getName();
        
        return rtnString;
        
    }/* END getItemName() */

    
    // ========================================================================
    /**
     * Runs the {@link JobSet}.
     * <p>
     * If the {@code JobSet} is already running this method does nothing.
     * @param observer
     */
    @Override //RunnableItemController
    public void runItem( RunObserver observer ) {
        
        if( isRunningFlag == false ) { //If the JobSet is not already running...
            
            jobSetObserver = (ProgressObserver) observer;
            isRunningFlag = true;
            theJobSetPanel.setOptionCheckBoxesEnabled( false ); //Disable the option check boxes
            
            synchronized( theRunningJobControllerList ) {
                
                theRunningJobControllerList.clear();
                
                if( theJobSet.runsConcurrently() ) {
                    //Run the Jobs together.
                    
                    //Run all of the Jobs...
                    for( JobController jobController : theJobControllerList ) {
                        theRunningJobControllerList.add( jobController );
                        jobController.runItem( this );
                    }
                
                } else {
                    //Run the Jobs in sequence.
                    
                    theRunningJobIndex = 0;
                    errorOccurredFlag = false;
                    
                    //Run the first Job...
                    theRunningJobControllerList.add( theJobControllerList.get( theRunningJobIndex ) );
                    theRunningJobControllerList.get( theRunningJobIndex ).runItem( this );

                }
                
            } //End of "synchronized( theRunningJobControllerList ) {"
            
        }
        
    }/* END runItem() */

    
    // ========================================================================
    /**
     * 
     */
    private void runHasCompleted() {
        
        isRunningFlag = false;
        
//        if( theJobSet.rebootsAfter() ) {
//            Utilities.reboot();
//        }
        
        if(jobSetObserver != null){
            jobSetObserver.runCompleted(this);
            jobSetObserver = null;
        }
        
        theJobSetPanel.setOptionCheckBoxesEnabled( true ); //Enable the option check boxes
        
    }/* END runHasCompleted() */
    
    
    // ========================================================================
    /**
     * 
     */
    @Override //RunnableItemController
    public void cancelRun() {
        
        //Stop the execution for any running Jobs
        
        synchronized( theRunningJobControllerList ) {
            
            for( JobController controller : theRunningJobControllerList ) {
                controller.cancelRun();
            }
            
        }
        
        isRunningFlag = false;
        
    }/* END cancelRun() */

    
    // ========================================================================
    /**
     * 
     * @return 
     */
    @Override //RunnableItemController
    public boolean isRunning() {
        return isRunningFlag;
    }/* END isRunning() */

    
    // ========================================================================
    /**
     * Determines if the {@link JobSet} can run.
     * 
     * @return null if the {@code JobSet} can be run; a String describing why the
     * {@code JobSet} cannot be run
     */
    @Override
    public String canRun() {
        
        String rtnString = null;
        
        if( theJobControllerList.isEmpty() ) { //If there are no Jobs in the JobSet...
            rtnString = "There are no Jobs to run.";
        } else { //If there is at least one Job in the JobSet...
        
            List<JobController> jobsThatCannotRun = null;
            for( JobController jobController : theJobControllerList ) { //For each JobController...

                if( jobController.canRun() != null ) { //If the controller's Job cannot run...

                    //Lazily initiate the list of Jobs that can't run...
                    if( jobsThatCannotRun == null )
                        jobsThatCannotRun = new ArrayList<>();

                    jobsThatCannotRun.add( jobController );

                }

            }

            if( jobsThatCannotRun != null ) { //If at least one of the Job's cannot run...

                StringBuilder stringBuilder = new StringBuilder();

                if( jobsThatCannotRun.size() == 1 ) { //If only one Job cannot run...

                    stringBuilder.append( "The " ).append( jobsThatCannotRun.get( 0 ).getJobType() )
                            .append( " \"" ).append( jobsThatCannotRun.get( 0 ).getItemName() ).append( "\" cannot currently run." );

                } else { //If more than one Job cannot run...

                    stringBuilder.append( "These jobs cannot currently run...\n" );
                    JobController jCont;
                    for( int i = 0; i < jobsThatCannotRun.size(); i++ ) { //For each JobController...

                        jCont = jobsThatCannotRun.get( i );
                        stringBuilder.append( "     " ).append( jCont.getItemName() ).append( " (" ).append( jCont.getJobType() ).append( ")" );
                        if( i < jobsThatCannotRun.size() - 1 ) //If there is another Job...
                            stringBuilder.append( "\n" );
                            
                    }

                }

                rtnString = stringBuilder.toString();

            }
            
        } //End of "} else { //If there is at least one Job in the JobSet..."
        
        return rtnString;
        
    }/* END canRun() */
    
    
    // ========================================================================
    /**
     * Creates a message for confirming the run of the {@link JobSet}.
     * <p>
     * If the argument is null this method does nothing.
     * 
     * @param builder the {@link StringBuilder} to which the message is to be appended
     */
    @Override //RunnableItemController
    public void createRunConfirmationMessage( StringBuilder builder ) {
        
        if( builder == null ) return;
        
        builder.append( "Are you sure you want to run these Jobs?\n" );
        JobController jCont;
        for( int i = 0; i < theJobControllerList.size(); i++ ) { //For each JobController...

            jCont = theJobControllerList.get( i );
            builder.append( "     " ).append( jCont.getItemName() );
            if( i < theJobControllerList.size() - 1 ) { //If there is another parameter...
                builder.append( "\n" );
            }

        }

    }/* END createRunConfirmationMessage( StringBuilder ) */
    
    
    // ========================================================================
    /**
     * Returns the name of the {@link JobSet} type for use in messages to the
     * user.
     * @return 
     */
    @Override //LibraryItemController
    public String getItemTypeDisplayName() {
        return "Job Set";
    }/* END getItemTypeDisplayName() */
    
    
    // ========================================================================
    /**
     * 
     * @param newValue
     */
    @Override //JobSetPanelListener
    public void handleRebootOnCompletionOptionChange( boolean newValue ) {
        theJobSet.setRebootOnCompletion( newValue );
        setIsDirty( true );
        if( theListener != null )
            theListener.persistentValueChanged( this );
    }/* END handleRebootOnCompletionOptionChange( boolean ) */

    
    // ========================================================================
    /**
     * 
     * @param newValue
     */
    @Override //JobSetPanelListener
    public void handleStopOnErrorOptionChange( boolean newValue ) {
        theJobSet.setStopOnError( newValue );
        setIsDirty( true );
        if( theListener != null )
            theListener.persistentValueChanged( this );
    }/* END handleStopOnErrorOptionChange( boolean ) */

    
    // ========================================================================
    /**
     * 
     * @param newValue
     */
    @Override //JobSetPanelListener
    public void handleRunConcurrentlyOptionChange( boolean newValue ) {
        theJobSet.setRunConcurrently( newValue );
        setIsDirty( true );
        if( theListener != null )
            theListener.persistentValueChanged( this );
    }/* END handleRunConcurrentlyOptionChange( boolean ) */
    
    
    // ========================================================================
    /**
     * 
     * @param newText
     */
    @Override //JobSetPanelListener
    public void handleDescriptionChange( String newText ) {
        theJobSet.setDescription( newText );
        
        if( theListener != null )
            theListener.valuesChanged(this);
        
    }/* END handleDescriptionChange( String ) */

    
    // ========================================================================
    /**
     * 
     * @param controller
     */
    @Override //RunObserver
    public void preparingToRun( RunnableItemController controller ) {
        
        //Update the gui
        if(jobSetObserver != null){
            jobSetObserver.preparingToRun(this);
        }
        
    }/* END preparingToRun( RunnableItemController ) */
    
    
    // ========================================================================
    /**
     * 
     * @param controller
     */
    @Override //RunObserver
    public void runBeginning( RunnableItemController controller ) {
        
        //Update the gui
        if(jobSetObserver != null){
            jobSetObserver.runBeginning(this);
        }
        
    }/* END beginningRun( RunnableItemController ) */
    
    
    // ========================================================================
    /**
     * 
     * @param controller
     */
    @Override //RunObserver
    public void errorOccurred( RunnableItemController controller ) {
        
        errorOccurredFlag = true;
        
        //Update the gui
        if(jobSetObserver != null){
            jobSetObserver.errorOccurred(this);
        }
        
    }/* END errorOccurred( RunnableItemController ) */
    
    
    // ========================================================================
    /**
     * 
     * @param controller
     */
    @Override //RunObserver
    public void runCompleted( RunnableItemController controller ) {
        
        if( ( controller instanceof JobController ) == false ) return;
        
        synchronized( theRunningJobControllerList ) {
            theRunningJobControllerList.remove( (JobController)controller );
            
            if( theJobSet.runsConcurrently() ) { //If the JobSet is running concurrently...
                
                if( theRunningJobControllerList.isEmpty() ) { //If the running Job list is empty...
                    //The given JobController was the last to finish.
                    runHasCompleted(); //The JobSet's run has completed
                }
                
            } else { //If the JobSet is running in sequence...
                
                if( theRunningJobIndex < ( theJobControllerList.size() - 1 ) ) {
                    //There is at least one more Job to run.
                    
                    if( errorOccurredFlag && theJobSet.stopsOnError() ) {
                        //An error occurred during the last Job and the JobSet is configured
                        //  to stop in the event of an error.
                        runHasCompleted(); //The JobSet's run has completed
                    } else {
                        //Either no error occurred during the last Job, the JobSet is
                        //  not configured to stop in the event of an error, or both.
                        
                        //Run the next Job...
                        theRunningJobIndex++;
                        theRunningJobControllerList.add( theJobControllerList.get( theRunningJobIndex ) );
                        theRunningJobControllerList.get( 0 ).runItem( this );
                        
                    }

                    
                } else {
                    //There are no more Jobs to run.
                    runHasCompleted();
                }
            }
            
        }
        
        //Update the gui
        if(jobSetObserver != null){
            jobSetObserver.updateProgress(this);
        }
        
    }/* END runCompleted( RunnableItemController ) */

    
    // ========================================================================
    /**
     * Determines if the {@link JobSetController} has a reference to the given {@link JobController}.
     * <p>
     * If the argument is null this method does nothing and returns {@code false}.
     * 
     * @param controller the {@code JobController}
     * 
     * @return {@code true} if the {@code JobSetController} has a reference to the
     * given {@code JobController}; {@code false} otherwise
     */
    public boolean contains( JobController controller ) {
        
        if( controller == null ) return false;
        
        return theJobControllerList.contains( controller );
        
    }/* END contains( JobController ) */
    
    
    // ========================================================================
    /**
     * 
     * @param controllerList
     */
    public void addJobControllerReferences( List controllerList ) {
        
        if( controllerList == null ) return;
        
        if( theJobSet != null ) {
            
            String controllerItemName;
            String controllerItemType;
            List<JobRef> jobRefList = theJobSet.getJobRefList();
            List<JobController> clonedList = new ArrayList<>(controllerList);

            for( JobRef jobRef : jobRefList ) { //For every Job the JobSet references...
                
                //Find the JobController controlling the Job...
                for( int i=0; i< clonedList.size(); i++ ) { //For each Job[Controller]...
                    
                    JobController controller = clonedList.get(i);
                    controllerItemName = controller.getItemName(); //Get the Job's name
                    controllerItemType = controller.getJobType(); //Get the Job's type
                    
                    //If the JobController's item has the same name and is of the
                    //  same type as the Job referenced in the JobRef...
                    if( controllerItemName.equals( jobRef.getJobName() )
                            && controllerItemType.equals( jobRef.getJobType() ) ) {
                        
                        theJobControllerList.add( controller ); //Add the Job[Controller] to the JobSet[Controller]
                        clonedList.remove(i);
                        break; //Stop iterating through the JobControllers
                    }
                    
                    //NOTE: If the list of JobController's is exhausted without finding
                    //  a match for the JobRef, then there's been some kind of corruption.
                    //  That case is not yet handled here.
                    
                }
                
            }
            
        }
        
    }/* END addJobControllerReferences( List<JobController> ) */
    
    
    // ========================================================================
    /**
     * 
     * @return 
     */
    @Override //Ancestor
    public List<LibraryItemController> getChildren() {
        
        List<LibraryItemController> rtnList = new ArrayList<>();
        rtnList.addAll( theJobControllerList );
        return rtnList;
        
    }/* END getChildren() */

    
    // ========================================================================
    /**
     * Adds the given {@link LibraryItemController} to the {@link JobSetController}.
     * <p>
     * If the argument is null or not an instance of {@link JobController} this method
     * does nothing.
     * 
     * @param controller
     */
    @Override
    public void addChild( LibraryItemController controller ) {
        
        if( ( controller instanceof JobController ) == false ) return;
        //NOTE: If the argument is null the condition above will cause the method
        //  to return.
                
        theJobControllerList.add( (JobController)controller );
        theJobSet.addJobRef( new JobRef( controller.getItemName(), ( (JobController)controller ).getJobType() ) );
        
    }

    
    // ========================================================================
    /**
     * Adds the given {@link LibraryItemController} to the {@link JobSetController}
     * at the given index.
     * <p>
     * If the given {@code LibraryItemController} is null or not an instance of
     * {@link JobController} this method does nothing. If the given index is less
     * than 0 this method does nothing. If the given index is greater than the upper
     * bound the {@code JobController} is added at the end position.
     * 
     * @param controller
     * @param index
     */
    @Override //Ancestor
    public void insertChild( LibraryItemController controller, int index ) {
        
        if( ( controller instanceof JobController ) == false ) return;
        //NOTE: If the argument is null the condition above will cause the method
        //  to return.
        
        theJobControllerList.remove( (JobController)controller );
        
        JobRef jobRef = theJobSet.getJobRef( controller.getItemName(), ( (JobController)controller ).getJobType() );
        if( jobRef == null ) //If the JobSet doesn't have a JobRef for the Job...
            jobRef = new JobRef( controller.getItemName(), ( (JobController)controller ).getJobType() ); //Create one
        
        if( index < 0 || theJobControllerList.size() < index ) {
            theJobControllerList.add( (JobController)controller );
            theJobSet.addJobRef( jobRef );
        } else {
            theJobControllerList.add( index, (JobController)controller );
            theJobSet.insertJobRef( jobRef, index );
        }
        
    }

    
    // ========================================================================
    /**
     * Removes the given {@link LibraryItemController} from the {@link JobSetController}.
     * <p>
     * If the argument is null or not an instance of {@link JobController} this method
     * does nothing and returns {@code false}.
     * 
     * @param controller
     * @return 
     */
    @Override //Ancestor
    public boolean removeChild( LibraryItemController controller ) {
        
        if( ( controller instanceof JobController ) == false ) 
            return false;
         
        JobController aJC = (JobController)controller;
        theJobSet.removeJobRef( aJC.getItemName(), aJC.getJobType() );       
        
        return theJobControllerList.remove( aJC );
        
    }
    
    
    // ========================================================================
    /**
     * Determines if the {@link JobSetController} has a reference to the given {@link JobController}.
     * <p>
     * If the argument is null or not an instance of {@link JobController} this method
     * does nothing and returns {@code false}.
     * 
     * @param controller
     * @return 
     */
    @Override //Ancestor
    public boolean hasChild( LibraryItemController controller ) {
        
        if( ( controller instanceof JobController ) == false ) 
            return false;
          
        return theJobControllerList.contains( (JobController)controller );
        
    }
    
    
    
    // ========================================================================
    /**
     * 
     */
    @Override //LibraryItemController
    public void saveToDisk() {
        
        //Update the JobSet's list of JobRefs to match the current list of JobControllers.
        
        List<JobRef> jobRefList = theJobSet.getJobRefList();
        boolean jobFound = false;
        for( JobRef aRef : jobRefList ) { //For each JobRef...
            
            //Find the JobController controlling the referenced Job...
            for( JobController controller: theJobControllerList ) { //For each JobController...
                
                //If the JobController's item has the same name and is of the same
                //  type as the Job referenced in the JobRef...
                if( controller.getItemName().equals( aRef.getJobName() )
                        && controller.getJobType().equals( aRef.getJobType() ) ) {
                    
                    jobFound = true; 
                    break; 
                    
                }
                
            }    
            
            if( jobFound == false ) { //If the Job[Controller] was not found...
                theJobSet.removeComponent( aRef );
            }
        }
        
        super.saveToDisk();
        
    }
    
    
    // ========================================================================
    /**
     * 
     * @return 
     */
    @Override //Controller
    public Object getObject() {
        return theJobSet;
    }

    
    // ==========================================================================
    /**
     * 
     * @return 
     */
    @Override //Controller
    public JobSetPanel getRootPanel() {
        
        if( theJobSetPanel == null ) 
            theJobSetPanel = new JobSetPanel( this );        
        
        return theJobSetPanel;
        
    }

    
    // ========================================================================
    /**
     * 
     */
    @Override
    public void updateComponents() {
        JobSetPanel localPanel = getRootPanel();
        localPanel.populateComponents( this );
    }
    
    
    // ========================================================================
    /**
     * 
     * @return 
     */
    @Override //Object
    public String toString() {
        
        String rtnStr;
        
        if( theJobSet != null )
            rtnStr = theJobSet.getName();
        else
            rtnStr = super.toString();
        
        
        return rtnStr;
        
    }

    // ========================================================================
    /**
     *  Notifies the JTree to select the passed library item controller.
     * @param aController
     * @param showRunner
     */
    @Override
    public void selectController(LibraryItemController aController, boolean showRunner) {
        theListener.selectController( aController, showRunner );
    }

    // ========================================================================
    /**
     *  Sets the Object managed by the controller.
     * @param passedObj
     */
    @Override
    public void setObject(Object passedObj) {
        theJobSet = (JobSet) passedObj;
        saveToDisk();
    }
    
    // ==========================================================================
    /**
    * Clones the controller and the underlying object.
    *
     * @return 
    */
    @Override
    public JobSetController copy(){
        
        JobSet newJobSet;
        JobSetController aJobSetController = null;
        try {
                        
            
            //Clone the job set and create a new controller
            newJobSet = XmlBaseFactory.clone( theJobSet);
            aJobSetController = new JobSetController( newJobSet, theListener );
            
            //Fill the controlller with the new controllers
            List<JobController> newJobControllerList = new ArrayList<>();
            for( JobController oldController : theJobControllerList){
                newJobControllerList.add( (JobController) oldController.copy() );
            }
            
            aJobSetController.addJobControllerReferences(newJobControllerList);
            
        } catch (XmlBaseCreationException ex) {
            Log.log(Level.SEVERE, NAME_Class, "clone()", ex.getMessage(), ex );
        }
        
        return aJobSetController;
    }
     
     // ========================================================================
    /**
     *  Returns the right click popup menu for the library item.
     * @param multi
     * @return 
     */
    @Override
    public JPopupMenu getPopupJMenu( boolean multi ){
        
        JMenuItem menuItem;
        jobSetPopupMenu.removeAll();
        
        if( getHostController().isLocalHost() ){

            menuItem = new JMenuItem( Constants.ACTION_Run );
            menuItem.setActionCommand( Constants.ACTION_Run );
            menuItem.setEnabled( true );
            menuItem.addActionListener( (ActionListener)theListener );

            jobSetPopupMenu.add( menuItem );

        } else {

            //Only show run if it is connected
            if( getHostController().isConnected() ){
                menuItem = new JMenuItem( Constants.ACTION_Run );
                menuItem.setActionCommand( Constants.ACTION_Run );
                menuItem.setEnabled( true );
                menuItem.addActionListener( (ActionListener)theListener );
                jobSetPopupMenu.add(menuItem);
            }

        }

        menuItem = new JMenuItem( Constants.ACTION_Remove );
        menuItem.setActionCommand( Constants.ACTION_Remove );
        menuItem.setEnabled( true );
        menuItem.addActionListener( (ActionListener)theListener );
        jobSetPopupMenu.add(menuItem);
        
        menuItem = new JMenuItem( Constants.ACTION_Rename );
        menuItem.setActionCommand( Constants.ACTION_Rename );
        menuItem.setEnabled( true );
        menuItem.addActionListener( (ActionListener)theListener );
        jobSetPopupMenu.add( menuItem );       

        jobSetPopupMenu.addSeparator();
        menuItem = new JMenuItem( Constants.EXPORT_TO_BUNDLE );
        menuItem.setActionCommand( Constants.EXPORT_TO_BUNDLE );
        menuItem.setEnabled( true );
        menuItem.addActionListener( (ActionListener)theListener );
        jobSetPopupMenu.add(menuItem); 
        
        return jobSetPopupMenu;
    }
 
    // ========================================================================
    /**
     *  Returns the string displayed when adding a new object and controller.
     * @return 
     */
    @Override
    public String getCreationAction() {
        return ACTION_Create;
    }
    
    // ========================================================================
    /**
     *  Creates the underlying item that the controller is in charge of.
     * @param passedListener
     * @return 
     */
    @Override
    public Object createItem( LibraryItemControllerListener passedListener ) {
    
        JobSet newJobSet = null;
        String jobSetName;
        theListener = (JobSetControllerListener) passedListener;
        MainGuiController theMainController = (MainGuiController)theListener;
        MainGui theGui = (MainGui) theListener.getListenerComponent();
        
        while( true ) { //Forever...
            
            //Get a name for the new JobSet from the user
            jobSetName = JOptionPane.showInputDialog( theGui,
                    "Enter a name for the Job Set:\n(Leading and trailing whitespace is ignored.)", "Job Set Name", JOptionPane.PLAIN_MESSAGE );
            
            if( jobSetName != null ) { //If the user provided a value...

                jobSetName = jobSetName.trim(); //Trim any surrounding whitespace
                if( jobSetName.isEmpty() == false ) { //If the user provided a valid value...

                    if( theMainController.getControllerByObjName( HostFactory.LOCALHOST, JobSet.class.getSimpleName(), jobSetName) == null ) { //If no other JobSet has the given name...

                        newJobSet = new JobSet();
                        break; //Stop prompting the user

                    } else { //If another JobSet has the given name...
                        JOptionPane.showMessageDialog( theGui,
                                "A Job Set with that name already exists.  Please enter a different name.",
                                "Duplicate Name", JOptionPane.ERROR_MESSAGE );
                    }

                } else { //If the user provided an invalid value...
                    JOptionPane.showMessageDialog( theGui,
                            "That is not a valid name.", "Invalid Name", JOptionPane.ERROR_MESSAGE );
                }

            } else { //If the user did not provide a value...
                //The user pressed either the "X" (exit) button or "Cancel" button.
                break; //Abort the creation
            }
        
        } 
        
    
        if( newJobSet != null ) { //If a new JobSet was created...
            
            newJobSet.setId( IdGenerator.next() );
            newJobSet.setAttribute( JobSet.ATTRIBUTE_Name, jobSetName );
            
        }
        
        return newJobSet;
    
    }
    
    // ==========================================================================
    /**
     *  Get the path of the parent for saving to disk
     * 
     * @return 
     */
    @Override
    public HostController getHostController() {
        return theListener.getParentController(this);
    }


}/* END CLASS JobSetController */
