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
* Created on Jul 11, 2013, 11:12:32 PM
*/

package pwnbrew.network.control.messages;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import pwnbrew.host.Host;
import pwnbrew.log.Log;
import pwnbrew.manager.ConnectionManager;
import pwnbrew.manager.DataManager;
import pwnbrew.manager.NotificationManager;
import pwnbrew.manager.PortManager;
import pwnbrew.manager.ServerManager;
import pwnbrew.misc.DebugPrinter;
import pwnbrew.network.ControlOption;
import pwnbrew.network.Nic;
import pwnbrew.network.PortRouter;
import pwnbrew.network.control.ControlMessageManager;
import pwnbrew.selector.SocketChannelHandler;
import pwnbrew.utilities.SocketUtilities;

/**
 *
 *  
 */

@SuppressWarnings("ucd")
public final class Hello extends ControlMessage {
    
    private static final byte OPTION_HOSTNAME = 6;
    private static final byte OPTION_OS_NAME = 14;    
    private static final byte OPTION_JAVA_ARCH = 15; 
    private static final byte OPTION_NIC_INFO = 18;
    private static final byte OPTION_JAR_VERSION = 19;    
    private static final byte OPTION_JRE_VERSION = 20;
    private static final byte OPTION_PID = 21;
    private static final byte OPTION_CHANNEL_ID = 102; 
        
    private String clientHostname = "";
    private final List<String> theNicInfoList = new ArrayList();
    private String os_name = "";
    private String jar_version = "";
    private String jre_version = "";
    private String java_arch = "";
    private String pid = "";
    private int theChannelId = 0; 
    
    public static final short MESSAGE_ID = 0x3b;
    
    //Class name
    private static final String NAME_Class = Hello.class.getSimpleName();    

    //===========================================================================
    /**
     *  Constructor
     * 
     * @param passedId 
     */
    public Hello( byte[] passedId ) {
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
                case OPTION_OS_NAME:
                    os_name = new String(theValue, "US-ASCII");
                    break;
                case OPTION_JAR_VERSION:
                    jar_version = new String(theValue, "US-ASCII");
                    break;
                 case OPTION_JRE_VERSION:
                    jre_version = new String(theValue, "US-ASCII");
                    break;
                case OPTION_JAVA_ARCH:
                    java_arch = new String(theValue, "US-ASCII");
                    break;
                case OPTION_PID:
                    pid = new String(theValue, "US-ASCII");
                    break;
                case OPTION_CHANNEL_ID:
                    theChannelId = SocketUtilities.byteArrayToInt(theValue);
                    break;
                case OPTION_NIC_INFO:
                    
                    //Attempt to get the subnet mask
                    String nicStr = new String(theValue, "US-ASCII");
                    theNicInfoList.add(nicStr);
                    
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
     *  Returns the hostname
     * @return 
    */
    public String getHostname() {
        return clientHostname;
    }
    
    //===============================================================
    /**
     *  Returns the os name
     * @return 
    */
    public String getOsName() {
        return os_name;
    }
    
     //===============================================================
    /**
     *  Returns the arch of the JVM
     * @return 
    */
    public String getJavaArch() {
        return java_arch;
    }
    
     //===============================================================
    /**
     *  Returns the pid
     * @return 
    */
    public String getPid() {
        return pid;
    }

    //===============================================================
    /**
     *  Returns the list of Nics
     * @return 
     */
    public List<String> getNicInfoList() {
        return theNicInfoList;
    }
    
    //===============================================================
    /**
    *   Performs the logic specific to the message.
    *
     * @param passedManager
    */
    @Override
    public void evaluate( PortManager passedManager ) {
        
        //Get the address and connect
        try {
            
            //Make sure the host is named localhost or it will screw up the server
            if( clientHostname.equals("localhost")){
                Log.log(Level.INFO, NAME_Class, "evaluate()", "Unable to register host with hostname 'localhost' because it conflicts with server host object.", null );
                return;
            }
            
            ControlMessageManager aCMManager = ControlMessageManager.getMessageManager();
            if( aCMManager != null ){
           
                //Register the host
                Integer theClientId = getSrcHostId();
                Log.log(Level.INFO, NAME_Class, "evaluate()", "Accepted connection from host id: " + theClientId + " channel: " + Integer.toString(theChannelId), null);
                PortRouter aPR = passedManager.getPortRouter( aCMManager.getPort() );
                              
                //Get connection manager
                ConnectionManager aCM = aPR.getConnectionManager(theClientId);
                if( aCM != null ){
                    
                    SocketChannelHandler aSCH = aCM.getSocketChannelHandler( theChannelId );
                    if( aSCH != null ){

                        //Send HostAck
                        HelloAck aHostAck = new HelloAck( theClientId );
                        DataManager.send( passedManager, aHostAck );

                        //Create a host
                        Host aHost = new Host(theClientId);   
                        aHost.setName(clientHostname);                    

                        //Get the list
                        List<String> nicInfoList = getNicInfoList();
                        for( String aStr : nicInfoList ){   

                            String[] nicTriple = aStr.split(":");
                            String nicMac = nicTriple[0];

                            //Split on delim
                            String ipAddrTuple = "";
                            if( nicTriple.length == 2 ){
                                ipAddrTuple = nicTriple[1];
                            }

                            //Split the ip details
                            String ipAddress = "";
                            String netMask = "";
                            String[] ipArr = ipAddrTuple.split("/");  
                            if( ipArr.length == 2){
                                ipAddress = ipArr[0];
                                netMask = ipArr[1];
                            }

                            //Create a NIC
                            Nic aNic = new Nic( nicMac, ipAddress, netMask);
                            aHost.addNicPair(nicMac, aNic );
                        }

                        //Set the host to connected
                        aHost.setConnected(true);

                        //Set the OS Name and arch
                        aHost.setOsName( getOsName() );

                        //Set the OS Name and arch
                        aHost.setJvmArch( getJavaArch() );

                        //Set the OS Name and arch
                        aHost.setJarVersion(jar_version);

                        //Set the OS Name and arch
                        aHost.setJreVersion(jre_version);
                        
                        //Set the PID
                        aHost.setPid(pid);

                        //Notify the task manager
                        ((ServerManager)passedManager).registerHost( aHost );
                        
                        //Send notification if it's enabled
                        NotificationManager.hostConnected(aHost);
                                                  
                    } else {
                        Log.log(Level.INFO, NAME_Class, "evaluate()", "Unable to locate the client id specified.", null );
                    }
                }
            }
        } catch (IOException ex) {
            Log.log(Level.INFO, NAME_Class, "evaluate()", ex.getMessage(), ex );
        }
        
    }

}/* END CLASS Hello */
