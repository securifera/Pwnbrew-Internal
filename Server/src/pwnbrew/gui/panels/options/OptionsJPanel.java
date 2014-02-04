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
 * OptionsJPanel.java
 *
 * Created on June 23, 2013, 2:12 PM
 */
package pwnbrew.gui.panels.options;

import javax.swing.JPanel;
import pwnbrew.gui.panels.PanelListener;

/**
 *
 */
 public abstract class OptionsJPanel extends JPanel{
     
    private String thePanelName;
    private volatile boolean dirtyFlag = false;
    protected final PanelListener theListener;

    abstract public void saveChanges();

    //==============================================================
    /**
     * Constructor
     * @param panelName
     * @param passedListener 
     */
    public OptionsJPanel( String panelName, PanelListener passedListener) {
        thePanelName = panelName;
        theListener = passedListener;
    }  
    
    //===============================================================
    /**
     * 
     * @return 
    */
    @Override
    public String getName() {
        return thePanelName;
    }
    
    //===============================================================
    /**
     * 
     * @return 
    */
    public boolean isDirty() {
        return dirtyFlag;
    }
    
    //===============================================================
    /**
     * 
     * @param passedBool
     * @return 
     */
    public void setDirtyFlag( boolean passedBool ){
        dirtyFlag = passedBool;
    }
    
    //===============================================================
    /**
    * Sets the save button enablement
     * @param passedBool
    */
    public void setSaveButton(boolean passedBool){
        if(!isDirty()){
            setDirtyFlag( true );
            theListener.valueChanged(passedBool);
        }
    }

    //===============================================================
    /**
     * 
     */
    public void doClose() {}
    
}
