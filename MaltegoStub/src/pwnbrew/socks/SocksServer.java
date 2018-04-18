

package	pwnbrew.socks;


import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import pwnbrew.MaltegoStub;
import pwnbrew.manager.DataManager;
import pwnbrew.misc.Constants;
import pwnbrew.misc.DebugPrinter;
import pwnbrew.misc.ManagedRunnable;
import pwnbrew.network.control.messages.SocksOperation;
import pwnbrew.utilities.SocketUtilities;


public class SocksServer extends ManagedRunnable {
    
    private static final String NAME_Class = SocksServer.class.getSimpleName();
    protected Thread m_TheThread = null;
    protected ServerSocket theServerSocket = null;
    protected final int theListenPort;
    
    private final int theHostId;
    private final int theChannelId;
         
    private final Map<Integer, SocksHandler> handlerMap = new HashMap<>();
    
    public static final int LISTEN_TIMEOUT = 200;
    public static final int DEFAULT_SERVER_TIMEOUT = 200;

    //==========================================================================
    /**
     * 
     * @param listenPort 
     * @param passedHostId 
     * @param passedChannelId 
     */
    public SocksServer( int listenPort, int passedHostId, int passedChannelId ) {
        super( Constants.Executor);
        theListenPort	= listenPort;
        theHostId = passedHostId;
        theChannelId = passedChannelId;
    }

    //==========================================================================
    /**
     * 
     * @return 
     */
    public int getPort()	{	
        return	theListenPort;	
    }
   
    //==========================================================================
    /**
     * 
     */
    @Override
    public void go(){
        listen();
        close();
    }

    //==========================================================================
    /**
     * 
     */
    public void close() {

        if( theServerSocket != null )	{
            try	{
                theServerSocket.close();
            } catch( IOException e )	{
            }
        }
        theServerSocket = null;
        
        //Kill all the threads
        synchronized( handlerMap ){
            for( Iterator<Map.Entry<Integer, SocksHandler>> anIter = handlerMap.entrySet().iterator(); anIter.hasNext(); ){
                //Add each entry
                Map.Entry<Integer, SocksHandler> anEntry = anIter.next();
                anEntry.getValue().shutdown();
            }
        }
        
        //Send message to create channel for socks proxy
        SocksOperation aSocksMsg = new SocksOperation( theHostId, SocksOperation.SOCKS_STOP );
        DataManager.send(MaltegoStub.getMaltegoStub(), aSocksMsg);
    }

    //==========================================================================
    /**
     * 
     * @throws java.net.BindException
     * @throws IOException 
     */
    private void prepareToListen() throws java.net.BindException, IOException {
       
        theServerSocket = new ServerSocket( theListenPort );
        theServerSocket.setSoTimeout( LISTEN_TIMEOUT );                
    }

    //==========================================================================
    /**
     * 
     */
    protected void listen() {

        try {
            prepareToListen();
        } catch( java.net.BindException ex ){
            DebugPrinter.printMessage( NAME_Class, "listen", "The Port "+theListenPort+" is in use !", null );
            DebugPrinter.printMessage( NAME_Class, "listen", ex.getMessage(), ex );
            return;
        } catch( IOException ex)	{
            DebugPrinter.printMessage( NAME_Class, "listen", ex.getMessage(), ex );            
            return;
        }

        //Main accept loop
        while( theServerSocket != null && !finished()  )
            checkClientConnection();
        
    }

    //=========================================================================
    /**
     * 
     */
    public void checkClientConnection()	{
        
        //Close() method was probably called.
        if( theServerSocket == null )
            return;

        try {
            
            Socket clientSocket = theServerSocket.accept();
            clientSocket.setSoTimeout( DEFAULT_SERVER_TIMEOUT );
            DebugPrinter.printMessage( NAME_Class, "SocksServer", "Connection from : " + getSocketInfo( clientSocket ), null );
            
            //register the socks handler
            registerSocksHandler(clientSocket);
            
            
        } catch( InterruptedIOException | SocketException e ) {
        } catch( Exception ex )	{
            DebugPrinter.printMessage( NAME_Class, "checkClientConnection", ex.getMessage(), ex ); 
        }
    }
    
    //=========================================================================
    /**
     * 
     * @param clientSocket 
     */
    public void registerSocksHandler( Socket clientSocket ){
        
        //Create new handler, register it, and start it
        int handlerId = SocketUtilities.getNextId();
        synchronized( handlerMap ){
            while( true ){
                SocksHandler aHandler = handlerMap.get(handlerId);
                if( aHandler != null )
                    handlerId = SocketUtilities.getNextId();
                else
                    break;
            }
        }

        //Create the socks handler and register it
        SocksHandler aHandler;
        synchronized( handlerMap ){
            aHandler = new SocksHandler( handlerId, clientSocket, theHostId, theChannelId );
            handlerMap.put(handlerId, aHandler);
        }
        aHandler.start();
        
    }

    
    //=========================================================================
    /**
     * 
     * @param sock
     * @return 
     */
    public String getSocketInfo( Socket sock )	{
	
	if( sock == null )
            return "<NA/NA:0>";
		
	return	"<"+iP2Str( sock.getInetAddress() )+":"+ sock.getPort() + ">";
    }
    
    //==========================================================================
    /**
     * 
     * @param IP
     * @return 
     */
    public String iP2Str( InetAddress IP ) {
        if( IP == null )	
            return "NA/NA";
        return	IP.getHostName()+"/"+IP.getHostAddress();
    }

    //==========================================================================
    /**
     * 
     * @param theHandlerId 
     * @return  
     */
    public SocksHandler getSocksHandler(int theHandlerId) {
        SocksHandler retHandler;
        synchronized(handlerMap){
            retHandler = handlerMap.get(theHandlerId);
        }
        return retHandler;
    }
    
    //==========================================================================
    /**
     * 
     * @param theHandlerId 
     * @return  
     */
    public SocksHandler removeSocksHandler(int theHandlerId) {
        SocksHandler retHandler;
        synchronized(handlerMap){
            retHandler = handlerMap.remove(theHandlerId);
        }
        return retHandler;
    }
            
}

