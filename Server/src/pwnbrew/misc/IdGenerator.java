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
 * IdGenerator.java
 *
 * Created on June 25, 2013, 6:42:21 PM
 */

package pwnbrew.misc;

import pwnbrew.utilities.Utilities;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * 
 */
final public class IdGenerator {

    

    //Session's start date/time...
    private static final String FORMAT_SessionDateTime
            = new StringBuilder( "yyyyMMdd" ).append( "_" ).append( "HHmmss" ).toString();

    private static final String SessionDateTime;
    static {
        //Create the session's date/time String...
        SessionDateTime =
                new SimpleDateFormat( FORMAT_SessionDateTime ).format( new Date() );
    }

    //Random number...
    private static final String BASE_RandomNumber = "000000";
    private static final int LIMIT_Random = (int)Math.pow( 10, BASE_RandomNumber.length() );
    //NOTE: The 6 digits accommodate 1,000,000 (one million) values.

    //Sequence number...
    private static final String BASE_SequenceNumber = "000000000000";
    private static final long LIMIT_Sequence = (long)Math.pow( 10, BASE_SequenceNumber.length() );
    private static long theCurrentSequenceNumber = 0;
    //NOTE: The 12 digits accommodate 1,000,000,000,000 (one trillion) values.
    //NOTE: With 12 digits, you could increment the sequence 100 times every second
    //  for more than 316 years before reaching the limit.

    private static String tempStr;


    // ==========================================================================
    /**
     * Prevents instantiation of {@link IdGenerator} outside of itself.
     */
    private IdGenerator() {
    }/* END CONSTRUCTOR() */


    // ==========================================================================
    /**
     * Generates a random number.
     * <p>
     * The generated number is prepended with zeros to the length of the base.
     *
     * @return a {@code String} containing a pseudo-randomly generated number.
     */
    private static String getNextRandom() {

        //Generate a random number within the limit and convert it to a String
        tempStr = "" + Utilities.SecureRandomGen.nextInt( LIMIT_Random );

        //Prepend the number with zeros and return it
        return ( BASE_RandomNumber.substring( tempStr.length() ) + tempStr );

    }/* END getNextRandom() */


    // ==========================================================================
    /**
     * Generates the next sequence number.
     * <p>
     * The generated number is prepended with zeros to the length of the base.
     *
     * @return a {@code String} containing the next sequence number.
     */
    private static String getNextSequenceNumber() {

        theCurrentSequenceNumber++; //Increment to the next sequence number
        if( theCurrentSequenceNumber == LIMIT_Sequence ) { //If the number has reached the limit...
            theCurrentSequenceNumber = 0; //Reset the sequence number
        }
        tempStr = "" + theCurrentSequenceNumber; //Convert the sequence number to a String

        //Prepend the number with zeros and return it
        return ( BASE_SequenceNumber.substring( tempStr.length() ) + tempStr );

    }/* END getNextSequenceNumber() */


    // ==========================================================================
    /**
     * Generates and returns the next id.
     *
     * @return the next id
     */
    public static synchronized String next() {

        // Date     Time   Random Sequence
        // XXXXXXXX_XXXXXX_XXXXXX_XXXXXXXXXXXX

        StringBuilder sb = new StringBuilder( SessionDateTime )
                .append( "_" ).append( getNextRandom() )
                .append( "_" ).append( getNextSequenceNumber() );
        
        return sb.toString();

    }/* END next() */

}/* END CLASS IdGenerator */
