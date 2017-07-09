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
 * XmlHandler.java
 *
 * Created on June 21, 2013, 9:21:32 PM
 */

package pwnbrew.xml;

import pwnbrew.exception.XmlObjectCreationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import pwnbrew.log.Log;
import pwnbrew.log.LoggableException;


/**
 * 
 */

@SuppressWarnings("ucd")
public class XmlHandler extends DefaultHandler {

    private static final String NAME_Class = XmlHandler.class.getSimpleName();

    private boolean skipTheElement = false;

    private ArrayList<XmlObject> theObjectStack = new ArrayList<>();

    private XmlObject theXmlObject = null;

    private String errorMessage = null;
    private Locator theLocator = null;


    // ==========================================================================
    /**
    * Constructor
    */
    public XmlHandler() {
    }


    // ==========================================================================
    /**
    * Creates a new instance of {@link XmlHandler}.
    */
    XmlHandler( boolean headerOnly ) {
    }


    // ==========================================================================
    /**
    * Returns the finished {@link XmlObject}.
    *
    * @return the finished {@code XmlObject}
    */
    public XmlObject getFinishedXmlObject() {

        XmlObject rtnXB = null;
        if( theObjectStack.isEmpty() == false ) 
            rtnXB = theObjectStack.get( 0 );        

        theObjectStack = null; //Null the reference
        return rtnXB;

    }


    // ==========================================================================
    /**
    *
    * @param uri
    * @param atts
    * @param localName
    * @param qName
    */
    @Override
    public void startElement( String uri, String localName, String qName, Attributes atts ) {

        try {

            try {
                theXmlObject = (XmlObject) XmlObjectFactory.instantiateClassByName( localName ); //Get a new instance of the appropriate Class
            } catch (IllegalAccessException | InstantiationException ex) {
                throw new LoggableException(ex);
            }

        } catch( LoggableException ex ) {

            StringBuilder strBld = new StringBuilder( "Could not instantiate the class \"" );
            strBld.append( localName ).append( "\"." );
            if( theObjectStack.isEmpty() == false ) { //If there are no objects on the stack...

                strBld.append( "\n" );
                strBld.append( "  The object stack:" );
                String id;
                for( XmlObject xb : theObjectStack ) { //For each XmlObject on the stack...

                    strBld.append( "\n" );
                    strBld.append( "    Class: \"" ).append( xb.getClass().getSimpleName() ).append( "  Id: " );
                    id = xb.getId(); //Get the XmlObject's id
                    if( id.isEmpty() ) //If the id is an empty String...
                        strBld.append( "<empty>" );
                    else //If the id is not an empty String...
                        strBld.append( "\"" ).append( id ).append( "\"" );

                }

            }

        Log.log( Level.WARNING, NAME_Class, "startElement()", strBld.toString(), null );
        }

        if( theXmlObject == null )
            throw new XmlObjectCreationException("Invalid XmlObject class '" + localName + "'");
        else if( theXmlObject instanceof FileContent){
            skipTheElement = true;
            return;
        }

        //Populate the object's data fields...
        for( int i = 0; i < atts.getLength(); i++ ) { //For each attribute...
            theXmlObject.setProperty( atts.getQName( i ), atts.getValue( i ) ); //Set the XmlObject's attribute
        }

        theObjectStack.add( theXmlObject ); 
        theXmlObject = null;

    }


    // ==========================================================================
    /**
    *
    * @param uri
    * @param qName
    * @param localName
    */
    @Override
    public void endElement( String uri, String localName, String qName ) {

        if( skipTheElement == false ) { //If the element is not being skipped...

            int stackSize = theObjectStack.size(); //Get the number of objects on the stack

            if( stackSize > 1 ) { //If there are at least two objects on the stack...
                //Remove (pop) the last object from the stack and add it as a component to the next-to-last object
                ( theObjectStack.get( stackSize - 2 ) ).addChildObject( theObjectStack.remove( stackSize - 1 ) );
            } else if( stackSize == 1 ) { //If there is only one object on the stack...
                theObjectStack.trimToSize(); //Remove any unused space
            } else { //If there are no objects on the stack...

            ///////////////////////
            // Possible???
            ///////////////////////

            }

        } else
            skipTheElement = false;
        

    }


    // ==========================================================================
    /**
    *
    * @return 
    */
    public String getErrorMessage() {
        return errorMessage;
    }


    // ==========================================================================
    /**
    *
    * @param name
    */
    @Override
    public void skippedEntity( String name ) {
    }

    // ==========================================================================
    /**
    *
    * @param target
    * @param data
    */
    @Override
    public void processingInstruction( String target, String data ) {
    }

    // ==========================================================================
    /**
    *
    * @param ch
    * @param length
    * @param start
    */
    @Override
    public void ignorableWhitespace( char[] ch, int start, int length ) {
    }


    // ==========================================================================
    /**
    *
    * @param prefix
    */
    @Override
    public void endPrefixMapping( String prefix )  {
    }


    // ==========================================================================
    /**
    *
    * @param prefix
    * @param uri
    */
    @Override
    public void startPrefixMapping( String prefix, String uri ) {
    }


    // ==========================================================================
    /**
    *
    */
    @Override
    public void endDocument() {
    }

    // ==========================================================================
    /**
    *
    */
    @Override
    public void startDocument() {
    }


    // ==========================================================================
    /**
    *
    * @return 
    */
    public Locator getDocumentLocator() {
        return theLocator;
    }

    // ==========================================================================
    /**
    *
    * @param locator
    */
    @Override
    public void setDocumentLocator( Locator locator ) {
        theLocator = locator;
    }


    /**
    * Report a fatal XML parsing error.
    *
    * <p>The default implementation throws a SAXParseException.
    * Application writers may override this method in a subclass if
    * they need to take specific actions for each fatal error (such as
    * collecting all of the errors into a single report): in any case,
    * the application must stop all regular processing when this
    * method is invoked, since the document is no longer reliable, and
    * the parser may no longer report parsing events.</p>
    *
    * @param e The error information encoded as an exception.
    * @exception org.xml.sax.SAXException Any SAX exception, possibly
    *            wrapping another exception.
    * @see org.xml.sax.ErrorHandler#fatalError
    * @see org.xml.sax.SAXParseException
    */
    @Override
    public void fatalError (SAXParseException e) throws SAXException {
        errorMessage = e.getMessage();
        throw e;
    }

    /**
    * Receive notification of a recoverable parser error.
    *
    * <p>The default implementation does nothing.  Application writers
    * may override this method in a subclass to take specific actions
    * for each error, such as inserting the message in a log file or
    * printing it to the console.</p>
    *
    * @param e The error information encoded as an exception.
    * @exception org.xml.sax.SAXException Any SAX exception, possibly
    *            wrapping another exception.
    * @see org.xml.sax.ErrorHandler#warning
    * @see org.xml.sax.SAXParseException
    */
    @Override
    public void error (SAXParseException e) throws SAXException {
        errorMessage = e.getMessage();
    }

   
    /**
    * Resolve an external entity.
    *
    * <p>Always return null, so that the parser will use the system
    * identifier provided in the XML document.  This method implements
    * the SAX default behaviour: application writers can override it
    * in a subclass to do special translations such as catalog lookups
    * or URI redirection.</p>
    *
    * @param publicId The public identifier, or null if none is
    *                 available.
    * @param systemId The system identifier provided in the XML
    *                 document.
    * @return The new input source, or null to require the
    *         default behaviour.
    * @exception java.io.IOException If there is an error setting
    *            up the new input source.
    * @exception org.xml.sax.SAXException Any SAX exception, possibly
    *            wrapping another exception.
    * @see org.xml.sax.EntityResolver#resolveEntity
    */
    @Override
    public InputSource resolveEntity (String publicId, String systemId) throws IOException, SAXException {
        return null;
    }


}
