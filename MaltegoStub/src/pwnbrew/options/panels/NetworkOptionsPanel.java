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
        issuedToPanel = new javax.swing.JPanel();
        certLabel4 = new javax.swing.JLabel();
        certLabel5 = new javax.swing.JLabel();
        algoLabel1 = new javax.swing.JLabel();
        certLabel6 = new javax.swing.JLabel();
        algoLabel2 = new javax.swing.JLabel();
        certLabel7 = new javax.swing.JLabel();
        issueeName = new javax.swing.JTextField();
        issueeOrg = new javax.swing.JTextField();
        issueeCity = new javax.swing.JTextField();
        issueeOU = new javax.swing.JTextField();
        issueeState = new javax.swing.JTextField();
        issueeCountry = new javax.swing.JTextField();
        importButton = new javax.swing.JButton();
        issuedToPanel1 = new javax.swing.JPanel();
        certLabel12 = new javax.swing.JLabel();
        certLabel13 = new javax.swing.JLabel();
        algoLabel5 = new javax.swing.JLabel();
        certLabel14 = new javax.swing.JLabel();
        algoLabel6 = new javax.swing.JLabel();
        certLabel15 = new javax.swing.JLabel();
        issuerName = new javax.swing.JTextField();
        issuerOrg = new javax.swing.JTextField();
        issuerCity = new javax.swing.JTextField();
        issuerOU = new javax.swing.JTextField();
        issuerState = new javax.swing.JTextField();
        issuerCountry = new javax.swing.JTextField();
        expDateField = new javax.swing.JTextField();
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

        issuedToPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Issued To"));

        certLabel4.setText("Organization:");

        certLabel5.setText("OU:");

        algoLabel1.setText("Name:");

        certLabel6.setText("State:");

        algoLabel2.setText("City:");

        certLabel7.setText("Country:");

        issueeName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                issueeNameKeyReleased(evt);
            }
        });

        issueeOrg.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                issueeOrgKeyReleased(evt);
            }
        });

        issueeCity.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                issueeCityKeyReleased(evt);
            }
        });

        issueeOU.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                issueeOUKeyReleased(evt);
            }
        });

        issueeState.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                issueeStateKeyReleased(evt);
            }
        });

        issueeCountry.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                issueeCountryKeyReleased(evt);
            }
        });

        javax.swing.GroupLayout issuedToPanelLayout = new javax.swing.GroupLayout(issuedToPanel);
        issuedToPanel.setLayout(issuedToPanelLayout);
        issuedToPanelLayout.setHorizontalGroup(
            issuedToPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(issuedToPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(issuedToPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(certLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(algoLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(issuedToPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(issuedToPanelLayout.createSequentialGroup()
                        .addComponent(issueeOU, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(16, 16, 16)
                        .addComponent(algoLabel2)
                        .addGap(6, 6, 6)
                        .addComponent(issueeCity, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(certLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(issueeState, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(certLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(issueeCountry, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(issuedToPanelLayout.createSequentialGroup()
                        .addComponent(issueeName, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(certLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(issueeOrg)))
                .addContainerGap(18, Short.MAX_VALUE))
        );
        issuedToPanelLayout.setVerticalGroup(
            issuedToPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(issuedToPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(issuedToPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(algoLabel1)
                    .addComponent(certLabel4)
                    .addComponent(issueeName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(issueeOrg, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(issuedToPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(certLabel7, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(issuedToPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(algoLabel2)
                        .addComponent(certLabel6)
                        .addComponent(issueeCity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(certLabel5)
                        .addComponent(issueeOU, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(issueeState, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(issueeCountry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        importButton.setText("Import");
        importButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importButtonActionPerformed(evt);
            }
        });

        issuedToPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Issued By"));

        certLabel12.setText("Organization:");

        certLabel13.setText("OU:");

        algoLabel5.setText("Name:");

        certLabel14.setText("State:");

        algoLabel6.setText("City:");

        certLabel15.setText("Country:");

        issuerName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                issuerNameKeyReleased(evt);
            }
        });

        issuerOrg.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                issuerOrgKeyReleased(evt);
            }
        });

        issuerCity.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                issuerCityKeyReleased(evt);
            }
        });

        issuerOU.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                issuerOUKeyReleased(evt);
            }
        });

        issuerState.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                issuerStateKeyReleased(evt);
            }
        });

        issuerCountry.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                issuerCountryKeyReleased(evt);
            }
        });

        javax.swing.GroupLayout issuedToPanel1Layout = new javax.swing.GroupLayout(issuedToPanel1);
        issuedToPanel1.setLayout(issuedToPanel1Layout);
        issuedToPanel1Layout.setHorizontalGroup(
            issuedToPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(issuedToPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(issuedToPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(certLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(algoLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(issuedToPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(issuedToPanel1Layout.createSequentialGroup()
                        .addComponent(issuerOU, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(16, 16, 16)
                        .addComponent(algoLabel6)
                        .addGap(6, 6, 6)
                        .addComponent(issuerCity, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(certLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(issuerState, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(certLabel15)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(issuerCountry, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(issuedToPanel1Layout.createSequentialGroup()
                        .addComponent(issuerName, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(certLabel12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(issuerOrg)))
                .addContainerGap(18, Short.MAX_VALUE))
        );
        issuedToPanel1Layout.setVerticalGroup(
            issuedToPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(issuedToPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(issuedToPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(algoLabel5)
                    .addComponent(certLabel12)
                    .addComponent(issuerName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(issuerOrg, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(issuedToPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(certLabel15, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(issuedToPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(algoLabel6)
                        .addComponent(certLabel14)
                        .addComponent(issuerCity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(certLabel13)
                        .addComponent(issuerOU, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(issuerState, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(issuerCountry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        expDateField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                expDateFieldKeyReleased(evt);
            }
        });

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
                            .addComponent(certLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                        .addGroup(encryptionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(issuedToPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(issuedToPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(encryptionPanelLayout.createSequentialGroup()
                                .addComponent(algoLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
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
                .addGroup(encryptionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(algoValLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(encryptionPanelLayout.createSequentialGroup()
                        .addGroup(encryptionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(certLabel3)
                            .addComponent(algoLabel)
                            .addComponent(expDateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(issuedToPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(issuedToPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(35, 35, 35)
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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
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
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(ctrlPortField, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(ctrlPortLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(14, 14, 14))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(editCheckbox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
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

    private void issueeOUKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_issueeOUKeyReleased
        setSaveButton(true);
    }//GEN-LAST:event_issueeOUKeyReleased

    private void issueeNameKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_issueeNameKeyReleased
        setSaveButton(true);
    }//GEN-LAST:event_issueeNameKeyReleased

    private void issuerOUKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_issuerOUKeyReleased
        setSaveButton(true);
    }//GEN-LAST:event_issuerOUKeyReleased

    private void issuerNameKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_issuerNameKeyReleased
        setSaveButton(true);
    }//GEN-LAST:event_issuerNameKeyReleased

    private void issueeOrgKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_issueeOrgKeyReleased
        setSaveButton(true);
    }//GEN-LAST:event_issueeOrgKeyReleased

    private void issueeCityKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_issueeCityKeyReleased
        setSaveButton(true);
    }//GEN-LAST:event_issueeCityKeyReleased

    private void issueeStateKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_issueeStateKeyReleased
        setSaveButton(true);
    }//GEN-LAST:event_issueeStateKeyReleased

    private void issueeCountryKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_issueeCountryKeyReleased
        setSaveButton(true);
    }//GEN-LAST:event_issueeCountryKeyReleased

    private void issuerOrgKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_issuerOrgKeyReleased
        setSaveButton(true);
    }//GEN-LAST:event_issuerOrgKeyReleased

    private void issuerCityKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_issuerCityKeyReleased
        setSaveButton(true);
    }//GEN-LAST:event_issuerCityKeyReleased

    private void issuerStateKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_issuerStateKeyReleased
        setSaveButton(true);
    }//GEN-LAST:event_issuerStateKeyReleased

    private void issuerCountryKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_issuerCountryKeyReleased
        setSaveButton(true);
    }//GEN-LAST:event_issuerCountryKeyReleased

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


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel algoLabel;
    private javax.swing.JLabel algoLabel1;
    private javax.swing.JLabel algoLabel2;
    private javax.swing.JLabel algoLabel5;
    private javax.swing.JLabel algoLabel6;
    private javax.swing.JLabel algoValLabel;
    private javax.swing.JLabel certLabel;
    private javax.swing.JLabel certLabel12;
    private javax.swing.JLabel certLabel13;
    private javax.swing.JLabel certLabel14;
    private javax.swing.JLabel certLabel15;
    private javax.swing.JLabel certLabel3;
    private javax.swing.JLabel certLabel4;
    private javax.swing.JLabel certLabel5;
    private javax.swing.JLabel certLabel6;
    private javax.swing.JLabel certLabel7;
    private javax.swing.JTextField certPathTextField;
    private javax.swing.JTextField ctrlPortField;
    private javax.swing.JLabel ctrlPortLabel;
    private javax.swing.JCheckBox editCheckbox;
    private javax.swing.JPanel encryptionPanel;
    private javax.swing.JTextField expDateField;
    private javax.swing.JButton fileChooserButton;
    private javax.swing.JButton importButton;
    private javax.swing.JPanel issuedToPanel;
    private javax.swing.JPanel issuedToPanel1;
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

            //Get certpath
            String certPath = certPathTextField.getText();

            //If the the cert was defined
            if(!certPath.isEmpty()){
//                    char[] passArr = passwordField.getPassword();
//                    File theCertFile= new File(certPath);
//
//                    //Load the cert into the keystore
//                    if(theCertFile.exists())
//                        SSLUtilities.importPKCS12Keystore(theCertFile, passArr);                    
//
//                    //Return that the ssl context needs to be recreated
//                    reloadSSL = true;

            } else if( editCheckbox.isSelected() ){

                try {
                                       
                    String issueeDN = constructDN( SUBJECT );
                    String issuerDN = constructDN( ISSUER );

                    //Get the date and calculate how many days between
                    String dateStr = expDateField.getText();
                    Date futureDate = Constants.DEFAULT_DATE_FORMAT.parse(dateStr);
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
