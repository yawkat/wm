package at.yawk.wm.x;

import at.yawk.wm.x.font.GlyphFont;
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

    Graphics clearRect(int x, int y, int width, int height);

    Graphics drawPixMap(PixMap pixMap, int srcX, int srcY, int destX, int destY, int width, int height);

    void flush();
}
