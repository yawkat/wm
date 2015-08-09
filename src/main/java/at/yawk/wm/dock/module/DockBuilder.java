package at.yawk.wm.dock.module;

import at.yawk.wm.Scheduler;
import at.yawk.wm.dock.*;
import at.yawk.wm.style.FontDescriptor;
import at.yawk.wm.style.FontManager;
import at.yawk.wm.x.GlobalResourceRegistry;
import at.yawk.wm.x.Screen;
import at.yawk.wm.x.Window;
import at.yawk.wm.x.font.GlyphFont;
import at.yawk.yarn.Component;
import java.lang.reflect.Method;
import java.util.*;
import javax.inject.Inject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yawkat
 */
@Component
@Slf4j
public class DockBuilder implements RenderElf {
    @Inject DockConfig config;
    @Inject Screen screen;
    @Inject GlobalResourceRegistry globalResourceRegistry;
    @Inject Scheduler scheduler;
    @Inject FontManager fontManager;

    private final Map<FontDescriptor, GlyphFont> fontStyleMap = new HashMap<>();
    private Dock dock;

    void start(DockBootstrap bootstrap) {
        log.info("Initializing dock...");

        dock = new Dock(screen, config.getBackground());
        globalResourceRegistry.register(dock);
        dock.setBounds(0, 0, screen.getWidth(), config.getHeight());

        setupWidgets(bootstrap);

        bootstrap.dockStartHandlers.forEach(java.lang.Runnable::run);

        dock.show();
        log.info("Dock initialized");
    }

    private void decorate(Widget widget) {
        if (widget instanceof TextWidget) {
            ((TextWidget) widget).setTextHeight(config.getHeight());
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
        PeriodBuilder periodBuilder = new PeriodBuilder(this);

        List<Widget> widgets = bootstrap.widgets;
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

            // hook methods
            for (Method method : widgetClass.getDeclaredMethods()) {
                method.setAccessible(true);

                // periodic tasks
                periodBuilder.scan(widget, method);
            }
        }

        periodBuilder.flush(scheduler);
    }

    public Window getWindow() {
        return dock.getWindow();
    }
}
