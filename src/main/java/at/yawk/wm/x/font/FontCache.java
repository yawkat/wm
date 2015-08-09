package at.yawk.wm.x.font;

import at.yawk.wm.Config;
import at.yawk.wm.dock.module.FontSource;
import at.yawk.wm.style.FontDescriptor;
import at.yawk.wm.style.FontManager;
import at.yawk.wm.style.FontStyle;
import at.yawk.yarn.Component;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;

/**
 * @author yawkat
 */
@Component
public class FontCache implements FontSource {
    private final Map<FontDescriptor, GlyphFont> descriptorMap = new HashMap<>();
    private final Map<FontStyle, GlyphFont> styleMap = new HashMap<>();

    @Inject Config config;
    @Inject FontManager fontManager;

    @Override
    public GlyphFont getFont(FontDescriptor descriptor) {
        return descriptorMap.computeIfAbsent(descriptor, d -> getFont(fontManager.resolve(d)));
    }

    private GlyphFont getFont(FontStyle style) {
        return styleMap.computeIfAbsent(style, s -> new GlyphFont(s, config.getFontCacheDir()));
    }
}
