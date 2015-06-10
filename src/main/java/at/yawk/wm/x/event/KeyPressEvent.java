package at.yawk.wm.x.event;

import lombok.Value;

/**
 * @author yawkat
 */
@Value
public class KeyPressEvent {
    private int x;
    private int y;
    private int detail;
    private int symbol;
    private char keyChar;
}
