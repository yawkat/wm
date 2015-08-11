package at.yawk.wm.x.icon;

import at.yawk.wm.x.PixMapArea;
import java.awt.*;

/**
 * @author yawkat
 */
public interface Icon {
    PixMapArea colorize(Color foreground, Color background);

    int getWidth();

    int getHeight();
}
