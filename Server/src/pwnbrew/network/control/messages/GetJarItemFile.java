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

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import pwnbrew.log.Log;
import pwnbrew.manager.PortManager;
import pwnbrew.manager.DataManager;
import pwnbrew.misc.Directories;
import pwnbrew.network.ControlOption;
import pwnbrew.utilities.SocketUtilities;
import pwnbrew.utilities.Utilities;
import pwnbrew.xml.JarItem;

/**
 *
 *  
 */
@SuppressWarnings("ucd")
public final class GetJarItemFile extends JarItemMsg { 
        
    private static final byte OPTION_TASK_ID = 92;
    private int theTaskId;
    
    private static final String NAME_Class = GetJarItemFile.class.getSimpleName();
    
    public static final short MESSAGE_ID = 0x5c;
      
     // =====================================================================
    /**
     * Constructor
     *
     * @param passedId
    */
    public GetJarItemFile(byte[] passedId ) {
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
                case OPTION_TASK_ID:
                    theTaskId = SocketUtilities.byteArrayToInt(theValue);
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
     
        //Get the jar item
        JarItem theItem = Utilities.getStagerJarItem( theJvmVersion );
        if( theItem != null ){
         
            try {
                //Get the jar file and read into a byte array
                File libraryFile = new File( Directories.getFileLibraryDirectory(), theItem.getFileHash() ); //Create a File to represent the library file to be copied
                
                //Queue the file to be sent
                String fileHashNameStr = new StringBuilder().append("0").append(":").append(libraryFile.getAbsolutePath()).toString();
                PushFile thePFM = new PushFile( theTaskId, fileHashNameStr, libraryFile.length(), PushFile.JAR_DOWNLOAD, getSrcHostId() );
                DataManager.send(passedManager, thePFM);
                
            } catch (IOException ex) {
                Log.log(Level.SEVERE, NAME_Class, "evaluate()", ex.getMessage(), ex );
            }
            
        }
    }

}
