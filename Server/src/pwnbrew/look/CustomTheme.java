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
 * CustomTheme.java
 *
 * Created on July 14, 2013, 9:11:22 PM
 */

package pwnbrew.look;

import java.awt.Color;
import java.awt.Font;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.metal.OceanTheme;

/**
 *
 *  
 */
@SuppressWarnings("ucd")
public class CustomTheme extends OceanTheme{

    // These fields are the values returned by this Theme
        private final String displayName;
        private final FontUIResource controlFont, menuFont, smallFont;
        private final FontUIResource systemFont, userFont, titleFont;
        private final ColorUIResource primary1, primary2, primary3, highlight;
        private final ColorUIResource secondary1, secondary2, secondary3;
        private final ColorUIResource textColor = new ColorUIResource(Color.BLACK);
    
        /**
         * This constructor reads all the values it needs from the
         * GUIResourceBundle.  It uses intelligent defaults if properties
         * are not specified.
         **/
        public CustomTheme() {
            // Use this theme object to get default font values from

            // Look up the display name of the theme
            displayName = "CustomTheme";

            // Look up the fonts for the theme
            Font control = new Font("Tahoma", 0, 11);
            Font menu = new Font("Tahoma", 0, 11);
            Font small = new Font("Tahoma", 0, 11);
            Font system = new Font("Tahoma", 0, 11);
            Font user = new Font("Tahoma", 0, 11);
            Font title = new Font("Tahoma", 0, 11);

            // Convert fonts to FontUIResource, or get defaults
//            if (control != null) 
                controlFont = new FontUIResource(control);
//            else controlFont = oceanTheme.getControlTextFont( );
//            if (menu != null) 
                menuFont = new FontUIResource(menu);
//            else menuFont = oceanTheme.getMenuTextFont( );
//            if (small != null) 
                smallFont = new FontUIResource(small);
//            else smallFont = oceanTheme.getSubTextFont( );
//            if (system != null) 
                systemFont = new FontUIResource(system);
//            else systemFont = oceanTheme.getSystemTextFont( );
//            if (user != null) 
                userFont = new FontUIResource(user);
//            else userFont = oceanTheme.getUserTextFont( );
//            if (title != null) 
                titleFont = new FontUIResource(title);
//            else titleFont = oceanTheme.getWindowTitleFont( );

            // Look up primary and secondary colors
            //Blue
//            Color primary = new Color(0x2E43CF);
//            Color secondary = new Color(0x2E439F);

            //Grey
//            Color primary = new Color(0x3F3F3F);
            highlight =  new ColorUIResource( Colors.THEME_HIGHLIGHT_COLOR );
//            Color secondary = new Color(0x767676);

            // Derive all six colors from these two, using defaults if needed
//            if (primary != null) 
            primary1 = new ColorUIResource( Colors.THEME_PRIMARY_COLOR );
//            else primary1 = new ColorUIResource(102, 102, 153);
            primary2 = new ColorUIResource(primary1.brighter( ));
            primary3 = new ColorUIResource(highlight.brighter().brighter( ));
            
//            if (secondary != null) 
                secondary1 = new ColorUIResource( Colors.THEME_SECONDARY_COLOR );
//            else secondary1 = new ColorUIResource(102, 102, 102);
            secondary2 = new ColorUIResource(secondary1.brighter( ));
            secondary3 = new ColorUIResource(secondary2.brighter( ));

        }

        // These methods override DefaultMetalTheme and return the property
        // values we looked up and computed for this theme
    @Override
    public String getName( ) { return displayName; }
    @Override
    public FontUIResource getControlTextFont( ) { return controlFont;}
    @Override
    public FontUIResource getSystemTextFont( ) { return systemFont;}
    @Override
    public FontUIResource getUserTextFont( ) { return userFont;}
    @Override
    public FontUIResource getMenuTextFont( ) { return menuFont;}
    @Override
    public FontUIResource getWindowTitleFont( ) { return titleFont;}
    @Override
    public FontUIResource getSubTextFont( ) { return smallFont;}
    @Override
    protected ColorUIResource getPrimary1( ) { return primary1; }
    @Override
    protected ColorUIResource getPrimary2( ) { return primary2; }
    @Override
    protected ColorUIResource getPrimary3( ) { return primary3; }
    @Override
    protected ColorUIResource getSecondary1( ) { return secondary1; }
    @Override
    protected ColorUIResource getSecondary2( ) { return secondary2; }
    @Override
    protected ColorUIResource getSecondary3( ) { return secondary3; }
    @Override
    public ColorUIResource getMenuForeground() { return textColor;}
    @Override
    public ColorUIResource getControlTextColor() { return textColor;}
    @Override
    public ColorUIResource getControlHighlight() { return primary1;}
   
}

