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

package pwnbrew.filesystem;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import pwnbrew.misc.Constants;
import pwnbrew.misc.Iconable;
import pwnbrew.misc.Utilities;
import pwnbrew.network.control.messages.FileSystemMsg;

/**
 *
 *  
 */
public class FileNode implements Comparable{
    
    private static final String Harddisk_Img_File_Name = "harddisk.png";
    private static final BufferedImage HardDiskBuffImage = Utilities.loadImageFromJar( Harddisk_Img_File_Name );	
    public static final Icon DISK_ICON = new ImageIcon( HardDiskBuffImage.getScaledInstance(
            Iconable.ICON_WIDTH, Iconable.ICON_HEIGHT, Image.SCALE_SMOOTH ) );
    
    private static final String File_Img_File_Name = "file.png";
    private static final BufferedImage FileBuffImage = Utilities.loadImageFromJar( File_Img_File_Name );	
    public static final Icon FILE_ICON = new ImageIcon( FileBuffImage.getScaledInstance(
            Iconable.ICON_WIDTH, Iconable.ICON_HEIGHT, Image.SCALE_SMOOTH ) );
    
    public static final BufferedImage FolderBuffImage = Utilities.loadImageFromJar( Constants.FOLDER_IMG_STR );	
    public static final Icon FOLDER_ICON = new ImageIcon( FolderBuffImage.getScaledInstance(
            Iconable.ICON_WIDTH, Iconable.ICON_HEIGHT, Image.SCALE_SMOOTH ) );
    
    private static final BufferedImage FolderOpenBuffImage = Utilities.loadImageFromJar( Constants.FOLDER_OPEN_IMG_STR );	
    public static final Icon OPEN_FOLDER_ICON = new ImageIcon( FolderOpenBuffImage.getScaledInstance(
            Iconable.ICON_WIDTH, Iconable.ICON_HEIGHT, Image.SCALE_SMOOTH ) );
    
    //Image and icon...
    public static final Icon HOST_ICON = new ImageIcon( Utilities.loadImageFromJar( Constants.HOST_IMG_STR ).getScaledInstance(
            Iconable.ICON_WIDTH, Iconable.ICON_HEIGHT, Image.SCALE_SMOOTH ) );
      
    protected RemoteFile theFile;
    private final byte fileType;
    private final long fileSize;
    private final String lastModified;
    
    //The children nodes
    private final List<FileNode> childrenFileNodes = new ArrayList<>();
    

    //=========================================================================
    /**
     * Constructor 
     * @param passedFile
     * @param passedType 
     * @param passedSize 
     * @param passedDate 
     */
    public FileNode( RemoteFile passedFile, byte passedType, long passedSize, String passedDate ){
        theFile = passedFile;
        fileType = passedType;
        fileSize = passedSize;
        lastModified = passedDate;
    }

    //=========================================================================
    /**
     *  Get the type of the file node.
     * @return 
     */
    public byte getType(){ 
        return fileType;
    }
    
    //=========================================================================
    /**
     *  Get the last modified date.
     * @return 
     */
    public String getLastModified(){ 
        return lastModified;
    }
    
     //=========================================================================
    /**
     *  Get the size of the file.
     * @return 
     */
    public long getSize(){ 
        return fileSize;
    }
    
    //=========================================================================
    /**
     *  Get the file
     * @return 
     */
    public RemoteFile getFile(){ 
        return theFile;
    }
    
    //=========================================================================
    /**
     *  Clears the child node list
     */
    public void clearChildNodes(){
        synchronized(childrenFileNodes){
            childrenFileNodes.clear();
        }
    }
    
     //=========================================================================
    /**
     *  Clears the child node list
     * @return 
     */
    public List<FileNode> getChildNodes(){
        synchronized(childrenFileNodes){
            return new ArrayList<>(childrenFileNodes);
        }
    }
    
    //=========================================================================
    /**
     * 
     * @param passedNode 
     */
    public void addChildNode( FileNode passedNode ){
        synchronized(childrenFileNodes){
            childrenFileNodes.add(passedNode);
        }
    }

    //=========================================================================
    /**
     *  Get the to string representation.
     * @return 
     */
    @Override
    public String toString() { 
        return theFile.getName().length() > 0 ? theFile.getName() : theFile.getAbsolutePath();
    }

    //=========================================================================
    /**
     *  Return if the file has sub directories.
     * @return 
     */
    public boolean isDirectory(){
        return (fileType == FileSystemMsg.FOLDER);
    }
  
    //==========================================================================
    /**
     *  Compare the files
     * @param toCompare
     * @return 
     */
    @Override
    public int compareTo(Object toCompare){ 
        if( toCompare instanceof FileNode ){
            FileNode otherNode = (FileNode)toCompare;
            if( otherNode.fileType == fileType ){
                return theFile.getName().compareToIgnoreCase( otherNode.theFile.getName() ); 
            } else {
                if( fileType == FileSystemMsg.FOLDER && otherNode.fileType == FileSystemMsg.FILE ){
                    return -1;
                } else if( fileType == FileSystemMsg.FILE && otherNode.fileType == FileSystemMsg.FOLDER ){
                    return 1;
                }
            }
        }
        return 0; 
    }

    //==========================================================================
    /**
     * 
     * @return 
     */
    public boolean isDrive() {
         return (fileType == FileSystemMsg.DRIVE);
    }

    //==========================================================================
    /**
     *  Get icon for the type of node
     * @return 
     */
    public Icon getIcon() {
        
        Icon anIcon = null;
        switch(fileType){
            case FileSystemMsg.DRIVE:
                anIcon = DISK_ICON;
                break;
            case FileSystemMsg.FOLDER:
                anIcon = FOLDER_ICON;
                break;
            case FileSystemMsg.FILE:
                anIcon = FILE_ICON;
                break;
        }
        return anIcon;
    }
    
     //==========================================================================
    /**
     *  Get icon for the type of node
     * @return 
     */
    public Icon getExpandedIcon() {
        
        Icon anIcon = null;
        switch(fileType){
            case FileSystemMsg.FOLDER:
                anIcon = OPEN_FOLDER_ICON;
                break;            
        }
        return anIcon;
    }

}
