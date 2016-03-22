package at.yawk.wm.style

import java.nio.file.Path

/**
 * @author yawkat
 */
class StyleConfig(
        val fonts: Map<FontDescriptor, FontStyle>,
        val fontCacheDir: Path
)