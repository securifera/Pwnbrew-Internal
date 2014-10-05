///*
//
//Copyright (C) 2013-2014, Securifera, Inc 
//
//All rights reserved. 
//
//Redistribution and use in source and binary forms, with or without modification, 
//are permitted provided that the following conditions are met:
//
//    * Redistributions of source code must retain the above copyright notice,
//	this list of conditions and the following disclaimer.
//
//    * Redistributions in binary form must reproduce the above copyright notice,
//	this list of conditions and the following disclaimer in the documentation 
//	and/or other materials provided with the distribution.
//
//    * Neither the name of Securifera, Inc nor the names of its contributors may be 
//	used to endorse or promote products derived from this software without specific
//	prior written permission.
//
//THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS 
//OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY 
//AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER 
//OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
//CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
//SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON 
//ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
//(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
//EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
//================================================================================
//
//Pwnbrew is provided under the 3-clause BSD license above.
//
//The copyright on this package is held by Securifera, Inc
//
//*/
//
//
///*
//* TaskNew.java
//*
//* Created on Dec 09, 2013, 10:11:12PM
//*/
//
//package pwnbrew.network.control.messages;
//
//import pwnbrew.log.LoggableException;
//import java.io.IOException;
//import java.io.UnsupportedEncodingException;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import pwnbrew.network.ControlOption;
//
///**
// *
// *  
// */
//public final class TaskNew extends TaskStatus {
//
//    
//    private static final byte OPTION_CMD_STRING = 5;
//    private String[] cmdLine = null;
//    private final List<String> hashFilenameList = new ArrayList<>();
//  
//    //=========================================================================
//    /*
//     * Constructor
//     */
//    public TaskNew( byte[] passedId ) { // NO_UCD (use default)
//       super(passedId );
//    }
//   
//    //=========================================================================
//    /**
//     *  Sets the variable in the message related to this TLV
//     * 
//     * @param tempTlv 
//     * @return  
//     */
//    @Override
//    public boolean setOption( ControlOption tempTlv ){        
//       
//        boolean retVal = true;
//        if( !super.setOption(tempTlv)){
//            try {
//                byte[] theValue = tempTlv.getValue();
//                switch( tempTlv.getType()){
//                    case OPTION_CMD_STRING:
//
//                        String currStr = new String( theValue, "US-ASCII");
//                        List<String> cmdLineStringList = new ArrayList<>();
//
//                        //Split the strings out of the null byte delimited string
//                        while(currStr.length() > 0){
//                            int nullByteIndex = currStr.indexOf((byte)0x00);
//                            cmdLineStringList.add(currStr.substring(0, nullByteIndex));
//                            currStr = currStr.substring(nullByteIndex + 1, currStr.length());                   
//                        }
//
//                        cmdLine = (String[])(cmdLineStringList.toArray(new String[cmdLineStringList.size()]));
//                        break;
//
//                    case OPTION_HASH_FILENAME:
//                        hashFilenameList.add(new String( theValue, "US-ASCII"));                    
//                        break; 
//
//                    case TASK_STATUS:
//                        taskStatus = new String( theValue, "US-ASCII");
//                        break;
//
//                    default:
//                        break;
//                }
//            } catch (UnsupportedEncodingException ex) {
//                ex = null;
//            }
//        }
//        return retVal;
//    }
//
//    //===============================================================
//    /**
//     * Get command line option
//     *
//     * @return 
//    */
//    public String[] getCmdLine() {
//        return Arrays.copyOf(cmdLine, cmdLine.length);
//    }
//
//    //===============================================================
//    /**
//     * Returns a list of the support files
//     * @return 
//    */
//    public List<String> getSupportFiles() {
//        return new ArrayList<>(hashFilenameList);
//    }
//
//}/* END CLASS TaskNew */
