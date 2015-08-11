package at.yawk.wm.dbus;

/**
 * @author yawkat
 */
public enum Bus {
    USER(),
    SYSTEM("--system"),;

    final String[] flags;

    Bus(String... flags) {
        this.flags = flags;
    }
}
