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

package pwnbrew.filesystem;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
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
import pwnbrew.network.control.messages.FileOperation;
import pwnbrew.network.control.messages.FileSystemMsg;


//======================================================================
/**
 *
 *  
 */
public class FileTreePanel extends JPanel implements ActionListener {
    
    protected JTree  theJTree;
    protected DefaultTreeModel theJTreeModel;
    private final static String REFRESH_MENU_STR ="Refresh";
    private final static String DELETE_MENU_STR ="Delete";
    private final static String MKDIR_MENU_STR ="New Folder";
    
    private final FileBrowserListener theFileBrowserListener; 
    private final JScrollPane theScrollPane = new JScrollPane();       
    
    //Class name
    private static final String NAME_Class = FileTreePanel.class.getSimpleName();
  

    //=========================================================================
    /**
     * Constructor 
     * @param passeFileBrowserListener
     */
    public FileTreePanel( FileBrowserListener passeFileBrowserListener ) {
        
        //Set the listener
        theFileBrowserListener = passeFileBrowserListener;
        
        //Create the first host
        RemoteFile aRemoteFile = new RemoteFile( theFileBrowserListener.getHost(),  theFileBrowserListener.getHostDelimiter() );
        FileNode aFileNode = new FileNode( aRemoteFile, FileSystemMsg.HOST, 0, "" );
        
        //Create a node
        IconData theIconData = new IconData( FileNode.HOST_ICON, null,  aFileNode );
        DefaultMutableTreeNode top = new DefaultMutableTreeNode( theIconData );
        top.add(new DefaultMutableTreeNode( true));

        theJTreeModel = new DefaultTreeModel(top);
        theJTree = new JTree(theJTreeModel);
        theJTree.putClientProperty("JTree.lineStyle", "Angled");
        theJTree.setRowHeight(20);

        TreeCellRenderer renderer = new IconCellRenderer();
        theJTree.setCellRenderer(renderer);
        theJTree.addTreeExpansionListener(new DirExpansionListener( theFileBrowserListener ));
        theJTree.addTreeSelectionListener(new DirSelectionListener());

        theJTree.getSelectionModel().setSelectionMode( TreeSelectionModel.SINGLE_TREE_SELECTION); 
        theJTree.setShowsRootHandles(true); 
        theJTree.setEditable(false);
        theJTree.collapseRow(0);

        theScrollPane.getViewport().add(theJTree);
        
        theJTree.addMouseListener( new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e){
                if(e.isPopupTrigger()){
                    doTreePopupMenuLogic(e);
                } 
            }
        });
        
        

        setupLayout();
    }
    
     //=======================================================================
    /**
    * 
    */
    private void doTreePopupMenuLogic( MouseEvent e ) {
       
        
        JPopupMenu popup = new JPopupMenu();
        JMenuItem menuItem;
                 
        menuItem = new JMenuItem( REFRESH_MENU_STR );
        menuItem.setActionCommand( REFRESH_MENU_STR );
        menuItem.addActionListener(this);
        menuItem.setEnabled( true );
        popup.add(menuItem);
        
        menuItem = new JMenuItem( MKDIR_MENU_STR );
        menuItem.setActionCommand( MKDIR_MENU_STR );
        menuItem.addActionListener(this);
        menuItem.setEnabled( true );
        popup.add(menuItem);

        menuItem = new JMenuItem( DELETE_MENU_STR );
        menuItem.setActionCommand( DELETE_MENU_STR );
        menuItem.addActionListener(this);
        menuItem.setEnabled( true );
        popup.add(menuItem);

        if( popup.getComponentCount() > 0 )
            popup.show(e.getComponent(), e.getX(), e.getY());
       
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
    public DefaultMutableTreeNode getTreeNode(TreePath path){
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
     * @param e 
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        String actionCommand = e.getActionCommand();            
        switch (actionCommand) {
            case REFRESH_MENU_STR:
                TreePath theSelPath = theJTree.getSelectionPath(); 
                theFileBrowserListener.fileTreePanelValueChanged(theSelPath);
                break;
            case DELETE_MENU_STR:
                String theMessage = "Are you sure you want to delete the selected folder(s)?";
                int dialogValue = JOptionPane.showConfirmDialog(null, theMessage, "Delete folder(s)?", JOptionPane.YES_NO_OPTION);
                if ( dialogValue == JOptionPane.YES_OPTION ){
                   
                    theSelPath = theJTree.getSelectionPath(); 
                    if( theSelPath != null){
                        DefaultMutableTreeNode selNode = (DefaultMutableTreeNode) theSelPath.getLastPathComponent();
                        Object selObj = selNode.getUserObject();
                        if( selObj instanceof IconData ){
                            IconData iconDataObj = (IconData)selNode.getUserObject();
                            Object innerObj = iconDataObj.getObject();
                            if( innerObj instanceof FileNode){
                                FileNode aFileNode = (FileNode)innerObj;
                                if( aFileNode.getType() == FileSystemMsg.FOLDER){
                                    RemoteFile theFile = aFileNode.getFile();
                                    String filePath = theFile.getAbsolutePath();
                                    theFileBrowserListener.performFileOperation( FileOperation.DELETE, filePath, "" );
                                }
                            }
                        }
                    }
                }
                break;
            case MKDIR_MENU_STR:
                theSelPath = theJTree.getSelectionPath(); 
                if( theSelPath != null){
                    DefaultMutableTreeNode selNode = (DefaultMutableTreeNode) theSelPath.getLastPathComponent();
                    Object selObj = selNode.getUserObject();
                    if( selObj instanceof IconData ){
                        IconData iconDataObj = (IconData)selNode.getUserObject();
                        Object innerObj = iconDataObj.getObject();
                        if( innerObj instanceof FileNode){
                            FileNode aFileNode = (FileNode)innerObj;
                            if( aFileNode.getType() == FileSystemMsg.FOLDER){
                                RemoteFile theFile = aFileNode.getFile();
                                String filePath = theFile.getAbsolutePath();

                                Object userInputStr = JOptionPane.showInputDialog(null, null, "New Folder", JOptionPane.PLAIN_MESSAGE, null, null, null );
                                if( userInputStr != null && userInputStr instanceof String ) { //If the new name String is null...
                                     //Get the file
                                    theFileBrowserListener.performFileOperation( FileOperation.MAKE_DIR, filePath, (String) userInputStr);
                                }
                            }
                        }
                    }
                }
                
                break;
                        
        }

    }
    
    //=========================================================================
    /**
     *  Class for populating the right menu
     */
    class DirSelectionListener implements TreeSelectionListener {
        
        @Override
        public void valueChanged(TreeSelectionEvent event) {            
            theFileBrowserListener.fileTreePanelValueChanged(event.getPath());
        }
    }

}
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
