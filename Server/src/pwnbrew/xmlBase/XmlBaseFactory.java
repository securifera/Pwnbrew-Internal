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
 * XmlBaseFactory.java
 *
 * Created on Nov 22, 2013, 7:41:32 PM
 */

package pwnbrew.xmlBase;

import pwnbrew.exception.XmlBaseCreationException;
import pwnbrew.logging.Log;
import java.io.*;
import java.util.logging.Level;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
import pwnbrew.Environment;
import pwnbrew.utilities.FileUtilities;
import pwnbrew.misc.IdGenerator;


/**
 * 
 */
final public class XmlBaseFactory {

    private static final String NAME_Class = XmlBaseFactory.class.getSimpleName();

    // ==========================================================================
    /**
    * Constructor
    */
    private XmlBaseFactory() {
    }


    // ==========================================================================
    /**
    * Creates a deep copy of the given {@link XmlBase} descendent.
    * <p>
    * The clone will contain clones of the original's {@code XmlBase} components.
    *
    * @param <T>
    * @param xmlBase the {@code XmlBase} to clone
    * @return 
    *
    * @throws IllegalArgumentException if the argument is null
    * @throws XmlBaseCreationException if an {@link XmlReader} cannot be created
    *   or if the XML data from the {@code XmlBase} descendent could not be parsed
    */
    public static <T extends XmlBase> T clone( T xmlBase ) throws XmlBaseCreationException {

        if( xmlBase == null )
            throw new IllegalArgumentException( "The XmlBase descendent cannot be null." );
        
        T anXB = (T)createFromXml( xmlBase.getXml() );
        anXB.setId( IdGenerator.next() );
        anXB.doPostCreation();

        return anXB; 

    }

    // ==========================================================================
    /**
    * Creates a new instance of the {@link Class} having the given name.
    * <p>
    * The given {@code String} must be the simple name of the {@code Class}.
    *
    * @param className the simple name of the {@code Class} to be instantiated
    *
    * @return a new instance of the designated {@code Class}; null if no {@code Class}
    * is found for the given name, or the {@link Class} could not be instantiated
    * for some reason
    *
    * @throws IllegalArgumentException if the given {@code String} is null or empty
    * @throws IllegalAccessException if the class or its nullary constructor is not
    *   accessible.
    * @throws InstantiationException if the {@code Class} represents an abstract
    *   class, an interface, an array class, a primitive type, or void; or if the
    *   class has no nullary constructor; or if the instantiation fails for some
    *   other reason.
    */
    static Object instantiateClassByName( String className )
    throws IllegalAccessException, InstantiationException {

        if( className == null ) 
            throw new IllegalArgumentException( "The String cannot be null." );
        else if( className.isEmpty() )
            throw new IllegalArgumentException( "The String cannot be empty." );
        

        if(className.contains(".")){
            int lastDelim = className.lastIndexOf(".");
            className = className.substring(lastDelim + 1, className.length());
        }

        Class aClass = Environment.getClassByName( className ); 
        return instantiateClass(aClass);

    }

    // ==========================================================================
    /**
    * Creates a new instance of the {@link Class} having the given name.
    * <p>
    * The given {@code String} must be the simple name of the {@code Class}.
    *
    * @param aClass
    *
    * @return a new instance of the designated {@code Class}; null if no {@code Class}
    * is found for the given name, or the {@link Class} could not be instantiated
    * for some reason
    *
    * @throws IllegalArgumentException if the given {@code String} is null or empty
    * @throws IllegalAccessException if the class or its nullary constructor is not
    *   accessible.
    * @throws InstantiationException if the {@code Class} represents an abstract
    *   class, an interface, an array class, a primitive type, or void; or if the
    *   class has no nullary constructor; or if the instantiation fails for some
    *   other reason.
    */
    public static Object instantiateClass( Class aClass )
    throws IllegalAccessException, InstantiationException {

        Object rtnObj = null;
        if( aClass != null ) { 

            //Create a new instance of the Class...
            rtnObj = aClass.newInstance();
            ((XmlBase)rtnObj).setId(IdGenerator.next());
        }

        return rtnObj;

    }

    // ==========================================================================
    /**
    * Creates an {@link XmlBase} from the XML data in the given {@code String}.
    *
    * @param headerOnly a flag indicating whether to build the entire {@code XmlBase}
    * and its components or just its header
    * @param xml a {@code String} containing the XML data
    *
    * @return an {@code XmlBase} created from the XML data in the given {@code String}
    *
    * @throws IllegalArgumentException if the given {@code String} is null or empty
    * @throws XmlBaseCreationException if an {@link XmlReader} cannot be created
    *   or if the XML data in the {@code String} could not be parsed
    *
    * @see #createFromXml( boolean, InputSource )
    */
    static XmlBase createFromXml( String xml ) throws XmlBaseCreationException {

        if( xml == null )
            throw new IllegalArgumentException( "The String cannot be null." );
        else if( xml.isEmpty() )
            throw new IllegalArgumentException( "The String cannot be empty." );        

        return createFromXml( new InputSource( new CharArrayReader( xml.toCharArray() ) ) );

    }


    // ==========================================================================
    /**
    * Creates an {@link XmlBase} from the XML data in the file represented by the
    * given {@link File}.
    *
    * @param file a {@code File} representing the file containing the XML data
    *
    * @return an {@code XmlBase} created from the XML data in the file represented
    * by the given {@code File}
    *
    * @throws IllegalArgumentException if the given {@code File} is null
    * @throws XmlBaseCreationException if the file represented by the given {@code File}
    * cannot be read
    */
    public static XmlBase createFromXmlFile( File file ) throws XmlBaseCreationException {

        if( file == null )
            throw new IllegalArgumentException( "The File cannot be null." );
        else if( FileUtilities.verifyCanRead( file ) == false ) 
            throw new XmlBaseCreationException( "Cannot read the file \"" + file.toString() + "\"." );
        
        XmlBase rtnXB = null;

        //Create a FileInputStream for the File...
        FileInputStream aFileInputStream = null;
        try {
            aFileInputStream = new FileInputStream( file );
        } catch( FileNotFoundException ex ) {
            //The call to FileUtilities.verifyCanRead( file ) should obviate this scenario,
            //  but just in case...
            throw new XmlBaseCreationException( "Could not find the file \"" + file.toString() + "\"." );
        }

        try {
            //Create the XmlBase
            rtnXB = createFromXml( new InputSource( aFileInputStream ) );
        } finally {

            try {
                aFileInputStream.close(); //Close the FileInputStream
            } catch( IOException ex ) { 
            }

        }

        if( rtnXB == null )
            throw new XmlBaseCreationException(
        "Could not create the XmlBase from the file \"" + file.toString() + "\"." );
        

        return rtnXB;

    }

    // ==========================================================================
    /**
    * Creates an {@link XmlBase} from the XML data obtained from the given {@link InputSource}.
    *
    * @param inputSource the {@code InputSource}
    *
    * @return an {@link XmlBase} created from the XML data obtained from the given
    * {@link InputSource}
    *
    * @throws XmlBaseCreationException if an {@link XmlReader} cannot be created
    * or if the data from the {@code InputSource} could not be parsed
    */
    private static XmlBase createFromXml( InputSource inputSource )
    throws XmlBaseCreationException {

        XMLReader xmlReader = null;
//        try {
//            xmlReader = XMLReaderFactoryWrapper.createXMLReader();
//        } catch( LoggableException ex ) {
//            throw new XmlBaseCreationException( "Could not create an XMLReader." );
//        }
        
        try {
            xmlReader = XMLReaderFactory.createXMLReader();
//        } catch( LoggableException ex ) {
//            throw new XmlBaseCreationException( "Could not create an XMLReader." );
        } catch (SAXException ex) {
            throw new XmlBaseCreationException( "Could not create an XMLReader." );
        }

        XmlHandler theHandler = new XmlHandler( false );
        xmlReader.setContentHandler( theHandler );

        try { //Try to parse the XML file...
            xmlReader.parse( inputSource );
        } catch( IOException ex ) {
            throw new XmlBaseCreationException(
            "Could not parse the data from the InputSource \"" + inputSource.toString() + "\"." );
        } catch( SAXException ex ) {
            throw new XmlBaseCreationException(
            "Could not parse the data from the InputSource \"" + inputSource.toString() + "\"." );
        } catch(Error er ){

            ///////////////////
            ///////////////////
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            System.out.println(" Error.getClass()            >>" + er.getClass().getSimpleName() + "<<" );
            System.out.println(" Error.toString()            >>" + er.toString() + "<<" );
            System.out.println(" Error.getMessage()          >>" + er.getMessage() + "<<" );
            System.out.println(" Error.getLocalizedMessage() >>" + er.getLocalizedMessage() + "<<" );
            System.out.println(" Error.getCause              >>" + er.getCause() + "<<" );
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            ///////////////////
            ///////////////////

            //The support files were to big for the parser and caused
            //a heap overflow exception
            Log.log(Level.SEVERE, NAME_Class, "createFromXml()", er.getMessage(), er );

            //    } catch(Error heapError){
            //
            //       //The support files were to big for the parser and caused
            //       //a heap overflow exception
            //       Log.log(Level.SEVERE, NAME_Class, "createFromXml()", heapError.getMessage(), heapError );

        }

        //NOTE: The XMLReader's parse method extracts the information stored in the
        //  XML structures and the XmlHandler dictates what to do with that data.
        XmlBase theObject = theHandler.getFinishedXmlBase();

        return theObject;

    }

}
