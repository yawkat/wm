package at.yawk.wm.hl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import javax.inject.Singleton;
import org.slf4j.Logger;

/**
 * @author yawkat
 */
@Singleton
public class HerbstEventBus {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(HerbstEventBus.class);
    private final List<ShutdownEvent.Handler> shutdownEventHandlers = new ArrayList<>();
    private final List<TagEvent.Handler> tagEventHandlers = new ArrayList<>();
    private final List<TitleEvent.Handler> titleEventHandlers = new ArrayList<>();

    public void addShutdownEventHandler(ShutdownEvent.Handler handler) {
        shutdownEventHandlers.add(handler);
    }

    public void addTagEventHandler(TagEvent.Handler handler) {
        tagEventHandlers.add(handler);
    }

    public void addTitleEventHandlers(TitleEvent.Handler handler) {
        titleEventHandlers.add(handler);
    }

    void post(ShutdownEvent shutdownEvent) {
        forEach(shutdownEventHandlers, shutdownEvent, ShutdownEvent.Handler::handle);
    }

    void post(TagEvent tagEvent) {
        forEach(tagEventHandlers, tagEvent, TagEvent.Handler::handle);
    }

    void post(TitleEvent titleEvent) {
        forEach(titleEventHandlers, titleEvent, TitleEvent.Handler::handle);
    }

    private static <T, P> void forEach(Iterable<T> iterable, P parameter, BiConsumer<T, P> actions) {
        for (T t : iterable) {
            try {
                actions.accept(t, parameter);
            } catch (Throwable e) {
                log.error("Failed to pass event", e);
            }
        }
    }
}
