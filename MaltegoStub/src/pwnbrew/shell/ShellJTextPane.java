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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.BorderFactory;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
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
   private boolean ctrl_char = false; 
   private final Shell theShell;
      
    /**
    * This constructor sets the default mode used for displaying this object as
    * well as the mode to be used for any of its child objects.
    *
    */
    public ShellJTextPane( Shell passedShell ) {

        initComponent();     
        theShell = passedShell;

    }

    // ==========================================================================
    /**
     *  Initialize the components
     */
    private void initComponent() {
        
        setBackground(Color.BLACK);
        setForeground(Color.WHITE);
        
        Color bgColor = Color.BLACK;
        UIDefaults defaults = new UIDefaults();
        defaults.put("TextPane[Enabled].backgroundPainter", bgColor);
        putClientProperty("Nimbus.Overrides", defaults);
        putClientProperty("Nimbus.Overrides.InheritDefaults", true);
        setBackground(bgColor);
        
        LineBorder theBorder = new LineBorder(Color.BLACK);
        Border newBorder = BorderFactory.createCompoundBorder( theBorder, 
                BorderFactory.createEmptyBorder( 8, 8, 8, 8 ) );
        setBorder(newBorder);
        
        //Double buffer
        setDoubleBuffered(true);
        
        //Config
        setEditable(true);
        setCaretColor(Color.WHITE);
        final MutableAttributeSet aSet = new SimpleAttributeSet();
        StyleConstants.setForeground(aSet, Color.WHITE);           
         
        KeyListener keyAdapter = new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                
                try {
                    
                    updateCaret( getCaretPosition());     
                    if( e.getKeyChar() == KeyEvent.VK_ENTER ){

                        ShellStyledDocument aSD = (ShellStyledDocument) getStyledDocument();
                        int lastOutputOffset = getEndOffset();
                        
                        String theStr = "";
                        int len = aSD.getLength() - lastOutputOffset;
                        if( len > 0 )
                            theStr = aSD.getText(lastOutputOffset, len);                  

                        //Add the input terminator
                        String inputTerm = theShell.getInputTerminator();
                        String outputStr = theStr;
                        if( !inputTerm.isEmpty() ){
                            outputStr = theStr.concat( inputTerm );
                            //Insert the string
                            synchronized( aSD ){
                                //Set the type back
                                aSD.setInputSource(ShellStyledDocument.SHELL_OUTPUT);
                                aSD.insertString(aSD.getLength(), inputTerm, aSet);
                                //Set the type back
                                aSD.setInputSource(ShellStyledDocument.USER_INPUT);
                            }
                        }                       
                                                
                        //Reset the offset
                        theShell.setHistoryOffset(-1);

                        theShell.sendInput( outputStr );

                        //Add the command to the history
                        theShell.addCommandToHistory(theStr);

                    } else if( e.getKeyCode() == KeyEvent.VK_TAB ){

                    } else if( e.getKeyCode() == KeyEvent.VK_UP ){

                        theShell.printPreviousCommand();

                    } else if( e.getKeyCode() == KeyEvent.VK_DOWN ){

                        theShell.printNextCommand();
                        
                    } else{
                        
                        //If ctrl flag is set, pass to shell
                        if( ctrl_char ){
                            theShell.handleCtrlChar( e.getKeyCode() );
                        }
                        
                        //Set ctrl char flag
                        if ( e.getKeyCode() == KeyEvent.VK_CONTROL ){
                            ctrl_char = true; 
                        } else {
                            ctrl_char = false;
                        }  
                        
                    }
                    
                } catch (BadLocationException ex) {
                    ex = null;
                }
            }
        }; // end MouseAdapter class
        addKeyListener(keyAdapter);  
        
        MouseListener mouseAdapter = new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                
                if(e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1){
                    String selText = getSelectedText();
                    if( selText == null )
                        updateCaret( getCaretPosition());                    
                }

            } 
        };
        addMouseListener(mouseAdapter);        
        setStyledDocument( new ShellStyledDocument(this) );
        
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
                        if( theSD instanceof ShellStyledDocument ){
                            
                            ShellStyledDocument aSSD = (ShellStyledDocument) theSD;
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
                            synchronized( aSSD ){
                                aSSD.setInputSource(ShellStyledDocument.SHELL_OUTPUT);
                                aSSD.insertString(aSSD.getLength(), newStr, aSet);

                                //Set the type back
                                aSSD.setInputSource(ShellStyledDocument.USER_INPUT);
                            }

                            //Set the new length
                            int newLength = theSD.getLength();
                            theRunnerPane.setEndOffset(newLength);
                            //******** Not necessary ******
                            //theRunnerPane.updateCaret( newLength );
                           
                        }

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
    
     //==============================================================
    /*
     *  Check if the document should be altered
     */
    public boolean updateCaret( int offset ){

        boolean retVal = true;
        if( !isEnabled())
            return retVal;     

        int thePromptOffset = getEndOffset();
        if( thePromptOffset != -1 && offset < thePromptOffset ) {
            setCaretPosition( thePromptOffset );
            retVal = false;
        }
        
        return retVal;
    }
    
      //===============================================================
    /**
     * 
     * @param passedOffset
     * @return 
     * @throws javax.swing.text.BadLocationException 
     */
    public boolean canRemove( int passedOffset ) throws BadLocationException {        
         
        boolean retVal = true;

        if( !isEnabled() )
            return true;

        //Check if the offset has been set, if it has reset it
        int historyOffset = theShell.getHistoryOffset();
        if( historyOffset != -1 ){ 
            if( passedOffset >= historyOffset ){
                setEndOffset(passedOffset);
                return true;
            } else {
                return false;
            }
        }  

        if( !isEnabled() || isUpdating())
            return true;

        if( passedOffset < getEndOffset() )
            retVal = false;
        
        
        return retVal;
    }


}/* END CLASS RunnerPane */
