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
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import pwnbrew.MaltegoStub;
import pwnbrew.network.control.messages.RemoteException;
import pwnbrew.xml.maltego.MaltegoMessage;
import pwnbrew.xml.maltego.MaltegoTransformExceptionMessage;

/**
 *
 * @author Securifera
 */
abstract public class Function {

    protected final MaltegoStub theManager;
    private volatile boolean notified = false;
    
    
    //Create the return msg
    protected final MaltegoMessage theReturnMsg = new MaltegoMessage();
    
    //Set the exception msg
    private String theExceptionMsg = null;

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
     */
    abstract public void run( String passedObjectStr );
    
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
     * @param passedMsg 
     */
    public void setExceptionMsg( String passedMsg ) {
        theExceptionMsg = passedMsg;
    }   
        
    //===================================================================
     /**
     *
     * @return
     */
    public String getExceptionMsg() {
        return theExceptionMsg;
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
    
    // ==========================================================================
    /**
    * Causes the calling {@link Thread} to <tt>wait()</tt> until notified by
    * another.
    * <p>
    * <strong>This method most certainly "blocks".</strong>
     * @param anInt
    */
    protected synchronized void waitToBeNotified( Integer... anInt ) {

        while( !notified ) {

            try {
                
                //Add a timeout if necessary
                if( anInt.length > 0 ){
                    
                    wait( anInt[0]);
                    break;
                    
                } else {
                    wait(); //Wait here until notified
                }
                
            } catch( InterruptedException ex ) {
            }

        }
        notified = false;
    }
    
    //===============================================================
    /**
     * Notifies the thread
    */
    public synchronized void beNotified() {
        notified = true;
        notifyAll();
    }

    //===============================================================
    /**
     * 
     * @return 
     */
    public String getOutput() {
        
        if(theExceptionMsg != null ){
            
            //Create a relay object
            pwnbrew.xml.maltego.Exception exMsg = new pwnbrew.xml.maltego.Exception( theExceptionMsg );
            MaltegoTransformExceptionMessage malMsg = theReturnMsg.getExceptionMessage();

            //Create the message list
            malMsg.getExceptionMessages().addExceptionMessage(exMsg);  
            
        } 
        
        return theReturnMsg.getXml();
    }

    //===============================================================
    /**
     * 
     * @param taskId
     * @param fileLoc 
     */
    public void fileReceived(int taskId, File fileLoc) { }

    //===============================================================
    /**
     * 
     * @param passedMsg 
     */
    public void handleException(RemoteException passedMsg ) {
        
        //Create a relay object
        pwnbrew.xml.maltego.Exception exMsg = new pwnbrew.xml.maltego.Exception( passedMsg.getMessage() );
        MaltegoTransformExceptionMessage malMsg = theReturnMsg.getExceptionMessage();

        //Create the message list
        malMsg.getExceptionMessages().addExceptionMessage(exMsg); 
        System.out.println( theReturnMsg.getXml() );
        
    }
}
