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
 *  SocksMessageManager.java
 *
 */

package pwnbrew.socks;

import pwnbrew.network.PortRouter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import pwnbrew.MaltegoStub;
import pwnbrew.StubConfig;
import pwnbrew.functions.Function;
import pwnbrew.functions.ToSocks;
import pwnbrew.manager.PortManager;
import pwnbrew.manager.DataManager;

/**
 *
 *  
 */
public class SocksMessageManager extends DataManager {

    private static SocksMessageManager theSocksMessageManager;
    private static final String NAME_Class = SocksMessageManager.class.getSimpleName();
    
    private SocksServer theSocksServer = null;
    
    
    //===========================================================================
    /*
     *  Constructor
     */
    private SocksMessageManager( PortManager passedCommManager ) {
        
        super(passedCommManager);        

        //Create the handler
        SocksMessageHandler theMessageHandler = new SocksMessageHandler( this );
        theMessageHandler.start();
       
        //Set the data handler
        setDataHandler(theMessageHandler);
    }  
    
    // ==========================================================================
    /**
     *   Creates a ShellMessageManager
     * @param passedCommManager
     * @return 
     * @throws java.io.IOException 
     */
    public synchronized static SocksMessageManager initialize( PortManager passedCommManager ) throws IOException {

        if( theSocksMessageManager == null ) {
            theSocksMessageManager = new SocksMessageManager( passedCommManager );
            createPortRouter( passedCommManager, StubConfig.getConfig().getSocketPort(), true );
        }
        
        return theSocksMessageManager;

    }/* END initialize() */
    
    // ==========================================================================
    /**
     *   Gets the ShellMessageManager
     * @return 
     */
    public synchronized static SocksMessageManager getSocksMessageManager(){
        return theSocksMessageManager;
    }
    
    //===============================================================
    /**
     *   Send the message out the given channel.
     *
     * @param msgBytes
    */
    @Override
    public void handleMessage( byte[] msgBytes ) {        
        theSocksMessageManager.getDataHandler().processData(msgBytes);        
    }
    
    //===============================================================
    /**
     *   Send the message out the given channel.
     *
     * @param passedMessage
    */
    public void send( SocksMessage passedMessage ) {

        
        ByteBuffer aByteBuffer;
        int msgLen = passedMessage.getLength();
        aByteBuffer = ByteBuffer.allocate( msgLen );
        passedMessage.append(aByteBuffer);

        
        //Get the port router
        PortRouter thePR = thePortManager.getPortRouter(  StubConfig.getConfig().getSocketPort() );
                
        //Queue the message to be sent
        thePR.queueSend( Arrays.copyOf( aByteBuffer.array(), aByteBuffer.position()), passedMessage.getDestHostId() );
//        DebugPrinter.printMessage(this, "Queueing " + passedMessage.getClass().getSimpleName() + " message");
          
    }
    
    //===========================================================================
    /**
     * 
     * Returns the data handler
     * 
     * @return 
     */
    @Override
    public SocksMessageHandler getDataHandler() {
        return (SocksMessageHandler)theDataHandler;
    }   
    
    //===========================================================================
    /**
     * 
     */
    public void stopSocksServer() {
        //Close server
        if( theSocksServer != null ){
            theSocksServer.shutdown();
            theSocksServer = null;
        }
    }

    //===========================================================================
    /**
     * 
     * @param theChannelId 
     */
    public void startSocksServer(int theChannelId) {
        
        if( thePortManager instanceof MaltegoStub ){

            MaltegoStub maltegoManager = (MaltegoStub)thePortManager;
            Function theFunction = maltegoManager.getFunction();
            if( theFunction instanceof ToSocks ){
                ToSocks toSocksFunc = (ToSocks)theFunction;
                SocksJPanel thePanel = (SocksJPanel) toSocksFunc.getParentComponent();
                //Start the timer
                thePanel.startTimer();
                
                //Get the port
                String portStr = thePanel.getPortStr();
                int listenPort = Integer.parseInt(portStr);  
                
                //Host id
                int hostId = toSocksFunc.getHostId();
                                
                //Create the socks server and start it
                theSocksServer = new SocksServer( listenPort, hostId, theChannelId);
                theSocksServer.start();
            }
        }

    }

//    //===========================================================================
//    /**
//     * 
//     * @param theHandlerId 
//     * @param creationFlag 
//     */
//    public void startSocksHandler( int theHandlerId, boolean creationFlag ) {
//        
//        //Star the handler
//        if( theSocksServer != null ){
//            theSocksServer.startSocksHandler(theHandlerId);
//        }
//    }
    
    //==========================================================================
    /**
     * 
     * @return 
     */
    public SocksServer getSocksServer(){
       return theSocksServer; 
    }

    //==========================================================================
    /**
     * 
     * @param theHandlerId
     * @param creationFlag 
     */
    public void notifySocksHandler(int theHandlerId, boolean creationFlag) {
        //Star the handler
        if( theSocksServer != null ){
            SocksHandler theSH = theSocksServer.getSocksHandler(theHandlerId);
            theSH.setConnected(creationFlag);
            theSH.beNotified();
        }
    }
   
}
