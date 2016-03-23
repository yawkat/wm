package at.yawk.wm.dock.module.widget;

import at.yawk.wm.dock.Widget;
import at.yawk.wm.dock.module.DockBuilder;
import at.yawk.wm.dock.module.DockConfig;
import at.yawk.wm.dock.module.DockWidget;
import at.yawk.wm.hl.HerbstClient;
import at.yawk.wm.hl.Monitor;
import at.yawk.wm.progress.ProgressManager;
import at.yawk.wm.progress.ProgressTask;
import at.yawk.wm.x.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;

/**
 * @author yawkat
 */
@DockWidget(position = DockWidget.Position.LEFT, priority = Integer.MAX_VALUE)
public class ProgressWidget extends Widget {
    private final List<ProgressTask> tasks = Collections.synchronizedList(new ArrayList<>());

    @Inject DockBuilder dock;
    @Inject DockConfig config;
    @Inject HerbstClient herbstClient;
    @Inject Monitor monitor;

    {
        // we have 0 size in the layout
        setWidth(0);
        setHeight(0);
    }

    @Inject
    void listen(ProgressManager manager) {
        manager.addTaskCreateListener(task -> {
            tasks.add(task);
            task.addChangeListener(() -> {
                if (!task.isRunning()) {
                    tasks.remove(task);
                }
                markDirtyAndRepaint();
            });
            markDirtyAndRepaint();
        });
    }

    private void markDirtyAndRepaint() {
        markDirty();
        dock.render();
    }

    @Override
    protected void render(Graphics graphics) {
        if (herbstClient.getCurrentMonitor().getId() == monitor.getId()) {
            graphics.setForegroundColor(config.getActiveMonitorColor());
            graphics.fillRect(0, 0, dock.getWindow().getWidth(), 1);
        }
        graphics.setForegroundColor(config.getProgressColor());
        int y = 0;
        for (ProgressTask task : tasks) {
            graphics.fillRect(0, y, Math.round(dock.getWindow().getWidth() * task.getProgress()), 1);
        }
    }
}
