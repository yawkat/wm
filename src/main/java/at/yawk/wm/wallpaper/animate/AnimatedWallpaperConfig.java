package at.yawk.wm.wallpaper.animate;

import java.awt.*;
import java.nio.file.Path;
import lombok.Data;

/**
 * @author yawkat
 */
@Data
public class AnimatedWallpaperConfig {
    private Path cache;
    private Path input;
    private Color backgroundColor;
}
