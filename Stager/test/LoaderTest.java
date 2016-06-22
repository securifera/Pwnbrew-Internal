/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 *  
 */
public class LoaderTest {
    
     final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    
//    public URLClassLoader test( DataInputStream in, long fileLen ) throws IOException{
//        
//        byte[] core = new byte[(int)fileLen];
//        in.readFully(core);
//        URL coreURL = MemoryBufferURLConnection.createURL(core, "application/jar");
//
//        return new URLClassLoader(new URL[] { coreURL }, getClass().getClassLoader());
// 
//    }

    /**
     * @param args the command line arguments
     * @throws java.lang.Exception
     */
    public static void main(String[] args) throws Exception {
        
//        File aFile = new File("C:\\Users\\rwincey\\Desktop\\Desktop\\Pwnbrew\\Client\\dist\\Client.jar");
//        FileInputStream aJarStream = new FileInputStream( aFile );
//        DataInputStream aDIS = new DataInputStream(aJarStream);
//        
//        //Test the load
//        LoaderTest aTest = new LoaderTest();
//        URLClassLoader aLD = aTest.test( aDIS, aFile.length() );
//        
//        //Add another jar
//        File file2 = new File("C:\\Users\\rwincey\\Desktop\\Desktop\\Pwnbrew\\Stager\\dist\\Stager.jar");
//        aJarStream = new FileInputStream( file2 );
//        aDIS = new DataInputStream(aJarStream);
//        
//        byte[] core = new byte[(int)file2.length()];
//        aDIS.readFully(core);
//        URL coreURL = MemoryBufferURLConnection.createURL(core, "application/jar");
//        Utilities.addURLToClassLoader(aLD, coreURL);
//        
//        Class aClass = Class.forName("stager.SleepTimer", true, aLD);
//        Constructor aConstruct = aClass.getConstructor( String.class);
//        Object anObj = aConstruct.newInstance("");
//        int i = 0;
      
        //Send the message
//        byte[] msgBytes = new byte[]{ 88, 
//        /* msg len */                 0, 13, 
//        /*client id*/                 0, 0, 0, 0,
//        /* dst id */                  (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,           
//                                      0, 0, 0, 0,
//        /* length */                  0, 42
//        /* class path */              };
        byte[] msgByteArr = new byte[]{
            0,
            0,0,0,22,                                        //length
            0,0,0,0,                                         //src client id
            (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,  //dst client id
            0,0,0,0,                                         //channel id
            0,0,0,18                                         //Payload Id
//            0, 42,
//            0x70, 0x77, 0x6E, 0x62, 0x72, 0x65, 0x77, 0x2E,
//            0x6E, 0x65, 0x74, 0x77, 0x6F, 0x72, 0x6B, 0x2E, 0x63, 0x6F, 0x6E,
//            0x74, 0x72, 0x6F, 0x6C, 0x2E, 0x6D, 0x65, 0x73, 0x73, 0x61, 0x67, 0x65,
//            0x73, 0x2E, 0x53, 0x65, 0x6E, 0x64, 0x53, 0x74, 0x61, 0x67, 0x65, 
//            0x10, 0x00, 0x00, 0x00, 0x01, 0x36
            };

//        String aStr = "pwnbrew.network.control.messages.SendStage";
//        byte[] aByteArr = aStr.getBytes();
//        aStr = bytesToHex( aByteArr );
        
//        ByteBuffer aBB = ByteBuffer.allocate(2048);
//        aBB.put((byte)88);
//        aBB.put( new byte[]{0, 56}); /* msg len */   
//        aBB.put( new byte[]{ 0,0,0,0}); /*client id*/
//        aBB.put( new byte[]{ (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff }); /*client id*/
//        aBB.put( new byte[4]);
//        aBB.put( new byte[]{ 0, 42});
//        aBB.put(classPathArr);
        
//        byte[] msgBytes = Arrays.copyOf( msgByteArr.array(), aBB.position());
               
        //XOR the bytes
//        SecureRandom aSR = new SecureRandom();
//        byte[] randBytes = new byte[4];
//        aSR.nextBytes(randBytes);
//        String randStr = bytesToHex( randBytes );
        
        byte[] encodedBytes = xorData(msgByteArr, "PWNZ".getBytes());
        String hexStrArr = bytesToHex( encodedBytes );
        
//        String aStr = bytesToHex( new byte[]{ (byte)((char)'4' ^ (char)'N')} );
//        aStr = bytesToHex( new byte[]{  (byte)((char)'5' ^ (char)'N')} );
//        aStr = bytesToHex( new byte[]{  (byte)((char)'6' ^ (char)'N')} );
//        aStr = bytesToHex( new byte[]{  (byte)((char)'7' ^ (char)'N')} );
//        aStr = bytesToHex( new byte[]{  (byte)((char)'8' ^ (char)'N')} );
        
        
//        String beginStr = hexStrArr.substring(0, 6);
//        String endStr = hexStrArr.substring(14);
        
        //Print before
//        System.out.println(hexStrArr);
        
        //Print after
//        StringBuilder aSB = new StringBuilder().append(beginStr).append(randStr).append(endStr);
        System.out.println(hexStrArr);
        
//        
//        encodedBytes = xorData(new byte[]{ 88, 0, 56, 1,2,3,4,(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff }, "PWNZ".getBytes());
//        String hexStr = bytesToHex( encodedBytes );
//        System.out.println(hexStr);
//        byte[] theBytes = hexStringToByteArray(hexStr);
//        encodedBytes = xorData(theBytes, "PWNZ".getBytes());
        
        
        
//        encodedBytes = xorData(new byte[]{ 1,1,1,1}, "PWNZ".getBytes());
//        hexStr = bytesToHex( encodedBytes );
//        theBytes = hexStringToByteArray(hexStr);
//        encodedBytes = xorData(theBytes, "PWNZ".getBytes());
//        i = 0;
        
    }
    
     //===============================================================
    /**
     *  Convert string to byte[]
     * 
     * @param s
     * @return 
     */
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                + Character.digit(s.charAt(i+1), 16));
        }

        return data;
    }
    
    // ==========================================================================
    /**
     *  Xor the data.
     * 
     * @param bData
     * @param xorString
     * @return 
    */
    public static byte[] xorData(byte[] bData, byte[] xorString) {
       
        byte bCrypto[] = new byte[bData.length];
        char theChar = 0x0;
        for(int i = 0 ; i < bData.length; ++i) {
            theChar = (char) xorString[i % xorString.length];
            bCrypto[i] = (byte)(bData[i] ^ theChar);
        }
 
        return bCrypto;
    }
    
      // ==========================================================================
    /**
     *  Hex encode the string
     * 
     * @param bytes
     * @return 
    */
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for ( int j = 0; j < bytes.length; j++ ) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
