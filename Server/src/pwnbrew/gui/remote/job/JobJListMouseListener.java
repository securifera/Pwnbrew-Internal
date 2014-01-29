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
 * JobJListMouseListener.java
 *
 * Created on Aug 11, 2013, 8:12:11 PM
 */


package pwnbrew.gui.remote.job;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.JList;

/**
 *
*/
@SuppressWarnings("ucd")
public class JobJListMouseListener extends MouseAdapter {

    private final JList theJList;
    private Component currentComponent = null;

    JobJListMouseListener(JList passedJList) {
        theJList = passedJList;
    }

    //===============================================================
    /**
     *  Forwards the mouse event to the cancel button on the progress panel
    */
    private void forwardToPanel(MouseEvent e, int passedMouseEvent){

        int index = theJList.locationToIndex(e.getPoint());
        
        if(index == -1){
            return;
        }

        //Get the object
        Object value = theJList.getModel().getElementAt(index);
        if(value != null){
           Component theComp = theJList.getCellRenderer().getListCellRendererComponent(theJList, value, index, theJList.isSelectedIndex(index), true );

           if(theComp instanceof JobJProgressPanel){
               JobJProgressPanel theProgressPanel = (JobJProgressPanel)theComp;

               JButton theCancelButton = theProgressPanel.getCancelButton();

               int relEventX = e.getX();
               int relEventY = e.getY() - (index  * 60);

               //Pass on the mouse click events
               if(passedMouseEvent == MouseEvent.MOUSE_RELEASED){

                   //Restore the x and y if the right click was for the panel
                   if(e.isPopupTrigger()){
                      relEventX = e.getX();
                      relEventY = e.getY();
                   }

                   //Notify the JTable to start the tool tip timer
                   MouseEvent buttonEvent = new MouseEvent((Component)e.getSource(), e.getID(), e.getWhen(), e.getModifiers(), relEventX, relEventY, e.getClickCount(), e.isPopupTrigger(), e.getButton());
                   theProgressPanel.setRow(index);
                   theProgressPanel.dispatchEvent(buttonEvent);
                   return;
               }

               Rectangle cancelButtonRect = new Rectangle(theCancelButton.getLocation(), theCancelButton.getSize());
               if(cancelButtonRect.contains(relEventX, relEventY)){

                  //Notify the JTable to start the tool tip timer
                  MouseEvent buttonEvent = new MouseEvent(theJList, MouseEvent.MOUSE_ENTERED, 0, MouseEvent.META_MASK, e.getX(), e.getY(), 0, false);
                  theJList.dispatchEvent(buttonEvent);

                  //Notify the cancel button to render a border around the button
                  buttonEvent = new MouseEvent(theCancelButton, MouseEvent.MOUSE_ENTERED, 0, MouseEvent.META_MASK, relEventX, relEventY, 0, false);
                  currentComponent = theCancelButton;

                  theCancelButton.dispatchEvent(buttonEvent);
                  
                  //From event dispatcher
                  theJList.repaint();                 
                  
               } else if(currentComponent != null){

                  //Notify the cancel button to remove the border
                  MouseEvent buttonEvent = new MouseEvent(theCancelButton, MouseEvent.MOUSE_EXITED, 0, MouseEvent.META_MASK, relEventX, relEventY, 0, false);
                  currentComponent.dispatchEvent(buttonEvent);

                  //From event dispatcher
                  theJList.repaint();
                                    
                  currentComponent = null;

                  //Notify the JTable to stop the toop tip timer
                  buttonEvent = new MouseEvent(theJList, MouseEvent.MOUSE_EXITED, 0, MouseEvent.META_MASK, e.getX(), e.getY(), 0, false);
                  theJList.dispatchEvent(buttonEvent);

               }
           }
        }

    }

    //========================================================================
    /**
     * 
     * @param e 
     */
    @Override
    public void mouseReleased(MouseEvent e) {

       int index = theJList.locationToIndex(e.getPoint());

       if(theJList.isSelectedIndex(index)){
          forwardToPanel(e, MouseEvent.MOUSE_RELEASED);
       }
    }

    //======================================================================
    /**
     * 
     * @param e 
     */
    @Override
    public void mouseMoved(MouseEvent e) {
       forwardToPanel(e, MouseEvent.MOUSE_MOVED);
    }

}
