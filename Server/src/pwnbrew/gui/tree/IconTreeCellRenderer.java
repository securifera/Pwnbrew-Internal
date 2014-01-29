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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pwnbrew.gui.tree;

import pwnbrew.library.LibraryItemController;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import pwnbrew.look.Colors;

/**
 *
 *  
 */
public class IconTreeCellRenderer extends DefaultTreeCellRenderer {

  private final Color defaultSelectionBackColor = getBackgroundSelectionColor();
  private final Color defaultTextColor = getTextSelectionColor();

  //=============================================================================
  /**
   *    Return the component
   * 
   * @param passedTree
   * @param passedObject
   * @param sel
   * @param expanded
   * @param leaf
   * @param row
   * @param hasFocus
   * @return 
   */
  @Override
  public Component getTreeCellRendererComponent(JTree passedTree, Object passedObject,
         boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {

     Color nonSelectBckgrnd;
     Color selectBckgrnd;
     Color nonSelectText;
     Color selectText;

     if(passedObject instanceof IconNode){
        
        IconNode theNode = ((IconNode) passedObject);
        Object theObject = theNode.getUserObject();

        if(theObject instanceof LibraryItemController ){
            
           LibraryItemController theController = (LibraryItemController)theObject;
           nonSelectText = Color.BLACK;

           if(theController.isDirty()){
             selectBckgrnd = Color.YELLOW;
             nonSelectBckgrnd = Color.YELLOW;
             selectText = Color.BLACK;
           } else if(theController.justImported()){
             selectBckgrnd = Colors.JUST_IMPORTED;
             nonSelectBckgrnd =  Colors.JUST_IMPORTED;
             selectText = Color.BLACK;
           } else {
             selectBckgrnd = defaultSelectionBackColor;
             nonSelectBckgrnd = Color.WHITE;
             selectText = defaultTextColor;           
           }
               
           Color currColor = getBackgroundSelectionColor();
           if( !currColor.equals(selectBckgrnd)){
               setBackgroundSelectionColor(selectBckgrnd);
           }

           currColor = getBackgroundNonSelectionColor();
           if( !currColor.equals(nonSelectBckgrnd)){
               setBackgroundNonSelectionColor(nonSelectBckgrnd);
           }

           currColor = getTextNonSelectionColor();
           if( !currColor.equals(nonSelectText)){
               setTextNonSelectionColor(nonSelectText);
           }

           currColor = getTextSelectionColor();
           if( !currColor.equals(selectText)){
               setTextSelectionColor(selectText);
           }

        }

     }

     //Call parent to get the correct coloring
     //Must call after setting up the custom coloring, else it won't render correctly
     super.getTreeCellRendererComponent(passedTree, passedObject, sel, expanded, leaf, row, hasFocus);

     //Set tree node icon to custom icon
     if(passedObject instanceof IconNode){
        IconNode theNode = ((IconNode) passedObject);
        setIcon(theNode.getIcon());
     }
     return this;
  }
}

