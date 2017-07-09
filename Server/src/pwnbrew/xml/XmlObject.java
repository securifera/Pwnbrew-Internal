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
 * XmlObject.java
 *
 * Created on June 25, 2013, 11:21:41 PM
 */

package pwnbrew.xml;


import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import pwnbrew.utilities.FileUtilities;


/**
 *
 */
abstract public class XmlObject {

    protected final Map<String, String> thePropertyMap = new LinkedHashMap<>();
    protected final Map<String, AttributeCollection> thePropertyCollectionMap = new LinkedHashMap<>();

    public static final String OBJECT_ID = "Id";
    public static final String OBJECT_NAME = "Name";
 
    // ==========================================================================
    /**
    * Creates a new instance of {@link XmlObject}.
    */
    public XmlObject() {

        //Initialize attributes...
        thePropertyMap.put( OBJECT_ID, "" );
        thePropertyMap.put(OBJECT_NAME, "");

    }


    // ==========================================================================
    /**
    * Sets the property with the given name to the given value.
    *
    * @param name the name of the property to set
    * @param value the value to give the property
    */
    public synchronized void setProperty( String name, String value ) {

        if( thePropertyMap.get( name ) != null ) 
            thePropertyMap.put( name, ( value != null ? value : "" ) ); 

    }

    // ==========================================================================
    /**
    * Returns the value of the property with the given name.
    * <p>
    * If the given String is null, this method returns null.
    *
    * @param name the name of the attribute
    *
    * @return the value of the property with the given name, null if the {@link XmlObject}
    * has no such property
    */
    public final synchronized String getProperty( String name ) {
        return thePropertyMap.get( name );
    }

    // ==========================================================================
    /**
    * Returns the value of the {@link XmlObject}'s Id.
    *
    * @return the value of the {@code XmlObject}'s Id
    */
    public final String getId() {
        return getProperty( OBJECT_ID );
    }

    // ==========================================================================
    /**
    * Sets the value of the {@link XmlObject}'s Id attribute to the given value.
    *
    * @param id the new id
    */
    public final void setId( String id ) {
        setProperty( OBJECT_ID, id );
    }

    // ==========================================================================
    /**
    * Returns the value of the {@link XmlObject}'s Name
    *
    * @return the value of the {@code XmlObject}'s Name
    */
    public final String getName() {
        return getProperty(OBJECT_NAME );
    }

    // ==========================================================================
    /**
    * Sets the value of the {@link XmlObject}'s Name to the given value.
    *
     * @param name
    */
    public final void setName( String name ) {
        setProperty(OBJECT_NAME, name );
    }
    
    // ===============================================
    /**
    * Writes this object to the specified directory in XML format. 
    *
    * @param  targDirFile   the directory in which to store the file
    * @param  index   the location in the tree
    *
    * @return   an error message string, <code>null</code> if no errors occurred
    * @throws java.io.IOException
    */
    public String writeSelfToDisk( File targDirFile, int index ) throws IOException {

        String rtnStr = null;
        StringBuilder aSB = new StringBuilder();
        if(index != -1){
            aSB.append(Integer.toString(index));
        }
        aSB.append("_");

        String theObjId = getId(); //Get the object's instance ID
        if( theObjId != null ) { //If the String is not NULL...

        if( theObjId.isEmpty() == false ) { //If the String is not empty...
            aSB.append(theObjId);

            //Check if the unordered file exists first
            if( !(new File(targDirFile, aSB.toString()).exists())){

                File[] theFileList = targDirFile.listFiles();
                if( theFileList != null ){
                    for(File aFile : theFileList){
                        String fileName = aFile.getName();
                        if(fileName.contains(theObjId)){
                            aSB = new StringBuilder().append(fileName);
                            break;
                        }
                    }
                }
            }

            writeSelfToDisk( targDirFile, aSB.toString() );
        } else { //If the String is empty...
        rtnStr = "The instance ID is empty.";
        }

        } else { //If the String is NULL...
            rtnStr = "The instance ID is null.";
        }

        return rtnStr;

    }
    

    // ==========================================================================
    /**
    * Writes a file with the given name containing the {@link XmlObject}'s data in
    * the directory represented by the given {@link File}.
    *
    * @param directory a {@code File} representing the directory in which the file
    * is to be written
    * @param fileName the name the file is to be given
    *
    * @return a {@code File} representing the file to which the XML data was written
    *
    * @throws IllegalArgumentException if the {@code File} is null or does not represent
    * a directory; if the file name is invalid
    * @throws IOException if an I/O error occurs
    */
    public final synchronized File writeSelfToDisk( File directory, String fileName ) throws IOException {

        if( directory == null ) //If the File is null...
            throw new IllegalArgumentException( "The File cannot be null." );
        else if( !directory.exists() ) { //If the directory does not exist

            boolean directoryExists = directory.mkdirs();
            if( directoryExists == false ) //If the directory still doesn't exist...
                throw new IOException( new StringBuilder( "Could not create the parent directory \"" )
                .append( directory.getName() ).append( "\"" ).toString() );

        } else if( directory.isDirectory() == false ) //If the File does not represent a directory...
            throw new IllegalArgumentException( "The File must represent a directory." );


        if( fileName == null ) //If the String is null...
            throw new IllegalArgumentException( "The file name cannot be null." );
        else if( fileName.isEmpty() ) //If the String is empty...
            throw new IllegalArgumentException( "The file name cannot be an empty String." );
        else if( fileName.startsWith( "." ) ) //If the String starts with a period...
            throw new IllegalArgumentException( "The file name \"" + fileName + "\" is not valid." );

        File targetFile = new File( directory, fileName );
        //Ensure that the file is writable
        targetFile.setWritable(true);

        if( FileUtilities.verifyCanWrite( targetFile ) == false ) //If the file cannot be written to...
            throw new IOException( "Cannot write to the file \"" + targetFile + "\"." );

        //Block until we obtain a write lock
        RandomAccessFile randomAccessFile = new RandomAccessFile( targetFile, "rw" );
        try {

            FileChannel aFileChannel = randomAccessFile.getChannel();
            aFileChannel.lock();

            randomAccessFile.seek( 0L ); //Set the file pointer to the beginning of the file
            randomAccessFile.setLength( 0L ); //Truncate the file

            //Pass the file on to be written to
            writeXml(randomAccessFile);

        } finally {

            try {
                //Ensure all the bytes have been written by syncing
                randomAccessFile.getFD().sync();
            } catch (IOException ex){
                ex = null;
            }

            //Close the file
            try {
                randomAccessFile.close(); //Close the RandomAccessFile
            } catch (IOException ex){
                ex = null;
            }

        }

        //Set the file to read only
        targetFile.setReadOnly();
      
        return targetFile;

    }

    // ==========================================================================
    /**
    * Deletes the file representing this {@link XmlObject} from the specified directory.
    *
    * @param directory the directory from which to delete the file
    *
    * @return <tt>true</tt> if and only if a file was deleted; <tt>false</tt>
    * otherwise, specifically if the given {@code File} represents a file or a non-existent
    * directory or if no file is found
    *
    */
    public boolean deleteSelfFromDirectory( File directory ) {

        if( directory == null ) //If the File is null...
            return false;
       
        boolean rtnBool = false;

        if( directory.exists() ) { //If the given File exists...

            if( directory.isDirectory() ) { //If the given File is a directory...

                //Find the file to delete...
                final String fileName = getId(); //Generate a file name
                if( fileName != null && fileName.isEmpty() == false ) { //If the String is not null and not empty...
                    //Look for a file with this name.

                    File[] theFiles = directory.listFiles( new FileFilter(){

                        @Override
                        public boolean accept(File pathname) {
                            if(pathname.getName().contains(fileName)){
                                return true;
                            } else {
                                return false;
                            }
                        }
                    });

                    if( theFiles != null && theFiles.length > 0 ) //If the file exists...
                        for(File theFileToDelete : theFiles)
                            rtnBool = theFileToDelete.delete(); //Delete the file                  

                }

            }
        } 
        return rtnBool;

    }

    // ==========================================================================
    /**
    * Adds the given {@link XmlObject} to this {@code XmlObject} or updates an existing
    * component.
    * <p>
    * The behavior of this method is to be refined by subclasses of {@code XmlObject}.
    *
    * @param component the component to add/update
    */
    public void addChildObject( XmlObject component ) {

        if( component instanceof AttributeCollection )
            thePropertyCollectionMap.put(component.getName(), (AttributeCollection)component);        

    }


    // ==========================================================================
    /**
    * Removes references to the given {@link XmlObject} from this {@code XmlObject}.
    *
    * @param component the component to remove
    * 
    * @return <tt>true</tt> if references to the given {@code XmlObject} were removed,
    * <tt>false</tt> otherwise
    */
    public boolean removeChildObject( XmlObject component ) {

        boolean retVal = false;
        if( component instanceof AttributeCollection ) { //If the XmlObject is a AttributeCollection...
            thePropertyCollectionMap.remove(component.getName());
            retVal = true;
        }

        return retVal;
    }

    // ==========================================================================
    /**
    * @param <T>
    * @return an empty {@code ArrayList<XmlObject>}
    */
    public <T extends XmlObject> List<T> getXmlObjects() {

        List<T> rtnList = new ArrayList<>();
        synchronized( thePropertyCollectionMap ){
            if(thePropertyCollectionMap.size() > 0){
                for(String aKey : thePropertyCollectionMap.keySet()){
                    rtnList.add((T)thePropertyCollectionMap.get(aKey));
                }
            }
        }

        return rtnList;
    }

    // ==========================================================================
    /**
    * Generates the XML data for this {@link XmlObject} and its {@code XmlObject} components.
    *
    * @return a {@code String} containing the XML data representing this object
    */
    public String getXml() {

        StringBuilder stringBuilder = new StringBuilder();
        appendXml( stringBuilder ); //Compile the XML data
        String rtnStr = stringBuilder.toString();

        return rtnStr;

    }

    // ==========================================================================
    /**
    * Generates the XML data for this {@link XmlObject} and its {@code XmlObject} components
    * and appends it to the given {@link StringBuilder}.
    *
    * @param stringBuilder the {@link StringBuilder} to which the XML data is to
    * be appended
    *
    * @throws IllegalArgumentException if the argument is null
    */
    protected void appendXml( StringBuilder stringBuilder ) {

        if( stringBuilder == null ) { //If the StringBuilder is null...
            throw new IllegalArgumentException( "The StringBuilder cannot be null." );
        }

        List<XmlObject> componentList = getXmlObjects(); //Get the XMLable components
        if(  componentList == null || componentList.isEmpty() ) {

            appendXml_OneLine( stringBuilder ); //Append the XML data for this XmlObject in the one-line format

        } else { //If there are components or character data...

            //The start tag...
            appendXmlStartTag( stringBuilder ); //Add the start tag

            //The components...
            for( XmlObject anXB : componentList ) { //For each component...

                if( anXB != null ) { //If the component is not null...

                    //Add the XML data for the component...
                    anXB.appendXml( stringBuilder ); //Add the components XML (recursive call)

                }

            }

            appendXmlEndTag( stringBuilder ); //Add the end tag

        }

    }


    // ==========================================================================
    /**
    * Generates the XML data for this {@link XmlObject} in the single-line format and
    * appends it to the given {@code StringBuilder}.
    *
    * @param stringBuilder the {@link StringBuilder} to which the XML data is to
    * be appended
    *
    * @throws IllegalArgumentException if the argument is null
    */
    private void appendXml_OneLine( StringBuilder stringBuilder ) {

        if( stringBuilder == null ) { //If the StringBuilder is null...
            throw new IllegalArgumentException( "The StringBuilder cannot be null." );
        }

        appendXmlStartTag( stringBuilder ); //Append the XML start tag

        stringBuilder.insert( stringBuilder.length() - 1, "/" ); //Change the start tag to a one-line element

    }

    // ==========================================================================
    /**
    * Generates the XML start tag for this object and appends it to the given {@link StringBuilder}.
    * <p>
    * In the XML generated by this method, each attribute represents a {@link Field}
    * of this {@code XmlObject}.
    *
    * @param stringBuilder the {@link StringBuilder} to which the XML data is to
    * be appended
    *
    * @throws IllegalArgumentException if the argument is null
    */
    protected void appendXmlStartTag( StringBuilder stringBuilder ) {

        if( stringBuilder == null ) { //If the StringBuilder is null...
            throw new IllegalArgumentException( "The StringBuilder cannot be null." );
        }

        //Begin the start tag with the class name...
        stringBuilder.append( "<" ).append( this.getClass().getSimpleName() );

        //Add the attributes...
        for( String name : thePropertyMap.keySet() ) { //For each attribute name...

            String value = thePropertyMap.get( name ); //Get the value mapped to the name
            if( value != null ) { //If a value was obtained...

                stringBuilder.append( " " ); //Add a space
                stringBuilder.append( name ); //Add the attribute name
                stringBuilder.append( "=\"" ); //Add the equals sign and open quote
                stringBuilder.append( XmlUtilities.encode( value ) ); //Add the value
                stringBuilder.append( "\"" ); //Add the close quote

            } else { //If no value was obtained...
            }

        }

        stringBuilder.append( ">" ); //End the start tag

    }

    // ==========================================================================
    /**
    * Generates the XML end tag for this object and appends it to the given {@link StringBuilder}.
    *
    * @param stringBuilder the {@link StringBuilder} to which the XML data is to
    * be appended
    *
    * @throws IllegalArgumentException if the argument is null
    */
    private void appendXmlEndTag( StringBuilder stringBuilder ) {

        if( stringBuilder == null ) { //If the StringBuilder is null...
            throw new IllegalArgumentException( "The StringBuilder cannot be null." );
        }

        stringBuilder.append( "</" ).append( this.getClass().getSimpleName() ).append( ">" );

    }
    
    // ==========================================================================
    /**
    * Generates the XML start tag for this object and writes it to the given {@link RandomAccessFile}.
    * <p>
    * In the XML generated by this method, each attribute represents a {@link Field}
    * of this {@code XmlObject}.
    *
    * @param passedFile the {@link RandomAccessFile} to which the XML data is to
    * be written
     * @param isOneLiner
     * @throws java.io.IOException
    *
    * @throws IllegalArgumentException if the argument is null
    */
    protected void writeXmlStartTag( RandomAccessFile passedFile, boolean isOneLiner ) throws IOException {

        if( passedFile == null ) { //If the StringBuilder is null...
            throw new IllegalArgumentException( "The RandomAccessFile cannot be null." );
        }

        //Begin the start tag with the class name...
        StringBuilder stringBuilder = new StringBuilder().append( "<" ).append( this.getClass().getSimpleName() );

        //Add the attributes...
        for( String name : thePropertyMap.keySet() ) { //For each attribute name...

            String value = thePropertyMap.get( name ); //Get the value mapped to the name
            if( value != null ) { //If a value was obtained...

                stringBuilder.append( " " ); //Add a space
                stringBuilder.append( name ); //Add the attribute name
                stringBuilder.append( "=\"" ); //Add the equals sign and open quote
                stringBuilder.append( XmlUtilities.encode( value ) ); //Add the value
                stringBuilder.append( "\"" ); //Add the close quote

            } else { //If no value was obtained...
            }

        }

        //Change the start tag to a one-line element...
        if(isOneLiner){
            stringBuilder.append( "/>" ); //Add the one-line terminator "/>"
        } else {
            stringBuilder.append( ">" ); //End the start tag
        }
        passedFile.write(stringBuilder.toString().getBytes("US-ASCII"));

    }

    // ==========================================================================
    /**
    * Generates the XML end tag for this object and writes it to the given {@link RandomAccessFile}.
    *
    * @param passedFile the {@link RandomAccessFile} to which the XML data is to
    * be written
    *
    * @throws IllegalArgumentException if the argument is null
    */
    private void writeXmlEndTag( RandomAccessFile passedFile ) throws IOException {

        if( passedFile == null ) { //If the StringBuilder is null...
            throw new IllegalArgumentException( "The StringBuilder cannot be null." );
        }

        passedFile.write(new StringBuilder().append( "</" ).append( this.getClass().getSimpleName() ).append( ">" ).toString().getBytes("US-ASCII"));

    }

    @Override
    // ==========================================================================
    /**
     *  Get the string representation
     */
    public String toString() {
        return getProperty(OBJECT_NAME);
    }

    // ==========================================================================
    /**
    * Tells the XmlObject to write its bytes to the passed random access file
    *
    * @param randomAccessFile the {@link RandomAccessFile} to which the XML data is to
    * be written
    *
    * @throws Exception if anything goes wrong
    */
    private void writeXml(RandomAccessFile randomAccessFile) throws IOException{

        if( randomAccessFile == null ) //If the StringBuilder is null...
            throw new IllegalArgumentException( "The StringBuilder cannot be null." );

        List<XmlObject> componentList = getXmlObjects(); //Get the XMLable components
        if(  componentList == null || componentList.isEmpty() )
            writeXmlStartTag( randomAccessFile, true ); //Append the XML start tag
        else { //If there are components or character data...

            //The start tag...
            writeXmlStartTag( randomAccessFile, false ); //Add the start tag

            for( XmlObject anXB : componentList )//For each component...
                if( anXB != null ) //If the component is not null...
                    //Add the XML data for the component...
                    anXB.writeXml( randomAccessFile ); //Add the components XML (recursive call)
           
            writeXmlEndTag( randomAccessFile ); //Add the end tag

        }
    }

}
