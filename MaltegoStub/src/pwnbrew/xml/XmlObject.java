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
 */

package pwnbrew.xml;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import pwnbrew.xml.maltego.Entity;
 

/**
 *
 */
abstract public class XmlObject {

    protected final Map<String, String> theAttributeMap = new LinkedHashMap<>();
    private String theXmlObjectContent = "";
 
    // ==========================================================================
    /**
    * Constructor 
    */
    public XmlObject() {
    }
    
    // ==========================================================================
    /**
    * Constructor 
     * @param passedContent
    */
    public XmlObject( String passedContent ) {
        theXmlObjectContent = passedContent;
    }

    // ==========================================================================
    /**
     * 
     * @return 
     */
    public String getXmlObjectContent() {
        return theXmlObjectContent;
    }

    // ==========================================================================
    /**
     * 
     * @param passedStr 
     */
    public void setXmlObjectContent( String passedStr ) {
        theXmlObjectContent = passedStr;
    }

    // ==========================================================================
    /**
    * Sets the attribute with the given name to the given value.
    *
    * @param name the name of the attribute to set
    * @param value the value to give the attribute
    */
    public synchronized void setAttribute( String name, String value ) {

        //NOTE: The condition below ensures this method will not add attributes.
        if( theAttributeMap.get( name ) != null ) //If the key is already in the map...
            theAttributeMap.put( name, ( value != null ? value : "" ) ); //Set the value of the attribute

    }


    // ==========================================================================
    /**
    * Returns the value of the attribute with the given name.
    * <p>
    * If the given String is null, this method returns null.
    *
    * @param name the name of the attribute
    *
    * @return the value of the attribute with the given name, null if the {@link XmlObject}
    * has no such attribute
    */
    public final synchronized String getAttribute( String name ) {
        return theAttributeMap.get( name );
    }


    // ==========================================================================
    /**
    * Returns a list of the {@link XmlObject}'s components.
    * <p>
    * The list this method returns is to be defined by subclasses. Generally, the
    * first line in the subclasses' implementations of this method should be a call
    * to "super.getXmlComponents()" so that each Class need be concerned with adding
    * only the components it defines.
    * 
     * @param <T>
    * @return an empty {@code ArrayList<XmlObject>}
    */
    public <T extends XmlObject> List<T> getXmlComponents(){
        return new ArrayList<>();
    };


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

    }/* END getXml() */


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

        List<XmlObject> componentList = getXmlComponents(); //Get the XMLable components
        if(  (componentList == null || componentList.isEmpty()) && (theXmlObjectContent == null || theXmlObjectContent.isEmpty()) ) {

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

            //Add the xml content
            if( !theXmlObjectContent.isEmpty())
                stringBuilder.append(theXmlObjectContent);
              
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
    * Generates the XML data for this {@link XmlObject} in the single-line format and
    * appends it to the given {@code StringBuilder}.
    *
    * @param stringBuilder the {@link StringBuilder} to which the XML data is to
    * be appended
    *
    * @throws IllegalArgumentException if the argument is null
    */
    private void appendXml_OneLine( StringBuilder stringBuilder ) {

        if( stringBuilder == null )
            throw new IllegalArgumentException( "The StringBuilder cannot be null." );
        
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

        if( stringBuilder == null )
            throw new IllegalArgumentException( "The StringBuilder cannot be null." );
        
        //Get the class
        Class aClass = getClass();
        String className = aClass.getSimpleName();
        
        //Determine if the class is an Entity
        Class parentClass = aClass.getSuperclass();
        if( parentClass != null ){
            while( parentClass != Object.class){
                if( parentClass == Entity.class ){
                    className = Entity.class.getSimpleName();
                    break;
                } else {
                    parentClass = parentClass.getSuperclass();
                }
            }
        }
        
        //Begin the start tag with the class name...
        stringBuilder.append( "<" ).append( className );

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

        if( stringBuilder == null )
            throw new IllegalArgumentException( "The StringBuilder cannot be null." );
        
         //Get the class
        Class aClass = getClass();
        String className = aClass.getSimpleName();
        
        //Determine if the class is an Entity
        Class parentClass = aClass.getSuperclass();
        if( parentClass != null ){
            while( parentClass != Object.class){
                if( parentClass == Entity.class ){
                    className = Entity.class.getSimpleName();
                    break;
                } else {
                    parentClass = parentClass.getSuperclass();
                }
            }
        }
        
        stringBuilder.append( "</" ).append( className ).append( ">" );

    }


}
