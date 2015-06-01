package at.yawk.wm.dock.module.feature;

import at.yawk.wm.dock.module.DockConfig;
import at.yawk.wm.dock.module.DockStart;
import at.yawk.wm.hl.HerbstClient;
import at.yawk.yarn.Component;
import javax.inject.Inject;

/**
 * @author yawkat
 */
@Component
public class DockPadFeature {
    @Inject HerbstClient herbstClient;
    @Inject DockConfig dockConfig;

    @DockStart
    public void init() {
        herbstClient.pad(dockConfig.getHeight());
    }
}
