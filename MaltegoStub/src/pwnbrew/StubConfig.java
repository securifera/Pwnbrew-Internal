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
import pwnbrew.log.LoggableException;
import pwnbrew.misc.DebugPrinter;
import pwnbrew.misc.SocketUtilities;

/**
 *
 *  
 */
public class StubConfig implements Serializable {

    private String theHostId = "";
    private transient String theServerIp = "0.0.0.0";
    
    //Configurable Ports
    private transient int theSocketPort = 443;
  
    //The time to sleep between connections
    private transient static StubConfig theConf = null;
    private String theKeyStorePass = "";
    private String theCertAlias = "";
       
    private static final long serialVersionUID = 1L;
    private transient static final String NAME_Class = StubConfig.class.getSimpleName();
    private transient static final String aString = "The quick brown fox jumps over the lazy dog.";
    
    // ==========================================================================
    /**
     * Constructor
     *
    */
    public StubConfig() {      
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
            
            //Encode and write to the file
            sun.misc.BASE64Encoder encoder = new sun.misc.BASE64Encoder();
            String theConfByteStr = encoder.encode(theConfBytes);            
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
    public static StubConfig getConfig(){
        
        if(theConf == null){
            theConf = loadConfiguration();
        }
        
        return theConf;
    }

    //==========================================================================
    /** 
    *  Loads the configuration into memory.
    */
    private static StubConfig loadConfiguration(){

        List<byte[]> theConfEntries = Persistence.getLabelBytes( Persistence.CONF_CHUNK );
        if( !theConfEntries.isEmpty() ){

            try {

                //Decode the bytes
                for( Iterator<byte[]> theIter = theConfEntries.iterator(); theIter.hasNext(); ){

                    byte[] theConfBytes = theIter.next();
                    sun.misc.BASE64Decoder decoder = new sun.misc.BASE64Decoder();
                    theConfBytes = decoder.decodeBuffer( new String(theConfBytes));

                    //Get the object
                    ByteArrayInputStream theBIS = new ByteArrayInputStream( theConfBytes );
                    ObjectInput theInput = new ObjectInputStream( theBIS );

                    //Get the bytes
                    return (StubConfig) theInput.readObject();
                }
                
            } catch ( ClassNotFoundException | IOException ex ){
                DebugPrinter.printMessage( NAME_Class, "loadConfiguration", ex.getMessage(), ex);
            }

        } 
        
        //Create a new configuration file
        StubConfig localConf = null;
        try {
            
            localConf = new StubConfig();
            Integer anInteger = SocketUtilities.getNextId();
            localConf.setHostId(anInteger.toString());
            localConf.writeSelfToDisk();
            
        } catch ( LoggableException ex ){
            DebugPrinter.printMessage( NAME_Class, "loadConfiguration", ex.getMessage(), ex);
        }

       
        return localConf;
    }
    
    
    //==========================================================================
    /**
     * Returns the password to the java keystore for encryption
     * @return 
     * @throws pwnbrew.log.LoggableException 
    */
    public String getKeyStorePass() throws LoggableException {
        return theKeyStorePass;
    }

    //==========================================================================
    /**
     * Sets the password to the java keystore in the configuration file
     * @param passedKey
     * @throws pwnbrew.log.LoggableException
    */
    public void setKeyStorePass( String passedKey ) throws LoggableException {
        //Make sure the passed key pass is not empty
        if(passedKey != null ){
            theKeyStorePass = passedKey;
        }
    }
    
    //==========================================================================
    /**
     * Returns the alias for the local certificate
     * @return 
    */
    public String getAlias(){
       return theCertAlias;
    }

    //==========================================================================
    /**
     * Sets the alias for the local certificate
     * @param passedAlias
    */
    public void setAlias(String passedAlias){
        if( passedAlias != null ){
            theCertAlias = passedAlias;
        }
    }

}/* END CLASS ClientConfig */
