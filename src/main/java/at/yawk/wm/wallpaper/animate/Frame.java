package at.yawk.wm.wallpaper.animate;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * @author yawkat
 */
public class Frame {
    private int x;
    private int y;
    private int width;
    private int height;
    /**
     * RGB array of frame data. Always len = 3*width*height.
     */
    private byte[] data;

    public Frame() {}

    /**
     * Copy constructor.
     */
    public Frame(Frame original) {
        x = original.x;
        y = original.y;
        width = original.width;
        height = original.height;
        data = original.data.clone();
    }

    public void write(DataOutput output) throws IOException {
        output.writeInt(x);
        output.writeInt(y);
        output.writeInt(width);
        output.writeInt(height);
        output.write(data);
    }

    public static Frame read(DataInput input) throws IOException {
        Frame frame = new Frame();
        frame.setX(input.readInt());
        frame.setY(input.readInt());
        frame.setWidth(input.readInt());
        frame.setHeight(input.readInt());
        byte[] data = new byte[3 * frame.width * frame.height];
        frame.setData(data);
        input.readFully(data);
        return frame;
    }

    public boolean isEmpty() {
        return width == 0 || height == 0;
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

    public byte[] getData() {
        return this.data;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public boolean equals(Object o) {
        if (o == this) { return true; }
        if (!(o instanceof Frame)) { return false; }
        final Frame other = (Frame) o;
        if (!other.canEqual((Object) this)) { return false; }
        if (this.x != other.x) { return false; }
        if (this.y != other.y) { return false; }
        if (this.width != other.width) { return false; }
        if (this.height != other.height) { return false; }
        if (!java.util.Arrays.equals(this.data, other.data)) { return false; }
        return true;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.x;
        result = result * PRIME + this.y;
        result = result * PRIME + this.width;
        result = result * PRIME + this.height;
        result = result * PRIME + java.util.Arrays.hashCode(this.data);
        return result;
    }

    protected boolean canEqual(Object other) {
        return other instanceof Frame;
    }

    public String toString() {
        return "at.yawk.wm.wallpaper.animate.Frame(x=" + this.x + ", y=" + this.y + ", width=" + this.width +
               ", height=" + this.height + ", data=" + java.util.Arrays.toString(this.data) + ")";
    }
}
