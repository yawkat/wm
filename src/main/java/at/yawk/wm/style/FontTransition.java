package at.yawk.wm.style;

import java.awt.*;

/**
 * @author yawkat
 */
public class FontTransition {
    private static final int PRECISION = 20;

    private FontDescriptor low;
    private FontDescriptor high;

    public FontTransition() {
    }

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

    public FontDescriptor getLow() {
        return this.low;
    }

    public FontDescriptor getHigh() {
        return this.high;
    }

    public void setLow(FontDescriptor low) {
        this.low = low;
    }

    public void setHigh(FontDescriptor high) {
        this.high = high;
    }

    public boolean equals(Object o) {
        if (o == this) { return true; }
        if (!(o instanceof FontTransition)) { return false; }
        final FontTransition other = (FontTransition) o;
        if (!other.canEqual((Object) this)) { return false; }
        final Object this$low = this.low;
        final Object other$low = other.low;
        if (this$low == null ? other$low != null : !this$low.equals(other$low)) { return false; }
        final Object this$high = this.high;
        final Object other$high = other.high;
        if (this$high == null ? other$high != null : !this$high.equals(other$high)) { return false; }
        return true;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $low = this.low;
        result = result * PRIME + ($low == null ? 0 : $low.hashCode());
        final Object $high = this.high;
        result = result * PRIME + ($high == null ? 0 : $high.hashCode());
        return result;
    }

    protected boolean canEqual(Object other) {
        return other instanceof FontTransition;
    }

    public String toString() {
        return "at.yawk.wm.style.FontTransition(low=" + this.low + ", high=" + this.high + ")";
    }
}
