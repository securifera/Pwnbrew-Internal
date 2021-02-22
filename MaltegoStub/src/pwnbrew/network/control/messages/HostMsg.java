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
* HostMsg.java
*
*/

package pwnbrew.network.control.messages;

import java.io.UnsupportedEncodingException;
import pwnbrew.MaltegoStub;
import pwnbrew.functions.Function;
import pwnbrew.manager.PortManager;
import pwnbrew.misc.HostHandler;
import pwnbrew.utilities.SocketUtilities;
import pwnbrew.network.ControlOption;
import pwnbrew.xml.maltego.custom.Host;

/**
 *
 *  
 */
public final class HostMsg extends MaltegoMessage{ 
    
    private static final byte OPTION_HOSTNAME = 60;
    private static final byte OPTION_ARCH = 61;
    private static final byte OPTION_OS = 62;
    private static final byte OPTION_CONNECTED = 63;
    private static final byte OPTION_HOST_ID = 64;
    private static final byte OPTION_SLEEPABLE = 65;
    private static final byte OPTION_RELAY_PORT = 67;
    private static final byte OPTION_PID = 68;
    private static final byte OPTION_BEACON_INTERVAL = 69;
    
    private boolean connected = false;
    private boolean sleepable = false;
    private int relayPort = -1;
    private String clientHostname = "";
    private String os_name = "";
    private String java_arch = "";
    private String pid = "";
    private int hostid = 0;
    private int beaconInterval = 0;
    
    public static final short MESSAGE_ID = 0x6d;
    
     //===========================================================================
    /**
     *  Constructor
     * 
     * @param passedId 
     */
    public HostMsg( byte[] passedId ) {
       super(passedId);
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
                case OPTION_HOSTNAME:
                    clientHostname = new String( theValue, "US-ASCII");
                    break;
                case OPTION_ARCH:
                    java_arch = new String(theValue, "US-ASCII");
                    break;
                case OPTION_PID:
                    pid = new String(theValue, "US-ASCII");
                    break;
                case OPTION_OS:
                    os_name = new String(theValue, "US-ASCII");
                    break;
                case OPTION_HOST_ID:
                    hostid = SocketUtilities.byteArrayToInt(theValue);
                    break;
                case OPTION_BEACON_INTERVAL:
                    beaconInterval = SocketUtilities.byteArrayToInt(theValue);
                    break;
                case OPTION_CONNECTED:
                    
                    if( theValue.length > 0 ){
                        byte retByte = theValue[0];
                        if( retByte == 1 )
                            connected = true;
                        
                    }
                    
                    break;
                case OPTION_SLEEPABLE:
                    
                    if( theValue.length > 0 ){
                        byte retByte = theValue[0];
                        if( retByte == 1 )
                            sleepable = true;                        
                    }
                    
                    break;   
                case OPTION_RELAY_PORT:
                    relayPort = SocketUtilities.byteArrayToInt(theValue);
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
    
         if( passedManager instanceof MaltegoStub ){
            MaltegoStub theStub = (MaltegoStub)passedManager;
            Function aFunction = theStub.getFunction();
            if( aFunction instanceof HostHandler ){
                
                HostHandler aHandler = (HostHandler)aFunction;
                
                //Add the host to the list
                Host aHost = new Host( connected, sleepable, relayPort, clientHostname, java_arch, pid, os_name, Integer.toString( hostid), Integer.toString(beaconInterval) );
                aHandler.addHost( aHost );
            }            
        }  
    }
}