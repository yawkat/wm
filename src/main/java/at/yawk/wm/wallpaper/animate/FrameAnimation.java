package at.yawk.wm.wallpaper.animate;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yawkat
 */
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

    public long getInterval() {
        return this.interval;
    }

    public List<Frame> getFrames() {
        return this.frames;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }

    public void setFrames(List<Frame> frames) {
        this.frames = frames;
    }

    public boolean equals(Object o) {
        if (o == this) { return true; }
        if (!(o instanceof FrameAnimation)) { return false; }
        final FrameAnimation other = (FrameAnimation) o;
        if (!other.canEqual((Object) this)) { return false; }
        if (this.interval != other.interval) { return false; }
        final Object this$frames = this.frames;
        final Object other$frames = other.frames;
        if (this$frames == null ? other$frames != null : !this$frames.equals(other$frames)) { return false; }
        return true;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final long $interval = this.interval;
        result = result * PRIME + (int) ($interval >>> 32 ^ $interval);
        final Object $frames = this.frames;
        result = result * PRIME + ($frames == null ? 0 : $frames.hashCode());
        return result;
    }

    protected boolean canEqual(Object other) {
        return other instanceof FrameAnimation;
    }

    public String toString() {
        return "at.yawk.wm.wallpaper.animate.FrameAnimation(interval=" + this.interval + ", frames=" + this.frames +
               ")";
    }
}
