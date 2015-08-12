package at.yawk.wm.dock.module.widget;

import at.yawk.wm.dock.*;
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
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import lombok.Data;

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

    private List<DeviceHolder> devices = new ArrayList<>();

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
        List<BatteryState> batteries = new ArrayList<>();

        Process process = new ProcessBuilder()
                .command("upower", "-d")
                .start();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {

            BatteryState currentBattery = null;

            String line;
            while ((line = reader.readLine()) != null) {
                Matcher deviceMatcher = PATTERN_DEVICE.matcher(line);
                if (deviceMatcher.matches()) {
                    if (currentBattery != null) {
                        batteries.add(currentBattery);
                        currentBattery = null;
                    }
                    if (!deviceMatcher.group(1).contains("DisplayDevice")) {
                        currentBattery = new BatteryState();
                    }
                    continue;
                }

                if (currentBattery == null) {
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

                    currentBattery.setRemaining(Duration.ofSeconds(Math.round(durationF)));
                    currentBattery.setCharging(!durationMatcher.group(1).equals("empty"));
                } else {
                    Matcher percentageMatcher = PATTERN_PERCENTAGE.matcher(line);
                    if (percentageMatcher.matches()) {
                        int percentage = Integer.parseInt(percentageMatcher.group(1));
                        currentBattery.setCharge(percentage / 100F);
                    } else {
                        continue;
                    }
                }
            }

            if (currentBattery != null) {
                batteries.add(currentBattery);
            }
        }

        Iterator<DeviceHolder> deviceIterator = devices.iterator();
        for (BatteryState battery : batteries) {
            DeviceHolder holder = deviceIterator.hasNext() ?
                    deviceIterator.next() : new DeviceHolder();
            holder.updateState(battery);
        }

        while (deviceIterator.hasNext()) {
            deviceIterator.next().free();
            deviceIterator.remove();
        }
    }

    @Data
    private static final class BatteryState {
        private float charge;
        private boolean charging = true; // if we get no duration info, we're charging at 100%
        private Duration remaining = Duration.ZERO;
    }

    private class DeviceHolder {
        final TextWidget percentage;
        final TextWidget duration;
        final IconWidget icon;

        {
            duration = new TextWidget();
            duration.setFont(fontSource.getFont(config.getBatteryTime()));
            Positioned anchor = devices.isEmpty() ?
                    getAnchor() : devices.get(devices.size() - 1).icon;
            duration.after(anchor, Direction.HORIZONTAL);
            duration.setTextHeight(config.getHeight());
            duration.setPaddingLeft(0);

            percentage = new TextWidget();
            percentage.after(duration, Direction.HORIZONTAL);
            percentage.setTextHeight(config.getHeight());
            percentage.setPaddingLeft(0);

            icon = new IconWidget();
            icon.setColor(fontManager.resolve(config.getBatteryTime()));
            icon.after(percentage, Direction.HORIZONTAL);
            icon.setTargetHeight(config.getHeight());

            addWidget(duration);
            addWidget(percentage);
            addWidget(icon);
        }

        void updateState(BatteryState state) {
            percentage.setText(Math.round(state.getCharge() * 100) + "%");
            FontDescriptor transition = fontManager.compute(config.getBatteryTransition(), state.getCharge());
            percentage.setFont(fontSource.getFont(transition));

            StringBuilder durationStr = new StringBuilder();
            long seconds = state.getRemaining().getSeconds();
            if (seconds >= 60 * 60) {
                durationStr.append(seconds / (60 * 60)).append('h');
                seconds %= 60 * 60;
            }
            if (seconds >= 60) {
                durationStr.append(seconds / 60).append('m');
            }
            duration.setText(durationStr.toString());

            icon.setIcon(findChargeIcon(state.isCharging(), state.getCharge()));
        }

        void free() {
            removeWidget(duration);
            removeWidget(percentage);
            removeWidget(icon);
        }
    }
}
