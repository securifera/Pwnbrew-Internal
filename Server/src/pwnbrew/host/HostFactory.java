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
* HostFactory.java
*
* Created on Oct 21, 2013, 9:31:10 PM
*/

package pwnbrew.host;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;
import pwnbrew.log.LoggableException;
import pwnbrew.network.Nic;
import pwnbrew.utilities.Utilities;
import pwnbrew.xml.ServerConfig;

/**
 *
 */
public class HostFactory {
    
    private static Host theLocalHost = null;
    public static final String LOCALHOST = "localhost";
    public static final String LOCALHOST_MAC = "000000000000";

    //=======================================================
    /**
     *  Return a Host representing the local host.
     * 
     * @return 
     * @throws pwnbrew.log.LoggableException 
     */
    public static Host getLocalHost() throws LoggableException {
       
        if( theLocalHost == null ){
            
            //Get the host id
            int clientId = Integer.parseInt( ServerConfig.getServerConfig().getHostId() );
             
            theLocalHost = new Host(clientId);
            
            //Set the hostname
            theLocalHost.setName(LOCALHOST);
            
            //Add the OS Name
            theLocalHost.setOsName( Utilities.getOsName() );   
        
            //Add the Java Arch
            theLocalHost.setJvmArch( Utilities.getOsArch() );
            
            //Add the NICs
            try {
                Enumeration<NetworkInterface> anEnum = NetworkInterface.getNetworkInterfaces();
                while( anEnum.hasMoreElements() ){

                    //Add the option
                    NetworkInterface theInterface = anEnum.nextElement();

                    if( !theInterface.isLoopback() && !theInterface.isVirtual() && theInterface.isUp()){
                        List<InterfaceAddress> iaList = theInterface.getInterfaceAddresses(); //Get a list of the InterfaceAddresses
                        for( InterfaceAddress anIN : iaList ) { //If at least one InterfaceAddress was obtained...
                            InetAddress anAddr = anIN.getAddress();
                            if( anAddr instanceof Inet4Address ){

                                byte[] macAddressArr = theInterface.getHardwareAddress();

                                //Convert the mac to a string a add it to the message
                                String hexString = Utilities.convertHexBytesToString(macAddressArr);
                                if(hexString == null)
                                    hexString = "000000000000";

                                //Add the IP
                                String inetStr = anAddr.getHostAddress();

                                //Add the subnet number
                                int subnet = anIN.getNetworkPrefixLength();
                                String subnetMask = Utilities.get( subnet );

                                Nic aNic = new Nic( hexString, inetStr, subnetMask );
                                //Add the NIC
                                theLocalHost.addNicPair(hexString, aNic);

                                break;
                            }
                        }

                    }

                }
            } catch( SocketException ex ){
                
            }
            
            theLocalHost.setConnected(true);

        }
       
        return theLocalHost;
    
    }

    //=======================================================
    /**
     * 
     * @param aHost 
     */
    public static void setLocalHost(Host aHost) {
        theLocalHost = aHost;
    }
 
}
