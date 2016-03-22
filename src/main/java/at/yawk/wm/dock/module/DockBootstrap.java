package at.yawk.wm.dock.module;

import at.yawk.wm.dock.Widget;
import at.yawk.wm.dock.module.feature.DockPadFeature;
import at.yawk.wm.dock.module.feature.ScrollTagChange;
import at.yawk.wm.dock.module.widget.*;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;

/**
 * @author yawkat
 */
public class DockBootstrap {
    @Inject DockBuilder dock;

    @Inject BatteryWidget batteryWidget;
    @Inject ClockWidget clockWidget;
    @Inject CpuWidget cpuWidget;
    @Inject MediaWidget mediaWidget;
    @Inject MemoryWidget memoryWidget;
    @Inject NetworkWidget networkWidget;
    @Inject ProgressWidget progressWidget;
    @Inject TagListWidget tagListWidget;
    @Inject TitleWidget titleWidget;

    @Inject DockPadFeature dockPadFeature;
    @Inject ScrollTagChange scrollTagChange;

    public List<Widget> getWidgets() {
        return Arrays.asList(
                batteryWidget,
                clockWidget,
                cpuWidget,
                mediaWidget,
                memoryWidget,
                networkWidget,
                progressWidget,
                tagListWidget,
                titleWidget
        );
    }

    public List<Runnable> getDockStartListeners() {
        return Arrays.asList(
                dockPadFeature::init,
                scrollTagChange::listen
        );
    }

    public void startDock() {
        dock.start(this);
    }
}
