package at.yawk.wm.dock.module.widget;

import at.yawk.wm.style.FontStyle;
import at.yawk.wm.ui.Direction;
import at.yawk.wm.ui.FlowCompositeWidget;
import at.yawk.wm.ui.TextWidget;
import at.yawk.wm.dock.module.DockConfig;
import at.yawk.wm.dock.module.DockWidget;
import at.yawk.wm.dock.module.FontSource;
import at.yawk.wm.ui.RenderElf;
import at.yawk.wm.hl.HerbstClient;
import at.yawk.wm.hl.HerbstEventBus;
import at.yawk.wm.hl.Monitor;
import at.yawk.wm.hl.Tag;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

/**
 * @author yawkat
 */
@DockWidget(position = DockWidget.Position.LEFT)
public class TagListWidget extends FlowCompositeWidget {
    private final List<TextWidget> tagWidgets = new ArrayList<>();

    @Inject FontSource fontSource;
    @Inject HerbstClient herbstClient;
    @Inject RenderElf renderElf;
    @Inject HerbstEventBus herbstEventBus;
    @Inject Monitor monitor;

    @Override
    public void init() {
        update();
        herbstEventBus.addTagEventHandler(event -> update());
    }

    private void update() {
        int i = 0;
        List<Tag> tags = herbstClient.getTags(monitor);
        for (; i < tags.size(); i++) {
            Tag tag = tags.get(i);
            TextWidget widget;
            if (tagWidgets.size() > i) {
                widget = tagWidgets.get(i);
            } else {
                widget = new TextWidget();
                if (tagWidgets.isEmpty()) {
                    widget.after(getAnchor(), Direction.HORIZONTAL);
                } else {
                    widget.after(tagWidgets.get(tagWidgets.size() - 1), Direction.HORIZONTAL);
                }
                addWidget(widget);
                tagWidgets.add(widget);
            }

            FontStyle style;
            switch (tag.getState()) {
            case SELECTED:
                style = DockConfig.INSTANCE.getActiveFont();
                break;
            case SELECTED_ELSEWHERE:
                style = DockConfig.INSTANCE.getActiveElsewhereFont();
                break;
            case RUNNING:
            default:
                style = DockConfig.INSTANCE.getRunningFont();
                break;
            case EMPTY:
                style = DockConfig.INSTANCE.getEmptyFont();
                break;
            }

            widget.setFont(fontSource.getFont(style));
            widget.setText(tag.getId());
        }
        renderElf.render();
    }
}
