package at.yawk.wm.dock.module.widget;

import at.yawk.wm.dock.Direction;
import at.yawk.wm.dock.FlowCompositeWidget;
import at.yawk.wm.dock.TextWidget;
import at.yawk.wm.dock.module.DockConfig;
import at.yawk.wm.dock.module.DockWidget;
import at.yawk.wm.dock.module.FontSource;
import at.yawk.wm.dock.module.RenderElf;
import at.yawk.wm.hl.HerbstClient;
import at.yawk.wm.hl.Subscribe;
import at.yawk.wm.hl.Tag;
import at.yawk.wm.hl.TagEvent;
import at.yawk.wm.x.font.FontStyle;
import at.yawk.yarn.Component;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * @author yawkat
 */
@Component
@DockWidget(position = DockWidget.Position.LEFT)
public class TagListWidget extends FlowCompositeWidget {
    private final List<TextWidget> tagWidgets = new ArrayList<>();

    @Inject DockConfig config;
    @Inject FontSource fontSource;
    @Inject HerbstClient herbstClient;
    @Inject RenderElf renderElf;

    @PostConstruct
    void init() {
        update();
    }

    @Subscribe
    public void onTagEvent(TagEvent event) {
        update();
    }

    private void update() {
        int i = 0;
        List<Tag> tags = herbstClient.getTags();
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
                style = config.getActiveFont();
                break;
            case RUNNING:
            default:
                style = config.getRunningFont();
                break;
            case EMPTY:
                style = config.getEmptyFont();
                break;
            }

            widget.setFont(fontSource.getFont(style));
            widget.setText(tag.getId());
        }
        renderElf.render();
    }
}
