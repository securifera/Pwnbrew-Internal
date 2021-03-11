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

package pwnbrew.network;

import java.io.IOException;
import pwnbrew.utilities.ManagedRunnable;
import pwnbrew.utilities.Constants;
import java.util.concurrent.TimeoutException;
import pwnbrew.network.socket.SocketChannelWrapper;
import pwnbrew.selector.SocketChannelHandler;


/**
 *
 * @author user
 */
public class SocketTimeoutThread extends ManagedRunnable {
    
    private SocketChannelWrapper theSCW = null;
    
    //Static instance
    private static final String NAME_Class = SocketTimeoutThread.class.getSimpleName();
           

    // ==========================================================================
    /**
     * Constructor
     *
     * @param aSCW
    */
    @SuppressWarnings("ucd")
    public SocketTimeoutThread( SocketChannelWrapper aSCW ) {
        super(Constants.Executor);
        theSCW = aSCW;
    }
    
    // ==========================================================================
    /**
     *  Main thread loop
     *
    */
    @Override
    public void go() {
                 
        // Ten seconds
        int sleepTime = 10000;
        //Loop while connected
        while( !shutdownRequested ){

            try {
                waitToBeNotified(sleepTime);
            } catch (TimeoutException ex) {
            }

            //Close and exit loop
            if( theSCW.getBytesReadFlag() == false ){
                try {
                    theSCW.close();
                } catch (IOException ex) {
                }
                
                //Get the socket channel handler
                SocketChannelHandler aSCH = theSCW.getSocketChannelHandler();
                if( aSCH != null){
                    aSCH.disconnect();
                    aSCH.getPortRouter().socketClosed( aSCH.getHostId(), aSCH.getChannelId());
                }
                          
                
                break;
            }
            
            //Reset the flag
            theSCW.setBytesReadFlag(false);

        }
        
    }
     
    //===============================================================
    /**
    *  Shut down the detector
    */
    @Override
    public void shutdown(){        
        super.shutdown();      
    }
}
    
    
    

