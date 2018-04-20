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
package pwnbrew.notification;

import com.sun.mail.util.MailSSLSocketFactory;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;
import pwnbrew.log.Log;

/**
 *
 * @author user
 */
public class SmtpNotifier {
    
    private static SmtpNotifier smtpInstance;
    private static final int DEFAULT_MAIL_PORT = 465;
    private String theProtocol = "smtps";
    private String theRecipient = null;
    
    private static final String NAME_Class = SmtpNotifier.class.getSimpleName();
    
    private SmtpNotifier() throws GeneralSecurityException{
        
        Properties props = System.getProperties();
        props.put("mail.smtps.port", Integer.toString(DEFAULT_MAIL_PORT));
        props.put("mail.smtps.ssl.enable", "true");        
        props.put("mail.from", "notifcation@pwnbrew.io");        
        props.put("mail.smtps.auth", "false");        
         
        MailSSLSocketFactory sf = new MailSSLSocketFactory();
	sf.setTrustAllHosts(true);
	props.put("mail.smtps.ssl.socketFactory", sf);
        
    }
    
    public static SmtpNotifier getInstance() throws GeneralSecurityException{
        if(smtpInstance == null){
            smtpInstance = new SmtpNotifier();
        }
        return smtpInstance;
    }
    
    //==========================================================================
    /**
     * 
     * @param passedHost 
     */
    public void setMailHost(String passedHost){
        Properties props = System.getProperties();
        props.put("mail.smtps.host", passedHost);
    }
    
    //==========================================================================
    /**
     * 
     * @param passedHost 
     */
    public void setLocalHost(String passedHost){
        Properties props = System.getProperties();
        props.put("mail.smtps.localhost", passedHost);
    }
    
    //==========================================================================
    /**
     * 
     * @param passedPort 
     */
    public void setMailPort(int passedPort){
        Properties props = System.getProperties();
        props.put("mail.smtps.port", Integer.toString(passedPort));
    }
    
     //==========================================================================
    /**
     * 
     * @param passedEmail
     */
    public void setRecipient(String passedEmail){
        theRecipient = passedEmail;
    }
    
    //==========================================================================
    /**
     * 
     * @param passedProtocol
     */
    public void setMailPort(String passedProtocol){
        theProtocol = passedProtocol;
    }
    
    //=========================================================================
    /**
     * 
     * @param subject 
     * @param messageBody 
     */
    public void sendMail( String subject, String messageBody ){
               
        if( theRecipient == null){            
            Log.log(Level.WARNING, NAME_Class, "sendMail()", "Unable to send notification. Recipient is not set.", null );
            return;
        }
        
        Session session = Session.getInstance(System.getProperties(), null);
        MimeMessage msg = new MimeMessage(session);
                
        try {
            
            msg.setRecipients(Message.RecipientType.TO, theRecipient);
            msg.setSubject(subject);
            msg.setSentDate(new Date());
            msg.setText(messageBody);
            
            Transport trnsport;
            trnsport = session.getTransport(theProtocol);
            trnsport.connect();
            msg.saveChanges(); 
            trnsport.sendMessage(msg, msg.getAllRecipients());
            trnsport.close();
            
        } catch (MessagingException ex) {
            Log.log(Level.WARNING, NAME_Class, "sendMail()", ex.getMessage(), ex );
        }
        
    }
        
}
