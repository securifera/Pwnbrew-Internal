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
* ReflectionUtilities.java
*
* Created on June 25, 2013, 9:31:25 PM
*/

package pwnbrew.utilities;

import pwnbrew.log.LoggableException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 *
 *  
 */
public class ReflectionUtilities {

    //======================================================================
    /**
    /* Returns the current value for the passed field
     *
     * @param passedInstance
     * @param passedField
     * @return 
     * @throws java.lang.IllegalAccessException 
     * @throws pwnbrew.log.LoggableException 
    */
    public static Object getValue( Object passedInstance, String passedField ) throws IllegalArgumentException, IllegalAccessException, LoggableException {

        //Access field of passed instance
        Field aField = null;
        Object anObj = null;
        Class<?> theEntityClass = passedInstance.getClass();

        //Retrieve the field
        while(aField == null){

           try {
              aField = theEntityClass.getDeclaredField(passedField);
           } catch (NoSuchFieldException ex) {
              theEntityClass = theEntityClass.getSuperclass();
              if(theEntityClass == Object.class){
                 break;
              }
           }
        }

        //Set to accessible and assign to a variable
        if(aField != null){
            aField.setAccessible(true);
            anObj = aField.get(passedInstance);
        } else {
            throw new LoggableException(new StringBuilder().append("Unable to find ")
                    .append(passedField).append(" declared variable.").toString());
        }

        return anObj;
    }

    //======================================================================
    /**
    /* Sets the value for the passed field
     *
     * @param passedInstance
     * @param passedValue
     * @param passedField
     * @throws java.lang.IllegalAccessException
     * @throws pwnbrew.log.LoggableException
    */
    public static void setValue(Object passedInstance, String passedField, Object passedValue) throws IllegalArgumentException, IllegalAccessException, LoggableException {

        //Access field of passed instance
        Field aField = null;
        Class<?> theEntityClass = passedInstance.getClass();

        //Retrieve the field
        while(aField == null){

           try {
              aField = theEntityClass.getDeclaredField(passedField);
           } catch (NoSuchFieldException ex) {
              theEntityClass = theEntityClass.getSuperclass();
              if(theEntityClass == Object.class){
                 break;
              }
           }
        }

        //Set to accessible and assign to a variable
        if(aField != null){
            aField.setAccessible(true);
            aField.set(passedInstance, passedValue);
        } else {
            throw new LoggableException(new StringBuilder().append("Unable to find ")
                    .append(passedField).append(" declared variable.").toString());
        }
    }


    //======================================================================
    /**
    /* Calls a method in a specified class
     *
     * @param passedInstance
     * @param methodParams
     * @param passedMethod
     * @param passedClassArr
     * @throws pwnbrew.log.LoggableException
     * @throws java.lang.reflect.InvocationTargetException
     * @throws java.lang.IllegalAccessException
    */
    public static void invokeMethod(Object passedInstance, String passedMethod, Class[] passedClassArr, Object[] methodParams) throws LoggableException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

        //Access field of passed instance
        Method aMethod = null;
        Class<?> theEntityClass = passedInstance.getClass();

        //Retrieve the field
        while(aMethod == null){

           try {
               //Add the new scanner to the component list
              aMethod = theEntityClass.getDeclaredMethod(passedMethod, passedClassArr );
           } catch (NoSuchMethodException ex) {
              theEntityClass = theEntityClass.getSuperclass();
              if(theEntityClass == Object.class){
                 break;
              }
           }
        }

        //Set to accessible and assign to a variable
        if(aMethod != null){
           aMethod.setAccessible(true);
           aMethod.invoke(passedInstance, methodParams);

        } else {
            throw new LoggableException(new StringBuilder().append("Unable to find ")
                    .append(passedMethod).append(" declared variable.").toString());
        }
    }

}/* END CLASS ReflectionUtilities */
