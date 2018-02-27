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

package pwnbrew.options.panels;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.Border;
import pwnbrew.StubConfig;
import pwnbrew.generic.gui.PanelListener;
import pwnbrew.misc.Constants;
import pwnbrew.misc.FileFilterImp;
import pwnbrew.misc.StandardValidation;

/**
 *
 */
public class NetworkOptionsPanel extends OptionsJPanel {

    private JFileChooser theCertificateChooser = null;
    private final FileFilterImp theCertFilter = new FileFilterImp();
    
    public static final Color COLOR_InputDeviceBackground_Disabled = new Color( 0xDFDFDF );
    public static final Color COLOR_InputDeviceBackground_Normal = Color.WHITE;
        
    private static final String P12_EXT = "p12"; 
    private static final String PFX_EXT = "pfx"; 
    private static final String NAME_Class = NetworkOptionsPanel.class.getSimpleName();
    private static final String SUBJECT = "SUBJECT";
    private static final String ISSUER = "ISSUER";
    
    private ActionEvent lastActionEvt = new ActionEvent(new JTextPane(), 0, "");
   
    //===============================================================
    /**
     *  Constructor
     * @param passedTitle
     * @param parent
    */
    public NetworkOptionsPanel( String passedTitle, PanelListener parent ) {
        super( passedTitle, parent );
        initComponents();
        initializeComponents();
    }

    //===============================================================
    /**
    * Initialize components
    */
    private void initializeComponents() {
        
        //Create a listener
        editCheckbox.setIconTextGap(8);
        editCheckbox.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editJCheckboxActionPerformed(evt);
            }
        });
        
        //Configure the borders for the JTextField...
        Border theBorder = certPathTextField.getBorder();
        Border newBorder = BorderFactory.createCompoundBorder( theBorder, 
                BorderFactory.createEmptyBorder( 4, 4, 4, 4 ) );
        
        //Set the border for each text field
        certPathTextField.setBorder( newBorder );
        issueeName.setBorder( newBorder );
        issuerName.setBorder( newBorder );
        issueeOU.setBorder( newBorder );
        issuerOU.setBorder( newBorder );
        issueeOrg.setBorder( newBorder );
        issuerOrg.setBorder( newBorder );
        issueeCity.setBorder( newBorder );
        issuerCity.setBorder( newBorder );
        issueeState.setBorder( newBorder );
        issuerState.setBorder( newBorder );
        issueeCountry.setBorder( newBorder );
        issuerCountry.setBorder( newBorder );  
        expDateField.setBorder(newBorder);
        
        //Set enablements
        updateComponentEnablements(false);

        //Create a JFileChooser to select wim files...
        theCertFilter.addExt( P12_EXT);
        theCertFilter.addExt( PFX_EXT);
        theCertificateChooser = new JFileChooser();
        theCertificateChooser.setMultiSelectionEnabled(false);
        theCertificateChooser.setFileFilter(theCertFilter);

    }
    
    //=========================================================================
    /**
     * 
     * @param passedPort
     * @param issueeName
     * @param issuerName
     * @param alg
     * @param expDate 
     */
    public void setNetworkSettings( int passedPort, String issueeName, String issuerName, String alg, String expDate ){
         
        //Populate the componenets
        ctrlPortField.setText(Integer.toString( passedPort ));
        
        //Get the issuee name
        populateCertComponents( SUBJECT, issueeName );

        //Get the issuer org

        populateCertComponents( ISSUER, issuerName );

        //Set the algorythm
        algoValLabel.setText(alg);

        //Set the exp date
        expDateField.setText(expDate);     
       
    }
    
    //=============================================================
    /**
     * Handler for when the checkbox is changed
    */
    private void editJCheckboxActionPerformed(java.awt.event.ActionEvent evt) {

       if(lastActionEvt != evt ){
           updateComponentEnablements(editCheckbox.isSelected());
       }
       lastActionEvt = evt;
    }
    
    // =============================================================
    /**
    * Enables or disables the components
    */
    void updateComponentEnablements(boolean passedBool){
        
        issueeName.setEnabled(passedBool);
        issuerName.setEnabled(passedBool);
        issueeOU.setEnabled(passedBool);
        issuerOU.setEnabled(passedBool);
        issueeOrg.setEnabled(passedBool);
        issuerOrg.setEnabled(passedBool);
        issueeCity.setEnabled(passedBool);
        issuerCity.setEnabled(passedBool);
        issueeState.setEnabled(passedBool);
        issuerState.setEnabled(passedBool);
        issueeCountry.setEnabled(passedBool);
        issuerCountry.setEnabled(passedBool); 
        expDateField.setEnabled(passedBool);

        if(passedBool == false){
           
            issueeName.setBackground( COLOR_InputDeviceBackground_Disabled);
            issuerName.setBackground( COLOR_InputDeviceBackground_Disabled);
            issueeOU.setBackground( COLOR_InputDeviceBackground_Disabled);
            issuerOU.setBackground( COLOR_InputDeviceBackground_Disabled);
            issueeOrg.setBackground( COLOR_InputDeviceBackground_Disabled);
            issuerOrg.setBackground( COLOR_InputDeviceBackground_Disabled);
            issueeCity.setBackground( COLOR_InputDeviceBackground_Disabled);
            issuerCity.setBackground( COLOR_InputDeviceBackground_Disabled);
            issueeState.setBackground( COLOR_InputDeviceBackground_Disabled);
            issuerState.setBackground( COLOR_InputDeviceBackground_Disabled);
            issueeCountry.setBackground( COLOR_InputDeviceBackground_Disabled);
            issuerCountry.setBackground( COLOR_InputDeviceBackground_Disabled);
            expDateField.setBackground( COLOR_InputDeviceBackground_Disabled);
            
            
        } else {
            issueeName.setBackground( COLOR_InputDeviceBackground_Normal);
            issuerName.setBackground( COLOR_InputDeviceBackground_Normal);
            issueeOU.setBackground( COLOR_InputDeviceBackground_Normal);
            issuerOU.setBackground( COLOR_InputDeviceBackground_Normal);
            issueeOrg.setBackground( COLOR_InputDeviceBackground_Normal);
            issuerOrg.setBackground( COLOR_InputDeviceBackground_Normal);
            issueeCity.setBackground( COLOR_InputDeviceBackground_Normal);
            issuerCity.setBackground( COLOR_InputDeviceBackground_Normal);
            issueeState.setBackground( COLOR_InputDeviceBackground_Normal);
            issuerState.setBackground( COLOR_InputDeviceBackground_Normal);
            issueeCountry.setBackground( COLOR_InputDeviceBackground_Normal);
            issuerCountry.setBackground( COLOR_InputDeviceBackground_Normal);
            expDateField.setBackground(  COLOR_InputDeviceBackground_Normal);
        }

    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        encryptionPanel = new javax.swing.JPanel();
        certLabel = new javax.swing.JLabel();
        certPathTextField = new javax.swing.JTextField();
        fileChooserButton = new javax.swing.JButton();
        passwordLabel = new javax.swing.JLabel();
        passwordField = new javax.swing.JPasswordField();
        noteLabel = new javax.swing.JLabel();
        algoLabel = new javax.swing.JLabel();
        algoValLabel = new javax.swing.JLabel();
        certLabel3 = new javax.swing.JLabel();
        importButton = new javax.swing.JButton();
        expDateField = new javax.swing.JTextField();
        issuedToPanel = new javax.swing.JPanel();
        certLabel8 = new javax.swing.JLabel();
        certLabel9 = new javax.swing.JLabel();
        algoLabel3 = new javax.swing.JLabel();
        certLabel10 = new javax.swing.JLabel();
        algoLabel4 = new javax.swing.JLabel();
        certLabel11 = new javax.swing.JLabel();
        issueeName = new javax.swing.JTextField();
        issueeOrg = new javax.swing.JTextField();
        issueeCity = new javax.swing.JTextField();
        issueeOU = new javax.swing.JTextField();
        issueeState = new javax.swing.JTextField();
        issueeCountry = new javax.swing.JTextField();
        issuedByPanel = new javax.swing.JPanel();
        certLabel12 = new javax.swing.JLabel();
        certLabel13 = new javax.swing.JLabel();
        algoLabel5 = new javax.swing.JLabel();
        certLabel14 = new javax.swing.JLabel();
        certLabel15 = new javax.swing.JLabel();
        issuerName = new javax.swing.JTextField();
        issuerOrg = new javax.swing.JTextField();
        issuerCity = new javax.swing.JTextField();
        issuerOU = new javax.swing.JTextField();
        issuerState = new javax.swing.JTextField();
        issuerCountry = new javax.swing.JTextField();
        certLabel16 = new javax.swing.JLabel();
        ctrlPortLabel = new javax.swing.JLabel();
        ctrlPortField = new javax.swing.JTextField();
        editCheckbox = new javax.swing.JCheckBox();

        encryptionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("PKI Certificate Information"));

        certLabel.setText("Certificate Path:");

        certPathTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                certPathTextFieldKeyReleased(evt);
            }
        });

        fileChooserButton.setText("...");
        fileChooserButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileChooserButtonActionPerformed(evt);
            }
        });

        passwordLabel.setText("Password:");

        passwordField.setPreferredSize(new java.awt.Dimension(111, 22));
        passwordField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                passwordFieldKeyReleased(evt);
            }
        });

        noteLabel.setText("(Note:  If a certificate path is not provided, one will be generated.)");

        algoLabel.setText("Algorithm:");

        certLabel3.setText("Expiration:");

        importButton.setText("Import");
        importButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importButtonActionPerformed(evt);
            }
        });

        expDateField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                expDateFieldKeyReleased(evt);
            }
        });

        issuedToPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Issued To"));

        certLabel8.setText("Organization:");

        certLabel9.setText("OU:");

        algoLabel3.setText("Name:");

        certLabel10.setText("State:");

        algoLabel4.setText("City:");

        certLabel11.setText("Country:");

        javax.swing.GroupLayout issuedToPanelLayout = new javax.swing.GroupLayout(issuedToPanel);
        issuedToPanel.setLayout(issuedToPanelLayout);
        issuedToPanelLayout.setHorizontalGroup(
            issuedToPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(issuedToPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(issuedToPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(issuedToPanelLayout.createSequentialGroup()
                        .addGroup(issuedToPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(algoLabel3)
                            .addComponent(certLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(issuedToPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(issuedToPanelLayout.createSequentialGroup()
                                .addComponent(issueeName, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(certLabel8))
                            .addGroup(issuedToPanelLayout.createSequentialGroup()
                                .addComponent(issueeOU, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(issuedToPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(certLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(algoLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(issuedToPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(issueeCountry, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(issueeCity, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(issueeOrg, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(issuedToPanelLayout.createSequentialGroup()
                        .addComponent(certLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(issueeState, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        issuedToPanelLayout.setVerticalGroup(
            issuedToPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(issuedToPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(issuedToPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(algoLabel3)
                    .addComponent(certLabel8)
                    .addComponent(issueeName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(issueeOrg, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(issuedToPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(certLabel9)
                    .addComponent(issueeOU, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(algoLabel4)
                    .addComponent(issueeCity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(issuedToPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(certLabel10)
                    .addComponent(issueeState, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(issueeCountry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(certLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        issuedByPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Issued By"));

        certLabel12.setText("Organization:");

        certLabel13.setText("OU:");

        algoLabel5.setText("Name:");

        certLabel14.setText("State:");

        certLabel15.setText("Country:");

        certLabel16.setText("City:");

        javax.swing.GroupLayout issuedByPanelLayout = new javax.swing.GroupLayout(issuedByPanel);
        issuedByPanel.setLayout(issuedByPanelLayout);
        issuedByPanelLayout.setHorizontalGroup(
            issuedByPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(issuedByPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(issuedByPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(issuedByPanelLayout.createSequentialGroup()
                        .addComponent(certLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(issuerState, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(issuedByPanelLayout.createSequentialGroup()
                        .addGroup(issuedByPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(algoLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, 34, Short.MAX_VALUE)
                            .addComponent(certLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(issuedByPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(issuerOU, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(issuerName, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(issuedByPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(certLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(certLabel15)
                    .addComponent(certLabel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(issuedByPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(issuerOrg, javax.swing.GroupLayout.DEFAULT_SIZE, 137, Short.MAX_VALUE)
                    .addComponent(issuerCity)
                    .addComponent(issuerCountry))
                .addContainerGap(20, Short.MAX_VALUE))
        );
        issuedByPanelLayout.setVerticalGroup(
            issuedByPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(issuedByPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(issuedByPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(algoLabel5)
                    .addComponent(certLabel12)
                    .addComponent(issuerName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(issuerOrg, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(issuedByPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(certLabel13)
                    .addComponent(issuerOU, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(issuerCity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(certLabel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(issuedByPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(issuedByPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(issuerCountry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(certLabel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(issuedByPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(certLabel14)
                        .addComponent(issuerState, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout encryptionPanelLayout = new javax.swing.GroupLayout(encryptionPanel);
        encryptionPanel.setLayout(encryptionPanelLayout);
        encryptionPanelLayout.setHorizontalGroup(
            encryptionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(encryptionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(encryptionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(encryptionPanelLayout.createSequentialGroup()
                        .addGroup(encryptionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(passwordLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(certLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(encryptionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(encryptionPanelLayout.createSequentialGroup()
                                .addGroup(encryptionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(noteLabel)
                                    .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(46, 46, 46))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, encryptionPanelLayout.createSequentialGroup()
                                .addGroup(encryptionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(encryptionPanelLayout.createSequentialGroup()
                                        .addGap(0, 0, Short.MAX_VALUE)
                                        .addComponent(importButton))
                                    .addComponent(certPathTextField))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(fileChooserButton, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(encryptionPanelLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(encryptionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(encryptionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(issuedToPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(issuedByPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(encryptionPanelLayout.createSequentialGroup()
                                .addComponent(algoLabel)
                                .addGap(18, 18, 18)
                                .addComponent(algoValLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(certLabel3)
                                .addGap(18, 18, 18)
                                .addComponent(expDateField, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        encryptionPanelLayout.setVerticalGroup(
            encryptionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(encryptionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(encryptionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(algoValLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(expDateField)
                    .addComponent(certLabel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(algoLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 10, Short.MAX_VALUE)
                .addComponent(issuedToPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(issuedByPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(encryptionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(certLabel)
                    .addComponent(certPathTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fileChooserButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(encryptionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(passwordLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(noteLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(importButton)
                .addContainerGap())
        );

        ctrlPortLabel.setText("Communication Port:");

        ctrlPortField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        ctrlPortField.setMargin(new java.awt.Insets(4, 4, 4, 4));
        ctrlPortField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                ctrlPortFieldKeyReleased(evt);
            }
        });

        editCheckbox.setText("Edit Certificate");
        editCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editCheckboxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(encryptionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(ctrlPortLabel)
                        .addGap(18, 18, 18)
                        .addComponent(ctrlPortField, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(editCheckbox)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(ctrlPortField, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(ctrlPortLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(17, 17, 17))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(editCheckbox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)))
                .addComponent(encryptionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void ctrlPortFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_ctrlPortFieldKeyReleased
        evaluateValue(ctrlPortField);
        setSaveButton(true);
    }//GEN-LAST:event_ctrlPortFieldKeyReleased

    private void certPathTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_certPathTextFieldKeyReleased
        setSaveButton(true);
    }//GEN-LAST:event_certPathTextFieldKeyReleased

    private void fileChooserButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileChooserButtonActionPerformed
        selectKeystorePath();
    }//GEN-LAST:event_fileChooserButtonActionPerformed

    private void passwordFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_passwordFieldKeyReleased
        setSaveButton(true);
    }//GEN-LAST:event_passwordFieldKeyReleased

    private void expDateFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_expDateFieldKeyReleased
        setSaveButton(true);
    }//GEN-LAST:event_expDateFieldKeyReleased

    private void importButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importButtonActionPerformed
        String certPath = certPathTextField.getText();
        char[] passArr = passwordField.getPassword();

        //If the the cert was defined
        if(!certPath.isEmpty()){
            File theCertFile= new File(certPath);

            //Load the cert into the keystore
            if(theCertFile.exists())
                getListener().sendCertFile( theCertFile, new String(passArr));
        }
    }//GEN-LAST:event_importButtonActionPerformed

    private void editCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editCheckboxActionPerformed
        setSaveButton(true);
    }//GEN-LAST:event_editCheckboxActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel algoLabel;
    private javax.swing.JLabel algoLabel3;
    private javax.swing.JLabel algoLabel4;
    private javax.swing.JLabel algoLabel5;
    private javax.swing.JLabel algoValLabel;
    private javax.swing.JLabel certLabel;
    private javax.swing.JLabel certLabel10;
    private javax.swing.JLabel certLabel11;
    private javax.swing.JLabel certLabel12;
    private javax.swing.JLabel certLabel13;
    private javax.swing.JLabel certLabel14;
    private javax.swing.JLabel certLabel15;
    private javax.swing.JLabel certLabel16;
    private javax.swing.JLabel certLabel3;
    private javax.swing.JLabel certLabel8;
    private javax.swing.JLabel certLabel9;
    private javax.swing.JTextField certPathTextField;
    private javax.swing.JTextField ctrlPortField;
    private javax.swing.JLabel ctrlPortLabel;
    private javax.swing.JCheckBox editCheckbox;
    private javax.swing.JPanel encryptionPanel;
    private javax.swing.JTextField expDateField;
    private javax.swing.JButton fileChooserButton;
    private javax.swing.JButton importButton;
    private javax.swing.JPanel issuedByPanel;
    private javax.swing.JPanel issuedToPanel;
    private javax.swing.JTextField issueeCity;
    private javax.swing.JTextField issueeCountry;
    private javax.swing.JTextField issueeName;
    private javax.swing.JTextField issueeOU;
    private javax.swing.JTextField issueeOrg;
    private javax.swing.JTextField issueeState;
    private javax.swing.JTextField issuerCity;
    private javax.swing.JTextField issuerCountry;
    private javax.swing.JTextField issuerName;
    private javax.swing.JTextField issuerOU;
    private javax.swing.JTextField issuerOrg;
    private javax.swing.JTextField issuerState;
    private javax.swing.JLabel noteLabel;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JLabel passwordLabel;
    // End of variables declaration//GEN-END:variables

    // ==========================================================================
    /**
    * Selects the keystore path via a {@link JFileChooser}.
    */
    private void selectKeystorePath() {

        File userSelectedFile = null;


        int returnVal = theCertificateChooser.showDialog( this, "Select PKCS#12 File" ); //Show the dialogue
        switch( returnVal ) {

           case JFileChooser.CANCEL_OPTION: //If the user canceled the selecting...
              break;
           case JFileChooser.ERROR_OPTION: //If the dialogue was dismissed or an error occurred...
              break; //Do nothing

           case JFileChooser.APPROVE_OPTION: //If the user approved the selection...
              userSelectedFile = theCertificateChooser.getSelectedFile(); //Get the files the user selected
              break;
           default:
              break;

        }

        //Check if the returned file is valid
        if(userSelectedFile == null  || userSelectedFile.isDirectory())
           return;
        

        String userPath = userSelectedFile.getAbsolutePath();
        certPathTextField.setText(userPath);
        setSaveButton(true);

    }/* END selectKeystorePath() */

     // ==========================================================================
    /**
     * Evaluates the current value in the {@link JTextField}.
     * <p>
     * If the value is a valid port this method sets the background of th
     * field to white; otherwise the background color is set to red.
     *
     */
    private boolean evaluateValue(JTextField passedField) {

        String value = passedField.getText(); //Get the value
        boolean dataIsValid = false;

        try {
           dataIsValid = StandardValidation.validate( StandardValidation.KEYWORD_Port, value );
        } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(NetworkOptionsPanel.class.getName()).log(Level.SEVERE, null, ex);
        }

        //Set the backgroun color of the text field...
        if( dataIsValid )
           passedField.setBackground(Color.WHITE);
        else 
           passedField.setBackground(Color.RED);
        

        return dataIsValid;


    }/* END evaluateValue() */

    //===============================================================
    /**
     * Saves any changes that have been performed
    */
    @Override
    public void saveChanges(){

        //Reset the dirty flag
        setDirtyFlag( false );
          
        //Set the server ip and port
        StubConfig theConf = StubConfig.getConfig();
        if(theConf != null){

            String ctrlPortStr = ctrlPortField.getText();

            //Ensure the ports are valid
            if(!evaluateValue(ctrlPortField)){
                JOptionPane.showMessageDialog( this, "The control port is invalid.","Error", JOptionPane.ERROR_MESSAGE );
                ctrlPortStr = Integer.toString( theConf.getSocketPort() );
            }

            //If the the cert was defined
            if( editCheckbox.isSelected() ){

                try {
                                       
                    String issueeDN = constructDN( SUBJECT );
                    String issuerDN = constructDN( ISSUER );

                    //Get the date and calculate how many days between
                    String dateStr = expDateField.getText();
                    Date futureDate = Constants.CHECKIN_DATE_FORMAT.parse(dateStr);
                    long difference = futureDate.getTime() - (new Date()).getTime();
                    int days = (int) (difference / 86400000); //milliseconds in one day

                    getListener().sendCertInfo( Integer.parseInt(ctrlPortStr), issueeDN, issuerDN, days );

                } catch (ParseException ex) {
                    Logger.getLogger(NetworkOptionsPanel.class.getName()).log(Level.SEVERE, null, ex);
                }

            }

        }

    } 

    //===============================================================
    /**
     *  Populates the components from the cert being used for encryption
     * 
     * @param passedFlag flag
     * @param theName 
     */
    private void populateCertComponents(String passedFlag, String theName) {
    
        //Split the DN into components
        String[] theDnArr = theName.split(",");
        for( String aStr : theDnArr ){
            String[] DnElementArr = aStr.trim().split("=");
            if( DnElementArr.length == 2){
                
                String type = DnElementArr[0];
                String value = DnElementArr[1];
                
                //Switch on the type
                switch( type ){
                    case "CN":
                        switch( passedFlag ){
                            case SUBJECT:
                                issueeName.setText(value);
                                break;
                            case ISSUER:
                                issuerName.setText(value);
                                break;
                        }                            
                        break;
                    case "OU":
                        switch( passedFlag ){
                            case SUBJECT:
                                issueeOU.setText(value);
                                break;
                            case ISSUER:
                                issuerOU.setText(value);
                                break;
                        }
                        break;
                    case "O":
                        switch( passedFlag ){
                            case SUBJECT:
                                issueeOrg.setText(value);
                                break;
                            case ISSUER:
                                issuerOrg.setText(value);
                                break;
                        }
                        break;
                    case "L": 
                        switch( passedFlag ){
                            case SUBJECT:
                                issueeCity.setText(value);
                                break;
                            case ISSUER:
                                issuerCity.setText(value);
                                break;
                        }
                        break;
                    case "S": 
                        switch( passedFlag ){
                            case SUBJECT:
                                issueeState.setText(value);
                                break;
                            case ISSUER:
                                issuerState.setText(value);
                                break;
                        }
                        break;
                    case "ST": 
                        switch( passedFlag ){
                            case SUBJECT:
                                issueeState.setText(value);
                                break;
                            case ISSUER:
                                issuerState.setText(value);
                                break;
                        }
                        break;
                    case "C":
                        switch( passedFlag ){
                            case SUBJECT:
                                issueeCountry.setText(value);
                                break;
                            case ISSUER:
                                issuerCountry.setText(value);
                                break;
                        }
                        break;
                    default:
                        break;
                }    
                
                
            }
        }
    
    }

    //===============================================================
    /**
     * 
     * @param passedFlag
     * @return 
     */
    private String constructDN( String passedFlag ) {
    
        StringBuilder aSB = new StringBuilder();
        //Switch on the type
        switch( passedFlag ){
            case SUBJECT:
                 
                //Get the name first
                String tempStr = issueeName.getText();
                if( !tempStr.isEmpty()){
                    aSB.append("CN=").append(tempStr).append(",");
                }
                
                //Get the ou
                tempStr = issueeOU.getText();
                if( !tempStr.isEmpty()){
                    aSB.append("OU=").append(tempStr).append(",");
                }
                
                //Get the org
                tempStr = issueeOrg.getText();
                if( !tempStr.isEmpty()){
                    aSB.append("O=").append(tempStr).append(",");
                }
                
                //Get the city
                tempStr = issueeCity.getText();
                if( !tempStr.isEmpty()){
                    aSB.append("L=").append(tempStr).append(",");
                }
                
                //Get the state
                tempStr = issueeState.getText();
                if( !tempStr.isEmpty()){
                    aSB.append("S=").append(tempStr).append(",");
                }
                
                //Get the country
                tempStr = issueeCountry.getText();
                if( !tempStr.isEmpty()){
                    aSB.append("C=").append(tempStr);
                }
                
                break;
            case ISSUER:
               
                //Get the name first
                tempStr = issuerName.getText();
                if( !tempStr.isEmpty()){
                    aSB.append("CN=").append(tempStr).append(",");
                }
                
                //Get the ou
                tempStr = issuerOU.getText();
                if( !tempStr.isEmpty()){
                    aSB.append("OU=").append(tempStr).append(",");
                }
                
                //Get the org
                tempStr = issuerOrg.getText();
                if( !tempStr.isEmpty()){
                    aSB.append("O=").append(tempStr).append(",");
                }
                
                //Get the city
                tempStr = issuerCity.getText();
                if( !tempStr.isEmpty()){
                    aSB.append("L=").append(tempStr).append(",");
                }
                
                //Get the state
                tempStr = issuerState.getText();
                if( !tempStr.isEmpty()){
                    aSB.append("S=").append(tempStr).append(",");
                }
                
                //Get the country
                tempStr = issuerCountry.getText();
                if( !tempStr.isEmpty()){
                    aSB.append("C=").append(tempStr);
                }
                
                break;
           default:
                break;
        }   
        
        return aSB.toString();
    }
    
    //========================================================================
    /**
     * 
     * @return 
     */
    @Override
    public NetworkPanelListener getListener() {
        return (NetworkPanelListener)super.getListener();
    }


}
