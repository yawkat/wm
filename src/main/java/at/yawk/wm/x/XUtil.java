package at.yawk.wm.x;

import at.yawk.wm.x.image.LocalImage;
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
     * @param connection The XCB connection
     * @param drawable   Target drawable ID (probably the drawable the graphics context was created from)
     * @param graphics   Graphics context ID
     * @param depth      Image depth, use <code>connector.getScreen().screen.getRoot_depth()</code>
     * @param width      Width of the image
     * @param height     Height of the image
     * @param image      The image to put
     */
    public static void putImage(SWIGTYPE_p_xcb_connection_t connection, int drawable, int graphics, short depth,
                                int x, int y, int width, int height, LocalImage image) {
        ZFormatImage zImage = image.as(ZFormatImage.TYPE);
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
                zImage.getBuffer().capacity(),
                zImage.getBuffer()
        );
    }
}
