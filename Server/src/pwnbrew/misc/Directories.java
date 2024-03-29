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
 * Directories.java
 *
 * Created on June 21, 2013, 6:55:23 PM
 */

package pwnbrew.misc;

import pwnbrew.utilities.Utilities;
import java.io.File;
import java.io.IOException;
import pwnbrew.host.HostFactory;


/**
 *
 */
final public class Directories {

    private static final String DELIM_Path = File.separator;
    
    // <root>
    private static final String DEFAULT_Root_Windows = "C:" + DELIM_Path + "Program Files";
    private static final String DEFAULT_Root_Unix = "/opt";
    private static String PATH_Root;

    private static final String DEFAULT_NAME_Home = "Pwnbrew";
    private static final String NAME_Home = DEFAULT_NAME_Home;
    private static String PATH_Home;    
    private static String PATH_Data;    
    private static String PATH_Log;
    
    private static String PATH_FileLibrary; 
    private static String PATH_JarLib; 
    private static String PATH_ObjectLibrary;
    private static String PATH_LocalObjectLibrary; 
    private static String PATH_Tasks; 
    private static String PATH_RemoteTasks;
    
    private static boolean directoriesCreated = false;
    static {      
        try {
          
            determinePathRoot();
            buildDirectoryPaths();
            createDirectories();            
            directoriesCreated = true;
            
        } catch( IOException ex ) {
            System.err.println( ex.getMessage() );
            System.exit(1);
        }      
    }
    
    
    // ==========================================================================
    /**
     * Prevents instantiation of {@link Directories} outside of itself.
     */
    private Directories() {
    }    
    
    // ==========================================================================
    /**
     * Determines if the host is running a Windows or Unix operating system and
     * sets the {@code PATH_Root} accordingly.
     * 
     * @throws IOException if the host's operating system is not of the Windows
     * nor Unix families
     */
    private static void determinePathRoot() throws IOException {

        if( Utilities.isWindows( Utilities.getOsName() ) ) //If the operating system is a Windows system...
            PATH_Root = DEFAULT_Root_Windows;
        else if( Utilities.isUnix( Utilities.getOsName() ) ) //If the operating system is a Unix system...
            PATH_Root = DEFAULT_Root_Unix;
        else //If the operating system is neither Windows nor Unix...
            throw new IOException (
                    new StringBuilder( "The local operating system \"" ).append( Utilities.OsName )
                    .append( "\" is not recognized." ).toString() );

    }
    
    
    // ==========================================================================
    /**
     * Builds the paths to each of the directories. Each path will begin with the
     * OS-dependent root and be delimited by the OS-dependent File.separator.
     */
    private static void buildDirectoryPaths() {
      
        StringBuilder strBldr = new StringBuilder();
      
        PATH_Home = strBldr.append( PATH_Root ).append( DELIM_Path ).append( NAME_Home ).toString();
        strBldr.setLength( 0 ); //Reset the StringBuilder

        PATH_Data = strBldr.append( PATH_Home ).append( DELIM_Path ).append( "data" ).toString();
        strBldr.setLength( 0 );

        PATH_Log = strBldr.append( PATH_Home ).append( DELIM_Path ).append( "log" ).toString();
        strBldr.setLength( 0 );
        
        PATH_FileLibrary = strBldr.append( PATH_Data ).append( DELIM_Path ).append( "filelib" ).toString();
        strBldr.setLength( 0 );
        
        PATH_JarLib = strBldr.append( PATH_Data ).append( DELIM_Path ).append( "jarlib" ).toString();
        strBldr.setLength( 0 );
        
        PATH_ObjectLibrary = strBldr.append( PATH_Data ).append( DELIM_Path ).append( "objlib" ).toString();
        PATH_LocalObjectLibrary = strBldr.append( DELIM_Path ).append( HostFactory.LOCALHOST ).toString();        
        strBldr.setLength( 0 );
        
        PATH_Tasks = strBldr.append( PATH_Log ).append( DELIM_Path ).append( "taskarc" ).toString();
        strBldr.setLength( 0 );
        
        PATH_RemoteTasks = strBldr.append( PATH_Tasks ).append( DELIM_Path ).append( "remote" ).toString();
        strBldr.setLength( 0 );

    }


    // ==========================================================================
    /**
     * Creates the directories.
     * 
     * @throws IOException if any of the directories could not be created
     */
    private static void createDirectories() throws IOException {

        // <root>/<home>
        ensureDirectoryExists( PATH_Home );

        // <root>/<home>/...
        ensureDirectoryExists( PATH_Data );
        ensureDirectoryExists( PATH_Log );
        ensureDirectoryExists( PATH_JarLib );
        
        // <root>/<home>/data/...
        ensureDirectoryExists( PATH_FileLibrary );
        ensureDirectoryExists( PATH_ObjectLibrary );
        ensureDirectoryExists( PATH_LocalObjectLibrary );
        ensureDirectoryExists( PATH_Tasks );

        // <root>/<home>/data/taskarc/...
        ensureDirectoryExists( PATH_RemoteTasks );
        
    }    
  
    // ==========================================================================
    /**
     * Ensures the directory at the given path exists.
     * <p>
     * If the directory already exists this method does nothing; otherwise this
     * method will attempt to create it. This method will also attempt to create
     * any necessary but non-existing parents of the given directory.
     * 
     * @param path the directory path
     * 
     * @throws IOException if the directory did not exist and could not be created
     */
    public static void ensureDirectoryExists( String path ) throws IOException {
        ensureDirectoryExists( new File( path ) );
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
      
        if( !directory.exists() ) //If the directory does not exist...
            if( !directory.mkdirs() ) //If the directory cannot be made...
                throw new IOException(
                        new StringBuilder( "Could not create the directory \"" )
                        .append( directory ).append( "\"" ).toString() );
   
    }
    
    
    // ==========================================================================
    /**
     * Indicates if the directory structure is in place.
     * 
     * @return {@code true} if all of the directories exist; {@code false} otherwise
     */
    public static boolean directoryStructureInitialized() {
        return directoriesCreated;
    }


    // ==========================================================================
    /**
     * Returns the path to the home directory.
     *
     * @return the path to the home directory
     */
    public static String getHomeDir() {
        return PATH_Home;
    }

    
    
    // ==========================================================================
    /**
     * Returns the path to the data directory.
     * 
     * @return the path to the data directory
     */
    public static String getDataPath() {
        return PATH_Data;
    }
    
     // ==========================================================================
    /**
     * Returns the path to the jar library.
     * 
     * @return the path to the JAR library
     */
    public static String getJarLibPath() {
        return PATH_JarLib;
    }
   
    // ==========================================================================
    /**
     * Returns the path to the log directory.
     * 
     * @return the path to the log directory
     */
    public static String getLogPath() {
        StringBuilder aSB = new StringBuilder();
        aSB.append(PATH_Log);

        return aSB.toString();
    }
    
    // ==========================================================================
    /**
     * Returns the path to the file library directory.
     * 
     * @return the path to the file library directory
     */
    public static String getFileLibraryPath() {
        return PATH_FileLibrary;
    }
    
    
    // ==========================================================================
    /**
     * Returns a {@link File} representing the file library directory.
     * 
     * @return a {@code File} representing the file library directory
     */
    public static File getFileLibraryDirectory() {
        return new File( getFileLibraryPath() );
    }
    
    
    // ==========================================================================
    /**
     * Returns the path to the object library directory.
     * 
     * @return the path to the object library directory
     */
    public static String getObjectLibraryPath() {
        return PATH_ObjectLibrary;
    }
    
     // ==========================================================================
    /**
     * Returns the path to the local object library directory.
     * 
     * @return the path to the local object library directory
     */
    public static String getLocalObjectLibraryPath() {
        return PATH_LocalObjectLibrary;
    }
    
    
    // ==========================================================================
    /**
     * Returns a {@link File} representing the object library directory.
     * 
     * @return a {@code File} representing the object library directory
     */
    public static File getObjectLibraryDirectory() {
        return new File( getObjectLibraryPath() );
    }
    
    // ==========================================================================
    /**
     * Returns a {@link File} representing the local object library directory.
     * 
     * @return a {@code File} representing the local object library directory
     */
    public static File getLocalObjectLibraryDirectory() {
        return new File( getLocalObjectLibraryPath() );
    }
    
    // ==========================================================================
    /**
     * Returns the path to the remote tasks directory.
     * 
     * @return the path to the remote tasks directory
     */
    public static String getRemoteTasksPath() {
        return PATH_RemoteTasks;
    }
    
    
    // ==========================================================================
    /**
     * Returns a {@link File} representing the remote tasks directory.
     * 
     * @return a {@code File} representing the remote tasks directory
     */
    public static File getRemoteTasksDirectory() {
        return new File( getRemoteTasksPath() );
    }
    
}
