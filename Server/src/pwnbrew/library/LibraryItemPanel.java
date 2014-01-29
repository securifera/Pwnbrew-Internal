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
 * LibraryItemPanel.java
 *
 * Created on Jun 23, 2013, 1:12:31 PM
 */

package pwnbrew.library;

import java.awt.Color;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.JTextComponent;
import pwnbrew.controllers.MainGuiController;
import pwnbrew.gui.panels.GradientPanel;
import pwnbrew.look.Colors;
import pwnbrew.misc.Constants;
import pwnbrew.misc.EditMenuUpdater;
import pwnbrew.utilities.GuiUtilities;
import pwnbrew.generic.gui.SavablePanel;


/**
 *
 */
abstract public class LibraryItemPanel extends GradientPanel implements CaretListener, SavablePanel {

    protected final JLabel theLibraryItemNameJLabel;
       
    protected javax.swing.JToolBar theToolbar = new javax.swing.JToolBar();
    protected javax.swing.JButton runButton = new javax.swing.JButton();
    protected javax.swing.JButton stopButton = new javax.swing.JButton();
    private final javax.swing.JButton saveButton = new javax.swing.JButton();
    
    
    // ========================================================================
    /**
     * Creates a new instance of {@link LibraryItemPanel}.
     * @param passedListener
     */
    public LibraryItemPanel( ActionListener passedListener ) {
        this( Colors.PANEL_BASE_COLOR, true, false, passedListener );
    }
    
    
    // ========================================================================
    /**
     * Creates a new instance of {@link LibraryItemPanel}.
     * 
     * @param startColor
     * @param leftToRight
     * @param topToBottom
     * @param passedListener
     */
    public LibraryItemPanel( Color startColor, boolean leftToRight, boolean topToBottom, ActionListener passedListener ) {
        this( startColor, startColor.darker(), leftToRight, topToBottom, passedListener );
    }

    
    // ========================================================================
    /**
     * Creates a new instance of {@link LibraryItemPanel}.
     * 
     * @param startColor
     * @param endColor
     * @param leftToRight
     * @param topToBottom
     */
    private LibraryItemPanel( Color startColor, Color endColor, boolean leftToRight, boolean topToBottom, ActionListener passedListener ) {
        super( startColor, endColor, leftToRight, topToBottom );
        
        //Name label...
        theLibraryItemNameJLabel = new JLabel();
        theLibraryItemNameJLabel.setFont( new java.awt.Font( "Microsoft Sans Serif", 1, 12 ) );
        theLibraryItemNameJLabel.setForeground( new java.awt.Color( 255, 255, 255 ) );
        
        initializeComponents( passedListener );
    

    }/* END ( Color, Color, boolean, boolean ) */
    
    // ========================================================================
    /**
    * Updates whether the save button is enabled
     * @param passedVal
    */
    @Override
    public void setSaveButton(boolean passedVal) {
        saveButton.setEnabled(passedVal);
    }
    
    // ========================================================================
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
            
            //If any text is selected, enable cut and copy
            boolean enableCutAndCopy = ( ( (JTextComponent)eventSource ).getSelectedText() != null );
            
            EditMenuUpdater.updateEditMenu( enableCutAndCopy, enableCutAndCopy, true, true );
            
        } else {
            EditMenuUpdater.updateEditMenu( false, false, false, false );
        }
        
    }
    
    
    // ========================================================================
    /**
     * 
     * @param name the new name of the {@code LibraryItem}
     */
    public void setLibraryItemName( String name ) {
        
        if( name == null )
            name = "";
        
        theLibraryItemNameJLabel.setText( name );
        
    }

    // ========================================================================
    /**
     *  Sets up the components
     */
    private void initializeComponents( ActionListener passedListener ) {
        
        int buttonHeight = 24;
        int buttonWidth = buttonHeight;
        
        theToolbar.setFloatable(false);
        theToolbar.setRollover(true);
        theToolbar.setDoubleBuffered(true);
        theToolbar.setOpaque(false);
        theToolbar.setBorder(null);

        runButton.setText("runButton");
        runButton.setFocusable(false);
        runButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        runButton.setOpaque(false);
        runButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        theToolbar.add(runButton);

        stopButton.setText("stopButton");
        stopButton.setFocusable(false);
        stopButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        stopButton.setOpaque(false);
        stopButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        theToolbar.add(stopButton);

        saveButton.setText("saveButton");
        saveButton.setFocusable(false);
        saveButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        saveButton.setOpaque(false);
        saveButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        theToolbar.add(saveButton);
        
        runButton.setToolTipText( MainGuiController.ACTION_RunSelectedItem );
        runButton.setActionCommand( Constants.ACTION_Run );
        runButton.addActionListener( passedListener );

        stopButton.setToolTipText( MainGuiController.ACTION_StopSelectedItem );
        stopButton.setActionCommand( Constants.CANCEL );
        stopButton.addActionListener( passedListener );

        saveButton.setToolTipText( Constants.SAVE_ALL );
        saveButton.setActionCommand( Constants.SAVE_ALL );
        saveButton.addActionListener( passedListener );
        
        //Set the image icons
        GuiUtilities.setComponentIcon(runButton, buttonWidth, buttonHeight, Constants.PLAY_IMG_STR);
        GuiUtilities.setComponentIcon(stopButton, buttonWidth, buttonHeight, Constants.STOP_IMG_STR);
        GuiUtilities.setComponentIcon(saveButton, buttonWidth, buttonHeight, Constants.SAVE_IMG_STR);
        
        //Disable the save button
        setSaveButton(false);
    }

}
