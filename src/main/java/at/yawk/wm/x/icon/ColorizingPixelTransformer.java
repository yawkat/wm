package at.yawk.wm.x.icon;

import at.yawk.wm.x.image.PixelTransformer;
import java.awt.*;

/**
 * {@link PixelTransformer} implementation that colors a greyscale image to a given foreground and background color.
 * White areas will take the foreground color, Dark areas the background color.
 *
 * @author yawkat
 */
class ColorizingPixelTransformer implements PixelTransformer {
    // bg and fg color values, 0x0 - 0xff
    int fgr;
    int fgg;
    int fgb;
    int bgr;
    int bgg;
    int bgb;

    public ColorizingPixelTransformer(Color foreground, Color background) {
        fgr = foreground.getRed();
        fgg = foreground.getBlue();
        fgb = foreground.getGreen();
        bgr = background.getRed();
        bgg = background.getBlue();
        bgb = background.getGreen();
    }

    @Override
    public int transform(int rgb) {
        byte r = blendColor(fgr, bgr, (byte) ((rgb >>> 16) & 0xff));
        byte g = blendColor(fgg, bgg, (byte) ((rgb >>> 8) & 0xff));
        byte b = blendColor(fgb, bgb, (byte) (rgb & 0xff));
        return ((r & 0xff) << 16) | ((b & 0xff) << 8) | (g & 0xff);
    }

    static byte blendColor(int fg, int bg, byte opacity) {
        int fgMask = opacity & 0xff;
        int bgMask = 0xff - fgMask;
        int newFg = fg * fgMask;
        int newBg = bg * bgMask;
        return (byte) ((newFg + newBg) / 0xff);
    }
}
