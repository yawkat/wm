package at.yawk.wm.x.font;

import at.yawk.wm.x.AbstractResource;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.freedesktop.xcb.SWIGTYPE_p_xcb_connection_t;
import org.freedesktop.xcb.xcb_format_t;

/**
 * @author yawkat
 */
@RequiredArgsConstructor
public class FontRenderer extends AbstractResource {
    private final GlyphFont manager;

    private final SWIGTYPE_p_xcb_connection_t connection;
    private final xcb_format_t format;
    private final int drawable;
    private final short depth;

    private final Map<GlyphFile, GlyphRenderer> handlerMap =
            Collections.synchronizedMap(new IdentityHashMap<>());

    private GlyphFile getFile(char c) {
        return manager.resolveGlyphFile(c);
    }

    private GlyphRenderer getHandler(char c) {
        GlyphFile file = getFile(c);
        return handlerMap.computeIfAbsent(file, f -> new GlyphRenderer(connection, format, drawable, depth, f));
    }

    public int getCharWidth(char c) {
        return getFile(c).getWidth(c);
    }

    public int render(int gc, CharSequence s, int x, int y) {
        if (s.length() == 0) { return 0; }
        int maxAscent = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            maxAscent = Math.max(maxAscent, getFile(c).getAscent(c));
        }
        int xOffset = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            xOffset += getHandler(c).renderChar(gc, c, x + xOffset, y + maxAscent);
        }
        return xOffset;
    }

    @Override
    public void close() {
        synchronized (handlerMap) {
            handlerMap.values().forEach(GlyphRenderer::close);
            handlerMap.clear();
        }
    }
}
