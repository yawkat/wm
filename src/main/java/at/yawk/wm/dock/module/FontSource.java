package at.yawk.wm.dock.module;

import at.yawk.wm.style.FontDescriptor;
import at.yawk.wm.x.font.GlyphFont;

/**
 * @author yawkat
 */
public interface FontSource {
    GlyphFont getFont(FontDescriptor descriptor);
}
