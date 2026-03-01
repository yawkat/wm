package at.yawk.wm.tac.launcher

object LauncherConfig {
    val shortcuts = mapOf(
        "firefox" to Command(listOf("/usr/bin/firefox")),
        "thunderbird" to Command(listOf("/usr/bin/thunderbird")),
        "quasselclient" to Command(listOf("/home/yawkat/dev/mirrors/quassel/build/quasselclient")),
        "subl3" to Command(listOf("/usr/bin/subl3")),
        "genpw16" to Command(listOf("/bin/bash", "-c", "< /dev/urandom tr -dc a-z0-9 | head -c\${1:-16} | xclip -selection c")),
        "genpw32" to Command(listOf("/bin/bash", "-c", "< /dev/urandom tr -dc a-z0-9 | head -c\${1:-32} | xclip -selection c")),
        "spotify" to Command(listOf("/usr/bin/spotify", "--force-device-scale-factor=1.0000001")),
    )
    val shutdownCommand = listOf("/usr/bin/systemctl", "poweroff")
}