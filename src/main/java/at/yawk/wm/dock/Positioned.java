package at.yawk.wm.dock;

/**
 * @author yawkat
 */
public interface Positioned {
    Origin getOrigin();

    int getX();

    int getY();

    int getWidth();

    int getHeight();

    default int getX2() {
        return getX() + getOrigin().getWidthMultiplier() * getWidth();
    }

    default int getY2() {
        return getY() + getOrigin().getHeightMultiplier() * getHeight();
    }

    void addGeometryListener(Runnable listener);
}
