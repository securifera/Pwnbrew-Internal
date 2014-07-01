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
import java.net.SocketException;
import java.util.List;
import java.util.logging.Level;
import pwnbrew.controllers.MainGuiController;
import pwnbrew.host.Host;
import pwnbrew.host.HostController;
import pwnbrew.host.HostFactory;
import pwnbrew.library.LibraryItemController;
import pwnbrew.logging.Log;
import pwnbrew.logging.LoggableException;
import pwnbrew.manager.CommManager;
import pwnbrew.misc.Constants;
import pwnbrew.network.ControlOption;
import pwnbrew.network.control.ControlMessageManager;
import pwnbrew.tasks.TaskManager;
import pwnbrew.utilities.SocketUtilities;
import pwnbrew.xmlBase.ServerConfig;

/**
 *
 *  
 */
public final class GetCount extends ControlMessage{ // NO_UCD (use default)
    
    private static final String NAME_Class = GetCount.class.getSimpleName();    
    private static final byte OPTION_COUNT_ID = 80;
    private static final byte OPTION_OPTIONAL_ID = 81;
    
    public static final byte HOST_COUNT = 20;
    public static final byte NIC_COUNT = 21;
    
    private int countType;
    private int optionalId;

    // ==========================================================================
    /**
     * Constructor
     *
     * @param passedId
    */
    public GetCount(byte[] passedId ) {
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
            case OPTION_COUNT_ID:
                countType = SocketUtilities.byteArrayToInt(theValue);
                break;
            case OPTION_OPTIONAL_ID:
                optionalId = SocketUtilities.byteArrayToInt(theValue);
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
    public void evaluate( CommManager passedManager ) throws LoggableException {     
        
        ControlMessageManager aCMManager = ControlMessageManager.getControlMessageManager();
        if( aCMManager != null ){
            
            int retCount = 0;
            TaskManager theTaskManager = passedManager.getTaskManager();
            switch( countType ){
                case HOST_COUNT:
                    if( theTaskManager instanceof MainGuiController ){

                        //Should ever return null since it's a closing socket
//                        int controlPort = aCMManager.getPort();
//                        ServerPortRouter aSPR = (ServerPortRouter)passedManager.getPortRouter( controlPort );
                        
                        //Get the host controllers 
//                        HashSet<Integer> internalHosts = new HashSet<>();
                        MainGuiController theGuiController = (MainGuiController)theTaskManager;
                        List<LibraryItemController> theHostControllers = theGuiController.getHostControllers();
                        retCount = theHostControllers.size() - 1;
                                
                        //If 
                        if(optionalId == Constants.SERVER_ID ){
                            
                            try {
                                
                                Host localHost = HostFactory.getLocalHost();
                                List<String> hostIdList = localHost.getConnectedHostIdList();
//                                for( LibraryItemController aController : theHostControllers ){
//                                    if( aController instanceof HostController ){
//                                        HostController aHostController = (HostController)aController;
//                                        
//                                        //Get host id
//                                        if( !aHostController.isLocalHost() ){
//                                            int clientId = Integer.parseInt( aHostController.getId() );
//                                            SocketChannelHandler aHandler = aSPR.getSocketChannelHandler( clientId );
//                                            
//                                            //Add to the hash set
//                                            if( aHandler != null )
//                                                internalHosts.addAll( aHandler.getInternalHosts());
//                                        }
//                                    }
//                                }                                
                                
                                //Subtract all internal hosts and the server
                                retCount = hostIdList.size();
                                
                            } catch (SocketException ex) {
                                Log.log(Level.INFO, NAME_Class, "evaluate()", ex.getMessage(), ex );
                            }
                            
                        } else if( optionalId != 0 ){
                            
                            //Get the host
                            HostController aHostController = theGuiController.getHostController( Integer.toString( optionalId ));
                            Host aHost = aHostController.getHost();
                            retCount = aHost.getConnectedHostIdList().size();
//                            SocketChannelHandler aHandler = aSPR.getSocketChannelHandler( optionalId );
//                            if( aHandler != null )
//                                retCount = aHandler.getInternalHosts().size();
                            
                        }

                    }
                    break;
                case NIC_COUNT:
                    if( theTaskManager instanceof MainGuiController ){

                        String hostIdStr = Integer.toString( optionalId );
                        if( optionalId == -1 )
                            hostIdStr  = ServerConfig.getServerConfig().getHostId();                            
                            
                        //Get the host controller 
                        MainGuiController theGuiController = (MainGuiController)theTaskManager;
                        HostController theHostController = theGuiController.getHostController( hostIdStr );
                        if( theHostController != null ){
                            Host theHost = theHostController.getObject();
                            retCount = theHost.getNicMap().size();
                        }       

                    }
                    break;
                
                default:
                    Log.log(Level.WARNING, NAME_Class, "evaluate()", "Unknown count id type.", null );    
                    return;                    
            }
            
            try {
                CountReply aHostMsg = new CountReply( getSrcHostId(), retCount, countType, optionalId);
                aCMManager.send(aHostMsg);
            } catch (UnsupportedEncodingException ex) {
                Log.log(Level.WARNING, NAME_Class, "evaluate()", ex.getMessage(), ex );                                
            }  
                        
        }        
    }

}
