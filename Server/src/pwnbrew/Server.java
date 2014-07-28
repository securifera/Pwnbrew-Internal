/*

Copyright (C) 2013-2014, Securifera, Inc 

All rights reserved. 

Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice,
	this list of conditions and the following disclaimer.

    * Redistributions in binary form must reproduce the above copyright notice,
	this list of conditions and the following disclaimer in the documentation 
	and/or other materials provided with the distribution.

    * Neither the name of Securifera, Inc nor the names of its contributors may be 
	used to endorse or promote products derived from this software without specific
	prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS 
OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY 
AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER 
OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON 
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

================================================================================

Pwnbrew is provided under the 3-clause BSD license above.

The copyright on this package is held by Securifera, Inc

*/


/*
* Server.java
*
* Created on June 21, 2013, 9:23:11 PM
*/

package pwnbrew;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.security.GeneralSecurityException;
import java.util.logging.Level;
import javax.swing.JDialog;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import pwnbrew.gui.MainGui;
import pwnbrew.logging.Log;
import pwnbrew.logging.LoggableException;
import pwnbrew.look.CustomLookAndFeel;
import pwnbrew.manager.DataManager;
import pwnbrew.manager.ServerManager;
import pwnbrew.misc.Constants;
import pwnbrew.misc.DebugPrinter;
import pwnbrew.misc.Directories;
import pwnbrew.network.ServerPortRouter;
import pwnbrew.utilities.FileUtilities;
import pwnbrew.network.control.ControlMessageManager;
import pwnbrew.network.file.FileMessageManager;
import pwnbrew.network.http.Http;
import pwnbrew.network.http.ServerHttpWrapper;
import pwnbrew.network.relay.RelayManager;
import pwnbrew.shell.ShellMessageManager;

/**
 *
 * 
 */
public final class Server {

    private static Server staticSelf = null;
    private final ServerManager theServerManager;
    private static final String NAME_Class = Server.class.getSimpleName();
    
    //FileLock variables
    private static final String lckFileName = "srv.lck";
    private FileLock theLock = null;
    private FileChannel theLockFileChannel = null;    
    private static final boolean debug = true;
    
    private static final String SHOW_GUI_ARG = "-gui";
    private static final String REMOTE_MANAGEMENT_ARG = "-rmp";
    
    //=========================================================================
    /**
     * Constructor
     * 
     * @param passedArg
     * @throws LoggableException
     * @throws IOException 
     */
    private Server( boolean passedBool ) throws LoggableException, IOException  {
        
        initialize();
        
        //Create the server manager
        theServerManager = new ServerManager( this, passedBool );
                                 
        try {

            ControlMessageManager aCMManager = ControlMessageManager.getControlMessageManager();
            if( aCMManager == null ){
                aCMManager = ControlMessageManager.initialize( theServerManager );
            }

            //Create and set the http wrapper
            int port = aCMManager.getPort();
            switch( port ){
                case Http.DEFAULT_PORT:
                case Http.SECURE_PORT:
                    ServerHttpWrapper aWrapper = new ServerHttpWrapper();
                    DataManager.setPortWrapper( port, aWrapper);
                    break;               
            }

        } catch (IOException ex ){
            Log.log(Level.WARNING, NAME_Class, "initialize()", ex.getMessage(), ex);
        }
            
    }

     //===============================================================
    /**
     * Starts the server threads
     *
    */
    private void start( int remManagePort ) throws LoggableException, IOException, GeneralSecurityException {

        theServerManager.start();
        
        if( remManagePort != -1 ){
            
            RelayManager aManager = RelayManager.getRelayManager();
            if( aManager == null ){
                aManager = RelayManager.initialize( theServerManager );
            }
            
            ServerPortRouter aSPR = aManager.getServerPorterRouter();
            if( aSPR.getServerSocketChannel() == null )
                aSPR.startServer(null, remManagePort );
            
        }
        
    }

    //===============================================================
    /**
     * Handles the shutdown tasks for the thread
     *
    */
    public void shutdown(){

        theServerManager.shutdown();

        //Shutdown the control and file message managers
        ControlMessageManager aCMM = ControlMessageManager.getControlMessageManager();
        if( aCMM != null ){
            aCMM.shutdown();
        }
        
        FileMessageManager aFMM = FileMessageManager.getFileMessageManager();
        if( aFMM != null ){
            aFMM.shutdown();
        }
        
        ShellMessageManager aSMM = ShellMessageManager.getShellMessageManager();
        if( aSMM != null ){
            aSMM.shutdown();
        }
        
        RelayManager aRMM = RelayManager.getRelayManager();
        if( aRMM != null ){
            aRMM.shutdown();
        }

        //Debug
        DebugPrinter.shutdown();        
        Constants.Executor.shutdownNow();
    }

    /**
     * @param args the command line arguments
     */
    public static void main( String[] args ) {

        try {
            
            boolean showGui = false;
            int remManagePort = -1;
            
            //Assign the service name
            for( String aString : args ){
                if(aString.equals( SHOW_GUI_ARG )){
                    showGui = true;
                } else if( aString.contains(REMOTE_MANAGEMENT_ARG)){
                    String[] argStrArr = aString.split("=");
                    if(argStrArr.length > 1 ){
                        try{ 
                            remManagePort = Integer.parseInt( argStrArr[1] );
                        } catch( NumberFormatException ex ){
                            remManagePort = -1;
                        }
                    }                    
                }
            }          
            
            staticSelf = new Server( showGui );
            staticSelf.start( remManagePort );

        } catch ( Throwable ex) {

            //Make sure the executor is shutdown
            ex.printStackTrace();
            Log.log(Level.SEVERE, NAME_Class, "main()", ex.getMessage(), ex);
            
            //Shutdown, there was an exception
            if(staticSelf != null){
               staticSelf.shutdown();
            }
            
            DebugPrinter.shutdown();
            Constants.Executor.shutdownNow();
            
        }
        
    }

    //===============================================================
    /**
     * Returns a reference server manager
     *
     * @return 
    */
    public ServerManager getServerManager() {
        return theServerManager;
    }

    //===============================================================
    /**
     *  Determines if an instance of the Server is already running.
     *
    */
   
    private boolean canRun() {    

        //Check the server file
        final File srvLockFile = new File( Directories.getDataPath() + File.separator + lckFileName);
        try{
            theLockFileChannel = new RandomAccessFile(srvLockFile, "rw").getChannel();
            theLock = theLockFileChannel.tryLock();
        } catch (IOException | OverlappingFileLockException ex) {
            closeFileLock();
            return false;
        }

        if(theLock == null){
            return false;
        }

        //Add a hook to close the lock on shutdown
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run(){
                closeFileLock();
                FileUtilities.deleteFile( srvLockFile );
            }
        });

        return true;
    
    }
    
    //===============================================================
    /**
    *  This function closes the file lock and channel.
    *
    */
  
    private void closeFileLock(){
        try {
            //Release the lock
            if( theLock != null ){
                theLock.release();                
            }
            
            if( theLockFileChannel != null && theLockFileChannel.isOpen() ){
                theLockFileChannel.close();
            }
        } catch (IOException ex) {
            Log.log(Level.SEVERE, NAME_Class, "closeLock()", ex.getMessage(), ex);
        }
    }
    
    // ========================================================================
    /**
     *  Handles any functions that need to be executed before the program starts.
     */
   
    private void initialize() throws LoggableException {

        if( !Directories.directoryStructureInitialized() ) {
            System.out.println( "Could not initialize the directory structure.\nTerminating run..." );
            System.exit( 1 );
        }

        try {
            
            //Add debug if turned on
            DebugPrinter.enable( debug );
            
            try {
                //Initialize log
                Log.initializeLog( new File( Directories.getLogPath() ));
            } catch (IOException ex) {
               throw new LoggableException(ex);
            }

            Log.log(Level.INFO, NAME_Class, "initialize()", "Starting server.", null);
             
        } catch( LoggableException ex ) {
            System.out.println( new StringBuilder( "Could not initialize the logging system. \"" )
                    .append( ex.getMessage() ).append( "\"\n" )
                    + "Logging is unavailable." );
        }
        
        try {
            UIManager.setLookAndFeel(new CustomLookAndFeel());
        } catch (UnsupportedLookAndFeelException ex) {
            Log.log(Level.SEVERE, NAME_Class, "main()", ex.getMessage(), ex );
        }
        
        //Make sure the server can run
        if(!canRun()){
            throw new LoggableException("Unable to start the Server.  An entry point has already been executed.");
        }
        
        MainGui.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);
    }


}/* END CLASS Server */
