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
 * JobSetPanel.java
 *
 * Created on June 26, 2013, 8:11:31 PM
 */

package pwnbrew.gui.panels.jobset;

import pwnbrew.controllers.JobController;
import pwnbrew.controllers.JobSetController;
import pwnbrew.library.LibraryItemController;
import pwnbrew.library.LibraryItemPanel;
import pwnbrew.xmlBase.job.Job;
import pwnbrew.xmlBase.job.JobSet;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import pwnbrew.misc.Constants;
import pwnbrew.utilities.Utilities;


/**
 * 
 */
public class JobSetPanel extends LibraryItemPanel {

    private final static int overviewImageWidth = 128;
    private final static int overviewImageHeight = 160;
    private final static String overviewLogoStr = "scriptSet1.png";
    
    
    private JScrollPane descScrollPane;
    private JTextPane descTextPane;
    
    private JPanel executionPanel;
    private JPanel innerPanel;
    private JLabel overviewImage;
    
    private JCheckBox stopOnErrorCheckBox;
    private JCheckBox runConcurrentlyCheckBox;
    private JCheckBox rebootCheckbox;
    
    private JPanel detailsPanel;
    private JList<LibraryItemController> jobsJList;
    
    private JScrollPane jobsScrollPane;

    private JobSetPanelListener theListener = null;
    
    private boolean allowListenerNotifications = true;
    
    
    // ========================================================================
    /**
     * Creates a new instance of {@link JobSetPanel}.
     * 
     * @param listener the {@link JobSetPanelListener}
     */
    public JobSetPanel( JobSetPanelListener listener ) {
        super( listener );

        if( listener == null )
            throw new IllegalArgumentException( "The JobSetPanelListener cannot be null." );
        
        theListener = listener;

        createComponents();
        initializeComponents();
        
    }/* END CONSTRUCTOR( JobSetPanelListener ) */
    
    
    // ========================================================================
    /**
     * Enables or disables the option check boxes.
     * 
     * @param enabled
     */
    public void setOptionCheckBoxesEnabled( boolean enabled ) {
        
        runConcurrentlyCheckBox.setEnabled( enabled );
//        rebootCheckbox.setEnabled( enabled );
        stopOnErrorCheckBox.setEnabled( enabled && !runConcurrentlyCheckBox.isSelected() );
        
    }/* END setOptionCheckBoxesEnabled( boolean ) */
    
    
    // ========================================================================
    /**
     * <p>
     * 
     * 
     * @param jobSetController
     */
    public void populateComponents( JobSetController jobSetController ) {
        
        JobSet aJobSet = (JobSet)jobSetController.getObject();
        if( aJobSet != null ) { //If a JobSet was given...

            setLibraryItemName( aJobSet.getName() ); //Set the JobSet name label

           //Set the text of the description text area
           allowListenerNotifications = false;
           descTextPane.setText( aJobSet.getAttribute( JobSet.ATTRIBUTE_Description ) );
           allowListenerNotifications = true;
           
           //Set the checkboxes...
           runConcurrentlyCheckBox.setSelected( aJobSet.runsConcurrently() );
           rebootCheckbox.setSelected( aJobSet.rebootsAfter() );
           stopOnErrorCheckBox.setSelected( aJobSet.stopsOnError() );
           stopOnErrorCheckBox.setEnabled( !aJobSet.runsConcurrently() );

           ListModel<LibraryItemController> aModel = jobsJList.getModel();
           if(aModel instanceof DefaultListModel){
              DefaultListModel<LibraryItemController> newModel = (DefaultListModel<LibraryItemController>)aModel;
              newModel.clear();

              List<LibraryItemController> localJobList = jobSetController.getChildren();
              for(LibraryItemController aJobController : localJobList){
                 newModel.addElement(aJobController);
              }
           }           
           
           
        } else { //If a JobSet was not given...

            setLibraryItemName( "" ); //Set the JobSet name label
            
            descTextPane.setText( "" ); //Clear the description
            
            //Uncheck the option boxes...
            runConcurrentlyCheckBox.setSelected( false );
            stopOnErrorCheckBox.setSelected( false );
            rebootCheckbox.setSelected( false );
            
            ///////////////////////
            ///////////////////////
            //CLEAR THE COMPONENTS
            ///////////////////////
            ///////////////////////

        }

    }/* END populateComponents( JobSet ) */
    

    // ========================================================================
    /**
     * 
     */
    private void initializeComponents(){

        Image logoImage = Utilities.loadImageFromJar( overviewLogoStr );
        if( logoImage != null ) {
            logoImage = logoImage.getScaledInstance( overviewImageWidth, overviewImageHeight, Image.SCALE_SMOOTH );
            overviewImage.setIcon( new ImageIcon( logoImage ) );
        }
        
       
        //Set a new model for the list in the overview tab
        jobsJList.setModel( new DefaultListModel<LibraryItemController>());
        jobsJList.setCellRenderer( new ListCellRenderer<Object>() {

            //Customizing renderer for the listbox
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                DefaultListCellRenderer theRenderer = new DefaultListCellRenderer();
                JLabel aLabel = (JLabel)theRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                if(value instanceof JobController){

                    JobController aController = (JobController)value;
                    String lastRunResult = aController.getLastRunResult();

                    Job theJob = (Job)aController.getObject();
                    StringBuilder aSB = new StringBuilder().append(theJob.getName())
                            .append(" - ")
                            .append(theJob.getAttribute(Job.ATTRIBUTE_LastRunResult));
      
                    aLabel.setText(aSB.toString());

                    if(isSelected){
                        aLabel.setBackground(list.getSelectionBackground());
                        aLabel.setForeground(list.getSelectionForeground());
                    } else {
                        aLabel.setBackground(list.getBackground());
                        aLabel.setForeground(list.getForeground());
                    }

                    if(!lastRunResult.isEmpty() && lastRunResult.equals( Constants.LastRunResults_ErrorOccurred)){
                        aLabel.setBackground(Color.red);
                    }
                }

                return aLabel;
            }

        });
       
        jobsJList.addMouseListener( (MouseListener)new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent mouseEvent){
                JList<?> theList = (JList<?>)mouseEvent.getSource();
                if(mouseEvent.getClickCount() == 2){
                    
                    int index = theList.locationToIndex(mouseEvent.getPoint());
                    if(index >= 0){
                        
                        boolean showRunner = false;
                        Object theObject = theList.getModel().getElementAt(index);
                        if(theObject instanceof JobController){
                            JobController aController = (JobController)theObject;
                            String lastRunResult = aController.getLastRunResult();

                            if(!lastRunResult.isEmpty()){
                                showRunner = true;
                            }

                            theListener.selectController(aController, showRunner);
                        }
                    }
                }
            }
       });

    }/* END initializeComponents() */

    
    // ========================================================================
    /**
     * 
     */
    private void handleDocumentChange( DocumentEvent e ) {

        String input = null;
        try {
            input = e.getDocument().getText( 0, e.getDocument().getLength() );
        } catch( BadLocationException ex ) {
            ex = null;
        }

        if( input != null ) { //If the input was obtained...

            theListener.handleDescriptionChange( descTextPane.getText() );

        }

    }/* END handleInputChange( DocumentEvent ) */
    
    
    // ========================================================================
    /**
     * 
     */
    private void createCheckBoxes() {
        
        //Run Concurrently...
        runConcurrentlyCheckBox = new JCheckBox();
        runConcurrentlyCheckBox.setText( "Run concurrently" );
        runConcurrentlyCheckBox.setToolTipText( "Start all of the Jobs at the same time." );
        runConcurrentlyCheckBox.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent e ) {
                
                if( runConcurrentlyCheckBox.isSelected() ) {
                    
                    stopOnErrorCheckBox.setEnabled( false );
                    stopOnErrorCheckBox.setSelected( false );
                    theListener.handleStopOnErrorOptionChange( false );
                    
                } else {
                    stopOnErrorCheckBox.setEnabled( true );
                }
                
                theListener.handleRunConcurrentlyOptionChange( runConcurrentlyCheckBox.isSelected() );
                
            }
        } );
        
        //Reboot On Completion...
        rebootCheckbox = new JCheckBox();
        rebootCheckbox.setText( "Reboot on completion" );
        rebootCheckbox.setToolTipText( "Reboot the machine when the Job Set completes." );
        rebootCheckbox.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent e ) {
                theListener.handleRebootOnCompletionOptionChange( rebootCheckbox.isSelected() );
            }
        } );
        
        //XXX Disabled as a safety precaution
        rebootCheckbox.setEnabled(false);
        
        //Stop On Error...
        stopOnErrorCheckBox = new JCheckBox();
        stopOnErrorCheckBox.setText( "Stop on error" );
        stopOnErrorCheckBox.setToolTipText( "Stop running if a Job produces an error." );
        stopOnErrorCheckBox.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent e ) {
                    theListener.handleStopOnErrorOptionChange( stopOnErrorCheckBox.isSelected() );
            }
        } );
        
    }/* END createCheckBoxes() */
    
    
    // ========================================================================
    /**
     * 
     */
    private void createDescriptionPane() {
        
        //The description JTextPane...
        descTextPane = new JTextPane();
        descTextPane.addCaretListener( this );
        descTextPane.getDocument().addDocumentListener( new DocumentListener() {
            
            @Override
            public void insertUpdate( DocumentEvent e ) {
                if( allowListenerNotifications )
                    handleDocumentChange( e );
            }

            @Override
            public void removeUpdate( DocumentEvent e ) {
                if( allowListenerNotifications )
                    handleDocumentChange( e );
            }

            @Override
            public void changedUpdate( DocumentEvent e ) {
                if( allowListenerNotifications )
                    handleDocumentChange( e );
            }

        });
        
        //The description JScrollPane...
        descScrollPane = new JScrollPane();
        descScrollPane.setBorder(
                BorderFactory.createTitledBorder( null, "Description",
                TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                new Font( "Tahoma", 1, 11 ) ) );
        descScrollPane.setOpaque( false );
        descScrollPane.setViewportView( descTextPane );
        
    }/* END createDescriptionTextPane() */
    
    
    // ========================================================================
    /**
     * 
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void createComponents() {
        
        createCheckBoxes();
        
        createDescriptionPane();
        
        innerPanel = new JPanel();

        
        jobsJList = new JList();
        executionPanel = new JPanel();
        overviewImage = new JLabel();
        

        setPreferredSize( new Dimension( 346, 448 ) );

        detailsPanel = new JPanel();
        detailsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "General Details", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N
        detailsPanel.setPreferredSize(new Dimension(336, 340));

        jobsScrollPane = new JScrollPane();
        jobsScrollPane.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Jobs", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N
        jobsScrollPane.setViewportView(jobsJList);

        javax.swing.GroupLayout scriptDetailsLayout = new javax.swing.GroupLayout(detailsPanel);
        detailsPanel.setLayout(scriptDetailsLayout);
        scriptDetailsLayout.setHorizontalGroup(
            scriptDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, scriptDetailsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(scriptDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jobsScrollPane, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
                    .addComponent(descScrollPane, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 304, Short.MAX_VALUE))
                .addContainerGap())
        );
        scriptDetailsLayout.setVerticalGroup(
            scriptDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(scriptDetailsLayout.createSequentialGroup()
                .addComponent(descScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jobsScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 197, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(27, 27, 27))
        );

        executionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Options", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N
        executionPanel.setPreferredSize(new java.awt.Dimension(336, 102));

        javax.swing.GroupLayout executionPanelLayout = new javax.swing.GroupLayout(executionPanel);
        executionPanel.setLayout(executionPanelLayout);
        executionPanelLayout.setHorizontalGroup(
            executionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(executionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(executionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(executionPanelLayout.createSequentialGroup()
                        .addComponent(rebootCheckbox)
                        .addGap(26, 26, 26)
                        .addComponent(runConcurrentlyCheckBox))
                    .addComponent(stopOnErrorCheckBox))
                .addContainerGap(34, Short.MAX_VALUE))
        );
        executionPanelLayout.setVerticalGroup(
            executionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(executionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(executionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rebootCheckbox)
                    .addComponent(runConcurrentlyCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(stopOnErrorCheckBox)
                .addContainerGap(16, Short.MAX_VALUE))
        );

        overviewImage.setText(" ");

        javax.swing.GroupLayout innerPanelLayout = new javax.swing.GroupLayout(innerPanel);
        innerPanel.setLayout(innerPanelLayout);
        innerPanelLayout.setHorizontalGroup(
            innerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(innerPanelLayout.createSequentialGroup()
                .addGroup(innerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(innerPanelLayout.createSequentialGroup()
                        .addComponent(detailsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(38, 38, Short.MAX_VALUE)
                        .addComponent(overviewImage, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(executionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        innerPanelLayout.setVerticalGroup(
            innerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(innerPanelLayout.createSequentialGroup()
                .addGroup(innerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(innerPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(detailsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 344, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(innerPanelLayout.createSequentialGroup()
                        .addGap(83, 83, 83)
                        .addComponent(overviewImage, javax.swing.GroupLayout.PREFERRED_SIZE, 222, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(executionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(innerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGap(5)
                .addComponent(theLibraryItemNameJLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
                .addComponent(theToolbar, 112, 112, 112)
                .addGap(14))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(theToolbar, 30, 30, 30)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent( theLibraryItemNameJLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE )
                    .addGap(18, 18, 18)
                    .addComponent(innerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>                        

}/* END CLASS JobSetPanel */
