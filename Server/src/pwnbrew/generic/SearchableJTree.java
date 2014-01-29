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
 * SearchableJTree.java
 *
 * Created on June 21, 2013  8:18:32 PM
 */

package pwnbrew.generic;

import java.util.Enumeration;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

/**
 *
 *  
 */
public class SearchableJTree extends JTree {
    
    
    //****************************************************************************
    /**
    * Returns the {@link TreePath} to the passed {@code objectToFind} within the
    * passed {@link JTree}.
    *
    * @param theTree       the {@link JTree} to be searched for {@code objectToFind}
    * @param objectToFind  the object whose path is to be found in {@code theTree}
    *
    * @return  the {@link TreePath} to the passed object ({@code null} if it is not found})
    */
    private TreePath findObjectPathInTree( Object objectToFind ) {
        
        TreePath objectPath = null;
        if( objectToFind != null ) {

            DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode)getModel().getRoot();
            Enumeration<DefaultMutableTreeNode> e = rootNode.depthFirstEnumeration();
            DefaultMutableTreeNode currentNode;
            while( e.hasMoreElements() ) {
                currentNode = e.nextElement();
                if( objectToFind == currentNode.getUserObject() ) {
                    objectPath = new TreePath( currentNode.getPath() );
                    break;
                }
            }
        }

        return objectPath;
    }
    
    //===========================================================================
    /**
    * Selects the tree node containing the {@code objectToSelect}.  If the {@code objectToSelect}
    * is {@code null}, or it is not found in any tree node, the root node will be selected.
    *
    * @param objectToSelect  the object to be selected in the tree
    */
    public void setSelectedObject( Object objectToSelect ) {
        TreePath objectPath = findObjectPathInTree( objectToSelect );
        setSelectionPath( objectPath );      
    }

}
