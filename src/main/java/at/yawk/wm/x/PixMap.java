package at.yawk.wm.x;

import lombok.Getter;
import org.freedesktop.xcb.LibXcb;

/**
 * @author yawkat
 */
public class PixMap extends AbstractResource {
    final XcbConnector connector;
    final ColorMap colorMap;
    final int id;
    @Getter final int width;
    @Getter final int height;

    final ResourceSet resources = new ResourceSet();

    PixMap(XcbConnector connector, int drawable, ColorMap colorMap, int width, int height) {
        this.connector = connector;
        this.colorMap = colorMap;
        this.id = LibXcb.xcb_generate_id(connector.connection);
        this.width = width;
        this.height = height;

        LibXcb.xcb_create_pixmap(
                connector.connection,
                connector.getScreen().screen.getRoot_depth(),
                id,
                drawable,
                width,
                height
        );
    }

    @Override
    public void close() {
        resources.close();
        LibXcb.xcb_free_pixmap(connector.connection, id);
    }

    public Graphics createGraphics() {
        GraphicsImpl graphics = new GraphicsImpl(this);
        resources.register(graphics);
        return graphics;
    }
}
