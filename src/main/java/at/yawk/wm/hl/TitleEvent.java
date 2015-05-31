package at.yawk.wm.hl;

import lombok.Value;

/**
 * @author yawkat
 */
@Value
public class TitleEvent {
    String title;

    interface Handler {
        void handle(TitleEvent event);
    }
}
