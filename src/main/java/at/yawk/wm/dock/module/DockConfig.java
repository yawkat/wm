package at.yawk.wm.dock.module;

import at.yawk.wm.x.font.FontStyle;
import java.awt.*;
import lombok.Data;

/**
 * @author yawkat
 */
@Data
public class DockConfig {
    private Color background;

    private FontStyle time;

    private FontStyle batteryTime;
    private FontStyle batteryPercentage = new FontStyle(); // color is shaded
    private Color batteryPercentageColorLow;
    private Color batteryPercentageColorHigh;

    private int height;
}
