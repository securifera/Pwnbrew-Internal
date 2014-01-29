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
 * FileNode.java
 *
 * Created on December 22, 2013
 */


package pwnbrew.host.gui;

import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import pwnbrew.gui.Iconable;
import pwnbrew.network.control.messages.FileSystemMsg;
import pwnbrew.utilities.Utilities;

/**
 *
 *  
 */
public class FileNode implements Comparable{
    
    private static final String Harddisk_Img_File_Name = "harddisk.png";
    private static final BufferedImage HostBuffImage = Utilities.loadImageFromJar( Harddisk_Img_File_Name );	
    public static final Icon DISK_ICON = new ImageIcon( HostBuffImage.getScaledInstance(
            Iconable.ICON_WIDTH, Iconable.ICON_HEIGHT, Image.SCALE_SMOOTH ) );
    
    private static final String Folder_Img_File_Name = "folder_yellow.png";
    private static final BufferedImage FolderBuffImage = Utilities.loadImageFromJar( Folder_Img_File_Name );	
    public static final Icon FOLDER_ICON = new ImageIcon( FolderBuffImage.getScaledInstance(
            Iconable.ICON_WIDTH, Iconable.ICON_HEIGHT, Image.SCALE_SMOOTH ) );
    
    private static final String FolderExp_Img_File_Name = "folder_open.png";
    private static final BufferedImage FolderOpenBuffImage = Utilities.loadImageFromJar( FolderExp_Img_File_Name );	
    public static final Icon OPEN_FOLDER_ICON = new ImageIcon( FolderOpenBuffImage.getScaledInstance(
            Iconable.ICON_WIDTH, Iconable.ICON_HEIGHT, Image.SCALE_SMOOTH ) );
    
    protected RemoteFile theFile;
    private final byte fileType;
    private final long fileSize;
    private final String lastModified;
    

    //=========================================================================
    /**
     * Constructor 
     * @param passedFile
     * @param passedType 
     * @param passedSize 
     * @param passedDate 
     */
    public FileNode(RemoteFile passedFile, byte passedType, long passedSize, String passedDate ){
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
                break;
        }
        return anIcon;
    }

}
