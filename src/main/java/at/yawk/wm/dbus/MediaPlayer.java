package at.yawk.wm.dbus;

/**
 * @author yawkat
 */
@Destination("org.mpris.MediaPlayer2.spotify")
@ObjectPath("/org/mpris/MediaPlayer2")
@Interface("org.mpris.MediaPlayer2.Player")
public interface MediaPlayer {
    @DbusMethod("PlayPause")
    void playPause();

    @DbusProperty("PlaybackStatus")
    String getPlaybackStatus();

    @DbusProperty("LoopStatus")
    String getLoopStatus();
}
