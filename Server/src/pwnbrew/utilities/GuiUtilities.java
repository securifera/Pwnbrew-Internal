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



package pwnbrew.utilities;

import java.awt.Desktop;
import java.awt.FontMetrics;
import java.awt.Image;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultEditorKit;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import pwnbrew.misc.Constants;
import pwnbrew.misc.Directories;
import pwnbrew.utilities.Utilities.EditMenuOptions;
import static pwnbrew.utilities.Utilities.IMAGE_PATH_IN_JAR;
import static pwnbrew.utilities.Utilities.loadImageFromJar;

/**
 *
 *  This class provides various utility functions.
 *
 *  
 *
 */
public class GuiUtilities {
     
    //============================================================
    /**
     * Converts a TreeNode to a TreePath
     * @param aTreeNode
     * @return 
    */
    public static TreePath getTreePath(TreeNode aTreeNode){
        
        List<TreeNode> nodeList = new ArrayList<>();
        
        while(aTreeNode != null){
           nodeList.add(aTreeNode);
           aTreeNode = aTreeNode.getParent();
        }
        
        //Reverse the list
        Collections.reverse(nodeList);
        
        return new TreePath(nodeList.toArray());
    }
    
    //****************************************************************************
    /**
    * Convenience method for setComponentIcon
     * @param passedJComponent
     * @param logoImageStr
     * @param buttonWidth
     * @param buttonHeight
    */
    public static void setComponentIcon(JComponent passedJComponent, int buttonWidth, int buttonHeight, String logoImageStr ){
        setComponentIcon(passedJComponent, buttonWidth, buttonHeight, logoImageStr, Utilities.getClassPath(), IMAGE_PATH_IN_JAR);
    }
    
    //****************************************************************************
    /**
    * Sets the icon for the given component
    */
    private static void setComponentIcon(JComponent passedJComponent, int buttonWidth, int buttonHeight, String logoImageStr, File passedFile, String imageLocation ){

       Image nodeImage = loadImageFromJar( logoImageStr, passedFile, imageLocation );
       if(nodeImage != null){
          ImageIcon nodeIcon = new ImageIcon(nodeImage.getScaledInstance(buttonWidth, buttonHeight, Image.SCALE_SMOOTH));

          if(passedJComponent instanceof AbstractButton){
             JButton passedJButton = (JButton)passedJComponent;
             passedJButton.setIcon(nodeIcon);
             passedJButton.setText("");
          }
       }
    }
    
    //****************************************************************************
    /**
    * Sets the icon for the given component
     * @param passedJComponent
     * @param tabIndex
     * @param buttonWidth
     * @param logoImageStr
     * @param buttonHeight
    */
    public static void setTabIcon(JComponent passedJComponent, int buttonWidth, int buttonHeight, String logoImageStr, int tabIndex){

       Image nodeImage = loadImageFromJar( logoImageStr );
       if(nodeImage != null){
          ImageIcon nodeIcon = new ImageIcon(nodeImage.getScaledInstance(buttonWidth, buttonHeight, Image.SCALE_SMOOTH));

          if(passedJComponent instanceof JTabbedPane){
             JTabbedPane thePane = (JTabbedPane)passedJComponent;
             thePane.setIconAt(tabIndex, nodeIcon);
          }
       }
    }
    
    // ==========================================================================
    /**
    * Returns the node nearest to the given {@link DefaultMutableTreeNode}.
    * <p>
    * If the given {@code DefaultMutableTreeNode} is null, this method does nothing
    * and returns null.
    * <p>
    * If the given node has a next sibling, it is returned. If it doesn't have a next
    * sibling, the previous sibling is returned. If the given node doesn't have a
    * previous sibling, it's parent is returned.
    *
    * @param node the node for which the nearest node is to be found
    *
    * @return the node nearest to the given node, null if the given node is the root
    */
    public static DefaultMutableTreeNode getNearestNode( DefaultMutableTreeNode node ) {

        if( node == null ) { //If the given node is null...
            return null; //Do nothing
        }

        DefaultMutableTreeNode theNearestNeighborNode = node.getNextSibling(); //Get the node's next sibling
        if( theNearestNeighborNode == null ) { //If the next sibling was not obtained...

            theNearestNeighborNode = node.getPreviousSibling(); //Get the node's previous sibling
            if( theNearestNeighborNode == null ) { //Get the node's previous sibling was not obtained...
                theNearestNeighborNode = (DefaultMutableTreeNode)node.getParent(); //Get the node's parent
            }

        }
        return theNearestNeighborNode;

    }/* END getNearestNode( DefaultMutableTreeNode ) */
    
        //****************************************************************************
    /**
    *   Removes all of the children from the passed node in the tree model.
    *
    * @param aNode  the {@link DefaultMutableTreeNode} to remove the children.
    * @param theTreeModel      the tree model to remove from.
    */
    public static void removeAllChildren( DefaultMutableTreeNode aNode, DefaultTreeModel theTreeModel ) {
        
        List<DefaultMutableTreeNode> nodeChildren = Collections.list(aNode.children());
        for( DefaultMutableTreeNode nextNode : nodeChildren ){
            theTreeModel.removeNodeFromParent( nextNode ); 
        }           
    }
    
    // ==========================================================================
    /**
    *
     * @return 
    */
    public static HashMap<String, JMenuItem> createEditMenuOptions() {

        HashMap<String, JMenuItem> aNameToMenuItemMap = new HashMap<>();
        javax.swing.Action[] actionsArray = ( new JTextField() ).getActions(); //Get the Actions that can be performed on a JTextField
        String actionName;
        String menuItemName = null;
        JMenuItem aJMenuItem = null;
        for( Action anAction: actionsArray ) { //For each Action...

            try {
                actionName = (String)anAction.getValue( Action.NAME ); //Get the Action's name
            } catch( ClassCastException ex ) {
                continue;
            }
            
            switch (actionName) {
                case DefaultEditorKit.cutAction:
                    menuItemName = EditMenuOptions.CUT.getValue();
                    aJMenuItem = new JMenuItem( anAction );
                    aJMenuItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK ) ); //Set the "accelerator" key
                    aJMenuItem.setMnemonic( 't' );
                    break;
                case DefaultEditorKit.copyAction:
                    menuItemName = EditMenuOptions.COPY.getValue();
                    aJMenuItem = new JMenuItem( anAction );
                    aJMenuItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK ) ); //Set the "accelerator" key...
                    aJMenuItem.setMnemonic( 'C' );
                    break;
                case DefaultEditorKit.pasteAction:
                    menuItemName = EditMenuOptions.PASTE.getValue();
                    aJMenuItem = new JMenuItem( anAction );
                    aJMenuItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK ) ); //Set the "accelerator" key...
                    aJMenuItem.setMnemonic( 'P' );
                    break;
                case DefaultEditorKit.selectAllAction:
                    menuItemName = EditMenuOptions.SELECT_ALL.getValue();
                    aJMenuItem = new JMenuItem( anAction ); //Create a menu time for the Select All Action
                    aJMenuItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK ) ); //Set the "accelerator" key...
                    aJMenuItem.setMnemonic( 'A' );
                    break;

            }

            if( aJMenuItem != null ) { //If a JMenuItem was created...

                aJMenuItem.setEnabled( false );

                if(menuItemName != null){
                    aJMenuItem.setText(menuItemName);
                    aNameToMenuItemMap.put( menuItemName, aJMenuItem );
                    menuItemName = null; //Reset for the next iteration
                }

            }

        } //End of "for( Action anAction: actionsArray ) { //For each Action..."
        
        return aNameToMenuItemMap;

    }/* END createEditMenuOptions() */
    
    //============================================================
    /**
     * Allows DnD with one click
     * @param tree
    */
    public static void oneClickDnd(JTree tree){
       MouseListener dragListener = null;
       
       try{
           Class theClass = Class.forName("javax.swing.plaf.basic.BasicDragGestureRecognizer");
           MouseListener[] mouseListeners = tree.getMouseListeners();
           for(MouseListener aListener : mouseListeners){
              if(theClass.isInstance(aListener.getClass())){
                  dragListener = aListener;
                  break;
              }    
           }
           
           if(dragListener != null){
              tree.removeMouseListener(dragListener);
              tree.removeMouseMotionListener((MouseMotionListener)dragListener);
              tree.addMouseListener(dragListener);
              tree.addMouseMotionListener((MouseMotionListener)dragListener);
           }
           
       } catch(ClassNotFoundException ex){
           ex = null;
       }
    }     
    
    //=========================================================================
    /**
    *  Displays the user manual
     * @return 
    */
    public static String displayUserManual() {

        String retString = null;
        String theManualPath = new StringBuilder( Directories.getDocPath() )
                .append( File.separator ).append( Constants.Manual_Name ).toString();

        File actManual = new File(theManualPath);
        if(actManual.exists()){
            if(!FileUtilities.openFileInEditor(actManual, Desktop.Action.OPEN)){
                retString = "Unable to find the default file editor.";
            }
        } else {
            retString = "Unable to locate User Manual.  Location: " + theManualPath;
        }

        return retString;

    }
    
    
     // ==========================================================================
    /**
    *   Wraps the string array to the label length.
    *
     * @param label
     * @param text
    */
    public static void wrapTextToLabel( JLabel label, String[] text ) {
  
        // measure the length of font in pixel
        FontMetrics fm = label.getFontMetrics(label.getFont());
        int contWidth = label.getParent().getWidth();

        // to find the word separation
        BreakIterator boundary = BreakIterator.getWordInstance();
        StringBuilder m = new StringBuilder("<html>");

        // loop each index of array
        for(String str : text) {
            boundary.setText(str);

            // save each line
            StringBuffer line = new StringBuffer();
            StringBuffer par = new StringBuffer();
            int start = boundary.first();

            // wrap loop
            for(int end = boundary.next(); end != BreakIterator.DONE; start = end, end = boundary.next()) {
                String word = str.substring(start,end);
                line.append(word);

                // compare width with font metrics
                int trialWidth = SwingUtilities.computeStringWidth(fm, line.toString());
                // if bigger, add new line
                if(trialWidth > contWidth) {
                line = new StringBuffer(word);
                par.append("<br />");
                }

                // add new word to paragraphs
                par.append(word);
            }

            par.append("<br />");
            m.append(par);
        }

        m.append("</html>");
        label.setText(m.toString());
    
    }
}
