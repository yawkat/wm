package at.yawk.wm.dock.module.widget;

import at.yawk.wm.di.PerMonitor;
import at.yawk.wm.ui.TextWidget;
import at.yawk.wm.dock.module.DockConfig;
import at.yawk.wm.dock.module.DockWidget;
import at.yawk.wm.dock.module.FontSource;
import at.yawk.wm.dock.module.Periodic;
import java.time.Clock;
import java.time.LocalDateTime;
import javax.inject.Inject;

/**
 * @author yawkat
 */
@PerMonitor
@DockWidget(position = DockWidget.Position.RIGHT, priority = 200)
public class ClockWidget extends TextWidget {
    private Clock clock;

    @Inject
    public ClockWidget() {}

    @Inject
    void setup(FontSource fontSource) {
        clock = Clock.systemDefaultZone();
        setFont(fontSource.getFont(DockConfig.INSTANCE.getClockFont()));
    }

    @Periodic(value = 1, render = true)
    void update() {
        setText(DockConfig.INSTANCE.getClockFormat().format(LocalDateTime.now(clock)));
    }
}
