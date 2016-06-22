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
 * FileContentRef.java
 *
 * Created on June 23, 2013, 8:47 PM
 */

package pwnbrew.xmlBase;

import pwnbrew.log.LoggableException;
import java.io.File;
import pwnbrew.misc.Directories;
import pwnbrew.utilities.FileUtilities;


/**
 *
 *  
 */
public class FileContentRef extends XmlBase{


    //The hash of the file being referenced and the name
    public static final String ATTRIBUTE_FileHash  = "FileHash";
    private static final String ATTRIBUTE_ModifiedDate = "ModifiedDate";
  
    //=========================================================================
    /**
     * Constructor
     */
    public FileContentRef() {
       theAttributeMap.put( ATTRIBUTE_FileHash, "");
       
       //Add the attributes
       theAttributeMap.put( ATTRIBUTE_ModifiedDate,  ""  );
    }    
    
    //===============================================================
    /**
    * Returns the hash for the file associated with this file reference
    *
    * @return
    */
    public String getFileHash(){
        return getAttribute(ATTRIBUTE_FileHash);
    }
    
     //===============================================================
    /**
     *  Sets the file hash
     * 
     * @param passedString 
     */
    public void setFileHash( String passedString) {
        setAttribute( ATTRIBUTE_FileHash, passedString);
    }
    
    //===============================================================
    /**
     *  Set the modified
     * 
     * @return 
     */
    public String getModifiedDate(){
        return getAttribute( ATTRIBUTE_ModifiedDate );
    }
    
    //===============================================================
    /**
     *  Sets the modified date.
     * 
     * @param passedDate 
     */
    public void setModifiedDate( String passedDate) {
        setAttribute( ATTRIBUTE_ModifiedDate, passedDate);
    }
  
    // ========================================================================
    /**
     * 
     * @return 
     */
    public String getFilename() {
        return getAttribute( ATTRIBUTE_Name );
    }/* END getFilename() */
  
    //===============================================================
    /**
     *  Sets the modified date.
     * 
     * @param passedName 
     */
    public void setFilename( String passedName) {
        setAttribute( ATTRIBUTE_Name, passedName);
    }

    // ==========================================================================
    /**
    * Builds a {@link FileContent} container for the given reference
    *
    * @return the {@code FileContent} referenced by the {@code FileContentRef}, null
    * if the {@code FileContent} could not be created
     * @throws pwnbrew.logging.LoggableException
    */
    public FileContent getFileContent() throws LoggableException {

        FileContent rtnFileContent = null;
        try {
            
            rtnFileContent = (FileContent) XmlBaseFactory.instantiateClass( FileContent.class );
            rtnFileContent.setName(getName()); //Convert the file's contents to base-64
            rtnFileContent.setId(getId());
            rtnFileContent.setFileHash(getFileHash());
            rtnFileContent.setModifiedDate( getModifiedDate());
            
        } catch (IllegalAccessException | InstantiationException ex) {
            throw new LoggableException(ex);
        }
        return rtnFileContent;
        
    }/* END getFileContent() */


    // ==========================================================================
    /**
    * Deletes the {@link FileContent} referenced by this {@link FileContentRef}
    * from the library.
    */
    public void deleteFileContentFromLibrary() {

        String theHash = getFileHash(); //Get the instance id
        //NOTE: The FileContentRef's instance id is actually the id of the FileContent
        //  it references.

        if( !theHash.isEmpty()) { //If an id was obtained...
            //NOTE: The FileContentRef may be a quasi-FileContentRef. If the file related
            //  to this FileContentRef was not found at the time the FileContentRef was
            //  created, no FileContent could have been created, thus there would have
            //  been no instance id to place in this FileContentRef's instance id member.

            File objDir = Directories.getFileLibraryDirectory();
            File theFileToDelete = new File( objDir, theHash );
            FileUtilities.deleteFile( theFileToDelete );

        }

    }/* END deleteFileContentFromLibrary() */

}
