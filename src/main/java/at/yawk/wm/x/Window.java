package at.yawk.wm.x;

import at.yawk.wm.x.image.LocalImage;
import java.awt.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.Set;
import java.util.function.Consumer;
import javax.annotation.concurrent.ThreadSafe;
import lombok.Getter;
import org.freedesktop.xcb.*;

/**
 * @author yawkat
 */
@ThreadSafe
public class Window extends AbstractResource {
    @Getter final Screen screen;
    @Getter final int windowId;

    final ResourceSet resources = new ResourceSet();
    ColorMap colorMap;

    @Getter boolean visible = false;

    private final MaskAttributeSet attributes = new MaskAttributeSet();
    private final MaskAttributeSet config = new MaskAttributeSet();
    final Set<EventGroup> eventGroups;

    @Getter private int width;
    @Getter private int height;

    /**
     * @see RootWindow
     */
    protected Window(Screen screen, int id) {
        this.screen = screen;
        this.windowId = id;
        this.eventGroups = Collections.emptySet();
        this.colorMap = new LazyColorMap(screen);
        resources.register(colorMap);
    }

    Window(Screen screen, int parent, int visual, Set<EventGroup> eventGroups) {
        this.screen = screen;
        this.eventGroups = eventGroups;
        this.colorMap = new LazyColorMap(screen);
        resources.register(colorMap);

        windowId = LibXcb.xcb_generate_id(screen.connector.connection);
        attributes.set(xcb_cw_t.XCB_CW_EVENT_MASK, EventGroup.getMask(eventGroups));
        MaskAttributeSet.Diff diff = attributes.flush();
        LibXcb.xcb_create_window(
                screen.connector.connection,
                (short) LibXcbConstants.XCB_COPY_FROM_PARENT,
                windowId,
                parent,
                (short) 0, (short) 0,
                1, 1, // 1x1+0+0 initial geometry
                0,
                xcb_window_class_t.XCB_WINDOW_CLASS_INPUT_OUTPUT,
                visual,
                diff.getMask(), diff.getValues()
        );
    }

    @Override
    public void close() {
        resources.close();

        destroy();
        screen.connector.flush();
    }

    public void clear() {
        flushAttributes();
        GraphicsImpl.clear(screen.connector, windowId, 0, 0, getWidth(), getHeight());
    }

    protected void destroy() {
        screen.connector.getEventManager().destroyContext(new EventManager.WindowContext(windowId));
        LibXcb.xcb_destroy_window(screen.connector.connection, windowId);
    }

    public PixMap createPixMap(int width, int height) {
        PixMap pixMap = new PixMap(screen.connector, windowId, colorMap, width, height);
        resources.register(pixMap);
        return pixMap;
    }

    public Graphics createGraphics() {
        GraphicsImpl graphics = new GraphicsImpl(this);
        resources.register(graphics);
        return screen.connector.wrapGraphics(graphics);
    }

    public TrayServer createTrayServer() {
        return new TrayServer(this);
    }

    public void setColorMap(ColorMap colorMap) {
        this.colorMap = colorMap;
    }

    private void flushAttributes() {
        MaskAttributeSet.Diff attribDiff = attributes.flush();
        if (!attribDiff.isEmpty()) {
            LibXcb.xcb_change_window_attributes(
                    screen.connector.connection, windowId, attribDiff.getMask(), attribDiff.getValues());
        }
        MaskAttributeSet.Diff configDiff = config.flush();
        if (!configDiff.isEmpty()) {
            LibXcb.xcb_configure_window(
                    screen.connector.connection, windowId, configDiff.getMask(), configDiff.getValues());
        }
    }

    private void setProperty(String key, String type, int valueChunkLength, int valueChunkCount, ByteBuffer value) {
        int[] atoms = screen.connector.internAtoms(key, type);
        LibXcb.xcb_change_property(
                screen.connector.connection,
                (short) xcb_prop_mode_t.XCB_PROP_MODE_REPLACE,
                windowId,
                atoms[0],
                atoms[1],
                (short) valueChunkLength,
                valueChunkCount,
                value
        );
    }

    public void setPropertyAtom(String key, String atom) {
        int i = screen.connector.internAtom(atom);
        setProperty(
                key,
                "ATOM",
                32, 1, // 1 chunk of 32 bits
                ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder()).putInt(i)
        );
    }

    public Window setDock() {
        setPropertyAtom("_NET_WM_WINDOW_TYPE", "_NET_WM_WINDOW_TYPE_DOCK");
        return this;
    }

    private void setAttribute(int key, int value) {
        attributes.set(key, value);
        if (visible) { flushAttributes(); }
    }

    public Window setBackgroundColor(Color color) {
        setAttribute(xcb_cw_t.XCB_CW_BACK_PIXEL, colorMap.get(color));
        return this;
    }

    public Window setBackgroundPixMap(PixMap map) {
        setAttribute(xcb_cw_t.XCB_CW_BACK_PIXMAP, map.id);
        return this;
    }

    public <E> Window addListener(Class<E> eventType, Consumer<E> handler) {
        boolean maySubscribe = false;
        for (EventGroup group : eventGroups) {
            if (group.getEventClasses().contains(eventType)) {
                maySubscribe = true;
                break;
            }
        }
        if (!maySubscribe) {
            throw new UnsupportedOperationException(
                    "Cannot subscribe to " + eventType.getName() + ", event groups are " + eventGroups);
        }
        screen.connector.getEventManager().addEventHandler(
                eventType, new EventManager.WindowContext(windowId), handler);
        return this;
    }

    public Window show() {
        flushAttributes();
        LibXcb.xcb_map_window(screen.connector.connection, windowId);
        screen.connector.flush();
        visible = true;
        return this;
    }

    public Window setBounds(int x, int y, int width, int height) {
        this.width = width;
        this.height = height;
        config.set(xcb_config_window_t.XCB_CONFIG_WINDOW_X, x);
        config.set(xcb_config_window_t.XCB_CONFIG_WINDOW_Y, y);
        config.set(xcb_config_window_t.XCB_CONFIG_WINDOW_WIDTH, width);
        config.set(xcb_config_window_t.XCB_CONFIG_WINDOW_HEIGHT, height);
        if (visible) { flushAttributes(); }
        return this;
    }

    public Window flush() {
        flushAttributes();
        screen.connector.checkError();
        return this;
    }

    public Window acquireFocus() {
        LibXcb.xcb_set_input_focus(
                screen.connector.connection,
                (short) xcb_input_focus_t.XCB_INPUT_FOCUS_POINTER_ROOT,
                windowId,
                LibXcbConstants.XCB_CURRENT_TIME
        );
        return this;
    }

    /**
     * Create a screenshot.
     *
     * @return an array of RGB data of the screenshot.
     */
    public LocalImage capture(int x, int y, int width, int height) {
        xcb_get_image_cookie_t cookie = LibXcb.xcb_get_image(
                screen.connector.connection,
                (short) xcb_image_format_t.XCB_IMAGE_FORMAT_Z_PIXMAP,
                windowId,
                (short) x, (short) y, width, height,
                0xffffffff
        );
        xcb_get_image_reply_t reply = LibXcb.xcb_get_image_reply(
                screen.connector.connection, cookie, new xcb_generic_error_t(0, false));
        ByteBuffer data = LibXcb.xcb_get_image_data(reply);
        // we do limit-slice here so we don't get ridiculous capacities
        data.position(0);
        data.limit(LibXcb.xcb_get_image_data_length(reply));
        return new ZFormatImage(width, height, data.slice());
    }
}
