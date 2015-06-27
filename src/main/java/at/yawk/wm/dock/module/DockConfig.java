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

    private FontStyle cpuFont = new FontStyle(); // color is shaded
    private Color cpuColorLow;
    private Color cpuColorHigh;

    private FontStyle memoryFont = new FontStyle(); // color is shaded
    private Color memoryColorLow;
    private Color memoryColorHigh;

    private FontStyle swapFont = new FontStyle(); // color is shaded
    private Color swapColorLow;
    private Color swapColorHigh;

    private String clockFormat;
    private FontStyle clockFont;

    private FontStyle netUpFont;
    private FontStyle netDownFont;

    private FontStyle activeFont;
    private FontStyle runningFont;
    private FontStyle emptyFont;

    private FontStyle windowTitleFont;

    private Color progressColor;

    private int height;
}
