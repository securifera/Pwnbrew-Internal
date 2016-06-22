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
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.logging.Level;
import static pwnbrew.Environment.addClassToMap;
import pwnbrew.log.Log;
import pwnbrew.manager.PortManager;
import pwnbrew.manager.DataManager;
import pwnbrew.misc.Directories;
import pwnbrew.utilities.FileUtilities;
import pwnbrew.utilities.Utilities;
import pwnbrew.xmlBase.JarItem;
import pwnbrew.xmlBase.JarItemException;

/**
 *
 *  
 */
public final class AddToJarLibrary extends JarItemMsg{ // NO_UCD (use default)
    
    private static final String NAME_Class = AddToJarLibrary.class.getSimpleName();
    
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
    public AddToJarLibrary(int dstHostId, String passedName, String passedType, String passedJvmVersion, String passedJarVersion ) throws UnsupportedEncodingException {
        super( dstHostId, passedName, passedType, passedJvmVersion, passedJarVersion );
    }
    
    
    // ==========================================================================
    /**
     * Constructor
     *
     * @param passedId
    */
    public AddToJarLibrary(byte[] passedId ) {
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
        
        try {
            File aFile = File.createTempFile("tmp", null);
            File libDir = aFile.getParentFile();
            aFile.delete();

            //Get the filename
            String[] fileHashFileNameArr = theJarName.split(":", 2);
            if( fileHashFileNameArr.length != 2 ){
                Log.log(Level.SEVERE, NAME_Class, "evaluate()", "Passed hash filename string is not correct.", null);         
                return;
            }

            //Get the jar file
            File tempJarFile = new File( libDir, fileHashFileNameArr[1]);
            if( tempJarFile.exists()){

                //Create a FileContentRef
                JarItem aJarItem;
                try {
                    aJarItem = Utilities.getJavaItem( tempJarFile );
                } catch (JarItemException ex) {
                    Log.log(Level.SEVERE, NAME_Class, "evaluate()", ex.getMessage(), ex);         
                    return;
                }
                
                if( aJarItem != null ){
                    
                    List<JarItem> jarList = Utilities.getJarItems();
                    try {

                        for( JarItem currentItem : jarList ){

                            //Check if the jvm version is the same first
                            if( aJarItem.getJvmMajorVersion().equals(currentItem.getJvmMajorVersion()) && 
                                    aJarItem.getType().equals( currentItem.getType()) ){

                                //Only one Stager and Payload are allowed
                                if( aJarItem.getType().equals(JarItem.STAGER_TYPE) || aJarItem.getType().equals(JarItem.PAYLOAD_TYPE)){
                                    Utilities.removeJarItem(currentItem);
                                    if( !currentItem.getFileHash().equals(aJarItem.getFileHash())){
                                        currentItem.deleteSelfFromDirectory( new File( Directories.getJarLibPath() ));
                                        currentItem.deleteFileContentFromLibrary();
                                    }
                                    break;
                                //Check if one with the same name exists
                                } else if( aJarItem.getName().equals(currentItem.getName() )) {
                                    Utilities.removeJarItem(currentItem);
                                    if( !currentItem.getFileHash().equals(aJarItem.getFileHash())){
                                        currentItem.deleteSelfFromDirectory( new File( Directories.getJarLibPath() ));
                                        currentItem.deleteFileContentFromLibrary();
                                    }
                                    break;
                                }
                            }
                        }

                        //Add the jar
                        Utilities.addJarItem( aJarItem );

                        //Write the file to disk
                        String fileHash = FileUtilities.createHashedFile( tempJarFile, null );
                        if( fileHash != null ) {

                            //Create a FileContentRef
                            aJarItem.setFileHash( fileHash ); //Set the file's hash

                            //Write to disk
                            aJarItem.writeSelfToDisk();

                            //If it is a local extension then load it
                            if( aJarItem.getType().equals(JarItem.LOCAL_EXTENSION_TYPE)){

                                //Load the jar
                                File libraryFile = new File( Directories.getFileLibraryDirectory(), aJarItem.getFileHash() ); //Create a File to represent the library file to be copied
                                List<Class<?>> theClasses = Utilities.loadJar(libraryFile);
                                for( Class aClass : theClasses )
                                    addClassToMap(aClass);                            
                            }

                            try {

                                //Send the msg
                                AddToJarLibrary aMsg = new AddToJarLibrary( getSrcHostId(), aJarItem.toString(), theJarType, aJarItem.getJvmMajorVersion(), aJarItem.getVersion() );
                                DataManager.send( passedManager, aMsg);

                                //Delete the file
                                tempJarFile.delete();

                            } catch (UnsupportedEncodingException ex) {
                                Log.log(Level.WARNING, NAME_Class, "deleteJarItem", ex.getMessage(), ex );
                            }

                        } 

                    } catch ( NoSuchAlgorithmException | IOException ex) {
                        Log.log(Level.SEVERE, NAME_Class, "evaluate()", ex.getMessage(), ex);     
                    }
                }
                
            }
        } catch (IOException ex) {
            Log.log(Level.SEVERE, NAME_Class, "evaluate()", ex.getMessage(), ex);         
        }
    }

}
