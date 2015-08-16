package at.yawk.wm.dbus;

import at.yawk.dbus.client.annotation.*;

/**
 * @author yawkat
 */
@SystemBus
@Destination("org.freedesktop.NetworkManager")
@ObjectPath("/org/freedesktop/NetworkManager")
@Interface("org.freedesktop.NetworkManager")
public interface NetworkManager {
    @GetProperty
    @Member("Connectivity")
    int getConnectivity();

    @Member("StateChanged")
    @Listener
    void onStateChanged(Runnable listener);
}
