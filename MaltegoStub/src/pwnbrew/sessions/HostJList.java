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


package pwnbrew.sessions;

import java.awt.Image;
import java.awt.event.*;
import javax.swing.*;
import pwnbrew.misc.Constants;
import pwnbrew.misc.Utilities;
import static pwnbrew.misc.Utilities.loadImageFromJar;
import pwnbrew.xml.maltego.custom.Host;

/**
 *
 *  
 */
@SuppressWarnings("ucd")
public class HostJList extends JList implements ActionListener {

    private final SessionJFrameListener theListener;
   
    //===============================================================
    /**
     *  Constructor
     * @param passedListener
    */
    public HostJList(SessionJFrameListener passedListener) {
        
        theListener = passedListener;      
    }
  
    //****************************************************************************
    /**
    *  Determines what menu options to show on the popup menu based on the
    *  {@link XmlBase} object contained in the currently selected node.
    *
    *  @param  e   the {@code MouseEvent} that triggered the popup
    */
    public void doPopupMenuLogic( MouseEvent e ) {

        JPopupMenu popup = new JPopupMenu();
        
        Image nodeImage = loadImageFromJar( Constants.DELETE_IMG_STR, Utilities.getClassPath(), Utilities.IMAGE_PATH_IN_JAR );
        if(nodeImage != null){
            ImageIcon nodeIcon = new ImageIcon(nodeImage.getScaledInstance(20, 20, Image.SCALE_SMOOTH));

            JMenuItem menuItem = new JMenuItem( Constants.DELETE );
            menuItem.setIcon(nodeIcon);
            menuItem.setActionCommand( Constants.DELETE );
            menuItem.addActionListener(this);
            menuItem.setEnabled( true );
            popup.add(menuItem);

            if( popup.getComponentCount() > 0 ) {
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        }

    }
   
    //=========================================================================
    /**
     *  Perform the action needed when an event is fired
     * @param evt 
     */
    @Override
    public void actionPerformed(ActionEvent evt) {

        String actionCommand = evt.getActionCommand();            
        if( actionCommand.equals(Constants.DELETE) ) {
            Object anObj = getSelectedValue();
            if( anObj instanceof Host ){
                Host aHost = (Host)anObj;
                theListener.removeHost(aHost);
            }
            //Remove the element
            DefaultListModel theModel = (DefaultListModel) getModel();
            theModel.removeElement(anObj);
        }   

   }

}
