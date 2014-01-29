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
 *  Loader.java
 *
 *  Created on June 2, 2013
 */

package pwnbrew.utilities;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;
import java.util.jar.JarFile;
import sun.net.www.protocol.jar.JarURLConnection;

/**
 *
 *  
 * 
 */
public class LoaderUtilities {

    private static final HashSet<String> setJarFileNames2Close = new HashSet<>();

    // ========================================================================
    /**
     *  Attempts to close the passed class loader
     * 
     * @param theLoader 
    */
    public static void close( ClassLoader theLoader ) {
        setJarFileNames2Close.clear();
        closeClassLoader( theLoader );
        finalizeNativeLibs( theLoader );
        cleanupJarFileFactory();
    }
 
    // ========================================================================
    /**
    *    Cleanup jar file factory cache
    */
    private static boolean cleanupJarFileFactory(){
        
        boolean retVal = false;
        Class<JarURLConnection> classJarURLConnection = JarURLConnection.class;
               
        Field aField = null;
        try {
            aField = classJarURLConnection.getDeclaredField("factory");
        } catch (NoSuchFieldException e) {
        //ignore
        }
        
        if (aField == null) {
            return retVal;
        }
        
        aField.setAccessible(true);
        Object anObj = null;
        try {
            anObj = aField.get(null);
        } catch (IllegalAccessException e) {
        //ignore
        }
        
        if (anObj == null) {
            return retVal;
        }
        
        Class<? extends Object> classJarFileFactory = anObj.getClass();
        //
        HashMap<?, ?> fileCache = null;
        try {
            aField = classJarFileFactory.getDeclaredField("fileCache");
            aField.setAccessible(true);
            anObj = aField.get(null);
            if (anObj instanceof HashMap) {
                fileCache = (HashMap<?, ?>)anObj;
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
        }
        
        HashMap<?, ?> urlCache = null;
        try {
            aField = classJarFileFactory.getDeclaredField("urlCache");
            aField.setAccessible(true);
            anObj = aField.get(null);
            if (anObj instanceof HashMap) {
                urlCache = (HashMap<?, ?>)anObj;
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
        }
        
        if (urlCache != null) {
            
            HashMap<?, ?> urlCacheTmp = (HashMap<?, ?>)urlCache.clone();
            Iterator<?> it = urlCacheTmp.keySet().iterator();
            while (it.hasNext()) {
                anObj = it.next();
                if (!(anObj instanceof JarFile)) {
                    continue;
                }
                
                JarFile aJarFile = (JarFile)anObj;
                if (setJarFileNames2Close.contains(aJarFile.getName())) {
                    try {
                        aJarFile.close();
                    } catch (IOException e) {
                    //ignore
                    }
                    if (fileCache != null) {
                        fileCache.remove(urlCache.get(aJarFile));
                    }
                    urlCache.remove(aJarFile);
                }
            }
            retVal = true;
            
        } else if (fileCache != null) {
            
            // urlCache := null
            HashMap<?, ?> fileCacheTmp = (HashMap<?, ?>)fileCache.clone();
            Iterator<?> it = fileCacheTmp.keySet().iterator();
            while (it.hasNext()) {
                
                Object key = it.next();
                anObj = fileCache.get(key);
                if (!(anObj instanceof JarFile)) {
                    continue;
                }
                JarFile jarFile = (JarFile)anObj;
                if (setJarFileNames2Close.contains(jarFile.getName())) {
                    try {
                        jarFile.close();
                    } catch (IOException e) {
                    //ignore
                    }
                    fileCache.remove(key);
                }
            }
            retVal = true;
        }
        setJarFileNames2Close.clear();
        return retVal;
    }
 
    // ========================================================================
    /**
    *   Close jar files of the passed ClassLoader
    * 
    * @param passedLoader
    * @return
    */
    private static boolean closeClassLoader( ClassLoader passedLoader ) {
        
        boolean retVal = false;
        if (passedLoader == null) {
            return retVal;
        }
        
        Class<URLClassLoader> classURLClassLoader = URLClassLoader.class;
        Field aField = null;
        try {
            aField = classURLClassLoader.getDeclaredField("ucp");
        } catch (NoSuchFieldException e1) {
        //ignore
        }
        
        if (aField != null) {
            
            aField.setAccessible(true);
            Object obj = null;
            try {
                obj = aField.get(passedLoader);
            } catch (IllegalAccessException e1) {
            //ignore
            }
            
            if (obj != null) {
                
                final Object ucp = obj;
                aField = null;
                try {
                    aField = ucp.getClass().getDeclaredField("loaders");
                } catch (NoSuchFieldException e1) {
                //ignore
                }
                
                if (aField != null) {
                    
                    aField.setAccessible(true);
                    ArrayList<?> loaders = null;
                    try {
                        loaders = (ArrayList<?>) aField.get(ucp);
                        retVal = true;
                    } catch (IllegalAccessException e1) {
                    //ignore
                    }
                    
                    //Loop through the loaders
                    for (int i = 0; loaders != null && i < loaders.size(); i++) {
                        
                        obj = loaders.get(i);
                        aField = null;
                        try {
                            aField = obj.getClass().getDeclaredField("jar");
                        } catch (NoSuchFieldException e) {
                        //ignore
                        }
                        
                        if (aField != null) {
                            
                            aField.setAccessible(true);
                            try {
                                obj = aField.get(obj);
                            } catch (IllegalAccessException e1) {
                            // ignore
                            }
                            
                            if (obj instanceof JarFile) {
                                final JarFile jarFile = (JarFile)obj;
                                setJarFileNames2Close.add(jarFile.getName());
                             
                                try {
                                    jarFile.close();
                                } catch (IOException e) {
                                // ignore
                                }
                            }
                        }
                    }
                }
            }
        }
        return retVal;
    }
 
    // ========================================================================
    /**
    *   Finalize native libraries
    * 
    * @param passedLoader 
    * @return
    */
    private static boolean finalizeNativeLibs( ClassLoader passedLoader ) {
        
        boolean retVal = false;
        Class classClassLoader = ClassLoader.class;
        Field nativeLibraries = null;
        try {
            nativeLibraries = classClassLoader.getDeclaredField("nativeLibraries");
        } catch (NoSuchFieldException e1) {
        //ignore
        }
        if (nativeLibraries == null) {
            return retVal;
        }
        nativeLibraries.setAccessible(true);
        Object obj = null;
        try {
            obj = nativeLibraries.get(passedLoader );
        } catch (IllegalAccessException e1) {
        //ignore
        }
        if (!(obj instanceof Vector)) {
            return retVal;
        }
        
        retVal = true;
        Vector vectorLib = (Vector)obj;
        for (Object lib : vectorLib) {
            
            Method finalize = null;
            try {
                finalize = lib.getClass().getDeclaredMethod("finalize", new Class[0]);
            } catch (NoSuchMethodException e) {
            //ignore
            }
            
            if (finalize != null) {
                finalize.setAccessible(true);
                try {
                    finalize.invoke(lib, new Object[0]);
                } catch ( IllegalAccessException | InvocationTargetException e) {
                }
            }
        }
        return retVal;
    }

}
