package at.yawk.wm.x;

import at.yawk.wm.x.font.GlyphFont;
import java.awt.*;

/**
 * @author yawkat
 */
public interface Graphics extends Resource {
    GraphicsImpl setFont(String font);

    GraphicsImpl setForegroundColor(Color color);

    GraphicsImpl setBackgroundColor(Color color);

    GraphicsImpl setFont(GlyphFont font);

    GraphicsImpl drawText(int x, int y, String text);

    GraphicsImpl clearRect(int x, int y, int width, int height);

    void flush();
}
