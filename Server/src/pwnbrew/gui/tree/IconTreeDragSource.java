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
 * IconTreeDragSource
 *
 * Created on July 14, 2013, 6:32 PM
 *
 */

package pwnbrew.gui.tree;

import java.awt.Point;
import java.awt.dnd.*;
import java.util.Arrays;
import java.util.List;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

/**
 *
 */
public class IconTreeDragSource extends DragSource implements DragSourceListener, DragGestureListener {

    private final JTree theSourceTree;

    //=========================================================================
    /**
    * Constructor
    * @param tree 
    */
    public IconTreeDragSource(JTree tree) {
        theSourceTree = tree;
    }

    //=========================================================================
    /**
    * Creates a new <code>DragGestureRecognizer</code>
    * 
     * @param actions
     * @return 
    */
    public DragGestureRecognizer createDefaultDragGestureRecognizer( int actions ) {
        return createDefaultDragGestureRecognizer(theSourceTree, actions , this);
    }

    //=========================================================================
    /*
    * Drag Gesture Handler
    */
    @Override
    public void dragGestureRecognized(DragGestureEvent dge) {

        TreePath[] paths = theSourceTree.getSelectionPaths();
        if ((paths == null) || (paths.length == 0)) {
            // We can't move the root node or an empty selection
            return;
        }

        Point eventPoint = dge.getDragOrigin();
        TreePath eventPath = theSourceTree.getPathForLocation( eventPoint.x, eventPoint.y );
        List<TreePath> selTreePaths = Arrays.asList(paths);

        if( selTreePaths.contains(eventPath) ) {
            TransferableNode transferable = new TransferableNode(selTreePaths);
            startDrag(dge, DragSource.DefaultMoveDrop, transferable, this);
        }

    }

    /*
    * Drag Event Handlers
    */
    @Override
    public void dragEnter(DragSourceDragEvent dragEvent) {}

    @Override
    public void dragExit(DragSourceEvent dse) {}

    @Override
    public void dragOver(DragSourceDragEvent dragEvent) {}

    @Override
    public void dropActionChanged(DragSourceDragEvent dsde) {}

    @Override
    public void dragDropEnd(DragSourceDropEvent dsde) {}
   
}

