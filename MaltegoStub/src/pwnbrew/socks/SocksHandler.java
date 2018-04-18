


package	pwnbrew.socks;


import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import pwnbrew.MaltegoStub;
import pwnbrew.manager.DataManager;
import pwnbrew.misc.Constants;
import pwnbrew.misc.DebugPrinter;
import pwnbrew.misc.ManagedRunnable;
import pwnbrew.network.control.ControlMessageManager;
import pwnbrew.network.control.messages.SocksCreateHandlerMsg;
import pwnbrew.network.control.messages.SocksOperation;



public class SocksHandler extends ManagedRunnable {
    
    private static final String NAME_Class = SocksHandler.class.getSimpleName();
    
    private final int theSocksHandlerId;
    private final Socket theClientSocket;
    
    public byte[] socketBuffer		= null;
    public InputStream theClientInputStream	= null;
    public OutputStream theClientOutputStream	= null;
    
    private final int theDstId;
    private final int theChannelId;
        
    public static final int DEFAULT_PROXY_TIMEOUT = 10;
    public static final int DEFAULT_SOCKS_BUFFER_SIZE = 65500;;
    
    private boolean connected = false;

    //========================================================================
    /**
     * 
     * @param passedHandlerId
     * @param clientSocket 
     * @param passedDstId 
     * @param passedChannelId 
     */
    public SocksHandler( int passedHandlerId, Socket clientSocket, int passedDstId, int passedChannelId ) {	
        
        super(Constants.Executor);
        
        theSocksHandlerId = passedHandlerId;
        theClientSocket = clientSocket;
                
        theDstId = passedDstId;
        theChannelId = passedChannelId;
        
        if( theClientSocket != null )	{
            try	{
                theClientSocket.setSoTimeout( DEFAULT_PROXY_TIMEOUT );
            } catch( SocketException ex ) {
               DebugPrinter.printMessage( NAME_Class, "SocksHandler()", ex.getMessage(), ex );  
            }
        }

        socketBuffer = new byte[ DEFAULT_SOCKS_BUFFER_SIZE ];
//        DebugLog.getInstance().println( "Proxy Created." );
    }
    
    //========================================================================
    /**
     * 
     * @return 
     */
    public Socket getClientSocket(){
        return theClientSocket;
    }

    //==========================================================================
    /**
     * 
     */
    public void	stop()	{
        try {
            if( theClientSocket != null )
                theClientSocket.close();
        } catch( IOException e ){
        }
    }

    //=========================================================================
    /**
     * 
     */
    @Override
    public void	go(){
        
        if( !prepareClient() )	{
            //DebugLog.getInstance().error( "Proxy - client socket is null !" );
            return;
        }

        processRelay();

        //DebugPrinter.printMessage( NAME_Class, "close", "Shutting down Sockshandler thread.", null );
        
        //Close
        close();
    }
    
    //=========================================================================
    /**
     * 
     */
    public void readLoop(){
        
        while( !theClientSocket.isClosed() && !finished() ){
        
            //Receive data
            int dlen = checkClientData();
            if( dlen < 0 )	
                break;
            
            //Send the bytes to the server from the socks client
            if( dlen > 0 ){
//                logClientData( dlen );
                sendToServer( socketBuffer, dlen );
            }
                                        
        } 
        
        //Send message to create channel for socks proxy
        SocksOperation aSocksMsg = new SocksOperation( theDstId, SocksOperation.HANDLER_STOP, theSocksHandlerId );
        DataManager.send( MaltegoStub.getMaltegoStub(), aSocksMsg );        
        
    }
    
    //=========================================================================
    /**
     * 
     */
    public void processRelay()	{

        Socks4Impl comm = null;    
        try {
            
            byte SOCKS_Version	= getByteFromClient();

            switch( SOCKS_Version ){
                case Constants.SOCKS4_Version:
                    comm = new Socks4Impl( this );
                    break;
                case Constants.SOCKS5_Version:	
                    comm = new Socks5Impl( this );	
                    break;
                default:	
                    DebugPrinter.printMessage( NAME_Class, "processRelay","Invalid SOKCS version : "+SOCKS_Version, null );
                    return;
            }
            
            //DebugPrinter.printMessage( NAME_Class, "processRelay","Accepted SOCKS "+SOCKS_Version+" Request.", null );

            comm.authenticate( SOCKS_Version );
            comm.getClientCommand();

            switch ( comm.socksCommand ){
                case Constants.SC_CONNECT:
                    comm.connect();
                    readLoop();
                    break;

//                case Constants.SC_BIND:
////                    comm.bind();
////                    relay();
//                    DebugPrinter.printMessage( NAME_Class, "processRelay", "SOCKS command not supported.", null );
//                    break;

                case Constants.SC_UDP:
                    comm.udp();
                    break;
                default:
                    DebugPrinter.printMessage( NAME_Class, "processRelay", "SOCKS command not supported.", null );
                    break;
            }
        } catch( Exception ex )	{
            DebugPrinter.printMessage( NAME_Class, "processRelay", ex.getMessage(), null );
        }
    } 

    //=========================================================================
    /**
     * 
     */
    public void close()	{
        
        
        //DebugPrinter.printMessage( NAME_Class, "close", "Closing socket.", null );
        
        //Close all of the streams
        try {
            if( theClientOutputStream != null ){
                theClientOutputStream.flush();
                theClientOutputStream.close();
            }
        } catch( IOException e ){
        }
        
        try {
            if( theClientSocket != null ){
                theClientSocket.close();
            }
        } catch( IOException e ){
        }
               
//            DebugLog.getInstance().println( "Proxy Closed." );
    }

    //=========================================================================
    /**
     * 
     * @param buffer
     * @param len 
     * @return  
     */
    public boolean sendToClient( byte[] buffer, int len )	{
        
        if( theClientSocket.isClosed() ){
            DebugPrinter.printMessage( NAME_Class, "sendToClient()", "Unable to send, socket closed.", null );  
            return false;
        }
            
        if( theClientOutputStream == null )	
            return false;
        if( len <= 0 || len > buffer.length )
            return false;

        try {
            theClientOutputStream.write( buffer, 0, len );
            theClientOutputStream.flush();
        } catch( IOException ex ){
            DebugPrinter.printMessage( NAME_Class, "sendToClient()", ex.getMessage(), ex );  
//            DebugLog.getInstance().error( "Sending data to client" );
            return false;
        }
        
        return true;
       
    }

    //=========================================================================
    /**
     * 
     * @param buffer
     * @param len 
     */
    public void sendToServer( byte[] buffer, int len )	{
        
        //Copy of bytes read
        byte[] tempArr = Arrays.copyOf(buffer, len);
                        
        //Send the file data
        SocksMessage socksMsg = new SocksMessage(theSocksHandlerId, tempArr);  
        socksMsg.setChannelId(theChannelId);
        socksMsg.setDestHostId(theDstId );   

        //Send the message
        DataManager.send( MaltegoStub.getMaltegoStub(), socksMsg );    
        
    }

    //==========================================================================
    /**
     * 
     * @return 
     */
    public synchronized boolean prepareClient()	{
        
        if( theClientSocket == null )
            return false;

        try {
            theClientInputStream = theClientSocket.getInputStream();
            theClientOutputStream= theClientSocket.getOutputStream();
        } catch( IOException ex )	{
            DebugPrinter.printMessage( NAME_Class, "prepareClient()", ex.getMessage(), ex ); 
            return false;
        }
        return true;
    }

    //=========================================================================
    /**
     * 
     * @return 
     */
    public synchronized	int checkClientData()	{
            
        //The client side is not opened.
        if( theClientInputStream == null )
            return -1;

        int dlen;
        try {
            dlen = theClientInputStream.read(socketBuffer, 0, DEFAULT_SOCKS_BUFFER_SIZE );
        } catch( InterruptedIOException e ){
            return 0;
        } catch( IOException e )		{
//            DebugLog.getInstance().println( "Client connection Closed!" );
            return -1;
        }


        return	dlen;
           
    }
      
    //==========================================================================
    /**
     * 
     * @return
     * @throws Exception 
     */
    public byte getByteFromClient() throws Exception {
        int b;
        while( theClientSocket != null ) {

            try	{
                b = theClientInputStream.read();
            } catch( InterruptedIOException e )		{
                Thread.yield();
                continue;
            }
            return (byte)b; // return loaded byte

        } // while...
        throw new Exception( "Interrupted Reading GetByteFromClient()");
    } 
    
    //=========================================================================
    /**
     * 
     * @param passedBool 
     */
    public void setConnected( boolean passedBool ){
        connected = passedBool;
    }
    
     //=========================================================================
    /**
     * 
     * @param server
     * @param port
     * @return 
     * @throws IOException 
     */
    public boolean connectToServer( String server, int port ) throws IOException {
                
        //Create the connect string
        String connectStr = server + ":" +Integer.toString(port);

        //Send the socks start msg
        SocksCreateHandlerMsg aMsg = new SocksCreateHandlerMsg( theDstId, theSocksHandlerId, connectStr);
        DataManager.send( MaltegoStub.getMaltegoStub(), aMsg);
        
        //For for connection
        waitToBeNotified();
        
        return connected;
        
    }
}
