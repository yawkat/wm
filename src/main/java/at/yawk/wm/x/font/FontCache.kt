package at.yawk.wm.x.font

import at.yawk.wm.dock.module.FontSource
import at.yawk.wm.style.FontStyle
import at.yawk.wm.style.StyleConfig
import java.util.HashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FontCache @Inject constructor() : FontSource {
    private val styleMap: MutableMap<FontStyle, GlyphFont> = HashMap<FontStyle, GlyphFont>()

    override fun getFont(style: FontStyle): GlyphFont {
        return styleMap.computeIfAbsent(style) { s: FontStyle? -> GlyphFont(s, StyleConfig.fontCacheDir) }
    }
}