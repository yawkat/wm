package at.yawk.wm.dock.module.feature;

import at.yawk.wm.dock.module.DockBuilder;
import at.yawk.wm.dock.module.DockStart;
import at.yawk.wm.hl.HerbstClient;
import at.yawk.wm.x.event.Button;
import at.yawk.wm.x.event.ButtonPressEvent;
import at.yawk.yarn.Component;
import javax.inject.Inject;

/**
 * @author yawkat
 */
@Component
public class ScrollTagChange {
    @Inject DockBuilder dockBuilder;
    @Inject HerbstClient herbstClient;

    @DockStart
    public void listen() {
        dockBuilder.addWindowListener(
                ButtonPressEvent.class,
                evt -> {
                    if (evt.contains(Button.SCROLL_UP)) {
                        herbstClient.advanceTag(-1);
                    }
                    if (evt.contains(Button.SCROLL_DOWN)) {
                        herbstClient.advanceTag(+1);
                    }
                }
        );
    }
}
