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
package pwnbrew.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import pwnbrew.utilities.ReconnectTimer;
import pwnbrew.network.KeepAliveTimer;
import pwnbrew.network.shell.Shell;
import pwnbrew.selector.SocketChannelHandler;
import pwnbrew.utilities.SocketUtilities;

/**
 *
 * @author Securifera
 */
public class OutgoingConnectionManager extends ConnectionManager {
    
    
    private final Map<Integer, SocketChannelHandler> channelIdHandlerMap = new HashMap<>();
    private final Map<Integer, KeepAliveTimer> theKeepAliveTimerMap = new HashMap<>();
    private final Map<Integer, ReconnectTimer> theReconnectTimerMap = new HashMap<>();
    private final Map<Integer, Shell> theShellMap = new HashMap<>();
    
    //==========================================================================
    /**
     * 
     */
    public OutgoingConnectionManager() {        
    }
    
     //===========================================================================
    /*
     *  Set the shell
     */
    public void setShell( int channelId, Shell passedShell ) {
        //Kill the previous shell
        synchronized( theShellMap ){
            Shell theShell = theShellMap.get(channelId);
            if( theShell != null )
                theShell.shutdown();

            theShellMap.put(channelId, passedShell);
        }
    }
    
     //===========================================================================
    /*
     *  Set the shell
     */
    public void removeShell( int channelId ) {
        //Kill the previous shell
        Shell theShell;
        synchronized( theShellMap ){
            theShell = theShellMap.remove(channelId);
            if( theShell != null )
                theShell.shutdown();
        }
    }
    
    //===========================================================================
    /*
     *  Return the shell
     */
    public Shell getShell( int channelId ) {
        Shell theShell;
        synchronized( theShellMap ){
            theShell = theShellMap.get(channelId);
        }
        return theShell;
    } 

    //===============================================================
    /**
     * Sets the access handler for the server
     *
     * @param passedId
     * @param theAccessHandler
     * @return 
     */
    @Override
    public boolean setHandler( int passedId, SocketChannelHandler theAccessHandler ) {
        synchronized( channelIdHandlerMap){
            channelIdHandlerMap.put( passedId, theAccessHandler);
        }
        return true;
    }
    
     //===============================================================
    /**
     *  Removes the client id
     * 
     * @param passedId 
     * @return  
    */
    @Override
    public SocketChannelHandler removeHandler( int passedId ) {
        SocketChannelHandler aSCH;
        synchronized( channelIdHandlerMap ){
            aSCH = channelIdHandlerMap.remove( passedId );
        }
        return aSCH;
    }
 
    //==========================================================================
    /**
     *  Returns the keep alive timer
     * @param passedId
     * @return 
     */
    public KeepAliveTimer getKeepAliveTimer( int passedId ) {
        
        KeepAliveTimer theTimer;
        synchronized(theKeepAliveTimerMap){
            theTimer = theKeepAliveTimerMap.get(passedId);
        }
        return theTimer;
    }
    
    //==========================================================================
    /**
     *  Returns the reconnect timer
     * @param passedId
     * @return 
     */
    public ReconnectTimer getReconnectTimer( int passedId ) {
        
        ReconnectTimer theTimer;
        synchronized(theReconnectTimerMap){
            theTimer = theReconnectTimerMap.get(passedId);
        }
        return theTimer;
    }
    
     //==========================================================================
    /**
     *  Returns the reconnect timer
     * @param passedId
     * @param passedTimer 
     */
    public void setReconnectTimer( int passedId, ReconnectTimer passedTimer ) {
        
        synchronized(theReconnectTimerMap){
            theReconnectTimerMap.put(passedId, passedTimer);
        }
    }
    
    //===============================================================
    /**
     *  Returns the SocketChannelHandler for the server.
     * 
     * @param passedId
     * @return 
    */  
    @Override
    public SocketChannelHandler getSocketChannelHandler( Integer passedId ){
        SocketChannelHandler aSCH;
        synchronized( channelIdHandlerMap){
            aSCH = channelIdHandlerMap.get( passedId );
        }
        return aSCH;
    }
    
    //================================================================
    /**
     * 
     */
    @Override
    public void shutdown() {
        
         //Shutdown the handlers and there keepalives
        Set<Integer> theKeys = theKeepAliveTimerMap.keySet();
        for( Integer aKey : theKeys )
            theKeepAliveTimerMap.get(aKey).shutdown();
        
        theKeys = channelIdHandlerMap.keySet();
        for( Integer aKey : theKeys )
            channelIdHandlerMap.get(aKey).shutdown();
        
        //Shutdown the shells any exists
        theKeys = theShellMap.keySet();
        for( Integer aKey : theKeys )
            theShellMap.get(aKey).shutdown();
    }

     //===============================================================
    /**
    * Returns an integer to be used for packet ids
    *
    * @return
    */
    public int getNextChannelId(){

        SocketChannelHandler aSH;
        int channelId;
                
        synchronized( channelIdHandlerMap){
            do {
                channelId = SocketUtilities.SecureRandomGen.nextInt();
                aSH = channelIdHandlerMap.get( channelId );
            } while( aSH != null );
        }       

        return channelId;
    }

    //===========================================================
    /**
     * 
     * @param passedId
     */
    public void closeShell( int passedId ) {
        
        //Shut down the shell
        Shell theShell = getShell(passedId);
        if( theShell != null )
            theShell.shutdown();
        
        //Kill the timers
        KeepAliveTimer aKAT = getKeepAliveTimer(passedId);
        if( aKAT != null )
            aKAT.shutdown();
        
        //Kill the timers
        ReconnectTimer aRT = getReconnectTimer(passedId);
        if( aRT != null )
            aRT.shutdown();
        
        //Close the connection
        SocketChannelHandler aSCH = removeHandler(passedId);
        if( aSCH != null )
            aSCH.shutdown();
        
    }

    //=======================================================================
    /**
     * 
     * @param passedId
     * @param theKeepAliveTimer 
     */
    public void setKeepAliveTimer(int passedId, KeepAliveTimer theKeepAliveTimer) {
        synchronized(theKeepAliveTimerMap){
            theKeepAliveTimerMap.put(passedId, theKeepAliveTimer);
        }
    }
    
    
}
