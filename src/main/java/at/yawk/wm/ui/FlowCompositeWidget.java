package at.yawk.wm.ui;

import at.yawk.wm.x.Graphics;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yawkat
 */
public class FlowCompositeWidget extends Widget {
    private final List<Widget> widgets = new ArrayList<>();

    private final Anchor anchor = new Anchor();

    @Override
    public void setOrigin(Origin origin) {
        super.setOrigin(origin);
        for (Widget widget : widgets) {
            widget.setOrigin(origin);
        }
    }

    public void addWidget(Widget widget) {
        widget.setOrigin(getOrigin());
        widgets.add(widget);
        widget.owner = this;
    }

    public void removeWidget(Widget widget) {
        widgets.remove(widget);
        widget.owner = null;
    }

    @Override
    protected void layout(Graphics graphics) {
        anchor.setX(getX());
        anchor.setY(getY());

        for (Widget widget : widgets) {
            widget.doLayout(graphics);
        }

        int width = 0;
        int height = 0;
        for (Widget widget : widgets) {
            width = Math.max(width, Math.max(Math.abs(getX() - widget.getX()), Math.abs(getX() - widget.getX2())));
            height = Math.max(height, Math.max(Math.abs(getY() - widget.getY()), Math.abs(getY() - widget.getY2())));
        }

        setWidth(width);
        setHeight(height);
    }

    @Override
    protected void render(Graphics graphics) {
        for (Widget widget : widgets) {
            widget.doRender(graphics);
        }
    }

    public List<Widget> getWidgets() {
        return this.widgets;
    }

    public Anchor getAnchor() {
        return this.anchor;
    }
}
