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

package pwnbrew.filesystem;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import pwnbrew.fileoperation.FileOperationJList;
import pwnbrew.fileoperation.FileOperationUpdater;
import pwnbrew.fileoperation.RemoteFileIO;
import pwnbrew.misc.Constants;
import pwnbrew.misc.Utilities;

/**
 *
 * @author Securifera
 */
public class FileSystemJFrame extends JFrame implements Observer, FileJTableListener {

    
    private JFileChooser theFileChooser = null;
    
    //The file tree panel
    private FileTreePanel theFileTreePanel;
    private FileJTable theFileJTable;
    private final FileBrowserListener theListener;
    
    //The job lsit
    private FileOperationJList theFOList;
    
    //The JList observer
    private FileOperationUpdater theUpdater = null;
    
    /**
     * Creates new form FileSystemJFrame
     * @param passedListener
     */
    public FileSystemJFrame( FileBrowserListener passedListener ) {
        
        theListener = passedListener;
        initComponents();
        initializeComponents();
    }
    
    // ==========================================================================
    /**
    * Processes {@link WindowEvent}s occurring on this component.
    * @param event the {@code WindowEvent}
    */
    @Override //Overrides JFrame.processWindowEvent( WindowEvent )
    protected void processWindowEvent( WindowEvent event ) {
        if( WindowEvent.WINDOW_CLOSING == event.getID() ) { //If the event is the window closing...
            
            //Shutdown the updater
            theUpdater.shutdown();
            
            dispose();
            theListener.beNotified();
        } else
            super.processWindowEvent( event ); //Proceed normally
         
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainSplitPane = new javax.swing.JSplitPane();
        leftSplitPane = new javax.swing.JPanel();
        rightSplitPane = new javax.swing.JPanel();
        uploadButton = new javax.swing.JButton();
        downloadButton = new javax.swing.JButton();
        searchTextField = new javax.swing.JTextField();
        searchButton = new javax.swing.JButton();
        fileTableScrollPane = new javax.swing.JScrollPane();
        downloadFolderButton = new javax.swing.JButton();
        fileOperationScrollPane = new javax.swing.JScrollPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        mainSplitPane.setDividerLocation(240);

        javax.swing.GroupLayout leftSplitPaneLayout = new javax.swing.GroupLayout(leftSplitPane);
        leftSplitPane.setLayout(leftSplitPaneLayout);
        leftSplitPaneLayout.setHorizontalGroup(
            leftSplitPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 239, Short.MAX_VALUE)
        );
        leftSplitPaneLayout.setVerticalGroup(
            leftSplitPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 424, Short.MAX_VALUE)
        );

        mainSplitPane.setLeftComponent(leftSplitPane);

        uploadButton.setText("jButton1");

        downloadButton.setText("jButton1");

        searchButton.setText("jButton1");
        searchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchButtonActionPerformed(evt);
            }
        });

        downloadFolderButton.setText("jButton1");

        javax.swing.GroupLayout rightSplitPaneLayout = new javax.swing.GroupLayout(rightSplitPane);
        rightSplitPane.setLayout(rightSplitPaneLayout);
        rightSplitPaneLayout.setHorizontalGroup(
            rightSplitPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rightSplitPaneLayout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addComponent(uploadButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(downloadButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(downloadFolderButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 138, Short.MAX_VALUE)
                .addComponent(searchTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(searchButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5))
            .addComponent(fileTableScrollPane, javax.swing.GroupLayout.Alignment.TRAILING)
        );
        rightSplitPaneLayout.setVerticalGroup(
            rightSplitPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rightSplitPaneLayout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(rightSplitPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(uploadButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(downloadButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(searchTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(searchButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(downloadFolderButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(5, 5, 5)
                .addComponent(fileTableScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 389, Short.MAX_VALUE))
        );

        mainSplitPane.setRightComponent(rightSplitPane);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(mainSplitPane)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(fileOperationScrollPane)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(mainSplitPane)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fileOperationScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void searchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchButtonActionPerformed
        
        JTree theJTree = theFileTreePanel.getJTree(); 
        TreePath theSelPath = theJTree.getSelectionPath();    

        //Update the table
        if(searchButton.getToolTipText().equals("Search")){
            
            if( theSelPath != null ){

                DefaultMutableTreeNode selNode = (DefaultMutableTreeNode) theSelPath.getLastPathComponent();

                 //Get the search string
                String searchStr = searchTextField.getText();
                if( !searchStr.isEmpty() )
                    theListener.searchForFiles( selNode, searchStr);
                

            } 
            
            //Set the tooltip and image
            searchButton.setToolTipText("Cancel");
            Utilities.setComponentIcon(searchButton, 20, 20, Constants.DELETE_IMG_STR);
            
        } else if(searchButton.getToolTipText().equals("Cancel")){
            
            //Cancel the search
            theListener.cancelSearch();
            setCursor(null);
           
        }
        
    }//GEN-LAST:event_searchButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton downloadButton;
    private javax.swing.JButton downloadFolderButton;
    private javax.swing.JScrollPane fileOperationScrollPane;
    private javax.swing.JScrollPane fileTableScrollPane;
    private javax.swing.JPanel leftSplitPane;
    private javax.swing.JSplitPane mainSplitPane;
    private javax.swing.JPanel rightSplitPane;
    private javax.swing.JButton searchButton;
    private javax.swing.JTextField searchTextField;
    private javax.swing.JButton uploadButton;
    // End of variables declaration//GEN-END:variables

    private void initializeComponents() {
        
        theFileChooser = new JFileChooser();
        theFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        theFileChooser.setMultiSelectionEnabled( true ); //Let the user select multiple files
        
        searchButton.setOpaque(true);
        searchButton.setContentAreaFilled(false);
        searchButton.setMargin(new java.awt.Insets(0, 0, 0, 1));
        searchButton.setToolTipText("Search");
        Utilities.setComponentIcon(searchButton, 25, 25, Constants.SEARCH_IMG_STR);
        
        downloadFolderButton.setOpaque(true);
        downloadFolderButton.setContentAreaFilled(false);
        downloadFolderButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        downloadFolderButton.setToolTipText("Open Download Folder");
        Utilities.setComponentIcon(downloadFolderButton, 21, 21, Constants.FOLDER_IMG_STR);
        
        uploadButton.setOpaque(false);
        uploadButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        uploadButton.setToolTipText("Upload File");
        Utilities.setComponentIcon(uploadButton, 27, 27, Constants.UPLOAD_IMG_STR);
        
        downloadButton.setOpaque(false);
        downloadButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        downloadButton.setToolTipText("Download File");
        Utilities.setComponentIcon(downloadButton, 27, 27, Constants.DOWNLOAD_IMG_STR);
    
        uploadButton.addActionListener( new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent ae) {
                uploadButtonActionPerformed(ae);
            }
        });
        
        downloadButton.addActionListener( new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent ae) {
                downloadButtonActionPerformed(ae);
            }
        });
        
        downloadFolderButton.addActionListener( new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent ae) {
                try {
                    downloadFolderButtonActionPerformed(ae);
                } catch (IOException ex) {
                }
            }
        });
        
        //Disable them both
        setFileIOButtonEnablements(false, false);
        
        theFileTreePanel = new FileTreePanel( theListener );
        
        //Set the panels in the split pane
        mainSplitPane.setLeftComponent(theFileTreePanel);
        
        //Create a file table
        theFileJTable = new FileJTable( theListener );
        fileTableScrollPane.setViewportView(theFileJTable);
        fileTableScrollPane.getViewport().setBackground(Color.WHITE);
        
        
        //Setup the file list
        theFOList = new FileOperationJList();
        theFOList.initTable( theListener );
      
        fileOperationScrollPane.setViewportView(theFOList);
        
        //Add the observer
        theUpdater = new FileOperationUpdater();
        theUpdater.addObserver(this);
        
        //Configure the borders for the JTextField...
        Border theBorder = searchTextField.getBorder();
        Border newBorder = BorderFactory.createCompoundBorder( theBorder, 
                BorderFactory.createEmptyBorder( 2, 2, 2, 2 ) );
        searchTextField.setBorder(newBorder);
        
    }
    
    //===============================================================
    /**
    * Returns the updater
    * @return 
    */
    public FileOperationUpdater getUpdater(){
        return theUpdater;
    }
    
     //===============================================================
    /**
     *  Adds the task
     * @param passedTask
    */
    public void addTask(final RemoteFileIO passedTask){
        
        //Start the timer or increment it
        theUpdater.startRepaintTimer();
        
        DefaultListModel theListModel = (DefaultListModel)theFOList.getModel();
        theFOList.clearSelection();
        theListModel.add(0, passedTask);
       
    }
     //=================================================================
    /**
     * 
     * @param theRemoteTask
     */
    public void removeTask( RemoteFileIO theRemoteTask ) {

        DefaultListModel theListModel = (DefaultListModel)theFOList.getModel();
        theListModel.removeElement(theRemoteTask);
        theFOList.removeTask(theRemoteTask);
        
    }
    
    
    //====================================================================
    /**
     * Get the JTable
     * @return 
     */
    public FileJTable getFileJTable(){
        return theFileJTable;
    }
    
     //===========================================================================
    /**
     *  
     * @param evt 
     */
    private void uploadButtonActionPerformed(java.awt.event.ActionEvent evt) {                                             
       
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
            List<File> theObjList = Arrays.asList(userSelectedFiles);
//            int selRow = theFileJTable.getSelectedRow();
//            if( selRow != -1){
//                DefaultTableModel theTableModel = (DefaultTableModel) theFileJTable.getModel();            
//
//                //Get the directory
//                FileNode aFileNode = (FileNode)theTableModel.getValueAt(selRow, 0);
//                String filePath = aFileNode.getFile().getAbsolutePath();
//
//            } else {
//                
//            }
            theListener.uploadFiles(theObjList );  
                    
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
       theFOList.repaint();
    }
       
    //===========================================================================
    /**
     *  
     * @param evt 
     */
    private void downloadFolderButtonActionPerformed(java.awt.event.ActionEvent evt) throws IOException { 
    
        //Open a file browser to the task directory
        File fileDir = theListener.getDownloadDirectory();    
        if(fileDir.exists()){
            if(Desktop.isDesktopSupported()){
                Desktop.getDesktop().open(fileDir.getCanonicalFile());
            } else {
                JOptionPane.showMessageDialog( this, "Unable to locate the default editor for this file type.","Error", JOptionPane.ERROR_MESSAGE );           
            }
        }   
           
    }
    
    //===========================================================================
    /**
     *  
     * @param evt 
     */
    private void downloadButtonActionPerformed(java.awt.event.ActionEvent evt) { 
  
        List<RemoteFile> theFileList = new ArrayList<>();
        int[] selRowIndexes = theFileJTable.getSelectedRows();
        DefaultTableModel theTableModel = (DefaultTableModel) theFileJTable.getModel();
        for( int anInt : selRowIndexes ){
            
            FileNode aFileNode = (FileNode)theTableModel.getValueAt(anInt, 0);
            RemoteFile filePath = aFileNode.getFile();
            theFileList.add(filePath);
            
        }      
        
        theListener.downloadFiles( theFileList );
        
    }
    
    //========================================================================
    /**
     * Get the File Tree Panel
     * @return 
     */
    public FileTreePanel getFileTreePanel() {
        return theFileTreePanel;
    }

    
    //========================================================================
    /**
     * 
     * @param passedEvent 
     */
    @Override
    public void tableValueChanged(ListSelectionEvent passedEvent) {
        
        String theTypeStr = null;
        int[] selRowIndexes = theFileJTable.getSelectedRows();
        DefaultTableModel theTableModel = (DefaultTableModel) theFileJTable.getModel();
        for( int anInt : selRowIndexes ){
            String tempStr = (String)theTableModel.getValueAt(anInt, 2);
            if( theTypeStr == null )
                theTypeStr = tempStr;
            else if( !theTypeStr.equals( tempStr )){
                setFileIOButtonEnablements(false, false);
                return;
            }
            
        }
        
        //If more than one folder is selected we won't allow upload
        boolean containerUpload = true;
        if( selRowIndexes.length > 1 )
            containerUpload = false;
                       
        //Set the type
        if( theTypeStr != null )
            switch( theTypeStr ){
                case "Directory":
                case "Drive":
                    setFileIOButtonEnablements(containerUpload, false);
                    break;
                case "File":
                    setFileIOButtonEnablements(false, true);
                    break;
            }
    }

    //========================================================================
    /**
     * Sets the enablement for the upload and download buttons
     * 
     * @param uploadEnable
     * @param downloadEnable 
     */
    public void setFileIOButtonEnablements(boolean uploadEnable , boolean downloadEnable) {
        uploadButton.setEnabled(uploadEnable);
        downloadButton.setEnabled(downloadEnable);
    }

    //========================================================================
    /**
     * 
     */
    public void searchComplete() {
        
        //Set the tooltip and image and cursor
        setCursor(null);
        searchButton.setToolTipText("Search");
        Utilities.setComponentIcon(searchButton, 25, 25, Constants.SEARCH_IMG_STR);
        
    }

}
