
package pwnbrew.functions;

import java.io.IOException;
import java.util.Map;
import pwnbrew.MaltegoStub;
import pwnbrew.StubConfig;
import pwnbrew.misc.Constants;
import pwnbrew.misc.DebugPrinter;
import pwnbrew.misc.SocketUtilities;
import pwnbrew.network.ClientPortRouter;
import pwnbrew.network.control.ControlMessageManager;
import pwnbrew.xml.maltego.MaltegoMessage;

/**
 *
 * @author Securifera
 */
public class Reload extends Function {
    
    private static final String NAME_Class = Reload.class.getSimpleName();
        
    //Create the return msg
    private MaltegoMessage theReturnMsg = new MaltegoMessage();
  
    //==================================================================
    /**
     * Constructor
     * @param passedManager
     */
    public Reload( MaltegoStub passedManager ) {
        super(passedManager);
    }      
    
    //===================================================================
    /**
     * 
     * @param passedObjectStr
     * @return 
     */
    @Override
    public String run(String passedObjectStr) {
        
        String retStr = "";
        Map<String, String> objectMap = getKeyValueMap(passedObjectStr); 
         
        //Get server IP
        String serverIp = objectMap.get( Constants.SERVER_IP);
        if( serverIp == null ){
            DebugPrinter.printMessage( NAME_Class, "run", "No pwnbrew server IP provided", null);
            return retStr;
        }
         
        //Get server port
        String serverPortStr = objectMap.get( Constants.SERVER_PORT);
        if( serverPortStr == null ){
            DebugPrinter.printMessage( NAME_Class, "run", "No pwnbrew server port provided", null);
            return retStr;
        }
        
        //Get host id
        String hostIdStr = objectMap.get( Constants.HOST_ID);
        if( hostIdStr == null ){
            DebugPrinter.printMessage( NAME_Class, "run", "No host id provided", null);
            return retStr;
        }
         
        //Create the connection
        try {
            
            //Set the server ip and port
            StubConfig theConfig = StubConfig.getConfig();
            theConfig.setServerIp(serverIp);
            theConfig.setSocketPort(serverPortStr);
            
            //Set the client id
            Integer anInteger = SocketUtilities.getNextId();
            theConfig.setHostId(anInteger.toString());
            
            ControlMessageManager aCMManager = ControlMessageManager.getControlMessageManager();
            if( aCMManager == null ){
                aCMManager = ControlMessageManager.initialize( theManager );
            }

            //Get the port router
            int serverPort = Integer.parseInt( serverPortStr);
            ClientPortRouter aPR = (ClientPortRouter) theManager.getPortRouter( serverPort );

            //Initiate the file transfer
            if(aPR == null){
                DebugPrinter.printMessage( NAME_Class, "listclients", "Unable to retrieve port router.", null);
                return retStr;     
            }           
            
            //Set up the port wrapper
            theManager.initialize();
            
            //Connect to server
            boolean connected = aPR.ensureConnectivity( serverPort, theManager );
            if( connected ){
             
                //Get the client count
                int hostId = Integer.parseInt(hostIdStr);
                pwnbrew.network.control.messages.Reload aRelMsg = new pwnbrew.network.control.messages.Reload(hostId);               
                aCMManager.send(aRelMsg );    
                
                try {
                    //Sleep for a few seconds
                    Thread.sleep(3000);
                } catch (InterruptedException ex) {
                }
                
                retStr = theReturnMsg.getXml();
                            
            } else {
                StringBuilder aSB = new StringBuilder()
                        .append("Unable to connect to the Pwnbrew server at \"")
                        .append(serverIp).append(":").append(serverPort).append("\"");
                DebugPrinter.printMessage( NAME_Class, "listclients", aSB.toString(), null);
            }
            
        } catch (IOException ex) {
            DebugPrinter.printMessage( NAME_Class, "listclients", ex.getMessage(), ex );
        }
        
        return retStr;
    }
    
}
