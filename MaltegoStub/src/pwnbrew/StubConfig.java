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

import pwnbrew.misc.SocketUtilities;

/**
 *
 *  
 */
public class StubConfig {

    private String theHostId = "";
    private transient String theServerIp = "0.0.0.0";
    
    //Configurable Ports
    private transient int theSocketPort = 443;
    
    //Java keystore variables
    public static final String MALTEGO_CERT_PW = "m@lt3g0";
    public static final String MALTEGO_CERT_ALIAS = "maltego";
  
    //The time to sleep between connections
    private transient static StubConfig theConf = null;    
    private transient static final String NAME_Class = StubConfig.class.getSimpleName();
    
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
        if(ipStr != null)
            theServerIp = ipStr;
    }

    //==========================================================================
    /**
     * Sets the host id
     * @param hostIdStr
    */
    public void setHostId(String hostIdStr) {
        if( hostIdStr != null )
            theHostId = hostIdStr;        
    }
      
    //===========================================================================
    /**
     * Returns the configuration object
     * @return 
    */
    public synchronized static StubConfig getConfig(){
        
        if(theConf == null)
            theConf = loadConfiguration();   
        
        return theConf;
    }
    
    //==========================================================================
    /** 
    *  Loads the configuration into memory.
    */
    private static StubConfig loadConfiguration(){
        
        //Create a new configuration file
        StubConfig localConf = new StubConfig();

        Integer anInteger = SocketUtilities.getNextId();
        localConf.setHostId(anInteger.toString());
       
        return localConf;
    } 

}/* END CLASS StubConfig */
