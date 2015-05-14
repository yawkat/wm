package at.yawk.wm.dock;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * @author yawkat
 */
@NotThreadSafe
abstract class WidgetChain implements WidgetSet {
    private final LayoutManager layoutManager;
    private Positioned front;

    public WidgetChain(LayoutManager layoutManager, Positioned front) {
        this.layoutManager = layoutManager;
        this.front = front;
    }

    @Override
    public void addWidget(Widget widget) {
        layoutManager.addWidget(widget);
        chain(widget, front);
        front = widget;
    }

    protected abstract void chain(Widget widget, Positioned to);
}
