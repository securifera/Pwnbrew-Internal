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
package pwnbrew.xml.maltego;

import pwnbrew.xml.XmlObject;

/**
 *
 * @author Securifera
 */
public class Field extends XmlObject {
    
    private static final String ATTRIBUTE_Name = "Name";
    private static final String ATTRIBUTE_DisplayName = "DisplayName";
    private static final String ATTRIBUTE_MatchingRule = "MatchingRule";
    
    public static final String STRICT_MATCHING = "strict";
    
    // ==========================================================================
    /**
    * Constructor
    */
    public Field( String passedName ) {
        theAttributeMap.put( ATTRIBUTE_Name, passedName  );
        theAttributeMap.put( ATTRIBUTE_DisplayName, ""  );
    }
    
     //===========================================================================
    /**
     * 
     * @param passedValue 
     */
    public void setStrictMatching() {
        String attr = getAttribute( ATTRIBUTE_MatchingRule );
        if( attr == null )
            theAttributeMap.put( ATTRIBUTE_MatchingRule, STRICT_MATCHING  );
        else 
            setAttribute(ATTRIBUTE_MatchingRule, STRICT_MATCHING);
    }

    //===========================================================================
    /**
     * Sets the display name
     * @param passedStr 
     */
    public void setDisplayName(String passedStr ) {
        setAttribute(ATTRIBUTE_DisplayName, passedStr);
    }
   
}
