package at.yawk.wm.dock.module;

import at.yawk.wm.PeriodBuilder;
import at.yawk.wm.Scheduler;
import at.yawk.wm.dock.Dock;
import at.yawk.wm.hl.Monitor;
import at.yawk.wm.style.FontManager;
import at.yawk.wm.ui.*;
import at.yawk.wm.x.GlobalResourceRegistry;
import at.yawk.wm.x.Screen;
import at.yawk.wm.x.Window;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.SneakyThrows;
import org.slf4j.Logger;

/**
 * @author yawkat
 */
@Singleton
public class DockBuilder implements RenderElf {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(DockBuilder.class);
    @Inject Monitor monitor;
    @Inject DockConfig config;
    @Inject Screen screen;
    @Inject GlobalResourceRegistry globalResourceRegistry;
    @Inject Scheduler scheduler;
    @Inject FontManager fontManager;
    @Inject PeriodBuilder periodBuilder;

    private Dock dock;

    void start(DockBootstrap bootstrap) {
        log.info("Initializing dock...");

        dock = new Dock(screen, config.getBackground());
        globalResourceRegistry.register(dock);
        dock.setBounds(monitor.getX(), monitor.getY(), monitor.getWidth(), config.getHeight());

        setupWidgets(bootstrap);

        bootstrap.getDockStartListeners().forEach(Runnable::run);

        dock.show();
        log.info("Dock initialized");
    }

    private void decorate(Widget widget) {
        if (widget instanceof TextWidget) {
            ((TextWidget) widget).setTextHeight(config.getHeight());
        } else if (widget instanceof IconWidget) {
            ((IconWidget) widget).setTargetHeight(config.getHeight());
        } else if (widget instanceof FlowCompositeWidget) {
            ((FlowCompositeWidget) widget).getWidgets().forEach(this::decorate);
        }
    }

    void addLeft(Widget widget) {
        widget.setOrigin(Origin.TOP_LEFT);
        decorate(widget);
        dock.getLeft().addWidget(widget);
    }

    void addRight(Widget widget) {
        widget.setOrigin(Origin.TOP_RIGHT);
        decorate(widget);
        dock.getRight().addWidget(widget);
    }

    @Override
    public void render() {
        if (dock != null) {
            dock.render();
        }
    }

    //////// SETUP ////////

    @SuppressWarnings("unchecked")
    @SneakyThrows // lazy
    private void setupWidgets(DockBootstrap bootstrap) {
        List<Widget> widgets = bootstrap.getWidgets();
        log.info("Visiting {} widgets...", widgets.size());
        Collections.sort(widgets, Comparator.comparingInt(
                w -> w.getClass().getAnnotation(DockWidget.class).priority()));
        for (Widget widget : widgets) {
            Class<? extends Widget> widgetClass = widget.getClass();
            DockWidget annotation = widgetClass.getAnnotation(DockWidget.class);

            DockWidget.Position position = annotation.position();
            if (position == DockWidget.Position.LEFT) {
                addLeft(widget);
            } else {
                addRight(widget);
            }

            widget.init();

            periodBuilder.scan(widget);
        }

        periodBuilder.flush();
    }

    public Window getWindow() {
        return dock.getWindow();
    }
}
