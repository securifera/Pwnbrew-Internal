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
class TranscriptScreen implements Screen {
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

    @Override
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
    @Override
    public void set(int x, int y, int codePoint, int style) {
        mData.setChar(x, y, codePoint, style);
    }

    @Override
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
    @Override
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
    @Override
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
    @Override
    public void blockSet(int sx, int sy, int w, int h, int val,
                         int style) {
        mData.blockSet(sx, sy, w, h, val, style);
    }

    /**
     * Get the count of active rows.
     *
     * @return the count of active rows.
     */
    @Override
    public int getActiveRows() {
        return mData.getActiveRows();
    }

    /**
     * Get the count of active transcript rows.
     *
     * @return the count of active transcript rows.
     */
    public int getActiveTranscriptRows() {
        return mData.getActiveTranscriptRows();
    }

    @Override
    public String getTranscriptText() {
        return internalGetTranscriptText(null, 0, -mData.getActiveTranscriptRows(), mColumns, mScreenRows);
    }

    @Override
    public String getTranscriptText(GrowableIntArray colors) {
        return internalGetTranscriptText(colors, 0, -mData.getActiveTranscriptRows(), mColumns, mScreenRows);
    }

    @Override
    public String getSelectedText(int selX1, int selY1, int selX2, int selY2) {
        return internalGetTranscriptText(null, selX1, selY1, selX2, selY2);
    }

    @Override
    public String getSelectedText(GrowableIntArray colors, int selX1, int selY1, int selX2, int selY2) {
        return internalGetTranscriptText(colors, selX1, selY1, selX2, selY2);
    }

//    private String internalGetTranscriptText(GrowableIntArray colors, int selX1, int selY1, int selX2, int selY2) {
//        StringBuilder builder = new StringBuilder();
//        UnicodeTranscript data = mData;
//        int columns = mColumns;
//        char[] line;
//        StyleRow rowColorBuffer = null;
//        if (selY1 < -data.getActiveTranscriptRows()) {
//            selY1 = -data.getActiveTranscriptRows();
//        }
//        if (selY2 >= mScreenRows) {
//            selY2 = mScreenRows - 1;
//        }
//        for (int row = selY1; row <= selY2; row++) {
//            int x1 = 0;
//            int x2;
//            if ( row == selY1 ) {
//                x1 = selX1;
//            }
//            if ( row == selY2 ) {
//                x2 = selX2 + 1;
//                if (x2 > columns) {
//                    x2 = columns;
//                }
//            } else {
//                x2 = columns;
//            }
//            line = data.getLine(row, x1, x2);
//            if (colors != null) {
//                rowColorBuffer = data.getLineColor(row, x1, x2);
//            }
//            if (line == null) {
//                if (!data.getLineWrap(row) && row < selY2 && row < mScreenRows - 1) {
//                    //builder.append('\n');
//                    if (colors != null) {
//                        colors.append(0);
//                    }
//                }
//                continue;
//            }
//            int defaultColor = mData.getDefaultStyle();
//            int lastPrintingChar = -1;
//            int lineLen = line.length;
//            int i;
//            int column = 0;
//            for (i = 0; i < lineLen; ++i) {
//                char c = line[i];
//                if (c == 0) {
//                    break;
//                }
//
//                int style = defaultColor;
//                try {
//                    if (rowColorBuffer != null) {
//                        style = rowColorBuffer.get(column);
//                    }
//                } catch (ArrayIndexOutOfBoundsException e) {
//                    // XXX This probably shouldn't happen ...
//                    style = defaultColor;
//                }
//
//                if (c != ' ' || style != defaultColor) {
//                    lastPrintingChar = i;
//                }
//                if (!Character.isLowSurrogate(c)) {
//                    column += UnicodeTranscript.charWidth(line, i);
//                }
//            }
//            if (data.getLineWrap(row) && lastPrintingChar > -1 && x2 == columns) {
//                // If the line was wrapped, we shouldn't lose trailing space
//                lastPrintingChar = i - 1;
//            }
//            builder.append(line, 0, lastPrintingChar + 1);
//            if (colors != null) {
//                if (rowColorBuffer != null) {
//                    column = 0;
//                    for (int j = 0; j <= lastPrintingChar; ++j) {
//                        colors.append(rowColorBuffer.get(column));
//                        column += UnicodeTranscript.charWidth(line, j);
//                        if (Character.isHighSurrogate(line[j])) {
//                            ++j;
//                        }
//                    }
//                } else {
//                    for (int j = 0; j <= lastPrintingChar; ++j) {
//                        colors.append(defaultColor);
//                        char c = line[j];
//                        if (Character.isHighSurrogate(c)) {
//                            ++j;
//                        }
//                    }
//                }
//            }
//            if (!data.getLineWrap(row) && row < selY2 && row < mScreenRows - 1) {
//                builder.append('\n');
//                //builder.append(' ');
//                if (colors != null) {
//                    colors.append((char) 0);
//                }
//            }
//        }
//        return builder.toString();
//    }
    
    private String internalGetTranscriptText(GrowableIntArray colors, int selX1, int selY1, int selX2, int selY2) {
        StringBuilder builder = new StringBuilder();
        UnicodeTranscript data = mData;
        int columns = mColumns;
        char[] line;
        StyleRow rowColorBuffer = null;
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
            if (colors != null) {
                rowColorBuffer = data.getLineColor(row, x1, x2);
            }
            if (line == null) {  
                
                int intRow = mData.externalToInternalRow(row);
                mData.allocateBasicLine(intRow, mColumns);
                line = data.getLine(row, x1, x2);
                if (line == null) {               
                    if (!data.getLineWrap(row) && row < selY2 && row < mScreenRows - 1) {
                        if (colors != null) {
                            colors.append(0);
                        }
                    }
                    builder.append( new char[80]);
                    continue;
                }
            }
            int defaultColor = mData.getDefaultStyle();
            int lastPrintingChar = -1;
            int lineLen = line.length;
            int i;
            int column = 0;
            for (i = 0; i < lineLen; ++i) {
                char c = line[i];
                if (c == 0) {
                    break;
                }
                
//                if( c== '\n' || c == '\r'){
//                    System.out.println("***********************Somehow line contains newline.*****************************");
//                }

                int style = defaultColor;
                try {
                    if (rowColorBuffer != null) {
                        style = rowColorBuffer.get(column);
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    // XXX This probably shouldn't happen ...
                    style = defaultColor;
                }

                if (c != ' ' || style != defaultColor) {
                    lastPrintingChar = i;
                }
                if (!Character.isLowSurrogate(c)) {
                    column += UnicodeTranscript.charWidth(line, i);
                }
            }
            if (data.getLineWrap(row) && lastPrintingChar > -1 && x2 == columns) {
                // If the line was wrapped, we shouldn't lose trailing space
                lastPrintingChar = i - 1;
            }
            //builder.append(line, 0, lastPrintingChar + 1);
            builder.append(line);
            if (colors != null) {
                if (rowColorBuffer != null) {
                    column = 0;
                    for (int j = 0; j <= lastPrintingChar; ++j) {
                        colors.append(rowColorBuffer.get(column));
                        column += UnicodeTranscript.charWidth(line, j);
                        if (Character.isHighSurrogate(line[j])) {
                            ++j;
                        }
                    }
                } else {
                    for (int j = 0; j <= lastPrintingChar; ++j) {
                        colors.append(defaultColor);
                        char c = line[j];
                        if (Character.isHighSurrogate(c)) {
                            ++j;
                        }
                    }
                }
            }
            //if (!data.getLineWrap(row) && row < selY2 && row < mScreenRows - 1) {
                builder.append('\n');
                //builder.append(' ');
                if (colors != null) {
                    colors.append((char) 0);
                }
            //}
        }
        return builder.toString();
    }

    @Override
    public boolean fastResize(int columns, int rows, int[] cursor) {
        if (mData == null) {
            // XXX Trying to resize a finished TranscriptScreen?
            return true;
        }
        if (mData.resize(columns, rows, cursor)) {
            mColumns = columns;
            mScreenRows = rows;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void resize(int columns, int rows, int style) {
        // Ensure backing store will be large enough to hold the whole screen
        if (rows > mTotalRows) {
            mTotalRows = rows;
        }
        init(columns, mTotalRows, rows, style);
    }

    /**
     *
     * Return the UnicodeTranscript line at this row index.
     * @param row The row index to be queried
     * @return The line of text at this row index
     */
    char[] getScriptLine(int row)
    {
        try
        {
            return mData.getLine(row);
        }
        catch (IllegalArgumentException | NullPointerException e)
        {
            return null;
        }
    }

    /**
     * Get the line wrap status of the row provided.
     * @param row The row to check for line-wrap status
     * @return The line wrap status of the row provided
     */
    boolean getScriptLineWrap(int row)
    {
        return mData.getLineWrap(row);
    }

    /**
     * Get whether the line at this index is "basic" (contains only BMP
     * characters of width 1).
     */
    boolean isBasicLine(int row) {
        return mData == null || mData.isBasicLine(row);
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