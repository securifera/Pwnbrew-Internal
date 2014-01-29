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
 * Command.java
 *
 * Created on Jun 13, 2013, 3:05:49 PM
 */

package pwnbrew.xmlBase;

import pwnbrew.controllers.CommandController;
import pwnbrew.library.LibraryItemControllerListener;
import pwnbrew.xmlBase.job.Job;
import java.util.List;
import pwnbrew.utilities.Utilities;


/**
 * 
 */
public class Command extends Job {   

    private static final String ATTRIBUTE_Command = "Command";    
    public static final String ICON_STR = "taskIcon.png";
    public static final String NEG_TASK_ICON_STR = "taskRunning.png";    
    
    public static final String NAME_Class = Command.class.getSimpleName();
    
    
    // ==========================================================================
    /**
     * Creates a new instance of {@link Command}.
     */
    public Command() {

        //Add the cmd line array
        theAttributeCollectionMap.put(ATTRIBUTE_Command, new AttributeCollection(ATTRIBUTE_Command, new String[0]));
    
    }/* END CONSTRUCTOR() */

    
    // ==========================================================================
    /**
     * Sets the command to the given {@code String}.
     * <p>
     * If the given {@code String} is null, the command will be set to the empty
     * {@code String}.
     *
     * @param command
     */
    public void setCommand( String[] command ) {

        if( command == null ) { //If the String is null...
            command = new String[0]; //Use the empty String
        }

        //Add the cmd line array
        theAttributeCollectionMap.put(ATTRIBUTE_Command, new AttributeCollection(ATTRIBUTE_Command, command));
     
    }/* END setCommand( String ) */


    // ==========================================================================
    /**
     * Returns the command.
     *
     * @return the command
     */
    public List<String> getCommand() {
        List<String> theCmdLine = null;
        
        AttributeCollection theCollection = theAttributeCollectionMap.get(ATTRIBUTE_Command);
        if(theCollection != null){
           theCmdLine = theCollection.getCollection();
        }
        return theCmdLine;
        
    }/* END getCommand() */

    // ==========================================================================
    /**
     * Returns an array of strings representing the command line arguments.
     *
     * @param passedOsName
     * @return a {@code String[]} representing the command line arguments.
     */
    @Override //ExecutableItem
    public String[] getCommandArgs( String passedOsName ) {
        
          List<String> theCmdList = getCommand();
          String[] taskCmdLineArgs = theCmdList.toArray( new String[theCmdList.size()]);
          
          //Setup the cmd line args
          String[] theCmdLineArgs;
          if( Utilities.isWindows(passedOsName)){
             theCmdLineArgs = new String[taskCmdLineArgs.length + 2];
             theCmdLineArgs[0] = "cmd.exe";
             theCmdLineArgs[1] = "/c";
             
             //Copy over the rest of the arguments
             System.arraycopy(taskCmdLineArgs, 0, theCmdLineArgs, 2, taskCmdLineArgs.length);

          } else {
             
             //Copy over to cmd line args
             theCmdLineArgs = new String[taskCmdLineArgs.length];
             System.arraycopy(taskCmdLineArgs, 0, theCmdLineArgs, 0, taskCmdLineArgs.length);

          }
          
          return theCmdLineArgs;
    }

    // ========================================================================
    /**
     * Creates a new CommandController and assigns this object to it.
     * 
     * @param passedListener
     * @return 
     */
    @Override
    public CommandController instantiateController(LibraryItemControllerListener passedListener) {
        return new CommandController( this, passedListener );
    }

}/* END CLASS Command */
