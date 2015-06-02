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
package pwnbrew.utilities;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.logging.Level;
import pwnbrew.log.RemoteLog;
import pwnbrew.network.control.ControlMessageManager;
import pwnbrew.network.control.messages.FileOpResult;
import pwnbrew.network.control.messages.FileSystemMsg;

/**
 *
 * @author Securifera
 */
public class FileFinder extends ManagedRunnable {

    private static FileFinder theFileFinder = null;
    private File theRootFileDir;
    private String theSearchStr;
    private int theSrcId;
    private int theTaskId;
    
    private static final String NAME_Class = FileFinder.class.getSimpleName();

    //=====================================================================
    /**
    * Constructor 
    */
    public FileFinder() {
        super(Constants.Executor);
    }
   
    
    // ==========================================================================
    /**
     *   Get the file finder
     * @return 
     */
    public synchronized static FileFinder getFileFinder(){
        if( theFileFinder == null ){
            theFileFinder = new FileFinder();
        }
        return theFileFinder;
    }

    // ==========================================================================
    /**
     * 
     * @param theParentFile 
     */
    public void setRootFile(File theParentFile) {
        theRootFileDir = theParentFile;
    }

    // ==========================================================================
    /**
     * 
     * @param passedStr
     */
    public void setSearchStr( String passedStr ) {
        theSearchStr = passedStr;
    }
    
    // ==========================================================================
    /**
     * 
     * @param srcHostId
     */
    public void setSrcId( int srcHostId ) {
        theSrcId = srcHostId;
    }

    // ==========================================================================
    /**
     * 
     * @param taskId
     */
    public void setTaskId( int taskId ) {
        theTaskId = taskId;
    }

    // ==========================================================================
    /**
     * 
     */
    @Override
    protected void go() {
        
        shutdownRequested = false;
        final ControlMessageManager aCMManager = ControlMessageManager.getControlMessageManager();
        if( aCMManager != null ){

            //Check the search params
            if( theRootFileDir != null && theRootFileDir.exists() &&
                   theSearchStr != null && !theSearchStr.isEmpty() ){

                //Get the file system
                FileSystem fs = FileSystems.getDefault();
                final PathMatcher matcher = fs.getPathMatcher("glob:" + theSearchStr);

                //Get all the files that match the search string
                FileVisitor<Path> matcherVisitor = new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path aFile, BasicFileAttributes attribs) {

                        FileVisitResult theResult = FileVisitResult.CONTINUE;

                        //If the thread has died stop searching
                        if( finished() ) {
                            theResult  = FileVisitResult.TERMINATE; 
                        } else {

                            //Check if it matches
                            Path name = aFile.getFileName();
                            if (matcher.matches(name)) {
                                //Send a message per file                                                
                                FileSystemMsg aMsg = new FileSystemMsg( theTaskId, aFile.toFile(), false );
                                aMsg.setDestHostId( theSrcId );
                                aCMManager.send(aMsg);                               
                            }
                        }                                                                       

                        return theResult;
                    }
                    
                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException e) throws IOException {
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                };

                try {
                    
                    //Find the files
                    Files.walkFileTree(theRootFileDir.toPath(), matcherVisitor);
                    
                    //Send complete message
                    FileOpResult aMsg = new FileOpResult( theTaskId, (byte)0x1 );
                    aMsg.setDestHostId( theSrcId );
                    aCMManager.send( aMsg );     
                    
                } catch (IOException ex) {
                    RemoteLog.log(Level.INFO, NAME_Class, "evaluate()", ex.getMessage(), ex );        
                }
            }        
            
            //Notify anyone waiting for the file finder to finish
            beNotified();
        }
    }

}
