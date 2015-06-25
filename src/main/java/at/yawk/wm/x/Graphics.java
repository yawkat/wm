package at.yawk.wm.x;

import at.yawk.wm.x.font.GlyphFont;
import java.awt.*;
import org.freedesktop.xcb.SWIGTYPE_p_xcb_connection_t;

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

    Graphics drawPixMap(PixMap pixMap, int srcX, int srcY, int destX, int destY, int width, int height);

    /**
     * @see XUtil#putImage(SWIGTYPE_p_xcb_connection_t, int, int, short, int, int, int, int, byte[], int, int)
     */
    Graphics putImage(int x, int y, int width, int height, byte[] data, int offset, int pixelOffset);

    void flush();
}
