/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pwnbrew.socks;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import javax.swing.Timer;
import pwnbrew.generic.gui.ValidTextField;
import pwnbrew.manager.DataManager;
import pwnbrew.misc.StandardValidation;
import pwnbrew.network.control.messages.SocksOperation;

/**
 *
 * @author user
 */
public class SocksJPanel extends javax.swing.JPanel {

    private final SocksJPanelListener theListener;
    private Timer socksTimer = null;
    private long activeTimeMs = 0;
    
    private static final SimpleDateFormat activeSDF =  new SimpleDateFormat("HH:mm:ss");
    
    /**
     * Creates new form SocksJPanel
     * @param passedListener
     */
    public SocksJPanel( SocksJPanelListener passedListener ) {
        initComponents();
        initializeComponents();
        theListener = passedListener;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        portLabel = new javax.swing.JLabel();
        portJTextField = new ValidTextField( "0" );
        actionButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        activeLabel = new javax.swing.JLabel();

        portLabel.setText("Listening Port:");

        portJTextField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        actionButton.setText("Start");
        actionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionButtonActionPerformed(evt);
            }
        });

        jLabel1.setText("Active:");

        activeLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        activeLabel.setText("0");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(portLabel))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(activeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(portJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 95, Short.MAX_VALUE)
                        .addComponent(actionButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(portLabel)
                    .addComponent(portJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(actionButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(activeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(23, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void actionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actionButtonActionPerformed
        String text = actionButton.getText();
        if(text.equals("Start")){
            
            //Do nothing if port is invalid
            if( !((ValidTextField)portJTextField).isDataValid() )
                return;                        
                      
            //Send message to create channel for socks proxy
            int clientId = theListener.getHostId();
            SocksOperation aSocksMsg = new SocksOperation( clientId, SocksOperation.SOCKS_START );
            DataManager.send( theListener.getPortManager(), aSocksMsg );


            actionButton.setText("Please Wait...");

            //Disable
            portJTextField.setEnabled(false);
                
               
        } else {
            //Change the panels to the defaul
            resetPanel();
        }
    }//GEN-LAST:event_actionButtonActionPerformed

    //======================================================================
    /**
     * 
     * @param b 
     */
    public void disablePanel(boolean b) {
        
        
    
    }
    
    //======================================================================
    /**
     * 
     */
    public void resetPanel(){
    
        //Reset everything
        actionButton.setText("Start");
        if( socksTimer != null ){
            socksTimer.stop();
            socksTimer = null;
        }

        activeTimeMs = 0;

        //Shutdown the server
        SocksMessageManager aSMM = SocksMessageManager.getSocksMessageManager();
        aSMM.stopSocksServer();

        Date aDate = new Date(0);
        activeLabel.setText( activeSDF.format(aDate) );

        //Reenable
        portJTextField.setEnabled(true);
    }
    


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton actionButton;
    private javax.swing.JLabel activeLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JTextField portJTextField;
    private javax.swing.JLabel portLabel;
    // End of variables declaration//GEN-END:variables

    private void initializeComponents() {
        ((ValidTextField)portJTextField).setValidation( StandardValidation.KEYWORD_Port );
        activeSDF.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date aDate = new Date(0);
        activeLabel.setText( activeSDF.format(aDate) );
        portJTextField.setText("1080");
    }

    //================================================================
    /**
     * 
     * @return 
     */
    public String getPortStr(){
        return portJTextField.getText();
    }
    
    //=========================================================================
    /**
     * 
     */
    public void startTimer() {
        
        //Reset everything
        actionButton.setText("Stop");

        //Start the timer
        Date aDate = new Date(0);
        activeLabel.setText( activeSDF.format(aDate) );

        socksTimer = new Timer(1000, new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                activeTimeMs += 1000;
                Date tempDate = new Date(activeTimeMs);
                activeLabel.setText( activeSDF.format(tempDate) );
            }
        });
        socksTimer.start();
    }
}
