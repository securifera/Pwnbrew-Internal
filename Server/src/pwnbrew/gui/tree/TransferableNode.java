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
 * TransferableNode
 *
 * Created on Dec 11, 2013, 7:28 PM
 *
 */

package pwnbrew.gui.tree;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javax.swing.tree.TreePath;

/**
 *
 */
@SuppressWarnings("ucd")
public class TransferableNode implements Transferable {

    private final static DataFlavor TREE_PATH_FLAVOR = new DataFlavor(TreePath.class, "Tree Path");

    private final DataFlavor theFlavors[] = { TREE_PATH_FLAVOR, DataFlavor.javaFileListFlavor };
    private final List<TreePath> theTreePathList;

    //==========================================================================
    /**
    * Constructor
    * @param passedTreePathList 
    */
    TransferableNode(List<TreePath> passedTreePathList) {
        theTreePathList = passedTreePathList;
    }

    //==========================================================================
    /**
     * 
     * @return 
     */
    @Override
    public synchronized DataFlavor[] getTransferDataFlavors() {
        return Arrays.copyOf(theFlavors, theFlavors.length);
    }

    //==========================================================================
    /**
     * 
     * @param passedFlavor
     * @return 
     */
    @Override
    public boolean isDataFlavorSupported(DataFlavor passedFlavor) {
        boolean isSupported = false;
        for(DataFlavor aFlavor: theFlavors){
            if(passedFlavor.equals(aFlavor)){
                isSupported = true;
                break;
            }
        }
        return isSupported;
    }

    @Override
    public synchronized Object getTransferData(DataFlavor passedFlavor) throws UnsupportedFlavorException, IOException {
        if (isDataFlavorSupported(passedFlavor))
            return (Object) theTreePathList.toArray();
        else
            throw new UnsupportedFlavorException(passedFlavor);
        
    }
}
