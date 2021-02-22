/*
 */
package pwnbrew.sessions;

import pwnbrew.generic.gui.ValidTextField;

/**
 *
 * @author b0yd
 */
public class BeaconIntervalField extends ValidTextField {
    
    private final BeaconIntervalChangeListener theListener;

    //=======================================================================
    /**
     * 
     * @param passedListener
     * @param passedString 
     */
    public BeaconIntervalField(BeaconIntervalChangeListener passedListener, String passedString) {
        super(passedString);
        theListener = passedListener;
    }
    
     // ==========================================================================
    /**
     * Evaluates the current value in the {@link ValidTextField}.
     * <p>
     * If the value is valid this method sets the background of th
     * field to white; otherwise the background color is set to red.
     *
     */
    @Override
    protected void evaluateValue() {
        super.evaluateValue();
        if( dataIsValid && theListener != null )
            theListener.beaconIntervalChanged(getText());        
        
    }
    
}
