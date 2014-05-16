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

import java.awt.Component;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import pwnbrew.misc.Constants;
import pwnbrew.misc.Iconable;
import pwnbrew.misc.Utilities;
import pwnbrew.sessions.wizard.HostCheckInWizard;
import pwnbrew.sessions.wizard.HostCheckInWizardController;
import pwnbrew.xml.maltego.Field;
import pwnbrew.xml.maltego.custom.Host;

/**
 *
 * @author Securifera
 */
public class SessionsJFrame extends javax.swing.JFrame {

    private final SessionJFrameListener theListener;
    
    //=======================================================================
    /**
     * Constructor
     * @param passedListener 
     * @param passedHostList 
     */
    public SessionsJFrame( SessionJFrameListener passedListener, List<Host> passedHostList ) {
        theListener = passedListener;
        
        initComponents();
        initializeComponents( passedHostList );
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        hostScrollPane = new javax.swing.JScrollPane();
        hostJList = new javax.swing.JList();
        checkInPanel = new javax.swing.JPanel();
        sessionPane = new javax.swing.JScrollPane();
        sessionTable = new javax.swing.JTable();
        clearButton = new javax.swing.JButton();
        checkInPane = new javax.swing.JScrollPane();
        checkInTimeList = new HostCheckInList( theListener );
        scheduleButton = new javax.swing.JButton();
        autoSleepCheckbox = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        hostScrollPane.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Hosts", 0, 0, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

        hostJList.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4));
        hostScrollPane.setViewportView(hostJList);

        checkInPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Sessions", 0, 0, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

        sessionTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Check-In Time", "Disconnect Time"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        sessionPane.setViewportView(sessionTable);

        clearButton.setText("Clear");
        clearButton.setMargin(new java.awt.Insets(0, 4, 0, 4));
        clearButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout checkInPanelLayout = new javax.swing.GroupLayout(checkInPanel);
        checkInPanel.setLayout(checkInPanelLayout);
        checkInPanelLayout.setHorizontalGroup(
            checkInPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(checkInPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(checkInPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(sessionPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 221, Short.MAX_VALUE)
                    .addComponent(clearButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        checkInPanelLayout.setVerticalGroup(
            checkInPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(checkInPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(sessionPane, javax.swing.GroupLayout.DEFAULT_SIZE, 199, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(clearButton, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        checkInPane.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Next Check-In Times", 0, 0, new java.awt.Font("Tahoma", 1, 11))); // NOI18N
        checkInPane.setViewportView(checkInTimeList);

        scheduleButton.setText("jButton1");
        scheduleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scheduleButtonActionPerformed(evt);
            }
        });

        autoSleepCheckbox.setText("Auto-Sleep");
        autoSleepCheckbox.setIconTextGap(8);
        autoSleepCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                autoSleepCheckboxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(hostScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 202, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(checkInPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addComponent(checkInPane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(scheduleButton, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(autoSleepCheckbox)
                        .addContainerGap(41, Short.MAX_VALUE))))
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addComponent(checkInPane, javax.swing.GroupLayout.PREFERRED_SIZE, 233, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(autoSleepCheckbox)
                            .addComponent(scheduleButton, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(checkInPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(hostScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 268, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void clearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearButtonActionPerformed
        
        //Make sure they want to clear the session list
        int rtnCode = JOptionPane.CLOSED_OPTION;
        String messageBuilder = "Are you sure you want to clear the session list?";
        while( rtnCode == JOptionPane.CLOSED_OPTION ) { //Until the user chooses 'Yes' or 'No'...
            //Prompt user to confirm the delete
            rtnCode = JOptionPane.showConfirmDialog( null, messageBuilder,
                    "Clear Session List", JOptionPane.YES_NO_OPTION );
        } 
                            
        //Get the last-selected node
        if( rtnCode == JOptionPane.YES_OPTION ) { //If the delete is confirmed...

                                     //Set the last check in
            DefaultTableModel aModel = new DefaultTableModel( new Object [][] {},
                new String [] {
                    "Check-In Time", "Disconnect Time"
                }){
                    Class[] types = new Class [] {
                        java.lang.String.class, java.lang.String.class
                    };
                    boolean[] canEdit = new boolean [] {
                        false, false
                    };

                    @Override
                    public Class getColumnClass(int columnIndex) {
                        return types [columnIndex];
                    }

                    @Override
                    public boolean isCellEditable(int rowIndex, int columnIndex) {
                        return canEdit [columnIndex];
                    }
                };

            sessionTable.setModel(aModel);

            //Clear the times
            Object anObj = hostJList.getSelectedValue();
            if( anObj != null && anObj instanceof Host ){
                Host aHost = (Host)anObj;
                Field hostIdField = aHost.getField( Constants.HOST_ID );
                String hostIdStr = hostIdField.getXmlObjectContent();
                theListener.clearSessionList(hostIdStr);  
            }   
        }      

    }//GEN-LAST:event_clearButtonActionPerformed

    private void autoSleepCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autoSleepCheckboxActionPerformed
        theListener.setAutoSleepFlag( autoSleepCheckbox.isSelected() );
    }//GEN-LAST:event_autoSleepCheckboxActionPerformed

    private void scheduleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scheduleButtonActionPerformed
        
        //Create the wizard
        HostCheckInWizard remoteWizard = new HostCheckInWizard( this, new HostCheckInWizardController());
        JDialog aDialog = new JDialog( this, true);
        aDialog.add(remoteWizard);
        aDialog.pack();
        aDialog.setVisible(true);  
        
    }//GEN-LAST:event_scheduleButtonActionPerformed
   
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox autoSleepCheckbox;
    private javax.swing.JScrollPane checkInPane;
    private javax.swing.JPanel checkInPanel;
    private javax.swing.JList checkInTimeList;
    private javax.swing.JButton clearButton;
    private javax.swing.JList hostJList;
    private javax.swing.JScrollPane hostScrollPane;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JButton scheduleButton;
    private javax.swing.JScrollPane sessionPane;
    private javax.swing.JTable sessionTable;
    // End of variables declaration//GEN-END:variables

    //======================================================================
    /**
     * 
     */
    private void initializeComponents(  List<Host> theHostList ) {
        
        scheduleButton.setOpaque(false);
        scheduleButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        scheduleButton.setToolTipText("Open Schedule Assistant");
        Utilities.setComponentIcon(scheduleButton, 21, 21, Constants.SCHEDULE_IMG_STR);
        
        DefaultListModel theModel = new DefaultListModel();
        for( Host aHost : theHostList )
            theModel.addElement(aHost);        
                                                
        //Set the model
        hostJList.setModel(theModel);
        hostJList.setCellRenderer( new DefaultListCellRenderer(){
            @Override
            public Component getListCellRendererComponent( JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus){
                Component aComponent = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                
                if( value instanceof Host ){
                    Host aHost = (Host)value;
                    String theType = aHost.getType();
                    BufferedImage iconImage;
                    if( theType.equals(Host.PWNBREW_HOST_CONNECTED))
                        iconImage = Utilities.loadImageFromJar( Constants.HOST_IMG_STR );	
                    else
                        iconImage = Utilities.loadImageFromJar( Constants.DIS_HOST_IMG_STR );	
                    
                    setIcon(new ImageIcon( iconImage.getScaledInstance( Iconable.ICON_WIDTH, Iconable.ICON_HEIGHT, Image.SCALE_SMOOTH ) ));
                }
                
                return aComponent;
            }
        });        
        
        //Add the list selection listener
        hostJList.addListSelectionListener( new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if( !e.getValueIsAdjusting()){

                    DefaultTableModel aModel = new DefaultTableModel( new Object [][] {},
                    new String [] {
                        "Check-In Time", "Disconnect Time"
                    }){
                        Class[] types = new Class [] {
                            java.lang.String.class, java.lang.String.class
                        };
                        boolean[] canEdit = new boolean [] {
                            false, false
                        };

                        @Override
                        public Class getColumnClass(int columnIndex) {
                            return types [columnIndex];
                        }

                        @Override
                        public boolean isCellEditable(int rowIndex, int columnIndex) {
                            return canEdit [columnIndex];
                        }
                    };
                    
                    //Clear the model
                    sessionTable.setModel(aModel);
                    checkInTimeList.setModel( new HostCheckInListModel( theListener ));
                    
                    //Get the selected Host
                    Object anObj = hostJList.getSelectedValue();
                    if( anObj != null && anObj instanceof Host ){
                        Host aHost = (Host)anObj;
                        Field hostIdField = aHost.getField( Constants.HOST_ID );
                        String hostIdStr = hostIdField.getXmlObjectContent();
                        theListener.hostSelected(hostIdStr);
                    }
                }
            }
        });
               
    }

    //=================================================================
    /**
     * Get the Host list
     * @return 
     */
    public JList getHostJList() {
        return hostJList;
    }
    
    private boolean isSelectedHost( int hostId ){
        
        boolean retVal = false;
        Object anObj = hostJList.getSelectedValue();
        if( anObj != null && anObj instanceof Host ){
            Host aHost = (Host)anObj;
            Field hostIdField = aHost.getField( Constants.HOST_ID );
            String hostIdStr = hostIdField.getXmlObjectContent();
            
            //Check if they are equal
            if( Integer.parseInt(hostIdStr) == hostId )
                retVal = true;                
            
        }
        return retVal;
    }

    //=================================================================
    /**
     * 
     * @param hostId
     * @param checkInDatStr
     * @param checkOutDatStr 
     */
    public synchronized void addSession( int hostId, String checkInDatStr, String checkOutDatStr) {     
        if( isSelectedHost(hostId)){
            DefaultTableModel aModel = (DefaultTableModel) sessionTable.getModel();
            aModel.addRow( new Object[]{checkInDatStr, checkOutDatStr});
        }
    }

    //=================================================================
    /**
     * 
     * @param hostId
     * @param checkInDatStr 
     */
    public synchronized void addCheckInDate( int hostId, String checkInDatStr) {
        if( isSelectedHost(hostId)){
            HostCheckInListModel theModel = (HostCheckInListModel) checkInTimeList.getModel();        
            theModel.addElement( checkInDatStr );
        }
    }
}
