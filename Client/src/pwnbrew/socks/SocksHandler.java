


package	pwnbrew.socks;


import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import pwnbrew.manager.DataManager;
import pwnbrew.network.control.messages.SocksCreateHandlerAckMsg;
import pwnbrew.network.control.messages.SocksOperation;
import pwnbrew.utilities.Constants;
import pwnbrew.utilities.DebugPrinter;
import pwnbrew.utilities.ManagedRunnable;


public class SocksHandler extends ManagedRunnable {
    
    private static final String NAME_Class = SocksHandler.class.getSimpleName();
    
    private final SocksMessageManager theSMM;
    private final int theSocksHandlerId;
    private final int theSrcHostId;
    private final int theChannelId;
    private final String theConnectStr;
        
    private Socket theExternalSocket	= null;
    private byte[] socketBuffer		= null;
    private InputStream theExternalSocketInputStream = null;
    private BufferedOutputStream theExternalSocketOutputStream	= null;   
           

    //========================================================================
    /**
     * 
     * @param passedSMM
     * @param passedHostId
     * @param passedChannelId
     * @param passedHandlerId 
     * @param passedConnectStr 
     */
    public SocksHandler( SocksMessageManager passedSMM, int passedHostId, int passedChannelId, 
            int passedHandlerId, String passedConnectStr ) {	
        
        super(Constants.Executor);
        
        theSMM = passedSMM;
        theSocksHandlerId = passedHandlerId;
        theSrcHostId = passedHostId;
        theChannelId = passedChannelId;
        theConnectStr = passedConnectStr;
        socketBuffer = new byte[ Constants.DEFAULT_SOCKS_BUFFER_SIZE ];
//        DebugLog.getInstance().println( "Proxy Created." );
    }

    //=========================================================================
    /**
     * 
     */
    @Override
    public void	go(){
        
        boolean connected = true;
        try {
            //Connect to the server
            connectToServer();
        } catch (IOException ex) {
            DebugPrinter.printMessage( NAME_Class, ex.getMessage() );             
                            
//            //Send message to create channel for socks proxy
//            SocksOperation aSocksMsg = new SocksOperation( theSrcHostId, SocksOperation.HANDLER_STOP, theSocksHandlerId );
//            DataManager.send(theSMM.getPortManager(), aSocksMsg );
            connected = false;
//            return;
        }
        
        //Create message
        SocksCreateHandlerAckMsg anAck = new SocksCreateHandlerAckMsg(theSocksHandlerId, connected);
        anAck.setDestHostId(theSrcHostId);
     
        //Send back message
        DataManager.send(theSMM.getPortManager(), anAck);
        
        //If no connection, return
        if( !connected )
            return;
        
        //Main loop
        recvLoop();
        //Close
        close();
    }

    //=========================================================================
    /**
     * 
     */
    public void close()	{
                
        try {
            if( theExternalSocketOutputStream != null ){
                theExternalSocketOutputStream.flush();
                theExternalSocketOutputStream.close();
            }
        } catch( IOException e ){
        }

        try {
            if( theExternalSocket != null ){
                theExternalSocket.close();
            }
        } catch( IOException e ){
        }

        theExternalSocket = null;
      
//            DebugLog.getInstance().println( "Proxy Closed." );
    }

//    //=========================================================================
//    /**
//     * 
//     * @param buffer
//     * @param len 
//     */
//    public void sendToClient( byte[] buffer, int len )	{
//        
//        //Copy of bytes read
//        byte[] tempArr = Arrays.copyOf(buffer, len);
//        
//        //Send the file data
//        SocksMessage socksMsg = new SocksMessage(theSocksHandlerId, tempArr);  
//        socksMsg.setChannelId(theChannelId);
//        socksMsg.setDestHostId(theSrcHostId );   
//
//        //Send the message
//        DataManager.send( theSMM.getPortManager(), socksMsg);
//    }

//    //=========================================================================
//    /**
//     * 
//     * @param buffer 
//     */
//    public void sendToServer( byte[] buffer )	{
//        sendToServer( buffer, buffer.length );
//    }

    //=========================================================================
    /**
     * 
     * @param buffer
     * @param len 
     */
    public void sendToServer( byte[] buffer, int len ) {
        
        if( theExternalSocketOutputStream == null )
            return;
        if( len <= 0 || len > buffer.length )
            return;

        try {
            theExternalSocketOutputStream.write( buffer, 0, len );
            theExternalSocketOutputStream.flush();
        } catch( IOException ex )	{
            DebugPrinter.printMessage( NAME_Class, ex.getMessage() ); 
        }
    }

    //=========================================================================
    /**
     * 
     * @throws IOException
     * @throws UnknownHostException 
     */
    public void connectToServer() throws IOException, UnknownHostException {

        if( theConnectStr == null || theConnectStr.isEmpty() )	{
            close();
            DebugPrinter.printMessage( NAME_Class, "Invalid connection string." ); 
            return;
        }

        String[] connectArr = theConnectStr.split(":");
        //theExternalSocket = new Socket( connectArr[0], Integer.parseInt(connectArr[1]) );
        theExternalSocket = new Socket();
        theExternalSocket.connect(new InetSocketAddress(connectArr[0], Integer.parseInt(connectArr[1])), Constants.DEFAULT_PROXY_TIMEOUT);
        // This stops the request from dragging on after connection succeeds.
        //theExternalSocket.setSoTimeout(Constants.DEFAULT_PROXY_TIMEOUT);

        DebugPrinter.printMessage(NAME_Class, "Connected to "+ theConnectStr );
        prepareServer();
    }

    //=========================================================================
    /**
     * 
     * @throws IOException 
     */
    protected synchronized void prepareServer() throws IOException	{

        theExternalSocketInputStream  = theExternalSocket.getInputStream();
        theExternalSocketOutputStream = new BufferedOutputStream( theExternalSocket.getOutputStream() );

    }

    //==========================================================================
    /**
     * 
     */
    public void recvLoop(){

        boolean	isActive = true;
        int dlen;

        while( isActive ){

            //---> Check for Server data <---
            dlen = checkServerData();
            if( dlen < 0 )
                isActive = false;
            if( dlen > 0 ){                
                //Copy of bytes read
                byte[] tempArr = Arrays.copyOf(socketBuffer, dlen);

                //Send socks data
                SocksMessage socksMsg = new SocksMessage(theSocksHandlerId, tempArr);  
                socksMsg.setChannelId(theChannelId);
                socksMsg.setDestHostId(theSrcHostId );   

                //Send the message
                DataManager.send( theSMM.getPortManager(), socksMsg);
            }
        }
        
        //Send empty socks msg to close the connection
        SocksMessage socksMsg = new SocksMessage(theSocksHandlerId, new byte[0]);  
        socksMsg.setChannelId(theChannelId);
        socksMsg.setDestHostId(theSrcHostId );   

        //Send the message
        DataManager.send( theSMM.getPortManager(), socksMsg);
                
//        //Send message to create channel for socks proxy
//        SocksOperation aSocksMsg = new SocksOperation( theSrcHostId, SocksOperation.HANDLER_STOP, theSocksHandlerId );
//        DataManager.send(theSMM.getPortManager(), aSocksMsg );
    }

    //=========================================================================
    /**
     * 
     * @return 
     */
    public synchronized	int checkServerData()	{
           
        // The server connection is not open
        if( theExternalSocketInputStream == null )
            return -1;

        int dlen;
        try {
            socketBuffer = new byte[ Constants.DEFAULT_SOCKS_BUFFER_SIZE ];
            dlen = theExternalSocketInputStream.read(socketBuffer, 0, Constants.DEFAULT_SOCKS_BUFFER_SIZE );
        } catch( InterruptedIOException e ){
            return 0;
        } catch( IOException e )		{
            DebugPrinter.printMessage(NAME_Class, "Server connection Closed!" );
//            close();	//	Close the server on this exception
            return -1;
        }

//        if( dlen < 0 )
//            close();

        return	dlen;
            
    }    
 
//    //=========================================================================
//    /**
//     * 
//     * @param traffic 
//     */
//    public void logServerData( int traffic ) {
//        DebugPrinter.printMessage(NAME_Class, "Srv data : "+
//                                    comm.m_ServerIP.getHostName()+"/"+
//                                    comm.m_ServerIP.getHostAddress()+":"+
//                                    comm.m_nServerPort+"> : " + 
//                                    traffic +" bytes." );
//    }   	
}
