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
 * Nic.java
 *
 * Created on May 15, 2013, 1:53 PM
 */

package pwnbrew.network;

import pwnbrew.xmlBase.XmlBase;


/**
 *
 *  
 */
public class Nic extends XmlBase{

   private static final String ATTRIBUTE_MAC_ADDRESS = "MacAddress";
   private static final String ATTRIBUTE_IP_ADDRESS = "IpAddress";
   private static final String ATTRIBUTE_SUBNET_MASK = "SubnetMask";
 
   
   // ==========================================================================
   /**
   * Creates a new instance of Nic
   */
   public Nic() {
      theAttributeMap.put( ATTRIBUTE_MAC_ADDRESS, "");
      theAttributeMap.put( ATTRIBUTE_IP_ADDRESS, "");
      theAttributeMap.put( ATTRIBUTE_SUBNET_MASK, "");
   }

   // ==========================================================================
   /**
   * Creates a new instance of ScriptRef
     * @param macAddress
     * @param subnetMask
     * @param passedInet
   */
   public Nic( String macAddress, String passedInet, String subnetMask ) {
      
      theAttributeMap.put( ATTRIBUTE_MAC_ADDRESS, macAddress);
      theAttributeMap.put( ATTRIBUTE_IP_ADDRESS,  passedInet );
      theAttributeMap.put( ATTRIBUTE_SUBNET_MASK,  subnetMask );
 
   }

   //===============================================================
    /**
     * Returns the ip address
    *  
    * @return
   */
   public String getMacAddress(){
      return getAttribute( ATTRIBUTE_MAC_ADDRESS );
   }
   
   //===============================================================
    /**
     * Returns the ip address
    *  
    * @return
   */
   public String getIpAddress(){
      return getAttribute( ATTRIBUTE_IP_ADDRESS );
   }
   
    //===============================================================
    /**
     * Returns the subnet mask
    *  
    * @return
   */
   public String getSubnetMask(){
      return getAttribute( ATTRIBUTE_SUBNET_MASK );
   }

   //===============================================================
   /**
    *   Returns the string representation of the NIC
    * 
    * @return 
   */
   @Override
   public String toString() {
      
      StringBuilder aSB = new StringBuilder("");
      String theMac = getMacAddress();
      
      if( theMac.length() == 12 ){
          aSB.append( theMac.substring(0, 2))
                  .append("-").append(theMac.substring(2, 4))
                  .append("-").append(theMac.substring(4, 6))
                  .append("-").append(theMac.substring(6, 8))
                  .append("-").append(theMac.substring(8, 10))
                  .append("-").append(theMac.substring(10, 12));
                  
      }

      return aSB.toString();
   }

}
