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
 * IconData.java
 *
 * Created on December 22, 2013
 */

package pwnbrew.host.gui;

import javax.swing.Icon;

/**
 *
 *  
 */
public class IconData {
    
    protected Icon   theIcon;
    protected Icon   theDisabledIcon;
    protected Icon   theExpandedIcon;
    protected Object theObject;

    //==========================================================================
    /**
     *  Constructor
     * @param icon
     * @param disabledIcon
     * @param data 
     */
    public IconData(Icon icon, Icon disabledIcon, Object data){
        theIcon = icon;
        theDisabledIcon = disabledIcon;
        theObject = data;
    }

    //==========================================================================
    /**
     *  Constructor
     * @param icon
     * @param disabledIcon
     * @param expandedIcon
     * @param data 
     */
    public IconData(Icon icon, Icon disabledIcon, Icon expandedIcon, Object data) {
        this(icon, disabledIcon, data);
        theExpandedIcon = expandedIcon;
    }

    //==========================================================================
    /**
     * Get icon.
     * @param isEnabled
     * @return 
     */
    public Icon getIcon( boolean isEnabled ){ 
        Icon anIcon = theIcon;
        if( !isEnabled){
            anIcon = theDisabledIcon;
        }
        return anIcon;
    }

    //==========================================================================
    /**
     *  Get the expanded icon
     * @return 
     */
    public Icon getExpandedIcon(){ 
        return theExpandedIcon!=null ? theExpandedIcon : theIcon;
    }

    //==========================================================================
    /**
     * Get the object
     * @return 
     */
    public Object getObject(){ 
        return theObject;
    }

    //==========================================================================
    /**
     *  Get the string representation of the icon
     * @return 
     */
    @Override
    public String toString(){ 
        return theObject.toString();
    }
}

