package at.yawk.wm.tac;

import at.yawk.wm.style.FontDescriptor;
import at.yawk.wm.style.FontStyle;
import java.awt.*;
import lombok.Data;

/**
 * @author yawkat
 */
@Data
public class TacConfig {
    private FontDescriptor fontPrimary;
    private FontDescriptor fontPrimarySelected;
    private FontDescriptor fontSecondary;
    private FontDescriptor fontSecondarySelected;

    private Color colorBackground;
    private Color colorSelected;

    private int width;
    private int rowHeight;
    private int padding;
}
