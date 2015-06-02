package at.yawk.wm.tac;

import lombok.Value;
import lombok.experimental.Wither;

/**
 * @author yawkat
 */
@Value
@Wither
public class EntryState {
    String text;
    boolean lowPriority;
    boolean selected;
}
