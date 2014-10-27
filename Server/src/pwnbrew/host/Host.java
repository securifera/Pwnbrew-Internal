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

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import pwnbrew.utilities.Utilities;
import pwnbrew.xmlBase.AttributeCollection;
import pwnbrew.xmlBase.Node;
import pwnbrew.xmlBase.XmlBase;

/**
 *
 *  
 */
public class Host extends Node {
    
    //Image and icon...
    private static final String Host_Img_File_Name = "computer_small.png";
    private static final String Host_DisConnect_Img_File_Name = "dis_computer_small.png";
    private static final BufferedImage HostBuffImage = Utilities.loadImageFromJar( Host_Img_File_Name );	
    public static final Icon HOST_ICON = new ImageIcon( HostBuffImage.getScaledInstance(
            ICON_WIDTH, ICON_HEIGHT, Image.SCALE_SMOOTH ) );
    
    public static final Icon HOST_DISCONNECT_ICON = new ImageIcon( Utilities.loadImageFromJar( Host_DisConnect_Img_File_Name ).getScaledInstance(
            ICON_WIDTH, ICON_HEIGHT, Image.SCALE_SMOOTH));
 
    private boolean virtual = false;
    
    //Get the MAC that is currently connected.
    private boolean connected = false;
    
    private List<Session> sessionList = new LinkedList<>(); //A list of sessions
    private static final String ATTRIBUTE_CheckInDates = "CheckInDates";
    private static final String ATTRIBUTE_ConnectedHostIds = "ConnectedHostIds";
    private static final String ATTRIBUTE_AutoSleep = "autoSleep";
  
    private static final String ATTRIBUTE_Os_Name = "osName";
    private static final String ATTRIBUTE_JVM_Arch = "jvmArch";
    private static final String ATTRIBUTE_Relay_Port = "relayPort";
    private static final String ATTRIBUTE_JAR_Version = "jarVersion";
    private static final String ATTRIBUTE_JRE_Version = "jreVersion";
    
    // ========================================================================
    /**
     * Creates a new instance of {@link Host}.
     */
    public Host() {
        
        theIcon = HOST_ICON;
        theBufferedImage = HostBuffImage;
        
         //Add the attributes
        theAttributeMap.put( ATTRIBUTE_Os_Name,  ""  );
        
         //Add the attributes
        theAttributeMap.put( ATTRIBUTE_JVM_Arch,  ""  );
        
        //Add the attributes
        theAttributeMap.put( ATTRIBUTE_JAR_Version,  ""  );
        
        //Add the attributes
        theAttributeMap.put( ATTRIBUTE_JRE_Version,  ""  );
        
         //Add the attributes
        theAttributeMap.put( ATTRIBUTE_Relay_Port,  ""  );
              
        //Add the check in date array
        theAttributeCollectionMap.put(ATTRIBUTE_CheckInDates, new AttributeCollection(ATTRIBUTE_CheckInDates, new String[0]));
        
        theAttributeCollectionMap.put(ATTRIBUTE_ConnectedHostIds, new AttributeCollection(ATTRIBUTE_ConnectedHostIds, new String[0]));

        //Add the auto sleep flag
        theAttributeMap.put( ATTRIBUTE_AutoSleep, "FALSE"  );
        
    }/* END CONSTRUCTOR() */
    
    
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
    * Adds and updates local support objects, determining the appropriate manner
    * in which to do so according to the class of the <code>passedGRB</code> argument.
    *
    * @param xmlBase the support object to be added/updated
    */
    @Override
    public void addUpdateComponent( XmlBase xmlBase ) {

        super.addUpdateComponent(xmlBase);
        if( xmlBase instanceof Session ) { //If the XmlBase is a session...
            sessionList.add( (Session)xmlBase );
        }
    }
    
      // ==========================================================================
    /**
    *  Removes a supporting object from the XmlBase
    *
    *  @param passedGRB  the object to be removed
    *
    *  @return true if the object was successfully removed
    */
    @Override
    public boolean removeComponent( XmlBase passedGRB ) {
        
        boolean objectRemoved = false;
        if( passedGRB instanceof Session ) { //If the object is a Parameter...
            Session aSession = (Session)passedGRB;
            if( sessionList.remove( aSession ) ) { //If the Parameter is successfully removed from the list...
                objectRemoved = true; //...update the return value
            }
        } else {
            objectRemoved = super.removeComponent(passedGRB);
        }

        return objectRemoved;
    }/* END removeComponent() */
    
    // ==========================================================================
    /**
    * Returns a list of this object's subcomponents that should be added to its
    * XML data.
    * <p>
    * NOTE: This overrides a method in {@link XmlBase}.
    * 
    * @return an {@link ArrayList} of the {@link XmlBase} components for this
    * object
    */
    @Override
    public List<XmlBase> getXmlComponents() {

        List<XmlBase> rtnList = super.getXmlComponents();
        
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
        
    }/* END addSession() */
    
    // ==========================================================================
    /**
    * Returns all of the {@link Session}s.
    *
    * @return all of the {@code Session}s
    */
    public List<Session> getSessionList() {
        return new ArrayList<>( sessionList );
    }/* END getSessionList() */
    
    
    // ==========================================================================
    /**
    * Sets the session list
     * @param passedList
    */
    public void setSessionList( List<Session> passedList ) {
        sessionList = new ArrayList<>( passedList );
    }/* END setSessionList() */
    
      // ==========================================================================
    /**
     * Returns the check-in list.
     *
     * @return the command
     */
    public List<String> getCheckInList() {
        
        List<String> theCheckInList = null;
        
        AttributeCollection theCollection = theAttributeCollectionMap.get( ATTRIBUTE_CheckInDates );
        if(theCollection != null){
           theCheckInList = theCollection.getCollection();
        }
        return theCheckInList;
        
    }/* END getCheckIn() */
    
     // ==========================================================================
    /**
     * Returns the check-in list.
     *
     * @param passedList
     */
    public void setCheckInList( List<String> passedList ) {
        
        AttributeCollection theCollection = theAttributeCollectionMap.get( ATTRIBUTE_CheckInDates );
        if(theCollection != null){
            theCollection.setCollection( passedList );
        }
        
    }/* END setCheckInList() */
    
    // ========================================================================
    /**
     *  Adds a check in time.
     * 
     * @param passedDateStr 
     */
    public void addCheckInTime( String passedDateStr ){
        
        List<String> theCheckInList = getCheckInList();
        if( theCheckInList != null && !theCheckInList.contains( passedDateStr )){
            AttributeCollection theCollection = theAttributeCollectionMap.get( ATTRIBUTE_CheckInDates );
            theCollection.addToCollection( passedDateStr );
        }
    }
    
    // ========================================================================
    /**
     *  Removes a check in time.
     * 
     * @param passedDateStr 
     */
    public void removeCheckInTime( String passedDateStr ){
        AttributeCollection theCollection = theAttributeCollectionMap.get( ATTRIBUTE_CheckInDates );
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
        
        AttributeCollection theCollection = theAttributeCollectionMap.get( ATTRIBUTE_ConnectedHostIds );
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
        
        AttributeCollection theCollection = theAttributeCollectionMap.get( ATTRIBUTE_ConnectedHostIds );
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
            AttributeCollection theCollection = theAttributeCollectionMap.get( ATTRIBUTE_ConnectedHostIds );
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
        AttributeCollection theCollection = theAttributeCollectionMap.get( ATTRIBUTE_ConnectedHostIds );
        theCollection.removeFromCollection(passedDateStr);
    }
    
     // ========================================================================
    /**
     *  Removes the first check in time.
     * 
     * @return passedDateStr 
     */
    public String getNextCheckInTime(){
        
        AttributeCollection theCollection = theAttributeCollectionMap.get( ATTRIBUTE_CheckInDates );
        
        return theCollection.getStringAt( 0 );
    }
    
    //===============================================================
    /**
    *   Returns the icon
    * @return
    */
    @Override
    public Icon getIcon() {
        return ( connected ? HOST_ICON : HOST_DISCONNECT_ICON);
    }/* END getHostIcon() */

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
        return getAttribute( ATTRIBUTE_Os_Name );
    }
    
    //===============================================================
    /**
     *  Sets the OS Name
     * 
     * @param passedDate 
     */
    public void setOsName( String passedDate) {
        setAttribute( ATTRIBUTE_Os_Name, passedDate);
    }
    
    //===============================================================
    /**
     *  Get the JVM Arch
     * 
     * @return 
     */
    public String getJvmArch(){
        return getAttribute( ATTRIBUTE_JVM_Arch );
    }
    
    //===============================================================
    /**
     *  Sets the jvm architecture.
     * 
     * @param passedArch 
     */
    public void setJvmArch( String passedArch ) {
        setAttribute( ATTRIBUTE_JVM_Arch, passedArch);
    }
    
    //===============================================================
    /**
     *  Get the JAR version.
     * 
     * @return 
     */
    public String getJarVersion(){
        return getAttribute( ATTRIBUTE_JAR_Version );
    }
    
    //===============================================================
    /**
     *  Sets the JAR version.
     * 
     * @param passedString 
     */
    public void setJarVersion( String passedString ) {
        setAttribute( ATTRIBUTE_JAR_Version, passedString);
    }
    
     //===============================================================
    /**
     *  Get the JRE version.
     * 
     * @return 
     */
    public String getJreVersion(){
        return getAttribute( ATTRIBUTE_JRE_Version );
    }
    
    //===============================================================
    /**
     *  Sets the JRE version.
     * 
     * @param passedString 
     */
    public void setJreVersion( String passedString ) {
        setAttribute( ATTRIBUTE_JRE_Version, passedString);
    }
    
      //===============================================================
    /**
     *  Gets the relay port
     * 
     * @return 
     */
    public String getRelayPort(){
        return getAttribute( ATTRIBUTE_Relay_Port );
    }
    
    //===============================================================
    /**
     *  Sets the relay port.
     * 
     * @param passedPort 
     */
    public void setRelayPort( String passedPort ) {
        setAttribute( ATTRIBUTE_Relay_Port, passedPort);
    }
    
    //===================================================================
    /**
     *  Returns the auto sleep flag
     * 
     * @return 
     */
    public boolean getAutoSleepFlag(){

        String theVal = theAttributeMap.get(ATTRIBUTE_AutoSleep);
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

        theAttributeMap.put(ATTRIBUTE_AutoSleep, autoSleepFlag);
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
    }
   
   
}/* END CLASS Host */
