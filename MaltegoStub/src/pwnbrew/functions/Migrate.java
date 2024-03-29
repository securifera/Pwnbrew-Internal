
package pwnbrew.functions;

import java.awt.Insets;
import java.io.IOException;
import java.util.Map;
import javax.swing.JOptionPane;
import pwnbrew.MaltegoStub;
import pwnbrew.StubConfig;
import pwnbrew.generic.gui.ValidTextField;
import pwnbrew.log.LoggableException;
import pwnbrew.manager.DataManager;
import pwnbrew.misc.Constants;
import pwnbrew.misc.DebugPrinter;
import pwnbrew.utilities.SocketUtilities;
import pwnbrew.misc.StandardValidation;
import pwnbrew.network.ClientPortRouter;
import pwnbrew.xml.maltego.MaltegoTransformExceptionMessage;

/**
 *
 * @author Securifera
 */
public class Migrate extends Function {
    
    private static final String NAME_Class = Migrate.class.getSimpleName();
          
    //==================================================================
    /**
     * Constructor
     * @param passedManager
     */
    public Migrate( MaltegoStub passedManager ) {
        super(passedManager);
    }      
    
    //===================================================================
    /**
     * 
     * @param passedObjectStr 
     */
    @Override
    public void run(String passedObjectStr) {
        
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
        
        //Create the connection
        try {
            
            //Have the user manually put in the server ip
            ValidTextField aField = new ValidTextField( "0.0.0.0:0" );
            aField.setValidation( StandardValidation.KEYWORD_ClientConnect );
            aField.setMargin(new Insets(2,4,2,4));
            Object[] objMsg = new Object[]{ "Please enter the [IP address:port] of the new server.", " ", aField};
            Object retVal = JOptionPane.showOptionDialog(null, objMsg, "Enter [IP Address:Port]",
                   JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);

            //If the user pressed OK and the ip was valid
            if((Integer)retVal == JOptionPane.OK_OPTION && aField.isDataValid()){
                
                String newServerIp = aField.getText();    
            
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
                        DebugPrinter.printMessage( NAME_Class, "migrate", "Unable to create port router.", ex);
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
                    pwnbrew.network.control.messages.Migrate aMsg = new pwnbrew.network.control.messages.Migrate(hostId, newServerIp);               
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
            }
            
        } catch (IOException ex) {
            DebugPrinter.printMessage( NAME_Class, "listclients", ex.getMessage(), ex );
        }
        
    }
    
}
