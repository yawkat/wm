package at.yawk.wm.x;

import lombok.Value;

/**
 * @author yawkat
 */
@Value
class PixMapAreaImpl implements PixMapArea {
    private final PixMap pixMap;
    private final int x;
    private final int y;
    private final int width;
    private final int height;

    @Override
    public PixMapArea getArea(int x, int y, int width, int height) {
        return new PixMapAreaImpl(pixMap, this.x + x, this.y + y, width, height);
    }
}
