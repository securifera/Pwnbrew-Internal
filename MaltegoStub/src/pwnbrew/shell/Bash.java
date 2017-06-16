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

package pwnbrew.shell;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.regex.Pattern;
import javax.swing.text.StyledDocument;

/**
 *
 *  
 */
public class Bash extends Shell {
    
//    private static final String[] BASH_EXE_STR = new String[]{ "/bin/bash", "-i"};
    private static final String[] BASH_EXE_STR = new String[]{ "python", "-c", "import pty;pty.spawn(\"/bin/bash\")"};
    private static final String encoding = "UTF-8";
    private static final String PROMPT_REGEX_BASH = "\\x1b.*[$#]";
    private static final Pattern PROMPT_PATTERN = Pattern.compile(PROMPT_REGEX_BASH);
    
    /**
     * Keeps track of the current argument of the current escape sequence.
     * Ranges from 0 to MAX_ESCAPE_PARAMETERS-1. (Typically just 0 or 1.)
     */
    private int mArgIndex;
    
    /**
     * The number of parameter arguments. This name comes from the ANSI standard
     * for terminal escape codes.
     */
    private static final int MAX_ESCAPE_PARAMETERS = 16;

//    /**
//     * Holds the arguments of the current escape sequence.
//     */
//    private int[] mArgs = new int[MAX_ESCAPE_PARAMETERS];
//
//    /**
//     * Holds OSC arguments, which can be strings.
//     */
//    private byte[] mOSCArg = new byte[MAX_OSC_STRING_LENGTH];

    private int mOSCArgLength;


    /**
     * Don't know what the actual limit is, this seems OK for now.
     */
    private static final int MAX_OSC_STRING_LENGTH = 512;
    
     /**
     * Escape processing state: Not currently in an escape sequence.
     */
    private static final int ESC_NONE = 0;

    /**
     * Escape processing state: Have seen an ESC character
     */
    private static final int ESC = 1;

    /**
     * Escape processing state: Have seen ESC POUND
     */
    private static final int ESC_POUND = 2;

    /**
     * Escape processing state: Have seen ESC and a character-set-select char
     */
    private static final int ESC_SELECT_LEFT_PAREN = 3;

    /**
     * Escape processing state: Have seen ESC and a character-set-select char
     */
    private static final int ESC_SELECT_RIGHT_PAREN = 4;

    /**
     * Escape processing state: ESC [
     */
    private static final int ESC_LEFT_SQUARE_BRACKET = 5;

    /**
     * Escape processing state: ESC [ ?
     */
    private static final int ESC_LEFT_SQUARE_BRACKET_QUESTION_MARK = 6;

    /**
     * Escape processing state: ESC %
     */
    private static final int ESC_PERCENT = 7;

    /**
     * Escape processing state: ESC ] (AKA OSC - Operating System Controls)
     */
    private static final int ESC_RIGHT_SQUARE_BRACKET = 8;

    /**
     * Escape processing state: ESC ] (AKA OSC - Operating System Controls)
     */
    private static final int ESC_RIGHT_SQUARE_BRACKET_ESC = 9;
    
    /**
     * True if the current escape sequence should continue, false if the current
     * escape sequence should be terminated. Used when parsing a single
     * character.
     */
    private boolean mContinueSequence;
    
    /**
     * The current state of the escape sequence state machine.
     */
    private int mEscapeState;
    
    private ByteBuffer aBB;
   
    
    // ==========================================================================
    /**
     *  Constructor
     * 
     * @param passedExecutor
     * @param passedListener 
     */
    public Bash(Executor passedExecutor, ShellListener passedListener) {
        super(passedExecutor, passedListener);
    }
    
    // ==========================================================================
    /**
     *  Get the command string
     * 
     * @return 
     */
    @Override
    public String[] getCommandStringArray() {                
        return BASH_EXE_STR;
    }
    
    private void process(byte b ) {
       
        // Handle C1 control characters
        if ((b & 0x80) == 0x80 && (b & 0x7f) <= 0x1f) {
            /* ESC ((code & 0x7f) + 0x40) is the two-byte escape sequence
               corresponding to a particular C1 code */
            process((byte) 27 );
            process((byte) ((b & 0x7f) + 0x40) );
            return;
        }

        switch (b) {
        case 0: // NUL
            // Do nothing
            break;

        case 7: // BEL
            /* If in an OSC sequence, BEL may terminate a string; otherwise do
             * nothing */
            if (mEscapeState == ESC_RIGHT_SQUARE_BRACKET) {
                doEscRightSquareBracket(b);
            }
            break;

        case 8: // BS
        case 9: // HT
        case 13:
            aBB.put((byte) b);
            break;

        case 10: // CR
        case 11: // VT
        case 12: // LF
            doLinefeed();
            break;

        case 14: // SO:
        case 15: // SI:
            break;


        case 24: // CAN
        case 26: // SUB
            if (mEscapeState != ESC_NONE) {
                mEscapeState = ESC_NONE;
                aBB.put((byte) 127);
            }
            break;

        case 27: // ESC
            // Starts an escape sequence unless we're parsing a string
            if (mEscapeState != ESC_RIGHT_SQUARE_BRACKET) {
                startEscapeSequence(ESC);
            } else {
                doEscRightSquareBracket(b);
            }
            break;

        default:
            mContinueSequence = false;
            switch (mEscapeState) {
            case ESC_NONE:
                if (b >= 32) 
                    aBB.put( b);                
                break;

            case ESC:
                doEsc(b);
                break;

            case ESC_POUND:
            case ESC_SELECT_LEFT_PAREN:
            case ESC_SELECT_RIGHT_PAREN:
                break;

            case ESC_LEFT_SQUARE_BRACKET:
                doEscLeftSquareBracket(b); // CSI
                break;

            case ESC_LEFT_SQUARE_BRACKET_QUESTION_MARK:
            case ESC_PERCENT:
                break;

            case ESC_RIGHT_SQUARE_BRACKET:
                doEscRightSquareBracket(b);
                break;

            case ESC_RIGHT_SQUARE_BRACKET_ESC:
                doEscRightSquareBracketEsc(b);
                break;

            default:
                unknownSequence(b);
                break;
            }
            if (!mContinueSequence) {
                mEscapeState = ESC_NONE;
            }
            break;
        }
    }
    
    private void unknownSequence(byte b) {
        finishSequence();
    }
    
    private void finishSequence() {
        mEscapeState = ESC_NONE;
    }
    
    private void startEscapeSequence(int escapeState) {
        mEscapeState = escapeState;
        mArgIndex = 0;
//        for (int j = 0; j < MAX_ESCAPE_PARAMETERS; j++) {
//            mArgs[j] = -1;
//        }
    }
    
    private void doEsc(byte b) {
        switch (b) {
        case '#':
            continueSequence(ESC_POUND);
            break;

        case '(':
            continueSequence(ESC_SELECT_LEFT_PAREN);
            break;

        case ')':
            continueSequence(ESC_SELECT_RIGHT_PAREN);
            break;

        case '7': 
        case '8': 
            break;

        case 'D': // INDEX
            doLinefeed();
            break;

        case 'E': // NEL
            doLinefeed();
            break;

        case 'F': // Cursor to lower-left corner of screen
        case 'H': // Tab set
        case 'M': // Reverse index
            break;

        case 'N': // SS2
            unimplementedSequence(b);
            break;

        case '0': // SS3
            unimplementedSequence(b);
            break;

        case 'P': // Device control string
            unimplementedSequence(b);
            break;

        case 'Z': // return terminal ID
            //sendDeviceAttributes();
            break;

        case '[':
            continueSequence(ESC_LEFT_SQUARE_BRACKET);
            break;

        case '=': // DECKPAM
            break;

        case ']': // OSC
            startCollectingOSCArgs();
            continueSequence(ESC_RIGHT_SQUARE_BRACKET);
            break;

        case '>' : // DECKPNM
            break;

        default:
            unknownSequence(b);
            break;
        }
    }

    private void doEscLeftSquareBracket(byte b) {
        // CSI
        switch (b) {
        case '@': // ESC [ Pn @ - ICH Insert Characters
        case 'A': // ESC [ Pn A - Cursor Up
        case 'B': // ESC [ Pn B - Cursor Down
        case 'C': // ESC [ Pn C - Cursor Right
            break;
        case 'D': // ESC [ Pn D - Cursor Left
            break;
        case 'G': // ESC [ Pn G - Cursor Horizontal Absolute
        case 'H': // ESC [ Pn ; H - Cursor Position
        case 'J': // ESC [ Pn J - ED - Erase in Display
        case 'K': // ESC [ Pn K - Erase in Line
        case 'L': // Insert Lines
        case 'M': // Delete Lines
        case 'P': // Delete Characters
        case 'T': // Mouse tracking
        case 'X': // Erase characters
        case 'Z': // Back tab
        case '?': // Esc [ ? -- start of a private mode set
        case 'c': // Send device attributes
        case 'd': // ESC [ Pn d - Vert Position Absolute
        case 'f': // Horizontal and Vertical Position
        case 'g': // Clear tab stop
        case 'h': // Set Mode
        case 'l': // Reset Mode
        case 'm': // Esc [ Pn m - character attributes.
        case 'n': // Esc [ Pn n - ECMA-48 Status Report Commands
        case 'r': // Esc [ Pn ; Pn r - set top and bottom margins
            break;

        default:
            parseArg(b);
            break;
        }
    }
    
    /**
     * Process the next ASCII character of a parameter.
     *
     * @param b The next ASCII character of the paramater sequence.
     */
    private void parseArg(byte b) {
        if (b >= '0' && b <= '9') {
//            if (mArgIndex < mArgs.length) {
//                int oldValue = mArgs[mArgIndex];
//                int thisDigit = b - '0';
//                int value;
//                if (oldValue >= 0) {
//                    value = oldValue * 10 + thisDigit;
//                } else {
//                    value = thisDigit;
//                }
//                mArgs[mArgIndex] = value;
//            }
            continueSequence();
        } else if (b == ';') {
//            if (mArgIndex < mArgs.length) {
//                mArgIndex++;
//            }
            continueSequence();
        } else {
            unknownSequence(b);
        }
    }
    
    private void startCollectingOSCArgs() {
        mOSCArgLength = 0;
    }

    private void collectOSCArgs(byte b) {
        if (mOSCArgLength < MAX_OSC_STRING_LENGTH) {
//            mOSCArg[mOSCArgLength++] = b;
            continueSequence();
        } else {
            unknownSequence(b);
        }
    }

    
    private void doEscRightSquareBracket(byte b) {
        switch (b) {
        case 0x7:
            doOSC();
            break;
        case 0x1b: // Esc, probably start of Esc \ sequence
            continueSequence(ESC_RIGHT_SQUARE_BRACKET_ESC);
            break;
        default:
            collectOSCArgs(b);
            break;
        }
    }

    private void doEscRightSquareBracketEsc(byte b) {
        switch (b) {
        case '\\':
            doOSC();
            break;

        default:
            // The ESC character was not followed by a \, so insert the ESC and
            // the current character in arg buffer.
            collectOSCArgs((byte) 0x1b);
            collectOSCArgs(b);
            continueSequence(ESC_RIGHT_SQUARE_BRACKET);
            break;
        }
    }

    private void doOSC() { // Operating System Controls
        finishSequence();
    }
    
    private void unimplementedSequence(byte b) {
        finishSequence();
    }
    
    private void continueSequence() {
        mContinueSequence = true;
    }

    private void continueSequence(int state) {
        mEscapeState = state;
        mContinueSequence = true;
    }
    
    private void doLinefeed() {
        aBB.put((byte)'\n');
    }

   
    // ==========================================================================
    /**
     * Handles the bytes read
     *
     * @param passedId
     * @param buffer the buffer into which the bytes were read
     */
    @Override
    public void handleBytesRead( int passedId, byte[] buffer ) {

        //Remove ansi codes
        aBB = ByteBuffer.allocate(buffer.length);
        for( byte cur : buffer ){
            process(cur);
        }
        
        byte[] byteArr = Arrays.copyOf(aBB.array(), aBB.position());
        super.handleBytesRead(passedId, byteArr);
       
    }
    
     // ==========================================================================
    /**
     * 
     * @param keyCode 
     */
    @Override
    public void handleCtrlChar(int keyCode) {
        char EOT = (byte)0x3;
        sendInput( "" + EOT );
    }
    
    //===============================================================
    /**
     *
    */
    @Override
    public void printPreviousCommand(){
                
        char escape = (byte)0x1b;
        sendInput( escape + "[A" );
    }
    
    //===============================================================
    /**
     *
    */
    @Override
    public void printNextCommand(){
                
        char escape = 0x1b;
        sendInput( escape + "[B" );
    }
    
    // ==========================================================================
    /**
     *  Get character encoding.
     * 
     * @return 
     */
    @Override
    public String getEncoding() {
        return encoding;
    }
    
    // ==========================================================================
    /**
     * 
     * @return 
     */
    @Override
    public String toString(){
        return "Bash";
    }
}
