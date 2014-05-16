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


package pwnbrew.sessions;

import java.awt.Color;
import java.awt.event.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.*;
import pwnbrew.generic.gui.MutableJList;
import pwnbrew.misc.Constants;

/**
 *
 *  
 */
@SuppressWarnings("ucd")
public class HostCheckInList extends MutableJList implements ActionListener {

    private final HostCheckInListListener theListener;
    private final boolean validFlag = true;

    //===============================================================
    /**
     *  Constructor
    */
    public HostCheckInList(HostCheckInListListener passedListener) {
        
        super( new HostCheckInListModel( passedListener ) );
        theListener = passedListener;

        final JTextField theTxtField = new JTextField();
        theTxtField.addKeyListener( new KeyAdapter(){
            /**
            * Invoked when a key has been released.
            */
            @Override
            public void keyReleased(KeyEvent e) {
                validateDate(theTxtField);
            }
        });

        setListCellEditor( new HostCheckInListCellEditor(theTxtField) );
      
    }
    
    //===============================================================
    /**
     *  Sets the value at the index
     * 
     * @param value
     * @param index 
     */
    @Override
    public void setValueAt(Object value, int index){

        HostCheckInListModel theModel = (HostCheckInListModel) getModel();
        String theDateStr = (String)theModel.getElementAt(index);
        if(!theDateStr.equals(value)){
            theModel.setValueAt(value, index);
            
            //Get a list of the task and the appropriate comparator
            Object[] theDateArray = theModel.toArray();
            List<String> theDates = new ArrayList(Arrays.asList(theDateArray));

            //Sort the dates and add them back
            Collections.sort( theDates );
            theModel.clear();

            //Add the sorted list back to the model
            for(String aDate : theDates){
                theModel.addElement(aDate);
            }
        }
    }

    //****************************************************************************
    /**
    *  Determines what menu options to show on the popup menu based on the
    *  {@link XmlBase} object contained in the currently selected node.
    *
    *  @param  e   the {@code MouseEvent} that triggered the popup
    */
    @Override
    public void doPopupMenuLogic( MouseEvent e ) {

        JPopupMenu popup = new JPopupMenu();

        JMenuItem menuItem = new JMenuItem( Constants.DELETE );
        menuItem.setActionCommand( Constants.DELETE );
        menuItem.addActionListener(this);
        menuItem.setEnabled( true );
        popup.add(menuItem);

        if( popup.getComponentCount() > 0 ) {
            popup.show(e.getComponent(), e.getX(), e.getY());
        }

    }
   
    //=========================================================================
    /**
     *  Perform the action needed when an event is fired
     * @param evt 
     */
    @Override
    public void actionPerformed(ActionEvent evt) {

        String actionCommand = evt.getActionCommand();            
        if( actionCommand.equals(Constants.DELETE) ) {
            theListener.removeCheckInDates();
        }   

   }

    //===============================================================
    /**
    * Returns whether the value is valid
    *
     * @param theObj
     * @return 
    */
    @Override
    public boolean isValueValid(Object theObj){
       return validFlag;
    }

    //===============================================================
    /**
     * 
     * @param index
     * @return 
     */
    
    @Override
    public boolean isCellEditable(int index){

        HostCheckInListModel theModel = (HostCheckInListModel)getModel();
        return theModel.isCellEditable(index);
    }

    // ===============================================================
    /**
     * Validates the date.
     * @param theTxtField 
    */
    private void validateDate( JTextField theTxtField ){

        String fieldVal = theTxtField.getText();
        setRequestFocusEnabled(true);
        
        try {
            
            //try and parse the field
            Constants.DEFAULT_DATE_FORMAT.parse((String)fieldVal);
            theTxtField.setBackground(Color.WHITE);
            
        } catch (ParseException ex) {
            
            theTxtField.setBackground(Color.RED);
            setRequestFocusEnabled(false);
        
        }       
       
    }


}
