package at.yawk.wm.dbus;

import at.yawk.dbus.client.annotation.*;

/**
 * @author yawkat
 */
@SessionBus
@Destination("org.mpris.MediaPlayer2.spotify")
@ObjectPath("/org/mpris/MediaPlayer2")
@Interface("org.mpris.MediaPlayer2.Player")
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
    @Member("PlaybackStatus")
    String getPlaybackStatus();

    @Interface("org.freedesktop.DBus.Properties")
    @Member("PropertiesChanged")
    @Listener
    void onPropertiesChanged(Runnable listener);
}
