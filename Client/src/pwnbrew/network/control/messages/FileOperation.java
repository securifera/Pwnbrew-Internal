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
* FileOperation.java
*
* Created on Dec 25, 2013, 10:12:42 PM
*/

package pwnbrew.network.control.messages;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Date;
import java.util.logging.Level;
import pwnbrew.log.RemoteLog;
import pwnbrew.manager.CommManager;
import pwnbrew.misc.Constants;
import pwnbrew.network.ControlOption;
import pwnbrew.network.control.ControlMessageManager;

/**
 *
 *  
 */
public final class FileOperation extends Tasking {
    
    private static final byte DELETE = 78;
    private static final byte RENAME = 79;
    private static final byte DATE = 80;
            
    private static final byte OPTION_OPERATION = 42;
    private static final byte OPTION_FILE_PATH = 43;
    private static final byte OPTION_ADDITIONAL = 44;
    
    private byte theOperation;
    private String theFilePath;
    private String addParam;
    
    private static final String NAME_Class = FileOperation.class.getSimpleName();
    
    
    // ==========================================================================
    /**
     * Constructor
     *
     * @param passedId
    */
    public FileOperation(byte[] passedId ) {
        super( passedId );
    }
    
     //=========================================================================
    /**
     *  Sets the variable in the message related to this TLV
     * 
     * @param tempTlv 
     * @return  
     */
    @Override
    public boolean setOption( ControlOption tempTlv ){        

        boolean retVal = true;    
        if( !super.setOption(tempTlv)){
            try {
                byte[] theValue = tempTlv.getValue();
                switch( tempTlv.getType()){
                    case OPTION_OPERATION:
                        if( theValue.length > 0 ){
                            theOperation = theValue[0];
                        }
                        break;
                    case OPTION_FILE_PATH:
                        theFilePath = new String( theValue, "US-ASCII");
                        break;
                    case OPTION_ADDITIONAL:
                        addParam = new String( theValue, "US-ASCII");
                        break;
                    default:
                        retVal = false;
                        break;
                }           
            } catch (UnsupportedEncodingException ex) {
                ex = null;
            }
        }
        return retVal;
    }
    
    //===============================================================
    /**
    *   Performs the logic specific to the message.
    *
     * @param passedManager
    */
    @Override
    public void evaluate( CommManager passedManager ) {   
    
        //Perform the operation
        boolean retVal = false;
        File theFile = new File(theFilePath);  
        if( theFile.exists() ){
            switch( theOperation ){
                case DELETE:
                    retVal = theFile.delete();              
                    break;
                case RENAME:
                    File newFile = new File( theFile.getParentFile(), addParam);                    
                    retVal = theFile.renameTo(newFile);  
                    break;
                case DATE:
                    try {
                        Date aDate = Constants.CHECKIN_DATE_FORMAT.parse(addParam);
                        long newTime = aDate.getTime();
                        retVal = theFile.setLastModified(newTime);
                    } catch (ParseException ex) {
                    }
                    break;
            }
        }
        
        //Send back the result
        byte retByte = 0;
        if( retVal){
            retByte = 1;
        }
        
        try {
            ControlMessageManager aCMManager = ControlMessageManager.getControlMessageManager();
            if( aCMManager != null ){
                FileOpResult theResult = new FileOpResult( getTaskId(), retByte);
                aCMManager.send(theResult);
            }
        } catch (UnsupportedEncodingException ex) {
            RemoteLog.log(Level.WARNING, NAME_Class, "evaluate()", ex.getMessage(), ex);
        }       
    
    }
   
}
