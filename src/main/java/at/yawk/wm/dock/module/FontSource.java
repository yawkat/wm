package at.yawk.wm.dock.module;

import at.yawk.wm.x.font.FontStyle;
import at.yawk.wm.x.font.GlyphFont;

/**
 * @author yawkat
 */
public interface FontSource {
    GlyphFont getFont(FontStyle style);
}
