package at.yawk.wm.ui;

/**
 * @author yawkat
 */
public class DirectionalWidgetChain extends WidgetChain {
    private final Direction direction;

    public DirectionalWidgetChain(LayoutManager layoutManager, Positioned front, Direction direction) {
        super(layoutManager, front);
        this.direction = direction;
    }

    @Override
    protected void chain(Widget widget, Positioned to) {
        widget.after(to, direction);
    }
}
