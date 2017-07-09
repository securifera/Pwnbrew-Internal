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
* GetCount.java
*
*/

package pwnbrew.network.control.messages;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.logging.Level;
import pwnbrew.log.Log;
import pwnbrew.manager.PortManager;
import pwnbrew.manager.DataManager;
import pwnbrew.misc.Directories;
import pwnbrew.utilities.Utilities;
import pwnbrew.xml.JarItem;

/**
 *
 *  
 */
@SuppressWarnings("ucd")
public final class DeleteJarItem extends JarItemMsg { 
        
     private static final String NAME_Class = DeleteJarItem.class.getSimpleName();
     
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
    public DeleteJarItem(int dstHostId, String passedName, String passedType, String passedJvmVersion, String passedJarVersion ) throws UnsupportedEncodingException {
        super( dstHostId, passedName, passedType, passedJvmVersion, passedJarVersion );
    }
    
    // =====================================================================
    /**
     * Constructor
     *
     * @param passedId
    */
    public DeleteJarItem(byte[] passedId ) {
        super( passedId );
    }
    
    //===============================================================
    /**
    *   Performs the logic specific to the message.
    *
     * @param passedManager
    */
    @Override
    public void evaluate( PortManager passedManager ) {
    
        List<JarItem> jarList = Utilities.getJarItems();
        for( JarItem anItem : jarList){
               
            if( anItem.toString().equals(theJarName) &&  anItem.getType().equals(theJarType) && 
                    anItem.getJvmMajorVersion().equals(theJvmVersion) &&
                    anItem.getVersion().equals(theJarVersion) ){
                Utilities.removeJarItem(anItem);
                anItem.deleteSelfFromDirectory( new File( Directories.getJarLibPath() ));
                break;
            }
        }
        
        try {

            //Send the msg
            DeleteJarItem aMsg = new DeleteJarItem( getSrcHostId(), theJarName, theJarType, theJvmVersion, theJarVersion );
            DataManager.send( passedManager,aMsg);
           
        } catch (UnsupportedEncodingException ex) {
            Log.log(Level.WARNING, NAME_Class, "deleteJarItem", ex.getMessage(), ex );
        }
    }

}
