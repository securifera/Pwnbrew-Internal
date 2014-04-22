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
package pwnbrew.xmlBase;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import pwnbrew.misc.Directories;

/**
 *
 * @author Securifera
 */
public class JarItem extends FileContentRef {
    
    //The hash of the file being referenced and the name
    private static final String theJavaMajorVersion  = "JavaMajorVersion";
    private static final String theJarVersion = "JarVersion";
    private static final String theJarType = "JarType";
    
    public static final String PAYLOAD_TYPE = "PAYLOAD";
    public static final String STAGER_TYPE = "STAGER";
    public static final String LOCAL_EXTENSION_TYPE = "LOCAL EXTENSION";
    public static final String REMOTE_EXTENSION_TYPE = "REMOTE EXTENSION";
    
    //=========================================================================
    /**
     * Constructor
     */
    public JarItem() {
       theAttributeMap.put( theJavaMajorVersion, "");    
       theAttributeMap.put( theJarVersion,  ""  );
       theAttributeMap.put( theJarType,  ""  );
    }  
    
    //=====================================================================
    /**
     * 
     * @return 
     */
    public String getJvmMajorVersion() {
        return getAttribute(theJavaMajorVersion);
    }

    //=====================================================================
    /**
     * 
     * @param passedVersion 
     */
    public void setJvmMajorVersion( String passedString ) {
        setAttribute(theJavaMajorVersion, passedString );
    }
    
    //=====================================================================
    /**
     * 
     * @return 
     */
    public String getType() {
        return getAttribute(theJarType);
    }
    
    //=====================================================================
    /**
     * 
     * @return 
     */
    static public List<String> getTypes() {
        return Arrays.asList( new String[]{ PAYLOAD_TYPE, STAGER_TYPE, LOCAL_EXTENSION_TYPE, REMOTE_EXTENSION_TYPE} );
    }

    //=====================================================================
    /**
     * 
     * @param passedVersion 
     */
    public void setType( String passedString ) {
        setAttribute(theJarType, passedString );
    }

    //=====================================================================
    /**
     * 
     * @return 
     */
    public String getVersion() {
        return getAttribute(theJarVersion);
    }

    //=====================================================================
    /**
     * 
     * @param passedVersion 
     */
    public void setVersion( String passedVersion ) {
        setAttribute(theJarVersion, passedVersion );
    }    
    
    //=====================================================================
    /**
     * Return the name
     * @return 
     */
    @Override
    public String toString(){
        return getName();
    }
    
    //=====================================================================
    /**
     * 
     */    
    public void writeSelfToDisk() throws IOException{        
        File saveDir = new File( Directories.getJarLibPath() );
        writeSelfToDisk(saveDir, -1);
    }
    
}
