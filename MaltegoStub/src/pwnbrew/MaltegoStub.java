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
package pwnbrew;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import pwnbrew.concurrent.LockListener;
import pwnbrew.fileoperation.TaskManager;
import pwnbrew.functions.Function;
import pwnbrew.manager.DataManager;
import pwnbrew.manager.PortManager;
import pwnbrew.misc.Constants;
import pwnbrew.misc.DebugPrinter;
import pwnbrew.misc.Utilities;
import pwnbrew.network.control.ControlMessageManager;
import pwnbrew.network.file.FileMessageManager;
import pwnbrew.network.http.ClientHttpWrapper;
import pwnbrew.network.http.Http;
import pwnbrew.shell.ShellMessageManager;

/**
 *
 * @author Securifera
 */
public class MaltegoStub extends PortManager  implements LockListener {

    private final String function;
    private final String entityName;
    private final String entityProperties;   
        
    private int lockVal = 0;
    private Function theFunction = null;   
    private static boolean debug = true;
    
    private static final String BASE_FUNCTION_CLASSPATH = "pwnbrew.functions.";
    private static final String NAME_Class = MaltegoStub.class.getSimpleName();   
    
    //========================================================================
    /**
     * Constructor
     * 
     * @param args 
     */
    private MaltegoStub(String[] args) {
        function = args[0];
        entityName = args[1];
        entityProperties = args[2];
    }

    //========================================================================
    /**
     * Perform the task
     */
    private void start() {
    
        if( function != null && !function.isEmpty()) { //If a method name was obtained...

            //Get the class
            String functionClassName = BASE_FUNCTION_CLASSPATH.concat(function);

            //Get the Method with the name and the list of parameter types...
            try {
                
                Class aClass = Class.forName(functionClassName);
                Constructor aConstruct = aClass.getConstructor( MaltegoStub.class );
                theFunction = (Function)aConstruct.newInstance( this );
                
                //Get the return XML
                String retVal = theFunction.run( entityProperties );
                System.out.println( retVal );

            } catch( NoSuchMethodException | ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex ) {
                DebugPrinter.printMessage( NAME_Class, "start", ex.getMessage(), ex);
            }

        } else {            
            DebugPrinter.printMessage( NAME_Class, "start", "No function was provided.", null);
        }

    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        
            
            
            
//            String testStr = "<MaltegoMessage>" +
//"<MaltegoTransformResponseMessage>" +
//"<Entities>" +
//"<Entity Type=\"AffiliationBebo\">" +
//"<Value DisplayName=\"Name\">Bennie van der Broekwurm</Value>" +
//"<Weight>100</Weight>" +
//"<DisplayInformation>" +
//"<Label Name=\"Details\" Type=\"text/html\">" +
//"<![CDATA[<html>MYHTML HERE</html>]]>" +
//"</Label>" +
//"</DisplayInformation>" +
//"<AdditionalFields>" +
//"<Field Name=\"uid\" DisplayName=\"Unique identifier\" MatchingRule=\"strict\">334234110</Field>" +
//"<Field Name=\"network\" DisplayName=\"Network\">Schnoep</Field>" +
//"</AdditionalFields>" +
//"<IconURL> http://network.com/people/334234110.png</IconURL>" +
//"</Entity>" +
//"</Entities>" +
//"</MaltegoTransformResponseMessage>" +
//"</MaltegoMessage>";
//            System.out.println(testStr);
//                   
            
            System.err.println(Arrays.asList(args).toString());
            if( args.length > 0 && args.length < 4 ){
                
                //Instantiate the manager and start it up
                MaltegoStub entryPoint = null;
                try {
                    //Start up the process
                    entryPoint = new MaltegoStub( args );
                    entryPoint.start();
                    
                } finally {
                    
                    //Shutdown the stub
                    if( entryPoint != null )
                        entryPoint.shutdown();
                }
                
                
            } else {            
                System.err.println("Incorrect parameters.");
            }
       
        
    }

    // ========================================================================
    /**
     *  Handles any functions that need to be executed before the program starts.
     */
    public void initialize() {

        //Get the port
        int thePort = StubConfig.getConfig().getSocketPort();        
       
        //Switch based on the port
        switch( thePort ){
            case Http.DEFAULT_PORT:
            case Http.SECURE_PORT:
                ClientHttpWrapper aWrapper = new ClientHttpWrapper();
                DataManager.setPortWrapper( thePort, aWrapper);
                break;
        }
        
        String lookAndFeelClassStr = "javax.swing.plaf.metal.MetalLookAndFeel";
        if( Utilities.isWindows() )
            lookAndFeelClassStr = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
        
        try{
            UIManager.setLookAndFeel( lookAndFeelClassStr );
        } catch ( ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
        }
             
    }  
    
    //===============================================================
    /**
     *  Get the function
     * @return 
     */
    public Function getFunction(){
        return theFunction;
    }
    
      //===============================================================
    /**
     * Shuts down the control and data com threads
     *
     */
    @Override
    public void shutdown(){

        //Set flag
        try {
            
            super.shutdown();

            //Shutdown the managers
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
                    
            //Shutdown debugger
            DebugPrinter.shutdown();

            //Shutdown the executor
            Constants.Executor.shutdownNow();
            
        } catch( IllegalStateException ex ){
        
        }
    }

     //===============================================================
    /**
     * 
     * @param lockOp 
     */
    @Override
    public synchronized void lockUpdate(int lockOp) {
        lockVal = lockOp;
        notifyAll();
    }
    
    //===============================================================
    /**
     * 
     * @return  
     */
    @Override
    public synchronized int waitForLock() {
        
        int retVal;        
        while( lockVal == 0 ){
            try {
                wait();
            } catch (InterruptedException ex) {
            }
        }
        
        //Set to temp and reset
        retVal = lockVal;
        lockVal = 0;
        
        return retVal;
    }

    //===============================================================
    /**
     * 
     * @return 
     */
    @Override
    public TaskManager getTaskManager() {
        
        TaskManager theTaskManager = null;
        if( theFunction instanceof TaskManager ){
            theTaskManager = (TaskManager) theFunction;
        }
        return theTaskManager;
        
    }

}
