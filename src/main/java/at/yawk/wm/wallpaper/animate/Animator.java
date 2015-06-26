package at.yawk.wm.wallpaper.animate;

import at.yawk.wm.x.AbstractResource;
import at.yawk.wm.x.Graphics;
import at.yawk.wm.x.PixMap;
import at.yawk.wm.x.Window;
import java.awt.*;
import java.util.Iterator;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author yawkat
 */
public class Animator extends AbstractResource {
    private final Color backgroundColor;
    private final AnimatedWallpaper wallpaper;
    private final ScheduledExecutorService scheduler;

    private final Window window;
    private Graphics graphics;

    private Future<?> currentRunningTask = null;

    private int canvasWidth;
    private int canvasHeight;

    public Animator(AnimatedWallpaper wallpaper, Color backgroundColor, ScheduledExecutorService scheduler,
                    Window window) {
        this.backgroundColor = backgroundColor;
        this.wallpaper = new AnimatedWallpaper(wallpaper); // copy so we can free frames when done
        this.scheduler = scheduler;
        this.window = window;

        graphics = window.createGraphics();
        canvasWidth = wallpaper.getBaseFrame().getWidth();
        canvasHeight = wallpaper.getBaseFrame().getHeight();
    }

    private void drawBase() {
        // set the window background to the base frame
        Frame frame = wallpaper.getBaseFrame();

        PixMap pixMap = window.createPixMap(window.getScreen().getWidth(), window.getScreen().getHeight());

        Graphics pixMapGraphics = pixMap.createGraphics();
        pixMapGraphics.setForegroundColor(backgroundColor);
        pixMapGraphics.setBackgroundColor(backgroundColor);
        pixMapGraphics.fillRect(0, 0, window.getWidth(), window.getHeight());
        pixMapGraphics.putImage(
                (window.getWidth() - frame.getWidth()) / 2,
                (window.getHeight() - frame.getHeight()) / 2,
                frame.getWidth(), frame.getHeight(),
                frame.getData(), 0, 3
        );
        pixMapGraphics.close();

        window.setBackgroundPixMap(pixMap);
        window.clear();
    }

    public Future<?> start() {
        drawBase();
        wallpaper.setBaseFrame(null);
        return show(wallpaper.getStart());
    }

    public Future<?> stop() {
        return show(wallpaper.getStop());
    }

    /**
     * @return A future that completes when the animation is done.
     */
    private synchronized Future<?> show(FrameAnimation animation) {
        stopTask();

        Iterator<Frame> frameIterator = animation.getFrames().iterator();
        return currentRunningTask = scheduler.scheduleAtFixedRate(() -> {
            try {
                if (frameIterator.hasNext()) {
                    Frame nextFrame = frameIterator.next();
                    if (!nextFrame.isEmpty()) {
                        graphics.putImage(
                                (window.getWidth() - canvasWidth) / 2 + nextFrame.getX(),
                                (window.getHeight() - canvasHeight) / 2 + nextFrame.getY(),
                                nextFrame.getWidth(), nextFrame.getHeight(),
                                nextFrame.getData(), 0, 3
                        );
                        graphics.flush();
                    }
                    frameIterator.remove(); // remove from iterator to allow GC
                } else {
                    stopTask();
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }, 0, animation.getInterval(), TimeUnit.MILLISECONDS);
    }

    private synchronized void stopTask() {
        if (currentRunningTask != null) {
            currentRunningTask.cancel(false);
            currentRunningTask = null;
        }
    }

    @Override
    public void close() {
        graphics.close();
    }
}