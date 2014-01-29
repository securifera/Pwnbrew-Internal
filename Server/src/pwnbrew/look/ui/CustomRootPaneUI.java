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
 * CustomRootPaneUI.java
 *
 * Created on June 24, 2013, 11:21:12 PM
 */


package pwnbrew.look.ui;

import pwnbrew.logging.Log;
import java.beans.PropertyChangeEvent;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import javax.swing.JComponent;
import javax.swing.JRootPane;
import javax.swing.LookAndFeel;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.metal.MetalRootPaneUI;
import pwnbrew.look.CustomTitlePane;

/**
 *
 *  
 */
public class CustomRootPaneUI extends MetalRootPaneUI {

    private static final String NAME_Class = CustomRootPaneUI.class.getSimpleName();   
    
    /**
     * Creates a UI for a <code>JRootPane</code>.
     *
     * @param c the JRootPane the RootPaneUI will be created for
     * @return the RootPaneUI implementation for the passed in JRootPane
     */
    public static ComponentUI createUI(JComponent c) {
        return new CustomRootPaneUI();
    }

    /**
     * Invoked when a property changes. <code>MetalRootPaneUI</code> is
     * primarily interested in events originating from the
     * <code>JRootPane</code> it has been installed on identifying the
     * property <code>windowDecorationStyle</code>. If the
     * <code>windowDecorationStyle</code> has changed to a value other
     * than <code>JRootPane.NONE</code>, this will add a <code>Component</code>
     * to the <code>JRootPane</code> to render the window decorations, as well
     * as installing a <code>Border</code> on the <code>JRootPane</code>.
     * On the other hand, if the <code>windowDecorationStyle</code> has
     * changed to <code>JRootPane.NONE</code>, this will remove the
     * <code>Component</code> that has been added to the <code>JRootPane</code>
     * as well resetting the Border to what it was before
     * <code>installUI</code> was invoked.
     *
     * @param e A PropertyChangeEvent object describing the event source
     *          and the property that has changed.
     */
    @Override
    public void propertyChange(PropertyChangeEvent e) {
        super.propertyChange(e);

        String propertyName = e.getPropertyName();
        if(propertyName == null) {
            return;
        }

        if(propertyName.equals("windowDecorationStyle")) {
            JRootPane theRootPane = (JRootPane) e.getSource();
          
            // Set the title pane to one that doesn't have the bumps in it
            try{
                
                Class metalRootPaneUIClass = MetalRootPaneUI.class;
                Class params[] = { JRootPane.class, JComponent.class};
                Method aMethod = metalRootPaneUIClass.getDeclaredMethod("setTitlePane", params);
                aMethod.setAccessible(true);

                //Create a new title pane
                CustomTitlePane theTitlePane = new CustomTitlePane(theRootPane, this);
                aMethod.invoke(this, theRootPane, theTitlePane);
                
            } catch (    NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException | IllegalAccessException ex) {
               Log.log(Level.SEVERE, NAME_Class, "propertyChange()", ex.getMessage(), ex);
            }

        }

    }

    /**
     * Installs the appropriate <code>Border</code> onto the
     * <code>JRootPane</code>.
     * @param root
     */
    public void installBorder(JRootPane root) {
        int style = root.getWindowDecorationStyle();

        Class metalRootPaneUIClass = MetalRootPaneUI.class;
        String[] theBorderKeys = null;

        //Retrieve the private field from the parent class
        try {
           Field aField = metalRootPaneUIClass.getDeclaredField("borderKeys");
           aField.setAccessible(true);
           theBorderKeys = (String[])aField.get(this);
        } catch (NoSuchFieldException | SecurityException | IllegalAccessException ex) {
           Log.log( Level.SEVERE, NAME_Class, "installBorder()", "Unable to install the Dialog border. \n" + ex.getMessage(), ex);
        }        

        if (style == JRootPane.NONE)
            LookAndFeel.uninstallBorder(root);
        else
            //Install borderkeys border
            if(theBorderKeys != null)
               LookAndFeel.installBorder(root, theBorderKeys[style]);
                    
    }


}
