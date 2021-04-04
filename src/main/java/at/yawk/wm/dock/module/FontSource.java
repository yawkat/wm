package at.yawk.wm.dock.module;

import at.yawk.wm.style.FontStyle;
import at.yawk.wm.x.font.FontCache;
import at.yawk.wm.x.font.GlyphFont;
import com.google.inject.ImplementedBy;

/**
 * @author yawkat
 */
@ImplementedBy(FontCache.class)
public interface FontSource {
    GlyphFont getFont(FontStyle style);
}
