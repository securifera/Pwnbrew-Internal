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
* GarbageCollector.java
*
* Created on Jun 14, 2013, 3:19:31 PM
*/

package pwnbrew.misc;

import pwnbrew.utilities.FileUtilities;
import pwnbrew.library.LibraryItemController;
import pwnbrew.gui.tree.MainGuiTreeModel;
import pwnbrew.gui.tree.LibraryItemJTree;
import pwnbrew.xmlBase.XmlBase;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import javax.swing.tree.DefaultMutableTreeNode;
import pwnbrew.host.HostController;
import pwnbrew.log.Log;

/**
 *
 *  
 */
public class GarbageCollector implements Runnable {

//    private final List<LibraryItemController> theControllerList;
    private final LibraryItemJTree theJTree;

    private static final String NAME_Class = GarbageCollector.class.getSimpleName();

    //=======================================================
    /**
     *  Constructor
     * 
     * @param passedJTree 
    */
    public GarbageCollector( LibraryItemJTree passedJTree ) {
//       theControllerList =  passedJTree.getLibraryItemControllers( null );
       theJTree = passedJTree;
    }

    //Cleans up unnecessary files in the data directory
    @Override
    public void run() {

       try {
//           File fileContentDir = Directories.getFileLibraryDirectory();
//           File[] libFiles = fileContentDir.listFiles();
//
//           //Loops through every file seeing if it is referenced by any file refs
//           //If not it removes it
//           //TODO Come back and consolidate this once Task and Script extend Job
//           for(File aFile : libFiles){
//               
//               boolean fileExists = false;
//               for(LibraryItemController aController : theControllerList){
//
//                  Object anObj = aController.getObject();
//                  if(anObj instanceof Script ){
//                     Script aScript = (Script)anObj;
//                     List<FileContentRef> theFileRefs = aScript.getFileContentRefList();
//                     for(FileContentRef aFCR : theFileRefs){
//                        if(aFile.getName().equals(aFCR.getFileHash())){
//                           fileExists = true;
//                           break;
//                        }
//                     }
//                     
//                  } else if(anObj instanceof Command){
//
//                     Command aTask = (Command)anObj;
//                     List<FileContentRef> theFileRefs = aTask.getFileContentRefList();
//                     for(FileContentRef aFCR : theFileRefs){
//                        if(aFile.getName().equals(aFCR.getFileHash())){
//                           fileExists = true;
//                           break;
//                        }
//                     }
//                  }
//                  
//                  //Break out if the file was already found
//                  if( fileExists ){
//                      break;
//                  }
//               }
//
//               //Delete if not referenced
//               if(!fileExists){
//                  FileUtilities.deleteFile(aFile);
//               }
//           }

           MainGuiTreeModel theModel = theJTree.getModel();
           if( theModel != null ) { //If the model was obtained...
               //Reorder the library
                DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) theModel.getRoot();
        
                //Loop throught the children and add the objects
                for( int i =0; i < rootNode.getChildCount(); i++  ){
                    
                    DefaultMutableTreeNode hostNode = (DefaultMutableTreeNode) rootNode.getChildAt(i);
                    Object anObj = hostNode.getUserObject();

                    //Get the host first
                    if( anObj instanceof HostController ){

                        HostController aHostController = (HostController)anObj;
                        orderLibrary( aHostController.getObjectLibraryDirectory(), hostNode );
                    }
                }
           }
           
       } catch (IOException ex) {
          Log.log(Level.WARNING, NAME_Class, "deleteFile()", ex.getMessage(), ex );
       }
    }

    //===============================================================
    /**
    * Reorders the objects in the object directory based on
    * their position in the tree model.
    *
    * @param saveDir
    * @return
    */
    private void orderLibrary( File saveDir, DefaultMutableTreeNode hostNode ) throws IOException {

        if( saveDir.exists()){
            List<File> libFiles = new ArrayList<>(Arrays.asList(saveDir.listFiles()));

            //Get the tree model
            MainGuiTreeModel theModel = theJTree.getModel();
            if( theModel != null ) { //If the model was obtained...

                //Get the root node
                if(hostNode != null){

                    for(int i = 0; i < theModel.getChildCount(hostNode); i++){

                    DefaultMutableTreeNode aChild = (DefaultMutableTreeNode)theModel.getChild(hostNode, i);
                    LibraryItemController aController = (LibraryItemController) aChild.getUserObject();
                    XmlBase aLibObj = (XmlBase)aController.getObject();

                    StringBuilder orderStr = new StringBuilder().append("_").append(aLibObj.getId());

                    File objFile = new File(saveDir, orderStr.toString());
                    orderStr.insert(0, Integer.toHexString(i));

                    if(objFile.exists()){
                        //Take it out of the list and rename it
                        libFiles.remove(objFile);
                        File dstFile = new File(saveDir, orderStr.toString());
                        if(!objFile.renameTo(dstFile)){
                            //throw new IOException("Unable to rename file " +  orderStr.toString());
                            FileUtilities.moveFile(objFile, dstFile);
                        }
                    } else {

                        //Check if the correctly ordered file exists
                        objFile = new File(saveDir, orderStr.toString());
                        if(objFile.exists()){

                            libFiles.remove(objFile);

                        } else {

                            //Loop through and find the correct file and rename it
                            for( File nextFile : libFiles){

                                //If the filename contains the objid, rename and remove from list
                                if(nextFile.getName().contains(aLibObj.getId())){

                                    File dstFile = new File(saveDir, orderStr.toString());
                                    if(!nextFile.renameTo(dstFile)){
                                        //throw new IOException("Unable to rename file " +  orderStr.toString());
                                        FileUtilities.moveFile(nextFile, dstFile);
                                    }
                                    libFiles.remove(nextFile);
                                    break;
                                }
                            }
                        }

                    }
                }
            }

            //Remove any files left in the list that didn't match anything
            for( File nextFile : libFiles){

                String saveDirName = saveDir.getName();
                String nextFileName = nextFile.getName();
                if(!nextFile.isDirectory() && !saveDirName.contains( nextFileName )){
                    FileUtilities.deleteFile(nextFile);
                }

            }
         }
     }

  }

}/* END CLASS GarbageCollector */
