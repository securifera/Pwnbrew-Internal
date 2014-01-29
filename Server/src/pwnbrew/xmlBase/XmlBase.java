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
 * XmlBase.java
 *
 * Created on June 25, 2013, 11:21:41 PM
 */

package pwnbrew.xmlBase;

import pwnbrew.exception.WriteFileException;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import pwnbrew.generic.Identifiable;
import pwnbrew.utilities.FileUtilities;


/**
 *
 */
abstract public class XmlBase implements Identifiable{

    //Attributes...
    /** The attribute HashMap maps the attributes' names to their respective values.
    * <p>
    * The use of 'attributes' reflects the XML terminology in which each this=that
    * pair in the start tag is called an attribute with 'this' being the name and
    * 'that' being the value. */
    //NOTE: HashMap is NOT synchronized, will allow null keys and values, and makes
    //  no guarantees about the order of the entries when iterating.
    protected final Map<String, String> theAttributeMap = new LinkedHashMap<>();
    protected final Map<String, AttributeCollection> theAttributeCollectionMap = new LinkedHashMap<>();

    /** The XmlBase's instance ID. */
    public static final String ATT_Id = "Id";
    public static final String ATTRIBUTE_Name = "Name";
 
    // ==========================================================================
    /**
    * Creates a new instance of {@link XmlBase}.
    */
    public XmlBase() {

        //Initialize attributes...
        theAttributeMap.put( ATT_Id, "" );
        theAttributeMap.put( ATTRIBUTE_Name, "");

    }/* END CONSTRUCTOR() */


    // ==========================================================================
    /**
    * Sets the attribute with the given name to the given value.
    * <p>
    * If the {@link XmlBase} does not have an attribute with the given name or if
    * the 'name' argument is null, this method does nothing. If the 'value' argument
    * is null, the attribute's value is set to the empty String.
    *
    * @param name the name of the attribute to set
    * @param value the value to give the attribute
    */
    public synchronized void setAttribute( String name, String value ) {

        //NOTE: The condition below ensures this method will not add attributes.
        if( theAttributeMap.get( name ) != null ) //If the key is already in the map...
            theAttributeMap.put( name, ( value != null ? value : "" ) ); //Set the value of the attribute

    }/* END setFieldValue( String, String ) */


    // ==========================================================================
    /**
    * Returns the value of the attribute with the given name.
    * <p>
    * If the given String is null, this method returns null.
    *
    * @param name the name of the attribute
    *
    * @return the value of the attribute with the given name, null if the {@link XmlBase}
    * has no such attribute
    */
    public final synchronized String getAttribute( String name ) {
        return theAttributeMap.get( name );
    }/* END getFieldValue( String ) */

    // ==========================================================================
    /**
    * Returns the value of the {@link XmlBase}'s Id attribute.
    *
    * @return the value of the {@code XmlBase}'s Id attribute
    */
    public final String getId() {
        return getAttribute( ATT_Id );
    }/* END getId() */


    // ==========================================================================
    /**
    * Sets the value of the {@link XmlBase}'s Id attribute to the given value.
    * <p>
    * If the given String is null, the Id attribute's value is set to the empty String.
    *
    * @param id the new id
    */
    public final void setId( String id ) {
        setAttribute( ATT_Id, id );
    }/* END setId( String ) */


    // ==========================================================================
    /**
    * Returns the value of the {@link XmlBase}'s Name attribute.
    *
    * @return the value of the {@code XmlBase}'s Name attribute
    */
    @Override
    public final String getName() {
        return getAttribute( ATTRIBUTE_Name );
    }/* END getName() */


    // ==========================================================================
    /**
    * Sets the value of the {@link XmlBase}'s Name attribute to the given value.
    * <p>
    * If the given String is null, the Name attribute's value is set to the empty String.
    *
     * @param name
    */
    public final void setName( String name ) {
        setAttribute( ATTRIBUTE_Name, name );
    }/* END setName( String ) */

  // ===============================================
    /**
    * Writes this object to the specified directory in XML format.  The file
    * is given the InstanceID of the object as its filename.  To specify the
    * filename, use {@link #writeSelfToDirectoryInXML(java.io.File, java.lang.String)}.
    *
    * @param  targDirFile   the directory in which to store the file
    * @param  index   the location in the tree
    *
    * @return   an error message string, <code>null</code> if no errors occurred
     * @throws java.io.IOException
    *
    * @see #writeSelfToDirectoryInXML(java.io.File, java.lang.String)
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

    }/* END writeSelfToDirectoryInXML( File ) */


    // ==========================================================================
    /**
    * Writes a file with the given name containing the {@link XmlBase}'s data in
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
    * @throws WriteFileException if the target file cannot be written to
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
            throw new WriteFileException( "Cannot write to the file \"" + targetFile + "\"." );

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

    }/* END writeSelfToDisk( File, String ) */


    // ==========================================================================
    /**
    * Deletes the file representing this {@link XmlBase} from the specified directory.
    * <p>
    * This method looks for a file that has a name matching the value returned by
    * {@link #generateFileName()}. If such a file is found it is deleted.
    * <p>
    * If the given {@code File} is null, this method does nothing and returns false.
    *
    * @param directory the directory from which to delete the file
    *
    * @return <tt>true</tt> if and only if a file was deleted; <tt>false</tt>
    * otherwise, specifically if the given {@code File} represents a file or a non-existent
    * directory or if no file is found
    *
    * @throws IllegalArgumentException if the argument is null
    * @throws SecurityException if a security manager exists and its {@link
    * java.lang.SecurityManager#checkDelete} method denies delete access to the file
    *
    * @see #generateFileName()
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

            } //End of "if( theDirectory.isDirectory() ) { //If the given File is a directory..."

        } //End of "if( theDirectory.exists() ) { //If the given File exists..."

        return rtnBool;

    }/* END deleteSelfFromDirectory( File ) */


    // ==========================================================================
    /**
    * Adds the given {@link XmlBase} to this {@code XmlBase} or updates an existing
    * component.
    * <p>
    * The behavior of this method is to be refined by subclasses of {@code XmlBase}.
    *
    * @param component the component to add/update
    */
    public void addUpdateComponent( XmlBase component ) {

        if( component instanceof AttributeCollection ) { //If the XmlBase is a AttributeCollection...
            theAttributeCollectionMap.put(component.getName(), (AttributeCollection)component);
        }

    }/* END addUpdateComponent( XmlBase ) */


    // ==========================================================================
    /**
    * Removes references to the given {@link XmlBase} from this {@code XmlBase}.
    * <p>
    * The behavior of this method is to be defined by subclasses of {@code XmlBase}.
    * This version does nothing and always returns <tt>false</tt>.
    *
    * @param component the component to remove
    * 
    * @return <tt>true</tt> if references to the given {@code XmlBase} were removed,
    * <tt>false</tt> otherwise
    */
    public boolean removeComponent( XmlBase component ) {

        boolean retVal = false;
        if( component instanceof AttributeCollection ) { //If the XmlBase is a AttributeCollection...
            theAttributeCollectionMap.remove(component.getName());
            retVal = true;
        }

        return retVal;
    }/* END removeComponent( XmlBase ) */

    // ==========================================================================
    /**
    * Returns a list of the {@link XmlBase}'s components.
    * <p>
    * The list this method returns is to be defined by subclasses. Generally, the
    * first line in the subclasses' implementations of this method should be a call
    * to "super.getXmlComponents()" so that each Class need be concerned with adding
    * only the components it defines.
    * 
     * @param <T>
    * @return an empty {@code ArrayList<XmlBase>}
    */
    public <T extends XmlBase> List<T> getXmlComponents() {

        List<T> rtnList = new ArrayList<>();
        synchronized( theAttributeCollectionMap ){
            if(theAttributeCollectionMap.size() > 0){
                for(String aKey : theAttributeCollectionMap.keySet()){
                    rtnList.add((T)theAttributeCollectionMap.get(aKey));
                }
            }
        }

        return rtnList;
    }/* END getXmlComponents() */


    // ==========================================================================
    /**
    * Generates the XML data for this {@link XmlBase} and its {@code XmlBase} components.
    *
    * @return a {@code String} containing the XML data representing this object
    */
    public String getXml() {

        StringBuilder stringBuilder = new StringBuilder();
        appendXml( stringBuilder ); //Compile the XML data
        String rtnStr = stringBuilder.toString();

        return rtnStr;

    }/* END getXml() */


    // ==========================================================================
    /**
    * Generates the XML data for this {@link XmlBase} and its {@code XmlBase} components
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

        List<XmlBase> componentList = getXmlComponents(); //Get the XMLable components
        if(  componentList == null || componentList.isEmpty() ) {

            appendXml_OneLine( stringBuilder ); //Append the XML data for this XmlBase in the one-line format

        } else { //If there are components or character data...

            //The start tag...
            appendXmlStartTag( stringBuilder ); //Add the start tag

            //The components...
            for( XmlBase anXB : componentList ) { //For each component...

                if( anXB != null ) { //If the component is not null...

                    //Add the XML data for the component...
                    anXB.appendXml( stringBuilder ); //Add the components XML (recursive call)

                }

            }

            appendXmlEndTag( stringBuilder ); //Add the end tag

        }

    //NOTE: While it may be tempting to add line separators between the components
    //  so the data will have a nice, structured, indented look when you view it
    //  in a text editor, don't do it. The XML parser will assume the line separator
    //  is the beginning of the character data for the next component. A known sign
    //  of this situation (indeed how it was discovered) is the appearance of an
    //  unexpected '&#xa;' (the XML-flavored '\n') as the first part of the character
    //  data in the following element.

    }/* END getXml( StringBuilder ) */  


    // ==========================================================================
    /**
    * Generates the XML data for this {@link XmlBase} in the single-line format and
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

    }/* END appendXml_OneLine( StringBuilder ) */


    // ==========================================================================
    /**
    * Generates the XML start tag for this object and appends it to the given {@link StringBuilder}.
    * <p>
    * In the XML generated by this method, each attribute represents a {@link Field}
    * of this {@code XmlBase}.
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
        for( String name : theAttributeMap.keySet() ) { //For each attribute name...

            String value = theAttributeMap.get( name ); //Get the value mapped to the name
            if( value != null ) { //If a value was obtained...

                stringBuilder.append( " " ); //Add a space
                stringBuilder.append( name ); //Add the attribute name
                stringBuilder.append( "=\"" ); //Add the equals sign and open quote
                stringBuilder.append( XmlUtilities.encode( value ) ); //Add the value
                stringBuilder.append( "\"" ); //Add the close quote

            } else { //If no value was obtained...
            //The attribute name/value pair was not added (or was removed) from the
            //  attribute HashMap.
            //Error?
            }

        }

        stringBuilder.append( ">" ); //End the start tag

    }/* END appendXmlStartTag( StringBuilder ) */


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

    }/* END appendXmlEndTag( StringBuilder ) */

    // ==========================================================================
    /**
    * Generates the XML start tag for this object and writes it to the given {@link RandomAccessFile}.
    * <p>
    * In the XML generated by this method, each attribute represents a {@link Field}
    * of this {@code XmlBase}.
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
        for( String name : theAttributeMap.keySet() ) { //For each attribute name...

            String value = theAttributeMap.get( name ); //Get the value mapped to the name
            if( value != null ) { //If a value was obtained...

                stringBuilder.append( " " ); //Add a space
                stringBuilder.append( name ); //Add the attribute name
                stringBuilder.append( "=\"" ); //Add the equals sign and open quote
                stringBuilder.append( XmlUtilities.encode( value ) ); //Add the value
                stringBuilder.append( "\"" ); //Add the close quote

            } else { //If no value was obtained...
            //The attribute name/value pair was not added (or was removed) from the
            //  attribute HashMap.
            //Error?
            }

        }

        //Change the start tag to a one-line element...
        if(isOneLiner){
            stringBuilder.append( "/>" ); //Add the one-line terminator "/>"
        } else {
            stringBuilder.append( ">" ); //End the start tag
        }
        passedFile.write(stringBuilder.toString().getBytes("US-ASCII"));

    }/* END writeXmlStartTag( RandomAccessFile ) */


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

    }/* END writeXmlEndTag( RandomAccessFile ) */



    @Override
    // ==========================================================================
    /**
     *  Get the string representation
     */
    public String toString() {
        return getAttribute(ATTRIBUTE_Name);
    }

    // ==========================================================================
    /**
     * 
     */
    public void doPostCreation(){}

    // ==========================================================================
    /**
    * Tells the XmlBase to write its bytes to the passed random access file
    *
    * @param randomAccessFile the {@link RandomAccessFile} to which the XML data is to
    * be written
    *
    * @throws Exception if anything goes wrong
    */
    private void writeXml(RandomAccessFile randomAccessFile) throws IOException{

        if( randomAccessFile == null ) //If the StringBuilder is null...
            throw new IllegalArgumentException( "The StringBuilder cannot be null." );

        List<XmlBase> componentList = getXmlComponents(); //Get the XMLable components
        if(  componentList == null || componentList.isEmpty() )
            writeXmlStartTag( randomAccessFile, true ); //Append the XML start tag
        else { //If there are components or character data...

            //The start tag...
            writeXmlStartTag( randomAccessFile, false ); //Add the start tag

            for( XmlBase anXB : componentList )//For each component...
                if( anXB != null ) //If the component is not null...
                    //Add the XML data for the component...
                    anXB.writeXml( randomAccessFile ); //Add the components XML (recursive call)
           
            writeXmlEndTag( randomAccessFile ); //Add the end tag

        }
    }


}/* END CLASS XmlBase */
