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
 * DefaultCellBorderRenderer.java
 *
 * Created on June 24, 2013, 10:55 PM
 */


package pwnbrew.generic.gui;

import java.awt.Color;
import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.border.Border;

/**
 *
 *  
 */
public class DefaultCellBorderRenderer extends DefaultListCellRenderer{

    
    private final Border componentBorder;
    
    //===========================================================================
    /**
     *  Constructor
     * 
     * @param passedBorder 
     */
    public DefaultCellBorderRenderer( Border passedBorder ) {
        componentBorder = passedBorder;
    }   
    
    //===========================================================================
    /**
     * 
     * @param list
     * @param value
     * @param index
     * @param isSelected
     * @param cellHasFocus
     * @return 
     */
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus){
        Component retComp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if(!isSelected){
            retComp.setBackground(Color.WHITE);
        }

        if(retComp instanceof JLabel){
            JLabel theLabel = (JLabel)retComp;
            theLabel.setBorder( componentBorder );
        }
        return retComp;
    }

}
