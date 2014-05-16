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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import javax.swing.JPanel;


public class WizardModel {

    /**
     * Identification string for the current panel.
     */    
    public static final String CURRENT_PANEL_DESCRIPTOR_PROPERTY = "currentPanelDescriptorProperty";
    public static final String BACK_BUTTON_TEXT_PROPERTY = "backButtonTextProperty";
    public static final String BACK_BUTTON_ICON_PROPERTY = "backButtonIconProperty";
    public static final String BACK_BUTTON_ENABLED_PROPERTY = "backButtonEnabledProperty";
    public static final String NEXT_FINISH_BUTTON_TEXT_PROPERTY = "nextButtonTextProperty";
    public static final String NEXT_FINISH_BUTTON_ICON_PROPERTY = "nextButtonIconProperty";
    public static final String NEXT_FINISH_BUTTON_ENABLED_PROPERTY = "nextButtonEnabledProperty";
    public static final String CANCEL_BUTTON_TEXT_PROPERTY = "cancelButtonTextProperty";
    public static final String CANCEL_BUTTON_ICON_PROPERTY = "cancelButtonIconProperty";
    public static final String CANCEL_BUTTON_ENABLED_PROPERTY = "cancelButtonEnabledProperty";
    
    private WizardPanelDescriptor currentPanelDescriptor;
    
    protected HashMap panelHashmap;
    
    private final HashMap buttonTextHashmap;
    private final HashMap buttonEnabledHashmap;    
    private final PropertyChangeSupport propertyChangeSupport;
   
    
    //========================================================================
    /**
     * Constructor.
     */    
    public WizardModel() {
        
        panelHashmap = new HashMap();
        
        buttonTextHashmap = new HashMap();
        buttonEnabledHashmap = new HashMap();
        
        propertyChangeSupport = new PropertyChangeSupport(this);

    }
      
    //========================================================================
    /**
     * Returns the currently displayed WizardPanelDescriptor.
     * @return The currently displayed WizardPanelDescriptor
     */    
    public WizardPanelDescriptor getCurrentPanelDescriptor() {
        return currentPanelDescriptor;
    }
    
    //========================================================================
    /**
     * Registers the WizardPanelDescriptor in the model using the Object-identifier specified.
     * @param id Object-based identifier
     * @param descriptor WizardPanelDescriptor that describes the panel
     */    
    public void registerPanel(String id, WizardPanelDescriptor descriptor) {
        
        //  Place a reference to it in a hashtable so we can access it later
        //  when it is about to be displayed.
        
        panelHashmap.put(id, descriptor);
        
    }

    //========================================================================
    /**
     * Returns JPanel for the given id
     * @param id Object-based identifier
     * @return 
     */
    public JPanel getPanel(String id) {

         JPanel thePanel = null;
         WizardPanelDescriptor theDescriptor = (WizardPanelDescriptor)panelHashmap.get(id);
         if(theDescriptor != null)
            thePanel = (JPanel)theDescriptor.getPanelComponent();         

         return thePanel;
    }
    
    //========================================================================
    /**
     * Sets the current panel to that identified by the Object passed in.
     * @param id Object-based panel identifier
     * @return boolean indicating success or failure
     */    
     boolean setCurrentPanel(Object id) {

        //  First, get the hashtable reference to the panel that should
        //  be displayed.
        
        WizardPanelDescriptor nextPanel = (WizardPanelDescriptor)panelHashmap.get(id);
        
        if (nextPanel == null)
            throw new WizardPanelNotFoundException();   

        WizardPanelDescriptor oldPanel = currentPanelDescriptor;
        currentPanelDescriptor = nextPanel;
        
        if (oldPanel != currentPanelDescriptor)
            firePropertyChange(CURRENT_PANEL_DESCRIPTOR_PROPERTY, oldPanel, currentPanelDescriptor);
        
        return true;
        
    }

    //========================================================================
    /**
     * 
     * @return 
     */
    public String getBackButtonText() {
        return (String)buttonTextHashmap.get(BACK_BUTTON_TEXT_PROPERTY);
    }
    
    //========================================================================
    /**
     * 
     * @param newText 
     */
    public void setBackButtonText(String newText) {
        
        String oldText = getBackButtonText();
        if (!newText.equals(oldText)) {
            buttonTextHashmap.put(BACK_BUTTON_TEXT_PROPERTY, newText);
            firePropertyChange(BACK_BUTTON_TEXT_PROPERTY, oldText, newText);
        }
    }

    //========================================================================
    /**
     * 
     * @return 
     */
    public String getNextFinishButtonText() {
        return (String)buttonTextHashmap.get(NEXT_FINISH_BUTTON_TEXT_PROPERTY);
    }
    
    //========================================================================
    /**
     * 
     * @param newText 
     */
    public void setNextFinishButtonText(String newText) {
        
        String oldText = getNextFinishButtonText();
        if (!newText.equals(oldText)) {
            buttonTextHashmap.put(NEXT_FINISH_BUTTON_TEXT_PROPERTY, newText);
            firePropertyChange(NEXT_FINISH_BUTTON_TEXT_PROPERTY, oldText, newText);
        }
    }

    //========================================================================
    /**
     * 
     * @return 
     */
    public String getCancelButtonText() {
        return (String) buttonTextHashmap.get(CANCEL_BUTTON_TEXT_PROPERTY);
    }
    
    //========================================================================
    /**
     * 
     * @param newText 
     */
    public void setCancelButtonText(String newText) {
        
        String oldText = getCancelButtonText();
        if (!newText.equals(oldText)) {
            buttonTextHashmap.put(CANCEL_BUTTON_TEXT_PROPERTY, newText);
            firePropertyChange(CANCEL_BUTTON_TEXT_PROPERTY, oldText, newText);
        }
    } 
    
    //========================================================================
    /**
     * 
     * @return 
     */
    public Boolean getBackButtonEnabled() {
        return (Boolean)buttonEnabledHashmap.get(BACK_BUTTON_ENABLED_PROPERTY);
    }
    
    //========================================================================
    /**
     * 
     * @param newValue 
     */
    public void setBackButtonEnabled(Boolean newValue) {
        
        Boolean oldValue = getBackButtonEnabled();        
        if (!newValue.equals(oldValue)) {
            buttonEnabledHashmap.put(BACK_BUTTON_ENABLED_PROPERTY, newValue);
            firePropertyChange(BACK_BUTTON_ENABLED_PROPERTY, oldValue, newValue);
        }
    }

    //========================================================================
    /**
     * 
     * @return 
     */
    public Boolean getNextFinishButtonEnabled() {
        return (Boolean)buttonEnabledHashmap.get(NEXT_FINISH_BUTTON_ENABLED_PROPERTY);
    }
    
    //========================================================================
    /**
     * 
     * @param newValue 
     */
    public void setNextFinishButtonEnabled(Boolean newValue) {
        
        Boolean oldValue = getNextFinishButtonEnabled();        
        if (!newValue.equals(oldValue)) {
            buttonEnabledHashmap.put(NEXT_FINISH_BUTTON_ENABLED_PROPERTY, newValue);
            firePropertyChange(NEXT_FINISH_BUTTON_ENABLED_PROPERTY, oldValue, newValue);
        }
    }
    
    //========================================================================
    /**
     * 
     * @return 
     */
    public Boolean getCancelButtonEnabled() {
        return (Boolean)buttonEnabledHashmap.get(CANCEL_BUTTON_ENABLED_PROPERTY);
    }
    
    //========================================================================
    /**
     * 
     * @param newValue 
     */
    public void setCancelButtonEnabled(Boolean newValue) {
        
        Boolean oldValue = getCancelButtonEnabled();        
        if (!newValue.equals(oldValue)) {
            buttonEnabledHashmap.put(CANCEL_BUTTON_ENABLED_PROPERTY, newValue);
            firePropertyChange(CANCEL_BUTTON_ENABLED_PROPERTY, oldValue, newValue);
        }
    }
    
    //========================================================================
    /**
     * 
     * @param p 
     */
    public void addPropertyChangeListener(PropertyChangeListener p) {
        propertyChangeSupport.addPropertyChangeListener(p);
    }
    
    //========================================================================
    /**
     * 
     * @param p 
     */
    public void removePropertyChangeListener(PropertyChangeListener p) {
        propertyChangeSupport.removePropertyChangeListener(p);
    }
    
    //========================================================================
    /**
     * 
     * @param propertyName
     * @param oldValue
     * @param newValue 
     */
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }
    
}
