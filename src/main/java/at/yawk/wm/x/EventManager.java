package at.yawk.wm.x;

import at.yawk.wm.x.event.*;
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
        put(3, "Argument is not a window");
        put(4, "Argument is not a pixmap");
        put(12, "Argument is not a color map");
        put(13, "Argument is not a graphics context");
        put(16, "Expected different data length from arguments");
    }};

    private final XcbConnector connector;

    private final Map<Class<?>, Map<Context, List<Consumer<?>>>> eventHandlers = new HashMap<>();

    private int atomNetSystemTrayOpcode = 0;
    private short xkbEventCode;

    Throwable lastFlushStackTrace = null;

    @Override
    public void run() {
        atomNetSystemTrayOpcode = connector.internAtom("_NET_SYSTEM_TRAY_OPCODE");

        xcb_query_extension_reply_t extensionReply = LibXcb.xcb_get_extension_data(
                connector.connection, LibXcb.getXcb_xkb_id());
        if (extensionReply.getPresent() == 0) {
            throw new IllegalStateException("Could not load XKB extension");
        }
        xkbEventCode = extensionReply.getFirst_event();

        while (!Thread.interrupted()) {
            xcb_generic_event_t evt = LibXcb.xcb_wait_for_event(connector.connection);
            if (evt == null) { break; }
            handleGenericEvent(evt);
        }
    }

    private void handleGenericEvent(xcb_generic_event_t evt) {
        short type = evt.getResponse_type();
        switch (type) {
        case 0: // error
            xcb_generic_error_t error = cast(evt, xcb_generic_error_t::new);
            int errorCode = error.getError_code();
            System.err.println("X Error: " + errorCode + " " +
                               ERROR_MESSAGES.getOrDefault(errorCode, "Unknown"));
            if (lastFlushStackTrace != null) {
                System.err.println("Last flush:");
                lastFlushStackTrace.printStackTrace();
            }
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
        case LibXcbConstants.XCB_BUTTON_PRESS:
            xcb_button_press_event_t press = cast(evt, xcb_button_press_event_t::new);
            submitEvent(new WindowContext(press.getEvent()), new ButtonPressEvent(
                    press.getEvent_x(), press.getEvent_y(),
                    press.getDetail()
            ));
            break;
        case LibXcbConstants.XCB_KEY_PRESS:
            xcb_key_press_event_t keyPress = cast(evt, xcb_key_press_event_t::new);
            connector.keyManager.updateState(keyPress.getState());
            submitEvent(new WindowContext(keyPress.getEvent()), new KeyPressEvent(
                    keyPress.getEvent_x(), keyPress.getEvent_y(),
                    keyPress.getDetail(),
                    connector.keyManager.getKeySym(keyPress.getDetail()),
                    connector.keyManager.getKeyChar(keyPress.getDetail())
            ));
            break;
        case LibXcbConstants.XCB_FOCUS_IN:
            // ignored
            break;
        case LibXcbConstants.XCB_FOCUS_OUT:
            xcb_focus_in_event_t focusOut = cast(evt, xcb_focus_in_event_t::new);
            submitEvent(new WindowContext(focusOut.getEvent()), new FocusLostEvent());
            break;
        case LibXcbConstants.XCB_CLIENT_MESSAGE:
            xcb_client_message_event_t clientMessage = cast(evt, xcb_client_message_event_t::new);
            if (clientMessage.getType() == atomNetSystemTrayOpcode) {
                UCharArray arr = UCharArray.frompointer(clientMessage.getData().getData8());
                System.out.printf("Received DOCK: %02x\n", arr.getitem(0));
            }
            break;
        default:
            if (type == xkbEventCode) {
                // todo: this is never called, fix that
                connector.keyManager.onEvent(evt);
            } else {
                System.out.println("Unhandled event " + evt.getResponse_type());
            }
            break;
        }
    }

    static <E> E cast(xcb_generic_event_t generic, BiFunction<Long, Boolean, E> constructor) {
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
                    if (event instanceof Cancellable && ((Cancellable) event).isCancelled()) {
                        break;
                    }
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
