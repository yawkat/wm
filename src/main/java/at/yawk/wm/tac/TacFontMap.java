package at.yawk.wm.tac;

import at.yawk.wm.x.font.ConfiguredFont;
import at.yawk.wm.x.font.FontFactory;
import at.yawk.wm.x.font.FontStyle;
import at.yawk.wm.x.font.GlyphFont;
import java.awt.*;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.Value;

/**
 * @author yawkat
 */
@RequiredArgsConstructor
class TacFontMap {
    private final Map<Entry, GlyphFont> entries = new HashMap<>();
    private final Path cacheRoot;
    private final FontFactory font;

    public synchronized GlyphFont getFont(FontStyle style, Color background) {
        Entry entry = new Entry(style, background);
        return entries.computeIfAbsent(entry, e -> new GlyphFont(
                new ConfiguredFont(style, background, font), cacheRoot
        ));
    }

    @Value
    private static class Entry {
        FontStyle style;
        Color background;
    }
}
