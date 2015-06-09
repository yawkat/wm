package at.yawk.wm.x;

import java.nio.ByteBuffer;
import org.freedesktop.xcb.LibXcb;
import org.freedesktop.xcb.SWIGTYPE_p__XCBKeySymbols;

/**
 * @author yawkat
 */
class KeyManager extends AbstractResource {
    private final SWIGTYPE_p__XCBKeySymbols symbols;

    KeyManager(XcbConnector connector) {
        symbols = LibXcb.xcb_key_symbols_alloc(connector.connection);
    }

    public int getKeySymbol(int code, int mod) {
        int sym = LibXcb.xcb_key_symbols_get_keysym(symbols, (short) code, mod);
        return sym;
    }

    public short getKeyCode(int symbol) {
        ByteBuffer buf = LibXcb.xcb_key_symbols_get_keycode(symbols, symbol);
        return buf.getShort();
    }

    @Override
    public void close() {
        LibXcb.xcb_key_symbols_free(symbols);
    }
}
