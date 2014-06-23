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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

abstract public class WizardController implements ActionListener {

    protected Wizard theWizard = null;;

    //=======================================================================
    /*
    * Constructor
    */
    public WizardController() {

    }

    //=======================================================================
    /*

    */
    public void setWizard(Wizard passedWizard) {
        theWizard = passedWizard;
    }

    //=======================================================================
    /*        
    */
    protected void cancelButtonPressed() {
        theWizard.close(Wizard.CANCEL_RETURN_CODE);
    }

    //=======================================================================
    /*        
    */
    protected void nextButtonPressed() {

        WizardModel model = theWizard.getModel();
        WizardPanelDescriptor descriptor = model.getCurrentPanelDescriptor();

        String nextPanelDescriptor = descriptor.getNextPanelDescriptor();
        theWizard.setCurrentPanel(nextPanelDescriptor);

    }

    //=======================================================================
    /*        
    */
    protected void finishButtonPressed() {
        theWizard.close(Wizard.FINISH_RETURN_CODE);
    }

    //=======================================================================
    /*        
    */
    protected void backButtonPressed() {

        WizardModel model = theWizard.getModel();
        WizardPanelDescriptor descriptor = model.getCurrentPanelDescriptor();

        //  Get the descriptor that the current panel identifies as the previous
        //  panel, and display it.

        String backPanelDescriptor = descriptor.getBackPanelDescriptor();
        theWizard.setCurrentPanel(backPanelDescriptor);

    }

    //=======================================================================
    /*        
    */
    public void resetButtonsToPanelRules() {

        //  Reset the buttons to support the original panel rules,
        //  including whether the next or back buttons are enabled or
        //  disabled, or if the panel is finishable.

        WizardModel model = theWizard.getModel();
        WizardPanelDescriptor descriptor = model.getCurrentPanelDescriptor();

        model.setCancelButtonText("Cancel");

        //  If the panel in question has another panel behind it, enable
        //  the back button. Otherwise, disable it.

        model.setBackButtonText("Back");

        if (descriptor.getBackPanelDescriptor() != null)
            model.setBackButtonEnabled(Boolean.TRUE);
        else
            model.setBackButtonEnabled(Boolean.FALSE);

        //  If the panel in question has one or more panels in front of it,
        //  enable the next button. Otherwise, disable it.

        String nextPanelDescriptor = descriptor.getNextPanelDescriptor();
        if (nextPanelDescriptor == null )
            model.setNextFinishButtonEnabled(Boolean.FALSE);
        else {

            //  If the panel in question is the last panel in the series, change
            //  the Next button to Finish. Otherwise, set the text back to Next.
            if (nextPanelDescriptor.equals(WizardPanelDescriptor.FINISH))
                model.setNextFinishButtonText("Finish");
            else
                model.setNextFinishButtonText("Next");
            
        }

    }

    //=======================================================================
    /*        
    */
    @Override
    public void actionPerformed(ActionEvent event) {
        String theCommand = event.getActionCommand();
        switch (theCommand) {
            case "Back":
                backButtonPressed();
                break;
            case "Next":
                nextButtonPressed();
                break;
            case "Finish":
                finishButtonPressed();
                break;
            case "Cancel":
                cancelButtonPressed();
                break;
        }
    }

    //=======================================================================
    /*        
    */
    abstract public String getInitialPanelId();

    //=======================================================================
    /*        
    */
    public void close( int code ) {}

}/* END CLASS WizardController */
