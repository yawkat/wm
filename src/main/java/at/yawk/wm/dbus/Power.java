package at.yawk.wm.dbus;

import at.yawk.dbus.client.annotation.*;

/**
 * @author yawkat
 */
@SystemBus
@Destination("org.freedesktop.UPower")
@ObjectPath("/org/freedesktop/UPower/devices/DisplayDevice")
@Interface("org.freedesktop.UPower.Device")
public interface Power {
    @Interface("org.freedesktop.DBus.Properties")
    @Member("PropertiesChanged")
    @Listener
    void onPropertiesChanged(Runnable listener);

    @Member("IsPresent")
    @GetProperty
    boolean isPresent();

    /**
     * http://upower.freedesktop.org/docs/Device.html#Device:State
     */
    @Member("State")
    @GetProperty
    int getState();

    /**
     * @return seconds or 0 if charging
     */
    @Member("TimeToEmpty")
    @GetProperty
    long getTimeToEmpty();

    /**
     * @return seconds or 0 if not charging
     */
    @Member("TimeToFull")
    @GetProperty
    long getTimeToFull();

    @Member("Percentage")
    @GetProperty
    double getPercentage();
}
