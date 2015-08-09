package at.yawk.wm.style;

import java.awt.*;
import lombok.Data;

/**
 * @author yawkat
 */
@Data
public class FontTransition {
    private static final int PRECISION = 20;

    private FontDescriptor low;
    private FontDescriptor high;

    FontStyle computeStyle(FontManager fontManager, float value) {
        FontStyle lowStyle = fontManager.resolve(low);
        FontStyle highStyle = fontManager.resolve(high);

        return lowStyle.withColor(shade(lowStyle.getForeground(), highStyle.getForeground(), value));
    }

    private static Color shade(Color low, Color high, float value) {
        float m2 = (float) Math.round(value * PRECISION) / PRECISION;

        if (m2 <= 0) { return low; }
        if (m2 >= 1) { return high; }

        float[] hsbLow = toHSB(low);
        float[] hsbHigh = toHSB(high);

        float m1 = 1 - m2;
        int rgb = Color.HSBtoRGB(
                hsbLow[0] * m1 + hsbHigh[0] * m2,
                hsbLow[1] * m1 + hsbHigh[1] * m2,
                hsbLow[2] * m1 + hsbHigh[2] * m2
        );
        return new Color(rgb);
    }

    private static float[] toHSB(Color color) {
        return Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
    }
}
