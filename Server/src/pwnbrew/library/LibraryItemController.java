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
 * LibraryItemController.java
 *
 * Created on June 21, 2013, 7:55:11 PM
 */

package pwnbrew.library;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import pwnbrew.xmlBase.XmlBase;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import pwnbrew.controllers.Controller;
import pwnbrew.gui.Iconable;
import pwnbrew.host.HostController;
import pwnbrew.host.HostFactory;
import pwnbrew.logging.Log;
import pwnbrew.misc.Constants;
import pwnbrew.misc.Directories;
import pwnbrew.generic.gui.SavablePanel;
import pwnbrew.gui.panels.RunnerPane;


/**
 * 
 */
abstract public class LibraryItemController extends Controller implements Iconable, ActionListener {

    protected Icon theIcon;
      
  
    //Dirty flag - used to determine if an object needs to be saved
    private boolean isDirtyFlag = false;
    
    //Just Imported flag - used to determine how to render object's that have just been imported into the app
    private boolean justImported = false;
    protected static final String NAME_Class = LibraryItemController.class.getSimpleName();
    
    // ==========================================================================
    /**
    *   Action Listener implementation.
    *
     * @param evt
    */
    @Override
    public void actionPerformed(ActionEvent evt) {
         
        String actionCommand = evt.getActionCommand();
        switch (actionCommand) {
           
            case Constants.SAVE_ALL:
                
                //Save to disk
                saveToDisk();
                
                //Disable the save button and repaint
                LibraryItemPanel thePanel = (LibraryItemPanel) getRootPanel();
                thePanel.setSaveButton( false );
                thePanel.repaint();
                
                //Call for a repaint
                getLibraryItemControllerListener().getListenerComponent().repaint();
                break;
                
            default:               
                break;
        }
    }
    
    // ==========================================================================
    /**
    * Returns the value of the {@link XmlBase}'s "dirty" flag indicating if the data
    * in this object has been changed.
    *
    * @return the value of the {@code XmlBase}'s "dirty" flag
    */
    public final boolean isDirty() {
        return isDirtyFlag;
    }/* END isDirty() */


    // ==========================================================================
    /**
    * Sets the value of the {@link XmlBase}'s dirty flag to the given value.
    * <p>
    *
    * @param passedDirtyFlag the new dirty flag value.
    */
    public final void setIsDirty( boolean passedDirtyFlag ) {
        isDirtyFlag = passedDirtyFlag;

        //Disable the save button and repaint
        JPanel aPanel = getRootPanel();
        if( aPanel instanceof SavablePanel ){
            SavablePanel thePanel = (SavablePanel) getRootPanel();
            thePanel.setSaveButton(passedDirtyFlag);
        }

    }/* END setIsDirty( boolean ) */
    
    // ========================================================================
    /**
     *  Returns the library item listener
     * 
     * @return 
    */
    abstract public LibraryItemControllerListener getLibraryItemControllerListener();

  
    // ==========================================================================
    /**
    * Returns the value of the {@link XmlBase}'s "just imported" flag indicating if the data
    * in this object has been changed.
    *
    * @return the value of the {@code XmlBase}'s "just imported" flag
    */
    public final boolean justImported() {
        return justImported;
    }/* END justImported() */
    
    // ==========================================================================
    /**
    * Sets the value of the {@link XmlBase}'s just imported flag to the given value.
    * <p>
    *
    * @param passedBool the new just imported value.
    */
    public final void setJustImported( boolean passedBool ) {
        justImported = passedBool;
    }/* END setJustImported( boolean ) */
    
    
    // ========================================================================
    /**
     *  Returns the icon for the library item.
     * @return 
     */
    @Override
    public Icon getIcon() {
        return theIcon;
    }
    
    // ========================================================================
    /**
     *  Returns the icon for the library item.
     * @return 
     */
    public File getObjectLibraryDirectory() {
        
        String subDirPath = "";
        HostController aHostController = getHostController();
        if( aHostController != null ){
        
            if( aHostController.isLocalHost() ){
            
                //Get the object id
                subDirPath = aHostController.getId();                
                
            } else {
                
                //Get the parent id
                Object controlledObject = aHostController.getObject();
                if( controlledObject instanceof XmlBase ){
                    subDirPath = "r" + ((XmlBase)controlledObject).getId();
                }  
            
            }
            
        } else {
            
            subDirPath = HostFactory.LOCALHOST;
        }
                
        File objectDir = new File( Directories.getObjectLibraryDirectory(), subDirPath);
        return objectDir;
    }
    
    // ========================================================================
    /**
     *  Get the parent HostController.
     * 
     * @return 
     */
    public HostController getHostController(){
        return null;
    }
    
    // ========================================================================
    /**
     *  Returns the right click popup menu for the library item.
     * @param multiSelect
     * @return 
     */
    abstract public JPopupMenu getPopupJMenu( boolean multiSelect );
    
    // ========================================================================
    /**
     *  Returns the string displayed when 
     * @return 
     */
    abstract public String getCreationAction();
    
    // ========================================================================
    /**
     *  Creates the underlying item that the controller is in charge of.
     * @param passedListener
     * @return 
     */
    abstract public Object createItem( LibraryItemControllerListener passedListener );

    // ==========================================================================
    /**
     * Saves the given {@link XmlBase} to the Object Library directory.
     * 
    */
    public void saveToDisk() {
     
        Object theObject = getObject();
        if( theObject == null ) return;

        if( theObject instanceof XmlBase ){
            
            XmlBase xmlBase = (XmlBase)theObject;
            File saveDir = getObjectLibraryDirectory();
            if( saveDir != null ) {

                String saveResult;
                try {

                    saveResult = xmlBase.writeSelfToDisk( saveDir, -1 );

                    if( saveResult != null ) {
                        Log.log( Level.SEVERE, NAME_Class, "saveToDisk()",
                                new StringBuilder( "Could not save \"" ).append( xmlBase.getName() )
                                .append( "\". " ).append( saveResult ).toString(), null );
                    } else {
                        setIsDirty( false );
                    }

                } catch( IOException ex ) {
                    Log.log( Level.SEVERE, NAME_Class, "saveToDisk()",
                            new StringBuilder( "Could not save \"" ).append( xmlBase.getName() )
                            .append( "\". " ).append( ex.getMessage() ).toString(), ex );
                }

            } else {
                Log.log( Level.WARNING, NAME_Class, "saveToDisk()", "Could not obtain a save directory.", null );
            }
        }

    }/* END saveToDisk() */
    
    //****************************************************************************
    /**
    * Deletes the given {@link XmlBase} from the library.
    * 
    * <p>
    * If the given {@code XmlBase} is null, this method does nothing.
    *
    */
    public void deleteFromLibrary() {

        Object theObj = getObject();
        if( theObj == null ) {
            return; //Do nothing
        }

        //Delete the object's file
        XmlBase theXB = (XmlBase)theObj;
        theXB.deleteSelfFromDirectory( getObjectLibraryDirectory() );

    }
    
    
    // ========================================================================
    /**
     * 
     * @return 
     */
    abstract public String getItemName();
    
    
    // ========================================================================
    /**
     * Returns the name of the {@link LibraryItem} type for use in messages to the
     * user.
     * @return 
     */
    abstract public String getItemTypeDisplayName();
    
    
    // ========================================================================
    /**
     * Determines if the given String is a valid name for the {@link LibraryItemController}'s
     * item type.
     * 
     * @param name the String to test
     * 
     * @return {@code true} if the name is valid; {@code false} otherwise
     */
    public boolean isValidNameForItem( String name ) {
        
        boolean rtnBool = false;
        
        if( name != null ) {
            
            name = name.trim();
            if( name.isEmpty() == false )
                rtnBool = true;
            
        }
        
        return rtnBool;
        
    }/* END isValidNameForItem( String ) */
    
    // ==========================================================================
    /**
    * Clones the controller and the underlying object.
    * (Named it copy because FindBugs complains about the name clone())
     * @return 
    */
    abstract public LibraryItemController copy();
    
    
    // ========================================================================
    /**
     * 
     * @param name
     */
    public void changeLibraryItemName( String name ) {
        
        Object object = getObject();
        if( object instanceof XmlBase ) {
            
            //Change the name in the item...
            ( (XmlBase)object ).setAttribute( XmlBase.ATTRIBUTE_Name, name );
            saveToDisk();
            
            //Change the name in the interface...
            JPanel jpanel = getRootPanel();
            if( jpanel instanceof LibraryItemPanel )
                ( (LibraryItemPanel)jpanel ).setLibraryItemName( name );
            
        }
        
    }/* END changeLibraryItemName( String ) */

    // ========================================================================
    /**
     *  Gets the runner pane
     * 
     * @param b whether to show the runner pane
     * @return 
     */
    public RunnerPane getRunnerPane(boolean b) {
        return null;
    }
    

        
}/* END CLASS LibraryItemController */
