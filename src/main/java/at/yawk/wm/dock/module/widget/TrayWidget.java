package at.yawk.wm.dock.module.widget;

import at.yawk.wm.dock.Widget;
import at.yawk.wm.dock.module.DockBuilder;
import at.yawk.wm.dock.module.DockStart;
import at.yawk.wm.dock.module.DockWidget;
import at.yawk.wm.x.Graphics;
import at.yawk.wm.x.TrayServer;
import at.yawk.wm.x.XcbConnector;
import at.yawk.yarn.Component;
import javax.inject.Inject;

/**
 * @author yawkat
 */
//@Component
//@DockWidget(position = DockWidget.Position.RIGHT, priority = 300)
public class TrayWidget extends Widget {
    @Inject DockBuilder builder;

    @DockStart
    public void start() {
        TrayServer trayServer = builder.getWindow().createTrayServer();
    }

    @Override
    protected void layout(Graphics graphics) {
        
    }
}
