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


package pwnbrew.generic.gui;

/*
* DefaultListCellEditor.java
*
* Created on Jun 22, 2013, 9:21:50 PM
*/


import java.awt.Component;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JTextField;

/**
 *
 *
 */
public class DefaultListCellEditor extends DefaultCellEditor implements ListCellEditor {

    public DefaultListCellEditor(final JCheckBox passedComp){
        super(passedComp);
    }

    public DefaultListCellEditor(final JComboBox<?> passedComp){
        super(passedComp);
    }

    public DefaultListCellEditor(final JTextField passedComp){
        super(passedComp);
    }

    @Override
    public Component getListCellEditorComponenet(JList list, Object value, boolean isSelected, int index) {
       delegate.setValue(value);
       return editorComponent;
    }

}/* END CLASS DefaultListCellEditor */
