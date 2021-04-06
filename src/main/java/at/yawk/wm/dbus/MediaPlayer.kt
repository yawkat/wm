package at.yawk.wm.dbus;

import at.yawk.dbus.client.annotation.*;
import at.yawk.dbus.protocol.object.DbusObject;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author yawkat
 */
@SessionBus
@Destination("org.mpris.MediaPlayer2.spotify")
@ObjectPath("/org/mpris/MediaPlayer2")
@Interface("org.mpris.MediaPlayer2.Player")
@Timeout(value = 1, unit = TimeUnit.SECONDS)
public interface MediaPlayer {
    @Call
    @Member("PlayPause")
    void playPause();

    @Call
    @Member("Stop")
    void stop();

    @Call
    @Member("Previous")
    void previous();

    @Call
    @Member("Next")
    void next();

    @GetProperty
    @Member("Metadata")
    Map<String, DbusObject> getMetadata();

    @Interface("org.freedesktop.DBus.Properties")
    @Member("PropertiesChanged")
    @Listener
    void onPropertiesChanged(Runnable listener);
}
