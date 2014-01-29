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
 * Created on June 10, 2013, 8:11:12 PM
 */



import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import pwnbrew.logging.LoggableException;
import pwnbrew.misc.Constants;
import pwnbrew.misc.Directories;
import pwnbrew.png.Png;
import pwnbrew.png.PngParser;
import pwnbrew.utilities.SocketUtilities;
import pwnbrew.utilities.Utilities;


/**
 *
 */
final public class Persistence {
    
    //The configuration file
    public transient static final String CONF_CHUNK = "xPWn";
    public transient static final String SSL_CHUNK = "sSLn";     
    
    // ==========================================================================
    /**
     * Prevents instantiation of {@link Directories} outside of itself.
     */
    private Persistence() {
    }/* END CONSTRUCTOR() */

     
   //=========================================================================
    /** 
    *  Get the path to the directory to write the conf file.
    */
    private static File getFile() {        

        File binDir = new File( Directories.getRoot(), "bin");
        File theLogo = new File(binDir, Constants.SPLASH_IMG_STR);

        
        return theLogo;
    }
    
    //=========================================================================
    /** 
    *   Gets the bytes for the label that was passed from the data store. (PNG, etc.)
     * @param passedLabel
     * @return 
     * @throws pwnbrew.logging.LoggableException 
    */
    public static List<byte[]> getLabelBytes( String passedLabel ) throws LoggableException{
        
        //Add each byte array to the return list
        List<byte[]> byteArrList = new ArrayList<>();
        File theLogo = Persistence.getFile();

        if( theLogo != null && theLogo.exists() ){
            
            //Parse the png
            Png theLogoPNG = PngParser.parse(theLogo);
            Set<Png.PngChunk> chuckSet = theLogoPNG.getChunks( passedLabel );
            
            //And the chunk bytes to the list
            for( Iterator<Png.PngChunk> theIter = chuckSet.iterator(); theIter.hasNext(); ){       
                byteArrList.add( theIter.next().getValue() );
            }
            
        }
        
        return byteArrList;
        
    }
    
    //=========================================================================
    /** 
    *   Gets the bytes for the label that was passed from the data store. (PNG, etc.)
     * @param passedLabel
     * @param passedBytes
     * @throws pwnbrew.logging.LoggableException
    */
    public static void writeLabel( String passedLabel, byte[] passedBytes ) throws LoggableException{
        
        long lastModified;
        File theLogo = Persistence.getFile();
        
        if( theLogo != null){
            
            if( !theLogo.exists() ){
                
                //Set the modified time to two years prior
                Utilities.writeJarElementToDisk(theLogo, Constants.IMAGE_PATH_IN_JAR, Constants.SPLASH_IMG_STR);
        
                Calendar aCalendar = Calendar.getInstance();
                aCalendar.setTime( new Date());
                aCalendar.add( Calendar.YEAR, -3 );
                lastModified = aCalendar.getTimeInMillis();
                
            } else {
                //Set the date to 
                lastModified = theLogo.lastModified();
            }

            //Parse the png
            Png theLogoPNG = PngParser.parse(theLogo);
            try {

                //Create a new PNGChunk
                byte[] byteLen = SocketUtilities.intToByteArray( passedBytes.length );
                Png.PngChunk aChunk = new Png.PngChunk(byteLen, passedLabel.getBytes("US-ASCII"), passedBytes, null );
                theLogoPNG.replaceChunk( passedLabel, aChunk);

                //Write to disk
                theLogoPNG.writeToDisk();

                //Set the date to the previous date
                theLogo.setLastModified(lastModified);

            } catch (IOException ex) {
                throw new LoggableException(ex);
            }
        }
        
    }
    
    //=========================================================================
    /**
     *  Remove any entries that match the passed label.
     * 
     * @param passedLabel
     * @throws LoggableException 
    */ 
   
    public static void removeLabel( String passedLabel ) throws LoggableException{
        
        File theLogo = Persistence.getFile();          
        if( theLogo != null && theLogo.exists() ){
                           
            //Set the date to 
            long lastModified = theLogo.lastModified();
           
            //Parse the png
            Png theLogoPNG = PngParser.parse(theLogo);

            //Remove any chunks that match the label
            theLogoPNG.removeChunks( passedLabel );

            //Write to disk
            theLogoPNG.writeToDisk();

            //Set the date to the previous date
            theLogo.setLastModified(lastModified);

        }
        
    }    
   

}/* END CLASS Directories */
