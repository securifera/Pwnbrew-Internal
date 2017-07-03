/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pwnbrew.shell.term;


/**
 * A TranscriptScreen is a screen that remembers data that's been scrolled. The
 * old data is stored in a ring buffer to minimize the amount of copying that
 * needs to be done. The transcript does its own drawing, to avoid having to
 * expose its internal data structures.
 */
class TranscriptScreen {
    /**
     * The width of the transcript, in characters. Fixed at initialization.
     */
    private int mColumns;

    /**
     * The total number of rows in the transcript and the screen. Fixed at
     * initialization.
     */
    private int mTotalRows;

    /**
     * The number of rows in the screen.
     */
    private int mScreenRows;

    private UnicodeTranscript mData;

    /**
     * Create a transcript screen.
     *
     * @param columns the width of the screen in characters.
     * @param totalRows the height of the entire text area, in rows of text.
     * @param screenRows the height of just the screen, not including the
     *        transcript that holds lines that have scrolled off the top of the
     *        screen.
     */
    public TranscriptScreen(int columns, int totalRows, int screenRows ) {
        init(columns, totalRows, screenRows, TextStyle.kNormalTextStyle);
    }

    private void init(int columns, int totalRows, int screenRows, int style) {
        mColumns = columns;
        mTotalRows = totalRows;
        mScreenRows = screenRows;

        mData = new UnicodeTranscript(columns, totalRows, screenRows, style);
        mData.blockSet(0, 0, mColumns, mScreenRows, ' ', style);
    }

    public void setLineWrap(int row) {
        mData.setLineWrap(row);
    }

    /**
     * Store a Unicode code point into the screen at location (x, y)
     *
     * @param x X coordinate (also known as column)
     * @param y Y coordinate (also known as row)
     * @param codePoint Unicode codepoint to store
     * @param foreColor the foreground color
     * @param backColor the background color
     */
    public void set(int x, int y, int codePoint, int style) {
        mData.setChar(x, y, codePoint, style);
    }

    public void set(int x, int y, byte b, int style) {
        mData.setChar(x, y, b, style);
    }

    /**
     * Scroll the screen down one line. To scroll the whole screen of a 24 line
     * screen, the arguments would be (0, 24).
     *
     * @param topMargin First line that is scrolled.
     * @param bottomMargin One line after the last line that is scrolled.
     * @param style the style for the newly exposed line.
     */
    public void scroll(int topMargin, int bottomMargin, int style) {
        mData.scroll(topMargin, bottomMargin, style);
    }

    /**
     * Block copy characters from one position in the screen to another. The two
     * positions can overlap. All characters of the source and destination must
     * be within the bounds of the screen, or else an InvalidParemeterException
     * will be thrown.
     *
     * @param sx source X coordinate
     * @param sy source Y coordinate
     * @param w width
     * @param h height
     * @param dx destination X coordinate
     * @param dy destination Y coordinate
     */
    public void blockCopy(int sx, int sy, int w, int h, int dx, int dy) {
        mData.blockCopy(sx, sy, w, h, dx, dy);
    }

    /**
     * Block set characters. All characters must be within the bounds of the
     * screen, or else and InvalidParemeterException will be thrown. Typically
     * this is called with a "val" argument of 32 to clear a block of
     * characters.
     *
     * @param sx source X
     * @param sy source Y
     * @param w width
     * @param h height
     * @param val value to set.
     */
    public void blockSet(int sx, int sy, int w, int h, int val,
                         int style) {
        mData.blockSet(sx, sy, w, h, val, style);
    }

    //=========================================================================
    /**
     * 
     * @return 
     */
    public String getTranscriptText() {
        return internalGetTranscriptText( 0, -mData.getActiveTranscriptRows(), mColumns, mScreenRows);
    }

    //=========================================================================
    /**
     * 
     * @param selX1
     * @param selY1
     * @param selX2
     * @param selY2
     * @return 
     */
    private String internalGetTranscriptText( int selX1, int selY1, int selX2, int selY2) {
        StringBuilder builder = new StringBuilder();
        UnicodeTranscript data = mData;
        int columns = mColumns;
        char[] line;
        
        if (selY1 < -data.getActiveTranscriptRows()) {
            selY1 = -data.getActiveTranscriptRows();
        }
        if (selY2 >= mScreenRows) {
            selY2 = mScreenRows - 1;
        }
        for (int row = selY1; row <= selY2; row++) {
            int x1 = 0;
            int x2;
            if ( row == selY1 ) {
                x1 = selX1;
            }
            if ( row == selY2 ) {
                x2 = selX2 + 1;
                if (x2 > columns) {
                    x2 = columns;
                }
            } else {
                x2 = columns;
            }
            line = data.getLine(row, x1, x2);
            if (line == null) {  
                
                int intRow = mData.externalToInternalRow(row);
                mData.allocateBasicLine(intRow, mColumns);
                line = data.getLine(row, x1, x2);
                if (line == null) {               
                    builder.append( new char[80]);
                    continue;
                }
            }
            builder.append(line);
            builder.append('\n');
        }
        return builder.toString();
    }
    
    //==================================================================
    /**
     * Wrapper
     * @param extRow
     * @return 
     */
    public int externalToInternalRow(int extRow) {
        int row = 0;
        if( mData != null )
            row = mData.externalToInternalRow(extRow);
        
        return row;
    }
}