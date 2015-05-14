package at.yawk.wm.x.event;

import lombok.Value;

/**
 * @author yawkat
 */
@Value
public class ExposeEvent {
    private int x;
    private int y;
    private int width;
    private int height;
}
