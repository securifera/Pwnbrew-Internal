import java.io.DataInputStream;
import java.io.OutputStream;

public interface Stage {
    
    public void start(DataInputStream in, OutputStream out, String[] passedParameters ) throws Exception;
    
}
