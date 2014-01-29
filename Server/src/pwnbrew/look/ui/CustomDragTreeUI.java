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
 * CustomDragTreeUI.java
 *
 * Created on July 14, 2013, 10:12:32 PM
 */

package pwnbrew.look.ui;

import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.metal.MetalTreeUI;

/**
 *
 *  
 */
public class CustomDragTreeUI extends MetalTreeUI {

    //===========================================================================
    /**
     * 
     */
    public CustomDragTreeUI() {    
        super();
    } 
    
    /**
     * Creates a UI
     *
     * @param c
     * @return 
    */
    public static ComponentUI createUI(JComponent c) {
        return new CustomDragTreeUI();
    }
    
    //Overridden mouse handler
    public class MouseHandler extends MetalTreeUI.MouseHandler {
       
       private int x1, y1;
       
       @Override
       public void mousePressed(MouseEvent e){
           
           x1 = e.getX();
           y1 = e.getY();
           
           //Get the rows already selected
           int[] treeRows = tree.getSelectionRows();
           
           //Check if something has already been selected,
           //If so then consume the event
           if(treeRows != null){
               for(int i = 0; i < treeRows.length; i++){
                   Rectangle rect3 = tree.getRowBounds(treeRows[i]);
                   if(rect3.contains(x1, y1)){
                       e.consume();
                       break;
                   }
               }
           }
           super.mousePressed(e);
       }
    }

    @Override
    protected MouseListener createMouseListener(){
        return new MouseHandler();
    }
      
    
}
