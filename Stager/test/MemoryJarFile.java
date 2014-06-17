
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

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
/**
 *
 * @author Securifera
 */
public class MemoryJarFile extends JarFile {
    
    private final Map<String, Object[]> theClassMap = new HashMap<String, Object[]>();
    private Manifest theManifest = null;
    private final JarInputStream theJarInputStream;
    
    //==================================================================
    /**
     * Constructor
     * @param passedJar 
     * @param jarBytes 
     * @throws java.io.IOException 
     */
    public MemoryJarFile( File passedJar, byte[] jarBytes ) throws IOException{
        super(passedJar);
        
        //Creat an inputstream
        ByteArrayInputStream aBIS = new ByteArrayInputStream( jarBytes );  
        theJarInputStream = new JarInputStream(aBIS);
        
        //Set manifest
        theManifest = theJarInputStream.getManifest();
        JarEntry aJarEntry = theJarInputStream.getNextJarEntry();
                
        //Add the jar entries to the map
        synchronized(theClassMap){
            while( aJarEntry != null ){                
                //Set the manifest
                String jarEntryName = aJarEntry.getName();
                 
                //Add if it's not a directory
                if( !jarEntryName.endsWith("/")){
                    
                    int entrySize = (int)aJarEntry.getSize();
                    ByteBuffer aBB = ByteBuffer.allocate(entrySize);
                    
                    //Read the class and put it in the map
                    int temp;
                    byte[] buffer = new byte[1024];
                    while((temp = theJarInputStream.read(buffer)) > 0)
                        aBB.put(buffer, 0 , temp);
                    
                    theClassMap.put(aJarEntry.getName(), new Object[]{ aJarEntry, Arrays.copyOf(aBB.array(), aBB.position()) });
                    
                }
                
                //Get next entry
                aJarEntry = theJarInputStream.getNextJarEntry();
            }
        }
        
    }
    
    //==================================================================
     /**
     * Returns the <code>JarEntry</code> for the given entry name or
     * <code>null</code> if not found.
     *
     * @param name the jar file entry name
     * @return the <code>JarEntry</code> for the given entry name or
     *         <code>null</code> if not found.
     *
     * @throws IllegalStateException
     *         may be thrown if the jar file has been closed
     *
     * @see java.util.jar.JarEntry
     */
    @Override
    public JarEntry getJarEntry( String name ) {
        
        Object[] theObjArr;
        synchronized(theClassMap){
            theObjArr = theClassMap.get( name );
        }
        
         //Get the jar entry
        JarEntry theJarEntry = null;
        if( theObjArr != null ){
            Object anObj = theObjArr[0];
            if( anObj instanceof JarEntry )
                theJarEntry = (JarEntry)anObj;
            
        }
        
        return theJarEntry;
    }
    
    //==================================================================
     /**
     * Returns the jar file manifest, or <code>null</code> if none.
     *
     * @return the jar file manifest, or <code>null</code> if none
     * @throws java.io.IOException
     *
     * @throws IllegalStateException
     *         may be thrown if the jar file has been closed
     */
    @Override
    public Manifest getManifest() throws IOException {
        return theManifest;
    }
    
    //==================================================================
    /**
     * Returns an enumeration of the zip file entries.
     * @return 
     */
    @Override
    public Enumeration<JarEntry> entries() {
        
        List<JarEntry> entryList = new ArrayList<JarEntry>();
        //Get the object arrays
        Collection<Object[]> theObjArr;
	synchronized(theClassMap){
            theObjArr = theClassMap.values();
        }
        
        //Get the jar entry
        for( Object[] anObjArry : theObjArr ){
            Object anObj = anObjArry[0];
            if( anObj instanceof JarEntry )
                entryList.add( (JarEntry)anObj);
        }
        
        return Collections.enumeration(entryList);
    }
    
    //==================================================================
     /**
     * Returns an input stream for reading the contents of the specified
     * zip file entry.
     * @param ze the zip file entry
     * @return an input stream for reading the contents of the specified
     *         zip file entry
     * @throws IOException if an I/O error has occurred
     * @throws SecurityException if any of the jar file entries
     *         are incorrectly signed.
     * @throws IllegalStateException
     *         may be thrown if the jar file has been closed
     */
    @Override
    public InputStream getInputStream( ZipEntry ze ) throws IOException {
        
        String entryName = ze.getName();
        Object[] theObjArr;
        synchronized(theClassMap){
            theObjArr = theClassMap.get( entryName );
        }
        
        ByteArrayInputStream aBIS = null;
        if( theObjArr != null ){
            Object anObj = theObjArr[1];
            if( anObj instanceof byte[] ){
                byte[] theByteArr = (byte[])anObj;
                aBIS = new ByteArrayInputStream(theByteArr);
            }
            
        }
        
        return aBIS;
    }
}
