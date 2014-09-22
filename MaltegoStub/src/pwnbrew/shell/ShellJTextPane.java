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

package pwnbrew.shell;

import java.awt.Color;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.text.*;
import pwnbrew.misc.Constants;
import pwnbrew.misc.DebugPrinter;
import pwnbrew.output.StreamReceiver;

/**
 *
 *  
 */
public class ShellJTextPane extends JTextPane implements StreamReceiver {

   private static final String NAME_Class = ShellJTextPane.class.getSimpleName();
   private volatile int outputOffset = -1;
   private boolean updating = false;
      
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
                DebugPrinter.printMessage( NAME_Class, "handleEndOfStream()", "Unrecognized stream id.", null );    
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

            final ShellJTextPane theRunnerPane = this;
            SwingUtilities.invokeLater(new Runnable(){

                @Override
                public void run() {
                    try {

                        //Get the current length of the document
                        StyledDocument theSD = theRunnerPane.getStyledDocument(); 
                        String newStr = passedStr;
                        if( passedStr.contains("\b")){
                            int totalLength = theSD.getLength();

                            //Get the length of the passed string and remove any backspaces
                            int strLen = passedStr.length();
                            newStr = passedStr.replaceAll("\b", "");

                            //Number of backspaces
                            int numBack = strLen - newStr.length();                       
                             
                            //Set the new offset
                            theRunnerPane.setEndOffset( totalLength - numBack);

                            //Remove the previous one
                            theRunnerPane.setUpdatingFlag(true);
                            theSD.remove(totalLength - numBack, numBack);
                            theRunnerPane.setUpdatingFlag(false);                            
                        }      
                        
                        //Insert the string
                        theSD.insertString(theSD.getLength(), newStr, aSet);
                        
                        //Set the new length
                        int newLength = theSD.getLength();
                        setCaretPosition( newLength );
                        
                        theRunnerPane.setEndOffset(newLength);

                    } catch ( BadLocationException ex) {
                        DebugPrinter.printMessage( NAME_Class, "handleStdOut()", ex.getMessage(), ex );
                    }
                }
            });

        }

    }

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
