package at.yawk.wm.wallpaper.animate;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import lombok.Data;

/**
 * @author yawkat
 */
@Data
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
}
