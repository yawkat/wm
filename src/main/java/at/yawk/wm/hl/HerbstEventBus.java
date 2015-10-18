package at.yawk.wm.hl;

import at.yawk.yarn.AcceptMethods;
import at.yawk.yarn.AnnotatedWith;
import at.yawk.yarn.Component;
import java.util.List;
import java.util.function.BiConsumer;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yawkat
 */
@Slf4j
@Component
public class HerbstEventBus {
    @AnnotatedWith(Subscribe.class)
    @AcceptMethods
    @Inject
    List<ShutdownEvent.Handler> shutdownEventHandlers;

    @AnnotatedWith(Subscribe.class)
    @AcceptMethods
    @Inject
    List<TagEvent.Handler> tagEventHandlers;

    @AnnotatedWith(Subscribe.class)
    @AcceptMethods
    @Inject
    List<TitleEvent.Handler> titleEventHandlers;

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
