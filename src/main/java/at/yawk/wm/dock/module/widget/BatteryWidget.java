package at.yawk.wm.dock.module.widget;

import at.yawk.wm.dock.Direction;
import at.yawk.wm.dock.FlowCompositeWidget;
import at.yawk.wm.dock.TextWidget;
import at.yawk.wm.dock.module.*;
import at.yawk.wm.x.font.FontStyle;
import at.yawk.yarn.Component;
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
@Component
@DockWidget(position = DockWidget.Position.RIGHT, priority = -100)
public class BatteryWidget extends FlowCompositeWidget {
    private static final Pattern PATTERN_DEVICE =
            Pattern.compile("Device: (.*)");
    private static final Pattern PATTERN_DURATION =
            Pattern.compile("\\s*time to (empty|full):\\s*([\\d,\\.]*) (hours|minutes|seconds)");
    private static final Pattern PATTERN_PERCENTAGE =
            Pattern.compile("\\s*percentage:\\s*(\\d*)%");

    @Inject DockConfig config;
    @Inject FontSource fontSource;

    private final List<TextWidget> widgets = new ArrayList<>();

    @Periodic(20)
    void updateBattery() throws IOException {
        int i = 0;
        Process process = new ProcessBuilder()
                .command("upower", "-d")
                .start();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {

            boolean showCurrentDevice = true;

            String line;
            while ((line = reader.readLine()) != null) {
                String text;
                FontStyle style;

                Matcher deviceMatcher = PATTERN_DEVICE.matcher(line);
                if (deviceMatcher.matches()) {
                    showCurrentDevice = !deviceMatcher.group(1).contains("DisplayDevice");
                    continue;
                }

                if (!showCurrentDevice) {
                    continue;
                }

                Matcher durationMatcher = PATTERN_DURATION.matcher(line);
                if (durationMatcher.matches()) {
                    float durationF = Float.parseFloat(durationMatcher.group(2).replace(',', '.'));
                    switch (durationMatcher.group(3)) {
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
                    builder.append(durationMatcher.group(1).equals("empty") ? '\u2193' : '\u2191');
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
