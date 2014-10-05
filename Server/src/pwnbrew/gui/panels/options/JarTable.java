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
package pwnbrew.gui.panels.options;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/**
 *
 * @author Securifera
 */
public class JarTable extends JTable {

    private final JarTableListener theListener;
    protected JPopupMenu theJPopup;
    
    //====================================================================
    /**
     * Constructor
     * @param passedListener
     */
    public JarTable( JarTableListener passedListener ) {
        
        //Set the listener
        theListener = passedListener;
        
        DefaultTableModel theTableModel = new javax.swing.table.DefaultTableModel(
            new Object [][] {},
            new String [] {
                "UID", "Type", "Java Major Version", "Release Version"
            }
        ){
            //Make the table uneditable
            @Override
            public boolean isCellEditable(int row, int column){
                return false;
            }
        };
        
        //set the model and remove the grid lines
        setModel(theTableModel);
        setRowHeight(19);

        setAutoCreateRowSorter(true);
        TableRowSorter theRowSorter = (TableRowSorter) getRowSorter();
        setRowSorter(theRowSorter);
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment( JLabel.CENTER );        
        //Center values
        for(int i=0; i < getColumnCount(); i++){
            getColumnModel().getColumn(i).setCellRenderer( centerRenderer );
        }
   
        //Set the background
        setBackground( Color.WHITE );
            
        // NEW
        theJPopup = new JPopupMenu();

        //Add the action listeners
        final JarTable theJTable = this;
        Action deleteAction = new AbstractAction("Delete"){ 
            @Override
            public void actionPerformed(ActionEvent e){

                int[] selRowIndexes = theJTable.getSelectedRows();
                for( int anInt : selRowIndexes ){     
                    theListener.deleteJarItem( anInt );
                }
               
            }
        };
        theJPopup.add(deleteAction);        
        add(theJPopup);
        
        addMouseListener( new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e){
                int r = theJTable.rowAtPoint(e.getPoint());
                if (r >= 0 && r < theJTable.getRowCount()) {
                    theJTable.setRowSelectionInterval(r, r);
                } else {
                    theJTable.clearSelection();
                }

                int rowindex = theJTable.getSelectedRow();
                if (rowindex < 0)
                    return;
                if (e.isPopupTrigger() && e.getComponent() instanceof JTable ) {
                    theJPopup.show(e.getComponent(), e.getX(), e.getY());                               
                }           
            }
        });
    }  

}
