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
 * AdvancedlOptionsPanel.java
 *
 * Created on June 23, 2013, 10:21:49 AM
 */

package pwnbrew.gui.panels.options;

import pwnbrew.gui.panels.PanelListener;
import java.awt.event.ActionEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;
import javax.swing.JFileChooser;
import javax.swing.JTextPane;
import pwnbrew.logging.Log;
import pwnbrew.logging.LoggableException;
import pwnbrew.misc.Constants;
import pwnbrew.misc.FileFilterImp;
import pwnbrew.xmlBase.ServerConfig;

/**
 *
 */
public class InterfaceOptionsPanel extends OptionsJPanel {

//    private final PanelListener theListener;
    private ActionEvent lastActionEvt = new ActionEvent(new JTextPane(), 0, "");
//    private volatile boolean dirtyFlag = false;
    private ServerConfig theConf = null;
    
    private JFileChooser theClientJarChooser = null;
    private final FileFilterImp theJarFilter = new FileFilterImp();
    private static final String JAR_EXT = "jar";

    private static final String NAME_Class = InterfaceOptionsPanel.class.getSimpleName();

    //===================================================================
    /** Creates new form AdvancedlOptionsPanel
     * @param parent
     * @throws pwnbrew.logging.LoggableException */
    public InterfaceOptionsPanel( PanelListener parent ) throws LoggableException {
        super( "General", parent );
        initComponents();
        initializeComponents();
    }

    //===================================================================
    /**
     * Initialize components
    */
    private void initializeComponents() throws LoggableException{

        theConf = ServerConfig.getServerConfig();

        //Populate the componenets
        if(theConf != null){
            editScriptCheckbox.setSelected(theConf.showEditButton());
        }

        editScriptCheckbox.setIconTextGap(8);
        editScriptCheckbox.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editScriptJCheckboxActionPerformed(evt);
            }
        });

        //Create a JFileChooser to select wim files...
        theJarFilter.addExt( JAR_EXT);
        theClientJarChooser = new JFileChooser();
        theClientJarChooser.setMultiSelectionEnabled(false);
        theClientJarChooser.setFileFilter(theJarFilter);

        String theVersion = getVersion(Constants.PAYLOAD_PATH);
        if( theVersion != null && !theVersion.isEmpty() ){
            versionValue.setText( theVersion);
        }

    }

    //****************************************************************************
    /**
     * Handler for when the checkbox is changed
    */
    private void editScriptJCheckboxActionPerformed(ActionEvent evt) {

       if(lastActionEvt != evt ){
          setSaveButton(true);
       }
       lastActionEvt = evt;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        editScriptCheckbox = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();
        versionLabel = new javax.swing.JLabel();
        versionValue = new javax.swing.JLabel();
        updateButton = new javax.swing.JButton();

        editScriptCheckbox.setText("Allow Modification");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Staging"));

        versionLabel.setText("Payload Version:");

        versionValue.setText("N/A");

        updateButton.setText("Update");
        updateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addComponent(versionLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 112, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(versionValue, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(updateButton)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(versionLabel)
                    .addComponent(versionValue)
                    .addComponent(updateButton))
                .addContainerGap(19, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(editScriptCheckbox)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(editScriptCheckbox)
                .addGap(18, 18, 18)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(97, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void updateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateButtonActionPerformed
        selectClientJar();        
    }//GEN-LAST:event_updateButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox editScriptCheckbox;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton updateButton;
    private javax.swing.JLabel versionLabel;
    private javax.swing.JLabel versionValue;
    // End of variables declaration//GEN-END:variables

//    
//    //****************************************************************************
//    /**
//     * Saves any changes that have been performed
//     * @return 
//    */
//    public boolean isDirty() {
//       return dirtyFlag;
//    }

//    /**
//    * Sets the save button enablement
//     * @param passedBool
//    */
//    public void setSaveButton(boolean passedBool){
//       if(!dirtyFlag){
//          dirtyFlag = true;
//          theListener.valueChanged(passedBool);
//       }
//    }

    //===================================================================
    /**
     * Saves any changes that have been performed
    */
    @Override
    public void saveChanges(){

        //Reset the dirty flag
        setDirtyFlag(false);

        //Write the changes to disk
        try {
            
            if(theConf != null){

                theConf.setShowEditButton(editScriptCheckbox.isSelected());
                theConf.writeSelfToDisk();
              
            }

        } catch (LoggableException ex){
           Log.log(Level.SEVERE, NAME_Class, "saveChanges()", ex.getMessage(), ex);
        }

    }
    
    // ==========================================================================
    /**
    * Selects the client jar.
    */
    private void selectClientJar() {

        File userSelectedFile = null;

        int returnVal = theClientJarChooser.showDialog( this, "Select Client JAR File" ); //Show the dialogue
        switch( returnVal ) {

           case JFileChooser.CANCEL_OPTION: //If the user canceled the selecting...
              break;
           case JFileChooser.ERROR_OPTION: //If the dialogue was dismissed or an error occurred...
              break; //Do nothing

           case JFileChooser.APPROVE_OPTION: //If the user approved the selection...
              userSelectedFile = theClientJarChooser.getSelectedFile(); //Get the files the user selected
              break;
           default:
              break;

        }

        //Check if the returned file is valid
        if(userSelectedFile == null  || userSelectedFile.isDirectory()){
           return;
        }

        String userPath = userSelectedFile.getAbsolutePath();
        try {
            Files.copy(Paths.get(userPath), Constants.PAYLOAD_PATH, StandardCopyOption.REPLACE_EXISTING);
            String theVersion = getVersion(Constants.PAYLOAD_PATH);
            versionValue.setText( theVersion );
            setSaveButton(true);
        } catch (IOException ex) {
           Log.log(Level.SEVERE, NAME_Class, "saveChanges()", ex.getMessage(), ex);
        }
        
    }/* END selectKeystorePath() */
    
    //==========================================================================
    /**
     *  Get version
     * 
     * @param payloadPath
     * @return 
     */
    public String getVersion( Path payloadPath ){
        
        //Remove JAR if that's how we are running
        File payloadFile = payloadPath.toFile();
        String retString = "";
        if( payloadFile != null && payloadFile.getPath().endsWith(".jar")){ 

            try { 
                
                //Open the zip
                ByteArrayOutputStream aBOS = new ByteArrayOutputStream();
                ByteArrayInputStream aBIS;
                try{
                    
                    FileInputStream fis = new FileInputStream(payloadFile);
                    try{

                        //Read into the buffer
                        byte[] buf = new byte[1024];                
                        for (int readNum; (readNum = fis.read(buf)) != -1;) {
                            aBOS.write(buf, 0, readNum);
                        }

                    } finally{

                        try {
                            //Close and delete
                            fis.close();
                        } catch (IOException ex) {                        
                        }
                    }

                    //Creat an inputstream
                    aBIS = new ByteArrayInputStream(aBOS.toByteArray());    
                
                } finally {
                    try {
                        aBOS.close();
                    } catch (IOException ex) {
                    }
                }

                //Open the zip input stream
                ZipInputStream theZipInputStream = new ZipInputStream(aBIS);
                ZipEntry anEntry;
                try {
                    
                    while((anEntry = theZipInputStream.getNextEntry())!=null){
                        //Get the entry name
                        String theEntryName = anEntry.getName();

                        //Change the properties file
                        if( theEntryName.equals( Constants.PAYLOAD_PROPERTIES_FILE ) ){

                            //Get the input stream and modify the value
                            Properties localProperties = new Properties();
                            localProperties.load(theZipInputStream);

                            //Set the IP to something else
                            String version = localProperties.getProperty(Constants.PAYLOAD_VERSION_LABEL);
                            if( version != null ){

                                //Add the entry
                                retString = version;
                                break;
                            }     
                            continue;
                        } 

                    }

                //Close the jar
                } finally {
                    
                    try {
                        theZipInputStream.close();
                    } catch (IOException ex) {
                    }
                    
                }
                
            } catch (ZipException ex) {  
                Log.log(Level.SEVERE, NAME_Class, "saveChanges()", ex.getMessage(), ex);
            } catch (IOException ex) {        
                Log.log(Level.SEVERE, NAME_Class, "saveChanges()", ex.getMessage(), ex);
            } 
        }
        return retString;
    }
    
}
