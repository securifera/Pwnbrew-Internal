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
* ServerConfig.java
*
* Created on July 20, 2013, 6:17:21 PM
*/

package pwnbrew.xml;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import pwnbrew.log.Log;
import pwnbrew.log.LoggableException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import pwnbrew.misc.Constants;
import pwnbrew.misc.Directories;
import pwnbrew.utilities.SocketUtilities;

/**
 *
 *  
 */
public class ServerConfig extends XmlObject {

    private static final String ATTRIBUTE_HostId = "hostId";
    
    //Java keystore variables
    private static final String ATTRIBUTE_StorePass = "storePass";
    private static final String ATTRIBUTE_CertAlias = "certAlias";
    
    //Configurable Ports
    private static final String ATTRIBUTE_CommPort = "ctrlPort";   
    
    //Current version
    private static final String ATTRIBUTE_CurrentVersion = "version";  
    
    //SMTP settings
    private static final String ATTRIBUTE_SMTP_ENABLED = "smtpEnabled";
    private static final String ATTRIBUTE_SMTP_HOST = "smtpHost";
    private static final String ATTRIBUTE_SMTP_PORT = "smtpPort";
    private static final String ATTRIBUTE_SMTP_RECIPIENT = "smtpRecipient";
    private static final String ATTRIBUTE_SMTP_FROM_HOST = "smtpFromHost"; 
    
    
    private static final String theConfigFileName = "config.xml";
    private static ServerConfig theConf = null;
    
    
    private transient static final String aString = "The quick brown fox jumps over the lazy dog.";
    private static final String NAME_Class = ServerConfig.class.getSimpleName();
    
    //===========================================================================
    /**
     *  Constructor
     */
    public ServerConfig() {
        // Extend the object's structure
        thePropertyMap.put( ATTRIBUTE_HostId, ""  );
        thePropertyMap.put( ATTRIBUTE_StorePass, "password" );
        thePropertyMap.put( ATTRIBUTE_CertAlias, "" );
        thePropertyMap.put( ATTRIBUTE_CommPort, Integer.toString(Constants.COMM_PORT) );
        thePropertyMap.put( ATTRIBUTE_CurrentVersion, "" );
        
        //SMTP values
        thePropertyMap.put( ATTRIBUTE_SMTP_ENABLED, "False"  );
        thePropertyMap.put( ATTRIBUTE_SMTP_HOST, ""  );
        thePropertyMap.put( ATTRIBUTE_SMTP_PORT, ""  );
        thePropertyMap.put( ATTRIBUTE_SMTP_RECIPIENT, ""  );
        thePropertyMap.put( ATTRIBUTE_SMTP_FROM_HOST, ""  );
        
    }

     //==========================================================================
    /**
     * Returns the host id
     * @return 
    */
    public String getHostId(){
        return thePropertyMap.get(ATTRIBUTE_HostId);
    }


     //==========================================================================
    /**
     * Returns the alias for the local certificate
     * @return 
    */
    public String getAlias(){
        return thePropertyMap.get(ATTRIBUTE_CertAlias);
    }

     //==========================================================================
    /**
     * Sets the alias for the local certificate
     * @param passedAlias
    */
    public void setAlias(String passedAlias){
        thePropertyMap.put(ATTRIBUTE_CertAlias, passedAlias);
    }

     //==========================================================================
    /**
     * Returns the port of the control channel
     * @return 
    */
    public int getSocketPort(){

        //Return the default if the value is empty
        int retPort = Constants.COMM_PORT;
        String thePort = thePropertyMap.get(ATTRIBUTE_CommPort);
        if(!thePort.isEmpty())
            retPort = Integer.valueOf( thePort );
        
        return retPort;
    }

    //==========================================================================
    /**
     * Sets the port for the control channel
     * @param passedPort
    */
    public void setSocketPort(String passedPort){
        thePropertyMap.put(ATTRIBUTE_CommPort, passedPort);
    }

     //==========================================================================
    /**
     * Returns the password to the java keystore for encryption
     * @return 
    */
    public String getKeyStorePass(){
        return thePropertyMap.get(ATTRIBUTE_StorePass);
    }
    
      //==========================================================================
    /**
     * Sets the password to the java keystore in the configuration file
     * @param passedKey
     * @throws pwnbrew.log.LoggableException
    */
    public void setKeyStorePass( String passedKey ) throws LoggableException {

        //Make sure the passed key pass is not empty
        if(passedKey != null )
            thePropertyMap.put(ATTRIBUTE_StorePass, passedKey);
    }
    
     //==========================================================================
    /**
     * Sets the host id
     * @param hostIdStr
    */
    public void setHostId(String hostIdStr) {
        if(hostIdStr != null){
           thePropertyMap.put(ATTRIBUTE_HostId, hostIdStr);
        }  
    }
    
    //==========================================================================
    /**
     * Returns the current server version stored in the config
     * @return 
    */
    public String getCurrentVersion(){
        return thePropertyMap.get(ATTRIBUTE_CurrentVersion);
    }
    
     //==========================================================================
    /**
     * Sets the current version of the server
     * @param currVersionStr
    */
    public void setCurrentVersion(String currVersionStr) {
        if(currVersionStr != null){
           thePropertyMap.put(ATTRIBUTE_CurrentVersion, currVersionStr);
        }  
    }
    
     //==========================================================================
    /**
     * Writes the configuration file to the appropriate place
     * @throws pwnbrew.log.LoggableException
    */
    public void writeSelfToDisk() throws LoggableException {
        
        try {
            
            byte[] theConfBytes = getXml().getBytes("US-ASCII");
            String rootPathStr = Directories.getHomeDir();
            File configFile = new File(rootPathStr, theConfigFileName );
            
            FileOutputStream theOutStream = new FileOutputStream(configFile);
            BufferedOutputStream theBOS = new BufferedOutputStream(theOutStream);
            try {
                theBOS.write(theConfBytes, 0, theConfBytes.length);
                theBOS.flush();

            } finally {
                //Close output stream
                theBOS.close();
            }
                    
        } catch (IOException ex) {
            throw new LoggableException(ex);
        }

    }
    
    //==========================================================================
    /**
     * Returns the configuration object
     * @return 
     * @throws pwnbrew.log.LoggableException 
    */
    public static ServerConfig getServerConfig() throws LoggableException{
        
        if(theConf == null)
            theConf = loadConfig();        
        
        return theConf;
    }

    //==========================================================================
    /** 
    *  Loads the configuration into memory.
    */
    private static ServerConfig loadConfig() throws LoggableException{

        ServerConfig localConf = null;
        try {

            String rootPathStr = Directories.getHomeDir();
            File configFile = new File(rootPathStr, theConfigFileName );
            if( configFile.exists() ){
         
                try {
                    FileInputStream theFileStream = new FileInputStream(configFile);
                    BufferedInputStream theBufferedIS = new BufferedInputStream(theFileStream);
                    try {

                        int bytesRead = 0;
                        ByteArrayOutputStream theBOS = new ByteArrayOutputStream();
                        try {

                            //Read to the end
                            byte[] byteArr = new byte[1024];
                            while( bytesRead != -1){
                                bytesRead = theBufferedIS.read(byteArr);
                                if(bytesRead != -1){
                                    theBOS.write(byteArr, 0, bytesRead);
                                }
                            }

                            theBOS.flush();

                        } catch (IOException ex) {
                        } finally {
                            try {
                                //Close output stream
                                theBOS.close();
                            } catch (IOException ex) {
                            }
                        }            

                        //Queue up the classes to be sent
                        byte[] theConfBytes = theBOS.toByteArray();

                        //Get the object
                        XmlObject anXB = XmlObjectFactory.createFromXml(new String(theConfBytes, "US-ASCII"));
                        if(anXB instanceof ServerConfig)
                            localConf = (ServerConfig)anXB;                            
                                            

                    } catch (UnsupportedEncodingException ex) {
                    } finally {
                        try {
                            theBufferedIS.close();
                        } catch (IOException ex) {
                        }
                    } 
                } catch(FileNotFoundException ex){                    
                }
                
                //Set the current version
                if(localConf != null && localConf.getCurrentVersion().isEmpty()){
                    localConf.setCurrentVersion( Constants.CURRENT_VERSION );
                    localConf.writeSelfToDisk();
                }

            } else {

                //Create a new configuration file
                localConf = new ServerConfig();
                Integer anInteger = SocketUtilities.getNextId();
                localConf.setHostId(anInteger.toString());
                localConf.setCurrentVersion( Constants.CURRENT_VERSION );
                localConf.writeSelfToDisk();
            }

        } catch ( LoggableException ex ){
            Log.log(Level.WARNING, NAME_Class, "loadConfiguration()", ex.getMessage(), ex);
            throw ex;
        }

        return localConf;
    }
    
}/* END CLASS ServerConfig */
