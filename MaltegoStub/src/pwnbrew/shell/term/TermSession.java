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
import java.util.concurrent.locks.ReentrantLock;
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
//    private boolean mIsRunning = false;
    
    //Necessary so model doesn't get updated in the middle of a paint event
//    private static final ReentrantLock theTermLock = new ReentrantLock();    

    public TermSession( Shell passedShell, final boolean exitOnEOF) {
        theShell = passedShell;
        mWriteCharBuffer = CharBuffer.allocate(2);
        mWriteByteBuffer = ByteBuffer.allocate(4);
        mUTF8Encoder = Charset.forName("UTF-8").newEncoder();
        mUTF8Encoder.onMalformedInput(CodingErrorAction.REPLACE);
        mUTF8Encoder.onUnmappableCharacter(CodingErrorAction.REPLACE);

        this.exitOnEOF = exitOnEOF;
    }
    
    //========================================================================
    /**
     * 
     * @return 
     */
    public int getMaxRows(){
        return TRANSCRIPT_ROWS;
    }
    
//    //========================================================================
//    /**
//     * This is a blocking event
//     */
//    public void getLock(){
//        theTermLock.lock();
//    }
//    
//    //========================================================================
//    /**
//     * This is a blocking event
//     */
//    public void releaseLock(){
//        theTermLock.unlock();
//    }
    
    //=========================================================================
    /**
     * 
     * @param l 
     */
    public void setKeyListener(TermKeyListener l) {
        mKeyListener = l;
    }
    
    //=========================================================================
    /**
     * 
     * @param passedId
     * @param buffer 
     */
    public void handleIncoming( int passedId, byte[] buffer ){
        //getLock();
        try {
            processInput(buffer, 0, buffer.length);
        } catch(IllegalArgumentException ex){
            System.out.println(ex.toString());
//        } finally {
//            releaseLock();
        }
    }

    /**
     * Set the terminal emulator's window size and start terminal emulation.
     *
     * @param columns The number of columns in the terminal window.
     * @param rows The number of rows in the terminal window.
     */
    public void initializeEmulator(int columns, int rows) {
        
        mEmulator = new TerminalEmulator(this, columns, rows );
        mEmulator.setDefaultUTF8Mode(mDefaultUTF8Mode);
        mEmulator.setKeyListener(mKeyListener);

//        mIsRunning = true;
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

    //========================================================================
    /**
     * 
     * @return 
     */
    TerminalEmulator getEmulator() {
        return mEmulator;
    }

    //========================================================================
    /**
     * Get the terminal session's title (may be null).
     * @return 
     */
    public String getTitle() {
        return mTitle;
    }

    //========================================================================
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
  /**
//     * Finish this terminal session.  Frees resources used by the terminal
//     * emulator and closes the attached <code>InputStream</code> and
//     * <code>OutputStream</code>.
//     */
//    public void finish() {
//        mIsRunning = false;                        
//    }
//  
}
