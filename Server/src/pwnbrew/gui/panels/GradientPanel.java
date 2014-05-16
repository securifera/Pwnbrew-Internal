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

package pwnbrew.gui.panels;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;

/**
 *
 */
public class GradientPanel extends JPanel{

    private Color startColor;
    private Color endColor;
    private boolean leftToRight = true;
    private boolean topToBottom = true;
    private GradientPaint gradientPaint = null;

    private int panelWidth = getWidth( );
    private int panelHeight = getHeight( );

    //=========================================================================
    /**
     * Constructor
     * 
     * @param passedStartColor
     * @param passedEndColor
     * @param passedLtoR
     * @param passedTtoB 
     */
    public GradientPanel(Color passedStartColor, Color passedEndColor, boolean passedLtoR, boolean passedTtoB){

       startColor =  passedStartColor;
       endColor = passedEndColor;
       leftToRight = passedLtoR;
       topToBottom = passedTtoB;

    }

    //=========================================================================
    /**
     *  Constructor
     * 
     * @param passedStartColor
     * @param passedLtoR
     * @param passedTtoB 
     */
    public GradientPanel(Color passedStartColor, boolean passedLtoR, boolean passedTtoB){

       this(passedStartColor, passedStartColor.darker(), passedLtoR, passedTtoB);

    }

    //=========================================================================
    /**
     * 
     * @param g 
     */
    @Override
    protected void paintComponent( Graphics g ){

        int tempWidth = getWidth( );
        int tempHeight = getHeight( );

        if( tempWidth != panelWidth || tempHeight != panelHeight){
           panelWidth = tempWidth;
           panelHeight = tempHeight;
            gradientPaint = new GradientPaint( 0, 0, startColor, ( leftToRight ? panelWidth : 0 ), ( topToBottom ? panelHeight : 0 ), endColor );
        }

        super.paintComponent( g );

        if(g instanceof Graphics2D && gradientPaint != null){

           Graphics2D g2d = (Graphics2D)g;
           g2d.setPaint( gradientPaint );
           g2d.fillRect( 0, 0, panelWidth, panelHeight );

        }
    }

}
