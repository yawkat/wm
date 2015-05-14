package at.yawk.wm.x;

import at.yawk.wm.x.font.FontRenderer;
import at.yawk.wm.x.font.GlyphFont;
import java.awt.*;
import javax.annotation.concurrent.NotThreadSafe;
import org.freedesktop.xcb.LibXcb;
import org.freedesktop.xcb.xcb_gc_t;

/**
 * @author yawkat
 */
@NotThreadSafe
class GraphicsImpl extends AbstractResource implements Graphics {
    private final Window window;
    private int contextId;
    private ColorMap colorMap;

    private final MaskAttributeSet flags = new MaskAttributeSet();

    private boolean created = false;

    private FontRenderer fontRenderer;

    GraphicsImpl(Window window, ColorMap colorMap) {
        this.window = window;
        this.colorMap = colorMap;
    }

    private void flushFlags() {
        MaskAttributeSet.Diff diff = flags.flush();
        if (created) {
            if (!diff.isEmpty()) {
                LibXcb.xcb_change_gc(
                        window.screen.connector.connection,
                        contextId,
                        diff.getMask(), diff.getValues()
                );
            }
        } else {
            contextId = LibXcb.xcb_generate_id(window.screen.connector.connection);
            LibXcb.xcb_create_gc(
                    window.screen.connector.connection,
                    contextId,
                    window.windowId,
                    diff.getMask(), diff.getValues()
            );
            created = true;
        }
    }

    @Override
    public void flush() {
        window.screen.connector.flush();
    }

    @Override
    public void close() {
        if (created) {
            LibXcb.xcb_free_gc(window.screen.connector.connection, contextId);
        }
    }

    public void setColorMap(ColorMap colorMap) {
        this.colorMap = colorMap;
    }

    @Override
    public GraphicsImpl setFont(String font) {
        flags.set(xcb_gc_t.XCB_GC_FONT, window.screen.connector.getBasicFontRegistry().get(font));
        return this;
    }

    @Override
    public GraphicsImpl setForegroundColor(Color color) {
        flags.set(xcb_gc_t.XCB_GC_FOREGROUND, colorMap.get(color));
        return this;
    }

    @Override
    public GraphicsImpl setBackgroundColor(Color color) {
        flags.set(xcb_gc_t.XCB_GC_BACKGROUND, colorMap.get(color));
        return this;
    }

    @Override
    public GraphicsImpl setFont(GlyphFont font) {
        this.fontRenderer = window.fontRenderers.computeIfAbsent(
                font,
                f -> new FontRenderer(f,
                                      window.screen.connector.connection,
                                      window.screen.connector.format,
                                      window.windowId,
                                      (short) window.screen.screen.getRoot_depth())
        );
        return this;
    }

    @Override
    public GraphicsImpl drawText(int x, int y, String text) {
        flushFlags();
        if (fontRenderer == null) {
            LibXcb.xcb_image_text_8(
                    window.screen.connector.connection,
                    (short) text.length(),
                    window.windowId,
                    contextId,
                    (short) x, (short) y,
                    text
            );
        } else {
            fontRenderer.render(contextId, text, x, y);
        }
        return this;
    }

    @Override
    public GraphicsImpl clearRect(int x, int y, int width, int height) {
        LibXcb.xcb_clear_area(
                window.screen.connector.connection,
                (short) 0,
                window.windowId,
                (short) x, (short) y, width, height
        );
        return this;
    }
}
