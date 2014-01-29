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
 * OptionsJDialog.java
 *
 * Created on June 23, 2013, 2:12 PM
 */

package pwnbrew.gui.dialogs;

import pwnbrew.gui.panels.PanelListener;
import java.awt.event.WindowEvent;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import pwnbrew.gui.panels.options.OptionsJPanel;
import pwnbrew.logging.LoggableException;
import pwnbrew.utilities.Utilities;

/**
 *
 *  
 */
public class OptionsJDialog extends JDialog implements PanelListener {

    private final OptionsJDialogListener theListener;
    private final List<OptionsJPanel> theExtPanelList = new ArrayList<>();
 
    //=======================================================================
    /** Creates new form OptionsJDialog
     * @param parent
     * @param modal
     * @throws pwnbrew.logging.LoggableException */
    public OptionsJDialog( OptionsJDialogListener parent, boolean modal ) throws LoggableException {
       super( parent.getParentJFrame() , modal);
       
       theListener = parent;

       //Get the extension panels
       List<String> theClassList = Utilities.getExtPanelClassList();
       for( String aString : theClassList ){
           
           try {
               
               Class aClass = Class.forName(aString);
               Constructor aConstruct = aClass.getConstructor( PanelListener.class );
               Object anObj = aConstruct.newInstance( this );
               if( anObj instanceof OptionsJPanel ){
                   theExtPanelList.add((OptionsJPanel) anObj);
               }
               
           } catch ( ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
           }
           
       }
     
       initComponents();
       initializeComponents();
       setLocationRelativeTo(null);

    }//End Constructor

     //=======================================================================
    /**
     * Initializes all the components
    */
    private void initializeComponents(){
    
        //Setup tab pane
//        optionsTabbedPane.addTab("General", theInterfaceOptionsPanel);      
//        optionsTabbedPane.addTab("Network", theNetworkOptionsPanel);
        
        //Add the extension panels
        if( theExtPanelList != null ){
            for( OptionsJPanel aPanel : theExtPanelList ){
                optionsTabbedPane.addTab(aPanel.getName(), aPanel);
            }
        }
        

//        optionsTabbedPane.addChangeListener( new ChangeListener(){
//
//            @Override
//            public void stateChanged(ChangeEvent e) {
//                if(optionsTabbedPane.getSelectedIndex() == 1){
//                  theScriptingOptionsPanel.focusList();
//                }
//            }
//
//        });
    }

    /**
    * Sets the save button enablement
     * @param passedBool
    */
    public void setSaveButton(boolean passedBool){
       if(passedBool){
           saveOrOkJButton.setText("Save");
       } else {
           saveOrOkJButton.setText("OK");
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

        cancelJButton = new javax.swing.JButton();
        saveOrOkJButton = new javax.swing.JButton();
        optionsTabbedPane = new javax.swing.JTabbedPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Options");
        setModal(true);
        setName("Options"); // NOI18N
        setResizable(false);

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
                    .addComponent(optionsTabbedPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 482, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(saveOrOkJButton, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelJButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(optionsTabbedPane, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(saveOrOkJButton)
                    .addComponent(cancelJButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

   private void cancelJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelJButtonActionPerformed
       closeDialog();
   }//GEN-LAST:event_cancelJButtonActionPerformed

   private void saveOrOkJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveOrOkJButtonActionPerformed

        String buttonText = saveOrOkJButton.getText();

        if(buttonText.equals("Save")){
            
            //Add the extension panels
            if( theExtPanelList != null ){
                for( OptionsJPanel aPanel : theExtPanelList ){
                    if(aPanel.isDirty()){
                        aPanel.saveChanges();
                    }
                }
            }

//            //Save changes to the scripting options panel
//            if(theScriptingOptionsPanel.isDirty()){
//                theScriptingOptionsPanel.saveChanges();
//            }

//            //Save changes to the scripting options panel
//            if(theInterfaceOptionsPanel.isDirty()){
//                theInterfaceOptionsPanel.saveChanges();
//            }
//
//            //Save changes to the scripting options panel
//            if(theNetworkOptionsPanel.isDirty()){
//                theNetworkOptionsPanel.saveChanges();
//            }
         
            setSaveButton(false);

        } else {

            //Call close dialog
            closeDialog();
        }
   }//GEN-LAST:event_saveOrOkJButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelJButton;
    private javax.swing.JTabbedPane optionsTabbedPane;
    private javax.swing.JButton saveOrOkJButton;
    // End of variables declaration//GEN-END:variables

    //========================================================================
    /**
     * Get the listener
     * @return 
     */
    public OptionsJDialogListener getDialogListener() {
        return theListener;
    }

    
    // Internal class to limit the length of the param delim fields
    public static class JTextFieldLimit extends PlainDocument {

       private final int limit;

       JTextFieldLimit( int passedLimit){
           limit = passedLimit;
       }

       @Override
       public void insertString(int offset, String passedStr, AttributeSet attr) throws BadLocationException{
          if(passedStr != null){
             if(getLength() + passedStr.length() <= limit){
                super.insertString(offset, passedStr, attr);
             }
          }
       }

    }

    //=============================================================
    /**
    * Notifies the dialog that something has changed on one of the panels in the
     * tabbed pane.
     * @param passedBool
    */
    @Override
    public void valueChanged(boolean passedBool) {
       setSaveButton(passedBool);
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

        if( WindowEvent.WINDOW_CLOSING == event.getID() ) { //If the event is the window closing...
            closeDialog();            
        } else { //If the event is not the window closing...
            super.processWindowEvent( event ); //Proceed normally
        }

    }/* END processWindowEvent( WindowEvent ) */
    
    // ==========================================================================
    /**
    *   Handles the logic necessary before the dialog is closed
    */
    private void closeDialog() {
        
        //Add the extension panels
        if( theExtPanelList != null ){
            for( OptionsJPanel aPanel : theExtPanelList ){
                aPanel.doClose();
            }
        }
        
        dispose();

    }

}
