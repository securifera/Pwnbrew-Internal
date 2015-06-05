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
 * JobJProgressPanel.java
 *
 * Created on June 23, 2013, 8:55:12 AM
 */

package pwnbrew.gui.remote.job;

import pwnbrew.gui.dialogs.TasksJDialog;
import pwnbrew.tasks.RemoteTask;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import javax.swing.*;
import pwnbrew.generic.gui.ImageJButton;
import pwnbrew.log.Log;
import pwnbrew.misc.Constants;
import pwnbrew.utilities.FileUtilities;
import pwnbrew.utilities.Utilities;
import pwnbrew.tasks.TaskManager;

/**
 *
 *  
 */
public class JobJProgressPanel extends JPanel implements ActionListener {

     private final JLabel taskIconLabel = new JLabel();
     private final JLabel taskNameLabel = new JLabel();
     private final JLabel endTimeLabel = new JLabel();
     private final JProgressBar taskProgressBar = new JobJProgressBar();
     private final JLabel taskDestLabel = new JLabel();
     private final JButton cancelImgButton = new ImageJButton("stop.png");
     private final RemoteTask theRemoteTask;

     private final static String retry = "Retry";
     private final static String cancel ="Cancel";
     private final static String removeFromList = "Remove From List";
     private final static String openTaskFolderStr = "Open Task Folder";
     private boolean startedTimer = false;

     private final static int iconWidth = 40;
     private final static int iconHeight = 40;
     private final TaskManager theListener;
    
     private static final String NAME_Class = JobJProgressPanel.class.getSimpleName();

     public JobJProgressPanel(TaskManager passedListener, RemoteTask passedTask) {
          initComponents();

          theListener = passedListener;
          theRemoteTask = passedTask;
      
          initializeComponents();
     }

     private void initializeComponents() {

         setupLayout();
         setBackground(Color.WHITE);
         taskNameLabel.setText(theRemoteTask.getName());

         String taskDestLabelStr = new StringBuilder().append(theRemoteTask.getState())
                 .append(" - ").append(theRemoteTask.getTargetHostname()).append("(")
                 .append(theRemoteTask.getTargetIp()).append(")").toString();

         taskDestLabel.setText(taskDestLabelStr);
         taskDestLabel.setFont(new Font("Tahoma", Font.PLAIN, 10));

         cancelImgButton.setEnabled(true);

         //Create image
         String aStr = theRemoteTask.getIconString();
         TasksJDialog theDialog = TasksJDialog.getTasksJDialog();
         ImageIcon theIconImg = theDialog.getIconImage(aStr);
         if( theIconImg == null ){
             Image anImage = Utilities.loadImageFromJar( aStr ); 
             if( anImage != null ){
                theIconImg = new ImageIcon(anImage.getScaledInstance(iconWidth, iconHeight, Image.SCALE_SMOOTH));
                theDialog.setIconImage( aStr, theIconImg );
             }
         }
         
         
//         if( theImage != null ){
//            ImageIcon theIconImg = new ImageIcon(theImage.getScaledInstance(iconWidth, iconHeight, Image.SCALE_SMOOTH));
//            taskIconLabel.setIcon(theIconImg);
//         }
         taskIconLabel.setIcon(theIconImg);
         taskIconLabel.setText("");

         //Set the end time to not visible
         endTimeLabel.setVisible(false);

         //Listener for right click and for cancel button click
         addMouseListener( new MouseAdapter(){
            @Override
            public void mouseReleased(MouseEvent e){
                if(e.isPopupTrigger()){
                    doTreePopupMenuLogic(e);
                } else {

                    JButton theCancelButton = getCancelButton();
                    Rectangle cancelButtonRect = new Rectangle(theCancelButton.getLocation(), theCancelButton.getSize());
                    if(cancelButtonRect.contains(e.getX(), e.getY())){

                        theListener.cancelTask(Integer.parseInt( theRemoteTask.getClientId()), Integer.parseInt( theRemoteTask.getTaskId() ) );
                        updateComponents();
                                      
                    } else if(e.getClickCount() == 2){

                        try {
                            openTaskFolder();
                        } catch (IOException ex) {
                            Log.log(Level.WARNING, NAME_Class, "mouseReleased()", ex.getMessage(), ex );
                        }
                    }
                }
            }
         });

         String theTaskState = theRemoteTask.getState();
         if(theTaskState.equals( RemoteTask.TASK_COMPLETED) ||
              theTaskState.equals( RemoteTask.TASK_FAILED) ||
                 theTaskState.equals( RemoteTask.TASK_CANCELLED)){

            //The task end time
            String endTime = theRemoteTask.getEndTime();

            //Get a date object
            try {

               if(!endTime.isEmpty()){
                  Date theDate = new SimpleDateFormat(Constants.FORMAT_SessionDateTime).parse(endTime);
                  String theDateString = Utilities.getCustomDateString(theDate);

                  endTimeLabel.setText(theDateString);
                  endTimeLabel.setVisible(true);
               }

            } catch (ParseException ex) {
               Log.log(Level.INFO, NAME_Class, "initializeComponents()", ex.getMessage(), ex );
            }

            //Hide the progress bar and cancel button
            taskProgressBar.setValue(100);
            taskProgressBar.setVisible(false);
            cancelImgButton.setVisible(false);
            
         } else {

            taskProgressBar.setValue(0);
            taskProgressBar.setStringPainted(true);
         }

     }

    //****************************************************************************
    /**
    *  Determines what menu options to show on the popup menu based on the
    *  {@link XmlBase} object contained in the currently selected node.
    *
    *  @param  e   the {@code MouseEvent} that triggered the popup
    */
    private void doTreePopupMenuLogic( MouseEvent e ) {
       
        
       JPopupMenu popup = new JPopupMenu();
       JMenuItem menuItem;

       String currentState = theRemoteTask.getState();
       switch (currentState) {
             case RemoteTask.TASK_CANCELLED:
             case RemoteTask.TASK_FAILED:
                 menuItem = new JMenuItem( retry );
                 menuItem.setActionCommand( retry );
                 menuItem.addActionListener(this);
                 menuItem.setEnabled( true );
                 popup.add(menuItem);
                 //Add separator
                 popup.addSeparator();
                 break;
             case RemoteTask.TASK_XFER_FILES:
             case RemoteTask.TASK_RUNNING:
             case RemoteTask.TASK_XFER_RESULTS:
                 menuItem = new JMenuItem( cancel );
                 menuItem.setActionCommand( cancel );
                 menuItem.addActionListener(this);
                 menuItem.setEnabled( true );
                 popup.add(menuItem);
                 //Add separator
                 popup.addSeparator();
                 break;
       }

       //If the desktop api is available
       if(Desktop.isDesktopSupported()){
          menuItem = new JMenuItem( openTaskFolderStr );
          menuItem.setActionCommand( openTaskFolderStr );
          menuItem.addActionListener(this);
          menuItem.setEnabled( true );
          popup.add(menuItem);

          //Add separator
          popup.addSeparator();
       }

       menuItem = new JMenuItem( removeFromList );
       menuItem.setActionCommand( removeFromList );
       menuItem.addActionListener(this);
       menuItem.setEnabled( true );
       popup.add(menuItem);

       if( popup.getComponentCount() > 0 ) {
          popup.show(e.getComponent(), e.getX(), e.getY());
       }

    }
     
    //===============================================================
    /**
     *  Organizes the GUI components on the screen
    */
    private void setupLayout(){

        add(taskIconLabel);
        add(taskNameLabel);
        add(taskProgressBar);
        add(taskDestLabel);
        add(endTimeLabel);

        GroupLayout layout = new GroupLayout(this);
        setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(taskIconLabel, 60, 60, 60)
                .addGap(10)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                       .addContainerGap()
                       .addComponent(taskNameLabel, GroupLayout.PREFERRED_SIZE, 250, 250))
                    .addGroup(layout.createSequentialGroup()
                       .addContainerGap()
                       .addComponent(taskProgressBar, GroupLayout.PREFERRED_SIZE, 320, 320)
                       .addGap(15)
                       .addComponent(cancelImgButton, 15, 15, 15))
                    .addGroup(layout.createSequentialGroup()
                       .addContainerGap(15, 15)
                       .addComponent(taskDestLabel, GroupLayout.PREFERRED_SIZE, 250, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                       .addGap(0, 100, Short.MAX_VALUE)
                       .addComponent(endTimeLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE)
                       .addGap(12))))
            
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(taskIconLabel, 60, 60, 60))
            .addGroup(layout.createSequentialGroup()
                .addGap(20)
                .addComponent(endTimeLabel, 15, 15, 15))
            .addGroup(layout.createSequentialGroup()
                .addGap(6)
                .addComponent(taskNameLabel, 15, 15, 15)
                .addGap(2)
                .addGroup(layout.createParallelGroup()
                   .addComponent(taskProgressBar, 15, 15, 15)
                   .addComponent(cancelImgButton, 15, 15, 15))
                .addGap(4)
                .addComponent(taskDestLabel, 10, 10, 10)
                .addGap(6))
        );
        
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(null);
    }// </editor-fold>//GEN-END:initComponents


     //===============================================================
    /**
     *  Updates the GUI components on the screen to reflect the Task passed
    */
    public void updateComponents() {

        String theTaskState = theRemoteTask.getState();
        final TasksJDialog theJDialog = TasksJDialog.getTasksJDialog();
         switch (theTaskState) {
             
             case RemoteTask.TASK_COMPLETED:
             case RemoteTask.TASK_FAILED:
             case RemoteTask.TASK_CANCELLED:
                 
                 //If the timer had been started
                 if(startedTimer){
                    theJDialog.getUpdater().decrementRepaintTimer();
                    startedTimer = false;
                 }
                 //Hide the progress bar and cancel button
                 taskProgressBar.setValue(100);
                 taskProgressBar.setVisible(false);
                 taskProgressBar.setIndeterminate(false);
                 cancelImgButton.setVisible(false);
                 //The task end time
                 String endTime = theRemoteTask.getEndTime();
                 //Get a date object
                 try {

                    if(!endTime.isEmpty()){
                       Date theDate = new SimpleDateFormat(Constants.FORMAT_SessionDateTime).parse(endTime);
                       String theDateString = Utilities.getCustomDateString(theDate);

                       endTimeLabel.setText(theDateString);
                       endTimeLabel.setVisible(true);
                    }
                    
                 } catch (ParseException ex) {
                    Log.log(Level.INFO, NAME_Class, "updateComponents()", ex.getMessage(), ex );
                 }
                 break;
                 
             case RemoteTask.TASK_XFER_FILES:
             case RemoteTask.TASK_XFER_RESULTS:
                 
                 //If the timer had been started
                 if(startedTimer){
                    theJDialog.getUpdater().decrementRepaintTimer();
                    startedTimer = false;
                 }
                 //Set the progress bar to show the progress updates and the percentage
                 taskProgressBar.setStringPainted(true);
                 taskProgressBar.setIndeterminate(false);
                 taskProgressBar.setVisible(true);
                 cancelImgButton.setVisible(true);
                 int taskProgressValue = theRemoteTask.getStateProgress();
                 taskProgressBar.setValue(taskProgressValue);
                 
                 //Set to complete
                 if( taskProgressValue == 100 && ( theRemoteTask.getType().equals(Constants.FILE_UPLOAD)
                     || theRemoteTask.getType().equals(Constants.FILE_DOWNLOAD)) ){
                     
                     //Set to complete
                     theRemoteTask.setState(RemoteTask.TASK_COMPLETED);
                     updateComponents();
                     return;
                 }
                 
                 break;
                 
             case RemoteTask.TASK_RUNNING:
                 
                 //Set the progress bar to indeterminate
                 taskProgressBar.setStringPainted(false);
                 taskProgressBar.setIndeterminate(true);
                 taskProgressBar.setVisible(true);
                 cancelImgButton.setVisible(true);
                 theJDialog.getUpdater().startRepaintTimer();
                 startedTimer = true;
                 break;
         }

        String taskDestLabelStr = new StringBuilder().append(theRemoteTask.getState())
                .append(" - ").append(theRemoteTask.getTargetHostname()).append("(")
                .append(theRemoteTask.getTargetIp()).append(")").toString();

        taskDestLabel.setText(taskDestLabelStr);

        theJDialog.getUpdater().taskChanged();
    }

    //========================================================================
    /**
     *  Returns the cancel button
     * 
     * @return 
     */
    public JButton getCancelButton() {
       return cancelImgButton;
    }

    //========================================================================
    /**
     *  If the user hovers over the cancel button then display the cancel task tool tip
     * @param e
     * @return 
    */
    @Override
    public String getToolTipText(MouseEvent e){
       String tip = null;

       JButton theCancelButton = getCancelButton();
       Rectangle cancelButtonRect = new Rectangle(theCancelButton.getLocation(), theCancelButton.getSize());
       if(cancelButtonRect.contains(e.getX(), e.getY())){
          tip = "Cancel Task";
       }

       return tip;
    }

    //========================================================================
    /**
     *  Handles the various actions
     * @param evt
    */
    @Override
    public void actionPerformed(ActionEvent evt) {
        
        String actionCommand = evt.getActionCommand();

        try {
            
            switch (actionCommand) {
                case openTaskFolderStr:
                    openTaskFolder();
                    break;
                case removeFromList:
                    //Remove the task from the JTable and the JTable cell renderer map
                    TasksJDialog theTasksJDialog = TasksJDialog.getTasksJDialog();
                    theTasksJDialog.removeTask( theRemoteTask );
                    
                    //Remove the task
                    File aClientDir = theListener.getServerManager().getHostDirectory( Integer.parseInt(theRemoteTask.getClientId()) );
                    File theTaskDir = new File(aClientDir, theRemoteTask.getTaskId());
                    
                    FileUtilities.deleteDir(theTaskDir);
                    break;
                case retry:
                    //Try and run the task again
                    endTimeLabel.setVisible(false);
                    theListener.retryTask( theRemoteTask );
                    updateComponents();
                    break;
                case cancel:
                        
                    //Get the task IP and cancel the task
                    theListener.cancelTask(Integer.parseInt( theRemoteTask.getClientId()), Integer.parseInt( theRemoteTask.getTaskId() ) );
                    updateComponents();

                    break;
            }

        } catch (IOException ex) {
            Log.log(Level.WARNING, NAME_Class, "actionPerformed()", ex.getMessage(), ex );
        }

    }

    //========================================================================
    /**
     *  Opens up the folder that contains the task info
    */
    private void openTaskFolder() throws IOException {
        //Open a file browser to the task directory
        File aClientDir = theListener.getServerManager().getHostDirectory( Integer.parseInt(theRemoteTask.getClientId()) );
        File theTaskDir = new File(aClientDir, theRemoteTask.getTaskId());

        
//        File theTaskDir = new File(Directories.getRemoteTasksDirectory(), theRemoteTask.getTaskId());
        if(theTaskDir.exists()){
            if(Desktop.isDesktopSupported()){
                Desktop.getDesktop().open(theTaskDir);
            } else {
                JOptionPane.showMessageDialog( this, "Unable to locate the default editor for this file type.","Error", JOptionPane.ERROR_MESSAGE );           
            }
        }
    }

    //========================================================================
    /**
     *  Sets the progress bar from indeterminate so that the application
     * will close cleanly.
    */
    void prepClose() {

       TasksJDialog theJDialog = TasksJDialog.getTasksJDialog();
       taskProgressBar.setIndeterminate(false);

       //If the timer had been started
       if(startedTimer){
          theJDialog.getUpdater().decrementRepaintTimer();
          startedTimer = false;
       }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables



}
