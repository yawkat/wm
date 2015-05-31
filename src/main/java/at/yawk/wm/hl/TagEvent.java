package at.yawk.wm.hl;

/**
 * @author yawkat
 */
public class TagEvent {
    public interface Handler {
        void handle(TagEvent event);
    }
}
