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
* Host.java
*
* Created on June 21, 2013, 7:48:12 PM
*/

package pwnbrew.host;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import pwnbrew.xml.AttributeCollection;
import pwnbrew.xml.Node;
import pwnbrew.xml.XmlObject;

/**
 *
 *  
 */
public class Host extends Node {

 
    private boolean virtual = false;
    
    //Get the MAC that is currently connected.
    private boolean connected = false;
    
    private List<Session> sessionList = new LinkedList<>(); //A list of sessions
    private static final String CHECK_IN_DATES = "CheckInDates";
    private static final String CONNECTED_HOST_IDS = "ConnectedHostIds";
    private static final String AUTO_SLEEP = "autoSleep";
  
    private static final String OS_NAME = "osName";
    private static final String JVM_ARCH = "jvmArch";
    private static final String RELAY_PORT = "relayPort";
    private static final String JAR_VERSION = "jarVersion";
    private static final String JRE_VERSION = "jreVersion";
    private static final String PID = "pid";
    
    // ========================================================================
    /**
     * Creates a new instance of {@link Host}.
     */
    public Host() {
                
         //Add the attributes
        thePropertyMap.put( OS_NAME,  ""  );
        
         //Add the attributes
        thePropertyMap.put( JVM_ARCH,  ""  );
        
        //Add the attributes
        thePropertyMap.put( JAR_VERSION,  ""  );
        
        //Add the attributes
        thePropertyMap.put( JRE_VERSION,  ""  );
        
        //Add the attributes
        thePropertyMap.put( PID,  ""  );
        
         //Add the attributes
        thePropertyMap.put( RELAY_PORT,  ""  );
              
        //Add the check in date array
        thePropertyCollectionMap.put(CHECK_IN_DATES, new AttributeCollection(CHECK_IN_DATES, new String[0]));
        
        thePropertyCollectionMap.put(CONNECTED_HOST_IDS, new AttributeCollection(CONNECTED_HOST_IDS, new String[0]));

        //Add the auto sleep flag
        thePropertyMap.put( AUTO_SLEEP, "FALSE"  );
        
    }
    
    
    //===============================================================
    /**
     *  Constructor
     * @param passedId
    */
    public Host( int passedId ) {
        this();
        setId( Integer.toString( passedId ) );
    }
   
    
    // ==========================================================================
    /**
    *
    * @param passedObj the support object to be added/updated
    */
    @Override
    public void addChildObject( XmlObject passedObj ) {

        super.addChildObject(passedObj);
        if( passedObj instanceof Session )
            sessionList.add( (Session)passedObj );
        
    }
    
      // ==========================================================================
    /**
    *  Removes a supporting object from the XmlObject
    *
    *  @param passedGRB  the object to be removed
    *
    *  @return true if the object was successfully removed
    */
    @Override
    public boolean removeChildObject( XmlObject passedGRB ) {
        
        boolean objectRemoved = false;
        if( passedGRB instanceof Session ) { //If the object is a Parameter...
            Session aSession = (Session)passedGRB;
            if( sessionList.remove( aSession ) ) { //If the Parameter is successfully removed from the list...
                objectRemoved = true; //...update the return value
            }
        } else {
            objectRemoved = super.removeChildObject(passedGRB);
        }

        return objectRemoved;
    }/* END removeComponent() */
    
    // ==========================================================================
    /**
    * Returns a list of this object's subcomponents that should be added to its
    * XML data.
    * <p>
    * NOTE: This overrides a method in {@link XmlObject}.
    * 
    * @return an {@link ArrayList} of the {@link XmlObject} components for this
    * object
    */
    @Override
    public List<XmlObject> getXmlObjects() {

        List<XmlObject> rtnList = super.getXmlObjects();
        
        //Add the sessions
        rtnList.addAll( sessionList );
        
        return rtnList;
    }
    
    // ==========================================================================
    /**
    * Adds a session.
    *
     * @param aSession
    */
    public void addSession( Session aSession ) {
        sessionList.add(aSession);
        
    }
    
    // ==========================================================================
    /**
    * Returns all of the {@link Session}s.
    *
    * @return all of the {@code Session}s
    */
    public List<Session> getSessionList() {
        return new ArrayList<>( sessionList );
    }
    
    // ==========================================================================
    /**
    * Sets the session list
     * @param passedList
    */
    public void setSessionList( List<Session> passedList ) {
        sessionList = new ArrayList<>( passedList );
    }
    
    // ==========================================================================
    /**
     * Returns the check-in list.
     *
     * @return the command
     */
    public List<String> getCheckInList() {
        
        List<String> theCheckInList = null;
        
        AttributeCollection theCollection = thePropertyCollectionMap.get( CHECK_IN_DATES );
        if(theCollection != null){
           theCheckInList = theCollection.getCollection();
        }
        return theCheckInList;
        
    }
    
     // ==========================================================================
    /**
     * Returns the check-in list.
     *
     * @param passedList
     */
    public void setCheckInList( List<String> passedList ) {
        
        AttributeCollection theCollection = thePropertyCollectionMap.get( CHECK_IN_DATES );
        if(theCollection != null){
            theCollection.setCollection( passedList );
        }
        
    }
    
    // ========================================================================
    /**
     *  Adds a check in time.
     * 
     * @param passedDateStr 
     */
    @SuppressWarnings("ucd")
    public void addCheckInTime( String passedDateStr ){
        
        List<String> theCheckInList = getCheckInList();
        if( theCheckInList != null && !theCheckInList.contains( passedDateStr )){
            AttributeCollection theCollection = thePropertyCollectionMap.get( CHECK_IN_DATES );
            theCollection.addToCollection( passedDateStr );
        }
    }
    
    // ========================================================================
    /**
     *  Removes a check in time.
     * 
     @SuppressWarnings("ucd")
     * @param passedDateStr 
     */
    public void removeCheckInTime( String passedDateStr ){
        AttributeCollection theCollection = thePropertyCollectionMap.get( CHECK_IN_DATES );
        theCollection.removeFromCollection(passedDateStr);
    }
    
    // ==========================================================================
    /**
     * Returns a list of connected host ids
     *
     * @return the command
     */
    public List<String> getConnectedHostIdList() {
        
        List<String> theCheckInList = null;
        
        AttributeCollection theCollection = thePropertyCollectionMap.get( CONNECTED_HOST_IDS );
        if(theCollection != null)
           theCheckInList = theCollection.getCollection();
        
        return theCheckInList;
        
    }
    
     // ==========================================================================
    /**
     * Returns the check-in list.
     *
     * @param passedList
     */
    public void setConnectedHostIdList( List<String> passedList ) {
        
        AttributeCollection theCollection = thePropertyCollectionMap.get( CONNECTED_HOST_IDS );
        if(theCollection != null)
            theCollection.setCollection( passedList );        
        
    }
    
    // ========================================================================
    /**
     *  Adds host id
     * 
     * @param passedDateStr 
     */
    public void addConnectedHostId( String passedDateStr ){
        
        List<String> hostIdList = getConnectedHostIdList();
        if( hostIdList != null && !hostIdList.contains( passedDateStr )){
            AttributeCollection theCollection = thePropertyCollectionMap.get( CONNECTED_HOST_IDS );
            theCollection.addToCollection( passedDateStr );
        }
    }
    
    // ========================================================================
    /**
     *  Remove host id
     * 
     * @param passedDateStr 
     */
    public void removeConnectedHostId( String passedDateStr ){
        AttributeCollection theCollection = thePropertyCollectionMap.get( CONNECTED_HOST_IDS );
        theCollection.removeFromCollection(passedDateStr);
    }
    
     // ========================================================================
    /**
     *  Removes the first check in time.
     * 
     * @return passedDateStr 
     */
    public String getNextCheckInTime(){
        
        AttributeCollection theCollection = thePropertyCollectionMap.get( CHECK_IN_DATES );        
        return theCollection.getStringAt( 0 );
    }

    // ========================================================================
    /**
     *  Returns whether the host is virtual.
     * @return 
     */
    public boolean isVirtual() {
        return virtual;
    }
    
    // ========================================================================
    /**
    * Indicates whether some other object is "equal to" this one.
    * <p>
    * @param passedObject   the reference object with which to compare
    *
    * @return  {@code true} if this object is the same as the obj argument; {@code false} otherwise
    *
    */
    @Override
    public boolean equals( Object passedObject ) {

        boolean retVal = super.equals(passedObject);

        if(passedObject != null && passedObject instanceof Host){
            Host passedHost = (Host)passedObject;
            retVal = retVal && (getId().equals(passedHost.getId()) );
            retVal = retVal && (virtual == passedHost.isVirtual());
        }

        return retVal;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        return hash;
    }
    
    // ========================================================================
    /**
     *  Sets the virtual flag for the host
     * @param passedBool
     */
    public void setVirtualFlag(boolean passedBool) {
        virtual = passedBool;
    }
    
    //===============================================================
    /**
    *   Combines the two nodes into one node
     * @param prevNode
    */
    @Override
    public void update( Node prevNode ) {
        super.update(prevNode);
        
        if( prevNode instanceof Host ){
            virtual = ((Host)prevNode).isVirtual();
        }
    }

    //===============================================================
    /**
     *  Returns whether the client is currently connected
     * 
     * @return 
     */
    public boolean isConnected(){
        return connected;
    }
    
    //===============================================================
    /**
     *  Sets the connected flag
     * 
     * @param passedBool 
     */
    public void setConnected(boolean passedBool) {
        connected = passedBool;
    }
    
    //===============================================================
    /**
     *  Get the OS Name
     * 
     * @return 
     */
    public String getOsName(){
        return getProperty( OS_NAME );
    }
    
    //===============================================================
    /**
     *  Sets the OS Name
     * 
     * @param passedDate 
     */
    public void setOsName( String passedDate) {
        setProperty( OS_NAME, passedDate);
    }
    
    //===============================================================
    /**
     *  Get the JVM Arch
     * 
     * @return 
     */
    public String getJvmArch(){
        return getProperty( JVM_ARCH );
    }
    
    //===============================================================
    /**
     *  Sets the jvm architecture.
     * 
     * @param passedArch 
     */
    public void setJvmArch( String passedArch ) {
        setProperty( JVM_ARCH, passedArch);
    }
    
    //===============================================================
    /**
     *  Get the JAR version.
     * 
     * @return 
     */
    public String getJarVersion(){
        return getProperty( JAR_VERSION );
    }
    
    //===============================================================
    /**
     *  Sets the JAR version.
     * 
     * @param passedString 
     */
    public void setJarVersion( String passedString ) {
        setProperty( JAR_VERSION, passedString);
    }
    
     //===============================================================
    /**
     *  Get the JRE version.
     * 
     * @return 
     */
    public String getJreVersion(){
        return getProperty( JRE_VERSION );
    }
    
    //===============================================================
    /**
     *  Sets the JRE version.
     * 
     * @param passedString 
     */
    public void setJreVersion( String passedString ) {
        setProperty( JRE_VERSION, passedString);
    }
    
      //===============================================================
    /**
     *  Get the PID
     * 
     * @return 
     */
    public String getPid(){
        return getProperty( PID );
    }
    
    //===============================================================
    /**
     *  Sets the PID
     * 
     * @param passedString 
     */
    public void setPid( String passedString ) {
        setProperty( PID, passedString);
    }
    
      //===============================================================
    /**
     *  Gets the relay port
     * 
     * @return 
     */
    public String getRelayPort(){
        return getProperty( RELAY_PORT );
    }
    
    //===============================================================
    /**
     *  Sets the relay port.
     * 
     * @param passedPort 
     */
    public void setRelayPort( String passedPort ) {
        setProperty( RELAY_PORT, passedPort);
    }
    
    //===================================================================
    /**
     *  Returns the auto sleep flag
     * 
     * @return 
     */
    public boolean getAutoSleepFlag(){

        String theVal = thePropertyMap.get(AUTO_SLEEP);
        if(theVal.equals("TRUE")){
            return true;
        }
        return false;
    }
    
    //===================================================================
    /**
     *  Sets the auto sleep flag
     * 
     * @param shouldUse 
    */
    public void setAutoSleepFlag( boolean shouldUse ){

        String autoSleepFlag = "FALSE";
        if(shouldUse){
            autoSleepFlag = "TRUE";
        }

        thePropertyMap.put(AUTO_SLEEP, autoSleepFlag);
    }

    //===================================================================
    /**
     * 
     * @param passedHost 
     */
    public void updateData(Host passedHost) {
        setHostname(passedHost.getHostname());
        addNicPairs( passedHost.getNicMap() );
        setJarVersion( passedHost.getJarVersion());
        setJvmArch( passedHost.getJvmArch());
        setJreVersion( passedHost.getJreVersion());
        setOsName( passedHost.getOsName());
        setPid( passedHost.getPid());
    }
   
   
}
