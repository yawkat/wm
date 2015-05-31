package at.yawk.wm.hl;

import at.yawk.yarn.AcceptMethods;
import at.yawk.yarn.AnnotatedWith;
import at.yawk.yarn.Component;
import java.util.List;
import javax.inject.Inject;

/**
 * @author yawkat
 */
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
        for (ShutdownEvent.Handler handler : shutdownEventHandlers) {
            handler.handle(shutdownEvent);
        }
    }

    void post(TagEvent tagEvent) {
        for (TagEvent.Handler handler : tagEventHandlers) {
            handler.handle(tagEvent);
        }
    }

    void post(TitleEvent titleEvent) {
        for (TitleEvent.Handler handler : titleEventHandlers) {
            handler.handle(titleEvent);
        }
    }
}
