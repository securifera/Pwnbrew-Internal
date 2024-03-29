/*

Copyright (C) 2013-2016, Securifera, Inc 

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

The copyright on this package is held by Securifera, Inc

*/

/*
 * SettingsJDialog.java
 *
 */

package pwnbrew.shell;

import java.awt.Cursor;
import java.awt.event.WindowEvent;
import javax.swing.JDialog;

/**
 *
 *  
 */
public class ShellSettingsJDialog extends JDialog {
    
    private final ShellJPanelListener theListener;
    private static final String NAME_Class = ShellSettingsJDialog.class.getSimpleName();
 
    //=======================================================================
    /** Creates new form SettingsJDialog
     * @param parent
     * @param modal */
    public ShellSettingsJDialog( ShellJPanelListener parent, boolean modal ) {
       super( parent.getParentJFrame() , modal);
       
       theListener = parent;
     
       initComponents();
       initializeComponents();
       setLocationRelativeTo(null);

    }//End Constructor

     //=======================================================================
    /**
     * Initializes all the components
    */
    private void initializeComponents(){
                        
        ShellSettings theSettings = theListener.getShellSettings();
        String curDir = theSettings.getCurrentDir();
        if( curDir != null ){
            curDirField.setText(curDir);
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

        encodingButtonGroup = new javax.swing.ButtonGroup();
        cancelJButton = new javax.swing.JButton();
        saveOrOkJButton = new javax.swing.JButton();
        curDirLabel = new javax.swing.JLabel();
        curDirField = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Settings");
        setModal(true);
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

        curDirLabel.setText("Current Directory:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(saveOrOkJButton, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(2, 2, 2)
                        .addComponent(cancelJButton))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(24, 24, 24)
                        .addComponent(curDirLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(curDirField, javax.swing.GroupLayout.DEFAULT_SIZE, 363, Short.MAX_VALUE)
                        .addGap(28, 28, 28)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(curDirLabel)
                    .addComponent(curDirField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 145, Short.MAX_VALUE)
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

        ShellSettings theSettings = theListener.getShellSettings();
        String curDir = curDirField.getText();
        theSettings.setCurrentDir(curDir);
        closeDialog();        
   }//GEN-LAST:event_saveOrOkJButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelJButton;
    private javax.swing.JTextField curDirField;
    private javax.swing.JLabel curDirLabel;
    private javax.swing.ButtonGroup encodingButtonGroup;
    private javax.swing.JButton saveOrOkJButton;
    // End of variables declaration//GEN-END:variables

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
                
        dispose();

    }

}
