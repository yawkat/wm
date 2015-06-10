package at.yawk.wm.x;

import java.nio.ByteBuffer;
import org.freedesktop.xcb.*;

/**
 * @author yawkat
 */
class KeyManager extends AbstractResource {
    private final SWIGTYPE_p_xkb_context context;
    private final SWIGTYPE_p_xkb_keymap keymap;
    private final SWIGTYPE_p_xkb_state state;

    KeyManager(XcbConnector connector) {
        ByteBuffer buf = ByteBuffer.allocateDirect(4);
        LibXcb.xkb_x11_setup_xkb_extension(
                connector.connection,
                LibXcbConstants.XCB_XKB_MAJOR_VERSION,
                LibXcbConstants.XCB_XKB_MINOR_VERSION,
                xkb_x11_setup_xkb_extension_flags.XKB_X11_SETUP_XKB_EXTENSION_NO_FLAGS,
                buf, buf, buf, buf
        );

        context = LibXcb.xkb_context_new(xkb_context_flags.XKB_CONTEXT_NO_FLAGS);
        int coreDeviceId = LibXcb.xkb_x11_get_core_keyboard_device_id(connector.connection);
        keymap = LibXcb.xkb_x11_keymap_new_from_device(
                context,
                connector.connection,
                coreDeviceId,
                xkb_keymap_compile_flags.XKB_KEYMAP_COMPILE_NO_FLAGS
        );
        state = LibXcb.xkb_x11_state_new_from_device(
                keymap,
                connector.connection,
                coreDeviceId
        );
    }

    void onEvent(xcb_generic_event_t evt) {
        switch (evt.getPad0()) {
        case LibXcbConstants.XCB_XKB_STATE_NOTIFY:
            xcb_xkb_state_notify_event_t notify = EventManager.cast(evt, xcb_xkb_state_notify_event_t::new);
            LibXcb.xkb_state_update_mask(
                    state,
                    notify.getMods(),
                    notify.getLatchedMods(),
                    notify.getLockedMods(),
                    0, 0, 0
            );
            break;
        }
    }

    void updateState(int mods) {
        LibXcb.xkb_state_update_mask(
                state, mods,
                0, 0, 0, 0, 0
        );
    }

    public char getKeyChar(int code) {
        int codePoint = LibXcb.xkb_state_key_get_utf32(state, code);
        char[] chars = Character.toChars(codePoint);
        return chars.length == 1 ? chars[0] : 0;
    }

    public int getKeySym(int code) {
        return LibXcb.xkb_state_key_get_one_sym(state, code);
    }

    @Override
    public void close() {
        LibXcb.xkb_context_unref(context);
        LibXcb.xkb_keymap_unref(keymap);
        LibXcb.xkb_state_unref(state);
    }
}
