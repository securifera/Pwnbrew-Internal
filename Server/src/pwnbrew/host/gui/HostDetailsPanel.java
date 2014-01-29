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
 * HostDetailsPanel.java
 *
 * Created on June 24, 2013, 11:31:33 AM
 */

package pwnbrew.host.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import pwnbrew.network.Nic;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import pwnbrew.host.Host;
import pwnbrew.host.HostController;
import pwnbrew.host.Session;
import pwnbrew.misc.Constants;
import pwnbrew.network.control.messages.FileSystemMsg;
import pwnbrew.utilities.GuiUtilities;


/**
 *
 *  
 */

public class HostDetailsPanel extends JPanel {

    private final HostOverviewPanelListener theListener;
    private JPanel fileSystemPanel = new JPanel();
    private final JButton uploadButton = new JButton();
    private final JButton downButton = new JButton();
    private JFileChooser theFileChooser = null;
  
   
    /** Creates new form CommandOverviewPanel */
    HostDetailsPanel( HostOverviewPanelListener passedListener ) {

        theListener = passedListener;

        initComponents();
        initializeComponents();
    }

    //=======================================================================
    /**
    * Initializes all the components
    */
    private void initializeComponents(){
        
        //Set the data in the components
        HostController theController = theListener.getHostController();
        if( !theController.isLocalHost() ){
            fileSystemPanel = new FileTreePanel( theController );
            fileSystemPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "File System", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N
        }
        
        theFileChooser = new JFileChooser();
        theFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        theFileChooser.setMultiSelectionEnabled( true ); //Let the user select multiple files
                
        uploadButton.setEnabled(false);  
        uploadButton.setOpaque(false);
        uploadButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        uploadButton.setToolTipText("Upload File");
        GuiUtilities.setComponentIcon(uploadButton, 30, 30, Constants.UPLOAD_IMG_STR);
        
        downButton.setEnabled(false);  
        downButton.setOpaque(false);
        downButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        downButton.setToolTipText("Download File");
        GuiUtilities.setComponentIcon(downButton, 30, 30, Constants.DOWNLOAD_IMG_STR);
    
        uploadButton.addActionListener( new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent ae) {
                uploadButtonActionPerformed(ae);
            }
        });
        
        downButton.addActionListener( new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent ae) {
                downloadButtonActionPerformed(ae);
            }
        });
        
        setupLayout();
        populateComponents();
               
    }
    
    //===========================================================================
    /**
     *  
     * @param evt 
     */
    private void downloadButtonActionPerformed(java.awt.event.ActionEvent evt) { 
    
        FileTreePanel thePanel = getFileTreePanel();            
        DefaultMutableTreeNode aNode = (DefaultMutableTreeNode)thePanel.theJTree.getSelectionPath().getLastPathComponent();
        Object anObj = aNode.getUserObject();
        if( anObj instanceof IconData ){
                
                IconData id = (IconData)anObj;
                anObj = id.getObject();
                if( anObj instanceof FileNode ){
                    
                    //Get the file
                    FileNode aFN = (FileNode)anObj;
                    RemoteFile filePath = aFN.getFile();
                    
                    //Add the file to the list
                    List<RemoteFile> theFileList = new ArrayList<>();
                    theFileList.add(filePath);
                    
                    //Download the files
                    theListener.getHostController().downloadFiles( theFileList );   
                }
        }      
            
    }
    
    //===========================================================================
    /**
     *  
     * @param evt 
     */
    private void uploadButtonActionPerformed(java.awt.event.ActionEvent evt) {                                             
       
        File[] userSelectedFiles = null;

        int returnVal = theFileChooser.showDialog( this, "Select File(s)" ); //Show the dialog
        switch( returnVal ) {

          case JFileChooser.CANCEL_OPTION: //If the user canceled the selecting...
          case JFileChooser.ERROR_OPTION: //If the dialog was dismissed or an error occurred...
            break; //Do nothing

          case JFileChooser.APPROVE_OPTION: //If the user approved the selection...
            userSelectedFiles = theFileChooser.getSelectedFiles(); //Get the files the user selected
            break;
          default:
            break;

        }

        if(userSelectedFiles != null){
            
            //Get the file panel
            FileTreePanel thePanel = getFileTreePanel();
            DefaultMutableTreeNode aNode = (DefaultMutableTreeNode)thePanel.theJTree.getSelectionPath().getLastPathComponent();
            Object anObj = aNode.getUserObject();
            if( anObj instanceof IconData ){
                
                IconData id = (IconData)anObj;
                anObj = id.getObject();
                if( anObj instanceof FileNode ){
                    FileNode aFN = (FileNode)anObj;
                    String filePath = aFN.getFile().getAbsolutePath();
                    
                     //Create a list out of the array
                    List<File> theObjList = Arrays.asList(userSelectedFiles);
                    theListener.getHostController().uploadFiles(theObjList, filePath);   
                }
            }
                    
        }
       
    }     

    //=========================================================================
    /**
    * Sets the value of the relay
     * @param passedBool
    */
    public void setRelayValue( boolean passedBool ){
        
        String relay = "Not Listening";
        if( passedBool ){
            relay = "Listening";
        } else {
            
            HostController aController = theListener.getHostController();
            Host aHost = aController.getObject();
            aHost.setRelayPort( "" );
            aController.saveToDisk();
                       
        }
        relayValue.setText(relay);
        
        //Repaint
        SwingUtilities.invokeLater( new Runnable(){
            @Override
            public void run() {
                theListener.getHostController().refreshOverviewPanel();
            }
        });
               
    }
    
    //=========================================================================
    /**
    * Populate the components
    */
    public void populateComponents(){
        
        HostController theController = theListener.getHostController();
        Host theHost = (Host) theController.getObject();
        if( !theController.isLocalHost() ){
            
            boolean connected = theHost.isConnected();
            String aStr = ( connected ? "Connected" : "Disconnected" );
            
            //Set the label
            statusValueLabel.setText( aStr );
            relayPortValue.setText( theHost.getRelayPort() );
            fileSystemPanel.setEnabled(connected); 
            uploadButton.setEnabled(connected);
            downButton.setEnabled(connected);
            filePanel.setEnabled(connected);
                      
        } else {
            
            statusValueLabel.setText( "Connected (localhost)" );
            checkInPanel.setVisible( false );  
            uploadButton.setVisible(false);
            downButton.setVisible(false);
            filePanel.setVisible(false);
                      
            //Hide relay info
            relayLabel.setVisible(false);
            relayPortLabel.setVisible(false);
            relayValue.setVisible(false);
            relayPortValue.setVisible(false);
        }
        
        //Set the arch
        jvmArchValue.setText( theHost.getJvmArch());
        osValue.setText( theHost.getOsName());
        
        //Add the NIC list
        DefaultTableModel aModel = new DefaultTableModel(new Object [][] {},
            new String [] {
                "MAC Address", "IP Address", "Subnet Mask"
        }){
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false
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
        
        Map<String, Nic> nicMap = theHost.getNicMap();     
        for( Iterator<Nic> anIter = nicMap.values().iterator(); anIter.hasNext(); ){
            
            //Add an entry
            Nic anEntry = anIter.next();
            aModel.addRow( new Object[]{anEntry.getMacAddress(), anEntry.getIpAddress(), anEntry.getSubnetMask()});
        } 
        nicTable.setModel(aModel);
        
        //Set the last check in
        aModel = new DefaultTableModel( new Object [][] {},
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
        
        List<Session> sessionList = theHost.getSessionList();
        for( Iterator<Session> anIter = sessionList.iterator(); anIter.hasNext();){
            Session aSession = anIter.next();
            aModel.addRow( new Object[]{aSession.getCheckInTime(), aSession.getDisconnectedTime()});
        }
        sessionTable.setModel(aModel);
        
        //Set the next date str
        String theNextCheckStr = "-";
        List<String> dateList = theHost.getCheckInList();
        if( dateList != null && dateList.size() > 0 ){
            theNextCheckStr = dateList.get(0);
        }
        nextCheckValLabel.setText( theNextCheckStr.trim() );
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        nicPanel = new javax.swing.JPanel();
        nicScrollPane = new javax.swing.JScrollPane();
        nicTable = new javax.swing.JTable();
        checkInPanel = new javax.swing.JPanel();
        nextCheckLabel = new javax.swing.JLabel();
        nextCheckValLabel = new javax.swing.JLabel();
        sessionPane = new javax.swing.JScrollPane();
        sessionTable = new javax.swing.JTable();
        clearButton = new javax.swing.JButton();
        propertiesPanel = new javax.swing.JPanel();
        statusLabel = new javax.swing.JLabel();
        statusValueLabel = new javax.swing.JLabel();
        osLabel = new javax.swing.JLabel();
        osValue = new javax.swing.JLabel();
        jvmLabel = new javax.swing.JLabel();
        jvmArchValue = new javax.swing.JLabel();
        relayLabel = new javax.swing.JLabel();
        relayValue = new javax.swing.JLabel();
        relayPortLabel = new javax.swing.JLabel();
        relayPortValue = new javax.swing.JLabel();
        filePanel = new javax.swing.JPanel();
        typeLabel = new javax.swing.JLabel();
        nameLabel = new javax.swing.JLabel();
        sizeLabel = new javax.swing.JLabel();
        lastDateLabel = new javax.swing.JLabel();
        nameLabelValue = new javax.swing.JLabel();
        typeLabelValue = new javax.swing.JLabel();
        sizeLabelValue = new javax.swing.JLabel();
        lastDateLabelValue = new javax.swing.JLabel();

        setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Host Details", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N
        setLayout(null);

        nicPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Network Interfaces", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

        nicTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "MAC Address", "IP Address", "Subnet Mask"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        nicScrollPane.setViewportView(nicTable);

        javax.swing.GroupLayout nicPanelLayout = new javax.swing.GroupLayout(nicPanel);
        nicPanel.setLayout(nicPanelLayout);
        nicPanelLayout.setHorizontalGroup(
            nicPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(nicPanelLayout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addComponent(nicScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 277, Short.MAX_VALUE)
                .addGap(5, 5, 5))
        );
        nicPanelLayout.setVerticalGroup(
            nicPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(nicPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(nicScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(19, Short.MAX_VALUE))
        );

        add(nicPanel);
        nicPanel.setBounds(6, 173, 299, 122);

        checkInPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Sessions", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

        nextCheckLabel.setText("Next Check-In:");

        nextCheckValLabel.setText(" ");

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
            checkInPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(checkInPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(checkInPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(checkInPanelLayout.createSequentialGroup()
                        .addComponent(sessionPane, javax.swing.GroupLayout.PREFERRED_SIZE, 238, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(checkInPanelLayout.createSequentialGroup()
                        .addComponent(nextCheckLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(nextCheckValLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(clearButton)))
                .addContainerGap())
        );
        checkInPanelLayout.setVerticalGroup(
            checkInPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(checkInPanelLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(checkInPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nextCheckLabel)
                    .addComponent(nextCheckValLabel)
                    .addComponent(clearButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(sessionPane, javax.swing.GroupLayout.PREFERRED_SIZE, 197, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        add(checkInPanel);
        checkInPanel.setBounds(311, 27, 270, 267);

        propertiesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Properties", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

        statusLabel.setText("Status:");

        statusValueLabel.setText(" ");

        osLabel.setText("Operating System:");

        osValue.setText(" ");

        jvmLabel.setText("JVM Architecture:");

        jvmArchValue.setText(" ");

        relayLabel.setText("Relay Status:");

        relayValue.setText("N/A");

        relayPortLabel.setText("Relay Port:");

        relayPortValue.setText("N/A");

        javax.swing.GroupLayout propertiesPanelLayout = new javax.swing.GroupLayout(propertiesPanel);
        propertiesPanel.setLayout(propertiesPanelLayout);
        propertiesPanelLayout.setHorizontalGroup(
            propertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(propertiesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(propertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(propertiesPanelLayout.createSequentialGroup()
                        .addGroup(propertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jvmLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(relayLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(osLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(statusLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(propertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(statusValueLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(osValue, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jvmArchValue, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(relayValue, javax.swing.GroupLayout.DEFAULT_SIZE, 171, Short.MAX_VALUE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, propertiesPanelLayout.createSequentialGroup()
                        .addComponent(relayPortLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(relayPortValue, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        propertiesPanelLayout.setVerticalGroup(
            propertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(propertiesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(propertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(statusLabel)
                    .addComponent(statusValueLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(propertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(osLabel)
                    .addComponent(osValue))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(propertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jvmLabel)
                    .addComponent(jvmArchValue))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(propertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(relayLabel)
                    .addComponent(relayValue, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(propertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(relayPortLabel)
                    .addComponent(relayPortValue, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12))
        );

        add(propertiesPanel);
        propertiesPanel.setBounds(6, 27, 299, 140);

        filePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "File Properties", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

        typeLabel.setText("Type:");

        nameLabel.setText("Name:");

        sizeLabel.setText("Size:");

        lastDateLabel.setText("Last Modified:");

        javax.swing.GroupLayout filePanelLayout = new javax.swing.GroupLayout(filePanel);
        filePanel.setLayout(filePanelLayout);
        filePanelLayout.setHorizontalGroup(
            filePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(filePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(filePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lastDateLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(sizeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(typeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(nameLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(filePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(nameLabelValue, javax.swing.GroupLayout.DEFAULT_SIZE, 93, Short.MAX_VALUE)
                    .addComponent(typeLabelValue, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(sizeLabelValue, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lastDateLabelValue, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        filePanelLayout.setVerticalGroup(
            filePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(filePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(filePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(nameLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(nameLabelValue, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(filePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(typeLabelValue, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(typeLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(filePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sizeLabel)
                    .addComponent(sizeLabelValue, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(filePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lastDateLabel)
                    .addComponent(lastDateLabelValue, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(15, 15, 15))
        );

        add(filePanel);
        filePanel.setBounds(340, 300, 210, 120);
    }// </editor-fold>//GEN-END:initComponents

    private void clearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearButtonActionPerformed
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
        HostController theController = theListener.getHostController();
        Host theHost = (Host) theController.getObject();
        theHost.setSessionList( new ArrayList<Session>());
        theController.setIsDirty(true);
        theController.getLibraryItemControllerListener().getListenerComponent().repaint();
        
    }//GEN-LAST:event_clearButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel checkInPanel;
    private javax.swing.JButton clearButton;
    private javax.swing.JPanel filePanel;
    private javax.swing.JLabel jvmArchValue;
    private javax.swing.JLabel jvmLabel;
    private javax.swing.JLabel lastDateLabel;
    private javax.swing.JLabel lastDateLabelValue;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JLabel nameLabelValue;
    private javax.swing.JLabel nextCheckLabel;
    private javax.swing.JLabel nextCheckValLabel;
    private javax.swing.JPanel nicPanel;
    private javax.swing.JScrollPane nicScrollPane;
    private javax.swing.JTable nicTable;
    private javax.swing.JLabel osLabel;
    private javax.swing.JLabel osValue;
    private javax.swing.JPanel propertiesPanel;
    private javax.swing.JLabel relayLabel;
    private javax.swing.JLabel relayPortLabel;
    private javax.swing.JLabel relayPortValue;
    private javax.swing.JLabel relayValue;
    private javax.swing.JScrollPane sessionPane;
    private javax.swing.JTable sessionTable;
    private javax.swing.JLabel sizeLabel;
    private javax.swing.JLabel sizeLabelValue;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JLabel statusValueLabel;
    private javax.swing.JLabel typeLabel;
    private javax.swing.JLabel typeLabelValue;
    // End of variables declaration//GEN-END:variables

    
    //=======================================================================
    /**
     * 
     */
    public void setupLayout(){
        
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(nicPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(propertiesPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(checkInPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(fileSystemPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(filePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE)
                                .addGroup(layout.createSequentialGroup()
                                    .addGap(0, 0, Short.MAX_VALUE)
                                    .addComponent(uploadButton, 30,30,30)
                                    .addComponent(downButton, 30,30,30)
                                    .addGap(0, 0, Short.MAX_VALUE)                                    
                                )
                        )
                    ))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(propertiesPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(nicPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(checkInPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup( layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(fileSystemPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE) 
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(filePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE)
                        .addGap(10)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(uploadButton, 30,30,30)
                            .addComponent(downButton, 30,30,30)
                        )
                        .addGap(18, 18, Short.MAX_VALUE)                      
                    ))
                )
        );
    }
    
    //========================================================================
    /**
     * Get the File Tree Panel
     * @return 
     */
    public FileTreePanel getFileTreePanel() {
        return (FileTreePanel) fileSystemPanel;
    }

    //========================================================================
    /**
     * 
     * @param aNode 
     */
    public void updateFilePanel(FileNode aNode) {
        
        //Set the name
        String filename = aNode.getFile().getName();
        if( filename != null ){
            nameLabelValue.setText(filename);
        }
        
        //Set the type
        byte theType = aNode.getType();
        String theTypeStr = "";
        switch( theType ){
            case FileSystemMsg.FOLDER:
                theTypeStr = "Directory";
                uploadButton.setEnabled(true);
                downButton.setEnabled(false);
                break;
            case FileSystemMsg.FILE:
                theTypeStr = "File";
                uploadButton.setEnabled(false);
                downButton.setEnabled(true);
                break;
            case FileSystemMsg.DRIVE:
                theTypeStr = "Drive";
                uploadButton.setEnabled(false);
                downButton.setEnabled(false);
                break;
        }
        typeLabelValue.setText(theTypeStr);
        
        //Get the size
        long size = aNode.getSize();
        sizeLabelValue.setText(Long.toString(size));
        
        //Set last modified
        String dateStr = aNode.getLastModified();
        lastDateLabelValue.setText(dateStr);
        
    }

}
