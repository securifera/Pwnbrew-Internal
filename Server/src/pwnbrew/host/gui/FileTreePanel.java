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
* FileTreePanel.java
*
* Created on December 14, 2013, 7:22:43 PM
*/

package pwnbrew.host.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import pwnbrew.host.Host;
import pwnbrew.host.HostController;
import pwnbrew.network.control.messages.FileOperation;

/**
 *
 *  
 */
public class FileTreePanel extends JPanel {
    
    protected JTree  theJTree;
    protected DefaultTreeModel theJTreeModel;
    
    private final HostController theController;    
    private final JScrollPane theScrollPane = new JScrollPane();       

    // NEW
    protected JPopupMenu theJPopup;
//    protected TreePath theClickedPath;
    
    //Class name
    private static final String NAME_Class = FileTreePanel.class.getSimpleName();
  

    //=========================================================================
    /**
     * Constructor 
     * @param passedController
     */
    public FileTreePanel( HostController passedController ) {
        
        theController = passedController;
        DefaultMutableTreeNode top = new DefaultMutableTreeNode( new IconData( Host.HOST_ICON, Host.HOST_DISCONNECT_ICON,  passedController.getObject()) );
        top.add(new DefaultMutableTreeNode( true));

        theJTreeModel = new DefaultTreeModel(top);
        theJTree = new JTree(theJTreeModel);
        theJTree.putClientProperty("JTree.lineStyle", "Angled");
        theJTree.setRowHeight(20);

        TreeCellRenderer renderer = new IconCellRenderer();
        theJTree.setCellRenderer(renderer);
        theJTree.addTreeExpansionListener(new DirExpansionListener( theController ));
        theJTree.addTreeSelectionListener(new DirSelectionListener());

        theJTree.getSelectionModel().setSelectionMode( TreeSelectionModel.SINGLE_TREE_SELECTION); 
        theJTree.setShowsRootHandles(true); 
        theJTree.setEditable(false);
        theJTree.collapseRow(0);

        theScrollPane.getViewport().add(theJTree);

        // NEW
        theJPopup = new JPopupMenu();

        Action deleteAction = new AbstractAction("Delete"){ 
            @Override
            public void actionPerformed(ActionEvent e){

                DefaultMutableTreeNode aNode = (DefaultMutableTreeNode)theJTree.getSelectionPath().getLastPathComponent();
                Object anObj = aNode.getUserObject();
                if( anObj instanceof IconData ){

                        IconData id = (IconData)anObj;
                        anObj = id.getObject();
                        if( anObj instanceof FileNode ){

                            //Get the file
                            FileNode aFN = (FileNode)anObj;
                            String filePath = aFN.getFile().getAbsolutePath();
                            theController.performFileOperation( FileOperation.DELETE, filePath, "" );
                        }
                }
            }
        };
        theJPopup.add(deleteAction);

        Action renameAction = new AbstractAction("Rename"){ 
            @Override
            public void actionPerformed(ActionEvent e){
                DefaultMutableTreeNode aNode = (DefaultMutableTreeNode)theJTree.getSelectionPath().getLastPathComponent();
                Object anObj = aNode.getUserObject();
                if( anObj instanceof IconData ){

                    IconData id = (IconData)anObj;
                    anObj = id.getObject();
                    if( anObj instanceof FileNode ){
                        
                        //Get the file
                        FileNode aFN = (FileNode)anObj;
                        String filePath = aFN.getFile().getAbsolutePath();
                        File aFile = new File(filePath);

                        Object userInputStr = JOptionPane.showInputDialog(null, null, "Rename File", JOptionPane.PLAIN_MESSAGE, null, null, aFile.getName() );
                        if( userInputStr != null && userInputStr instanceof String ) { //If the new name String is null...
                            //Get the file
                            theController.performFileOperation( FileOperation.RENAME, filePath, (String) userInputStr);
                        }
                    }
                }
            }
        };
        theJPopup.add(renameAction);
        
        //Add the date change option
        Action dateAction = new AbstractAction("Change Modified Date"){ 
            @Override
            public void actionPerformed(ActionEvent e){

                DefaultMutableTreeNode aNode = (DefaultMutableTreeNode)theJTree.getSelectionPath().getLastPathComponent();
                Object anObj = aNode.getUserObject();
                if( anObj instanceof IconData ){

                        IconData id = (IconData)anObj;
                        anObj = id.getObject();
                        if( anObj instanceof FileNode ){

                            //Get the file
                            FileNode aFN = (FileNode)anObj;
                            String filePath = aFN.getFile().getAbsolutePath();
                            String lastModified = aFN.getLastModified();

                            Object userInputStr = JOptionPane.showInputDialog(null, null, "Change Modified Date", JOptionPane.PLAIN_MESSAGE, null, null, lastModified );
                            if( userInputStr != null && userInputStr instanceof String ) { //If the new name String is null...
                                //Get the file
                                theController.performFileOperation( FileOperation.DATE, filePath, (String) userInputStr);
                            }
                        }
                }
            }
        };
        theJPopup.add(dateAction);
        
        
        theJTree.add(theJPopup);
        theJTree.addMouseListener(new PopupTrigger());

        setupLayout();
    }
    
     //==========================================================================
    /**
     *  Get the JTree
     * @return 
     */
    public JTree getJTree(){
        return theJTree;
    }
    
    //==========================================================================
    /**
     *  Get the tree model
     * @return 
     */
    public DefaultTreeModel getTreeModel(){
        return theJTreeModel;
    }
    
    //==========================================================================
    /**
     *  Get the tree node
     * @param path
     * @return 
     */
    DefaultMutableTreeNode getTreeNode(TreePath path){
        return (DefaultMutableTreeNode)(path.getLastPathComponent());
    }

    //==========================================================================
    /**
     *  Enables/Disables the panel
     * @param passedBool 
     */
    @Override
    public void setEnabled(boolean passedBool ){
        super.setEnabled(passedBool);
        theScrollPane.setEnabled(passedBool); 
        theScrollPane.setOpaque(passedBool);
        theJTree.setEnabled(passedBool);
        theJTree.setOpaque(passedBool);
    }

    //=========================================================================
    /**
     *  Setup the layout
     */
    private void setupLayout() {
        
        javax.swing.GroupLayout nicPanelLayout = new javax.swing.GroupLayout(this);
        setLayout(nicPanelLayout);
        nicPanelLayout.setHorizontalGroup(
            nicPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(nicPanelLayout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addComponent(theScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 277, Short.MAX_VALUE)
                .addGap(5, 5, 5))
        );
        nicPanelLayout.setVerticalGroup(
            nicPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(nicPanelLayout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addComponent(theScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 150, Short.MAX_VALUE)
                .addGap(5, 5, 5))
        
        );
    }

    //=========================================================================
    /**
     * 
     */
    class PopupTrigger extends MouseAdapter{
        @Override
        public void mouseReleased(MouseEvent e){
            if (e.isPopupTrigger()){
                int x = e.getX();
                int y = e.getY();
                TreePath path = theJTree.getPathForLocation(x, y);
                if (path != null){
                    theJPopup.show(theJTree, x, y);
//                    theClickedPath = path;
                }
            }
        }
    }
    
    //=========================================================================
    /**
     *  Class for populating the right menu
     */
    class DirSelectionListener implements TreeSelectionListener {
        
        @Override
        public void valueChanged(TreeSelectionEvent event) {
            DefaultMutableTreeNode node = getTreeNode( event.getPath() );
            Object anObj = node.getUserObject();
            if( anObj instanceof IconData ){
                IconData theIconData = (IconData)anObj;
                anObj = theIconData.getObject();
            }
             
            //If it's a file node
            if( anObj instanceof FileNode ){
                FileNode aNode = (FileNode)anObj;
                theController.updateFileDetailsPanel( aNode );
            }
        }
    }

}

//======================================================================
/**
 * Tree Cell renderer class calls 
 * 
 *  
 */
class IconCellRenderer extends JLabel implements TreeCellRenderer {
    protected Color m_textSelectionColor;
    protected Color m_textNonSelectionColor;
    protected Color m_bkSelectionColor;
    protected Color m_bkNonSelectionColor;
    protected Color m_borderSelectionColor;

    protected boolean m_selected;

    //=========================================================================
    /**
     * Constructor
     */
    public IconCellRenderer() {
        super();
        m_textSelectionColor = UIManager.getColor("Tree.selectionForeground");
        m_textNonSelectionColor = UIManager.getColor("Tree.textForeground");
        m_bkSelectionColor = UIManager.getColor("Tree.selectionBackground");
        m_bkNonSelectionColor = UIManager.getColor("Tree.textBackground");
        m_borderSelectionColor = UIManager.getColor("Tree.selectionBorderColor");
        setOpaque(false);
    }

     //=========================================================================
    /**
     *  Get the tree render
     * @param tree
     * @param value
     * @param sel
     * @param expanded
     * @param leaf
     * @param row
     * @param hasFocus
     * @return 
     */
    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, 
            boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        Object obj = node.getUserObject();
        setText(obj.toString());

        if (obj instanceof Boolean)
            setText("Retrieving data...");

        if (obj instanceof IconData) {
            IconData idata = (IconData)obj;
            if (expanded)
                setIcon(idata.getExpandedIcon());
            else
                setIcon(idata.getIcon( tree.isEnabled() ));
        }
        else
            setIcon(null);

        setFont(tree.getFont());
        setForeground(sel ? m_textSelectionColor : m_textNonSelectionColor);
        setBackground(sel ? m_bkSelectionColor : m_bkNonSelectionColor);
        m_selected = sel;
        
        return this;
    }
    
    //=========================================================================
    /**
     *  Paint the component
     * @param g 
     */
    @Override
    public void paintComponent(Graphics g) {
        Color bColor = getBackground();
        Icon icon = getIcon();

        g.setColor(bColor);
        int offset = 0;
        if(icon != null && getText() != null) 
            offset = (icon.getIconWidth() + getIconTextGap());
        g.fillRect(offset, 0, getWidth() - 1 - offset,
        getHeight() - 1);

        if (m_selected){
            g.setColor(m_borderSelectionColor);
            g.drawRect(offset, 0, getWidth()-1-offset, getHeight()-1);
        }

        super.paintComponent(g);
    }
   
}
