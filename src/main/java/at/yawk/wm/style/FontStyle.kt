package at.yawk.wm.style

data class FontStyle(
    val family: String,
    val foreground: Color,
    val background: Color,
    val bold: Boolean,
    val italic: Boolean,
    val size: Int
) {
    fun getDescriptor(): String {
        val builder = StringBuilder(family)
        builder.append('-').append(java.lang.String.format("%06x", foreground.rgb))
        builder.append('-').append(java.lang.String.format("%06x", background.rgb))
        if (bold) {
            builder.append("-bold")
        }
        if (italic) {
            builder.append("-italic")
        }
        builder.append('-').append(size)
        return builder.toString()
    }
}