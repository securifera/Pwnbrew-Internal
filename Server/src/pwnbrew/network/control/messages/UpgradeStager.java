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
* Payload.java
*
* Created on Feb 2, 2014, 7:21:29 PM
*/

package pwnbrew.network.control.messages;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;
import pwnbrew.log.Log;
import pwnbrew.misc.Directories;
import pwnbrew.network.ControlOption;
import pwnbrew.xmlBase.JarItem;

/**
 *
 *  
 */

@SuppressWarnings("ucd")
public final class UpgradeStager extends ControlMessage{
    
    private static final byte OPTION_STAGER = 34;
    private static final byte OPTION_JAR_VERSION = 19;    
    
     //Class name
    private static final String NAME_Class = UpgradeStager.class.getSimpleName();    


    // ==========================================================================
    /**
     * Constructor
     *
     * @param dstHostId
     * @param passedJarItem
    */
    public UpgradeStager( int dstHostId, JarItem passedJarItem ) {
        super( dstHostId );
        
        //Get the jar file and read into a byte array
        File libraryFile = new File( Directories.getFileLibraryDirectory(), passedJarItem.getFileHash() ); //Create a File to represent the library file to be copied
        
        //Read the file
        ByteArrayOutputStream aBOS = new ByteArrayOutputStream();
        try {

            FileInputStream fis = new FileInputStream(libraryFile);
            try{

                //Read into the buffer
                byte[] buf = new byte[1024];                
                for (int readNum; (readNum = fis.read(buf)) != -1;) 
                    aBOS.write(buf, 0, readNum);                

            //Close
            } finally {
                try {
                    fis.close();
                } catch (IOException ex) {
                    ex = null;
                }
            }
        } catch (IOException ex) {
            Log.log(Level.INFO, NAME_Class, "UpgradeStager()", ex.getMessage(), ex );   
        }
        
        //Add the stager
        ControlOption aTlv = new ControlOption( OPTION_STAGER, aBOS.toByteArray());
        addOption(aTlv);
        
        //Add the version        
        String jarVersion = passedJarItem.getVersion();
        aTlv = new ControlOption( OPTION_JAR_VERSION, jarVersion.getBytes());
        addOption(aTlv);
    }

}
