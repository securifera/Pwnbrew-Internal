/*
 * IMPORTANT - READ CAREFULLY: This License Agreement is a legal agreement between You and Securifera, Inc. Read it carefully before completing the installation process and using the Software. It provides a license to use the Software and contains warranty information and liability disclaimers. BY INSTALLING, COPYING OR OTHERWISE USING THE SOFTWARE, YOU ARE CONFIRMING YOUR ACCEPTANCE OF THE SOFTWARE AND AGREEING TO BECOME BOUND BY THE TERMS OF THIS AGREEMENT. IF YOU DO NOT AGREE, DO NOT INSTALL OR USE THE PRODUCT. The Software is owned by Securifera, Inc and/or its licensors and is protected by copyright laws and international copyright treaties, as well as other intellectual property laws and treaties. THE SOFTWARE IS LICENSED, NOT SOLD.

 * DEFINITIONS.
 * "Vendor" means Securifera, Inc
 * "You", "Your" means you and your company.
 * "Software" means the product provided to You, which includes computer software and may include associated media, printed materials, and "online" or electronic documentation. 
 * OWNERSHIP. The Software is owned and copyrighted by Vendor and/or its licensors. Your license confers no title or ownership in the Software and is not a sale of any rights in the Software.
 * GRANT OF LICENSE. Vendor grants You the following rights provided You comply with all terms and conditions of this agreement. For each license You have acquired for the Software:
 * You are granted a non-exclusive right to use and install ONE copy of the software on ONE computer.
 * You may make one copy for backup or archival purposes.
 * You may modify the configuration files (if applicable).
 * RESTRICTED USE.
 * You agree to use reasonable efforts to prevent unauthorized copying of the Software.
 * You may not disable any licensing or control features of the Software or allow the Software to be used with such features disabled.
 * You may not share, rent, or lease Your right to use the Software.
 * You may not modify, sublicense, copy, rent, sell, distribute or transfer any part of the Software except as provided in this Agreement.
 * You may not reverse engineer, decompile, translate, create derivative works, decipher, decrypt, disassemble, or otherwise convert the Software to a more human-readable form for any reason.
 * You will return or destroy all copies of the Software if and when Your right to use it ends.
 * You may not use the Software for any purpose that is unlawful.
 * ADDITIONAL SOFTWARE This license applies to updates, upgrades, plug-ins and any other additions to the original Software provided by Vendor, unless Vendor provides other terms along with the additional software.
 * REGISTRATION. The software will electronically register itself during installation to confirm that You have entered a valid “License Key". The registration process only sends the license information that You've entered (License key) and information about the software installed (Program ID, Version, Checksum and selected Network Interface MAC address). No other information is sent.
 * UPGRADES. If this copy of the software is an upgrade from an earlier version of the software, it is provided to You on a license exchange basis. Your use of the Software upgrade is subject to the terms of this license, and You agree by Your installation and use of this copy of the Software to voluntarily terminate Your earlier license and that You will not continue to use the earlier version of the Software or transfer it to another person or entity.
 * TRANSFER. You cannot transfer the Software and Your rights under this license to another party.
 * SUBLICENSING. You may not sublicense the Software and Your rights under this license to another party
 * TERMINATION. Vendor may terminate Your license if You do not abide by the license terms or if You have not paid applicable license fees. Termination of the license may include, but not be limited to, marking the License Key as invalid to prevent further installations or usage. Upon termination of license, You shall immediately discontinue the use of the Software and shall within ten (10) days return to Vendor all copies of the Software or confirm that You have destroyed all copies of it. Your obligations to pay accrued charges and fees, if any, shall survive any termination of this Agreement. You agree to indemnify Vendor and its licensors for reasonable attorney fees in enforcing its rights pursuant to this license.
 * DISCLAIMER OF WARRANTY. The Software is provided on an "AS IS" basis, without warranty of any kind, including, without limitation, the warranties of merchantability, fitness for a particular purpose and non- infringement. The entire risk as to the quality and performance of the Software is borne by You. Should the Software prove defective, You, not Vendor or its licensors, assume the entire cost of any service and repair. If the Software is intended to link to, extract content from or otherwise integrate with a third party service, Vendor makes no representation or warranty that Your particular use of the Software is or will continue to be authorized by law in Your jurisdiction or that the third party service will continue to be available to You. This disclaimer of warranty constitutes an essential part of the agreement.
 * LIMITATION OF LIABILITY. UNDER NO CIRCUMSTANCES AND UNDER NO LEGAL THEORY, TORT, CONTRACT, OR OTHERWISE, SHALL VENDOR OR ITS LICENSORS BE LIABLE TO YOU OR ANY OTHER PERSON FOR ANY INDIRECT, SPECIAL, PUNITIVE, INCIDENTAL, OR CONSEQUENTIAL DAMAGES OF ANY CHARACTER INCLUDING, WITHOUT LIMITATION, DAMAGES FOR WORK STOPPAGE, COMPUTER FAILURE OR LOSS OF REVENUES, PROFITS, GOODWILL, USE, DATA OR OTHER INTANGIBLE OR ECONOMIC LOSSES. IN NO EVENT WILL VENDOR OR ITS LICENSORS BE LIABLE FOR ANY DAMAGES IN EXCESS OF THE AMOUNT PAID TO LICENSE THE SOFTWARE, EVEN IF YOU OR ANY OTHER PARTY SHALL HAVE INFORMED VENDOR OR ITS LICENSORS OF THE POSSIBILITY OF SUCH DAMAGES, OR FOR ANY CLAIM. NO CLAIM, REGARDLESS OF FORM, MAY BE MADE OR ACTION BROUGHT BY YOU MORE THAN ONE YEAR AFTER THE BASIS FOR THE CLAIM BECOMES KNOWN TO THE PARTY ASSERTING IT.
 * APPLICABLE LAW. This license shall be interpreted in accordance with the laws of the United States of America. Any disputes arising out of this license shall be adjudicated in a court of competent jurisdiction in the United States of America.
 * GOVERNING LANGUAGE. Any translation of this License is done for local requirements and in the event of a dispute between the English and any non-English versions, the English version of this License shall govern.
 * ENTIRE AGREEMENT. This license constitutes the entire agreement between the parties relating to the Software and supersedes any proposal or prior agreement, oral or written, and any other communication relating to the subject matter of this license. Any conflict between the terms of this License Agreement and any Purchase Order, invoice, or representation shall be resolved in favour of the terms of this License Agreement. In the event that any clause or portion of any such clause is declared invalid for any reason, such finding shall not affect the enforceability of the remaining portions of this License and the unenforceable clause shall be severed from this license. Any amendment to this agreement must be in writing and signed by both parties.

 */
package pwnbrew.socks;


import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import pwnbrew.misc.Constants;
import pwnbrew.misc.DebugPrinter;


public class Socks5Impl extends Socks4Impl implements SocksCommonInterface {
        
    private static final String NAME_Class = Socks5Impl.class.getSimpleName();
	
    protected DatagramSocket DGSocket = null;
    protected DatagramPacket DGPack = null;

    private InetAddress UDP_IA = null;
    private int UDP_port = 0;

    //-------------------


//	public			byte	SOCKS_Version;	// Version of SOCKS
//	public			byte	Command;		// Command code
    public			byte 	RSV;			// Reserved.Must be'00'
    public			byte	ATYP;			// Address Type
//	public			byte[]	DST_Addr;		// Destination Address
//	public			byte[]	DST_Port;		// Destination Port
                                                                                    // in Network order

    static final int ADDR_Size[]={ -1, //'00' No such AType 
                                   4, //'01' IP v4 - 4Bytes
                                  -1, //'02' No such AType
                                  -1, //'03' First Byte is Len
                                  16  //'04' IP v6 - 16bytes
                                   };



    //=========================================================================
    /**
     * 
     * @param Parent 
     */
    public Socks5Impl( SocksHandler Parent ){

        super( Parent );
        DST_Addr = new byte[Constants.MAX_ADDR_LEN]; 
    }

    //=========================================================================
    /**
     * 
     * @param AType
     * @param addr
     * @return 
     */
    public String getServAddr( byte AType, byte[] addr ) {
        
        String retAddr = null;
        switch( AType )	{
                    // Version IP 4
            case 0x01:	
                InetAddress IA = Utils.getInstance().calcInetAddress( addr );
                
                break;
                    // Version IP DOMAIN NAME
            case 0x03:	
                if( addr[0] <= 0  ){
                    DebugPrinter.printMessage( NAME_Class, "calcInetAddress","SOCKS 5 - calcInetAddress() : BAD IP in command - size : " + addr[0], null );
                    return null;	
                }
                retAddr = "";
                for( int i = 1; i <= addr[0]; i++ ){
                    retAddr += (char)addr[i];
                }
//                try {
//                    IA = InetAddress.getByName( sIA );
//                } catch( UnknownHostException e )	{
//                    return null;
//                }
                break;
            default:
                return null;
        }
        return retAddr;
    } 

    
    //--- Reply Codes ---
    //=========================================================================
    /**
     * 
     * @return 
     */
    @Override
    public byte getSuccessCode(){
        return 00;
    }
    
    //=========================================================================
    /**
     * 
     * @return 
     */
    @Override
    public byte getFailCode() {
        return 04;
    }

    //=========================================================================
    /**
     * 
     * @return 
     */
    @Override
    public boolean calculateAddress(){

        m_ServerAddr = getServAddr( ATYP, DST_Addr );
        m_nServerPort	= Utils.getInstance().calcPort( DST_Port[0], DST_Port[1] );

//        m_ClientIP		= m_Parent.m_ClientSocket.getInetAddress();
//        m_nClientPort	= m_Parent.m_ClientSocket.getPort();

        return ( (m_ServerAddr != null) && (m_nServerPort >= 0) );
    }							

    
    //=========================================================================
    /**
     * 
     * @param SOCKS_Ver
     * @throws Exception 
     */
    @Override
    public void authenticate( byte SOCKS_Ver ) throws Exception {

        super.authenticate( SOCKS_Ver ); // Sets SOCKS Version...

        if( SOCKS_Version == Constants.SOCKS5_Version )	{	
            if( !checkAuthentication() )	{// It reads whole Cli Request
                    refuseAuthentication("SOCKS 5 - Not Supported Authentication!");
                    throw new Exception("SOCKS 5 - Not Supported Authentication.");
            }
            acceptAuthentication();
        } else {
            refuseAuthentication( "Incorrect SOCKS version : "+SOCKS_Version );
            throw new Exception( "Not Supported SOCKS Version -'"+SOCKS_Version + "'");
        }
    } 

    //=========================================================================
    /**
     * 
     * @param msg 
     */
    public void refuseAuthentication( String msg ){

        DebugPrinter.printMessage( NAME_Class, "refuseAuthentication","SOCKS 5 - Refuse Authentication: '"+msg+"'", null );
        m_Parent.sendToClient( Constants.SRE_Refuse, Constants.SRE_Refuse.length );
    }

    //=========================================================================
    /**
     * 
     */
    public void acceptAuthentication(){
        DebugPrinter.printMessage( NAME_Class, "acceptAuthentication", "SOCKS 5 - Accepts Auth. method 'NO_AUTH'", null );
        byte[] tSRE_Accept = Constants.SRE_Accept;
        tSRE_Accept[0] = SOCKS_Version;
        m_Parent.sendToClient( tSRE_Accept, tSRE_Accept.length );
    }

    //=========================================================================
    /**
     * 
     * @return
     * @throws Exception 
     */
    public boolean checkAuthentication() throws Exception {
        //boolean	Have_NoAuthentication = false;
        byte Methods_Num = getByte();
        String Methods = "";

        for( int i=0; i<Methods_Num; i++ ){
            Methods += ",-" + getByte() + '-';
        }
        
        return	( (Methods.indexOf( "-0-"  ) != -1) || (Methods.indexOf( "-00-" ) != -1) );
    }


    //=========================================================================
    /**
     * 
     * @throws Exception 
     */
    @Override
    public void getClientCommand() throws Exception {
        
        int Addr_Len;

        SOCKS_Version = getByte();
        socksCommand = getByte();
        RSV = getByte();
        ATYP = getByte();

        Addr_Len = ADDR_Size[ATYP];
        DST_Addr[0] =  getByte();
        if( ATYP==0x03 ){
            Addr_Len = DST_Addr[0]+1;
        }

        for( int i=1; i<Addr_Len; i++ )	{
            DST_Addr[i]= getByte();
        }
        DST_Port[0]	= getByte();
        DST_Port[1]	= getByte();

        if( SOCKS_Version != Constants.SOCKS5_Version )	{
            DebugPrinter.printMessage( NAME_Class, "getClientCommand" ,"SOCKS 5 - Incorrect SOCKS Version of Command: "+
                                     SOCKS_Version, null );
            refuseCommand( (byte)0xFF );
            throw new Exception("Incorrect SOCKS Version of Command: "+ 
                                                          SOCKS_Version);
        }

        if( (socksCommand < Constants.SC_CONNECT) || (socksCommand > Constants.SC_UDP) ){
            DebugPrinter.printMessage( NAME_Class, "getClientCommand" ,"SOCKS 5 - GetClientCommand() - Unsupported Command : \"" + commName( socksCommand )+"\"", null );
            refuseCommand( (byte)0x07 );
            throw new Exception("SOCKS 5 - Unsupported Command: \"" + socksCommand +"\"" );
        }

        if( ATYP == 0x04 ){
            DebugPrinter.printMessage( NAME_Class, "getClientCommand" , "SOCKS 5 - GetClientCommand() - Unsupported Address Type - IP v6", null );
            refuseCommand( (byte)0x08 );
            throw new Exception( "Unsupported Address Type - IP v6" );
        }

        if( (ATYP >= 0x04) || (ATYP <=0) ){
            DebugPrinter.printMessage( NAME_Class, "getClientCommand" , "SOCKS 5 - GetClientCommand() - Unsupported Address Type: " + ATYP, null );
            refuseCommand( (byte)0x08 );
            throw new Exception( "SOCKS 5 - Unsupported Address Type: " + ATYP );
        }

        if( !calculateAddress() ){  // Gets the IP Address 
            refuseCommand( (byte)0x04 );// Host Not Exists...
            throw new Exception( "SOCKS 5 - Unknown Host/IP address '" + m_ServerAddr.toString()+"'" );
        }

        DebugPrinter.printMessage( NAME_Class,  "getClientCommand" ,"SOCKS 5 - Accepted SOCKS5 Command: \""+commName(socksCommand)+"\"", null );
    }  

    //=========================================================================
    /**
     * 
     * @param replyCode 
     */
    @Override
    public void replyCommand( byte replyCode ) {
        DebugPrinter.printMessage( NAME_Class, "replyCommand" ,"SOCKS 5 - Reply to Client \"" + replyName(replyCode)+"\"", null );

        int	pt = 0;

        byte[]	REPLY	= new byte[10];
        byte	IP[]	= new byte[4];

//        if( m_Parent.m_ServerSocket != null )	{
//            //IA = m_Parent.m_ServerSocket.getInetAddress();
//            //DN = IA.toString();
//            pt = m_Parent.m_ServerSocket.getLocalPort();
//        } else	{
            IP[0]=0;
            IP[1]=0;
            IP[2]=0;
            IP[3]=0;
            pt = 0;
//        }

        REPLY[0] = Constants.SOCKS5_Version;
        REPLY[1] = replyCode;	// Reply Code;
        REPLY[2] = 0x00;		// Reserved	'00'
        REPLY[3] = 0x01;		// DOMAIN NAME Type IP ver.4
        REPLY[4]= IP[0];
        REPLY[5]= IP[1];
        REPLY[6]= IP[2];
        REPLY[7]= IP[3];
        REPLY[8] = (byte)((pt & 0xFF00) >> 8);// Port High
        REPLY[9] = (byte) (pt & 0x00FF);     // Port Low

        m_Parent.sendToClient( REPLY, REPLY.length );// BND.PORT
        
    } // Reply_Command()
    /////////////////////////////////////////////////////////////

            /////////////////////////////////////////////////////////////

//    public	void	bindReply( byte replyCode, InetAddress IA, int PT )
//    {
//            byte	IP[] = {0,0,0,0};
//
//            DebugLog.getInstance().println( "BIND Reply to Client \"" + replyName( replyCode )+"\"" );
//
//            byte[]	REPLY = new byte[10];
//            if( IA != null )	IP = IA.getAddress();
//
//            REPLY[0] = Constants.SOCKS5_Version;
//            REPLY[1] = (byte)((int)replyCode - 90);	// Reply Code;
//            REPLY[2] = 0x00;		// Reserved	'00'
//            REPLY[3] = 0x01;		// IP ver.4 Type
//            REPLY[4] = IP[0];
//            REPLY[5] = IP[1];
//            REPLY[6] = IP[2];
//            REPLY[7] = IP[3];
//            REPLY[8] = (byte)((PT & 0xFF00) >> 8);
//            REPLY[9] = (byte) (PT & 0x00FF);
//
//            if( m_Parent.isActive() )	{
//                    m_Parent.sendToClient( REPLY );
//            }
//            else	{
//                    DebugLog.getInstance().println( "BIND - Closed Client Connection" );
//            }
//    } 



    //=========================================================================
    /**
     * 
     * @param replyCode
     * @param IA
     * @param PT
     * @throws IOException 
     */
    public void udpReply( byte replyCode, InetAddress IA, int PT ) throws IOException {

            DebugPrinter.printMessage(NAME_Class, "udpReply","Reply to Client \"" + replyName( replyCode )+"\"", null );

//            if( m_Parent.m_ClientSocket == null )	{
//                    DebugLog.getInstance().println( "Error in UDP_Reply() - Client socket is NULL" );	
//            }
            byte[]	IP = IA.getAddress();

            byte[]	REPLY = new byte[10];

            REPLY[0] = Constants.SOCKS5_Version;
            REPLY[1] = replyCode;	// Reply Code;
            REPLY[2] = 0x00;		// Reserved	'00'
            REPLY[3] = 0x01;		// Address Type	IP v4
            REPLY[4] = IP[0];
            REPLY[5] = IP[1];
            REPLY[6] = IP[2];
            REPLY[7] = IP[3];

            REPLY[8] = (byte)((PT & 0xFF00) >> 8);// Port High
            REPLY[9] = (byte) (PT & 0x00FF);		 // Port Low

            m_Parent.sendToClient( REPLY, REPLY.length );// BND.PORT
    } 

//    //=========================================================================
//    /**
//     * 
//     * @throws IOException 
//     */
//    public void udp() throws IOException {
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
    /////////////////////////////////////////////////////////////

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
	
}					
