package at.yawk.wm.x;

import org.freedesktop.xcb.LibXcb;
import org.freedesktop.xcb.LibXcbConstants;
import org.freedesktop.xcb.SWIGTYPE_p__XCBKeySymbols;

/**
 * @author yawkat
 */
class KeyManager extends AbstractResource {
    private final SWIGTYPE_p__XCBKeySymbols symbols;

    KeyManager(XcbConnector connector) {
        symbols = LibXcb.xcb_key_symbols_alloc(connector.connection);
    }

    public int getKeyChar(int code) {
        return LibXcb.xcb_key_symbols_get_keysym(symbols, (short) code, 0);
    }

    @Override
    public void close() {
        LibXcb.xcb_key_symbols_free(symbols);
    }
}
