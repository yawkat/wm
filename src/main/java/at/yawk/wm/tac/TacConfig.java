package at.yawk.wm.tac;

import at.yawk.wm.x.font.FontStyle;
import java.awt.*;
import lombok.Data;

/**
 * @author yawkat
 */
@Data
public class TacConfig {
    private FontStyle fontNormal;
    private FontStyle fontLowPriority;

    private Color colorBackground;
    private Color colorSelected;

    private int width;
}
