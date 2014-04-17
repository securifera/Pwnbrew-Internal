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

/*
 * Persistence.java
 *
*/


import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import pwnbrew.log.LoggableException;
import pwnbrew.misc.Constants;
import pwnbrew.misc.DebugPrinter;
import pwnbrew.misc.SocketUtilities;
import pwnbrew.misc.Utilities;
import pwnbrew.png.Png;
import pwnbrew.png.PngParser;


/**
 *  
 */
final public class Persistence {
    
    transient static final String CONF_CHUNK = "xPWn";
    public transient static final String SSL_CHUNK = "sSLn";   
//    private transient static final String WINDOWS_CONF_IMG_STR = "ie.png"; 
//    
//    private transient static final String LINUX_CONF_IMG_STR = "linux.png";  
    private transient static final String RESOURCE_PATH_IN_JAR= "pwnbrew/iamges";
    private transient static String CONF_IMG_STR; 
 
    private static final String DELIM_Path = File.separator;
    private static final String USER_DIR = System.getProperty("user.home"); 

    private static String PATH_Root;

    // <root>/...
//    private static final String MICROSOFT_NAME_Home = "SystemCertificates";
//    private static final String LINUX_NAME_Home = "share";  
    
    // <root>/...
//    private static final String MICROSOFT_DATA_Home = "Response";
//    private static final String LINUX_DATA_Home = "icc";    
    
    private static String NAME_Home;
    private static String DATA_Home;
    private static String PATH_Home; // <root>/<home>
    
    // <root>/<home>/...
    private static String PATH_Data; // <root>/<home>/data
    
//    static {
//      
//        try {
          
//            determinePathRoot();
//            buildDirectoryPaths();
//            createDirectories();
            
//        } catch( IOException ex ) {
//            System.err.println( ex.getMessage() );
//        }
//      
//    }
    
    private static final String NAME_Class = Persistence.class.getSimpleName();
       
//    // ==========================================================================
//    /**
//     * Determines if the host is running a Windows or Unix operating system and
//     * sets the {@code PATH_Root} accordingly.
//     * 
//     * @throws IOException if the host's operating system is not of the Windows
//     * nor Unix families
//     */
//    private static void determinePathRoot() throws IOException {
//
//        StringBuilder aSB = new StringBuilder();
//        if( Utilities.isWindows() ){ //If the operating system is a Windows system...
//            
//            String appDataDir = System.getenv("APPDATA");
//            aSB.append( appDataDir );
//            aSB.append("\\Microsoft" );
//            
//            //Set the name and data paths
//            NAME_Home = MICROSOFT_NAME_Home;
//            DATA_Home = MICROSOFT_DATA_Home;
//            
//        } else if( Utilities.isUnix() ){ //If the operating system is a Unix system...
//            
//            aSB.append( USER_DIR ).append("/");  
//            aSB.append(".local");
//            
//             //Set the name and data paths
//            NAME_Home = LINUX_NAME_Home;
//            DATA_Home = LINUX_DATA_Home;
//
//        } else //If the operating system is neither Windows nor Unix...
//            throw new IOException (
//                    new StringBuilder( "The local operating system \"" ).append( Utilities.OS_NAME )
//                    .append( "\" is not recognized." ).toString() );
//
//        PATH_Root = aSB.toString();
//    }/* END determinePathRoot() */
//    
    
//    // ==========================================================================
//    /**
//     * Builds the paths to each of the directories. Each path will begin with the
//     * OS-dependent root and be delimited by the OS-dependent File.separator.
//     */
//    private static void buildDirectoryPaths() {
//      
//        StringBuilder strBldr = new StringBuilder();
//      
//        // <root>/<home>
//        PATH_Home = strBldr.append( PATH_Root ).append( DELIM_Path ).append( NAME_Home ).toString();
//        strBldr.setLength( 0 ); //Reset the StringBuilder
//        
//        PATH_Data = strBldr.append( PATH_Home ).append( DELIM_Path ).append( DATA_Home ).toString();
//        strBldr.setLength( 0 );
// 
//    }/* END createDirectoryPaths() */


//    // ==========================================================================
//    /**
//     * Creates the directories.
//     * 
//     * @throws IOException if any of the directories could not be created
//     */
//    private static void createDirectories() throws IOException {
//
//        // <root>/<home>
//        ensureDirectoryExists( PATH_Home );
//        ensureDirectoryExists( PATH_Data );
//        
//    }/* END createDirectories() */
    
//    // ==========================================================================
//    /**
//     * Ensures the directory at the given path exists.
//     * <p>
//     * If the directory already exists this method does nothing; otherwise this
//     * method will attempt to create it. This method will also attempt to create
//     * any necessary but non-existing parents of the given directory.
//     * 
//     * @param path the directory path
//     * 
//     * @throws IOException if the directory did not exist and could not be created
//     */
//    private static void ensureDirectoryExists( String path ) throws IOException {
//        ensureDirectoryExists( new File( path ) );
//    }/* END ensureDirectoryExists( String ) */
    
    
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
      
        if( !directory.exists() ) 
            if( !directory.mkdirs() ) 
                throw new IOException(
                        new StringBuilder( "Could not create the directory \"" )
                        .append( directory ).append( "\"" ).toString() );

    }
    
//    // ==========================================================================
//    /**
//     * Returns the path to the data directory.
//     * 
//     * @return the path to the data directory
//     */
//    public static String getDataPath() {
//        return PATH_Data;
//    }/* END getDataPath() */
//    
//     // ==========================================================================
//    /**
//     * Returns the path to the user directory.
//     * 
//     * @return the path to the user directory
//     */
//    private static String getUserPath() {
//        return USER_DIR;
//    }
    
    //=========================================================================
    /** 
    *  Get the path to the directory to write the conf file.
    */
    private static File getFile() {        

//        File dataDir = new File( Directories.getRoot(), "data");
        File theLogo = new File( Constants.EDITOR_IMG_STR );
        
        return theLogo;
    }
//    //=======================================================================
//    /** 
//    *  Get the path to the directory to write the conf file.
//    */
//    private static File getFile() {        
//       
//        File confFile = null;
//        try{
//            if( Utilities.isWindows()){
//
//                //Add the 64 bit one
//                File dir = new File( PATH_Root + "\\Internet Explorer");
//
//                //Check the dir
//                ensureDirectoryExists(dir);
//
//                confFile = new File( dir, WINDOWS_CONF_IMG_STR );
//                CONF_IMG_STR = WINDOWS_CONF_IMG_STR;
//                
//            }  else if( Utilities.isUnix() ){ //If the operating system is a Unix system...
//
//                //Check the dir
//                File confDir = new File(Persistence.getUserPath() + "/.cache");
//                Persistence.ensureDirectoryExists(confDir);
//                
//                confFile = new File( confDir, Persistence.LINUX_CONF_IMG_STR);
//                CONF_IMG_STR = Persistence.LINUX_CONF_IMG_STR;
//
//            } 
//            
//        } catch( IOException ex ){
//            DebugPrinter.printMessage( NAME_Class, "getFile", ex.getMessage(), ex);      
//        }
//        
//        return confFile;
//    }
    
    //=======================================================================
    /** 
    *   Gets the bytes for the label that was passed from the data store. (PNG, etc.)
     * @param passedLabel
     * @return 
    */
    public static List<byte[]> getLabelBytes( String passedLabel ) {
        
        //Add each byte array to the return list
        List<byte[]> byteArrList = new ArrayList<>();
        File confFile = Persistence.getFile();

        try {
            
            if( confFile != null && confFile.exists() ){

                //Parse the png
                Png theLogoPNG = PngParser.parse(confFile);
                Set<Png.PngChunk> chuckSet = theLogoPNG.getChunks( passedLabel );

                //And the chunk bytes to the list
                for( Iterator<Png.PngChunk> theIter = chuckSet.iterator(); theIter.hasNext(); ){       
                    byteArrList.add( theIter.next().getValue() );
                }

            }
            
        } catch( LoggableException ex ){
            DebugPrinter.printMessage( NAME_Class, "getLabelBytes", ex.getMessage(), ex); 
        }
        
        return byteArrList;
        
    }
    
    //=======================================================================
    /** 
    *   Gets the bytes for the label that was passed from the data store. (PNG, etc.)
     * @param passedLabel
     * @param passedBytes
     * @throws pwnbrew.log.LoggableException
    */
    public static void writeLabel( String passedLabel, byte[] passedBytes ) throws LoggableException{
        
        long lastModified;
        File confFile = Persistence.getFile();
        
        if( confFile != null){
            
            if( !confFile.exists() ){
                
                //Set the modified time to two years prior
                Utilities.writeJarElementToDisk(confFile, Persistence.RESOURCE_PATH_IN_JAR, Persistence.CONF_IMG_STR);
                Calendar aCalendar = Calendar.getInstance();
                aCalendar.setTime( new Date());
                aCalendar.add( Calendar.YEAR, -3 );
                lastModified = aCalendar.getTimeInMillis();
                
            } else {
                //Set the date to 
                lastModified = confFile.lastModified();
            }

            //Parse the png
            Png theLogoPNG = PngParser.parse(confFile);
            try {

                //Create a new PNGChunk
                byte[] byteLen = SocketUtilities.intToByteArray( passedBytes.length );
                Png.PngChunk aChunk = new Png.PngChunk(byteLen, passedLabel.getBytes("US-ASCII"), passedBytes, null );
                theLogoPNG.replaceChunk( passedLabel, aChunk);

                //Write to disk
                theLogoPNG.writeToDisk();

                //Set the date to the previous date
                confFile.setLastModified(lastModified);

            } catch (UnsupportedEncodingException ex) {
                throw new LoggableException(ex);
            }
        }
        
    }
    
    //=======================================================================
    /**
     *  Remove any entries that match the passed label.
     * 
     * @param passedLabel
     * @throws LoggableException 
    */ 
   
    public static void removeLabel( String passedLabel ) throws LoggableException{
        
        File confFile = Persistence.getFile();          
        if( confFile != null && confFile.exists() ){
                           
            //Set the date to 
            long lastModified = confFile.lastModified();
           
            //Parse the png
            Png theLogoPNG = PngParser.parse(confFile);

            //Remove any chunks that match the label
            theLogoPNG.removeChunks( passedLabel );

            //Write to disk
            theLogoPNG.writeToDisk();

            //Set the date to the previous date
            confFile.setLastModified(lastModified);

        }
        
    }
   
}
