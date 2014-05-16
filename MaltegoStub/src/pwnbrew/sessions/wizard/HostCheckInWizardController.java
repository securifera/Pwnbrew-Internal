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
package pwnbrew.sessions.wizard;

import pwnbrew.generic.gui.wizard.WizardController;
import pwnbrew.generic.gui.wizard.WizardModel;
import pwnbrew.generic.gui.wizard.WizardPanelDescriptor;

public class HostCheckInWizardController extends WizardController {

    protected static final String NAME_Class = HostCheckInWizardController.class.getSimpleName();
    
    @Override
    protected void nextButtonPressed() {

        WizardModel model = theWizard.getModel();
        WizardPanelDescriptor descriptor = model.getCurrentPanelDescriptor();
        
        String nextPanelDescriptor = descriptor.getNextPanelDescriptor();
        switch (nextPanelDescriptor) {
            case HostSchedulerDescriptor.IDENTIFIER:
                //Populate the components for the panel
                HostSchedulerPanel theHostSchedulerPanel = (HostSchedulerPanel)model.getPanel(HostSchedulerDescriptor.IDENTIFIER);
                if( theHostSchedulerPanel == null ){
                    HostSchedulerDescriptor theHostSchedulerDescriptor = new HostSchedulerDescriptor( (HostCheckInWizard)theWizard );
                    theWizard.registerWizardPanel(HostSchedulerDescriptor.IDENTIFIER, theHostSchedulerDescriptor);
                }
                break;
            case ConfirmationDescriptor.IDENTIFIER:
                //Populate the components for the panel
                ConfirmationPanel theConfPanel = (ConfirmationPanel)model.getPanel(ConfirmationDescriptor.IDENTIFIER);
                if( theConfPanel == null ){
                    ConfirmationDescriptor descriptor2 = new ConfirmationDescriptor( (HostCheckInWizard)theWizard );
                    theWizard.registerWizardPanel(ConfirmationDescriptor.IDENTIFIER, descriptor2);
                    theConfPanel = (ConfirmationPanel) descriptor2.getPanelComponent();
                }   theConfPanel.populateComponents();
                break;          
        }

        theWizard.setCurrentPanel(nextPanelDescriptor);

    }

    //====================================================================
    /**
     * 
     */
    @Override
    protected void backButtonPressed() {
        
//        WizardModel model = theWizard.getModel();        
//        WizardPanelDescriptor descriptor = model.getCurrentPanelDescriptor();
        
//        if(descriptor.getPanelDescriptorIdentifier().equals(CSParameterDescriptor.IDENTIFIER)){   
//            
//            StringBuilder messageBldr = new StringBuilder();
//            messageBldr.append("If you go back, all parameters in this window will be reset.\n")
//                    .append("Are you sure you want to go back?");
//            
//            int usersChoice = JOptionPane.showConfirmDialog( theWizard.getParentFrame(), messageBldr.toString(), //The message
//                "Clear entered data?",  //The title
//                JOptionPane.YES_NO_OPTION ); //Dialog option
//            
//            //Proceed on yes
//            if( usersChoice == JOptionPane.YES_OPTION){
//                super.backButtonPressed();
//            }
//            
//        } else {
            
            //Use the usual function
            super.backButtonPressed();
//        } 
        
    }

    //===============================================================
    /**
     *  Get the ID of the first panel to show
     * @return 
    */   
    @Override
    public String getInitialPanelId() {
        
        HostCheckInWizard theHostCheckInWizard = (HostCheckInWizard)theWizard;
        HostSelectionDescriptor theCSD = new HostSelectionDescriptor( theHostCheckInWizard );
        theHostCheckInWizard.registerWizardPanel(HostSelectionDescriptor.IDENTIFIER, theCSD);
        
        return HostSelectionDescriptor.IDENTIFIER;
    }
    
    //===============================================================
    /**
     *  Perform any cleanup duties before closing the wizard.
     */
    @Override
    public void close() {
        
//        WizardModel model = theWizard.getModel();
//        HostSelectionPanel theCSPpanel = (HostSelectionPanel) model.getPanel(HostSelectionDescriptor.IDENTIFIER);
//        DetectionManager.getDetectionManager().removeDetectListener((HostListener)theCSPpanel);

    }    
  
}
