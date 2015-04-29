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
import java.util.logging.Level;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.*;
import pwnbrew.host.gui.ShellStyledDocument;
import pwnbrew.logging.Log;
import pwnbrew.misc.Constants;
import pwnbrew.misc.EditMenuUpdater;
import pwnbrew.misc.StreamReceiver;

/**
 *
 *  
 */
public class RunnerPane extends JTextPane implements CaretListener, StreamReceiver {

   private static final String NAME_Class = RunnerPane.class.getSimpleName();
   private volatile int outputOffset = -1;
   private boolean updating = false;
      
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
    
    //=======================================================================
    /**
     * 
     * @return 
     */
    public int getEndOffset(){
        return outputOffset;
    }
    
    //=======================================================================
    /**
     * 
     * @param passedOffset 
     */
    public void setEndOffset( int passedOffset ){
        outputOffset = passedOffset;
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
                Log.log(Level.SEVERE, NAME_Class, "handleEndOfStream()", "Unrecognized stream id.", null );    
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

            final RunnerPane theRunnerPane = this;
            SwingUtilities.invokeLater(new Runnable(){

                @Override
                public void run() {
                    try {

                        //Get the current length of the document
                        StyledDocument theSD = theRunnerPane.getStyledDocument(); 
                        if( theSD instanceof ShellStyledDocument ){
                            
                            ShellStyledDocument aSSD = (ShellStyledDocument) theSD;
                            String newStr = passedStr;
                            if( passedStr.contains("\b")){
                                int totalLength = aSSD.getLength();

                                //Get the length of the passed string and remove any backspaces
                                int strLen = passedStr.length();
                                newStr = passedStr.replaceAll("\b", "");

                                //Number of backspaces
                                int numBack = strLen - newStr.length();                       

                                //Set the new offset
                                theRunnerPane.setEndOffset( totalLength - numBack);

                                //Remove the previous one
                                theRunnerPane.setUpdatingFlag(true);
                                aSSD.remove(totalLength - numBack, numBack);
                                theRunnerPane.setUpdatingFlag(false);                            
                            }      

                            //Insert the string
                            synchronized( aSSD ){
                                aSSD.setInputSource(ShellStyledDocument.SHELL_OUTPUT);
                                aSSD.insertString(aSSD.getLength(), newStr, aSet);

                                //Set the type back
                                aSSD.setInputSource(ShellStyledDocument.USER_INPUT);
                            }

                            //Set the new length
                            int newLength = aSSD.getLength();
                            setCaretPosition( newLength );

                            theRunnerPane.setEndOffset(newLength);
                        }
                        

                    } catch ( BadLocationException ex) {
                        Log.log(Level.SEVERE, NAME_Class, "handleStdOut()", ex.getMessage(), ex );
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
            EditMenuUpdater.updateEditMenu( false, enableCopy, !enableCopy, true );
        } else {
            EditMenuUpdater.updateEditMenu( false, false, false, false );
        }
    
    }/* END caretUpdate( CaretEvent ) */

     //===================================================================
    /**
     * 
     * @param passedBool
     */
    public synchronized void setUpdatingFlag( boolean passedBool ) {
        updating = passedBool;
    }
    
    //===================================================================
    /**
     * 
     * @return 
     */
    public synchronized boolean isUpdating() {
        return updating;
    }


}/* END CLASS RunnerPane */
