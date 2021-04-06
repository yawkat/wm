package at.yawk.wm.x;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.Consumer;
import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Singleton;

import org.freedesktop.xcb.xcb_screen_t;

@Singleton
@ThreadSafe
public class Screen {
    final XcbConnector connector;
    final xcb_screen_t screen;

    private final ResourceSet resources = new ResourceSet();
    private final Window rootWindow;

    Screen(XcbConnector connector, xcb_screen_t screen) {
        this.connector = connector;
        this.screen = screen;

        rootWindow = new RootWindow(this);
        resources.register(rootWindow);
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

    public Window createWindow(EventGroup... groups) {
        EnumSet<EventGroup> groupSet = EnumSet.noneOf(EventGroup.class);
        Collections.addAll(groupSet, groups);
        return createWindow(groupSet);
    }

    public Window createWindow(Set<EventGroup> groups) {
        Window window = new Window(this, screen.getRoot(), screen.getRoot_visual(), groups);
        resources.register(window);
        return window;
    }

    public Window getRootWindow() {
        return rootWindow;
    }

    public int getWidth() {
        return screen.getWidth_in_pixels();
    }

    public int getHeight() {
        return screen.getHeight_in_pixels();
    }

    public <E> Screen addListener(Class<E> eventType, Consumer<E> handler) {
        connector.getEventManager().addEventHandler(
                eventType, new EventManager.WindowContext(screen.getRoot()), handler);
        return this;
    }
}
