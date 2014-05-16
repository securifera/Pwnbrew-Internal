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
 * TaskDialog.java
 *
 * Created on June 13, 2013, 11:52 AM
 */

package pwnbrew.gui.dialogs;

import pwnbrew.tasks.RemoteTask;
import pwnbrew.exception.XmlBaseCreationException;
import java.awt.Color;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ItemEvent;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import javax.swing.*;
import pwnbrew.controllers.MainGuiController;
import pwnbrew.generic.gui.DefaultCellBorderRenderer;
import pwnbrew.gui.MainGui;
import pwnbrew.gui.remote.job.JobJList;
import pwnbrew.gui.remote.job.JobUpdater;
import pwnbrew.gui.remote.job.JobJListCellRenderer;
import pwnbrew.logging.Log;
import pwnbrew.misc.Directories;
import pwnbrew.utilities.Utilities;

/**
 *
 *  
 */
public final class TasksJDialog extends JDialog implements Observer{

   private static MainGuiController theGuiController = null;
   private JobJList theTaskJList;
   private static TasksJDialog staticSelf = null;

   private final static String dateAdded = "Date Added";
   private final static String client = "Client";
   private final static String status = "Status";

   private JobUpdater theUpdater = null;

   private static final String NAME_Class = TasksJDialog.class.getSimpleName();
   private final Map<String, ImageIcon> theIconMap = new HashMap<>();

   //=======================================================================
   /**
    * Creates new dialog TasksJDialog
   */
   private TasksJDialog() {
      //if the server isn't null then pass the gui from otherwise pass null
      super( (theGuiController != null) ? (MainGui)theGuiController.getObject() : null, "Tasks");
      
      //Add the observer
      theUpdater = new JobUpdater();
      theUpdater.addObserver(this);
      
      initComponents();
      initializeComponents();
      setLocationRelativeTo(null);
   
   }//End Constructor

   //===============================================================
    /**
     *  Ensure that there is only one task dialog
     * 
     * @return 
     */
   public static TasksJDialog getTasksJDialog() {

       if(staticSelf == null){
          staticSelf = new TasksJDialog();
       }

       return staticSelf;
   }
   
   //===============================================================
   /**
    *   Sets the server controller 
    * @param passedController 
   */   
   public static void setMainGuiController( MainGuiController passedController ){
       theGuiController = passedController;
   }

   //****************************************************************************
   /**
    * Returns the task updater
     * @return 
   */
   public JobUpdater getUpdater(){
      return theUpdater;
   }

   //****************************************************************************
   /**
    * Initializes all the components
   */
   private void initializeComponents(){
      
      theTaskJList = new JobJList();
      theTaskJList.initTable( theGuiController );
      
      taskScrollPane.setViewportView(theTaskJList);
      taskScrollPane.getViewport().setBackground(Color.WHITE);
      
      setupLayout();

      DefaultComboBoxModel theModel = new DefaultComboBoxModel();
    
      theModel.addElement(dateAdded);
      theModel.addElement(client);
      theModel.addElement(status);
      sortCombo.setModel(theModel);
      sortCombo.setRenderer(new DefaultCellBorderRenderer( BorderFactory.createEmptyBorder(4, 4, 4, 4) ));

      //Get task from disk, sort them by date, and add them to the list
      try {
         
         List<RemoteTask> theOldTasks = Utilities.getTasksFromDisk( Directories.getRemoteTasksDirectory() );
         Collections.sort( theOldTasks, RemoteTask.DATE_SORT);

         //Add the sorted list back to the model
         DefaultListModel theListModel = (DefaultListModel)theTaskJList.getModel();
         for(RemoteTask aTask : theOldTasks){
            theListModel.addElement(aTask);
         }
         
      } catch (XmlBaseCreationException ex) {
         Log.log(Level.WARNING, NAME_Class, "run()", ex.getMessage(), ex );
      } catch(IOException ex){
         Log.log(Level.SEVERE, NAME_Class, "run()", ex.getMessage(), ex );
      }

   }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
   @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        clearButton = new javax.swing.JButton();
        taskScrollPane = new javax.swing.JScrollPane();
        sortCombo = new javax.swing.JComboBox();
        sortLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Tasks");
        setName("Tasks"); // NOI18N
        setResizable(false);
        getContentPane().setLayout(null);

        clearButton.setText("Clear");
        clearButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearButtonActionPerformed(evt);
            }
        });
        getContentPane().add(clearButton);
        clearButton.setBounds(10, 246, 57, 23);
        getContentPane().add(taskScrollPane);
        taskScrollPane.setBounds(0, 0, 2, 2);

        sortCombo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                sortComboItemStateChanged(evt);
            }
        });
        getContentPane().add(sortCombo);
        sortCombo.setBounds(290, 250, 80, 20);

        sortLabel.setText("Sort By:");
        getContentPane().add(sortLabel);
        sortLabel.setBounds(234, 250, 40, 14);

        pack();
    }// </editor-fold>//GEN-END:initComponents

   private void clearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearButtonActionPerformed
   
       try {
          theTaskJList.clearTasks();
       } catch (IOException ex) {
          Log.log(Level.SEVERE, NAME_Class, "clearButtonActionPerformed()", ex.getMessage(), ex );
       }
   }//GEN-LAST:event_clearButtonActionPerformed

   private void sortComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_sortComboItemStateChanged

       if(evt.getStateChange() == ItemEvent.SELECTED){
           String theSortMethod = (String)sortCombo.getSelectedItem();
           DefaultListModel theModel = (DefaultListModel) theTaskJList.getModel();
           
           //Get a list of the task and the appropriate comparator
           Object[] theTaskArr = theModel.toArray();
           List<RemoteTask> theTasks = new ArrayList(Arrays.asList(theTaskArr));
           Comparator<RemoteTask> theComparator;

           if(theSortMethod.equals(status)){
              theComparator = RemoteTask.STATUS_SORT;
           } else if(theSortMethod.equals(client)){
              theComparator = RemoteTask.CLIENT_SORT;
           } else {
              theComparator = RemoteTask.DATE_SORT;
           }

           Collections.sort( theTasks, theComparator);
           theModel.clear();

           //Add the sorted list back to the model
           for(RemoteTask aTask : theTasks){
              theModel.addElement(aTask);
           }
          
       }
   }//GEN-LAST:event_sortComboItemStateChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton clearButton;
    private javax.swing.JComboBox sortCombo;
    private javax.swing.JLabel sortLabel;
    private javax.swing.JScrollPane taskScrollPane;
    // End of variables declaration//GEN-END:variables

    //===============================================================
    /**
     *  Organizes the GUI components on the screen
    */
    private void setupLayout(){

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(taskScrollPane, GroupLayout.DEFAULT_SIZE, 504, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(clearButton)
                .addGap(150, 150, Short.MAX_VALUE)
                .addComponent(sortLabel)
                .addGap(10)
                .addComponent(sortCombo, 90, 90, 90)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(taskScrollPane, GroupLayout.PREFERRED_SIZE, 235, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(clearButton)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGap(252)
                .addComponent(sortLabel)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGap(250)
                .addComponent( sortCombo, 20, 20, 20)
                .addContainerGap())
        );

        pack();
    }

    //===============================================================
    /**
     *  Adds the task
     * @param passedTask
    */
    public void addTask(final RemoteTask passedTask){
        
       DefaultListModel theListModel = (DefaultListModel)theTaskJList.getModel();
       theTaskJList.clearSelection();
       theListModel.add(0, passedTask);
       
    }

    //===============================================================
    /**
     *  Removes the task from the JTable model
     * @param theTask
    */
    public void removeTask( RemoteTask theTask ) {
        DefaultListModel theListModel = (DefaultListModel)theTaskJList.getModel();
        theListModel.removeElement(theTask);
        theTaskJList.removeTask(theTask);
    }

    //===============================================================
    /**
     *  Saves all the task in the task window to disk
     * @throws java.io.IOException
    */
    public void saveAllTasks() throws IOException {
        DefaultListModel theListModel = (DefaultListModel)theTaskJList.getModel();
        Enumeration theElements = theListModel.elements();
        while( theElements.hasMoreElements() ){
           Object nextElement = theElements.nextElement();
           if(nextElement instanceof RemoteTask){
              ((RemoteTask)nextElement).writeSelfToDisk();
           }
        }
    }

    //===============================================================
    /**
     *  Tell the table to repaint
     * @param o
     * @param arg
    */
    @Override
    public void update(Observable o, Object arg) {
       theTaskJList.repaint();
    }

    //===============================================================
    /**
     * 
     * @param passedBool 
     */
    @Override
    public void setVisible(boolean passedBool){

      if(passedBool && !isVisible()){
         
         Window theWindow = getOwner();
         if( theWindow instanceof MainGui ){
             
             MainGui rootFrame = (MainGui)theWindow;
             Point rootFrameLoc = rootFrame.getLocation();

             int xLoc = rootFrameLoc.x + rootFrame.getWidth() - getWidth();
             int yLoc = rootFrameLoc.y;

             setLocation(xLoc, yLoc);
         }
      }

      //Set visible
      super.setVisible(true);

   }

    //===============================================================
    /**
    *  Ensures none of the tasks are still running.
    */
    public void prepClose() {
        JobJListCellRenderer theRenderer = (JobJListCellRenderer) theTaskJList.getCellRenderer();
        theRenderer.prepClose();
    }

    //===============================================================
    /**
     * 
     * @param aStr
     * @return 
     */
    public ImageIcon getIconImage(String aStr) {
        
        ImageIcon anIcon;
        synchronized( theIconMap ){
            anIcon = theIconMap.get(aStr);
        }
        return anIcon;
    }

     //===============================================================
    /**
     *  Set the icon for the string
     * @param aStr
     * @param theIconImg 
     */
    public void setIconImage(String aStr, ImageIcon theIconImg) {
        synchronized( theIconMap ){
            theIconMap.put(aStr, theIconImg);
        }
    }

}
