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
 * JobRef.java
 *
 * Created on July 30, 2013, 10:01:21 PM
 */

package pwnbrew.xmlBase.job;

import pwnbrew.xmlBase.XmlBase;


/**
 * 
 */
public class JobRef extends XmlBase {
    
    private static final String ATTRIBUTE_JobName = "JobName";
    private static final String ATTRIBUTE_JobType = "JobType";
    
    
    // ========================================================================
    /**
     * Creates a new instance of {@link JobRef}.
     */
    public JobRef() {
        
        theAttributeMap.put( ATTRIBUTE_JobName, "" );
        theAttributeMap.put( ATTRIBUTE_JobType, "" );
        
    }/* END CONSTRUCTOR() */

    
    // ========================================================================
    /**
     * Creates a new instance of {@link JobRef}.
     * 
     * @param jobName the {@link Job}'s name
     * @param jobType the {@link Job}'s type
     */
    public JobRef( String jobName, String jobType ) {
        
        theAttributeMap.put( ATTRIBUTE_JobName, ( jobName == null ? "" : jobName ) );
        theAttributeMap.put( ATTRIBUTE_JobType, ( jobType == null ? "" : jobType ) );
        
    }/* END CONSTRUCTOR( String, String ) */


    // ==========================================================================
    /**
     * Sets the {@link JobRef}'s Job name to the given String.
     * <p>
     * If the given {@code String} is null, the name is set to the empty String.
     *
     * @param name the new name
     */
    public void setJobName( String name ) {
        setAttribute( ATTRIBUTE_JobName, ( name == null ? "" : name ) );
    }/* END setJobName( String ) */


    // ==========================================================================
    /**
     * Returns the {@link JobRef}'s Job name.
     *
     * @return the {@code JobRef}'s Job name
     */
    public String getJobName() {
        return getAttribute( ATTRIBUTE_JobName );
    }/* END getJobName() */


    // ==========================================================================
    /**
     * Sets the {@link JobRef}'s Job type to the given String.
     * <p>
     * If the given {@code String} is null, the type is set to the empty String.
     *
     * @param type the new type
     */
    public void setJobType( String type ) {
        setAttribute( ATTRIBUTE_JobType, ( type == null ? "" : type ) );
    }/* END setJobType( String ) */


    // ==========================================================================
    /**
     * Returns the {@link JobRef}'s Job type.
     *
     * @return the {@code JobRef}'s Job type
     */
    public String getJobType() {
        return getAttribute( ATTRIBUTE_JobType );
    }/* END getJobType() */

}/* END CLASS JobRef */
