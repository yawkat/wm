package at.yawk.wm.dock.module.widget;

import at.yawk.wm.ui.TextWidget;
import at.yawk.wm.dock.module.DockConfig;
import at.yawk.wm.dock.module.DockWidget;
import at.yawk.wm.dock.module.FontSource;
import at.yawk.wm.dock.module.Periodic;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.inject.Inject;

/**
 * @author yawkat
 */
@DockWidget(position = DockWidget.Position.RIGHT, priority = 200)
public class ClockWidget extends TextWidget {
    private DateTimeFormatter formatter;
    private Clock clock;

    @Inject
    void setup(DockConfig config, FontSource fontSource) {
        formatter = DateTimeFormatter.ofPattern(config.getClockFormat());
        clock = Clock.systemDefaultZone();
        setFont(fontSource.getFont(config.getClockFont()));
    }

    @Periodic(value = 1, render = true)
    void update() {
        setText(formatter.format(LocalDateTime.now(clock)));
    }
}
