package at.yawk.wm.dock.module;

import at.yawk.wm.style.FontStyle;
import at.yawk.wm.x.font.GlyphFont;

public interface FontSource {
    GlyphFont getFont(FontStyle style);
}
