package at.yawk.wm.paste;

import at.yawk.wm.tac.Modal;
import at.yawk.wm.x.*;
import at.yawk.wm.x.event.*;
import at.yawk.wm.x.image.BufferedLocalImage;
import at.yawk.wm.x.image.LocalImage;
import at.yawk.wm.x.image.SubImageView;
import java.util.ArrayList;
import java.util.List;
import sun.awt.X11.XKeySymConstants;

/**
 * @author yawkat
 */
class ScreenshotOverlay implements Modal {
    private final PasteManager pasteManager;
    private final XcbConnector connector;

    private final List<Runnable> closeListeners = new ArrayList<>();

    private LocalImage capture;
    private Window displayWindow;
    private Graphics displayGraphics;
    private PixMap darkenedPixMap;
    private PixMap normalPixMap;

    private boolean down = false;
    private int startX;
    private int startY;
    private int endX;
    private int endY;

    @java.beans.ConstructorProperties({ "pasteManager", "connector" })
    public ScreenshotOverlay(PasteManager pasteManager, XcbConnector connector) {
        this.pasteManager = pasteManager;
        this.connector = connector;
    }

    public void capture() {
        Window rootWindow = connector.getScreen().getRootWindow();
        capture = rootWindow.capture(0, 0, rootWindow.getWidth(), rootWindow.getHeight());
    }

    public void open() {
        LocalImage darkened = capture.copy();
        darkened.apply((rgb) -> {
            // 50% brightness (>>> offset + 1 << offset)
            // we basically shift the three bytes right by one bit, effectively halving their values
            return ((rgb >>> 17) & 0x7f) << 16 |
                   ((rgb >>> 9) & 0x7f) << 8 |
                   ((rgb >>> 1) & 0x7f);
        });

        displayWindow = connector.getScreen().createWindow(
                EventGroup.PAINT, EventGroup.MOUSE_MOTION, EventGroup.KEYBOARD,
                EventGroup.MOUSE_PRESS
        );
        displayGraphics = displayWindow.createGraphics();

        darkenedPixMap = displayWindow.createPixMap(capture.getWidth(), capture.getHeight());
        Graphics dg = darkenedPixMap.createGraphics();
        dg.putImage(0, 0, darkened);
        dg.close();

        normalPixMap = displayWindow.createPixMap(capture.getWidth(), capture.getHeight());
        Graphics ng = normalPixMap.createGraphics();
        ng.putImage(0, 0, capture);
        ng.close();

        displayWindow.setBackgroundPixMap(darkenedPixMap);
        displayWindow.setType(WindowType.DOCK);
        displayWindow.setBounds(0, 0, capture.getWidth(), capture.getHeight());
        displayWindow.show();
        displayWindow.acquireFocus();
        displayWindow.addListener(KeyPressEvent.class, evt -> {
            if (evt.getSymbol() == XKeySymConstants.XK_Escape) {
                close();
                evt.cancel();
            }
        });
        displayWindow.addListener(ExposeEvent.class, evt -> {
            paintFrame();
        });
        displayWindow.addListener(ButtonPressEvent.class, evt -> {
            startX = endX = evt.getX();
            startY = endY = evt.getY();
            down = true;
            paintFrame();
        });
        displayWindow.addListener(MouseMoveEvent.class, evt -> {
            endX = evt.getX();
            endY = evt.getY();
            paintFrame();
        });
        displayWindow.addListener(ButtonReleaseEvent.class, evt -> {
            endX = evt.getX();
            endY = evt.getY();
            down = false;
            send();
        });
    }

    private void send() {
        close();
        int minX = Math.min(startX, endX);
        int minY = Math.min(startY, endY);
        int maxX = Math.max(startX, endX);
        int maxY = Math.max(startY, endY);
        if (minX != maxX && minY != maxY) {
            LocalImage region = new SubImageView(capture, minX, minY, maxX - minX, maxY - minY);
            pasteManager.upload(region.as(BufferedLocalImage.TYPE).getImage());
        }
    }

    private void paintFrame() {
        boolean hasContent = false;
        if (down) {
            int minX = Math.min(startX, endX);
            int minY = Math.min(startY, endY);
            int maxX = Math.max(startX, endX);
            int maxY = Math.max(startY, endY);
            if (minX != maxX && minY != maxY) {
                displayGraphics.drawPixMap(normalPixMap, minX, minY, minX, minY, maxX - minX, maxY - minY);
                int w = displayWindow.getWidth();
                int h = displayWindow.getHeight();
                // only clear borders: this makes for less flickering
                if (minX > 0) { displayGraphics.clearRect(0, 0, minX, h); }
                if (maxX < w) { displayGraphics.clearRect(maxX, 0, w - maxX, h); }
                if (minY > 0) { displayGraphics.clearRect(minX, 0, maxX - minX, minY); }
                if (maxY < h) { displayGraphics.clearRect(minX, maxY, maxX - minX, h - maxY); }
                hasContent = true;
            }
        }
        if (!hasContent) {
            displayWindow.clear();
        }
        displayGraphics.flush();
    }

    @Override
    public void close() {
        // all other resources are held by the window
        displayWindow.close();
        closeListeners.forEach(Runnable::run);
    }

    @Override
    public void addCloseListener(Runnable listener) {
        closeListeners.add(listener);
    }
}
