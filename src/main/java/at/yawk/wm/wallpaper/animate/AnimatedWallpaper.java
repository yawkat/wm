package at.yawk.wm.wallpaper.animate;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * @author yawkat
 */
public class AnimatedWallpaper {
    private Frame baseFrame;
    private FrameAnimation start;
    private FrameAnimation stop;

    public AnimatedWallpaper() {}

    /**
     * Copy constructor. Used by the animator for freeing unneeded frames.
     */
    public AnimatedWallpaper(AnimatedWallpaper original) {
        baseFrame = original.baseFrame;
        start = new FrameAnimation(original.start);
        stop = new FrameAnimation(original.stop);
    }

    public void write(DataOutput output) throws IOException {
        baseFrame.write(output);
        start.write(output);
        stop.write(output);
    }

    public static AnimatedWallpaper read(DataInput input) throws IOException {
        AnimatedWallpaper wallpaper = new AnimatedWallpaper();
        wallpaper.setBaseFrame(Frame.read(input));
        wallpaper.setStart(FrameAnimation.read(input));
        wallpaper.setStop(FrameAnimation.read(input));
        return wallpaper;
    }

    public Frame getBaseFrame() {
        return this.baseFrame;
    }

    public FrameAnimation getStart() {
        return this.start;
    }

    public FrameAnimation getStop() {
        return this.stop;
    }

    public void setBaseFrame(Frame baseFrame) {
        this.baseFrame = baseFrame;
    }

    public void setStart(FrameAnimation start) {
        this.start = start;
    }

    public void setStop(FrameAnimation stop) {
        this.stop = stop;
    }

    public boolean equals(Object o) {
        if (o == this) { return true; }
        if (!(o instanceof AnimatedWallpaper)) { return false; }
        final AnimatedWallpaper other = (AnimatedWallpaper) o;
        if (!other.canEqual((Object) this)) { return false; }
        final Object this$baseFrame = this.baseFrame;
        final Object other$baseFrame = other.baseFrame;
        if (this$baseFrame == null ? other$baseFrame != null : !this$baseFrame.equals(other$baseFrame)) {
            return false;
        }
        final Object this$start = this.start;
        final Object other$start = other.start;
        if (this$start == null ? other$start != null : !this$start.equals(other$start)) { return false; }
        final Object this$stop = this.stop;
        final Object other$stop = other.stop;
        if (this$stop == null ? other$stop != null : !this$stop.equals(other$stop)) { return false; }
        return true;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $baseFrame = this.baseFrame;
        result = result * PRIME + ($baseFrame == null ? 0 : $baseFrame.hashCode());
        final Object $start = this.start;
        result = result * PRIME + ($start == null ? 0 : $start.hashCode());
        final Object $stop = this.stop;
        result = result * PRIME + ($stop == null ? 0 : $stop.hashCode());
        return result;
    }

    protected boolean canEqual(Object other) {
        return other instanceof AnimatedWallpaper;
    }

    public String toString() {
        return "at.yawk.wm.wallpaper.animate.AnimatedWallpaper(baseFrame=" + this.baseFrame + ", start=" + this.start +
               ", stop=" + this.stop + ")";
    }
}
