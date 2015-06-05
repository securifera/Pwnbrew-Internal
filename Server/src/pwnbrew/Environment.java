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
 * Environment.java
 *
 * Created on June 26, 2013, 7:31:44 PM
 */

package pwnbrew;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.security.NoSuchAlgorithmException;
import java.security.ProtectionDomain;
import java.util.Enumeration;
import java.util.List;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import pwnbrew.gui.panels.options.OptionsJPanel;
import pwnbrew.log.Log;
import pwnbrew.log.LogLevel;
import pwnbrew.misc.Directories;
import pwnbrew.utilities.FileUtilities;
import pwnbrew.utilities.Utilities;
import pwnbrew.xmlBase.JarItem;
import pwnbrew.xmlBase.JarItemException;
import pwnbrew.xmlBase.XmlBase;
import pwnbrew.xmlBase.XmlBaseFactory;


/**
 * 
 */
final public class Environment {

    private static final String NAME_Class = Environment.class.getSimpleName();

    private static final TreeMap<String, Class<? extends Object>> thePathToClassMap = new TreeMap<>();

    private static final String ClassNameDiscriminator = ".class";       

    //private static final String theArch = System.getProperty("os.arch");
    private static final ProtectionDomain theProtectionDomain = Environment.class.getProtectionDomain();
    private static final CodeSource theCodeSource = theProtectionDomain.getCodeSource();
    private static final URL theCodeSourceLocationUrl = theCodeSource.getLocation();
    private static URI theCodeSourceLocationUri = null;
    private static File theCodeSourceFile = null;

    private static boolean isRunningFromJar = false;
    private static boolean isRunningWithinIde = false;
    
    private static final String MODULE_PACKAGE = "pwnbrew/modules/";
    private static final String CLIENT_MODULE = "10000";
    private static final String STAGER_MODULE = "10002";
    private static final String MALTEGO_MODULE = "10001";
    private static final String[] DEFAULT_MODULE_LIST = new String[]{ CLIENT_MODULE, MALTEGO_MODULE, STAGER_MODULE };

    static {
        determineRunLocation();
        populateNameToClassMap();
        loadSavedModules();
        loadDefaultModules();
    }

    // ==========================================================================
    /**
    * Prevents instantiation of {@link Environment} outside of itself.
    */
    private Environment() {
    }

    // ==========================================================================
    /**
     * Load any extension JARs
     */
    private static void loadSavedModules() {
        
        //For local 
        File jarLibDir = new File( Directories.getJarLibPath());
        File[] fileArr = jarLibDir.listFiles();
        for( File aFile : fileArr ){
            XmlBase anObj = XmlBaseFactory.createFromXmlFile(aFile);
            if( anObj instanceof JarItem ){
                
                //Add the jar to the map
                JarItem aRef = (JarItem)anObj;
                Utilities.addJarItem(aRef);
                
                //If it is a local extension then load it
                if( aRef.getType().equals(JarItem.LOCAL_EXTENSION_TYPE)){
                    
                    //Load the jar
                    File libraryFile = new File( Directories.getFileLibraryDirectory(), aRef.getFileHash() ); 
                    List<Class<?>> theClasses = Utilities.loadJar(libraryFile);
                    for( Class aClass : theClasses ){
                        addClassToMap(aClass);
                    }
                }
            }                    
        }        
        
    }
    
     // ==========================================================================
    /**
     * Load any extension JARs
     */
    private static void loadDefaultModules() {
        
        List<JarItem> jarList = Utilities.getJarItems();
        for( String aModuleUID : DEFAULT_MODULE_LIST ){
            boolean found = false;
            for( JarItem aJarItem : jarList )
                if( aJarItem.getId().equals(aModuleUID)){
                    found = true;                
                    break;
                }
            
            //Write the JAR out and load it into the server
            if( !found ){
                 
                try {
                    int bytesRead = 0;
                    //Create a temp file to write to
                    File aFile = File.createTempFile("tmp", null);
                    ClassLoader theClassLoader = Environment.class.getClassLoader();
                    String jarPath = MODULE_PACKAGE + aModuleUID;
                    //Get the resource
                    try (InputStream anIS = theClassLoader.getResourceAsStream(jarPath)) {
                        if( anIS != null ){
                            
                            byte[] byteBuffer = new byte[ 4096 ];
                            FileOutputStream theOutStream = new FileOutputStream(aFile);
                            try (BufferedOutputStream theBOS = new BufferedOutputStream(theOutStream)) {

                                //Read to the end
                                while( bytesRead != -1){
                                    bytesRead = anIS.read(byteBuffer);
                                    if(bytesRead != -1)
                                        theBOS.write(byteBuffer, 0, bytesRead);                                   
                                }
                                theBOS.flush();
                            }      
                                                         
                            //Create a FileContentRef
                            JarItem aJarItem = null;
                            try {
                                aJarItem = Utilities.getJavaItem( aFile );
                            } catch (JarItemException ex) {
                                Log.log(Level.SEVERE, NAME_Class, "evaluate()", ex.getMessage(), ex);         
                                return;
                            }
                            
                            //Add the jar
                            if( aJarItem != null ){
                                Utilities.addJarItem( aJarItem );

                                //Write the file to disk
                                String fileHash = FileUtilities.createHashedFile( aFile, null );
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

                                } 
                            }

                            //Delete the temp file
                            aFile.delete();
                        }
                    }
                    
//                    //Create a FileContentRef
//                    JarItem aJarItem = null;
//                    try {
//                        aJarItem = Utilities.getJavaItem( aFile );
//                    } catch (JarItemException ex) {
//                        Log.log(Level.SEVERE, NAME_Class, "evaluate()", ex.getMessage(), ex);         
//                        return;
//                    }
                    
//                     //Add the jar
//                    if( aJarItem != null ){
//                        Utilities.addJarItem( aJarItem );
//
//                        //Write the file to disk
//                        String fileHash = FileUtilities.createHashedFile( aFile, null );
//                        if( fileHash != null ) {
//
//                            //Create a FileContentRef
//                            aJarItem.setFileHash( fileHash ); //Set the file's hash
//
//                            //Write to disk
//                            aJarItem.writeSelfToDisk();
//
//                            //If it is a local extension then load it
//                            if( aJarItem.getType().equals(JarItem.LOCAL_EXTENSION_TYPE)){
//
//                                //Load the jar
//                                File libraryFile = new File( Directories.getFileLibraryDirectory(), aJarItem.getFileHash() ); //Create a File to represent the library file to be copied
//                                List<Class<?>> theClasses = Utilities.loadJar(libraryFile);
//                                for( Class aClass : theClasses )
//                                    addClassToMap(aClass);                            
//                            }
//
//                        } 
//                    }
//                    
//                    //Delete the temp file
//                    aFile.delete();
                } catch (IOException | NoSuchAlgorithmException ex) {
                    Log.log( LogLevel.SEVERE, NAME_Class, "loadDefaultModules()", ex.getMessage(), ex );
                }
            }
        }
              
    }    
    
    
    // ==========================================================================
    /**
    * Determines if the program is running from a jar file or within an IDE.
    * <p>
    * NOTE: This method used to make this determination is known to work only for
    * NetBeans.
    */
    private static void determineRunLocation() {

        try {
            theCodeSourceLocationUri = theCodeSourceLocationUrl.toURI(); //Get the URI of the code source location
        } catch( URISyntaxException ex ) {
            //Do nothing
            ex = null;
        }

        if( theCodeSourceLocationUri != null ) { //If the code source location URI was obtained...

            theCodeSourceFile = new File( theCodeSourceLocationUri ); //Create a File to represent the code source
            if( theCodeSourceFile.canRead() ) { //If the code source File can be read...

                if ( theCodeSourceFile.isDirectory() ) { //If the code source File represents a directory...
                    isRunningWithinIde = true; //The program is being run from within the IDE.
                } else { //If the code source File does not represent a directory...
                    isRunningFromJar = true; //The program is being run from the jar file
                }

            } else { //If the code source File cannot be read...
                Log.log( LogLevel.SEVERE, NAME_Class, "determineRunLocation()", "Cannot read the code source File.", null );
            }

        } else { //If the code source location URI was not obtained...
            Log.log( LogLevel.SEVERE, NAME_Class, "determineRunLocation()", "Could not obtain a URI for the code source.", null );
        }

    }/* END determineRunLocation() */

    // ==========================================================================
    /**
    *
     * @return 
    */
    public static URL getCodeSourceLocation() {
        return theCodeSourceLocationUrl;
    }

    // ==========================================================================
    /**
    * Populates the {@link TreeMap} of Classes and their simple names.
    */
    private static void populateNameToClassMap() {

        if( isRunningWithinIde ) { //If the program is running within the IDE...
          
            if( theCodeSourceFile != null ) { //If the code source was found...

                //Populate the map from the directory
                populateNameToClassMapFromDirectory( theCodeSourceFile.getAbsolutePath().length() + 1, theCodeSourceFile );

            } else { //If the code source was not found...
                Log.log( LogLevel.SEVERE, NAME_Class, "populateNameToClassMap()",
                "Determined the program is running within an IDE, but could not find the code source directory.", null );
            }

        } else if( isRunningFromJar ) { //If the program is running from a jar file...

            JarFile jarFile;
            try {
                jarFile = new JarFile( theCodeSourceFile ); //Create a JarFile
            } catch( IOException ex ) {
                Log.log( LogLevel.SEVERE, NAME_Class, "populateNameToClassMap()",
                "Determined the program is running from a jar file, but an IOException was thrown while "
                + "creating a JarFile. Message: \"" + ex.getMessage() + "\"", ex );
                return;
            }

            populateNameToClassMapFromJarFile( jarFile ); //Populate the map from the jar file

            try {
                jarFile.close();
            } catch( IOException ex ) {
                Log.log( LogLevel.WARNING, NAME_Class, "populateNameToClassMap()",
                "Could not close the jar file. Message: \"" + ex.getMessage() + "\"", ex );
            }

        } else { //If the program is not running from a jar file nor within the IDE...
            Log.log( LogLevel.SEVERE, NAME_Class, "populateNameToClassMap()", "Cannot determine where the program is running.", null );
        }

    }

    // ==========================================================================
    /**
    * Populates the {@link TreeMap} of Classes and their simple names from the directory
    * represented by the given {@link File}.
    * 
    * @param pathPrefixLength
    * @param directory
    *
    * @throws IllegalArgumentException if the given {@code File} is null
    */
    private static void populateNameToClassMapFromDirectory( int pathPrefixLength, File directory ) {

        if( directory == null ||  directory.exists() == false || directory.isDirectory() == false 
                || directory.canRead() == false) { //If the File is null...
            throw new IllegalArgumentException( "The directory is invalid or null" );
        }

        //Get a list of the directory's files
        File[] fileList = directory.listFiles();
        StringBuilder strBldr = new StringBuilder();
        int slashAtIndex;

        for( File aFile : fileList ) {

            if( aFile.isDirectory() == true ) { 

                //Populate the map with the directory's contents (recursive)
                populateNameToClassMapFromDirectory( pathPrefixLength, aFile );

            } else { 

                String fullPathStr = aFile.getAbsolutePath();

                if( fullPathStr.endsWith( ClassNameDiscriminator )) {
                    
                    strBldr.setLength( 0 ); //Reset the StringBuilder
                    strBldr.append( fullPathStr ); //Append the path
                    strBldr.setLength( strBldr.length() - ClassNameDiscriminator.length() ); //Trim the ".class"
                    strBldr.delete( 0, pathPrefixLength ); //Trim the path

                    //Replace all backslashes in the JarEntry's name with periods...
                    //Replace "\\" with File.separator because "\\" is operating system dependant
                    slashAtIndex = strBldr.indexOf( File.separator ); //Find the first occuring backslash
                    while( slashAtIndex > -1 ) { //While a backslash is found...
                        if(slashAtIndex == 0)
                            strBldr.deleteCharAt(slashAtIndex);
                        else
                            strBldr.setCharAt( slashAtIndex, '.' ); //Replace the backslash with a period
                        
                        slashAtIndex = strBldr.indexOf( File.separator ); //Find the next backslash
                    }

                    addClassToMap( strBldr.toString() ); //Add the Class to the map

                }
            } 
        } 
    }

    // ==========================================================================
    /**
    * Populates the {@link TreeMap} of Classes and their simple names from the directory
    * represented by the given {@link File}.
    *
    * @param pathPrefixLength
    * @param directory
    *
    * @throws IllegalArgumentException if the given {@code File} is null
    */
    private static void populateNameToClassMapFromJarFile( JarFile jarFile ) {

        Enumeration<JarEntry> jarEntryEnum = jarFile.entries(); //Get an Enumeration of the JarEntrys
        JarEntry aJarEntry;
        String jarEntryName;
        StringBuilder strBldr = new StringBuilder();
        int slashAtIndex;

        while( jarEntryEnum.hasMoreElements() ) { //While the Enumeration has another JarEntry...

            aJarEntry = jarEntryEnum.nextElement(); //Get the next JarEntry
            jarEntryName = aJarEntry.getName(); //Get the JarEntry's name

            if( jarEntryName.endsWith( ClassNameDiscriminator )){ //If the JarEntry's name ends with ".class"...

                strBldr.setLength( 0 ); //Reset the StringBuilder
                strBldr.append( jarEntryName ); //Append the JarEntry's name
                strBldr.setLength( jarEntryName.length() - ClassNameDiscriminator.length() ); //Trim the ".class"

                //Replace all slashes in the JarEntry's name with periods...
                slashAtIndex = strBldr.indexOf( "/" ); //Find the first occuring slash
                while( slashAtIndex > -1 ) { //While a slash is found...
                    strBldr.setCharAt( slashAtIndex, '.' ); //Replace the slash with a period
                    slashAtIndex = strBldr.indexOf( "/" ); //Find the next slash
                }

                addClassToMap( strBldr.toString() ); //Add the Class to the map

            }
        }
    }
    // ==========================================================================
    /**
    * Finds the {@link Class} that has the given path and adds the path/{@code Class}
    * pair to the map.
    * <p>
    * If the given {@code String} is null or empty, this method does nothing.
    *
     * @param aClass
    */
    public static void addClassToMap( Class aClass ) {

        if( aClass != null ){
            //Determine if the class is an XmlBase
            Class parentClass = aClass.getSuperclass();
            if( parentClass != null ){
                while( parentClass != Object.class){
                    if( parentClass == XmlBase.class ){
                        thePathToClassMap.put( aClass.getSimpleName(), aClass ); //Map the path to the Class
                        break;
//                    } else if( parentClass == RunnableItemController.class ){
//                        Utilities.addRunnableController( aClass.getCanonicalName() );
//                        break;
                    } else if( parentClass == OptionsJPanel.class ){
                        Utilities.addOptionsJPanel( aClass.getCanonicalName() );
                        break;
                    }else {
                        parentClass = parentClass.getSuperclass();
                    }
                }
            }
        }

    }/* END addClassToMap( String ) */
    
    // ==========================================================================
    /**
    * Finds the {@link Class} that has the given path and adds the path/{@code Class}
    * pair to the map.
    * <p>
    * If the given {@code String} is null or empty, this method does nothing.
    *
    * @param path the class path
    */
    public static void addClassToMap( String path ) {

        if( path == null || path.isEmpty() )
            return; 
        
        //Get the Class with the path...
        Class aClass;
        try {
            aClass = Class.forName( path );
        } catch( ClassNotFoundException ex ) {
            Log.log( LogLevel.WARNING, NAME_Class, "addClassToMap()",
            "Could not obtain a Class for the path provided. Will not be able to instantiate the class.",ex );
            return;
        }  
        
        addClassToMap(aClass);

    }/* END addClassToMap( String ) */


    // ==========================================================================
    /**
    * Returns the {@link Class} having the given name.
    * <p>
    * The given {@code String} must be the simple name of the {@code Class}.
    *
    * @param className the simple name of the {@code Class}
    *
    * @return the {@code Class} having the given name; null if no such {@code Class}
    * is found
    *
    * @throws IllegalArgumentException if the given {@code String} is null or empty
    */
    public static Class getClassByName( String className ) {

        if( className == null )
            throw new IllegalArgumentException( "The String cannot be null." );
        else if( className.isEmpty() )
            throw new IllegalArgumentException( "The String cannot be empty." );
        
        return thePathToClassMap.get( className );

    }

}/* END CLASS Environment */
