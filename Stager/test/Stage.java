import java.io.DataInputStream;
import java.io.OutputStream;
import sun.net.www.protocol.jar.URLJarFileCallBack;

public interface Stage extends URLJarFileCallBack{
    
    public void start(DataInputStream in, OutputStream out, String[] passedParameters ) throws Exception;
    
}
