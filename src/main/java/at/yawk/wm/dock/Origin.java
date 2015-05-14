package at.yawk.wm.dock;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author yawkat
 */
@RequiredArgsConstructor
@Getter
public enum Origin {
    TOP_LEFT(1, 1),
    TOP_RIGHT(-1, 1),
    BOTTOM_LEFT(1, -1),
    BOTTOM_RIGHT(-1, -1);

    private final int widthMultiplier;
    private final int heightMultiplier;

    public boolean isLeft() {
        return this == TOP_LEFT |
               this == BOTTOM_LEFT;
    }

    public boolean isTop() {
        return this == TOP_LEFT |
               this == TOP_RIGHT;
    }
}
