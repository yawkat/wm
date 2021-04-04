package at.yawk.wm.dock.module.widget;

import at.yawk.wm.ui.TextWidget;
import at.yawk.wm.dock.module.DockConfig;
import at.yawk.wm.dock.module.DockWidget;
import at.yawk.wm.dock.module.FontSource;
import at.yawk.wm.hl.HerbstEventBus;
import javax.inject.Inject;

/**
 * @author yawkat
 */
@DockWidget(position = DockWidget.Position.LEFT, priority = 100)
public class TitleWidget extends TextWidget {
    @Inject HerbstEventBus eventBus;

    {
        setZ(-1000);
    }

    @Override
    public void init() {
        eventBus.addTitleEventHandlers(event -> setText(event.getTitle()));
    }

    @Inject
    void init(FontSource fontSource) {
        setFont(fontSource.getFont(DockConfig.INSTANCE.getWindowTitleFont()));
    }

}
