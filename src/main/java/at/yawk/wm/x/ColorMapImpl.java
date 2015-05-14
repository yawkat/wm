package at.yawk.wm.x;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.concurrent.ThreadSafe;
import lombok.Value;
import org.freedesktop.xcb.*;

/**
 * @author yawkat
 */
@ThreadSafe
class ColorMapImpl extends ColorMap {
    private final XcbConnector connector;
    private final int colormapId;

    private final Map<ColorDesc, Integer> map = new HashMap<>();

    ColorMapImpl(Screen screen) {
        this.connector = screen.connector;
        this.colormapId = LibXcb.xcb_generate_id(connector.connection);
        LibXcb.xcb_create_colormap(
                connector.connection,
                (short) xcb_colormap_alloc_t.XCB_COLORMAP_ALLOC_NONE,
                colormapId,
                screen.screen.getRoot(),
                screen.screen.getRoot_visual()
        );
    }

    int get(Color color) {
        return get(new ColorDesc(
                color.getRed() * 256,
                color.getGreen() * 256,
                color.getBlue() * 256
        ));
    }

    private synchronized int get(ColorDesc desc) {
        return map.computeIfAbsent(desc, this::allocate);
    }

    private int allocate(ColorDesc desc) {
        xcb_alloc_color_cookie_t alloc = LibXcb.xcb_alloc_color(
                connector.connection, colormapId, desc.r, desc.g, desc.b);
        xcb_alloc_color_reply_t reply = LibXcb.xcb_alloc_color_reply(
                connector.connection, alloc, new xcb_generic_error_t(0, false));
        return reply.getPixel();
    }

    @Override
    public void close() {
        LibXcb.xcb_free_colormap(connector.connection, colormapId);
    }

    @Value
    private static class ColorDesc {
        // uint16 (0-65535)

        private final int r;
        private final int g;
        private final int b;
    }
}
