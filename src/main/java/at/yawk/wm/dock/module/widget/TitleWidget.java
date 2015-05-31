package at.yawk.wm.dock.module.widget;

import at.yawk.wm.dock.TextWidget;
import at.yawk.wm.dock.module.DockConfig;
import at.yawk.wm.dock.module.DockWidget;
import at.yawk.wm.dock.module.FontSource;
import at.yawk.wm.hl.Subscribe;
import at.yawk.wm.hl.TitleEvent;
import at.yawk.yarn.Component;
import javax.inject.Inject;

/**
 * @author yawkat
 */
@Component
@DockWidget(position = DockWidget.Position.LEFT, priority = 100)
public class TitleWidget extends TextWidget {
    {
        setZ(-1000);
    }

    @Inject
    void init(DockConfig config, FontSource fontSource) {
        setFont(fontSource.getFont(config.getWindowTitleFont()));
    }

    @Subscribe
    public void onTagEvent(TitleEvent event) {
        setText(event.getTitle());
    }
}
