package at.yawk.wm.dock;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.concurrent.ThreadSafe;

/**
 * @author yawkat
 */
@ThreadSafe
final class Anchor implements Positioned {
    private int x;
    private int y;

    private final Set<Runnable> geometryListeners = Collections.synchronizedSet(new HashSet<>());

    @Override
    public Origin getOrigin() {
        return Origin.TOP_LEFT;
    }

    @Override
    public int getWidth() {
        return 0;
    }

    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    public void addGeometryListener(Runnable listener) {
        geometryListeners.add(listener);
    }

    private void invokeGeometryListeners() {
        geometryListeners.forEach(Runnable::run);
    }

    public void setX(int x) {
        if (this.x != x) {
            this.x = x;
            invokeGeometryListeners();
        }
    }

    public void setY(int y) {
        if (this.y != y) {
            this.y = y;
            invokeGeometryListeners();
        }
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public Set<Runnable> getGeometryListeners() {
        return this.geometryListeners;
    }
}
