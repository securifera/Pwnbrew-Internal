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
// * RunnableItemController.java
// *
// * Created on July 16, 2013, 9:11:12 AM
// */
//
//package pwnbrew.controllers;
//
//import java.awt.event.ActionEvent;
//import javax.swing.JOptionPane;
//import pwnbrew.library.LibraryItemController;
//import pwnbrew.execution.ExecutionHandler;
//import pwnbrew.gui.MainGui;
//import pwnbrew.host.HostController;
//import pwnbrew.library.LibraryItemControllerListener;
//import pwnbrew.misc.Constants;
//
//
///**
// *
// */
//abstract public class RunnableItemController extends LibraryItemController {
//
//    protected boolean errorOccurred = false;
//    protected boolean cancelled = false;
//    
//    protected ExecutionHandler theExecutionHandler = null;
//    protected RunObserver theRunObserver = null;  
//
//    //====================================================================
//    /**
//     * 
//     * @param passedBool 
//     */
//    public RunnableItemController(boolean passedBool) {
//        super(passedBool);
//    }   
// 
//     // ==========================================================================
//    /**
//    *   Action Listener implementation.
//    *
//     * @param evt
//    */
//    @Override
//    public void actionPerformed(ActionEvent evt) {
//        
//        String actionCommand = evt.getActionCommand();
//        
//        boolean found = true;
//        LibraryItemControllerListener theLibraryItemControllerListener = getLibraryItemControllerListener();
//        if( theLibraryItemControllerListener != null ){
//           
//            MainGui theGui = (MainGui)theLibraryItemControllerListener.getParentJFrame();
//            switch (actionCommand) {
//
//                case Constants.ACTION_Run:
//
//                    //Run locally
//                    HostController theHostController = theLibraryItemControllerListener.getParentController( this );
//                    if( theHostController != null ){
//                        if( theHostController.isLocalHost() ){
//
//                            if( isRunning() ) { //If the item is already running...
//                                theGui.informUserItemIsAlreadyRunning( this );
//                            } else { //If the item is not already running...
//
//                                String cantRunMessage = canRun(); //Determine if the item can be run
//                                if( cantRunMessage == null ) { //If the item can be run...
//
//                                    //Prompt the user to confirm the run...
//                                    if( theGui.promptUserToConfirmRun( this ) ) { //If the user confirms the run...
//                                        runItem( null ); //Run the item
//                                    }
//
//                                } else { //If the item can't be run...
//                                    theGui.informUserItemCantRun( getItemName(), cantRunMessage );
//                                }
//
//                            }                       
//
//                        } else {            
//
//                            //Try and run remotely
//                            if( theHostController.isConnected() ){                                
//                                theLibraryItemControllerListener.sendRemoteJob( this );                                 
//                            } else {
//                                //Popup an error message because the host isn't connected
//                                JOptionPane.showMessageDialog( theGui, "Unable to execute remote job.  The host is disconnected.",
//                                                "Error", JOptionPane.ERROR_MESSAGE );
//                            }            
//
//                        }
//                    }
//                    break;
//
//                case Constants.CANCEL:
//                    
//                    //Attempts to cancel the running job
//                    theLibraryItemControllerListener.cancelRunForCurrentNode( this );
//                    break;
//                default:   
//                    found = false;
//                    break;
//            }
//            
//        } else {
//            found = false;
//        }
//        
//        //Call the parent listener if the others didn't catch it
//        if( !found ){
//            super.actionPerformed(evt);
//        }
//    }
//    
//    // ========================================================================
//    /**
//     * Runs the {@link RunnableItemController}'s item.
//     * @param observer
//     */
//    abstract public void runItem( RunObserver observer );
//    
//   
//    // ========================================================================
//    /**
//     * Cancels the running of the {@link RunnableItemController}'s item.
//     */
//    abstract public void cancelRun();
//      
//    // ========================================================================
//    /**
//     * Determines if the {@link RunnableItemController}'s item is running.
//     * 
//     * @return {@code true} if the item is running; {@code false} otherwise
//     */
//    public boolean isRunning() {
//        return theExecutionHandler != null ;
//    }
//    
//    
//    // ========================================================================
//    /**
//     * Determines if the {@link RunnableItemController}'s item can run.
//     * 
//     * @return null if the item can be run; a String describing why the item cannot
//     * be run
//     */
//    abstract public String canRun();
//    
//    
//    // ========================================================================
//    /**
//     * Creates a message for confirming the run of the {@link RunnableItemController}'s item.
//     * 
//     * @param builder the {@link StringBuilder} to which the message is to be appended
//     */
//    abstract public void createRunConfirmationMessage( StringBuilder builder );
//    
//}/* END CLASS RunnableItemController */
