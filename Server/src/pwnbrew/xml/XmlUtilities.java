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
 * XmlUtilities.java
 *
 * Created on Sept 11, 2013, 8:21:29 PM
 */

package pwnbrew.xml;

import java.util.regex.Pattern;

/**
 * 
 */
@SuppressWarnings("ucd")
public class XmlUtilities {
  
    /** XML encoding of the "ampersand" character. */
    private static final String ENCODING_Ampersand = "&#38;";

    /** Regular expression recognizing XML-escaped "ampersand" characters. */
    private static final Pattern PATTERN_Ampersand = Pattern.compile( "&(?:amp|#0*+38|#x0*+26);" );

    /** XML encoding of the "apostrophe" character. */
    private static final String ENCODING_Apostrophe = "&#39;";

    /** Regular expression recognizing XML-escaped "apostrophe" characters. */
    private static final Pattern PATTERN_Apostrophe = Pattern.compile( "&(?:apos|#0*+39|#x0*+27);" );

    /** XML encoding of the "carriage return" character. */
    private static final String ENCODING_CarriageReturn = "&#xd;";

    /** Regular expression recognizing XML-escaped "carriage return" characters. */
    private static final Pattern PATTERN_CarriageReturn= Pattern.compile( ENCODING_CarriageReturn );
  
    /** XML encoding of the "delete" character. */
    private static final String ENCODING_Delete = "&#x7f;";

    /** Regular expression recognizing XML-escaped "delete" characters. */
    private static final Pattern PATTERN_Delete = Pattern.compile( ENCODING_Delete );
    
    /** XML encoding of the "greater than" character. */
    private static final String ENCODING_GreaterThan = "&#62;";

    /** Regular expression recognizing XML-escaped "greater than" characters. */
    private static final Pattern PATTERN_GreaterThan = Pattern.compile( "&(?:gt|#0*+62|#x0*+3[eE]);" );
  
    /** XML encoding of the "less than" character. */
    private static final String ENCODING_LessThan = "&#60;";

    /** Regular expression recognizing XML-escaped "less than" characters. */
    private static final Pattern PATTERN_LessThan = Pattern.compile( "&(?:lt|#0*+60|#x0*+3[cC]);" );

    /** XML encoding of the "new line" character. */
    private static final String ENCODING_NewLine = "&#xa;";

    /** Regular expression recognizing XML-escaped "new line" characters. */
    private static final Pattern PATTERN_NewLine = Pattern.compile( ENCODING_NewLine );
  
    /** XML encoding of the "quote" character. */
    private static final String ENCODING_Quote = "&#34;";

    /** Regular expression recognizing XML-escaped "quote" characters. */
    private static final Pattern PATTERN_Quote = Pattern.compile( "&(?:quot|#0*+34|#x0*+22);" );

    /** XML encoding of the "tab" character. */
    private static final String ENCODING_Tab = "&#9;";

    /** Regular expression recognizing XML-escaped "tab" characters. */
    private static final Pattern PATTERN_Tab = Pattern.compile( ENCODING_Tab );

    // ========================================================================
    /**
    * Replaces the XML representations of special characters with the characters.
    *
    * @param object the data to be decoded
    * 
    * @return the decoded data
    */
    static String decode( Object object ) {

        if( object == null ) return null;

        String str = String.valueOf( object );

        str = PATTERN_Ampersand.matcher( str ).replaceAll( "&" );
        str = PATTERN_Apostrophe.matcher( str ).replaceAll( "'" );
        str = PATTERN_CarriageReturn.matcher( str ).replaceAll( "\r" );
        str = PATTERN_Delete.matcher( str ).replaceAll( "\u007f" );
        str = PATTERN_GreaterThan.matcher( str ).replaceAll( ">" );
        str = PATTERN_LessThan.matcher( str ).replaceAll( "<" );
        str = PATTERN_NewLine.matcher( str ).replaceAll( "\n" );
        str = PATTERN_Quote.matcher( str ).replaceAll( "\"" );
        str = PATTERN_Tab.matcher( str ).replaceAll( "\t" );

        return str;

    }/* END decode( Object ) */


    // ========================================================================
    /**
    * Replaces the special characters with their XML-safe representations.
    * 
    * @param content the data to be encoded
    * 
    * @return the encoded data
    */
    @SuppressWarnings("ucd")
    public static String encode( Object content ) {

        if( content == null ) return null;

        String str = String.valueOf( content );

        str = str.replaceAll( "&", ENCODING_Ampersand );
        str = str.replaceAll( "'", ENCODING_Apostrophe );
        str = str.replaceAll( "\r", ENCODING_CarriageReturn );
        str = str.replaceAll( "\u007f", ENCODING_Delete );
        str = str.replaceAll( ">", ENCODING_GreaterThan );
        str = str.replaceAll( "<", ENCODING_LessThan );
        str = str.replaceAll( "\n", ENCODING_NewLine );
        str = str.replaceAll( "\"", ENCODING_Quote );
        str = str.replaceAll( "\t", ENCODING_Tab );

        return str;

    }

}
