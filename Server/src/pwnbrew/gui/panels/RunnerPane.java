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

package pwnbrew.gui.panels;

import java.awt.Color;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.*;
import pwnbrew.logging.Log;
import pwnbrew.misc.Constants;
import pwnbrew.misc.EditMenuUpdater;
import pwnbrew.misc.StdErrReceiver;
import pwnbrew.misc.StdOutReceiver;

/**
 *
 *  
 */
public class RunnerPane extends JTextPane implements CaretListener, StdErrReceiver, StdOutReceiver{

   private static final String NAME_Class = RunnerPane.class.getSimpleName();
      
   /**
   * This constructor sets the default mode used for displaying this object as
   * well as the mode to be used for any of its child objects.
   *
   */
   public RunnerPane( ) {

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

    // ==========================================================================
    /**
     *  Handle the std err bytes
     * 
     * @param aBuffer 
     */
   @Override //IStdErrReceiver
   public synchronized void handleStdErr(final byte[] aBuffer) {

      if(aBuffer != null){

          final StyledDocument theSD = getStyledDocument();
          final MutableAttributeSet aSet = new SimpleAttributeSet();
          StyleConstants.setForeground(aSet, Color.RED);
                   
          final String aStr;
          try {
              
              aStr = new String(aBuffer, Constants.SHELL_ENCODING);
             
          
              SwingUtilities.invokeLater(new Runnable(){

                 @Override
                 public void run() {
                    try {
                        theSD.insertString(theSD.getLength(), aStr, aSet);
                        setCaretPosition( theSD.getLength() ); //Scroll to the bottom
                    } catch (BadLocationException ex) {
                        Log.log(Level.SEVERE, NAME_Class, "iStdErrReceiver_HandleData()", ex.getMessage(), ex );
                    }
                 }

               });
             
              
          } catch (UnsupportedEncodingException ex) {
              Log.log(Level.SEVERE, NAME_Class, "iStdErrReceiver_HandleData()", ex.getMessage(), ex );
          }

      } 
    
   }

    // ==========================================================================
    /**
     *  Handle the std out bytes
     * 
     * @param aBuffer 
     */
   @Override
   public synchronized void handleStdOut(final byte[] aBuffer) {

      if(aBuffer != null){

          final StyledDocument theSD = getStyledDocument();
          final MutableAttributeSet aSet = new SimpleAttributeSet();
          StyleConstants.setForeground(aSet, Color.WHITE);
          
          SwingUtilities.invokeLater(new Runnable(){

                @Override
                public void run() {
                    try {
                        
                        String aStr = new String(aBuffer,  Constants.SHELL_ENCODING);
                        
                        theSD.insertString(theSD.getLength(), aStr, aSet);
                        setCaretPosition( theSD.getLength() ); //Scroll to the bottom
                        
                    } catch ( UnsupportedEncodingException | BadLocationException ex) {
                        Log.log(Level.SEVERE, NAME_Class, "iStdOutReceiver_HandleData()", ex.getMessage(), ex );
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
    @Override //CaretListener
    public void caretUpdate( CaretEvent event ) {

        if( event == null ) //If the CaretEvent is null...
            return; //Do nothing

        Object eventSource = event.getSource();
        if( eventSource instanceof JTextComponent ) {
            String selectedText = ( (JTextComponent)eventSource ).getSelectedText();
            boolean enableCopy = ( selectedText != null );
            EditMenuUpdater.updateEditMenu( false, enableCopy, !enableCopy, true );
        } else {
            EditMenuUpdater.updateEditMenu( false, false, false, false );
        }
    
    }/* END caretUpdate( CaretEvent ) */


}/* END CLASS RunnerPane */
