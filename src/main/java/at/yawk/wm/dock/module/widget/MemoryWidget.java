package at.yawk.wm.dock.module.widget;

import at.yawk.wm.dock.Direction;
import at.yawk.wm.dock.FlowCompositeWidget;
import at.yawk.wm.dock.TextWidget;
import at.yawk.wm.dock.module.*;
import at.yawk.yarn.Component;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.inject.Inject;

/**
 * @author yawkat
 */
@Component
@DockWidget(position = DockWidget.Position.RIGHT, priority = 99)
public class MemoryWidget extends FlowCompositeWidget {
    private static final Path MEMINFO_PATH = Paths.get("/proc/meminfo");

    @Inject DockConfig dockConfig;
    @Inject FontSource fontSource;

    private TextWidget ram;
    private TextWidget swap;

    {
        swap = new TextWidget();
        swap.after(getAnchor(), Direction.HORIZONTAL);
        addWidget(swap);

        ram = new TextWidget();
        ram.after(swap, Direction.HORIZONTAL);
        addWidget(ram);
    }

    @Periodic(value = 1, render = true)
    void update() throws IOException {
        long memTotal = 0;
        long memFree = 0;
        long buffers = 0;
        long cached = 0;
        long swapTotal = 0;
        long swapFree = 0;

        try (BufferedReader reader = Files.newBufferedReader(MEMINFO_PATH)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("MemTotal:")) {
                    memTotal = parseMem(line, 10);
                } else if (line.startsWith("MemFree:")) {
                    memFree = parseMem(line, 9);
                } else if (line.startsWith("Cached:")) {
                    cached = parseMem(line, 8);
                } else if (line.startsWith("Buffers:")) {
                    buffers = parseMem(line, 9);
                } else if (line.startsWith("SwapTotal:")) {
                    swapTotal = parseMem(line, 11);
                } else if (line.startsWith("SwapFree:")) {
                    swapFree = parseMem(line, 10);
                }
            }
        }

        float ramUse = memTotal == 0 ? 1 : (float) (memTotal - memFree - buffers - cached) / memTotal;
        float swapUse = swapTotal == 0 ? 1 : (float) (swapTotal - swapFree) / swapTotal;

        ram.setFont(fontSource.getFont(dockConfig.getMemoryFont().withColor(Shader.shade(
                dockConfig.getMemoryColorLow(),
                dockConfig.getMemoryColorHigh(),
                ramUse
        ))));
        ram.setText(CpuWidget.formatPercent(ramUse));
        swap.setFont(fontSource.getFont(dockConfig.getMemoryFont().withColor(Shader.shade(
                dockConfig.getSwapColorLow(),
                dockConfig.getSwapColorHigh(),
                swapUse
        ))));
        swap.setText(CpuWidget.formatPercent(swapUse));
    }

    private static long parseMem(String s, int off) {
        long amount = 0;
        for (int i = off; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= '0' && c <= '9') {
                amount *= 10;
                amount += c - '0';
            } else if (c == 'k') {
                amount *= 1024;
            } else if (c == 'm') {
                amount *= 1024 * 1024;
            } else if (c == 'g') {
                amount *= 1024 * 1024 * 1024;
            }
        }
        return amount;
    }
}
