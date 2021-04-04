package at.yawk.wm.tac.launcher

object LauncherConfig {
    val shortcuts = mapOf(
        "firefox" to Command(listOf("firefox")),
        "thunderbird" to Command(listOf("thunderbird")),
        "quasselclient" to Command(listOf("/home/yawkat/dev/mirrors/quassel/build/quasselclient")),
        "subl3" to Command(listOf("subl3")),
        "genpw16" to Command(listOf("bash", "-c", "< /dev/urandom tr -dc a-z0-9 | head -c\${1:-16} | xclip -selection c")),
        "genpw16" to Command(listOf("bash", "-c", "< /dev/urandom tr -dc a-z0-9 | head -c\${1:-32} | xclip -selection c")),
        "spotify" to Command(listOf("spotify", "--force-device-scale-factor=1.0000001")),
    )
    val shutdownCommand = listOf("systemctl", "poweroff")
}