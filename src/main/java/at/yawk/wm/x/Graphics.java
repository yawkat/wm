package at.yawk.wm.x;

import at.yawk.wm.x.font.GlyphFont;
import at.yawk.wm.x.image.LocalImage;
import java.awt.*;

/**
 * @author yawkat
 */
public interface Graphics extends Resource {
    Graphics setFont(String font);

    Graphics setForegroundColor(Color color);

    Graphics setBackgroundColor(Color color);

    Graphics setFont(GlyphFont font);

    Graphics drawText(int x, int y, String text);

    Graphics fillRect(int x, int y, int width, int height);

    Graphics clearRect(int x, int y, int width, int height);

    Graphics drawPixMap(PixMapArea area, int x, int y);

    Graphics drawPixMap(PixMap pixMap, int srcX, int srcY, int destX, int destY, int width, int height);

    /**
     * Draw a local image onto this canvas.
     */
    Graphics putImage(int x, int y, LocalImage image);

    void flush();
}
