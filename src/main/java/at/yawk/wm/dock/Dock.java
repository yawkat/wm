package at.yawk.wm.dock;

import at.yawk.wm.ui.*;
import at.yawk.wm.ui.LayoutManager;
import at.yawk.wm.x.*;
import at.yawk.wm.x.Graphics;
import at.yawk.wm.x.Window;
import at.yawk.wm.x.event.ExposeEvent;
import java.awt.*;
import javax.annotation.concurrent.ThreadSafe;

/**
 * @author yawkat
 */
@ThreadSafe
public class Dock extends AbstractResource {
    private final Window window;
    private final Graphics windowGraphics;
    private final Color backgroundColor;
    private PixMap buffer = null;
    private Graphics graphics;

    private final LayoutManager layoutManager = new LayoutManager();

    private final WidgetSet left;
    private final WidgetSet right;

    private final Anchor leftAnchor = new Anchor();
    private final Anchor rightAnchor = new Anchor();

    {
        left = new DirectionalWidgetChain(layoutManager, leftAnchor, Direction.HORIZONTAL);
        right = new DirectionalWidgetChain(layoutManager, rightAnchor, Direction.HORIZONTAL);
    }

    public Dock(Screen screen, Color backgroundColor) {
        window = screen.createWindow(EventGroup.PAINT, EventGroup.MOUSE_PRESS)
                .setType(WindowType.DOCK)
                .setBackgroundColor(backgroundColor);
        windowGraphics = window.createGraphics();
        window.addListener(ExposeEvent.class, evt -> doRender(new RenderPass(graphics, true)));
        this.backgroundColor = backgroundColor;
    }

    public void setBounds(int x, int y, int width, int height) {
        window.setBounds(x, y, width, height);
        rightAnchor.setX(width);
        window.setStrutPartial(
                0, 0, height, 0,
                0, 0, 0, 0, x, x + width, 0, 0
        );

        if (buffer != null) {
            buffer.close();
        }
        buffer = window.createPixMap(width, height);
        graphics = buffer.createGraphics();
    }

    public WidgetSet getWidgets() {
        return layoutManager;
    }

    public void render() {
        doRender(new RenderPass(graphics, false));
    }

    private synchronized void doRender(RenderPass pass) {
        graphics.setForegroundColor(backgroundColor);
        graphics.fillRect(0, 0, buffer.getWidth(), buffer.getHeight());
        layoutManager.render(pass);
        windowGraphics.drawPixMap(buffer, 0, 0, 0, 0, buffer.getWidth(), buffer.getHeight());
        windowGraphics.flush();
    }

    @Override
    public synchronized void close() {
        // should close all other stuff
        window.close();
    }

    public void show() {
        window.show();
    }

    public Window getWindow() {
        return this.window;
    }

    public Graphics getGraphics() {
        return this.graphics;
    }

    public WidgetSet getLeft() {
        return this.left;
    }

    public WidgetSet getRight() {
        return this.right;
    }
}
