package at.yawk.wm.wallpaper.animate;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * @author yawkat
 */
@Data
public class FrameAnimation {
    private long interval;
    private List<Frame> frames;

    public FrameAnimation() {}

    /**
     * Copy constructor.
     */
    public FrameAnimation(FrameAnimation original) {
        interval = original.interval;
        frames = new ArrayList<>(original.frames);
    }

    public void write(DataOutput output) throws IOException {
        output.writeLong(interval);
        output.writeInt(frames.size());
        for (Frame frame : frames) {
            frame.write(output);
        }
    }

    public static FrameAnimation read(DataInput input) throws IOException {
        FrameAnimation animation = new FrameAnimation();
        animation.setInterval(input.readLong());
        int frameCount = input.readInt();
        animation.setFrames(new ArrayList<>(frameCount));
        for (int i = 0; i < frameCount; i++) {
            animation.getFrames().add(Frame.read(input));
        }
        return animation;
    }
}
