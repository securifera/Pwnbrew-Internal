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

package pwnbrew.generic.gui.wizard;

import java.awt.CardLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JPanel;

public class Wizard extends JPanel implements PropertyChangeListener {

    protected WizardController theController = null;
    protected WizardModel theWizardModel = null;
    protected final JDialog theParent;

    protected CardLayout theCardLayout = new CardLayout();

    public static final int FINISH_RETURN_CODE = 0;
    public static final int CANCEL_RETURN_CODE = 1;
    public static final int ERROR_RETURN_CODE = 2;

    private int returnCode;

    //=======================================================================
    /** 
     * Constructor
     * 
     * @param parentFrame 
     * @param passedController 
     */
    public Wizard( JDialog parentFrame, WizardController passedController){

        theWizardModel = new WizardModel();
        theController = passedController;
        theParent = parentFrame;
        
        initComponents();
        initializeComponents();
    }

    //=======================================================================
    /**
     * Returns the current model of the wizard dialog.
     * @return A WizardModel instance, which serves as the model for the wizard dialog.
     */
    public WizardModel getModel() {
        return theWizardModel;
    }
    
    //=======================================================================
    /**
     * Returns the parent frame.
     * @return 
    */
    public JDialog getParentDialog() {
        return theParent;
    }

    //=======================================================================
    /**
     * Returns the current controller of the wizard dialog.
     * @return A WizardController instance, which serves as the model for the wizard dialog.
    */
    public WizardController getController() {
        return theController;
    }

    //=======================================================================
    /**
     * Displays the panel identified by the object passed in. This is the same Object-based
     * identified used when registering the panel.
     * @param panelId The Object-based identifier of the panel to be displayed.
     */
    public void setCurrentPanel(String panelId) {

        //  Get the hashtable reference to the panel that should
        //  be displayed. If the identifier passed in is null, then close
        //  the dialog.

        if (panelId == null)
            close(ERROR_RETURN_CODE);
        else {
            WizardPanelDescriptor oldPanelDescriptor = theWizardModel.getCurrentPanelDescriptor();
            if (oldPanelDescriptor != null)
                oldPanelDescriptor.aboutToHidePanel();

            theWizardModel.setCurrentPanel(panelId);
            theWizardModel.getCurrentPanelDescriptor().aboutToDisplayPanel();

            //  Show the panel in the dialog.
            theCardLayout.show( cardPanel, panelId );
            theWizardModel.getCurrentPanelDescriptor().displayingPanel();
        }

    }

    //=======================================================================
    /**
     * Method used to listen for property change events from the model and update the
     * dialog's graphical components as necessary.
     * @param evt PropertyChangeEvent passed from the model to signal that one of its properties has changed value.
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {

        switch (evt.getPropertyName()) {
            case WizardModel.CURRENT_PANEL_DESCRIPTOR_PROPERTY:
                theController.resetButtonsToPanelRules();
                break;
            case WizardModel.NEXT_FINISH_BUTTON_TEXT_PROPERTY:
                nextButton.setText(evt.getNewValue().toString());
                break;
            case WizardModel.BACK_BUTTON_TEXT_PROPERTY:
                backButton.setText(evt.getNewValue().toString());
                break;
            case WizardModel.CANCEL_BUTTON_TEXT_PROPERTY:
                cancelButton.setText(evt.getNewValue().toString());
                break;
            case WizardModel.NEXT_FINISH_BUTTON_ENABLED_PROPERTY:
                nextButton.setEnabled(((Boolean)evt.getNewValue()));
                break;
            case WizardModel.BACK_BUTTON_ENABLED_PROPERTY:
                backButton.setEnabled(((Boolean)evt.getNewValue()));
                break;
            case WizardModel.CANCEL_BUTTON_ENABLED_PROPERTY:
                cancelButton.setEnabled(((Boolean)evt.getNewValue()));
                break;
            case WizardModel.NEXT_FINISH_BUTTON_ICON_PROPERTY:
                nextButton.setIcon((Icon)evt.getNewValue());
                break;
            case WizardModel.BACK_BUTTON_ICON_PROPERTY:
                backButton.setIcon((Icon)evt.getNewValue());
                break;
            case WizardModel.CANCEL_BUTTON_ICON_PROPERTY:
                cancelButton.setIcon((Icon)evt.getNewValue());
                break;
        }

    }

    //=======================================================================
    /**
     * Retrieves the last return code set by the dialog.
     * @return An integer that identifies how the dialog was closed. See the *_RETURN_CODE
     * constants of this class for possible values.
     */
    public int getReturnCode() {
        return returnCode;
    }

    //=======================================================================
   /**
     * Mirrors the WizardModel method of the same name.
     * @return A boolean indicating if the button is enabled.
     */
    public boolean getBackButtonEnabled() {
        return theWizardModel.getBackButtonEnabled();
    }

    //=======================================================================
   /**
     * Mirrors the WizardModel method of the same name.
     * @param newValue
     */
    public void setBackButtonEnabled(boolean newValue) {
        theWizardModel.setBackButtonEnabled(newValue);
    }

    //=======================================================================
   /**
     * Mirrors the WizardModel method of the same name.
     * @return A boolean indicating if the button is enabled.
     */
    public boolean getNextFinishButtonEnabled() {
        return theWizardModel.getNextFinishButtonEnabled();
    }

    //=======================================================================
   /**
     * Mirrors the WizardModel method of the same name.
     * @param newValue
     */
    public void setNextFinishButtonEnabled(boolean newValue) {
        theWizardModel.setNextFinishButtonEnabled(newValue);
    }

    //=======================================================================
   /**
     * Mirrors the WizardModel method of the same name.
     * @return A boolean indicating if the button is enabled.
     */
    public boolean getCancelButtonEnabled() {
        return theWizardModel.getCancelButtonEnabled();
    }

    //=======================================================================
    /**
     * Mirrors the WizardModel method of the same name.
     * @param newValue
     */
    public void setCancelButtonEnabled(boolean newValue) {
        theWizardModel.setCancelButtonEnabled(newValue);
    }

    //=======================================================================
    /**
     * Closes the dialog and sets the return code to the integer parameter.
     * @param code The return code.
     */
    public void close(int code) {
        returnCode = code;
    }

    //=======================================================================
    /**
     * Add a Component as a panel for the wizard dialog by registering its
     * WizardPanelDescriptor object. Each panel is identified by a unique Object-based
     * identifier (often a String), which can be used by the setCurrentPanel()
     * method to display the panel at runtime.
     * @param id An Object-based identifier used to identify the WizardPanelDescriptor object.
     * @param passedDescriptor
     */
    public void registerWizardPanel( String id, WizardPanelDescriptor passedDescriptor ) {

        //Add the panel to the card panel and register it with the model
        cardPanel.add(passedDescriptor.getPanelComponent(), id);
        passedDescriptor.setWizard(this);
        theWizardModel.registerPanel(id, passedDescriptor);

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonPanel = new javax.swing.JPanel();
        cancelButton = new javax.swing.JButton();
        nextButton = new javax.swing.JButton();
        backButton = new javax.swing.JButton();
        aSeparator = new javax.swing.JSeparator();
        cardPanel = new javax.swing.JPanel();

        cancelButton.setText("Cancel");
        cancelButton.setMaximumSize(new java.awt.Dimension(66, 23));
        cancelButton.setMinimumSize(new java.awt.Dimension(66, 23));
        cancelButton.setPreferredSize(new java.awt.Dimension(66, 23));

        nextButton.setText("Next >");
        nextButton.setMaximumSize(new java.awt.Dimension(66, 23));
        nextButton.setMinimumSize(new java.awt.Dimension(66, 23));
        nextButton.setPreferredSize(new java.awt.Dimension(66, 23));

        backButton.setText("< Back");
        backButton.setMaximumSize(new java.awt.Dimension(66, 23));
        backButton.setMinimumSize(new java.awt.Dimension(66, 23));
        backButton.setPreferredSize(new java.awt.Dimension(66, 23));
        backButton.setRolloverEnabled(false);

        javax.swing.GroupLayout buttonPanelLayout = new javax.swing.GroupLayout(buttonPanel);
        buttonPanel.setLayout(buttonPanelLayout);
        buttonPanelLayout.setHorizontalGroup(
            buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(buttonPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, buttonPanelLayout.createSequentialGroup()
                        .addComponent(backButton, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(nextButton, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(36, 36, 36)
                        .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(aSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 420, Short.MAX_VALUE))
                .addContainerGap())
        );
        buttonPanelLayout.setVerticalGroup(
            buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, buttonPanelLayout.createSequentialGroup()
                .addComponent(aSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 5, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nextButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(backButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        cardPanel.setLayout(null);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(buttonPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(cardPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(cardPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 369, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSeparator aSeparator;
    private javax.swing.JButton backButton;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel cardPanel;
    private javax.swing.JButton nextButton;
    // End of variables declaration//GEN-END:variables

    private void initializeComponents() {
        cardPanel.setLayout(theCardLayout);
        theController.setWizard(this);
        theWizardModel.addPropertyChangeListener(this);
     
        backButton.addActionListener(theController);
        nextButton.addActionListener(theController);
        cancelButton.addActionListener(theController);
    }

}
