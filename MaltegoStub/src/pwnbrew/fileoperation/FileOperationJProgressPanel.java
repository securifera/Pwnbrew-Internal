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
 * FileOperationJProgressPanel.java
 *
 * Created on June 23, 2013, 8:55:12 AM
 */

package pwnbrew.fileoperation;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import pwnbrew.misc.DebugPrinter;
import pwnbrew.misc.ImageJButton;
import pwnbrew.misc.Utilities;
/**
 *
 *  
 */
public class FileOperationJProgressPanel extends JPanel implements ActionListener {

     private final JLabel theFileIOIconLabel = new JLabel();
     private final JLabel theFilePath = new JLabel();
     private final JLabel theTaskIdLabel = new JLabel();
     private final JProgressBar theProgressBar = new FileOperationJProgressBar();
     private final JButton cancelImgButton = new ImageJButton("stop.png");
     private final RemoteFileIO theRemoteTask;

     private final static String cancel ="Cancel";
     private final static String removeFromList = "Remove From List";
     private final static String openTaskFolderStr = "Open Task Folder";
     private boolean startedTimer = false;

     private final static int iconWidth = 15;
     private final static int iconHeight = 15;
     private final TaskManager theListener;
    
     private final static Map<String, ImageIcon> theIconMap = new HashMap<>();
     
     private static final String NAME_Class = FileOperationJProgressPanel.class.getSimpleName();
     

     //=======================================================================
     /**
      *     Constructor
      * 
      * @param passedListener
      * @param passedTask 
      */
     public FileOperationJProgressPanel(TaskManager passedListener, RemoteFileIO passedTask) {
          initComponents();

          theListener = passedListener;
          theRemoteTask = passedTask;
      
          initializeComponents();
     }

     private void initializeComponents() {

         setupLayout();
         setBackground(Color.WHITE);
         theFilePath.setText(theRemoteTask.getFilePath());
//         theFilePath.setHorizontalAlignment( SwingConstants.CENTER );

         cancelImgButton.setEnabled(true);

         //Create image
         String aStr = theRemoteTask.getIconString();
         ImageIcon theIconImg = getIconImage(aStr);
         if( theIconImg == null ){
             Image anImage = Utilities.loadImageFromJar( aStr ); 
             if( anImage != null ){
                theIconImg = new ImageIcon(anImage.getScaledInstance(iconWidth, iconHeight, Image.SCALE_SMOOTH));
                setIconImage( aStr, theIconImg );
             }
         }

         theFileIOIconLabel.setIcon(theIconImg);
         theFileIOIconLabel.setText("");
         theTaskIdLabel.setText( "ID:  " + theRemoteTask.getTaskId());

         //Set the end time to not visible
//         endTimeLabel.setVisible(false);

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
                        
                        theListener.cancelTask( Integer.parseInt( theRemoteTask.getTaskId() ) );
                        updateComponents();                                       
                        
                    } else if(e.getClickCount() == 2){

                        try {
                            openTaskFolder();
                        } catch (IOException ex) {
                            DebugPrinter.printMessage( NAME_Class, "mouseReleased", ex.getMessage(), ex);
                        }
                    }
                }
            }
         });

         String theTaskState = theRemoteTask.getState();
         if(theTaskState.equals( RemoteFileIO.TASK_COMPLETED) ||
              theTaskState.equals( RemoteFileIO.TASK_FAILED) ||
                 theTaskState.equals( RemoteFileIO.TASK_CANCELLED)){

            //Hide the progress bar and cancel button
            theProgressBar.setValue(100);
            theProgressBar.setVisible(false);
            cancelImgButton.setVisible(false);
            
         } else {

            theProgressBar.setValue(0);
            theProgressBar.setStringPainted(true);
         }

     }

    //=======================================================================
    /**
    *  Determines what menu options to show on the popup menu based on the
    *  {@link XmlObject} object contained in the currently selected node.
    *
    *  @param  e   the {@code MouseEvent} that triggered the popup
    */
    private void doTreePopupMenuLogic( MouseEvent e ) {
       
        
       JPopupMenu popup = new JPopupMenu();
       JMenuItem menuItem;

       String currentState = theRemoteTask.getState();
       switch (currentState) {
             case RemoteFileIO.TASK_XFER_FILES:
                 
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

       if( popup.getComponentCount() > 0 )
          popup.show(e.getComponent(), e.getX(), e.getY());
       
    }
     
    //===============================================================
    /**
     *  Organizes the GUI components on the screen
    */
    private void setupLayout(){

        add(theFileIOIconLabel);
        add(theFilePath);
        add(theProgressBar);
        add(theTaskIdLabel);

        GroupLayout layout = new GroupLayout(this);
        setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(theFileIOIconLabel, 15, 15, 15)
                .addGap(10)
                .addComponent(theFilePath, GroupLayout.PREFERRED_SIZE, 330, Short.MAX_VALUE)
                .addGap(10)
                .addComponent(theTaskIdLabel)
                .addGap(15)
                .addComponent(theProgressBar, GroupLayout.PREFERRED_SIZE, 135, 135)
                .addGap(10)
                .addComponent(cancelImgButton, 15, 15, 15)
                .addGap(10)
            )
  
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(3)
                .addGroup(layout.createParallelGroup()
                    .addComponent(theTaskIdLabel)
                    .addComponent(theFileIOIconLabel, 16,16,16)
                    .addComponent(theProgressBar, 16,16,16)
                    .addComponent(cancelImgButton, 16,16,16)
                    .addComponent(theFilePath, 16,16,16))
                .addGap(3))
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
        FileOperationUpdater theUpdater = theListener.getUpdater();
         switch (theTaskState) {
             
             case RemoteFileIO.TASK_COMPLETED:
             case RemoteFileIO.TASK_FAILED:
             case RemoteFileIO.TASK_CANCELLED:
                 
                 //If the timer had been started
                 if(startedTimer){
                    theUpdater.decrementRepaintTimer();
                    startedTimer = false;
                 }
                 //Hide the progress bar and cancel button
                 cancelImgButton.setVisible(false);
                 break;
                 
             case RemoteFileIO.TASK_XFER_FILES:
                 
                 //If the timer had been started
                 if(startedTimer){
                    theUpdater.decrementRepaintTimer();
                    startedTimer = false;
                 }
                 //Set the progress bar to show the progress updates and the percentage
                 theProgressBar.setStringPainted(true);
                 theProgressBar.setVisible(true);
                 cancelImgButton.setVisible(true);
                 int taskProgressValue = theRemoteTask.getStateProgress();
                 theProgressBar.setValue(taskProgressValue);
                 
                 //Set to complete
                 if( taskProgressValue == 100  ){
                     
                     //Set to complete
                     theRemoteTask.setState(RemoteFileIO.TASK_COMPLETED);
                     updateComponents();
                     return;
                 }
                 
                 break;

         }

        theUpdater.taskChanged();
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
        if(cancelButtonRect.contains(e.getX(), e.getY()))
            tip = "Cancel Task";        

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
                    theListener.removeTask(theRemoteTask);
                    break;
                case cancel:
                        
                    //Get the task IP and cancel the task
                    theListener.cancelTask( Integer.parseInt( theRemoteTask.getTaskId() ) );
                    updateComponents();

                    break;
            }

        } catch (IOException ex) {
            DebugPrinter.printMessage( NAME_Class, "actionPerformed", ex.getMessage(), ex);
        }

    }

    //========================================================================
    /**
     *  Opens up the folder that contains the task info
    */
    private void openTaskFolder() throws IOException {
        //Open a file browser to the task directory
        String taskId = theRemoteTask.getTaskId();
        File theTaskDir = theListener.getDownloadDirectory( taskId );
        if(theTaskDir.exists()){
            if(Desktop.isDesktopSupported()){
                Desktop.getDesktop().open(theTaskDir.getCanonicalFile());
            } else {
                JOptionPane.showMessageDialog( this, "Unable to locate the default editor for this file type.","Error", JOptionPane.ERROR_MESSAGE );           
            }
        }
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

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables



}
