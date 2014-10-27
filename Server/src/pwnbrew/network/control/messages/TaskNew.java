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
//* Created on June 23, 2013, 2:12:31 PM
//*/
//
//package pwnbrew.network.control.messages;
//
//import pwnbrew.logging.LoggableException;
//import java.io.IOException;
//import java.nio.ByteBuffer;
//import java.util.Collection;
//import pwnbrew.misc.Constants;
//import pwnbrew.network.ControlOption;
//import pwnbrew.tasks.RemoteTask;
//import pwnbrew.xmlBase.FileContentRef;
//
///**
// *
// *  
// */
//public final class TaskNew extends TaskStatus {
//    
//    private static final byte OPTION_CMD_STRING = 5;
//   
//    //=====================================================================
//    /*
//    * Constructor
//    */
//    public TaskNew( int taskId, String[] cmdLineArgs, Collection<FileContentRef> neededFileRefs, int dstHostId ) throws IOException, LoggableException  {
//        super(taskId, RemoteTask.TASK_START, dstHostId);
//
//        if(cmdLineArgs != null && cmdLineArgs.length > 0){
//
//            //Get each string and append a null byte to ensure that each string is terminated
//            ByteBuffer aBB = ByteBuffer.allocate(Constants.GENERIC_BUFFER_SIZE);
//            for(String aString : cmdLineArgs){
//
//                byte[] strBytes = aString.getBytes("US-ASCII");
//
//                aBB.put(strBytes);
//                aBB.put((byte)0x00);
//            }
//
//            //Create a byte array to hold the delimited string
//            byte[] cmdLineArr = new byte[aBB.position()];
//            aBB.flip();
//            aBB.get(cmdLineArr);
//
//            ControlOption aTlv = new ControlOption(OPTION_CMD_STRING, cmdLineArr);
//            addOption(aTlv);
//
//            //Add the support files
//            if(neededFileRefs != null){
//                for(FileContentRef aRef : neededFileRefs){
//                    addSupportFile(aRef.getName(),  aRef.getFileHash());
//                }
//            }
//            
//        } else {
//            throw new LoggableException("The cmd line array passed is null or empty.");
//        }
//    }
//    
//    //===============================================================
//    /**
//     * Adds a file to the list of needed files
//     *
//     * @param fileName the file name
//     * @param fileHash the file hash
//    */
//    private void addSupportFile(String fileName, String fileHash) throws IOException  {
//
//        String fileHashNameStr = new StringBuilder().append(fileHash).append(":").append(fileName).toString();
//        byte[] strBytes = fileHashNameStr.getBytes("US-ASCII");
//
//        ControlOption aTlv = new ControlOption(OPTION_HASH_FILENAME, strBytes);
//        addOption(aTlv);
//    }
//
//}/* END CLASS TaskNew */
