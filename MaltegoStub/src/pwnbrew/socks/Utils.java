package pwnbrew.socks;

import java.net.InetAddress;
import java.net.UnknownHostException;
import pwnbrew.misc.DebugPrinter;

public class Utils {
    
    private static final String NAME_Class = Utils.class.getSimpleName();
	
	private static Utils INSTANCE = null;
	private Utils() {}
	
	public static Utils getInstance(){
		if(INSTANCE == null) INSTANCE = new Utils();
		return INSTANCE;
	}

	
	public	InetAddress calcInetAddress( byte[] addr )	{
		InetAddress	IA  = null;
		String		sIA = "";		
		
		if( addr.length < 4 )	{
                    DebugPrinter.printMessage(NAME_Class,  "calcInetAddress()" , "Invalid length of IP v4 - "+addr.length+" bytes", null );	
                    return null;
		}
		
		// IP v4 Address Type
		for( int i=0; i<4; i++ )	{
			sIA += byte2int( addr[i] );
			if( i<3 )	sIA += ".";
		}
		
		try	{
			IA = InetAddress.getByName( sIA );
		}
		catch( UnknownHostException e )	{
			return null;
		}
		
		return	IA; // IP Address
	}
	
	public	int	byte2int( byte b )	{
		int	res = b;
		if( res < 0 ) res = (int)( 0x100 + res );
		return	res;
	}

	
	public	int	calcPort( byte Hi, byte Lo )	{
		
		return ( (byte2int( Hi ) << 8) | byte2int( Lo ) );	
	}
	
	
	public	String	iP2Str( InetAddress IP )	{
		if( IP == null )	return "NA/NA";
		
		return	IP.getHostName()+"/"+IP.getHostAddress();
	}

}
