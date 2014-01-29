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
* FileTransferHandler.java
*
* Created on June 26, 2013, 7:21:42 PM
*/

package pwnbrew.generic;

import pwnbrew.logging.Log;
import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTargetEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Level;
import javax.swing.TransferHandler;
import pwnbrew.generic.gui.GenericProgressDialog;
import pwnbrew.misc.ProgressDriver;

/**
 *
 *  
 */
public class FileTransferHandler extends TransferHandler {

    private final ProgressDriver theProgressLogic;
    private static final String NAME_Class = FileTransferHandler.class.getSimpleName();

    //===========================================================================
    /**
     *  Constructor
     * 
     * @param passedProgressLogic 
     */
    public FileTransferHandler(ProgressDriver passedProgressLogic) {
       theProgressLogic = passedProgressLogic;
    }

    //===========================================================================
    /**
     *  Called to decided if the object being dragged can be dropped into the tree
     * @param info
     * @return 
    */
    @Override
    public boolean canImport(TransferHandler.TransferSupport info) {

        if (!info.isDrop()) {
            return false;
        }
        return true;
     }

    //===========================================================================
    /**
     *  Handles the dropping of objects in the JTree
     * @param info
     * @return 
     */
    @Override
     public boolean importData(TransferHandler.TransferSupport info) {

        Transferable theTransferable = info.getTransferable();
        try {
           Object theObject = theTransferable.getTransferData(DataFlavor.javaFileListFlavor);

           //Drag and Drop from outside the application
           if(theObject instanceof List){
              List<File> fileList = (List<File>)theObject;
              GenericProgressDialog pDialog = new GenericProgressDialog(null, "Importing files to library...", theProgressLogic, false, fileList);
              pDialog.setVisible(true);
           }
           
        } catch (IOException | UnsupportedFlavorException ex) {
           Log.log(Level.WARNING, NAME_Class, "importData()", ex.getMessage(), ex );
        }

        cancelDnD(info);
        return false;

     }

    private void cancelDnD(TransferSupport support) {

        //TransferSupport.setDNDVariables(Component component, DropTargetEvent event)
        //Call setDNDVariables(null, null) to free the component.
        try{
            Method m = support.getClass().getDeclaredMethod("setDNDVariables", new Class[] { Component.class, DropTargetEvent.class });
            m.setAccessible(true);
            m.invoke(support, null, null);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException ex) {
            Log.log(Level.INFO, NAME_Class, "cancelDnD()", ex.getMessage(), ex );
        }
    }


}/* END CLASS FileTransferHandler */
