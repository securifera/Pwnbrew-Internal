///*
//
//Copyright (C) 2013-2014, Securifera, Inc 
//
//All rights reserved. 
//
//Redistribution and use in source and binary forms, with or without modification, 
//are permitted provided that the following conditions are met:
//
//    * Redistributions of source code must retain the above copyright notice,
//	this list of conditions and the following disclaimer.
//
//    * Redistributions in binary form must reproduce the above copyright notice,
//	this list of conditions and the following disclaimer in the documentation 
//	and/or other materials provided with the distribution.
//
//    * Neither the name of Securifera, Inc nor the names of its contributors may be 
//	used to endorse or promote products derived from this software without specific
//	prior written permission.
//
//THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS 
//OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY 
//AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER 
//OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
//CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
//SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON 
//ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
//(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
//EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
//================================================================================
//
//Pwnbrew is provided under the 3-clause BSD license above.
//
//The copyright on this package is held by Securifera, Inc
//
//*/
//
//
///*
//* CommandTabPanel.java
//*
//* Created on June 26, 2013, 9:14:32 PM
//*/
//
//package pwnbrew.gui.panels.command;
//
//import pwnbrew.library.LibraryItemPanel;
//import java.awt.Image;
//import javax.swing.*;
//import pwnbrew.controllers.CommandController;
//import pwnbrew.look.Colors;
//import pwnbrew.misc.Constants;
//import pwnbrew.utilities.GuiUtilities;
//import pwnbrew.utilities.Utilities;
//
//
///**
// *
// *  
// */
//public class CommandTabPanel extends LibraryItemPanel {
//
//      private final JTabbedPane tabCollection = new JTabbedPane();
//      private final JPanel logoPanel = new JPanel();
//      private final JLabel logoScriptLabel = new JLabel();
//      private final JPanel overviewPanel = new JPanel();
//      private JPanel innerOverviewPanel = new JPanel();
//      private final JPanel runnerPanel = new JPanel();
//      private final JScrollPane runnerScrollPane = new JScrollPane();
//      private JTextPane runnerTextPane = new JTextPane();
//
//      private final static String overviewLogoStr = "checklist.png";
//      private final static int LogoImageSize = 165;
//
//      
//      public CommandTabPanel( CommandController passedController ) {
//
//          super( Colors.PANEL_BASE_COLOR, true, false, passedController );
//          
//          initializeComponents( passedController );
//          initializePanel();
//          initLayout();
//      }
//
//      
//      private void initializePanel() {
//         setAutoscrolls(true);
//         setPreferredSize(new java.awt.Dimension(528, 457));
//
//         GuiUtilities.setTabIcon(tabCollection, 18,18, Constants.OVERVIEW_IMG_STR, 0);
//         GuiUtilities.setTabIcon(tabCollection, 18,18, Constants.TERM_IMG_STR, 1); 
//         tabCollection.setBackgroundAt(0, Colors.THEME_GRADIENT_COLOR);
//         tabCollection.setBackgroundAt(1, Colors.THEME_GRADIENT_COLOR);
//
//      }
//
//      //===============================================================
//      /**
//       * Sets up the layout
//       *
//       * @param scriptNameJLabel
//       * @param tabCollection
//      */
//      private void initLayout(){
//
//         GroupLayout mainJPanelLayout = new javax.swing.GroupLayout(this);
//          setLayout(mainJPanelLayout);
//          mainJPanelLayout.setHorizontalGroup(
//            mainJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
//            .addGroup(mainJPanelLayout.createSequentialGroup()
//                .addGroup(mainJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
//                    .addGroup(mainJPanelLayout.createSequentialGroup()
//                        .addGap(5)
//                        .addComponent(theLibraryItemNameJLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
//                        .addComponent(theToolbar, 112, 112, 112))
//                    .addComponent(tabCollection, javax.swing.GroupLayout.DEFAULT_SIZE, 530, Short.MAX_VALUE)))
//          );
//          mainJPanelLayout.setVerticalGroup(
//            mainJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)   
//                .addComponent(theToolbar, 30, 30, 30)
//                .addGroup(mainJPanelLayout.createSequentialGroup()
//                    .addContainerGap()
//                    .addComponent(theLibraryItemNameJLabel)
//                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
//                    .addComponent(tabCollection, javax.swing.GroupLayout.DEFAULT_SIZE, 487, Short.MAX_VALUE))
//          );
//
//          GroupLayout logoPanelLayout = new javax.swing.GroupLayout(logoPanel);
//          logoPanel.setLayout(logoPanelLayout);
//          logoPanelLayout.setHorizontalGroup(
//            logoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
//            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, logoPanelLayout.createSequentialGroup()
//                .addGap(165, 165, 165))
//            .addGroup(logoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
//                .addComponent(logoScriptLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 163, Short.MAX_VALUE))
//          );
//          logoPanelLayout.setVerticalGroup(
//                logoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
//                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, logoPanelLayout.createSequentialGroup()
//                    .addContainerGap(214, Short.MAX_VALUE)
//                    .addGap(49, 49, 49))
//                .addGroup(logoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
//                    .addGroup(logoPanelLayout.createSequentialGroup()
//                        .addContainerGap()
//                        .addComponent(logoScriptLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
//                        .addContainerGap(85, Short.MAX_VALUE)))
//          );
//
//
//           GroupLayout overviewPanelLayout = new javax.swing.GroupLayout(overviewPanel);
//          overviewPanel.setLayout(overviewPanelLayout);
//          overviewPanelLayout.setHorizontalGroup(
//              overviewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
//              .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, overviewPanelLayout.createSequentialGroup()
//                  .addGap(5)
//                  .addComponent(innerOverviewPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
//                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
//                  .addGroup(overviewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
//                      .addGroup(overviewPanelLayout.createSequentialGroup()
//                          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 10, Short.MAX_VALUE)
//                          .addComponent(logoPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
//                          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 10, Short.MAX_VALUE))
//                          )
//                  .addContainerGap())
//          );
//          overviewPanelLayout.setVerticalGroup(
//              overviewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
//              .addGroup(overviewPanelLayout.createSequentialGroup()
//                  .addGap(71, 71, 71)
//                  .addComponent(logoPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
//                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 88, Short.MAX_VALUE)
//                  .addContainerGap())
//                  .addComponent(innerOverviewPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 459, javax.swing.GroupLayout.PREFERRED_SIZE)
//          );
//
//          GroupLayout runnerPanelLayout = new GroupLayout(runnerPanel);
//          runnerPanel.setLayout(runnerPanelLayout);
//          runnerPanelLayout.setHorizontalGroup(
//              runnerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
//                .addGroup(runnerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
//                       .addGroup(runnerPanelLayout.createSequentialGroup()
//                          .addContainerGap()
//                          .addComponent(runnerScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 505, Short.MAX_VALUE)
//                          .addContainerGap()))
//          );
//          runnerPanelLayout.setVerticalGroup(
//              runnerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
//                  .addGroup(runnerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
//                      .addGroup(runnerPanelLayout.createSequentialGroup()
//                          .addContainerGap()
//                          .addComponent(runnerScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 375, Short.MAX_VALUE)
//                          .addGap(15)))
//          );
//    }
//
//    //===============================================================
//    /**
//     * Returns the tab collection
//     *
//     * @return 
//    */
//    public JTabbedPane getTabCollection() {
//        return tabCollection;
//    }
//
//    //===============================================================
//    /**
//     * Sets up the components
//     *
//    */
//    private void initializeComponents( CommandController passedController ) {
//
//       innerOverviewPanel.setMaximumSize(new java.awt.Dimension(340, 400));
//       innerOverviewPanel.setPreferredSize(new java.awt.Dimension(340, 400));
//
//       runnerScrollPane.setViewportView(runnerTextPane);
//       
//       passedController.getObject();
//       innerOverviewPanel = new CommandOverviewPanel(passedController, passedController.getObject());
//
//       //Setup tab pane
//       tabCollection.setMaximumSize(new java.awt.Dimension(530, 487));
//       tabCollection.addTab("Overview", overviewPanel);
//       tabCollection.addTab("Runner", runnerPanel);
//
//       //Create the image logo
//       setOverviewImage(overviewLogoStr);
//    }
//
//    /**
//     * Sets the image displayed on the top of the window.
//    */
//    private void setOverviewImage( String logoImageStr ) {
//
//        Image logoImage = Utilities.loadImageFromJar( logoImageStr );
//
//        if( logoImage != null ) {
//           logoImage = logoImage.getScaledInstance( LogoImageSize, LogoImageSize, Image.SCALE_SMOOTH );
//           logoScriptLabel.setIcon( new ImageIcon( logoImage ) );
//        }
//    }
//
//    
//    //===============================================================
//    /**
//     * Returns the overview panel
//     *
//     * @return 
//    */
//    public JPanel getOverviewPanel() {
//       return overviewPanel;
//    }
//
//
//    //===============================================================
//    /**
//     * Returns the inner overview scroll pane
//     *
//     * @return 
//    */
//    public JPanel getInnerOverviewPanel() {
//        return innerOverviewPanel;
//    }
//
//    //===============================================================
//    /**
//     * Returns the runner panel
//     *
//     * @return 
//    */
//    public JPanel getRunnerPanel() {
//       return runnerPanel;
//    }
//
//    //===============================================================
//    /**
//     * Returns the runner text pane
//     *
//     * @return 
//    */
//    public JTextPane getRunnerTextPane() {
//        return runnerTextPane;
//    }
//
//    /**
//     * Sets the runner text pane to the passed runner text pane
//     * @param aRunnerPane
//    */
//    public void setRunnerTextPane(JTextPane aRunnerPane) {
//        if(aRunnerPane != null){
//           runnerTextPane = aRunnerPane;
//           runnerScrollPane.setViewportView(runnerTextPane);
//        }
//    }
//
//
//}
