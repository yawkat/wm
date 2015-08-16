package at.yawk.wm.dock.module.widget;

import at.yawk.wm.dbus.Power;
import at.yawk.wm.dock.*;
import at.yawk.wm.dock.module.*;
import at.yawk.wm.style.FontDescriptor;
import at.yawk.wm.style.FontManager;
import at.yawk.wm.x.icon.Icon;
import at.yawk.wm.x.icon.IconDescriptor;
import at.yawk.wm.x.icon.IconManager;
import at.yawk.yarn.Component;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.Executor;
import javax.inject.Inject;
import lombok.Data;

/**
 * @author yawkat
 */
@Component
@DockWidget(position = DockWidget.Position.RIGHT, priority = -100)
public class BatteryWidget extends FlowCompositeWidget {
    @Inject DockConfig config;
    @Inject FontSource fontSource;
    @Inject FontManager fontManager;
    @Inject IconManager iconManager;
    @Inject Power power;

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

    @Inject
    void listen(Executor executor, RenderElf renderElf) {
        power.onPropertiesChanged(() -> executor.execute(() -> {
            updateBattery();
            renderElf.render();
        }));
    }

    @Periodic(30)
    synchronized void updateBattery() {
        List<BatteryState> batteries = new ArrayList<>();

        BatteryState state = new BatteryState();
        state.setCharge((float) (power.getPercentage() / 100));
        boolean charging;
        switch (power.getState()) {
        case 1:
        case 4:
            charging = true;
            break;
        default:
            charging = false;
            break;
        }
        if (charging) {
            state.setCharging(true);
            state.setRemaining(Duration.ofSeconds(power.getTimeToFull()));
        } else {
            state.setCharging(false);
            state.setRemaining(Duration.ofSeconds(power.getTimeToEmpty()));
        }
        batteries.add(state);

        Iterator<DeviceHolder> deviceIterator = devices.iterator();
        for (BatteryState battery : batteries) {
            DeviceHolder holder;
            if (deviceIterator != null && deviceIterator.hasNext()) {
                holder = deviceIterator.next();
            } else {
                holder = new DeviceHolder();
                devices.add(holder);
                deviceIterator = null;
            }
            holder.updateState(battery);
        }

        while (deviceIterator != null && deviceIterator.hasNext()) {
            deviceIterator.next().free();
            deviceIterator.remove();
        }
    }

    @Data
    private static final class BatteryState {
        private float charge;
        private boolean charging;
        private Duration remaining;
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
