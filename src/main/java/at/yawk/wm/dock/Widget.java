package at.yawk.wm.dock;

import at.yawk.wm.x.Graphics;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;

/**
 * @author yawkat
 */
public abstract class Widget implements Positioned {
    private static final boolean OPTIMIZE_REPAINT = false;

    @Getter Origin origin = Origin.TOP_LEFT;
    @Getter private int x;
    @Getter private int y;
    @Getter private int width;
    @Getter private int height;

    int lastX = 0;
    int lastY = 0;
    int lastWidth = 0;
    int lastHeight = 0;

    boolean dirty;
    private boolean first = true;
    Widget owner = null;

    final Set<Runnable> geometryListeners = Collections.synchronizedSet(new HashSet<>());
    final Set<Widget> layoutDependencies = new HashSet<>();

    public void markDirty() {
        dirty = true;
        if (owner != null) {
            owner.markDirty();
        }
    }

    @Override
    public void addGeometryListener(Runnable listener) {
        geometryListeners.add(listener);
    }

    final void addLayoutDependency(Widget dependency) {
        layoutDependencies.add(dependency);
    }

    private void addLayoutDependency(Positioned dependency) {
        if (dependency instanceof Widget) {
            addLayoutDependency((Widget) dependency);
        }
    }

    /*
     * We have a pre render and a render step. We do layouting (invokePreRender) before both.
     *
     * In the first step, we clear
     */

    final void preRender(RenderPass pass) {
        if (!first && (dirty || pass.exposePass)) {
            int cx = lastX;
            int cy = lastY;
            if (!getOrigin().isLeft()) {
                cx -= lastWidth;
            }
            if (!getOrigin().isTop()) {
                cy -= lastHeight;
            }
            pass.graphics.clearRect(cx, cy, lastWidth, lastHeight);
        }
        first = false;
    }

    final void internalRender(RenderPass pass) {
        if (!OPTIMIZE_REPAINT || dirty || pass.exposePass) {
            lastX = getX();
            lastY = getY();
            lastWidth = getWidth();
            lastHeight = getHeight();

            render(pass.graphics);
            dirty = false;
        }
    }

    protected void render(Graphics graphics) {}

    private final CollectorSink geometryListenerRefs = new CollectorSink();

    public final void after(Positioned positioned, Direction direction) {
        addLayoutDependency(positioned);
        Runnable listener;
        if (direction == Direction.HORIZONTAL) {
            listener = () -> setX(positioned.getX2());
        } else {
            listener = () -> setY(positioned.getY2());
        }
        listener.run();
        geometryListenerRefs.add(listener);
        positioned.addGeometryListener(new WeakRunnable(listener));
    }

    private void markGeometryDirty() {
        markDirty();
        geometryListeners.forEach(Runnable::run);
    }

    public void setOrigin(Origin origin) {
        if (this.origin != origin) {
            this.origin = origin;
            markGeometryDirty();
        }
    }

    public void setX(int x) {
        if (this.x != x) {
            this.x = x;
            markGeometryDirty();
        }
    }

    public void setY(int y) {
        if (this.y != y) {
            this.y = y;
            markGeometryDirty();
        }
    }

    public void setWidth(int width) {
        if (this.width != width) {
            this.width = width;
            markGeometryDirty();
        }
    }

    public void setHeight(int height) {
        if (this.height != height) {
            this.height = height;
            markGeometryDirty();
        }
    }
}
