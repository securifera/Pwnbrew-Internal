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
 * SubnetMaskTable_IPv4.java
 *
 * This class manages a list of all IPv4 subnet masks.
 *
 * Created on June 13, 2013, 11:32:36 PM
 */

package pwnbrew.misc;

import java.util.ArrayList;
import java.util.List;

/**
 *  
 */
abstract public class SubnetMaskTable_IPv4 {

    private final static ArrayList<String> theSubnetMaskList = new ArrayList<>();

    static {

        theSubnetMaskList.add( "0.0.0.0" );        //0        
        theSubnetMaskList.add( "128.0.0.0" );        //1
        theSubnetMaskList.add( "192.0.0.0" );        //2
        theSubnetMaskList.add( "224.0.0.0" );        //3
        theSubnetMaskList.add( "240.0.0.0" );        //4
        theSubnetMaskList.add( "248.0.0.0" );        //5
        theSubnetMaskList.add( "252.0.0.0" );        //6
        theSubnetMaskList.add( "254.0.0.0" );        //7
        theSubnetMaskList.add( "255.0.0.0" );        //8

        theSubnetMaskList.add( "255.128.0.0" );      //9
        theSubnetMaskList.add( "255.192.0.0" );      //10
        theSubnetMaskList.add( "255.224.0.0" );      //11
        theSubnetMaskList.add( "255.240.0.0" );      //12
        theSubnetMaskList.add( "255.248.0.0" );      //13
        theSubnetMaskList.add( "255.252.0.0" );      //14
        theSubnetMaskList.add( "255.254.0.0" );      //15
        theSubnetMaskList.add( "255.255.0.0" );      //16

        theSubnetMaskList.add( "255.255.128.0" );    //17
        theSubnetMaskList.add( "255.255.192.0" );    //18
        theSubnetMaskList.add( "255.255.224.0" );    //19
        theSubnetMaskList.add( "255.255.240.0" );    //20
        theSubnetMaskList.add( "255.255.248.0" );    //21
        theSubnetMaskList.add( "255.255.252.0" );    //22
        theSubnetMaskList.add( "255.255.254.0" );    //23
        theSubnetMaskList.add( "255.255.255.0" );    //24

        theSubnetMaskList.add( "255.255.255.128" );  //25
        theSubnetMaskList.add( "255.255.255.192" );  //26
        theSubnetMaskList.add( "255.255.255.224" );  //27
        theSubnetMaskList.add( "255.255.255.240" );  //28
        theSubnetMaskList.add( "255.255.255.248" );  //29
        theSubnetMaskList.add( "255.255.255.252" );  //30
        theSubnetMaskList.add( "255.255.255.254" );  //31
        theSubnetMaskList.add( "255.255.255.255" );  //32

    }


    // ==========================================================================
    /**
    * Gets the octet notation associated with the given integer subnet mask.
    *
    * @param passedMask the subnet mask in integer notation
    * 
    * @return the corresponding octet notation or NULL if the arguement is
    * invalid
    */
    public static String get( int passedMask ) {

        if( passedMask < 0 || 32 < passedMask ) { //If the given int is not a mask...
            return null; //...the arguement is invalid
        }

        return theSubnetMaskList.get( passedMask ); //Return the mask's octet notation

    }/* END get( int ) */


    


    // ==========================================================================
    /**
    * Produces a list of all IPv4 subnet masks.
    *
    * @return an <tt>ArrayList</tt> such that the index [0 - 32] is the subnet
    * mask's integer notation and the element's are <tt>String</tt>s containing
    * the mask's corresponding octet notation
    */
    public static ArrayList<String> getList() {

        ArrayList<String> rtnList = new ArrayList<>();
        rtnList.addAll( theSubnetMaskList );
        return rtnList;

    }/* END getList() */

    // ==========================================================================
    /**
    *
     * @param mask
     * @return 
    */
    public static boolean isSubnetMask( String mask ) {
        return ( -1 < theSubnetMaskList.indexOf( mask ) );
    }/* END isSubnetMask( String ) */


    // ==========================================================================
    /**
    * Returns a {@code String[]} of all IPv4 subnet masks in octet notation.
    *
    * @return a {@code String[]} of all IPv4 subnet masks in octet notation
    */
    public static String[] getArray() {
        List<String> enumValues = getList(); //Get the list of subnet masks
        return enumValues.toArray( new String[ enumValues.size() ] );
    }/* END getList() */

}
