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

package pwnbrew.network.shell;

import pwnbrew.network.PortRouter;
import java.io.IOException;
import java.util.logging.Level;
import pwnbrew.logging.Log;
import pwnbrew.logging.LoggableException;
import pwnbrew.manager.PortManager;
import pwnbrew.manager.DataManager;
import pwnbrew.xmlBase.ServerConfig;

/**
 *
 *  
 */
public class ShellMessageManager extends DataManager {

    private static ShellMessageManager theShellMsgManager;
    private static final String NAME_Class = ShellMessageManager.class.getSimpleName();
    
    //The map for relating shells to their ids
    private Shell theShell = null;
    
    //===========================================================================
    /*
     *  Constructor
     */
    private ShellMessageManager( PortManager passedCommManager ) {
        
        super(passedCommManager);        
        
        //Set the port
        try {
            
            ServerConfig theConfig = ServerConfig.getServerConfig();
            int thePort = theConfig.getSocketPort();
            setPort( thePort );
            
        } catch (LoggableException ex) {
            Log.log( Level.SEVERE, NAME_Class, "ShellMessageManager()", ex.getMessage(), ex);
        }
        
        //Create the handler
        ShellMessageHandler theMessageHandler = new ShellMessageHandler( this );
        theMessageHandler.start();
       
        //Set the data handler
        setDataHandler(theMessageHandler);
    }  
    
    // ==========================================================================
    /**
     *   Creates a ShellMessageManager
     * @param passedCommManager
     * @return 
     * @throws java.io.IOException 
     */
    public synchronized static ShellMessageManager initialize( PortManager passedCommManager ) throws IOException {

        if( theShellMsgManager == null ) {
            theShellMsgManager = new ShellMessageManager( passedCommManager );
            createPortRouter( passedCommManager, theShellMsgManager.getPort(), true );
        }
        
        return theShellMsgManager;

    }/* END initialize() */
    
    // ==========================================================================
    /**
     *   Gets the ShellMessageManager
     * @return 
     */
    public synchronized static ShellMessageManager getMessageManager(){
        return theShellMsgManager;
    }
    
    //===============================================================
    /**
     *   Send the message out the given channel.
     *
     * @param msgBytes
    */
    @Override
    public void handleMessage( PortRouter srcPortRouter, byte[] msgBytes ) {        
        theShellMsgManager.getDataHandler().processData( srcPortRouter, msgBytes);        
    }
    
    //===========================================================================
    /*
     *  Returns the data handler
    */
    @Override
    public ShellMessageHandler getDataHandler() {
        return (ShellMessageHandler)theDataHandler;
    }   

    //===========================================================================
    /*
     *  Return the shell
     */
    public Shell getShell() {
        return theShell;
    }    
    
    //===========================================================================
    /*
     *  Set the shell
     */
    public void setShell(Shell aShell) {
        theShell = aShell;
    }
    
}
