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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import pwnbrew.host.Host;
import pwnbrew.host.HostController;
import pwnbrew.host.HostFactory;
import pwnbrew.log.Log;
import pwnbrew.log.LoggableException;
import pwnbrew.manager.PortManager;
import pwnbrew.manager.DataManager;
import pwnbrew.manager.ServerManager;
import pwnbrew.misc.Constants;
import pwnbrew.network.ControlOption;
import pwnbrew.utilities.SocketUtilities;

/**
 *
 *  
 */
public final class GetHosts extends MaltegoMessage{ // NO_UCD (use default)
    
    private static final String NAME_Class = GetHosts.class.getSimpleName();
    private static final byte OPTION_HOST_ID = 124;

    private int hostId;

    // ==========================================================================
    /**
     * Constructor
     *
     * @param passedId
    */
    public GetHosts(byte[] passedId ) {
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
    */
    @Override
    public void evaluate( PortManager passedManager ) {     
        
        ServerManager aSM = (ServerManager) passedManager;
        //Get the host controllers 
        List<HostController> theHostControllers = new ArrayList<>();
        if( hostId == 0 ){

            //Add everything
            theHostControllers.addAll( aSM.getHostControllers() );

        } else if( hostId == Constants.SERVER_ID ){

            try {
                Host localHost = HostFactory.getLocalHost();
                List<String> hostIdList = localHost.getConnectedHostIdList();
                for( String anId : hostIdList ){
                    HostController aController = aSM.getHostController(anId);
                    if(aController != null )
                        theHostControllers.add(aController);

                }

            } catch (LoggableException ex) {
                Log.log(Level.WARNING, NAME_Class, "evaluate()", ex.getMessage(), ex );                                
            }

        } else {

            //Get the host
            HostController aHostController = aSM.getHostController( Integer.toString( hostId ));
            Host aHost = aHostController.getHost();
            List<String> internalHosts = aHost.getConnectedHostIdList();

            //Add each host to the list
            for( String anId : internalHosts ){
                HostController aController = aSM.getHostController( anId );
                if( aController != null)
                    theHostControllers.add(aController);
            }
        }                

        //Create a hsot msg for each controller
        for( HostController aHostController : theHostControllers ){
            
            if( !aHostController.isLocalHost() ){

                Host aHost = aHostController.getHost();
                try {

                    HostMsg aHostMsg = new HostMsg( getSrcHostId(), aHost.getHostname(), 
                        aHost.getOsName(), aHost.getJvmArch(), Integer.parseInt(aHost.getId()), aHost.isConnected(),
                        !aHost.getCheckInList().isEmpty() );

                    String relayPort = aHost.getRelayPort();
                    if( !relayPort.isEmpty() )
                        aHostMsg.addRelayPort( Integer.parseInt(relayPort));

                    DataManager.send( passedManager, aHostMsg);
                } catch ( UnsupportedEncodingException ex) {
                    Log.log(Level.WARNING, NAME_Class, "evaluate()", ex.getMessage(), ex );                                
                }

            }
            
        }           
            
    }

}/* END CLASS GetHosts */
