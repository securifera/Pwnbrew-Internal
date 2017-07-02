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

import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.util.Arrays;
import pwnbrew.shell.Shell;


/**
 * A terminal session, consisting of a VT100 terminal emulator and its
 * input and output streams.
 * <p>
 * You need to supply an {@link InputStream} and {@link OutputStream} to
 * provide input and output to the terminal.  For a locally running
 * program, these would typically point to a tty; for a telnet program
 * they might point to a network socket.  Reader and writer threads will be
 * spawned to do I/O to these streams.  All other operations, including
 * processing of input and output in {@link #processInput processInput} and
 * {@link #write(byte[], int, int) write}, will be performed on the main thread.
 * <p>
 * Call {@link #setTermIn} and {@link #setTermOut} to connect the input and
 * output streams to the emulator.  When all of your initialization is
 * complete, your initial screen size is known, and you're ready to
 * start VT100 emulation, call {@link #initializeEmulator} or {@link
 * #updateSize} with the number of rows and columns the terminal should
 * initially have.  (If you attach the session to an {@link EmulatorView},
 * the view will take care of setting the screen size and initializing the
 * emulator for you.)
 * <p>
 * When you're done with the session, you should call {@link #finish} on it.
 * This frees emulator data from memory, stops the reader and writer threads,
 * and closes the attached I/O streams.
 */
public class TermSession {
    
    private TermKeyListener mKeyListener;

//    private ColorScheme mColorScheme = BaseTextRenderer.defaultColorScheme;
//    private UpdateCallback mNotify;
    private String mTitle;
    private TerminalEmulator mEmulator;

    private boolean mDefaultUTF8Mode;
    private boolean exitOnEOF;
    
    private final CharBuffer mWriteCharBuffer;
    private final ByteBuffer mWriteByteBuffer;
    private final CharsetEncoder mUTF8Encoder;

    // Number of rows in the transcript
    private static final int TRANSCRIPT_ROWS = 10000;
    
    private final Shell theShell;    
    private boolean mIsRunning = false;
    

    public TermSession( Shell passedShell, final boolean exitOnEOF) {
        theShell = passedShell;
        mWriteCharBuffer = CharBuffer.allocate(2);
        mWriteByteBuffer = ByteBuffer.allocate(4);
        mUTF8Encoder = Charset.forName("UTF-8").newEncoder();
        mUTF8Encoder.onMalformedInput(CodingErrorAction.REPLACE);
        mUTF8Encoder.onUnmappableCharacter(CodingErrorAction.REPLACE);

        this.exitOnEOF = exitOnEOF;
    }
    
    public void setKeyListener(TermKeyListener l) {
        mKeyListener = l;
    }
    
    public void handleIncoming( int passedId, byte[] buffer ){
        //mReaderThread.addToQueue(buffer);
        processInput(buffer, 0, buffer.length);
//        notifyUpdate();
    }

    /**
     * Set the terminal emulator's window size and start terminal emulation.
     *
     * @param columns The number of columns in the terminal window.
     * @param rows The number of rows in the terminal window.
     */
    public void initializeEmulator(int columns, int rows) {
//        mTranscriptScreen = new TranscriptScreen(columns, TRANSCRIPT_ROWS, rows, mColorScheme);
        mEmulator = new TerminalEmulator(this, columns, rows );
        mEmulator.setDefaultUTF8Mode(mDefaultUTF8Mode);
        mEmulator.setKeyListener(mKeyListener);

        mIsRunning = true;
//        mReaderThread.start();
//        mWriterThread.start();
    }

    /**
     * Write data to the terminal output.  The written data will be consumed by
     * the emulation client as input.
     * <p>
     * <code>write</code> itself runs on the main thread.  The default
     * implementation writes the data into a circular buffer and signals the
     * writer thread to copy it from there to the {@link OutputStream}.
     * <p>
     * Subclasses may override this method to modify the output before writing
     * it to the stream, but implementations in derived classes should call
     * through to this method to do the actual writing.
     *
     * @param data An array of bytes to write to the terminal.
     * @param offset The offset into the array at which the data starts.
     * @param count The number of bytes to be written.
     */
    public void write(byte[] data, int offset, int count) {
        
        byte[] byteArr = Arrays.copyOfRange(data, offset, count);
        //mWriterThread.addToQueue(byteArr);
        theShell.sendInput( new String(byteArr));    
    }

    /**
     * Write the UTF-8 representation of a String to the terminal output.  The
     * written data will be consumed by the emulation client as input.
     * <p>
     * This implementation encodes the String and then calls
     * {@link #write(byte[], int, int)} to do the actual writing.  It should
     * therefore usually be unnecessary to override this method; override
     * {@link #write(byte[], int, int)} instead.
     *
     * @param data The String to write to the terminal.
     */
    public void write(String data) {
        try {
            byte[] bytes = data.getBytes("UTF-8");
            write(bytes, 0, bytes.length);
        } catch (UnsupportedEncodingException ignored) {
        }
    }

    /**
     * Write the UTF-8 representation of a single Unicode code point to the
     * terminal output.  The written data will be consumed by the emulation
     * client as input.
     * <p>
     * This implementation encodes the code point and then calls
     * {@link #write(byte[], int, int)} to do the actual writing.  It should
     * therefore usually be unnecessary to override this method; override
     * {@link #write(byte[], int, int)} instead.
     *
     * @param codePoint The Unicode code point to write to the terminal.
     */
    public void write(int codePoint) {
        ByteBuffer byteBuf = mWriteByteBuffer;
        if (codePoint < 128) {
            // Fast path for ASCII characters
            byte[] buf = byteBuf.array();
            buf[0] = (byte) codePoint;
            write(buf, 0, 1);
            return;
        }

        CharBuffer charBuf = mWriteCharBuffer;
        CharsetEncoder encoder = mUTF8Encoder;

        charBuf.clear();
        byteBuf.clear();
        Character.toChars(codePoint, charBuf.array(), 0);
        encoder.reset();
        encoder.encode(charBuf, byteBuf, true);
        encoder.flush(byteBuf);
        write(byteBuf.array(), 0, byteBuf.position()-1);
    }

    /**
     * @return Whether the terminal emulation is currently running.
     */
    public boolean isRunning() {
        return mIsRunning;
    }

    TerminalEmulator getEmulator() {
        return mEmulator;
    }

//    /**
//     * Set an {@link UpdateCallback} to be invoked when the terminal emulator's
//     * screen is changed.
//     *
//     * @param notify The {@link UpdateCallback} to be invoked on changes.
//     */
//    public void setUpdateCallback(UpdateCallback notify) {
//        mNotify = notify;
//    }
//
//    /**
//     * Notify the {@link UpdateCallback} registered by {@link
//     * #setUpdateCallback setUpdateCallback} that the screen has changed.
//     */
//    protected void notifyUpdate() {
//        if (mNotify != null) {
//            mNotify.onUpdate();
//        }
//    }

    /**
     * Get the terminal session's title (may be null).
     * @return 
     */
    public String getTitle() {
        return mTitle;
    }

    /**
     * Change the terminal session's title.
     * @param title
     */
    public void setTitle(String title) {
        mTitle = title;
        theShell.getListener().setFrameTitle(title);
    }

    /**
     * Change the terminal's window size.  Will call {@link #initializeEmulator}
     * if the emulator is not yet running.
     * <p>
     * You should override this method if your application needs to be notified
     * when the screen size changes (for example, if you need to issue
     * <code>TIOCSWINSZ</code> to a tty to adjust the window size).  <em>If you
     * do override this method, you must call through to the superclass
     * implementation.</em>
     *
     * @param columns The number of columns in the terminal window.
     * @param rows The number of rows in the terminal window.
     */
    public void updateSize(int columns, int rows) {
        if (mEmulator == null) {
            initializeEmulator(columns, rows);
//        } else {
//            mEmulator.updateSize(columns, rows);
        }
    }

    /**
     * Retrieve the terminal's screen and scrollback buffer.
     *
     * @return A {@link String} containing the contents of the screen and
     *         scrollback buffer.
     */
    public String getTranscriptText() {
        String aStr = "";
        if(mEmulator != null )
            aStr = mEmulator.getScreen().getTranscriptText();
        
        return aStr;
    }

    /**
     * Process input and send it to the terminal emulator.  This method is
     * invoked on the main thread whenever new data is read from the
     * InputStream.
     * <p>
     * The default implementation sends the data straight to the terminal
     * emulator without modifying it in any way.  Subclasses can override it to
     * modify the data before giving it to the terminal.
     *
     * @param data A byte array containing the data read.
     * @param offset The offset into the buffer where the read data begins.
     * @param count The number of bytes read.
     */
    protected void processInput(byte[] data, int offset, int count) {
        mEmulator.append(data, offset, count);
    }
  
    /**
     * Write something directly to the terminal emulator input, bypassing the
     * emulation client, the session's {@link InputStream}, and any processing
     * being done by {@link #processInput processInput}.
     *
     * @param data The data to be written to the terminal.
     * @param offset The starting offset into the buffer of the data.
     * @param count The length of the data to be written.
     */
    protected final void appendToEmulator(byte[] data, int offset, int count) {
        mEmulator.append(data, offset, count);
    }

//    /**
//     * Set the terminal emulator's color scheme (default colors).
//     *
//     * @param scheme The {@link ColorScheme} to be used (use null for the
//     *               default scheme).
//     */
//    public void setColorScheme(ColorScheme scheme) {
//        if (scheme == null) {
//            scheme = BaseTextRenderer.defaultColorScheme;
//        }
//        mColorScheme = scheme;
//        if (mEmulator == null) {
//            return;
//        }
//        mEmulator.setColorScheme(scheme);
//    }

    /**
     * Set whether the terminal emulator should be in UTF-8 mode by default.
     * <p>
     * In UTF-8 mode, the terminal will handle UTF-8 sequences, allowing the
     * display of text in most of the world's languages, but applications must
     * encode C1 control characters and graphics drawing characters as the
     * corresponding UTF-8 sequences.
     *
     * @param utf8ByDefault Whether the terminal emulator should be in UTF-8
     *                      mode by default.
     */
    public void setDefaultUTF8Mode(boolean utf8ByDefault) {
        mDefaultUTF8Mode = utf8ByDefault;
        if (mEmulator == null) {
            return;
        }
        mEmulator.setDefaultUTF8Mode(utf8ByDefault);
    }

    /**
     * Get whether the terminal emulator is currently in UTF-8 mode.
     *
     * @return Whether the emulator is currently in UTF-8 mode.
     */
    public boolean getUTF8Mode() {
        if (mEmulator == null) {
            return mDefaultUTF8Mode;
        } else {
            return mEmulator.getUTF8Mode();
        }
    }

//    /**
//     * Set an {@link UpdateCallback} to be invoked when the terminal emulator
//     * goes into or out of UTF-8 mode.
//     *
//     * @param utf8ModeNotify The {@link UpdateCallback} to be invoked.
//     */
//    public void setUTF8ModeUpdateCallback(UpdateCallback utf8ModeNotify) {
//        if (mEmulator != null) {
//            mEmulator.setUTF8ModeUpdateCallback(utf8ModeNotify);
//        }
//    }

//    /**
//     * Reset the terminal emulator's state.
//     */
//    public void reset() {
//        mEmulator.reset();
//        notifyUpdate();
//    }

    /**
     * Finish this terminal session.  Frees resources used by the terminal
     * emulator and closes the attached <code>InputStream</code> and
     * <code>OutputStream</code>.
     */
    public void finish() {
        mIsRunning = false;                        
    }
}
