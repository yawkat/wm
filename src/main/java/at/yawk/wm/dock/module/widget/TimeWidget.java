package at.yawk.wm.dock.module.widget;

import at.yawk.wm.dock.TextWidget;
import at.yawk.wm.dock.module.DockConfig;
import at.yawk.wm.dock.module.DockWidget;
import at.yawk.wm.dock.module.FontSource;
import at.yawk.wm.dock.module.Periodic;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * @author yawkat
 */
@DockWidget(position = DockWidget.Position.RIGHT)
public class TimeWidget extends TextWidget {
    private static final DateTimeFormatter format = new DateTimeFormatterBuilder()
            .appendValue(ChronoField.YEAR, 4)
            .appendLiteral('-')
            .appendValue(ChronoField.MONTH_OF_YEAR, 2)
            .appendLiteral('-')
            .appendValue(ChronoField.DAY_OF_MONTH, 2)
            .appendLiteral(' ')
            .appendValue(ChronoField.HOUR_OF_DAY, 2)
            .appendLiteral(':')
            .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
            .appendLiteral(':')
            .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
            .toFormatter();

    @Inject DockConfig dockConfig;
    @Inject FontSource fontSource;

    @PostConstruct
    private void setupFont() {
        setFont(fontSource.getFont(dockConfig.getTime()));
    }

    @Periodic(value = 1, render = true)
    void updateTime() {
        setText(format.format(LocalDateTime.now()));
    }
}
