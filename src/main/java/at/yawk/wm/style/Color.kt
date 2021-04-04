package at.yawk.wm.style

inline class Color(val rgb: Int) {
    val red: Int
        get() = (rgb ushr 16) and 0xff
    val green: Int
        get() = (rgb ushr 8) and 0xff
    val blue: Int
        get() = rgb and 0xff
    val awt: java.awt.Color
        get() = java.awt.Color(rgb)

    companion object {
        val white = Color(0xffffff)
    }

    object Solarized {
        val base03 = Color(0x002b36)
        val base02 = Color(0x073642)
        val base01 = Color(0x586e75)
        val base00 = Color(0x657b83)
        val base0 = Color(0x839496)
        val base1 = Color(0x93a1a1)
        val base2 = Color(0xeee8d5)
        val base3 = Color(0xfdf6e3)
        val yellow = Color(0xb58900)
        val orange = Color(0xcb4b16)
        val red = Color(0xdc322f)
        val magenta = Color(0xd33682)
        val violet = Color(0x6c71c4)
        val blue = Color(0x268bd2)
        val cyan = Color(0x2aa198)
        val green = Color(0x859900)
    }
}