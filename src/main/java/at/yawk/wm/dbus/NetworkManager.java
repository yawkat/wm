package at.yawk.wm.dbus;

import at.yawk.dbus.client.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * @author yawkat
 */
@SystemBus
@Destination("org.freedesktop.NetworkManager")
@ObjectPath("/org/freedesktop/NetworkManager")
@Interface("org.freedesktop.NetworkManager")
@Timeout(value = 1, unit = TimeUnit.SECONDS)
public interface NetworkManager {
    @GetProperty
    @Member("Connectivity")
    int getConnectivity();

    @Member("StateChanged")
    @Listener
    void onStateChanged(Runnable listener);
}
