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
 * StandardValidation.java
 *
 * Created on June 25, 2013, 7:12:33 PM
 */

package pwnbrew.validation;

import pwnbrew.exception.NoSuchValidationException;
import pwnbrew.log.LoggableException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


/**
 **
 */
public class StandardValidation {

    //IPv4 address regular expressions...
    private static final String REGEX_Ipv4Octet = "25[0-5]|2[0-4]\\d|[01]?\\d?\\d";
    private static final String REGEX_Ipv4Address = "((" + REGEX_Ipv4Octet + ")\\.){3}(" + REGEX_Ipv4Octet + ")";

    //Add port to IPv4
    private static final String REGEX_PORT = "(6553[0-5]|655[0-2]\\d|65[0-4]\\d{2}|6[0-4]\\d{3}|5\\d{4}|[0-9]\\d{0,3})";
    //Special Validation for Client
    private static final String REGEX_Client_Connection = REGEX_Ipv4Address +"(:("+REGEX_PORT+"))";

    
    //IPv6 address regular expression...
    private static final String REGEX_Ipv6Address = "^(((?=(?>.*?::)(?!.*::)))(::)?([0-9A-Fa-f]{1,4}::?){0,5}|([0-9A-Fa-f]{1,4}:){6})(\2([0-9A-Fa-f]{1,4}(::?|$)){0,2}|((25[0-5]|(2[0-4]|1\\d|[1-9])?\\d)(\\.|$)){4}|[0-9A-Fa-f]{1,4}:[0-9A-Fa-f]{1,4})(?<![^:]:|\\.)\\z";
 
    //Hostname regular expression...
    private static final String REGEX_Label = "[\\w&&[^_]][\\w\\-&&[^_]]{0,60}[\\w&&[^_]]";

    //StandardValidation keywords...
    static final String KEYWORD_Hostname = "hostname";
    public static final String KEYWORD_Port = "port";
    public static final String KEYWORD_ClientConnect = "clientconnect";
    private static final String KEYWORD_TTL = "ttl";
    private static final String KEYWORD_IpHostname = "iphostname";
    static final String KEYWORD_IpAddress = "ipaddress";
    public static final String KEYWORD_Ipv4Address = "ipv4address";    
    private static final String KEYWORD_Ipv6Address = "ipv6address";
    public static final String KEYWORD_SubnetMask = "subnetmask";
    private static final String KEYWORD_Ipv4SubnetMask = "ipv4subnetmask";
    private static final String KEYWORD_Ipv6SubnetMask = "ipv6subnetmask";
    static final String KEYWORD_MacAddress = "macaddress";
    private static final String KEYWORD_MacAddress_Colon = "macaddress:";
    private static final String KEYWORD_MacAddress_Hyphen = "macaddress-";
    private static final String KEYWORD_MacAddress_Period = "macaddress.";
    private static final String KEYWORD_MacAddress_Undelimited = "macaddress_";
    //
    private static final List<String> KEYWORD_LIST;
    static {
        List<String> tempList = new ArrayList<>();
        tempList.add( KEYWORD_Hostname );
        tempList.add( KEYWORD_Port );
        tempList.add( KEYWORD_TTL );
        tempList.add( KEYWORD_IpHostname );
        tempList.add( KEYWORD_IpAddress );
        tempList.add( KEYWORD_Ipv4Address );
        tempList.add( KEYWORD_ClientConnect );
        tempList.add( KEYWORD_Ipv6Address );
        tempList.add( KEYWORD_SubnetMask );
        tempList.add( KEYWORD_Ipv4SubnetMask );
        tempList.add( KEYWORD_Ipv6SubnetMask );
        tempList.add( KEYWORD_MacAddress );
        tempList.add( KEYWORD_MacAddress_Colon );
        tempList.add( KEYWORD_MacAddress_Hyphen );
        tempList.add( KEYWORD_MacAddress_Period );
        tempList.add( KEYWORD_MacAddress_Undelimited );
        KEYWORD_LIST = Collections.unmodifiableList( tempList );
    }

    //Linkage of validation keywords to methods...
    private static final HashMap<String, String> theKeywordToMethodNameMap = new HashMap<>();
    static {
        theKeywordToMethodNameMap.put( KEYWORD_Hostname, "validateHostname" );
        theKeywordToMethodNameMap.put( KEYWORD_Port, "validatePort" );
        theKeywordToMethodNameMap.put( KEYWORD_TTL, "validateTTL" );
        theKeywordToMethodNameMap.put( KEYWORD_IpHostname, "validateIpHostname" );
        theKeywordToMethodNameMap.put( KEYWORD_IpAddress, "validateIpAddress" );
        theKeywordToMethodNameMap.put( KEYWORD_Ipv4Address, "validateIpv4Address" );
        theKeywordToMethodNameMap.put( KEYWORD_ClientConnect, "validateClientConnect" );
        theKeywordToMethodNameMap.put( KEYWORD_Ipv6Address, "validateIpv6Address" );
        theKeywordToMethodNameMap.put( KEYWORD_SubnetMask, "validateSubnetMask" );
        theKeywordToMethodNameMap.put( KEYWORD_Ipv4SubnetMask, "validateIpv4SubnetMask" );
        theKeywordToMethodNameMap.put( KEYWORD_Ipv6SubnetMask, "validateIpv6SubnetMask" );
        theKeywordToMethodNameMap.put( KEYWORD_MacAddress, "validateMacAddress" );
        theKeywordToMethodNameMap.put( KEYWORD_MacAddress_Colon, "validateMacAddress_ColonDelimited" );
        theKeywordToMethodNameMap.put( KEYWORD_MacAddress_Hyphen, "validateMacAddress_HyphenDelimited" );
        theKeywordToMethodNameMap.put( KEYWORD_MacAddress_Period, "validateMacAddress_PeriodDelimited" );
        theKeywordToMethodNameMap.put( KEYWORD_MacAddress_Undelimited, "validateMacAddress_Undelimited" );
    }



    // ==========================================================================
    /**
     * Translates the given String to a validation keyword.
     * <p>
     * The process for looking up the appropriate validation method to invoke is
     * sensitive to the cases (upper or lower) of the characters in the validation
     * keywords. This method will translate the given String (assuming it is a variant
     * of one of the validation keywords) into the equivalent keyword that can be
     * used in the method look-up process.
     * <p>
     * If the argument is null this method returns null.
     *
     * @param string the validation keyword variation to translate
     *
     * @return the validation keyword equal to (ignoring case) the given String;
     * null if there is no such keyword
     */
    public static String translateToKeyword( String string ) {

        String rtnStr = null;

        for( String keyword : KEYWORD_LIST ) //For each validation keyword...
            if( keyword.equalsIgnoreCase( string ) ) { //If the given String equals (ignoring case) the keyword...
                rtnStr = keyword; //Return the keyword
                break; //Stop iterating through the keywords
            }

        //NOTE: String.equalsIgnoreCase( String ) will return false if the argument
        //  is null or has a different length.

        return rtnStr;

    }/* END translateToKeyword( String ) */


    // ==========================================================================
    /**
     * Determines if the given value is valid according to the validation identified
     * by the given keyword.
     * <p>
     * If the {@code value} argument is null this method returns false. Null is
     * never valid, even for parameters that have no validation.
     * 
     * @param keyword the keyword identifying the validation to use
     * @param value the value to validate
     *
     * @return {@code true} if the value is valid according to the specified validation;
     * {@code false} otherwise
     *
     * @throws NoSuchValidationException if the given keyword is not a recognized
     * validation keyword
     * @throws pwnbrew.log.LoggableException
     */
    public static boolean validate( String keyword, String value ) throws NoSuchValidationException, LoggableException {

        if( value == null ) //If the "value" String is null...
            return false; //Do nothing / Null is never valid

        boolean rtnBool = false;

        String methodName = theKeywordToMethodNameMap.get( translateToKeyword( keyword ) ); //Get the method name mapped to the keyword
        if( methodName != null ) { //If a method name was obtained...

            //Create a list of the parameter types
            Class[] parameterTypes = new Class[]{ String.class };

            //Get the Method with the name and the list of parameter types...
            Method validationMethod = null;
            try {
              validationMethod = StandardValidation.class.getMethod( methodName, parameterTypes );
            } catch( NoSuchMethodException ex ) {
                //If this case occurs it means that an invalid keyword-to-method-name mapping
                //  was put into the HashMap.
                throw new LoggableException(ex);
            }

            if( validationMethod != null ) { //If a Method was obtained...

                try {
                    rtnBool = (Boolean)validationMethod.invoke( null, value ); //Validate the value
                } catch( IllegalAccessException | InvocationTargetException ex ) {
                    //This case would occur if the method being invoked was inaccessible.
                    //  All of the validation methods should be public and in this class;
                    //  accessibility should not be an issue.
                    throw new LoggableException(ex);
                }

            }

        } else { //If a validation method name was not obtained...
            //There is no validation method for the given keyword.
            throw new NoSuchValidationException( "The String \"" + keyword + "\" is not a recognized validation keyword." );
        }

        return rtnBool;

    }/* END validate( String, String ) */
 // ==========================================================================
    /**
     * Determines if the given value is a valid host name.
     * <p>
     * The determination is made by testing the given value for compliance with the
     * specifications for host names defined in RFCs 952 and 1123.
     * <ul>
     *   <li>The entire hostname (including periods) has a maximum of 255 characters
     *   <li>A fully qualified domain name (fqdn) is a series of labels concatenated with periods
     *   <li>Each label must be BETWEEN 1 and 63 characters; [2-62] characters
     *   <li>Labels can contain only a-z (case-insensitive), 0-9, and '-'(hyphen)
     *   <li>Labels cannot being nor end with '-'
     *   <li>The hostname cannot begin nor end with '.'
     * </ul>
     * (Allowing a trailing '.' as the root is still a topic for discussion.)
     * <p>
     * This method does not tolerate white space.
     * <p>
     * If the argument is null this method returns false.
     *
     * @param hostname the value to be validated
     *
     * @return <tt>true</tt> if the given value is a valid host name; <tt>false</tt>
     * otherwise
     */
    public static boolean validateHostname( String hostname ) {

        if( hostname == null ) //If the String is null...
            return false; //Do nothing

        boolean rtnBool = false;

        if( 1 < hostname.length() && hostname.length() < 256 ) { //If the String has more than 1 and fewer than 256 characters...

            String[] labels = hostname.split( "[.]", -1 ); //Split the hostname into labels
            String label;
            for( int i = 0; i < labels.length; i++ ) { //For each label...

                label = labels[ i ];

                if( label.matches( REGEX_Label ) ) { //If the label is valid...

                    if( i == ( labels.length - 1 ) ) { //If the label is the last...
                        rtnBool = true; //The hostname is valid

                        //NOTE: When the first invalid label is discovered, we break out of
                        //  the for loop so if we find a label to be valid and it is the last
                        //  label the hostname is valid.

                    } //Else, test the next label

                } else { //If the label is not valid...
                  //The String is not a valid host name
                  break; //Stop iterating through the labels...
                }

            }

        } //Else, the String is not a valid host name

        return rtnBool;

    }/* END validateHostname( String ) */


    // ==========================================================================
    /**
     * Determines if the given String is a valid IP address or a valid hostname.
     * <p>
     * If the argument is null this method returns false.
     *
     * @param value the String to test
     *
     * @return {@code true} if the given String is a valid IP address or hostname;
     * {@code false} otherwise
     */
    public static boolean validateIpHostname( String value ) {

        if( value == null ) //If the String is null...
            return false; //Do nothing

        boolean rtnBool;

        if( validateHostname( value ) ) //If the String is a valid hostname...
            rtnBool = true; //The String is valid
        else //If the String is not a valid hostname...
            rtnBool = validateIpAddress( value ); //Determine if the String is a valid IP address

        return rtnBool;

    }/* END validateIpHostname( String ) */

    // ==========================================================================
    /**
     * Determines if the given String is a valid port.
     * <p>
     * If the argument is null this method returns false.
     *
     * @param value the String to test
     *
     * @return {@code true} if the given String is a valid port;
     * {@code false} otherwise
     */
    public static boolean validatePort( String value ) {

        if( value == null ) //If the String is null...
            return false; //Do nothing

        boolean rtnBool = false;

        try {
           int intVal = Integer.parseInt(value);
           if(intVal >= 0 && intVal <= 65535){
              rtnBool = true;
           }
        } catch (NumberFormatException ex){
           return false;
        }
        return rtnBool;

    }/* END validatePort( String ) */


    // ==========================================================================
    /**
     * Determines if the given String is a valid ttl packet value.
     * <p>
     * If the argument is null this method returns false.
     *
     * @param value the String to test
     *
     * @return {@code true} if the given String is a valid ttl
     */
    public static boolean validateTTL( String value ) {

        if( value == null ) //If the String is null...
            return false; //Do nothing

        boolean rtnBool = false;

        try {
           int intVal = Integer.parseInt(value);
           if(intVal >= 0 && intVal <= 255){
              rtnBool = true;
           }
        } catch (NumberFormatException ex){
           return false;
        }
        return rtnBool;

    }/* END validateTTL( String ) */
    
    // ==========================================================================
    /**
     * Determines if the given String is a valid IP v4 or v6 address.
     * <p>
     * If the argument is null this method returns false.
     *
     * @param address the String to test
     *
     * @return {@code true} if the given String is a valid IP v4 or v6 address;
     * {@code false} otherwise
     */
    public static boolean validateIpAddress( String address ) {

        if( address == null ) //If the String is null...
            return false; //Do nothing

        boolean rtnBool;

        if( validateIpv4Address( address ) ) //If the String is a valid IPv4 address...
            rtnBool = true; //The String is valid
        else //If the String is not a valid IPv4 address...
            rtnBool = validateIpv6Address( address ); //Determine if the String is a valid IPv6 address

        return rtnBool;

    }/* END validateIpAddress( String ) */

     // ==========================================================================
    /**
     * Determines if the given String is a valid IPv4 address in the dotted-decimal
     * notation. (ie. "172.16.254.1:80:80")
     * <p>
     * If the argument is null this method returns false.
     *
     * @param address the String to test
     *
     * @return {@code true} if the given String is a valid IPv4 address in the dotted-decimal
     * notation; {@code false} otherwise
     */
    public static boolean validateClientConnect( String address ) {

        if( address == null ) //If the String is null...
            return false; //Do nothing

        return address.matches( REGEX_Client_Connection ); //Determine if the String is a IPv4 address

    }/* END validateClientConnect( String ) */

    // ==========================================================================
    /**
     * Determines if the given String is a valid IPv4 address in the dotted-decimal
     * notation. (ie. "172.16.254.1")
     * <p>
     * If the argument is null this method returns false.
     *
     * @param address the String to test
     *
     * @return {@code true} if the given String is a valid IPv4 address in the dotted-decimal
     * notation; {@code false} otherwise
     */
    public static boolean validateIpv4Address( String address ) {

        if( address == null ) //If the String is null...
            return false; //Do nothing

        return address.matches( REGEX_Ipv4Address ); //Determine if the String is a IPv4 address

    }/* END validateIpv4Address( String ) */
    
     // ==========================================================================
    /**
     * Determines if the given String is a valid IPv4 address in the dotted-decimal
     * notation. (ie. "172.16.254.1")
     * <p>
     * If the argument is null this method returns false.
     *
     * @param address the String to test
     *
     * @return {@code true} if the given String is a valid IPv4 address in the dotted-decimal
     * notation; {@code false} otherwise
     */
    public static boolean validateClientMigrate( String address ) {

        if( address == null ) //If the String is null...
            return false; //Do nothing

        return address.matches( REGEX_Ipv4Address ); //Determine if the String is a IPv4 address

    }/* END validateIpv4Address( String ) */


    // ==========================================================================
    /**
     * Determines if the given String is a valid IPv6 address.
     * <p>
     * If the argument is null this method returns false.
     *
     * @param address the String to test
     *
     * @return {@code true} if the given String is a valid IPv6 address; {@code false}
     * otherwise
     */
    public static boolean validateIpv6Address( String address ) {

        if( address == null ) //If the String is null...
            return false; //Do nothing

        return address.matches( REGEX_Ipv6Address ); //Determine if the String is a IPv6 address

    }/* END validateIpv6Address( String ) */

}/* END CLASS StandardValidation */
