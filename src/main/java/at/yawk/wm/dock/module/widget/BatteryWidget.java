package at.yawk.wm.dock.module.widget;

import at.yawk.wm.dock.Direction;
import at.yawk.wm.dock.FlowCompositeWidget;
import at.yawk.wm.dock.TextWidget;
import at.yawk.wm.dock.module.*;
import at.yawk.wm.x.font.FontStyle;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;

/**
 * @author yawkat
 */
@DockWidget(position = DockWidget.Position.RIGHT)
public class BatteryWidget extends FlowCompositeWidget {
    private static final Pattern PATTERN_DURATION =
            Pattern.compile("\\s*time to empty:\\s*([\\d,\\.]*) (hours|minutes|seconds)");
    private static final Pattern PATTERN_PERCENTAGE =
            Pattern.compile("\\s*percentage:\\s*(\\d*)%");

    @Inject DockConfig config;
    @Inject FontSource fontSource;

    private final List<TextWidget> widgets = new ArrayList<>();

    @Periodic(1)
        // todo
    void updateBattery() throws IOException {
        int i = 0;
        Process process = new ProcessBuilder()
                .command("upower", "-d")
                .start();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String text;
                FontStyle style;

                Matcher durationMatcher = PATTERN_DURATION.matcher(line);
                if (durationMatcher.matches()) {
                    float durationF = Float.parseFloat(durationMatcher.group(1).replace(',', '.'));
                    switch (durationMatcher.group(2)) {
                    case "hours":
                        durationF *= 60;
                        // FALL-THROUGH
                    case "minutes":
                        durationF *= 60;
                    }

                    int duration = Math.round(durationF);
                    int minutes = (duration / 60) % 60;
                    int hours = (duration / 60 / 60) % 60;

                    StringBuilder builder = new StringBuilder();
                    if (hours > 0) { builder.append(hours).append('h'); }
                    builder.append(minutes).append('m');
                    text = builder.toString();
                    style = config.getBatteryTime();
                } else {

                    Matcher percentageMatcher = PATTERN_PERCENTAGE.matcher(line);
                    if (percentageMatcher.matches()) {
                        int percentage = Integer.parseInt(percentageMatcher.group(1));
                        style = config.getBatteryPercentage().withColor(Shader.shade(
                                config.getBatteryPercentageColorLow(),
                                config.getBatteryPercentageColorHigh(),
                                percentage / 100F
                        ));
                        text = percentage + "%";
                    } else {
                        continue;
                    }
                }

                TextWidget widget;
                if (i >= widgets.size()) {
                    widget = new TextWidget();
                    if (widgets.isEmpty()) {
                        widget.after(getAnchor(), Direction.HORIZONTAL);
                    } else {
                        widget.after(widgets.get(widgets.size() - 1), Direction.HORIZONTAL);
                    }
                    widgets.add(widget);
                    addWidget(widget);
                } else {
                    widget = widgets.get(i);
                }

                widget.setText(text);
                widget.setFont(fontSource.getFont(style));

                i++;
            }
        }

        // remove newly unused widgets
        List<TextWidget> toRemove = widgets.subList(i, widgets.size());
        toRemove.forEach(this::removeWidget);
        toRemove.clear();
    }
}
