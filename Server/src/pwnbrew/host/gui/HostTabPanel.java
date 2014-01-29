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
* HostTabPanel.java
*
* Created on June 21, 2013, 8:11:13 PM
*/

package pwnbrew.host.gui;

import pwnbrew.library.LibraryItemPanel;
import javax.swing.*;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import pwnbrew.host.HostController;
import pwnbrew.look.Colors;
import pwnbrew.misc.Constants;
import pwnbrew.utilities.GuiUtilities;


/**
 *
 *  
 */
public final class HostTabPanel extends LibraryItemPanel implements HostOverviewPanelListener {

      private final HostController theController;
      private final JTabbedPane tabCollection = new JTabbedPane();
      private final HostDetailsPanel overviewPanel;
      private final HostShellPanel shellPanel;
      private HostSchedulerPanel schedulerPanel = null;
    
      // ==========================================================================
      /**
      *  Constructor 
     * @param passedController
      */
      public HostTabPanel( HostController passedController ) {

          super( Colors.PANEL_BASE_COLOR, true, false, passedController );
          theController = passedController;
          overviewPanel = new HostDetailsPanel( this );
          shellPanel = new HostShellPanel( passedController );
          
          //Setup tab pane
          int i = 0;  
          tabCollection.setMaximumSize(new java.awt.Dimension(530, 487));
          tabCollection.addTab("Overview", overviewPanel);
          
          //Set the background color
          tabCollection.setBackgroundAt(i, Colors.THEME_GRADIENT_COLOR);
          GuiUtilities.setTabIcon(tabCollection, 18,18, Constants.OVERVIEW_IMG_STR, i++);         
          
          if( !theController.isLocalHost() ){
              schedulerPanel = new HostSchedulerPanel( theController );
              tabCollection.addTab("Scheduler", schedulerPanel);
              
              //Set the background color
              tabCollection.setBackgroundAt(i, Colors.THEME_GRADIENT_COLOR);
              GuiUtilities.setTabIcon(tabCollection, 18,18, Constants.SCHED_IMG_STR, i++);
             
          }
          
          tabCollection.addTab(" Shell ", shellPanel);
          GuiUtilities.setTabIcon(tabCollection, 18,18, Constants.TERM_IMG_STR, i);
          
          //Set the background color
          tabCollection.setBackgroundAt(i, Colors.THEME_GRADIENT_COLOR);
                        
          setAutoscrolls(true);
          setPreferredSize(new java.awt.Dimension(528, 457));
          
          theToolbar.remove(stopButton);
          theToolbar.remove(runButton);
          initLayout();
      }


    //===============================================================
    /**
    * Sets up the layout
    *
    * @param scriptNameJLabel
    * @param tabCollection
    */
    private void initLayout(){
          
        GroupLayout mainJPanelLayout = new javax.swing.GroupLayout(this);
        SequentialGroup aSG = mainJPanelLayout.createSequentialGroup()
                        .addGap(5)
                        .addComponent(theLibraryItemNameJLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 275, Short.MAX_VALUE);
        ParallelGroup aPG = mainJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING);    
        
        //Add the toolbar if it's not local host
        if( !theController.isLocalHost() ){
             aSG.addComponent(theToolbar, 42, 42, 42);
             aSG.addGap(10, 10, 10); 
             aPG.addComponent(theToolbar, 30, 30, 30);
        }
        
        setLayout(mainJPanelLayout);
        mainJPanelLayout.setHorizontalGroup(
            mainJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainJPanelLayout.createSequentialGroup()
                .addGroup(mainJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    //Add the sequential group
                    .addGroup( aSG )
                    .addComponent(tabCollection, javax.swing.GroupLayout.DEFAULT_SIZE, 530, Short.MAX_VALUE)))
        );
        
        //Set the vertical group
        aPG.addGroup(mainJPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(theLibraryItemNameJLabel)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(tabCollection, javax.swing.GroupLayout.DEFAULT_SIZE, 487, Short.MAX_VALUE));
        
        
        mainJPanelLayout.setVerticalGroup( aPG );

    }

    //===============================================================
    /**
     * Returns the tab collection
     *
     * @return 
    */
    public JTabbedPane getTabCollection() {
        return tabCollection;
    }
    
    //===============================================================
    /**
     * Returns the scheduler panel
     *
     * @return 
    */
    public HostDetailsPanel getOverviewPanel() {
       return overviewPanel;
    }
    
     //===============================================================
    /**
     * Returns the shell panel
     *
     * @return 
    */
    public HostShellPanel getShellPanel() {
       return shellPanel;
    }
    
    //===============================================================
    /**
     * Returns the scheduler panel
     *
     * @return 
    */
    public HostSchedulerPanel getSchedulerPanel() {
       return schedulerPanel;
    }

    //===============================================================
    /**
     *  Returns the HostController
     * 
     * @return 
    */    
    @Override
    public HostController getHostController() {
        return theController;
    }
   
}
