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
* PortManager.java
*
* Created on June 7, 2013, 11:49:21 PM
*/

package pwnbrew.manager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import pwnbrew.network.PortRouter;
import pwnbrew.selector.SocketChannelHandler;

/**
 *
 *  
 */
public abstract class PortManager {

    //The map that relates the port to the port router
    private final Map<Integer, PortRouter> thePortRouterMap = new HashMap<>();
    private final Map<Integer, Integer> theClientParentMap = new HashMap<>();
    
    abstract public void socketClosed( SocketChannelHandler thePortRouter );

    //===========================================================================
    /*
     *  Returns the port router operating on the passed port
     * 
     */
    public PortRouter getPortRouter( int operatingPort ) {
        
        PortRouter thePR;
        synchronized( thePortRouterMap ){
            thePR = thePortRouterMap.get( operatingPort );
        }
        return thePR;
    }
    
    //===============================================================
    /**
     * Handles the shutdown tasks for the thread
     *
    */
    public void shutdown() {
        
        //Shutdown each port router
        synchronized( thePortRouterMap ){
            for( Iterator<PortRouter> anIter = thePortRouterMap.values().iterator(); anIter.hasNext();  ){
                anIter.next().shutdown();
            }
        }
    }   

    //===========================================================================
    /**
     *  Sets the port router for the given port.
     * @param passedPort
     * @param aPR 
     */
    void setPortRouter(int passedPort, PortRouter aPR) {
        synchronized( thePortRouterMap ){
            thePortRouterMap.put( passedPort, aPR );
        }
    }

    //===========================================================================
    /**
     * 
     * @param passedClientId
     * @param passedParentId 
     */
    public void setClientParent(int passedClientId, int passedParentId) {        
        synchronized( theClientParentMap ){
            theClientParentMap.put(passedClientId, passedParentId);
        }
    }
    
     //===========================================================================
    /**
     * 
     * @param passedClientId 
     * @return  
     */
    public Integer getClientParent(int passedClientId) {        
        synchronized( theClientParentMap ){
            return theClientParentMap.get(passedClientId);
        }
    }

}
