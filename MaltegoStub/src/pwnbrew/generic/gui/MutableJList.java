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
* MutableJList.java
*
* Created on Aug 21, 2013, 9:20:21 PM
*/

package pwnbrew.generic.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.EventObject;
import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;

/**
 *
 * 
 */
public class MutableJList extends JList<Object> implements CellEditorListener, ListValidator {

    private Component editorComp = null;
    private int editingIndex = -1;
    private ListCellEditor theEditor = null;
    private PropertyChangeListener editorRemover = null;
    private int cellHeight = -1;
 
    public MutableJList(ListModel model) {
        super(model);
        init();
    }

    private void init() {
        getActionMap().put("startEditing", new StartEditingAction());
        getActionMap().put("cancel", new CancelEditingAction());
        addMouseListener( new MouseListener(this) );
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "startEditing");
        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
        putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
    }

    public void setListCellEditor(ListCellEditor passedEditor){
        theEditor = passedEditor;
    }

    public ListCellEditor getListCellEditor(){
        return theEditor;
    }

    public boolean isEditing(){
        return (editorComp == null) ? false : true;
    }

    public Component getEditorComponent(){
        return editorComp;
    }

    public int getEditingIndex(){
        return editingIndex;
    }

    private Component prepareEditor(int index){
       Object value = getModel().getElementAt(index);
       boolean isSelected = isSelectedIndex(index);
       Component aComp = theEditor.getListCellEditorComponenet(this, value, isSelected, index);
       return aComp;
    }

    private void removeEditor(){
        KeyboardFocusManager.getCurrentKeyboardFocusManager().
                removePropertyChangeListener("permanentFocusOwner",editorRemover);
        editorRemover = null;

        if(theEditor != null){
            theEditor.removeCellEditorListener(this);

            if(editorComp != null){
                remove(editorComp);
            }

            Rectangle cellRect = getCellBounds(editingIndex, editingIndex);
            editingIndex = -1;
            editorComp = null;

            if( cellRect != null )
                repaint(cellRect);
        }
    }

    private boolean editCellAt(int index, EventObject e){
        if(theEditor!=null && !theEditor.stopCellEditing())
            return false;

        if(index<0 || index>=getModel().getSize())
            return false;

        if (editorRemover == null){
            KeyboardFocusManager fm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
            editorRemover = new CellEditorRemover(fm);
            fm.addPropertyChangeListener("permanentFocusOwner", editorRemover);    //NOI18N
        }

        if (theEditor != null && theEditor.isCellEditable(e)) {

            if(!isCellEditable(index))
               return false;

            editorComp = prepareEditor(index);
            if (editorComp == null) {
                removeEditor();
                return false;
            }
            editorComp.setBounds(getCellBounds(index, index));
            add(editorComp);
            editorComp.validate();

            editingIndex = index;
            theEditor.addCellEditorListener(this);

            return true;
        }
        return false;
    }

    @Override
    public void removeNotify() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().
            removePropertyChangeListener("permanentFocusOwner", editorRemover);   //NOI18N
        super.removeNotify();
    }

    @Override
    public boolean isValueValid(Object passedValue) {
       return true;
    }

    // This class tracks changes in the keyboard focus state. It is used
    // when the XList is editing to determine when to cancel the edit.
    // If focus switches to a component outside of the XList, but in the
    // same window, this will cancel editing.
    class CellEditorRemover implements PropertyChangeListener {
        private final KeyboardFocusManager focusManager;

        private CellEditorRemover(KeyboardFocusManager fm) {
            this.focusManager = fm;
        }

        @Override
        public void propertyChange(PropertyChangeEvent ev) {
            if (!isEditing() || ((Boolean)getClientProperty("terminateEditOnFocusLost")).equals(Boolean.TRUE)) {   //NOI18N
                return;
            }

            Component theComponent = focusManager.getPermanentFocusOwner();
            while (theComponent != null) {
                if (theComponent == MutableJList.this) {
                    // focus remains inside the table
                    return;
                } else if (theComponent instanceof Window) {
                    if (theComponent == SwingUtilities.getRoot(MutableJList.this)) {
                        if (!getListCellEditor().stopCellEditing()) {
                            getListCellEditor().cancelCellEditing();
                        }
                    }
                    break;
                }
                theComponent = theComponent.getParent();
            }
        }
    }

    /*-------------------------------------------------[ Model Support ]---------------------------------------------------*/

    public boolean isCellEditable(int index){
        if(getModel() instanceof MutableListModel)
            return ((MutableListModel)getModel()).isCellEditable(index);
        return false;
    }

    public void setValueAt(Object value, int index){
        ((MutableListModel)getModel()).setValueAt(value, index);
    }

    /*-------------------------------------------------[ Editing Actions]---------------------------------------------------*/

    public static class StartEditingAction extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            MutableJList list = (MutableJList)e.getSource();
            if (!list.hasFocus()) {
                CellEditor cellEditor = list.getListCellEditor();
                if(cellEditor!=null && !cellEditor.stopCellEditing()) {
                    return;
                }
                list.requestFocus();
                return;
            }

            ListSelectionModel rsm = list.getSelectionModel();
            int anchorRow =    rsm.getAnchorSelectionIndex();
            list.editCellAt(anchorRow, null);
            final Component editorComp = list.getEditorComponent();

            if (editorComp != null) {
                editorComp.requestFocus();
                SwingUtilities.invokeLater( new Runnable(){
                    @Override
                    public void run() {
                       KeyEvent ke = new KeyEvent(editorComp, KeyEvent.KEY_RELEASED, System.currentTimeMillis(),
                               -1, KeyEvent.VK_SPACE, (char)KeyEvent.VK_SPACE);
                       editorComp.dispatchEvent(ke);
                    }
                });
            }
        }
    }

    public class CancelEditingAction extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            MutableJList list = (MutableJList)e.getSource();
            list.removeEditor();
        }

        @Override
        public boolean isEnabled(){
            return isEditing();
        }
    }

    private class MouseListener extends MouseAdapter{
        private Component dispatchComponent;
        private final MutableJList theList;

        public MouseListener(MutableJList passedList) {
            theList = passedList;
        }       

        private void setDispatchComponent(MouseEvent e) {
            Component editorComponent = getEditorComponent();
            Point p = e.getPoint();
            Point p2 = SwingUtilities.convertPoint(MutableJList.this, p, editorComponent);
            dispatchComponent = SwingUtilities.getDeepestComponentAt(editorComponent,
                                                                 p2.x, p2.y);
        }

        private boolean repostEvent(MouseEvent e) {
            // Check for isEditing() in case another event has
            // caused the editor to be removed. See bug #4306499.
            if (dispatchComponent == null || !isEditing()) {
                return false;
            }
            MouseEvent e2 = SwingUtilities.convertMouseEvent(MutableJList.this, e, dispatchComponent);
            dispatchComponent.dispatchEvent(e2);
            return true;
        }

        private boolean shouldIgnore(MouseEvent e) {
            return e.isConsumed() || (!(SwingUtilities.isLeftMouseButton(e) && isEnabled()));
        }

        @Override
        public void mousePressed(MouseEvent e){
            if(shouldIgnore(e))
                return;
            Point p = e.getPoint();
            int index = locationToIndex(p);
            // The autoscroller can generate drag events outside the Table's range.
            if(index==-1)
                return;

            if(editCellAt(index, e)){
                setDispatchComponent(e);
                repostEvent(e);

                SwingUtilities.invokeLater( new Runnable(){
                    @Override
                    public void run() {
                       KeyEvent ke = new KeyEvent(editorComp, KeyEvent.KEY_RELEASED, System.currentTimeMillis(),
                               -1, KeyEvent.VK_SPACE, (char)KeyEvent.VK_SPACE);
                       editorComp.dispatchEvent(ke);
                    }
                });

            } else if(isRequestFocusEnabled())
                requestFocus();
        }

        @Override
        public void mouseReleased(MouseEvent evt){

            if( evt.isPopupTrigger() == true ) {
                Point clickPoint = evt.getPoint();
                int selectedIndex = getSelectedIndex();
                if(selectedIndex != -1){
                    Point selectedPoint = indexToLocation(selectedIndex);
                  
                    if(cellHeight == -1){
                       Point point0 = indexToLocation(0);
                       Point point1 = indexToLocation(1);
                       
                       if(point1 != null){
                           cellHeight = point1.y - point0.y;
                       } else {
                           cellHeight = 20;
                       }
                    }

                    if(!(clickPoint.y >= selectedPoint.y) || !(clickPoint.y <= selectedPoint.y + (cellHeight - 1))){
                        clearSelection();
                    }
                }

                doPopupMenuLogic( evt );
            }
        }
    }

     //****************************************************************************
    /**
    *  Shows the popup allowing a user to add a scripting language
    *
    *  @param  evt   the {@code MouseEvent} that triggered the popup
    */
    protected void doPopupMenuLogic( MouseEvent evt ) {
    }


    //CellEditorListener functions
    @Override
    public void editingStopped(ChangeEvent e) {
        if(theEditor != null){

            Object value = theEditor.getCellEditorValue();
            if(isValueValid(value)){
               setValueAt(value, editingIndex);
               removeEditor();
            }
        }
    }

    @Override
    public void editingCanceled(ChangeEvent e) {
        removeEditor();
    }


}/* END CLASS MutableJList */
