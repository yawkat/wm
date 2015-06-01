package at.yawk.wm.x;

import java.awt.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.function.Consumer;
import javax.annotation.concurrent.ThreadSafe;
import lombok.Getter;
import org.freedesktop.xcb.*;

/**
 * @author yawkat
 */
@ThreadSafe
public class Window extends AbstractResource {
    final Screen screen;
    final int windowId;

    final ResourceSet resources = new ResourceSet();
    ColorMap colorMap;

    @Getter boolean visible = false;

    private final MaskAttributeSet attributes = new MaskAttributeSet();
    private final MaskAttributeSet config = new MaskAttributeSet();

    Window(Screen screen, int parent, int visual) {
        this.screen = screen;
        this.colorMap = new LazyColorMap(screen);
        resources.register(colorMap);

        windowId = LibXcb.xcb_generate_id(screen.connector.connection);
        attributes.set(xcb_cw_t.XCB_CW_EVENT_MASK,
                       xcb_event_mask_t.XCB_EVENT_MASK_EXPOSURE |
                       xcb_event_mask_t.XCB_EVENT_MASK_BUTTON_PRESS);
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

        LibXcb.xcb_destroy_window(screen.connector.connection, windowId);
        screen.connector.getEventManager().destroyContext(new EventManager.WindowContext(windowId));
    }

    public PixMap createPixMap(int width, int height) {
        PixMap pixMap = new PixMap(screen.connector, windowId, colorMap, width, height);
        resources.register(pixMap);
        return pixMap;
    }

    public Graphics createGraphics() {
        GraphicsImpl graphics = new GraphicsImpl(this);
        resources.register(graphics);
        return graphics;
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

    private void setPropertyAtom(String key, String atom) {
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

    public <E> Window addListener(Class<E> eventType, Consumer<E> handler) {
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
        config.set(xcb_config_window_t.XCB_CONFIG_WINDOW_X, x);
        config.set(xcb_config_window_t.XCB_CONFIG_WINDOW_Y, y);
        config.set(xcb_config_window_t.XCB_CONFIG_WINDOW_WIDTH, width);
        config.set(xcb_config_window_t.XCB_CONFIG_WINDOW_HEIGHT, height);
        if (visible) { flushAttributes(); }
        return this;
    }
}
