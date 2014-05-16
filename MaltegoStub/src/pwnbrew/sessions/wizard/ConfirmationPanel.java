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

public final class ConfirmationPanel extends HostCheckInWizardPanel {

    
    //=======================================================================
    /** 
     * Constuctor
     * @param passedWizard */
    public ConfirmationPanel(HostCheckInWizard passedWizard) {
        super("Finish Wizard", passedWizard);
        initComponents();
        setupLayout();
        initializeComponents();
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
                        .addComponent(treeScrollPane, 340, 340, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(descSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(innerListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                           .addGroup(innerListPanelLayout.createSequentialGroup()
                              .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                              .addComponent(clientDescription, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                           ))
                    .addGroup(innerListPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(headerSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 507, Short.MAX_VALUE)))
                .addContainerGap())
        );

        innerListPanelLayout.setVerticalGroup(
            innerListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, innerListPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(headerSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(innerListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(innerListPanelLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(innerListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(innerListPanelLayout.createSequentialGroup()
                                .addGap(15, 15, 15)
                                .addComponent(treeScrollPane,  345, 345, Short.MAX_VALUE)
                                .addGap(5, 5, 5))
                            .addComponent(descSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 392, Short.MAX_VALUE)))
                    .addGroup(innerListPanelLayout.createSequentialGroup()
                        .addGap(36, 36, 36)
                        .addComponent(clientDescription, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE)
                        ))
                .addContainerGap())
        );

        super.setupLayout();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        treeScrollPane = new javax.swing.JScrollPane();
        descSeparator = new javax.swing.JSeparator();
        headerSeparator = new javax.swing.JSeparator();
        clientDescription = new javax.swing.JLabel();

        setLayout(null);

        confJTree.setBackground(new java.awt.Color(226, 225, 225));
        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("root");
        confJTree.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        confJTree.setRootVisible(false);
        confJTree.setShowsRootHandles(true);
        treeScrollPane.setViewportView(confJTree);

        add(treeScrollPane);
        treeScrollPane.setBounds(40, 60, 290, 360);

        descSeparator.setOrientation(javax.swing.SwingConstants.VERTICAL);
        add(descSeparator);
        descSeparator.setBounds(350, 50, 12, 392);
        add(headerSeparator);
        headerSeparator.setBounds(10, 38, 507, 10);

        clientDescription.setText("Notice:");
        clientDescription.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        clientDescription.setAlignmentY(0.0F);
        add(clientDescription);
        clientDescription.setBounds(360, 90, 120, 250);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel clientDescription;
    private final javax.swing.JTree confJTree = new javax.swing.JTree();
    private javax.swing.JSeparator descSeparator;
    private javax.swing.JSeparator headerSeparator;
    private javax.swing.JScrollPane treeScrollPane;
    // End of variables declaration//GEN-END:variables

    private void initializeComponents() {
        clientDescription.setText(new StringBuilder()
                 .append("<html><b>Notice:</b><br><br>")
                 .append("Please verify the configuration data entered is correct before proceeding.")
                 .append("</html>").toString());
    }

    //===============================================================
    /**
     *  Sets up the JTree with the current clients and the scripts to be run on them.
     *  Also displays the parameters that have been configured in the earlier wizard panels.
    */
    public void populateComponents() {                                

    }


}
