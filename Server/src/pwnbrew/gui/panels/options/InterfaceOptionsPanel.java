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

import java.awt.Color;
import pwnbrew.gui.panels.PanelListener;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import static pwnbrew.Environment.addClassToMap;
import pwnbrew.generic.gui.GenericProgressDialog;
import pwnbrew.logging.LoggableException;
import pwnbrew.misc.Constants;
import pwnbrew.misc.Directories;
import pwnbrew.misc.FileFilterImp;
import pwnbrew.misc.ProgressDriver;
import pwnbrew.misc.ProgressListener;
import pwnbrew.utilities.FileUtilities;
import pwnbrew.utilities.GuiUtilities;
import pwnbrew.utilities.Utilities;
import pwnbrew.xmlBase.JarItem;

/**
 *
 */
public class InterfaceOptionsPanel extends OptionsJPanel implements ProgressDriver, JarTableListener {

    private JFileChooser theJarChooser = null;    
    private final FileFilterImp theJarFilter = new FileFilterImp();
    private static final String JAR_EXT = "jar";
    private JarTable theJarTable;
    
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

        //Create a JFileChooser to select wim files...
        theJarFilter.addExt( JAR_EXT);
        theJarChooser = new JFileChooser();
        theJarChooser.setMultiSelectionEnabled(false);
        theJarChooser.setFileFilter(theJarFilter);
            
        
        GuiUtilities.setComponentIcon(addFile,  15, 15, Constants.ADD_IMAGE_STR);
        GuiUtilities.setComponentIcon(removeFile, 15, 15, Constants.DELETE_IMG_STR);
        
        //Create a file table
        theJarTable = new JarTable( this );
        jarScrollPane.setViewportView(theJarTable);
        jarScrollPane.getViewport().setBackground(Color.WHITE);
        
        List<JarItem> jarList = new ArrayList<>();
        jarList.addAll( Utilities.getJarItems());
        
        //Get the table model
        DefaultTableModel theModel = (DefaultTableModel) theJarTable.getModel();
        for( JarItem anItem : jarList )
            theModel.addRow( new Object[]{ anItem, anItem.getType(), 
                anItem.getJvmMajorVersion(), anItem.getVersion() });
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        removeFile = new javax.swing.JButton();
        addFile = new javax.swing.JButton();
        jarScrollPane = new javax.swing.JScrollPane();

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
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 209, Short.MAX_VALUE)
                        .addComponent(addFile)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeFile)
                        .addGap(0, 209, Short.MAX_VALUE))
                    .addComponent(jarScrollPane))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(62, 62, 62)
                .addComponent(jarScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 224, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addFile)
                    .addComponent(removeFile))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addFile;
    private javax.swing.JScrollPane jarScrollPane;
    private javax.swing.JButton removeFile;
    // End of variables declaration//GEN-END:variables

    //===================================================================
    /**
     * Saves any changes that have been performed
    */
    @Override
    public void saveChanges(){
        //Reset the dirty flag
        setDirtyFlag(false);
    }
    
    // ==========================================================================
    /**
    * Selects the client jar.
    */
    private void selectJar() {
        
        //Have the user manually put in the server ip
        JComboBox aCB = new JComboBox();
        aCB.setRenderer(new pwnbrew.generic.gui.DefaultCellBorderRenderer(BorderFactory.createEmptyBorder(0, 4, 0, 0)));
        List<String> jarTypes = JarItem.getTypes();
        for( String aJarType : jarTypes )
            aCB.addItem(aJarType);
        
        Object[] objMsg = new Object[]{ "Please select the type of JAR being imported.", " ", aCB};
        Object retVal = JOptionPane.showOptionDialog(null, objMsg, "Select JAR type",
               JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);

        //Check that they clicked ok
        if((Integer)retVal == JOptionPane.OK_OPTION ) {
            
            String selVal = (String) aCB.getSelectedItem();
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
            if(userSelectedFile == null  || userSelectedFile.isDirectory()){
               return;
            }

            //Create a list out of the array
            List theObjList = new ArrayList();
            theObjList.add(userSelectedFile);
            theObjList.add(selVal);

            GenericProgressDialog pDialog = new GenericProgressDialog(null, "Importing files to library...", this, false, theObjList);
            pDialog.setVisible(true);       
        } 
                
    }/* END selectJar() */

    //========================================================================
    /**
     * 
     * @param Listener
     * @param theObjList
     * @return 
     */
    @Override
    public String executeFunction(ProgressListener progressListener, List<?> theObjList) {
        
        String retVal = null;
        if( theObjList != null ) { //If the user selected any files...
            
            File aFile = (File)theObjList.get(0);
            String theType = (String)theObjList.get(1);                    
                    
            if( FileUtilities.verifyCanRead( aFile ) ) { //If the file the File represents can be read...

                try {
                    
                    //Create a FileContentRef
                    JarItem aJarItem = Utilities.getJavaItem(aFile);
                    aJarItem.setFilename( aFile.getName() ); //Set the file's name
                    
                    //Add the JAR to utilities
                    if ( Utilities.addJarItem( aJarItem ) ){
                        
                        //Write the file to disk
                        String fileHash = FileUtilities.createHashedFile( aFile, progressListener );
                        if( fileHash != null ) {

                            //Create a FileContentRef
                            aJarItem.setFileHash( fileHash ); //Set the file's hash
                            aJarItem.setType(theType);
                                                
                            //Write to disk
                            aJarItem.writeSelfToDisk();

                            DefaultTableModel theModel = (DefaultTableModel) theJarTable.getModel();
                            theModel.addRow( new Object[]{ aJarItem, 
                                    aJarItem.getType(), aJarItem.getJvmMajorVersion(), aJarItem.getVersion() });

                            //If it is a local extension then load it
                            if( aJarItem.getType().equals(JarItem.LOCAL_EXTENSION_TYPE)){

                                //Load the jar
                                File libraryFile = new File( Directories.getFileLibraryDirectory(), aJarItem.getFileHash() ); //Create a File to represent the library file to be copied
                                List<Class<?>> theClasses = Utilities.loadJar(libraryFile);
                                for( Class aClass : theClasses ){
                                    addClassToMap(aClass);
                                }
                            }
                            
                        } else {
                            
                            //Display a dialog box explaining the problem...
                            String errorMsg = "Unable to add the JAR to the library.  Please make sure there aren't any conflicts";                          
                            JOptionPane.showMessageDialog( this, errorMsg, "Import Failed", JOptionPane.ERROR_MESSAGE );
                            
                        }

                    }

                } catch ( NoSuchAlgorithmException | IOException ex) {
                    JOptionPane.showMessageDialog( this, ex.getMessage(), "Could not add the file to the task.", JOptionPane.ERROR_MESSAGE );
                    retVal = "Could not add the file to the task.";
                }

            } else { //If the file cannot be read...
               JOptionPane.showMessageDialog( this, new StringBuilder( "\tThis support file could not be read: \"" )
                        .append( aFile.getAbsolutePath() ).append( "\"" ).toString(), "Could not add the file to the task.", JOptionPane.ERROR_MESSAGE );
               retVal = "Could not add the file to the task.";
            }                
            
        }
        return retVal;
    }

    //========================================================================
    /**
     * Delete the file
     * 
     * @param aJarItem 
     */
    @Override
    public void deleteJarItem(int anInt ) {
        
        //Get the model and remove the item
        DefaultTableModel theTableModel = (DefaultTableModel) theJarTable.getModel();    
        JarItem aJarItem = (JarItem)theTableModel.getValueAt(anInt, 0);
        theTableModel.removeRow(anInt);
                    
        Utilities.removeJarItem(aJarItem);
        aJarItem.deleteSelfFromDirectory( new File( Directories.getJarLibPath() ));
        
    }
    
}
