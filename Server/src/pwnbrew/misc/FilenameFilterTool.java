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
 * FileFilterTool.java
 *
 * Created on June 22, 2013, 3:21 PM
 *
 *
 */

package pwnbrew.misc;

import java.io.File;
import java.io.FilenameFilter;
import javax.swing.filechooser.FileFilter;


// ===============================================
/**
 *
 *  
 */
public class FilenameFilterTool extends FileFilter implements FilenameFilter {
  
    private final String FilenameSuffixStr;


    // ================================================================
    /**
    * Creates a new instance of FileFilterTool
    *
     * @param passedFilenameSuffixStr
    */
    public FilenameFilterTool( String passedFilenameSuffixStr ) {
        FilenameSuffixStr= passedFilenameSuffixStr;
    }


    // ===============================================
    /**
    * {@inheritDoc }
     * @param thePathAndBasicFilenameStr
     * @return 
    */
    @Override
    public boolean accept( File thePathAndBasicFilenameStr ) {
        boolean isOkFlag= false;

        if ( thePathAndBasicFilenameStr != null ) {
            if ( thePathAndBasicFilenameStr.isDirectory() ) {
                return true;
            }

            String theBasicFilenameStr= thePathAndBasicFilenameStr.getName();
            isOkFlag= endsWithCaseInsensitive( theBasicFilenameStr, FilenameSuffixStr );
        }

        return isOkFlag;
    }


    // ===============================================
    /**
    * {@inheritDoc }
     * @param theDirFile
     * @param theBasicFilenameStr
     * @return 
    */
    @Override
    public boolean accept( File theDirFile, String theBasicFilenameStr ) {
        boolean isOkFlag= false;

        if ( theBasicFilenameStr != null ) {
            isOkFlag= endsWithCaseInsensitive( theBasicFilenameStr, FilenameSuffixStr );
        }

        return isOkFlag;
    }


    // ===============================================
    /**
    * Performs a case insensitive comparison to determine if {@code theLongerStr}
    * ends with {@code theShorterStr}.  This is a case insensitive version of
    * {@link String#endsWith(java.lang.String)}.
    *
     * @param theLongerStr
     * @param theShorterStr
     * @return 
    * @see String#endsWith(java.lang.String)
    */
    public boolean endsWithCaseInsensitive( String theLongerStr, String theShorterStr ) {

        boolean rtnFlag= false;

        if ( theLongerStr == null || theLongerStr.isEmpty() ) {
            return false;
        }

        if ( theShorterStr == null ) {
            return false;
        }

        int shorterStrLen= theShorterStr.length();
        int longerStrLen=  theLongerStr.length();

        if ( longerStrLen < shorterStrLen ) {
            return false;
        }

        String endOfLongerStr= theLongerStr.substring( longerStrLen - shorterStrLen );

        if ( endOfLongerStr.equalsIgnoreCase( theShorterStr ) == true ) {
            rtnFlag= true;
        }

        endOfLongerStr= null;
        //
        return rtnFlag;
    }


    // ===============================================
    /**
    * {@inheritDoc }
     * @return 
    */
    @Override
    public String getDescription() {
        return " (*" + FilenameSuffixStr + ")";
    }


    @Override
    public String toString() {
        return "Filename Filter: " + FilenameSuffixStr;
    }

}
