package at.yawk.wm.dock.module.feature;

import at.yawk.wm.dock.module.DockConfig;
import at.yawk.wm.dock.module.DockStart;
import at.yawk.wm.hl.HerbstClient;
import at.yawk.wm.hl.Monitor;
import javax.inject.Inject;

/**
 * @author yawkat
 */
public class DockPadFeature {
    @Inject HerbstClient herbstClient;
    @Inject DockConfig dockConfig;
    @Inject Monitor monitor;

    @DockStart
    public void init() {
        herbstClient.pad(monitor, dockConfig.getHeight());
    }
}
