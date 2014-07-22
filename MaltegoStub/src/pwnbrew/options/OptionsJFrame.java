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

package pwnbrew.options;

import java.awt.Cursor;
import java.awt.Image;
import java.awt.event.WindowEvent;
import java.io.File;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import pwnbrew.misc.Constants;
import pwnbrew.misc.Utilities;
import pwnbrew.options.panels.JarLibraryPanel;
import pwnbrew.options.panels.JarLibraryPanelListener;
import pwnbrew.options.panels.NetworkOptionsPanel;
import pwnbrew.options.panels.NetworkPanelListener;

/**
 *
 *  
 */
public class OptionsJFrame extends JFrame implements JarLibraryPanelListener, NetworkPanelListener {

    private static final String NETWORK_PANEL_TITLE = "Network";
    private static final String LIBRARY_PANEL_TITLE = "JAR Library";
    
    private JarLibraryPanel theJarImportPanel = null;
    private NetworkOptionsPanel theNetworkOptionsPanel = null;
    
    private final OptionsJFrameListener theListener;
 
    //=======================================================================
    /** 
     * Creates new form OptionsJDialog
     * @param passedListener 
     */
    public OptionsJFrame( OptionsJFrameListener passedListener )  {
       super();
       
       theListener = passedListener;
       theJarImportPanel = new JarLibraryPanel(LIBRARY_PANEL_TITLE, this);
       theNetworkOptionsPanel = new NetworkOptionsPanel(NETWORK_PANEL_TITLE, this);
     
       initComponents();
       initializeComponents();
       setLocationRelativeTo(null);

    }//End Constructor

     //=======================================================================
    /**
     * Initializes all the components
    */
    private void initializeComponents(){
    
        //Add the extension panels
        //Close the jar panel
        if( theJarImportPanel != null )
            optionsTabbedPane.addTab(theJarImportPanel.getName(), theJarImportPanel);
        
        //Close the jar panel
        if( theNetworkOptionsPanel != null )
            optionsTabbedPane.addTab(theNetworkOptionsPanel.getName(), theNetworkOptionsPanel);

        pack();

        //Add the change listener
        ChangeListener changeListener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                JTabbedPane sourceTabbedPane = (JTabbedPane) changeEvent.getSource();
                int index = sourceTabbedPane.getSelectedIndex();
                String theTitle = sourceTabbedPane.getTitleAt(index);
                
                //Handle the change
                if( theTitle.equals( LIBRARY_PANEL_TITLE )){
                    theJarImportPanel.clearTable();
                    theListener.getJarItems();
                } else if(theTitle.equals( NETWORK_PANEL_TITLE )){
                    
                }
                
            }
        };
        optionsTabbedPane.addChangeListener(changeListener);
        
        //Set the icon
        Image appIcon = Utilities.loadImageFromJar( Constants.OPTIONS_IMG_STR );
        if( appIcon != null )
            setIconImage( appIcon );
        
    }

    /**
    * Sets the save button enablement
     * @param passedBool
    */
    public void setSaveButton(boolean passedBool){
        if(passedBool)
            saveOrOkJButton.setText("Save");
        else
            saveOrOkJButton.setText("OK");
        
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
   @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        cancelJButton = new javax.swing.JButton();
        saveOrOkJButton = new javax.swing.JButton();
        optionsTabbedPane = new javax.swing.JTabbedPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Options");
        setName("Options"); // NOI18N

        cancelJButton.setText("Cancel");
        cancelJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelJButtonActionPerformed(evt);
            }
        });

        saveOrOkJButton.setText("OK");
        saveOrOkJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveOrOkJButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(optionsTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 575, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(saveOrOkJButton, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(2, 2, 2)
                        .addComponent(cancelJButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(optionsTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 478, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(saveOrOkJButton)
                    .addComponent(cancelJButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

   private void cancelJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelJButtonActionPerformed
       closeDialog();
   }//GEN-LAST:event_cancelJButtonActionPerformed

   private void saveOrOkJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveOrOkJButtonActionPerformed

        String buttonText = saveOrOkJButton.getText();

        if(buttonText.equals("Save")){
            
            setCursor( Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR ) );
            try {

                //Save the changes
                if( theJarImportPanel != null )
                    theJarImportPanel.saveChanges();

                //Save the changes
                if( theNetworkOptionsPanel != null )
                    theNetworkOptionsPanel.saveChanges();                

                setSaveButton(false);
                
            } finally {
                setCursor(null);
            }

        } else
            closeDialog();
        
   }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelJButton;
    private javax.swing.JTabbedPane optionsTabbedPane;
    private javax.swing.JButton saveOrOkJButton;
    // End of variables declaration//GEN-END:variables

    //========================================================================
    /**
     * 
     * @return 
     */
    public JarLibraryPanel getJarLibraryPanel() {
        return theJarImportPanel;
    }

    //========================================================================
    /**
     * 
     * @param passedBool 
     */
    @Override
    public void valueChanged(boolean passedBool) {
        setSaveButton(passedBool);
    }

    //========================================================================
    /**
     * 
     * @param jarName
     * @param jarType
     * @param jvmVersion
     * @param jarVersion 
     */
    @Override
    public void deleteJarItem(String jarName, String jarType, String jvmVersion, String jarVersion) {
        theListener.sendDeleteJarItemMsg(jarName, jarType, jvmVersion, jarVersion);
    }

    //========================================================================
    /**
     * 
     * @param userSelectedFile
     * @param selVal 
     */
    @Override
    public void sendJarFile(File userSelectedFile, String selVal) {
        theListener.sendJarFile(userSelectedFile, selVal);
    }

    //========================================================================
    /**
     * 
     * @return 
     */
    public NetworkOptionsPanel getNetworkSettingsPanel() {
        return theNetworkOptionsPanel;
    }

    //========================================================================
    /**
     * 
     * @param sueeDN
     * @param suerDN
     * @param days 
     */
    @Override
    public void sendCertInfo( int serverPort, String sueeDN, String suerDN, int days) {
        theListener.sendCertInfo(serverPort, sueeDN, suerDN, days);
    }
    
    //========================================================================
    /**
     *  Internal class to limit the length of the param delim fields
     */
    public static class JTextFieldLimit extends PlainDocument {

       private final int limit;

       JTextFieldLimit( int passedLimit){
           limit = passedLimit;
       }

       @Override
       public void insertString(int offset, String passedStr, AttributeSet attr) throws BadLocationException{
           if(passedStr != null && getLength() + passedStr.length() <= limit)
                 super.insertString(offset, passedStr, attr);             
          
       }

    }
    
    // ==========================================================================
    /**
    * Processes {@link WindowEvent}s occurring on this component.
    * <p>
    * This method is overridden to handle unsaved changes when the window is closed
    * using the X(exit) button and give the user the option of cancelling the close.
    *
    * @param event the {@code WindowEvent}
    */
    @Override //Overrides JFrame.processWindowEvent( WindowEvent )
    protected void processWindowEvent( WindowEvent event ) {

        if( WindowEvent.WINDOW_CLOSING == event.getID() )
            closeDialog();            
        else
            super.processWindowEvent( event );

    }
    
    // ==========================================================================
    /**
    *   Handles the logic necessary before the dialog is closed
    */
    private void closeDialog() {
        
        //Close the jar panel
        if( theJarImportPanel != null )
            theJarImportPanel.doClose();
        
        //Close the jar panel
        if( theNetworkOptionsPanel != null )
            theNetworkOptionsPanel.doClose();
              
        dispose();
        theListener.beNotified();

    }
    
    

}
