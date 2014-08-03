
package pwnbrew.functions;

import java.io.IOException;
import java.util.Map;
import javax.swing.JOptionPane;
import pwnbrew.MaltegoStub;
import pwnbrew.StubConfig;
import pwnbrew.misc.Constants;
import pwnbrew.misc.DebugPrinter;
import pwnbrew.misc.SocketUtilities;
import pwnbrew.network.ClientPortRouter;
import pwnbrew.network.control.ControlMessageManager;
import pwnbrew.network.control.messages.ControlMessage;
import pwnbrew.network.control.messages.GetUpgradeFlag;
import pwnbrew.network.control.messages.UpgradeStagerRelay;
import pwnbrew.xml.maltego.MaltegoMessage;

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
                
                //Check if client can be upgraded
                ControlMessage aMsg = new GetUpgradeFlag( Constants.SERVER_ID,  hostIdStr);
                aCMManager.send(aMsg);
                
                //Wait for the response
                waitToBeNotified( 180 * 1000);
                
                if(oldStager){
                    String theMessage = "Would you like to upgrade the stager prior to reloading the client?";
                    int dialogValue = JOptionPane.showConfirmDialog(null, theMessage, "Upgrade stager?", JOptionPane.YES_NO_OPTION);

                    if ( dialogValue == JOptionPane.YES_OPTION ){
                        //Upgrade the stager
                        UpgradeStagerRelay aRelMsg = new UpgradeStagerRelay(Constants.SERVER_ID,  hostIdStr);               
                        aCMManager.send(aRelMsg );
                        
                        //Wait for the response
                        waitToBeNotified( 180 * 1000);
                    }
                           
                }  
             
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
                String aSB = String.valueOf("Unable to connect to the Pwnbrew server at \"" + serverIp + ":") + Integer.toString(serverPort) + "\"";
                DebugPrinter.printMessage( NAME_Class, "listclients", aSB, null);
            }
            
        } catch (IOException ex) {
            DebugPrinter.printMessage( NAME_Class, "listclients", ex.getMessage(), ex );
        }
        
        return retStr;
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
    
     //===============================================================
    /**
     * Notifies the thread
    */
    public synchronized void beNotified() {
        notified = true;
        notifyAll();
    }
    
    // ==========================================================================
    /**
    * Causes the calling {@link Thread} to <tt>wait()</tt> until notified by
    * another.
    * <p>
    * <strong>This method most certainly "blocks".</strong>
     * @param anInt
    */
    protected synchronized void waitToBeNotified( Integer... anInt ) {

        while( !notified ) {

            try {
                
                //Add a timeout if necessary
                if( anInt.length > 0 ){
                    
                    wait( anInt[0]);
                    break;
                    
                } else {
                    wait(); //Wait here until notified
                }
                
            } catch( InterruptedException ex ) {
            }

        }
        notified = false;
    }
    
}
