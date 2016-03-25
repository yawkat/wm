package at.yawk.wm.dock.module.widget;

import at.yawk.wm.Util;
import at.yawk.wm.ui.TextWidget;
import at.yawk.wm.dock.module.DockConfig;
import at.yawk.wm.dock.module.DockWidget;
import at.yawk.wm.dock.module.FontSource;
import at.yawk.wm.dock.module.Periodic;
import at.yawk.wm.style.FontManager;
import at.yawk.wm.x.icon.IconManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import javax.inject.Inject;

/**
 * @author yawkat
 */
@DockWidget(position = DockWidget.Position.RIGHT, priority = 100)
public class CpuWidget extends TextWidget {
    private static final Path STAT_PATH = Paths.get("/proc/stat");

    @Inject DockConfig dockConfig;
    @Inject FontSource fontSource;
    @Inject FontManager fontManager;
    @Inject IconManager iconManager;

    private final MovingAverage cpuUsage = new MovingAverage(0.8);
    private long lastTime = 0;
    private long lastShares = 0;

    @Override
    public void init() {
        setIcon(iconManager.getIconOrNull(dockConfig.getCpuIcon()));
    }

    @Periodic(value = 1, render = true)
    void update() throws IOException {
        int cpuCount = 0;
        String firstLine;
        try (BufferedReader reader = Files.newBufferedReader(STAT_PATH)) {
            firstLine = reader.readLine();
            while (reader.readLine().startsWith("cpu")) {
                cpuCount++;
            }
        }

        List<String> items = Util.INSTANCE.split(firstLine, ' ', 4);
        long shares = Long.parseUnsignedLong(items.get(1)) +
                      Long.parseUnsignedLong(items.get(2)) +
                      Long.parseUnsignedLong(items.get(3));
        long now = System.currentTimeMillis();
        if (lastTime != 0) {
            long timeDelta = now - lastTime;
            if (timeDelta <= 0) { timeDelta = 1; } // prevent NaN
            long shareDelta = shares - lastShares;
            // shares are emitted at 100Hz, time at 1000Hz (1000ms/s) so we need to do *10
            double usage = shareDelta * 10. / timeDelta / cpuCount;
            cpuUsage.offer(usage);
        }
        lastShares = shares;
        lastTime = now;

        // one significant digit precision
        setText(formatPercent(cpuUsage.getAverage()));
        setFont(fontSource.getFont(fontManager.compute(dockConfig.getCpuTransition(),
                                                       (float) cpuUsage.getAverage())));
    }

    static String formatPercent(double number) {
        return String.format("%04.1f%%", number * 100);
    }

}
