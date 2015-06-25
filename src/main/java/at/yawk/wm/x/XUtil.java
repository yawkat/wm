package at.yawk.wm.x;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.freedesktop.xcb.LibXcb;
import org.freedesktop.xcb.SWIGTYPE_p_xcb_connection_t;
import org.freedesktop.xcb.xcb_image_format_t;

/**
 * @author yawkat
 */
public class XUtil {
    private XUtil() {}

    /**
     * Draw an image from memory to a graphics context.
     *
     * @param connection  The XCB connection
     * @param drawable    Target drawable ID (probably the drawable the graphics context was created from)
     * @param graphics    Graphics context ID
     * @param depth       Image depth, use <code>connector.getScreen().screen.getRoot_depth()</code>
     * @param width       Width of the image
     * @param height      Height of the image
     * @param data        Image data (RGB order, length >= offset + width * height * pixelOffset)
     * @param offset      Start offset in the data array
     * @param pixelOffset Size of a pixel (3 for RGB input, 4 for RGBA input)
     */
    public static void putImage(SWIGTYPE_p_xcb_connection_t connection, int drawable, int graphics, short depth,
                                int x, int y, int width, int height, byte[] data, int offset, int pixelOffset) {
        ByteBuffer buffer = ByteBuffer.allocateDirect((width * 4) * height)
                .order(ByteOrder.nativeOrder());
        for (int sourceY = 0; sourceY < height; sourceY++) {
            for (int sourceX = 0; sourceX < width; sourceX++) {
                int pos = offset + (sourceY * width + sourceX) * pixelOffset;
                byte r = data[pos];
                byte g = data[pos + 1];
                byte b = data[pos + 2];
                buffer.put(b);
                buffer.put(g);
                buffer.put(r);
                buffer.put((byte) 0);
            }
        }
        LibXcb.xcb_put_image(
                connection,
                (short) xcb_image_format_t.XCB_IMAGE_FORMAT_Z_PIXMAP,
                drawable,
                graphics,
                width,
                height,
                (short) x,
                (short) y,
                (short) 0,
                depth,
                buffer.capacity(),
                buffer
        );
    }
}
