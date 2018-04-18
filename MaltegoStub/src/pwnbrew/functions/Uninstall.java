
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
        
        //Get Name
        String nameStr = objectMap.get( Constants.NAME);
        if( nameStr == null ){
            DebugPrinter.printMessage( NAME_Class, "Uninstall", "No nameStr provided", null);
            return;
        }
                 
        String theMessage = "Are you sure you want to uninstall host: " + nameStr + "?";
        int dialogValue = JOptionPane.showConfirmDialog(null, theMessage, "Uninstall Host?", JOptionPane.YES_NO_OPTION);
        if ( dialogValue != JOptionPane.YES_OPTION )
            return;
        
        StubConfig theConfig = StubConfig.getConfig();
        theConfig.setServerIp(serverIp);
        theConfig.setSocketPort(serverPortStr);
        Integer anInteger = SocketUtilities.getNextId();
        theConfig.setHostId(anInteger.toString());
        int serverPort = Integer.parseInt( serverPortStr);
        ClientPortRouter aPR = (ClientPortRouter) theManager.getPortRouter( serverPort );
        if(aPR == null){
            try {
                aPR = (ClientPortRouter)DataManager.createPortRouter(theManager, serverPort, true);
            } catch (IOException ex) {
                DebugPrinter.printMessage( NAME_Class, "to_uninstall", "Unable to create port router.", ex);
                return;
            }
        }  
        theManager.initialize();
        try {
            
            aPR.ensureConnectivity( serverPort, theManager );
            
            //Get the client count
            int hostId = Integer.parseInt(hostIdStr);
            pwnbrew.network.control.messages.Uninstall aMsg = new pwnbrew.network.control.messages.Uninstall(hostId);
            DataManager.send( theManager, aMsg );
            
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
    
    }
    
}
