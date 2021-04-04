package at.yawk.wm.x.icon;

import at.yawk.wm.x.PixMapArea;
import java.awt.*;

/**
 * @author yawkat
 */
public interface LoadedIcon {
    PixMapArea colorize(Color foreground, Color background);

    int getWidth();

    int getHeight();
}
