package at.yawk.wm.dock.module;

import java.awt.*;

/**
 * @author yawkat
 */
public class Shader {
    private static final int PRECISION = 20;

    private Shader() {}

    public static Color shade(Color low, Color high, float value) {
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
