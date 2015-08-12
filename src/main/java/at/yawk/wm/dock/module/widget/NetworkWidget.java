package at.yawk.wm.dock.module.widget;

import at.yawk.wm.Util;
import at.yawk.wm.dbus.NetworkManager;
import at.yawk.wm.dock.Direction;
import at.yawk.wm.dock.FlowCompositeWidget;
import at.yawk.wm.dock.IconWidget;
import at.yawk.wm.dock.TextWidget;
import at.yawk.wm.dock.module.*;
import at.yawk.wm.style.FontManager;
import at.yawk.wm.x.icon.IconManager;
import at.yawk.yarn.Component;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import javax.inject.Inject;

/**
 * @author yawkat
 */
@Component
@DockWidget(position = DockWidget.Position.RIGHT, priority = 50)
public class NetworkWidget extends FlowCompositeWidget {
    @Inject IconManager iconManager;
    @Inject DockConfig config;
    @Inject NetworkManager networkManager;
    @Inject RenderElf renderElf;

    private TextWidget down;
    private IconWidget iconWidget;
    private TextWidget up;

    private final MovingAverage downAverage = new MovingAverage(0.8);
    private final MovingAverage upAverage = new MovingAverage(0.8);

    @Inject
    void init(FontSource fontSource, FontManager fontManager) {
        up = new TextWidget();
        up.setFont(fontSource.getFont(config.getNetUpFont()));
        up.after(getAnchor(), Direction.HORIZONTAL);
        addWidget(up);

        down = new TextWidget();
        down.setFont(fontSource.getFont(config.getNetDownFont()));
        down.after(up, Direction.HORIZONTAL);
        addWidget(down);

        iconWidget = new IconWidget();
        iconWidget.setColor(fontManager.resolve(config.getNetIconFont()));
        iconWidget.after(down, Direction.HORIZONTAL);
        addWidget(iconWidget);

        down.setPaddingLeft(2);
        up.setPaddingLeft(0);

        networkManager.onStateChanged(() -> {
            updateOnline();
            renderElf.render();
        });
    }

    @Periodic(value = 1, render = true)
    void update() throws IOException {
        Process process = new ProcessBuilder()
                .command("nstat", "-t", "1")
                .start();

        long downOctets = 0;
        long upOctets = 0;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("IpExtInOctets")) {
                    downOctets = Long.parseUnsignedLong(Util.split(line, ' ', 2).get(1));
                } else if (line.startsWith("IpExtOutOctets")) {
                    upOctets = Long.parseUnsignedLong(Util.split(line, ' ', 2).get(1));
                }
            }
        }

        downAverage.offer(downOctets);
        upAverage.offer(upOctets);

        down.setText(format(downAverage.getAverage()));
        up.setText(format(upAverage.getAverage()));
    }

    @Periodic(value = 30, render = true)
    void updateOnline() {
        boolean online = networkManager.getConnectivity() > 1;
        iconWidget.setIcon(iconManager.getIconOrNull(online ? config.getNetIconOnline() : config.getNetIconOffline()));
    }

    private static String format(double traffic) {
        int exp = 0;
        while (traffic >= 1000) {
            traffic /= 1024;
            exp++;
        }
        char unit;
        switch (exp) {
        case 0:
            unit = 'B';
            break;
        case 1:
            unit = 'K';
            break;
        case 2:
            unit = 'M';
            break;
        case 3:
            unit = 'G';
            break;
        case 4:
            unit = 'T';
            break;
        default:
            unit = '?';
            break;
        }
        return String.format("%05.1f%s", traffic, unit);
    }
}
