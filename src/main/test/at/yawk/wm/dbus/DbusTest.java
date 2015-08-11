package at.yawk.wm.dbus;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author yawkat
 */
public class DbusTest {
    @Test(enabled = false)
    public void test() throws Exception {
        MediaPlayer mediaPlayer = new Dbus().mediaPlayer();
        System.out.println(mediaPlayer.getLoopStatus());
        System.out.println(mediaPlayer.getPlaybackStatus());
    }
}