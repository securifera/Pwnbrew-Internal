
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.ArrayList;
import java.util.List;

/**
 * An {@link URLStreamHandler} for a {@link MemoryBufferURLConnection}
 * 
 * @author mihi
 */
public class MemoryBufferURLStreamHandler extends URLStreamHandler {

	private List files = new ArrayList();

        @Override
	protected URLConnection openConnection(URL u) throws IOException {
            MemoryBufferURLConnection aMBU = new MemoryBufferURLConnection(u);            
            aMBU.setDefaultUseCaches(false);
            return aMBU;
	}
	
	public List getFiles() {
            return files;
	}
}
