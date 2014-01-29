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
 * Colors.java
 *
 * Created on July 18, 2013, 11:22:12PM
 */

package pwnbrew.look;

import java.awt.Color;


/**
 *
 */
public class Colors {

    //Background colors for InputDevices...
    public static final Color COLOR_InputDeviceBackground_Normal = Color.WHITE;
    public static final Color COLOR_InputDeviceBackground_Disabled = new Color( 0xDFDFDF );
    public static final Color COLOR_InputDeviceBackground_Invalid = Color.RED;
    public static final Color COLOR_InputDeviceBackground_DisabledAndInvalid =
            COLOR_InputDeviceBackground_Invalid.darker();
    
    public static final Color PANEL_BASE_COLOR = new Color(0xBC9F6F);
//    public static final Color PANEL_BASE_COLOR = new Color(0x6F6F5F);
    public static final Color THEME_PRIMARY_COLOR = new Color(0xA58A66);
//    public static final Color THEME_PRIMARY_COLOR = new Color(0x3F3F3F);
    public static final Color THEME_SECONDARY_COLOR = new Color(0x967C5E);
//    public static final Color THEME_SECONDARY_COLOR = new Color(0x767676);
    public static final Color THEME_HIGHLIGHT_COLOR = new Color(0x8E7657);
//    public static final Color THEME_HIGHLIGHT_COLOR = new Color(0x5F5F5F);
    public static final Color THEME_GRADIENT_COLOR = new Color(0xE5B380);
//    public static final Color THEME_GRADIENT_COLOR = new Color(0xCEAD7B);
    
    public static final Color JUST_IMPORTED =  new Color( 228,255,228 );
    
//    public static final Color JUST_IMPORTED = new Color(0x2F2F2F);


}/* END CLASS Colors */
