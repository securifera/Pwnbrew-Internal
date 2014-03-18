///*
//
//Copyright (C) 2013-2014, Securifera, Inc 
//
//All rights reserved. 
//
//Redistribution and use in source and binary forms, with or without modification, 
//are permitted provided that the following conditions are met:
//
//    * Redistributions of source code must retain the above copyright notice,
//	this list of conditions and the following disclaimer.
//
//    * Redistributions in binary form must reproduce the above copyright notice,
//	this list of conditions and the following disclaimer in the documentation 
//	and/or other materials provided with the distribution.
//
//    * Neither the name of Securifera, Inc nor the names of its contributors may be 
//	used to endorse or promote products derived from this software without specific
//	prior written permission.
//
//THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS 
//OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY 
//AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER 
//OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
//CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
//SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON 
//ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
//(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
//EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
//================================================================================
//
//Pwnbrew is provided under the 3-clause BSD license above.
//
//The copyright on this package is held by Securifera, Inc
//
//*/
//
//
///*
//* XMLReaderFactoryWrapper.java
//*
//* Created on June 9, 2013, 11:11:32 PM
//*/
//
//package pwnbrew.xmlBase;
//
//import pwnbrew.logging.LoggableException;
//import java.lang.reflect.InvocationTargetException;
//import org.apache.xerces.parsers.XML11Configuration;
//import org.apache.xerces.xni.parser.XMLComponent;
//import org.xml.sax.SAXException;
//import org.xml.sax.XMLReader;
//import org.xml.sax.helpers2.XMLReaderFactory;
//import pwnbrew.utilities.ReflectionUtilities;
//
///**
// *
// */
//@SuppressWarnings("ucd")
//public class XMLReaderFactoryWrapper {    
//
//    //=========================================================================
//    /**
//     * This function wraps the Xerces XmlReaderFactory function so we
//     * can add our own custom scanner and make the necessary reflexion calls.
//     *
//     * @see #XMLReaderFactory.createXMLReader()
//    */
//    static XMLReader createXMLReader () throws LoggableException {
//
//        XmlCustomScanner theCustomScanner = new XmlCustomScanner();
//        XMLReader xmlReader = null;
//
//        try {
//
//            xmlReader = XMLReaderFactory.createXMLReader();
//            Object anObj = ReflectionUtilities.getValue(xmlReader, "fConfiguration");
//
//            //Use reflection to get the scanner from the configuration
//            if(anObj instanceof XML11Configuration){
//                XML11Configuration theConf = (XML11Configuration)anObj;
//
//                //Retrieve the field
//                ReflectionUtilities.setValue(theConf, "fNamespaceScanner", theCustomScanner);
//
//                //Add the new scanner to the component list
//                ReflectionUtilities.invokeMethod(theConf, "addComponent",  new Class[] { XMLComponent.class }, new Object[]{(XMLComponent)theCustomScanner});
//
//            } else {
//                throw new SAXException("Unable to find 'fNamespaceScanner' declared field.");
//            }
//
//            xmlReader.setProperty("http://apache.org/xml/properties/internal/document-scanner", theCustomScanner);
//            xmlReader.setFeature( "http://xml.org/sax/features/namespace-prefixes", true );
//
//        } catch (InvocationTargetException | IllegalArgumentException | IllegalAccessException | SAXException ex) {
//            throw new LoggableException(ex);
//        }
//
//        return xmlReader;
//    }
//
//}/* END CLASS XMLReaderFactoryWrapper */
