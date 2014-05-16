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
 * RunnerPane
 *
 * Created on June 25, 2013, 8:32 PM
 *
 */

package pwnbrew.shell;

import java.awt.Color;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.*;
import pwnbrew.misc.Constants;
import pwnbrew.misc.DebugPrinter;
import pwnbrew.output.StreamReceiver;

/**
 *
 *  
 */
public class ShellJTextPane extends JTextPane implements CaretListener, StreamReceiver {

   private static final String NAME_Class = ShellJTextPane.class.getSimpleName();
      
   /**
   * This constructor sets the default mode used for displaying this object as
   * well as the mode to be used for any of its child objects.
   *
   */
   public ShellJTextPane( ) {

      initComponent();
      setEditable(false);      

      TitledBorder theBorder = new TitledBorder("Console Output");
      theBorder.setTitleColor(Color.WHITE);
      setBorder(theBorder);
      

      addCaretListener( this );

   }

    // ==========================================================================
    /**
     *  Initialize the components
     */
    private void initComponent() {
        setBackground(Color.BLACK);
        setForeground(Color.WHITE);

    }
    
    //========================================================================
    /**
     * 
     * @param passedId 
     * @param aStr 
     */
    @Override
    public void handleStreamBytes(int passedId, String aStr ) {
        
        final MutableAttributeSet aSet = new SimpleAttributeSet();
        switch( passedId ){
            case Constants.STD_OUT_ID:
                StyleConstants.setForeground(aSet, Color.WHITE);
                break;
            case Constants.STD_ERR_ID:
                StyleConstants.setForeground(aSet, Color.RED);
                break;
            default:
                DebugPrinter.printMessage( NAME_Class, "listclients", "Unrecognized stream id.", null);
                return;
        } 
        
        //Add the string
        insertString(aStr, aSet);
        
    }

    // ==========================================================================
    /**
    *  Handle the std out bytes
    * 
    * @param passedStr 
     * @param aSet 
    */
    private synchronized void insertString( final String passedStr, final MutableAttributeSet aSet ) {

        if(passedStr != null && !passedStr.isEmpty() ){

            final StyledDocument theSD = getStyledDocument();
            SwingUtilities.invokeLater(new Runnable(){

                @Override
                public void run() {
                    try {

                        theSD.insertString(theSD.getLength(), passedStr, aSet);
                        setCaretPosition( theSD.getLength() );                       

                    } catch ( BadLocationException ex) {
                        DebugPrinter.printMessage( NAME_Class, "listclients", ex.getMessage(), ex);
                    }
                }
            });

        }

    }

    // ==========================================================================
    /**
     * Updates the edit menu in response to the given {@link CaretEvent}.
     * <p>
     * If the argument is null this method does nothing.
     *
     * @param event
     * @event the {@code CaretEvent}
     */
    @Override 
    public void caretUpdate( CaretEvent event ) {

        if( event == null ) //If the CaretEvent is null...
            return; 

        Object eventSource = event.getSource();
        if( eventSource instanceof JTextComponent ) {
            String selectedText = ( (JTextComponent)eventSource ).getSelectedText();
            boolean enableCopy = ( selectedText != null );
//            EditMenuUpdater.updateEditMenu( false, enableCopy, !enableCopy, true );
        } else {
//            EditMenuUpdater.updateEditMenu( false, false, false, false );
        }
    
    }/* END caretUpdate( CaretEvent ) */


}/* END CLASS RunnerPane */
