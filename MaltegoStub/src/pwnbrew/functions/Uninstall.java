
package pwnbrew.functions;

import java.io.IOException;
import java.util.Map;
import pwnbrew.MaltegoStub;
import pwnbrew.StubConfig;
import pwnbrew.log.LoggableException;
import pwnbrew.misc.Constants;
import pwnbrew.misc.DebugPrinter;
import pwnbrew.misc.SocketUtilities;
import pwnbrew.network.ClientPortRouter;
import pwnbrew.network.control.ControlMessageManager;
import pwnbrew.xml.maltego.MaltegoTransformExceptionMessage;

/**
 *
 * @author Securifera
 */
public class Uninstall extends Function {
    
    private static final String NAME_Class = Uninstall.class.getSimpleName();
          
    //==================================================================
    /**
     * Constructor
     * @param passedManager
     */
    public Uninstall( MaltegoStub passedManager ) {
        super(passedManager);
    }      
    
    //===================================================================
    /**
     * 
     * @param passedObjectStr 
     */
    @Override
    public void run(String passedObjectStr) {
        
        String retStr = "";
        Map<String, String> objectMap = getKeyValueMap(passedObjectStr); 
         
        //Get server IP
        String serverIp = objectMap.get( Constants.SERVER_IP);
        if( serverIp == null ){
            DebugPrinter.printMessage( NAME_Class, "Uninstall", "No pwnbrew server IP provided", null);
            return;
        }
         
        //Get server port
        String serverPortStr = objectMap.get( Constants.SERVER_PORT);
        if( serverPortStr == null ){
            DebugPrinter.printMessage( NAME_Class, "Uninstall", "No pwnbrew server port provided", null);
            return;
        }
        
        //Get host id
        String hostIdStr = objectMap.get( Constants.HOST_ID);
        if( hostIdStr == null ){
            DebugPrinter.printMessage( NAME_Class, "Uninstall", "No host id provided", null);
            return;
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
                DebugPrinter.printMessage( NAME_Class, "Uninstall", "Unable to retrieve port router.", null);
                return;     
            }           
            
            //Set up the port wrapper
            theManager.initialize();
            
            //Connect to server
            try {
                
                aPR.ensureConnectivity( serverPort, theManager );
             
                //Get the client count
                int hostId = Integer.parseInt(hostIdStr);
                pwnbrew.network.control.messages.Uninstall aMsg = new pwnbrew.network.control.messages.Uninstall(hostId);               
                aCMManager.send(aMsg );    
                
                try {
                    //Sleep for a few seconds
                    Thread.sleep(3000);
                } catch (InterruptedException ex) {
                }
                
                //Create a relay object
                pwnbrew.xml.maltego.Exception exMsg = new pwnbrew.xml.maltego.Exception( "Client uninstalled. Please remove the client from the graph.");
                MaltegoTransformExceptionMessage malMsg = theReturnMsg.getExceptionMessage();

                //Create the message list
                malMsg.getExceptionMessages().addExceptionMessage(exMsg); 
                
                retStr = theReturnMsg.getXml();
                            
            } catch( LoggableException ex ) {
                
                //Create a relay object
                pwnbrew.xml.maltego.Exception exMsg = new pwnbrew.xml.maltego.Exception( ex.getMessage() );
                MaltegoTransformExceptionMessage malMsg = theReturnMsg.getExceptionMessage();

                //Create the message list
                malMsg.getExceptionMessages().addExceptionMessage(exMsg);  
            }
            
        } catch (IOException ex) {
            DebugPrinter.printMessage( NAME_Class, "listclients", ex.getMessage(), ex );
        }
    
    }
    
}
