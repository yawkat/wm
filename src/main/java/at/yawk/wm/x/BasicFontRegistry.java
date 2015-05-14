package at.yawk.wm.x;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.concurrent.ThreadSafe;
import org.freedesktop.xcb.LibXcb;

/**
 * @author yawkat
 */
@ThreadSafe
class BasicFontRegistry extends AbstractResource {
    private final XcbConnector connector;
    private final Map<String, Integer> fonts = new HashMap<>();

    BasicFontRegistry(XcbConnector connector) {
        this.connector = connector;
    }

    public synchronized int get(String font) {
        return fonts.computeIfAbsent(font, f -> {
            int fid = LibXcb.xcb_generate_id(connector.connection);
            LibXcb.xcb_open_font(connector.connection, fid, font.length(), font);
            return fid;
        });
    }

    @Override
    public synchronized void close() {
        fonts.values().forEach(fid -> LibXcb.xcb_close_font(connector.connection, fid));
        fonts.clear();
    }
}
