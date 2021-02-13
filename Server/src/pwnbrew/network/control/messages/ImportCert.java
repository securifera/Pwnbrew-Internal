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
//package pwnbrew.network.control.messages;
//
//import java.io.File;
//import java.io.IOException;
//import java.io.UnsupportedEncodingException;
//import java.security.KeyStoreException;
//import java.security.Principal;
//import java.security.cert.Certificate;
//import java.util.Date;
//import java.util.logging.Level;
//import pwnbrew.log.Log;
//import pwnbrew.log.LoggableException;
//import pwnbrew.manager.PortManager;
//import pwnbrew.manager.DataManager;
//import pwnbrew.misc.Constants;
//import pwnbrew.network.ControlOption;
//import pwnbrew.utilities.SSLUtilities;
//import pwnbrew.xml.ServerConfig;
//
///**
// *
// *  
// */
//public final class ImportCert extends MaltegoMessage { // NO_UCD (use default)
//        
//    private static final byte OPTION_CERT_FILENAME = 78;
//    private static final byte OPTION_CERT_PASSWORD = 79;
//    
//    protected String theCertFilename = null;
//    protected String theCertPass = null;
//    
//    private static final String NAME_Class = ImportCert.class.getSimpleName();
//    
//    public static final short MESSAGE_ID = 0x60;
//        
//    // ==========================================================================
//    /**
//     * Constructor
//     *
//     * @param passedId
//     */
//    public ImportCert(byte[] passedId) {
//        super(passedId);
//    }
//
//    //=========================================================================
//    /**
//     * Sets the variable in the message related to this TLV
//     *
//     * @param tempTlv
//     * @return
//     */
//    @Override
//    public boolean setOption(ControlOption tempTlv) {
//
//        boolean retVal = true;
//        try {
//            byte[] theValue = tempTlv.getValue();
//            switch (tempTlv.getType()) {
//                case OPTION_CERT_FILENAME:
//                    theCertFilename = new String(theValue, "US-ASCII");
//                    break;
//                case OPTION_CERT_PASSWORD:
//                    theCertPass = new String(theValue, "US-ASCII");
//                    break;
//                default:
//                    retVal = false;
//                    break;
//            }
//        } catch (UnsupportedEncodingException ex) {
//            ex = null;
//        }
//        return retVal;
//    }
//
//    //===============================================================
//    /**
//     * Performs the logic specific to the message.
//     *
//     * @param passedManager
//     */
//    @Override
//    public void evaluate( PortManager passedManager) {
//        
//        try {
//            File aFile = File.createTempFile("tmp", null);
//            File libDir = aFile.getParentFile();
//            aFile.delete();
//
//            //Get the filename
//            String[] fileHashFileNameArr = theCertFilename.split(":", 2);
//            if( fileHashFileNameArr.length != 2 ){
//                Log.log(Level.SEVERE, NAME_Class, "evaluate()", "Passed hash filename string is not correct.", null);         
//                return;
//            }
//
//            //Get the jar file
//            File theCertFile = new File( libDir, fileHashFileNameArr[1]);
//            if( theCertFile.exists()){
//                
//                //Import the cert
//                if( SSLUtilities.importPKCS12Keystore(theCertFile, theCertPass.toCharArray()) ){
//                
//                    //Get the conf file
//                    ServerConfig theConf = ServerConfig.getServerConfig();        
//                    if(theConf != null){
//                        
//                        //Get the port
//                        int serverPort = theConf.getSocketPort();
//                            
//                        Certificate theCert = SSLUtilities.getCertificate();
//                        if( theCert instanceof sun.security.x509.X509CertImpl ){
//                            sun.security.x509.X509CertImpl theCustomCert = (sun.security.x509.X509CertImpl)theCert;
//
//                            //Get the issuee name
//                            Principal aPrincipal = theCustomCert.getSubjectDN();
//                            String issueeName = aPrincipal.getName();
//
//                            //Get the issuer org
//                            aPrincipal = theCustomCert.getIssuerDN();
//                            String issuerName = aPrincipal.getName();
//
//                            //Set the algorithm
//                            String theAlgorithm = theCustomCert.getSigAlgName();
//
//                            //Set the date
//                            Date expirationDate = theCustomCert.getNotAfter();
//                            String expDateStr = Constants.CHECKIN_DATE_FORMAT.format(expirationDate);    
//                            
//                            //Set the serial num
//                            String serialNum = theCustomCert.getSerialNumber().toString(10);
//
//                            //Send back the data
//                            NetworkSettingsMsg aMsg = new NetworkSettingsMsg( getSrcHostId(), serverPort, issueeName, issuerName, theAlgorithm, expDateStr, serialNum );
//                            DataManager.send( passedManager, aMsg);
//
//                        }
//                       
//                    }
//                }
//            }        
//            
//        
//        } catch (IOException | LoggableException | KeyStoreException ex) {
//            Log.log(Level.SEVERE, NAME_Class, "evaluate()", ex.getMessage(), ex);         
//        }
//      
//    }
//
//}
