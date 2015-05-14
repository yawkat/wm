package at.yawk.wm.dock.module;

import at.yawk.wm.Config;
import at.yawk.wm.dock.Dock;
import at.yawk.wm.dock.Origin;
import at.yawk.wm.dock.TextWidget;
import at.yawk.wm.dock.Widget;
import at.yawk.wm.dock.module.widget.BatteryWidget;
import at.yawk.wm.dock.module.widget.TimeWidget;
import at.yawk.wm.x.GlobalResourceRegistry;
import at.yawk.wm.x.Screen;
import at.yawk.wm.x.font.ConfiguredFont;
import at.yawk.wm.x.font.FontStyle;
import at.yawk.wm.x.font.GlyphFont;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import lombok.SneakyThrows;

/**
 * @author yawkat
 */
public class DockBuilder implements FontSource, RenderElf {
    @Inject Config config;
    @Inject Screen screen;
    @Inject GlobalResourceRegistry globalResourceRegistry;
    @Inject ScheduledExecutorService scheduler;

    private final Map<FontStyle, GlyphFont> fontStyleMap = new HashMap<>();
    private DockConfig dockConfig;
    private Dock dock;

    public void start() {
        dockConfig = config.getDock();
        dock = new Dock(screen, dockConfig.getBackground());
        globalResourceRegistry.register(dock);
        dock.setBounds(0, 20, screen.getWidth(), dockConfig.getHeight());

        setupWidgets();

        dock.show();
    }

    private void decorate(Widget widget) {
        if (widget instanceof TextWidget) {
            ((TextWidget) widget).setTextHeight(dockConfig.getHeight());
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
                              config.getCacheDir()));
    }

    @Override
    public void render() {
        dock.render();
    }

    //////// SETUP ////////

    @SuppressWarnings("unchecked")
    @SneakyThrows // lazy
    private void setupWidgets() {
        PeriodBuilder periodBuilder = new PeriodBuilder(this);

        for (Widget widget : new Widget[]{
                new TimeWidget(),
                new BatteryWidget()
        }) {
            Class<? extends Widget> widgetClass = widget.getClass();
            DockWidget annotation = widgetClass.getAnnotation(DockWidget.class);
            if (annotation == null) { continue; }

            DockWidget.Position position = annotation.position();
            if (position == DockWidget.Position.LEFT) {
                addLeft(widget);
            } else {
                addRight(widget);
            }

            // dependency injection
            for (Field field : widgetClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
                    field.setAccessible(true);
                    Class<?> type = field.getType();
                    Object v;
                    if (type == Config.class) {
                        v = config;
                    } else if (type == DockConfig.class) {
                        v = config.getDock();
                    } else if (type == FontSource.class) {
                        v = this;
                    } else if (type == RenderElf.class) {
                        v = this;
                    } else {
                        throw new AssertionError("Cannot inject field of type " + type.getName());
                    }
                    field.set(widget, v);
                }
            }

            // hook methods
            for (Method method : widgetClass.getDeclaredMethods()) {
                method.setAccessible(true);

                // post construct
                if (method.isAnnotationPresent(PostConstruct.class)) {
                    method.invoke(widget);
                }

                // periodic tasks
                periodBuilder.scan(widget, method);
            }
        }

        periodBuilder.flush(scheduler);
    }
}
