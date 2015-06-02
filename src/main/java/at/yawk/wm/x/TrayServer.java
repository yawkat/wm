package at.yawk.wm.x;

import java.nio.ByteBuffer;
import org.freedesktop.xcb.LibXcb;

/**
 * @author yawkat
 */
public class TrayServer {
    private final XcbConnector connector;

    TrayServer(Window window) {
        connector = window.screen.connector;
        LibXcb.xcb_set_selection_owner(
                connector.connection,
                window.windowId,
                connector.internAtom("_NET_SYSTEM_TRAY_S"),
                (int) (System.currentTimeMillis() / 1000)
        );
        LibXcb.xcb_ewmh_send_client_message(
                connector.connection,
                window.screen.screen.getRoot(),
                window.screen.screen.getRoot(),
                connector.internAtom("MANAGER"),
                0, ByteBuffer.allocateDirect(0)
        );
    }
}
