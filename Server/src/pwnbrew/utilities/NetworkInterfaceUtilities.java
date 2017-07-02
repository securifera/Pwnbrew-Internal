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
 * Created on October 13, 2013, 11:32 PM
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

  private static final Map<String, Byte[]> theGetHardwareAddress_SPECIALIZED_Hashtable= new HashMap<String, Byte[]>();
  private static final ReentrantLock lockFor_GetHardwareAddress_SPECIALIZED= new ReentrantLock();

  
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
        
        if( aNI != null ) { //If the NetworkInterface is not NULL...
          rtnList.add( aNI ); //Add the NetworkInterface to the list
        }
        
      }
           
    }
    
    return rtnList;
    
  }/* END getAllNetworkInterfaces() */
  
  
  // ==========================================================================
  /**
   * This wrapper method should returns the same results as would
   * a call like: passedNI.getHardwareAddress() except that it attempts to
   * minimize the actual number of times that the JVM actually calls the
   * method 'getHardwareAddress()'.
   *
   * It does this by keeping a cache of NetworkInterface.getName() as a <KEY>
   * and keeps an object version of the byte[] data.
   *
   * Motive_ONE- We have observed that our Unix(Solaris_8) version of the JVM runtime
   * would actually open a file for each call to getHardwareAddress() and
   * did not ever close/delete/unlink that (temp) file, while the process was running.
   *
   * Now combine this odd (?bad) behavour with another Unix issue: the /etc/services
   * file has specified that
   *
   * Motive_TWO- I have just discovered that when a test Win_XP box has two USB-Cat5
   * adapters added and removed (in various configurations), it (the JVM) changes
   * the value returned by aNI.getName().
   *
   * eg. plug in first USB-Cat5, call its getName()... get "eth2". Plug in second
   * USB-Cat5, call its getName()... get "eth3". THEN unplug the first USB-Cat5
   * and re-call its getName() this time the JVM returns a suprising "eth2"  !!!
   * (..which was previously the String for the FIRST USB-Cat5, that was unplugged!!)
   *
   * So, now there is special-special handling being added.. when the JVM is running
   * on a Microsoft-Based OS, then this wrapper method will simply call the
   * "classic" passedNI.getHardwareAddress() method.    ...remember that MS did not
   * deplete/exceed a limit of OPEN_FILE_DECRIPTORS.
     * @param passedNI
     * @return 
     * @throws java.net.SocketException 
   */
  public static byte[] getHardwareAddress_SPECIALIZED( NetworkInterface passedNI )
                                          throws SocketException {

    if ( passedNI == null ) {
      return null;
    }

    byte [] rtnBytes= null;

   
      // The correct logic depends upon the system family type
      //

//      if ( Constants.UNIX_FAMILY_STRING.equals( Constants.OS_LOOKUP_TABLE.get(System.getProperty( "os.name" )))) {

        lockFor_GetHardwareAddress_SPECIALIZED.lock();
        //
        try {
            // When it is known to be a Unix-based system them use the SPECIAL handling logic
            // to avoid the bug which causes a crash because it leaves file decriptors open then finally fails.
            String tmpName= passedNI.getName();
            Byte [] lookupResultBytes= theGetHardwareAddress_SPECIALIZED_Hashtable.get( tmpName );

            if ( lookupResultBytes == null ) {
              // First time seeing this NI
              // Do the call
              //System.out.println("...had to perform a call to: getHardwareAddress()");
              rtnBytes= passedNI.getHardwareAddress();
              if ( rtnBytes == null ) {
                // ..and we return the null within rtnBytes
                theGetHardwareAddress_SPECIALIZED_Hashtable.put( tmpName, new Byte[ 0 ] ); // NOT A NULL value
              } else {
                // ..and we return the non-null already pushed into rtnBytes
                Byte [] theByteObjList= new Byte[ rtnBytes.length ];
                for ( int q= 0; q < rtnBytes.length; q++ ) {
                  theByteObjList[ q ] = Byte.valueOf( rtnBytes[ q ] );
                }
                theGetHardwareAddress_SPECIALIZED_Hashtable.put( tmpName, theByteObjList );
                //System.out.println("***********************the specialize is adding: " +tmpName +" with bytes: "
                //  +convertHexBytesToString( theByteObjList ) );
              }

            } else {
              //System.out.println("...using cached results from a previuos call to getHardwareAddress()");
              // Found a previous value within our cache list
              // convert the Byte[] back to byte[]
              //
              // Note that if a Byte[] was found but came from a previous NULL
              // then it would have cached a Byte[] having length ZERO
              // In that case: just rteturn the var rtnBytes which should have been populated with NULL.


              if ( lookupResultBytes.length > 0 ) {
                int theQty= lookupResultBytes.length;
                //
                rtnBytes= new byte[ theQty ];
                //
                for ( int q= 0; q < theQty; q++ ) {
                  rtnBytes[ q ]= lookupResultBytes[ q ].byteValue();
                }
              }
            }
        //System.out.println("  BYW- The list size == " +theGetHardwareAddress_SPECIALIZED_Hashtable.size() );
//        } catch(Exception ex) {
//            System.out.println("Caught an exception getting network interfaces.");
        } finally {
          lockFor_GetHardwareAddress_SPECIALIZED.unlock();
        }
        
//      } else {
        // ALL others Os-Famliy values use the classic-JVM getHardwareAddress() every time
        // This logic is used as the the default for "Unknown" OS..
//        rtnBytes= passedNI.getHardwareAddress();
//      }

    return rtnBytes;
  }

  
 // ==========================================================================
  /**
   * 
   * @param passedBytes
   * @return
   */
  public static String convertHexBytesToString( byte [] passedBytes ) {
      if ( passedBytes == null ) {
          return null;
      }
      StringBuilder strBuf= new StringBuilder();

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
  
  // ==========================================================================
  /**
   * Returns a list of all {@link NetworkInterface}s for which 
   * {@link NetworkInterface#isUp()} returns true.
   * <p>
   * NOTE: The loopback interface is excluded from the returned list.
   * 
   * @return a list of all {@link NetworkInterface}s for which 
   * {@link NetworkInterface#isUp()} returns true
   * 
   * @throws java.net.SocketException
   */
  static ArrayList<NetworkInterface> getAllUpInterfaces() throws SocketException {
    
    ArrayList<NetworkInterface> rtnList = new ArrayList<>();
    
    ArrayList<NetworkInterface> allNIs = getAllNetworkInterfaces(); //Get all of the NetworkInterfaces
    for( int i = 0; i < allNIs.size(); i++ ) { //For each NetworkInterface...
      
      NetworkInterface aNI = allNIs.get( i );
      
      //Remove the loopback NetworkInterface...
      if( aNI.isLoopback() ) { //If the NetworkInterface is the loopback interface...
        continue; //Continue to the next NetworkInterface
      }
      
      //Remove any down NetworkInterfaces...
      if( aNI.isUp() == false ) { //If the NetworkInterface is not up...
        continue; //Continue to the next NetworkInterface
      }
      
      rtnList.add( aNI ); //Add the NetworkInterface to the list
      
    }
       
    return rtnList;
    
  }/* END getAllUpInterfaces() */
  
}/* END CLASS NetworkInterfaceUtilities */
