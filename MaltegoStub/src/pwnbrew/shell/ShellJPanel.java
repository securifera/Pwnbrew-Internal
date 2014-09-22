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
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 *
 *  
 */
@SuppressWarnings("ucd")
public class ShellJPanel extends javax.swing.JPanel {

    private final ShellJPanelListener theListener;   
//    private final ArrayList<String> history = new ArrayList<>();
//    private ListIterator<String> historyIterator = null;
//    private String lastCommand = "";
    private JFileChooser theFileChooser = null;
        
    //===============================================================
    /**
     * Constructor
     * @param passedListener
     */
    public ShellJPanel( ShellJPanelListener passedListener ) {
        
        theListener = passedListener;
        initComponents();
        initializeComponents();
                 
    }
//    
//    //==============================================================
//    /*
//     *  Check if the document should be altered
//     */
//    private int getPromptEndOffset( ShellJTextPane theTextPane ){
//        
//        int retVal = -1;
//        
//         //Get the prompt
//        String thePrompt = theListener.getShell().getShellPrompt();
//        StyledDocument theDoc = theTextPane.getStyledDocument();
//        Element rootElement = theDoc.getDefaultRootElement();
//        if( rootElement != null ){
//
//            //find the prompt working backwords
//            int docElements = rootElement.getElementCount();
//            for( int i = 1; i <= docElements; i++){
//                
//                //Get the element
//                Element anElement = rootElement.getElement( rootElement.getElementCount() - i );
//                int startPos = anElement.getStartOffset();
//                int endPos = anElement.getEndOffset();
//                
//                //Get the string for that element
//                try {
//                    String theStr = theDoc.getText(startPos, (endPos - startPos) - 1);
//                    int promptIndex = theStr.indexOf(thePrompt);
//                    if( promptIndex != -1){
//                        
//                        retVal = startPos + promptIndex + thePrompt.length();
//                        break;
//                    }
//                } catch (BadLocationException ex ){
//                        
//                }
//            }
//
//        } 
//        
//        return retVal;
//    }
    
    //==============================================================
    /*
     *  Check if the document should be altered
     */
    private boolean updateCaret( ShellJTextPane theTextPane, int offset ){

        boolean retVal = true;
        if( !theTextPane.isEnabled())
            return retVal;     
                    
        int thePromptOffset = theTextPane.getEndOffset();
        if( thePromptOffset != -1 && offset < thePromptOffset ) {
            theTextPane.setCaretPosition( thePromptOffset );
            retVal = false;
        }
        
        return retVal;
    }
       
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        shellScrollPane = new javax.swing.JScrollPane();
        openButton = new javax.swing.JButton();
        shellCombo = new javax.swing.JComboBox();

        openButton.setText("Open Shell");
        openButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(shellScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 602, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(shellCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(openButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(shellScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 418, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(shellCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(openButton))
                .addGap(24, 24, 24))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void openButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openButtonActionPerformed
        
         if(openButton.getText().equals("Open Shell")){
            
            
            ShellJTextPane thePane = getShellTextPane();
            thePane.setEnabled( true );
            thePane.requestFocus();
        
            //Spawn the shell
            ClassWrapper aClassWrapper = (ClassWrapper)shellCombo.getSelectedItem();
            theListener.spawnShell( aClassWrapper.theClass );
            
            //Set to stop shell
            openButton.setText("Kill Shell");
            
        } else
            disablePanel( true );

    }//GEN-LAST:event_openButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton openButton;
    private javax.swing.JComboBox shellCombo;
    private javax.swing.JScrollPane shellScrollPane;
    // End of variables declaration//GEN-END:variables

    /**
     * Sets the runner text pane to the passed runner text pane
     * @param shellPane
    */
    public void setShellTextPane( JTextPane shellPane ) {
        if(shellPane != null)
           shellScrollPane.setViewportView( shellPane );
        
    }
    
    //===============================================================
    /**
     *  Disable the shell panel
     * @param passedBool boolean for whether to send a kill message to the client
     */
    public void disablePanel( boolean passedBool ){
        
        //Disable it
        ShellJTextPane theTextPane = getShellTextPane();
        theTextPane.setEnabled( false );

        //Clear the panel
        theTextPane.setText("");
        theTextPane.setEndOffset(-1);

        //Kill the shell
        if( passedBool )
            theListener.killShell();

         //Set to stop shell
        openButton.setText("Open Shell");
    }
    
    //===============================================================
    /**
     * Returns the runner text pane
     *
     * @return 
    */
    public ShellJTextPane getShellTextPane() {
        return (ShellJTextPane) shellScrollPane.getViewport().getView();
    }
    
    //===============================================================
    /**
     * 
     * @param theOffset
     * @return 
     */
    private boolean canRemove( int passedOffset ) throws BadLocationException {
        
         
        boolean retVal = true;
        ShellJTextPane theTextPane = getShellTextPane();
        
        if( !theTextPane.isEnabled() )
            return true;
        
        //Check if the offset has been set, if it has reset it
        int historyOffset = theListener.getShell().getHistoryOffset();
        if( historyOffset != -1 ){ 
            if( passedOffset >= historyOffset ){
                theTextPane.setEndOffset(passedOffset);
                return true;
            } else {
                return false;
            }
        }  
        
        if( !theTextPane.isEnabled() || theTextPane.isUpdating())
            return true;
                
        if( passedOffset < theTextPane.getEndOffset() )
            retVal = false;
        
        return retVal;
    }

    //===============================================================
    /**
     *  Initialize the panel components
     */
    private void initializeComponents() {
        
         theFileChooser = new JFileChooser();
        theFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        theFileChooser.setMultiSelectionEnabled( true ); //Let the user select multiple files
       
        final ShellJTextPane theTextPane = new ShellJTextPane();
        theTextPane.setEditable(true);
        theTextPane.setCaretColor(Color.WHITE);
        final MutableAttributeSet aSet = new SimpleAttributeSet();
        StyleConstants.setForeground(aSet, Color.WHITE);           
         
        KeyListener keyAdapter = new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                
                try {
                    
                    updateCaret( theTextPane, theTextPane.getCaretPosition());     
                    if( e.getKeyChar() == KeyEvent.VK_ENTER ){

                        StyledDocument aSD = theTextPane.getStyledDocument();
                        int lastOutputOffset = theTextPane.getEndOffset();
                        
                        String theStr = "";
                        int len = aSD.getLength() - lastOutputOffset;
                        if( len > 0 )
                            theStr = aSD.getText(lastOutputOffset, len - 1);                  

                        //Add the input terminator
                        String inputTerm = theListener.getShell().getInputTerminator();
                        String outputStr = theStr;
                        if( !inputTerm.isEmpty() )
                            outputStr = theStr.concat( inputTerm );                        
                                                
                        //Reset the offset
                        theListener.getShell().setHistoryOffset(-1);

                        theListener.sendInput( outputStr );

                        //Add the command to the history
                        theListener.getShell().addCommandToHistory(theStr);

                    } else if( e.getKeyCode() == KeyEvent.VK_TAB ){

                    } else if( e.getKeyCode() == KeyEvent.VK_UP ){

                        theListener.getShell().printPreviousCommand();

                    } else if( e.getKeyCode() == KeyEvent.VK_DOWN ){

                        theListener.getShell().printNextCommand();
                    }
                    
                } catch (BadLocationException ex) {
                    ex = null;
                }
            }
        }; // end MouseAdapter class
        theTextPane.addKeyListener(keyAdapter);  
        
        MouseListener mouseAdapter = new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                
                if(e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1){
                    String selText = theTextPane.getSelectedText();
                    if( selText == null )
                        updateCaret( theTextPane, theTextPane.getCaretPosition());
                    
                }

            } 
        };
        theTextPane.addMouseListener(mouseAdapter);        
        theTextPane.setStyledDocument( new DefaultStyledDocument(){
        
            //============================================================
            /*
             *  Insert the string if it is at the end of the textpane
             */
            @Override
            public void insertString( int offset, String str, AttributeSet a) throws BadLocationException{
                if( updateCaret( theTextPane, offset ) )
                    super.insertString(offset, str, a);                
            }
            
            //============================================================
            /*
             *  Remove the string if it is at the end of the textpane
             */
            @Override
            public void remove( int theOffset, int len) throws BadLocationException{
                if( canRemove( theOffset ) )
                    super.remove(theOffset, len);
            }
            
        });
        
        theTextPane.setEnabled( false );
        setShellTextPane( theTextPane );
        
        //Add the command prompt
        List<Class> shellList = theListener.getShellList();
        for( Class aClass : shellList )
            shellCombo.addItem( new ClassWrapper( aClass ) );
        
        //Center the items
        ((JLabel)shellCombo.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
     
    } 
    
    //=======================================================================
    /**
     * Internal class for populating the shell combobox
     */
    class ClassWrapper {
        
        public final Class theClass;
        
        public ClassWrapper( Class passedClass ){
            theClass = passedClass;
        }
              
        @Override
        public String toString(){
            return theClass.getSimpleName();
        }
    }

}
