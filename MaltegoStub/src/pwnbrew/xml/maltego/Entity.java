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
package pwnbrew.xml.maltego;

import java.util.List;
import pwnbrew.xml.XmlObject;

/**
 *
 * @author Securifera
 */
abstract public class Entity extends XmlObject {
    
    private static final String ATTRIBUTE_Type = "Type";
    
    //Internal Objects
    protected Value mainPropertyValue = new Value();
    protected Weight entityWeight = null;
    protected DisplayInformation displayInfo = null;
    protected AdditionalFields fieldList = null;
    protected IconURL urlToIcon = null;
    
    // ==========================================================================
    /**
    * Constructor
     * @param passedType
    */
    public Entity( String passedType ) {
        theAttributeMap.put( ATTRIBUTE_Type, passedType  );
    }

    // ==========================================================================
    /**
    * Returns the type of the entity
    *
    * @return the type of entity
    */
    public String getType() {
        return getAttribute( ATTRIBUTE_Type );
    }
    
    // ==========================================================================
    /**
    * Add the field to the entity
    *
     * @param aField
    */
    public void addField( Field aField ) {
        
        //If the object is null then create it
        if( fieldList == null )
            fieldList = new AdditionalFields();
        
        fieldList.addField(aField);
    }
    
    // ==========================================================================
    /**
    * Add the field to the entity
    *
     * @param fieldName
     * @return 
    */
    public Field getField( String fieldName ) {
        
        //If the object is null then create it
        Field aField = null;
        if( fieldList != null )
            fieldList.getField(fieldName);
        
        return aField;
    }
    
     //===========================================================================
    /**
     * 
     * @return  
     */
    public String getDisplayValue() {
        return mainPropertyValue.getXmlObjectContent();
    }
    
    //===========================================================================
    /**
     * 
     * @param passedValue 
     */
    public void setDisplayValue( String passedValue ) {
        mainPropertyValue.setXmlObjectContent( passedValue );
    }
    
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
    public List<XmlObject> getXmlComponents() {
        
        List<XmlObject> rtnList = super.getXmlComponents();
        
        //Add the internal objects
        rtnList.add( mainPropertyValue );
        
        //Add weight if it has been set
        if( entityWeight != null )
            rtnList.add( entityWeight );
        
        //Add any display info
        if( displayInfo != null )
            rtnList.add( displayInfo );
        
        //Add any additional fields
        if( fieldList != null )
            rtnList.add( fieldList );
        
        //Add any additional fields
        if( urlToIcon != null )
            rtnList.add( urlToIcon );
        
        return rtnList;
    }
       
}
