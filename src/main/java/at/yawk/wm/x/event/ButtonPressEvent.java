package at.yawk.wm.x.event;

import lombok.Value;

/**
 * @author yawkat
 */
@Value
public class ButtonPressEvent {
    private int x;
    private int y;
    private int detail;

    public boolean contains(Button button) {
        return detail == button.id;
    }
}
