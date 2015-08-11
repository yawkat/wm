package at.yawk.wm.dock.module.widget;

import at.yawk.wm.dock.Direction;
import at.yawk.wm.dock.FlowCompositeWidget;
import at.yawk.wm.dock.TextWidget;
import at.yawk.wm.dock.Widget;
import at.yawk.wm.dock.module.DockConfig;
import at.yawk.wm.dock.module.DockWidget;
import at.yawk.wm.dock.module.FontSource;
import at.yawk.wm.dock.module.Periodic;
import at.yawk.wm.style.FontDescriptor;
import at.yawk.wm.style.FontManager;
import at.yawk.wm.x.icon.Icon;
import at.yawk.wm.x.icon.IconDescriptor;
import at.yawk.wm.x.icon.IconManager;
import at.yawk.yarn.Component;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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
    @Inject FontManager fontManager;
    @Inject IconManager iconManager;

    private Icon findChargeIcon(boolean charging, float charge) {
        Map<Float, IconDescriptor> iconSet =
                charging ? config.getChargingIcons() : config.getDischargingIcons();
        IconDescriptor descriptor = iconSet.entrySet().stream()
                // sort by distance to current charge
                .sorted(Comparator.comparingDouble(e -> Math.abs(e.getKey() - charge)))
                .findFirst().get().getValue();
        return iconManager.getIcon(descriptor);
    }

    @Periodic(20)
    void updateBattery() throws IOException {
        int i = 0;
        Process process = new ProcessBuilder()
                .command("upower", "-d")
                .start();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {

            boolean showCurrentDevice = true;
            // just hope that the percentage appears before the time
            float charge = 0;

            String line;
            while ((line = reader.readLine()) != null) {
                Icon icon = null;
                String text;
                FontDescriptor style;

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
                    boolean charging = !durationMatcher.group(1).equals("empty");
                    icon = findChargeIcon(charging, charge);

                    text = builder.toString();
                    style = config.getBatteryTime();
                } else {
                    Matcher percentageMatcher = PATTERN_PERCENTAGE.matcher(line);
                    if (percentageMatcher.matches()) {
                        int percentage = Integer.parseInt(percentageMatcher.group(1));
                        charge = percentage / 100F;
                        style = fontManager.compute(config.getBatteryTransition(), charge);
                        text = percentage + "%";
                    } else {
                        continue;
                    }
                }

                TextWidget widget;
                if (i >= getWidgets().size()) {
                    widget = new TextWidget();
                    widget.setTextHeight(config.getHeight());
                    if (getWidgets().isEmpty()) {
                        widget.after(getAnchor(), Direction.HORIZONTAL);
                    } else {
                        widget.after(getWidgets().get(getWidgets().size() - 1), Direction.HORIZONTAL);
                    }
                    addWidget(widget);
                } else {
                    widget = (TextWidget) getWidgets().get(i);
                }

                widget.setText(text);
                widget.setFont(fontSource.getFont(style));
                widget.setIcon(icon);
                widget.setPaddingLeft(icon == null ? 4 : 0);

                i++;
            }
        }

        // remove newly unused widgets
        if (i < getWidgets().size()) {
            List<Widget> toRemove = new ArrayList<>(getWidgets().subList(i, getWidgets().size()));
            toRemove.forEach(this::removeWidget);
        }
    }
}
