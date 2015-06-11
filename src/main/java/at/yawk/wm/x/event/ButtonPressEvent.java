package at.yawk.wm.x.event;

import lombok.EqualsAndHashCode;
import lombok.Value;

/**
 * @author yawkat
 */
@EqualsAndHashCode(callSuper = false)
@Value
public class ButtonPressEvent extends AbstractCancellable {
    private int x;
    private int y;
    private int detail;

    public boolean contains(Button button) {
        return detail == button.id;
    }
}
