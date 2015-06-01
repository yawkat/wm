package at.yawk.wm.x;

import java.util.function.Consumer;
import javax.annotation.concurrent.ThreadSafe;
import org.freedesktop.xcb.xcb_screen_t;

/**
 * @author yawkat
 */
@ThreadSafe
public class Screen {
    final XcbConnector connector;
    final xcb_screen_t screen;

    private final ResourceSet resources = new ResourceSet();

    Screen(XcbConnector connector, xcb_screen_t screen) {
        this.connector = connector;
        this.screen = screen;
    }

    public ColorMap createColorMap() {
        ColorMap map = new ColorMapImpl(this);
        resources.register(map);
        return map;
    }

    ColorMap createLazyColorMap() {
        ColorMap map = new LazyColorMap(this);
        resources.register(map);
        return map;
    }

    public Window createWindow() {
        Window window = new Window(this, screen.getRoot(), screen.getRoot_visual());
        resources.register(window);
        return window;
    }

    public int getWidth() {
        return screen.getWidth_in_pixels();
    }

    public int getHeight() {
        return screen.getWidth_in_pixels();
    }

    public <E> Screen addListener(Class<E> eventType, Consumer<E> handler) {
        connector.getEventManager().addEventHandler(
                eventType, new EventManager.WindowContext(screen.getRoot()), handler);
        return this;
    }
}
