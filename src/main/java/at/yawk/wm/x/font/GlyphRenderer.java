package at.yawk.wm.x.font;

import at.yawk.wm.x.AbstractResource;
import at.yawk.wm.x.XUtil;
import at.yawk.wm.x.image.ByteArrayImage;
import java.nio.ByteBuffer;
import org.freedesktop.xcb.LibXcb;
import org.freedesktop.xcb.SWIGTYPE_p_xcb_connection_t;
import org.freedesktop.xcb.xcb_format_t;

/**
 * @author yawkat
 */
class GlyphRenderer extends AbstractResource {
    private final SWIGTYPE_p_xcb_connection_t connection;
    private final xcb_format_t format;
    private final int rootDrawable;
    /**
     * pixmap depth
     */
    private final short depth;
    private final GlyphFile file;

    private volatile boolean pixmapLoaded;
    private int pixmapId;

    @java.beans.ConstructorProperties({ "connection", "format", "rootDrawable", "depth", "file" })
    public GlyphRenderer(SWIGTYPE_p_xcb_connection_t connection, xcb_format_t format, int rootDrawable, short depth, GlyphFile file) {
        this.connection = connection;
        this.format = format;
        this.rootDrawable = rootDrawable;
        this.depth = depth;
        this.file = file;
    }

    /**
     * @param drawable  target drawable
     * @param gc        graphics context
     * @param c         character
     * @param x         x location
     * @param baselineY baseline y location
     * @return width of the rendered character
     */
    public int renderChar(int drawable, int gc, char c, int x, int baselineY) {
        int i = c - file.getStartInclusive();
        int ascent = file.getAscent(c);
        int height = file.getHeight(c);
        int width = file.getWidth(c);

        loadIfAbsent();
        LibXcb.xcb_copy_area(
                connection,
                pixmapId,
                drawable,
                gc,
                (short) (i * file.getCellWidth()),
                (short) 0,
                (short) x,
                (short) (baselineY - ascent),
                width,
                height
        );
        return width;
    }

    private void loadIfAbsent() {
        if (pixmapLoaded) { return; }
        synchronized (this) {
            if (pixmapLoaded) { return; }
            pixmapId = LibXcb.xcb_generate_id(connection);
            byte cellHeight = file.getCellHeight();
            byte cellWidth = file.getCellWidth();
            int charCount = file.getCharCount();
            int rowWidth = cellWidth * charCount;

            // ALLOCATE PIXMAP AND GRAPHICS CONTEXT

            LibXcb.xcb_create_pixmap(
                    connection,
                    depth,
                    pixmapId,
                    rootDrawable,
                    rowWidth,
                    cellHeight
            );
            int gcId = LibXcb.xcb_generate_id(connection);
            LibXcb.xcb_create_gc(
                    connection,
                    gcId,
                    pixmapId,
                    0, ByteBuffer.allocateDirect(0)
            );

            // SEND IMAGE

            byte[] data = file.getData();
            int headerLength = charCount * GlyphFile.GLYPH_HEADER_LENGTH;

            XUtil.putImage(
                    connection, pixmapId, gcId, depth,
                    0, 0, rowWidth, (int) cellHeight,
                    new ByteArrayImage(rowWidth, cellHeight, data, headerLength, 3)
            );

            // FREE GRAPHICS CONTEXT
            LibXcb.xcb_free_gc(connection, gcId);

            // done

            pixmapLoaded = true;
            file.freeData(); // don't need to do that again
        }
    }

    @Override
    public synchronized void close() {
        if (pixmapLoaded) {
            LibXcb.xcb_free_pixmap(connection, pixmapId);
            pixmapLoaded = false;
        }
    }
}
