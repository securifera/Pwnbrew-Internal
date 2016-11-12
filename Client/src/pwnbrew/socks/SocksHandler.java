


package	pwnbrew.socks;


import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import pwnbrew.manager.DataManager;
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
        
    private Socket m_ServerSocket	= null;
    private byte[] socketBuffer		= null;
    private InputStream m_ServerInput = null;
    private BufferedOutputStream m_ServerOutput	= null;   
    
    
//    private Socks4Impl	comm = null;    
//    private final Queue<byte[]> clientReceivedPackets = new LinkedList<>();
    private ByteBuffer currByteBuffer = null;
       

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
        socketBuffer = new byte[ Constants.DEFAULT_BUF_SIZE ];
//        DebugLog.getInstance().println( "Proxy Created." );
    }

    //==========================================================================
    /**
     * 
     */
    public void	stop()	{
//        try {
//            if( theClientSocket != null )
//                theClientSocket.close();
////            if( m_ServerSocket  != null )
////                m_ServerSocket.close();
//        } catch( IOException e ){
//        }

//        theClientSocket = null;
//        m_ServerSocket  = null;

        //    DebugLog.getInstance().println( "Proxy Stopped." );
//        m_TheThread.interrupt();
    }

    //=========================================================================
    /**
     * 
     */
    @Override
    public void	go(){

        //Wait for first packet
//        waitToBeNotified();
        
        try {
            //Connect to the server
            connectToServer();
        } catch (IOException ex) {
            DebugPrinter.printMessage( NAME_Class, ex.getMessage() ); 
            return;
        }
        
        //Main loop
        relay();
        //Close
        close();
    }

    //=========================================================================
    /**
     * 
     */
    public void close()	{
                
        try {
            if( m_ServerOutput != null ){
                m_ServerOutput.flush();
                m_ServerOutput.close();
            }
        } catch( IOException e ){
        }

        try {
            if( m_ServerSocket != null ){
                m_ServerSocket.close();
            }
        } catch( IOException e ){
        }

        m_ServerSocket = null;
      
//            DebugLog.getInstance().println( "Proxy Closed." );
    }

    //=========================================================================
    /**
     * 
     * @param buffer 
     */
    public void sendToClient( byte[] buffer )	{
        sendToClient( buffer, buffer.length );
    }

    //=========================================================================
    /**
     * 
     * @param buffer
     * @param len 
     */
    public void sendToClient( byte[] buffer, int len )	{
        
        //Copy of bytes read
        byte[] tempArr = Arrays.copyOf(buffer, len);
        
        //Send the file data
        SocksMessage socksMsg = new SocksMessage(theSocksHandlerId, tempArr);  
        socksMsg.setChannelId(theChannelId);
        socksMsg.setDestHostId(theSrcHostId );   

        //Send the message
        DataManager.send( theSMM.getPortManager(), socksMsg);
    }

    //=========================================================================
    /**
     * 
     * @param buffer 
     */
    public void sendToServer( byte[] buffer )	{
        sendToServer( buffer, buffer.length );
    }

    //=========================================================================
    /**
     * 
     * @param buffer
     * @param len 
     */
    public void sendToServer( byte[] buffer, int len )	{
        if( m_ServerOutput == null )
            return;
        if( len <= 0 || len > buffer.length )
            return;

        try {
            m_ServerOutput.write( buffer, 0, len );
            m_ServerOutput.flush();
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
        m_ServerSocket = new Socket( connectArr[0], Integer.parseInt(connectArr[1]) );
        m_ServerSocket.setSoTimeout( Constants.DEFAULT_PROXY_TIMEOUT );
        m_ServerSocket.setKeepAlive(true);

//        DebugLog.getInstance().println( "Connected to "+DebugLog.getInstance().getSocketInfo( m_ServerSocket ) );
        prepareServer();
    }

    //=========================================================================
    /**
     * 
     * @throws IOException 
     */
    protected synchronized void prepareServer() throws IOException	{

        m_ServerInput  = m_ServerSocket.getInputStream();
        
        
        m_ServerOutput = new BufferedOutputStream( m_ServerSocket.getOutputStream() );
        //m_ServerOutput = m_ServerSocket.getOutputStream();

    }

//    //=========================================================================
//    /**
//     * 
//     */
//    public void processRelay()	{
//
//        try {
//            
//            byte SOCKS_Version	= getByteFromClient();
//
//            switch( SOCKS_Version ){
//                case Constants.SOCKS4_Version:
//                    comm = new Socks4Impl( this );
//                    break;
////                case Constants.SOCKS5_Version:	
////                    comm = new Socks5Impl( this );	
////                    break;
//                default:	
//                    DebugPrinter.printMessage( NAME_Class, "Invalid SOKCS version : "+SOCKS_Version );
//                    return;
//            }
//            DebugPrinter.printMessage( NAME_Class, "Accepted SOCKS "+SOCKS_Version+" Request." );
//
//            comm.authenticate( SOCKS_Version );
//            comm.getClientCommand();
//
//            switch ( comm.socksCommand ){
//                case Constants.SC_CONNECT:
//                    comm.connect();
//                    relay();
//                    break;
//
//                case Constants.SC_BIND:
////                    comm.bind();
////                    relay();
//                    DebugPrinter.printMessage( NAME_Class, "BIND not supported." );
//                    break;
//
//                case Constants.SC_UDP:
//                    comm.udp();
//                    break;
//            }
//        } catch( Exception ex )	{
//            DebugPrinter.printMessage( NAME_Class, ex.getMessage() );
//        }
//    } 

//    //==========================================================================
//    /**
//     * 
//     * @return
//     * @throws Exception 
//     */
//    public byte getByteFromClient() throws Exception {
//        int aByte=0;
//        if( currByteBuffer == null ){
//            byte[] aByteArr;
//            synchronized(clientReceivedPackets){
//                aByteArr = clientReceivedPackets.poll();
//            }
//            //Wrap bytes in ByteBuffer
//            if( aByteArr != null){
//                currByteBuffer = ByteBuffer.wrap(aByteArr);
//            }
//        }
//        
//        if( currByteBuffer != null && currByteBuffer.hasRemaining() ){
//            aByte = currByteBuffer.get();
//        } else {
//            throw new Exception("No data to read.");
//        }
//        
//        return (byte)aByte; // return loaded byte
//
//    } 

    //==========================================================================
    /**
     * 
     */
    public void relay(){

        boolean	isActive = true;
        int dlen;

        while( isActive ){

        //---> Check for client data <---

//            dlen = checkClientData();
//            if( dlen < 0 )	
//                isActive = false;
//            if( dlen > 0 ){
////                logClientData( dlen );
//                sendToServer( socketBuffer, dlen );
//            }

            //---> Check for Server data <---
            dlen = checkServerData();

            if( dlen < 0 )
                isActive = false;
            if( dlen > 0 ){
//                logServerData( dlen );
                sendToClient( socketBuffer, dlen );
            }

            Thread.currentThread();
            Thread.yield();
        }	// while
        
        
        //Send message to create channel for socks proxy
        SocksOperation aSocksMsg = new SocksOperation( theSrcHostId, SocksOperation.HANDLER_STOP, theSocksHandlerId );
        DataManager.send(theSMM.getPortManager(), aSocksMsg );
    }


//    //=========================================================================
//    /**
//     * 
//     * @return 
//     */
//    public synchronized	int checkClientData()	{
//            
//        int dlen = 0;
//        if( currByteBuffer != null ){
//            socketBuffer = Arrays.copyOfRange(currByteBuffer.array(), currByteBuffer.position(),currByteBuffer.limit());
//            currByteBuffer = null;
//            dlen = socketBuffer.length;
//        } else {
//            synchronized( clientReceivedPackets ){
//                socketBuffer = clientReceivedPackets.poll();
//            }
//            
//            if( socketBuffer != null )
//                dlen = socketBuffer.length;
//            
//        }
//
//        return dlen;
//           
//    }

    //=========================================================================
    /**
     * 
     * @return 
     */
    public synchronized	int checkServerData()	{
           
        // The server connection is not open
        if( m_ServerInput == null )
            return -1;

        int dlen;
        try {
            socketBuffer = new byte[ Constants.DEFAULT_BUF_SIZE ];
            dlen = m_ServerInput.read(socketBuffer, 0, Constants.DEFAULT_BUF_SIZE );
        } catch( InterruptedIOException e ){
            return 0;
        } catch( IOException e )		{
            DebugPrinter.printMessage(NAME_Class, "Server connection Closed!" );
            close();	//	Close the server on this exception
            return -1;
        }

        if( dlen < 0 )
            close();

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

//    //=========================================================================
//    /**
//     * 
//     * @param traffic 
//     */
//    public void logClientData( int traffic )	{
//        DebugPrinter.printMessage(NAME_Class, "Cli data : "+
//                                    comm.m_ServerIP.getHostName()+"/"+
//                                    comm.m_ServerIP.getHostAddress()+":"+
//                                    comm.m_nServerPort+"> : " + 
//                                    traffic +" bytes." );
//    }
    
//    //=========================================================================
//    /**
//     * 
//     * @param bytes 
//     */
//    public void queueSocksMsg(byte[] bytes) {
//        synchronized(clientReceivedPackets){
//            clientReceivedPackets.add(bytes);
//        }
//        beNotified();
//    }
	
}
