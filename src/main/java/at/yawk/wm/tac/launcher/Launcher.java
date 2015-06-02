package at.yawk.wm.tac.launcher;

import at.yawk.wm.Config;
import at.yawk.wm.tac.TacUI;
import at.yawk.wm.x.XcbConnector;
import at.yawk.yarn.Component;
import java.util.Arrays;
import javax.inject.Inject;

/**
 * @author yawkat
 */
@Component
public class Launcher {
    @Inject Config config;
    @Inject XcbConnector connector;

    public void open() {
        TacUI ui = new TacUI(
                config, connector, /*connector.getScreen().getWidth()*/0, config.getDock().getHeight(), 200);
        ui.setEntries(Arrays.asList(
                new LauncherEntry("xfce4-terminal", "Terminal", true),
                new LauncherEntry("firefox", "Firefox", true)
        ));
    }
}
