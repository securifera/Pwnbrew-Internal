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
package pwnbrew.xml.maltego.custom;

import pwnbrew.StubConfig;
import pwnbrew.misc.Constants;
import pwnbrew.xml.maltego.Entity;
import pwnbrew.xml.maltego.Field;

/**
 *
 * @author Securifera
 */
public class Host extends Entity {

    public static final String PWNBREW_HOST_CONNECTED = "pwnbrew.host.connected";
    public static final String PWNBREW_HOST_SLEEPABLE = "pwnbrew.host.sleepable";
    public static final String PWNBREW_HOST_DISCONNECTED = "pwnbrew.host.disconnected";
    
    
    //=========================================================================
    /**
     * Constructor
     * 
     * @param connected
     * @param sleepable
     * @param relayPort
     * @param passedHostname
     * @param passedArch 
     * @param passedPid 
     * @param passedOS 
     * @param passedId 
     * @param passedBeaconInterval 
     */
    public Host( boolean connected, boolean sleepable, int relayPort, String passedHostname, String passedArch, String passedPid, String passedOS, String passedId, String passedBeaconInterval ) {
        super( connected ? ( sleepable ? PWNBREW_HOST_SLEEPABLE : PWNBREW_HOST_CONNECTED ): PWNBREW_HOST_DISCONNECTED );
        
        //Set the hostname
        setDisplayValue(passedHostname);
        
        //Create the defaul fields for a host
        StubConfig theConfig = StubConfig.getConfig();
        
        //Add the server ip
        Field aField = new Field( Constants.SERVER_IP );
        aField.setXmlObjectContent( theConfig.getServerIp() );
        addField(aField);
        
        //Add the server port
        aField = new Field( Constants.SERVER_PORT );
        aField.setXmlObjectContent( Integer.toString( theConfig.getSocketPort()) );
        addField(aField);
        
        //Add the server ip
        aField = new Field( Constants.HOST_ARCH );
        aField.setXmlObjectContent( passedArch );
        addField(aField);
        
        //Add the host pid
        aField = new Field( Constants.HOST_PID );
        aField.setXmlObjectContent( passedPid );
        addField(aField);
        
        //Add the host os
        aField = new Field( Constants.HOST_OS );
        aField.setXmlObjectContent( passedOS );
        addField(aField);
        
        //Add the host id
        aField = new Field( Constants.HOST_ID );
        aField.setXmlObjectContent( passedId );
        addField(aField);
        
        //Add the beacon interval
        aField = new Field( Constants.BEACON_INTERVAL );
        aField.setXmlObjectContent( passedBeaconInterval );
        addField(aField);
        
        //Add the relaying
        if( relayPort != -1 ){
            aField = new Field( Constants.RELAY_PORT );
            aField.setDisplayName( "Relay Port" );
            aField.setXmlObjectContent( Integer.toString(relayPort) );
            addField(aField);
        }
        
    }    
     
    @Override
    public String toString(){
        return getDisplayValue();
    }
    
}
