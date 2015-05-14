package at.yawk.wm.x;

import java.awt.*;

/**
 * @author yawkat
 */
public class LazyColorMap extends ColorMap {
    private final Screen screen;
    private ColorMap handle;

    public LazyColorMap(Screen screen) {
        this.screen = screen;
    }

    @Override
    synchronized int get(Color color) {
        if (handle == null) {
            handle = screen.createColorMap();
        }
        return handle.get(color);
    }

    @Override
    public synchronized void close() {
        if (handle != null) {
            handle.close();
        }
    }
}
