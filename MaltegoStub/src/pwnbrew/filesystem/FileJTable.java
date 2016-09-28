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
package pwnbrew.filesystem;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.ParseException;
import java.util.Comparator;
import java.util.Date;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import pwnbrew.misc.Constants;
import pwnbrew.network.control.messages.FileOperation;
import pwnbrew.network.control.messages.FileSystemMsg;

/**
 *
 * @author Securifera
 */
public class FileJTable extends JTable {

    private final FileBrowserListener theListener;
    protected JPopupMenu theJPopup;
    
    //====================================================================
    /**
     * Constructor
     * @param passedListener
     */
    public FileJTable( FileBrowserListener passedListener ) {
        
        //Set the listener
        theListener = passedListener;
        
        DefaultTableModel theTableModel = new javax.swing.table.DefaultTableModel(
            new Object [][] {},
            new String [] {
                "Name", "Date Modified", "Type", "Size"
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
        TableColumnModel theCM = getColumnModel();
        theCM.getColumn(0).setCellRenderer( new DefaultTableCellRenderer(){
        
             @Override
             public Component getTableCellRendererComponent (JTable table,
                                                    Object value,
                                                    boolean isSelected,
                                                    boolean hasFocus,
                                                    int row, int column) {
                 if(isSelected) {
                    setBackground(table.getSelectionBackground());
                    setForeground(table.getSelectionForeground());
                 } else {
                    setBackground(table.getBackground());
                    setForeground(table.getForeground());
                 }
                 
                 FileNode aFileNode = (FileNode)value;
                 setFont(null);
                 setText(aFileNode.getFile().getName());
                 setIcon(aFileNode.getIcon());
                 return this;             
             }     
        
        });
        
        setShowGrid(false);
        setAutoCreateRowSorter(true);
        TableRowSorter theRowSorter = (TableRowSorter) getRowSorter();
        setRowSorter(theRowSorter);
        
        //Set the size comparator
        theRowSorter.setComparator(0, new Comparator() {

            @Override
            public int compare(Object o1, Object o2) {
                if( o1 instanceof FileNode && o2 instanceof FileNode ){
                    FileNode firstNode = (FileNode)o1;
                    FileNode secondNode = (FileNode)o2;
                    if( firstNode.getType() == secondNode.getType() )
                        return firstNode.getFile().getName().toLowerCase().compareTo(secondNode.getFile().getName().toLowerCase());
                    else if( firstNode.getType() == FileSystemMsg.FOLDER && secondNode.getType() == FileSystemMsg.FILE )
                        return -1;
                }
                return 1;
            }
        });
        
        //Set the date comparator
        theRowSorter.setComparator(1, new Comparator() {

            @Override
            public int compare(Object o1, Object o2) {
                
                if( o1 instanceof String && o2 instanceof String){
                    String firstDateStr = (String)o1;
                    String secondDateStr = (String)o2;
                    
                    //Compare the dates
                    try {
                        
                        Date firstDate = Constants.DEFAULT_DATE_FORMAT.parse(firstDateStr);
                        Date secondDate = Constants.DEFAULT_DATE_FORMAT.parse(secondDateStr);
                        return firstDate.compareTo(secondDate);
                        
                    } catch (ParseException ex) {                    
                    }
                    
                }
                
                return 0;                
            }
        });
        
         //Set the size comparator
        theRowSorter.setComparator(3, new Comparator() {

            @Override
            public int compare(Object o1, Object o2) {
                if( o1 instanceof Long && o2 instanceof Long )
                    return ((Long)o1).compareTo((Long)o2);                
                else if( o1 instanceof String && o2 instanceof String)
                    return ((String)o1).compareTo((String)o2);
                else if( o1 instanceof String )
                    return -1;
                else 
                    return 1;
                
            }
        });
        
        //Set the background
        setBackground( Color.WHITE );
        
        //Add a listener
        ListSelectionModel aSelectionModel = getSelectionModel();
        aSelectionModel.addListSelectionListener( new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if( !e.getValueIsAdjusting())
                    theListener.fileJTableValueChanged( e );
            }
        });
        
        // NEW
        theJPopup = new JPopupMenu();

        //Add the action listeners
        final FileJTable theJTable = this;
        Action deleteAction = new AbstractAction("Delete"){ 
            @Override
            public void actionPerformed(ActionEvent e){

                int[] selRowIndexes = theJTable.getSelectedRows();
                DefaultTableModel theTableModel = (DefaultTableModel) theJTable.getModel();
                for( int anInt : selRowIndexes ){
                     
                    //Converts the view index for the row to the underlying model
                    anInt = theJTable.convertRowIndexToModel(anInt);            
                    FileNode aFileNode = (FileNode)theTableModel.getValueAt(anInt, 0);
                    RemoteFile theFile = aFileNode.getFile();
                    String filePath = theFile.getAbsolutePath();
                    theListener.performFileOperation( FileOperation.DELETE, filePath, "" );

                }
               
            }
        };
        theJPopup.add(deleteAction);

        Action renameAction = new AbstractAction("Rename"){ 
            @Override
            public void actionPerformed(ActionEvent e){
                
                int[] selRowIndexes = theJTable.getSelectedRows();
                DefaultTableModel theTableModel = (DefaultTableModel) theJTable.getModel();
                for( int anInt : selRowIndexes ){
                    
                      //Converts the view index for the row to the underlying model
                    anInt = theJTable.convertRowIndexToModel(anInt);
            
                    FileNode aFileNode = (FileNode)theTableModel.getValueAt(anInt, 0);
                    RemoteFile theFile = aFileNode.getFile();
                    String filePath = theFile.getAbsolutePath();
                    File aFile = new File(filePath);

                    Object userInputStr = JOptionPane.showInputDialog(null, null, "Rename File", JOptionPane.PLAIN_MESSAGE, null, null, aFile.getName() );
                    if( userInputStr != null && userInputStr instanceof String ) { //If the new name String is null...
                         //Get the file
                        theListener.performFileOperation( FileOperation.RENAME, filePath, (String) userInputStr);
                    }
                }    
            
            }
        };
        theJPopup.add(renameAction);
        
        //Add the date change option
        Action dateAction = new AbstractAction("Change Modified Date"){ 
            @Override
            public void actionPerformed(ActionEvent e){

                 int[] selRowIndexes = theJTable.getSelectedRows();
                 DefaultTableModel theTableModel = (DefaultTableModel) theJTable.getModel();
                 for( int anInt : selRowIndexes ){
                    
                    //Converts the view index for the row to the underlying model
                    anInt = theJTable.convertRowIndexToModel(anInt);
            
                    FileNode aFileNode = (FileNode)theTableModel.getValueAt(anInt, 0);
                    RemoteFile theFile = aFileNode.getFile();
                    String filePath = theFile.getAbsolutePath();
                    String lastModified = aFileNode.getLastModified();

                    Object userInputStr = JOptionPane.showInputDialog(null, null, "Change Modified Date", JOptionPane.PLAIN_MESSAGE, null, null, lastModified );
                    if( userInputStr != null && userInputStr instanceof String ) { //If the new name String is null...
                         //Get the file
                        theListener.performFileOperation( FileOperation.DATE, filePath, (String) userInputStr);
                    }                        
                }
            }
        };
        theJPopup.add(dateAction);        
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
                } else if(e.getClickCount() == 2){
                    int selRow = theJTable.getSelectedRow();
                    if( selRow != -1 ){
                        FileNode aFileNode = (FileNode)getValueAt(selRow, 0);   
                        theListener.selectNodeInTree(aFileNode);
                    }                 
                }           
            }
        });
    }  
    
    //====================================================================
    /**
     * 
     * @param e
     * @return 
     */
    @Override
    public String getToolTipText(MouseEvent e) {
        
        String tip = null;
        java.awt.Point p = e.getPoint();
        int rowIndex = rowAtPoint(p);

        try {
            //comment row, exclude heading
            if(rowIndex != -1){
                FileNode aFileNode = (FileNode)getValueAt(rowIndex, 0);   
                tip = aFileNode.getFile().getAbsolutePath();
            }
        } catch (RuntimeException e1) {
        }

        return tip;
    }

}
