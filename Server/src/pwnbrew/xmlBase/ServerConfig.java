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

package pwnbrew.xmlBase;

import pwnbrew.logging.Log;
import pwnbrew.logging.LoggableException;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import pwnbrew.Persistence;
import pwnbrew.misc.Constants;
import pwnbrew.utilities.SocketUtilities;
import pwnbrew.utilities.Utilities;

/**
 *
 *  
 */
public class ServerConfig extends XmlBase {

    private static final String ATTRIBUTE_HostId = "hostId";
    private static final String ATTRIBUTE_ShowEditButton = "showEditButton";
    
    //Java keystore variables
    private static final String ATTRIBUTE_StorePass = "storePass";
    private static final String ATTRIBUTE_CertAlias = "certAlias";
    
    //Configurable Ports
    private static final String ATTRIBUTE_CtrlPort = "ctrlPort";
    private static final String ATTRIBUTE_DataPort = "dataPort";
    
    private static ServerConfig theConf = null;
    
    private transient static final String aString = "The quick brown fox jumps over the lazy dog.";
    private static final String NAME_Class = ServerConfig.class.getSimpleName();
    
    //===========================================================================
    /**
     *  Constructor
     */
    public ServerConfig() {
       // Extend the object's structure
       theAttributeMap.put( ATTRIBUTE_HostId, ""  );
       theAttributeMap.put( ATTRIBUTE_ShowEditButton, "FALSE" );
       theAttributeMap.put( ATTRIBUTE_StorePass, "" );
       theAttributeMap.put( ATTRIBUTE_CertAlias, "" );
       theAttributeMap.put( ATTRIBUTE_CtrlPort, Integer.toString(Constants.CONTROL_PORT) );
       theAttributeMap.put( ATTRIBUTE_DataPort, Integer.toString(Constants.DATA_PORT) );

    }

    
    //==========================================================================
    /**
     * Returns whether to show the edit button on the overview panel
     * @return 
    */
    public boolean showEditButton(){

       String theVal = theAttributeMap.get(ATTRIBUTE_ShowEditButton);
       if(theVal.equals("TRUE")){
          return true;
       }
       return false;
    }

    //==========================================================================
    /**
     * Sets the show edit button flag
     * @param shouldShow
    */
    public void setShowEditButton(boolean shouldShow){

       String showFlag = "FALSE";
       if(shouldShow){
          showFlag = "TRUE";
       }

       theAttributeMap.put(ATTRIBUTE_ShowEditButton, showFlag);
    }

     //==========================================================================
    /**
     * Returns the host id
     * @return 
    */
    public String getHostId(){

       return theAttributeMap.get(ATTRIBUTE_HostId);
    }


     //==========================================================================
    /**
     * Returns the alias for the local certificate
     * @return 
    */
    public String getAlias(){

       return theAttributeMap.get(ATTRIBUTE_CertAlias);
    }

     //==========================================================================
    /**
     * Sets the alias for the local certificate
     * @param passedAlias
    */
    public void setAlias(String passedAlias){
       theAttributeMap.put(ATTRIBUTE_CertAlias, passedAlias);
    }

     //==========================================================================
    /**
     * Returns the port of the control channel
     * @return 
    */
    public int getControlPort(){

       //Return the default if the value is empty
       int retPort = Constants.CONTROL_PORT;
       String thePort = theAttributeMap.get(ATTRIBUTE_CtrlPort);
       if(!thePort.isEmpty()){
          retPort = Integer.valueOf( thePort );
       }
       return retPort;
    }

    //==========================================================================
    /**
     * Sets the port for the control channel
     * @param passedPort
    */
    public void setControlPort(String passedPort){
       theAttributeMap.put(ATTRIBUTE_CtrlPort, passedPort);
    }

     //****************************************************************************
    /**
     * Returns the port of the data channel
     * @return 
    */
    public int getDataPort(){

       //Return the default if the value is empty
       int retPort = Constants.DATA_PORT;
       String thePort = theAttributeMap.get(ATTRIBUTE_DataPort);
       if(!thePort.isEmpty()){ 
          retPort = Integer.valueOf( thePort );
       }
       return retPort;
    }

    //==========================================================================
    /**
     * Sets the port for the data channel
     * @param passedPort
    */
    public void setDataPort(String passedPort){
       theAttributeMap.put(ATTRIBUTE_DataPort, passedPort);
    }

     //==========================================================================
    /**
     * Returns the password to the java keystore for encryption
     * @return 
     * @throws pwnbrew.logging.LoggableException 
    */
    public String getKeyStorePass() throws LoggableException {

       String retVal = "";
       String b64encoded = theAttributeMap.get(ATTRIBUTE_StorePass);
       String theKey = getHostId();

       if(!b64encoded.isEmpty() && !theKey.isEmpty()){
          retVal = Utilities.simpleDecrypt(b64encoded, theKey);
       }

       return retVal;
    }

     //==========================================================================
    /**
     * Sets the password to the java keystore in the configuration file
     * @param passedKey
     * @throws pwnbrew.logging.LoggableException
    */
    public void setKeyStorePass(String passedKey) throws LoggableException {

       String theKey = getHostId();
       if(theKey.isEmpty()){
          theKey = Integer.toString(SocketUtilities.getNextId());
          setHostId(theKey);
       }

       //Make sure the passed key pass is not empty
       if(passedKey != null ){
           if( !passedKey.isEmpty() && !theKey.isEmpty() ){
              String b64encoded = Utilities.simpleEncrypt(passedKey, theKey);
              if(b64encoded != null){
                 theAttributeMap.put(ATTRIBUTE_StorePass, b64encoded);
              }

           } else {
              theAttributeMap.put(ATTRIBUTE_StorePass, "");
           }
       }
    }
    
     //==========================================================================
    /**
     * Sets the host id
     * @param hostIdStr
    */
    public void setHostId(String hostIdStr) {
        if(hostIdStr != null){
           theAttributeMap.put(ATTRIBUTE_HostId, hostIdStr);
        }  
    }
    
     //==========================================================================
    /**
     * Writes the configuration file to the appropriate place
     * @throws pwnbrew.logging.LoggableException
    */
    public void writeSelfToDisk() throws LoggableException {
        
        try {
            
            byte[] theConfBytes = getXml().getBytes("US-ASCII");
            
            //Encrypt and write to the file
            theConfBytes = Utilities.simpleEncrypt( theConfBytes, aString);
            Persistence.writeLabel( Persistence.CONF_CHUNK, theConfBytes);
        
        } catch (IOException ex) {
            throw new LoggableException(ex);
        }

    }
    
    //==========================================================================
    /**
     * Returns the configuration object
     * @return 
     * @throws pwnbrew.logging.LoggableException 
    */
    public static ServerConfig getServerConfig() throws LoggableException{
        
        if(theConf == null){
            theConf = loadConfig();
        }
        
        return theConf;
    }

    //==========================================================================
    /** 
    *  Loads the configuration into memory.
    */
    private static ServerConfig loadConfig() throws LoggableException{

        ServerConfig localConf = null;
        try {

            List<byte[]> theConfEntries = Persistence.getLabelBytes( Persistence.CONF_CHUNK );
            if( !theConfEntries.isEmpty() ){

                //Decode the bytes
                for( Iterator<byte[]> theIter = theConfEntries.iterator(); theIter.hasNext(); ){

                    try {

                        byte[] theConfBytes = theIter.next();
                        theConfBytes = Utilities.simpleDecrypt( theConfBytes, aString);

                        //Get the object
                        XmlBase anXB = XmlBaseFactory.createFromXml(new String(theConfBytes, "US-ASCII"));
                        if(anXB instanceof ServerConfig){
                            localConf = (ServerConfig)anXB;
                        }
                        break;

                    } catch ( IOException ex) {
                        throw new LoggableException(ex);
                    }

                }

            } else {

                //Create a new configuration file
                localConf = new ServerConfig();
                Integer anInteger = SocketUtilities.getNextId();
                localConf.setHostId(anInteger.toString());
                localConf.writeSelfToDisk();
            }

        } catch ( LoggableException ex ){
            Log.log(Level.WARNING, NAME_Class, "loadConfiguration()", ex.getMessage(), ex);
            throw ex;
        }

        return localConf;
    }
    
}/* END CLASS ServerConfig */
