

package	pwnbrew.socks;

import java.io.IOException;
import java.net.InetAddress;
import pwnbrew.misc.Constants;
import pwnbrew.misc.DebugPrinter;

public class Socks4Impl {
    
    private static final String NAME_Class = Socks4Impl.class.getSimpleName();
    
    public byte	SOCKS_Version = 0;
    public SocksHandler	m_Parent = null;
    public byte	socksCommand;
    public byte	DST_Port[] = null;
    public byte	DST_Addr[] = null;
    public byte	UserID[] = null;
    public String UID = "";

    //-------------------
    protected String m_ServerAddr = null;
    protected int m_nServerPort = 0;

    public InetAddress m_ExtLocalIP	= null;
    	
   
    //========================================================================
    /**
     * 
     * @param Parent 
     */
    public Socks4Impl( SocksHandler Parent ){

        m_Parent = Parent;
        DST_Addr = new byte[4];
        DST_Port = new byte[2];
    }


    
    //========================================================================
    /**
     * 
     * @param code
     * @return 
     */
    public String commName( byte code )	{

        switch( code )	{
                case 0x01: return "CONNECT";
                case 0x02: return "BIND";
                case 0x03: return "UDP Association";
                default:	return "Unknown Command";
        }

    }
   
    //========================================================================
    /**
     * 
     * @param code
     * @return 
     */
    public String replyName( byte code ){

        switch( code )	{
            case 0: return "SUCCESS";
            case 1: return "General SOCKS Server failure";
            case 2: return "Connection not allowed by ruleset";
            case 3: return "Network Unreachable";
            case 4: return "HOST Unreachable";
            case 5: return "Connection Refused";
            case 6: return "TTL Expired";
            case 7: return "Command not supported";
            case 8: return "Address Type not Supported";
            case 9: return "to 0xFF UnAssigned";

            case 90: return "Request GRANTED";
            case 91: return "Request REJECTED or FAILED";
            case 92: return "Request REJECTED - SOCKS server can't connect to Identd on the client";
            case 93: return "Request REJECTED - Client and Identd report diff user-ID";		 

            default:	return "Unknown Command";
        }
    }
    
    //========================================================================
    /**
     * 
     */
    public void calculateUserID(){

        String	s = UID + " ";
        UserID = s.getBytes();
        UserID[UserID.length-1] = 0x00;
    }


    //========================================================================
    /**
     * 
     * @return 
     */
    public byte getSuccessCode() {
        return 90;
    }
    
    //========================================================================
    /**
     * 
     * @return 
     */
    public byte getFailCode(){
        return 91;
    }
    
    //========================================================================
    /**
     * 
     * @return 
     */
    public String getServerAddress(){
        return m_ServerAddr;
    }

    //========================================================================
    /**
     * 
     * @return 
     */
    public int getServerPort(){ 
        return m_nServerPort;
    }

    //=========================================================================
    /**
     * 
     * @return 
     */
    public boolean calculateAddress() {

        // IP v4 Address Type
        InetAddress serverInet = Utils.getInstance().calcInetAddress( DST_Addr );
        m_ServerAddr = serverInet.getHostAddress();
        m_nServerPort = Utils.getInstance().calcPort( DST_Port[0], DST_Port[1] );

        return ( (m_ServerAddr != null) && (m_nServerPort >= 0) );
    }							
    
    //=========================================================================
    /**
     * 
     * @return 
     */
    protected byte getByte(){
        
        byte b;
        try{
            b = m_Parent.getByteFromClient();
        } catch( Exception e )	{
            b = 0;
        }
        return	b;
    }
    
    //=========================================================================
    /**
     * 
     * @param SOCKS_Ver
     * @throws Exception 
     */
    public void authenticate( byte SOCKS_Ver ) throws Exception {
        SOCKS_Version = SOCKS_Ver;
    }

    //=========================================================================
    /**
     * 
     * @throws Exception 
     */
    public void	getClientCommand() throws Exception {
       
        // Version was get in method Authenticate()
        socksCommand = getByte();

        DST_Port[0] = getByte();
        DST_Port[1] = getByte();

        for( int i=0; i<4; i++ ){
            DST_Addr[i] = getByte();
        }
         
        byte b;
        while( (b=getByte()) != 0x00 ){
            UID += (char)b;
        }
        calculateUserID();

        if( (socksCommand < Constants.SC_CONNECT) || (socksCommand > Constants.SC_BIND) )	{
            refuseCommand( (byte)91 );
            throw new Exception( "Socks 4 - Unsupported Command : "+commName( socksCommand ) );
        }

        if( !calculateAddress() ){  // Gets the IP Address 
            refuseCommand( (byte)92 );	// Host Not Exists...
            throw new Exception( "Socks 4 - Unknown Host/IP address '"+m_ServerAddr.toString() );
        }

        //DebugPrinter.printMessage( NAME_Class , "getClientCommand", "Accepted SOCKS 4 Command: \""+ commName( socksCommand )+"\"", null );
    } 
    
    //=========================================================================
    /**
     * 
     * @param ReplyCode 
     */
    public void replyCommand( byte ReplyCode ){
        
//        DebugPrinter.printMessage( NAME_Class , "getClientCommand", "Socks 4 reply: \""+replyName( ReplyCode)+"\"", null );

        byte[] retBytes = new byte[8];
        retBytes[0]= 0;
        retBytes[1]= ReplyCode;
        retBytes[2]= DST_Port[0];
        retBytes[3]= DST_Port[1];
        retBytes[4]= DST_Addr[0];
        retBytes[5]= DST_Addr[1];
        retBytes[6]= DST_Addr[2];
        retBytes[7]= DST_Addr[3];

        m_Parent.sendToClient( retBytes, retBytes.length );
    } 

    //=========================================================================
    /**
     * 
     * @param errorCode 
     */
    protected void refuseCommand( byte errorCode ){
        DebugPrinter.printMessage( NAME_Class, "refuseCommand",  "Socks 4 - Refuse Command: \""+replyName(errorCode)+"\"", null );
        replyCommand( errorCode );
    }	

    
    //=========================================================================
    /**
     * 
     * @throws Exception 
     */
    public void	connect() throws Exception {

//        DebugPrinter.printMessage( NAME_Class , "connect","Connecting...", null );
//        //Connect to the Remote Host
        try{
            m_Parent.connectToServer(m_ServerAddr, m_nServerPort );
        } catch( IOException e )	{
            refuseCommand( getFailCode() ); // Connection Refused
            throw new Exception("Socks 4 - Can't connect to " + m_ServerAddr + ":" + m_nServerPort );
        }
//
//        DebugPrinter.printMessage( NAME_Class, "Connected to "+ m_ServerIP.getHostAddress() + ":" + m_nServerPort );
        replyCommand( getSuccessCode() );
    }	

    //========================================================================
    /**
     * 
     * @throws java.io.IOException
     */
    public void udp() throws IOException {
        DebugPrinter.printMessage(UID, "udp","Error - Socks 4 don't support UDP Association!", null );
        DebugPrinter.printMessage(UID, "udp","Check your Software please...", null );
        refuseCommand( (byte)91 );	// SOCKS4 don't support UDP
    }
    
}
