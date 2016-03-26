package at.yawk.wm.x;

import lombok.ToString;
import org.freedesktop.xcb.LibXcb;

/**
 * @author yawkat
 */
@ToString(of = { "id", "width", "height" })
public class PixMap extends AbstractResource implements PixMapArea {
    final XcbConnector connector;
    final ColorMap colorMap;
    final int id;
    final int width;
    final int height;

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
