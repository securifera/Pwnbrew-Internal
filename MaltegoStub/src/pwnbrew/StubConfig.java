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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import pwnbrew.log.LoggableException;
import pwnbrew.misc.Constants;
import pwnbrew.misc.DebugPrinter;
import pwnbrew.misc.LoaderUtilities;
import pwnbrew.misc.SocketUtilities;
import pwnbrew.misc.Utilities;

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
    private String theStorePass = "password";
    private String theCertAlias = "";
  
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
        theCertAlias = passedAlias;
    }
    
     //==========================================================================
    /**
     * Returns the password to the java keystore for encryption
     * @return 
     * @throws pwnbrew.log.LoggableException 
    */
    public String getKeyStorePass() throws LoggableException {
       return theStorePass;
    }

     //==========================================================================
    /**
     * Sets the password to the java keystore in the configuration file
     * @param passedKey
     * @throws pwnbrew.log.LoggableException
    */
    public void setKeyStorePass( String passedKey ) throws LoggableException {
        theStorePass = passedKey;       
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
        try {
        
            Integer anInteger = SocketUtilities.getNextId();
            localConf.setHostId(anInteger.toString());
            
            //Get the manifest
            Utilities.ManifestProperties localProperties = new Utilities.ManifestProperties();
            String properties = Constants.MANIFEST_FILE;
       
            URL ourUrl = StubConfig.class.getProtectionDomain().getCodeSource().getLocation();
            String aStr = ourUrl.toExternalForm();

            final URL manifest =  new URL("jar:" + aStr + "!/" + properties);
            URLConnection theConnection = manifest.openConnection();
            InputStream localInputStream = theConnection.getInputStream();

            if (localInputStream != null) {

                //Load the properties
                localProperties.load(localInputStream);
                localInputStream.close();

                //Get the alias
                String certAlias = localProperties.getProperty(Constants.CERT_ALIAS, null);
                if( certAlias != null )
                    localConf.setAlias(certAlias );
                
                //Get the alias
                String certPW = localProperties.getProperty(Constants.CERT_PW, null);
                if( certPW != null )
                    localConf.setKeyStorePass(certPW);
                                
            }
            
        } catch ( LoggableException | IOException ex ){
            DebugPrinter.printMessage( NAME_Class, "listclients", ex.getMessage(), ex );
        }

       
        return localConf;
    } 
    
     //==========================================================================
    /**
     * Writes the configuration file to the appropriate place
     * @throws pwnbrew.log.LoggableException
    */
    public synchronized void writeSelfToDisk() throws LoggableException {
            
        //Check for null
        File theClassPath = Utilities.getClassPath();
        ClassLoader aClassLoader;

        //Check if we are coming from a stager 
        aClassLoader = ClassLoader.getSystemClassLoader();            

        //Get the properties 
        String properties = Constants.MANIFEST_FILE;
        Map<String, String> propMap = new HashMap<>();
        propMap.put(Constants.CERT_ALIAS, theCertAlias );
        propMap.put(Constants.CERT_PW, theStorePass );
      
        //Unload the stager
        LoaderUtilities.unloadLibs( aClassLoader );

        //Add the properties
        Utilities.updateJarProperties( theClassPath, properties, propMap ); 

        //Load it back
        LoaderUtilities.reloadLib(theClassPath); 

    }

}/* END CLASS StubConfig */
