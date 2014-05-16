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

package pwnbrew.sessions.wizard;

import java.awt.Component;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.TransferHandler;
import pwnbrew.xml.maltego.custom.Host;


public class JListTransferHandler extends TransferHandler{

    private final JList sourceJList;		

    //===============================================================
    /**
    * Constructor
    *
    * @param pSourceJList
    */
    public JListTransferHandler(JList pSourceJList) {		
        sourceJList = pSourceJList;
    }

    //========================================================================
    /**
    * Check whether it is ok to drag and drop
     * @param pTrxferSupport
     * @return 
    */
    @Override
    public boolean canImport(TransferSupport pTrxferSupport) {

        if(sourceJList == null)
            return false;        

        Component targetComponent = pTrxferSupport.getComponent();

        return targetComponent instanceof JList;
    }


    //========================================================================
    /**
    * We only allow MOVE as the action(moving items from one 
    * JList to the other).
     * @return 
    */
    @Override
    public int getSourceActions(JComponent c) {				
        //only allow moving software b/n JList's
        return TransferHandler.MOVE;
    }


    //========================================================================
    /**
    *
     * @return 
    */
    @Override
    protected Transferable createTransferable(JComponent c) {
        //note this is NOT used in our transfer b/n JList's
        // just returning something anyway to make Swing happy(null wouldn't work)

        Host aNode = (Host)sourceJList.getSelectedValue();
        String sSelection = aNode == null ? "" : aNode.toString();

        return new StringSelection(sSelection);
    }


    //========================================================================
    /**
     * @param pTxferSupport
     * @return 
    */
    @Override
    public boolean importData(TransferSupport pTxferSupport) {

        JList targetJList = (JList)pTxferSupport.getComponent();

        dropSelectionsToTarget(sourceJList, targetJList);

        List<Object> selectedValues = sourceJList.getSelectedValuesList();
        selectTargetTxfers(selectedValues, targetJList);

        //remove items from the source list
        clearSourceSelections(selectedValues, sourceJList);

        //put focus on the target jlist since items were just moved there
        targetJList.grabFocus();

        return true;
    }


    //========================================================================
    /** 
    * @param listObjects - the items in the target list that need to
    *   be selected
    */
    private void selectTargetTxfers(List<Object> listObjects, JList pTargetJList){

        DefaultListModel targetJListModel = (DefaultListModel)pTargetJList.getModel();

        //build a List of integers representing the indices of sw items
        // that should be selected in the target JLIst
        int[] aSwIndicesToSelect = new int[listObjects.size()];

        for(int i=0; i < listObjects.size(); i++){
            Object anObject = listObjects.get(i);
            aSwIndicesToSelect[i] = targetJListModel.indexOf(anObject);
        }

        //hack(selection will be overwritten by the setSelectedIndicies() call),
        // but do this version to get the scrollbar to scroll to a selected item
        pTargetJList.setSelectedValue(listObjects.get(0), true);
        pTargetJList.setSelectedIndices(aSwIndicesToSelect);
    }

    //========================================================================
    /**
    * @param pTargetList
    */
    private void dropSelectionsToTarget(JList pSourceList, JList pTargetList){

        DefaultListModel targetJListModel = (DefaultListModel) pTargetList.getModel();

        //add items from sourcejlist to targetjlist
        for(Object iSelection : pSourceList.getSelectedValuesList())
            targetJListModel.addElement(iSelection);
        
    }


    //========================================================================
    /**
    * 
    * @param pSourceJList
    */
    private void clearSourceSelections(List<Object> itemToRemove, JList pSourceJList){

        DefaultListModel sourceJListModel = (DefaultListModel) pSourceJList.getModel();

        //remove selected items from the source jlist
        for (Object iSelection : itemToRemove) 
            sourceJListModel.removeElement(iSelection);
        
        pSourceJList.clearSelection();

    }
	

}

