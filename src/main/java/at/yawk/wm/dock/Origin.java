package at.yawk.wm.dock;

/**
 * @author yawkat
 */
public enum Origin {
    TOP_LEFT(1, 1),
    TOP_RIGHT(-1, 1),
    BOTTOM_LEFT(1, -1),
    BOTTOM_RIGHT(-1, -1);

    private final int widthMultiplier;
    private final int heightMultiplier;

    @java.beans.ConstructorProperties({ "widthMultiplier", "heightMultiplier" })
    private Origin(int widthMultiplier, int heightMultiplier) {
        this.widthMultiplier = widthMultiplier;
        this.heightMultiplier = heightMultiplier;
    }

    public boolean isLeft() {
        return this == TOP_LEFT |
               this == BOTTOM_LEFT;
    }

    public boolean isTop() {
        return this == TOP_LEFT |
               this == TOP_RIGHT;
    }

    public int getWidthMultiplier() {
        return this.widthMultiplier;
    }

    public int getHeightMultiplier() {
        return this.heightMultiplier;
    }
}
