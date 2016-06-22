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
 * NetworkInterfaceUtilities.java
 *
 * Created on November 24, 2013, 2:12 PM
 */

package pwnbrew.utilities;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;


/**
 */
public class NetworkInterfaceUtilities {

    private static final Map<String, Byte[]> theMacMap= new HashMap<>();
    private static final ReentrantLock theReentrantLock = new ReentrantLock();


    // ==========================================================================
    /**
    * Returns a list of all {@link NetworkInterface}s for the local host.
    * <p>
    * Specifically, the returned list contains all of the
    * {@link NetworkInterface}s in the enumeration returned by
    * {@link NetworkInterface#getNetworkInterfaces()}.
    * 
    * @see NetworkInterface#getNetworkInterfaces()
    * 
    * 
    * @return a list of all {@link NetworkInterface}s for the local host
    * 
    * @throws java.net.SocketException
    */
    private static ArrayList<NetworkInterface> getAllNetworkInterfaces() throws SocketException {

        ArrayList<NetworkInterface> rtnList = new ArrayList<>();
        Enumeration<NetworkInterface> niEnum = null;

        try {
            niEnum = NetworkInterface.getNetworkInterfaces(); //Get an enumeration of the NetworkInterfaces
        } catch( SocketException ex ) {
            throw ex;
        }

        if( niEnum != null ) { //If the NetworkInterface enumeration is not NULL...

            while( niEnum.hasMoreElements() ) { //While there are more NetworkInterfaces...

                NetworkInterface aNI = niEnum.nextElement(); //Get the next NetworkInterface
                if( aNI != null )
                    rtnList.add( aNI );

            }

        }

        return rtnList;

    }

    // ==========================================================================
    /**
    * This wrapper method should returns the same results as would
    * a call like: passedNI.getHardwareAddress() except that it attempts to
    * minimize the actual number of times that the JVM actually calls the
    * method 'getHardwareAddress()'.
    *
    * @param passedNI
    * @return 
    * @throws java.net.SocketException 
    */
    public static byte[] getHardwareAddress_SPECIALIZED( NetworkInterface passedNI )
                          throws SocketException {

        if ( passedNI == null )
            return null;
        
        byte [] rtnBytes= null;

        theReentrantLock .lock();
        try {
            
            String tmpName= passedNI.getName();
            Byte [] lookupResultBytes= theMacMap.get( tmpName );

            if ( lookupResultBytes == null ) {
            
                rtnBytes= passedNI.getHardwareAddress();
                if ( rtnBytes == null )
                    theMacMap.put( tmpName, new Byte[ 0 ] );
                else {
                
                    Byte [] theByteObjList= new Byte[ rtnBytes.length ];
                    for ( int q= 0; q < rtnBytes.length; q++ )
                        theByteObjList[ q ] = rtnBytes[ q ];
                    
                    theMacMap.put( tmpName, theByteObjList );                 
                }

            } else {
         
                if ( lookupResultBytes.length > 0 ) {
                    int theQty= lookupResultBytes.length;
                    rtnBytes= new byte[ theQty ];
                    for ( int q= 0; q < theQty; q++ )
                        rtnBytes[ q ]= lookupResultBytes[ q ];
                }
            }
        
        } finally {
            theReentrantLock .unlock();
        }

        return rtnBytes;
    }

    // ==========================================================================
    /**
    * 
    * @param passedBytes
    * @return
    */
    public static String convertHexBytesToString( byte [] passedBytes ) {
        
        if ( passedBytes == null )
            return null;
        
        StringBuilder strBuf = new StringBuilder();
        int byteInt;
        for ( int i = 0; i < passedBytes.length; i++ ) { //For each byte...

            byteInt = passedBytes[ i ]; //NOTE: this assignment promotes to 32 bits

            byteInt = byteInt << 24; //Shift off any sign bits
            byteInt = byteInt >>> 24; //Shift back, filling with zeros

            //Add the byte as hex to the String
            strBuf.append( byteInt < 16 ? "0" + Integer.toHexString( byteInt ) : Integer.toHexString( byteInt ) );
        }

        return strBuf.toString().toUpperCase();
    }
  
//    // ==========================================================================
//    /**
//    * Returns a list of all {@link NetworkInterface}s for which 
//    * {@link NetworkInterface#isUp()} returns true.
//    * <p>
//    * NOTE: The loopback interface is excluded from the returned list.
//    * 
//    * @return a list of all {@link NetworkInterface}s for which 
//    * {@link NetworkInterface#isUp()} returns true
//    * 
//    * @throws java.net.SocketException
//    */
//    public static ArrayList<NetworkInterface> getAllUpInterfaces() throws SocketException {
//
//        ArrayList<NetworkInterface> rtnList = new ArrayList<>();
//        ArrayList<NetworkInterface> allNIs = getAllNetworkInterfaces(); //Get all of the NetworkInterfaces
//        for (NetworkInterface aNI : allNIs) {
//            //Remove the loopback NetworkInterface...
//            if( aNI.isLoopback() )
//                continue;
//            
//            //Remove any down NetworkInterfaces...
//            if( aNI.isUp() == false )
//                continue; 
//            
//            rtnList.add( aNI );
//        }
//
//        return rtnList;
//
//    }
  
}