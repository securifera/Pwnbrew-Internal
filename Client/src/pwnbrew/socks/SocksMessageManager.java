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
 *  Created on July 27, 2013
 */

package pwnbrew.socks;

import pwnbrew.network.PortRouter;
import java.util.HashMap;
import java.util.Map;
import pwnbrew.Pwnbrew;
import pwnbrew.manager.PortManager;
import pwnbrew.manager.DataManager;

/**
 *
 *  
 */
public class SocksMessageManager extends DataManager {

    private static SocksMessageManager theSocksMessageManager;
    private static final String NAME_Class = SocksMessageManager.class.getSimpleName();
    
    private final Map<Integer, SockHandlerStruct> hostIdToSockHandlerStructMap = new HashMap<>();
    
    
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
     *   Gets the SocksMessageManager
     * @return 
     */
    public synchronized static SocksMessageManager getSocksMessageManager(){
        if( theSocksMessageManager == null )
            theSocksMessageManager = new SocksMessageManager( Pwnbrew.getPwnbrewInstance() );
        return theSocksMessageManager;
    }
    
    //===============================================================
    /**
     *   Send the message out the given channel.
     *
     * @param msgBytes
    */
    @Override
    public void handleMessage( PortRouter srcPortRouter, byte[] msgBytes ) {        
        theSocksMessageManager.getDataHandler().processData(srcPortRouter, msgBytes);        
    }
    
    //===========================================================================
    /*
     *  Returns the data handler
    */
    @Override
    public SocksMessageHandler getDataHandler() {
        return (SocksMessageHandler)theDataHandler;
    }   

    //===========================================================================
    /**
     * 
     * @param theSrcHostId
     * @param theHandlerId 
     * @param connectStr 
     * @return  
     */
    public boolean createHandler(int theSrcHostId, int theHandlerId, String connectStr ) {
        
        boolean retVal = false;
        SockHandlerStruct aSHS;
        synchronized(hostIdToSockHandlerStructMap){
            aSHS = hostIdToSockHandlerStructMap.get(theSrcHostId);
        }
        
        if( aSHS != null ){
            
            SocksHandler aHandler = aSHS.getSockHandler(theHandlerId);
            if( aHandler != null )
                return retVal;

            //Add the Handler & start it
            aHandler = new SocksHandler( this, theSrcHostId, aSHS.getChannelId(), theHandlerId, connectStr );
            aHandler.start();
            aSHS.setSockHandler(theHandlerId, aHandler);                
            retVal = true;
            
        }
        return retVal;
    }
    
    //========================================================================
    /**
     * 
     * @param passedHostId
     * @param passedHandlerId
     * @return 
     */
    public SocksHandler getSocksHandler( int passedHostId, int passedHandlerId ){
        
        SocksHandler aHandler = null;
        SockHandlerStruct aSHS;
        synchronized(hostIdToSockHandlerStructMap){
            aSHS = hostIdToSockHandlerStructMap.get(passedHostId);
        }
        
        //Get the socks handler
        if( aSHS != null )
            aHandler = aSHS.getSockHandler( passedHandlerId);
                
        return aHandler;
    }

    //========================================================================
    /**
     * 
     * @param theHostId
     * @param theChannelId 
     */
    public void setChannelId(int theHostId, int theChannelId) {
        SockHandlerStruct aSHS = new SockHandlerStruct(theChannelId);
        synchronized(hostIdToSockHandlerStructMap){            
            hostIdToSockHandlerStructMap.put(theHostId, aSHS);            
        }
    }

    //========================================================================
    /**
     * 
     * @param srcHostId 
     */
    public void stopSocksHandlers(int srcHostId) {
        SockHandlerStruct aSHS;
        synchronized(hostIdToSockHandlerStructMap){
            aSHS = hostIdToSockHandlerStructMap.remove(srcHostId);
        }
        
        //Shutdown the handlers and remove
        if( aSHS != null ){
            aSHS.shutdown();
        }
    }
    
    //===========================================================================
    /**
     *  Shutdown the handler 
     */
    @Override
    public void shutdown() {
        super.shutdown();
        synchronized(hostIdToSockHandlerStructMap){
            for( SockHandlerStruct aSHS : hostIdToSockHandlerStructMap.values()){
                aSHS.shutdown();
            }
        }
    }

    //========================================================================
    /**
     * 
     * @param srcHostId
     * @param theHandlerId 
     */
    public void stopSocksHandler(int srcHostId, int theHandlerId) {
        SockHandlerStruct aSHS;
        synchronized(hostIdToSockHandlerStructMap){
            aSHS = hostIdToSockHandlerStructMap.get(srcHostId);
        }
        
        //Get the sock handler struct
        if( aSHS != null ){
            //Remove the handler and shut it down
            SocksHandler aSH = aSHS.removeSockHandler(theHandlerId);
            if( aSH != null )
                aSH.close();
        }
    }
    
    class SockHandlerStruct {
        
        private int channelId;
        private final Map<Integer, SocksHandler> handlerIdSockHandlerMap = new HashMap<>();

        //=====================================================================
        /**
         * 
         * @param passedChannelId 
         */
        private SockHandlerStruct(int passedChannelId) {
            channelId = passedChannelId;
        }
                
        //=====================================================================
        /**
         * 
         * @param passedChannelId 
         */
        public int getChannelId(){
            return channelId;
        }
        
        //=====================================================================
        /**
         * 
         * @param passedHandlerId
         * @param aSH 
         */
        public void setSockHandler( int passedHandlerId, SocksHandler aSH ){
            synchronized( handlerIdSockHandlerMap){
                handlerIdSockHandlerMap.put(passedHandlerId, aSH);
            }
        }
        
        //=====================================================================
        /**
         * 
         * @param passedHandlerId
         * @param aSH 
         */
        public SocksHandler getSockHandler( int passedHandlerId ){
            SocksHandler aSH;
            synchronized( handlerIdSockHandlerMap){
                aSH = handlerIdSockHandlerMap.get(passedHandlerId);
            }
            return aSH;
        }
        
        //=====================================================================
        /**
         * 
         */
        public void shutdown(){
            synchronized( handlerIdSockHandlerMap){
                for( SocksHandler aHandler : handlerIdSockHandlerMap.values()){
                    aHandler.close();
                }
            }
        }

        //=====================================================================
        /**
         * 
         * @param theHandlerId
         * @return 
         */
        private SocksHandler removeSockHandler(int theHandlerId) {
            SocksHandler aSH;
            synchronized( handlerIdSockHandlerMap){
                aSH = handlerIdSockHandlerMap.remove(theHandlerId);
            }
            return aSH;
        }
        
    }
  
}
