package at.yawk.wm.x;

import at.yawk.wm.x.event.ExposeEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.freedesktop.xcb.*;

/**
 * @author yawkat
 */
@RequiredArgsConstructor
class EventManager implements Runnable {
    private static final Map<Integer, String> ERROR_MESSAGES = new HashMap<Integer, String>() {{
        put(16, "Expected different data length from arguments");
    }};

    private final SWIGTYPE_p_xcb_connection_t connection;

    private final Map<Class<?>, Map<Context, List<Consumer<?>>>> eventHandlers = new HashMap<>();

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            xcb_generic_event_t evt = LibXcb.xcb_wait_for_event(connection);
            if (evt == null) { break; }
            handleGenericEvent(evt);
        }
    }

    private void handleGenericEvent(xcb_generic_event_t evt) {
        switch (evt.getResponse_type()) {
        case 0: // error
            xcb_generic_error_t error = cast(evt, xcb_generic_error_t::new);
            int errorCode = error.getError_code();
            System.err.println("X Error: " + errorCode + " " +
                               ERROR_MESSAGES.getOrDefault(errorCode, "Unknown"));
            break;
        case LibXcbConstants.XCB_EXPOSE:
            xcb_expose_event_t expose = cast(evt, xcb_expose_event_t::new);
            submitEvent(new WindowContext(expose.getWindow()), new ExposeEvent(
                    expose.getX(), expose.getY(),
                    expose.getWidth(), expose.getHeight()
            ));
            break;
        case LibXcbConstants.XCB_NO_EXPOSURE:
            break;
        default:
            System.out.println("Unhandled event " + evt.getResponse_type());
            break;
        }
    }

    private static <E> E cast(xcb_generic_event_t generic, BiFunction<Long, Boolean, E> constructor) {
        return constructor.apply(xcb_generic_event_t.getCPtr(generic), false);
    }

    @SuppressWarnings("unchecked")
    public void submitEvent(Context context, Object event) {
        Map<Context, List<Consumer<?>>> m = eventHandlers.get(event.getClass());
        if (m != null) {
            List<Consumer<?>> l = m.get(context);
            if (l != null) {
                for (Consumer handler : l) {
                    handler.accept(event);
                }
            }
        }
    }

    public <E> void addEventHandler(Class<E> type, Context context, Consumer<E> handler) {
        eventHandlers
                .computeIfAbsent(type, c -> new HashMap<>())
                .computeIfAbsent(context, c -> new ArrayList<>())
                .add(handler);
    }

    public void destroyContext(Context context) {
        eventHandlers.values().forEach(m -> m.keySet().remove(context));
    }

    interface Context {}

    @Value
    static class WindowContext implements Context {
        private final int window;
    }
}
