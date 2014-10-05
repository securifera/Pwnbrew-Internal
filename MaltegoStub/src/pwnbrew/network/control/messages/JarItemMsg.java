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

import java.io.UnsupportedEncodingException;
import pwnbrew.MaltegoStub;
import pwnbrew.functions.Function;
import pwnbrew.functions.ToServerConfiguration;
import pwnbrew.manager.PortManager;
import pwnbrew.network.ControlOption;

/**
 *
 *  
 */
public class JarItemMsg extends MaltegoMessage{ 
    
    protected static final byte OPTION_JAR_NAME = 32;
    protected static final byte OPTION_JAR_TYPE = 33;
    protected static final byte OPTION_JVM_VERSION = 34;
    protected static final byte OPTION_JAR_VERSION = 35;
    
    protected String theJarName = null;
    protected String theJarType = null;
    protected String theJarVersion = null;
    protected String theJvmVersion = null;
    
     // ==========================================================================
    /**
     * Constructor
     *
     * @param dstHostId
     * @param passedName
     * @param passedType
     * @param passedJvmVersion
     * @param passedJarVersion
     * @throws java.io.UnsupportedEncodingException
    */
    public JarItemMsg( int dstHostId, String passedName, String passedType, String passedJvmVersion, String passedJarVersion ) throws UnsupportedEncodingException {
        super( dstHostId );
        
        byte[] tempBytes = passedName.getBytes("US-ASCII");
        ControlOption aTlv = new ControlOption( OPTION_JAR_NAME, tempBytes);
        addOption(aTlv);
        
        tempBytes = passedType.getBytes("US-ASCII");
        aTlv = new ControlOption( OPTION_JAR_TYPE, tempBytes);
        addOption(aTlv);
        
        tempBytes = passedJvmVersion.getBytes("US-ASCII");
        aTlv = new ControlOption( OPTION_JVM_VERSION, tempBytes);
        addOption(aTlv);
        
        tempBytes = passedJarVersion.getBytes("US-ASCII");
        aTlv = new ControlOption( OPTION_JAR_VERSION, tempBytes);
        addOption(aTlv);
 
    }
    
    
     // ==========================================================================
    /**
     * Constructor
     *
     * @param passedId
    */
    public JarItemMsg(byte[] passedId ) {
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
                case OPTION_JAR_NAME:
                    theJarName = new String( theValue, "US-ASCII");
                    break;
                case OPTION_JAR_TYPE:
                    theJarType = new String( theValue, "US-ASCII");
                    break;
                case OPTION_JVM_VERSION:
                    theJvmVersion = new String( theValue, "US-ASCII");
                    break;
                case OPTION_JAR_VERSION:
                    theJarVersion = new String( theValue, "US-ASCII");
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
            if( aFunction instanceof ToServerConfiguration ){
                
                //Cast the function
                ToServerConfiguration aFunc = (ToServerConfiguration)aFunction;
                
                //Add the jar item
                aFunc.addJarItem(theJarName, theJarType, theJvmVersion, theJarVersion);
            }            
        }  
    }

}
