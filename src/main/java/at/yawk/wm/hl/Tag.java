package at.yawk.wm.hl;

import lombok.Data;

/**
 * @author yawkat
 */
@Data
public class Tag {
    String id;
    State state;

    public enum State {
        SELECTED,
        RUNNING,
        EMPTY
    }
}
