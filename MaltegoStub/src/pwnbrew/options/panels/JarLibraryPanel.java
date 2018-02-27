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

package pwnbrew.options.panels;

import java.awt.Color;
import java.awt.Insets;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import pwnbrew.StubConfig;
import pwnbrew.generic.gui.PanelListener;
import pwnbrew.generic.gui.ValidTextField;
import pwnbrew.misc.Constants;
import pwnbrew.misc.FileFilterImp;
import pwnbrew.misc.JarItemException;
import pwnbrew.misc.StandardValidation;
import pwnbrew.misc.Utilities;

/**
 *
 */
public class JarLibraryPanel extends OptionsJPanel implements JarTableListener {

    private JFileChooser theJarChooser = null;    
    private final FileFilterImp theJarFilter = new FileFilterImp();
    private JarTable theJarTable;
    
    private static final String NAME_Class = JarLibraryPanel.class.getSimpleName();
    
    private static final String JAR_EXT = "jar";
      

    //===================================================================
    /** Creates new form AdvancedlOptionsPanel
     * @param passedTitle
     * @param parent 
     */
    public JarLibraryPanel( String passedTitle, PanelListener parent ) {
        super( passedTitle, parent );
        initComponents();
        initializeComponents();
    }

    //===================================================================
    /**
     * Initialize components
    */
    private void initializeComponents() {

        //Create a JFileChooser to select wim files...
        theJarFilter.addExt( JAR_EXT);
        theJarChooser = new JFileChooser();
        theJarChooser.setMultiSelectionEnabled(false);
        theJarChooser.setFileFilter(theJarFilter);            
        
        Utilities.setComponentIcon(addFile,  15, 15, Constants.ADD_IMAGE_STR);
        Utilities.setComponentIcon(removeFile, 15, 15, Constants.DELETE_IMG_STR);
        Utilities.setComponentIcon(buildStagerButton, 28, 28, Constants.BUILD_STAGER_IMG_STR);
        buildStagerButton.setText("Build");
        
        addFile.setToolTipText("Add JAR to Library");
        removeFile.setToolTipText("Remove JAR from Library");
        
        //Create a file table
        theJarTable = new JarTable( this );
        jarScrollPane.setViewportView(theJarTable);
        jarScrollPane.getViewport().setBackground(Color.WHITE);
        
        StubConfig theConf = StubConfig.getConfig();
        String serverIp = theConf.getServerIp();
        ((ValidTextField)ipTextField).setValidation( StandardValidation.KEYWORD_Host);
        ipTextField.setText(serverIp);
        ipTextField.setMargin(new Insets(2,4,2,4));
        
        ((ValidTextField)cdnTextField).setValidation( StandardValidation.KEYWORD_Host);
        cdnTextField.setText(serverIp);
        cdnTextField.setMargin(new Insets(2,4,2,4));
        
        ((ValidTextField)portTextField).setValidation( StandardValidation.KEYWORD_Port);
        portTextField.setMargin(new Insets(2,4,2,4));
        
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        stagerSetupPanel = new javax.swing.JPanel();
        buildStagerButton = new javax.swing.JButton();
        ipLabel = new javax.swing.JLabel();
        ipTextField = new ValidTextField( "0.0.0.0" );
        portLabel = new javax.swing.JLabel();
        portTextField = portTextField = new ValidTextField( "443" );
        cdnLabel = new javax.swing.JLabel();
        cdnTextField =  new ValidTextField( "localhost.localhost" );
        removeFile = new javax.swing.JButton();
        addFile = new javax.swing.JButton();
        jarScrollPane = new javax.swing.JScrollPane();

        stagerSetupPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Stager Setup"));

        buildStagerButton.setText("Build");
        buildStagerButton.setIconTextGap(7);
        buildStagerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buildStagerButtonActionPerformed(evt);
            }
        });

        ipLabel.setText("Server Host/IP:");

        portLabel.setText("Port:");

        cdnLabel.setText("Host Header:");

        cdnTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cdnTextFieldActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout stagerSetupPanelLayout = new javax.swing.GroupLayout(stagerSetupPanel);
        stagerSetupPanel.setLayout(stagerSetupPanelLayout);
        stagerSetupPanelLayout.setHorizontalGroup(
            stagerSetupPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(stagerSetupPanelLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(stagerSetupPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(stagerSetupPanelLayout.createSequentialGroup()
                        .addComponent(ipLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(ipTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(portLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(portTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 43, Short.MAX_VALUE)
                        .addComponent(buildStagerButton, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(stagerSetupPanelLayout.createSequentialGroup()
                        .addComponent(cdnLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cdnTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        stagerSetupPanelLayout.setVerticalGroup(
            stagerSetupPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(stagerSetupPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(stagerSetupPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(stagerSetupPanelLayout.createSequentialGroup()
                        .addGroup(stagerSetupPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(ipTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(ipLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(stagerSetupPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cdnTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cdnLabel))
                        .addGap(11, 11, 11))
                    .addGroup(stagerSetupPanelLayout.createSequentialGroup()
                        .addGroup(stagerSetupPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(buildStagerButton)
                            .addGroup(stagerSetupPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(portTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(portLabel)))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );

        removeFile.setText(" ");
        removeFile.setIconTextGap(0);
        removeFile.setMargin(new java.awt.Insets(2, 2, 2, 2));
        removeFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeFileActionPerformed(evt);
            }
        });

        addFile.setText(" ");
        addFile.setIconTextGap(0);
        addFile.setMargin(new java.awt.Insets(2, 2, 2, 2));
        addFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addFileActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jarScrollPane)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(addFile)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeFile)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(stagerSetupPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(stagerSetupPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jarScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 315, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addFile)
                    .addComponent(removeFile))
                .addGap(45, 45, 45))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void removeFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeFileActionPerformed
        int[] selRowIndexes = theJarTable.getSelectedRows();
        for( int anInt : selRowIndexes )   
            deleteJarItem(anInt);
    }//GEN-LAST:event_removeFileActionPerformed

    private void addFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addFileActionPerformed
        selectJar();
    }//GEN-LAST:event_addFileActionPerformed

    private void buildStagerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buildStagerButtonActionPerformed
        ValidTextField ipField = (ValidTextField) ipTextField;
        ValidTextField portField = (ValidTextField) portTextField;
        if( ipField.isValid() && portField.isValid()){
            
            //See if the table already contains the entry
            DefaultTableModel theModel = (DefaultTableModel) theJarTable.getModel();
            for( int i =0; i < theModel.getRowCount(); i++ ){

                //Get table entries
                String jarType = (String) theJarTable.getValueAt(i, 1);
                if(jarType.equals(Constants.STAGER_TYPE)){
                    String ipStr = ipField.getText().trim();
                    String connectStr = "https://" + ipStr + ":" + portTextField.getText().trim();
                    //Get table entries
                    String jarName = (String) theJarTable.getValueAt(i, 0);
                    String jvmVersion = (String) theJarTable.getValueAt(i, 2);
                    String jarVersion = (String) theJarTable.getValueAt(i, 3);
                    
                    //See if the host header has been set and it's different than the C2 IP
                    String hostHeaderStr = null;
                    ValidTextField cdnField = (ValidTextField) cdnTextField;
                    if(cdnField.isValid() ){
                        String cdnHost = cdnField.getText().trim();
                        if( !cdnHost.equals(ipStr))
                            hostHeaderStr = cdnHost;
                                               
                    }                    
                    
                    getListener().getStagerFile( connectStr, jarName, jarType, jvmVersion, jarVersion, hostHeaderStr );
                    return;
                }
            }

            JOptionPane.showMessageDialog( this, "No stager", "Unable to build Stager. No stager exist in the module library.", JOptionPane.ERROR_MESSAGE ); 
        
        } else {
            JOptionPane.showMessageDialog( this, "Invalid values","Unable to build Stager. IP and port must be valid values.", JOptionPane.ERROR_MESSAGE ); 
        }
    }//GEN-LAST:event_buildStagerButtonActionPerformed

    private void cdnTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cdnTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cdnTextFieldActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addFile;
    private javax.swing.JButton buildStagerButton;
    private javax.swing.JLabel cdnLabel;
    private javax.swing.JTextField cdnTextField;
    private javax.swing.JLabel ipLabel;
    private javax.swing.JTextField ipTextField;
    private javax.swing.JScrollPane jarScrollPane;
    private javax.swing.JLabel portLabel;
    private javax.swing.JTextField portTextField;
    private javax.swing.JButton removeFile;
    private javax.swing.JPanel stagerSetupPanel;
    // End of variables declaration//GEN-END:variables
    
    // ==========================================================================
    /**
     * 
     */
    public void clearTable() {
        theJarTable.clear();
    }
    
    
    // ==========================================================================
    /**
    * Selects the client jar.
    */
    private void selectJar() {

        File userSelectedFile = null;

        int returnVal = theJarChooser.showDialog( this, "Select JAR File" ); //Show the dialogue
        switch( returnVal ) {

           case JFileChooser.CANCEL_OPTION: //If the user canceled the selecting...
              break;
           case JFileChooser.ERROR_OPTION: //If the dialogue was dismissed or an error occurred...
              break; //Do nothing

           case JFileChooser.APPROVE_OPTION: //If the user approved the selection...
              userSelectedFile = theJarChooser.getSelectedFile(); //Get the files the user selected
              break;
           default:
              break;

        }

        //Check if the returned file is valid
        if(userSelectedFile == null  || userSelectedFile.isDirectory() || !userSelectedFile.canRead())
            return;

        //Create the java item
        String[] aStrArr;
        try {
            aStrArr = Utilities.getJavaItem(userSelectedFile );
        } catch (JarItemException ex) {
            JOptionPane.showMessageDialog( this, ex.getMessage(),"Error", JOptionPane.ERROR_MESSAGE );           
            return;
        }

        if( aStrArr != null ){

            String selJarName = aStrArr[0];
            String selJarType = aStrArr[1];
            String selJarVersion = aStrArr[2];
            String selJarJvmVersion = aStrArr[3];

            //See if the table already contains the entry
            DefaultTableModel theModel = (DefaultTableModel) theJarTable.getModel();
            int rowToDelete = -1;
            for( int i =0; i < theModel.getRowCount(); i++ ){

                //Get table entries
                String jarName = (String) theJarTable.getValueAt(i, 0);
                String jarType = (String) theJarTable.getValueAt(i, 1);
                String jvmVersion = (String) theJarTable.getValueAt(i, 2);
                String jarVersion = (String) theJarTable.getValueAt(i, 3);

                //Check if the jvm version is the same first
                if( jvmVersion.equals(selJarJvmVersion) && 
                        jarType.equals( selJarType) ){

                    //Only one Stager and Payload are allowed
                    if( jarType.equals( Constants.STAGER_TYPE) || jarType.equals( Constants.PAYLOAD_TYPE)){
                        rowToDelete = i;
                        break;
                    //Check if one with the same name exists
                    } else if( jarName.equals( selJarName )) {
                        rowToDelete = i;
                        break;
                    }

                }

            }

            //If a similar library already exist
            if( rowToDelete != -1 ){                        
                String theMessage = new StringBuilder("Would you like to replace the existing ")
                        .append( selJarType ).append(" named \"")
                        .append( selJarName ).append("\" versioned \"")
                        .append( selJarVersion ).append("\"?").toString();
                int dialogValue = JOptionPane.showConfirmDialog(this, theMessage, "Replace JAR Library?", JOptionPane.YES_NO_OPTION);

                //Add the JAR to utilities
                if ( dialogValue == JOptionPane.YES_OPTION ){
                    deleteJarItem(rowToDelete);                    
                } else {
                    return;
                }
            }   

            //Queue the file to be sent
            getListener().sendJarFile( userSelectedFile, selJarType);

        }
    }

    //========================================================================
    /**
     * Delete the file
     * 
     * @param anInt 
     */
    @Override
    public void deleteJarItem(int anInt ) {
        
        //Get the model and remove the item
        DefaultTableModel theTableModel = (DefaultTableModel) theJarTable.getModel();    
        String jarName = (String) theTableModel.getValueAt(anInt, 0);
        String jarType = (String) theTableModel.getValueAt(anInt, 1);
        String jvmVersion = (String) theTableModel.getValueAt(anInt, 2);
        String jarVersion = (String) theTableModel.getValueAt(anInt, 3);
                          
        getListener().deleteJarItem(jarName, jarType, jvmVersion, jarVersion);        
    }
    
    //========================================================================
    /**
     * Delete jar item entry
     * 
     * @param jarName
     * @param jarType
     * @param jvmVersion
     * @param jarVersion
     */
    public synchronized void deleteJarItemFromTable(String jarName, String jarType, String jvmVersion, String jarVersion ) {
        
        //Get the model and remove the item
        DefaultTableModel theTableModel = (DefaultTableModel) theJarTable.getModel();   
        int rowCount = theTableModel.getRowCount();
        for( int i = 0; i < rowCount; i++ ){
            if( jarName.equals((String) theTableModel.getValueAt(i, 0)) &&
                jarType.equals((String) theTableModel.getValueAt(i, 1)) &&
                jvmVersion.equals((String) theTableModel.getValueAt(i, 2)) &&
                jarVersion.equals((String) theTableModel.getValueAt(i, 3))){
                
                //Remove from table
                theTableModel.removeRow(i);
                break;
            }
        }     
    }

    //========================================================================
    /**
     * 
     * @param theJarName
     * @param theJarType
     * @param theJvmVersion
     * @param theJarVersion 
     */
    public synchronized void addJarItem(String theJarName, String theJarType, String theJvmVersion, String theJarVersion) {
        DefaultTableModel theModel = (DefaultTableModel) theJarTable.getModel();
        theModel.addRow( new Object[]{ theJarName, theJarType, theJvmVersion, theJarVersion });
    }

    //========================================================================
    /**
     * 
     */
    @Override
    public void saveChanges() {
    }

    //========================================================================
    /**
     * 
     * @return 
     */
    @Override
    public JarLibraryPanelListener getListener() {
        return (JarLibraryPanelListener)super.getListener();
    }

    
}
