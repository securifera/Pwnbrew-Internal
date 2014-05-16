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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JList;
import pwnbrew.sessions.SessionsJFrame;
import pwnbrew.xml.maltego.custom.Host;


public final class HostSelectionPanel extends HostCheckInWizardPanel {

     // ==========================================================================
     /**
     * Constructor
     *
     * @param passedWizard
     */
     public HostSelectionPanel( HostCheckInWizard passedWizard ) {
          super("Host Selection", passedWizard);
          
          initComponents();
          setupLayout();
          initializeComponents();
     }

     // ==========================================================================
     /**
      * 
      * @return 
      */
     public List<Host> getSelectedHosts(){
         DefaultListModel theModel = (DefaultListModel)selectedList.getModel();
         List<Host> theList = new ArrayList(Arrays.asList(theModel.toArray()));
         return theList;
     }

    // ==========================================================================
    /**
    * 
    */
    private void initializeComponents() {
        
        existingClientLabel.setText("<html>  <b>Available</b> Hosts:</html>");
        selectedClientLabel.setText("<html>  <b>Selected</b> Hosts:</html>");
        clientDescription.setText(new StringBuilder()
            .append("<html><b>Description:</b> <br><br>")
            .append("Please select the host(s) from the first list")
            .append(" for which you wish to setup a check-in schedule.")
            .toString());        


        //Setup the JLists
        DefaultListModel theModel = new DefaultListModel();
        existingList.setModel(theModel);
        DefaultListModel anotherModel = new DefaultListModel();
        selectedList.setModel(anotherModel);
        theWizard.setSelectedModel(anotherModel);

        //Retrieve the currently detected nodes
        SessionsJFrame theParentFrame = (SessionsJFrame) theWizard.getParentFrame();
        JList hostJList = theParentFrame.getHostJList();
        DefaultListModel theListModel = (DefaultListModel) hostJList.getModel();

        //Add all the nodes that are already detected
        for( int i=0; i < theListModel.size(); i++ )
            theModel.addElement(theListModel.get(i));         

        //Add drag and drop handlers
        existingList.setDragEnabled(true);
        existingList.setDropMode(DropMode.INSERT);
        existingList.addMouseListener((MouseListener)new MouseAdapter(){
        @Override
        public void mouseClicked(MouseEvent mouseEvent){
            if(mouseEvent.getClickCount() == 2)
                handleMoveSelected( existingList, selectedList );            
        }

        });

        selectedList.setDragEnabled(true);
        selectedList.setDropMode(DropMode.INSERT);
        selectedList.addMouseListener((MouseListener)new MouseAdapter(){
        @Override
        public void mouseClicked(MouseEvent mouseEvent){
            if(mouseEvent.getClickCount() == 2)
                handleMoveSelected( selectedList, existingList);            
        }

        });

        JListTransferHandler existingListHandler = new JListTransferHandler(selectedList);
        existingList.setTransferHandler(existingListHandler);
        JListTransferHandler selectedListHandler = new JListTransferHandler(existingList);
        selectedList.setTransferHandler(selectedListHandler);

        updateButtonEnablements();

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        existingAddButton = new javax.swing.JButton();
        selectedAddButton = new javax.swing.JButton();
        selectedClientLabel = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        existingListPane = new javax.swing.JScrollPane();
        existingList = new javax.swing.JList();
        selectedAddAllButton = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        existingClientLabel = new javax.swing.JLabel();
        selectedListPane = new javax.swing.JScrollPane();
        selectedList = new javax.swing.JList();
        existingAddAllButton = new javax.swing.JButton();
        clientDescription = new javax.swing.JLabel();

        setLayout(null);

        existingAddButton.setText(">");
        existingAddButton.setMargin(new java.awt.Insets(2, 8, 2, 8));
        existingAddButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                existingAddButtonActionPerformed(evt);
            }
        });
        add(existingAddButton);
        existingAddButton.setBounds(170, 120, 29, 23);

        selectedAddButton.setText("<");
        selectedAddButton.setMargin(new java.awt.Insets(2, 8, 2, 8));
        selectedAddButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectedAddButtonActionPerformed(evt);
            }
        });
        add(selectedAddButton);
        selectedAddButton.setBounds(170, 280, 29, 23);

        selectedClientLabel.setText("  Selected Clients:");
        add(selectedClientLabel);
        selectedClientLabel.setBounds(210, 60, 130, 14);

        jSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);
        add(jSeparator2);
        jSeparator2.setBounds(371, 54, 12, 310);

        existingListPane.setViewportView(existingList);

        add(existingListPane);
        existingListPane.setBounds(20, 80, 130, 260);

        selectedAddAllButton.setText("<<");
        selectedAddAllButton.setMargin(new java.awt.Insets(2, 4, 2, 4));
        selectedAddAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectedAddAllButtonActionPerformed(evt);
            }
        });
        add(selectedAddAllButton);
        selectedAddAllButton.setBounds(170, 250, 29, 23);
        add(jSeparator1);
        jSeparator1.setBounds(10, 38, 507, 10);

        existingClientLabel.setText("  Existing Clients:");
        add(existingClientLabel);
        existingClientLabel.setBounds(20, 60, 130, 14);

        selectedList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { " " };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        selectedListPane.setViewportView(selectedList);

        add(selectedListPane);
        selectedListPane.setBounds(210, 80, 130, 260);

        existingAddAllButton.setText(">>");
        existingAddAllButton.setMargin(new java.awt.Insets(2, 4, 2, 4));
        existingAddAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                existingAddAllButtonActionPerformed(evt);
            }
        });
        add(existingAddAllButton);
        existingAddAllButton.setBounds(170, 150, 29, 23);

        clientDescription.setText("Description:");
        clientDescription.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        clientDescription.setAlignmentY(0.0F);
        add(clientDescription);
        clientDescription.setBounds(390, 60, 120, 250);
    }// </editor-fold>//GEN-END:initComponents

    private void existingAddAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_existingAddAllButtonActionPerformed
        handleMoveAll(existingList, selectedList);
    }//GEN-LAST:event_existingAddAllButtonActionPerformed

    private void selectedAddAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectedAddAllButtonActionPerformed
        handleMoveAll(selectedList, existingList);
    }//GEN-LAST:event_selectedAddAllButtonActionPerformed

    private void existingAddButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_existingAddButtonActionPerformed
        handleMoveSelected(existingList, selectedList);
    }//GEN-LAST:event_existingAddButtonActionPerformed

    private void selectedAddButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectedAddButtonActionPerformed
        handleMoveSelected(selectedList, existingList);
    }//GEN-LAST:event_selectedAddButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel clientDescription;
    private javax.swing.JButton existingAddAllButton;
    private javax.swing.JButton existingAddButton;
    private javax.swing.JLabel existingClientLabel;
    private javax.swing.JList existingList;
    private javax.swing.JScrollPane existingListPane;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JButton selectedAddAllButton;
    private javax.swing.JButton selectedAddButton;
    private javax.swing.JLabel selectedClientLabel;
    private javax.swing.JList selectedList;
    private javax.swing.JScrollPane selectedListPane;
    // End of variables declaration//GEN-END:variables


    //===============================================================
    /**
     * Moves all the elements from one list to the other
     *
    */
    private void handleMoveAll(JList srcList, JList destList) {

        //Move all the elements from one list to the other
        DefaultListModel srcModel = (DefaultListModel)srcList.getModel();
        DefaultListModel destModel = (DefaultListModel)destList.getModel();


        Enumeration theElements = srcModel.elements();
        while( theElements.hasMoreElements() ){
            Object nextElement = theElements.nextElement();

            if(!destModel.contains(nextElement))
                destModel.addElement(nextElement);
            
        }

        if(srcList != existingList)
            srcModel.clear();        

        updateButtonEnablements();

    }

    //===============================================================
    /**
     * Enables/Disables the move all buttons
    */
    private void updateButtonEnablements() {
       
       boolean existingEnabled = false;
       boolean selectedEnabled = false;
       
       DefaultListModel existingModel = (DefaultListModel)existingList.getModel();
       DefaultListModel selectedModel = (DefaultListModel)selectedList.getModel();
       
       if(existingModel.size() > 0)
          existingEnabled = true;       

       if(selectedModel.size() > 0)
          selectedEnabled = true;       

       existingAddAllButton.setEnabled(existingEnabled);
       existingAddButton.setEnabled(existingEnabled);
       selectedAddAllButton.setEnabled(selectedEnabled);
       selectedAddButton.setEnabled(selectedEnabled);
       
       //Set the button enablements
       if(selectedList.getModel().getSize() != 0)
          theWizard.setNextFinishButtonEnabled(true);
       else
          theWizard.setNextFinishButtonEnabled(false);       
       
    }

    //===============================================================
    /**
     * Handles moving all the elements from one list to another
     *
     * @param existingList
    */
    private void handleMoveSelected(JList srcList, JList destList) {

        //Move the selected elements from one list to the other
        DefaultListModel srcModel = (DefaultListModel)srcList.getModel();
        DefaultListModel destModel = (DefaultListModel)destList.getModel();

        Object[] theSelVals = srcList.getSelectedValues();

        for(Object anElement : theSelVals){

            if(!destModel.contains(anElement))
                destModel.addElement(anElement);

            if( srcList != existingList )
                srcModel.removeElement(anElement);

        }

        updateButtonEnablements();
    }

    //===============================================================
    /**
     *  Organizes the GUI components on the screen
    */
    @Override
    protected void setupLayout() {

        javax.swing.GroupLayout innerListPanelLayout = new javax.swing.GroupLayout(innerListPanel);
        innerListPanel.setLayout(innerListPanelLayout);
        innerListPanelLayout.setHorizontalGroup(
            innerListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(innerListPanelLayout.createSequentialGroup()
                .addGroup(innerListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(innerListPanelLayout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(innerListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(existingClientLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(existingListPane, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(innerListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(selectedAddButton, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(selectedAddAllButton, javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(innerListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(existingAddButton)
                                .addComponent(existingAddAllButton)))
                        .addGap(18, 18, 18)
                        .addGroup(innerListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(selectedClientLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(selectedListPane, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(22, 22, 22)
                        .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(innerListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(clientDescription, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                            ))
                    .addGroup(innerListPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)))
                .addContainerGap())
        );
        innerListPanelLayout.setVerticalGroup(
            innerListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, innerListPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(innerListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(innerListPanelLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(innerListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(innerListPanelLayout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addGroup(innerListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(innerListPanelLayout.createSequentialGroup()
                                        .addGap(57, 57, 57)
                                        .addComponent(existingAddButton)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(existingAddAllButton)
                                        .addGap(77, 77, 77)
                                        .addComponent(selectedAddAllButton)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(selectedAddButton))
                                    .addGroup(innerListPanelLayout.createSequentialGroup()
                                        .addComponent(selectedClientLabel)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(selectedListPane, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(innerListPanelLayout.createSequentialGroup()
                                        .addComponent(existingClientLabel)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(existingListPane, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addComponent(jSeparator2, javax.swing.GroupLayout.DEFAULT_SIZE, 312, Short.MAX_VALUE)))
                    .addGroup(innerListPanelLayout.createSequentialGroup()
                        .addGap(47, 47, 47)
                        .addComponent(clientDescription, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                        ))
                .addContainerGap())
        );

        super.setupLayout();
    }

}
