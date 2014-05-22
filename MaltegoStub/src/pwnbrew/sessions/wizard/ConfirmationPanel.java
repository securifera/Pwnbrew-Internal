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
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import pwnbrew.generic.gui.wizard.WizardModel;
import pwnbrew.misc.Constants;
import pwnbrew.misc.Iconable;
import pwnbrew.misc.Utilities;
import pwnbrew.xml.maltego.custom.Host;

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
                        .addComponent(treeScrollPane, 240, 240, Short.MAX_VALUE)
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
                                .addComponent(treeScrollPane,  245, 245, Short.MAX_VALUE)
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

        confJTree.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 2, 2, 2));
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
        
        confJTree.setCellRenderer( new DefaultTreeCellRenderer(){
            //=============================================================================
            /**
             *    Return the component
            */
            @Override
            public Component getTreeCellRendererComponent(JTree passedTree, Object passedObject,
                   boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                  
                Component aComponent = super.getTreeCellRendererComponent(confJTree, passedObject, sel, expanded, leaf, row, hasFocus);
                if( passedObject instanceof DefaultMutableTreeNode ){
                    
                    DefaultMutableTreeNode aNode = (DefaultMutableTreeNode) passedObject;
                    Object anObj = aNode.getUserObject();
                    if( anObj instanceof Host ){
                
                        Host aHost = (Host)anObj;
                        String theType = aHost.getType();
                        BufferedImage iconImage;
                        
                        if( theType.equals(Host.PWNBREW_HOST_CONNECTED))
                            iconImage = Utilities.loadImageFromJar( Constants.HOST_IMG_STR );	
                        else
                            iconImage = Utilities.loadImageFromJar( Constants.DIS_HOST_IMG_STR );	

                        setIcon(new ImageIcon( iconImage.getScaledInstance( Iconable.ICON_WIDTH, Iconable.ICON_HEIGHT, Image.SCALE_SMOOTH ) ));
                    } else {
                        setIcon(null);
                    }
                }
                
                return aComponent;
            }  
        });
    }
    
    //===============================================================
    /**
    * Adds {@code theObjectToAdd} as a child node under {@code parentNode} at the specified {@code index}.
    *
    * @param theObjectToAdd  the {@link XmlBase} object to be added to the tree
    * @param parentNode      the node under which this object will be added ({@code null}
    *                        to set {@code theObjectToAdd} as the root of the tree)
    * @param index           the child index of {@code parentNode} at which to add {@code theNodeToAdd},
    *                        if {@code null}, it will be added as the last child of {@code parentNode}
    *
    */
    private DefaultMutableTreeNode addObjectToTree( Object theObjectToAdd, DefaultMutableTreeNode parentNode, Integer index ) {

        DefaultMutableTreeNode theNodeToAdd = new DefaultMutableTreeNode(theObjectToAdd);

        // Add the current object into the tree.
        if ( index == -1 )
            parentNode.add( theNodeToAdd );
        else 
            ((DefaultTreeModel)confJTree.getModel()).insertNodeInto( theNodeToAdd, parentNode, index );

        return theNodeToAdd;
    }

    //===============================================================
    /**
     *  Sets up the JTree with the current clients and the scripts to be run on them.
     *  Also displays the parameters that have been configured in the earlier wizard panels.
    */
    public void populateComponents() {  
        
        DefaultTreeModel treeModel =  new DefaultTreeModel( new DefaultMutableTreeNode());
        confJTree.setModel(treeModel);
        DefaultMutableTreeNode treeRoot = (DefaultMutableTreeNode)treeModel.getRoot();
        
        //Get the wizard
        WizardModel theWizardModel = theWizard.getModel();
        HostSelectionPanel theHSP = (HostSelectionPanel) theWizardModel.getPanel( HostSelectionDescriptor.IDENTIFIER );
        
        //Get the check-in list
        HostSchedulerPanel theHschP = (HostSchedulerPanel) theWizardModel.getPanel( HostSchedulerDescriptor.IDENTIFIER );
        List<String> theList = theHschP.getCheckInDates();
        //Get the host list
        List<Host> theHostList = theHSP.getSelectedHosts();
        for( Host aHost : theHostList ){
                
            DefaultMutableTreeNode clientNode = addObjectToTree( aHost, treeRoot, -1);

            //Used to add the id for the next task
            for( String aDate : theList )
                addObjectToTree( aDate, clientNode, -1);
            
        }
        
        //Reload the model
        treeModel.reload();
    }


}
