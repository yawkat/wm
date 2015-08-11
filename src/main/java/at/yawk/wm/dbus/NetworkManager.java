package at.yawk.wm.dbus;

/**
 * @author yawkat
 */
@Destination("org.freedesktop.NetworkManager")
@ObjectPath(value = "/org/freedesktop/NetworkManager", bus = Bus.SYSTEM)
@Interface("org.freedesktop.NetworkManager")
public interface NetworkManager {
    @DbusProperty("Connectivity")
    int getConnectivity();
}
