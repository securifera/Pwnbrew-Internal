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
* Node.java
*
* Created on July 20, 2013, 1:21:45 PM
*/

package pwnbrew.xmlBase;

import pwnbrew.network.Nic;
import java.awt.image.BufferedImage;
import java.util.*;
import javax.swing.Icon;
import pwnbrew.gui.Iconable;
import pwnbrew.utilities.Utilities;

/**
 *
 *  
 */
public class Node extends XmlBase implements Iconable, java.io.Serializable {
    
    protected transient Icon theIcon;
    protected transient BufferedImage theBufferedImage;
    
    //A list of InetAddress,MAC pairs
    private final Map<String, Nic> nicMap = new HashMap<>();

    private static final long serialVersionUID = 1L;
    
    //===============================================================
    /**
     *  Constructor
    */
    public Node() {        
        //Add the attributes
        theAttributeMap.put( ATTRIBUTE_Name,  "" );
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

        if( xmlBase instanceof Nic ) { //If the XmlBase is a Parameter...
            
            Nic aNic = (Nic)xmlBase;
            nicMap.put( aNic.getMacAddress(), aNic );
        
        } else {
            super.addUpdateComponent( xmlBase );
        }

    }/* END addUpdateComponent() */
    
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
        boolean objectRemoved = true;

        if( passedGRB instanceof Nic ) { //If the object is a Parameter...
            Nic aNic = (Nic)passedGRB;
            nicMap.remove( aNic.getMacAddress() );
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

        if( nicMap.size() > 0 ) //If there are Nics...
            rtnList.addAll( nicMap.values() ); //Add the Nics
   
        return rtnList;

    }/* END getXmlComponents() */
    
    //===============================================================
    /**
     *  Returns the hostname.
     * 
     * @return 
    */
    public String getHostname() {
        return getAttribute( ATTRIBUTE_Name );
    }

    //===============================================================
    /**
    *   Returns a map filled with the NIC pairs for the Node.
     * @return 
    */
    public Map<String, Nic> getNicMap() {
        synchronized(nicMap){
            return new HashMap<>( nicMap );
        }
    }
    
    //===============================================================
    /**
    *   Adds a nic pair to the list.
     * @param macAddr
     * @param passedNic
    */
    public void addNicPair(String macAddr, Nic passedNic){
        synchronized(nicMap){
            nicMap.put( macAddr, passedNic ); 
        }       
    }
    
    //===============================================================
    /**
     *  Get the InetAddress for the passed MAC
     * 
     * @param connectedMac
     * @return 
    */
    public Nic getNic( String connectedMac ) {
        
        Nic aNic = null;
        if( connectedMac != null ){
            synchronized(nicMap){
                aNic = nicMap.get( connectedMac ); 
            } 
        }
        return aNic;
    }
    
    //===============================================================
    /**
     * Adds the NIC pairs to the node - all NICs added are inserted into
     * the beginning to show priority to newer connections.
     *
     * @param passedNicPairs
    */
    public void addNicPairs( Map<String, Nic> passedNicPairs) {
        
        nicMap.putAll(passedNicPairs);
    }

     //===============================================================
    /**
     *  The equals operator
     * 
     * @param passedObj
     * @return 
     */
    @Override
    public boolean equals( Object passedObj ) {

        boolean rtnBool = false;
        
        if( passedObj != null ) {
           
            Class<? extends Object> nodeClass = passedObj.getClass();
            Class<? extends Node> thisClass = getClass();
           
            if( nodeClass == thisClass ) {
               
                if( getHostname().equals( ( (Node)passedObj ).getHostname() ) ) {
                    rtnBool = true;
                }
                
            }
            
        }
        
        return rtnBool;
        
    }

    @Override
    public int hashCode() {
        int hash = 7;
        return hash;
    }

    //===============================================================
    /**
     *  Get the hostname
     * 
     * @return 
     */
    @Override
    public String toString(){
       return getHostname();
    }

    

    //===============================================================
    /**
    *   Returns the icon
    * @return
    */
    @Override
    public Icon getIcon() {
        return theIcon;
    }
    
    
    // ========================================================================
    /**
     * Returns an image representing a node.
     * 
     * @return an image representing a node
     */
    public BufferedImage getBufferedImage() {
        return theBufferedImage;
    }/* END getBufferedImage() */
    
    
    //===============================================================
    /**
    *   Combines the two nodes into one node
     * @param prevNode
     * @param newNode
    * @return
    */
    public static Node combine( Node prevNode, Node newNode ) {
        
        Node retNode;
         
        if( Utilities.isParentClass(prevNode.getClass(), newNode.getClass()) ) {
            retNode = newNode;
            retNode.update(prevNode);
        } else { //if( Utilities.isParentClass(newNode.getClass(), prevNode.getClass())) {
            retNode = prevNode;
            retNode.update(newNode);
        }
        
        return retNode;
    }

    //===============================================================
    /**
    *   Combines the two nodes into one node
     * @param prevNode
    */
    public void update( Node prevNode ) {
        
        Map<String, Nic> prevMap = prevNode.getNicMap();
        nicMap.putAll( prevMap );
        
        //If the hostname is not empty
        if( !prevNode.getHostname().isEmpty() ){
            setHostname( prevNode.getHostname() );
        }
    }

    //===============================================================
    /**
    *   Combines the two nodes into one node
     * @param theHostname
    */
    public void setHostname(String theHostname) {
        if( theHostname != null ){
            setAttribute( ATTRIBUTE_Name, theHostname);
        }
    }

}/* END CLASS Node */
