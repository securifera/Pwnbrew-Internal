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
public class Relay extends Entity {

    public static final String PWNBREW_RELAY = "pwnbrew.relay";
    
    
    //=========================================================================
    /**
     * Constructor
     * 
     * @param passedId 
     * @param passedPort 
     */
    public Relay( String passedId, String passedPort ) {
        super( PWNBREW_RELAY );
                
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
        
        //Add the relay port
        aField = new Field( Constants.RELAY_PORT );
        aField.setXmlObjectContent( passedPort );
        addField(aField);
        
        //Add the host id
        aField = new Field( Constants.HOST_ID );
        aField.setXmlObjectContent( passedId );
        addField(aField);
        
    }    
    
}
