package at.yawk.wm.x.icon

import java.nio.file.Path

/**
 * @author yawkat
 */
data class IconConfig(
        val cacheDir: Path,
        val icons: Map<IconDescriptor, Path>
)
