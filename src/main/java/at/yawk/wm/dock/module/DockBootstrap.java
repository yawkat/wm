package at.yawk.wm.dock.module;

import at.yawk.wm.dock.Widget;
import at.yawk.yarn.AcceptMethods;
import at.yawk.yarn.AnnotatedWith;
import at.yawk.yarn.Component;
import java.util.List;
import javax.inject.Inject;

/**
 * @author yawkat
 */
@Component
public class DockBootstrap {
    @AnnotatedWith(DockWidget.class)
    @Inject List<Widget> widgets;

    @AnnotatedWith(DockStart.class)
    @AcceptMethods
    @Inject List<Runnable> dockStartHandlers;

    @Inject
    void startDock(DockBuilder dock) {
        dock.start(this);
    }
}
