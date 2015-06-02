package pwnbrew.utilities;

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
// * Symbol.java
// *
// * Created on June 7, 2013, 6:58:43 PM
// */
//
//package pwnbrew.misc;
//
//
///**
// * This enumeration is a set of all of the non-alphanumeric characters appearing
// * on the standard English keyboard.
// *
// */
//public enum Symbol {
//
//    Ampersand( '&' ), Apostrophe( '\'' ), Asterisk( '*' ), At( '@' ), BackSlash( '\\' ), // NO_UCD (unused code)
//    Backtick( '`' ), BraceOpen( '{' ), BraceClose( '}' ), BracketOpen( '[' ), BracketClose( ']' ), // NO_UCD (unused code)
//    Caret( '^' ), Colon( ':' ), Comma( ',' ), Dollar( '$' ), Equals( '=' ), // NO_UCD (unused code)
//    ExclamationMark( '!' ), GreaterThan( '>' ), Hyphen( '-' ), LessThan( '<' ), NewLine( '\n' ), // NO_UCD (unused code)
//    Number( '#' ), Percent( '%' ), ParenthesesOpen( '(' ), ParenthesesClose( ')' ), Period( '.' ), // NO_UCD (unused code)
//    Pipe( '|' ), Plus( '+' ), QuestionMark( '?' ), Quote( '\"' ), Semicolon( ';' ), // NO_UCD (unused code)
//    Space( ' ' ), Slash( '/' ), Tilde( '~' ), Underscore( '_' ); // NO_UCD (unused code)
//
//    private final char CHAR;
//
//    
//    // ==========================================================================
//    /**
//     * Creates a new instance of {@link Symbol}.
//     * @param symbol the symbol
//     */
//    private Symbol( char symbol ) {
//        CHAR = symbol;
//    }/* END CONSTRUCTOR( char ) */
//
//
//    // ==========================================================================
//    /**
//     * Returns the symbol's character.
//     * @return the symbol's character
//     */
//    public char getChar() {
//        return CHAR;
//    }/* END getChar() */
//
//
//    // ==========================================================================
//    /**
//     * Returns the symbol as a String.
//     * @return the symbol as a String
//     */
//    public String asString() {
//        return "" + CHAR;
//    }/* END asString() */
//
//
//    // ==========================================================================
//    /**
//     * Determines if the given {@code char} is this symbol.
//     *
//     * @param c
//     * @return {@code true} if the given {@code char} is of this symbol.
//     */
//    public boolean is( char c ) {
//        return ( CHAR == c );
//    }/* END is( char ) */
//
//   
//    // ==========================================================================
//    /**
//     * Returns the name of the {@link Symbol}.
//     * 
//     * @return the name of the {@code Symbol}
//     */
//    @Override //Object
//    public String toString() {
//        return name();
//    }/* END toString() */
//    
//}/* END ENUM Symbol */
