package at.yawk.wm.x;

/**
 * @author yawkat
 */
class PixMapAreaImpl implements PixMapArea {
    private final PixMap pixMap;
    private final int x;
    private final int y;
    private final int width;
    private final int height;

    @java.beans.ConstructorProperties({ "pixMap", "x", "y", "width", "height" })
    public PixMapAreaImpl(PixMap pixMap, int x, int y, int width, int height) {
        this.pixMap = pixMap;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public PixMapArea getArea(int x, int y, int width, int height) {
        return new PixMapAreaImpl(pixMap, this.x + x, this.y + y, width, height);
    }

    public PixMap getPixMap() {
        return this.pixMap;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public boolean equals(Object o) {
        if (o == this) { return true; }
        if (!(o instanceof PixMapAreaImpl)) { return false; }
        final PixMapAreaImpl other = (PixMapAreaImpl) o;
        final Object this$pixMap = this.pixMap;
        final Object other$pixMap = other.pixMap;
        if (this$pixMap == null ? other$pixMap != null : !this$pixMap.equals(other$pixMap)) { return false; }
        if (this.x != other.x) { return false; }
        if (this.y != other.y) { return false; }
        if (this.width != other.width) { return false; }
        if (this.height != other.height) { return false; }
        return true;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $pixMap = this.pixMap;
        result = result * PRIME + ($pixMap == null ? 0 : $pixMap.hashCode());
        result = result * PRIME + this.x;
        result = result * PRIME + this.y;
        result = result * PRIME + this.width;
        result = result * PRIME + this.height;
        return result;
    }

    public String toString() {
        return "at.yawk.wm.x.PixMapAreaImpl(pixMap=" + this.pixMap + ", x=" + this.x + ", y=" + this.y + ", width=" +
               this.width + ", height=" + this.height + ")";
    }
}
