package pwnbrew.shell.term;


import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * An ASCII key listener. Supports control characters and escape. Keeps track of
 * the current state of the alt, shift, fn, and control keys.
 *
 */
class TermKeyListener {
    
    private final static String TAG = "TermKeyListener";
    
    //private static final boolean LOG_KEYS = false;
    private static final boolean LOG_COMBINING_ACCENT = false;

    /** Disabled for now because it interferes with ALT processing on phones with physical keyboards. */
    private final static boolean SUPPORT_8_BIT_META = false;

    private static final int KEYMOD_ALT   = 0x80000000;
    private static final int KEYMOD_CTRL  = 0x40000000;
    private static final int KEYMOD_SHIFT = 0x20000000;
    /** Means this maps raw scancode */
    //private static final int KEYMOD_SCAN  = 0x10000000;

    private static Map<Integer, String> mKeyMap;

    private String[] mKeyCodes = new String[1024];
    private String[] mAppKeyCodes = new String[1024];

    private void initKeyCodes() {
        mKeyMap = new HashMap<Integer, String>();
        mKeyMap.put(KEYMOD_SHIFT | KeyEvent.VK_LEFT, "\033[1;2D");
        mKeyMap.put(KEYMOD_ALT | KeyEvent.VK_LEFT, "\033[1;3D");
        mKeyMap.put(KEYMOD_ALT | KEYMOD_SHIFT | KeyEvent.VK_LEFT, "\033[1;4D");
        mKeyMap.put(KEYMOD_CTRL | KeyEvent.VK_LEFT, "\033[1;5D");
        mKeyMap.put(KEYMOD_CTRL | KEYMOD_SHIFT | KeyEvent.VK_LEFT, "\033[1;6D");
        mKeyMap.put(KEYMOD_CTRL | KEYMOD_ALT | KeyEvent.VK_LEFT, "\033[1;7D");
        mKeyMap.put(KEYMOD_CTRL | KEYMOD_ALT | KEYMOD_SHIFT | KeyEvent.VK_LEFT, "\033[1;8D");

        mKeyMap.put(KEYMOD_SHIFT | KeyEvent.VK_RIGHT, "\033[1;2C");
        mKeyMap.put(KEYMOD_ALT | KeyEvent.VK_RIGHT, "\033[1;3C");
        mKeyMap.put(KEYMOD_ALT | KEYMOD_SHIFT | KeyEvent.VK_RIGHT, "\033[1;4C");
        mKeyMap.put(KEYMOD_CTRL | KeyEvent.VK_RIGHT, "\033[1;5C");
        mKeyMap.put(KEYMOD_CTRL | KEYMOD_SHIFT | KeyEvent.VK_RIGHT, "\033[1;6C");
        mKeyMap.put(KEYMOD_CTRL | KEYMOD_ALT | KeyEvent.VK_RIGHT, "\033[1;7C");
        mKeyMap.put(KEYMOD_CTRL | KEYMOD_ALT | KEYMOD_SHIFT | KeyEvent.VK_RIGHT, "\033[1;8C");

        mKeyMap.put(KEYMOD_SHIFT | KeyEvent.VK_UP, "\033[1;2A");
        mKeyMap.put(KEYMOD_ALT | KeyEvent.VK_UP, "\033[1;3A");
        mKeyMap.put(KEYMOD_ALT | KEYMOD_SHIFT | KeyEvent.VK_UP, "\033[1;4A");
        mKeyMap.put(KEYMOD_CTRL | KeyEvent.VK_UP, "\033[1;5A");
        mKeyMap.put(KEYMOD_CTRL | KEYMOD_SHIFT | KeyEvent.VK_UP, "\033[1;6A");
        mKeyMap.put(KEYMOD_CTRL | KEYMOD_ALT | KeyEvent.VK_UP, "\033[1;7A");
        mKeyMap.put(KEYMOD_CTRL | KEYMOD_ALT | KEYMOD_SHIFT | KeyEvent.VK_UP, "\033[1;8A");

        mKeyMap.put(KEYMOD_SHIFT | KeyEvent.VK_DOWN, "\033[1;2B");
        mKeyMap.put(KEYMOD_ALT | KeyEvent.VK_DOWN, "\033[1;3B");
        mKeyMap.put(KEYMOD_ALT | KEYMOD_SHIFT | KeyEvent.VK_DOWN, "\033[1;4B");
        mKeyMap.put(KEYMOD_CTRL | KeyEvent.VK_DOWN, "\033[1;5B");
        mKeyMap.put(KEYMOD_CTRL | KEYMOD_SHIFT | KeyEvent.VK_DOWN, "\033[1;6B");
        mKeyMap.put(KEYMOD_CTRL | KEYMOD_ALT | KeyEvent.VK_DOWN, "\033[1;7B");
        mKeyMap.put(KEYMOD_CTRL | KEYMOD_ALT | KEYMOD_SHIFT | KeyEvent.VK_DOWN, "\033[1;8B");

        //^[[3~
        mKeyMap.put(KEYMOD_SHIFT | KeyEvent.VK_DELETE, "\033[3;2~");
        mKeyMap.put(KEYMOD_ALT | KeyEvent.VK_DELETE, "\033[3;3~");
        mKeyMap.put(KEYMOD_CTRL | KeyEvent.VK_DELETE, "\033[3;5~");

        //^[[2~
        mKeyMap.put(KEYMOD_SHIFT | KeyEvent.VK_INSERT, "\033[2;2~");
        mKeyMap.put(KEYMOD_ALT | KeyEvent.VK_INSERT, "\033[2;3~");
        mKeyMap.put(KEYMOD_CTRL | KeyEvent.VK_INSERT, "\033[2;5~");

        mKeyMap.put(KEYMOD_CTRL | KeyEvent.VK_HOME, "\033[1;5H");
        mKeyMap.put(KEYMOD_CTRL | KeyEvent.VK_END, "\033[1;5F");

        mKeyMap.put(KEYMOD_ALT | KeyEvent.VK_ENTER, "\033\r");
        mKeyMap.put(KEYMOD_CTRL | KeyEvent.VK_ENTER, "\n");
        // Duh, so special...
        mKeyMap.put(KEYMOD_CTRL | KeyEvent.VK_SPACE, "\000");

        mKeyMap.put(KEYMOD_SHIFT | KeyEvent.VK_F1, "\033[1;2P");
        mKeyMap.put(KEYMOD_SHIFT | KeyEvent.VK_F2, "\033[1;2Q");
        mKeyMap.put(KEYMOD_SHIFT | KeyEvent.VK_F3, "\033[1;2R");
        mKeyMap.put(KEYMOD_SHIFT | KeyEvent.VK_F4, "\033[1;2S");
        mKeyMap.put(KEYMOD_SHIFT | KeyEvent.VK_F5, "\033[15;2~");
        mKeyMap.put(KEYMOD_SHIFT | KeyEvent.VK_F6, "\033[17;2~");
        mKeyMap.put(KEYMOD_SHIFT | KeyEvent.VK_F7, "\033[18;2~");
        mKeyMap.put(KEYMOD_SHIFT | KeyEvent.VK_F8, "\033[19;2~");
        mKeyMap.put(KEYMOD_SHIFT | KeyEvent.VK_F9, "\033[20;2~");
        mKeyMap.put(KEYMOD_SHIFT | KeyEvent.VK_F10, "\033[21;2~");

//        mKeyCodes[KeyEvent.VK] = "\015";
        mKeyCodes[KeyEvent.VK_UP] = "\033[A";
        mKeyCodes[KeyEvent.VK_DOWN] = "\033[B";
        mKeyCodes[KeyEvent.VK_RIGHT] = "\033[C";
        mKeyCodes[KeyEvent.VK_LEFT] = "\033[D";
        setFnKeys("vt100");
        mKeyCodes[KeyEvent.VK_PRINTSCREEN] = "\033[32~"; // Sys Request / Print
        // Is this Scroll lock? mKeyCodes[Cancel] = "\033[33~";
        mKeyCodes[KeyEvent.VK_PAUSE] = "\033[34~"; // Pause/Break

        mKeyCodes[KeyEvent.VK_TAB] = "\011";
        mKeyCodes[KeyEvent.VK_ENTER] = "\015";
        mKeyCodes[KeyEvent.VK_ESCAPE] = "\033";

        mKeyCodes[KeyEvent.VK_INSERT] = "\033[2~";
        mKeyCodes[KeyEvent.VK_DELETE] = "\033[3~";
        // Home/End keys are set by setFnKeys()
        mKeyCodes[KeyEvent.VK_PAGE_UP] = "\033[5~";
        mKeyCodes[KeyEvent.VK_PAGE_DOWN] = "\033[6~";
        //mKeyCodes[KeyEvent.VK_DELETE]= "\177";
        //mKeyCodes[KeyEvent.VK_NUM_LOCK] = "\033OP";
        mKeyCodes[KeyEvent.VK_DIVIDE] = "/";
        mKeyCodes[KeyEvent.VK_MULTIPLY] = "*";
        mKeyCodes[KeyEvent.VK_SUBTRACT] = "-";
        //mKeyCodes[KeyEvent.VK_ADD] = "+";
        //mKeyCodes[KeyEvent.VK_ENTER] = "\015";
        //mKeyCodes[KeyEvent.VK_EQUALS] = "=";
        //mKeyCodes[KeyEvent.VK_COMMA] = ",";
/*
        mKeyCodes[KEYCODE_NUMPAD_DOT] = ".";
        mKeyCodes[KEYCODE_NUMPAD_0] = "0";
        mKeyCodes[KEYCODE_NUMPAD_1] = "1";
        mKeyCodes[KEYCODE_NUMPAD_2] = "2";
        mKeyCodes[KEYCODE_NUMPAD_3] = "3";
        mKeyCodes[KEYCODE_NUMPAD_4] = "4";
        mKeyCodes[KEYCODE_NUMPAD_5] = "5";
        mKeyCodes[KEYCODE_NUMPAD_6] = "6";
        mKeyCodes[KEYCODE_NUMPAD_7] = "7";
        mKeyCodes[KEYCODE_NUMPAD_8] = "8";
        mKeyCodes[KEYCODE_NUMPAD_9] = "9";
*/
        // Keypad is used for cursor/func keys
        mKeyCodes[KeyEvent.VK_DECIMAL] = mKeyCodes[KeyEvent.VK_DELETE];
        mKeyCodes[KeyEvent.VK_NUMPAD0] = mKeyCodes[KeyEvent.VK_INSERT];
        mKeyCodes[KeyEvent.VK_NUMPAD1] = mKeyCodes[KeyEvent.VK_END];
        mKeyCodes[KeyEvent.VK_NUMPAD2] = mKeyCodes[KeyEvent.VK_DOWN];
        mKeyCodes[KeyEvent.VK_NUMPAD3] = mKeyCodes[KeyEvent.VK_PAGE_DOWN];
        mKeyCodes[KeyEvent.VK_NUMPAD4] = mKeyCodes[KeyEvent.VK_LEFT];
        mKeyCodes[KeyEvent.VK_NUMPAD5] = "5";
        mKeyCodes[KeyEvent.VK_NUMPAD6] = mKeyCodes[KeyEvent.VK_RIGHT];
        mKeyCodes[KeyEvent.VK_NUMPAD7] = mKeyCodes[KeyEvent.VK_HOME];
        mKeyCodes[KeyEvent.VK_NUMPAD8] = mKeyCodes[KeyEvent.VK_UP];
        mKeyCodes[KeyEvent.VK_NUMPAD9] = mKeyCodes[KeyEvent.VK_PAGE_UP];


//        mAppKeyCodes[KEYCODE_DPAD_UP] = "\033OA";
//        mAppKeyCodes[KEYCODE_DPAD_DOWN] = "\033OB";
//        mAppKeyCodes[KEYCODE_DPAD_RIGHT] = "\033OC";
//        mAppKeyCodes[KEYCODE_DPAD_LEFT] = "\033OD";
        mAppKeyCodes[KeyEvent.VK_DIVIDE] = "\033Oo";
        mAppKeyCodes[KeyEvent.VK_MULTIPLY] = "\033Oj";
        mAppKeyCodes[KeyEvent.VK_SUBTRACT] = "\033Om";
        mAppKeyCodes[KeyEvent.VK_PLUS] = "\033Ok";
        mAppKeyCodes[KeyEvent.VK_ENTER] = "\033OM";
        mAppKeyCodes[KeyEvent.VK_EQUALS] = "\033OX";
        mAppKeyCodes[KeyEvent.VK_DECIMAL] = "\033On";
        mAppKeyCodes[KeyEvent.VK_COMMA] = "\033Ol";
        mAppKeyCodes[KeyEvent.VK_NUMPAD0] = "\033Op";
        mAppKeyCodes[KeyEvent.VK_NUMPAD1] = "\033Oq";
        mAppKeyCodes[KeyEvent.VK_NUMPAD2] = "\033Or";
        mAppKeyCodes[KeyEvent.VK_NUMPAD3] = "\033Os";
        mAppKeyCodes[KeyEvent.VK_NUMPAD4] = "\033Ot";
        mAppKeyCodes[KeyEvent.VK_NUMPAD5] = "\033Ou";
        mAppKeyCodes[KeyEvent.VK_NUMPAD6] = "\033Ov";
        mAppKeyCodes[KeyEvent.VK_NUMPAD7] = "\033Ow";
        mAppKeyCodes[KeyEvent.VK_NUMPAD8] = "\033Ox";
        mAppKeyCodes[KeyEvent.VK_NUMPAD9] = "\033Oy";
        setCursorKeysApplicationMode(mCursorApplicationMode);
    }

    private boolean mCursorApplicationMode = true;
    public void setCursorKeysApplicationMode(boolean val) {
        mCursorApplicationMode = val;       
        if (val) {
            mKeyCodes[KeyEvent.VK_NUMPAD8] = mKeyCodes[KeyEvent.VK_UP] = "\033OA";
            mKeyCodes[KeyEvent.VK_NUMPAD2] = mKeyCodes[KeyEvent.VK_DOWN] = "\033OB";
            mKeyCodes[KeyEvent.VK_NUMPAD6] = mKeyCodes[KeyEvent.VK_RIGHT] = "\033OC";
            mKeyCodes[KeyEvent.VK_NUMPAD4] = mKeyCodes[KeyEvent.VK_LEFT] = "\033OD";
        } else {
            mKeyCodes[KeyEvent.VK_NUMPAD8] = mKeyCodes[KeyEvent.VK_UP] = "\033[A";
            mKeyCodes[KeyEvent.VK_NUMPAD2] = mKeyCodes[KeyEvent.VK_DOWN] = "\033[B";
            mKeyCodes[KeyEvent.VK_NUMPAD6] = mKeyCodes[KeyEvent.VK_RIGHT] = "\033[C";
            mKeyCodes[KeyEvent.VK_NUMPAD4] = mKeyCodes[KeyEvent.VK_LEFT] = "\033[D";
        }
    }

    /**
     * The state engine for a modifier key. Can be pressed, released, locked,
     * and so on.
     *
     */
    private class ModifierKey {

        private int mState;

        private static final int UNPRESSED = 0;

        private static final int PRESSED = 1;

        private static final int RELEASED = 2;

        private static final int USED = 3;

        private static final int LOCKED = 4;

        /**
         * Construct a modifier key. UNPRESSED by default.
         *
         */
        public ModifierKey() {
            mState = UNPRESSED;
        }

        public void onPress() {
            switch (mState) {
                case PRESSED:
                    // This is a repeat before use
                    break;
                case RELEASED:
                    mState = LOCKED;
                    break;
                case USED:
                    // This is a repeat after use
                    break;
                case LOCKED:
                    mState = UNPRESSED;
                    break;
                default:
                    mState = PRESSED;
                    break;
            }
        }

        public void onRelease() {
            switch (mState) {
                case USED:
                    mState = UNPRESSED;
                    break;
                case PRESSED:
                    mState = RELEASED;
                    break;
                default:
                    // Leave state alone
                    break;
            }
        }

        public void adjustAfterKeypress() {
            switch (mState) {
                case PRESSED:
                    mState = USED;
                    break;
                case RELEASED:
                    mState = UNPRESSED;
                    break;
                default:
                    // Leave state alone
                    break;
            }
        }

        public boolean isActive() {
            return mState != UNPRESSED;
        }
    }

    private ModifierKey mAltKey = new ModifierKey();

    private ModifierKey mCapKey = new ModifierKey();

    private ModifierKey mControlKey = new ModifierKey();

    private ModifierKey mFnKey = new ModifierKey();


    private TermSession mTermSession;

    private boolean mAltSendsEsc;


    // Map keycodes out of (above) the Unicode code point space.
    static public final int KEYCODE_OFFSET = 0xA00000;

    /**
     * Construct a term key listener.
     *
     */
    public TermKeyListener(TermSession termSession) {
        mTermSession = termSession;
        initKeyCodes();
    }


    public void setAltSendsEsc(boolean flag) {
        mAltSendsEsc = flag;
    }

    public void handleControlKey(boolean down) {
        if (down) {
            mControlKey.onPress();
        } else {
            mControlKey.onRelease();
        }
    }

    public void handleAltKey(boolean down) {
        if (down) {
            mAltKey.onPress();
        } else {
            mAltKey.onRelease();
        }
    }

    public void handleFnKey(boolean down) {
        if (down) {
            mFnKey.onPress();
        } else {
            mFnKey.onRelease();
        }
    }

    public void setTermType(String termType) {
        setFnKeys(termType);
    }

    private void setFnKeys(String termType) {
        // These key assignments taken from the debian squeeze terminfo database.
        if (termType.equals("xterm")) {
            mKeyCodes[KeyEvent.VK_NUMPAD7] = mKeyCodes[KeyEvent.VK_HOME] = "\033OH";
            mKeyCodes[KeyEvent.VK_NUMPAD1] = mKeyCodes[KeyEvent.VK_END] = "\033OF";
        } else {
            mKeyCodes[KeyEvent.VK_NUMPAD7] = mKeyCodes[KeyEvent.VK_HOME] = "\033[1~";
            mKeyCodes[KeyEvent.VK_NUMPAD1] = mKeyCodes[KeyEvent.VK_END] = "\033[4~";
        }
        if (termType.equals("vt100")) {
            mKeyCodes[KeyEvent.VK_F1] = "\033OP"; // VT100 PF1
            mKeyCodes[KeyEvent.VK_F2] = "\033OQ"; // VT100 PF2
            mKeyCodes[KeyEvent.VK_F3] = "\033OR"; // VT100 PF3
            mKeyCodes[KeyEvent.VK_F4] = "\033OS"; // VT100 PF4
            // the following keys are in the database, but aren't on a real vt100.
            mKeyCodes[KeyEvent.VK_F5] = "\033Ot";
            mKeyCodes[KeyEvent.VK_F6] = "\033Ou";
            mKeyCodes[KeyEvent.VK_F7] = "\033Ov";
            mKeyCodes[KeyEvent.VK_F8] = "\033Ol";
            mKeyCodes[KeyEvent.VK_F9] = "\033Ow";
            mKeyCodes[KeyEvent.VK_F10] = "\033Ox";
            // The following keys are not in database.
            mKeyCodes[KeyEvent.VK_F11] = "\033[23~";
            mKeyCodes[KeyEvent.VK_F12] = "\033[24~";
        } else if (termType.startsWith("linux")) {
            mKeyCodes[KeyEvent.VK_F1] = "\033[[A";
            mKeyCodes[KeyEvent.VK_F2] = "\033[[B";
            mKeyCodes[KeyEvent.VK_F3] = "\033[[C";
            mKeyCodes[KeyEvent.VK_F4] = "\033[[D";
            mKeyCodes[KeyEvent.VK_F5] = "\033[[E";
            mKeyCodes[KeyEvent.VK_F6] = "\033[17~";
            mKeyCodes[KeyEvent.VK_F7] = "\033[18~";
            mKeyCodes[KeyEvent.VK_F8] = "\033[19~";
            mKeyCodes[KeyEvent.VK_F9] = "\033[20~";
            mKeyCodes[KeyEvent.VK_F10] = "\033[21~";
            mKeyCodes[KeyEvent.VK_F11] = "\033[23~";
            mKeyCodes[KeyEvent.VK_F12] = "\033[24~";
        } else {
            // default
            // screen, screen-256colors, xterm, anything new
            mKeyCodes[KeyEvent.VK_F1] = "\033OP"; // VT100 PF1
            mKeyCodes[KeyEvent.VK_F2] = "\033OQ"; // VT100 PF2
            mKeyCodes[KeyEvent.VK_F3] = "\033OR"; // VT100 PF3
            mKeyCodes[KeyEvent.VK_F4] = "\033OS"; // VT100 PF4
            mKeyCodes[KeyEvent.VK_F5] = "\033[15~";
            mKeyCodes[KeyEvent.VK_F6] = "\033[17~";
            mKeyCodes[KeyEvent.VK_F7] = "\033[18~";
            mKeyCodes[KeyEvent.VK_F8] = "\033[19~";
            mKeyCodes[KeyEvent.VK_F9] = "\033[20~";
            mKeyCodes[KeyEvent.VK_F10] = "\033[21~";
            mKeyCodes[KeyEvent.VK_F11] = "\033[23~";
            mKeyCodes[KeyEvent.VK_F12] = "\033[24~";
        }
    }

    public int mapControlChar(int ch) {
        return mapControlChar( mControlKey.isActive(), ch);
    }

    public int mapControlChar(boolean control, int ch) {
        int result = ch;
        if (control) {
            // Search is the control key.
            if (result >= 'a' && result <= 'z') {
                result = (char) (result - 'a' + '\001');
            } else if (result >= 'A' && result <= 'Z') {
                result = (char) (result - 'A' + '\001');
            } else if (result == ' ' || result == '2') {
                result = 0;
            } else if (result == '[' || result == '3') {
                result = 27; // ^[ (Esc)
            } else if (result == '\\' || result == '4') {
                result = 28;
            } else if (result == ']' || result == '5') {
                result = 29;
            } else if (result == '^' || result == '6') {
                result = 30; // control-^
            } else if (result == '_' || result == '7') {
                result = 31;
            } else if (result == '8') {
                result = 127; // DEL
            } else if (result == '9') {
                result = KEYCODE_OFFSET + KeyEvent.VK_F11;
            } else if (result == '0') {
                result = KEYCODE_OFFSET + KeyEvent.VK_F12;
            }
//        } else if (fn) {
//            if (result == 'w' || result == 'W') {
//                result = KEYCODE_OFFSET + KeyEvent.VK_UP;
//            } else if (result == 'a' || result == 'A') {
//                result = KEYCODE_OFFSET + KeyEvent.VK_LEFT;
//            } else if (result == 's' || result == 'S') {
//                result = KEYCODE_OFFSET + KeyEvent.VK_DOWN;
//            } else if (result == 'd' || result == 'D') {
//                result = KEYCODE_OFFSET + KeyEvent.VK_RIGHT;
//            } else if (result == 'p' || result == 'P') {
//                result = KEYCODE_OFFSET + KeyEvent.VK_PAGE_UP;
//            } else if (result == 'n' || result == 'N') {
//                result = KEYCODE_OFFSET + KeyEvent.VK_PAGE_DOWN;
//            } else if (result == 't' || result == 'T') {
//                result = KEYCODE_OFFSET + KeyEvent.VK_TAB;
//            } else if (result == 'l' || result == 'L') {
//                result = '|';
//            } else if (result == 'u' || result == 'U') {
//                result = '_';
//            } else if (result == 'e' || result == 'E') {
//                result = 27; // ^[ (Esc)
//            } else if (result == '.') {
//                result = 28; // ^\
//            } else if (result > '0' && result <= '9') {
//                // F1-F9
//                result = (char)(result + KEYCODE_OFFSET + KeyEvent.VK_F1 - 1);
//            } else if (result == '0') {
//                result = KEYCODE_OFFSET + KeyEvent.VK_F10;
//            } else if (result == 'i' || result == 'I') {
//                result = KEYCODE_OFFSET + KeyEvent.VK_INSERT;
//            } else if (result == 'x' || result == 'X') {
//                result = KEYCODE_OFFSET + KeyEvent.VK_DELETE;
//            } else if (result == 'h' || result == 'H') {
//                result = KEYCODE_OFFSET + KeyEvent.VK_HOME;
//            } else if (result == 'f' || result == 'F') {
//                result = KEYCODE_OFFSET + KeyEvent.VK_END;
//            }
        }

        if (result > -1) {
            mAltKey.adjustAfterKeypress();
            mCapKey.adjustAfterKeypress();
            mControlKey.adjustAfterKeypress();
            mFnKey.adjustAfterKeypress();
        }

        return result;
    }

    //=========================================================================
    /**
     *  * 
     * Handle a keyDown event.
     *
     * @param keyCode the keycode of the keyDown event
     * @param keyChar
     * @param event
     * @param appMode
     * @throws IOException 
     */
    public void keyDown( int keyCode, int keyChar, KeyEvent event, boolean appMode ) throws IOException {

        if( keyChar != 65535 ){
            mTermSession.write(keyChar);
        } else {
            int result = keyChar;
            if( keyCode != -1 ){
                if (event != null && handleKeyCode(keyCode, event, appMode)) {
                    return;
                }

                switch (keyCode) {
                    case KeyEvent.VK_ALT:
                        return;

                    case KeyEvent.VK_SHIFT:
                        return;

                    case KeyEvent.VK_CONTROL:
                        return;

                    case KeyEvent.VK_CAPS_LOCK:
                        // Ignore the capslock key.
                        return;

                    default: {
                        break;
                    }
                }

                boolean effectiveControl = mControlKey.isActive();
                result = mapControlChar(effectiveControl, result);
            }
            if (result >= 0)
                mTermSession.write(result);
        }
        
    }

    //============================================================================================
    /**
     * 
     * @param keyCode
     * @param event
     * @param appMode
     * @return
     * @throws IOException 
     */
    public boolean handleKeyCode(int keyCode, KeyEvent event, boolean appMode) throws IOException {
      
        String code = null;
        if (event != null) {
            int keyMod = 0;
            if ( event.isControlDown() || mControlKey.isActive()) {
                keyMod |= KEYMOD_CTRL;
            }
            if ( event.isAltDown() ) {
                keyMod |= KEYMOD_ALT;
            }
            if ( event.isShiftDown() ) {
                keyMod |= KEYMOD_SHIFT;
            }
            // First try to map scancode
            code = mKeyMap.get(event.getKeyCode()  | keyMod);
            if (code == null) {
                code = mKeyMap.get(keyCode | keyMod);
            }
        }

        if (code == null && keyCode >= 0 && keyCode < mKeyCodes.length) {
            if (appMode) {
                code = mAppKeyCodes[keyCode];
            }
            if (code == null) {
                code = mKeyCodes[keyCode];
            }
        }
        
        if (code != null) {
            mTermSession.write(code);
            return true;
        }
        return false;
    }

    /**
     * Handle a keyUp event.
     *
     * @param keyCode the keyCode of the keyUp event
     */
    public void keyUp(int keyCode, KeyEvent event) {
        
        switch (keyCode) {
            case 213:
            case 214:
            case 215:
//                // HENKAN, MUHENKAN, KATAKANA_HIRAGANA
//                if (mThumbCtrl) mAltControlKey = false;
                break;
            case KeyEvent.VK_ALT:
            //case KeyEvent.KEYCODE_ALT_RIGHT:
//                if (allowToggle) {
//                    mAltKey.onRelease();
//                    updateCursorMode();
//                }
                break;
            case KeyEvent.VK_SHIFT:
            //case KeyEvent.KEYCODE_SHIFT_RIGHT:
//                if (allowToggle) {
//                    mCapKey.onRelease();
//                    updateCursorMode();
//                }
                break;

            case KeyEvent.VK_CONTROL: //KEYCODE_CTRL_LEFT:
            //case KEYCODE_CTRL_RIGHT:
                // ignore control keys.
                break;

            default:
                // Ignore other keyUps
                break;
        }
    }

    public boolean getAltSendsEsc() {
        return mAltSendsEsc;
    }

    public boolean isAltActive() {
        return mAltKey.isActive();
    }

    public boolean isCtrlActive() {
        return mControlKey.isActive();
    }
}