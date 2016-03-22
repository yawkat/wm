package at.yawk.wm.x;

import at.yawk.wm.x.event.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.freedesktop.xcb.xcb_event_mask_t;

/**
 * @author yawkat
 */
public enum EventGroup {
    PAINT(
            xcb_event_mask_t.XCB_EVENT_MASK_EXPOSURE,
            ExposeEvent.class
    ),
    MOUSE_PRESS(
            xcb_event_mask_t.XCB_EVENT_MASK_BUTTON_PRESS |
            xcb_event_mask_t.XCB_EVENT_MASK_BUTTON_RELEASE,
            ButtonPressEvent.class,
            ButtonReleaseEvent.class
    ),
    MOUSE_MOTION(
            xcb_event_mask_t.XCB_EVENT_MASK_BUTTON_1_MOTION |
            xcb_event_mask_t.XCB_EVENT_MASK_BUTTON_2_MOTION |
            xcb_event_mask_t.XCB_EVENT_MASK_BUTTON_3_MOTION |
            xcb_event_mask_t.XCB_EVENT_MASK_BUTTON_4_MOTION |
            xcb_event_mask_t.XCB_EVENT_MASK_BUTTON_5_MOTION,
            MouseMoveEvent.class
    ),
    KEYBOARD(
            xcb_event_mask_t.XCB_EVENT_MASK_KEY_PRESS,
            KeyPressEvent.class
    ),
    FOCUS(
            xcb_event_mask_t.XCB_EVENT_MASK_FOCUS_CHANGE,
            FocusLostEvent.class
    ),
    PROPERTY(
            xcb_event_mask_t.XCB_EVENT_MASK_PROPERTY_CHANGE
    );

    private final int mask;
    private final Set<Class<?>> eventClasses;

    EventGroup(int mask, Class<?>... eventClasses) {
        this.mask = mask;
        this.eventClasses = new HashSet<>(Arrays.asList(eventClasses));
    }

    public static int getMask(Iterable<EventGroup> groups) {
        int mask = 0;
        for (EventGroup group : groups) {
            mask |= group.mask;
        }
        return mask;
    }

    public int getMask() {
        return this.mask;
    }

    public Set<Class<?>> getEventClasses() {
        return this.eventClasses;
    }
}
