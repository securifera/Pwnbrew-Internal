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

import java.awt.Insets;
import java.io.IOException;
import java.util.Map;
import javax.swing.JOptionPane;
import pwnbrew.MaltegoStub;
import pwnbrew.StubConfig;
import pwnbrew.generic.gui.ValidTextField;
import pwnbrew.misc.Constants;
import pwnbrew.misc.DebugPrinter;
import pwnbrew.misc.SocketUtilities;
import pwnbrew.misc.StandardValidation;
import pwnbrew.network.ClientPortRouter;
import pwnbrew.network.control.ControlMessageManager;
import pwnbrew.network.control.messages.RelayStartRelay;
import pwnbrew.network.control.messages.RelayStopRelay;
import pwnbrew.xml.maltego.Entities;
import pwnbrew.xml.maltego.MaltegoMessage;
import pwnbrew.xml.maltego.MaltegoTransformExceptionMessage;
import pwnbrew.xml.maltego.MaltegoTransformResponseMessage;
import pwnbrew.xml.maltego.custom.Relay;

/**
 *
 * @author Securifera
 */
public class StopRelay extends Function {
    
    private static final String NAME_Class = StopRelay.class.getSimpleName();
    
    private volatile boolean notified = false;
    private volatile boolean isConnected = false;
    
    //Create the return msg
    private MaltegoMessage theReturnMsg = new MaltegoMessage();
  
    //==================================================================
    /**
     * Constructor
     * @param passedManager
     */
    public StopRelay( MaltegoStub passedManager ) {
        super(passedManager);
    }      
    
    //===================================================================
    /**
     * 
     * @param passedObjectStr
     * @return 
     */
    @Override
    public String run(String passedObjectStr) {
        
        String retStr = "";
        Map<String, String> objectMap = getKeyValueMap(passedObjectStr); 
         
        //Get server IP
        String serverIp = objectMap.get( Constants.SERVER_IP);
        if( serverIp == null ){
            DebugPrinter.printMessage( NAME_Class, "run", "No pwnbrew server IP provided", null);
            return retStr;
        }
         
        //Get server port
        String serverPortStr = objectMap.get( Constants.SERVER_PORT);
        if( serverPortStr == null ){
            DebugPrinter.printMessage( NAME_Class, "run", "No pwnbrew server port provided", null);
            return retStr;
        }
        
        //Get host id
        String hostIdStr = objectMap.get( Constants.HOST_ID);
        if( hostIdStr == null ){
            DebugPrinter.printMessage( NAME_Class, "run", "No host id provided", null);
            return retStr;
        }
        
        //Create the connection
        try {        
            
            //Set the server ip and port
            StubConfig theConfig = StubConfig.getConfig();
            theConfig.setServerIp(serverIp);
            theConfig.setSocketPort(serverPortStr);

            //Set the client id
            Integer anInteger = SocketUtilities.getNextId();
            theConfig.setHostId(anInteger.toString());            
                    
            ControlMessageManager aCMManager = ControlMessageManager.getControlMessageManager();
            if( aCMManager == null ){
                aCMManager = ControlMessageManager.initialize( theManager );
            }

            //Get the port router
            int serverPort = Integer.parseInt( serverPortStr);
            ClientPortRouter aPR = (ClientPortRouter) theManager.getPortRouter( serverPort );

            //Initiate the file transfer
            if(aPR == null){
                DebugPrinter.printMessage( NAME_Class, "listclients", "Unable to retrieve port router.", null);
                return retStr;     
            }           

            //Set up the port wrapper
            theManager.initialize();

            //Connect to server
            boolean connected = aPR.ensureConnectivity( serverPort, theManager );
            if( connected ){

                //Get the client count
                int hostId = Integer.parseInt( hostIdStr);
                RelayStopRelay aMsg = new RelayStopRelay( Constants.SERVER_ID, hostId );               
                aCMManager.send(aMsg );  

                //Wait for the response
                waitToBeNotified( 180 * 1000);

                //If connected create a relay
                if( !isConnected ){

                    //Create a relay object
                    pwnbrew.xml.maltego.Exception exMsg = new pwnbrew.xml.maltego.Exception( "Relay stopped. Please remove the relay and any hosts connected to it.");
                    MaltegoTransformExceptionMessage malMsg = theReturnMsg.getExceptionMessage();

                    //Create the message list
                    malMsg.getExceptionMessages().addExceptionMessage(exMsg);      

                } 

                try {
                    //Sleep for a few seconds
                    Thread.sleep(3000);
                } catch (InterruptedException ex) {
                }

            } else {
                StringBuilder aSB = new StringBuilder()
                        .append("Unable to connect to the Pwnbrew server at \"")
                        .append(serverIp).append(":").append(serverPort).append("\"");
                DebugPrinter.printMessage( NAME_Class, "listclients", aSB.toString(), null);
            }

                
            
            
            //Create the return message
            retStr = theReturnMsg.getXml();
        } catch (IOException ex) {
            DebugPrinter.printMessage( NAME_Class, "listclients", ex.getMessage(), ex );
        }
        
        return retStr;
    }
    
    // ==========================================================================
    /**
    * Causes the calling {@link Thread} to <tt>wait()</tt> until notified by
    * another.
    * <p>
    * <strong>This method most certainly "blocks".</strong>
     * @param anInt
    */
    protected synchronized void waitToBeNotified( Integer... anInt ) {

        while( !notified ) {

            try {
                
                //Add a timeout if necessary
                if( anInt.length > 0 ){
                    
                    wait( anInt[0]);
                    break;
                    
                } else {
                    wait(); //Wait here until notified
                }
                
            } catch( InterruptedException ex ) {
            }

        }
        notified = false;
    }
    
    //===============================================================
    /**
     * Notifies the thread
    */
    protected synchronized void beNotified() {
        notified = true;
        notifyAll();
    }   

    //===============================================================
    /**
     * 
     * @param connected 
     */
    public void setStatus(boolean connected) {
        isConnected = connected;
        beNotified();
    }


}
