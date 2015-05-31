package at.yawk.wm.dock;

import at.yawk.wm.x.AbstractResource;
import at.yawk.wm.x.Graphics;
import at.yawk.wm.x.Screen;
import at.yawk.wm.x.Window;
import at.yawk.wm.x.event.ExposeEvent;
import java.awt.*;
import javax.annotation.concurrent.ThreadSafe;
import lombok.Getter;

/**
 * @author yawkat
 */
@ThreadSafe
public class Dock extends AbstractResource {
    private final Window window;
    @Getter private final Graphics graphics;

    private final LayoutManager layoutManager = new LayoutManager();

    @Getter private final WidgetSet left;
    @Getter private final WidgetSet right;

    private final Anchor leftAnchor = new Anchor();
    private final Anchor rightAnchor = new Anchor();

    {
        left = new DirectionalWidgetChain(layoutManager, leftAnchor, Direction.HORIZONTAL);
        right = new DirectionalWidgetChain(layoutManager, rightAnchor, Direction.HORIZONTAL);
    }

    public Dock(Screen screen, Color backgroundColor) {
        window = screen.createWindow()
                .setDock()
                .setBackgroundColor(backgroundColor);
        graphics = window.createGraphics();
        window.addListener(ExposeEvent.class, evt -> doRender(new RenderPass(graphics, true)));
    }

    public void setBounds(int x, int y, int width, int height) {
        window.setBounds(x, y, width, height);
        rightAnchor.setX(width);
    }

    public WidgetSet getWidgets() {
        return layoutManager;
    }

    public void render() {
        doRender(new RenderPass(graphics, false));
    }

    private synchronized void doRender(RenderPass pass) {
        layoutManager.render(pass);
        graphics.flush();
    }

    @Override
    public synchronized void close() {
        graphics.close();
        window.close();
    }

    public void show() {
        window.show();
    }
}
