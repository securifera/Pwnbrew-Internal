
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
import pwnbrew.network.control.ControlMessageManager;
import pwnbrew.network.control.messages.ControlMessage;
import pwnbrew.network.control.messages.GetUpgradeFlag;
import pwnbrew.network.control.messages.UpgradeStagerRelay;
import pwnbrew.xml.maltego.MaltegoMessage;
import pwnbrew.xml.maltego.MaltegoTransformExceptionMessage;

/**
 *
 * @author Securifera
 */
public class Reload extends Function {
    
    private volatile boolean notified = false;
    private volatile boolean oldStager = false;
    private static final String NAME_Class = Reload.class.getSimpleName();
        
    //Create the return msg
    private final MaltegoMessage theReturnMsg = new MaltegoMessage();
  
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
                DebugPrinter.printMessage( NAME_Class, "reload", "Unable to create port router.", ex);
                return;
            }
        } 
        theManager.initialize();
        try {
            
            aPR.ensureConnectivity( serverPort, theManager );
            
            //Check if client can be upgraded
            ControlMessage aMsg = new GetUpgradeFlag( Constants.SERVER_ID,  hostIdStr);
            DataManager.send( theManager, aMsg);
            
            //Wait for the response
            waitToBeNotified( 180 * 1000);           
            
            if(oldStager){
                String theMessage = "Would you like to upgrade the stager prior to reloading the client?";
                int dialogValue = JOptionPane.showConfirmDialog(null, theMessage, "Upgrade stager?", JOptionPane.YES_NO_OPTION);
                
                if ( dialogValue == JOptionPane.YES_OPTION ){
                    //Upgrade the stager
                    UpgradeStagerRelay aRelMsg = new UpgradeStagerRelay(Constants.SERVER_ID,  hostIdStr);
                    DataManager.send( theManager, aRelMsg);
                    
                    //Wait for the response
                    waitToBeNotified( 180 * 1000);
                }
                
            }
            
            //Get the client count
            int hostId = Integer.parseInt(hostIdStr);
            pwnbrew.network.control.messages.Reload aRelMsg = new pwnbrew.network.control.messages.Reload(hostId);
            DataManager.send( theManager, aRelMsg);
            
            try {
                //Sleep for a few seconds
                Thread.sleep(3000);
            } catch (InterruptedException ex) {
            }
            
            retStr = theReturnMsg.getXml();
            
        } catch( LoggableException ex ) {
            
            //Create a relay object
            pwnbrew.xml.maltego.Exception exMsg = new pwnbrew.xml.maltego.Exception( ex.getMessage() );
            MaltegoTransformExceptionMessage malMsg = theReturnMsg.getExceptionMessage();
            
            //Create the message list
            malMsg.getExceptionMessages().addExceptionMessage(exMsg);
            
        }
    }
    
    //===============================================================
    /**
     * 
     * @param passedBool 
     */
    public synchronized void setUpgradeFlag( boolean passedBool ) {
        oldStager = passedBool;
        beNotified();
    }
    
}
