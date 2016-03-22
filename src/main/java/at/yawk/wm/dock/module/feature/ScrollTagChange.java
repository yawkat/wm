package at.yawk.wm.dock.module.feature;

import at.yawk.wm.dock.module.DockBuilder;
import at.yawk.wm.hl.HerbstClient;
import at.yawk.wm.x.event.Button;
import at.yawk.wm.x.event.ButtonPressEvent;
import javax.inject.Inject;

/**
 * @author yawkat
 */
public class ScrollTagChange {
    @Inject DockBuilder dockBuilder;
    @Inject HerbstClient herbstClient;

    public void listen() {
        dockBuilder.getWindow().addListener(ButtonPressEvent.class, evt -> {
            if (evt.contains(Button.SCROLL_UP)) {
                herbstClient.advanceTag(-1);
            }
            if (evt.contains(Button.SCROLL_DOWN)) {
                herbstClient.advanceTag(+1);
            }
        });
    }
}
