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


package pwnbrew;

/*
* ClientConfig.java
*
* Created on Oct 21, 2013, 8:21:21 PM
*/

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import pwnbrew.log.RemoteLog;
import pwnbrew.log.LoggableException;
import pwnbrew.misc.Base64Converter;
import pwnbrew.misc.SocketUtilities;

/**
 *
 *  
 */
public class ClientConfig implements Serializable {

    private String theHostId = "";
    private transient String theServerIp = "0.0.0.0";
    private transient int theServerId = -1;
    
    //Configurable Ports
    private transient int theSocketPort = 443;
  
    //The time to sleep between connections
    private transient static ClientConfig theConf = null;
       
    private static final long serialVersionUID = 1L;
    private transient static final String NAME_Class = ClientConfig.class.getSimpleName();
    private transient static final String aString = "The quick brown fox jumps over the lazy dog.";
    
    // ==========================================================================
    /**
     * Constructor
     *
    */
    public ClientConfig() {    
    }

    //==========================================================================
    /**
     * Returns the host id
     * @return 
    */
    public String getHostId(){
        return theHostId;
    }

    //==========================================================================
    /**
     * Returns the port of the control channel
     * @return 
    */
    public int getSocketPort(){
        return theSocketPort;
    }

    //==========================================================================
    /**
     * Sets the port for the control channel
     * @param passedPort
    */
    public void setSocketPort( String passedPort ){
        theSocketPort = Integer.parseInt( passedPort );
    }
      
    //==========================================================================
    /**
     * Returns the server host ip
     * @return 
    */
    public String getServerIp(){
       return theServerIp;
    }
    
    //==========================================================================
    /**
     * Sets the server host ip
     * @param ipStr
    */
    public void setServerIp( String ipStr ) {
        if(ipStr != null){
            theServerIp = ipStr;
        }  
    }

    //==========================================================================
    /**
     * Sets the host id
     * @param hostIdStr
    */
    public void setHostId(String hostIdStr) {
        if( hostIdStr != null ){
            theHostId = hostIdStr;
        }  
    }
    
     //==========================================================================
    /**
     * Returns the server id
     * @return 
    */
    public int getServerId(){
       return theServerId;
    }
    
     //==========================================================================
    /**
     * Sets the server id
     * @param passedId
    */
    public void setServerId( int passedId) {
        theServerId = passedId;
    }

    //==========================================================================
    /**
     * Writes the configuration file to the appropriate place
     * @throws pwnbrew.log.LoggableException
    */
    public void writeSelfToDisk() throws LoggableException {
        
        try {
            
            ByteArrayOutputStream theBos = new ByteArrayOutputStream();
            ObjectOutput theOutput = new ObjectOutputStream( theBos );

            //Get the bytes
            theOutput.writeObject( this );
            byte[] theConfBytes = theBos.toByteArray();
            
            //Encrypt and write to the file
            String theConfByteStr = Base64Converter.encode(theConfBytes);
            //theConfBytes = Utilities.simpleEncrypt( theConfBytes, aString);
            Persistence.writeLabel( Persistence.CONF_CHUNK, theConfByteStr.getBytes());
                
        } catch (IOException ex) {
            throw new LoggableException(ex);
        }

    }
      
    //===========================================================================
    /**
     * Returns the configuration object
     * @return 
    */
    public static ClientConfig getConfig(){
        
        if(theConf == null){
            theConf = loadConfiguration();
        }
        
        return theConf;
    }

    //==========================================================================
    /** 
    *  Loads the configuration into memory.
    */
    private static ClientConfig loadConfiguration(){

        List<byte[]> theConfEntries = Persistence.getLabelBytes( Persistence.CONF_CHUNK );
        if( !theConfEntries.isEmpty() ){

            try {

                //Decode the bytes
                for( Iterator<byte[]> theIter = theConfEntries.iterator(); theIter.hasNext(); ){

                    byte[] theConfBytes = theIter.next();
                    theConfBytes = Base64Converter.decode( new String(theConfBytes));

                    //Get the object
                    ByteArrayInputStream theBIS = new ByteArrayInputStream( theConfBytes );
                    ObjectInput theInput = new ObjectInputStream( theBIS );

                    //Get the bytes
                    return (ClientConfig) theInput.readObject();
                }
                
            } catch ( ClassNotFoundException | IOException ex ){
                RemoteLog.log(Level.WARNING, NAME_Class, "loadConfiguration()", ex.getMessage(), ex);
            }

        } 
        
        //Create a new configuration file
        ClientConfig localConf = null;
        try {
            
            localConf = new ClientConfig();
            Integer anInteger = SocketUtilities.getNextId();
            localConf.setHostId(anInteger.toString());
            localConf.writeSelfToDisk();
            
        } catch ( LoggableException ex ){
            RemoteLog.log(Level.WARNING, NAME_Class, "loadConfiguration()", ex.getMessage(), ex);
        }

       
        return localConf;
    }
    
    //==========================================================================
    /**
     *  Initialize all of the transient fields.
     */
    private void initTransients() {
        theServerIp = "0.0.0.0";
        theServerId = -1;
        theSocketPort = 443;
    }

    //==========================================================================
    /**
     * 
     * @param in
     * @throws IOException
     * @throws ClassNotFoundException 
     */
    private void readObject(java.io.ObjectInputStream in)
        throws IOException, ClassNotFoundException {

        in.defaultReadObject();
        initTransients();
    }


}/* END CLASS ClientConfig */
