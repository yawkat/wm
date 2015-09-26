package at.yawk.wm.dock.module;

import at.yawk.wm.plot.WeatherPlot;
import at.yawk.wm.style.FontDescriptor;
import at.yawk.wm.style.FontTransition;
import at.yawk.wm.x.icon.IconDescriptor;
import java.awt.*;
import java.util.Map;
import lombok.Data;

/**
 * @author yawkat
 */
@Data
public class DockConfig {
    private Color background;

    private FontDescriptor time;

    private FontDescriptor batteryTime;
    private FontTransition batteryTransition;
    private Map<Float, IconDescriptor> chargingIcons;
    private Map<Float, IconDescriptor> dischargingIcons;

    private FontTransition cpuTransition;
    private IconDescriptor cpuIcon;
    private FontTransition memoryTransition;
    private FontTransition swapTransition;

    private String clockFormat;
    private FontDescriptor clockFont;

    private FontDescriptor netUpFont;
    private FontDescriptor netDownFont;
    private FontDescriptor netIconFont;
    private IconDescriptor netIconOnline;
    private IconDescriptor netIconOffline;

    private FontDescriptor activeFont;
    private FontDescriptor runningFont;
    private FontDescriptor emptyFont;

    private FontDescriptor windowTitleFont;

    private Color progressColor;

    private FontDescriptor mediaFont;
    private IconDescriptor mediaPlaying;
    private IconDescriptor mediaPaused;

    private int height;
}
