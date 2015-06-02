package at.yawk.wm.dock.module;

import at.yawk.wm.Config;
import at.yawk.wm.dock.Dock;
import at.yawk.wm.dock.Origin;
import at.yawk.wm.dock.TextWidget;
import at.yawk.wm.dock.Widget;
import at.yawk.wm.x.GlobalResourceRegistry;
import at.yawk.wm.x.Screen;
import at.yawk.wm.x.font.ConfiguredFont;
import at.yawk.wm.x.font.FontStyle;
import at.yawk.wm.x.font.GlyphFont;
import at.yawk.yarn.AcceptMethods;
import at.yawk.yarn.AnnotatedWith;
import at.yawk.yarn.Component;
import at.yawk.yarn.Provides;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;
import javax.inject.Inject;
import javax.inject.Provider;
import lombok.SneakyThrows;

/**
 * @author yawkat
 */
@Component
public class DockBuilder implements FontSource, RenderElf {
    @Inject Config config;
    @Inject Screen screen;
    @Inject GlobalResourceRegistry globalResourceRegistry;
    @Inject ScheduledExecutorService scheduler;

    @AnnotatedWith(DockWidget.class)
    @Inject Provider<List<Widget>> widgets;
    @AnnotatedWith(DockStart.class)
    @AcceptMethods
    @Inject Provider<List<Runnable>> dockStartHandlers;

    private final Map<FontStyle, GlyphFont> fontStyleMap = new HashMap<>();
    private Dock dock;

    @Provides
    DockConfig dockConfig() {
        return config.getDock();
    }

    public void start() {
        dock = new Dock(screen, dockConfig().getBackground());
        globalResourceRegistry.register(dock);
        dock.setBounds(0, 0, screen.getWidth(), dockConfig().getHeight());

        setupWidgets();

        dockStartHandlers.get().forEach(java.lang.Runnable::run);

        dock.show();
    }

    private void decorate(Widget widget) {
        if (widget instanceof TextWidget) {
            ((TextWidget) widget).setTextHeight(dockConfig().getHeight());
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
    public GlyphFont getFont(FontStyle style) {
        return fontStyleMap.computeIfAbsent(style, s ->
                new GlyphFont(new ConfiguredFont(s, config.getDock().getBackground(), config.getFont()),
                              config.getFontCacheDir()));
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
    private void setupWidgets() {
        PeriodBuilder periodBuilder = new PeriodBuilder(this);

        List<Widget> widgetList = widgets.get();
        Collections.sort(widgetList, Comparator.comparingInt(
                w -> w.getClass().getAnnotation(DockWidget.class).priority()));
        for (Widget widget : widgetList) {
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

    public <E> void addWindowListener(Class<E> eventType, Consumer<E> handler) {
        dock.getWindow().addListener(eventType, handler);
    }
}
