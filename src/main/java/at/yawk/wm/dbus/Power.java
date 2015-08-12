package at.yawk.wm.dbus;

/**
 * @author yawkat
 */
@Destination("org.freedesktop.UPower")
@ObjectPath(value = "/org/freedesktop/UPower/devices/DisplayDevice", bus = Bus.SYSTEM)
@Interface("org.freedesktop.UPower.Device")
public interface Power {
    /**
     * http://upower.freedesktop.org/docs/Device.html#Device:State
     */
    @DbusProperty("State")
    int getState();

    /**
     * @return seconds or 0 if charging
     */
    @DbusProperty("TimeToEmpty")
    long getTimeToEmpty();

    /**
     * @return seconds or 0 if not charging
     */
    @DbusProperty("TimeToFull")
    long getTimeToFull();

    @DbusProperty("Percentage")
    double getPercentage();
}
