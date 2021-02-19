
package pwnbrew.functions;

import java.io.IOException;
import java.util.Map;
import javax.swing.JOptionPane;
import pwnbrew.MaltegoStub;
import pwnbrew.StubConfig;
import pwnbrew.log.LoggableException;
import pwnbrew.manager.DataManager;
import pwnbrew.misc.Constants;
import pwnbrew.misc.DebugPrinter;
import pwnbrew.utilities.SocketUtilities;
import pwnbrew.network.ClientPortRouter;
import pwnbrew.network.control.messages.SleepRelay;
import pwnbrew.xml.maltego.MaltegoTransformExceptionMessage;

/**
 *
 * @author Securifera
 */
public class Sleep extends Function {
    
    private static final String NAME_Class = Sleep.class.getSimpleName();
          
    //==================================================================
    /**
     * Constructor
     * @param passedManager
     */
    public Sleep( MaltegoStub passedManager ) {
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
            DebugPrinter.printMessage( NAME_Class, "run", "No pwnbrew server IP provided", null);
            return;
        }
         
        //Get server port
        String serverPortStr = objectMap.get( Constants.SERVER_PORT);
        if( serverPortStr == null ){
            DebugPrinter.printMessage( NAME_Class, "run", "No pwnbrew server port provided", null);
            return;
        }
        
        //Get host id
        String hostIdStr = objectMap.get( Constants.HOST_ID);
        if( hostIdStr == null ){
            DebugPrinter.printMessage( NAME_Class, "run", "No host id provided", null);
            return;
        }
        
        String theMessage = "Are you sure you want to put host to sleep?";
        int dialogValue = JOptionPane.showConfirmDialog(null, theMessage, "Sleep?", JOptionPane.YES_NO_OPTION);
        if ( dialogValue != JOptionPane.YES_OPTION ){
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

            //Get the port router
            int serverPort = Integer.parseInt( serverPortStr);
            ClientPortRouter aPR = (ClientPortRouter) theManager.getPortRouter( serverPort );

            //Initiate the file transfer
            if(aPR == null){
                try {
                    aPR = (ClientPortRouter)DataManager.createPortRouter(theManager, serverPort, true);
                } catch (IOException ex) {
                    DebugPrinter.printMessage( NAME_Class, "sleep", "Unable to create port router.", ex);
                    return;
                }
            }          

            //Set up the port wrapper
            theManager.initialize();

            //Connect to server
            try {
                
                aPR.ensureConnectivity( serverPort, theManager );

                //Get the client count
                int hostId = Integer.parseInt(hostIdStr);
                SleepRelay aMsg = new SleepRelay( Constants.SERVER_ID, hostId);               
                DataManager.send( theManager, aMsg);  

                try {
                    //Sleep for a few seconds
                    Thread.sleep(3000);
                } catch (InterruptedException ex) {
                }

            } catch( LoggableException ex ) {
                
                //Create a relay object
                pwnbrew.xml.maltego.Exception exMsg = new pwnbrew.xml.maltego.Exception( ex.getMessage() );
                MaltegoTransformExceptionMessage malMsg = theReturnMsg.getExceptionMessage();

                //Create the message list
                malMsg.getExceptionMessages().addExceptionMessage(exMsg);  
            }        
            
            retStr = theReturnMsg.getXml();
            
        } catch (IOException ex) {
            DebugPrinter.printMessage( NAME_Class, "listclients", ex.getMessage(), ex );
        }
    }
    
}
