package at.yawk.wm.x.event;

import lombok.EqualsAndHashCode;
import lombok.Value;

/**
 * @author yawkat
 */
@EqualsAndHashCode(callSuper = false)
@Value
public class KeyPressEvent extends AbstractCancellable {
    private int x;
    private int y;
    private int detail;
    private int symbol;
    private char keyChar;
}
