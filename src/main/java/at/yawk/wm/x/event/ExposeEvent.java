package at.yawk.wm.x.event;

/**
 * @author yawkat
 */
public class ExposeEvent {
    private int x;
    private int y;
    private int width;
    private int height;

    @java.beans.ConstructorProperties({ "x", "y", "width", "height" })
    public ExposeEvent(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
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
        if (!(o instanceof ExposeEvent)) { return false; }
        final ExposeEvent other = (ExposeEvent) o;
        if (this.x != other.x) { return false; }
        if (this.y != other.y) { return false; }
        if (this.width != other.width) { return false; }
        if (this.height != other.height) { return false; }
        return true;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.x;
        result = result * PRIME + this.y;
        result = result * PRIME + this.width;
        result = result * PRIME + this.height;
        return result;
    }

    public String toString() {
        return "at.yawk.wm.x.event.ExposeEvent(x=" + this.x + ", y=" + this.y + ", width=" + this.width + ", height=" +
               this.height + ")";
    }
}
