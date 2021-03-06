package at.yawk.wm.x.font;

import at.yawk.wm.x.AbstractResource;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import org.freedesktop.xcb.SWIGTYPE_p_xcb_connection_t;

public class FontRenderer extends AbstractResource {
    private final GlyphFont manager;

    private final SWIGTYPE_p_xcb_connection_t connection;
    private final int rootDrawable;
    private final short depth;

    private final Map<GlyphFile, GlyphRenderer> handlerMap =
            Collections.synchronizedMap(new IdentityHashMap<>());

    @java.beans.ConstructorProperties({ "manager", "connection", "format", "rootDrawable", "depth" })
    public FontRenderer(GlyphFont manager, SWIGTYPE_p_xcb_connection_t connection, int rootDrawable, short depth) {
        this.manager = manager;
        this.connection = connection;
        this.rootDrawable = rootDrawable;
        this.depth = depth;
    }

    private GlyphFile getFile(char c) {
        return manager.resolveGlyphFile(c);
    }

    private GlyphRenderer getHandler(char c) {
        GlyphFile file = getFile(c);
        return handlerMap.computeIfAbsent(file, f -> new GlyphRenderer(connection, rootDrawable, depth, f));
    }

    public int getCharWidth(char c) {
        return getFile(c).getWidth(c);
    }

    public int render(int drawable, int gc, CharSequence s, int x, int y) {
        if (s.length() == 0) { return 0; }
        int maxAscent = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            maxAscent = Math.max(maxAscent, getFile(c).getAscent(c));
        }
        int xOffset = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            xOffset += getHandler(c).renderChar(drawable, gc, c, x + xOffset, y + maxAscent);
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
