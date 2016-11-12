package pwnbrew.socks;

//
//
//package	pwnbrew.socks;
//
//
//import java.io.IOException;
//import java.io.InterruptedIOException;
//import java.net.DatagramPacket;
//import java.net.DatagramSocket;
//import java.net.InetAddress;
//import java.net.UnknownHostException;
//
//import pwnbrew.utilities.Constants;
//import pwnbrew.utilities.DebugPrinter;
//
//
//
//public class Socks5Impl extends Socks4Impl implements SocksCommonInterface {
//        
//    private static final String NAME_Class = Socks5Impl.class.getSimpleName();
//	
//    protected DatagramSocket DGSocket = null;
//    protected DatagramPacket DGPack = null;
//
//    private InetAddress UDP_IA = null;
//    private int UDP_port = 0;
//
//    //-------------------
//
//
////	public			byte	SOCKS_Version;	// Version of SOCKS
////	public			byte	Command;		// Command code
//    public			byte 	RSV;			// Reserved.Must be'00'
//    public			byte	ATYP;			// Address Type
////	public			byte[]	DST_Addr;		// Destination Address
////	public			byte[]	DST_Port;		// Destination Port
//                                                                                    // in Network order
//
//    static final int ADDR_Size[]={ -1, //'00' No such AType 
//                                   4, //'01' IP v4 - 4Bytes
//                                  -1, //'02' No such AType
//                                  -1, //'03' First Byte is Len
//                                  16  //'04' IP v6 - 16bytes
//                                   };
//
//
//
//    //=========================================================================
//    /**
//     * 
//     * @param Parent 
//     */
//    public Socks5Impl( SocksHandler Parent ){
//
//        super( Parent );
//        DST_Addr = new byte[Constants.MAX_ADDR_LEN]; 
//    }
//
//    //=========================================================================
//    /**
//     * 
//     * @param AType
//     * @param addr
//     * @return 
//     */
//    public InetAddress calcInetAddress( byte AType, byte[] addr ) {
//        
//        InetAddress	IA = null;
//
//        switch( AType )	{
//                    // Version IP 4
//            case 0x01:	
//                IA = Utils.getInstance().calcInetAddress( addr );
//                break;
//                    // Version IP DOMAIN NAME
//            case 0x03:	
//                if( addr[0] <= 0  ){
//                    DebugPrinter.printMessage( NAME_Class, "SOCKS 5 - calcInetAddress() : BAD IP in command - size : " + addr[0] );
//                    return null;	
//                }
//                String sIA= "";
//                for( int i = 1; i <= addr[0]; i++ ){
//                    sIA += (char)addr[i];
//                }
//                try {
//                    IA = InetAddress.getByName( sIA );
//                } catch( UnknownHostException e )	{
//                    return null;
//                }
//                break;
//            default:	return null;
//            }
//            return IA;
//    } // calcInetAddress()
//
//    
//    //--- Reply Codes ---
//    //=========================================================================
//    /**
//     * 
//     * @return 
//     */
//    @Override
//    public byte getSuccessCode(){
//        return 00;
//    }
//    
//    //=========================================================================
//    /**
//     * 
//     * @return 
//     */
//    @Override
//    public byte getFailCode() {
//        return 04;
//    }
//
//    //=========================================================================
//    /**
//     * 
//     * @return 
//     */
//    @Override
//    public boolean calculateAddress(){
//
//        m_ServerIP		= calcInetAddress( ATYP, DST_Addr );
//        m_nServerPort	= Utils.getInstance().calcPort( DST_Port[0], DST_Port[1] );
//
////        m_ClientIP		= m_Parent.m_ClientSocket.getInetAddress();
////        m_nClientPort	= m_Parent.m_ClientSocket.getPort();
//
//        return ( (m_ServerIP != null) && (m_nServerPort >= 0) );
//    }							
//
//    
//    //=========================================================================
//    /**
//     * 
//     * @param SOCKS_Ver
//     * @throws Exception 
//     */
//    @Override
//    public void authenticate( byte SOCKS_Ver ) throws Exception {
//
//        super.authenticate( SOCKS_Ver ); // Sets SOCKS Version...
//
//        if( SOCKS_Version == Constants.SOCKS5_Version )	{	
//            if( !checkAuthentication() )	{// It reads whole Cli Request
//                    refuseAuthentication("SOCKS 5 - Not Supported Authentication!");
//                    throw new Exception("SOCKS 5 - Not Supported Authentication.");
//            }
//            acceptAuthentication();
//        }// if( SOCKS_Version...
//        else	{
//            refuseAuthentication( "Incorrect SOCKS version : "+SOCKS_Version );
//            throw new Exception( "Not Supported SOCKS Version -'"+SOCKS_Version + "'");
//        }
//    } 
//
//    //=========================================================================
//    /**
//     * 
//     * @param msg 
//     */
//    public void refuseAuthentication( String msg ){
//
//        DebugPrinter.printMessage( NAME_Class, "SOCKS 5 - Refuse Authentication: '"+msg+"'" );
//        m_Parent.sendToClient( Constants.SRE_Refuse );
//    }
//
//    //=========================================================================
//    /**
//     * 
//     */
//    public void acceptAuthentication(){
//        DebugPrinter.printMessage( NAME_Class, "SOCKS 5 - Accepts Auth. method 'NO_AUTH'" );
//        byte[] tSRE_Accept = Constants.SRE_Accept;
//        tSRE_Accept[0] = SOCKS_Version;
//        m_Parent.sendToClient( tSRE_Accept );
//    }
//
//    //=========================================================================
//    /**
//     * 
//     * @return
//     * @throws Exception 
//     */
//    public boolean checkAuthentication() throws Exception {
//        //boolean	Have_NoAuthentication = false;
//        byte Methods_Num = getByte();
//        String Methods = "";
//
//        for( int i=0; i<Methods_Num; i++ ){
//            Methods += ",-" + getByte() + '-';
//        }
//        
//        return	( (Methods.indexOf( "-0-"  ) != -1) || (Methods.indexOf( "-00-" ) != -1) );
//    }
//
//
//    //=========================================================================
//    /**
//     * 
//     * @throws Exception 
//     */
//    @Override
//    public void getClientCommand() throws Exception {
//        
//        int Addr_Len;
//
//        SOCKS_Version = getByte();
//        socksCommand = getByte();
//        RSV = getByte();
//        ATYP = getByte();
//
//        Addr_Len = ADDR_Size[ATYP];
//        DST_Addr[0] =  getByte();
//        if( ATYP==0x03 ){
//            Addr_Len = DST_Addr[0]+1;
//        }
//
//        for( int i=1; i<Addr_Len; i++ )	{
//            DST_Addr[i]= getByte();
//        }
//        DST_Port[0]	= getByte();
//        DST_Port[1]	= getByte();
//
//        if( SOCKS_Version != Constants.SOCKS5_Version )	{
//            DebugPrinter.printMessage( NAME_Class, "SOCKS 5 - Incorrect SOCKS Version of Command: "+
//                                     SOCKS_Version );
//            refuseCommand( (byte)0xFF );
//            throw new Exception("Incorrect SOCKS Version of Command: "+ 
//                                                          SOCKS_Version);
//        }
//
//        if( (socksCommand < Constants.SC_CONNECT) || (socksCommand > Constants.SC_UDP) ){
//            DebugPrinter.printMessage( NAME_Class, "SOCKS 5 - GetClientCommand() - Unsupported Command : \"" + commName( socksCommand )+"\"" );
//            refuseCommand( (byte)0x07 );
//            throw new Exception("SOCKS 5 - Unsupported Command: \"" + socksCommand +"\"" );
//        }
//
//        if( ATYP == 0x04 ){
//            DebugPrinter.printMessage( NAME_Class, "SOCKS 5 - GetClientCommand() - Unsupported Address Type - IP v6" );
//            refuseCommand( (byte)0x08 );
//            throw new Exception( "Unsupported Address Type - IP v6" );
//        }
//
//        if( (ATYP >= 0x04) || (ATYP <=0) ){
//            DebugPrinter.printMessage( NAME_Class, "SOCKS 5 - GetClientCommand() - Unsupported Address Type: " + ATYP );
//            refuseCommand( (byte)0x08 );
//            throw new Exception( "SOCKS 5 - Unsupported Address Type: " + ATYP );
//        }
//
//        if( !calculateAddress() ){  // Gets the IP Address 
//            refuseCommand( (byte)0x04 );// Host Not Exists...
//            throw new Exception( "SOCKS 5 - Unknown Host/IP address '" + m_ServerIP.toString()+"'" );
//        }
//
//        DebugPrinter.printMessage( NAME_Class, "SOCKS 5 - Accepted SOCKS5 Command: \""+commName(socksCommand)+"\"" );
//    }  
//
//    //=========================================================================
//    /**
//     * 
//     * @param replyCode 
//     */
//    @Override
//    public void replyCommand( byte replyCode ) {
//        DebugPrinter.printMessage( NAME_Class, "SOCKS 5 - Reply to Client \"" + replyName(replyCode)+"\"" );
//
//        int	pt = 0;
//
//
//        byte[]	REPLY	= new byte[10];
//        byte	IP[]	= new byte[4];
//
//        if( m_Parent.m_ServerSocket != null )	{
//            //IA = m_Parent.m_ServerSocket.getInetAddress();
//            //DN = IA.toString();
//            pt = m_Parent.m_ServerSocket.getLocalPort();
//        } else	{
//            IP[0]=0;
//            IP[1]=0;
//            IP[2]=0;
//            IP[3]=0;
//            pt = 0;
//        }
//
//        REPLY[0] = Constants.SOCKS5_Version;
//        REPLY[1] = replyCode;	// Reply Code;
//        REPLY[2] = 0x00;		// Reserved	'00'
//        REPLY[3] = 0x01;		// DOMAIN NAME Type IP ver.4
//        REPLY[4]= IP[0];
//        REPLY[5]= IP[1];
//        REPLY[6]= IP[2];
//        REPLY[7]= IP[3];
//        REPLY[8] = (byte)((pt & 0xFF00) >> 8);// Port High
//        REPLY[9] = (byte) (pt & 0x00FF);	  // Port Low
//
//        m_Parent.sendToClient( REPLY );// BND.PORT
//        
//    } // Reply_Command()
//    /////////////////////////////////////////////////////////////
//
//            /////////////////////////////////////////////////////////////
//
////    public	void	bindReply( byte replyCode, InetAddress IA, int PT )
////    {
////            byte	IP[] = {0,0,0,0};
////
////            DebugLog.getInstance().println( "BIND Reply to Client \"" + replyName( replyCode )+"\"" );
////
////            byte[]	REPLY = new byte[10];
////            if( IA != null )	IP = IA.getAddress();
////
////            REPLY[0] = Constants.SOCKS5_Version;
////            REPLY[1] = (byte)((int)replyCode - 90);	// Reply Code;
////            REPLY[2] = 0x00;		// Reserved	'00'
////            REPLY[3] = 0x01;		// IP ver.4 Type
////            REPLY[4] = IP[0];
////            REPLY[5] = IP[1];
////            REPLY[6] = IP[2];
////            REPLY[7] = IP[3];
////            REPLY[8] = (byte)((PT & 0xFF00) >> 8);
////            REPLY[9] = (byte) (PT & 0x00FF);
////
////            if( m_Parent.isActive() )	{
////                    m_Parent.sendToClient( REPLY );
////            }
////            else	{
////                    DebugLog.getInstance().println( "BIND - Closed Client Connection" );
////            }
////    } 
//
//
//
//    public void udpReply( byte replyCode, InetAddress IA, int PT )
//            throws	IOException	{
//
//            DebugLog.getInstance().println( "Reply to Client \"" + replyName( replyCode )+"\"" );
//
//            if( m_Parent.m_ClientSocket == null )	{
//                    DebugLog.getInstance().println( "Error in UDP_Reply() - Client socket is NULL" );	
//            }
//            byte[]	IP = IA.getAddress();
//
//            byte[]	REPLY = new byte[10];
//
//            REPLY[0] = Constants.SOCKS5_Version;
//            REPLY[1] = replyCode;	// Reply Code;
//            REPLY[2] = 0x00;		// Reserved	'00'
//            REPLY[3] = 0x01;		// Address Type	IP v4
//            REPLY[4] = IP[0];
//            REPLY[5] = IP[1];
//            REPLY[6] = IP[2];
//            REPLY[7] = IP[3];
//
//            REPLY[8] = (byte)((PT & 0xFF00) >> 8);// Port High
//            REPLY[9] = (byte) (PT & 0x00FF);		 // Port Low
//
//            m_Parent.sendToClient( REPLY );// BND.PORT
//    } 
//
//    public	void	udp() throws IOException {
//
//            //	Connect to the Remote Host
//
//            try	{
//                    DGSocket  = new DatagramSocket();
//                    initUdpInOut();
//            }
//            catch( IOException e )	{
//                    refuseCommand( (byte)0x05 ); // Connection Refused
//                    throw new IOException( "Connection Refused - FAILED TO INITIALIZE UDP Association." );
//            }
//
//            InetAddress	MyIP   = m_Parent.m_ClientSocket.getLocalAddress();
//            int			MyPort = DGSocket.getLocalPort();
//
//            //	Return response to the Client   
//            // Code '00' - Connection Succeeded,
//            // IP/Port where Server will listen
//            udpReply( (byte)0, MyIP, MyPort );
//
//            DebugLog.getInstance().println( "UDP Listen at: <"+MyIP.toString()+":"+MyPort+">" );
//
//            while( m_Parent.checkClientData() >= 0 )
//            {
//                    processUdp();
//                    Thread.yield();
//            }
//            DebugLog.getInstance().println( "UDP - Closed TCP Master of UDP Association" );
//    } // UDP ...
//    /////////////////////////////////////////////////////////////
//
//    private	void	initUdpInOut()	throws IOException {
//
//            DGSocket.setSoTimeout ( Constants.DEFAULT_PROXY_TIMEOUT );	
//
//            m_Parent.m_Buffer = new byte[ Constants.DEFAULT_BUF_SIZE ];
//
//            DGPack = new DatagramPacket( m_Parent.m_Buffer, Constants.DEFAULT_BUF_SIZE );
//    }
//    /////////////////////////////////////////////////////////////
//
//    private	byte[]	addDgpHead( byte[]	buffer )	{
//
//            //int		bl			= Buffer.length;
//            byte	IABuf[]		= DGPack.getAddress().getAddress();
//            int		DGport		= DGPack.getPort();
//            int		HeaderLen	= 6 + IABuf.length;
//            int		DataLen		= DGPack.getLength();
//            int		NewPackLen	= HeaderLen + DataLen;
//
//            byte	UB[] = new byte[ NewPackLen ];
//
//            UB[0] = (byte)0x00;	// Reserved 0x00
//            UB[1] = (byte)0x00;	// Reserved 0x00
//            UB[2] = (byte)0x00;	// FRAG '00' - Standalone DataGram
//            UB[3] = (byte)0x01;	// Address Type -->'01'-IP v4
//            System.arraycopy( IABuf,0, UB,4, IABuf.length );
//            UB[4+IABuf.length] = (byte)((DGport >> 8) & 0xFF);
//            UB[5+IABuf.length] = (byte)((DGport     ) & 0xFF);
//            System.arraycopy( buffer,0, UB, 6+IABuf.length, DataLen );
//            System.arraycopy( UB,0, buffer,0, NewPackLen );
//
//            return	UB;
//
//    } 
//
//    private	byte[]	clearDgpHead( byte[] buffer )	{
//            int	IAlen = 0;
//            //int	bl	= Buffer.length;
//            int	p	= 4;	// First byte of IP Address
//
//            byte	AType = buffer[3];	// IP Address Type
//            switch( AType )	{
//            case	0x01:	IAlen = 4;   break;
//            case	0x03:	IAlen = buffer[p]+1; break; // One for Size Byte
//            default		:	DebugLog.getInstance().println( "Error in ClearDGPhead() - Invalid Destination IP Addres type " + AType );
//                                            return null;
//            }
//
//            byte	IABuf[] = new byte[IAlen];
//            System.arraycopy( buffer, p, IABuf, 0, IAlen );
//            p += IAlen;
//
//            UDP_IA   = calcInetAddress( AType , IABuf );
//            UDP_port = Utils.getInstance().calcPort( buffer[p++], buffer[p++] );
//
//            if( UDP_IA == null )	{
//                    DebugLog.getInstance().println( "Error in ClearDGPHead() - Invalid UDP dest IP address: NULL" );
//                    return null;
//            }
//
//            int	DataLen = DGPack.getLength();
//            DataLen -= p; // <p> is length of UDP Header
//
//            byte	UB[] = new byte[ DataLen ];
//            System.arraycopy( buffer,p, UB,0, DataLen );
//            System.arraycopy( UB,0, buffer,0, DataLen );
//
//            return UB;
//
//    } 
//
//    protected	void	udpSend( DatagramPacket	DGP )	{
//
//            if( DGP == null )	return;
//
//            String	LogString =	DGP.getAddress()+ ":" + 
//                                                    DGP.getPort()	+ "> : " + 
//                                                    DGP.getLength()	+ " bytes";
//            try	{
//                    DGSocket.send( DGP );
//            }
//            catch( IOException e )	{
//                    DebugLog.getInstance().println( "Error in ProcessUDPClient() - Failed to Send DGP to "+ LogString );
//                    return;
//            }
//    }
//
//
//    public	void	processUdp()	{
//
//            // Trying to Receive DataGram
//            try	{
//            DGSocket.receive( DGPack );
//            }
//            catch( InterruptedIOException e )	{
//                    return;	// Time Out		
//            }
//            catch( IOException e )	{
//                    DebugLog.getInstance().println( "Error in ProcessUDP() - "+ e.toString() );
//                    return;
//            }
//
//            if( m_ClientIP.equals( DGPack.getAddress() ) )	{
//
//                    processUdpClient();
//            }
//            else	{
//
//                    processUdpRemote();
//            }
//
//            try	{
//                    initUdpInOut();	// Clean DGPack & Buffer
//            }
//            catch( IOException e )	{
//                    DebugLog.getInstance().println( "IOError in Init_UDP_IO() - "+ e.toString() );
//                    m_Parent.close();
//            }
//    } 
//
//    /** Processing Client's datagram
//     * This Method must be called only from <ProcessUDP()>
//    */
//    public	void	processUdpClient()	{
//
//            m_nClientPort = DGPack.getPort();
//
//            // Also calculates UDP_IA & UDP_port ...
//            byte[]	Buf = clearDgpHead( DGPack.getData() );
//            if( Buf == null )	return;
//
//            if( Buf.length <= 0 )	return;				
//
//            if( UDP_IA == null )	{
//                    DebugLog.getInstance().println( "Error in ProcessUDPClient() - Invalid Destination IP - NULL" );
//                    return;
//            }
//            if( UDP_port == 0 )	{
//                    DebugLog.getInstance().println( "Error in ProcessUDPClient() - Invalid Destination Port - 0" );
//                    return;
//            }
//
//            if( m_ServerIP != UDP_IA || m_nServerPort != UDP_port )	{
//                    m_ServerIP		= UDP_IA;
//                    m_nServerPort	= UDP_port;
//            }
//
//            DebugLog.getInstance().println( "Datagram : "+ Buf.length + " bytes : "+DebugLog.getInstance().getSocketInfo(	DGPack )+
//                                     " >> <" + Utils.getInstance().iP2Str( m_ServerIP )+":"+m_nServerPort+">" );
//
//            DatagramPacket	DGPSend = new DatagramPacket( Buf, Buf.length,
//                                                                                                      UDP_IA, UDP_port );
//
//            udpSend( DGPSend );
//    }		
//
//
//
//    public	void	processUdpRemote()	{
//
//            DebugLog.getInstance().println( "Datagram : "+ DGPack.getLength()+" bytes : "+
//                                     "<"+Utils.getInstance().iP2Str( m_ClientIP )+":"+m_nClientPort+"> << " +
//                                     DebugLog.getInstance().getSocketInfo( DGPack ) );
//
//            // This Method must be CALL only from <ProcessUDP()>
//            // ProcessUDP() Reads a Datagram packet <DGPack>
//
//            InetAddress	DGP_IP	= DGPack.getAddress();
//            int			DGP_Port= DGPack.getPort();
//
//            byte[]	Buf;
//
//            Buf = addDgpHead( m_Parent.m_Buffer );
//            if( Buf == null )	return;
//
//            // SendTo Client
//            DatagramPacket	DGPSend = new DatagramPacket( Buf, Buf.length,
//                                                                                                      m_ClientIP, m_nClientPort );
//            udpSend( DGPSend );
//
//            if( DGP_IP != UDP_IA || DGP_Port != UDP_port )	{
//                    m_ServerIP		= DGP_IP;
//                    m_nServerPort	= DGP_Port;
//            }
//    }			
//	
//}														 
