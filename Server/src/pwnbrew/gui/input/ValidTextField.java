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

package pwnbrew.gui.input;

import pwnbrew.logging.Log;
import pwnbrew.logging.LoggableException;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.logging.Level;
import javax.swing.JTextField;
import pwnbrew.exception.NoSuchValidationException;
import pwnbrew.validation.StandardValidation;


/**
 *  
 */
public class ValidTextField extends JTextField implements KeyListener {

    private String thePreviousValue = null;
    private boolean dataIsValid = true;
    private String validation = "";

    private static final String NAME_Class = ValidTextField.class.getSimpleName();


    // ==========================================================================
    /**
     * Creates a new instance of {@link Ipv4AddressTextField}.
     */
    public ValidTextField() {
        this( "" );
    }/* END CONSTRUCTOR() */


    // ==========================================================================
    /**
     * Creates a new instance of {@link Ipv4AddressTextField}.
     * <p>
     * If the argument is null an empty String is used instead.
     *
     * @param text the text to be displayed
     */
    public ValidTextField( String text ) {

        super( ( text == null ? "" : text ) );

        thePreviousValue = getText();
        evaluateValue();

        addKeyListener( this );
        //NOTE: The leak of 'this' should be incocuous.

    }/* END CONSTRUCTOR( String ) */


    // ==========================================================================
    /**
     * Evaluates the current value in the {@link ClientMigrateTextField}.
     * <p>
     * If the value is a valid IPv4 address and ports this method sets the background of th
     * field to white; otherwise the background color is set to red.
     *
     */
    private void evaluateValue() {

        String value = getText(); //Get the value

        if( thePreviousValue.equals( value ) == false ) { //If the value has changed...

            if( validation != null && !validation.isEmpty() ){
                try {
                    dataIsValid = StandardValidation.validate( validation, value );
                } catch( NoSuchValidationException ex ) {
                    //This case won't occur.
                    ex = null;
                } catch( LoggableException ex ) {
                   Log.log(Level.WARNING, NAME_Class, "evaluateValue()", ex.getMessage(),ex );
                }
            }

            //Set the backgroun color of the text field...
            if( dataIsValid ) //If the data is valid...
                setBackground( Color.WHITE );
            else //If the data is not valid...
                setBackground( Color.RED );

        } //Else, the value has not changed; do nothing

    }/* END evaluateValue() */

     // ==========================================================================
    /**
     * Sets the validation string
     * 
     * @param passedValidationString
     */
    public void setValidation( String passedValidationString ) {
        validation = passedValidationString;
    }/* END setValidation() */

    // ==========================================================================
    /**
     * Determines whether the text most recently evaluated was valid.
     *
     * @return <tt>true</tt> if text most recently evaluated was valid; <tt>false</tt>
     * otherwise
     */
    public boolean isDataValid() {
        return dataIsValid;
    }/* END isDataValid() */


    // ==========================================================================
    /**
     * Responds to the event.
     *
     * @param e the {@code KeyEvent} (ignored)
     */
    @Override //KeyListener
    public void keyTyped( KeyEvent e ) {
        evaluateValue();
    }/* END keyTyped( KeyEvent ) */


    // ==========================================================================
    /**
     * Does nothing.
     *
     * @param e the {@code KeyEvent} (ignored)
     */
    @Override //KeyListener
    public void keyPressed( KeyEvent e ) {
        //Do nothing.
    }


    // ==========================================================================
    /**
     * Responds to the event.
     *
     * @param e the {@code KeyEvent} (ignored)
     */
    @Override //KeyListener
    public void keyReleased( KeyEvent e ) {
        evaluateValue();
    }

}
