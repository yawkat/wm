package at.yawk.wm.wallpaper.animate;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import lombok.Data;

/**
 * @author yawkat
 */
@Data
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
}
