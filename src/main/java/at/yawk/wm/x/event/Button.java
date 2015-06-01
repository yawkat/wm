package at.yawk.wm.x.event;

import lombok.RequiredArgsConstructor;

/**
 * @author yawkat
 */
@RequiredArgsConstructor
public enum Button {
    LEFT(1),
    MIDDLE(2),
    RIGHT(3),
    SCROLL_UP(4),
    SCROLL_DOWN(5);

    final int id;
}
