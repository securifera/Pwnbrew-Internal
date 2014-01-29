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
* HostCheckInListModel.java
*
* Created on June 24, 2013, 7:23:42 PM
*/

package pwnbrew.host.gui;

import java.text.ParseException;
import javax.swing.DefaultListModel;
import pwnbrew.generic.gui.MutableListModel;
import pwnbrew.misc.Constants;

/**
 *
 *  
 */
@SuppressWarnings("ucd")
public class HostCheckInListModel extends DefaultListModel implements MutableListModel {

    private final HostSchedulerPanel theParent;

    //===============================================================
    /**
     * Constructor
     * 
     * @param passedParent 
    */
    HostCheckInListModel(HostSchedulerPanel passedParent ) {
        theParent = passedParent;
    }   
    
    //===============================================================
    /**
     * Determines if the cell is editable
     *
     * @param index
     * @return
     */
    @Override
    public boolean isCellEditable(int index) {
        return true;
    }

    //===============================================================
    /**
     *  Sets the value for the Date
     * 
     * @param value
     * @param index 
     */
    @Override
    public void setValueAt(Object value, int index) {
        
        String newDateStr = (String)value;
        try {
            Constants.CHECKIN_DATE_FORMAT.parse((String)value);
        } catch (ParseException ex) {
            return;
        }
        
        String aDate = (String)super.getElementAt(index);  
        super.setElementAt(newDateStr, index);
        theParent.getListener().replaceDate( aDate, newDateStr );                
        
    }

}/* END CLASS HostCheckInListModel */
