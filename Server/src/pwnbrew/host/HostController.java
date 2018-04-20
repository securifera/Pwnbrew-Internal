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
 * HostController.java
 *
 * Created on June 17, 2013, 11:46 PM
 */

package pwnbrew.host;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import pwnbrew.log.Log;
import pwnbrew.manager.DataManager;
import pwnbrew.manager.ServerManager;
import pwnbrew.misc.Constants;
import pwnbrew.misc.Directories;
import pwnbrew.network.control.messages.Sleep;
import pwnbrew.utilities.FileUtilities;
import pwnbrew.xml.XmlObject;

/**
 *
 *  
 */
public final class HostController {
    
    private static final String NAME_Class = HostController.class.getSimpleName();

    private Host theHost; 
    
    // ==========================================================================
    /**
     *  Constructor 
     * 
     * @param passedHost 
    */
    public HostController( Host passedHost ) {
        //super();
        setObject(passedHost);
    }
     
    // ========================================================================
    /**
     *  Returns the scripting language managed by the controller.
     * @return 
     */
//    @Override
    public Host getObject() {
        return theHost;
    }    
    
    // ========================================================================
    /**
     *  Returns whether this host controller represents the local host
     * 
     * @return 
    */     
    public boolean isLocalHost(){    
        return theHost.getHostname().equals( HostFactory.LOCALHOST);        
    }
 
    // ========================================================================
    /**
     *  Sets the Object managed by the controller.
     * @param passedObj
     */
//    @Override
    public void setObject(Object passedObj) {
        if( passedObj != null ){
            theHost = (Host) passedObj;
        }
    }
      
    // ========================================================================
    /**
     * Returns the name of the {@link Host}.
     * 
     * @return the name of the {@code Host}; null if the {@code Host} is not set
     */
//    @Override 
    public String getItemName() {
        
        String rtnString = null;
        if( theHost != null )
            rtnString = theHost.getHostname();
        
        return rtnString;
        
    }/* END getItemName() */
    
    
    // ==========================================================================
    /**
     *  Returns the object library.
     * @return 
     */
//    @Override
    public File getObjectLibraryDirectory() {
        
        File objDir;
        if( isLocalHost() ){
            objDir = new File( Directories.getLocalObjectLibraryPath() );
        } else {
            objDir = new File( Directories.getObjectLibraryDirectory(), "r" + theHost.getId() );
        }
        return objDir;
    }
    
    
    // ==========================================================================
    /**
     * Returns the string representation of the scripting language.
     * 
     * @return 
    */
    @Override
    public String toString(){
        return theHost.getHostname();
    }

    
    // ========================================================================
    /**
     * Returns the name of the {@link ScriptingLanguage} type for use in messages
     * to the user.
     * @return 
     */
//    @Override
    public String getItemTypeDisplayName() {
        return "Host";
    }

    // ==========================================================================
    /**
     *  Returns the check-in time list
     * 
     * @return 
     */
    public List<String> getCheckInDateList() {
        return theHost.getCheckInList();
    }
    
    // ==========================================================================
    /**
     * Removes the date from the Host
     * 
     * @param passedDate 
     */
    public void addCheckInDate(String passedDate) {
        theHost.addCheckInTime(passedDate);
    }
    
    //=========================================================================
    /**
     *  Replaces one date with the other
     * @param oldDate
     * @param newDate
     */
    public void replaceDate(String oldDate, String newDate){
       
        //Replace and refresh
        List<String> theCheckInList = theHost.getCheckInList();
        theCheckInList.remove(oldDate);
        
        //Add the new one and sort it
        theCheckInList.add(newDate);
        Collections.sort(theCheckInList);
        
        //Set the list
        theHost.setCheckInList( theCheckInList );
        
    }
      
    // ==========================================================================
    /**
     * Removes the next check in.
     * 
     * @param passedDate 
     */
    private void removeCheckInDate( String passedStr ) {
        
        //Remove the first checkin date        
        List<String> strList = new ArrayList<>();
        strList.add( passedStr );
        removeCheckInDates( strList );
        
    }
    
    // ==========================================================================
    /**
     * Removes the date from the Host
     * 
     * @param passedDateList 
     */
    public void removeCheckInDates( List<String> passedDateList ) {
                
        for( String aDateStr : passedDateList ){
            theHost.removeCheckInTime( aDateStr );
        }

        //Set the checkbox enablement
        if( theHost.getCheckInList().isEmpty() ){
            theHost.setAutoSleepFlag(false);
        }
        
    }
    
     //===================================================================
    /**
    * Deletes the given {@link XmlObject} from the library.
    * 
    * <p>
    * If the given {@code XmlObject} is null, this method does nothing.
    *
    */
//    @Override
    public void deleteFromLibrary() {

        Object theObj = getObject();
        if( theObj != null ) {        
            //Delete the object's file
            XmlObject theXB = (XmlObject)theObj;
            theXB.deleteSelfFromDirectory( getObjectLibraryDirectory() );
        }
        try {
            FileUtilities.deleteDir( getObjectLibraryDirectory() );
        } catch (IOException ex) {
            Log.log(Level.WARNING, NAME_Class, "deleteFromLibrary()", ex.getMessage(), ex );        
        }
        
   }

    //===================================================================
    /**
     *  Sets the auto sleep flag in the host
     * 
     * @param selected 
     */
    public void setAutoSleepFlag(boolean selected) {        
        theHost.setAutoSleepFlag( selected );        
    }
    
    //===================================================================
    /**
     *  Returns the auto sleep flag
     * 
     * @return 
     */
    public boolean getAutoSleepFlag(){
        return theHost.getAutoSleepFlag();
    } 
    
     // ==========================================================================
    /**
     * Saves the given {@link XmlObject} to the Object Library directory.
     * 
    */
//    @Override
    public void saveToDisk() {
        
        try {            
            String clientIdStr = getId();
           
            //Make sure it save correctly
            theHost.writeSelfToDisk(getObjectLibraryDirectory(), clientIdStr);
            
        } catch (IOException ex) {
            Log.log(Level.WARNING, NAME_Class, "saveToDisk()", ex.getMessage(), ex );          
        }
    }

    // ==========================================================================
    /**
     *  Gets the first sleep date and sends a message to the client to goto sleep.
     * 
     * @param passedManager
     * @param autoFlag
    */
    public void sleep( ServerManager passedManager, boolean autoFlag ) {
        
        //Purge stale dates
        removeStaleDates();        
        try {
            //Get the sleep time
            List<String> theCheckInList = theHost.getCheckInList();
            if( !theCheckInList.isEmpty() ){

                //Get the first time
                String theCheckInTime = theCheckInList.get(0);

                //Send sleep message
                int dstHostId = Integer.parseInt( theHost.getId());
                Sleep sleepMsg = new Sleep( dstHostId, theCheckInTime ); //Convert mins to seconds
                DataManager.send( passedManager, sleepMsg );

            }

        } catch( UnsupportedEncodingException ex ){
            Log.log(Level.WARNING, NAME_Class, "actionPerformed()", ex.getMessage(), ex );
        }
        
    }

    // ==========================================================================
    /**
     *  Remove any dates that are before the current time
     */
    public void removeStaleDates() {
        
        //Create a calendar object
        Calendar currCalendar = Calendar.getInstance();
        currCalendar.setTime( new Date() );

        //Loop on the next check in
        Calendar nextCalendar = Calendar.getInstance();
        while( true ){

            String nextCheckInStr = theHost.getNextCheckInTime();
            if( nextCheckInStr != null ){
                try {

                    //Parse the next date
                    Date nextDate = Constants.CHECKIN_DATE_FORMAT.parse( nextCheckInStr );
                    nextCalendar.setTime(nextDate);                       

                    //Keep pulling dates until one is found that is after
                    if( nextCalendar.before(currCalendar) )
                        removeCheckInDate( nextCheckInStr );
                    else
                        break;

                } catch (ParseException ex) {
                    ex = null;
                }

            } else
                break;            
        } 
    }
    
    // ==========================================================================
    /**
     *  Returns the id of the host controller to save to
     * 
     * @return 
     */
    public String getId() {
        
        String retStr;
        if( isLocalHost()){
            retStr = HostFactory.LOCALHOST;
        } else {
            retStr = theHost.getId();
        }
        
        return retStr;
    }

    //===============================================================
    /**
     *  If the host is connected then return true.
     * 
     * @return 
     */
    public boolean isConnected() {        
        return theHost.isConnected();
    }
 
    //========================================================================
    /**
     * 
     * @return 
     */
    public Host getHost() {
        return getObject();
    }

}
