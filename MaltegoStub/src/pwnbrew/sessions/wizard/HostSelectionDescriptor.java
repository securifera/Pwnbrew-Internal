package pwnbrew.sessions.wizard;

import pwnbrew.generic.gui.wizard.WizardPanelDescriptor;



public class HostSelectionDescriptor extends WizardPanelDescriptor {

    protected static final String NAME_Class = HostSelectionDescriptor.class.getSimpleName();
    public static final String IDENTIFIER = "HOST_SELECTION_PANEL";

    //===============================================================
    /**
     *  Constructor
     * @param passedWizard
    */
    public HostSelectionDescriptor( HostCheckInWizard passedWizard) {
        super(IDENTIFIER, new HostSelectionPanel( passedWizard), passedWizard );
    }

    //===============================================================
    /**
     *  Get the descriptor for the next panel
     * 
     * @return 
     */
    @Override
    public String getNextPanelDescriptor() {

        String nextDescriptor = HostSchedulerDescriptor.IDENTIFIER;        
        return nextDescriptor;
    }
    
    //===============================================================
    /**
     * 
     * @return 
     */
    @Override
    public String getBackPanelDescriptor() {
        return null;
    }  
    
}
