package at.yawk.wm.dbus;

/**
 * @author yawkat
 */
@Destination(value = "org.freedesktop.NetworkManager", system = true)
@ObjectPath("/org/freedesktop/NetworkManager")
@Interface("org.freedesktop.NetworkManager")
public interface NetworkManager {
    @DbusProperty("Connectivity")
    int getConnectivity();
}
