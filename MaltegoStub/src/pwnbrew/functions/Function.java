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
package pwnbrew.functions;

import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import pwnbrew.MaltegoStub;
import pwnbrew.xml.maltego.MaltegoMessage;

/**
 *
 * @author Securifera
 */
abstract public class Function {

    protected final MaltegoStub theManager;
    
    //Create the return msg
    protected final MaltegoMessage theReturnMsg = new MaltegoMessage();

    //===================================================================
    /**
     * Base Constructor
     * @param passedManager
     */
    public Function( MaltegoStub passedManager ) {
        theManager = passedManager;
    }
        
    //==================================================================
    /**
     * Runs the function and returns an XML string as output
     * @param passedObjectStr
     * @return 
     */
    abstract public String run( String passedObjectStr );
    
     //========================================================================
    /**
     *  Returns a map of the key value pairs
     * 
     * @param value
     * @return 
     */
    public static Map<String, String> getKeyValueMap(String value) {
        
        Map<String, String> retMap = new HashMap<>();
        List<String> tempList = new ArrayList<>();
        
        int index = 0;
        int oldIndex = 0;
        while( index < value.length() ){
            index = value.indexOf("#", index);
            if( index == -1 ){
                if( oldIndex > 0)
                    tempList.add(value.substring(oldIndex));
                break;
            } else {
                if( index > 0 && (value.charAt(index - 1) != '\\')){
                    tempList.add( value.substring(oldIndex, index++));
                    oldIndex = index;
                } else {
                    index++;
                }
                            
            }
                
        }
        
        //Split the key values
        for( String anEntry : tempList ){
            String[] keyValueArr = anEntry.split("=");
            if( keyValueArr.length > 1 )
                retMap.put( keyValueArr[0],keyValueArr[1]);
            
        }
        
        return retMap;
    }

    //===================================================================
    /**
     * 
     * @return 
     */
    public MaltegoMessage getMaltegoMsg() {
        return theReturnMsg;
    }

     //========================================================================
    /**
     * 
     * @return 
     */
    public Component getParentComponent(){
        return null;
    }
}
