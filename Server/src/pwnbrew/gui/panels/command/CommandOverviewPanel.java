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
 * CommandOverviewPanel.java
 *
 * Created on June 25, 2013, 11:42:22 AM
 */

package pwnbrew.gui.panels.command;

import pwnbrew.xmlBase.Command;
import pwnbrew.xmlBase.FileContentRef;
import pwnbrew.xmlBase.XmlBaseFactory;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import pwnbrew.generic.FileTransferHandler;
import pwnbrew.generic.gui.GenericProgressDialog;
import pwnbrew.misc.Constants;
import pwnbrew.misc.EditMenuUpdater;
import pwnbrew.utilities.FileUtilities;
import pwnbrew.utilities.GuiUtilities;
import pwnbrew.misc.ProgressDriver;
import pwnbrew.misc.ProgressListener;
import pwnbrew.utilities.StringUtilities;


/**
 *
 *  
 */

public class CommandOverviewPanel extends JPanel implements CaretListener, DocumentListener, ProgressDriver {

    private Command theCommand = null;
    private CommandOverviewPanelListener theListener = null;
    private JFileChooser theFileChooser = null;
   
    /** Creates new form CommandOverviewPanel
     * @param passedListener
     * @param passedTask */
    public CommandOverviewPanel( CommandOverviewPanelListener passedListener, Command passedTask ) {

        theListener = passedListener;
        theCommand = passedTask;

        initComponents();
        initializeComponents();
    }

    //****************************************************************************
    /**
    * Initializes all the components
    */
    private void initializeComponents(){

       theFileChooser = new JFileChooser();
       theFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
       theFileChooser.setMultiSelectionEnabled( true ); //Let the user select multiple files

       //Set a new model for the list in the overview tab
       DefaultListModel<FileContentRef> theModel = new DefaultListModel<>();
       fileList.setModel(theModel);

       //Set the data in the components
       populateComponents();
       
       //Add the listeners...
       descTextPane.getDocument().addDocumentListener( this );
       descTextPane.addCaretListener( this );

       //Add the listeners...
       commandTextField.addCaretListener( this );

       GuiUtilities.setComponentIcon(addFile,  15, 15, Constants.ADD_IMAGE_STR);
       GuiUtilities.setComponentIcon(removeFile, 15, 15, Constants.DELETE_IMG_STR);

       setupDragNDrop();
    }

    //****************************************************************************
    /**
    * Populate the components
    */
    public void populateComponents(){
        
        //Set the text of the Description text area
        descTextPane.setText(theCommand.getAttribute(Command.ATTRIBUTE_Description));

        //Set the command
        List<String> theCommandList = theCommand.getCommand();
        String commandStr = StringUtilities.join(theCommandList, " ");
        commandTextField.setText(commandStr);

        DefaultListModel theModel = (DefaultListModel) fileList.getModel();

        //If the file content does not contain an id then the file couldn't be found
        Map<String, FileContentRef> fileMap = theCommand.getFileContentRefMap();
        for( Iterator<FileContentRef> anIter = fileMap.values().iterator(); 
                       anIter.hasNext(); ) { 
            theModel.addElement(anIter.next());
        }
    }

    //****************************************************************************
    /**
    * Sets up drag and drop for the jTree
    */
    private void setupDragNDrop(){

       fileList.setDragEnabled(true);
       fileList.setDropMode(DropMode.ON_OR_INSERT);
       fileList.setTransferHandler( new FileTransferHandler(this));

    }

  // ==========================================================================
  /**
   *
   */
  private void handleDocumentChange( DocumentEvent e ) {

    String input = null;
    try {
      input = e.getDocument().getText( 0, e.getDocument().getLength() );
    } catch( BadLocationException ex ) {
      ex = null;
    }

    if( input != null ) { //If the input was obtained...

      //Set the text from the description area in the ScriptSet
      theListener.descriptionChanged(descTextPane.getText());

    }

  }/* END handleInputChange( DocumentEvent ) */


  // ==========================================================================
  /**
   *
   * <p>
   * Method from {@link DocumentListener} interface.
   *
   * @param e
   */
    @Override
  public void insertUpdate( DocumentEvent e ) {
    handleDocumentChange( e );
  }/* END insertUpdate( DocumentEvent ) */


  // ==========================================================================
  /**
   *
   * <p>
   * Method from {@link DocumentListener} interface.
   *
   * @param e
   */
    @Override
  public void removeUpdate( DocumentEvent e ) {
    handleDocumentChange( e );
  }/* END removeUpdate( DocumentEvent ) */


  // ==========================================================================
  /**
   *
   * <p>
   * Method from {@link DocumentListener} interface.
   *
   * @param e
   */
    @Override
  public void changedUpdate( DocumentEvent e ) {
     handleDocumentChange( e );
  }/* END changedUpdate( DocumentEvent ) */


    // ==========================================================================
    /**
     * Updates the edit menu in response to the given {@link CaretEvent}.
     * <p>
     * If the argument is null this method does nothing.
     *
     * @param event
     * @event the {@code CaretEvent}
     */
    @Override //CaretListener
    public void caretUpdate( CaretEvent event ) {

        if( event == null ) //If the CaretEvent is null...
            return; //Do nothing

        Object eventSource = event.getSource();
        if( eventSource instanceof JTextComponent ) {
            String selectedText = ( (JTextComponent)eventSource ).getSelectedText();
            boolean enableCutAndCopy = ( selectedText != null );
            EditMenuUpdater.updateEditMenu( enableCutAndCopy, enableCutAndCopy, true, true );
        } else {
            EditMenuUpdater.updateEditMenu( false, false, false, false );
        }
    }/* END caretUpdate( CaretEvent ) */


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scriptDetails = new javax.swing.JPanel();
        descScrollPane = new javax.swing.JScrollPane();
        descTextPane = new javax.swing.JTextPane();
        supportFilePane = new javax.swing.JScrollPane();
        fileList = new javax.swing.JList();
        addFile = new javax.swing.JButton();
        removeFile = new javax.swing.JButton();
        commandPanel = new javax.swing.JPanel();
        commandTextField = new javax.swing.JTextField();

        setPreferredSize(new java.awt.Dimension(346, 448));

        scriptDetails.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "General Details", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N
        scriptDetails.setPreferredSize(new java.awt.Dimension(336, 340));

        descScrollPane.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Description", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N
        descScrollPane.setOpaque(false);
        descScrollPane.setViewportView(descTextPane);

        supportFilePane.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Support Files", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N
        supportFilePane.setViewportView(fileList);

        addFile.setText(" ");
        addFile.setIconTextGap(0);
        addFile.setMargin(new java.awt.Insets(2, 2, 2, 2));
        addFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addFileActionPerformed(evt);
            }
        });

        removeFile.setText(" ");
        removeFile.setIconTextGap(0);
        removeFile.setMargin(new java.awt.Insets(2, 2, 2, 2));
        removeFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeFileActionPerformed(evt);
            }
        });

        commandPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Command", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

        commandTextField.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4));
        commandTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                commandTextFieldKeyReleased(evt);
            }
        });

        javax.swing.GroupLayout commandPanelLayout = new javax.swing.GroupLayout(commandPanel);
        commandPanel.setLayout(commandPanelLayout);
        commandPanelLayout.setHorizontalGroup(
            commandPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(commandTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 284, Short.MAX_VALUE)
        );
        commandPanelLayout.setVerticalGroup(
            commandPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(commandTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        javax.swing.GroupLayout scriptDetailsLayout = new javax.swing.GroupLayout(scriptDetails);
        scriptDetails.setLayout(scriptDetailsLayout);
        scriptDetailsLayout.setHorizontalGroup(
            scriptDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(scriptDetailsLayout.createSequentialGroup()
                .addGroup(scriptDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(scriptDetailsLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(scriptDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(commandPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(descScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
                            .addComponent(supportFilePane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)))
                    .addGroup(scriptDetailsLayout.createSequentialGroup()
                        .addGap(141, 141, 141)
                        .addComponent(addFile)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeFile)))
                .addContainerGap())
        );
        scriptDetailsLayout.setVerticalGroup(
            scriptDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(scriptDetailsLayout.createSequentialGroup()
                .addComponent(descScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(commandPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(supportFilePane, javax.swing.GroupLayout.PREFERRED_SIZE, 197, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(scriptDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addFile)
                    .addComponent(removeFile))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(scriptDetails, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scriptDetails, javax.swing.GroupLayout.DEFAULT_SIZE, 435, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void removeFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeFileActionPerformed
        removeFileFromCommand();
    }//GEN-LAST:event_removeFileActionPerformed

    private void addFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addFileActionPerformed
        
        File[] userSelectedFiles = null;

        int returnVal = theFileChooser.showDialog( this, "Select File(s)" ); //Show the dialog
        switch( returnVal ) {

          case JFileChooser.CANCEL_OPTION: //If the user canceled the selecting...
          case JFileChooser.ERROR_OPTION: //If the dialog was dismissed or an error occurred...
            break; //Do nothing

          case JFileChooser.APPROVE_OPTION: //If the user approved the selection...
            userSelectedFiles = theFileChooser.getSelectedFiles(); //Get the files the user selected
            break;
          default:
            break;

        }

        if(userSelectedFiles != null){
           //Create a list out of the array
           List theObjList = Arrays.asList(userSelectedFiles);

           GenericProgressDialog pDialog = new GenericProgressDialog(null, "Importing files to library...", this, false, theObjList);
           pDialog.setVisible(true);
        }
    }//GEN-LAST:event_addFileActionPerformed

    private void commandTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_commandTextFieldKeyReleased
        //Set the text from the description area in the ScriptSet
        theListener.commandChanged(commandTextField.getText());

    }//GEN-LAST:event_commandTextFieldKeyReleased


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addFile;
    private javax.swing.JPanel commandPanel;
    private javax.swing.JTextField commandTextField;
    private javax.swing.JScrollPane descScrollPane;
    private javax.swing.JTextPane descTextPane;
    private javax.swing.JList<FileContentRef> fileList;
    private javax.swing.JButton removeFile;
    private javax.swing.JPanel scriptDetails;
    private javax.swing.JScrollPane supportFilePane;
    // End of variables declaration//GEN-END:variables

   // ==========================================================================
   /**
    * Adds files to the task
    *
   */
    private String addFilesToTask( ProgressListener progressListener, List<Object> theObjList ) {

       String retVal = null;
       if( theObjList != null ) { //If the user selected any files...

            //Loop throught the files, create them, and add them to the list
            DefaultListModel theModel = (DefaultListModel) fileList.getModel();
            
            for( Object anObj : theObjList) { //For each file path...

                if(anObj instanceof File){
                    File aFile = (File)anObj;
                    if( FileUtilities.verifyCanRead( aFile ) ) { //If the file the File represents can be read...

                        try {
                            String fileHash = FileUtilities.createHashedFile( aFile, progressListener );
                            //NOTE: FileUtilities.createHashedFile( File ) writes the file to the library.

                            if( fileHash != null ) { //If the file was hashed (and presumably added to the library)...

                                //Create a FileContentRef
                                FileContentRef aFileContentRef = (FileContentRef)XmlBaseFactory.instantiateClass(FileContentRef.class );
                                aFileContentRef.setAttribute( FileContentRef.ATTRIBUTE_Name, aFile.getName() ); //Set the file's name
                                aFileContentRef.setAttribute( FileContentRef.ATTRIBUTE_FileHash, fileHash ); //Set the file's hash

                                //If it isn't in the list
                                if(!theModel.contains(aFileContentRef)){
                                   theModel.addElement(aFileContentRef);
                                   theListener.supportFileAdded(aFileContentRef);
                                }

                            }

                        } catch ( NoSuchAlgorithmException | IllegalAccessException | InstantiationException | IOException ex) {
                            JOptionPane.showMessageDialog( this, ex.getMessage(), "Could not add the file to the task.", JOptionPane.ERROR_MESSAGE );
                            retVal = "Could not add the file to the task.";
                        }

                    } else { //If the file cannot be read...
                       JOptionPane.showMessageDialog( this, new StringBuilder( "\tThis support file could not be read: \"" )
                                .append( aFile.getAbsolutePath() ).append( "\"" ).toString(), "Could not add the file to the task.", JOptionPane.ERROR_MESSAGE );
                       retVal = "Could not add the file to the task.";
                    }
                }

            }
       }
       return retVal;
    }

    // ==========================================================================
    /**
     * Removes files from the command
     *
    */
    private void removeFileFromCommand() {

        Object theSelObj = fileList.getSelectedValue();

        if(theSelObj != null && theSelObj instanceof FileContentRef){

           FileContentRef theFileContentRef = (FileContentRef)theSelObj;
           DefaultListModel theModel = (DefaultListModel) fileList.getModel();
           theModel.removeElement(theFileContentRef);

           theListener.supportFileRemoved(theFileContentRef);

        }
    }

    // ==========================================================================
    /**
     * Responsible for the importing of files
     *
     * @param theListener
     * @param passedObjectList
     * @return 
    */
    @Override
    public String executeFunction( ProgressListener theListener, List passedObjectList) {
        return addFilesToTask( theListener, passedObjectList );
    }


}
