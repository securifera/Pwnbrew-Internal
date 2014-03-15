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
 * Job.java
 *
 * Created on June 23, 2013, 4:21:34 PM
 */

package pwnbrew.xmlBase.job;

import pwnbrew.xmlBase.DescriptiveXmlBase;
import pwnbrew.xmlBase.FileContentRef;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import pwnbrew.execution.ExecutableItem;
import pwnbrew.library.LibraryItem;
import pwnbrew.xmlBase.XmlBase;


/**
 *  
 */
abstract public class Job extends DescriptiveXmlBase implements ExecutableItem, LibraryItem {

    protected final Map<String, FileContentRef> theFileContentRefMap = new HashMap<>();
    public static final String ATTRIBUTE_LastRunDate = "LastRunDate";
    public static final String ATTRIBUTE_LastRunResult = "LastRunResult";
    
    // ========================================================================
    /**
     * Constructor
     */
    public Job() {
        
        theAttributeMap.put( ATTRIBUTE_LastRunDate, "" );
        theAttributeMap.put( ATTRIBUTE_LastRunResult, "" );
    
    }/* END CONSTRUCTOR() */
    
    // ==========================================================================
    /**
    * Returns a list of the {@link FileContentRef}s.
    *
    * @return a list of the {@link FileContentRef}s
    */
    @Override
    public Map<String, FileContentRef> getFileContentRefMap() {

        return new HashMap<>( theFileContentRefMap );
        
    }/* END getFileContentRefMap() */
    
    // ==========================================================================
    /**
    * Adds and updates local support objects, determining the appropriate manner
    * in which to do so according to the class of the <code>passedGRB</code> argument.
    *
    * @param xmlBase the support object to be added/updated
    */
    @Override
    public void addUpdateComponent( XmlBase xmlBase ) {
        if( xmlBase instanceof FileContentRef ) { //If the XmlBase is a FileContentRef...

            FileContentRef aFCR = (FileContentRef)xmlBase;
            theFileContentRefMap.put(aFCR.getFileName(), aFCR);
        } else {
            super.addUpdateComponent( xmlBase );
        }
    }
    
    // ==========================================================================
    /**
    *  Removes a supporting object from the XmlBase
    *
    *  @param passedGRB  the object to be removed
    *
    *  @return true if the object was successfully removed
    */
    @Override
    public boolean removeComponent( XmlBase passedGRB ) {
        boolean objectRemoved = false;

        if( passedGRB instanceof FileContentRef ) { //If the object is a Parameter...
            FileContentRef aFCR = (FileContentRef)passedGRB;
            theFileContentRefMap.remove( aFCR.getFileName());
        } else {
            super.removeComponent(passedGRB);
        }

        return objectRemoved;
    }/* END removeComponent() */
    
     // ==========================================================================
    /**
     * Returns a list of {@link Task}'s subcomponents that should be added to its
     * XML data.
     * <p>
     * This method will return at least an empty {@link ArrayList}.
     *
     * @return an {@code ArrayList} of {@link XmlBase} components
     */
    @Override //XmlBase
    public List<XmlBase> getXmlComponents() {

        List<XmlBase> rtnList = super.getXmlComponents();
        rtnList.addAll( theFileContentRefMap.values() );

        return rtnList;

    }/* END getXmlComponents() */
    
    
    // ==========================================================================
    /**
    * Replaces the current {@link FileContentRef} with the given one.
    * <p>
    * If the given {@code FileContentRef} is null, this method does nothing.
    *
     * @param passedFileContentRef
    */
    public void setFileContentRef( FileContentRef passedFileContentRef ) {

        if( passedFileContentRef == null ) { //If the ScriptFileContentRef is null...
            return; //Do nothing
        }

        //Remove the existing ScriptFileContentRef...
        synchronized( theFileContentRefMap ){
            theFileContentRefMap.put(passedFileContentRef.getName(), passedFileContentRef);
        }

    }/* END setFileContentRef( ScriptFileContentRef ) */

    
}/* END CLASS Job */
