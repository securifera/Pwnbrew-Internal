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
 * JobSet.java
 *
 * Created on July 20, 2013, 6:38:45 PM
 */

package pwnbrew.xmlBase.job;

import pwnbrew.controllers.JobSetController;
import pwnbrew.library.LibraryItemControllerListener;
import pwnbrew.library.LibraryItem;
import pwnbrew.xmlBase.DescriptiveXmlBase;
import pwnbrew.xmlBase.XmlBase;
import java.util.ArrayList;
import java.util.List;


/**
 * 
 */
public class JobSet extends DescriptiveXmlBase implements LibraryItem {

    private static final String ATTRIBUTE_RebootAfter = "RebootAfter";
    private static final String ATTRIBUTE_StopOnError = "StopOnError";
    private static final String ATTRIBUTE_RunConcurrently = "RunConcurrently";

    private List<JobRef> theJobRefList = null;
    
    
    // ==========================================================================
    /**
     * Creates a new instance of {@link JobSet}.
     */
    public JobSet() {

        theAttributeMap.put( ATTRIBUTE_RebootAfter, Boolean.toString( false ) );
        theAttributeMap.put( ATTRIBUTE_StopOnError, Boolean.toString( false ) );
        theAttributeMap.put( ATTRIBUTE_RunConcurrently, Boolean.toString( false ) );

        theJobRefList = new ArrayList<>();
        
    }/* END CONSTRUCTOR() */
    
    
    // ========================================================================
    /**
     * 
     * @param reboot
     */
    public void setRebootOnCompletion( boolean reboot ) {
        setAttribute( ATTRIBUTE_RebootAfter, Boolean.toString( reboot ) );
    }/* END setRebootAfter( boolean ) */
    
    
    // ========================================================================
    /**
     * 
     * @return 
     */
    public boolean rebootsAfter() {
        return Boolean.parseBoolean( getAttribute( ATTRIBUTE_RebootAfter ) );
    }/* END rebootsAfter() */
    
    
    // ========================================================================
    /**
     * 
     * @param stop
     */
    public void setStopOnError( boolean stop ) {
        setAttribute( ATTRIBUTE_StopOnError, Boolean.toString( stop ) );
    }/* END setStopOnError( boolean ) */
    
    
    // ========================================================================
    /**
     * 
     * @return 
     */
    public boolean stopsOnError() {
        return Boolean.parseBoolean( getAttribute( ATTRIBUTE_StopOnError ) );
    }/* END stopsOnError() */
    
    
    // ========================================================================
    /**
     * 
     * @param concurrent
     */
    public void setRunConcurrently( boolean concurrent ) {
        setAttribute( ATTRIBUTE_RunConcurrently, Boolean.toString( concurrent ) );
    }/* END setRunConcurrently( boolean ) */
    
    
    // ========================================================================
    /**
     * 
     * @return 
     */
    public boolean runsConcurrently() {
        return Boolean.parseBoolean( getAttribute( ATTRIBUTE_RunConcurrently ) );
    }/* END runsConcurrently() */
    
    
    // ========================================================================
    /**
     * 
     * @return 
     */
    @Override //XmlBase
    public List<XmlBase> getXmlComponents() {
        
        List<XmlBase> rtnList = super.getXmlComponents();
        
        if( theJobRefList != null && theJobRefList.isEmpty() == false )
            rtnList.addAll( theJobRefList );
        
        return rtnList;
        
    }/* END getXmlComponents() */
    
    
    // ========================================================================
    /**
     * 
     * @return 
     */
    public List<JobRef> getJobRefList() {
        
        List<JobRef> rtnList = new ArrayList<>();
        rtnList.addAll( theJobRefList );
        return rtnList;
        
    }/* END getJobRefList() */   
    
    
    // ========================================================================
    /**
     * Returns the {@link JobRef} for the {@link Job} that has the given name and
     * type.
     * <p>
     * If either argument is null this method does nothing and returns null.
     * 
     * @param jobName the {@code Job}'s name
     * @param jobType the {@code Job}'s type
     * 
     * @return the {@link JobRef} for the {@link Job} that has the given name and
     * type; null if the {@link JobSet} has no such {@code JobRef}
     */
    public JobRef getJobRef( String jobName, String jobType ) {
        
        if( jobName == null || jobType == null ) return null;
        
        JobRef rtnJR = null;
        
        for( JobRef aJR : theJobRefList ) {
            
            if( jobName.equals( aJR.getJobName() )
                    && jobType.equals( aJR.getJobType() ) ) {
                rtnJR = aJR; //Return the JobRef
                break; //Stop iterating through the JobRefs
            }
            
        }
        
        return rtnJR;
        
    }/* END getJobRef( String, String ) */
    

    // ========================================================================
    /**
     * Adds the given {@link XmlBase} to the {@link JobSet} or updates an existing
     * component.
     *
     * @param component the component to add/update
     */
    @Override //XmlBase
    public void addUpdateComponent( XmlBase component ) {
        
        if( component instanceof JobRef ) { //If the XmlBase is a JobRef...
            theJobRefList.add( (JobRef)component );
        } else {
            super.addUpdateComponent( component );
        }
        
    }/* END addUpdateComponent( XmlBase ) */

    
    // ========================================================================
    /**
     * Adds the given {@link JobRef} to the {@link JobSet}.
     * <p>
     * If the {@code JobSet} already has the given {@code JobRef} this method will
     * move the {@code JobRef} to the end of the collection.
     * <p>
     * If the argument is null this method does nothing.
     * 
     * @param jobRef 
     */
    public void addJobRef( JobRef jobRef ) {
        
        if( jobRef == null ) return;
        
        theJobRefList.remove( jobRef );
        theJobRefList.add( jobRef );
        
    }/* END addJobRef( JobRef ) */
    
    
    // ========================================================================
    /**
     * Adds the given {@link JobRef} to the {@link JobSet} at the given index.
     * <p>
     * If the given {@code JobRef} is null this method does nothing. If the given
     * index is less than 0 this method does nothing. If the given index is greater
     * than the upper bound the {@code JobRef} is added at the end position.
     * 
     * @param jobRef
     * @param index
     */
    public void insertJobRef( JobRef jobRef, int index ) {
        
        if( jobRef == null ) return;
        
        if( index < 0 ) return;
        
        theJobRefList.remove( jobRef );
        
        if( index > theJobRefList.size() )
            theJobRefList.add( jobRef );
        else
            theJobRefList.add( index, jobRef );
        
    }/* END insertChild( LibraryItemController, int ) */
    
    
    
    
    
    // ========================================================================
    /**
     * Removes the first occurring {@link JobRef} with the given name and type.
     * <p>
     * If either argument is null this method does nothing.
     * 
     * @param jobName the name of the job
     * @param jobType the type of job
     */
    public void removeJobRef( String jobName, String jobType ) {
        
        if( jobName == null || jobType == null ) return;
        
        for( JobRef aJR : theJobRefList ) {
            
            if( jobName.equals( aJR.getJobName() )
                    && jobType.equals( aJR.getJobType() ) ) {
                theJobRefList.remove( aJR );
                break;
            }
            
        }
        
    }/* END removeJobRef( JobRef ) */
    
    // ========================================================================
    /**
     * Creates a new JobSetController and assigns this object to it.
     * 
     * @param passedListener
     * @return 
     */
    @Override
    public JobSetController instantiateController( LibraryItemControllerListener passedListener ) {
        return new JobSetController( this, passedListener );
    }
    
}/* END CLASS JobSet */
