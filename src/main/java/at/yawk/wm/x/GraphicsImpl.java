package at.yawk.wm.x;

import at.yawk.wm.x.font.FontRenderer;
import at.yawk.wm.x.font.GlyphFont;
import java.awt.*;
import javax.annotation.concurrent.NotThreadSafe;
import org.freedesktop.xcb.LibXcb;
import org.freedesktop.xcb.xcb_gc_t;
import org.freedesktop.xcb.xcb_rectangle_t;

/**
 * @author yawkat
 */
@NotThreadSafe
class GraphicsImpl extends AbstractResource implements Graphics {
    private final int containerDrawableId;
    private final XcbConnector connector;
    private int contextId;
    private ColorMap colorMap;

    private final MaskAttributeSet flags = new MaskAttributeSet();

    private boolean created = false;

    private FontRenderer fontRenderer;

    GraphicsImpl(Window window) {
        this.colorMap = window.colorMap;
        this.connector = window.screen.connector;
        this.containerDrawableId = window.windowId;
    }

    GraphicsImpl(PixMap pixMap) {
        this.colorMap = pixMap.colorMap;
        this.connector = pixMap.connector;
        this.containerDrawableId = pixMap.id;
    }

    private void flushFlags() {
        MaskAttributeSet.Diff diff = flags.flush();
        if (created) {
            if (!diff.isEmpty()) {
                LibXcb.xcb_change_gc(
                        connector.connection,
                        contextId,
                        diff.getMask(), diff.getValues()
                );
            }
        } else {
            contextId = LibXcb.xcb_generate_id(connector.connection);
            LibXcb.xcb_create_gc(
                    connector.connection,
                    contextId,
                    containerDrawableId,
                    diff.getMask(), diff.getValues()
            );
            created = true;
        }
    }

    @Override
    public void flush() {
        connector.flush();
    }

    @Override
    public void close() {
        if (created) {
            LibXcb.xcb_free_gc(connector.connection, contextId);
            created = false;
        }
    }

    public void setColorMap(ColorMap colorMap) {
        this.colorMap = colorMap;
    }

    @Override
    public GraphicsImpl setFont(String font) {
        flags.set(xcb_gc_t.XCB_GC_FONT, connector.getBasicFontRegistry().get(font));
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
        this.fontRenderer = connector.fontRenderers.computeIfAbsent(
                font,
                f -> new FontRenderer(
                        f,
                        connector.connection,
                        connector.format,
                        containerDrawableId,
                        (short) connector.getScreen().screen.getRoot_depth()
                )
        );
        return this;
    }

    @Override
    public GraphicsImpl drawText(int x, int y, String text) {
        flushFlags();
        if (fontRenderer == null) {
            LibXcb.xcb_image_text_8(
                    connector.connection,
                    (short) text.length(),
                    containerDrawableId,
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
    public GraphicsImpl fillRect(int x, int y, int width, int height) {
        flushFlags();
        xcb_rectangle_t rect = new xcb_rectangle_t();
        rect.setX((short) x);
        rect.setY((short) y);
        rect.setWidth(width);
        rect.setHeight(height);
        LibXcb.xcb_poly_fill_rectangle(
                connector.connection,
                containerDrawableId,
                contextId,
                1, rect
        );
        return this;
    }

    @Override
    public GraphicsImpl clearRect(int x, int y, int width, int height) {
        flushFlags();
        clear(connector, containerDrawableId, x, y, width, height);
        return this;
    }

    static void clear(XcbConnector connector, int drawable, int x, int y, int width, int height) {
        LibXcb.xcb_clear_area(
                connector.connection,
                (short) 0,
                drawable,
                (short) x, (short) y, width, height
        );
    }

    @Override
    public Graphics drawPixMap(PixMap pixMap, int srcX, int srcY, int destX, int destY, int width, int height) {
        flushFlags();
        LibXcb.xcb_copy_area(
                connector.connection,
                pixMap.id,
                this.containerDrawableId,
                this.contextId,
                (short) srcX,
                (short) srcY,
                (short) destX,
                (short) destY,
                width,
                height
        );
        return this;
    }

    @Override
    public Graphics putImage(int x, int y, int width, int height, byte[] data, int offset, int pixelOffset) {
        flushFlags();
        XUtil.putImage(
                connector.connection,
                containerDrawableId,
                contextId,
                connector.getScreen().screen.getRoot_depth(),
                x, y, width, height,
                data,
                offset,
                pixelOffset
        );
        return this;
    }
}
