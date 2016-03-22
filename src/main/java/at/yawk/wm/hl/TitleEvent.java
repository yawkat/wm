package at.yawk.wm.hl;

import lombok.Value;

/**
 * @author yawkat
 */
@Value
public class TitleEvent {
    String title;

    public interface Handler {
        void handle(TitleEvent event);
    }
}
