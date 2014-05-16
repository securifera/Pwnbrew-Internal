package pwnbrew.sessions.wizard;

import pwnbrew.generic.gui.wizard.WizardPanelDescriptor;



public class HostSchedulerDescriptor extends WizardPanelDescriptor {

    protected static final String NAME_Class = HostSchedulerDescriptor.class.getSimpleName();
    public static final String IDENTIFIER = "HOST_SCHEDULER_PANEL";

    //===============================================================
    /**
     *  Constructor
     * @param passedWizard
    */
    public HostSchedulerDescriptor( HostCheckInWizard passedWizard) {
        super(IDENTIFIER, new HostSchedulerPanel( passedWizard), passedWizard );
    }

    //===============================================================
    /**
     *  Get the descriptor for the next panel
     * 
     * @return 
     */
    @Override
    public String getNextPanelDescriptor() {

        String nextDescriptor = ConfirmationDescriptor.IDENTIFIER;        
        return nextDescriptor;
    }
    
    //===============================================================
    /**
     * 
     * @return 
     */
    @Override
    public String getBackPanelDescriptor() {
        String nextDescriptor = HostSelectionDescriptor.IDENTIFIER;        
        return nextDescriptor;
    }  
    
}
