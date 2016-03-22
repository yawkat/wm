package at.yawk.wm.x.event;

/**
 * @author yawkat
 */
public class MouseMoveEvent extends AbstractCancellable {
    private int x;
    private int y;
    private int detail;

    @java.beans.ConstructorProperties({ "x", "y", "detail" })
    public MouseMoveEvent(int x, int y, int detail) {
        this.x = x;
        this.y = y;
        this.detail = detail;
    }

    public boolean contains(Button button) {
        return detail == button.id;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getDetail() {
        return this.detail;
    }

    public String toString() {
        return "at.yawk.wm.x.event.MouseMoveEvent(x=" + this.x + ", y=" + this.y + ", detail=" + this.detail + ")";
    }

    public boolean equals(Object o) {
        if (o == this) { return true; }
        if (!(o instanceof MouseMoveEvent)) { return false; }
        final MouseMoveEvent other = (MouseMoveEvent) o;
        if (!other.canEqual((Object) this)) { return false; }
        if (this.getX() != other.getX()) { return false; }
        if (this.getY() != other.getY()) { return false; }
        if (this.getDetail() != other.getDetail()) { return false; }
        return true;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.getX();
        result = result * PRIME + this.getY();
        result = result * PRIME + this.getDetail();
        return result;
    }

    protected boolean canEqual(Object other) {
        return other instanceof MouseMoveEvent;
    }
}
