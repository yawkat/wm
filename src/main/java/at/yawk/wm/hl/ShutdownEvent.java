package at.yawk.wm.hl;

/**
 * @author yawkat
 */
public class ShutdownEvent {
    interface Handler {
        void handle(ShutdownEvent event);
    }
}
