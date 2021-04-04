package at.yawk.wm.style

import java.nio.file.Path
import java.nio.file.Paths

/**
 * @author yawkat
 */
object StyleConfig {
    private val base = FontStyle(
        background = Color.Solarized.base03,
        foreground = Color.white,
        italic = false,
        bold = false,
        size = 14,
        family = StandardFontFactory("Source Code Pro")
    )
    val base03 = base.copy(foreground = Color.Solarized.base03)
    val base02 = base.copy(foreground = Color.Solarized.base02)
    val base01 = base.copy(foreground = Color.Solarized.base01)
    val base00 = base.copy(foreground = Color.Solarized.base00)
    val base0 = base.copy(foreground = Color.Solarized.base0)
    val base1 = base.copy(foreground = Color.Solarized.base1)
    val base2 = base.copy(foreground = Color.Solarized.base2)
    val base3 = base.copy(foreground = Color.Solarized.base3)

    val highlight = base2.copy(background = Color.Solarized.base02)
    val highlightLow = base2
    val red = base.copy(foreground = Color.Solarized.red)
    val green = base.copy(foreground = Color.Solarized.green)
    val yellow = base.copy(foreground = Color.Solarized.yellow)

    val tacPrimary = base1
    val tacSecondary = base01
    val tacPrimarySelected = tacPrimary.copy(background = Color.Solarized.base02)
    val tacSecondarySelected = tacSecondary.copy(background = Color.Solarized.base02)

    val dashboardMain = base01.copy(size = 50, family = StandardFontFactory("DejaVu Sans Light"))
    val dashboardMedia = dashboardMain.copy(size = 30)
    val dashboardXkcd = dashboardMain.copy(size = 16, family = StandardFontFactory("DejaVu Sans"))

    val fontCacheDir: Path = Paths.get(".cache/font/")
}