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

package pwnbrew.network.control.messages;

import pwnbrew.host.Host;
import pwnbrew.host.HostController;
import pwnbrew.log.LoggableException;
import pwnbrew.manager.PortManager;
import pwnbrew.manager.DataManager;
import pwnbrew.manager.ServerManager;
import pwnbrew.network.ControlOption;
import pwnbrew.utilities.SocketUtilities;
import pwnbrew.utilities.Utilities;
import pwnbrew.xml.JarItem;
import pwnbrew.xml.ServerConfig;

/**
 *
 *  
 */
public final class UpgradeStagerRelay extends MaltegoMessage { // NO_UCD (use default)
    
    private static final byte OPTION_HOST_ID = 100;
    private int hostId;
    private static final String NAME_Class = UpgradeStagerRelay.class.getSimpleName();

    // ==========================================================================
    /**
     * Constructor
     *
     * @param passedId
    */
    public UpgradeStagerRelay(byte[] passedId ) {
        super( passedId );
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
            case OPTION_HOST_ID:
                hostId = SocketUtilities.byteArrayToInt(theValue);
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
     * @throws pwnbrew.logging.LoggableException
    */
    @Override
    public void evaluate( PortManager passedManager ) throws LoggableException {       

        String hostIdStr = Integer.toString( hostId );
        if( hostId == -1 )
            hostIdStr  = ServerConfig.getServerConfig().getHostId();  

        //Get the host controllers 
        ServerManager aSM = (ServerManager) passedManager;
        HostController theHostController = aSM.getHostController(hostIdStr);
        if( theHostController != null ){
            //Get the host
            Host aHost = theHostController.getHost();
            String jreVersion = aHost.getJreVersion();

            if( jreVersion.length() > 2 ){
                char theChar = jreVersion.charAt(2);
                //Get the jar item
                JarItem theItem = Utilities.getStagerJarItem( String.valueOf(theChar));
           
                //Send the new stager
                UpgradeStager upgradeMsg = new UpgradeStager(hostId, theItem); 
                upgradeMsg.setSrcHostId( getSrcHostId() );
                DataManager.send( passedManager, upgradeMsg );
                
            }

        }           

    }        
    
}
