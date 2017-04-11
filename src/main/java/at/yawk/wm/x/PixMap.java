package at.yawk.wm.x;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.freedesktop.xcb.LibXcb;
import org.freedesktop.xcb.xcb_generic_error_t;
import org.freedesktop.xcb.xcb_void_cookie_t;

/**
 * @author yawkat
 */
@Slf4j
@ToString(of = { "id", "width", "height" })
public class PixMap extends AbstractResource implements PixMapArea {
    final XcbConnector connector;
    final ColorMap colorMap;
    final int id;
    final int width;
    final int height;

    final ResourceSet resources = new ResourceSet();

    PixMap(XcbConnector connector, int drawable, ColorMap colorMap, int width, int height) {
        if (width > 8000 || height > 8000 || width < 0 || height < 0 || width * height < 0) {
            throw new IllegalArgumentException("Pixmap too large: " + width + "x" + height);
        }

        this.connector = connector;
        this.colorMap = colorMap;
        this.id = LibXcb.xcb_generate_id(connector.connection);
        this.width = width;
        this.height = height;

        short depth = connector.getScreen().screen.getRoot_depth();
        if (XcbConnector.DEBUG_ERRORS) {
            xcb_void_cookie_t cookie = LibXcb.xcb_create_pixmap_checked(
                    connector.connection, depth, id, drawable, width, height);
            xcb_generic_error_t error = LibXcb.xcb_request_check(connector.connection, cookie);
            if (error != null) {
                throw new RuntimeException("X Error (pixmap init): " + error.getError_code());
            }
        } else {
            LibXcb.xcb_create_pixmap(connector.connection, depth, id, drawable, width, height);
        }
        connector.checkError();
    }

    @Override
    public void close() {
        resources.close();
        LibXcb.xcb_free_pixmap(connector.connection, id);
    }

    public Graphics createGraphics() {
        GraphicsImpl graphics = new GraphicsImpl(this);
        resources.register(graphics);
        return connector.wrapGraphics(graphics);
    }

    @Override
    public PixMapArea getArea(int x, int y, int width, int height) {
        return new PixMapAreaImpl(this, x, y, width, height);
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }
}
