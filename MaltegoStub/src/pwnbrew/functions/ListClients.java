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
package pwnbrew.functions;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import pwnbrew.MaltegoStub;
import pwnbrew.StubConfig;
import pwnbrew.log.LoggableException;
import pwnbrew.manager.DataManager;
import pwnbrew.misc.Constants;
import pwnbrew.misc.CountSeeker;
import pwnbrew.misc.DebugPrinter;
import pwnbrew.misc.HostHandler;
import pwnbrew.utilities.SocketUtilities;
import pwnbrew.network.ClientPortRouter;
import pwnbrew.network.control.ControlMessageManager;
import pwnbrew.network.control.messages.ControlMessage;
import pwnbrew.network.control.messages.GetCount;
import pwnbrew.xml.maltego.Entities;
import pwnbrew.xml.maltego.MaltegoTransformExceptionMessage;
import pwnbrew.xml.maltego.MaltegoTransformResponseMessage;
import pwnbrew.xml.maltego.custom.Host;

/**
 *
 * @author Securifera
 */
public class ListClients extends Function implements HostHandler, CountSeeker{
    
    private static final String NAME_Class = MaltegoStub.class.getSimpleName();
    private volatile int theClientCount = 0;   
    private final Map<String,Host> hostMap = new HashMap<>();
    
    //==================================================================
    /**
     * Constructor
     * @param passedManager
     */
    public ListClients( MaltegoStub passedManager ) {
        super(passedManager);
    }      
    
    //===================================================================
    /**
     * 
     * @param passedObjectStr 
     */
    @Override
    public void run(String passedObjectStr) {
        
        Map<String, String> objectMap = getKeyValueMap(passedObjectStr); 
         
        //Get server IP
        String serverIp = objectMap.get( Constants.SERVER_IP);
        if( serverIp == null ){
            DebugPrinter.printMessage( NAME_Class, "listclients", "No pwnbrew server IP provided", null);
            return;
        }
         
        //Get server port
        String serverPortStr = objectMap.get( Constants.SERVER_PORT);
        if( serverPortStr == null ){
            DebugPrinter.printMessage( NAME_Class, "listclients", "No pwnbrew server port provided", null);
            return;
        }
        
         //Get host id
        String hostIdStr = objectMap.get( Constants.HOST_ID);
        if( hostIdStr == null ){
            DebugPrinter.printMessage( NAME_Class, "listclients", "No host id provided", null);
            return;
        }
         
        StubConfig theConfig = StubConfig.getConfig();
        theConfig.setServerIp(serverIp);
        theConfig.setSocketPort(serverPortStr);
        Integer anInteger = SocketUtilities.getNextId();
        theConfig.setHostId(anInteger.toString());
        int serverPort = Integer.parseInt( serverPortStr);
        ClientPortRouter aPR = (ClientPortRouter) theManager.getPortRouter( serverPort );
        if(aPR == null){
            try {
                aPR = (ClientPortRouter)DataManager.createPortRouter(theManager, serverPort, true);
            } catch (IOException ex) {
                DebugPrinter.printMessage( NAME_Class, "listclients", "Unable to create port router.", ex);
                return;
            }
        }
        theManager.initialize();
        try {
            
            aPR.ensureConnectivity( serverPort, theManager );
            
            //Get the client count
            ControlMessage aMsg = new GetCount( Constants.SERVER_ID, GetCount.HOST_COUNT, hostIdStr );
            DataManager.send( theManager, aMsg);           
            
            //Wait for the response
            waitToBeNotified( 180 * 1000);
            
            //Get the client info
            if( theClientCount > 0 ){
                
                //Get each client msg
                aMsg = new pwnbrew.network.control.messages.GetHosts( Constants.SERVER_ID, hostIdStr );
                DataManager.send( theManager, aMsg);
                
                //Wait for the response
                waitToBeNotified( 180 * 1000);
                
                //Add hosts to entity list
                addEntities();
                
            }
            
        } catch( LoggableException ex ) {
            
            //Create a relay object
            pwnbrew.xml.maltego.Exception exMsg = new pwnbrew.xml.maltego.Exception( ex.getMessage() );
            MaltegoTransformExceptionMessage malMsg = theReturnMsg.getExceptionMessage();
            
            //Create the message list
            malMsg.getExceptionMessages().addExceptionMessage(exMsg);
        }
        
        //Create the return message
        theReturnMsg.getXml();
        
    }
   
    //===============================================================
    /**
     * 
     * @param countType
     * @param objId
     */
    @Override
    public synchronized void setCount(int passedCount, int countType, int objId ) {
        theClientCount = passedCount;
        beNotified();
    }
    
    public void addEntities(){
        
        synchronized( hostMap ){
            for( Host aHost : hostMap.values() ){
                MaltegoTransformResponseMessage rspMsg = theReturnMsg.getResponseMessage();
                Entities theEntities = rspMsg.getEntityList();
                theEntities.addEntity(aHost);
            }
        }
        
    }

    //===============================================================
    /**
     * 
     * @param aHost 
     */
    @Override
    public void addHost(Host aHost) {
        
        String hostname = aHost.getDisplayValue();         
        synchronized( hostMap ){
            Host prevHost = hostMap.remove(hostname);
            if( prevHost != null ){ 
                if(prevHost.getType().equals( Host.PWNBREW_HOST_DISCONNECTED)) {
                    hostMap.put(hostname, aHost);
                } else {
                    hostMap.put(hostname, prevHost);
                }
            } else {
                hostMap.put(hostname, aHost);
            }
        }
        
        //Decrement and see if we are done
        theClientCount--;
        if( theClientCount == 0)
            beNotified();
        
    }


}
