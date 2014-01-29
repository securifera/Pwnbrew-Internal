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
 * LogFilter.java
 *
 * Created on Dec 22, 2013, 9:21:12 PM
 */

package pwnbrew.logging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;


/**
 * 
 */
@SuppressWarnings("ucd")
final public class LogFilter implements Filter {

    private final HashMap<Level, String> theLevelMap = new HashMap<>();
    private final static String DUMMY_String = "";


    // ==========================================================================
    /**
     * Constructor
     */
    public LogFilter() {
    }

    
    // ==========================================================================
    /**
     * Determines if messages at the given {@link Level} are being logged.
     * <p>
     * If the argument is null this method will return {@code false}.
     *
     * @param level the {@code Level} in question
     *
     * @return <tt>true</tt> if the {@link LogFilter} will allow messages at the
     * given {@code Level} to be logged, <tt>false</tt> otherwise
     */
    synchronized boolean isAllowed( Level level ) {
        return ( theLevelMap.get( level ) != null );
    }


    // ==========================================================================
    /**
     * Allows messages at the given {@link Level} to be logged.
     * <p>
     * If the argument is null this method does nothing.
     * 
     * @param level the {@code Level} to allow
     */
    public synchronized void allow( Level level ) {
        
        if( level == null ) return;
        
        theLevelMap.put( level, DUMMY_String );
        
    }


    // ==========================================================================
    /**
     * Prevents messages at the given {@link Level} from being logged.
     * <p>
     * If the argument is null this method does nothing.
     *
     * @param level the {@code Level} to disallow
     */
    synchronized void disallow( Level level ) {
        theLevelMap.remove( level );
    }/* END disallow( Level ) */


    // ==========================================================================
    /**
     * Clears all of the allowed {@link Level}s.
     * <p>
     * <strong>CAUTION:</strong> Calling this method effectively turns off all logging.
     */
    synchronized void disallowAll() {
        theLevelMap.clear(); //Remove all Levels from the map
    }/* END disallowAll() */
    
    
    // ==========================================================================
    /**
     * Returns a list of the allowed logging {@link Level}s.
     * <p>
     * NOTE: The returned list is the set of allowed {@code Level}s at the time
     * this method was invoked and may not accurately represent the allowed {@code Level}s
     * at an arbitrary time afterward.
     * 
     * @return a list of the allowed logging {@code Level}s
     */
    public synchronized List<Level> getAllowedLevels() {
      
        List<Level> rtnList = new ArrayList<>();
        
        rtnList.addAll( theLevelMap.keySet() );
        
        return rtnList;
        
    }/* END getAllowedLevels() */
    

    // ==========================================================================
    /**
     * Determines if the given {@link LogRecord} should be logged.
     * <p>
     * @param record the {@code LogRecord} to test
     *
     * @return <tt>true</tt> if the given {@code LogRecord} should be logged, <tt>false</tt>
     * otherwise
     *
    */
    @Override
    public synchronized boolean isLoggable( LogRecord record ) {
      
        if( record == null ) return false;
        
        return isAllowed( record.getLevel() );
        
    }

}/* END CLASS LogFilter */
