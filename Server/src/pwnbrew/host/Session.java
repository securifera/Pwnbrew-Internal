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
* Session.java
*
* Created on November 24, 2013, 1:48:12 PM
*/
package pwnbrew.host;

import java.util.Date;
import pwnbrew.misc.Constants;
import pwnbrew.xml.XmlObject;

/**
 *
 *  
 */
public class Session extends XmlObject {
    
    private static final String CHECK_IN_DATE = "CheckinDate";
    private static final String DISCONNECT_DATE = "DisconnectedDate";
    
    // ========================================================================
    /**
     * Creates a new instance of {@link Session}.
     */
    public Session() {
        
        //Add the attributes
        thePropertyMap.put( CHECK_IN_DATE,  Constants.CHECKIN_DATE_FORMAT.format( new Date() )  );
        
        //Add the attributes
        thePropertyMap.put( DISCONNECT_DATE,  ""  );
    }
    
    //===============================================================
    /**
     *  Get the Date of the check-in.
     * 
     * @return 
    */
    public String getCheckInTime(){
        return getProperty( CHECK_IN_DATE );
    }
    
    //===============================================================
    /**
     *  Sets the checked in date.
     * 
     * @param passedDate 
     */
    public void setCheckInDate( String passedDate) {
        setProperty( CHECK_IN_DATE, passedDate);
    }
    
    //===============================================================
    /**
     *  Get the Date of the last check-in.
     * 
     * @return 
     */
    public String getDisconnectedTime(){
        return getProperty( DISCONNECT_DATE );
    }
    
    //===============================================================
    /**
     *  Sets the disconnect date.
     * 
     * @param passedDate 
     */
    public void setDisconnectedTime( String passedDate) {
        setProperty( DISCONNECT_DATE, passedDate);
    }
 
}
