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
package pwnbrew.host.gui;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;

/**
 *
 * @author Securifera
 */
public class ShellStyledDocument extends DefaultStyledDocument {

    public static final int USER_INPUT = 0;
    public static final int SHELL_OUTPUT = 1;
    
    private final HostShellPanel theHostShellPanel;
    private volatile int theInputSrc = USER_INPUT;
    
    //============================================================
    /**
     * 
     * @param passedPanel
     */
    public ShellStyledDocument( HostShellPanel passedPanel ) {
        theHostShellPanel = passedPanel;
    }   
    
    //============================================================
    /**
     * 
     * @param passedType 
     */
    public void setInputSource( int passedType ){
        theInputSrc = passedType;
    }
    
    //============================================================
    /*
     *  Insert the string if it is at the end of the textpane
     */
    @Override
    public void insertString( int offset, String str, AttributeSet a) throws BadLocationException{
        
        if( str.equals("\n") && theInputSrc == USER_INPUT )
            return;
        
        synchronized( this ){
            if( theHostShellPanel.updateCaret( theHostShellPanel.getShellTextPane(), offset ) )
                super.insertString(offset, str, a);  
        }
    }

    //============================================================
    /*
     *  Remove the string if it is at the end of the textpane
     */
    @Override
    public void remove( int theOffset, int len) throws BadLocationException{
        if( theHostShellPanel.canRemove( theOffset ) )
            super.remove(theOffset, len);
    }

}
