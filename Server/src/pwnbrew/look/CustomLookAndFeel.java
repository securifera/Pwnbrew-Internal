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
 * CustomLookAndFeel.java
 *
 * Created on June 23, 2013, 8:11:23 PM
 */

package pwnbrew.look;

import com.sun.java.swing.plaf.windows.WindowsTreeUI;
import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.InsetsUIResource;
import javax.swing.plaf.metal.MetalIconFactory;
import javax.swing.plaf.metal.MetalLookAndFeel;
import pwnbrew.look.ui.CustomDragTreeUI;
import pwnbrew.look.ui.CustomRootPaneUI;
import pwnbrew.look.ui.CustomTabbedPaneUI;
import pwnbrew.utilities.Utilities;

/**
 *
 */
public class CustomLookAndFeel extends MetalLookAndFeel{

    /**
     * Load the SystemColors into the defaults table.  The keys
     * for SystemColor defaults are the same as the names of
     * the public fields in SystemColor.  If the table is being
     * created on a native Windows platform we use the SystemColor
     * values, otherwise we create color objects whose values match
     * the defaults Windows95 colors.
     * @param table
     */
    // XXX - there are probably a lot of redundant values that could be removed.
    // ie. Take a look at RadioButtonBorder, etc...
    @Override
    protected void initComponentDefaults(UIDefaults table)
    {

        super.initComponentDefaults( table );

        //Set the gradient for the components
        //Blue
//        Color aColor = new Color(0x1E13FF);

//        Color disabledTextComponentColor = Constants.COLOR_DisabledTextAreaBackground;

        //Grey
        Color primary = Colors.THEME_PRIMARY_COLOR;
        Color secondary = Colors.THEME_SECONDARY_COLOR;
        Color aColor = Colors.THEME_GRADIENT_COLOR;

        //Green of Logo
        //Color greenBorder = new Color(117,155,117);
        Color white = Color.WHITE;
        
        //Set the tab inserts
        Object tabbedPaneTabAreaInsets = new InsetsUIResource(3, 2, 2, 2);
        Object tabbedPaneTabInsets = new InsetsUIResource(1, 4, 1, 4);

        UIManager.put("TabbedPane.tabInsets", tabbedPaneTabInsets);
        UIManager.put("TabbedPane.tabAreaInsets", tabbedPaneTabAreaInsets);
        UIManager.put("TabbedPane.foreground", new ColorUIResource(Color.BLACK));
        UIManager.put("TabbedPane.background", new ColorUIResource(aColor));
        UIManager.put("TabbedPane.selected", new ColorUIResource(white));
        UIManager.put("TabbedPane.selectHighlight", new ColorUIResource(aColor));
        UIManager.put("TabbedPane.contentAreaColor", new ColorUIResource(white));
        UIManager.put("TabbedPane.tabsOverlapBorder", true);
        
        //Set for the custom tabbedpane UI
        UIManager.put("TabbedPane.unselectedBackground", new ColorUIResource( Colors.THEME_GRADIENT_COLOR) );
 
        UIManager.put("InternalFrame.titleFont", new FontUIResource(new Font("Tahoma", 1, 14)));
        UIManager.put("InternalFrame.activeTitleBackground", new ColorUIResource(aColor));
        UIManager.put("OptionPane.questionDialog.border.background", new ColorUIResource( Colors.THEME_SECONDARY_COLOR));
        UIManager.put("OptionPane.questionDialog.titlePane.background", secondary);
        UIManager.put("OptionPane.questionDialog.titlePane.foreground", Color.WHITE);
        UIManager.put("OptionPane.questionDialog.titlePane.shadow", primary);


        UIManager.put("OptionPane.warningDialog.border.background", new ColorUIResource(new Color(0x2F2F2F)));
        UIManager.put("OptionPane.warningDialog.titlePane.background", secondary);
        UIManager.put("OptionPane.warningDialog.titlePane.foreground", primary);
        UIManager.put("OptionPane.warningDialog.titlePane.shadow", primary);

        ArrayList<Object> gradients = new ArrayList<>(5);
        gradients.add(0.2f);
        gradients.add(0.00f);
//        gradients.add(aColor.brighter());
        gradients.add(aColor);
        gradients.add(aColor);
//        gradients.add(aColor.brighter());
//        gradients.add(aColor.darker());
        gradients.add(aColor.darker());

        UIManager.put("ToolBar.background", new ColorUIResource(aColor));
        UIManager.put("SplitPane.dividerSize", 5);

        UIManager.put("ToggleButton.gradient", gradients);
        UIManager.put("Button.gradient", gradients);
        UIManager.put("MenuBar.gradient", gradients);
//        UIManager.put("MenuBar.background", aColor );
        UIManager.put("ToolBar.gradient", gradients);
        UIManager.put("ScrollBar.gradient", gradients);
        UIManager.put("InternalFrame.activeTitleGradient", null);
        UIManager.put("InternalFrame.inactiveTitleGradient", null);

        UIManager.put("Tree.hash", new ColorUIResource(Colors.THEME_HIGHLIGHT_COLOR));
        UIManager.put("Tree.expandedIcon", new WindowsTreeUI.ExpandedIcon());
        UIManager.put("Tree.collapsedIcon", new WindowsTreeUI.CollapsedIcon());

//        //Set the background color for disabled TextComponents...
//        UIManager.put( "TextField.disabledBackground", disabledTextComponentColor );
//        UIManager.put( "ComboBox.disabledBackground", disabledTextComponentColor );

        //Set the background for tables
        UIManager.put( "Table.background", Color.WHITE );

        
        JFrame.setDefaultLookAndFeelDecorated( true );
        JDialog.setDefaultLookAndFeelDecorated( true );
        
    }

    /**
     * Populates {@code table} with mappings from {@code uiClassID} to
     * the fully qualified name of the ui class. {@code
     * MetalLookAndFeel} registers an entry for each of the classes in
     * the package {@code javax.swing.plaf.metal} that are named
     * MetalXXXUI. The string {@code XXX} is one of Swing's uiClassIDs. For
     * the {@code uiClassIDs} that do not have a class in metal, the
     * corresponding class in {@code javax.swing.plaf.basic} is
     * used. For example, metal does not have a class named {@code
     * "MetalColorChooserUI"}, as such, {@code
     * javax.swing.plaf.basic.BasicColorChooserUI} is used.
     *
     * @param table the {@code UIDefaults} instance the entries are
     *        added to
     * @throws NullPointerException if {@code table} is {@code null}
     *
     * @see javax.swing.plaf.basic.BasicLookAndFeel#initClassDefaults
     */
    @Override
    protected void initClassDefaults(UIDefaults table)
    {
        super.initClassDefaults(table);
        final String metalPackageName = "javax.swing.plaf.metal.";
        final String windowsPackageName = "com.sun.java.swing.plaf.windows.";
        String fileChooserUI = "";

        if( Utilities.isWindows( Utilities.getOsName() )){
            
            fileChooserUI = windowsPackageName + "WindowsFileChooserUI";

            //Fix for java 7
            table.put("FileChooser.viewMenuIcon", MetalIconFactory.getFileChooserDetailViewIcon() );

        } else {
            fileChooserUI = metalPackageName + "MetalFileChooserUI";
        }

        setCurrentTheme(new CustomTheme());

         Object[] uiDefaults = {
                   "ButtonUI", metalPackageName + "MetalButtonUI",
                 "CheckBoxUI", metalPackageName + "MetalCheckBoxUI",
                 "ComboBoxUI", metalPackageName + "MetalComboBoxUI",
              "DesktopIconUI", metalPackageName + "MetalDesktopIconUI",
              "FileChooserUI", fileChooserUI,
            "InternalFrameUI", metalPackageName + "MetalInternalFrameUI",
                    "LabelUI", metalPackageName + "MetalLabelUI",
       "PopupMenuSeparatorUI", metalPackageName + "MetalPopupMenuSeparatorUI",
              "ProgressBarUI", metalPackageName + "MetalProgressBarUI",
              "RadioButtonUI", metalPackageName + "MetalRadioButtonUI",
                "ScrollBarUI", metalPackageName + "MetalScrollBarUI",
               "ScrollPaneUI", metalPackageName + "MetalScrollPaneUI",
                "SeparatorUI", metalPackageName + "MetalSeparatorUI",
                   "SliderUI", metalPackageName + "MetalSliderUI",
                "SplitPaneUI", windowsPackageName + "WindowsSplitPaneUI",
               "TabbedPaneUI", CustomTabbedPaneUI.class.getName(),
                "TextFieldUI", metalPackageName + "MetalTextFieldUI",
             "ToggleButtonUI", metalPackageName + "MetalToggleButtonUI",
                  "ToolBarUI", metalPackageName + "MetalToolBarUI",
                  "ToolTipUI", metalPackageName + "MetalToolTipUI",
                     "TreeUI", CustomDragTreeUI.class.getName(),
                 "RootPaneUI", CustomRootPaneUI.class.getName()
        };

        table.putDefaults(uiDefaults);
    }

    
   
}
